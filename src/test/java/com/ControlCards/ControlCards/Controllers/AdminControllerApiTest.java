package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Service.Impl.LogEntryService;
import com.ControlCards.ControlCards.Service.UserService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import com.ControlCards.ControlCards.Service.WorkshopService;
import com.ControlCards.ControlCards.Util.Enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.ControlCards.ControlCards.Config.SecurityConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class AdminControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WorkCenterService workCenterService;

    @MockitoBean
    private WorkshopService workshopService;

    @MockitoBean
    private LogEntryService logEntryService;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.WORKER);
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setWorkshops(new ArrayList<>());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListUsers() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users"))
                .andExpect(model().attributeExists("users"));

        verify(userService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowCreateUserForm() throws Exception {
        when(workshopService.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/admin/users/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users-form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("allWorkshops"));

        verify(workshopService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowEditUserForm() throws Exception {
        when(userService.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.findByUsernameWithWorkshops("testuser")).thenReturn(Optional.of(testUser));
        when(workshopService.findAll()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/admin/users/edit/{id}", testUserId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-users-form"))
                .andExpect(model().attributeExists("user"))
                .andExpect(model().attributeExists("roles"))
                .andExpect(model().attributeExists("allWorkshops"));

        verify(userService, times(1)).findById(testUserId);
        verify(workshopService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowEditUserFormNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(userService.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/users/edit/{id}", nonExistentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).findById(nonExistentId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateUser() throws Exception {
        when(userService.existsById(testUserId)).thenReturn(true);
        when(userService.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(userService).deactivate(testUserId);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/users/deactivate/{id}", testUserId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).existsById(testUserId);
        verify(userService, times(1)).findById(testUserId);
        verify(userService, times(1)).deactivate(testUserId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateUser() throws Exception {
        testUser.setActive(false);
        when(userService.existsById(testUserId)).thenReturn(true);
        when(userService.findById(testUserId)).thenReturn(Optional.of(testUser));
        doNothing().when(userService).activate(testUserId);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/users/activate/{id}", testUserId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).existsById(testUserId);
        verify(userService, times(1)).findById(testUserId);
        verify(userService, times(1)).activate(testUserId);
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessDeniedForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());

        verify(userService, never()).findAll();
    }

    @Test
    void testUnauthenticatedAccess() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListWorkCenters() throws Exception {
        List<WorkCenter> workCenters = new ArrayList<>();
        when(workCenterService.findAll()).thenReturn(workCenters);

        mockMvc.perform(get("/admin/workcenters"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-workcenters"))
                .andExpect(model().attributeExists("workCenters"));

        verify(workCenterService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowCreateWorkCenterForm() throws Exception {
        when(workshopService.findAllActive()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/admin/workcenters/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-workcenters-form"))
                .andExpect(model().attributeExists("workCenter"))
                .andExpect(model().attributeExists("workshops"));

        verify(workshopService, times(1)).findAllActive();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateWorkCenter() throws Exception {
        UUID workshopId = UUID.randomUUID();
        Workshop workshop = new Workshop();
        workshop.setId(workshopId);
        when(workshopService.findById(workshopId)).thenReturn(Optional.of(workshop));
        when(workCenterService.save(any(WorkCenter.class))).thenReturn(new WorkCenter());
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/workcenters/new")
                        .param("number", "1001")
                        .param("description", "Test Work Center")
                        .param("workshopId", workshopId.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workcenters"));

        verify(workshopService, times(1)).findById(workshopId);
        verify(workCenterService, times(1)).save(any(WorkCenter.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowEditWorkCenterForm() throws Exception {
        UUID workCenterId = UUID.randomUUID();
        WorkCenter workCenter = new WorkCenter();
        workCenter.setId(workCenterId);
        when(workCenterService.findById(workCenterId)).thenReturn(Optional.of(workCenter));
        when(workshopService.findAllActive()).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/admin/workcenters/edit/{id}", workCenterId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-workcenters-form"))
                .andExpect(model().attributeExists("workCenter"))
                .andExpect(model().attributeExists("workshops"));

        verify(workCenterService, times(1)).findById(workCenterId);
        verify(workshopService, times(1)).findAllActive();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateWorkCenter() throws Exception {
        UUID workCenterId = UUID.randomUUID();
        WorkCenter workCenter = new WorkCenter();
        workCenter.setId(workCenterId);
        workCenter.setNumber("1001");
        when(workCenterService.existsById(workCenterId)).thenReturn(true);
        when(workCenterService.findById(workCenterId)).thenReturn(Optional.of(workCenter));
        doNothing().when(workCenterService).deactivate(workCenterId);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/workcenters/deactivate/{id}", workCenterId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workcenters"));

        verify(workCenterService, times(1)).existsById(workCenterId);
        verify(workCenterService, times(1)).deactivate(workCenterId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateWorkCenter() throws Exception {
        UUID workCenterId = UUID.randomUUID();
        WorkCenter workCenter = new WorkCenter();
        workCenter.setId(workCenterId);
        workCenter.setNumber("1001");
        when(workCenterService.existsById(workCenterId)).thenReturn(true);
        when(workCenterService.findById(workCenterId)).thenReturn(Optional.of(workCenter));
        doNothing().when(workCenterService).activate(workCenterId);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/workcenters/activate/{id}", workCenterId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workcenters"));

        verify(workCenterService, times(1)).existsById(workCenterId);
        verify(workCenterService, times(1)).activate(workCenterId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListWorkshops() throws Exception {
        List<Workshop> workshops = new ArrayList<>();
        when(workshopService.findAll()).thenReturn(workshops);

        mockMvc.perform(get("/admin/workshops"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-workshops"))
                .andExpect(model().attributeExists("workshops"));

        verify(workshopService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowCreateWorkshopForm() throws Exception {
        mockMvc.perform(get("/admin/workshops/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-workshops-form"))
                .andExpect(model().attributeExists("workshop"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateWorkshop() throws Exception {
        when(workshopService.save(any(Workshop.class))).thenReturn(new Workshop());
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/workshops/new")
                        .param("name", "Test Workshop")
                        .param("description", "Test Description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workshops"));

        verify(workshopService, times(1)).save(any(Workshop.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowEditWorkshopForm() throws Exception {
        UUID workshopId = UUID.randomUUID();
        Workshop workshop = new Workshop();
        workshop.setId(workshopId);
        when(workshopService.findById(workshopId)).thenReturn(Optional.of(workshop));

        mockMvc.perform(get("/admin/workshops/edit/{id}", workshopId))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-workshops-form"))
                .andExpect(model().attributeExists("workshop"));

        verify(workshopService, times(1)).findById(workshopId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeactivateWorkshop() throws Exception {
        UUID workshopId = UUID.randomUUID();
        Workshop workshop = new Workshop();
        workshop.setId(workshopId);
        workshop.setName("Test Workshop");
        when(workshopService.existsById(workshopId)).thenReturn(true);
        when(workshopService.findById(workshopId)).thenReturn(Optional.of(workshop));
        doNothing().when(workshopService).deactivate(workshopId);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/workshops/deactivate/{id}", workshopId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workshops"));

        verify(workshopService, times(1)).existsById(workshopId);
        verify(workshopService, times(1)).deactivate(workshopId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testActivateWorkshop() throws Exception {
        UUID workshopId = UUID.randomUUID();
        Workshop workshop = new Workshop();
        workshop.setId(workshopId);
        workshop.setName("Test Workshop");
        when(workshopService.existsById(workshopId)).thenReturn(true);
        when(workshopService.findById(workshopId)).thenReturn(Optional.of(workshop));
        doNothing().when(workshopService).activate(workshopId);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/workshops/activate/{id}", workshopId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workshops"));

        verify(workshopService, times(1)).existsById(workshopId);
        verify(workshopService, times(1)).activate(workshopId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListLogs() throws Exception {
        List<LogEntry> logs = new ArrayList<>();
        List<User> users = Arrays.asList(testUser);
        when(logEntryService.findAll()).thenReturn(logs);
        when(userService.findAll()).thenReturn(users);

        mockMvc.perform(get("/admin/logs"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-logs"))
                .andExpect(model().attributeExists("logs"))
                .andExpect(model().attributeExists("users"));

        verify(logEntryService, times(1)).findAll();
        verify(userService, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUserWithSelectAll() throws Exception {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");
        when(workshopService.findAll()).thenReturn(new ArrayList<>());
        when(userService.save(any(User.class))).thenReturn(newUser);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/users/new")
                        .param("username", "newuser")
                        .param("password", "password")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("role", "WORKER")
                        .param("selectAll", "true")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(workshopService, times(1)).findAll();
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateUserWithWorkshopIds() throws Exception {
        UUID workshopId1 = UUID.randomUUID();
        UUID workshopId2 = UUID.randomUUID();
        Workshop workshop1 = new Workshop();
        workshop1.setId(workshopId1);
        Workshop workshop2 = new Workshop();
        workshop2.setId(workshopId2);
        when(workshopService.findById(workshopId1)).thenReturn(Optional.of(workshop1));
        when(workshopService.findById(workshopId2)).thenReturn(Optional.of(workshop2));
        when(userService.save(any(User.class))).thenReturn(new User());
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/users/new")
                        .param("username", "newuser")
                        .param("password", "password")
                        .param("firstName", "New")
                        .param("lastName", "User")
                        .param("role", "WORKER")
                        .param("workshopIds", workshopId1.toString(), workshopId2.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(workshopService, times(1)).findById(workshopId1);
        verify(workshopService, times(1)).findById(workshopId2);
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser() throws Exception {
        when(userService.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userService.findByUsernameWithWorkshops(testUser.getUsername())).thenReturn(Optional.of(testUser));
        when(userService.save(any(User.class))).thenReturn(testUser);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/users/edit/{id}", testUserId)
                        .param("username", "updateduser")
                        .param("firstName", "Updated")
                        .param("lastName", "User")
                        .param("role", "TECHNICIAN")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));

        verify(userService, times(1)).findById(testUserId);
        verify(userService, times(1)).save(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateWorkCenter() throws Exception {
        UUID workCenterId = UUID.randomUUID();
        UUID workshopId = UUID.randomUUID();
        WorkCenter workCenter = new WorkCenter();
        workCenter.setId(workCenterId);
        workCenter.setNumber("1001");
        Workshop workshop = new Workshop();
        workshop.setId(workshopId);
        when(workCenterService.findById(workCenterId)).thenReturn(Optional.of(workCenter));
        when(workshopService.findById(workshopId)).thenReturn(Optional.of(workshop));
        when(workCenterService.save(any(WorkCenter.class))).thenReturn(workCenter);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/workcenters/edit/{id}", workCenterId)
                        .param("number", "2001")
                        .param("description", "Updated Description")
                        .param("workshopId", workshopId.toString())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workcenters"));

        verify(workCenterService, times(1)).findById(workCenterId);
        verify(workshopService, times(1)).findById(workshopId);
        verify(workCenterService, times(1)).save(any(WorkCenter.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateWorkshop() throws Exception {
        UUID workshopId = UUID.randomUUID();
        Workshop workshop = new Workshop();
        workshop.setId(workshopId);
        workshop.setName("Old Name");
        when(workshopService.findById(workshopId)).thenReturn(Optional.of(workshop));
        when(workshopService.save(any(Workshop.class))).thenReturn(workshop);
        when(userService.findByUsername(anyString())).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/admin/workshops/edit/{id}", workshopId)
                        .param("name", "New Name")
                        .param("description", "New Description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workshops"));

        verify(workshopService, times(1)).findById(workshopId);
        verify(workshopService, times(1)).save(any(Workshop.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowEditWorkCenterFormNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(workCenterService.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/workcenters/edit/{id}", nonExistentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workcenters"));

        verify(workCenterService, times(1)).findById(nonExistentId);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowEditWorkshopFormNotFound() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(workshopService.findById(nonExistentId)).thenReturn(Optional.empty());

        mockMvc.perform(get("/admin/workshops/edit/{id}", nonExistentId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/workshops"));

        verify(workshopService, times(1)).findById(nonExistentId);
    }
}

