package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.user.UserCreateRequest;
import com.greentrade.greentrade.dto.user.UserResponse;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.UserMapper;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public UserResponse createUser(UserCreateRequest userRequest) {
        validateUserData(userRequest);
        
        User user = userMapper.createRequestToEntity(userRequest);
        User savedUser = userRepository.save(user);
        
        return userMapper.toResponse(savedUser);
    }

    public UserResponse updateUser(Long id, UserCreateRequest userRequest) {
        validateUserData(userRequest);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        
        // Update fields
        user.setName(userRequest.getName());
        user.setEmail(userRequest.getEmail());
        
        User updatedUser = userRepository.save(user);
        return userMapper.toResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
    
    private void validateUserData(UserCreateRequest userRequest) {
        if (userRequest.getName() == null || userRequest.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        
        if (userRequest.getEmail() == null || userRequest.getEmail().trim().isEmpty() || !isValidEmail(userRequest.getEmail())) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}