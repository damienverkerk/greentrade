-- First, insert certificates
INSERT INTO certificates (id, name, issuer, issue_date, expiry_date, description, file_path) VALUES
(1, 'ISO 14001', 'Bureau Veritas', '2024-01-01', '2025-01-01', 'Environmental management system certificate', 'iso14001.pdf'),
(2, 'FSC Certificate', 'FSC Nederland', '2024-01-01', '2025-01-01', 'Sustainable forestry certificate', 'fsc.pdf');

-- Then insert users with certificate references
INSERT INTO users (name, email, password, role, verification_status, certificate_id) VALUES
('Admin User', 'admin@greentrade.nl', '$2a$10$aWwX5CJY1K9qjD.OB0M7QOQz8CxjFqYfZRB5CPqKhiVtQqcU1OUje', 'ROLE_ADMIN', true, 1),
('Seller Company', 'seller@greentrade.nl', '$2a$10$aWwX5CJY1K9qjD.OB0M7QOQz8CxjFqYfZRB5CPqKhiVtQqcU1OUje', 'ROLE_SELLER', true, 2),
('Test Buyer', 'buyer@greentrade.nl', '$2a$10$aWwX5CJY1K9qjD.OB0M7QOQz8CxjFqYfZRB5CPqKhiVtQqcU1OUje', 'ROLE_BUYER', true, null);

-- Products (seller_id references to the Seller Company user)
INSERT INTO products (name, description, price, sustainability_score, sustainability_certificate, seller_id) VALUES
('Sustainable Office Chair', 'Ergonomic chair made from recycled materials', 299.99, 85, 'ISO14001', 2),
('Bamboo Desk', 'Desk made from sustainable bamboo', 449.99, 90, 'FSC123', 2),
('Eco Lamp', 'Energy-efficient LED lamp from recyclable material', 79.99, 95, 'EnergyStar', 2);

-- Reviews
INSERT INTO reviews (product_id, reviewer_id, score, comment, date) VALUES
(1, 3, 5, 'Excellent sustainable chair, very comfortable!', '2024-01-15 10:00:00'),
(2, 3, 4, 'Beautiful desk, good quality bamboo', '2024-01-16 11:30:00');

-- Messages
INSERT INTO messages (sender_id, receiver_id, subject, content, timestamp, read) VALUES
(3, 2, 'Question about office chair', 'Is this chair height-adjustable?', '2024-01-15 09:00:00', false),
(2, 3, 'RE: Question about office chair', 'Yes, the chair is fully adjustable!', '2024-01-15 09:30:00', false);

-- Transactions
INSERT INTO transactions (buyer_id, product_id, amount, date, status) VALUES
(3, 1, 299.99, '2024-01-20 14:00:00', 'COMPLETED'),
(3, 2, 449.99, '2024-01-21 15:30:00', 'PROCESSING');

-- Product Verifications
INSERT INTO product_verifications (product_id, status, verification_date, reviewer_notes, reviewer_id, submission_date, sustainability_score) VALUES
(1, 'APPROVED', '2024-01-10 10:00:00', 'Product meets all sustainability criteria', 1, '2024-01-09 09:00:00', 85),
(2, 'PENDING', '2024-01-22 11:00:00', NULL, NULL, '2024-01-22 11:00:00', NULL);