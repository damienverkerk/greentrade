package com.greentrade.greentrade.services;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.dto.CertificateDTO;
import com.greentrade.greentrade.models.Certificate;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.CertificateRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@Service
public class CertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public CertificateService(CertificateRepository certificateRepository, 
                            UserRepository userRepository,
                            FileStorageService fileStorageService) {
        this.certificateRepository = certificateRepository;
        this.userRepository = userRepository;
        this.fileStorageService = fileStorageService;
    }

    public List<CertificateDTO> getAllCertificates() {
        return certificateRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CertificateDTO getCertificateById(Long id) {
        return certificateRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<CertificateDTO> getCertificatesForUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return certificateRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CertificateDTO> getExpiredCertificates(LocalDate date) {
        return certificateRepository.findByExpiryDateBefore(date).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CertificateDTO createCertificate(CertificateDTO certificateDTO) {
        Certificate certificate = convertToEntity(certificateDTO);
        Certificate savedCertificate = certificateRepository.save(certificate);
        return convertToDTO(savedCertificate);
    }

    public CertificateDTO updateCertificate(Long id, CertificateDTO certificateDTO) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found with id: " + id));

        updateCertificateFromDTO(certificate, certificateDTO);
        Certificate updatedCertificate = certificateRepository.save(certificate);
        return convertToDTO(updatedCertificate);
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

    public CertificateDTO updateCertificateFile(Long id, String fileName) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found with id: " + id));

        // Delete the old file if it exists
        if (certificate.getFilePath() != null) {
            try {
                fileStorageService.deleteFile(certificate.getFilePath());
            } catch (RuntimeException e) {
                // Log the error, but continue with the update
                System.err.println("Could not delete old file: " + e.getMessage());
            }
        }

        certificate.setFilePath(fileName);
        Certificate updatedCertificate = certificateRepository.save(certificate);
        return convertToDTO(updatedCertificate);
    }

    private CertificateDTO convertToDTO(Certificate certificate) {
        return new CertificateDTO(
            certificate.getId(),
            certificate.getName(),
            certificate.getIssuer(),
            certificate.getIssueDate(),
            certificate.getExpiryDate(),
            certificate.getDescription(),
            certificate.getFilePath(),
            certificate.getUser() != null ? certificate.getUser().getId() : null
        );
    }

    private Certificate convertToEntity(CertificateDTO dto) {
        Certificate certificate = new Certificate();
        updateCertificateFromDTO(certificate, dto);
        return certificate;
    }

    private void updateCertificateFromDTO(Certificate certificate, CertificateDTO dto) {
        certificate.setName(dto.getName());
        certificate.setIssuer(dto.getIssuer());
        certificate.setIssueDate(dto.getIssueDate());
        certificate.setExpiryDate(dto.getExpiryDate());
        certificate.setDescription(dto.getDescription());
        
        // Keep the current file path unless explicitly changed
        if (dto.getFilePath() != null) {
            certificate.setFilePath(dto.getFilePath());
        }

        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getUserId()));
            certificate.setUser(user);
        }
    }
}