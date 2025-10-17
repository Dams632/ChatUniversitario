package com.chat.common.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilidad para leer archivos de configuración
 */
public class ConfiguracionManager {
    
    private Properties propiedades;
    private String archivoConfig;
    
    public ConfiguracionManager(String archivoConfig) {
        this.archivoConfig = archivoConfig;
        this.propiedades = new Properties();
        cargarConfiguracion();
    }
    
    /**
     * Cargar configuración desde archivo
     */
    private void cargarConfiguracion() {
        try {
            // Intentar cargar desde classpath
            InputStream input = getClass().getClassLoader().getResourceAsStream(archivoConfig);
            
            if (input == null) {
                // Intentar cargar desde sistema de archivos
                input = new FileInputStream(archivoConfig);
            }
            
            propiedades.load(input);
            input.close();
            System.out.println("Configuración cargada desde: " + archivoConfig);
            
        } catch (IOException e) {
            System.err.println("Error al cargar configuración: " + e.getMessage());
            cargarConfiguracionPorDefecto();
        }
    }
    
    /**
     * Cargar valores por defecto si no se encuentra el archivo
     */
    private void cargarConfiguracionPorDefecto() {
        System.out.println("Usando configuración por defecto");
    }
    
    /**
     * Obtener propiedad como String
     */
    public String getPropiedad(String clave) {
        return propiedades.getProperty(clave);
    }
    
    /**
     * Obtener propiedad como String con valor por defecto
     */
    public String getPropiedad(String clave, String valorPorDefecto) {
        return propiedades.getProperty(clave, valorPorDefecto);
    }
    
    /**
     * Obtener propiedad como int
     */
    public int getPropiedadInt(String clave, int valorPorDefecto) {
        String valor = propiedades.getProperty(clave);
        if (valor != null) {
            try {
                return Integer.parseInt(valor);
            } catch (NumberFormatException e) {
                System.err.println("Valor inválido para " + clave + ": " + valor);
            }
        }
        return valorPorDefecto;
    }
    
    /**
     * Obtener propiedad como boolean
     */
    public boolean getPropiedadBoolean(String clave, boolean valorPorDefecto) {
        String valor = propiedades.getProperty(clave);
        if (valor != null) {
            return Boolean.parseBoolean(valor);
        }
        return valorPorDefecto;
    }
    
    /**
     * Obtener propiedad como long
     */
    public long getPropiedadLong(String clave, long valorPorDefecto) {
        String valor = propiedades.getProperty(clave);
        if (valor != null) {
            try {
                return Long.parseLong(valor);
            } catch (NumberFormatException e) {
                System.err.println("Valor inválido para " + clave + ": " + valor);
            }
        }
        return valorPorDefecto;
    }
    
    /**
     * Verificar si existe una propiedad
     */
    public boolean existePropiedad(String clave) {
        return propiedades.containsKey(clave);
    }
    
    /**
     * Obtener todas las propiedades
     */
    public Properties getPropiedades() {
        return propiedades;
    }
}
