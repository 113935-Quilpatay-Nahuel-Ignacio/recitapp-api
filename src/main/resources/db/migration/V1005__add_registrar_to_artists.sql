-- Migración para agregar el campo registrar_id a la tabla artists
-- Esto permite rastrear qué usuario registró cada artista

ALTER TABLE artists ADD registrar_id BIGINT;

-- Agregar la clave foránea
ALTER TABLE artists ADD FOREIGN KEY (registrar_id) REFERENCES users(id);

-- Agregar índice para mejorar el rendimiento
CREATE INDEX idx_artists_registrar_id ON artists(registrar_id);

-- Comentario para documentar el cambio
COMMENT ON COLUMN artists.registrar_id IS 'ID del usuario que registró este artista'; 