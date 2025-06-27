# 🔧 Configuración de Credenciales - Recitapp API

## ✅ Configuración Completada

He configurado todos los archivos de configuración con tus credenciales reales:

### 📁 Archivos Creados:

1. **`src/main/resources/application-development.properties`** - Configuración para desarrollo local
2. **`src/main/resources/application-production.properties`** - Configuración para producción
3. **`src/main/resources/firebase/recitapp-niquilpatay-firebase-adminsdk-fbsvc-edc354dbfc.json`** - Credenciales Firebase

### 🗑️ Archivos Eliminados:

- ❌ `application-notifications.properties.example`
- ❌ `firebase-service-account.json.example`

## 🔐 Credenciales Configuradas:

### Base de Datos
- **DB Name**: `recitapp`
- **Usuario**: `root`
- **Password**: `Nachuchi2003#`
- **URL**: `jdbc:mysql://localhost:3306/recitapp`

### Email (Gmail SMTP)
- **Email**: `niquilpatay@gmail.com`
- **App Password**: `gwva eiyh yycb nzdi`

### Twilio SMS
- **Account SID**: `AC16b496cfbdabe24202f6e24df6c8410e`
- **Auth Token**: `1683f062b8a8a0b46e94539108276064`
- **Phone**: `+19209499747`
- **Service SID**: `MG388334981ebb5212cf708b0fee9f8c00`

### MercadoPago (TEST)
- **Access Token**: `TEST-4403492759962042-060223-ccef50ad15229a32784b2504ce3d4f8c-1020599231`
- **Public Key**: `TEST-31b86a3a-dcb7-43c6-bfcd-0426844ad5a8`

### Firebase
- **Project**: `recitapp-niquilpatay`
- **Service Account**: Configurado con clave privada real

## 🛡️ Seguridad

- ✅ Todos los archivos con credenciales están en `.gitignore`
- ✅ No se subirán a Git las credenciales reales
- ✅ Solo existen archivos con credenciales reales (sin ejemplos)

## 🚀 Para Ejecutar:

```bash
# Desarrollo (por defecto)
mvn spring-boot:run

# Producción
mvn spring-boot:run -Dspring.profiles.active=production
```

## 📝 Notas:

- El perfil `development` está activo por defecto
- Para producción, debes cambiar las URLs de MercadoPago a tu dominio real
- Considera usar credenciales de producción de MercadoPago cuando sea necesario
- El directorio `logs/` se ha creado para los archivos de log

---
*Configuración completada el 27/06/2025* 