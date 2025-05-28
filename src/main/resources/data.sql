INSERT IGNORE INTO roles (name, description)
VALUES
    ('ADMIN', 'Administrador del sistema con acceso completo a todas las funcionalidades'),
    ('MODERADOR', 'Moderador de eventos que verifica la legitimidad de eventos y los publica para compradores'),
    ('REGISTRADOR_EVENTO', 'Usuario que puede crear y configurar eventos (manager, organizador, artista)'),
    ('COMPRADOR', 'Usuario estándar que puede buscar, comprar entradas y seguir artistas/eventos');

INSERT IGNORE INTO event_statuses (name, description)
VALUES
    ('PROXIMO', 'Evento anunciado pero aún no hay entradas a la venta'),
    ('EN_VENTA', 'Las entradas están disponibles para compra'),
    ('AGOTADO', 'Todas las entradas han sido vendidas'),
    ('CANCELADO', 'El evento ha sido cancelado'),
    ('FINALIZADO', 'El evento ya ocurrió');

INSERT IGNORE INTO transaction_statuses (name, description)
VALUES
    ('INICIADA', 'La transacción se ha iniciado pero no se ha completado el proceso'),
    ('PROCESANDO', 'La transacción está siendo procesada por el sistema de pagos'),
    ('COMPLETADA', 'La transacción se completó exitosamente'),
    ('FALLIDA', 'La transacción no pudo completarse (problema de pago, fondos insuficientes, etc.)'),
    ('REEMBOLSADA', 'El monto de la transacción ha sido devuelto al usuario');

INSERT IGNORE INTO ticket_statuses (name, description)
VALUES
    ('DISPONIBLE', 'La entrada está disponible para su compra'),
    ('RESERVADA', 'La entrada está temporalmente reservada durante un proceso de compra'),
    ('VENDIDA', 'La entrada ha sido vendida pero aún no ha sido utilizada'),
    ('CANCELADA', 'La entrada ha sido cancelada o reembolsada'),
    ('USADA', 'La entrada ya se utilizó para acceder al evento'),
    ('REGALO', 'La entrada fue asignada como regalo o premio');

INSERT IGNORE INTO notification_channels (name, description, active)
VALUES
    ('EMAIL', 'Notificaciones enviadas por correo electrónico', TRUE),
    ('PUSH', 'Notificaciones enviadas directamente a dispositivos móviles o navegadores', TRUE);

INSERT IGNORE INTO notification_types (name, description)
VALUES
    ('NUEVO_EVENTO', 'Aviso de un nuevo evento de un artista o ubicación que el usuario sigue'),
    ('POCAS_ENTRADAS', 'Alerta cuando quedan pocas entradas disponibles para un evento guardado'),
    ('CANCELACION', 'Aviso de cambios o cancelación de un evento'),
    ('RECORDATORIO', 'Recordatorio de un evento próximo'),
    ('RECOMENDACION', 'Sugerencias personalizadas basadas en los gustos del usuario'),
    ('MODIFICACION', 'Aviso específico de modificaciones en un evento');

INSERT IGNORE INTO payment_methods (name, active, description)
VALUES
    ('MERCADOPAGO', TRUE, 'Pago mediante MercadoPago'),
    ('CREDITO', TRUE, 'Pago con tarjeta de crédito'),
    ('DEBITO', TRUE, 'Pago con tarjeta de débito'),
    ('BILLETERA_VIRTUAL', TRUE, 'Pago con saldo de billetera virtual de Recitapp');

INSERT IGNORE INTO music_genres (name, description)
VALUES
    ('ROCK', 'Música rock en sus diferentes variantes'),
    ('POP', 'Música pop comercial'),
    ('INDIE', 'Música independiente'),
    ('ELECTRONICA', 'Música electrónica y DJ sets'),
    ('FOLKLORE', 'Música folklórica argentina'),
    ('TROPICAL', 'Cumbia, cuarteto y otros ritmos tropicales'),
    ('JAZZ', 'Jazz y fusión'),
    ('BLUES', 'Blues tradicional y contemporáneo'),
    ('METAL', 'Heavy metal y sus subgéneros'),
    ('PUNK', 'Punk rock y hardcore'),
    ('HIP_HOP', 'Hip hop, rap y trap'),
    ('REGGAETON', 'Reggaeton y música urbana'),
    ('ALTERNATIVA', 'Música alternativa y experimental'),
    ('CLASICA', 'Música clásica y de cámara');

