package com.chat.cliente.presentacion.gui.helpers;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;

/**
 * Helper para gestionar notificaciones sutiles cuando llegan mensajes nuevos
 * Reproduce un sonido corto y discreto sin ventanas emergentes invasivas
 */
public class NotificacionManager {
    
    // Configuración del sonido de notificación
    private static final float SAMPLE_RATE = 8000f;
    private static final int DURACION_MS = 150; // Sonido muy corto (0.15 segundos)
    private static final double FRECUENCIA = 800.0; // Tono medio-alto agradable
    
    /**
     * Reproducir sonido de notificación sutil
     * No bloquea el hilo, se ejecuta de forma asíncrona
     */
    public static void reproducirNotificacion() {
        new Thread(() -> {
            try {
                reproducirBeep();
            } catch (Exception e) {
                // Si falla el sonido, no hacer nada (notificación silenciosa)
                System.err.println("No se pudo reproducir sonido de notificación: " + e.getMessage());
            }
        }).start();
    }
    
    /**
     * Generar y reproducir un beep suave
     */
    private static void reproducirBeep() throws LineUnavailableException {
        // Generar audio sinusoidal
        byte[] buffer = generarTonoPuro();
        
        // Configurar formato de audio
        AudioFormat audioFormat = new AudioFormat(
            SAMPLE_RATE,  // Sample rate
            8,            // Sample size en bits
            1,            // Canales (mono)
            true,         // Signed
            false         // Big endian
        );
        
        // Crear y reproducir clip
        Clip clip = AudioSystem.getClip();
        clip.open(audioFormat, buffer, 0, buffer.length);
        clip.start();
        
        // Esperar a que termine y cerrar
        try {
            Thread.sleep(DURACION_MS + 50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        clip.close();
    }
    
    /**
     * Generar tono puro sinusoidal
     */
    private static byte[] generarTonoPuro() {
        int numSamples = (int) (SAMPLE_RATE * DURACION_MS / 1000);
        byte[] buffer = new byte[numSamples];
        
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * i * FRECUENCIA / SAMPLE_RATE;
            double amplitud = Math.sin(angle);
            
            // Aplicar fade in/out para evitar clicks
            double envelope = 1.0;
            if (i < numSamples * 0.1) {
                // Fade in (10% inicial)
                envelope = (double) i / (numSamples * 0.1);
            } else if (i > numSamples * 0.7) {
                // Fade out (30% final)
                envelope = (double) (numSamples - i) / (numSamples * 0.3);
            }
            
            // Volumen más bajo (30% del máximo)
            buffer[i] = (byte) (amplitud * envelope * 127.0 * 0.3);
        }
        
        return buffer;
    }
    
    /**
     * Método alternativo: reproducir sonido del sistema (más simple pero menos control)
     */
    public static void reproducirBeepSistema() {
        java.awt.Toolkit.getDefaultToolkit().beep();
    }
}
