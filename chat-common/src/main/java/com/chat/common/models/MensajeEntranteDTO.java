package com.chat.common.models;

import java.io.Serializable;

/**
 * DTO para datos de mensajes entrantes
 */
public class MensajeEntranteDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String remitente;
    private String contenido;
    private long timestamp;
    
    public MensajeEntranteDTO() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public MensajeEntranteDTO(String remitente, String contenido) {
        this();
        this.remitente = remitente;
        this.contenido = contenido;
    }
    
    public String getRemitente() {
        return remitente;
    }
    
    public void setRemitente(String remitente) {
        this.remitente = remitente;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "MensajeEntranteDTO{" +
                "remitente='" + remitente + '\'' +
                ", contenido='" + contenido + '\'' +
                '}';
    }
}
