package com.greentrade.greentrade.mappers;

import org.springframework.stereotype.Component;

import com.greentrade.greentrade.dto.certificate.CertificateCreateRequest;
import com.greentrade.greentrade.dto.certificate.CertificateResponse;
import com.greentrade.greentrade.dto.certificate.CertificateUpdateRequest;
import com.greentrade.greentrade.models.Certificate;
import com.greentrade.greentrade.models.User;

@Component
public class CertificateMapper {
    
    public CertificateResponse toResponse(Certificate certificate) {
        if (certificate == null) {
            return null;
        }
        
        return CertificateResponse.builder()
                .id(certificate.getId())
                .name(certificate.getName())
                .issuer(certificate.getIssuer())
                .issueDate(certificate.getIssueDate())
                .expiryDate(certificate.getExpiryDate())
                .description(certificate.getDescription())
                .filePath(certificate.getFilePath())
                .userId(certificate.getUser() != null ? certificate.getUser().getId() : null)
                .build();
    }
    
    public Certificate createRequestToEntity(CertificateCreateRequest request, User user) {
        if (request == null) {
            return null;
        }
        
        Certificate certificate = new Certificate();
        certificate.setName(request.getName());
        certificate.setIssuer(request.getIssuer());
        certificate.setIssueDate(request.getIssueDate());
        certificate.setExpiryDate(request.getExpiryDate());
        certificate.setDescription(request.getDescription());
        certificate.setUser(user);
        
        return certificate;
    }
    
    public void updateEntityFromRequest(Certificate certificate, CertificateUpdateRequest request, User user) {
        if (certificate == null || request == null) {
            return;
        }
        
        certificate.setName(request.getName());
        certificate.setIssuer(request.getIssuer());
        certificate.setIssueDate(request.getIssueDate());
        certificate.setExpiryDate(request.getExpiryDate());
        certificate.setDescription(request.getDescription());
        if (user != null) {
            certificate.setUser(user);
        }
    }
}