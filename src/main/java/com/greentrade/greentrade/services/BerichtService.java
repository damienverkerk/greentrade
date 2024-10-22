package com.greentrade.greentrade.services;

import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.MessageRepository;
import com.greentrade.greentrade.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BerichtService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public BerichtService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public List<Message> getAlleBerichten() {
        return messageRepository.findAll();
    }

    public Optional<Message> getBerichtById(Long id) {
        return messageRepository.findById(id);
    }

    public Message verstuurBericht(Message bericht) {
        return messageRepository.save(bericht);
    }

    public Message markeerAlsGelezen(Long id) {
        Message bericht = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bericht niet gevonden met id: " + id));
        bericht.setGelezen(true);
        return messageRepository.save(bericht);
    }

    public void verwijderBericht(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new RuntimeException("Bericht niet gevonden met id: " + id);
        }
        messageRepository.deleteById(id);
    }

    public List<Message> getOntvangenBerichtenVoorGebruiker(Long gebruikerId) {
        User gebruiker = userRepository.findById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        return messageRepository.findByOntvanger(gebruiker);
    }

    public List<Message> getVerzondenBerichtenVanGebruiker(Long gebruikerId) {
        User gebruiker = userRepository.findById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        return messageRepository.findByAfzender(gebruiker);
    }

    public List<Message> getOngelezenBerichtenVoorGebruiker(Long gebruikerId) {
        User gebruiker = userRepository.findById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        return messageRepository.findByOntvangerAndGelezenIsFalse(gebruiker);
    }
}