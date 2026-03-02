package com.api.boilerplate.service;

import com.api.boilerplate.dto.CreateUserRequest;
import com.api.boilerplate.dto.UpdateUserRequest;
import com.api.boilerplate.dto.UserResponse;
import com.api.boilerplate.entity.User;
import com.api.boilerplate.exception.DuplicateResourceException;
import com.api.boilerplate.exception.ResourceNotFoundException;
import com.api.boilerplate.mapper.UserMapper;
import com.api.boilerplate.repository.UserRepository;
import com.api.boilerplate.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

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

    @Mock
    private UserMapper userMapper;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UserResponse testUserResponse;
    private CreateUserRequest createRequest;
    private UpdateUserRequest updateRequest;

    @BeforeEach
    void setUp() {
        UUID userId = UUID.randomUUID();

        testUser = User.builder()
            .id(userId)
            .name("John Doe")
            .email("john@example.com")
            .password("hashedPassword")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        testUserResponse = new UserResponse(
            userId,
            "John Doe",
            "john@example.com",
            testUser.getCreatedAt(),
            testUser.getUpdatedAt()
        );

        createRequest = new CreateUserRequest("John Doe", "john@example.com", "password123");
        updateRequest = new UpdateUserRequest("John Updated", "john.updated@example.com");
    }

    @Test
    void createUser_Success() {
        when(userRepository.existsByEmail(createRequest.email())).thenReturn(false);
        when(userMapper.toEntity(createRequest)).thenReturn(testUser);
        when(passwordEncoder.encode(createRequest.password())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.createUser(createRequest);

        assertNotNull(result);
        assertEquals("John Doe", result.name());
        assertEquals("john@example.com", result.email());
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode(createRequest.password());
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByEmail(createRequest.email())).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> {
            userService.createUser(createRequest);
        });

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void getUserById_Success() {
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.getUserById(userId);

        assertNotNull(result);
        assertEquals(userId, result.id());
        verify(userRepository).findById(userId);
    }

    @Test
    void getUserById_NotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });
    }

    @Test
    void getAllUsers_Success() {
        List<User> users = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toResponse(any(User.class))).thenReturn(testUserResponse);

        List<UserResponse> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_Success() {
        UUID userId = testUser.getId();
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(updateRequest.email())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userMapper.toResponse(testUser)).thenReturn(testUserResponse);

        UserResponse result = userService.updateUser(userId, updateRequest);

        assertNotNull(result);
        verify(userRepository).save(testUser);
    }

    @Test
    void deleteUser_Success() {
        UUID userId = testUser.getId();
        when(userRepository.existsById(userId)).thenReturn(true);

        userService.deleteUser(userId);

        verify(userRepository).deleteById(userId);
    }

    @Test
    void deleteUser_NotFound_ThrowsException() {
        UUID userId = UUID.randomUUID();
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });

        verify(userRepository, never()).deleteById(any());
    }
}
