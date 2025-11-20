package com.ControlCards.ControlCards.Controllers;

import com.ControlCards.ControlCards.Config.SecurityConfig;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Service.CardService;
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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import com.ControlCards.ControlCards.DTO.CardCreateDTO;
import com.ControlCards.ControlCards.DTO.CardExtendDTO;
import com.ControlCards.ControlCards.Util.Enums.Shift;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class CardControllerApiTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private WorkCenterService workCenterService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
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
    @WithMockUser(roles = "WORKER")
    void testShowCreateForm() throws Exception {
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/cards/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("worker-cards-new"))
                .andExpect(model().attributeExists("workshops"))
                .andExpect(model().attributeExists("workCenters"))
                .andExpect(model().attributeExists("shifts"))
                .andExpect(model().attributeExists("card"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void testListCreatedCards() throws Exception {
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));
        when(cardService.getCreatedCards(any(User.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/cards/created"))
                .andExpect(status().isOk())
                .andExpect(view().name("technician-cards"))
                .andExpect(model().attributeExists("cards"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
        verify(cardService, times(1)).getCreatedCards(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListAllCards() throws Exception {
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));
        when(cardService.getAllCards(any(User.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/cards/all"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-cards"))
                .andExpect(model().attributeExists("cards"))
                .andExpect(model().attribute("pageTitle", "Всички карти"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
        verify(cardService, times(1)).getAllCards(any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListClosedCards() throws Exception {
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));
        when(cardService.getClosedCards(any(User.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/cards/closed"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-cards"))
                .andExpect(model().attributeExists("cards"))
                .andExpect(model().attribute("pageTitle", "Затворени карти"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
        verify(cardService, times(1)).getClosedCards(any(User.class));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void testShowExtendFormCardExists() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(cardService.cardExists(cardId)).thenReturn(true);
        when(cardService.canExtendCard(eq(cardId), any(User.class))).thenReturn(true);
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/cards/extend/{id}", cardId))
                .andExpect(status().isOk())
                .andExpect(view().name("technician-cards-extend"))
                .andExpect(model().attributeExists("cardId"))
                .andExpect(model().attributeExists("cardExtendDTO"));

        verify(cardService, times(1)).cardExists(cardId);
        verify(cardService, times(1)).canExtendCard(eq(cardId), any(User.class));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void testShowExtendFormCardNotExists() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(cardService.cardExists(cardId)).thenReturn(false);

        mockMvc.perform(get("/cards/extend/{id}", cardId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/created"));

        verify(cardService, times(1)).cardExists(cardId);
        verify(cardService, never()).canExtendCard(any(), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void testAccessDeniedForNonWorker() throws Exception {
        mockMvc.perform(get("/cards/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateCardSuccess() throws Exception {
        CardCreateDTO cardCreateDTO = new CardCreateDTO();
        cardCreateDTO.setWorkshopId(UUID.randomUUID());
        cardCreateDTO.setWorkCenterId(UUID.randomUUID());
        cardCreateDTO.setShift(Shift.FIRST);
        cardCreateDTO.setShortDescription("Test Description");

        testUser.setWorkshops(new ArrayList<>());
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));
        doNothing().when(cardService).createCard(any(CardCreateDTO.class), any(User.class));

        mockMvc.perform(post("/cards/new")
                        .param("workshopId", cardCreateDTO.getWorkshopId().toString())
                        .param("workCenterId", cardCreateDTO.getWorkCenterId().toString())
                        .param("shift", "FIRST")
                        .param("shortDescription", "Test Description")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
        verify(cardService, times(1)).createCard(any(CardCreateDTO.class), any(User.class));
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void testExtendCardSuccess() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));
        doNothing().when(cardService).extendCard(eq(cardId), any(CardExtendDTO.class), any(User.class));

        mockMvc.perform(post("/cards/extend/{id}", cardId)
                        .param("detailedDescription", "Extended description")
                        .param("resolutionDurationMinutes", "60")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/created"));

        verify(userService, times(1)).findByUsername("user");
        verify(cardService, times(1)).extendCard(eq(cardId), any(CardExtendDTO.class), any(User.class));
    }

    @Test
    @WithMockUser(roles = "PRODUCTION_MANAGER")
    void testCloseCardSuccess() throws Exception {
        UUID cardId = UUID.randomUUID();
        testUser.setRole(Role.PRODUCTION_MANAGER);
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));
        doNothing().when(cardService).closeCard(eq(cardId), any(User.class));

        mockMvc.perform(post("/cards/close/{id}", cardId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/extended"));

        verify(userService, times(1)).findByUsername("user");
        verify(cardService, times(1)).closeCard(eq(cardId), any(User.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCloseCardByAdmin() throws Exception {
        UUID cardId = UUID.randomUUID();
        testUser.setRole(Role.ADMIN);
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));
        doNothing().when(cardService).closeCard(eq(cardId), any(User.class));

        mockMvc.perform(post("/cards/close/{id}", cardId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/extended"));

        verify(userService, times(1)).findByUsername("user");
        verify(cardService, times(1)).closeCard(eq(cardId), any(User.class));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void testListExtendedCards() throws Exception {
        testUser.setRole(Role.MANAGER);
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));
        when(cardService.getExtendedCards(any(User.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/cards/extended"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-cards"))
                .andExpect(model().attributeExists("cards"))
                .andExpect(model().attribute("pageTitle", "Разширени карти"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
        verify(cardService, times(1)).getExtendedCards(any(User.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testCreateCardWithValidationErrors() throws Exception {
        testUser.setWorkshops(new ArrayList<>());
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));

        mockMvc.perform(post("/cards/new")
                        .param("workshopId", "")
                        .param("workCenterId", "")
                        .param("shift", "")
                        .param("shortDescription", "")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("worker-cards-new"))
                .andExpect(model().attributeExists("card"))
                .andExpect(model().attributeExists("workshops"))
                .andExpect(model().attributeExists("workCenters"))
                .andExpect(model().attributeExists("shifts"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
        verify(cardService, never()).createCard(any(), any());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void testExtendCardWithValidationErrors() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(cardService.cardExists(cardId)).thenReturn(true);
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(cardService.canExtendCard(eq(cardId), any(User.class))).thenReturn(true);

        mockMvc.perform(post("/cards/extend/{id}", cardId)
                        .param("detailedDescription", "")
                        .param("resolutionDurationMinutes", "-1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("technician-cards-extend"))
                .andExpect(model().attributeExists("cardId"))
                .andExpect(model().attributeExists("cardExtendDTO"));

        verify(cardService, never()).extendCard(any(), any(), any());
    }

    @Test
    @WithMockUser(roles = "TECHNICIAN")
    void testShowExtendFormCannotExtend() throws Exception {
        UUID cardId = UUID.randomUUID();
        when(cardService.cardExists(cardId)).thenReturn(true);
        when(userService.findByUsername("user")).thenReturn(Optional.of(testUser));
        when(cardService.canExtendCard(eq(cardId), any(User.class))).thenReturn(false);

        mockMvc.perform(get("/cards/extend/{id}", cardId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/created"));

        verify(cardService, times(1)).cardExists(cardId);
        verify(cardService, times(1)).canExtendCard(eq(cardId), any(User.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testListWorkerCards() throws Exception {
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));
        when(cardService.getAllCards(any(User.class))).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/cards/view"))
                .andExpect(status().isOk())
                .andExpect(view().name("manager-cards"))
                .andExpect(model().attributeExists("cards"))
                .andExpect(model().attribute("pageTitle", "Преглед на карти"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
        verify(cardService, times(1)).getAllCards(any(User.class));
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testShowCreateFormWithWorkshops() throws Exception {
        Workshop workshop = new Workshop();
        workshop.setId(UUID.randomUUID());
        workshop.setName("Test Workshop");
        workshop.setActive(true);
        testUser.setWorkshops(Arrays.asList(workshop));

        WorkCenter workCenter = new WorkCenter();
        workCenter.setId(UUID.randomUUID());
        workCenter.setNumber("1001");
        workCenter.setActive(true);
        workCenter.setWorkshop(workshop);
        when(userService.findByUsernameWithWorkshops("user")).thenReturn(Optional.of(testUser));
        when(workCenterService.findByWorkshopIdWithWorkshop(workshop.getId()))
                .thenReturn(Arrays.asList(workCenter));

        mockMvc.perform(get("/cards/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("worker-cards-new"))
                .andExpect(model().attributeExists("workshops"))
                .andExpect(model().attributeExists("workCenters"))
                .andExpect(model().attributeExists("shifts"))
                .andExpect(model().attributeExists("card"));

        verify(userService, times(1)).findByUsernameWithWorkshops("user");
        verify(workCenterService, times(1)).findByWorkshopIdWithWorkshop(workshop.getId());
    }
}

