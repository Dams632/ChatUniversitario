package com.chat.cliente.presentacion.gui.validators;

/**
 * Validador de credenciales de login
 * Aplica Single Responsibility Principle - Solo se encarga de validar credenciales
 */
public class LoginValidator {
    
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 4;
    
    /**
     * Validar username
     * @param username Username a validar
     * @return ValidationResult con el resultado de la validación
     */
    public ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.error("❌ Por favor ingresa tu usuario");
        }
        
        if (username.length() < MIN_USERNAME_LENGTH) {
            return ValidationResult.error("❌ El usuario debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validar password
     * @param password Password a validar
     * @return ValidationResult con el resultado de la validación
     */
    public ValidationResult validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return ValidationResult.error("❌ Por favor ingresa tu contraseña");
        }
        
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return ValidationResult.error("❌ La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        
        return ValidationResult.success();
    }
    
    /**
     * Validar credenciales completas
     * @param username Username a validar
     * @param password Password a validar
     * @return ValidationResult con el resultado de la validación
     */
    public ValidationResult validateCredentials(String username, String password) {
        ValidationResult usernameResult = validateUsername(username);
        if (!usernameResult.isValid()) {
            return usernameResult;
        }
        
        ValidationResult passwordResult = validatePassword(password);
        if (!passwordResult.isValid()) {
            return passwordResult;
        }
        
        return ValidationResult.success();
    }
}
