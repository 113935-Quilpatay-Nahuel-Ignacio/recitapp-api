# 🎯 Solución: Saldo de MercadoPago no aparece en Tests

## 📋 Problema Identificado

El checkout de MercadoPago solo muestra **tarjetas de débito/crédito** pero no aparece la opción de **"saldo de cuenta MercadoPago"** durante las pruebas.

## 🔍 Causa Raíz

**Los usuarios de prueba de MercadoPago NO tienen saldo automáticamente**. El checkout solo muestra las opciones de pago disponibles para el usuario, por lo que si no hay saldo en la cuenta, la opción no aparece.

## ✅ Solución Paso a Paso

### 1. Verificar Usuario de Prueba

1. **Accede a tu Dashboard de MercadoPago**: https://www.mercadopago.com.ar/developers/panel/app
2. Ve a **"Test accounts"** (Cuentas de prueba)
3. Verifica que tengas usuarios de prueba creados para Argentina

### 2. Agregar Saldo al Usuario de Prueba

#### Opción A: Desde el Dashboard de Desarrollador
1. En **"Test accounts"**, encuentra tu usuario de prueba
2. Haz clic en los **tres puntos** (⋮) al final de la fila
3. Selecciona **"Edit data"** (Editar datos)
4. **Agrega dinero ficticio** en el campo correspondiente (ej: $10000)
5. Guarda los cambios

#### Opción B: Desde la Cuenta del Usuario de Prueba
1. **Inicia sesión** en MercadoPago con las credenciales del usuario de prueba
2. Ve a la sección **"Mi dinero"** o **"Cargar dinero"**
3. Simula una **transferencia bancaria** o **depósito**
4. Agrega el monto que necesites para las pruebas

### 3. Verificar Configuración del Backend

Tu código ya está **perfectamente configurado**. Verifica que estés usando:

```java
// ✅ CORRECTO: Modo unificado (sin purpose)
PreferenceRequest preferenceRequest = PreferenceRequest.builder()
    .items(items)
    .backUrls(backUrls)
    .payer(payer)
    .externalReference(externalReference)
    .notificationUrl(webhookUrl)
    .expires(false)
    // SIN purpose = permite tarjetas + saldo MP + otros métodos automáticamente
    .build();
```

### 4. Probar el Flujo Completo

1. **Crea una preferencia** con tu backend
2. **Inicia sesión** en el checkout con el usuario de prueba que tiene saldo
3. **Verifica** que aparezcan las opciones:
   - ✅ Tarjetas de crédito/débito
   - ✅ **Dinero en cuenta de MercadoPago**
   - ✅ Otras opciones disponibles

## 🎯 Opciones de Pago Esperadas

Con un usuario que tiene saldo, deberías ver:

```
┌─────────────────────────────────────┐
│  💳 Tarjeta de crédito o débito     │
├─────────────────────────────────────┤
│  💰 Dinero en MercadoPago          │
│      Saldo disponible: $10,000     │
├─────────────────────────────────────┤
│  🏦 Transferencia bancaria          │
├─────────────────────────────────────┤
│  📱 Cuotas sin tarjeta             │
└─────────────────────────────────────┘
```

## 🔧 Comandos de Verificación

### Verificar Logs del Backend
```bash
# Verificar que se esté creando en modo unificado
grep "UNIFIED mode" logs/recitapp.log

# Verificar la preferencia creada
grep "Payment preference created successfully" logs/recitapp.log
```

### Verificar Preferencia en MercadoPago
```bash
# Consultar preferencia creada
curl -X GET \
  "https://api.mercadopago.com/checkout/preferences/{PREFERENCE_ID}" \
  -H "Authorization: Bearer YOUR_ACCESS_TOKEN"
```

## 📝 Checklist de Verificación

- [ ] Usuario de prueba creado para Argentina
- [ ] Usuario de prueba tiene saldo agregado ($1000+ recomendado)
- [ ] Backend configurado sin `purpose: "wallet_purchase"`
- [ ] Logs muestran "UNIFIED mode"
- [ ] Preferencia se crea exitosamente
- [ ] Usuario de prueba puede iniciar sesión en checkout

## 🚨 Problemas Comunes

### ❌ Solo aparecen tarjetas
**Causa**: Usuario sin saldo  
**Solución**: Agregar dinero ficticio al usuario de prueba

### ❌ Error al crear preferencia
**Causa**: Credenciales incorrectas  
**Solución**: Verificar ACCESS_TOKEN y PUBLIC_KEY de test

### ❌ Usuario no puede iniciar sesión
**Causa**: Usuario de prueba de país diferente  
**Solución**: Crear usuario de prueba para Argentina

## 📚 Referencias Útiles

- [Documentación de Usuarios de Prueba](https://www.mercadopago.com.ar/developers/es/docs/your-integrations/test/accounts)
- [Checkout Pro - Configuración](https://www.mercadopago.com.ar/developers/es/docs/checkout-pro/landing)
- [Métodos de Pago Disponibles](https://www.mercadopago.com.ar/developers/es/docs/checkout-pro/payment-methods)

## 🎉 Resultado Esperado

Después de seguir estos pasos, el checkout debería mostrar **todas las opciones de pago** incluyendo el saldo de MercadoPago, proporcionando una experiencia de pago completa y unificada para tus usuarios.

---

**💡 Tip**: Mantén varios usuarios de prueba con diferentes configuraciones (con saldo, sin saldo, con tarjetas guardadas) para probar todos los escenarios posibles. 