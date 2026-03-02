package com.api.boilerplate.service;

import com.api.boilerplate.dto.CreateUserRequest;
import com.api.boilerplate.dto.UpdateUserRequest;
import com.api.boilerplate.dto.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse getUserById(UUID id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void deleteUser(UUID id);
}
