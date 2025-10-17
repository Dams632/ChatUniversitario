package com.chat.common.dto;

import java.io.Serializable;

/**
 * DTO para transportar datos de audio recibido
 * Usado en eventos AUDIO_RECIBIDO y AUDIO_GRUPO_RECIBIDO
 */
public class AudioEntranteDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String remitente;
    private byte[] contenidoAudio;
    private String formato;
    private long duracionSegundos;
    private String timestamp;
    private Long canalId;  // Null si es mensaje privado
    
    public AudioEntranteDTO() {
    }
    
    public AudioEntranteDTO(String remitente, byte[] contenidoAudio, String formato, 
                           long duracionSegundos, String timestamp) {
        this.remitente = remitente;
        this.contenidoAudio = contenidoAudio;
        this.formato = formato;
        this.duracionSegundos = duracionSegundos;
        this.timestamp = timestamp;
    }
    
    // Getters y Setters
    public String getRemitente() {
        return remitente;
    }
    
    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }
    
    public byte[] getContenidoAudio() {
        return contenidoAudio;
    }
    
    public void setContenidoAudio(byte[] contenidoAudio) {
        this.contenidoAudio = contenidoAudio;
    }
    
    public String getFormato() {
        return formato;
    }
    
    public void setFormato(String formato) {
        this.formato = formato;
    }
    
    public long getDuracionSegundos() {
        return duracionSegundos;
    }
    
    public void setDuracionSegundos(long duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public Long getCanalId() {
        return canalId;
    }
    
    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }
    
    @Override
    public String toString() {
        return "AudioEntranteDTO{" +
                "remitente='" + remitente + '\'' +
                ", formato='" + formato + '\'' +
                ", duracionSegundos=" + duracionSegundos +
                ", timestamp='" + timestamp + '\'' +
                ", canalId=" + canalId +
                '}';
    }
}
