# Sistema de Autenticación - RecitApp API

## Descripción

Este documento describe la implementación del sistema de autenticación con Spring Security y JWT en RecitApp API.

## Características Implementadas

- ✅ Autenticación basada en JWT (tokens de 24 horas)
- ✅ Refresh tokens (duración de 7 días)
- ✅ Registro de usuarios con validaciones completas
- ✅ Recuperación de contraseñas por email
- ✅ Autorización por roles (ADMIN, MODERADOR, COMPRADOR)
- ✅ Endpoints protegidos
- ✅ Validación de tokens
- ✅ Logout con invalidación de refresh tokens
- ✅ Configuración CORS
- ✅ Documentación con Swagger

## Endpoints de Autenticación

### 1. Login
```
POST /api/auth/login
Content-Type: application/json

{
  "email": "usuario@recitapp.com",
  "password": "password"
}
```

**Respuesta exitosa:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "userId": 1,
  "email": "usuario@recitapp.com",
  "firstName": "Usuario",
  "lastName": "Prueba",
  "role": "COMPRADOR",
  "expiresIn": 86400000,
  "refreshExpiresIn": 604800000
}
```

### 2. Validar Token
```
POST /api/auth/validate
Content-Type: application/json

{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### 3. Logout
```
POST /api/auth/logout
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 4. Registro de Usuario
```
POST /api/auth/register
Content-Type: application/json

{
  "email": "nuevo@recitapp.com",
  "password": "Password123",
  "confirmPassword": "Password123",
  "firstName": "Nuevo",
  "lastName": "Usuario",
  "dni": "12345678",
  "phone": "+5491123456789",
  "address": "Calle Falsa 123"
}
```

### 5. Renovar Token
```
POST /api/auth/refresh
Content-Type: application/json

{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 6. Solicitar Recuperación de Contraseña
```
POST /api/auth/forgot-password
Content-Type: application/json

{
  "email": "usuario@recitapp.com"
}
```

### 7. Restablecer Contraseña
```
POST /api/auth/reset-password
Content-Type: application/json

{
  "token": "reset-token-uuid",
  "newPassword": "NuevaPassword123",
  "confirmPassword": "NuevaPassword123"
}
```

### 8. Validar Token de Recuperación
```
GET /api/auth/validate-reset-token?token=reset-token-uuid
```

## Endpoints Protegidos

### Perfil de Usuario
```
GET /api/user/profile
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Solo Administradores
```
GET /api/user/admin-only
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

### Moderadores y Administradores
```
GET /api/user/moderator-or-admin
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Usuarios de Prueba

Para probar el sistema, puedes usar estos usuarios (contraseña: "password"):

1. **Administrador**
   - Email: `admin@recitapp.com`
   - Rol: ADMIN

2. **Moderador**
   - Email: `moderador@recitapp.com`
   - Rol: MODERADOR

3. **Usuario Regular**
   - Email: `usuario@recitapp.com`
   - Rol: COMPRADOR

## Configuración

### Variables de Entorno JWT
```properties
# JWT Configuration
jwt.secret=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
jwt.expiration=86400000
jwt.refresh.expiration=604800000

# Password Reset Configuration
app.password-reset.expiration=3600000
app.password-reset.max-attempts=3
app.frontend.url=http://localhost:3000

# Email Configuration (configurar con tus credenciales)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

### Base de Datos
Asegúrate de que las tablas `roles` y `users` estén creadas y pobladas con datos de prueba.

## Uso del Token

Una vez que obtengas el token del endpoint de login, inclúyelo en el header `Authorization` de todas las peticiones protegidas:

```
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

## Swagger UI

Puedes probar todos los endpoints desde la interfaz de Swagger:
```
http://localhost:8080/api/swagger-ui.html
```

## Estructura de Archivos

```
src/main/java/com/recitapp/recitapp_api/
├── config/
│   ├── JwtService.java
│   ├── JwtAuthenticationFilter.java
│   └── SecurityConfig.java
├── modules/user/
│   ├── controller/
│   │   ├── AuthController.java
│   │   └── UserController.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   └── AuthResponse.java
│   ├── service/
│   │   ├── AuthService.java
│   │   └── CustomUserDetailsService.java
│   └── entity/
│       ├── User.java
│       └── Role.java
```

## Validaciones de Registro

El sistema incluye validaciones completas para el registro:

- **Email**: Formato válido y único en el sistema
- **Contraseña**: Mínimo 8 caracteres, debe incluir mayúscula, minúscula y número
- **DNI**: 8 dígitos numéricos y único en el sistema
- **Teléfono**: Formato internacional válido (opcional)
- **Nombres y apellidos**: Entre 2 y 50 caracteres

## Seguridad de Recuperación de Contraseñas

- **Límite de intentos**: Máximo 3 solicitudes por hora por usuario
- **Expiración**: Los tokens de recuperación expiran en 1 hora
- **Uso único**: Cada token solo puede usarse una vez
- **Invalidación**: Se invalidan tokens anteriores al generar uno nuevo

## Próximos Pasos

1. ✅ ~~Implementar registro de usuarios~~
2. ✅ ~~Agregar refresh tokens~~
3. ✅ ~~Implementar recuperación de contraseña~~
4. Agregar rate limiting para endpoints de autenticación
5. Implementar blacklist de tokens JWT
6. Agregar autenticación de dos factores (2FA)
7. Implementar notificaciones de seguridad por email

## Seguridad

- Las contraseñas se almacenan encriptadas con BCrypt
- Los tokens JWT tienen expiración configurable
- CORS está configurado para desarrollo
- Se valida la estructura de los tokens JWT
- Los endpoints están protegidos por roles 