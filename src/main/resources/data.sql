-- Eerst de certificaten
INSERT INTO certificaten (id, naam, uitgever, uitgiftedatum, vervaldatum, beschrijving, bestandspad) VALUES
(1, 'ISO 14001', 'Bureau Veritas', '2024-01-01', '2025-01-01', 'Milieumanagementsysteem certificaat', 'iso14001.pdf'),
(2, 'FSC Certificaat', 'FSC Nederland', '2024-01-01', '2025-01-01', 'Duurzaam houtbeheer certificaat', 'fsc.pdf');

-- Dan de users met de certificate_id referentie
INSERT INTO users (naam, email, wachtwoord, role, verificatie_status, certificate_id) VALUES
('Admin User', 'admin@greentrade.nl', '$2a$10$aWwX5CJY1K9qjD.OB0M7QOQz8CxjFqYfZRB5CPqKhiVtQqcU1OUje', 'ROLE_ADMIN', true, 1),
('Verkoper Bedrijf', 'verkoper@greentrade.nl', '$2a$10$aWwX5CJY1K9qjD.OB0M7QOQz8CxjFqYfZRB5CPqKhiVtQqcU1OUje', 'ROLE_VERKOPER', true, 2),
('Test Koper', 'koper@greentrade.nl', '$2a$10$aWwX5CJY1K9qjD.OB0M7QOQz8CxjFqYfZRB5CPqKhiVtQqcU1OUje', 'ROLE_KOPER', true, null);

-- Products (verkoper_id verwijst naar de Verkoper Bedrijf user)
INSERT INTO producten (naam, beschrijving, prijs, duurzaamheids_score, duurzaamheids_certificaat, verkoper_id) VALUES
('Duurzame Bureaustoel', 'Ergonomische stoel gemaakt van gerecycled materiaal', 299.99, 85, 'ISO14001', 2),
('Bamboe Bureau', 'Bureau gemaakt van duurzaam bamboe', 449.99, 90, 'FSC123', 2),
('Eco Lamp', 'Energiezuinige LED lamp van recyclebaar materiaal', 79.99, 95, 'EnergyStar', 2);

-- Reviews
INSERT INTO beoordelingen (product_id, recensent_id, score, commentaar, datum) VALUES
(1, 3, 5, 'Uitstekende duurzame stoel, zeer comfortabel!', '2024-01-15 10:00:00'),
(2, 3, 4, 'Mooi bureau, goede kwaliteit bamboe', '2024-01-16 11:30:00');

-- Messages
INSERT INTO berichten (afzender_id, ontvanger_id, onderwerp, inhoud, datum_tijd, gelezen) VALUES
(3, 2, 'Vraag over bureaustoel', 'Is deze stoel verstelbaar in hoogte?', '2024-01-15 09:00:00', false),
(2, 3, 'RE: Vraag over bureaustoel', 'Ja, de stoel is volledig verstelbaar!', '2024-01-15 09:30:00', false);

-- Transactions
INSERT INTO transacties (koper_id, product_id, bedrag, datum, status) VALUES
(3, 1, 299.99, '2024-01-20 14:00:00', 'VOLTOOID'),
(3, 2, 449.99, '2024-01-21 15:30:00', 'IN_BEHANDELING');

-- Product Verifications
INSERT INTO product_verifications (product_id, status, verification_date, reviewer_notes, reviewer_id, submission_date, sustainability_score) VALUES
(1, 'APPROVED', '2024-01-10 10:00:00', 'Product voldoet aan alle duurzaamheidscriteria', 1, '2024-01-09 09:00:00', 85),
(2, 'PENDING', '2024-01-22 11:00:00', NULL, NULL, '2024-01-22 11:00:00', NULL);