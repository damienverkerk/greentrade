package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.MessageDTO;
import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.User;

@Component
public class MessageMapper {
    
    public MessageDTO toDTO(Message message) {
        if (message == null) {
            return null;
        }
        
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
    
    public Message toEntity(MessageDTO dto, User sender, User receiver) {
        if (dto == null) {
            return null;
        }
        
        Message message = new Message();
        if (dto.getId() != null) {
            message.setId(dto.getId());
        }
        
        message.setSender(sender);
        message.setReceiver(receiver);
        message.setSubject(dto.getSubject());
        message.setContent(dto.getContent());
        message.setTimestamp(dto.getTimestamp());
        message.setRead(dto.isRead());
        
        return message;
    }
}