package com.chat.servidor.datos;

import com.chat.common.models.Mensaje;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar mensajes en MySQL
 */
public class MensajeDAO {
    
    private final Connection conexion;
    
    public MensajeDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Guardar mensaje
     */
    public Mensaje guardar(Mensaje mensaje) throws SQLException {
        String sql = "INSERT INTO mensajes (remitente_id, remitente_username, destinatario_id, grupo_id, contenido, fecha_envio, leido, tipo_mensaje) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, mensaje.getRemitenteId());
            stmt.setString(2, mensaje.getRemitenteUsername());
            
            if (mensaje.getDestinatarioId() != null) {
                stmt.setLong(3, mensaje.getDestinatarioId());
            } else {
                stmt.setNull(3, Types.BIGINT);
            }
            
            if (mensaje.getGrupoId() != null) {
                stmt.setLong(4, mensaje.getGrupoId());
            } else {
                stmt.setNull(4, Types.BIGINT);
            }
            
            stmt.setString(5, mensaje.getContenido());
            stmt.setTimestamp(6, Timestamp.valueOf(mensaje.getFechaEnvio()));
            stmt.setBoolean(7, mensaje.isLeido());
            stmt.setString(8, mensaje.getTipo().name());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        mensaje.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        
        return mensaje;
    }
    
    /**
     * Obtener mensajes entre dos usuarios
     */
    public List<Mensaje> obtenerMensajesPrivados(Long usuario1Id, Long usuario2Id, int limite) throws SQLException {
        String sql = "SELECT * FROM mensajes WHERE " +
                     "((remitente_id = ? AND destinatario_id = ?) OR (remitente_id = ? AND destinatario_id = ?)) " +
                     "AND tipo_mensaje = 'PRIVADO' " +
                     "ORDER BY fecha_envio DESC LIMIT ?";
        List<Mensaje> mensajes = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, usuario1Id);
            stmt.setLong(2, usuario2Id);
            stmt.setLong(3, usuario2Id);
            stmt.setLong(4, usuario1Id);
            stmt.setInt(5, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mensajes.add(mapearMensaje(rs));
                }
            }
        }
        
        return mensajes;
    }
    
    /**
     * Obtener mensajes de un grupo/canal
     */
    public List<Mensaje> obtenerMensajesGrupo(Long grupoId, int limite) throws SQLException {
        String sql = "SELECT * FROM mensajes WHERE grupo_id = ? AND tipo_mensaje = 'GRUPO' ORDER BY fecha_envio DESC LIMIT ?";
        List<Mensaje> mensajes = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, grupoId);
            stmt.setInt(2, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mensajes.add(mapearMensaje(rs));
                }
            }
        }
        
        return mensajes;
    }
    
    /**
     * Obtener todos los logs de mensajes
     */
    public List<Mensaje> obtenerTodosLosLogs(int limite) throws SQLException {
        String sql = "SELECT * FROM mensajes ORDER BY fecha_envio DESC LIMIT ?";
        List<Mensaje> mensajes = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    mensajes.add(mapearMensaje(rs));
                }
            }
        }
        
        return mensajes;
    }
    
    /**
     * Contar total de mensajes
     */
    public int contarMensajes() throws SQLException {
        String sql = "SELECT COUNT(*) FROM mensajes";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Mapear ResultSet a objeto Mensaje
     */
    private Mensaje mapearMensaje(ResultSet rs) throws SQLException {
        Mensaje mensaje = new Mensaje();
        mensaje.setId(rs.getLong("id"));
        mensaje.setRemitenteId(rs.getLong("remitente_id"));
        mensaje.setRemitenteUsername(rs.getString("remitente_username"));
        
        long destinatarioId = rs.getLong("destinatario_id");
        if (!rs.wasNull()) {
            mensaje.setDestinatarioId(destinatarioId);
        }
        
        long grupoId = rs.getLong("grupo_id");
        if (!rs.wasNull()) {
            mensaje.setGrupoId(grupoId);
        }
        
        mensaje.setContenido(rs.getString("contenido"));
        mensaje.setLeido(rs.getBoolean("leido"));
        mensaje.setTipo(Mensaje.TipoMensaje.valueOf(rs.getString("tipo_mensaje")));
        
        Timestamp fechaEnvio = rs.getTimestamp("fecha_envio");
        if (fechaEnvio != null) {
            mensaje.setFechaEnvio(fechaEnvio.toLocalDateTime());
        }
        
        return mensaje;
    }
}
