# 🔒 Security Guidelines - RecitApp API

## Configuración de Credenciales

### ⚠️ IMPORTANTE: NUNCA commit credenciales reales al repositorio

### Firebase Service Account

1. **Ubicación**: `src/main/resources/firebase/firebase-service-account.json`
2. **Origen**: Firebase Console > Project Settings > Service Accounts > Generate Private Key
3. **Plantilla**: Usar `firebase-service-account.json.example` como referencia

```bash
# Copiar archivo de ejemplo
cp src/main/resources/firebase/firebase-service-account.json.example \
   src/main/resources/firebase/firebase-service-account.json

# Editar con credenciales reales de Firebase
```

### Variables de Entorno Sensibles

Archivo: `src/main/resources/application.properties`

```properties
# Firebase
firebase.config.file=firebase/firebase-service-account.json

# Email Configuration (si usas SMTP)
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password

# SMS Configuration (Twilio)
twilio.account.sid=your-twilio-sid
twilio.auth.token=your-twilio-token
twilio.phone.number=your-twilio-phone

# Database (si no usas variables de entorno)
spring.datasource.username=your-db-user
spring.datasource.password=your-db-password
```

## Archivos Protegidos por .gitignore

### ✅ Archivos que NO se suben al repositorio:

- `src/main/resources/firebase/*.json`
- `src/main/resources/application.properties`
- `**/api-keys.json`
- `**/secrets.json`
- `**/credentials.json`

### ✅ Archivos de ejemplo que SÍ se suben:

- `firebase-service-account.json.example`
- `application.properties.example`

## Comandos de Verificación

```bash
# Verificar que archivos sensibles NO están en git
git status
git ls-files | grep -E "\.(json|properties)$"

# Ver qué archivos están siendo ignorados
git check-ignore src/main/resources/firebase/*.json
```

## 🚨 En caso de exposición accidental

Si accidentally commiteas credenciales:

1. **Cambiar credenciales inmediatamente**:
   - Firebase: Generar nueva clave de servicio
   - API Keys: Regenerar en consolas respectivas

2. **Limpiar historial de Git**:
   ```bash
   git filter-branch --force --index-filter \
   'git rm --cached --ignore-unmatch path/to/sensitive/file' \
   --prune-empty --tag-name-filter cat -- --all
   ```

3. **Force push** (solo si el repo es privado):
   ```bash
   git push origin --force --all
   ```

## Variables de Entorno en Producción

Para producción, usar variables de entorno en lugar de archivos:

```properties
# application-prod.properties
firebase.config.file=${FIREBASE_CONFIG_PATH}
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}
twilio.account.sid=${TWILIO_SID}
twilio.auth.token=${TWILIO_TOKEN}
``` 