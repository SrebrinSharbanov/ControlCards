package com.ControlCards.ControlCards.DTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class WorkCenterCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidWorkCenterCreateDTO() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("1001");
        dto.setDescription("Test Description");
        dto.setMachineType("CNC");
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankNumber() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("");
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Work center number is required")));
    }

    @Test
    void testNumberTooLong() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("123456");
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("5 characters")));
    }

    @Test
    void testNullWorkshopId() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("1001");
        dto.setWorkshopId(null);

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Workshop is required")));
    }

    @Test
    void testDescriptionTooLong() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("1001");
        dto.setDescription("a".repeat(501));
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("500 characters")));
    }

    @Test
    void testMachineTypeTooLong() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("1001");
        dto.setMachineType("a".repeat(101));
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("100 characters")));
    }

    @Test
    void testNullNumber() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber(null);
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Work center number is required")));
    }

    @Test
    void testNumberAtMaxLength() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("12345");
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testDescriptionAtMaxLength() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("1001");
        dto.setDescription("a".repeat(500));
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testMachineTypeAtMaxLength() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("1001");
        dto.setMachineType("a".repeat(100));
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullDescription() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("1001");
        dto.setDescription(null);
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testNullMachineType() {
        WorkCenterCreateDTO dto = new WorkCenterCreateDTO();
        dto.setNumber("1001");
        dto.setMachineType(null);
        dto.setWorkshopId(UUID.randomUUID());

        Set<ConstraintViolation<WorkCenterCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }
}

