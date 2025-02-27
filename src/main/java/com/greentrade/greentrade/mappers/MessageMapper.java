package com.greentrade.greentrade.mappers;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.message.MessageCreateRequest;
import com.greentrade.greentrade.dto.message.MessageResponse;
import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.User;

@Component
public class MessageMapper {
    
    public MessageResponse toResponse(Message message) {
        if (message == null) {
            return null;
        }
        
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .receiverId(message.getReceiver().getId())
                .subject(message.getSubject())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .read(message.isRead())
                .build();
    }
    
    public Message createRequestToEntity(MessageCreateRequest request, User sender, User receiver) {
        if (request == null) {
            return null;
        }
        
        Message message = new Message();
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject(request.getSubject());
        message.setContent(request.getContent());
        message.setTimestamp(LocalDateTime.now());
        message.setRead(false);
        
        return message;
    }
}