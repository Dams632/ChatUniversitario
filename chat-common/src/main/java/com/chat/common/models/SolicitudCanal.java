package com.chat.common.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modelo para solicitudes de ingreso a canales privados
 */
public class SolicitudCanal implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long canalId;
    private Long usuarioId;
    private String usuarioUsername;
    private String canalNombre;
    private EstadoSolicitud estado;
    private LocalDateTime fechaSolicitud;
    private LocalDateTime fechaRespuesta;
    private String mensajeRespuesta;
    
    public enum EstadoSolicitud {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA
    }
    
    public SolicitudCanal() {
        this.fechaSolicitud = LocalDateTime.now();
        this.estado = EstadoSolicitud.PENDIENTE;
    }
    
    public SolicitudCanal(Long canalId, Long usuarioId, String canalNombre, String usuarioUsername) {
        this();
        this.canalId = canalId;
        this.usuarioId = usuarioId;
        this.canalNombre = canalNombre;
        this.usuarioUsername = usuarioUsername;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getCanalId() {
        return canalId;
    }
    
    public void setCanalId(Long canalId) {
        this.canalId = canalId;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }
    
    public String getUsuarioUsername() {
        return usuarioUsername;
    }
    
    public void setUsuarioUsername(String usuarioUsername) {
        this.usuarioUsername = usuarioUsername;
    }
    
    public String getCanalNombre() {
        return canalNombre;
    }
    
    public void setCanalNombre(String canalNombre) {
        this.canalNombre = canalNombre;
    }
    
    public EstadoSolicitud getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaSolicitud() {
        return fechaSolicitud;
    }
    
    public void setFechaSolicitud(LocalDateTime fechaSolicitud) {
        this.fechaSolicitud = fechaSolicitud;
    }
    
    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }
    
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
    
    public String getMensajeRespuesta() {
        return mensajeRespuesta;
    }
    
    public void setMensajeRespuesta(String mensajeRespuesta) {
        this.mensajeRespuesta = mensajeRespuesta;
    }
    
    @Override
    public String toString() {
        return "SolicitudCanal{" +
                "id=" + id +
                ", usuarioUsername='" + usuarioUsername + '\'' +
                ", canalNombre='" + canalNombre + '\'' +
                ", estado=" + estado +
                '}';
    }
}
