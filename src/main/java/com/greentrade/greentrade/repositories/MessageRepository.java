package com.greentrade.greentrade.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByAfzender(User afzender);
    List<Message> findByOntvanger(User ontvanger);
    List<Message> findByOntvangerAndGelezenIsFalse(User ontvanger);
}