@echo off
echo ========================================
echo    RECITAPP - MERCADOPAGO LOG MONITOR
echo ========================================
echo.

REM Crear directorios de logs si no existen
if not exist "logs" mkdir logs

echo 📂 Creando directorio de logs...
if not exist "logs\mercadopago" mkdir logs\mercadopago
echo ✅ Directorio de logs creado

echo.
echo 🚀 Iniciando monitoreo de logs de MercadoPago...
echo.
echo Opciones disponibles:
echo [1] Ver logs de MercadoPago en tiempo real
echo [2] Ver logs de Webhooks en tiempo real  
echo [3] Ver logs generales en tiempo real
echo [4] Limpiar logs anteriores
echo [5] Mostrar últimas 50 líneas de MercadoPago
echo [6] Buscar errores en logs
echo [7] Salir
echo.

:menu
set /p choice="Selecciona una opción (1-7): "

if "%choice%"=="1" goto mercadopago_logs
if "%choice%"=="2" goto webhook_logs
if "%choice%"=="3" goto general_logs
if "%choice%"=="4" goto clean_logs
if "%choice%"=="5" goto last_50_mp
if "%choice%"=="6" goto search_errors
if "%choice%"=="7" goto exit

echo ❌ Opción inválida. Intenta de nuevo.
goto menu

:mercadopago_logs
echo.
echo 💳 Monitoreando logs de MercadoPago...
echo Presiona Ctrl+C para volver al menú
echo ======================================
if exist "logs\mercadopago.log" (
    powershell -Command "Get-Content -Path 'logs\mercadopago.log' -Wait -Tail 10"
) else (
    echo ⚠️ Archivo de log de MercadoPago no encontrado. Iniciando aplicación primero...
    echo Asegúrate de que la aplicación esté ejecutándose.
)
goto menu

:webhook_logs
echo.
echo 🎣 Monitoreando logs de Webhooks...
echo Presiona Ctrl+C para volver al menú
echo ===================================
if exist "logs\mercadopago-webhooks.log" (
    powershell -Command "Get-Content -Path 'logs\mercadopago-webhooks.log' -Wait -Tail 10"
) else (
    echo ⚠️ Archivo de log de Webhooks no encontrado.
    echo Los webhooks se registrarán cuando lleguen notificaciones.
)
goto menu

:general_logs
echo.
echo 📋 Monitoreando logs generales...
echo Presiona Ctrl+C para volver al menú
echo ==================================
if exist "logs\recitapp.log" (
    powershell -Command "Get-Content -Path 'logs\recitapp.log' -Wait -Tail 10"
) else (
    echo ⚠️ Archivo de log general no encontrado.
)
goto menu

:clean_logs
echo.
echo 🧹 Limpiando logs anteriores...
del /q "logs\*.log" 2>nul
echo ✅ Logs limpiados
echo.
goto menu

:last_50_mp
echo.
echo 📄 Últimas 50 líneas de MercadoPago:
echo ====================================
if exist "logs\mercadopago.log" (
    powershell -Command "Get-Content -Path 'logs\mercadopago.log' -Tail 50"
) else (
    echo ⚠️ Archivo de log de MercadoPago no encontrado.
)
echo.
pause
goto menu

:search_errors
echo.
echo 🔍 Buscando errores en logs...
echo ==============================
if exist "logs\mercadopago.log" (
    echo Errores en MercadoPago:
    findstr /i "error\|exception\|failed\|❌" "logs\mercadopago.log" 2>nul
    echo.
)
if exist "logs\recitapp.log" (
    echo Errores generales:
    findstr /i "error\|exception\|failed\|❌" "logs\recitapp.log" 2>nul
    echo.
)
pause
goto menu

:exit
echo.
echo 👋 ¡Hasta luego! Recuerda revisar los logs para debugging.
echo.
echo 💡 Tips para testing:
echo - Usa usuarios de test de MercadoPago
echo - Verifica que los webhooks lleguen correctamente
echo - Monitora los archivos logs/mercadopago.log para debugging
echo.
pause
exit

REM ================================
REM   INSTRUCCIONES DE USO
REM ================================
REM 
REM 1. Ejecuta este script desde la carpeta raíz del proyecto backend
REM 2. Asegúrate de que la aplicación Spring Boot esté ejecutándose
REM 3. Usa las opciones del menú para monitorear diferentes tipos de logs
REM 4. Los logs se guardan en la carpeta "logs" automáticamente
REM 
REM ARCHIVOS DE LOG GENERADOS:
REM - logs/mercadopago.log: Logs específicos de MercadoPago
REM - logs/mercadopago-webhooks.log: Logs de webhooks únicamente  
REM - logs/recitapp.log: Logs generales de la aplicación
REM 
REM TESTING RECOMENDADO:
REM - Crear preferencia de pago → Ver logs en tiempo real
REM - Procesar pago de prueba → Verificar webhook logs
REM - Probar reembolsos → Monitorear errores si los hay
REM ================================ 