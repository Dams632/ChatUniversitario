package com.chat.common.models;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo de Canal de comunicación (similar a Grupo pero con solicitudes)
 */
public class Canal implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String nombre;
    private String descripcion;
    private byte[] foto;
    private Long creadorId;
    private boolean esPrivado;
    private LocalDateTime fechaCreacion;
    private List<Long> miembrosIds;
    private boolean activo;
    
    public Canal() {
        this.fechaCreacion = LocalDateTime.now();
        this.miembrosIds = new ArrayList<>();
        this.activo = true;
        this.esPrivado = false;
    }
    
    public Canal(String nombre, String descripcion, Long creadorId, boolean esPrivado) {
        this();
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.creadorId = creadorId;
        this.esPrivado = esPrivado;
        this.miembrosIds.add(creadorId);
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getDescripcion() {
        return descripcion;
    }
    
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
    
    public byte[] getFoto() {
        return foto;
    }
    
    public void setFoto(byte[] foto) {
        this.foto = foto;
    }
    
    public Long getCreadorId() {
        return creadorId;
    }
    
    public void setCreadorId(Long creadorId) {
        this.creadorId = creadorId;
    }
    
    public boolean isEsPrivado() {
        return esPrivado;
    }
    
    public void setEsPrivado(boolean esPrivado) {
        this.esPrivado = esPrivado;
    }
    
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
    
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
    public List<Long> getMiembrosIds() {
        return miembrosIds;
    }
    
    public void setMiembrosIds(List<Long> miembrosIds) {
        this.miembrosIds = miembrosIds;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public void agregarMiembro(Long usuarioId) {
        if (!miembrosIds.contains(usuarioId)) {
            miembrosIds.add(usuarioId);
        }
    }
    
    public void removerMiembro(Long usuarioId) {
        miembrosIds.remove(usuarioId);
    }
    
    @Override
    public String toString() {
        return "Canal{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", esPrivado=" + esPrivado +
                ", miembros=" + miembrosIds.size() +
                '}';
    }
}
