package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.UserDTO;
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

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDTO)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public UserDTO createUser(UserDTO userDTO) {
        validateUserData(userDTO);
        
        User user = userMapper.toEntity(userDTO);
        User savedUser = userRepository.save(user);
        
        return userMapper.toDTO(savedUser);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        validateUserData(userDTO);
        
        return userRepository.findById(id)
                .map(user -> updateUserFields(user, userDTO))
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
    
    private void validateUserData(UserDTO userDTO) {
        if (userDTO.getName() == null || userDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("User name cannot be empty");
        }
        
        if (userDTO.getEmail() == null || userDTO.getEmail().trim().isEmpty() || !isValidEmail(userDTO.getEmail())) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }
    
    private boolean isValidEmail(String email) {
        // Simple email validation
        return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    private UserDTO updateUserFields(User user, UserDTO userDTO) {
        user.setName(userDTO.getName());
        user.setEmail(userDTO.getEmail());
        
        User updatedUser = userRepository.save(user);
        return userMapper.toDTO(updatedUser);
    }
}