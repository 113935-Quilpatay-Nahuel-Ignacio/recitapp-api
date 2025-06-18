# Sistema de Billetera Virtual - RecitApp

## 📋 **Funcionalidad Implementada**

### ✅ **Descuento Automático en Compras**
El sistema ahora descuenta automáticamente el saldo disponible en la billetera virtual del usuario durante las compras de entradas, con las siguientes características:

- **Descuento completo**: Si el saldo cubre toda la compra, se paga completamente con billetera virtual
- **Descuento parcial**: Si el saldo cubre parte de la compra, se descuenta lo disponible y el resto se paga con MercadoPago
- **Sin saldo**: Si no hay saldo, funciona como antes con MercadoPago

---

## 🛠 **Componentes Backend Desarrollados**

### 1. **TicketServiceImpl** (Actualizado)
**Archivo**: `modules/ticket/service/impl/TicketServiceImpl.java`

**Nuevas funcionalidades**:
- Validación automática de saldo de billetera virtual
- Descuento automático antes de procesar el pago
- Actualización del saldo del usuario
- Generación de descripciones descriptivas de la transacción

```java
// Lógica principal implementada:
if (user.getWalletBalance() != null && user.getWalletBalance() > 0) {
    BigDecimal availableWalletBalance = BigDecimal.valueOf(user.getWalletBalance());
    
    if (availableWalletBalance.compareTo(totalAmount) >= 0) {
        // Billetera cubre toda la compra
        walletDiscount = totalAmount;
        amountAfterWallet = BigDecimal.ZERO;
    } else {
        // Billetera cubre parte de la compra
        walletDiscount = availableWalletBalance;
        amountAfterWallet = totalAmount.subtract(availableWalletBalance);
    }
    
    // Actualizar saldo del usuario
    user.setWalletBalance(user.getWalletBalance() - walletDiscount.doubleValue());
    userRepository.save(user);
}
```

### 2. **PaymentController** (Nuevo Endpoint)
**Archivo**: `modules/payment/controller/PaymentController.java`

**Endpoint agregado**:
```http
POST /api/payments/wallet-purchase
```

**Funcionalidad**:
- Procesa compras usando exclusivamente billetera virtual
- Genera PDFs y envía emails de confirmación
- Retorna información detallada del descuento aplicado

### 3. **DTOs Actualizados**

#### **TicketPurchaseResponseDTO**
```java
// Nuevos campos agregados:
private BigDecimal walletDiscountApplied;
private BigDecimal amountAfterWallet;
private String walletMessage;
```

#### **PaymentResponseDTO**
```java
// Nuevos campos agregados:
private BigDecimal walletDiscountApplied;
private BigDecimal amountAfterWallet;
private String walletMessage;
```

---

## 🎨 **Componentes Frontend Desarrollados**

### 1. **TicketPurchaseComponent** (Actualizado)
**Archivo**: `modules/ticket/pages/ticket-purchase/ticket-purchase.component.ts`

**Nuevas propiedades**:
```typescript
// Wallet properties
userWalletBalance = 0;
walletDiscountApplied = 0;
amountAfterWallet = 0;
useWalletPayment = false;
isLoadingWallet = false;
showWalletOption = false;
```

**Nuevos métodos**:
- `loadUserWalletBalance()`: Carga saldo de billetera al inicializar
- `updateWalletCalculations()`: Calcula descuentos automáticamente
- `processWalletPayment()`: Procesa pagos completamente con billetera

### 2. **PaymentService** (Actualizado)
**Archivo**: `modules/payment/services/payment.service.ts`

**Nuevo método**:
```typescript
processWalletPayment(paymentRequest: PaymentRequest): Observable<PaymentResponse> {
  return this.http.post<PaymentResponse>(`${this.apiUrl}/wallet-purchase`, paymentRequest);
}
```

**Interface actualizada**:
```typescript
export interface PaymentResponse {
  // ... campos existentes ...
  
  // Wallet information
  walletDiscountApplied?: number;
  amountAfterWallet?: number;
  walletMessage?: string;
}
```

### 3. **Template HTML** (Actualizado)
**Archivo**: `modules/ticket/pages/ticket-purchase/ticket-purchase.component.html`

