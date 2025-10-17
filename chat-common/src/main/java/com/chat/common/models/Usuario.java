package com.chat.common.models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Modelo de Usuario compartido entre cliente y servidor
 */
public class Usuario implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String username;
    private String email;
    private String password;
    private byte[] foto;
    private String direccionIP;
    private boolean enLinea;
    private LocalDateTime fechaRegistro;
    private LocalDateTime ultimaConexion;
    
    public Usuario() {
        this.enLinea = false;
        this.fechaRegistro = LocalDateTime.now();
    }
    
    public Usuario(String username, String email, String password) {
        this();
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
    // Getters y Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public byte[] getFoto() {
        return foto;
    }
    
    public void setFoto(byte[] foto) {
        this.foto = foto;
    }
    
    public String getDireccionIP() {
        return direccionIP;
    }
    
    public void setDireccionIP(String direccionIP) {
        this.direccionIP = direccionIP;
    }
    
    public boolean isEnLinea() {
        return enLinea;
    }
    
    public void setEnLinea(boolean enLinea) {
        this.enLinea = enLinea;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public LocalDateTime getUltimaConexion() {
        return ultimaConexion;
    }
    
    public void setUltimaConexion(LocalDateTime ultimaConexion) {
        this.ultimaConexion = ultimaConexion;
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", enLinea=" + enLinea +
                '}';
    }
}
