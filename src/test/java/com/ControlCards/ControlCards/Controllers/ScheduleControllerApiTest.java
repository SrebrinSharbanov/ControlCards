package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.Client.WorkScheduleClient;
import com.ControlCards.ControlCards.Config.SecurityConfig;
import com.ControlCards.ControlCards.DTO.WorkScheduleDTO;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Service.Impl.LogEntryService;
import com.ControlCards.ControlCards.Service.UserService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import com.ControlCards.ControlCards.Util.Enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ScheduleController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class ScheduleControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkScheduleClient workScheduleClient;

    @MockitoBean
    private WorkCenterService workCenterService;

    @MockitoBean
    private LogEntryService logEntryService;

    @MockitoBean
    private UserService userService;

    private User testUser;
    private WorkCenter testWorkCenter;
    private WorkScheduleDTO testSchedule;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setRole(Role.ADMIN);
        testUser.setActive(true);

        testWorkCenter = new WorkCenter();
        testWorkCenter.setId(UUID.randomUUID());
        testWorkCenter.setNumber("1001");
        testWorkCenter.setDescription("Test Work Center");
        testWorkCenter.setActive(true);

        testSchedule = new WorkScheduleDTO();
        testSchedule.setId(UUID.randomUUID());
        testSchedule.setDate(LocalDate.now());
        testSchedule.setShift(1);
        testSchedule.setWorkCenter("1001");
    }

    @Test
    @WithMockUser
    void testShowSchedulesPage() throws Exception {
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(get("/schedules"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedules"))
                .andExpect(model().attributeExists("workCenters"));

        verify(workCenterService, times(1)).findAllActive();
    }

    @Test
    @WithMockUser
    void testSearchSchedulesWithoutFilters() throws Exception {
        when(workScheduleClient.getSchedules(null, null, null)).thenReturn(Arrays.asList(testSchedule));
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(get("/schedules/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedules"))
                .andExpect(model().attributeExists("schedules"))
                .andExpect(model().attributeExists("workCenters"));

        verify(workScheduleClient, times(1)).getSchedules(null, null, null);
    }

    @Test
    @WithMockUser
    void testSearchSchedulesWithWorkCenter() throws Exception {
        when(workCenterService.findById(testWorkCenter.getId())).thenReturn(Optional.of(testWorkCenter));
        when(workScheduleClient.getSchedules(null, null, "1001")).thenReturn(Arrays.asList(testSchedule));
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(get("/schedules/search")
                        .param("workCenterId", testWorkCenter.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("schedules"))
                .andExpect(model().attributeExists("schedules"));

        verify(workCenterService, times(1)).findById(testWorkCenter.getId());
        verify(workScheduleClient, times(1)).getSchedules(null, null, "1001");
    }

    @Test
    @WithMockUser
    void testSearchSchedulesWithDate() throws Exception {
        LocalDate testDate = LocalDate.now();
        when(workScheduleClient.getSchedules(testDate, null, null)).thenReturn(Arrays.asList(testSchedule));
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(get("/schedules/search")
                        .param("date", testDate.toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("schedules"))
                .andExpect(model().attributeExists("schedules"));

        verify(workScheduleClient, times(1)).getSchedules(testDate, null, null);
    }

    @Test
    @WithMockUser
    void testSearchSchedulesWithShift() throws Exception {
        when(workScheduleClient.getSchedules(null, 1, null)).thenReturn(Arrays.asList(testSchedule));
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(get("/schedules/search")
                        .param("shift", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedules"))
                .andExpect(model().attributeExists("schedules"));

        verify(workScheduleClient, times(1)).getSchedules(null, 1, null);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowCreateScheduleForm() throws Exception {
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(get("/schedules/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-form"))
                .andExpect(model().attributeExists("workCenters"))
                .andExpect(model().attributeExists("schedule"))
                .andExpect(model().attribute("isEdit", false));

        verify(workCenterService, times(1)).findAllActive();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateScheduleSuccess() throws Exception {
        when(workCenterService.findById(testWorkCenter.getId())).thenReturn(Optional.of(testWorkCenter));
        when(workScheduleClient.createSchedule(any(WorkScheduleDTO.class))).thenReturn(testSchedule);
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(logEntryService.createLog(any(User.class), anyString())).thenReturn(null);

        mockMvc.perform(post("/schedules/new")
                        .param("workCenterId", testWorkCenter.getId().toString())
                        .param("date", LocalDate.now().toString())
                        .param("shift", "1")
                        .param("productionOrder", "PO123")
                        .param("quantity", "100")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedules?success=created"));

        verify(workCenterService, times(1)).findById(testWorkCenter.getId());
        verify(workScheduleClient, times(1)).createSchedule(any(WorkScheduleDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateScheduleWithoutWorkCenter() throws Exception {
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(post("/schedules/new")
                        .param("date", LocalDate.now().toString())
                        .param("shift", "1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-form"))
                .andExpect(model().attributeExists("error"));

        verify(workScheduleClient, never()).createSchedule(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowEditScheduleForm() throws Exception {
        when(workScheduleClient.getScheduleById(testSchedule.getId())).thenReturn(testSchedule);
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(get("/schedules/edit/{id}", testSchedule.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-form"))
                .andExpect(model().attributeExists("workCenters"))
                .andExpect(model().attributeExists("schedule"))
                .andExpect(model().attribute("isEdit", true));

        verify(workScheduleClient, times(1)).getScheduleById(testSchedule.getId());
        verify(workCenterService, times(2)).findAllActive();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateScheduleSuccess() throws Exception {
        when(workCenterService.findById(testWorkCenter.getId())).thenReturn(Optional.of(testWorkCenter));
        when(workScheduleClient.updateSchedule(eq(testSchedule.getId()), any(WorkScheduleDTO.class))).thenReturn(testSchedule);
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(logEntryService.createLog(any(User.class), anyString())).thenReturn(null);

        mockMvc.perform(post("/schedules/edit/{id}", testSchedule.getId())
                        .param("workCenterId", testWorkCenter.getId().toString())
                        .param("date", LocalDate.now().toString())
                        .param("shift", "1")
                        .param("productionOrder", "PO123")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedules?success=updated"));

        verify(workCenterService, times(1)).findById(testWorkCenter.getId());
        verify(workScheduleClient, times(1)).updateSchedule(eq(testSchedule.getId()), any(WorkScheduleDTO.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteScheduleSuccess() throws Exception {
        when(workScheduleClient.getScheduleById(testSchedule.getId())).thenReturn(testSchedule);
        doNothing().when(workScheduleClient).deleteSchedule(testSchedule.getId());
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(logEntryService.createLog(any(User.class), anyString())).thenReturn(null);

        mockMvc.perform(post("/schedules/delete/{id}", testSchedule.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/schedules?success=deleted"));

        verify(workScheduleClient, times(1)).getScheduleById(testSchedule.getId());
        verify(workScheduleClient, times(1)).deleteSchedule(testSchedule.getId());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessDeniedForCreateSchedule() throws Exception {
        mockMvc.perform(get("/schedules/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateScheduleWithoutWorkCenterId() throws Exception {
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(post("/schedules/new")
                        .param("date", LocalDate.now().toString())
                        .param("shift", "1")
                        .param("productionOrder", "PO123")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("isEdit", false));

        verify(workCenterService, times(1)).findAllActive();
        verify(workScheduleClient, never()).createSchedule(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateScheduleWorkCenterNotFound() throws Exception {
        UUID nonExistentWorkCenterId = UUID.randomUUID();
        when(workCenterService.findById(nonExistentWorkCenterId)).thenReturn(Optional.empty());
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(post("/schedules/new")
                        .param("workCenterId", nonExistentWorkCenterId.toString())
                        .param("date", LocalDate.now().toString())
                        .param("shift", "1")
                        .param("productionOrder", "PO123")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"));

        verify(workCenterService, times(1)).findById(nonExistentWorkCenterId);
        verify(workScheduleClient, never()).createSchedule(any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testShowCreateScheduleFormWithError() throws Exception {
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(get("/schedules/new")
                        .param("error", "test-error"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedule-form"))
                .andExpect(model().attributeExists("error"))
                .andExpect(model().attribute("isEdit", false));

        verify(workCenterService, times(1)).findAllActive();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateScheduleWorkCenterNotFound() throws Exception {
        UUID nonExistentWorkCenterId = UUID.randomUUID();
        when(workCenterService.findById(nonExistentWorkCenterId)).thenReturn(Optional.empty());
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));

        mockMvc.perform(post("/schedules/edit/{id}", testSchedule.getId())
                        .param("workCenterId", nonExistentWorkCenterId.toString())
                        .param("date", LocalDate.now().toString())
                        .param("shift", "1")
                        .param("productionOrder", "PO123")
                        .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error"));

        verify(workCenterService, times(1)).findById(nonExistentWorkCenterId);
        verify(workScheduleClient, never()).updateSchedule(any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchSchedulesWithAllFilters() throws Exception {
        when(workScheduleClient.getSchedules(any(), any(), any())).thenReturn(Arrays.asList(testSchedule));
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));
        when(workCenterService.findById(testWorkCenter.getId())).thenReturn(Optional.of(testWorkCenter));

        mockMvc.perform(get("/schedules/search")
                        .param("workCenterId", testWorkCenter.getId().toString())
                        .param("date", LocalDate.now().toString())
                        .param("shift", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("schedules"))
                .andExpect(model().attributeExists("schedules"));

        verify(workScheduleClient, times(1)).getSchedules(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testSearchSchedulesByWorkCenterOnly() throws Exception {
        when(workScheduleClient.getSchedules(any(), any(), any())).thenReturn(Arrays.asList(testSchedule));
        when(workCenterService.findAllActive()).thenReturn(Arrays.asList(testWorkCenter));
        when(workCenterService.findById(testWorkCenter.getId())).thenReturn(Optional.of(testWorkCenter));

        mockMvc.perform(get("/schedules/search")
                        .param("workCenterId", testWorkCenter.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(view().name("schedules"))
                .andExpect(model().attributeExists("schedules"));

        verify(workScheduleClient, times(1)).getSchedules(any(), any(), any());
    }
}

