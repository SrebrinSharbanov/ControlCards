package com.ControlCards.ControlCards.DTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class WorkshopCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidWorkshopCreateDTO() {
        WorkshopCreateDTO dto = new WorkshopCreateDTO();
        dto.setName("Test Workshop");
        dto.setDescription("Test Description");

        Set<ConstraintViolation<WorkshopCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankName() {
        WorkshopCreateDTO dto = new WorkshopCreateDTO();
        dto.setName("");
        dto.setDescription("Test Description");

        Set<ConstraintViolation<WorkshopCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Workshop name is required")));
    }

    @Test
    void testNameTooLong() {
        WorkshopCreateDTO dto = new WorkshopCreateDTO();
        dto.setName("a".repeat(101));
        dto.setDescription("Test Description");

        Set<ConstraintViolation<WorkshopCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("100 characters")));
    }

    @Test
    void testDescriptionTooLong() {
        WorkshopCreateDTO dto = new WorkshopCreateDTO();
        dto.setName("Test Workshop");
        dto.setDescription("a".repeat(501));

        Set<ConstraintViolation<WorkshopCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("500 characters")));
    }

    @Test
    void testNullName() {
        WorkshopCreateDTO dto = new WorkshopCreateDTO();
        dto.setName(null);
        dto.setDescription("Test Description");

        Set<ConstraintViolation<WorkshopCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Workshop name is required")));
    }

    @Test
    void testNameAtMaxLength() {
        WorkshopCreateDTO dto = new WorkshopCreateDTO();
        dto.setName("a".repeat(100));
        dto.setDescription("Test Description");

        Set<ConstraintViolation<WorkshopCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testDescriptionAtMaxLength() {
        WorkshopCreateDTO dto = new WorkshopCreateDTO();
        dto.setName("Test Workshop");
        dto.setDescription("a".repeat(500));

        Set<ConstraintViolation<WorkshopCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullDescription() {
        WorkshopCreateDTO dto = new WorkshopCreateDTO();
        dto.setName("Test Workshop");
        dto.setDescription(null);

        Set<ConstraintViolation<WorkshopCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}

