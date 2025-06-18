-- Script para probar la corrección de descripciones en reembolsos fallback
-- Fecha: 2025-06-18

-- 1. Ver las transacciones reembolsadas actuales con descripciones problemáticas
SELECT 
    t.id,
    t.description,
    t.total_amount,
    t.transaction_date,
    t.is_refund,
    pm.name as payment_method,
    ts.status_name,
    CASE 
        WHEN t.description LIKE '%Procesado con sistema mejorado MercadoPago%' THEN 'DESCRIPCION_INCORRECTA'
        WHEN t.description LIKE '%billetera virtual%' THEN 'DESCRIPCION_CORRECTA'
        ELSE 'OTRA'
    END as tipo_descripcion
FROM transactions t
LEFT JOIN payment_methods pm ON t.payment_method_id = pm.id
LEFT JOIN transaction_status ts ON t.status_id = ts.id
WHERE t.is_refund = true
ORDER BY t.transaction_date DESC;

-- 2. Contar transacciones reembolsadas por tipo de descripción
SELECT 
    CASE 
        WHEN t.description LIKE '%Procesado con sistema mejorado MercadoPago%' THEN 'DESCRIPCION_INCORRECTA'
        WHEN t.description LIKE '%billetera virtual%' THEN 'DESCRIPCION_CORRECTA'
        ELSE 'OTRA'
    END as tipo_descripcion,
    COUNT(*) as cantidad
FROM transactions t
WHERE t.is_refund = true
GROUP BY 
    CASE 
        WHEN t.description LIKE '%Procesado con sistema mejorado MercadoPago%' THEN 'DESCRIPCION_INCORRECTA'
        WHEN t.description LIKE '%billetera virtual%' THEN 'DESCRIPCION_CORRECTA'
        ELSE 'OTRA'
    END;

-- 3. Ver detalles de transacciones con descripciones incorrectas
SELECT 
    t.id,
    t.description,
    t.total_amount,
    t.transaction_date,
    t.original_transaction_id,
    original_t.description as original_description
FROM transactions t
LEFT JOIN transactions original_t ON t.original_transaction_id = original_t.id
WHERE t.is_refund = true 
  AND t.description LIKE '%Procesado con sistema mejorado MercadoPago%'
ORDER BY t.transaction_date DESC;

-- NOTAS SOBRE LA CORRECCIÓN:
--
-- ✅ PROBLEMA SOLUCIONADO:
-- Ahora cuando un reembolso de MercadoPago falla y se usa wallet fallback,
-- la descripción será corregida automáticamente:
--
-- ANTES: "RAZON_USUARIO - Procesado con sistema mejorado MercadoPago"
-- DESPUÉS: "RAZON_USUARIO - Procesado como crédito en billetera virtual"
--
-- ✅ FUNCIONALIDAD IMPLEMENTADA:
-- 1. Limpia automáticamente referencias a "MercadoPago" de la descripción
-- 2. Agrega texto apropiado para wallet fallback
-- 3. Mantiene la razón original del usuario
-- 4. Guarda la transacción con la descripción corregida
--
-- ✅ MÉTODO AGREGADO:
-- updateTransactionDescriptionForWalletFallback()
-- - Limpia referencias a MercadoPago con regex
-- - Establece descripción apropiada para wallet fallback
-- - Actualiza y guarda la transacción en base de datos
--
-- ✅ PRÓXIMOS REEMBOLSOS:
-- Todos los nuevos reembolsos que usen wallet fallback tendrán
-- la descripción correcta automáticamente. 