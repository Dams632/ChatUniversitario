package com.chat.common.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO para peticiones del cliente al servidor
 */
public class RequestDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private TipoRequest tipo;
    private Map<String, Object> datos;
    private Long usuarioId;
    private String sessionToken;
    
    public enum TipoRequest {
        LOGIN,
        REGISTRO,
        LOGOUT,
        ENVIAR_MENSAJE,
        ENVIAR_MENSAJE_GRUPO,
        ENVIAR_MENSAJE_AUDIO,
        BROADCAST_MENSAJE,
        CREAR_GRUPO,
        CREAR_CANAL,
        CREAR_GRUPO_CON_INVITACIONES,
        UNIRSE_GRUPO,
        SOLICITAR_UNIRSE_CANAL,
        ACEPTAR_SOLICITUD_CANAL,
        RECHAZAR_SOLICITUD_CANAL,
        ACEPTAR_INVITACION,
        RECHAZAR_INVITACION,
        OBTENER_USUARIOS_ONLINE,
        OBTENER_TODOS_USUARIOS,
        OBTENER_GRUPOS,
        OBTENER_CANALES,
        OBTENER_SOLICITUDES_PENDIENTES,
        OBTENER_INVITACIONES_PENDIENTES,
        OBTENER_MENSAJES,
        OBTENER_LOGS,
        // Informes
        INFORME_USUARIOS_REGISTRADOS,
        INFORME_CANALES_CON_USUARIOS,
        INFORME_USUARIOS_CONECTADOS,
        INFORME_MENSAJES_AUDIO,
        INFORME_LOGS
    }
    
    public RequestDTO() {
        this.datos = new HashMap<>();
    }
    
    public RequestDTO(TipoRequest tipo) {
        this();
        this.tipo = tipo;
    }
    
    // Getters y Setters
    public TipoRequest getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoRequest tipo) {
        this.tipo = tipo;
    }
    
    public Map<String, Object> getDatos() {
        return datos;
    }
    
    public void setDatos(Map<String, Object> datos) {
        this.datos = datos;
    }
    
    public void addDato(String clave, Object valor) {
        this.datos.put(clave, valor);
    }
    
    public Object getDato(String clave) {
        return this.datos.get(clave);
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    @Override
    public String toString() {
        return "RequestDTO{" +
                "tipo=" + tipo +
                ", usuarioId=" + usuarioId +
                '}';
    }
}
