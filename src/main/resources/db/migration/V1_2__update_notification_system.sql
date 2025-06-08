-- ===================================================================
-- MIGRACIÓN PARA SINCRONIZAR SISTEMA DE NOTIFICACIONES CON LA IMPLEMENTACIÓN
-- Versión: 1.2
-- Fecha: 2025-01-27
-- ===================================================================

-- 1. Agregar canales que faltan
INSERT INTO notification_channels (name, description, active) 
VALUES ('SMS', 'Notificaciones por mensajes de texto SMS', true)
ON CONFLICT (name) DO NOTHING;

INSERT INTO notification_channels (name, description, active) 
VALUES ('WHATSAPP', 'Notificaciones por WhatsApp Business', true)
ON CONFLICT (name) DO NOTHING;

-- 2. Verificar y agregar tipo RECORDATORIO si no existe
INSERT INTO notification_types (name, description, template) 
VALUES ('RECORDATORIO', 'Recordatorio de evento próximo', 'event-reminder')
ON CONFLICT (name) DO NOTHING;

-- 3. Actualizar templates para los tipos existentes
UPDATE notification_types SET template = 'new-event' WHERE name = 'NUEVO_EVENTO' AND (template IS NULL OR template = '');
UPDATE notification_types SET template = 'low-availability' WHERE name = 'POCAS_ENTRADAS' AND (template IS NULL OR template = '');
UPDATE notification_types SET template = 'event-cancelled' WHERE name = 'CANCELACION' AND (template IS NULL OR template = '');
UPDATE notification_types SET template = 'event-modified' WHERE name = 'MODIFICACION' AND (template IS NULL OR template = '');
UPDATE notification_types SET template = 'recommendations' WHERE name = 'RECOMENDACION' AND (template IS NULL OR template = '');

-- 4. Agregar columnas adicionales si no existen (para futuras mejoras)
-- Nota: Estos ALTER TABLE fallarán si las columnas ya existen, pero es seguro

-- Para almacenar tokens de dispositivos FCM
CREATE TABLE IF NOT EXISTS user_device_tokens (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    device_token VARCHAR(255) NOT NULL,
    device_type VARCHAR(50) NOT NULL, -- 'ANDROID', 'IOS', 'WEB'
    device_name VARCHAR(100),
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, device_token)
);

-- Índices para optimizar consultas
CREATE INDEX IF NOT EXISTS idx_user_device_tokens_user_id ON user_device_tokens(user_id);
CREATE INDEX IF NOT EXISTS idx_user_device_tokens_active ON user_device_tokens(is_active);

-- Para almacenar números de teléfono (si no existe en users)
-- Verificar si la columna phone_number existe en la tabla users
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'users' AND column_name = 'phone_number') THEN
        ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
    END IF;
END
$$;

-- 5. Crear tabla de configuración de notificaciones por evento
CREATE TABLE IF NOT EXISTS notification_event_config (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES events(id) ON DELETE CASCADE,
    notification_type_id BIGINT NOT NULL REFERENCES notification_types(id),
    send_before_hours INTEGER, -- Horas antes del evento para enviar recordatorio
    is_enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(event_id, notification_type_id)
);

-- 6. Crear tabla de métricas de notificaciones
CREATE TABLE IF NOT EXISTS notification_metrics (
    id BIGSERIAL PRIMARY KEY,
    notification_history_id BIGINT NOT NULL REFERENCES notification_history(id) ON DELETE CASCADE,
    delivery_status VARCHAR(50) NOT NULL, -- 'SENT', 'DELIVERED', 'FAILED', 'OPENED', 'CLICKED'
    delivery_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    error_message TEXT,
    provider_response TEXT, -- Respuesta del proveedor (Firebase, Twilio, etc.)
    metadata JSONB -- Datos adicionales como tiempo de entrega, etc.
);

-- Índices para métricas
CREATE INDEX IF NOT EXISTS idx_notification_metrics_status ON notification_metrics(delivery_status);
CREATE INDEX IF NOT EXISTS idx_notification_metrics_timestamp ON notification_metrics(delivery_timestamp);

-- 7. Actualizar preferencias de notificación con nuevas opciones
-- Agregar preferencia para SMS si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'notification_preferences' AND column_name = 'receive_sms_notifications') THEN
        ALTER TABLE notification_preferences ADD COLUMN receive_sms_notifications BOOLEAN DEFAULT true;
    END IF;
END
$$;

-- Agregar preferencia para WhatsApp si no existe
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'notification_preferences' AND column_name = 'receive_whatsapp_notifications') THEN
        ALTER TABLE notification_preferences ADD COLUMN receive_whatsapp_notifications BOOLEAN DEFAULT true;
    END IF;
END
$$;

-- 8. Crear vista para reportes de notificaciones
CREATE OR REPLACE VIEW notification_report AS
SELECT 
    nh.id as notification_id,
    u.first_name || ' ' || u.last_name as user_name,
    u.email,
    nt.name as notification_type,
    nc.name as channel,
    nh.content,
    nh.sent_at,
    nh.read_at,
    CASE WHEN nh.read_at IS NOT NULL THEN 'READ' ELSE 'UNREAD' END as status,
    e.name as related_event_name,
    a.name as related_artist_name,
    v.name as related_venue_name
FROM notification_history nh
JOIN users u ON nh.user_id = u.id
JOIN notification_types nt ON nh.type_id = nt.id
JOIN notification_channels nc ON nh.channel_id = nc.id
LEFT JOIN events e ON nh.related_event_id = e.id
LEFT JOIN artists a ON nh.related_artist_id = a.id
LEFT JOIN venues v ON nh.related_venue_id = v.id;

-- 9. Función para limpiar notificaciones antiguas (más de 6 meses)
CREATE OR REPLACE FUNCTION cleanup_old_notifications()
RETURNS INTEGER AS $$
DECLARE
    deleted_count INTEGER;
BEGIN
    DELETE FROM notification_history 
    WHERE sent_at < NOW() - INTERVAL '6 months';
    
    GET DIAGNOSTICS deleted_count = ROW_COUNT;
    RETURN deleted_count;
END;
$$ LANGUAGE plpgsql;

-- 10. Insertar configuraciones predeterminadas para recordatorios
INSERT INTO notification_event_config (event_id, notification_type_id, send_before_hours, is_enabled)
SELECT 
    e.id as event_id,
    nt.id as notification_type_id,
    24 as send_before_hours, -- 24 horas antes
    true as is_enabled
FROM events e
CROSS JOIN notification_types nt
WHERE nt.name = 'RECORDATORIO'
  AND e.start_date_time > NOW() -- Solo eventos futuros
ON CONFLICT (event_id, notification_type_id) DO NOTHING;

-- 11. Actualizar estadísticas de la tabla
ANALYZE notification_history;
ANALYZE notification_preferences;
ANALYZE notification_types;
ANALYZE notification_channels;

-- Mostrar resumen de la migración
SELECT 'RESUMEN DE MIGRACIÓN:' as info;
SELECT 'Canales disponibles:' as info, COUNT(*) as total FROM notification_channels;
SELECT 'Tipos de notificación:' as info, COUNT(*) as total FROM notification_types;
SELECT 'Usuarios con preferencias:' as info, COUNT(*) as total FROM notification_preferences;
SELECT 'Notificaciones en historial:' as info, COUNT(*) as total FROM notification_history; 