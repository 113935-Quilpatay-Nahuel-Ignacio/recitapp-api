-- Script para verificar y entender el estado de los payment IDs de MercadoPago
-- Fecha: 2025-06-18

-- 1. Verificar el estado actual de las transacciones
SELECT 
    t.id,
    t.external_reference,
    t.total_amount,
    t.transaction_date,
    pm.name as payment_method_name,
    ts.status_name,
    u.first_name,
    u.last_name
FROM transactions t
LEFT JOIN payment_methods pm ON t.payment_method_id = pm.id
LEFT JOIN transaction_status ts ON t.status_id = ts.id
LEFT JOIN users u ON t.user_id = u.id
WHERE pm.name = 'MERCADOPAGO'
ORDER BY t.transaction_date DESC;

-- 2. Identificar transacciones sin payment ID real de MercadoPago
SELECT 
    t.id,
    t.external_reference,
    CASE 
        WHEN t.external_reference IS NULL THEN 'SIN_REFERENCIA'
        WHEN t.external_reference LIKE '%|%' THEN 'CON_PAYMENT_ID'
        ELSE 'SOLO_REFERENCIA_ORIGINAL'
    END as tipo_referencia,
    t.total_amount,
    t.transaction_date
FROM transactions t
LEFT JOIN payment_methods pm ON t.payment_method_id = pm.id
WHERE pm.name = 'MERCADOPAGO'
ORDER BY t.transaction_date DESC;

-- 3. Contar transacciones por tipo de referencia
SELECT 
    CASE 
        WHEN t.external_reference IS NULL THEN 'SIN_REFERENCIA'
        WHEN t.external_reference LIKE '%|%' THEN 'CON_PAYMENT_ID'
        ELSE 'SOLO_REFERENCIA_ORIGINAL'
    END as tipo_referencia,
    COUNT(*) as cantidad
FROM transactions t
LEFT JOIN payment_methods pm ON t.payment_method_id = pm.id
WHERE pm.name = 'MERCADOPAGO'
GROUP BY 
    CASE 
        WHEN t.external_reference IS NULL THEN 'SIN_REFERENCIA'
        WHEN t.external_reference LIKE '%|%' THEN 'CON_PAYMENT_ID'
        ELSE 'SOLO_REFERENCIA_ORIGINAL'
    END;

-- 4. Para verificar la estructura de la tabla transactions ejecutar:
-- SELECT column_name, data_type, is_nullable FROM information_schema.columns WHERE table_name = 'transactions';

-- NOTAS IMPORTANTES:
-- 
-- Para que los reembolsos de MercadoPago funcionen correctamente según
-- la documentación oficial (https://www.mercadopago.com.ar/developers/es/docs),
-- necesitamos:
--
-- 1. El payment ID real de MercadoPago (no la referencia externa generada por nosotros)
-- 2. Este payment ID se obtiene en el webhook cuando MercadoPago confirma el pago
-- 3. Se almacena en external_reference con formato: "REFERENCIA_ORIGINAL|PAYMENT_ID"
--
-- SOLUCIÓN PARA TRANSACCIONES EXISTENTES:
-- Las transacciones que ya fueron procesadas antes de implementar el webhook
-- no tienen el payment ID real. Para estas transacciones:
-- - Los reembolsos automáticamente usarán el sistema de wallet (fallback)
-- - El dinero se acreditará en la billetera virtual del usuario
-- - El sistema funcionará correctamente pero sin integración directa con MercadoPago
--
-- SOLUCIÓN PARA TRANSACCIONES FUTURAS:
-- Las nuevas transacciones procesadas a través del webhook ya capturan
-- correctamente el payment ID real y podrán ser reembolsadas directamente
-- en MercadoPago. 