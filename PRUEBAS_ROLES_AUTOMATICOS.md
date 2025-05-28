# 🧪 Pruebas de Asignación Automática de Roles

## 📋 Guía Rápida de Pruebas

Esta guía te permite probar rápidamente la funcionalidad de asignación automática de roles usando curl o Postman.

## 🔧 Configuración Previa

1. Asegúrate de que la aplicación esté ejecutándose
2. Ten a mano la URL base de tu API (ejemplo: `http://localhost:8080`)

## ⚠️ IMPORTANTE: Endpoints Disponibles

Hay **DOS endpoints de registro diferentes** y **AMBOS ahora tienen asignación automática de roles**:

1. **`/api/auth/register`** - Endpoint principal del frontend (AuthService) - **✅ CON asignación automática**
2. **`/users/register`** - Endpoint directo (UserService) - **✅ CON asignación automática**

**Ambos endpoints funcionan para probar la asignación automática de roles.**

## 🧪 Casos de Prueba

### Opción A: Usando `/users/register` (formato simple)

#### 1. Prueba de Rol ADMIN (`@recitapp-admin.com`)

```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.admin@recitapp-admin.com",
    "password": "admin123",
    "firstName": "Test",
    "lastName": "Admin",
    "dni": "99999001",
    "country": "Argentina",
    "city": "Córdoba"
  }'
```

#### 2. Prueba de Rol MODERADOR (`@recitapp-moderator.com`)

```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.moderator@recitapp-moderator.com",
    "password": "mod123",
    "firstName": "Test",
    "lastName": "Moderator",
    "dni": "99999002",
    "country": "Argentina",
    "city": "Buenos Aires"
  }'
```

#### 3. Prueba de Rol REGISTRADOR_EVENTO (`@recitapp-verifier.com`)

```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.verifier@recitapp-verifier.com",
    "password": "verify123",
    "firstName": "Test",
    "lastName": "Verifier",
    "dni": "99999003",
    "country": "Argentina",
    "city": "Rosario"
  }'
```

#### 4. Prueba de Rol COMPRADOR (dominio normal)

```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.user@gmail.com",
    "password": "user123",
    "firstName": "Test",
    "lastName": "User",
    "dni": "99999004",
    "country": "Argentina",
    "city": "Mendoza"
  }'
```

### Opción B: Usando `/api/auth/register` (formato del frontend)

#### 1. Prueba de Rol ADMIN (`@recitapp-admin.com`)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test2.admin@recitapp-admin.com",
    "password": "Admin123!",
    "confirmPassword": "Admin123!",
    "firstName": "Test2",
    "lastName": "Admin",
    "dni": "99999011",
    "phone": "1234567890",
    "address": "Test Address"
  }'
```

#### 2. Prueba de Rol MODERADOR (`@recitapp-moderator.com`)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test2.moderator@recitapp-moderator.com",
    "password": "Mod123!",
    "confirmPassword": "Mod123!",
    "firstName": "Test2",
    "lastName": "Moderator",
    "dni": "99999012",
    "phone": "1234567891",
    "address": "Test Address"
  }'
```

#### 3. Prueba de Rol REGISTRADOR_EVENTO (`@recitapp-verifier.com`)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test2.verifier@recitapp-verifier.com",
    "password": "Verify123!",
    "confirmPassword": "Verify123!",
    "firstName": "Test2",
    "lastName": "Verifier",
    "dni": "99999013",
    "phone": "1234567892",
    "address": "Test Address"
  }'
```

#### 4. Prueba de Rol COMPRADOR (dominio normal)

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test2.user@gmail.com",
    "password": "User123!",
    "confirmPassword": "User123!",
    "firstName": "Test2",
    "lastName": "User",
    "dni": "99999014",
    "phone": "1234567893",
    "address": "Test Address"
  }'
```

**Resultado esperado para todos:** Usuario creado con el rol correspondiente según el dominio del email.

## 📊 Verificación de Resultados

### Verificar Usuario Creado

```bash
# Reemplaza {user_id} con el ID del usuario creado
curl -X GET http://localhost:8080/users/{user_id}
```

### Verificar Todos los Usuarios (requiere autenticación admin)

```bash
curl -X GET http://localhost:8080/users \
  -H "Authorization: Bearer {admin_token}"
```

## 📝 Logs a Verificar

Después de cada registro, verifica los logs de la aplicación. Deberías ver:

### Para Admin:
```log
INFO - 🔐 ASIGNACIÓN AUTOMÁTICA: Rol ADMIN asignado al email: test.admin@recitapp-admin.com
INFO - ✅ Usuario registrado exitosamente: test.admin@recitapp-admin.com con rol: ADMIN (ID: 1)
```

