package com.chat.common.network;

import java.io.Serializable;

/**
 * Protocolo de comunicación entre cliente y servidor
 */
public class ProtocoloMensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private TipoProtocolo tipo;
    private Object payload;
    private long timestamp;
    
    public enum TipoProtocolo {
        REQUEST,
        RESPONSE,
        NOTIFICACION,
        HEARTBEAT
    }
    
    public ProtocoloMensaje() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public ProtocoloMensaje(TipoProtocolo tipo, Object payload) {
        this();
        this.tipo = tipo;
        this.payload = payload;
    }
    
    // Getters y Setters
    public TipoProtocolo getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoProtocolo tipo) {
        this.tipo = tipo;
    }
    
    public Object getPayload() {
        return payload;
    }
    
    public void setPayload(Object payload) {
        this.payload = payload;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    
    @Override
    public String toString() {
        return "ProtocoloMensaje{" +
                "tipo=" + tipo +
                ", timestamp=" + timestamp +
                '}';
    }
}
