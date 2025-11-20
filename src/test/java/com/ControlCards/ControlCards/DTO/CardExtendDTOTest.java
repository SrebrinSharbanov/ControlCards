package com.ControlCards.ControlCards.DTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CardExtendDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCardExtendDTO() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription("Valid detailed description");
        dto.setResolutionDurationMinutes(60);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankDetailedDescription() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription("");
        dto.setResolutionDurationMinutes(60);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Подробното описание е задължително")));
    }

    @Test
    void testNullDetailedDescription() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription(null);
        dto.setResolutionDurationMinutes(60);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Подробното описание е задължително")));
    }

    @Test
    void testDetailedDescriptionTooLong() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription("a".repeat(2001));
        dto.setResolutionDurationMinutes(60);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("2000 символа")));
    }

    @Test
    void testNullResolutionDurationMinutes() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription("Valid description");
        dto.setResolutionDurationMinutes(null);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Продължителността на разрешаването е задължителна")));
    }

    @Test
    void testResolutionDurationMinutesTooSmall() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription("Valid description");
        dto.setResolutionDurationMinutes(0);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("поне 1 минута")));
    }

    @Test
    void testDetailedDescriptionAtMaxLength() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription("a".repeat(2000));
        dto.setResolutionDurationMinutes(60);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testResolutionDurationMinutesAtMinValue() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription("Valid description");
        dto.setResolutionDurationMinutes(1);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testResolutionDurationMinutesLargeValue() {
        CardExtendDTO dto = new CardExtendDTO();
        dto.setDetailedDescription("Valid description");
        dto.setResolutionDurationMinutes(10000);

        Set<ConstraintViolation<CardExtendDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}

