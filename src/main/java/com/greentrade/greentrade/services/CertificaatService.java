package com.greentrade.greentrade.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.greentrade.greentrade.models.Certificate;
import com.greentrade.greentrade.models.User;
import com.greentrade.greentrade.repositories.CertificateRepository;

@Service
public class CertificaatService {

    private final CertificateRepository certificateRepository;

    @Autowired
    public CertificaatService(CertificateRepository certificateRepository) {
        this.certificateRepository = certificateRepository;
    }

    public List<Certificate> getAlleCertificaten() {
        return certificateRepository.findAll();
    }

    public Optional<Certificate> getCertificaatById(Long id) {
        return certificateRepository.findById(id);
    }

    public List<Certificate> getCertificatenVanGebruiker(User user) {
        return certificateRepository.findByUser(user);
    }

    public List<Certificate> getVerlopenCertificaten(LocalDate datum) {
        return certificateRepository.findByVervaldatumBefore(datum);
    }

    public Certificate maakCertificaat(Certificate certificaat) {
        // Hier kunt u extra validatie toevoegen
        return certificateRepository.save(certificaat);
    }

    public Certificate updateCertificaat(Long id, Certificate certificaatDetails) {
        Certificate certificaat = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificaat niet gevonden met id: " + id));

        certificaat.setNaam(certificaatDetails.getNaam());
        certificaat.setUitgever(certificaatDetails.getUitgever());
        certificaat.setUitgifteDatum(certificaatDetails.getUitgifteDatum());
        certificaat.setVervaldatum(certificaatDetails.getVervaldatum());
        certificaat.setBeschrijving(certificaatDetails.getBeschrijving());

        return certificateRepository.save(certificaat);
    }

    public void verwijderCertificaat(Long id) {
        if (!certificateRepository.existsById(id)) {
            throw new RuntimeException("Certificaat niet gevonden met id: " + id);
        }
        certificateRepository.deleteById(id);
    }
}