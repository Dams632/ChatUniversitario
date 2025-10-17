package com.chat.servidor.negocio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.chat.common.models.Usuario;
import com.chat.servidor.datos.UsuarioDAO;

/**
 * Servicio para gestión de usuarios
 */
public class ServicioUsuario {
    
    private final UsuarioDAO usuarioDAO;
    
    public ServicioUsuario(Connection conexion) {
        this.usuarioDAO = new UsuarioDAO(conexion);
    }
    
    /**
     * Obtener usuario por ID
     */
    public Optional<Usuario> obtenerUsuario(Long id) throws SQLException {
        return usuarioDAO.buscarPorId(id);
    }
    
    /**
     * Obtener todos los usuarios en línea
     */
    public List<Usuario> obtenerUsuariosEnLinea() throws SQLException {
        return usuarioDAO.obtenerUsuariosEnLinea();
    }
    
    /**
     * Obtener todos los usuarios (conectados y desconectados)
     */
    public List<Usuario> obtenerTodosLosUsuarios() throws SQLException {
        return usuarioDAO.obtenerTodosLosUsuarios();
    }
    
    /**
     * Buscar usuario por username
     */
    public Optional<Usuario> buscarPorUsername(String username) throws SQLException {
        return usuarioDAO.buscarPorUsername(username);
    }
    
    /**
     * Actualizar estado en línea del usuario
     */
    public void actualizarEstadoEnLinea(Long usuarioId, boolean enLinea) throws SQLException {
        usuarioDAO.actualizarEstadoEnLinea(usuarioId, enLinea);
    }
}
