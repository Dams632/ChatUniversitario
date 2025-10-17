package com.chat.common.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modelo para archivos de audio compartidos
 */
public class ArchivoAudio implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long mensajeId;
    private String nombreArchivo;
    private byte[] contenido;
    private long duracionSegundos;
    private String formato; // mp3, wav, ogg, etc.
    private long tamanoBytes;
    private String textoTranscrito;
    private LocalDateTime fechaCreacion;
    
    public ArchivoAudio() {
        this.fechaCreacion = LocalDateTime.now();
    }
    
    public ArchivoAudio(String nombreArchivo, byte[] contenido, String formato) {
        this();
        this.nombreArchivo = nombreArchivo;
        this.contenido = contenido;
        this.formato = formato;
        this.tamanoBytes = contenido != null ? contenido.length : 0;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getMensajeId() {
        return mensajeId;
    }
    
    public void setMensajeId(Long mensajeId) {
        this.mensajeId = mensajeId;
    }
    
    public String getNombreArchivo() {
        return nombreArchivo;
    }
    
    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }
    
    public byte[] getContenido() {
        return contenido;
    }
    
    public void setContenido(byte[] contenido) {
        this.contenido = contenido;
        this.tamanoBytes = contenido != null ? contenido.length : 0;
    }
    
    public long getDuracionSegundos() {
        return duracionSegundos;
    }
    
    public void setDuracionSegundos(long duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }
    
    public String getFormato() {
        return formato;
    }
    
    public void setFormato(String formato) {
        this.formato = formato;
    }
    
    public long getTamanoBytes() {
        return tamanoBytes;
    }
    
    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }
    
    public String getTextoTranscrito() {
        return textoTranscrito;
    }
    
    public void setTextoTranscrito(String textoTranscrito) {
        this.textoTranscrito = textoTranscrito;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public String getTamanoFormateado() {
        if (tamanoBytes < 1024) {
            return tamanoBytes + " B";
        } else if (tamanoBytes < 1024 * 1024) {
            return String.format("%.2f KB", tamanoBytes / 1024.0);
        } else {
            return String.format("%.2f MB", tamanoBytes / (1024.0 * 1024));
        }
    }
    
    @Override
    public String toString() {
        return "ArchivoAudio{" +
                "id=" + id +
                ", nombreArchivo='" + nombreArchivo + '\'' +
                ", formato='" + formato + '\'' +
                ", tamano=" + getTamanoFormateado() +
                ", duracion=" + duracionSegundos + "s" +
                '}';
    }
}
