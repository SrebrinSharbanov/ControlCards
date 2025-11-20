package com.ControlCards.ControlCards.Service.Impl;

import com.ControlCards.ControlCards.Model.User;
import com.ControlCards.ControlCards.Repository.UserRepository;
import com.ControlCards.ControlCards.Util.Enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private User adminUser;
    private UUID testUserId;
    private UUID adminUserId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        adminUserId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setRole(Role.WORKER);
        testUser.setActive(true);
        testUser.setCreatedAt(LocalDateTime.now());

        adminUser = new User();
        adminUser.setId(adminUserId);
        adminUser.setUsername("admin");
        adminUser.setPassword("encodedPassword");
        adminUser.setFirstName("Admin");
        adminUser.setLastName("User");
        adminUser.setRole(Role.ADMIN);
        adminUser.setActive(true);
        adminUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void testFindAll() {
        List<User> users = Arrays.asList(testUser, adminUser);
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(testUserId);

        assertTrue(result.isPresent());
        assertEquals(testUser.getUsername(), result.get().getUsername());
        verify(userRepository, times(1)).findById(testUserId);
    }

    @Test
    void testFindByIdNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(nonExistentId);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testFindByUsername() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void testSave() {
        User newUser = new User();
        newUser.setUsername("newuser");
        newUser.setPassword("password");
        newUser.setFirstName("New");
        newUser.setLastName("User");
        newUser.setRole(Role.TECHNICIAN);
        newUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User result = userService.save(newUser);

        assertNotNull(result);
        assertEquals("newuser", result.getUsername());
        verify(userRepository, times(1)).save(newUser);
    }

    @Test
    void testDeactivate() {
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.deactivate(testUserId);

        assertFalse(testUser.getActive());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testDeactivateUserNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.deactivate(nonExistentId));
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testDeactivateAdminThrowsException() {
        when(userRepository.findById(adminUserId)).thenReturn(Optional.of(adminUser));

        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> userService.deactivate(adminUserId));

        assertEquals("Cannot deactivate ADMIN user", exception.getMessage());
        verify(userRepository, times(1)).findById(adminUserId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testActivate() {
        testUser.setActive(false);
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.activate(testUserId);

        assertTrue(testUser.getActive());
        verify(userRepository, times(1)).findById(testUserId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testActivateUserNotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.activate(nonExistentId));
        verify(userRepository, times(1)).findById(nonExistentId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testExistsById() {
        when(userRepository.existsById(testUserId)).thenReturn(true);

        boolean result = userService.existsById(testUserId);

        assertTrue(result);
        verify(userRepository, times(1)).existsById(testUserId);
    }
}

