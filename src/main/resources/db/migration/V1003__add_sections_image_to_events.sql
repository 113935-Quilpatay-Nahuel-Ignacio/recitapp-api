-- Agregar columna sections_image a la tabla events
-- V1003__add_sections_image_to_events.sql

-- Agregar la nueva columna para imagen de secciones
ALTER TABLE events 
ADD sections_image VARCHAR(2000);

-- Comentario sobre el uso de la columna
-- Esta columna almacenará la URL de la imagen que muestra las secciones del recinto
-- para ayudar a los compradores a seleccionar sus ubicaciones preferidas 