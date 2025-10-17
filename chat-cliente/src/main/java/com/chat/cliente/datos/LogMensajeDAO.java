package com.chat.cliente.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.chat.common.models.Mensaje;

/**
 * DAO para logs de mensajes en H2
 */
public class LogMensajeDAO {
    
    private final Connection conexion;
    
    public LogMensajeDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Guardar mensaje en logs con remitente y destinatario
     */
    public void guardarLog(String remitente, String destinatario, String contenido, Timestamp fechaEnvio) throws SQLException {
        String sql = "INSERT INTO logs_mensajes (remitente_username, destinatario_username, " +
                     "grupo_nombre, contenido, fecha_envio, tipo_mensaje) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, remitente);
            stmt.setString(2, destinatario);
            stmt.setString(3, null);
            stmt.setString(4, contenido);
            stmt.setTimestamp(5, fechaEnvio);
            stmt.setString(6, "TEXTO");
            stmt.executeUpdate();
        }
    }
    
    /**
     * Guardar mensaje en logs
     */
    public void guardarLog(Mensaje mensaje) throws SQLException {
        String sql = "INSERT INTO logs_mensajes (remitente_username, destinatario_username, " +
                     "grupo_nombre, contenido, fecha_envio, tipo_mensaje) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, mensaje.getRemitenteUsername());
            stmt.setString(2, null); // Se puede extender para guardar destinatario
            stmt.setString(3, null); // Se puede extender para guardar grupo
            stmt.setString(4, mensaje.getContenido());
            stmt.setTimestamp(5, Timestamp.valueOf(mensaje.getFechaEnvio()));
            stmt.setString(6, mensaje.getTipo().toString());
            stmt.executeUpdate();
        }
    }
    
    /**
     * Obtener mensajes de una conversación específica entre dos usuarios
     */
    public List<MensajeConversacion> obtenerMensajesConversacion(String usuario1, String usuario2) throws SQLException {
        String sql = "SELECT remitente_username, destinatario_username, contenido, fecha_envio " +
                     "FROM logs_mensajes " +
                     "WHERE (remitente_username = ? AND destinatario_username = ?) " +
                     "   OR (remitente_username = ? AND destinatario_username = ?) " +
                     "ORDER BY fecha_envio ASC";
        
        List<MensajeConversacion> mensajes = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, usuario1);
            stmt.setString(2, usuario2);
            stmt.setString(3, usuario2);
            stmt.setString(4, usuario1);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String remitente = rs.getString("remitente_username");
                    String contenido = rs.getString("contenido");
                    Timestamp fechaEnvio = rs.getTimestamp("fecha_envio");
                    
                    mensajes.add(new MensajeConversacion(remitente, contenido, fechaEnvio));
                }
            }
        }
        
        return mensajes;
    }
    
    /**
     * Clase interna para representar un mensaje de conversación
     */
    public static class MensajeConversacion {
        private final String remitente;
        private final String contenido;
        private final Timestamp fechaEnvio;
        
        public MensajeConversacion(String remitente, String contenido, Timestamp fechaEnvio) {
            this.remitente = remitente;
            this.contenido = contenido;
            this.fechaEnvio = fechaEnvio;
        }
        
        public String getRemitente() {
            return remitente;
        }
        
        public String getContenido() {
            return contenido;
        }
        
        public Timestamp getFechaEnvio() {
            return fechaEnvio;
        }
    }
    
    /**
     * Guardar mensaje grupal en logs
     */
    public void guardarLogGrupo(String remitente, String grupoId, String contenido, Timestamp fechaEnvio) throws SQLException {
        String sql = "INSERT INTO logs_mensajes (remitente_username, destinatario_username, " +
                     "grupo_nombre, contenido, fecha_envio, tipo_mensaje) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, remitente);
            stmt.setString(2, null); // No hay destinatario específico en mensajes grupales
            stmt.setString(3, grupoId); // Guardamos el identificador del grupo (ej: "GRUPO_123")
            stmt.setString(4, contenido);
            stmt.setTimestamp(5, fechaEnvio);
            stmt.setString(6, "TEXTO");
            stmt.executeUpdate();
        }
    }
    
    /**
     * Obtener mensajes de un grupo específico
     */
    public List<MensajeConversacion> obtenerMensajesGrupo(String grupoId) throws SQLException {
        String sql = "SELECT remitente_username, contenido, fecha_envio " +
                     "FROM logs_mensajes " +
                     "WHERE grupo_nombre = ? " +
                     "ORDER BY fecha_envio ASC";
        
        List<MensajeConversacion> mensajes = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, grupoId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String remitente = rs.getString("remitente_username");
                    String contenido = rs.getString("contenido");
                    Timestamp fechaEnvio = rs.getTimestamp("fecha_envio");
                    
                    mensajes.add(new MensajeConversacion(remitente, contenido, fechaEnvio));
                }
            }
        }
        
        return mensajes;
    }
    
    /**
     * Obtener todos los logs
     */
    public List<Mensaje> obtenerTodosLosLogs() throws SQLException {
        String sql = "SELECT * FROM logs_mensajes ORDER BY fecha_envio DESC";
        List<Mensaje> logs = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Mensaje mensaje = new Mensaje();
                mensaje.setId(rs.getLong("id"));
                mensaje.setRemitenteUsername(rs.getString("remitente_username"));
                mensaje.setContenido(rs.getString("contenido"));
                mensaje.setFechaEnvio(rs.getTimestamp("fecha_envio").toLocalDateTime());
                mensaje.setTipo(Mensaje.TipoMensaje.valueOf(rs.getString("tipo_mensaje")));
                logs.add(mensaje);
            }
        }
        
        return logs;
    }
    
    /**
     * Limpiar logs antiguos
     */
    public void limpiarLogsAntiguos(int dias) throws SQLException {
        String sql = "DELETE FROM logs_mensajes WHERE fecha_envio < DATEADD('DAY', ?, CURRENT_TIMESTAMP)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, -dias);
            int eliminados = stmt.executeUpdate();
            System.out.println("Logs eliminados: " + eliminados);
        }
    }
}
