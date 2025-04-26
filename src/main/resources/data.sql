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
    ('RECOMENDACION', 'Sugerencias personalizadas basadas en los gustos del usuario');

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

INSERT IGNORE INTO users (role_id, email, password, first_name, last_name, dni, registration_date, active, auth_method, wallet_balance)
SELECT
    r.id,
    'admin@recitapp.com',
    '$2a$10$RBnWiXe9fWXZ7bAOhX8iZepGpBBk2C9fDAV75hWLc5MFRRdnKGJPi', -- Cambiar inmediatamente
    'Administrador',
    'Sistema',
    '00000000',
    CURRENT_TIMESTAMP,
    TRUE,
    'email',
    0.00
FROM roles r
WHERE r.name = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM users WHERE email = 'admin@recitapp.com'
);

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