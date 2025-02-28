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
        // Arrange - Nothing to arrange here since we're testing data initialization

        // Act & Assert - Verify seed data loaded correctly
        try {
            // Test Certificates
            var certificates = certificateRepository.findAll();
            logger.info("Number of certificates found: {}", certificates.size());
            assertEquals(2, certificates.size(), "There should be 2 certificates");

            // Test Users
            var users = userRepository.findAll();
            logger.info("Number of users found: {}", users.size());
            assertEquals(3, users.size(), "There should be 3 users");
            assertTrue(userRepository.findByEmail("admin@greentrade.nl").isPresent(), 
                "Admin user should exist");

            // Test Products
            var products = productRepository.findAll();
            logger.info("Number of products found: {}", products.size());
            assertEquals(3, products.size(), "There should be 3 products");
            
            // Test Reviews
            var reviews = reviewRepository.findAll();
            logger.info("Number of reviews found: {}", reviews.size());
            assertEquals(2, reviews.size(), "There should be 2 reviews");

            // Test Messages
            var messages = messageRepository.findAll();
            logger.info("Number of messages found: {}", messages.size());
            assertEquals(2, messages.size(), "There should be 2 messages");

            // Test Transactions
            var transactions = transactionRepository.findAll();
            logger.info("Number of transactions found: {}", transactions.size());
            assertEquals(2, transactions.size(), "There should be 2 transactions");

            // Test Verifications
            var verifications = verificationRepository.findAll();
            logger.info("Number of verifications found: {}", verifications.size());
            assertEquals(2, verifications.size(), "There should be 2 verifications");
            
        } catch (Exception e) {
            logger.error("Error during data.sql loading test", e);
            throw e;
        }
    }
}