-- Este bloque inserta dos venues de ejemplo en la base de datos
-- Cada venue tiene datos completos incluyendo nombre, dirección, capacidad, etc.
-- INSERT INTO venues (name, address, google_maps_url, total_capacity, description, instagram_url, web_url, image, active, registration_date, updated_at)
-- VALUES
--     (
--         'Estadio Mario Alberto Kempes',
--         'Av. Cárcano s/n, X5021 Córdoba',
--         'https://maps.app.goo.gl/FzVrN7tEvJg9Tezj6',
--         57000,
--         'Principal estadio de la ciudad de Córdoba. Sede habitual de eventos deportivos y conciertos de gran magnitud. Inaugurado para el Mundial de 1978, ha sido renovado en múltiples ocasiones para adaptarse a las necesidades actuales.',
--         'https://www.instagram.com/estadiokempescba/',
--         'https://estadiokempes.cba.gov.ar/',
--         'https://upload.wikimedia.org/wikipedia/commons/thumb/8/87/Estadio_Mario_Alberto_Kempes.jpg/1200px-Estadio_Mario_Alberto_Kempes.jpg',
--         TRUE,
--         CURRENT_TIMESTAMP,
--         CURRENT_TIMESTAMP
--     ),
--     (
--         'Quality Espacio',
--         'Av. Cruz Roja Argentina 200, X5016 Córdoba',
--         'https://maps.app.goo.gl/UQEqMBNzXDf1bsBWA',
--         4500,
--         'Moderno recinto multipropósito para eventos musicales y culturales. Cuenta con la mejor tecnología en sonido e iluminación, convirtiéndolo en uno de los espacios preferidos por artistas nacionales e internacionales para presentaciones en la ciudad de Córdoba.',
--         'https://www.instagram.com/qualityespacio/',
--         'https://www.qualityespacio.com/',
--         'https://cordobabn.com/wp-content/uploads/2022/02/quality.jpg',
--         TRUE,
--         CURRENT_TIMESTAMP,
--         CURRENT_TIMESTAMP
--     );

-- Los siguientes bloques crean secciones para los venues insertados anteriormente
-- Cada sección tiene nombre, capacidad, descripción, precio base y estado activo

-- Este bloque inserta la sección "Campo" para el Estadio Mario Alberto Kempes
-- INSERT INTO venue_sections (venue_id, name, capacity, description, base_price, active)
-- SELECT
--     v.id,
--     'Campo',
--     30000,
--     'Zona general de pie con vista directa al escenario',
--     5000.00,
--     TRUE
-- FROM venues v
-- WHERE v.name = 'Estadio Mario Alberto Kempes';

-- Este bloque inserta la sección "Platea Preferencial" para el Estadio Mario Alberto Kempes
-- INSERT INTO venue_sections (venue_id, name, capacity, description, base_price, active)
-- SELECT
--     v.id,
--     'Platea Preferencial',
--     15000,
--     'Asientos numerados con excelente vista al escenario',
--     8000.00,
--     TRUE
-- FROM venues v
-- WHERE v.name = 'Estadio Mario Alberto Kempes';

-- Este bloque inserta la sección "Platea Alta" para el Estadio Mario Alberto Kempes
-- INSERT INTO venue_sections (venue_id, name, capacity, description, base_price, active)
-- SELECT
--     v.id,
--     'Platea Alta',
--     12000,
--     'Asientos en la parte superior del estadio',
--     3500.00,
--     TRUE
-- FROM venues v
-- WHERE v.name = 'Estadio Mario Alberto Kempes';

-- Este bloque inserta la sección "General" para Quality Espacio
-- INSERT INTO venue_sections (venue_id, name, capacity, description, base_price, active)
-- SELECT
--     v.id,
--     'General',
--     3000,
--     'Acceso general de pie',
--     4000.00,
--     TRUE
-- FROM venues v
-- WHERE v.name = 'Quality Espacio';

-- Este bloque inserta la sección "VIP" para Quality Espacio
-- INSERT INTO venue_sections (venue_id, name, capacity, description, base_price, active)
-- SELECT
--     v.id,
--     'VIP',
--     1000,
--     'Zona exclusiva con mejor vista y servicio de bebidas',
--     7500.00,
--     TRUE
-- FROM venues v
-- WHERE v.name = 'Quality Espacio';

-- Este bloque inserta la sección "Palcos" para Quality Espacio
-- INSERT INTO venue_sections (venue_id, name, capacity, description, base_price, active)
-- SELECT
--     v.id,
--     'Palcos',
--     500,
--     'Secciones privadas elevadas con servicio premium',
--     10000.00,
--     TRUE
-- FROM venues v
-- WHERE v.name = 'Quality Espacio';

