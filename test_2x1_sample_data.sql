-- Script para crear datos de prueba con entradas PROMOTIONAL_2X1
-- Ejecutar después de aplicar la migración V1004

-- Insertar ticket_prices con tipo PROMOTIONAL_2X1 para el evento de prueba
INSERT INTO ticket_prices (event_section_id, ticket_type, price, available_quantity, created_at, updated_at) 
VALUES 
-- Para el evento ID 22 (Jazz en el Aire), sección ID 8330 (Platea)
(8330, 'PROMOTIONAL_2X1', 25000.00, 50, NOW(), NOW()),
(8330, 'GENERAL', 50000.00, 100, NOW(), NOW()),
(8330, 'VIP', 75000.00, 25, NOW(), NOW()),
(8330, 'GIFT', 0.00, 10, NOW(), NOW());

-- Verificar qué ticket_prices existen para debugging
SELECT 
    tp.ticket_price_id,
    tp.ticket_type,
    tp.price,
    tp.available_quantity,
    es.section_name,
    e.name as event_name
FROM ticket_prices tp
JOIN event_sections es ON tp.event_section_id = es.event_section_id
JOIN events e ON es.event_id = e.event_id
WHERE e.event_id = 22
ORDER BY tp.ticket_type, tp.price;

-- Mensaje informativo
SELECT 'Datos de prueba creados. Ahora puedes probar la compra de entradas PROMOTIONAL_2X1 desde el frontend.' as mensaje; 