package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.LogEntryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class LogEntryService {

    private final LogEntryRepository logEntryRepository;

    @Autowired
    public LogEntryService(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }

    public LogEntry createLog(User user, String action) {
        log.info("Създаване на лог запис за потребител: {} с действие: {}", user.getUsername(), action);
        
        LogEntry logEntry = new LogEntry(user, action);
        logEntryRepository.save(logEntry);
        
        log.info("Лог записът е създаден успешно с ID: {}", logEntry.getId());
        return logEntry;
    }

    public void saveLog(LogEntry logEntry) {
        log.debug("Запазване на лог запис: {}", logEntry.getDescription());
        logEntryRepository.save(logEntry);
    }

    public List<LogEntry> findAll() {
        log.debug("Търсене на всички лог записи");
        return logEntryRepository.findAll();
    }
    
    @Transactional
    public int deleteOldLogs(int daysToKeep) {
        log.info("Изтриване на лог записи по-стари от {} дни", daysToKeep);
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysToKeep);
        
        long countBefore = logEntryRepository.countByCreatedAtBefore(cutoffDate);
        log.info("Намерени {} лог записа за изтриване (по-стари от {})", countBefore, cutoffDate);
        
        if (countBefore > 0) {
            int deletedCount = logEntryRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("Успешно изтрити {} стари лог записа", deletedCount);
            return deletedCount;
        }
        
        log.debug("Няма стари лог записи за изтриване");
        return 0;
    }
}
