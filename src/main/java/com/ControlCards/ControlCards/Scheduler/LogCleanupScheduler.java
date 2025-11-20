package com.ControlCards.ControlCards.Scheduler;

import com.ControlCards.ControlCards.Service.Impl.LogEntryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LogCleanupScheduler {

    private final LogEntryService logEntryService;

    @Value("${log.cleanup.days-to-keep:90}")
    private int daysToKeep;

    @Autowired
    public LogCleanupScheduler(LogEntryService logEntryService) {
        this.logEntryService = logEntryService;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupOldLogs() {
        log.info("Starting scheduled cleanup of old log entries (keeping logs for {} days)", daysToKeep);
        
        try {
            int deletedCount = logEntryService.deleteOldLogs(daysToKeep);
            log.info("Log cleanup completed. Deleted {} old log entries", deletedCount);
        } catch (Exception e) {
            log.error("Error during log cleanup", e);
        }
    }
}

