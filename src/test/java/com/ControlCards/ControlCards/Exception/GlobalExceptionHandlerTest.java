package com.ControlCards.ControlCards.Exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import org.springframework.beans.TypeMismatchException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;
    private Model model;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
        model = mock(Model.class);
    }

    @Test
    void testHandleCardNotFoundException() {
        CardNotFoundException ex = new CardNotFoundException("Card with ID 123 not found");
        
        String result = exceptionHandler.handleCardNotFoundException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "Card with ID 123 not found");
        verify(model, times(1)).addAttribute("errorTitle", "Работна карта не е намерена");
    }

    @Test
    void testHandleUserNotFoundException() {
        UserNotFoundException ex = new UserNotFoundException("User not found");
        
        String result = exceptionHandler.handleUserNotFoundException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "User not found");
        verify(model, times(1)).addAttribute("errorTitle", "Потребител не е намерен");
    }

    @Test
    void testHandleWorkshopNotFoundException() {
        WorkshopNotFoundException ex = new WorkshopNotFoundException("Workshop not found");
        
        String result = exceptionHandler.handleWorkshopNotFoundException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "Workshop not found");
        verify(model, times(1)).addAttribute("errorTitle", "Цех не е намерен");
    }

    @Test
    void testHandleWorkCenterNotFoundException() {
        WorkCenterNotFoundException ex = new WorkCenterNotFoundException("Work center not found");
        
        String result = exceptionHandler.handleWorkCenterNotFoundException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "Work center not found");
        verify(model, times(1)).addAttribute("errorTitle", "Работен център не е намерен");
    }

    @Test
    void testHandleInvalidCardStatusException() {
        InvalidCardStatusException ex = new InvalidCardStatusException("Invalid card status");
        
        String result = exceptionHandler.handleInvalidCardStatusException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "Invalid card status");
        verify(model, times(1)).addAttribute("errorTitle", "Невалиден статус на работна карта");
    }

    @Test
    void testHandleTypeMismatchException() {
        TypeMismatchException ex = new TypeMismatchException("value", String.class);
        ex.initPropertyName("testField");
        
        ModelAndView result = exceptionHandler.handleTypeMismatchException(ex);
        
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        assertNotNull(result.getModel().get("errorMessage"));
        assertNotNull(result.getModel().get("errorTitle"));
    }

    @Test
    void testHandleTypeMismatchExceptionWithUUID() {
        TypeMismatchException ex = new TypeMismatchException("invalid-uuid", UUID.class);
        ex.initPropertyName("id");
        
        ModelAndView result = exceptionHandler.handleTypeMismatchException(ex);
        
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        String errorMessage = (String) result.getModel().get("errorMessage");
        assertNotNull(errorMessage);
        assertTrue(errorMessage.contains("валидна стойност"));
    }

    @Test
    void testHandleNoResourceFoundException() {
        NoResourceFoundException ex = new NoResourceFoundException(null, "/test/resource");
        
        String result = exceptionHandler.handleNoResourceFoundException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "Заявеният ресурс не е намерен.");
        verify(model, times(1)).addAttribute("errorTitle", "Ресурс не е намерен");
    }

    @Test
    void testHandleNoResourceFoundExceptionFavicon() {
        NoResourceFoundException ex = new NoResourceFoundException(null, "/favicon.ico");
        
        String result = exceptionHandler.handleNoResourceFoundException(ex, model);
        
        assertNull(result);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    void testHandleGenericException() {
        Exception ex = new RuntimeException("Generic error");
        
        String result = exceptionHandler.handleGenericException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "Възникна неочаквана грешка. Моля, опитайте отново по-късно.");
        verify(model, times(1)).addAttribute("errorTitle", "Вътрешна сървърна грешка");
    }

    @Test
    void testHandleMethodArgumentNotValidException() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        List<FieldError> fieldErrors = new ArrayList<>();
        FieldError fieldError1 = new FieldError("card", "shortDescription", "Short description is required");
        FieldError fieldError2 = new FieldError("card", "shift", "Shift is required");
        fieldErrors.add(fieldError1);
        fieldErrors.add(fieldError2);
        
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(new ArrayList<>(fieldErrors));
        
        String result = exceptionHandler.handleValidationExceptions(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute(eq("errors"), any(Map.class));
        verify(model, times(1)).addAttribute("errorMessage", "Валидацията не бе успешна. Моля, проверете въведените данни.");
        verify(model, times(1)).addAttribute("errorTitle", "Грешка при валидация");
    }

    @Test
    void testHandleAccessDeniedException() {
        AccessDeniedException ex = new AccessDeniedException("Access denied");
        
        String result = exceptionHandler.handleAccessDeniedException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "Нямате права за достъп до този ресурс.");
        verify(model, times(1)).addAttribute("errorTitle", "Достъпът е отказан");
    }

    @Test
    void testHandleAuthorizationDeniedException() {
        AuthorizationDeniedException ex = mock(AuthorizationDeniedException.class);
        when(ex.getMessage()).thenReturn("Access denied");
        
        String result = exceptionHandler.handleAccessDeniedException(ex, model);
        
        assertEquals("error", result);
        verify(model, times(1)).addAttribute("errorMessage", "Нямате права за достъп до този ресурс.");
        verify(model, times(1)).addAttribute("errorTitle", "Достъпът е отказан");
    }

    @Test
    void testHandleTypeMismatchExceptionWithShift() {
        TypeMismatchException ex = mock(TypeMismatchException.class);
        when(ex.getMessage()).thenReturn("Failed to convert value of type 'java.lang.String' to required type 'com.ControlCards.ControlCards.Util.Enums.Shift'");
        when(ex.getPropertyName()).thenReturn("shift");
        
        ModelAndView result = exceptionHandler.handleTypeMismatchException(ex);
        
        assertNotNull(result);
        assertEquals("error", result.getViewName());
        String errorMessage = (String) result.getModel().get("errorMessage");
        assertNotNull(errorMessage);
        assertTrue(errorMessage.contains("смяна"));
    }
}

