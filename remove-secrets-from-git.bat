@echo off
echo.
echo ===============================================
echo   REMOVIENDO ARCHIVOS CON SECRETS DE GIT
echo ===============================================
echo.

echo IMPORTANTE: Este script NO borra los archivos, solo deja de trackearlos en Git
echo.

echo Removiendo archivos sensibles del tracking de Git...
echo.

echo --- LOGS CON SECRETS ---
git rm --cached logs/recitapp.log
git rm --cached "logs/*.log"

echo.
echo --- APPLICATION PROPERTIES ---
git rm --cached src/main/resources/application.properties

echo.
echo --- FIREBASE CREDENTIALS ---
git rm --cached "src/main/resources/firebase/*.json"

echo.
echo --- FRONTEND ENVIRONMENT ---
git rm --cached ../recitapp-front/src/environments/environment.ts

echo.
echo --- VERIFICANDO ESTADO ---
git status

echo.
echo --- SIGUIENTE PASO ---
echo Para confirmar los cambios, ejecuta:
echo git add .gitignore
echo git commit -m "Remove sensitive files from Git tracking"
echo.

pause 