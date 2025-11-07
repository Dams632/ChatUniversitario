@echo off
setlocal

echo ========================================
echo    INICIANDO SERVIDOR CHAT UNIVERSITARIO
echo ========================================
echo.

set "SCRIPT_DIR=%~dp0"
set "CONFIG_FILE=%~1"

if "%CONFIG_FILE%"=="" (
	if exist "%SCRIPT_DIR%config\chat-servidor.properties" (
		set "CONFIG_FILE=%SCRIPT_DIR%config\chat-servidor.properties"
	) else if exist "%SCRIPT_DIR%config.properties" (
		set "CONFIG_FILE=%SCRIPT_DIR%config.properties"
	)
)

if not "%CONFIG_FILE%"=="" (
	for %%I in ("%CONFIG_FILE%") do set "CONFIG_FILE=%%~fI"
)

cd /d "%SCRIPT_DIR%chat-servidor\target"
set "JAVA_FLAGS=-Xms256m -Xmx1024m"

if not "%CONFIG_FILE%"=="" (
	if exist "%CONFIG_FILE%" (
		echo Usando configuración externa: %CONFIG_FILE%
		java %JAVA_FLAGS% -DconfigFile="%CONFIG_FILE%" -jar chat-servidor.jar
	) else (
		echo ADVERTENCIA: No se encontró %CONFIG_FILE%. Usando configuración por defecto del JAR.
		java %JAVA_FLAGS% -jar chat-servidor.jar
	)
) else (
	java %JAVA_FLAGS% -jar chat-servidor.jar
)

pause
endlocal
