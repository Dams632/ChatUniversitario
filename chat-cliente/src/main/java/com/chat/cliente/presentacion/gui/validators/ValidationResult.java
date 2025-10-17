package com.chat.cliente.presentacion.gui.validators;

/**
 * Resultado de una validación
 * Aplica Value Object pattern - Inmutable y sin comportamiento complejo
 */
public class ValidationResult {
    
    private final boolean valid;
    private final String errorMessage;
    
    private ValidationResult(boolean valid, String errorMessage) {
        this.valid = valid;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Crear resultado exitoso
     */
    public static ValidationResult success() {
        return new ValidationResult(true, null);
    }
    
    /**
     * Crear resultado con error
     */
    public static ValidationResult error(String errorMessage) {
        return new ValidationResult(false, errorMessage);
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }
}
