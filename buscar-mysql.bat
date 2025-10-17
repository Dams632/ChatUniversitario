@echo off
echo ========================================
echo   BUSCAR INSTALACION DE MySQL
echo ========================================
echo.
echo Buscando MySQL en ubicaciones comunes...
echo.

set FOUND=0

if exist "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    echo [ENCONTRADO] C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe
    set FOUND=1
)

if exist "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe" (
    echo [ENCONTRADO] C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe
    set FOUND=1
)

if exist "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe" (
    echo [ENCONTRADO] C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe
    set FOUND=1
)

if exist "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe" (
    echo [ENCONTRADO] C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe
    set FOUND=1
)

if exist "C:\xampp\mysql\bin\mysql.exe" (
    echo [ENCONTRADO] C:\xampp\mysql\bin\mysql.exe
    set FOUND=1
)

if exist "C:\wamp64\bin\mysql" (
    echo [ENCONTRADO] WAMP instalado en: C:\wamp64\bin\mysql
    dir "C:\wamp64\bin\mysql" /B
    set FOUND=1
)

where mysql.exe >nul 2>&1
if %ERRORLEVEL% EQU 0 (
    echo [ENCONTRADO] MySQL en PATH del sistema:
    where mysql.exe
    set FOUND=1
)

echo.
if %FOUND% EQU 0 (
    echo [NO ENCONTRADO] MySQL no se encuentra en ubicaciones comunes
    echo.
    echo Por favor verifica que MySQL este instalado.
    echo Descarga MySQL desde: https://dev.mysql.com/downloads/mysql/
    echo O usa XAMPP: https://www.apachefriends.org/
) else (
    echo.
    echo Usa una de las rutas anteriores en el script inicializar-db.bat
)

echo.
pause
