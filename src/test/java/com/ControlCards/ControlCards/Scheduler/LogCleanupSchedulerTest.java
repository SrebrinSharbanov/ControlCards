package com.ControlCards.ControlCards.Scheduler;

import com.ControlCards.ControlCards.Service.Impl.LogEntryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogCleanupSchedulerTest {

    @Mock
    private LogEntryService logEntryService;

    @InjectMocks
    private LogCleanupScheduler logCleanupScheduler;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(logCleanupScheduler, "daysToKeep", 90);
    }

    @Test
    void testCleanupOldLogsSuccess() {
        when(logEntryService.deleteOldLogs(90)).thenReturn(5);

        logCleanupScheduler.cleanupOldLogs();

        verify(logEntryService, times(1)).deleteOldLogs(90);
    }

    @Test
    void testCleanupOldLogsWithException() {
        when(logEntryService.deleteOldLogs(90)).thenThrow(new RuntimeException("Database error"));

        logCleanupScheduler.cleanupOldLogs();

        verify(logEntryService, times(1)).deleteOldLogs(90);
    }

    @Test
    void testCleanupOldLogsNoLogsToDelete() {
        when(logEntryService.deleteOldLogs(90)).thenReturn(0);

        logCleanupScheduler.cleanupOldLogs();

        verify(logEntryService, times(1)).deleteOldLogs(90);
    }
}

