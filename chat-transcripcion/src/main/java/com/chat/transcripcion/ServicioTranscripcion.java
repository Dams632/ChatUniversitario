package com.chat.transcripcion;

import org.vosk.LibVosk;
import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Servicio de transcripción de audio usando Vosk
 * Convierte archivos de audio WAV a texto en español
 */
public class ServicioTranscripcion {
    
    private static ServicioTranscripcion instancia;
    private Model modelo;
    private boolean inicializado = false;
    private String rutaModelo;
    
    // Configuración de audio
    private static final float SAMPLE_RATE = 16000.0f; // Vosk requiere 16kHz
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1; // Mono
    
    private ServicioTranscripcion() {
        // Constructor privado para singleton
    }
    
    /**
     * Obtener instancia singleton
     */
    public static synchronized ServicioTranscripcion obtenerInstancia() {
        if (instancia == null) {
            instancia = new ServicioTranscripcion();
        }
        return instancia;
    }
    
    /**
     * Inicializar el modelo de Vosk
     * @param rutaModelo Ruta al directorio del modelo (ej: "models/vosk-model-small-es-0.42")
     * @return true si se inicializó correctamente
     */
    public boolean inicializar(String rutaModelo) {
        if (inicializado) {
            System.out.println("⚠️ ServicioTranscripcion ya está inicializado");
            return true;
        }
        
        try {
            // Configurar nivel de log de Vosk
            LibVosk.setLogLevel(LogLevel.WARNINGS);
            
            // Validar que existe el directorio del modelo
            File dirModelo = new File(rutaModelo);
            if (!dirModelo.exists() || !dirModelo.isDirectory()) {
                System.err.println("❌ Error: No se encuentra el modelo en: " + rutaModelo);
                System.err.println("   Descarga el modelo desde: https://alphacephei.com/vosk/models");
                System.err.println("   Modelo recomendado: vosk-model-small-es-0.42.zip (~40MB)");
                return false;
            }
            
            System.out.println("🔄 Cargando modelo de Vosk desde: " + rutaModelo);
            modelo = new Model(rutaModelo);
            this.rutaModelo = rutaModelo;
            inicializado = true;
            System.out.println("✅ Modelo de transcripción cargado exitosamente");
            return true;
            
        } catch (IOException e) {
            System.err.println("❌ Error al cargar modelo de Vosk: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (UnsatisfiedLinkError e) {
            System.err.println("❌ Error: No se encontraron las librerías nativas de Vosk");
            System.err.println("   Asegúrate de tener la versión correcta de Vosk para tu sistema operativo");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Transcribir audio desde bytes (formato WAV)
     * @param audioBytes Array de bytes con el audio en formato WAV
     * @return Texto transcrito o null si hay error
     */
    public String transcribir(byte[] audioBytes) {
        if (!inicializado) {
            System.err.println("❌ Error: ServicioTranscripcion no está inicializado. Llama a inicializar() primero.");
            return null;
        }
        
        if (audioBytes == null || audioBytes.length == 0) {
            System.err.println("❌ Error: Audio vacío o nulo");
            return null;
        }
        
        try {
            // Intentar como WAV primero
            AudioInputStream audioStream = null;
            
            try {
                ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
                audioStream = AudioSystem.getAudioInputStream(bais);
            } catch (UnsupportedAudioFileException e) {
                // No es WAV, asumir PCM raw con formato estándar (16kHz, 16-bit, mono)
                System.out.println("ℹ️ Audio sin encabezado WAV, usando formato PCM raw (16kHz, 16-bit, mono)");
                AudioFormat rawFormat = new AudioFormat(
                    SAMPLE_RATE,  // 16kHz
                    SAMPLE_SIZE_BITS,  // 16 bits
                    CHANNELS,  // mono
                    true,  // signed
                    false  // little endian
                );
                ByteArrayInputStream bais = new ByteArrayInputStream(audioBytes);
                audioStream = new AudioInputStream(bais, rawFormat, audioBytes.length / rawFormat.getFrameSize());
            }
            
            try (AudioInputStream audio = audioStream) {
                // Obtener formato del audio
                AudioFormat format = audio.getFormat();
                
                // Verificar si necesita conversión a 16kHz mono
                AudioInputStream audioConvertido = audio;
                if (format.getSampleRate() != SAMPLE_RATE || format.getChannels() != CHANNELS) {
                    System.out.println("🔄 Convirtiendo audio a formato compatible (16kHz, mono)...");
                    AudioFormat targetFormat = new AudioFormat(
                        SAMPLE_RATE, 
                        SAMPLE_SIZE_BITS, 
                        CHANNELS, 
                        true, 
                        false
                    );
                    audioConvertido = AudioSystem.getAudioInputStream(targetFormat, audio);
                }
                
                // Crear recognizer
                try (Recognizer recognizer = new Recognizer(modelo, SAMPLE_RATE)) {
                    
                    // Leer audio en chunks
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    StringBuilder transcripcionCompleta = new StringBuilder();
                    
                    while ((bytesRead = audioConvertido.read(buffer)) != -1) {
                        if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                            String result = recognizer.getResult();
                            String texto = extraerTextoDeJson(result);
                            if (!texto.isEmpty()) {
                                transcripcionCompleta.append(texto).append(" ");
                            }
                        }
                    }
                    
                    // Obtener resultado final
                    String resultadoFinal = recognizer.getFinalResult();
                    String textoFinal = extraerTextoDeJson(resultadoFinal);
                    if (!textoFinal.isEmpty()) {
                        transcripcionCompleta.append(textoFinal);
                    }
                    
                    String transcripcion = transcripcionCompleta.toString().trim();
                    
                    if (transcripcion.isEmpty()) {
                        System.out.println("⚠️ No se detectó voz en el audio");
                        return "[Sin audio detectado]";
                    }
                    
                    System.out.println("✅ Transcripción exitosa: " + transcripcion);
                    return transcripcion;
                    
                }
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error al transcribir audio: " + e.getMessage());
            e.printStackTrace();
            return "[Error en transcripción]";
        }
    }
    
    /**
     * Extraer el texto del JSON retornado por Vosk
     */
    private String extraerTextoDeJson(String json) {
        try {
            JsonObject jsonObject = JsonParser.parseString(json).getAsJsonObject();
            if (jsonObject.has("text")) {
                return jsonObject.get("text").getAsString();
            }
        } catch (Exception e) {
            System.err.println("⚠️ Error al parsear JSON de Vosk: " + e.getMessage());
        }
        return "";
    }
    
    /**
     * Verificar si el servicio está inicializado
     */
    public boolean estaInicializado() {
        return inicializado;
    }
    
    /**
     * Obtener la ruta del modelo actual
     */
    public String getRutaModelo() {
        return rutaModelo;
    }
    
    /**
     * Cerrar y liberar recursos
     */
    public void cerrar() {
        if (modelo != null) {
            try {
                modelo.close();
                System.out.println("✅ Modelo de transcripción cerrado");
            } catch (Exception e) {
                System.err.println("⚠️ Error al cerrar modelo: " + e.getMessage());
            }
        }
        inicializado = false;
    }
}
