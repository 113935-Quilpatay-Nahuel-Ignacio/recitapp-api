@echo off
echo.
echo ================================
echo   VERIFICACION ARCHIVOS SENSIBLES
echo ================================
echo.

echo Verificando archivos con credenciales que pueden estar en Git...
echo.

echo --- BACKEND SENSIBLES ---
if exist "src\main\resources\application.properties" (
    echo [WARNING] application.properties existe - verificar si contiene credenciales reales
)

if exist "src\main\resources\firebase\firebase-service-account.json" (
    echo [CRITICAL] firebase-service-account.json existe - NO debe estar en Git
)

if exist "src\main\resources\firebase\recitapp-niquilpatay-firebase-adminsdk-fbsvc-edc354dbfc.json" (
    echo [CRITICAL] Archivo Firebase adminsdk existe - NO debe estar en Git
)

echo.
echo --- VERIFICANDO TRACKING EN GIT ---
git ls-files | findstr "application.properties" >nul 2>&1
if %errorlevel% equ 0 (
    echo [ERROR] application.properties esta siendo trackeado por Git!
) else (
    echo [OK] application.properties no esta siendo trackeado
)

git ls-files | findstr "firebase.*\.json" >nul 2>&1
if %errorlevel% equ 0 (
    echo [ERROR] Archivos Firebase JSON estan siendo trackeados por Git!
) else (
    echo [OK] Archivos Firebase JSON no estan siendo trackeados
)

git ls-files | findstr "environment\.ts" >nul 2>&1
if %errorlevel% equ 0 (
    echo [ERROR] environment.ts esta siendo trackeado por Git!
) else (
    echo [OK] environment.ts no esta siendo trackeado
)

git ls-files | findstr "logs.*\.log" >nul 2>&1
if %errorlevel% equ 0 (
    echo [ERROR] Archivos de log estan siendo trackeados por Git!
) else (
    echo [OK] Archivos de log no estan siendo trackeados
)

echo.
echo --- COMANDOS PARA LIMPIAR ---
echo Si hay archivos trackeados con secrets, ejecutar:
echo git rm --cached src/main/resources/application.properties
echo git rm --cached src/main/resources/firebase/*.json
echo git rm --cached ../recitapp-front/src/environments/environment.ts
echo git rm --cached logs/*.log
echo git rm --cached logs/recitapp.log
echo git commit -m "Remove sensitive files from tracking"
echo.

echo --- VERIFICAR CONTENIDO ---
echo Revisar manualmente si estos archivos contienen credenciales reales:
if exist "src\main\resources\application.properties" (
    echo - src\main\resources\application.properties
)
if exist "..\recitapp-front\src\environments\environment.ts" (
    echo - ..\recitapp-front\src\environments\environment.ts
)
echo.

pause 