**Nuevas secciones**:
- **Información de billetera virtual**: Muestra saldo disponible y descuentos
- **Opciones de pago**: Botones dinámicos según el saldo disponible
- **Mensajes informativos**: Feedback claro sobre el uso de billetera

---

## 📊 **Flujos de Compra Implementados**

### **Flujo 1: Compra Completamente Cubierta por Billetera**
```
1. Usuario tiene saldo >= precio total
2. Sistema muestra: "Compra completamente cubierta por billetera virtual"
3. Usuario hace clic en "Pagar con Billetera Virtual"
4. Se procesa inmediatamente sin MercadoPago
5. Se actualiza saldo del usuario
6. Se generan tickets y se envían por email
```

### **Flujo 2: Compra Parcialmente Cubierta por Billetera**
```
1. Usuario tiene saldo < precio total
2. Sistema muestra descuento aplicado y monto restante
3. Usuario puede elegir:
   - "Completar pago con MercadoPago": Procesa resto con MP
   - Agregar saldo a billetera primero
4. Se descuenta automáticamente lo disponible en billetera
5. Se procesa el resto con MercadoPago
```

### **Flujo 3: Sin Saldo en Billetera**
```
1. Usuario no tiene saldo o saldo = 0
2. Sistema funciona como antes
3. Pago completamente con MercadoPago
4. No se muestra información de billetera
```

---

## 🔧 **Configuración Requerida**

### **Base de Datos**
El método de pago `BILLETERA_VIRTUAL` ya existe en `data.sql`:
```sql
INSERT IGNORE INTO payment_methods (name, active, description)
VALUES ('BILLETERA_VIRTUAL', TRUE, 'Pago con saldo de billetera virtual de Recitapp');
```

### **Usuario Entity**
Campo `walletBalance` ya implementado:
```java
@Column(name = "wallet_balance")
private Double walletBalance;
```

---

## 🧪 **Casos de Prueba**

### **Escenario 1: Saldo Suficiente**
```
- Usuario saldo: $50,000
- Precio entrada: $30,000
- Resultado: Pago completo con billetera, saldo restante: $20,000
```

### **Escenario 2: Saldo Insuficiente**
```
- Usuario saldo: $20,000
- Precio entrada: $30,000
- Resultado: $20,000 descontados, $10,000 a pagar con MercadoPago
```

### **Escenario 3: Sin Saldo**
```
- Usuario saldo: $0
- Precio entrada: $30,000
- Resultado: Pago completo con MercadoPago (flujo normal)
```

---

## 📈 **Beneficios Implementados**

1. **Experiencia de Usuario Mejorada**:
   - Descuentos automáticos transparentes
   - Opciones de pago claras y visibles
   - Feedback inmediato sobre saldo disponible

2. **Funcionalidad Robusta**:
   - Validación de saldo en tiempo real
   - Manejo de errores y casos edge
   - Consistencia entre frontend y backend

3. **Integración Perfecta**:
   - No interfiere con flujos existentes
   - Compatible con sistema de reembolsos
   - Reutiliza infraestructura existente

---

## 🚀 **Estado del Desarrollo**

### ✅ **Completado**:
- [x] Descuento automático en compras
- [x] Interfaz de usuario actualizada
- [x] Endpoint de pago con billetera
- [x] Validaciones y casos edge
- [x] Integración con sistema existente

### 📋 **Funcionalidades Base Disponibles**:
- [x] Gestión de billetera virtual (`/transactions/virtual-wallet`)
- [x] Consulta de saldo (`GET /wallet/balance/{userId}`)
- [x] Actualización de saldo (`POST /wallet/transaction`)
- [x] Sistema de reembolsos a billetera

---

## 🔮 **Próximas Mejoras Sugeridas**

1. **Notificaciones**: Alertas cuando se aplican descuentos de billetera
2. **Historial**: Registro detallado de transacciones de billetera
3. **Promociones**: Bonificaciones por uso de billetera virtual
4. **Límites**: Configuración de límites máximos de saldo
5. **Recarga**: Integración directa para recargar billetera desde MercadoPago

---

**Fecha de implementación**: 2025-06-18  
**Desarrollado por**: RecitApp Development Team  
**Estado**: ✅ Producción Ready 