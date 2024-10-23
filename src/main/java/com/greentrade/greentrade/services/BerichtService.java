package com.greentrade.greentrade.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.MessageDTO;
import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.MessageRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class BerichtService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public BerichtService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public List<MessageDTO> getAlleBerichten() {
        return messageRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MessageDTO> getBerichtById(Long id) {
        return messageRepository.findById(id)
                .map(this::convertToDTO);
    }

    public MessageDTO verstuurBericht(MessageDTO berichtDTO) {
        Message bericht = convertToEntity(berichtDTO);
        Message savedBericht = messageRepository.save(bericht);
        return convertToDTO(savedBericht);
    }

    public MessageDTO markeerAlsGelezen(Long id) {
        Message bericht = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bericht niet gevonden met id: " + id));
        bericht.setGelezen(true);
        Message updatedBericht = messageRepository.save(bericht);
        return convertToDTO(updatedBericht);
    }

    public void verwijderBericht(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new RuntimeException("Bericht niet gevonden met id: " + id);
        }
        messageRepository.deleteById(id);
    }

    public List<MessageDTO> getOntvangenBerichtenVoorGebruiker(Long gebruikerId) {
        User ontvanger = userRepository.findById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        return messageRepository.findByOntvanger(ontvanger).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getVerzondenBerichtenVanGebruiker(Long gebruikerId) {
        User afzender = userRepository.findById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        return messageRepository.findByAfzender(afzender).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getOngelezenBerichtenVoorGebruiker(Long gebruikerId) {
        User ontvanger = userRepository.findById(gebruikerId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + gebruikerId));
        return messageRepository.findByOntvangerAndGelezenIsFalse(ontvanger).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MessageDTO convertToDTO(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getAfzender().getId(),
                message.getOntvanger().getId(),
                message.getOnderwerp(),
                message.getInhoud(),
                message.getDatumTijd(),
                message.isGelezen()
        );
    }

    private Message convertToEntity(MessageDTO dto) {
        Message message = new Message();
        if (dto.getId() != null) {
            message.setId(dto.getId());
        }
        
        User afzender = userRepository.findById(dto.getAfzenderId())
                .orElseThrow(() -> new RuntimeException("Afzender niet gevonden met id: " + dto.getAfzenderId()));
        User ontvanger = userRepository.findById(dto.getOntvangerId())
                .orElseThrow(() -> new RuntimeException("Ontvanger niet gevonden met id: " + dto.getOntvangerId()));

        message.setAfzender(afzender);
        message.setOntvanger(ontvanger);
        message.setOnderwerp(dto.getOnderwerp());
        message.setInhoud(dto.getInhoud());
        message.setDatumTijd(dto.getDatumTijd());
        message.setGelezen(dto.isGelezen());

        return message;
    }
}