@echo off
REM ========================================
REM Script para compilar TODOS los componentes
REM Genera JARs independientes para cada módulo
REM ========================================

echo.
echo ========================================
echo   BUILD ALL COMPONENTS
echo ========================================
echo.

echo [1/5] Compilando chat-common (modulo compartido)...
call mvn clean compile -pl chat-common -q
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al compilar chat-common
    pause
    exit /b 1
)
echo [OK] chat-common compilado exitosamente

echo.
echo [2/5] Compilando chat-transcripcion (servicio de transcripcion)...
call mvn clean package -pl chat-transcripcion -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al compilar chat-transcripcion
    pause
    exit /b 1
)
echo [OK] chat-transcripcion compilado exitosamente

echo.
echo [3/5] Compilando chat-servidor (servidor TCP)...
call mvn clean package -pl chat-servidor -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al compilar chat-servidor
    pause
    exit /b 1
)
echo [OK] chat-servidor compilado exitosamente

echo.
echo [4/5] Compilando chat-cliente (aplicacion cliente)...
call mvn clean package -pl chat-cliente -DskipTests -q
if %errorlevel% neq 0 (
    echo [ERROR] Fallo al compilar chat-cliente
    pause
    exit /b 1
)
echo [OK] chat-cliente compilado exitosamente

echo.
echo [5/5] Verificando JARs generados...
echo.

if exist "chat-common\target\chat-common-1.0.0.jar" (
    echo [OK] chat-common\target\chat-common-1.0.0.jar
) else (
    echo [WARN] chat-common JAR no encontrado
)

if exist "chat-transcripcion\target\chat-transcripcion-1.0.0-jar-with-dependencies.jar" (
    echo [OK] chat-transcripcion\target\chat-transcripcion-1.0.0-jar-with-dependencies.jar
) else (
    echo [WARN] chat-transcripcion JAR no encontrado
)

if exist "chat-servidor\target\chat-servidor.jar" (
    echo [OK] chat-servidor\target\chat-servidor.jar
) else (
    echo [WARN] chat-servidor JAR no encontrado
)

if exist "chat-cliente\target\chat-cliente.jar" (
    echo [OK] chat-cliente\target\chat-cliente.jar
) else (
    echo [WARN] chat-cliente JAR no encontrado
)

echo.
echo ========================================
echo   BUILD COMPLETADO
echo ========================================
echo.
echo Componentes generados:
echo   - chat-common: Libreria compartida (DTOs, modelos, patrones)
echo   - chat-transcripcion: Servicio de transcripcion independiente
echo   - chat-servidor: Servidor TCP ejecutable
echo   - chat-cliente: Cliente GUI ejecutable
echo.
pause
