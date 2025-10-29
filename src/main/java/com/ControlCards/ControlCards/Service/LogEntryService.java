package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.LogEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;

    @Autowired
    public LogEntryService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    public LogEntry createLog(User user, String action) {
        log.info("Creating log entry for user: {} with action: {}", user.getUsername(), action);
        
        LogEntry logEntry = new LogEntry(user, action);
        logEntryRepository.save(logEntry);
        
        log.info("Log entry created successfully with ID: {}", logEntry.getId());
        return logEntry;
    }

    public void saveLog(LogEntry logEntry) {
        log.debug("Saving log entry: {}", logEntry.getDescription());
        logEntryRepository.save(logEntry);
    }
}
