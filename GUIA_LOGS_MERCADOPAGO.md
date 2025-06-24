# 📊 Guía de Logs de MercadoPago - RecitApp

## 📋 **Tabla de Contenido**
1. [Configuración de Logs](#configuración-de-logs)
2. [Tipos de Logs](#tipos-de-logs)
3. [Emojis y Etiquetas](#emojis-y-etiquetas)
4. [Ubicación de Archivos](#ubicación-de-archivos)
5. [Herramientas de Monitoreo](#herramientas-de-monitoreo)
6. [Casos de Prueba](#casos-de-prueba)
7. [Troubleshooting](#troubleshooting)

---

## 🔧 **Configuración de Logs**

### **Archivos de Configuración:**
- `application.properties`: Configuración básica de niveles de log
- `logback-spring.xml`: Configuración avanzada de Logback con archivos separados

### **Niveles de Log Configurados:**
```properties
# Específicos de MercadoPago
logging.level.com.recitapp.recitapp_api.modules.payment.service.impl.MercadoPagoServiceImpl=DEBUG
logging.level.com.recitapp.recitapp_api.modules.payment.service.impl.MercadoPagoRefundServiceImpl=DEBUG
logging.level.com.recitapp.recitapp_api.modules.payment.controller.PaymentController=DEBUG

# SDK de MercadoPago
logging.level.com.mercadopago=DEBUG
```

---

## 📁 **Tipos de Logs**

### **1. Logs de Creación de Preferencias**
**Etiqueta**: `[MERCADOPAGO]`  
**Propósito**: Monitorear la creación de preferencias de pago

**Ejemplo de Output:**
```log
🚀 [MERCADOPAGO] Creating payment preference - Event: 1, User: 123, Amount: $5000.00, WalletOnly: false
💰 [MERCADOPAGO] Paid ticket - Type: VIP, Price: $2500.00, Quantity: 2, Total: $5000.00
🆔 [MERCADOPAGO] Generated external reference: EVENTO_1_USER_123_abc123...
📦 [MERCADOPAGO] Created 1 preference items for processing
🔗 [MERCADOPAGO] Configured redirect URLs - Success: http://localhost:4200/payment/success...
```

### **2. Logs de Webhooks**
**Etiqueta**: `[MERCADOPAGO-WEBHOOK]`  
**Propósito**: Debugging de notificaciones de MercadoPago

**Ejemplo de Output:**
```log
🔔 [MERCADOPAGO-WEBHOOK] Processing webhook notification
📥 [MERCADOPAGO-WEBHOOK] Params received: {type=payment, data.id=12345}
💳 [MERCADOPAGO-WEBHOOK] Processing payment notification for ID: 12345
📊 [MERCADOPAGO-WEBHOOK] Payment details - Method: visa, Type: credit_card, Status: approved
✅ [MERCADOPAGO-WEBHOOK] Payment approved - Updating transaction with payment ID: 12345
🎉 [MERCADOPAGO-WEBHOOK] Transaction successfully updated with payment ID: 12345
```

### **3. Logs de Reembolsos**
**Etiqueta**: `[MERCADOPAGO-REFUND]`  
**Propósito**: Tracking de procesos de reembolso

**Ejemplo de Output:**
```log
💸 [MERCADOPAGO-REFUND] Starting refund process for payment ID: 12345, amount: $1000.00
✅ [MERCADOPAGO-REFUND] Payment found - Status: approved, Amount: $5000.00
🎉 [MERCADOPAGO-REFUND] Refund created successfully - ID: 67890, Status: approved, Amount: $1000.00
```

### **4. Logs de Controladores**
**Etiqueta**: `[PAYMENT-CONTROLLER]`, `[WEBHOOK-CONTROLLER]`, `[WALLET-CONTROLLER]`  
**Propósito**: Tracking de requests HTTP

**Ejemplo de Output:**
```log
🎫 [PAYMENT-CONTROLLER] Creating payment preference for event: 1 and user: 123
✅ [PAYMENT-CONTROLLER] Payment preference created successfully - Preference ID: 12345-abc
🎣 [WEBHOOK-CONTROLLER] Received MercadoPago webhook notification
✅ [WEBHOOK-CONTROLLER] Webhook processed successfully
```

---

## 🎭 **Emojis y Etiquetas**

### **Emojis por Tipo de Operación:**
- 🚀 **Inicio de proceso**
- ✅ **Operación exitosa**
- ❌ **Error/Fallo**
- ⚠️ **Advertencia**
- 💳 **Operaciones de pago**
- 🎣 **Webhooks**
- 💸 **Reembolsos**
- 🏦 **Billetera virtual**
- 📊 **Información detallada**
- 🔍 **Debugging**

### **Etiquetas por Módulo:**
- `[MERCADOPAGO]` - Servicio principal
- `[MERCADOPAGO-WEBHOOK]` - Procesamiento de webhooks
- `[MERCADOPAGO-REFUND]` - Servicio de reembolsos
- `[PAYMENT-CONTROLLER]` - Controlador de pagos
- `[WEBHOOK-CONTROLLER]` - Controlador de webhooks
- `[WALLET-CONTROLLER]` - Controlador de billetera

---

## 📂 **Ubicación de Archivos**

### **Archivos de Log Generados:**
```
logs/
├── recitapp.log                    # Logs generales de la aplicación
├── mercadopago.log                 # Logs específicos de MercadoPago
├── mercadopago-webhooks.log        # Logs únicamente de webhooks
└── archived/                       # Logs archivados por fecha
    ├── recitapp.2024-01-15.1.log
    └── mercadopago.2024-01-15.1.log
```

### **Configuración de Rotación:**
- **Tamaño máximo por archivo**: 10MB
- **Archivos históricos**: 30 días
- **Tamaño total máximo**: 1GB (general), 500MB (MercadoPago)

---

## 🛠 **Herramientas de Monitoreo**

### **1. Script de Monitoreo (Windows)**
**Archivo**: `test-mercadopago-logs.bat`

**Opciones disponibles:**
1. Ver logs de MercadoPago en tiempo real
2. Ver logs de Webhooks en tiempo real  
3. Ver logs generales en tiempo real
4. Limpiar logs anteriores
5. Mostrar últimas 50 líneas de MercadoPago
6. Buscar errores en logs

**Uso:**
```bash
# Ejecutar desde la carpeta del backend
./test-mercadopago-logs.bat
```

### **2. Comandos Útiles**

**PowerShell (Windows):**
```powershell
# Monitorear logs en tiempo real
Get-Content -Path "logs\mercadopago.log" -Wait -Tail 10

# Buscar errores
Select-String -Path "logs\mercadopago.log" -Pattern "❌|ERROR|Exception"

# Filtrar por tipo de operación
Select-String -Path "logs\mercadopago.log" -Pattern "\[MERCADOPAGO-WEBHOOK\]"
```

**Command Line (Windows):**
```cmd
# Ver últimas líneas
powershell "Get-Content -Path 'logs\mercadopago.log' -Tail 20"

# Buscar webhooks
findstr /i "WEBHOOK" "logs\mercadopago.log"

# Buscar errores
findstr /i "ERROR\|❌" "logs\mercadopago.log"
```

---

## 🧪 **Casos de Prueba**

### **1. Test de Creación de Preferencia**
**Objetivo**: Verificar que se crea correctamente una preferencia de pago

**Logs esperados:**
```log
🚀 [MERCADOPAGO] Creating payment preference - Event: X, User: Y, Amount: $Z
🔓 [MERCADOPAGO] STANDARD mode - All payment methods available
💰 [MERCADOPAGO] Paid ticket - Type: VIP, Price: $X, Quantity: 1, Total: $X
🆔 [MERCADOPAGO] Generated external reference: EVENTO_X_USER_Y_...
✅ [PAYMENT-CONTROLLER] Payment preference created successfully - Preference ID: XXX
```

### **2. Test de Webhook**
**Objetivo**: Verificar que los webhooks se procesan correctamente

**Logs esperados:**
```log
🎣 [WEBHOOK-CONTROLLER] Received MercadoPago webhook notification
🔔 [MERCADOPAGO-WEBHOOK] Processing webhook notification
💳 [MERCADOPAGO-WEBHOOK] Processing payment notification for ID: 12345
📊 [MERCADOPAGO-WEBHOOK] Payment details - Method: visa, Type: credit_card, Status: approved
✅ [MERCADOPAGO-WEBHOOK] Payment approved - Updating transaction with payment ID: 12345
🎉 [MERCADOPAGO-WEBHOOK] Transaction successfully updated with payment ID: 12345
```

### **3. Test de Billetera Virtual**
**Objetivo**: Verificar funcionamiento de pagos con billetera

**Logs esperados:**
```log
💳 [PAYMENT-CONTROLLER] Creating WALLET-ONLY preference for event: 1 and user: 123
💳 [MERCADOPAGO] WALLET-ONLY mode enabled - Only MercadoPago accounts will be accepted
🏦 [WALLET-CONTROLLER] Processing wallet purchase for Event: 1, User: 123
```

### **4. Test de Reembolso**
**Objetivo**: Verificar el proceso de reembolsos

**Logs esperados:**
```log
💸 [MERCADOPAGO-REFUND] Starting refund process for payment ID: 12345, amount: $500.00
✅ [MERCADOPAGO-REFUND] Payment found - Status: approved, Amount: $1000.00
🎉 [MERCADOPAGO-REFUND] Refund created successfully - ID: 67890, Status: approved, Amount: $500.00
```

---

## 🔧 **Troubleshooting**

### **Problemas Comunes y Sus Logs**

#### **1. Error en creación de preferencia**
**Síntomas**: La preferencia no se crea
**Logs a buscar**:
```log
❌ [MERCADOPAGO] Error creating preference: Invalid credentials
❌ [PAYMENT-CONTROLLER] Error creating payment preference
```

#### **2. Webhook no llega**
**Síntomas**: Los pagos no se confirman automáticamente
**Logs a buscar**:
```log
⚠️ [MERCADOPAGO-WEBHOOK] Could not find transaction with external reference: XXX to update
❌ [WEBHOOK-CONTROLLER] Error processing webhook
```

#### **3. Error en reembolso**
**Síntomas**: El reembolso no se procesa
**Logs a buscar**:
```log
🚫 [MERCADOPAGO-REFUND] Payment cannot be refunded - Status: cancelled
❌ [MERCADOPAGO-REFUND] Payment not found: 12345
```

#### **4. Problema con billetera virtual**
**Síntomas**: Los pagos con wallet fallan
**Logs a buscar**:
```log
❌ [WALLET-CONTROLLER] Error processing wallet purchase
⚠️ [MERCADOPAGO] No payer information provided or missing email
```

---

## 📈 **Monitoreo en Producción**

### **Métricas Importantes:**
1. **Tasa de éxito de preferencias**: `✅ [PAYMENT-CONTROLLER] Payment preference created successfully`
2. **Webhooks procesados**: `✅ [WEBHOOK-CONTROLLER] Webhook processed successfully`
3. **Errores de MercadoPago**: `❌ [MERCADOPAGO]`
4. **Reembolsos exitosos**: `🎉 [MERCADOPAGO-REFUND] Refund created successfully`

### **Alertas Recomendadas:**
- Error rate > 5% en creación de preferencias
- Webhooks fallidos > 10% en 1 hora  
- Reembolsos fallidos > 1% en 1 día
- Ausencia de logs por > 30 minutos (indica que la app está down)

---

## 🎯 **Comandos Rápidos para Testing**

```bash
# Limpiar logs antes de test
del logs\*.log

# Iniciar monitoreo de MercadoPago
.\test-mercadopago-logs.bat

# Buscar solo errores en tiempo real
powershell "Get-Content -Path 'logs\mercadopago.log' -Wait | Select-String '❌|ERROR'"

# Verificar webhooks de las últimas 2 horas
powershell "Get-Content -Path 'logs\mercadopago-webhooks.log' | Where-Object {$_ -match (Get-Date).AddHours(-2).ToString('yyyy-MM-dd HH')}"
```

---

## 📝 **Notas para Desarrollo**

### **Para agregar nuevos logs:**
1. Usar el patrón de emojis + etiqueta
2. Incluir información relevante para debugging
3. Mantener consistencia en el formato
4. Actualizar esta documentación

### **Niveles de log recomendados:**
- `DEBUG`: Información detallada para debugging
- `INFO`: Operaciones importantes y exitosas
- `WARN`: Situaciones que requieren atención pero no fallan
- `ERROR`: Errores que impiden la operación

---

*Última actualización: 2024-01-15*
*Versión: 1.0* 