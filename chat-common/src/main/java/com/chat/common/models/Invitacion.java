package com.chat.common.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modelo de Invitación a un canal/grupo
 */
public class Invitacion implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long canalId;
    private String nombreCanal;
    private String descripcionCanal;
    private byte[] fotoCanal;
    private Long usuarioInvitadoId;
    private String usernameInvitado;
    private Long usuarioInvitadorId;
    private String usernameInvitador;
    private EstadoInvitacion estado;
    private LocalDateTime fechaInvitacion;
    private LocalDateTime fechaRespuesta;
    
    public enum EstadoInvitacion {
        PENDIENTE,
        ACEPTADA,
        RECHAZADA
    }
    
    public Invitacion() {
        this.fechaInvitacion = LocalDateTime.now();
        this.estado = EstadoInvitacion.PENDIENTE;
    }
    
    public Invitacion(Long canalId, Long usuarioInvitadoId, Long usuarioInvitadorId) {
        this();
        this.canalId = canalId;
        this.usuarioInvitadoId = usuarioInvitadoId;
        this.usuarioInvitadorId = usuarioInvitadorId;
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
    
    public String getNombreCanal() {
        return nombreCanal;
    }
    
    public void setNombreCanal(String nombreCanal) {
        this.nombreCanal = nombreCanal;
    }
    
    public String getDescripcionCanal() {
        return descripcionCanal;
    }
    
    public void setDescripcionCanal(String descripcionCanal) {
        this.descripcionCanal = descripcionCanal;
    }
    
    public byte[] getFotoCanal() {
        return fotoCanal;
    }
    
    public void setFotoCanal(byte[] fotoCanal) {
        this.fotoCanal = fotoCanal;
    }
    
    public Long getUsuarioInvitadoId() {
        return usuarioInvitadoId;
    }
    
    public void setUsuarioInvitadoId(Long usuarioInvitadoId) {
        this.usuarioInvitadoId = usuarioInvitadoId;
    }
    
    public String getUsernameInvitado() {
        return usernameInvitado;
    }
    
    public void setUsernameInvitado(String usernameInvitado) {
        this.usernameInvitado = usernameInvitado;
    }
    
    public Long getUsuarioInvitadorId() {
        return usuarioInvitadorId;
    }
    
    public void setUsuarioInvitadorId(Long usuarioInvitadorId) {
        this.usuarioInvitadorId = usuarioInvitadorId;
    }
    
    public String getUsernameInvitador() {
        return usernameInvitador;
    }
    
    public void setUsernameInvitador(String usernameInvitador) {
        this.usernameInvitador = usernameInvitador;
    }
    
    public EstadoInvitacion getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoInvitacion estado) {
        this.estado = estado;
    }
    
    public LocalDateTime getFechaInvitacion() {
        return fechaInvitacion;
    }
    
    public void setFechaInvitacion(LocalDateTime fechaInvitacion) {
        this.fechaInvitacion = fechaInvitacion;
    }
    
    public LocalDateTime getFechaRespuesta() {
        return fechaRespuesta;
    }
    
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }
    
    @Override
    public String toString() {
        return "Invitacion{" +
                "id=" + id +
                ", canalId=" + canalId +
                ", nombreCanal='" + nombreCanal + '\'' +
                ", usuarioInvitadoId=" + usuarioInvitadoId +
                ", usernameInvitado='" + usernameInvitado + '\'' +
                ", usuarioInvitadorId=" + usuarioInvitadorId +
                ", usernameInvitador='" + usernameInvitador + '\'' +
                ", estado=" + estado +
                ", fechaInvitacion=" + fechaInvitacion +
                '}';
    }
}
