package com.ControlCards.ControlCards.Exception;

public class WorkshopNotFoundException extends RuntimeException {
    
    public WorkshopNotFoundException(String message) {
        super(message);
    }
    
    public WorkshopNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

