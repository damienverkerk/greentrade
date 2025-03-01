package com.greentrade.greentrade.controllers;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.greentrade.greentrade.config.FileValidationConfig;
import com.greentrade.greentrade.dto.certificate.CertificateCreateRequest;
import com.greentrade.greentrade.dto.certificate.CertificateResponse;
import com.greentrade.greentrade.dto.certificate.CertificateUpdateRequest;
import com.greentrade.greentrade.exception.file.FileStorageException;
import com.greentrade.greentrade.exception.file.InvalidFileException;
import com.greentrade.greentrade.services.CertificateService;
import com.greentrade.greentrade.services.FileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/certificates")
@Tag(name = "Certificates", description = "API endpoints for managing sustainability certificates")
public class CertificateController {

    private final CertificateService certificateService;
    private final FileStorageService fileStorageService;
    private final FileValidationConfig fileValidationConfig;

    @Autowired
    public CertificateController(CertificateService certificateService, 
                               FileStorageService fileStorageService,
                               FileValidationConfig fileValidationConfig) {
        this.certificateService = certificateService;
        this.fileStorageService = fileStorageService;
        this.fileValidationConfig = fileValidationConfig;
    }

    @Operation(
        summary = "Get all certificates",
        description = "Retrieves a list of all sustainability certificates in the system"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Certificates successfully retrieved",
            content = @Content(schema = @Schema(implementation = CertificateResponse.class))
        )
    })
    @GetMapping
    public ResponseEntity<List<CertificateResponse>> getAllCertificates() {
        List<CertificateResponse> certificates = certificateService.getAllCertificates();
        return ResponseEntity.ok(certificates);
    }

    @Operation(
        summary = "Get a specific certificate",
        description = "Retrieves a specific certificate based on its ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificate successfully found"),
        @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CertificateResponse> getCertificateById(
            @Parameter(description = "ID of the certificate", required = true)
            @PathVariable Long id) {
        CertificateResponse certificate = certificateService.getCertificateById(id);
        return certificate != null ? ResponseEntity.ok(certificate) : ResponseEntity.notFound().build();
    }

    @Operation(
        summary = "Create a new certificate",
        description = "Registers a new sustainability certificate in the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Certificate successfully created"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<CertificateResponse> createCertificate(
            @Parameter(description = "Certificate data", required = true)
            @Valid @RequestBody CertificateCreateRequest request) {
        try {
            CertificateResponse newCertificate = certificateService.createCertificate(request);
            
            URI location = ServletUriComponentsBuilder
                    .fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(newCertificate.getId())
                    .toUri();
            
            return ResponseEntity.created(location).body(newCertificate);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Operation(
        summary = "Update an existing certificate",
        description = "Updates an existing certificate with new data"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificate successfully updated"),
        @ApiResponse(responseCode = "404", description = "Certificate not found"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<CertificateResponse> updateCertificate(
            @Parameter(description = "ID of the certificate", required = true)
            @PathVariable Long id,
            @Parameter(description = "Updated certificate data", required = true)
            @Valid @RequestBody CertificateUpdateRequest request) {
        try {
            CertificateResponse updatedCertificate = certificateService.updateCertificate(id, request);
            return ResponseEntity.ok(updatedCertificate);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Delete a certificate",
        description = "Deletes a certificate from the system"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Certificate successfully deleted"),
        @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCertificate(
            @Parameter(description = "ID of the certificate", required = true)
            @PathVariable Long id) {
        try {
            certificateService.deleteCertificate(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Get certificates for a user",
        description = "Retrieves all certificates linked to a specific user"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Certificates successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CertificateResponse>> getCertificatesForUser(
            @Parameter(description = "ID of the user", required = true)
            @PathVariable Long userId) {
        try {
            List<CertificateResponse> certificates = certificateService.getCertificatesForUser(userId);
            return ResponseEntity.ok(certificates);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Get expired certificates",
        description = "Retrieves all certificates that have expired before a certain date"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Expired certificates successfully retrieved")
    })
    @GetMapping("/expired")
    public ResponseEntity<List<CertificateResponse>> getExpiredCertificates(
            @Parameter(description = "Reference date (ISO format)", required = true, example = "2024-12-31")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<CertificateResponse> expiredCertificates = certificateService.getExpiredCertificates(date);
        return ResponseEntity.ok(expiredCertificates);
    }

    @Operation(
        summary = "Upload a certificate file",
        description = "Upload a PDF or image file for a certificate. " +
                    "Allowed file types: PDF, JPG, PNG."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File successfully uploaded"),
        @ApiResponse(responseCode = "400", description = "Invalid file or file format"),
        @ApiResponse(responseCode = "404", description = "Certificate not found")
    })
    @PostMapping("/{id}/file")
    public ResponseEntity<CertificateResponse> uploadCertificateFile(
        @PathVariable Long id,
        @RequestParam("file") MultipartFile file) {
        
        CertificateResponse certificate = certificateService.getCertificateById(id);
        if (certificate == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            // Validate file type
            String[] allowedExtensions = fileValidationConfig.getAllowedExtensions().toArray(String[]::new);
            fileStorageService.validateFileType(file, allowedExtensions);
            
            // Store file and update certificate
            String fileName = fileStorageService.storeFile(file);
            CertificateResponse updatedCertificate = certificateService.updateCertificateFile(id, fileName);
            
            // Return successful response with updated certificate
            return ResponseEntity.ok(updatedCertificate);
        } catch (InvalidFileException e) {
            // Return 400 Bad Request for invalid files
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @Operation(
        summary = "Download a certificate file",
        description = "Download the file associated with a certificate"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "File successfully downloaded"),
        @ApiResponse(responseCode = "404", description = "File not found")
    })
    @GetMapping("/{id}/file")
    public ResponseEntity<Resource> downloadCertificateFile(
            @Parameter(description = "ID of the certificate", required = true)
            @PathVariable Long id) {
        try {
            CertificateResponse certificate = certificateService.getCertificateById(id);
            if (certificate == null || certificate.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = fileStorageService.loadFileAsResource(certificate.getFilePath());
            String contentType = determineContentType(certificate.getFilePath());
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (FileStorageException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
    }

    @Operation(
        summary = "Delete a certificate file",
        description = "Deletes the file associated with a certificate"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "File successfully deleted"),
        @ApiResponse(responseCode = "404", description = "File not found")
    })
    @DeleteMapping("/{id}/file")
    public ResponseEntity<Void> deleteCertificateFile(
            @Parameter(description = "ID of the certificate", required = true)
            @PathVariable Long id) {
        try {
            CertificateResponse certificate = certificateService.getCertificateById(id);
            if (certificate == null || certificate.getFilePath() == null) {
                return ResponseEntity.notFound().build();
            }

            fileStorageService.deleteFile(certificate.getFilePath());
            certificateService.updateCertificateFile(id, null);
            
            return ResponseEntity.noContent().build();
        } catch (FileStorageException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found");
        }
    }

    private String determineContentType(String fileName) {
        String fileExtension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return switch (fileExtension) {
            case "pdf" -> "application/pdf";
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            default -> "application/octet-stream";
        };
    }
}