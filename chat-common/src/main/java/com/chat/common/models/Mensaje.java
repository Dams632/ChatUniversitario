package com.chat.common.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modelo de Mensaje compartido entre cliente y servidor
 */
public class Mensaje implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long remitenteId;
    private String remitenteUsername;
    private Long destinatarioId;
    private Long grupoId;
    private String contenido;
    private LocalDateTime fechaEnvio;
    private boolean leido;
    private TipoMensaje tipo;
    
    public enum TipoMensaje {
        PRIVADO,
        GRUPO,
        SISTEMA
    }
    
    public Mensaje() {
        this.fechaEnvio = LocalDateTime.now();
        this.leido = false;
    }
    
    public Mensaje(Long remitenteId, String remitenteUsername, String contenido, TipoMensaje tipo) {
        this();
        this.remitenteId = remitenteId;
        this.remitenteUsername = remitenteUsername;
        this.contenido = contenido;
        this.tipo = tipo;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getRemitenteId() {
        return remitenteId;
    }
    
    public void setRemitenteId(Long remitenteId) {
        this.remitenteId = remitenteId;
    }
    
    public String getRemitenteUsername() {
        return remitenteUsername;
    }
    
    public void setRemitenteUsername(String remitenteUsername) {
        this.remitenteUsername = remitenteUsername;
    }
    
    public Long getDestinatarioId() {
        return destinatarioId;
    }
    
    public void setDestinatarioId(Long destinatarioId) {
        this.destinatarioId = destinatarioId;
    }
    
    public Long getGrupoId() {
        return grupoId;
    }
    
    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }
    
    public String getContenido() {
        return contenido;
    }
    
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }
    
    public LocalDateTime getFechaEnvio() {
        return fechaEnvio;
    }
    
    public void setFechaEnvio(LocalDateTime fechaEnvio) {
        this.fechaEnvio = fechaEnvio;
    }
    
    public boolean isLeido() {
        return leido;
    }
    
    public void setLeido(boolean leido) {
        this.leido = leido;
    }
    
    public TipoMensaje getTipo() {
        return tipo;
    }
    
    public void setTipo(TipoMensaje tipo) {
        this.tipo = tipo;
    }
    
    @Override
    public String toString() {
        return "Mensaje{" +
                "id=" + id +
                ", remitenteUsername='" + remitenteUsername + '\'' +
                ", contenido='" + contenido + '\'' +
                ", tipo=" + tipo +
                ", fechaEnvio=" + fechaEnvio +
                '}';
    }
}
