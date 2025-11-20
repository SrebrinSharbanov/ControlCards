package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.WorkCenter;
import com.ControlCards.ControlCards.Repository.WorkCenterRepository;
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
class WorkCenterServiceImplTest {

    @Mock
    private WorkCenterRepository workCenterRepository;

    @InjectMocks
    private WorkCenterServiceImpl workCenterService;

    private WorkCenter testWorkCenter;
    private UUID testWorkCenterId;
    private UUID testWorkshopId;

    @BeforeEach
    void setUp() {
        testWorkCenterId = UUID.randomUUID();
        testWorkshopId = UUID.randomUUID();

        testWorkCenter = new WorkCenter();
        testWorkCenter.setId(testWorkCenterId);
        testWorkCenter.setNumber("1001");
        testWorkCenter.setDescription("Test Work Center");
        testWorkCenter.setActive(true);
    }

    @Test
    void testFindAll() {
        List<WorkCenter> workCenters = Arrays.asList(testWorkCenter);
        when(workCenterRepository.findAll()).thenReturn(workCenters);

        List<WorkCenter> result = workCenterService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(workCenterRepository, times(1)).findAll();
    }

    @Test
    void testFindAllActive() {
        List<WorkCenter> allWorkCenters = Arrays.asList(testWorkCenter);
        when(workCenterRepository.findAll()).thenReturn(allWorkCenters);

        List<WorkCenter> result = workCenterService.findAllActive();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
        verify(workCenterRepository, times(1)).findAll();
    }

    @Test
    void testFindAllActiveFiltersInactive() {
        WorkCenter inactiveWorkCenter = new WorkCenter();
        inactiveWorkCenter.setId(UUID.randomUUID());
        inactiveWorkCenter.setNumber("1002");
        inactiveWorkCenter.setActive(false);

        List<WorkCenter> allWorkCenters = Arrays.asList(testWorkCenter, inactiveWorkCenter);
        when(workCenterRepository.findAll()).thenReturn(allWorkCenters);

        List<WorkCenter> result = workCenterService.findAllActive();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getActive());
        verify(workCenterRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(workCenterRepository.findById(testWorkCenterId)).thenReturn(Optional.of(testWorkCenter));

        Optional<WorkCenter> result = workCenterService.findById(testWorkCenterId);

        assertTrue(result.isPresent());
        assertEquals(testWorkCenter.getNumber(), result.get().getNumber());
        verify(workCenterRepository, times(1)).findById(testWorkCenterId);
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(workCenterRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<WorkCenter> result = workCenterService.findById(nonExistentId);

        assertFalse(result.isPresent());
        verify(workCenterRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testSave() {
        WorkCenter newWorkCenter = new WorkCenter();
        newWorkCenter.setNumber("1003");
        newWorkCenter.setDescription("New Work Center");
        newWorkCenter.setActive(true);

        when(workCenterRepository.save(any(WorkCenter.class))).thenReturn(newWorkCenter);

        WorkCenter result = workCenterService.save(newWorkCenter);

        assertNotNull(result);
        assertEquals("1003", result.getNumber());
        verify(workCenterRepository, times(1)).save(newWorkCenter);
    }

    @Test
    void testFindByWorkshopId() {
        List<WorkCenter> workCenters = Arrays.asList(testWorkCenter);
        when(workCenterRepository.findByWorkshopId(testWorkshopId)).thenReturn(workCenters);

        List<WorkCenter> result = workCenterService.findByWorkshopId(testWorkshopId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(workCenterRepository, times(1)).findByWorkshopId(testWorkshopId);
    }

    @Test
    void testFindByWorkshopIdWithWorkshop() {
        List<WorkCenter> workCenters = Arrays.asList(testWorkCenter);
        when(workCenterRepository.findWithWorkshopByWorkshopIdAndActiveTrue(testWorkshopId)).thenReturn(workCenters);

        List<WorkCenter> result = workCenterService.findByWorkshopIdWithWorkshop(testWorkshopId);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(workCenterRepository, times(1)).findWithWorkshopByWorkshopIdAndActiveTrue(testWorkshopId);
    }

    @Test
    void testDeactivate() {
        when(workCenterRepository.findById(testWorkCenterId)).thenReturn(Optional.of(testWorkCenter));
        when(workCenterRepository.save(any(WorkCenter.class))).thenReturn(testWorkCenter);

        workCenterService.deactivate(testWorkCenterId);

        assertFalse(testWorkCenter.getActive());
        verify(workCenterRepository, times(1)).findById(testWorkCenterId);
        verify(workCenterRepository, times(1)).save(testWorkCenter);
    }

    @Test
    void testDeactivateWorkCenterNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(workCenterRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> workCenterService.deactivate(nonExistentId));
        verify(workCenterRepository, times(1)).findById(nonExistentId);
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void testActivate() {
        testWorkCenter.setActive(false);
        when(workCenterRepository.findById(testWorkCenterId)).thenReturn(Optional.of(testWorkCenter));
        when(workCenterRepository.save(any(WorkCenter.class))).thenReturn(testWorkCenter);

        workCenterService.activate(testWorkCenterId);

        assertTrue(testWorkCenter.getActive());
        verify(workCenterRepository, times(1)).findById(testWorkCenterId);
        verify(workCenterRepository, times(1)).save(testWorkCenter);
    }

    @Test
    void testActivateWorkCenterNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(workCenterRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> workCenterService.activate(nonExistentId));
        verify(workCenterRepository, times(1)).findById(nonExistentId);
        verify(workCenterRepository, never()).save(any(WorkCenter.class));
    }

    @Test
    void testExistsById() {
        when(workCenterRepository.existsById(testWorkCenterId)).thenReturn(true);

        boolean result = workCenterService.existsById(testWorkCenterId);

        assertTrue(result);
        verify(workCenterRepository, times(1)).existsById(testWorkCenterId);
    }
}

