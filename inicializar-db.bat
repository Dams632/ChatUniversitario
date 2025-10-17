@echo off
echo ========================================
echo   INICIALIZACION BASE DE DATOS MySQL
echo   Chat Universitario
echo ========================================
echo.

REM Configuracion
set MYSQL_USER=root
set DB_SCRIPT=database\init_mysql.sql

echo Este script creara la base de datos 'chat_universitario' en MySQL
echo.
echo Asegurate de que MySQL este ejecutandose antes de continuar.
echo.

REM Solicitar password de MySQL
set /p MYSQL_PASSWORD="Ingresa la contraseña de MySQL para el usuario '%MYSQL_USER%': "

echo.
echo ----------------------------------------
echo Conectando a MySQL...
echo ----------------------------------------

REM Ejecutar el script SQL
mysql -u %MYSQL_USER% -p%MYSQL_PASSWORD% < %DB_SCRIPT%

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   BASE DE DATOS CREADA EXITOSAMENTE
    echo ========================================
    echo.
    echo La base de datos 'chat_universitario' ha sido creada con las siguientes tablas:
    echo   - usuarios
    echo   - grupos
    echo   - grupo_miembros
    echo   - canales
    echo   - canal_miembros
    echo   - solicitudes_canal
    echo   - mensajes
    echo   - archivos_audio
    echo   - sesiones
    echo.
    echo Ahora puedes configurar el archivo:
    echo   chat-servidor\src\main\resources\config.properties
    echo.
    echo Y ejecutar el servidor con:
    echo   run-servidor.bat
    echo.
) else (
    echo.
    echo ========================================
    echo   ERROR AL CREAR LA BASE DE DATOS
    echo ========================================
    echo.
    echo Posibles causas:
    echo   1. MySQL no esta ejecutandose
    echo   2. Contraseña incorrecta
    echo   3. Usuario no tiene permisos
    echo.
    echo Verifica e intenta nuevamente.
    echo.
)

pause
