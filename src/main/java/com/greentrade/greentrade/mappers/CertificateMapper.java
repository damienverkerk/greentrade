package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.CertificateDTO;
import com.greentrade.greentrade.models.Certificate;
import com.greentrade.greentrade.models.User;

@Component
public class CertificateMapper {
    
    public CertificateDTO toDTO(Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        
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
    
    public Certificate toEntity(CertificateDTO dto, User user) {
        if (dto == null) {
            return null;
        }
        
        Certificate certificate = new Certificate();
        certificate.setId(dto.getId());
        certificate.setName(dto.getName());
        certificate.setIssuer(dto.getIssuer());
        certificate.setIssueDate(dto.getIssueDate());
        certificate.setExpiryDate(dto.getExpiryDate());
        certificate.setDescription(dto.getDescription());
        certificate.setFilePath(dto.getFilePath());
        certificate.setUser(user);
        
        return certificate;
    }
}