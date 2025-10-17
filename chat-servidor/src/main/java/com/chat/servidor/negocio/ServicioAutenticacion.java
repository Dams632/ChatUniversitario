package com.chat.servidor.negocio;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.chat.common.models.Usuario;
import com.chat.servidor.datos.UsuarioDAO;

/**
 * Servicio de autenticación y gestión de sesiones
 */
public class ServicioAutenticacion {
    
    private final UsuarioDAO usuarioDAO;
    private final ConcurrentHashMap<String, Long> sesionesActivas;
    
    public ServicioAutenticacion(Connection conexion) {
        this.usuarioDAO = new UsuarioDAO(conexion);
        this.sesionesActivas = new ConcurrentHashMap<>();
    }
    
    /**
     * Registrar un nuevo usuario
     */
    public Usuario registrar(String username, String email, String password, String direccionIP) throws SQLException {
        return registrar(username, email, password, direccionIP, null);
    }
    
    /**
     * Registrar un nuevo usuario con foto
     */
    public Usuario registrar(String username, String email, String password, String direccionIP, byte[] foto) throws SQLException {
        // Verificar si el usuario ya existe
        Optional<Usuario> usuarioExistente = usuarioDAO.buscarPorUsername(username);
        if (usuarioExistente.isPresent()) {
            throw new IllegalArgumentException("El usuario ya existe");
        }
        
        // Hashear la contraseña
        String passwordHash = hashearPassword(password);
        
        // Crear el usuario con dirección IP y foto
        Usuario nuevoUsuario = new Usuario(username, email, passwordHash);
        nuevoUsuario.setDireccionIP(direccionIP);
        nuevoUsuario.setFoto(foto);
        
        return usuarioDAO.crear(nuevoUsuario);
    }
    
    /**
     * Autenticar usuario y crear sesión
     */
    public String login(String username, String password) throws SQLException {
        // Buscar usuario
        Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorUsername(username);
        
        if (!usuarioOpt.isPresent()) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Verificar contraseña
        String passwordHash = hashearPassword(password);
        if (!passwordHash.equals(usuario.getPassword())) {
            throw new IllegalArgumentException("Usuario o contraseña incorrectos");
        }
        
        // Actualizar estado en línea
        usuarioDAO.actualizarEstadoEnLinea(usuario.getId(), true);
        
        // Crear token de sesión
        String token = generarToken();
        sesionesActivas.put(token, usuario.getId());
        
        return token;
    }
    
    /**
     * Cerrar sesión
     */
    public void logout(String token) throws SQLException {
        Long usuarioId = sesionesActivas.remove(token);
        if (usuarioId != null) {
            usuarioDAO.actualizarEstadoEnLinea(usuarioId, false);
        }
    }
    
    /**
     * Validar token de sesión
     */
    public boolean validarSesion(String token) {
        return sesionesActivas.containsKey(token);
    }
    
    /**
     * Obtener ID de usuario por token
     */
    public Long obtenerUsuarioIdPorToken(String token) {
        return sesionesActivas.get(token);
    }
    
    /**
     * Hashear contraseña con SHA-256
     */
    private String hashearPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al hashear contraseña", e);
        }
    }
    
    /**
     * Generar token único
     */
    private String generarToken() {
        return UUID.randomUUID().toString();
    }
}
