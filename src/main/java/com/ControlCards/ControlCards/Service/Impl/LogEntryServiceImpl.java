package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Repository.CardRepository;
import com.ControlCards.ControlCards.Repository.LogEntryRepository;
import com.ControlCards.ControlCards.Service.LogEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
public class LogEntryServiceImpl implements LogEntryService {

    private final LogEntryRepository logEntryRepository;

    @Autowired
    public LogEntryServiceImpl(LogEntryRepository logEntryRepository) {
        this.logEntryRepository = logEntryRepository;
    }


}
