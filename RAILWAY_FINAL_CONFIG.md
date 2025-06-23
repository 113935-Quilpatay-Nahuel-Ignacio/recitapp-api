# 🚀 CONFIGURACIÓN FINAL RAILWAY - RECITAPP

## 🌐 URLs Configuradas

**Backend Railway:** https://recitapp-api-production.up.railway.app
**Frontend:** (por configurar - Vercel/Netlify)

## 📋 Variables de Entorno OBLIGATORIAS para Railway

Copia y pega estas variables en tu Railway Dashboard → Variables:

```bash
# BACKEND URL (configurado automáticamente por Railway)
BASE_URL=https://recitapp-api-production.up.railway.app/api

# FRONTEND URL - ACTUALIZAR con tu dominio de frontend
FRONTEND_URL=https://tu-frontend-domain.vercel.app

# MERCADOPAGO - USAR CREDENCIALES REALES DE PRODUCCIÓN
MERCADOPAGO_ACCESS_TOKEN=tu-access-token-real-de-produccion
MERCADOPAGO_PUBLIC_KEY=tu-public-key-real-de-produccion
MERCADOPAGO_WEBHOOK_URL=https://recitapp-api-production.up.railway.app/api/payments/webhook
MERCADOPAGO_SUCCESS_URL=https://tu-frontend-domain.vercel.app/payment/success
MERCADOPAGO_FAILURE_URL=https://tu-frontend-domain.vercel.app/payment/failure
MERCADOPAGO_PENDING_URL=https://tu-frontend-domain.vercel.app/payment/pending

# JWT SECURITY - GENERAR UNO NUEVO Y SEGURO
JWT_SECRET=tu-jwt-secret-super-seguro-de-64-caracteres-minimo
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# EMAIL CONFIGURATION
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password-de-gmail

# FILE STORAGE (Railway temporary storage)
FILE_UPLOAD_DIR=/tmp/uploads
TICKET_PDF_STORAGE_PATH=/tmp/tickets
```

## ✅ Variables Ya Configuradas Automáticamente por Railway

Estas las maneja Railway automáticamente cuando agregas MySQL:

```bash
MYSQL_DATABASE=railway
MYSQL_PUBLIC_URL=mysql://root:PYaukFCrWvGehErXoDKAeGVPvjYGAqiK@metro.proxy.rlwy.net:37389/railway
MYSQL_ROOT_PASSWORD=PYaukFCrWvGehErXoDKAeGVPvjYGAqiK
MYSQL_URL=mysql://root:PYaukFCrWvGehErXoDKAeGVPvjYGAqiK@railway:3306/railway
MYSQLDATABASE=railway
MYSQLHOST=railway
MYSQLPASSWORD=PYaukFCrWvGehErXoDKAeGVPvjYGAqiK
MYSQLPORT=3306
MYSQLUSER=root
PORT=8080
```

## 🔧 Comandos de Testing

### Test de conexión al backend
```bash
curl https://recitapp-api-production.up.railway.app/actuator/health
```

### Test de API
```bash
curl https://recitapp-api-production.up.railway.app/api/artists
```

### Test de CORS
```bash
curl -H "Origin: https://tu-frontend-domain.vercel.app" \
     -H "Access-Control-Request-Method: GET" \
     -H "Access-Control-Request-Headers: X-Requested-With" \
     -X OPTIONS \
     https://recitapp-api-production.up.railway.app/api/artists
```

## 🚨 PASOS IMPORTANTES ANTES DE USAR EN PRODUCCIÓN

1. **🔐 Cambiar JWT_SECRET:**
   ```bash
   # Generar uno nuevo:
   openssl rand -base64 64
   ```

2. **💳 Configurar MercadoPago con credenciales REALES**
   - Ir a MercadoPago Developer Dashboard
   - Obtener credenciales de PRODUCCIÓN (no testing)
   - Actualizar MERCADOPAGO_ACCESS_TOKEN y MERCADOPAGO_PUBLIC_KEY

3. **📧 Configurar Email con credenciales reales**
   - Usar Gmail App Password (no la contraseña normal)
   - Activar autenticación de 2 factores en Gmail
   - Generar App Password específica

4. **🌐 Actualizar URLs del frontend**
   - Reemplazar todas las URLs con tu dominio real de frontend
   - Configurar el frontend en Vercel/Netlify
   - Actualizar FRONTEND_URL en Railway

## 📝 Configuración del Frontend

El archivo `environment.prod.ts` ya está configurado con:
```typescript
apiUrl: 'https://recitapp-api-production.up.railway.app/api'
```

### Build para producción:
```bash
cd recitapp-front
ng build --configuration production
```

### Deploy en Vercel:
```bash
npx vercel --prod
```

## ✅ Checklist Final

- [ ] Variables de entorno configuradas en Railway
- [ ] JWT_SECRET cambiado por uno seguro
- [ ] Credenciales reales de MercadoPago
- [ ] Credenciales reales de Gmail
- [ ] Frontend deployado
- [ ] URLs del frontend actualizadas en Railway
- [ ] Testing de API funcionando
- [ ] CORS funcionando entre frontend y backend 