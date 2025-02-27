package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.user.UserCreateRequest;
import com.greentrade.greentrade.dto.user.UserResponse;
import com.greentrade.greentrade.models.Role;
import com.greentrade.greentrade.models.User;

@Component
public class UserMapper {
    
    public UserResponse toResponse(User user) {
        if (user == null) {
            return null;
        }
        
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .build();
    }
    
    public User createRequestToEntity(UserCreateRequest request) {
        if (request == null) {
            return null;
        }
        
        return User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(Role.valueOf(request.getRole()))
                .build();
    }
}