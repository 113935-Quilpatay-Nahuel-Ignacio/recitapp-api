-- Script seguro para actualizar precios null en entradas de regalo
-- Evita el error de safe update mode usando la clave primaria

-- Paso 1: Permitir valores null en la columna price
ALTER TABLE ticket_prices MODIFY price DECIMAL(10,2) NULL;

-- Paso 2: Actualizar usando subconsulta con clave primaria (evita safe mode)
UPDATE ticket_prices 
SET price = NULL 
WHERE id IN (
    SELECT * FROM (
        SELECT id FROM ticket_prices WHERE is_gift = true
    ) AS temp
);

-- Paso 3: Verificar los cambios
SELECT id, ticket_type, price, is_gift, promotional_type 
FROM ticket_prices 
WHERE is_gift = true;

-- Paso 4: También actualizar por promotional_type si existe
UPDATE ticket_prices 
SET price = NULL 
WHERE id IN (
    SELECT * FROM (
        SELECT id FROM ticket_prices WHERE promotional_type = 'GIFT'
    ) AS temp
);

-- Paso 5: Verificación final
SELECT 
    COUNT(*) as total_gift_tickets,
    COUNT(CASE WHEN price IS NULL THEN 1 END) as tickets_with_null_price,
    COUNT(CASE WHEN price IS NOT NULL THEN 1 END) as tickets_with_price
FROM ticket_prices 
WHERE is_gift = true OR promotional_type = 'GIFT'; 