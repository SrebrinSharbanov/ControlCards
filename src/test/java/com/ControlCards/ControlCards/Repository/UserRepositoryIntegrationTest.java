package com.ControlCards.ControlCards.Repository;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Util.Enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.properties")
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser1 = new User();
        testUser1.setUsername("testuser1");
        testUser1.setPassword("encodedPassword1");
        testUser1.setFirstName("Test");
        testUser1.setLastName("User1");
        testUser1.setRole(Role.WORKER);
        testUser1.setActive(true);
        testUser1.setCreatedAt(LocalDateTime.now());

        testUser2 = new User();
        testUser2.setUsername("testuser2");
        testUser2.setPassword("encodedPassword2");
        testUser2.setFirstName("Test");
        testUser2.setLastName("User2");
        testUser2.setRole(Role.TECHNICIAN);
        testUser2.setActive(true);
        testUser2.setCreatedAt(LocalDateTime.now());

        userRepository.save(testUser1);
        userRepository.save(testUser2);
    }

    @Test
    void testSaveUser() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("encodedPassword");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole(Role.PRODUCTION_MANAGER);
        newUser.setActive(true);
        newUser.setCreatedAt(LocalDateTime.now());

        User savedUser = userRepository.save(newUser);

        assertNotNull(savedUser.getId());
        assertEquals("newuser", savedUser.getUsername());
        assertTrue(userRepository.existsById(savedUser.getId()));
    }

    @Test
    void testFindById() {
        Optional<User> foundUser = userRepository.findById(testUser1.getId());

        assertTrue(foundUser.isPresent());
        assertEquals("testuser1", foundUser.get().getUsername());
        assertEquals(Role.WORKER, foundUser.get().getRole());
    }

    @Test
    void testFindByIdNotFound() {
        Optional<User> foundUser = userRepository.findById(UUID.randomUUID());

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindByUsername() {
        Optional<User> foundUser = userRepository.findByUsername("testuser1");

        assertTrue(foundUser.isPresent());
        assertEquals("testuser1", foundUser.get().getUsername());
        assertEquals(testUser1.getId(), foundUser.get().getId());
    }

    @Test
    void testFindByUsernameNotFound() {
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        assertFalse(foundUser.isPresent());
    }

    @Test
    void testFindAll() {
        List<User> allUsers = userRepository.findAll();

        assertNotNull(allUsers);
        assertTrue(allUsers.size() >= 2);
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("testuser1")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getUsername().equals("testuser2")));
    }

    @Test
    void testUpdateUser() {
        testUser1.setFirstName("Updated");
        testUser1.setLastName("Name");
        User updatedUser = userRepository.save(testUser1);

        assertEquals("Updated", updatedUser.getFirstName());
        assertEquals("Name", updatedUser.getLastName());
        
        Optional<User> foundUser = userRepository.findById(testUser1.getId());
        assertTrue(foundUser.isPresent());
        assertEquals("Updated", foundUser.get().getFirstName());
    }

    @Test
    void testDeleteUser() {
        userRepository.delete(testUser1);

        assertFalse(userRepository.existsById(testUser1.getId()));
        assertTrue(userRepository.existsById(testUser2.getId()));
    }

    @Test
    void testUserActiveField() {
        testUser1.setActive(false);
        userRepository.save(testUser1);

        Optional<User> foundUser = userRepository.findById(testUser1.getId());
        assertTrue(foundUser.isPresent());
        assertFalse(foundUser.get().getActive());
    }

    @Test
    void testUsernameUniqueConstraint() {
        User duplicateUser = new User();
        duplicateUser.setUsername("testuser1");
        duplicateUser.setPassword("password");
        duplicateUser.setFirstName("Duplicate");
        duplicateUser.setLastName("User");
        duplicateUser.setRole(Role.WORKER);
        duplicateUser.setActive(true);
        duplicateUser.setCreatedAt(LocalDateTime.now());

        assertThrows(Exception.class, () -> userRepository.saveAndFlush(duplicateUser));
    }
}

