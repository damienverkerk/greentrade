package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.UserDTO;
import com.greentrade.greentrade.models.User;

@Component
public class UserMapper {
    
    public UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        
        return new UserDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole().toString()
        );
    }
    
    public User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        
        return user;
    }
}