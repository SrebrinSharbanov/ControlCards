package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.LogEntryRepository;
import com.ControlCards.ControlCards.Service.Impl.LogEntryService;
import com.ControlCards.ControlCards.Util.Enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogEntryServiceTest {

    @Mock
    private LogEntryRepository logEntryRepository;

    @InjectMocks
    private LogEntryService logEntryService;

    private User testUser;
    private LogEntry testLogEntry;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(UUID.randomUUID());
        testUser.setUsername("testuser");
        testUser.setRole(Role.WORKER);
        testUser.setActive(true);

        testLogEntry = new LogEntry();
        testLogEntry.setId(UUID.randomUUID());
        testLogEntry.setUser(testUser);
        testLogEntry.setDescription("Test action");
    }

    @Test
    void testCreateLog() {
        ArgumentCaptor<LogEntry> logEntryCaptor = ArgumentCaptor.forClass(LogEntry.class);
        when(logEntryRepository.save(any(LogEntry.class))).thenReturn(testLogEntry);

        LogEntry result = logEntryService.createLog(testUser, "Test action");

        assertNotNull(result);
        verify(logEntryRepository, times(1)).save(logEntryCaptor.capture());
        LogEntry captured = logEntryCaptor.getValue();
        assertEquals(testUser, captured.getUser());
        assertEquals("Test action", captured.getDescription());
    }

    @Test
    void testSaveLog() {
        logEntryService.saveLog(testLogEntry);

        verify(logEntryRepository, times(1)).save(testLogEntry);
    }

    @Test
    void testFindAll() {
        List<LogEntry> logEntries = Arrays.asList(testLogEntry);
        when(logEntryRepository.findAll()).thenReturn(logEntries);

        List<LogEntry> result = logEntryService.findAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(logEntryRepository, times(1)).findAll();
    }

    @Test
    void testDeleteOldLogs() {
        when(logEntryRepository.countByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(5L);
        when(logEntryRepository.deleteByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(5);

        int result = logEntryService.deleteOldLogs(90);

        assertEquals(5, result);
        verify(logEntryRepository, times(1)).countByCreatedAtBefore(any(LocalDateTime.class));
        verify(logEntryRepository, times(1)).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }

    @Test
    void testDeleteOldLogsNoLogsToDelete() {
        when(logEntryRepository.countByCreatedAtBefore(any(LocalDateTime.class))).thenReturn(0L);

        int result = logEntryService.deleteOldLogs(90);

        assertEquals(0, result);
        verify(logEntryRepository, times(1)).countByCreatedAtBefore(any(LocalDateTime.class));
        verify(logEntryRepository, never()).deleteByCreatedAtBefore(any(LocalDateTime.class));
    }
}

