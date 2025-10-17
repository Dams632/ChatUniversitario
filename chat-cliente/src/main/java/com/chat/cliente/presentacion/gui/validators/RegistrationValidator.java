package com.chat.cliente.presentacion.gui.validators;

import java.util.regex.Pattern;

/**
 * Validador de datos de registro de usuario
 * Aplica Single Responsibility Principle - Solo se encarga de validar datos de registro
 */
public class RegistrationValidator {
    
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern IP_PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");
    
    /**
     * Validar username
     */
    public ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error("❌ Por favor ingresa un nombre de usuario");
        }
        
        if (username.length() < MIN_USERNAME_LENGTH) {
            return ValidationResult.error("❌ El usuario debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres");
        }
        
        if (username.length() > MAX_USERNAME_LENGTH) {
            return ValidationResult.error("❌ El usuario no puede tener más de " + MAX_USERNAME_LENGTH + " caracteres");
        }
        
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return ValidationResult.error("❌ El usuario solo puede contener letras, números, guiones y guiones bajos");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validar email
     */
    public ValidationResult validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return ValidationResult.error("❌ Por favor ingresa un email");
        }
        
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return ValidationResult.error("❌ Por favor ingresa un email válido (ejemplo: usuario@dominio.com)");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validar password
     */
    public ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.error("❌ Por favor ingresa una contraseña");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error("❌ La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validar que las contraseñas coincidan
     */
    public ValidationResult validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            return ValidationResult.error("❌ Las contraseñas no coinciden");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validar IP del servidor
     */
    public ValidationResult validateServerIP(String ip) {
        if (ip == null || ip.trim().isEmpty()) {
            return ValidationResult.error("❌ Por favor ingresa la IP del servidor");
        }
        
        if (!IP_PATTERN.matcher(ip).matches()) {
            return ValidationResult.error("❌ Por favor ingresa una dirección IP válida (ejemplo: 192.168.1.100)");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validar todos los datos de registro
     */
    public ValidationResult validateRegistrationData(String username, String email, String password, 
                                                     String confirmPassword, String serverIP) {
        ValidationResult usernameResult = validateUsername(username);
        if (!usernameResult.isValid()) {
            return usernameResult;
        }
        
        ValidationResult emailResult = validateEmail(email);
        if (!emailResult.isValid()) {
            return emailResult;
        }
        
        ValidationResult passwordResult = validatePassword(password);
        if (!passwordResult.isValid()) {
            return passwordResult;
        }
        
        ValidationResult passwordMatchResult = validatePasswordMatch(password, confirmPassword);
        if (!passwordMatchResult.isValid()) {
            return passwordMatchResult;
        }
        
        ValidationResult serverIPResult = validateServerIP(serverIP);
        if (!serverIPResult.isValid()) {
            return serverIPResult;
        }
        
        return ValidationResult.success();
    }
}
