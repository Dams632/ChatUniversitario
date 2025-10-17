@echo off
REM ========================================
REM Script para compilar UN componente especifico
REM Uso: build-component.bat [common|transcripcion|servidor|cliente]
REM ========================================

if "%1"=="" (
    echo.
    echo ERROR: Debe especificar un componente
    echo.
    echo Uso: build-component.bat [COMPONENTE]
    echo.
    echo Componentes disponibles:
    echo   common        - Modulo comun compartido
    echo   transcripcion - Servicio de transcripcion
    echo   servidor      - Servidor TCP
    echo   cliente       - Cliente GUI
    echo   all           - Todos los componentes
    echo.
    pause
    exit /b 1
)

set COMPONENT=%1

if "%COMPONENT%"=="all" (
    call build-all-components.bat
    exit /b 0
)

if "%COMPONENT%"=="common" (
    echo.
    echo Compilando chat-common...
    call mvn clean compile -pl chat-common
    echo.
    if %errorlevel%==0 (
        echo [OK] JAR generado: chat-common\target\chat-common-1.0.0.jar
    )
    pause
    exit /b %errorlevel%
)

if "%COMPONENT%"=="transcripcion" (
    echo.
    echo Compilando chat-transcripcion...
    call mvn clean package -pl chat-transcripcion -DskipTests
    echo.
    if %errorlevel%==0 (
        echo [OK] JAR generado: chat-transcripcion\target\chat-transcripcion-1.0.0-jar-with-dependencies.jar
    )
    pause
    exit /b %errorlevel%
)

if "%COMPONENT%"=="servidor" (
    echo.
    echo Compilando chat-servidor...
    call mvn clean package -pl chat-servidor -DskipTests
    echo.
    if %errorlevel%==0 (
        echo [OK] JAR generado: chat-servidor\target\chat-servidor.jar
    )
    pause
    exit /b %errorlevel%
)

if "%COMPONENT%"=="cliente" (
    echo.
    echo Compilando chat-cliente...
    call mvn clean package -pl chat-cliente -DskipTests
    echo.
    if %errorlevel%==0 (
        echo [OK] JAR generado: chat-cliente\target\chat-cliente.jar
    )
    pause
    exit /b %errorlevel%
)

echo.
echo ERROR: Componente '%COMPONENT%' no reconocido
echo Componentes validos: common, transcripcion, servidor, cliente, all
echo.
pause
exit /b 1
