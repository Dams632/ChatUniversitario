# Script PowerShell para inicializar la base de datos MySQL
# Chat Universitario

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  INICIALIZACION BASE DE DATOS MySQL" -ForegroundColor Cyan
Write-Host "  Chat Universitario" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Configuración
$mysqlUser = "root"
$dbScript = "database\init_mysql.sql"
$mysqlCmd = $null

# Buscar MySQL en ubicaciones comunes
Write-Host "Buscando instalación de MySQL..." -ForegroundColor Yellow
Write-Host ""

$possiblePaths = @(
    "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe",
    "C:\Program Files\MySQL\MySQL Server 9.0\bin\mysql.exe",
    "C:\Program Files (x86)\MySQL\MySQL Server 8.0\bin\mysql.exe",
    "C:\xampp\mysql\bin\mysql.exe",
    "C:\wamp64\bin\mysql\mysql8.0.31\bin\mysql.exe",
    "C:\wamp\bin\mysql\mysql8.0.31\bin\mysql.exe"
)

foreach ($path in $possiblePaths) {
    if (Test-Path $path) {
        $mysqlCmd = $path
        Write-Host "[OK] MySQL encontrado en: $mysqlCmd" -ForegroundColor Green
        break
    }
}

# Si no se encontró, buscar en PATH
if (-not $mysqlCmd) {
    $mysqlInPath = Get-Command mysql.exe -ErrorAction SilentlyContinue
    if ($mysqlInPath) {
        $mysqlCmd = $mysqlInPath.Source
        Write-Host "[OK] MySQL encontrado en PATH: $mysqlCmd" -ForegroundColor Green
    }
}

# Si aún no se encontró, pedir al usuario
if (-not $mysqlCmd) {
    Write-Host "[ERROR] MySQL no se encontró en las ubicaciones comunes." -ForegroundColor Red
    Write-Host ""
    Write-Host "Por favor, ingresa la ruta completa a mysql.exe"
    Write-Host "Ejemplo: C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe"
    Write-Host ""
    $mysqlCmd = Read-Host "Ruta de mysql.exe"
    
    if (-not (Test-Path $mysqlCmd)) {
        Write-Host ""
        Write-Host "[ERROR] La ruta especificada no existe: $mysqlCmd" -ForegroundColor Red
        Write-Host ""
        Write-Host "Verifica la instalación de MySQL e intenta nuevamente."
        Read-Host "Presiona Enter para salir"
        exit 1
    }
}

Write-Host ""
Write-Host "Este script creará la base de datos 'chat_universitario' en MySQL"
Write-Host ""
Write-Host "Asegúrate de que MySQL esté ejecutándose antes de continuar."
Write-Host ""

# Solicitar contraseña
$mysqlPasswordSecure = Read-Host "Ingresa la contraseña de MySQL para el usuario '$mysqlUser'" -AsSecureString
$mysqlPassword = [Runtime.InteropServices.Marshal]::PtrToStringAuto(
    [Runtime.InteropServices.Marshal]::SecureStringToBSTR($mysqlPasswordSecure))

Write-Host ""
Write-Host "----------------------------------------" -ForegroundColor Yellow
Write-Host "Conectando a MySQL..." -ForegroundColor Yellow
Write-Host "----------------------------------------" -ForegroundColor Yellow

# Ejecutar el script SQL
try {
    $scriptContent = Get-Content $dbScript -Raw
    $scriptContent | & $mysqlCmd -u $mysqlUser -p"$mysqlPassword" 2>&1 | Out-Null
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host ""
        Write-Host "========================================" -ForegroundColor Green
        Write-Host "  BASE DE DATOS CREADA EXITOSAMENTE" -ForegroundColor Green
        Write-Host "========================================" -ForegroundColor Green
        Write-Host ""
        Write-Host "La base de datos 'chat_universitario' ha sido creada con las siguientes tablas:" -ForegroundColor White
        Write-Host "  - usuarios" -ForegroundColor Cyan
        Write-Host "  - grupos" -ForegroundColor Cyan
        Write-Host "  - grupo_miembros" -ForegroundColor Cyan
        Write-Host "  - canales" -ForegroundColor Cyan
        Write-Host "  - canal_miembros" -ForegroundColor Cyan
        Write-Host "  - solicitudes_canal" -ForegroundColor Cyan
        Write-Host "  - mensajes" -ForegroundColor Cyan
        Write-Host "  - archivos_audio" -ForegroundColor Cyan
        Write-Host "  - sesiones" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Ahora puedes configurar el archivo:" -ForegroundColor Yellow
        Write-Host "  chat-servidor\src\main\resources\config.properties"
        Write-Host ""
        Write-Host "Y ejecutar el servidor con:" -ForegroundColor Yellow
        Write-Host "  run-servidor.bat"
        Write-Host ""
    } else {
        throw "Error al ejecutar el script SQL"
    }
} catch {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "  ERROR AL CREAR LA BASE DE DATOS" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Error: $_" -ForegroundColor Red
    Write-Host ""
    Write-Host "Posibles causas:" -ForegroundColor Yellow
    Write-Host "  1. MySQL no está ejecutándose"
    Write-Host "  2. Contraseña incorrecta"
    Write-Host "  3. Usuario no tiene permisos"
    Write-Host ""
    Write-Host "Verifica e intenta nuevamente."
    Write-Host ""
}

Read-Host "Presiona Enter para salir"
