-- Script de prueba para tickets 2x1
-- Este script crea promociones 2x1 y tickets de ejemplo para probar la funcionalidad

-- 1. Crear una promoción 2x1
INSERT INTO promotions (event_id, name, description, minimum_quantity, discount_percentage, apply_to_total, start_date, end_date, active, promotion_code)
VALUES (
    1, -- Asumiendo que existe un evento con ID 1
    'Promoción 2x1 Especial',
    'Compra una entrada y lleva otra gratis - promoción 2x1',
    2,
    50.00,
    false,
    CURRENT_TIMESTAMP,
    DATEADD('DAY', 30, CURRENT_TIMESTAMP),
    true,
    'PROMO2X1TEST'
);

-- 2. Insertar un ticket con promoción 2x1 (simulando que se acaba de crear)
INSERT INTO tickets (event_id, section_id, status_id, sale_price, identification_code, qr_code, user_id, 
                    assigned_user_first_name, assigned_user_last_name, assigned_user_dni, 
                    purchase_date, is_gift, promotion_id, ticket_type, registration_date, updated_at)
VALUES (
    1, -- event_id
    1, -- section_id 
    (SELECT TOP 1 id FROM ticket_statuses WHERE name = 'VENDIDA'), -- status_id
    25000.00, -- sale_price
    'TEST2X1-001',
    'data:image/png;base64,TEST-QR-2X1-001',
    1, -- user_id
    'Juan Carlos',
    'Pérez',
    '12345678',
    CURRENT_TIMESTAMP,
    false, -- is_gift
    (SELECT TOP 1 id FROM promotions WHERE name = 'Promoción 2x1 Especial'), -- promotion_id
    'PROMOTIONAL_2X1', -- ticket_type
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 3. Insertar un ticket de regalo para comparar
INSERT INTO tickets (event_id, section_id, status_id, sale_price, identification_code, qr_code, user_id, 
                    assigned_user_first_name, assigned_user_last_name, assigned_user_dni, 
                    purchase_date, is_gift, promotion_id, ticket_type, registration_date, updated_at)
VALUES (
    1, -- event_id
    1, -- section_id 
    (SELECT TOP 1 id FROM ticket_statuses WHERE name = 'VENDIDA'), -- status_id
    0.00, -- sale_price
    'TESTGIFT-001',
    'data:image/png;base64,TEST-QR-GIFT-001',
    1, -- user_id
    'María José',
    'González',
    '87654321',
    CURRENT_TIMESTAMP,
    true, -- is_gift
    null, -- promotion_id
    'GIFT', -- ticket_type
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- 4. Insertar un ticket general normal para comparar
INSERT INTO tickets (event_id, section_id, status_id, sale_price, identification_code, qr_code, user_id, 
                    assigned_user_first_name, assigned_user_last_name, assigned_user_dni, 
                    purchase_date, is_gift, promotion_id, ticket_type, registration_date, updated_at)
VALUES (
    1, -- event_id
    1, -- section_id 
    (SELECT TOP 1 id FROM ticket_statuses WHERE name = 'VENDIDA'), -- status_id
    50000.00, -- sale_price
    'TESTGEN-001',
    'data:image/png;base64,TEST-QR-GEN-001',
    1, -- user_id
    'Pedro Luis',
    'Martínez',
    '11223344',
    CURRENT_TIMESTAMP,
    false, -- is_gift
    null, -- promotion_id
    'GENERAL', -- ticket_type
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Consulta para verificar los tickets creados
SELECT t.id, t.identification_code, t.ticket_type, t.is_gift, p.name as promotion_name, t.sale_price, 
       CONCAT(t.assigned_user_first_name, ' ', t.assigned_user_last_name) as attendee_name
FROM tickets t
LEFT JOIN promotions p ON t.promotion_id = p.id
WHERE t.identification_code LIKE 'TEST%'
ORDER BY t.id; 