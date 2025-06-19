-- Script para agregar método de pago para entradas de regalo
-- Ejecutar solo si no existe el método de pago GIFT

-- Verificar si existe el método de pago GIFT
SELECT * FROM payment_methods WHERE name = 'GIFT' OR name = 'ENTRADA_REGALO';

-- Insertar método de pago para entradas de regalo si no existe
INSERT INTO payment_methods (name, description, is_active, created_at, updated_at)
SELECT 'GIFT', 'Entrada de Regalo - Sin costo', true, NOW(), NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM payment_methods 
    WHERE name IN ('GIFT', 'ENTRADA_REGALO')
);

-- Verificar la inserción
SELECT * FROM payment_methods WHERE name = 'GIFT';

-- Mostrar todos los métodos de pago disponibles
SELECT id, name, description, is_active 
FROM payment_methods 
ORDER BY id; 