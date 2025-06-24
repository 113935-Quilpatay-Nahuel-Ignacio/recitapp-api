@echo off
echo.
echo ================================
echo   VERIFICACION SALDO USUARIO TEST
echo ================================
echo.

set ACCESS_TOKEN=TEST-4403492759962042-060223-ccef50ad15229a32784b2504ce3d4f8c-1020599231
set USER_ID=1020599231

echo Consultando saldo del usuario de prueba...
echo.

curl -X GET "https://api.mercadopago.com/users/%USER_ID%" ^
  -H "Authorization: Bearer %ACCESS_TOKEN%" ^
  -H "Content-Type: application/json"

echo.
echo.
echo ================================
echo   RESULTADO:
echo ================================
echo Si aparece informacion del usuario, las credenciales son correctas.
echo Para agregar saldo, ve al Dashboard de MercadoPago:
echo https://www.mercadopago.com.ar/developers/panel/app/4403492759962042
echo.
pause 