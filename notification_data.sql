-- ===================================================================
-- DATOS INICIALES PARA EL SISTEMA DE NOTIFICACIONES RECITAPP
-- ===================================================================

-- Insertar canales de notificación
INSERT INTO notification_channels (name, description, active) VALUES
('EMAIL', 'Notificaciones por correo electrónico', true),
('PUSH', 'Notificaciones push para dispositivos móviles', true),
('SMS', 'Notificaciones por mensajes de texto', true)
ON CONFLICT (name) DO NOTHING;

-- Insertar tipos de notificación
INSERT INTO notification_types (name, description, template) VALUES
('NUEVO_EVENTO', 'Notificación de nuevo evento disponible', 'new-event'),
('POCAS_ENTRADAS', 'Alerta de pocas entradas disponibles', 'low-availability'),
('CANCELACION', 'Notificación de cancelación de evento', 'event-cancelled'),
('MODIFICACION', 'Notificación de modificación de evento', 'event-modified'),
('RECORDATORIO', 'Recordatorio de evento próximo', 'event-reminder'),
('RECOMENDACION', 'Recomendaciones personalizadas de eventos', 'recommendations')
ON CONFLICT (name) DO NOTHING;

-- Insertar plantillas SMS para Twilio
UPDATE notification_types SET template = 'NEW_EVENT' WHERE name = 'NUEVO_EVENTO';
UPDATE notification_types SET template = 'LOW_AVAILABILITY' WHERE name = 'POCAS_ENTRADAS';
UPDATE notification_types SET template = 'EVENT_REMINDER' WHERE name = 'RECORDATORIO';
UPDATE notification_types SET template = 'EVENT_CANCELLED' WHERE name = 'CANCELACION';

-- Verificar datos insertados
SELECT 'Canales de notificación:' as info;
SELECT id, name, description, active FROM notification_channels ORDER BY id;

SELECT 'Tipos de notificación:' as info;
SELECT id, name, description, template FROM notification_types ORDER BY id; 