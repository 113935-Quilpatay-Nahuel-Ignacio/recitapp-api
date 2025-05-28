# 🔐 Asignación Automática de Roles - RecitApp API

## 📋 Descripción General

RecitApp incluye una funcionalidad de **asignación automática de roles** para usuarios que se registren con correos electrónicos de dominios especiales. Esta característica permite crear usuarios con roles específicos de forma automática sin necesidad de intervención manual.

## ⚙️ Funcionamiento

### Lógica de Asignación de Roles

Cuando un usuario se registra a través de **cualquiera de los endpoints de registro** (`/api/auth/register` o `/users/register`), el sistema evalúa el dominio del correo electrónico:

- **Si el email termina en `@recitapp-admin.com`** → Se asigna automáticamente el rol **ADMIN** (ID: 1)
- **Si el email termina en `@recitapp-moderator.com`** → Se asigna automáticamente el rol **MODERADOR** (ID: 2)
- **Si el email termina en `@recitapp-verifier.com`** → Se asigna automáticamente el rol **REGISTRADOR_EVENTO** (ID: 3)
- **Para cualquier otro dominio** → Se asigna el rol por defecto **COMPRADOR** (ID: 4)

### Implementación Técnica

La lógica está implementada en **dos servicios**:

#### 1. UserServiceImpl.registerUser() - Endpoint `/users/register`
```java
// En UserServiceImpl.registerUser()
String email = registrationDTO.getEmail().toLowerCase(); // Normalizar a minúsculas

if (email.endsWith("@recitapp-admin.com")) {
    userRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new RecitappException("Rol ADMIN no encontrado"));
    log.info("🔐 ASIGNACIÓN AUTOMÁTICA: Rol ADMIN asignado al email: {}", registrationDTO.getEmail());
} else if (email.endsWith("@recitapp-moderator.com")) {
    userRole = roleRepository.findByName("MODERADOR")
            .orElseThrow(() -> new RecitappException("Rol MODERADOR no encontrado"));
    log.info("🛡️ ASIGNACIÓN AUTOMÁTICA: Rol MODERADOR asignado al email: {}", registrationDTO.getEmail());
} else if (email.endsWith("@recitapp-verifier.com")) {
    userRole = roleRepository.findByName("REGISTRADOR_EVENTO")
            .orElseThrow(() -> new RecitappException("Rol REGISTRADOR_EVENTO no encontrado"));
    log.info("📝 ASIGNACIÓN AUTOMÁTICA: Rol REGISTRADOR_EVENTO asignado al email: {}", registrationDTO.getEmail());
} else {
    userRole = roleRepository.findByName("COMPRADOR")
            .orElseThrow(() -> new RecitappException("Rol COMPRADOR no encontrado"));
    log.info("👤 Rol por defecto COMPRADOR asignado al email: {}", registrationDTO.getEmail());
}
```

#### 2. AuthService.register() - Endpoint `/api/auth/register`
```java
// En AuthService.register()
String email = request.getEmail().toLowerCase(); // Normalizar a minúsculas

if (email.endsWith("@recitapp-admin.com")) {
    userRole = roleRepository.findByName("ADMIN")
            .orElseThrow(() -> new RuntimeException("Rol ADMIN no encontrado"));
    log.info("🔐 ASIGNACIÓN AUTOMÁTICA: Rol ADMIN asignado al email: {}", request.getEmail());
} else if (email.endsWith("@recitapp-moderator.com")) {
    userRole = roleRepository.findByName("MODERADOR")
            .orElseThrow(() -> new RuntimeException("Rol MODERADOR no encontrado"));
    log.info("🛡️ ASIGNACIÓN AUTOMÁTICA: Rol MODERADOR asignado al email: {}", request.getEmail());
} else if (email.endsWith("@recitapp-verifier.com")) {
    userRole = roleRepository.findByName("REGISTRADOR_EVENTO")
            .orElseThrow(() -> new RuntimeException("Rol REGISTRADOR_EVENTO no encontrado"));
    log.info("📝 ASIGNACIÓN AUTOMÁTICA: Rol REGISTRADOR_EVENTO asignado al email: {}", request.getEmail());
} else {
    userRole = roleRepository.findByName("COMPRADOR")
            .orElseThrow(() -> new RuntimeException("Rol COMPRADOR no encontrado"));
    log.info("👤 Rol por defecto COMPRADOR asignado al email: {}", request.getEmail());
}
```

## 📝 Uso de la API

