-- Script para agregar el estado VENCIDA a la tabla ticket_statuses
-- Ejecutar este script en la base de datos para habilitar la funcionalidad de tickets vencidos

-- Verificar si el estado VENCIDA ya existe
SELECT COUNT(*) FROM ticket_statuses WHERE name = 'VENCIDA';

-- Agregar el estado VENCIDA solo si no existe
INSERT INTO ticket_statuses (name, description)
SELECT 'VENCIDA', 'Ticket vencido - evento ya pasó'
WHERE NOT EXISTS (
    SELECT 1 FROM ticket_statuses WHERE name = 'VENCIDA'
);

-- Verificar que se agregó correctamente
SELECT * FROM ticket_statuses WHERE name = 'VENCIDA';

-- Opcional: Ver todos los estados disponibles
SELECT * FROM ticket_statuses ORDER BY id; 