-- Insertar usuarios de prueba con contraseñas hasheadas con BCrypt
-- Contraseñas originales: admin123, moderador123, password
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(1, 'admin3@recitapp.com', '$2a$10$/j4zhSDyrIozkO6ePYabTOEV4G8JwyKY3Jf.38JK.WNgkA81CQvlm', 'Admin', 'Sistema', '12345678', NOW(), true, 'EMAIL', 1000.00),
(2, 'moderador2@recitapp.com', '$2a$10$0BG2yEAg7QaSGCrlTpTcNeRwoLvoavsjglAVI.4t4ZNZv7reroynC', 'Moderador', 'Eventos', '87654321', NOW(), true, 'EMAIL', 0.00),
(4, 'usuario@recitapp.com', '$2a$10$uv1EOSw7MNtUB.le1uvjK.d9BhuPxdfYwpfOi5XSsxCs4RpI7oKii', 'Usuario', 'Prueba', '11111111', NOW(), true, 'EMAIL', 100.00);

-- Contraseñas de prueba:
-- admin3@recitapp.com: admin123
-- moderador@recitapp.com: moderador123
-- usuario@recitapp.com: password

SELECT 'Script de inicialización de datos ejecutado correctamente' AS mensaje;
SELECT 'Datos insertados:' AS mensaje
UNION SELECT '- Roles insertados: ADMIN, MODERADOR, REGISTRADOR_EVENTO, COMPRADOR'
UNION SELECT '- Estados de evento insertados: PROXIMO, EN_VENTA, AGOTADO, CANCELADO, FINALIZADO'
UNION SELECT '- Estados de transacción insertados: INICIADA, PROCESANDO, COMPLETADA, FALLIDA, REEMBOLSADA'
UNION SELECT '- Estados de ticket insertados: DISPONIBLE, RESERVADA, VENDIDA, CANCELADA, USADA, REGALO'
UNION SELECT '- Canales de notificación insertados: EMAIL, PUSH'
UNION SELECT '- Tipos de notificación insertados: NUEVO_EVENTO, POCAS_ENTRADAS, CANCELACION, RECORDATORIO, RECOMENDACION'
UNION SELECT '- Métodos de pago insertados: MERCADOPAGO, CREDITO, DEBITO, BILLETERA_VIRTUAL'
UNION SELECT '- Géneros musicales básicos insertados'
UNION SELECT '- Usuario admin por defecto creado (cambiar contraseña inmediatamente)';

-- ================================================================================
-- USUARIOS DE PRUEBA PARA RECITAPP
-- ================================================================================
-- Archivo: data.sql
-- Descripción: Inserts para usuarios con contraseñas usuales hasheadas con BCrypt
-- Fecha de creación: 2025-05-28
-- ================================================================================

-- Insertar roles si no existen
INSERT IGNORE INTO roles (id, name, description) VALUES
(1, 'ADMIN', 'Administrador del sistema con acceso completo a todas las funcionalidades'),
(2, 'MODERADOR', 'Moderador de eventos que verifica la legitimidad de eventos y los publica'),
(3, 'REGISTRADOR_EVENTO', 'Usuario que puede crear y configurar eventos (manager, organizador, artista)'),
(4, 'COMPRADOR', 'Usuario estándar que puede buscar, comprar entradas y seguir artistas/eventos');

-- ================================================================================
-- USUARIOS ADMINISTRADORES
-- ================================================================================

-- admin@recitapp.com | Contraseña: admin123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(1, 'admin@recitapp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin', 'Sistema', '10000001', NOW(), true, 'EMAIL', 1000.00);

-- admin2@recitapp.com | Contraseña: password
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(1, 'admin2@recitapp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Admin', 'Secundario', '10000002', NOW(), true, 'EMAIL', 1000.00);

-- superadmin@recitapp.com | Contraseña: super123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(1, 'superadmin@recitapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I6ZqKdvweR.1WR.xYp5YYKGT9/eoKO', 'Super', 'Admin', '10000003', NOW(), true, 'EMAIL', 1000.00);

-- ================================================================================
-- USUARIOS MODERADORES
-- ================================================================================

-- moderador@recitapp.com | Contraseña: moderador123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(2, 'moderador@recitapp.com', '$2a$10$0BG2yEAg7QaSGCrlTpTcNeRwoLvoavsjglAVI.4t4ZNZv7reroynC', 'Moderador', 'Principal', '20000001', NOW(), true, 'EMAIL', 500.00);

-- moderador2@recitapp.com | Contraseña: password
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(2, 'moderador2@recitapp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Moderador', 'Secundario', '20000002', NOW(), true, 'EMAIL', 500.00);

-- mod@recitapp.com | Contraseña: mod123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(2, 'mod@recitapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I6ZqKdvweR.1WR.xYp5YYKGT9/eoKO', 'Mod', 'User', '20000003', NOW(), true, 'EMAIL', 500.00);