### Ejemplos de Registro

#### Opción A: Endpoint `/users/register` (formato simple)

##### Registro de Administrador
```http
POST /users/register
Content-Type: application/json

{
  "email": "superadmin@recitapp-admin.com",
  "password": "super123",
  "firstName": "Super",
  "lastName": "Admin",
  "dni": "99999999",
  "country": "Argentina",
  "city": "Córdoba"
}
```

##### Registro de Moderador
```http
POST /users/register
Content-Type: application/json

{
  "email": "moderador@recitapp-moderator.com",
  "password": "mod123",
  "firstName": "Moderador",
  "lastName": "Eventos",
  "dni": "88888888",
  "country": "Argentina",
  "city": "Buenos Aires"
}
```

##### Registro de Verificador/Registrador de Eventos
```http
POST /users/register
Content-Type: application/json

{
  "email": "verificador@recitapp-verifier.com",
  "password": "verify123",
  "firstName": "Verificador",
  "lastName": "Eventos",
  "dni": "77777777",
  "country": "Argentina",
  "city": "Rosario"
}
```

#### Opción B: Endpoint `/api/auth/register` (formato del frontend)

##### Registro de Administrador
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "superadmin@recitapp-admin.com",
  "password": "Super123!",
  "confirmPassword": "Super123!",
  "firstName": "Super",
  "lastName": "Admin",
  "dni": "99999999",
  "phone": "1234567890",
  "address": "Córdoba, Argentina"
}
```

##### Registro de Moderador
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "moderador@recitapp-moderator.com",
  "password": "Mod123!",
  "confirmPassword": "Mod123!",
  "firstName": "Moderador",
  "lastName": "Eventos",
  "dni": "88888888",
  "phone": "1234567891",
  "address": "Buenos Aires, Argentina"
}
```

##### Registro de Verificador/Registrador de Eventos
```http
POST /api/auth/register
Content-Type: application/json

{
  "email": "verificador@recitapp-verifier.com",
  "password": "Verify123!",
  "confirmPassword": "Verify123!",
  "firstName": "Verificador",
  "lastName": "Eventos",
  "dni": "77777777",
  "phone": "1234567892",
  "address": "Rosario, Argentina"
}
```

### Respuestas Exitosas

#### Respuesta para Administrador
```json
{
  "id": 123,
  "email": "superadmin@recitapp-admin.com",
  "firstName": "Super",
  "lastName": "Admin",
  "dni": "99999999",
  "country": "Argentina",
  "city": "Córdoba",
  "registrationDate": "2024-05-28T11:45:00",
  "roleName": "ADMIN",
  "authMethod": "EMAIL"
}
```

#### Respuesta para Moderador
```json
{
  "id": 124,
  "email": "moderador@recitapp-moderator.com",
  "firstName": "Moderador",
  "lastName": "Eventos",
  "dni": "88888888",
  "country": "Argentina",
  "city": "Buenos Aires",
  "registrationDate": "2024-05-28T11:46:00",
  "roleName": "MODERADOR",
  "authMethod": "EMAIL"
}
```

#### Respuesta para Verificador
```json
{
  "id": 125,
  "email": "verificador@recitapp-verifier.com",
  "firstName": "Verificador",
  "lastName": "Eventos",
  "dni": "77777777",
  "country": "Argentina",
  "city": "Rosario",
  "registrationDate": "2024-05-28T11:47:00",
  "roleName": "REGISTRADOR_EVENTO",
  "authMethod": "EMAIL"
}
```

## 📝 Logs del Sistema

El sistema registra automáticamente cuando se asignan roles especiales:

```log
INFO  - 🔐 ASIGNACIÓN AUTOMÁTICA: Rol ADMIN asignado al email: superadmin@recitapp-admin.com
INFO  - ✅ Usuario registrado exitosamente: superadmin@recitapp-admin.com con rol: ADMIN (ID: 1)

INFO  - 🛡️ ASIGNACIÓN AUTOMÁTICA: Rol MODERADOR asignado al email: moderador@recitapp-moderator.com
INFO  - ✅ Usuario registrado exitosamente: moderador@recitapp-moderator.com con rol: MODERADOR (ID: 2)

INFO  - 📝 ASIGNACIÓN AUTOMÁTICA: Rol REGISTRADOR_EVENTO asignado al email: verificador@recitapp-verifier.com
INFO  - ✅ Usuario registrado exitosamente: verificador@recitapp-verifier.com con rol: REGISTRADOR_EVENTO (ID: 3)

INFO  - 👤 Rol por defecto COMPRADOR asignado al email: usuario@gmail.com
INFO  - ✅ Usuario registrado exitosamente: usuario@gmail.com con rol: COMPRADOR (ID: 4)
```

