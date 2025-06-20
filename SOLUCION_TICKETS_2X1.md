# Solución: Detección y Badges de Entradas 2x1

## Problema Identificado
Los tickets comprados con tipo PROMOTIONAL_2X1 aparecían en la base de datos como GENERAL, impidiendo que se mostraran las badges "2x1" en el frontend.

## Causa Raíz
El problema estaba en la capa de Payment Services, donde los métodos `buildTicketPurchaseRequest` y `buildWalletTicketPurchaseRequest` NO estaban pasando los campos `ticketPriceId` y `ticketType` del frontend al backend.

### Flujo de Datos Problemático:
1. **Frontend**: Enviaba correctamente `ticketPriceId` y `ticketType` en `PaymentRequestDTO.TicketItemDTO`
2. **Payment Controllers**: Recibían los datos correctamente
3. **🚨 PROBLEMA**: Los métodos de construcción de request omitían estos campos
4. **Backend**: Usaba logic de fallback que resultaba en `ticketType = 'GENERAL'`

## Archivos Corregidos

### 1. PaymentController.java
**Método**: `buildWalletTicketPurchaseRequest()`
**Líneas**: 218-220
```java
// AGREGADO: Pasar información del tipo de ticket
ticketRequest.setTicketPriceId(ticketItem.getTicketPriceId());
ticketRequest.setTicketType(ticketItem.getTicketType());
```

### 2. MercadoPagoServiceImpl.java
**Método**: `buildTicketPurchaseRequest()`
**Líneas**: 537-539
```java
// AGREGADO: Pasar información del tipo de ticket
ticketRequest.setTicketPriceId(ticketItem.getTicketPriceId());
ticketRequest.setTicketType(ticketItem.getTicketType());
```

**Método**: `buildTicketPurchaseRequestForGifts()`
**Líneas**: 682-684
```java
// AGREGADO: Pasar información del tipo de ticket
ticketRequest.setTicketPriceId(ticketItem.getTicketPriceId());
ticketRequest.setTicketType(ticketItem.getTicketType());
```

## Funcionalidad Existente del Frontend
✅ **YA IMPLEMENTADO** - No requiere cambios adicionales:

### Componentes con Badges:
- **ticket-list.component**: Muestra badges en listado de tickets
- **ticket-detail.component**: Muestra badges en detalles de ticket

### Lógica de Badges:
```typescript
isPromotional2x1(ticket: Ticket): boolean {
  return ticket.ticketType === 'PROMOTIONAL_2X1';
}
```

### Tipos de Badges:
- **PROMOTIONAL_2X1**: Badge amarillo "Promocional 2x1"
- **GIFT**: Badge azul "Entrada de Regalo"  
- **Otras promociones**: Badge verde genérico

## Archivos de Prueba
- `test_2x1_sample_data.sql`: Datos de prueba con entradas PROMOTIONAL_2X1
- `test_2x1_tickets.sql`: Script original con tickets de ejemplo

## Verificación
1. Aplicar migración V1004 (ya aplicada)
2. Ejecutar `test_2x1_sample_data.sql` para crear ticket_prices de prueba
3. Comprar entrada PROMOTIONAL_2X1 desde frontend
4. Verificar que el ticket se crea con `ticket_type = 'PROMOTIONAL_2X1'`
5. Confirmar que aparece badge "Promocional 2x1" (amarillo) en frontend

## Status
✅ **SOLUCIONADO**: La información de `ticketType` ahora se propaga correctamente desde el frontend hasta la base de datos, permitiendo que las badges 2x1 funcionen como esperado. 