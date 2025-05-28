# 🗑️ Eliminación en Cascada de Usuarios - RecitApp API

## 📋 Descripción General

Este documento describe la funcionalidad de **eliminación en cascada** implementada para los usuarios en RecitApp. Cuando se elimina un usuario, todos sus datos relacionados se eliminan automáticamente para mantener la integridad referencial de la base de datos.

## ⚠️ **ADVERTENCIA IMPORTANTE**

**La eliminación de usuarios es IRREVERSIBLE**. Una vez eliminado un usuario, todos sus datos relacionados se perderán permanentemente. Se recomienda encarecidamente:

1. **Hacer backup** de la base de datos antes de eliminar usuarios en producción
2. **Verificar los datos relacionados** antes de proceder con la eliminación
3. **Considerar implementar "soft delete"** (eliminación lógica) en lugar de eliminación física

## 🔗 Datos que se Eliminan en Cascada

Cuando se elimina un usuario, se eliminan automáticamente:

### 🔐 Autenticación y Seguridad
- **Refresh Tokens** (`refresh_tokens`)
- **Password Reset Tokens** (`password_reset_tokens`)

### 📧 Notificaciones
- **Preferencias de Notificación** (`notification_preferences`)
- **Historial de Notificaciones** (`notification_history`)

### 💰 Transacciones y Compras
- **Transacciones** (`transactions`)
- **Tickets Comprados** (`tickets`)

### 👤 Actividad del Usuario
- **Eventos Guardados** (`saved_events`)
- **Artistas Seguidos** (`artist_followers`)
- **Venues Seguidos** (`venue_followers`)
- **Entradas en Sala de Espera** (`waiting_room`)

## 🛠️ Implementación Técnica

### 1. Configuración JPA (Entidad User)

```java
@Entity
@Table(name = "users")
public class User {
    // ... campos básicos ...
    
    // Relaciones con eliminación en cascada
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RefreshToken> refreshTokens;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;
    
    // ... más relaciones ...
}
```

### 2. Configuración Base de Datos

```sql
-- Ejemplo de constraint con CASCADE DELETE
ALTER TABLE refresh_tokens 
ADD CONSTRAINT fk_refresh_tokens_user 
FOREIGN KEY (user_id) REFERENCES users(id) 
ON DELETE CASCADE ON UPDATE CASCADE;
```

### 3. Migración de Base de Datos

Ejecutar la migración: `V999__add_cascade_delete_constraints.sql`

```bash
# Si usas Flyway
mvn flyway:migrate

# O ejecutar manualmente el script SQL
mysql -u username -p database_name < V999__add_cascade_delete_constraints.sql
```

## 🔌 Endpoints de API

### Eliminar Usuario
```http
DELETE /users/{id}
Authorization: Bearer {admin_token}
```

**Respuesta:**
```json
{
  "message": "Usuario eliminado exitosamente",
  "userId": "123",
  "warning": "Todos los datos relacionados han sido eliminados permanentemente"
}
```

### Verificar Datos Relacionados (Antes de Eliminar)
```http
GET /users/{id}/related-data
Authorization: Bearer {admin_token}
```

**Respuesta:**
```json
{
  "userId": 123,
  "email": "usuario@example.com",
  "fullName": "Juan Pérez",
  "role": "COMPRADOR",
  "registrationDate": "2024-01-15T10:30:00",
  "active": true,
  "relatedDataCounts": {
    "refreshTokens": 2,
    "passwordResetTokens": 0,
    "notificationHistory": 15,
    "transactions": 5,
    "tickets": 8,
    "savedEvents": 3,
    "artistFollowers": 12,
    "venueFollowers": 4,
    "waitingRoomEntries": 1,
    "notificationPreferences": 1
  },
  "totalRelatedRecords": 51,
  "warnings": [
    "Este usuario tiene 51 registros relacionados que serán eliminados permanentemente",
    "El usuario tiene tickets comprados. La eliminación afectará el historial de ventas",
    "El usuario tiene transacciones registradas. Se perderá el historial financiero"
  ],
  "deletionImpact": "ALTO"
}
```

## 📊 Niveles de Impacto

- **BAJO**: ≤ 5 registros relacionados
- **MEDIO**: 6-10 registros relacionados  
- **ALTO**: > 10 registros relacionados

## 🔒 Seguridad y Permisos

- Solo usuarios con rol **ADMIN** pueden eliminar usuarios
- Se requiere autenticación JWT válida
- Se registran logs detallados de todas las eliminaciones

## 📝 Logs y Auditoría

El sistema registra automáticamente:

```log
INFO  - Iniciando eliminación del usuario con ID: 123
INFO  - Eliminando usuario: Juan Pérez (usuario@example.com)
INFO  - Rol del usuario: COMPRADOR
INFO  - Datos relacionados que serán eliminados en cascada para usuario ID 123:
INFO  - - 2 refresh tokens
INFO  - - 5 transacciones
INFO  - - 8 tickets
INFO  - Usuario con ID 123 eliminado exitosamente junto con todos sus datos relacionados
```

## 🧪 Testing

### Casos de Prueba Recomendados

1. **Usuario sin datos relacionados**
2. **Usuario con pocos datos relacionados**
3. **Usuario con muchos datos relacionados**
4. **Usuario con rol privilegiado (ADMIN/MODERADOR)**
5. **Verificación de integridad referencial post-eliminación**

### Script de Prueba

```bash
# 1. Crear usuario de prueba
curl -X POST /users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","firstName":"Test","lastName":"User",...}'

# 2. Verificar datos relacionados
curl -X GET /users/{id}/related-data \
  -H "Authorization: Bearer {admin_token}"

# 3. Eliminar usuario
curl -X DELETE /users/{id} \
  -H "Authorization: Bearer {admin_token}"

# 4. Verificar que no existen datos relacionados
# (Consultas SQL para verificar tablas relacionadas)
```

## 🚨 Troubleshooting

### Error: Foreign Key Constraint Fails

```sql
-- Verificar constraints existentes
SHOW CREATE TABLE tabla_name;

-- Eliminar constraint problemática
ALTER TABLE tabla_name DROP FOREIGN KEY constraint_name;

-- Volver a ejecutar migración
```

### Error: Cannot Delete User with Related Data

1. Verificar que las constraints CASCADE estén aplicadas
2. Revisar logs para identificar la tabla problemática
3. Ejecutar migración de constraints manualmente

## 📚 Referencias

- [JPA Cascade Types](https://docs.oracle.com/javaee/7/tutorial/persistence-cascade.htm)
- [MySQL Foreign Key Constraints](https://dev.mysql.com/doc/refman/8.0/en/create-table-foreign-keys.html)
- [Spring Data JPA Reference](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)

## 📞 Soporte

Para problemas relacionados con la eliminación en cascada:

1. Revisar logs de aplicación
2. Verificar constraints de base de datos
3. Consultar este documento
4. Contactar al equipo de desarrollo

---

**Última actualización:** 2025-05-28  
**Versión:** 1.0  
**Autor:** Sistema RecitApp 