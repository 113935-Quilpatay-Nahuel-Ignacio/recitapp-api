-- Corregir URLs de imágenes del evento 6
-- Opción 1: Establecer como NULL para que use placeholder
UPDATE events SET flyer_image = NULL, sections_image = NULL WHERE id = 6;

-- Opción 2: Usar una imagen de ejemplo válida (descomenta si tienes una imagen real)
-- UPDATE events SET 
--   flyer_image = 'http://localhost:8080/uploads/event-flyers/test.txt',
--   sections_image = NULL 
-- WHERE id = 6;

-- Verificar el cambio
SELECT id, name, flyer_image, sections_image FROM events WHERE id = 6; 