## 🧪 Casos de Prueba

### Usuarios que Obtendrán Roles Especiales

✅ **Válidos para rol ADMIN (ID: 1):**
- `superadmin@recitapp-admin.com`
- `admin.principal@recitapp-admin.com`
- `administrador@recitapp-admin.com`
- `test.admin@recitapp-admin.com`
- `cualquier.nombre@recitapp-admin.com`

✅ **Válidos para rol MODERADOR (ID: 2):**
- `moderador@recitapp-moderator.com`
- `mod.principal@recitapp-moderator.com`
- `moderador.eventos@recitapp-moderator.com`
- `test.moderator@recitapp-moderator.com`
- `cualquier.nombre@recitapp-moderator.com`

✅ **Válidos para rol REGISTRADOR_EVENTO (ID: 3):**
- `verificador@recitapp-verifier.com`
- `verifier.principal@recitapp-verifier.com`
- `registrador.eventos@recitapp-verifier.com`
- `test.verifier@recitapp-verifier.com`
- `cualquier.nombre@recitapp-verifier.com`

### Usuarios que Obtendrán Rol COMPRADOR

❌ **NO válidos para roles especiales (obtendrán COMPRADOR ID: 4):**
- `admin@recitapp.com` (dominio diferente)
- `moderador@recitapp.com` (dominio diferente)
- `usuario@gmail.com`
- `test@recitapp-admin.co` (dominio incompleto)
- `admin@recitapp-admin.com.ar` (dominio extendido)
- `mod@recitapp-moderator.co` (dominio incompleto)
- `verify@recitapp-verifier.net` (dominio diferente)

## ⚠️ Consideraciones de Seguridad

### Recomendaciones

1. **Control de Dominios**: Asegúrate de que los dominios especiales estén bajo tu control:
   - `@recitapp-admin.com` (para administradores)
   - `@recitapp-moderator.com` (para moderadores)
   - `@recitapp-verifier.com` (para verificadores/registradores)
2. **Monitoreo**: Revisa regularmente los logs para detectar registros con roles especiales
3. **Validación**: Considera implementar validación adicional (códigos de invitación, etc.)
4. **Auditoría**: Mantén un registro de todos los usuarios con roles privilegiados creados

### Riesgos Potenciales

- Si alguien obtiene acceso a los dominios especiales, podría crear usuarios con privilegios
- No hay validación adicional más allá del dominio del email
- Los usuarios creados automáticamente tienen acceso según su rol asignado

## 🔧 Configuración Avanzada

### Modificar los Dominios Especiales

Para cambiar los dominios que otorgan privilegios, modifica las líneas en `UserServiceImpl.java`:

```java
// Cambiar dominios especiales
if (email.endsWith("@tu-dominio-admin.com")) {
    // Rol ADMIN
} else if (email.endsWith("@tu-dominio-moderator.com")) {
    // Rol MODERADOR
} else if (email.endsWith("@tu-dominio-verifier.com")) {
    // Rol REGISTRADOR_EVENTO
}
```

### Agregar Múltiples Dominios por Rol

```java
String email = registrationDTO.getEmail().toLowerCase();

// Múltiples dominios para ADMIN
if (email.endsWith("@recitapp-admin.com") || 
    email.endsWith("@admin.recitapp.com") ||
    email.endsWith("@superadmin.recitapp.com")) {
    userRole = roleRepository.findByName("ADMIN");
    
// Múltiples dominios para MODERADOR
} else if (email.endsWith("@recitapp-moderator.com") || 
           email.endsWith("@mod.recitapp.com")) {
    userRole = roleRepository.findByName("MODERADOR");
    
// Múltiples dominios para REGISTRADOR_EVENTO
} else if (email.endsWith("@recitapp-verifier.com") || 
           email.endsWith("@verify.recitapp.com")) {
    userRole = roleRepository.findByName("REGISTRADOR_EVENTO");
}
```

### Deshabilitar la Funcionalidad

Para deshabilitar la asignación automática, simplifica el código:

```java
// Siempre asignar rol COMPRADOR (deshabilitar asignación automática)
Role userRole = roleRepository.findByName("COMPRADOR")
        .orElseThrow(() -> new RecitappException("Rol COMPRADOR no encontrado"));
```

