-- Script para agregar el rol VERIFICADOR_ENTRADAS
-- Ejecutar este script en tu base de datos de RecitApp

-- Insertar el nuevo rol VERIFICADOR_ENTRADAS si no existe
INSERT IGNORE INTO roles (name, description)
VALUES ('VERIFICADOR_ENTRADAS', 'Usuario especializado en verificar códigos QR de entradas en eventos');

-- Verificar que se insertó correctamente
SELECT * FROM roles WHERE name = 'VERIFICADOR_ENTRADAS';

-- Opcional: Ver todos los roles disponibles
SELECT id, name, description FROM roles ORDER BY id; 