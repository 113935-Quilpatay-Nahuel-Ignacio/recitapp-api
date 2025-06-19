-- Script para permitir valores NULL en sale_price de la tabla tickets
-- Esto es necesario para entradas de regalo que no tienen precio

-- Paso 1: Verificar estructura actual
DESCRIBE tickets;

-- Paso 2: Permitir valores NULL en sale_price
ALTER TABLE tickets MODIFY sale_price DECIMAL(10,2) NULL;

-- Paso 3: Verificar el cambio
DESCRIBE tickets;

-- Paso 4: Verificar si hay entradas existentes con problemas
SELECT COUNT(*) as tickets_with_null_sale_price 
FROM tickets 
WHERE sale_price IS NULL;

-- Paso 5: Mostrar algunas entradas para verificar
SELECT id, event_id, sale_price, is_gift 
FROM tickets 
ORDER BY id DESC 
LIMIT 10; 