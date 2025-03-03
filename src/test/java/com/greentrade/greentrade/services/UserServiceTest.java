package com.greentrade.greentrade.services;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greentrade.greentrade.dto.user.UserCreateRequest;
import com.greentrade.greentrade.dto.user.UserResponse;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.UserMapper;
import com.greentrade.greentrade.models.Role;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private UserResponse testUserResponse;
    private UserCreateRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        // Set up test user
        testUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .role(Role.ROLE_BUYER)
                .build();

        // Set up test user response
        testUserResponse = UserResponse.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .role("ROLE_BUYER")
                .build();

        // Set up valid create request
        validCreateRequest = UserCreateRequest.builder()
                .name("New User")
                .email("new@example.com")
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();
    }

    @Test
    void getAllUsers_ReturnsAllUsers() {
        // Arrange
        User user2 = User.builder()
                .id(2L)
                .name("User Two")
                .email("user2@example.com")
                .role(Role.ROLE_SELLER)
                .build();

        UserResponse user2Response = UserResponse.builder()
                .id(2L)
                .name("User Two")
                .email("user2@example.com")
                .role("ROLE_SELLER")
                .build();

        when(userRepository.findAll()).thenReturn(Arrays.asList(testUser, user2));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);
        when(userMapper.toResponse(user2)).thenReturn(user2Response);

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test User", result.get(0).getName());
        assertEquals("User Two", result.get(1).getName());
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<UserResponse> result = userService.getAllUsers();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        // Act
        UserResponse result = userService.getUserById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Test User", result.getName());
        assertEquals("test@example.com", result.getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_NonExistingUser_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userService.getUserById(999L)
        );
        assertEquals("User not found with ID: 999", thrown.getMessage());
    }

    @Test
    void createUser_ValidData_CreatesUser() {
        // Arrange
        User newUser = User.builder()
                .id(2L)
                .name("New User")
                .email("new@example.com")
                .role(Role.ROLE_BUYER)
                .build();

        UserResponse newUserResponse = UserResponse.builder()
                .id(2L)
                .name("New User")
                .email("new@example.com")
                .role("ROLE_BUYER")
                .build();

        when(userMapper.createRequestToEntity(validCreateRequest)).thenReturn(newUser);
        when(userRepository.save(newUser)).thenReturn(newUser);
        when(userMapper.toResponse(newUser)).thenReturn(newUserResponse);

        // Act
        UserResponse result = userService.createUser(validCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("New User", result.getName());
        assertEquals("new@example.com", result.getEmail());
        verify(userRepository).save(newUser);
    }

    @Test
    void createUser_EmptyName_ThrowsException() {
        // Arrange
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .name("")
                .email("new@example.com")
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidRequest)
        );
        assertTrue(thrown.getMessage().contains("User name cannot be empty"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_NullName_ThrowsException() {
        // Arrange
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .name(null)
                .email("new@example.com")
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidRequest)
        );
        assertTrue(thrown.getMessage().contains("User name cannot be empty"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_InvalidEmail_ThrowsException() {
        // Arrange
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .name("New User")
                .email("invalid-email")
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidRequest)
        );
        assertTrue(thrown.getMessage().contains("Invalid email address"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_NullEmail_ThrowsException() {
        // Arrange
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .name("New User")
                .email(null)
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidRequest)
        );
        assertTrue(thrown.getMessage().contains("Invalid email address"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_EmptyEmail_ThrowsException() {
        // Arrange
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .name("New User")
                .email("")
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.createUser(invalidRequest)
        );
        assertTrue(thrown.getMessage().contains("Invalid email address"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_ValidData_UpdatesUser() {
        // Arrange
        UserCreateRequest updateRequest = UserCreateRequest.builder()
                .name("Updated User")
                .email("updated@example.com")
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        
        User updatedUser = User.builder()
                .id(1L)
                .name("Updated User")
                .email("updated@example.com")
                .role(Role.ROLE_BUYER)
                .build();
        
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);
        
        UserResponse updatedResponse = UserResponse.builder()
                .id(1L)
                .name("Updated User")
                .email("updated@example.com")
                .role("ROLE_BUYER")
                .build();
                
        when(userMapper.toResponse(any(User.class))).thenReturn(updatedResponse);

        // Act
        UserResponse result = userService.updateUser(1L, updateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("Updated User", result.getName());
        assertEquals("updated@example.com", result.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateUser_NonExistingUser_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userService.updateUser(999L, validCreateRequest)
        );
        assertEquals("User not found with ID: 999", thrown.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_InvalidName_ThrowsException() {
        // Arrange
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .name("")
                .email("updated@example.com")
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(1L, invalidRequest)
        );
        assertTrue(thrown.getMessage().contains("User name cannot be empty"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUser_InvalidEmail_ThrowsException() {
        // Arrange
        UserCreateRequest invalidRequest = UserCreateRequest.builder()
                .name("Updated User")
                .email("invalid-email")
                .password("Password123!")
                .role("ROLE_BUYER")
                .build();

        // Act & Assert
        IllegalArgumentException thrown = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(1L, invalidRequest)
        );
        assertTrue(thrown.getMessage().contains("Invalid email address"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ExistingUser_DeletesUser() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_NonExistingUser_ThrowsException() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> userService.deleteUser(999L)
        );
        assertEquals("User not found with ID: 999", thrown.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }
}