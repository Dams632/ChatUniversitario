@echo off
setlocal enabledelayedexpansion
echo ========================================
echo   INICIALIZACION BASE DE DATOS MySQL
echo   Chat Universitario
echo ========================================
echo.

REM Configuracion
set MYSQL_USER=root
set DB_SCRIPT=database\init_mysql.sql
set MYSQL_CMD=

REM Buscar MySQL en ubicaciones comunes
echo Buscando instalacion de MySQL...
echo.

if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
    echo [OK] MySQL encontrado en: %MYSQL_CMD%
) else if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
    set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe
    echo [OK] MySQL encontrado en: %MYSQL_CMD%
) else if exist "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" (
    set MYSQL_CMD=C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe
    echo [OK] MySQL encontrado en: %MYSQL_CMD%
) else if exist "C:\xampp\mysql\bin\mysql.exe" (
    set MYSQL_CMD=C:\xampp\mysql\bin\mysql.exe
    echo [OK] MySQL encontrado en XAMPP: %MYSQL_CMD%
) else if exist "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe" (
    set MYSQL_CMD=C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe
    echo [OK] MySQL encontrado en WAMP: %MYSQL_CMD%
) else (
    where mysql.exe >nul 2>&1
    if %ERRORLEVEL% EQU 0 (
        set MYSQL_CMD=mysql.exe
        echo [OK] MySQL encontrado en PATH del sistema
    ) else (
        echo [ERROR] MySQL no se encontro en las ubicaciones comunes.
        echo.
        echo Por favor, ingresa la ruta completa a mysql.exe
        echo Ejemplo: C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
        echo.
        set /p MYSQL_CMD="Ruta de mysql.exe: "
        
        if not exist "!MYSQL_CMD!" (
            echo.
            echo [ERROR] La ruta especificada no existe: !MYSQL_CMD!
            echo.
            echo Verifica la instalacion de MySQL e intenta nuevamente.
            pause
            exit /b 1
        )
    )
)

echo.
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
"%MYSQL_CMD%" -u %MYSQL_USER% -p%MYSQL_PASSWORD% < %DB_SCRIPT%

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
