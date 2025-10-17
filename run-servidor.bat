@echo off
echo ========================================
echo    INICIANDO SERVIDOR CHAT UNIVERSITARIO
echo ========================================
echo.

cd chat-servidor\target
java -Xms256m -Xmx1024m -jar chat-servidor.jar

pause
