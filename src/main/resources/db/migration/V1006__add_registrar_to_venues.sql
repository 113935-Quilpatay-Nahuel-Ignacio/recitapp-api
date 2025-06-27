-- Migración para agregar el campo registrar_id a la tabla venues
-- Esto permite rastrear qué usuario registró cada venue

ALTER TABLE venues ADD registrar_id BIGINT;

-- Agregar la clave foránea
ALTER TABLE venues ADD FOREIGN KEY (registrar_id) REFERENCES users(id);

-- Agregar índice para mejorar el rendimiento
CREATE INDEX idx_venues_registrar_id ON venues(registrar_id); 