package com.ControlCards.ControlCards.DTO;

import com.ControlCards.ControlCards.Util.Enums.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserCreateDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidUserCreateDTO() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setRole(Role.WORKER);

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testBlankUsername() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("");
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Username is required")));
    }

    @Test
    void testUsernameTooShort() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("ab");
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("3 and 50 characters"));
    }

    @Test
    void testUsernameTooLong() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("a".repeat(51));
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("User");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("3 and 50 characters"));
    }

    @Test
    void testBlankPassword() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setPassword("");
        dto.setFirstName("Test");
        dto.setLastName("User");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertTrue(violations.size() >= 1);
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("Password is required")));
    }

    @Test
    void testPasswordTooShort() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setPassword("12345");
        dto.setFirstName("Test");
        dto.setLastName("User");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("at least 6 characters"));
    }

    @Test
    void testBlankFirstName() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setPassword("password123");
        dto.setFirstName("");
        dto.setLastName("User");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("First name is required", violations.iterator().next().getMessage());
    }

    @Test
    void testFirstNameTooLong() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setPassword("password123");
        dto.setFirstName("a".repeat(51));
        dto.setLastName("User");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("50 characters"));
    }

    @Test
    void testBlankLastName() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("");

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertEquals("Last name is required", violations.iterator().next().getMessage());
    }

    @Test
    void testLastNameTooLong() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("testuser");
        dto.setPassword("password123");
        dto.setFirstName("Test");
        dto.setLastName("a".repeat(51));

        Set<ConstraintViolation<UserCreateDTO>> violations = validator.validate(dto);

        assertEquals(1, violations.size());
        assertTrue(violations.iterator().next().getMessage().contains("50 characters"));
    }
}

