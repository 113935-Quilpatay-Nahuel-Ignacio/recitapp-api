-- Script para insertar usuarios de prueba con contraseñas encriptadas
-- Contraseñas: admin123, moderador123, usuario123

-- Insertar roles si no existen
INSERT IGNORE INTO roles (id, name, description) VALUES 
(1, 'ADMIN', 'Administrador del sistema con acceso completo a todas las funcionalidades'),
(2, 'MODERADOR', 'Moderador de eventos que verifica la legitimidad de eventos y los publica para compradores'),
(3, 'REGISTRADOR_EVENTO', 'Usuario que puede crear y configurar eventos (manager, organizador, artista)'),
(4, 'COMPRADOR', 'Usuario estándar que puede buscar, comprar entradas y seguir artistas/eventos');

-- Insertar usuarios de prueba
-- Contraseñas: admin123, moderador123, usuario123, password
INSERT IGNORE INTO users (id, role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance) VALUES 
(1, 1, 'admin@recitapp.com', '$2a$10$/j4zhSDyrIozkO6ePYabTOEV4G8JwyKY3Jf.38JK.WNgkA81CQvlm', 'Admin', 'Sistema', '12345678', NOW(), true, 'EMAIL', 0.00),
(2, 2, 'moderador@recitapp.com', '$2a$10$0BG2yEAg7QaSGCrlTpTcNeRwoLvoavsjglAVI.4t4ZNZv7reroynC', 'Moderador', 'Eventos', '87654321', NOW(), true, 'EMAIL', 0.00),
(3, 4, 'usuario@recitapp.com', '$2a$10$uv1EOSw7MNtUB.le1uvjK.d9BhuPxdfYwpfOi5XSsxCs4RpI7oKii', 'Usuario', 'Prueba', '11111111', NOW(), true, 'EMAIL', 100.00);

-- Contraseñas de prueba:
-- admin@recitapp.com: admin123
-- moderador@recitapp.com: moderador123  
-- usuario@recitapp.com: password 