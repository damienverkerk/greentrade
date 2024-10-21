package com.greentrade.greentrade.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greentrade.greentrade.models.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Hier kunnen we later custom query methodes toevoegen indien nodig
}