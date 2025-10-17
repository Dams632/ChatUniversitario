package com.chat.cliente.presentacion.gui.utils;

import java.awt.Font;
import java.awt.GraphicsEnvironment;

/**
 * Utilidad para crear fuentes con soporte de emojis
 */
public class FontHelper {
    
    private static Font emojiFont = null;
    private static boolean initialized = false;
    
    /**
     * Obtener fuente con soporte de emojis
     */
    public static Font getEmojiFont(int style, int size) {
        if (!initialized) {
            initializeEmojiFont();
        }
        
        if (emojiFont != null) {
            return emojiFont.deriveFont(style, size);
        }
        
        // Fallback a Segoe UI
        return new Font("Segoe UI", style, size);
    }
    
    /**
     * Inicializar fuente con soporte de emojis
     */
    private static void initializeEmojiFont() {
        initialized = true;
        
        // Obtener todas las fuentes disponibles
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        
        // Prioridad de fuentes que soportan emojis
        String[] preferredFonts = {
            "Segoe UI Emoji",      // Windows 10+
            "Segoe UI Symbol",     // Windows 8+
            "Apple Color Emoji",   // macOS
            "Noto Color Emoji",    // Linux
            "Android Emoji",       // Android
            "Twitter Color Emoji", // Web
            "Symbola",            // Universal
            "Arial Unicode MS",    // Fallback
            "Lucida Sans Unicode"  // Fallback
        };
        
        // Buscar la primera fuente disponible
        for (String preferredFont : preferredFonts) {
            for (String availableFont : fontNames) {
                if (availableFont.equalsIgnoreCase(preferredFont)) {
                    try {
                        emojiFont = new Font(availableFont, Font.PLAIN, 12);
                        System.out.println("Fuente de emoji encontrada: " + availableFont);
                        return;
                    } catch (Exception e) {
                        System.err.println("Error al cargar fuente: " + availableFont);
                    }
                }
            }
        }
        
        // Si no se encuentra ninguna, intentar crear una fuente compuesta
        try {
            emojiFont = new Font("Dialog", Font.PLAIN, 12);
        } catch (Exception e) {
            System.err.println("No se pudo cargar una fuente con soporte de emojis");
        }
    }
    
    /**
     * Crear una fuente con soporte de emojis para etiquetas
     */
    public static Font getLabelFont(int size) {
        return getEmojiFont(Font.PLAIN, size);
    }
    
    /**
     * Crear una fuente con soporte de emojis para etiquetas en negrita
     */
    public static Font getBoldLabelFont(int size) {
        return getEmojiFont(Font.BOLD, size);
    }
    
    /**
     * Crear una fuente con soporte de emojis para botones
     */
    public static Font getButtonFont(int size) {
        return getEmojiFont(Font.BOLD, size);
    }
    
    /**
     * Crear una fuente con soporte de emojis para texto
     */
    public static Font getTextFont(int size) {
        return getEmojiFont(Font.PLAIN, size);
    }
}
