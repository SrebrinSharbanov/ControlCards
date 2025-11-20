package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.Workshop;
import com.ControlCards.ControlCards.Repository.WorkshopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkshopServiceImplTest {

    @Mock
    private WorkshopRepository workshopRepository;

    @InjectMocks
    private WorkshopServiceImpl workshopService;

    private Workshop testWorkshop;
    private UUID testWorkshopId;

    @BeforeEach
    void setUp() {
        testWorkshopId = UUID.randomUUID();

        testWorkshop = new Workshop();
        testWorkshop.setId(testWorkshopId);
        testWorkshop.setName("Test Workshop");
        testWorkshop.setDescription("Test Description");
        testWorkshop.setActive(true);
    }

    @Test
    void testFindAll() {
        List<Workshop> workshops = Arrays.asList(testWorkshop);
        when(workshopRepository.findAll()).thenReturn(workshops);

        List<Workshop> result = workshopService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(workshopRepository, times(1)).findAll();
    }

    @Test
    void testFindAllActive() {
        List<Workshop> activeWorkshops = Arrays.asList(testWorkshop);
        when(workshopRepository.findByActiveTrue()).thenReturn(activeWorkshops);

        List<Workshop> result = workshopService.findAllActive();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
        verify(workshopRepository, times(1)).findByActiveTrue();
    }

    @Test
    void testFindById() {
        when(workshopRepository.findById(testWorkshopId)).thenReturn(Optional.of(testWorkshop));

        Optional<Workshop> result = workshopService.findById(testWorkshopId);

        assertTrue(result.isPresent());
        assertEquals(testWorkshop.getName(), result.get().getName());
        verify(workshopRepository, times(1)).findById(testWorkshopId);
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(workshopRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Workshop> result = workshopService.findById(nonExistentId);

        assertFalse(result.isPresent());
        verify(workshopRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testSave() {
        Workshop newWorkshop = new Workshop();
        newWorkshop.setName("New Workshop");
        newWorkshop.setDescription("New Description");
        newWorkshop.setActive(true);

        when(workshopRepository.save(any(Workshop.class))).thenReturn(newWorkshop);

        Workshop result = workshopService.save(newWorkshop);

        assertNotNull(result);
        assertEquals("New Workshop", result.getName());
        verify(workshopRepository, times(1)).save(newWorkshop);
    }

    @Test
    void testDeactivate() {
        when(workshopRepository.findById(testWorkshopId)).thenReturn(Optional.of(testWorkshop));
        when(workshopRepository.save(any(Workshop.class))).thenReturn(testWorkshop);

        workshopService.deactivate(testWorkshopId);

        assertFalse(testWorkshop.getActive());
        verify(workshopRepository, times(1)).findById(testWorkshopId);
        verify(workshopRepository, times(1)).save(testWorkshop);
    }

    @Test
    void testDeactivateWorkshopNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(workshopRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> workshopService.deactivate(nonExistentId));
        verify(workshopRepository, times(1)).findById(nonExistentId);
        verify(workshopRepository, never()).save(any(Workshop.class));
    }

    @Test
    void testActivate() {
        testWorkshop.setActive(false);
        when(workshopRepository.findById(testWorkshopId)).thenReturn(Optional.of(testWorkshop));
        when(workshopRepository.save(any(Workshop.class))).thenReturn(testWorkshop);

        workshopService.activate(testWorkshopId);

        assertTrue(testWorkshop.getActive());
        verify(workshopRepository, times(1)).findById(testWorkshopId);
        verify(workshopRepository, times(1)).save(testWorkshop);
    }

    @Test
    void testActivateWorkshopNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(workshopRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> workshopService.activate(nonExistentId));
        verify(workshopRepository, times(1)).findById(nonExistentId);
        verify(workshopRepository, never()).save(any(Workshop.class));
    }

    @Test
    void testExistsById() {
        when(workshopRepository.existsById(testWorkshopId)).thenReturn(true);

        boolean result = workshopService.existsById(testWorkshopId);

        assertTrue(result);
        verify(workshopRepository, times(1)).existsById(testWorkshopId);
    }
}

