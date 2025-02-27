package com.greentrade.greentrade.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.greentrade.greentrade.models.Certificate;
import com.greentrade.greentrade.models.User;

@Repository
public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findByUser(User user);
    List<Certificate> findByExpiryDateBefore(LocalDate date);
}