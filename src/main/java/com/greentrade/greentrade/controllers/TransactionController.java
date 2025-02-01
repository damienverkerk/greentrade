package com.greentrade.greentrade.controllers;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.greentrade.greentrade.dto.TransactionDTO;
import com.greentrade.greentrade.services.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/transacties")
@Tag(name = "Transacties", description = "API endpoints voor transactie management")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
        summary = "Haal alle transacties op",
        description = "Haalt een lijst van alle transacties op in het systeem"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Transacties succesvol opgehaald",
            content = @Content(schema = @Schema(implementation = TransactionDTO.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<TransactionDTO>> getAlleTransacties() {
        return new ResponseEntity<>(transactionService.getAlleTransacties(), HttpStatus.OK);
    }

    @Operation(
        summary = "Haal een specifieke transactie op",
        description = "Haalt een specifieke transactie op basis van het ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transactie succesvol gevonden"),
        @ApiResponse(responseCode = "404", description = "Transactie niet gevonden")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> getTransactieById(
            @Parameter(description = "ID van de transactie", required = true)
            @PathVariable Long id) {
        TransactionDTO transactie = transactionService.getTransactieById(id);
        return transactie != null ? new ResponseEntity<>(transactie, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(
        summary = "Maak een nieuwe transactie aan",
        description = "Maakt een nieuwe transactie aan in het systeem"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transactie succesvol aangemaakt"),
        @ApiResponse(responseCode = "400", description = "Ongeldige invoergegevens")
    })
    @PostMapping
    public ResponseEntity<TransactionDTO> maakTransactie(
        @Parameter(description = "Transactie gegevens", required = true)
        @RequestBody TransactionDTO transactieDTO) {
        try {
            TransactionDTO nieuweTransactie = transactionService.maakTransactie(transactieDTO);
            return new ResponseEntity<>(nieuweTransactie, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Update de status van een transactie",
        description = "Werkt de status van een bestaande transactie bij"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status succesvol bijgewerkt"),
        @ApiResponse(responseCode = "404", description = "Transactie niet gevonden")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionDTO> updateTransactieStatus(
            @Parameter(description = "ID van de transactie", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nieuwe status van de transactie", required = true)
            @RequestParam String nieuweStatus) {
        try {
            TransactionDTO updatedTransactie = transactionService.updateTransactieStatus(id, nieuweStatus);
            return new ResponseEntity<>(updatedTransactie, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Verwijder een transactie",
        description = "Verwijdert een transactie uit het systeem"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Transactie succesvol verwijderd"),
        @ApiResponse(responseCode = "404", description = "Transactie niet gevonden")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> verwijderTransactie(
            @Parameter(description = "ID van de transactie", required = true)
            @PathVariable Long id) {
        try {
            transactionService.verwijderTransactie(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Haal transacties van een koper op",
        description = "Haalt alle transacties op die zijn uitgevoerd door een specifieke koper"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacties succesvol opgehaald"),
        @ApiResponse(responseCode = "404", description = "Koper niet gevonden")
    })
    @GetMapping("/koper/{koperId}")
    @PreAuthorize("hasRole('KOPER')")
    public ResponseEntity<List<TransactionDTO>> getTransactiesDoorKoper(
            @Parameter(description = "ID van de koper", required = true)
            @PathVariable Long koperId) {
        List<TransactionDTO> transacties = transactionService.getTransactiesDoorKoper(koperId);
        return new ResponseEntity<>(transacties, HttpStatus.OK);
    }

    @Operation(
        summary = "Haal transacties van een verkoper op",
        description = "Haalt alle transacties op die zijn uitgevoerd door een specifieke verkoper"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacties succesvol opgehaald"),
        @ApiResponse(responseCode = "404", description = "Verkoper niet gevonden")
    })
    @GetMapping("/verkoper/{verkoperId}")
    public ResponseEntity<List<TransactionDTO>> getTransactiesDoorVerkoper(
            @Parameter(description = "ID van de verkoper", required = true)
            @PathVariable Long verkoperId) {
        List<TransactionDTO> transacties = transactionService.getTransactiesDoorVerkoper(verkoperId);
        return new ResponseEntity<>(transacties, HttpStatus.OK);
    }

    @Operation(
        summary = "Haal transacties op voor een periode",
        description = "Haalt alle transacties op tussen twee opgegeven datums"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transacties succesvol opgehaald")
    })
    @GetMapping("/periode")
    public ResponseEntity<List<TransactionDTO>> getTransactiesTussenData(
            @Parameter(description = "Startdatum en tijd (ISO format)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "Einddatum en tijd (ISO format)", required = true, example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime eind) {
        List<TransactionDTO> transacties = transactionService.getTransactiesTussenData(start, eind);
        return new ResponseEntity<>(transacties, HttpStatus.OK);
    }
}