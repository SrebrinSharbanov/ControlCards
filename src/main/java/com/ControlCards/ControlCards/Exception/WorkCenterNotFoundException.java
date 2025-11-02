package com.ControlCards.ControlCards.Exception;

public class WorkCenterNotFoundException extends RuntimeException {
    
    public WorkCenterNotFoundException(String message) {
        super(message);
    }
    
    public WorkCenterNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

