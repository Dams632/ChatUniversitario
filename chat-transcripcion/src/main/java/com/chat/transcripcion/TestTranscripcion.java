package com.chat.transcripcion;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Clase de prueba para el servicio de transcripción
 */
public class TestTranscripcion {
    
    public static void main(String[] args) {
        System.out.println("=".repeat(60));
        System.out.println("   TEST DE TRANSCRIPCIÓN DE AUDIO - VOSK");
        System.out.println("=".repeat(60));
        
        // Ruta al modelo (debes descargar el modelo primero)
        String rutaModelo = obtenerRutaModelo();
        
        if (rutaModelo == null) {
            System.err.println("\n❌ ERROR: No se encontró el modelo de Vosk");
            System.out.println("\n📥 INSTRUCCIONES PARA DESCARGAR EL MODELO:");
            System.out.println("   1. Visita: https://alphacephei.com/vosk/models");
            System.out.println("   2. Descarga: vosk-model-small-es-0.42.zip (~40MB)");
            System.out.println("   3. Descomprime en: chat-transcripcion/src/main/resources/models/");
            System.out.println("   4. La ruta final debe ser:");
            System.out.println("      chat-transcripcion/src/main/resources/models/vosk-model-small-es-0.42/");
            return;
        }
        
        // Inicializar servicio
        ServicioTranscripcion servicio = ServicioTranscripcion.obtenerInstancia();
        boolean inicializado = servicio.inicializar(rutaModelo);
        
        if (!inicializado) {
            System.err.println("\n❌ Error al inicializar el servicio de transcripción");
            return;
        }
        
        System.out.println("\n✅ Servicio de transcripción listo");
        System.out.println("   Modelo: " + servicio.getRutaModelo());
        
        // Buscar archivos WAV de prueba
        buscarArchivosWAV();
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("   Para probar con audio real:");
        System.out.println("   1. Coloca un archivo WAV en resources/test/");
        System.out.println("   2. Ejecuta nuevamente este programa");
        System.out.println("=".repeat(60));
        
        // Cerrar servicio
        servicio.cerrar();
    }
    
    /**
     * Obtener la ruta del modelo de Vosk
     */
    private static String obtenerRutaModelo() {
        // Posibles ubicaciones del modelo
        String[] posiblesRutas = {
            "chat-transcripcion/src/main/resources/models/vosk-model-small-es-0.42",
            "src/main/resources/models/vosk-model-small-es-0.42",
            "resources/models/vosk-model-small-es-0.42",
            "models/vosk-model-small-es-0.42"
        };
        
        for (String ruta : posiblesRutas) {
            File dir = new File(ruta);
            if (dir.exists() && dir.isDirectory()) {
                return ruta;
            }
        }
        
        return null;
    }
    
    /**
     * Buscar archivos WAV de prueba y transcribirlos
     */
    private static void buscarArchivosWAV() {
        String[] posiblesRutas = {
            "chat-transcripcion/src/main/resources/test",
            "src/main/resources/test",
            "resources/test"
        };
        
        ServicioTranscripcion servicio = ServicioTranscripcion.obtenerInstancia();
        
        for (String rutaDir : posiblesRutas) {
            File dir = new File(rutaDir);
            if (dir.exists() && dir.isDirectory()) {
                File[] archivos = dir.listFiles((d, name) -> 
                    name.toLowerCase().endsWith(".wav"));
                    
                if (archivos != null && archivos.length > 0) {
                    System.out.println("\n📁 Archivos WAV encontrados en: " + rutaDir);
                    
                    for (File archivo : archivos) {
                        System.out.println("\n🎤 Transcribiendo: " + archivo.getName());
                        try {
                            byte[] audioBytes = Files.readAllBytes(archivo.toPath());
                            String transcripcion = servicio.transcribir(audioBytes);
                            
                            if (transcripcion != null) {
                                System.out.println("   📝 Resultado: " + transcripcion);
                            } else {
                                System.out.println("   ❌ Error en transcripción");
                            }
                        } catch (Exception e) {
                            System.err.println("   ❌ Error al leer archivo: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
