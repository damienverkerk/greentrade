package com.greentrade.greentrade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.greentrade.greentrade.repositories.CertificateRepository;
import com.greentrade.greentrade.repositories.MessageRepository;
import com.greentrade.greentrade.repositories.ProductRepository;
import com.greentrade.greentrade.repositories.ProductVerificationRepository;
import com.greentrade.greentrade.repositories.ReviewRepository;
import com.greentrade.greentrade.repositories.TransactionRepository;
import com.greentrade.greentrade.repositories.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class DataSqlTest {
    
    private static final Logger logger = LoggerFactory.getLogger(DataSqlTest.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CertificateRepository certificateRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private ProductVerificationRepository verificationRepository;

    @Test
    void testDataSqlLoading() {
        try {
            // Test Certificates
            var certificates = certificateRepository.findAll();
            logger.info("Aantal gevonden certificaten: {}", certificates.size());
            assertEquals(2, certificates.size(), "Er moeten 2 certificaten zijn");

            // Test Users
            var users = userRepository.findAll();
            logger.info("Aantal gevonden users: {}", users.size());
            assertEquals(3, users.size(), "Er moeten 3 users zijn");
            assertTrue(userRepository.findByEmail("admin@greentrade.nl").isPresent(), 
                "Admin user moet bestaan");

            // Test Products
            var products = productRepository.findAll();
            logger.info("Aantal gevonden producten: {}", products.size());
            assertEquals(3, products.size(), "Er moeten 3 producten zijn");
            
            // Test Reviews
            var reviews = reviewRepository.findAll();
            logger.info("Aantal gevonden reviews: {}", reviews.size());
            assertEquals(2, reviews.size(), "Er moeten 2 reviews zijn");

            // Test Messages
            var messages = messageRepository.findAll();
            logger.info("Aantal gevonden berichten: {}", messages.size());
            assertEquals(2, messages.size(), "Er moeten 2 berichten zijn");

            // Test Transactions
            var transactions = transactionRepository.findAll();
            logger.info("Aantal gevonden transacties: {}", transactions.size());
            assertEquals(2, transactions.size(), "Er moeten 2 transacties zijn");

            // Test Verifications
            var verifications = verificationRepository.findAll();
            logger.info("Aantal gevonden verificaties: {}", verifications.size());
            assertEquals(2, verifications.size(), "Er moeten 2 verificaties zijn");
            
        } catch (Exception e) {
            logger.error("Fout tijdens het testen van data.sql loading", e);
            throw e;
        }
    }
}