-- ================================================================================
-- USUARIOS REGISTRADORES DE EVENTOS
-- ================================================================================

-- registrador@recitapp.com | Contraseña: registrador123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(3, 'registrador@recitapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I6ZqKdvweR.1WR.xYp5YYKGT9/eoKO', 'Registrador', 'Eventos', '30000001', NOW(), true, 'EMAIL', 200.00);

-- registrador2@recitapp.com | Contraseña: password
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(3, 'registrador2@recitapp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Event', 'Manager', '30000002', NOW(), true, 'EMAIL', 200.00);

-- manager@recitapp.com | Contraseña: manager123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(3, 'manager@recitapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I6ZqKdvweR.1WR.xYp5YYKGT9/eoKO', 'Event', 'Manager', '30000003', NOW(), true, 'EMAIL', 200.00);

-- ================================================================================
-- USUARIOS COMPRADORES / USUARIOS REGULARES
-- ================================================================================

-- usuario@recitapp.com | Contraseña: password
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(4, 'usuario@recitapp.com', '$2a$10$uv1EOSw7MNtUB.le1uvjK.d9BhuPxdfYwpfOi5XSsxCs4RpI7oKii', 'Usuario', 'Regular', '40000001', NOW(), true, 'EMAIL', 100.00);

-- test@recitapp.com | Contraseña: test123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(4, 'test@recitapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I6ZqKdvweR.1WR.xYp5YYKGT9/eoKO', 'Usuario', 'Test', '40000002', NOW(), true, 'EMAIL', 100.00);

-- test2@recitapp.com | Contraseña: password
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(4, 'test2@recitapp.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Test', 'Usuario', '40000003', NOW(), true, 'EMAIL', 100.00);

-- demo@recitapp.com | Contraseña: demo123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(4, 'demo@recitapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I6ZqKdvweR.1WR.xYp5YYKGT9/eoKO', 'Demo', 'User', '40000004', NOW(), true, 'EMAIL', 100.00);

-- guest@recitapp.com | Contraseña: guest123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(4, 'guest@recitapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I6ZqKdvweR.1WR.xYp5YYKGT9/eoKO', 'Guest', 'User', '40000005', NOW(), true, 'EMAIL', 100.00);

-- usuario2@recitapp.com | Contraseña: 123456
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(4, 'usuario2@recitapp.com', '$2a$10$6X.UOdWOQ4uYDOXyExMkKOEsb6hqry3qpAIZcQVzDJkqIXqHDrQQ2', 'Usuario', 'Dos', '40000006', NOW(), true, 'EMAIL', 100.00);

-- comprador@recitapp.com | Contraseña: comprador123
INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES
(4, 'comprador@recitapp.com', '$2a$10$N9qo8uLOickgx2ZMRZoMye7I6ZqKdvweR.1WR.xYp5YYKGT9/eoKO', 'Comprador', 'Regular', '40000007', NOW(), true, 'EMAIL', 100.00);

-- ================================================================================
-- RESUMEN DE CREDENCIALES DISPONIBLES
-- ================================================================================
/*
ADMINISTRADORES:
- admin@recitapp.com          | admin123
- admin2@recitapp.com         | password  
- superadmin@recitapp.com     | super123

MODERADORES:
- moderador@recitapp.com      | moderador123
- moderador2@recitapp.com     | password
- mod@recitapp.com            | mod123

REGISTRADORES DE EVENTOS:
- registrador@recitapp.com    | registrador123
- registrador2@recitapp.com   | password
- manager@recitapp.com        | manager123

COMPRADORES/USUARIOS:
- usuario@recitapp.com        | password
- test@recitapp.com           | test123
- test2@recitapp.com          | password
- demo@recitapp.com           | demo123
- guest@recitapp.com          | guest123
- usuario2@recitapp.com       | 123456
- comprador@recitapp.com      | comprador123

CONTRASEÑAS MÁS COMUNES:
- password (múltiples usuarios)
- admin123, moderador123, test123, demo123, guest123
- 123456, super123, mod123, manager123, registrador123, comprador123
*/

-- ================================================================================
-- NOTAS IMPORTANTES
-- ================================================================================
/*
1. Todos los hashes están generados con BCrypt usando el algoritmo $2a$10$
2. Los usuarios tienen diferentes balances de billetera según su rol
3. Todos los usuarios están activos y usan autenticación por EMAIL
4. Los DNI están generados automáticamente para evitar duplicados
5. Para testing rápido, usa: usuario@recitapp.com / password
6. Para testing de admin, usa: admin@recitapp.com / admin123
7. Para testing de moderador, usa: moderador@recitapp.com / moderador123
*/