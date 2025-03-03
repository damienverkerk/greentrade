package com.greentrade.greentrade.services;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.greentrade.greentrade.dto.message.MessageCreateRequest;
import com.greentrade.greentrade.dto.message.MessageResponse;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.MessageMapper;
import com.greentrade.greentrade.models.Message;
import com.greentrade.greentrade.models.Role;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.MessageRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    private MessageRepository messageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessageMapper messageMapper;

    @InjectMocks
    private MessageService messageService;

    private User sender;
    private User receiver;
    private Message testMessage;
    private MessageResponse testMessageResponse;
    private MessageCreateRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        // Set up users
        sender = User.builder()
                .id(1L)
                .name("Sender")
                .email("sender@example.com")
                .role(Role.ROLE_SELLER)
                .build();

        receiver = User.builder()
                .id(2L)
                .name("Receiver")
                .email("receiver@example.com")
                .role(Role.ROLE_BUYER)
                .build();

        // Set up test message
        testMessage = new Message();
        testMessage.setId(1L);
        testMessage.setSender(sender);
        testMessage.setReceiver(receiver);
        testMessage.setSubject("Test Subject");
        testMessage.setContent("Test Content");
        testMessage.setTimestamp(LocalDateTime.now());
        testMessage.setRead(false);

        // Set up test message response
        testMessageResponse = MessageResponse.builder()
                .id(1L)
                .senderId(1L)
                .receiverId(2L)
                .subject("Test Subject")
                .content("Test Content")
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        // Set up valid create request
        validCreateRequest = MessageCreateRequest.builder()
                .senderId(1L)
                .receiverId(2L)
                .subject("New Message")
                .content("New Content")
                .build();
    }

    @Test
    void getAllMessages_ReturnsAllMessages() {
        // Arrange
        Message message2 = new Message();
        message2.setId(2L);
        message2.setSender(receiver); // Swap sender and receiver
        message2.setReceiver(sender);
        message2.setSubject("Reply Subject");
        message2.setContent("Reply Content");
        message2.setTimestamp(LocalDateTime.now().plusHours(1));
        message2.setRead(true);

        MessageResponse message2Response = MessageResponse.builder()
                .id(2L)
                .senderId(2L)
                .receiverId(1L)
                .subject("Reply Subject")
                .content("Reply Content")
                .timestamp(LocalDateTime.now().plusHours(1))
                .read(true)
                .build();

        when(messageRepository.findAll()).thenReturn(Arrays.asList(testMessage, message2));
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);
        when(messageMapper.toResponse(message2)).thenReturn(message2Response);

        // Act
        List<MessageResponse> result = messageService.getAllMessages();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Subject", result.get(0).getSubject());
        assertEquals("Reply Subject", result.get(1).getSubject());
        verify(messageRepository).findAll();
    }

    @Test
    void getAllMessages_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(messageRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<MessageResponse> result = messageService.getAllMessages();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(messageRepository).findAll();
    }

    @Test
    void getMessageById_ExistingMessage_ReturnsMessage() {
        // Arrange
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        Optional<MessageResponse> result = messageService.getMessageById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        assertEquals("Test Subject", result.get().getSubject());
        assertEquals("Test Content", result.get().getContent());
        verify(messageRepository).findById(1L);
    }

    @Test
    void getMessageById_NonExistingMessage_ReturnsEmptyOptional() {
        // Arrange
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<MessageResponse> result = messageService.getMessageById(999L);

        // Assert
        assertTrue(result.isEmpty());
        verify(messageRepository).findById(999L);
    }

    @Test
    void sendMessage_ValidData_CreatesMessage() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        
        Message newMessage = new Message();
        newMessage.setId(3L);
        newMessage.setSender(sender);
        newMessage.setReceiver(receiver);
        newMessage.setSubject("New Message");
        newMessage.setContent("New Content");
        newMessage.setTimestamp(LocalDateTime.now());
        newMessage.setRead(false);
        
        MessageResponse newMessageResponse = MessageResponse.builder()
                .id(3L)
                .senderId(1L)
                .receiverId(2L)
                .subject("New Message")
                .content("New Content")
                .timestamp(LocalDateTime.now())
                .read(false)
                .build();

        when(messageMapper.createRequestToEntity(validCreateRequest, sender, receiver)).thenReturn(newMessage);
        when(messageRepository.save(newMessage)).thenReturn(newMessage);
        when(messageMapper.toResponse(newMessage)).thenReturn(newMessageResponse);

        // Act
        MessageResponse result = messageService.sendMessage(validCreateRequest);

        // Assert
        assertNotNull(result);
        assertEquals("New Message", result.getSubject());
        assertEquals("New Content", result.getContent());
        assertEquals(1L, result.getSenderId());
        assertEquals(2L, result.getReceiverId());
        verify(messageRepository).save(newMessage);
    }

    @Test
    void sendMessage_SenderNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        MessageCreateRequest invalidRequest = MessageCreateRequest.builder()
                .senderId(999L) // Non-existent sender
                .receiverId(2L)
                .subject("New Message")
                .content("New Content")
                .build();

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> messageService.sendMessage(invalidRequest)
        );
        assertEquals("User not found with ID: 999", thrown.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void sendMessage_ReceiverNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        
        MessageCreateRequest invalidRequest = MessageCreateRequest.builder()
                .senderId(1L)
                .receiverId(999L) // Non-existent receiver
                .subject("New Message")
                .content("New Content")
                .build();

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> messageService.sendMessage(invalidRequest)
        );
        assertEquals("User not found with ID: 999", thrown.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void markAsRead_ExistingMessage_MarksAsRead() {
        // Arrange
        when(messageRepository.findById(1L)).thenReturn(Optional.of(testMessage));
        
        Message readMessage = new Message();
        readMessage.setId(1L);
        readMessage.setSender(sender);
        readMessage.setReceiver(receiver);
        readMessage.setSubject("Test Subject");
        readMessage.setContent("Test Content");
        readMessage.setTimestamp(LocalDateTime.now());
        readMessage.setRead(true); // Message is now read
        
        MessageResponse readMessageResponse = MessageResponse.builder()
                .id(1L)
                .senderId(1L)
                .receiverId(2L)
                .subject("Test Subject")
                .content("Test Content")
                .timestamp(LocalDateTime.now())
                .read(true) // Response shows read=true
                .build();
                
        when(messageRepository.save(any(Message.class))).thenReturn(readMessage);
        when(messageMapper.toResponse(readMessage)).thenReturn(readMessageResponse);

        // Act
        MessageResponse result = messageService.markAsRead(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isRead()); // Message should be marked as read
        verify(messageRepository).save(any(Message.class));
    }

    @Test
    void markAsRead_NonExistingMessage_ThrowsException() {
        // Arrange
        when(messageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> messageService.markAsRead(999L)
        );
        assertEquals("Message not found with id: 999", thrown.getMessage());
        verify(messageRepository, never()).save(any(Message.class));
    }

    @Test
    void deleteMessage_ExistingMessage_DeletesMessage() {
        // Arrange
        when(messageRepository.existsById(1L)).thenReturn(true);
        doNothing().when(messageRepository).deleteById(1L);

        // Act
        messageService.deleteMessage(1L);

        // Assert
        verify(messageRepository).deleteById(1L);
    }

    @Test
    void deleteMessage_NonExistingMessage_ThrowsException() {
        // Arrange
        when(messageRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        RuntimeException thrown = assertThrows(
                RuntimeException.class,
                () -> messageService.deleteMessage(999L)
        );
        assertEquals("Message not found with id: 999", thrown.getMessage());
        verify(messageRepository, never()).deleteById(anyLong());
    }

    @Test
    void getReceivedMessagesForUser_ReturnsMessages() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.findByReceiver(receiver)).thenReturn(Collections.singletonList(testMessage));
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        List<MessageResponse> result = messageService.getReceivedMessagesForUser(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Subject", result.get(0).getSubject());
        assertEquals(1L, result.get(0).getSenderId());
        assertEquals(2L, result.get(0).getReceiverId());
        verify(messageRepository).findByReceiver(receiver);
    }

    @Test
    void getReceivedMessagesForUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> messageService.getReceivedMessagesForUser(999L)
        );
        assertEquals("User not found with ID: 999", thrown.getMessage());
        verify(messageRepository, never()).findByReceiver(any(User.class));
    }

    @Test
    void getSentMessagesByUser_ReturnsMessages() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(sender));
        when(messageRepository.findBySender(sender)).thenReturn(Collections.singletonList(testMessage));
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        List<MessageResponse> result = messageService.getSentMessagesByUser(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Subject", result.get(0).getSubject());
        assertEquals(1L, result.get(0).getSenderId());
        assertEquals(2L, result.get(0).getReceiverId());
        verify(messageRepository).findBySender(sender);
    }

    @Test
    void getSentMessagesByUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> messageService.getSentMessagesByUser(999L)
        );
        assertEquals("User not found with ID: 999", thrown.getMessage());
        verify(messageRepository, never()).findBySender(any(User.class));
    }

    @Test
    void getUnreadMessagesForUser_ReturnsMessages() {
        // Arrange
        when(userRepository.findById(2L)).thenReturn(Optional.of(receiver));
        when(messageRepository.findByReceiverAndReadIsFalse(receiver)).thenReturn(Collections.singletonList(testMessage));
        when(messageMapper.toResponse(testMessage)).thenReturn(testMessageResponse);

        // Act
        List<MessageResponse> result = messageService.getUnreadMessagesForUser(2L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Subject", result.get(0).getSubject());
        assertEquals(1L, result.get(0).getSenderId());
        assertEquals(2L, result.get(0).getReceiverId());
        assertFalse(result.get(0).isRead());
        verify(messageRepository).findByReceiverAndReadIsFalse(receiver);
    }

    @Test
    void getUnreadMessagesForUser_UserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        UserNotFoundException thrown = assertThrows(
                UserNotFoundException.class,
                () -> messageService.getUnreadMessagesForUser(999L)
        );
        assertEquals("User not found with ID: 999", thrown.getMessage());
        verify(messageRepository, never()).findByReceiverAndReadIsFalse(any(User.class));
    }
}