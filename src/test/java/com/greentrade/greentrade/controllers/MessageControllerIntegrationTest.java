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
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.greentrade.greentrade.dto.MessageDTO;
import com.greentrade.greentrade.services.MessageService;

@SpringBootTest
@AutoConfigureMockMvc
class MessageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MessageService messageService;

    private MessageDTO testMessage;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testMessage = new MessageDTO(
            1L,
            1L, // senderId
            2L, // receiverId
            "Product question",
            "Hello, is this product still available?",
            LocalDateTime.now(),
            false
        );
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
        when(messageService.sendMessage(any(MessageDTO.class)))
            .thenReturn(testMessage);

        mockMvc.perform(post("/api/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testMessage)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.subject").value("Product question"));
    }

    @Test
    @WithMockUser
    void whenMarkAsRead_thenSuccess() throws Exception {
        MessageDTO readMessage = new MessageDTO(
            1L, 1L, 2L, "Product question",
            "Hello, is this product still available?",
            LocalDateTime.now(), true
        );

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
}