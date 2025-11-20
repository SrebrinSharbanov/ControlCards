package com.ControlCards.ControlCards.DTO;

import com.ControlCards.ControlCards.Util.Enums.Shift;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CardCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidCardCreateDTO() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setWorkshopId(UUID.randomUUID());
        dto.setWorkCenterId(UUID.randomUUID());
        dto.setShift(Shift.FIRST);
        dto.setShortDescription("Test description");

        Set<ConstraintViolation<CardCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullWorkshopId() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setWorkshopId(null);
        dto.setWorkCenterId(UUID.randomUUID());
        dto.setShift(Shift.FIRST);
        dto.setShortDescription("Test description");

        Set<ConstraintViolation<CardCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("Цехът е задължителен", violations.iterator().next().getMessage());
    }

    @Test
    void testNullWorkCenterId() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setWorkshopId(UUID.randomUUID());
        dto.setWorkCenterId(null);
        dto.setShift(Shift.FIRST);
        dto.setShortDescription("Test description");

        Set<ConstraintViolation<CardCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("Работният център е задължителен", violations.iterator().next().getMessage());
    }

    @Test
    void testNullShift() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setWorkshopId(UUID.randomUUID());
        dto.setWorkCenterId(UUID.randomUUID());
        dto.setShift(null);
        dto.setShortDescription("Test description");

        Set<ConstraintViolation<CardCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("Смяната е задължителна", violations.iterator().next().getMessage());
    }

    @Test
    void testBlankShortDescription() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setWorkshopId(UUID.randomUUID());
        dto.setWorkCenterId(UUID.randomUUID());
        dto.setShift(Shift.FIRST);
        dto.setShortDescription("");

        Set<ConstraintViolation<CardCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("Краткото описание е задължително", violations.iterator().next().getMessage());
    }

    @Test
    void testNullShortDescription() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setWorkshopId(UUID.randomUUID());
        dto.setWorkCenterId(UUID.randomUUID());
        dto.setShift(Shift.FIRST);
        dto.setShortDescription(null);

        Set<ConstraintViolation<CardCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("Краткото описание е задължително", violations.iterator().next().getMessage());
    }

    @Test
    void testShortDescriptionTooLong() {
        CardCreateDTO dto = new CardCreateDTO();
        dto.setWorkshopId(UUID.randomUUID());
        dto.setWorkCenterId(UUID.randomUUID());
        dto.setShift(Shift.FIRST);
        dto.setShortDescription("a".repeat(501));

        Set<ConstraintViolation<CardCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("500 символа"));
    }
}

