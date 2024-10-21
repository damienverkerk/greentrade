package com.greentrade.greentrade.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.UserDTO;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public UserDTO createUser(UserDTO userDTO) {
        User user = convertToEntity(userDTO);
        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setNaam(userDTO.getNaam());
                    user.setEmail(userDTO.getEmail());
                    return convertToDTO(userRepository.save(user));
                })
                .orElse(null);
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    private UserDTO convertToDTO(User user) {
        return new UserDTO(user.getId(), user.getNaam(), user.getEmail(), user.getRole().toString());
    }

    private User convertToEntity(UserDTO userDTO) {
        User user = new User();
        user.setNaam(userDTO.getNaam());
        user.setEmail(userDTO.getEmail());
        return user;
    }
}