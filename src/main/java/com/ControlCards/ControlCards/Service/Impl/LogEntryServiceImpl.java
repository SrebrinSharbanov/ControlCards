package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.LogEntryRepository;
import com.ControlCards.ControlCards.Service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LogEntryServiceImpl implements LogEntryService {

    private final LogEntryRepository logEntryRepository;

    @Autowired
    public LogEntryServiceImpl(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    @Override
    public LogEntry createLog(User user, String description) {

        LogEntry logEntry = new LogEntry();
        logEntry.setUser(user);
        logEntry.setDescription(description);
        logEntry.setCreatedAt(LocalDateTime.now());
        return logEntry;
    }

    @Override
    public void saveLog(LogEntry logEntry) {
        logEntryRepository.save(logEntry);
    }
}