### Agregar Nuevo Rol Especial

Para agregar un nuevo dominio especial (ejemplo: `@recitapp-support.com` para soporte):

```java
} else if (email.endsWith("@recitapp-support.com")) {
    userRole = roleRepository.findByName("SOPORTE")
            .orElseThrow(() -> new RecitappException("Rol SOPORTE no encontrado"));
    log.info("🎧 ASIGNACIÓN AUTOMÁTICA: Rol SOPORTE asignado al email: {}", registrationDTO.getEmail());
```

## 📊 Estadísticas y Monitoreo

### Consultas SQL para Usuarios con Roles Especiales

```sql
-- Ver todos los administradores con dominio @recitapp-admin.com
SELECT u.id, u.email, u.first_name, u.last_name, u.registration_date, r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email LIKE '%@recitapp-admin.com'
AND r.name = 'ADMIN'
ORDER BY u.registration_date DESC;

-- Ver todos los moderadores con dominio @recitapp-moderator.com
SELECT u.id, u.email, u.first_name, u.last_name, u.registration_date, r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email LIKE '%@recitapp-moderator.com'
AND r.name = 'MODERADOR'
ORDER BY u.registration_date DESC;

-- Ver todos los verificadores con dominio @recitapp-verifier.com
SELECT u.id, u.email, u.first_name, u.last_name, u.registration_date, r.name as role
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email LIKE '%@recitapp-verifier.com'
AND r.name = 'REGISTRADOR_EVENTO'
ORDER BY u.registration_date DESC;

-- Resumen de usuarios por dominio especial
SELECT 
    CASE 
        WHEN u.email LIKE '%@recitapp-admin.com' THEN 'Admin Domain'
        WHEN u.email LIKE '%@recitapp-moderator.com' THEN 'Moderator Domain'
        WHEN u.email LIKE '%@recitapp-verifier.com' THEN 'Verifier Domain'
        ELSE 'Other Domains'
    END as domain_type,
    r.name as role,
    COUNT(*) as user_count
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email LIKE '%@recitapp-admin.com' 
   OR u.email LIKE '%@recitapp-moderator.com'
   OR u.email LIKE '%@recitapp-verifier.com'
GROUP BY domain_type, r.name
ORDER BY domain_type, r.name;
```

### Endpoints de Verificación

```http
# Ver todos los usuarios con roles especiales
GET /users?role=ADMIN
GET /users?role=MODERADOR
GET /users?role=REGISTRADOR_EVENTO
Authorization: Bearer {admin_token}
```

## 🔄 Historial de Cambios

- **v2.1** (2025-05-28): 
  - ✅ **IMPLEMENTACIÓN COMPLETA**: Asignación automática ahora funciona en **AMBOS endpoints**
  - ✅ Agregada lógica de asignación automática en `AuthService.register()` para `/api/auth/register`
  - ✅ Mantenida lógica existente en `UserServiceImpl.registerUser()` para `/users/register`
  - ✅ Ambos endpoints ahora soportan los tres dominios especiales
  - ✅ Logging unificado con emojis en ambos servicios
  - ✅ Documentación actualizada con ejemplos para ambos endpoints

- **v2.0** (2025-05-28): 
  - ✅ Agregado soporte para múltiples dominios especiales
  - ✅ Asignación automática de rol MODERADOR para `@recitapp-moderator.com`
  - ✅ Asignación automática de rol REGISTRADOR_EVENTO para `@recitapp-verifier.com`
  - ✅ Mejorado logging con emojis y IDs de rol
  - ✅ Normalización de emails a minúsculas para mayor robustez
  - ✅ Documentación actualizada con ejemplos completos

- **v1.0** (2025-05-28): 
  - Implementación inicial de asignación automática de rol ADMIN
  - Funcionalidad activa para dominio `@recitapp-admin.com`
  - Logging básico de asignaciones de rol

## 📞 Soporte

Para problemas relacionados con la asignación automática de roles:

1. Verificar que el dominio del email sea exactamente `@recitapp-admin.com`
2. Revisar logs de aplicación para mensajes de asignación de rol
3. Confirmar que el rol ADMIN existe en la base de datos
4. Verificar que el endpoint `/users/register` esté siendo utilizado

---

**Última actualización:** 2025-05-28  
**Versión:** 2.1  
**Autor:** Sistema RecitApp  
**Estado:** ✅ Funcionalidad completa en ambos endpoints 