package com.greentrade.greentrade.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class PasswordHashGenerator {
    
    @Bean
    public CommandLineRunner generatePasswordHash() {
        return args -> {
            BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
            String password = "password123";
            String encodedPassword = encoder.encode(password);
            System.out.println("Generated password hash for data.sql: " + encodedPassword);
        };
    }
}