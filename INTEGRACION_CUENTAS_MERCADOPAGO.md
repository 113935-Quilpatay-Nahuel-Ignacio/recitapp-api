# Integración de Cuentas Reales de MercadoPago

## 📋 Resumen de la Implementación

Esta implementación permite a los usuarios pagar utilizando sus cuentas reales de MercadoPago, incluyendo:
- **Saldo de cuenta MercadoPago**
- **Tarjetas guardadas en MercadoPago**
- **Cuotas sin Tarjeta de MercadoPago**

## 🔧 Cambios Implementados

### Backend (Java)

#### 1. MercadoPagoServiceImpl.java
- ✅ Agregado soporte para `purpose: "wallet_purchase"`
- ✅ Nuevo método `createPaymentPreferenceWalletOnly()`
- ✅ Método privado compartido con parámetro `walletOnly`

#### 2. MercadoPagoService.java (Interface)
- ✅ Agregado método `createPaymentPreferenceWalletOnly()`

#### 3. PaymentController.java
- ✅ Nuevo endpoint `/create-preference-wallet-only`
- ✅ Validaciones de seguridad incluidas

### Frontend (Angular)

#### 1. PaymentService
- ✅ Nuevo método `createPaymentPreferenceWalletOnly()`
- ✅ Interfaces actualizadas para compatibilidad

#### 2. TicketPurchaseComponent
- ✅ Nuevo método `processMercadoPagoWalletOnlyPayment()`
- ✅ Botón adicional en UI para "Pagar con Cuenta MercadoPago"

## 🚀 Cómo Usar

### Para el Usuario Final

1. **Proceso Normal**: Sigue funcionando como antes (todas las opciones de pago)
2. **Solo Cuenta MercadoPago**: Click en "Pagar con Cuenta MercadoPago" (botón amarillo)

### Para Desarrolladores

```typescript
// Crear preferencia normal (todas las opciones)
this.paymentService.createPaymentPreference(paymentRequest)

// Crear preferencia solo para cuentas de MercadoPago
this.paymentService.createPaymentPreferenceWalletOnly(paymentRequest)
```

## ⚠️ Consideraciones Importantes

### 1. Limitaciones del Modo Wallet-Only
- ❌ **NO** acepta pagos de usuarios sin cuenta MercadoPago
- ❌ **NO** acepta pagos en efectivo (Rapipago, Pago Fácil)
- ❌ **NO** acepta transferencias bancarias
- ✅ **SÍ** acepta tarjetas, saldo de cuenta y Cuotas sin Tarjeta

### 2. Credenciales de Test vs Producción

#### Test (Actual)
```properties
mercadopago.access.token=TEST-4403492759962042-060223-ccef50ad15229a32784b2504ce3d4f8c-1020599231
mercadopago.public.key=TEST-31b86a3a-dcb7-43c6-bfcd-0426844ad5a8
```

#### Para Producción (Cuando sea necesario)
```properties
mercadopago.access.token=APP_USR-[tu_production_access_token]
mercadopago.public.key=APP_USR-[tu_production_public_key]
```

## 🧪 Testing

### Usuarios de Test Disponibles

```
Comprador: TESTUSER1824080318 (B214FFE3#1807#4f74#)
Vendedor: TESTUSER1266730747 (WVmU4u0ui0)
```

### Cómo Probar

1. **Crear usuario de test en MercadoPago**:
   ```bash
   curl -X POST \
   https://api.mercadopago.com/users/test_user \
   -H "Authorization: Bearer TEST-4403492759962042-060223-ccef50ad15229a32784b2504ce3d4f8c-1020599231"
   ```

2. **Agregar saldo a cuenta de test**:
   - Ingresar a la cuenta de MercadoPago del usuario de test
   - Usar el simulador de saldo en el dashboard de desarrolladores

3. **Probar el flujo completo**:
   - Seleccionar entradas
   - Completar datos del pagador
   - Hacer click en "Pagar con Cuenta MercadoPago"
   - Loguearse con usuario de test
   - Pagar con saldo disponible

## 🔄 Flujo de Pago

### Modo Normal
```
Usuario → Datos → Opciones de Pago → MercadoPago (todas las opciones)
```

### Modo Wallet-Only
```
Usuario → Datos → "Pagar con Cuenta MercadoPago" → Login MercadoPago → Saldo/Tarjetas guardadas
```

## 📊 Diferencias en las Preferencias

### Preferencia Normal
```json
{
  "items": [...],
  "payer": {...},
  "back_urls": {...}
  // purpose no definido = todas las opciones
}
```

### Preferencia Wallet-Only
```json
{
  "items": [...],
  "payer": {...},
  "back_urls": {...},
  "purpose": "wallet_purchase"  // ← Clave para solo cuentas
}
```

## 🔐 Configuración de Producción

### Pasos para Pasar a Producción

1. **Obtener credenciales reales** en [MercadoPago Developers](https://www.mercadopago.com.ar/developers)

2. **Actualizar application.properties**:
   ```properties
   mercadopago.access.token=${MERCADOPAGO_ACCESS_TOKEN}
   mercadopago.public.key=${MERCADOPAGO_PUBLIC_KEY}
   ```

3. **Configurar variables de entorno**:
   ```bash
   export MERCADOPAGO_ACCESS_TOKEN="APP_USR-xxx"
   export MERCADOPAGO_PUBLIC_KEY="APP_USR-xxx"
   ```

4. **Activar webhooks en producción**:
   ```properties
   mercadopago.webhook.url=https://tu-dominio.com/api/payments/webhook
   ```

## 📈 Beneficios

### Para los Usuarios
- ✅ Pago con saldo de MercadoPago (dinero disponible)
- ✅ Uso de tarjetas guardadas
- ✅ Cuotas sin Tarjeta
- ✅ Experiencia más rápida (login único)

### Para el Negocio
- ✅ Mayor conversión de pagos
- ✅ Usuarios registrados en MercadoPago (mayor confianza)
- ✅ Menor abandono de carrito
- ✅ Pagos más seguros

## 🛠️ Mantenimiento

### Logs a Monitorear
```java
log.info("Preference configured for wallet-only payments");
log.info("Preference configured for all payment methods");
```

### Métricas Importantes
- Ratio de éxito wallet-only vs normal
- Tiempo de conversión
- Abandono de carrito por modo

## 🔗 Links Útiles

- [MercadoPago Developers](https://www.mercadopago.com.ar/developers)
- [Documentación Wallet Purchase](https://www.mercadopago.com.ar/developers/es/docs/checkout-api/integration-configuration/integrate-mp-wallet)
- [Preferencias API](https://www.mercadopago.com.ar/developers/es/docs/checkout-pro/additional-settings/preferences) 