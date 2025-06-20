-- Agregar columna ticket_type a la tabla tickets
ALTER TABLE tickets ADD ticket_type VARCHAR(50);

-- Actualizar tickets existentes basándose en la lógica actual
-- Primero marcamos todos como GENERAL por defecto
UPDATE tickets SET ticket_type = 'GENERAL';

-- Actualizar tickets de regalo
UPDATE tickets SET ticket_type = 'GIFT' WHERE is_gift = true;

-- Actualizar tickets promocionales 2x1 basándose en el nombre de la promoción
UPDATE tickets 
SET ticket_type = 'PROMOTIONAL_2X1' 
WHERE promotion_id IS NOT NULL 
AND promotion_id IN (
    SELECT id FROM promotions 
    WHERE LOWER(name) LIKE '%2x1%' OR 
          LOWER(name) LIKE '%dos por uno%' OR
          LOWER(description) LIKE '%2x1%' OR 
          LOWER(description) LIKE '%dos por uno%'
);

-- Actualizar otros tickets promocionales
UPDATE tickets 
SET ticket_type = 'PROMOTIONAL' 
WHERE promotion_id IS NOT NULL 
AND ticket_type = 'GENERAL'; -- Solo los que no fueron marcados como 2x1 