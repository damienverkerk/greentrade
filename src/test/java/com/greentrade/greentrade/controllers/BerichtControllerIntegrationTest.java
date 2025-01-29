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
import com.greentrade.greentrade.services.BerichtService;

@SpringBootTest
@AutoConfigureMockMvc
class BerichtControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BerichtService berichtService;

    private MessageDTO testBericht;

    @BeforeEach
    @SuppressWarnings("unused")
    void setUp() {
        testBericht = new MessageDTO(
            1L,
            1L, // afzenderId
            2L, // ontvangerId
            "Product vraag",
            "Hallo, is dit product nog beschikbaar?",
            LocalDateTime.now(),
            false
        );
    }

    @Test
    @WithMockUser
    void whenGetAlleBerichten_thenSuccess() throws Exception {
        when(berichtService.getAlleBerichten())
            .thenReturn(Arrays.asList(testBericht));

        mockMvc.perform(get("/api/berichten"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].onderwerp").value("Product vraag"));
    }

    @Test
    @WithMockUser
    void whenGetBerichtById_thenSuccess() throws Exception {
        when(berichtService.getBerichtById(anyLong()))
            .thenReturn(Optional.of(testBericht));

        mockMvc.perform(get("/api/berichten/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.onderwerp").value("Product vraag"));
    }

    @Test
    @WithMockUser
    void whenVerstuurBericht_thenSuccess() throws Exception {
        when(berichtService.verstuurBericht(any(MessageDTO.class)))
            .thenReturn(testBericht);

        mockMvc.perform(post("/api/berichten")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testBericht)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.onderwerp").value("Product vraag"));
    }

    @Test
    @WithMockUser
    void whenMarkeerAlsGelezen_thenSuccess() throws Exception {
        MessageDTO gelezenBericht = new MessageDTO(
            1L, 1L, 2L, "Product vraag",
            "Hallo, is dit product nog beschikbaar?",
            LocalDateTime.now(), true
        );

        when(berichtService.markeerAlsGelezen(anyLong()))
            .thenReturn(gelezenBericht);

        mockMvc.perform(put("/api/berichten/{id}/markeer-gelezen", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gelezen").value(true));
    }

    @Test
    @WithMockUser
    void whenGetOntvangenBerichten_thenSuccess() throws Exception {
        when(berichtService.getOntvangenBerichtenVoorGebruiker(anyLong()))
            .thenReturn(Arrays.asList(testBericht));

        mockMvc.perform(get("/api/berichten/ontvangen/{gebruikerId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ontvangerId").value(2));
    }

    @Test
    @WithMockUser
    void whenGetOngelezenBerichten_thenSuccess() throws Exception {
        when(berichtService.getOngelezenBerichtenVoorGebruiker(anyLong()))
            .thenReturn(Arrays.asList(testBericht));

        mockMvc.perform(get("/api/berichten/ongelezen/{gebruikerId}", 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].gelezen").value(false));
    }

    @Test
    void whenUnauthorizedAccess_thenForbidden() throws Exception {
        mockMvc.perform(get("/api/berichten"))
                .andExpect(status().isForbidden());
    }
}