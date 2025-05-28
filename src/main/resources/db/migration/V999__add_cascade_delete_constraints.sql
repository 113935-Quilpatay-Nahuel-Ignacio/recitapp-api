-- ================================================================================
-- MIGRACIÓN: CONFIGURAR ELIMINACIÓN EN CASCADA PARA USUARIO
-- ================================================================================
-- Archivo: V999__add_cascade_delete_constraints.sql
-- Descripción: Agrega restricciones de eliminación en cascada para todas las 
--              tablas que tienen foreign keys hacia la tabla users
-- Fecha: 2025-05-28
-- ================================================================================

-- Deshabilitar verificación de foreign keys temporalmente
SET FOREIGN_KEY_CHECKS = 0;

-- ================================================================================
-- ELIMINAR CONSTRAINTS EXISTENTES
-- ================================================================================

-- Refresh Tokens
ALTER TABLE refresh_tokens DROP FOREIGN KEY IF EXISTS refresh_tokens_ibfk_1;
ALTER TABLE refresh_tokens DROP FOREIGN KEY IF EXISTS fk_refresh_tokens_user;

-- Password Reset Tokens  
ALTER TABLE password_reset_tokens DROP FOREIGN KEY IF EXISTS password_reset_tokens_ibfk_1;
ALTER TABLE password_reset_tokens DROP FOREIGN KEY IF EXISTS fk_password_reset_tokens_user;

-- Notification Preferences
ALTER TABLE notification_preferences DROP FOREIGN KEY IF EXISTS notification_preferences_ibfk_1;
ALTER TABLE notification_preferences DROP FOREIGN KEY IF EXISTS fk_notification_preferences_user;

-- Notification History
ALTER TABLE notification_history DROP FOREIGN KEY IF EXISTS notification_history_ibfk_1;
ALTER TABLE notification_history DROP FOREIGN KEY IF EXISTS fk_notification_history_user;

-- Transactions
ALTER TABLE transactions DROP FOREIGN KEY IF EXISTS transactions_ibfk_1;
ALTER TABLE transactions DROP FOREIGN KEY IF EXISTS fk_transactions_user;

-- Tickets
ALTER TABLE tickets DROP FOREIGN KEY IF EXISTS tickets_ibfk_1;
ALTER TABLE tickets DROP FOREIGN KEY IF EXISTS fk_tickets_user;

-- Saved Events
ALTER TABLE saved_events DROP FOREIGN KEY IF EXISTS saved_events_ibfk_1;
ALTER TABLE saved_events DROP FOREIGN KEY IF EXISTS fk_saved_events_user;

-- Artist Followers
ALTER TABLE artist_followers DROP FOREIGN KEY IF EXISTS artist_followers_ibfk_1;
ALTER TABLE artist_followers DROP FOREIGN KEY IF EXISTS fk_artist_followers_user;

-- Venue Followers
ALTER TABLE venue_followers DROP FOREIGN KEY IF EXISTS venue_followers_ibfk_1;
ALTER TABLE venue_followers DROP FOREIGN KEY IF EXISTS fk_venue_followers_user;

-- Waiting Room
ALTER TABLE waiting_room DROP FOREIGN KEY IF EXISTS waiting_room_ibfk_1;
ALTER TABLE waiting_room DROP FOREIGN KEY IF EXISTS fk_waiting_room_user;

-- ================================================================================
-- AGREGAR NUEVAS CONSTRAINTS CON CASCADE DELETE
-- ================================================================================

-- Refresh Tokens - Eliminar tokens cuando se elimina el usuario
ALTER TABLE refresh_tokens 
ADD CONSTRAINT fk_refresh_tokens_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Password Reset Tokens - Eliminar tokens cuando se elimina el usuario
ALTER TABLE password_reset_tokens 
ADD CONSTRAINT fk_password_reset_tokens_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Notification Preferences - Eliminar preferencias cuando se elimina el usuario
ALTER TABLE notification_preferences 
ADD CONSTRAINT fk_notification_preferences_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Notification History - Eliminar historial cuando se elimina el usuario
ALTER TABLE notification_history 
ADD CONSTRAINT fk_notification_history_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Transactions - Eliminar transacciones cuando se elimina el usuario
ALTER TABLE transactions 
ADD CONSTRAINT fk_transactions_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Tickets - Eliminar tickets cuando se elimina el usuario
ALTER TABLE tickets 
ADD CONSTRAINT fk_tickets_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Saved Events - Eliminar eventos guardados cuando se elimina el usuario
ALTER TABLE saved_events 
ADD CONSTRAINT fk_saved_events_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Artist Followers - Eliminar seguimientos cuando se elimina el usuario
ALTER TABLE artist_followers 
ADD CONSTRAINT fk_artist_followers_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Venue Followers - Eliminar seguimientos cuando se elimina el usuario
ALTER TABLE venue_followers 
ADD CONSTRAINT fk_venue_followers_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- Waiting Room - Eliminar entradas de sala de espera cuando se elimina el usuario
ALTER TABLE waiting_room 
ADD CONSTRAINT fk_waiting_room_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- ================================================================================
-- VERIFICAR CONSTRAINTS ESPECIALES
-- ================================================================================

-- Verificar si existen otras tablas que referencien users
-- (Esta consulta ayuda a identificar foreign keys que puedan haberse omitido)

-- Habilitar verificación de foreign keys nuevamente
SET FOREIGN_KEY_CHECKS = 1;

-- ================================================================================
-- NOTAS IMPORTANTES
-- ================================================================================
/*
1. Esta migración configura eliminación en cascada para TODAS las tablas que 
   referencian la tabla users.

2. Cuando se elimine un usuario, se eliminarán automáticamente:
   - Todos sus refresh tokens
   - Todos sus password reset tokens  
   - Sus preferencias de notificación
   - Todo su historial de notificaciones
   - Todas sus transacciones
   - Todos sus tickets comprados
   - Todos sus eventos guardados
   - Todos sus seguimientos de artistas
   - Todos sus seguimientos de venues
   - Todas sus entradas en sala de espera

3. PRECAUCIÓN: Esta operación es IRREVERSIBLE. Una vez eliminado un usuario,
   todos sus datos relacionados se perderán permanentemente.

4. Se recomienda implementar un "soft delete" (eliminación lógica) en lugar de
   eliminación física para casos de producción.

5. Antes de ejecutar en producción, hacer backup completo de la base de datos.

6. Si ya existen foreign keys, puede ser necesario eliminarlas primero:
   - Verificar constraints existentes con: SHOW CREATE TABLE nombre_tabla;
   - Eliminar constraint existente: ALTER TABLE tabla DROP FOREIGN KEY nombre_constraint;
*/ 