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

    public List<CertificateDTO> getAlleCertificaten() {
        return certificateRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CertificateDTO getCertificaatById(Long id) {
        return certificateRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public List<CertificateDTO> getCertificatenVanGebruiker(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + userId));
        
        return certificateRepository.findByUser(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<CertificateDTO> getVerlopenCertificaten(LocalDate datum) {
        return certificateRepository.findByVervaldatumBefore(datum).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CertificateDTO maakCertificaat(CertificateDTO certificateDTO) {
        Certificate certificate = convertToEntity(certificateDTO);
        Certificate savedCertificate = certificateRepository.save(certificate);
        return convertToDTO(savedCertificate);
    }

    public CertificateDTO updateCertificaat(Long id, CertificateDTO certificateDTO) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificaat niet gevonden met id: " + id));

        updateCertificateFromDTO(certificate, certificateDTO);
        Certificate updatedCertificate = certificateRepository.save(certificate);
        return convertToDTO(updatedCertificate);
    }

    public void verwijderCertificaat(Long id) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificaat niet gevonden met id: " + id));

        // Verwijder het bestand als het bestaat
        if (certificate.getBestandsPad() != null) {
            try {
                fileStorageService.deleteFile(certificate.getBestandsPad());
            } catch (RuntimeException e) {
                // Log de fout, maar ga door met verwijderen van het certificaat
                System.err.println("Kon bestand niet verwijderen: " + e.getMessage());
            }
        }

        certificateRepository.deleteById(id);
    }

    public CertificateDTO updateCertificaatBestand(Long id, String bestandsNaam) {
        Certificate certificate = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificaat niet gevonden met id: " + id));

        // Verwijder het oude bestand als het bestaat
        if (certificate.getBestandsPad() != null) {
            try {
                fileStorageService.deleteFile(certificate.getBestandsPad());
            } catch (RuntimeException e) {
                // Log de fout, maar ga door met updaten
                System.err.println("Kon oud bestand niet verwijderen: " + e.getMessage());
            }
        }

        certificate.setBestandsPad(bestandsNaam);
        Certificate updatedCertificate = certificateRepository.save(certificate);
        return convertToDTO(updatedCertificate);
    }

    private CertificateDTO convertToDTO(Certificate certificate) {
        return new CertificateDTO(
            certificate.getId(),
            certificate.getNaam(),
            certificate.getUitgever(),
            certificate.getUitgifteDatum(),
            certificate.getVervaldatum(),
            certificate.getBeschrijving(),
            certificate.getBestandsPad(),
            certificate.getUser() != null ? certificate.getUser().getId() : null
        );
    }

    private Certificate convertToEntity(CertificateDTO dto) {
        Certificate certificate = new Certificate();
        updateCertificateFromDTO(certificate, dto);
        return certificate;
    }

    private void updateCertificateFromDTO(Certificate certificate, CertificateDTO dto) {
        certificate.setNaam(dto.getNaam());
        certificate.setUitgever(dto.getUitgever());
        certificate.setUitgifteDatum(dto.getUitgifteDatum());
        certificate.setVervaldatum(dto.getVervaldatum());
        certificate.setBeschrijving(dto.getBeschrijving());
        
        // Behoud het huidige bestandspad tenzij expliciet gewijzigd
        if (dto.getBestandsPad() != null) {
            certificate.setBestandsPad(dto.getBestandsPad());
        }

        if (dto.getUserId() != null) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Gebruiker niet gevonden met id: " + dto.getUserId()));
            certificate.setUser(user);
        }
    }
}