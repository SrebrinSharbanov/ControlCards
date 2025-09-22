package com.ControlCards.ControlCards.Service;

import com.ControlCards.ControlCards.Model.LogEntry;
import com.ControlCards.ControlCards.Model.User;

public interface LogEntryService {

    LogEntry createLog(User user, String description);
    void saveLog(LogEntry logEntry);
}
