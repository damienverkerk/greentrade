package com.greentrade.greentrade.controllers;

import java.net.URI;
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
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.greentrade.greentrade.dto.transaction.TransactionCreateRequest;
import com.greentrade.greentrade.dto.transaction.TransactionResponse;
import com.greentrade.greentrade.services.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "API endpoints for transaction management")
public class TransactionController {

    private final TransactionService transactionService;

    @Autowired
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Operation(
        summary = "Get all transactions",
        description = "Retrieves a list of all transactions in the system"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Transactions successfully retrieved",
            content = @Content(schema = @Schema(implementation = TransactionResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        return new ResponseEntity<>(transactionService.getAllTransactions(), HttpStatus.OK);
    }

    @Operation(
        summary = "Get a specific transaction",
        description = "Retrieves a specific transaction based on its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transaction successfully found"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getTransactionById(
            @Parameter(description = "ID of the transaction", required = true)
            @PathVariable Long id) {
        TransactionResponse transaction = transactionService.getTransactionById(id);
        return transaction != null ? new ResponseEntity<>(transaction, HttpStatus.OK) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @Operation(
        summary = "Create a new transaction",
        description = "Creates a new transaction in the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Transaction successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(
        @Parameter(description = "Transaction data", required = true)
        @Valid @RequestBody TransactionCreateRequest request) {
        try {
            TransactionResponse newTransaction = transactionService.createTransaction(request);
            
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newTransaction.getId())
                    .toUri();
            
            return ResponseEntity.created(location).body(newTransaction);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Update the status of a transaction",
        description = "Updates the status of an existing transaction"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Status successfully updated"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @PutMapping("/{id}/status")
    public ResponseEntity<TransactionResponse> updateTransactionStatus(
            @Parameter(description = "ID of the transaction", required = true)
            @PathVariable Long id,
            @Parameter(description = "New status of the transaction", required = true)
            @RequestParam String newStatus) {
        try {
            TransactionResponse updatedTransaction = transactionService.updateTransactionStatus(id, newStatus);
            return new ResponseEntity<>(updatedTransaction, HttpStatus.OK);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Delete a transaction",
        description = "Deletes a transaction from the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Transaction successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Transaction not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTransaction(
            @Parameter(description = "ID of the transaction", required = true)
            @PathVariable Long id) {
        try {
            transactionService.deleteTransaction(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @Operation(
        summary = "Get transactions for a buyer",
        description = "Retrieves all transactions made by a specific buyer"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transactions successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Buyer not found")
    })
    @GetMapping("/buyer/{buyerId}")
    @PreAuthorize("hasRole('BUYER')")
    public ResponseEntity<List<TransactionResponse>> getTransactionsByBuyer(
            @Parameter(description = "ID of the buyer", required = true)
            @PathVariable Long buyerId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsByBuyer(buyerId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @Operation(
        summary = "Get transactions for a seller",
        description = "Retrieves all transactions made by a specific seller"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transactions successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Seller not found")
    })
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<TransactionResponse>> getTransactionsBySeller(
            @Parameter(description = "ID of the seller", required = true)
            @PathVariable Long sellerId) {
        List<TransactionResponse> transactions = transactionService.getTransactionsBySeller(sellerId);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @Operation(
        summary = "Get transactions for a period",
        description = "Retrieves all transactions between two specified dates"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transactions successfully retrieved")
    })
    @GetMapping("/period")
    public ResponseEntity<List<TransactionResponse>> getTransactionsBetweenDates(
            @Parameter(description = "Start date and time (ISO format)", required = true, example = "2024-01-01T00:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @Parameter(description = "End date and time (ISO format)", required = true, example = "2024-12-31T23:59:59")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        List<TransactionResponse> transactions = transactionService.getTransactionsBetweenDates(start, end);
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }
}