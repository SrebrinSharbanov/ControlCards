package com.ControlCards.ControlCards.Exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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
        model.addAttribute("errorMessage", "Валидацията не бе успешна. Моля, проверете въведените данни.");
        model.addAttribute("errorTitle", "Грешка при валидация");
        
        log.error("Validation failed with {} errors", errors.size());
        return "error";
    }

    @ExceptionHandler(CardNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleCardNotFoundException(CardNotFoundException ex, Model model) {
        log.error("Card not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Работна карта не е намерена");
        return "error";
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleUserNotFoundException(UserNotFoundException ex, Model model) {
        log.error("User not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Потребител не е намерен");
        return "error";
    }

    @ExceptionHandler(WorkshopNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleWorkshopNotFoundException(WorkshopNotFoundException ex, Model model) {
        log.error("Workshop not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Цех не е намерен");
        return "error";
    }

    @ExceptionHandler(WorkCenterNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleWorkCenterNotFoundException(WorkCenterNotFoundException ex, Model model) {
        log.error("Work center not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Работен център не е намерен");
        return "error";
    }

    @ExceptionHandler(InvalidCardStatusException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleInvalidCardStatusException(InvalidCardStatusException ex, Model model) {
        log.error("Invalid card status: {}", ex.getMessage());
        model.addAttribute("errorMessage", ex.getMessage());
        model.addAttribute("errorTitle", "Невалиден статус на работна карта");
        return "error";
    }

    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNoResourceFoundException(NoResourceFoundException ex, Model model) {
        // Silently ignore favicon.ico and other static resource errors
        if (ex.getMessage() != null && ex.getMessage().contains("favicon.ico")) {
            return null; // Return null to indicate no view should be rendered
        }
        log.debug("Resource not found: {}", ex.getMessage());
        model.addAttribute("errorMessage", "Заявеният ресурс не е намерен.");
        model.addAttribute("errorTitle", "Ресурс не е намерен");
        return "error";
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public String handleGenericException(Exception ex, Model model) {
        log.error("Unexpected error occurred: {}", ex.getMessage(), ex);
        model.addAttribute("errorMessage", "Възникна неочаквана грешка. Моля, опитайте отново по-късно.");
        model.addAttribute("errorTitle", "Вътрешна сървърна грешка");
        return "error";
    }
}
