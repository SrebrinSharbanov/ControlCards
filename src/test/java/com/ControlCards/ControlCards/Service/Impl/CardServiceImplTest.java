package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.DTO.CardCreateDTO;
import com.ControlCards.ControlCards.DTO.CardExtendDTO;
import com.ControlCards.ControlCards.DTO.CardViewDTO;
import com.ControlCards.ControlCards.Exception.CardNotFoundException;
import com.ControlCards.ControlCards.Exception.InvalidCardStatusException;
import com.ControlCards.ControlCards.Exception.WorkshopNotFoundException;
import com.ControlCards.ControlCards.Exception.WorkCenterNotFoundException;
import com.ControlCards.ControlCards.Model.ArchivedCard;
import com.ControlCards.ControlCards.Model.Card;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Repository.CardRepository;
import com.ControlCards.ControlCards.Service.ArchivedCardService;
import com.ControlCards.ControlCards.Service.WorkCenterService;
import com.ControlCards.ControlCards.Service.WorkshopService;
import com.ControlCards.ControlCards.Util.Enums.CardStatus;
import com.ControlCards.ControlCards.Util.Enums.Role;
import com.ControlCards.ControlCards.Util.Enums.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private ArchivedCardService archivedCardService;

    @Mock
    private LogEntryService logEntryService;

    @Mock
    private WorkshopService workshopService;

    @Mock
    private WorkCenterService workCenterService;

    @InjectMocks
    private CardServiceImpl cardService;

    private User testUser;
    private User adminUser;
    private Workshop testWorkshop;
    private WorkCenter testWorkCenter;
    private Card testCard;
    private CardCreateDTO cardCreateDTO;
    private UUID testCardId;
    private UUID testWorkshopId;
    private UUID testWorkCenterId;

    @BeforeEach
    void setUp() {
        testCardId = UUID.randomUUID();
        testWorkshopId = UUID.randomUUID();
        testWorkCenterId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setRole(Role.TECHNICIAN);
        testUser.setActive(true);
        testUser.setWorkshops(new ArrayList<>());

        adminUser = new User();
        adminUser.setId(UUID.randomUUID());
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);

        testWorkshop = new Workshop();
        testWorkshop.setId(testWorkshopId);
        testWorkshop.setName("Test Workshop");
        testWorkshop.setActive(true);

        testWorkCenter = new WorkCenter();
        testWorkCenter.setId(testWorkCenterId);
        testWorkCenter.setNumber("1001");
        testWorkCenter.setDescription("Test Work Center");
        testWorkCenter.setActive(true);
        testWorkCenter.setWorkshop(testWorkshop);

        testCard = new Card();
        testCard.setId(testCardId);
        testCard.setShortDescription("Test Card");
        testCard.setShift(Shift.FIRST);
        testCard.setStatus(CardStatus.CREATED);
        testCard.setCreatedBy(testUser);
        testCard.setWorkshop(testWorkshop);
        testCard.setWorkCenter(testWorkCenter);
        testCard.setCreatedAt(LocalDateTime.now());

        cardCreateDTO = new CardCreateDTO();
        cardCreateDTO.setWorkshopId(testWorkshopId);
        cardCreateDTO.setWorkCenterId(testWorkCenterId);
        cardCreateDTO.setShift(Shift.FIRST);
        cardCreateDTO.setShortDescription("Test Description");
    }

    @Test
    void testCreateCard() {
        when(workshopService.findById(testWorkshopId)).thenReturn(Optional.of(testWorkshop));
        when(workCenterService.findById(testWorkCenterId)).thenReturn(Optional.of(testWorkCenter));
        when(cardRepository.save(any(Card.class))).thenAnswer(invocation -> {
            Card card = invocation.getArgument(0);
            card.setId(testCardId);
            return card;
        });
        when(logEntryService.createLog(any(User.class), anyString())).thenReturn(null);

        cardService.createCard(cardCreateDTO, testUser);

        verify(workshopService, times(1)).findById(testWorkshopId);
        verify(workCenterService, times(1)).findById(testWorkCenterId);
        verify(cardRepository, times(1)).save(any(Card.class));
        verify(logEntryService, times(1)).createLog(any(User.class), anyString());
    }

    @Test
    void testCreateCardWorkshopNotFound() {
        when(workshopService.findById(testWorkshopId)).thenReturn(Optional.empty());

        assertThrows(WorkshopNotFoundException.class, () -> 
            cardService.createCard(cardCreateDTO, testUser));
        
        verify(workshopService, times(1)).findById(testWorkshopId);
        verify(workCenterService, never()).findById(any());
        verify(cardRepository, never()).save(any());
    }

    @Test
    void testCreateCardWorkCenterNotFound() {
        when(workshopService.findById(testWorkshopId)).thenReturn(Optional.of(testWorkshop));
        when(workCenterService.findById(testWorkCenterId)).thenReturn(Optional.empty());

        assertThrows(WorkCenterNotFoundException.class, () -> 
            cardService.createCard(cardCreateDTO, testUser));
        
        verify(workshopService, times(1)).findById(testWorkshopId);
        verify(workCenterService, times(1)).findById(testWorkCenterId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void testGetCreatedCardsForAdmin() {
        when(cardRepository.findByStatus(CardStatus.CREATED)).thenReturn(Arrays.asList(testCard));

        List<CardViewDTO> result = cardService.getCreatedCards(adminUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository, times(1)).findByStatus(CardStatus.CREATED);
        verify(cardRepository, never()).findByStatusAndWorkshopIn(any(), any());
    }

    @Test
    void testGetCreatedCardsForTechnician() {
        testUser.setWorkshops(Arrays.asList(testWorkshop));
        when(cardRepository.findByStatusAndWorkshopIn(CardStatus.CREATED, Arrays.asList(testWorkshop)))
                .thenReturn(Arrays.asList(testCard));

        List<CardViewDTO> result = cardService.getCreatedCards(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository, never()).findByStatus(any());
        verify(cardRepository, times(1)).findByStatusAndWorkshopIn(CardStatus.CREATED, Arrays.asList(testWorkshop));
    }

    @Test
    void testGetCreatedCardsForUserWithNoWorkshops() {
        testUser.setWorkshops(new ArrayList<>());

        List<CardViewDTO> result = cardService.getCreatedCards(testUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(cardRepository, never()).findByStatus(any());
        verify(cardRepository, never()).findByStatusAndWorkshopIn(any(), any());
    }

    @Test
    void testGetExtendedCardsForAdmin() {
        testCard.setStatus(CardStatus.EXTENDED);
        when(cardRepository.findByStatus(CardStatus.EXTENDED)).thenReturn(Arrays.asList(testCard));

        List<CardViewDTO> result = cardService.getExtendedCards(adminUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository, times(1)).findByStatus(CardStatus.EXTENDED);
    }

    @Test
    void testGetAllCardsForAdmin() {
        when(cardRepository.findAll()).thenReturn(Arrays.asList(testCard));

        List<CardViewDTO> result = cardService.getAllCards(adminUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository, times(1)).findAll();
        verify(cardRepository, never()).findByWorkshopIn(any());
    }

    @Test
    void testGetAllCardsForTechnician() {
        testUser.setWorkshops(Arrays.asList(testWorkshop));
        when(cardRepository.findByWorkshopIn(Arrays.asList(testWorkshop))).thenReturn(Arrays.asList(testCard));

        List<CardViewDTO> result = cardService.getAllCards(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardRepository, never()).findAll();
        verify(cardRepository, times(1)).findByWorkshopIn(Arrays.asList(testWorkshop));
    }

    @Test
    void testExtendCard() {
        CardExtendDTO extendDTO = new CardExtendDTO();
        extendDTO.setDetailedDescription("Extended description");
        extendDTO.setResolutionDurationMinutes(60);

        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(logEntryService.createLog(any(User.class), anyString())).thenReturn(null);

        cardService.extendCard(testCardId, extendDTO, testUser);

        assertEquals(CardStatus.EXTENDED, testCard.getStatus());
        assertEquals("Extended description", testCard.getDetailedDescription());
        assertEquals(60, testCard.getResolutionDurationMinutes());
        verify(cardRepository, times(1)).findById(testCardId);
        verify(cardRepository, times(1)).save(testCard);
        verify(logEntryService, times(1)).createLog(any(User.class), anyString());
    }

    @Test
    void testExtendCardNotFound() {
        CardExtendDTO extendDTO = new CardExtendDTO();
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class, () -> 
            cardService.extendCard(testCardId, extendDTO, testUser));
        
        verify(cardRepository, times(1)).findById(testCardId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void testExtendCardInvalidStatus() {
        testCard.setStatus(CardStatus.EXTENDED);
        CardExtendDTO extendDTO = new CardExtendDTO();
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        assertThrows(InvalidCardStatusException.class, () -> 
            cardService.extendCard(testCardId, extendDTO, testUser));
        
        verify(cardRepository, times(1)).findById(testCardId);
        verify(cardRepository, never()).save(any());
    }

    @Test
    void testCloseCardByAdmin() {
        testCard.setStatus(CardStatus.CREATED);
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(archivedCardService.save(any(ArchivedCard.class))).thenReturn(null);
        doNothing().when(cardRepository).deleteById(testCardId);
        when(logEntryService.createLog(any(User.class), anyString())).thenReturn(null);

        cardService.closeCard(testCardId, adminUser);

        verify(cardRepository, times(1)).findById(testCardId);
        verify(cardRepository, times(1)).save(testCard);
        verify(archivedCardService, times(1)).save(any(ArchivedCard.class));
        verify(cardRepository, times(1)).deleteById(testCardId);
        verify(logEntryService, times(1)).createLog(any(User.class), anyString());
    }

    @Test
    void testCloseCardByTechnician() {
        testCard.setStatus(CardStatus.EXTENDED);
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(archivedCardService.save(any(ArchivedCard.class))).thenReturn(null);
        doNothing().when(cardRepository).deleteById(testCardId);
        when(logEntryService.createLog(any(User.class), anyString())).thenReturn(null);

        cardService.closeCard(testCardId, testUser);

        verify(cardRepository, times(1)).findById(testCardId);
        verify(cardRepository, times(1)).save(testCard);
        verify(archivedCardService, times(1)).save(any(ArchivedCard.class));
        verify(cardRepository, times(1)).deleteById(testCardId);
    }

    @Test
    void testCloseCardInvalidStatusForTechnician() {
        testCard.setStatus(CardStatus.CREATED);
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        assertThrows(InvalidCardStatusException.class, () -> 
            cardService.closeCard(testCardId, testUser));
        
        verify(cardRepository, times(1)).findById(testCardId);
        verify(cardRepository, never()).save(any());
        verify(archivedCardService, never()).save(any());
    }

    @Test
    void testCardExists() {
        when(cardRepository.existsById(testCardId)).thenReturn(true);

        boolean result = cardService.cardExists(testCardId);

        assertTrue(result);
        verify(cardRepository, times(1)).existsById(testCardId);
    }

    @Test
    void testCanExtendCard() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        boolean result = cardService.canExtendCard(testCardId, testUser);

        assertTrue(result);
        verify(cardRepository, times(1)).findById(testCardId);
    }

    @Test
    void testCanExtendCardNotFound() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        boolean result = cardService.canExtendCard(testCardId, testUser);

        assertFalse(result);
        verify(cardRepository, times(1)).findById(testCardId);
    }

    @Test
    void testCanExtendCardWrongStatus() {
        testCard.setStatus(CardStatus.EXTENDED);
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        boolean result = cardService.canExtendCard(testCardId, testUser);

        assertFalse(result);
        verify(cardRepository, times(1)).findById(testCardId);
    }

    @Test
    void testCanCloseCard() {
        testCard.setStatus(CardStatus.EXTENDED);
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        boolean result = cardService.canCloseCard(testCardId);

        assertTrue(result);
        verify(cardRepository, times(1)).findById(testCardId);
    }

    @Test
    void testCanCloseCardNotFound() {
        when(cardRepository.findById(testCardId)).thenReturn(Optional.empty());

        boolean result = cardService.canCloseCard(testCardId);

        assertFalse(result);
        verify(cardRepository, times(1)).findById(testCardId);
    }

    @Test
    void testCanCloseCardWrongStatus() {
        testCard.setStatus(CardStatus.CREATED);
        when(cardRepository.findById(testCardId)).thenReturn(Optional.of(testCard));

        boolean result = cardService.canCloseCard(testCardId);

        assertFalse(result);
        verify(cardRepository, times(1)).findById(testCardId);
    }

    @Test
    void testGetClosedCardsForAdmin() {
        ArchivedCard archivedCard = new ArchivedCard();
        archivedCard.setId(testCardId);
        archivedCard.setWorkshop(testWorkshop);
        when(archivedCardService.findAll()).thenReturn(Arrays.asList(archivedCard));

        List<CardViewDTO> result = cardService.getClosedCards(adminUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(archivedCardService, times(1)).findAll();
    }

    @Test
    void testGetClosedCardsForTechnician() {
        testUser.setWorkshops(Arrays.asList(testWorkshop));
        ArchivedCard archivedCard = new ArchivedCard();
        archivedCard.setId(testCardId);
        archivedCard.setWorkshop(testWorkshop);
        when(archivedCardService.findAll()).thenReturn(Arrays.asList(archivedCard));

        List<CardViewDTO> result = cardService.getClosedCards(testUser);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(archivedCardService, times(1)).findAll();
    }

    @Test
    void testGetClosedCardsForUserWithNoWorkshops() {
        testUser.setWorkshops(new ArrayList<>());
        when(archivedCardService.findAll()).thenReturn(Arrays.asList(new ArchivedCard()));

        List<CardViewDTO> result = cardService.getClosedCards(testUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(archivedCardService, times(1)).findAll();
    }
}

