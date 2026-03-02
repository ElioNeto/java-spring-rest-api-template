package com.api.boilerplate.service.impl;

import com.api.boilerplate.dto.CreateUserRequest;
import com.api.boilerplate.dto.UpdateUserRequest;
import com.api.boilerplate.dto.UserResponse;
import com.api.boilerplate.entity.User;
import com.api.boilerplate.exception.DuplicateResourceException;
import com.api.boilerplate.exception.ResourceNotFoundException;
import com.api.boilerplate.mapper.UserMapper;
import com.api.boilerplate.repository.UserRepository;
import com.api.boilerplate.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.email());

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        User savedUser = userRepository.save(user);
        log.info("User created successfully with ID: {}", savedUser.getId());

        return userMapper.toResponse(savedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {
        log.debug("Fetching user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        return userMapper.toResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        log.debug("Fetching all users");

        return userRepository.findAll()
            .stream()
            .map(userMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        log.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        if (!user.getEmail().equals(request.email()) &&
            userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already exists: " + request.email());
        }

        user.setName(request.name());
        user.setEmail(request.email());

        User updatedUser = userRepository.save(user);
        log.info("User updated successfully: {}", id);

        return userMapper.toResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        userRepository.deleteById(id);
        log.info("User deleted successfully: {}", id);
    }
}
