package com.greentrade.greentrade.controllers;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.message.MessageCreateRequest;
import com.greentrade.greentrade.dto.message.MessageResponse;
import com.greentrade.greentrade.services.MessageService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never"
})
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    private MessageResponse testMessage;
    private MessageCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        
        testMessage = MessageResponse.builder()
            .id(1L)
            .senderId(1L) 
            .receiverId(2L)
            .subject("Product question")
            .content("Hello, is this product still available?")
            .timestamp(LocalDateTime.now())
            .read(false)
            .build();
            
        
        createRequest = MessageCreateRequest.builder()
            .senderId(1L)
            .receiverId(2L)
            .subject("Product question")
            .content("Hello, is this product still available?")
            .build();
    }

    @Test
    @WithMockUser
    void whenGetAllMessages_thenSuccess() throws Exception {
        
        when(messageService.getAllMessages())
            .thenReturn(Arrays.asList(testMessage));

        
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subject").value("Product question"));
    }

    @Test
    @WithMockUser
    void whenGetMessageById_thenSuccess() throws Exception {
        
        when(messageService.getMessageById(anyLong()))
            .thenReturn(Optional.of(testMessage));

       
        mockMvc.perform(get("/api/messages/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("Product question"));
    }

    @Test
    @WithMockUser
    void whenSendMessage_thenSuccess() throws Exception {
        
        when(messageService.sendMessage(any(MessageCreateRequest.class)))
            .thenReturn(testMessage);

        
        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Product question"));
    }

    @Test
    @WithMockUser
    void whenMarkAsRead_thenSuccess() throws Exception {
        
        MessageResponse readMessage = MessageResponse.builder()
            .id(1L)
            .senderId(1L)
            .receiverId(2L)
            .subject("Product question")
            .content("Hello, is this product still available?")
            .timestamp(LocalDateTime.now())
            .read(true)
            .build();

        when(messageService.markAsRead(anyLong()))
            .thenReturn(readMessage);

        
        mockMvc.perform(put("/api/messages/{id}/mark-read", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));
    }

    @Test
    @WithMockUser
    void whenGetReceivedMessages_thenSuccess() throws Exception {
        
        when(messageService.getReceivedMessagesForUser(anyLong()))
            .thenReturn(Arrays.asList(testMessage));

        
        mockMvc.perform(get("/api/messages/received/{userId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].receiverId").value(2));
    }

    @Test
    @WithMockUser
    void whenGetUnreadMessages_thenSuccess() throws Exception {
        
        when(messageService.getUnreadMessagesForUser(anyLong()))
            .thenReturn(Arrays.asList(testMessage));

        
        mockMvc.perform(get("/api/messages/unread/{userId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].read").value(false));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        
        mockMvc.perform(get("/api/messages"))
                .andExpect(status().isForbidden());
    }
    
    @Test
    @WithMockUser
    void whenGetSentMessages_thenSuccess() throws Exception {
        
        when(messageService.getSentMessagesByUser(anyLong()))
            .thenReturn(Arrays.asList(testMessage));

        
        mockMvc.perform(get("/api/messages/sent/{userId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].senderId").value(1));
    }
    
    @Test
    @WithMockUser
    void whenGetMessageById_notFound_thenNotFound() throws Exception {
        
        when(messageService.getMessageById(99L))
            .thenReturn(Optional.empty());

        
        mockMvc.perform(get("/api/messages/{id}", 99L))
                .andExpect(status().isNotFound());
    }
}