package com.ControlCards.ControlCards.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleValidationExceptions(MethodArgumentNotValidException ex, Model model) {
        log.warn("Validation error occurred: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
            log.debug("Validation error for field '{}': {}", fieldName, errorMessage);
        });
        
        model.addAttribute("errors", errors);
        model.addAttribute("errorMessage", "Validation failed. Please check your input.");
        
        log.error("Validation failed with {} errors", errors.size());
        return "error";
    }

    @ExceptionHandler(CardNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCardNotFoundException(CardNotFoundException ex, Model model) {
        log.error("Card not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Card Not Found");
        return "error";
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFoundException(UserNotFoundException ex, Model model) {
        log.error("User not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "User Not Found");
        return "error";
    }

    @ExceptionHandler(WorkshopNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleWorkshopNotFoundException(WorkshopNotFoundException ex, Model model) {
        log.error("Workshop not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Workshop Not Found");
        return "error";
    }

    @ExceptionHandler(WorkCenterNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleWorkCenterNotFoundException(WorkCenterNotFoundException ex, Model model) {
        log.error("Work center not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Work Center Not Found");
        return "error";
    }

    @ExceptionHandler(InvalidCardStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidCardStatusException(InvalidCardStatusException ex, Model model) {
        log.error("Invalid card status: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Invalid Card Status");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "An unexpected error occurred. Please try again later.");
        model.addAttribute("errorTitle", "Internal Server Error");
        return "error";
    }
}