### Para Moderador:
```log
INFO - 🛡️ ASIGNACIÓN AUTOMÁTICA: Rol MODERADOR asignado al email: test.moderator@recitapp-moderator.com
INFO - ✅ Usuario registrado exitosamente: test.moderator@recitapp-moderator.com con rol: MODERADOR (ID: 2)
```

### Para Verificador:
```log
INFO - 📝 ASIGNACIÓN AUTOMÁTICA: Rol REGISTRADOR_EVENTO asignado al email: test.verifier@recitapp-verifier.com
INFO - ✅ Usuario registrado exitosamente: test.verifier@recitapp-verifier.com con rol: REGISTRADOR_EVENTO (ID: 3)
```

### Para Usuario Normal:
```log
INFO - 👤 Rol por defecto COMPRADOR asignado al email: test.user@gmail.com
INFO - ✅ Usuario registrado exitosamente: test.user@gmail.com con rol: COMPRADOR (ID: 4)
```

## 🔍 Consultas SQL de Verificación

```sql
-- Ver todos los usuarios creados con sus roles
SELECT u.id, u.email, u.first_name, u.last_name, r.name as role, r.id as role_id
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email LIKE '%test.%'
ORDER BY u.id DESC;

-- Verificar usuarios por dominio especial
SELECT 
    u.email,
    r.name as role,
    CASE 
        WHEN u.email LIKE '%@recitapp-admin.com' THEN '✅ Admin Domain'
        WHEN u.email LIKE '%@recitapp-moderator.com' THEN '✅ Moderator Domain'
        WHEN u.email LIKE '%@recitapp-verifier.com' THEN '✅ Verifier Domain'
        ELSE '👤 Regular Domain'
    END as domain_type
FROM users u
JOIN roles r ON u.role_id = r.id
WHERE u.email LIKE '%test.%'
ORDER BY u.registration_date DESC;
```

## ❌ Casos de Error a Probar

### Email Duplicado
```bash
# Intentar registrar el mismo email dos veces
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.admin@recitapp-admin.com",
    "password": "admin123",
    "firstName": "Test2",
    "lastName": "Admin2",
    "dni": "99999005",
    "country": "Argentina",
    "city": "Córdoba"
  }'
```

**Resultado esperado:** Error 400 - "El email ya está registrado"

### DNI Duplicado
```bash
# Intentar registrar el mismo DNI dos veces
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "otro.admin@recitapp-admin.com",
    "password": "admin123",
    "firstName": "Otro",
    "lastName": "Admin",
    "dni": "99999001",
    "country": "Argentina",
    "city": "Córdoba"
  }'
```

**Resultado esperado:** Error 400 - "El DNI ya está registrado"

## 🎯 Checklist de Pruebas

- [ ] ✅ Usuario con `@recitapp-admin.com` obtiene rol ADMIN (ID: 1)
- [ ] ✅ Usuario con `@recitapp-moderator.com` obtiene rol MODERADOR (ID: 2)
- [ ] ✅ Usuario con `@recitapp-verifier.com` obtiene rol REGISTRADOR_EVENTO (ID: 3)
- [ ] ✅ Usuario con dominio normal obtiene rol COMPRADOR (ID: 4)
- [ ] ✅ Logs muestran asignación automática correcta
- [ ] ✅ Emails duplicados son rechazados
- [ ] ✅ DNIs duplicados son rechazados
- [ ] ✅ Normalización de emails funciona (mayúsculas/minúsculas)

## 🚀 Prueba Rápida con Postman

1. Importa esta colección JSON en Postman:

```json
{
  "info": {
    "name": "RecitApp - Pruebas Roles Automáticos",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Registro Admin (/users/register)",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test.admin@recitapp-admin.com\",\n  \"password\": \"admin123\",\n  \"firstName\": \"Test\",\n  \"lastName\": \"Admin\",\n  \"dni\": \"99999001\",\n  \"country\": \"Argentina\",\n  \"city\": \"Córdoba\"\n}"
        },
        "url": {"raw": "{{base_url}}/users/register"}
      }
    },
    {
      "name": "Registro Admin (/api/auth/register)",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"email\": \"test2.admin@recitapp-admin.com\",\n  \"password\": \"Admin123!\",\n  \"confirmPassword\": \"Admin123!\",\n  \"firstName\": \"Test2\",\n  \"lastName\": \"Admin\",\n  \"dni\": \"99999011\",\n  \"phone\": \"1234567890\",\n  \"address\": \"Test Address\"\n}"
        },
        "url": {"raw": "{{base_url}}/api/auth/register"}
      }
    }
  ],
  "variable": [
    {"key": "base_url", "value": "http://localhost:8080"}
  ]
}
```

---

**¡Listo para probar!** 🎉

Ahora **AMBOS endpoints** (`/users/register` y `/api/auth/register`) tienen asignación automática de roles. 