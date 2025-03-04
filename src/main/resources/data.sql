INSERT INTO certificates (id, name, issuer, issue_date, expiry_date, description, file_path) 
VALUES
(1, 'ISO 14001', 'Bureau Veritas', '2024-01-01', '2025-01-01', 'Environmental management system certificate', 'iso14001.pdf'),
(2, 'FSC Certificate', 'FSC Nederland', '2024-01-01', '2025-01-01', 'Sustainable forestry certificate', 'fsc.pdf');

-- Then insert users with certificate references
-- Password is 'password123' encoded with BCrypt
INSERT INTO users (id, name, email, password, role, verification_status, certificate_id) 
VALUES
(1, 'Admin User', 'admin@greentrade.nl', '$2a$10$WHMDrzlU95I/ZuUTU2Piie7tFLpi7w8TMzCQQ7Kjb8HyD9qI2YrEG', 'ROLE_ADMIN', true, 1),
(2, 'Seller Company', 'seller@greentrade.nl', '$2a$10$WHMDrzlU95I/ZuUTU2Piie7tFLpi7w8TMzCQQ7Kjb8HyD9qI2YrEG', 'ROLE_SELLER', true, 2),
(3, 'Test Buyer', 'buyer@greentrade.nl', '$2a$10$WHMDrzlU95I/ZuUTU2Piie7tFLpi7w8TMzCQQ7Kjb8HyD9qI2YrEG', 'ROLE_BUYER', true, null);

-- Products (seller_id references to the Seller Company user)
INSERT INTO products (id, name, description, price, sustainability_score, sustainability_certificate, seller_id) 
VALUES
(1, 'Sustainable Office Chair', 'Ergonomic chair made from recycled materials', 299.99, 85, 'ISO14001', 2),
(2, 'Bamboo Desk', 'Desk made from sustainable bamboo', 449.99, 90, 'FSC123', 2),
(3, 'Eco Lamp', 'Energy-efficient LED lamp from recyclable material', 79.99, 95, 'EnergyStar', 2);

-- Reviews
INSERT INTO reviews (id, product_id, reviewer_id, score, comment, date) 
VALUES
(1, 1, 3, 5, 'Excellent sustainable chair, very comfortable!', '2024-01-15 10:00:00'),
(2, 2, 3, 4, 'Beautiful desk, good quality bamboo', '2024-01-16 11:30:00');

-- Messages
INSERT INTO messages (id, sender_id, receiver_id, subject, content, timestamp, read) 
VALUES
(1, 3, 2, 'Question about office chair', 'Is this chair height-adjustable?', '2024-01-15 09:00:00', false),
(2, 2, 3, 'RE: Question about office chair', 'Yes, the chair is fully adjustable!', '2024-01-15 09:30:00', false);

-- Transactions
INSERT INTO transactions (id, buyer_id, product_id, amount, date, status) 
VALUES
(1, 3, 1, 299.99, '2024-01-20 14:00:00', 'COMPLETED'),
(2, 3, 2, 449.99, '2024-01-21 15:30:00', 'PROCESSING');

-- Product Verifications
INSERT INTO product_verifications (id, product_id, status, verification_date, reviewer_notes, reviewer_id, submission_date, sustainability_score) 
VALUES
(1, 1, 'APPROVED', '2024-01-10 10:00:00', 'Product meets all sustainability criteria', 1, '2024-01-09 09:00:00', 85),
(2, 2, 'PENDING', '2024-01-22 11:00:00', NULL, NULL, '2024-01-22 11:00:00', NULL);

-- Reset sequences to continue from the highest ID values
SELECT setval(pg_get_serial_sequence('certificates', 'id'), (SELECT MAX(id) FROM certificates));
SELECT setval(pg_get_serial_sequence('users', 'id'), (SELECT MAX(id) FROM users));
SELECT setval(pg_get_serial_sequence('products', 'id'), (SELECT MAX(id) FROM products));
SELECT setval(pg_get_serial_sequence('messages', 'id'), (SELECT MAX(id) FROM messages));
SELECT setval(pg_get_serial_sequence('reviews', 'id'), (SELECT MAX(id) FROM reviews));
SELECT setval(pg_get_serial_sequence('transactions', 'id'), (SELECT MAX(id) FROM transactions));
SELECT setval(pg_get_serial_sequence('product_verifications', 'id'), (SELECT MAX(id) FROM product_verifications));