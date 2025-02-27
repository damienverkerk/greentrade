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
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository, UserRepository userRepository) {
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public List<MessageDTO> getAllMessages() {
        return messageRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<MessageDTO> getMessageById(Long id) {
        return messageRepository.findById(id)
                .map(this::convertToDTO);
    }

    public MessageDTO sendMessage(MessageDTO messageDTO) {
        Message message = convertToEntity(messageDTO);
        Message savedMessage = messageRepository.save(message);
        return convertToDTO(savedMessage);
    }

    public MessageDTO markAsRead(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + id));
        message.setRead(true);
        Message updatedMessage = messageRepository.save(message);
        return convertToDTO(updatedMessage);
    }

    public void deleteMessage(Long id) {
        if (!messageRepository.existsById(id)) {
            throw new RuntimeException("Message not found with id: " + id);
        }
        messageRepository.deleteById(id);
    }

    public List<MessageDTO> getReceivedMessagesForUser(Long userId) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return messageRepository.findByReceiver(receiver).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getSentMessagesByUser(Long userId) {
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return messageRepository.findBySender(sender).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<MessageDTO> getUnreadMessagesForUser(Long userId) {
        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return messageRepository.findByReceiverAndReadIsFalse(receiver).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private MessageDTO convertToDTO(Message message) {
        return new MessageDTO(
                message.getId(),
                message.getSender().getId(),
                message.getReceiver().getId(),
                message.getSubject(),
                message.getContent(),
                message.getTimestamp(),
                message.isRead()
        );
    }

    private Message convertToEntity(MessageDTO dto) {
        Message message = new Message();
        if (dto.getId() != null) {
            message.setId(dto.getId());
        }
        
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found with id: " + dto.getSenderId()));
        User receiver = userRepository.findById(dto.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found with id: " + dto.getReceiverId()));

        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject(dto.getSubject());
        message.setContent(dto.getContent());
        message.setTimestamp(dto.getTimestamp());
        message.setRead(dto.isRead());

        return message;
    }
}