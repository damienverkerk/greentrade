package com.greentrade.greentrade.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.MessageDTO;
import com.greentrade.greentrade.mappers.MessageMapper;
import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.MessageRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageMapper messageMapper;

    @Autowired
    public MessageService(
            MessageRepository messageRepository, 
            UserRepository userRepository,
            MessageMapper messageMapper) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
        this.messageMapper = messageMapper;
    }

    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<MessageDTO> getMessageById(Long id) {
        return messageRepository.findById(id)
                .map(messageMapper::toDTO);
    }

    public MessageDTO sendMessage(MessageDTO messageDTO) {
        User sender = findUserById(messageDTO.getSenderId());
        User receiver = findUserById(messageDTO.getReceiverId());
        
        Message message = messageMapper.toEntity(messageDTO, sender, receiver);
        Message savedMessage = messageRepository.save(message);
        
        return messageMapper.toDTO(savedMessage);
    }

    public MessageDTO markAsRead(Long id) {
        Message message = findMessageById(id);
        message.setRead(true);
        
        Message updatedMessage = messageRepository.save(message);
        return messageMapper.toDTO(updatedMessage);
    }

    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new RuntimeException("Message not found with id: " + id);
        }
        messageRepository.deleteById(id);
    }
    public List<MessageDTO> getReceivedMessagesForUser(Long userId) {
        User receiver = findUserById(userId);
        return messageRepository.findByReceiver(receiver).stream()
            .map(messageMapper::toDTO)
            .collect(Collectors.toList());
        }
    
    public List<MessageDTO> getSentMessagesByUser(Long userId) {
        User sender = findUserById(userId);
        return messageRepository.findBySender(sender).stream()
            .map(messageMapper::toDTO)
            .collect(Collectors.toList());
        }
    
    public List<MessageDTO> getUnreadMessagesForUser(Long userId) {
        User receiver = findUserById(userId);
        return messageRepository.findByReceiverAndReadIsFalse(receiver).stream()
                .map(messageMapper::toDTO)
                .collect(Collectors.toList());
        }
        
    private User findUserById(Long userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        }
        
    private Message findMessageById(Long id) {
        return messageRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        }
    }