package com.greentrade.greentrade.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.MessageRepository;

@Service
public class BerichtService {

    private final MessageRepository messageRepository;

    @Autowired
    public BerichtService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    public List<Message> getAlleBerichten() {
        return messageRepository.findAll();
    }

    public Optional<Message> getBerichtById(Long id) {
        return messageRepository.findById(id);
    }

    public List<Message> getVerzondenBerichtenVanGebruiker(User afzender) {
        return messageRepository.findByAfzender(afzender);
    }

    public List<Message> getOntvangenBerichtenVoorGebruiker(User ontvanger) {
        return messageRepository.findByOntvanger(ontvanger);
    }

    public List<Message> getOngelezenBerichtenVoorGebruiker(User ontvanger) {
        return messageRepository.findByOntvangerAndGelezenIsFalse(ontvanger);
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
}