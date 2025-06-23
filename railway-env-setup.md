# 🚀 Railway Environment Variables Setup

## Variables de entorno necesarias para Railway

### 📊 Database (Ya configuradas automáticamente por Railway MySQL)
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
```

### 🌐 Application Settings (Configurar manualmente)
```bash
# Railway will set this automatically
PORT=8080

# Base URL for your Railway app
BASE_URL=https://your-app-name.up.railway.app/api

# Frontend URL for CORS
FRONTEND_URL=https://your-frontend-domain.com

# File upload directory (Railway temporary storage)
FILE_UPLOAD_DIR=/tmp/uploads
TICKET_PDF_STORAGE_PATH=/tmp/tickets
```

### 🔐 Security Configuration
```bash
# JWT Configuration (IMPORTANT: Change these for production)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000
```

### 💳 MercadoPago Configuration
```bash
# MercadoPago credentials (Replace with your production keys)
MERCADOPAGO_ACCESS_TOKEN=your-production-access-token
MERCADOPAGO_PUBLIC_KEY=your-production-public-key

# MercadoPago URLs (Update with your Railway app URL)
MERCADOPAGO_WEBHOOK_URL=https://your-app-name.up.railway.app/api/payments/webhook
MERCADOPAGO_SUCCESS_URL=https://your-frontend-domain.com/payment/success
MERCADOPAGO_FAILURE_URL=https://your-frontend-domain.com/payment/failure
MERCADOPAGO_PENDING_URL=https://your-frontend-domain.com/payment/pending
```

### 📧 Email Configuration
```bash
# Gmail SMTP Configuration
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
```

### 📊 Redis Configuration (Optional)
```bash
# If you add Redis service to Railway
REDIS_HOST=redis-service-name
REDIS_PORT=6379
REDIS_PASSWORD=your-redis-password
```

## 📝 Cómo configurar en Railway

### Paso 1: Variables de entorno automáticas
Las variables de MySQL se configuran automáticamente cuando agregas el servicio MySQL.

### Paso 2: Variables de entorno manuales
En tu proyecto de Railway:

1. Ve a tu proyecto → **Variables**
2. Agrega cada variable manualmente:

```bash
BASE_URL=https://recitapp-backend.up.railway.app/api
FRONTEND_URL=https://recitapp-frontend.vercel.app
JWT_SECRET=tu-jwt-secret-super-seguro-para-produccion
MERCADOPAGO_ACCESS_TOKEN=tu-token-de-produccion
MERCADOPAGO_PUBLIC_KEY=tu-clave-publica-de-produccion
MERCADOPAGO_WEBHOOK_URL=https://recitapp-backend.up.railway.app/api/payments/webhook
MERCADOPAGO_SUCCESS_URL=https://recitapp-frontend.vercel.app/payment/success
MERCADOPAGO_FAILURE_URL=https://recitapp-frontend.vercel.app/payment/failure
MERCADOPAGO_PENDING_URL=https://recitapp-frontend.vercel.app/payment/pending
MAIL_USERNAME=tu-email@gmail.com
MAIL_PASSWORD=tu-app-password-de-gmail
```

### Paso 3: Deploy
```bash
# Railway detectará automáticamente el Dockerfile y construirá la aplicación
git push origin main
```

## ⚡ Comandos útiles

### Verificar variables de entorno
```bash
railway variables
```

### Ver logs en tiempo real
```bash
railway logs
```

### Conectar a la base de datos
```bash
railway connect mysql
```

## 🔍 Testing

### Health check
```bash
curl https://your-app-name.up.railway.app/actuator/health
```

### API Test
```bash
curl https://your-app-name.up.railway.app/api/artists
```

## 🚨 Importante
- ✅ Cambia `JWT_SECRET` por uno seguro para producción
- ✅ Configura las credenciales reales de MercadoPago
- ✅ Actualiza todas las URLs con tu dominio real
- ✅ Configura las credenciales de email reales 