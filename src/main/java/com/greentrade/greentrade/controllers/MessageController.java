package com.greentrade.greentrade.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.greentrade.greentrade.dto.message.MessageCreateRequest;
import com.greentrade.greentrade.dto.message.MessageResponse;
import com.greentrade.greentrade.services.MessageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "API endpoints for managing messages between users")
public class MessageController {

    private final MessageService messageService;

    @Autowired
    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "Get all messages")
    @GetMapping
    public ResponseEntity<List<MessageResponse>> getAllMessages() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }

    @Operation(summary = "Get a specific message")
    @GetMapping("/{id}")
    public ResponseEntity<MessageResponse> getMessageById(@PathVariable Long id) {
        return messageService.getMessageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Send a new message")
    @PostMapping
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody MessageCreateRequest request) {
        MessageResponse newMessage = messageService.sendMessage(request);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newMessage.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(newMessage);
    }

    @Operation(summary = "Mark a message as read")
    @PutMapping("/{id}/mark-read")
    public ResponseEntity<MessageResponse> markAsRead(@PathVariable Long id) {
        try {
            MessageResponse readMessage = messageService.markAsRead(id);
            return ResponseEntity.ok(readMessage);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete a message")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        try {
            messageService.deleteMessage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get received messages for a user")
    @GetMapping("/received/{userId}")
    public ResponseEntity<List<MessageResponse>> getReceivedMessagesForUser(@PathVariable Long userId) {
        try {
            List<MessageResponse> messages = messageService.getReceivedMessagesForUser(userId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get sent messages from a user")
    @GetMapping("/sent/{userId}")
    public ResponseEntity<List<MessageResponse>> getSentMessagesByUser(@PathVariable Long userId) {
        try {
            List<MessageResponse> messages = messageService.getSentMessagesByUser(userId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Get unread messages for a user")
    @GetMapping("/unread/{userId}")
    public ResponseEntity<List<MessageResponse>> getUnreadMessagesForUser(@PathVariable Long userId) {
        try {
            List<MessageResponse> messages = messageService.getUnreadMessagesForUser(userId);
            return ResponseEntity.ok(messages);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}