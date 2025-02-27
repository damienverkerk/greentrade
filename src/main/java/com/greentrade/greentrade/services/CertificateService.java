package com.greentrade.greentrade.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.certificate.CertificateCreateRequest;
import com.greentrade.greentrade.dto.certificate.CertificateResponse;
import com.greentrade.greentrade.dto.certificate.CertificateUpdateRequest;
import com.greentrade.greentrade.exception.security.UserNotFoundException;
import com.greentrade.greentrade.mappers.CertificateMapper;
import com.greentrade.greentrade.models.Certificate;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.CertificateRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final CertificateMapper certificateMapper;

    @Autowired
    public CertificateService(
            CertificateRepository certificateRepository, 
            UserRepository userRepository,
            FileStorageService fileStorageService,
            CertificateMapper certificateMapper) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
        this.certificateMapper = certificateMapper;
    }

    public List<CertificateResponse> getAllCertificates() {
        return certificateRepository.findAll().stream()
                .map(certificateMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CertificateResponse getCertificateById(Long id) {
        return certificateRepository.findById(id)
                .map(certificateMapper::toResponse)
                .orElse(null);
    }

    public List<CertificateResponse> getCertificatesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        return certificateRepository.findByUser(user).stream()
                .map(certificateMapper::toResponse)
                .collect(Collectors.toList());
    }

    public List<CertificateResponse> getExpiredCertificates(LocalDate date) {
        return certificateRepository.findByExpiryDateBefore(date).stream()
                .map(certificateMapper::toResponse)
                .collect(Collectors.toList());
    }

    public CertificateResponse createCertificate(CertificateCreateRequest request) {
        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        }
        
        Certificate certificate = certificateMapper.createRequestToEntity(request, user);
        Certificate savedCertificate = certificateRepository.save(certificate);
        return certificateMapper.toResponse(savedCertificate);
    }

    public CertificateResponse updateCertificate(Long id, CertificateUpdateRequest request) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found with id: " + id));

        User user = null;
        if (request.getUserId() != null) {
            user = userRepository.findById(request.getUserId())
                    .orElseThrow(() -> new UserNotFoundException(request.getUserId()));
        }
        
        certificateMapper.updateEntityFromRequest(certificate, request, user);
        Certificate updatedCertificate = certificateRepository.save(certificate);
        return certificateMapper.toResponse(updatedCertificate);
    }

    public void deleteCertificate(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found with id: " + id));

        // Delete the file if it exists
        if (certificate.getFilePath() != null) {
            try {
                fileStorageService.deleteFile(certificate.getFilePath());
            } catch (RuntimeException e) {
                // Log the error, but continue deleting the certificate
                System.err.println("Could not delete file: " + e.getMessage());
            }
        }

        certificateRepository.deleteById(id);
    }

    public CertificateResponse updateCertificateFile(Long id, String fileName) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found with id: " + id));

        // Delete the old file if it exists
        if (certificate.getFilePath() != null) {
            try {
                // src/main/java/com/greentrade/greentrade/services/CertificateService.java (continued)
                fileStorageService.deleteFile(certificate.getFilePath());
            } catch (RuntimeException e) {
                // Log the error, but continue with the update
                System.err.println("Could not delete old file: " + e.getMessage());
            }
        }

        certificate.setFilePath(fileName);
        Certificate updatedCertificate = certificateRepository.save(certificate);
        return certificateMapper.toResponse(updatedCertificate);
    }
}