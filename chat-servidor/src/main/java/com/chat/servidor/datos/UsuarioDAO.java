package com.chat.servidor.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.chat.common.models.Usuario;

/**
 * DAO para gestionar usuarios en MySQL
 */
public class UsuarioDAO {
    
    private Connection conexion;
    
    public UsuarioDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Crear un nuevo usuario
     */
    public Usuario crear(Usuario usuario) throws SQLException {
        String sql = "INSERT INTO usuarios (username, email, password, foto, en_linea, fecha_registro) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, usuario.getUsername());
            stmt.setString(2, usuario.getEmail());
            stmt.setString(3, usuario.getPassword());
            stmt.setBytes(4, usuario.getFoto()); // Agregar foto
            stmt.setBoolean(5, usuario.isEnLinea());
            stmt.setTimestamp(6, Timestamp.valueOf(usuario.getFechaRegistro()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        usuario.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        
        return usuario;
    }
    
    /**
     * Buscar usuario por username
     */
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE username = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, username);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Buscar usuario por ID
     */
    public Optional<Usuario> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearUsuario(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtener todos los usuarios en línea
     */
    public List<Usuario> obtenerUsuariosEnLinea() throws SQLException {
        String sql = "SELECT * FROM usuarios WHERE en_linea = true";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        
        return usuarios;
    }
    
    /**
     * Obtener todos los usuarios (conectados y desconectados)
     */
    public List<Usuario> obtenerTodosLosUsuarios() throws SQLException {
        String sql = "SELECT * FROM usuarios ORDER BY en_linea DESC, username ASC";
        List<Usuario> usuarios = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapearUsuario(rs));
            }
        }
        
        return usuarios;
    }
    
    /**
     * Actualizar estado en línea del usuario
     */
    public void actualizarEstadoEnLinea(Long usuarioId, boolean enLinea) throws SQLException {
        String sql = "UPDATE usuarios SET en_linea = ?, ultima_conexion = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setBoolean(1, enLinea);
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, usuarioId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Mapear ResultSet a objeto Usuario
     */
    private Usuario mapearUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setId(rs.getLong("id"));
        usuario.setUsername(rs.getString("username"));
        usuario.setEmail(rs.getString("email"));
        usuario.setPassword(rs.getString("password"));
        usuario.setFoto(rs.getBytes("foto")); // Leer foto
        usuario.setEnLinea(rs.getBoolean("en_linea"));
        
        Timestamp fechaRegistro = rs.getTimestamp("fecha_registro");
        if (fechaRegistro != null) {
            usuario.setFechaRegistro(fechaRegistro.toLocalDateTime());
        }
        
        Timestamp ultimaConexion = rs.getTimestamp("ultima_conexion");
        if (ultimaConexion != null) {
            usuario.setUltimaConexion(ultimaConexion.toLocalDateTime());
        }
        
        return usuario;
    }
}
