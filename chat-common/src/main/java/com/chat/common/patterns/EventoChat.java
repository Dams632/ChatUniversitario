package com.chat.common.patterns;

/**
 * Evento del chat con información del tipo y datos asociados
 */
public class EventoChat {
    private final TipoEvento tipo;
    private final Object datos;
    private final long timestamp;
    
    public enum TipoEvento {
        USUARIO_CONECTADO,
        USUARIO_DESCONECTADO,
        MENSAJE_RECIBIDO,
        MENSAJE_ENVIADO,
        MENSAJE_GRUPO_RECIBIDO,
        AUDIO_RECIBIDO,                  // Cuando se recibe un audio privado
        AUDIO_GRUPO_RECIBIDO,            // Cuando se recibe un audio grupal
        CANAL_CREADO,
        SOLICITUD_CANAL,
        USUARIO_UNIDO_CANAL,
        USUARIO_SALIO_CANAL,
        BROADCAST_MENSAJE,
        USUARIOS_ACTUALIZADOS,
        INVITACION_RECIBIDA,
        NOTIFICACION_SERVIDOR,           // Notificación broadcast del servidor a usuarios
        NOTIFICACION_SERVIDOR_GRUPO,     // Notificación broadcast del servidor a grupos
        DESCONEXION_FORZADA              // Cuando el servidor desconecta al cliente
    }
    
    public EventoChat(TipoEvento tipo, Object datos) {
        this.tipo = tipo;
        this.datos = datos;
        this.timestamp = System.currentTimeMillis();
    }
    
    public TipoEvento getTipo() {
        return tipo;
    }
    
    public Object getDatos() {
        return datos;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    @Override
    public String toString() {
        return "EventoChat{" +
                "tipo=" + tipo +
                ", timestamp=" + timestamp +
                '}';
    }
}
