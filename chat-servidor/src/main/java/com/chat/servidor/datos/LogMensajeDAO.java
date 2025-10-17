package com.chat.servidor.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para gestionar logs de mensajes (texto y audio) con transcripciones
 * Sigue el patrón DAO con inyección de dependencias de Connection
 */
public class LogMensajeDAO {
    
    private final Connection conexion;
    
    /**
     * Constructor con inyección de dependencias
     * @param conexion Conexión a la base de datos MySQL
     */
    public LogMensajeDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Guardar log de mensaje de texto privado
     */
    public boolean guardarLogTextoPrivado(String remitenteUsername, Long remitenteId,
                                           String destinatarioUsername, Long destinatarioId,
                                           String contenidoTexto, String direccionIP) {
        String sql = "INSERT INTO logs_mensajes (tipo_mensaje, tipo_conversacion, " +
                    "remitente_username, remitente_id, destinatario_username, destinatario_id, " +
                    "contenido_texto, timestamp, direccion_ip_remitente) " +
                    "VALUES ('TEXTO', 'PRIVADO', ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            
            stmt.setString(1, remitenteUsername);
            stmt.setObject(2, remitenteId);
            stmt.setString(3, destinatarioUsername);
            stmt.setObject(4, destinatarioId);
            stmt.setString(5, contenidoTexto);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(7, direccionIP);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al guardar log de texto privado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Guardar log de mensaje de texto en grupo
     */
    public boolean guardarLogTextoGrupo(String remitenteUsername, Long remitenteId,
                                         String grupoNombre, Long grupoId,
                                         String contenidoTexto, String direccionIP) {
        String sql = "INSERT INTO logs_mensajes (tipo_mensaje, tipo_conversacion, " +
                    "remitente_username, remitente_id, canal_nombre, canal_id, " +
                    "contenido_texto, timestamp, direccion_ip_remitente) " +
                    "VALUES ('TEXTO', 'GRUPO', ?, ?, ?, ?, ?, ?, ?)";
        
        try (
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            
            stmt.setString(1, remitenteUsername);
            stmt.setObject(2, remitenteId);
            stmt.setString(3, grupoNombre);
            stmt.setObject(4, grupoId);
            stmt.setString(5, contenidoTexto);
            stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(7, direccionIP);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al guardar log de texto en grupo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Guardar log de mensaje de audio privado con transcripción
     */
    public boolean guardarLogAudioPrivado(String remitenteUsername, Long remitenteId,
                                           String destinatarioUsername, Long destinatarioId,
                                           byte[] contenidoAudio, String transcripcion,
                                           Long duracionSegundos, String formato,
                                           String direccionIP) {
        String sql = "INSERT INTO logs_mensajes (tipo_mensaje, tipo_conversacion, " +
                    "remitente_username, remitente_id, destinatario_username, destinatario_id, " +
                    "contenido_audio, transcripcion_audio, duracion_segundos, formato_audio, " +
                    "timestamp, direccion_ip_remitente) " +
                    "VALUES ('AUDIO', 'PRIVADO', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            
            stmt.setString(1, remitenteUsername);
            stmt.setObject(2, remitenteId);
            stmt.setString(3, destinatarioUsername);
            stmt.setObject(4, destinatarioId);
            stmt.setBytes(5, contenidoAudio);
            stmt.setString(6, transcripcion);
            stmt.setObject(7, duracionSegundos);
            stmt.setString(8, formato);
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(10, direccionIP);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al guardar log de audio privado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Guardar log de mensaje de audio en grupo con transcripción
     */
    public boolean guardarLogAudioGrupo(String remitenteUsername, Long remitenteId,
                                         String grupoNombre, Long grupoId,
                                         byte[] contenidoAudio, String transcripcion,
                                         Long duracionSegundos, String formato,
                                         String direccionIP) {
        String sql = "INSERT INTO logs_mensajes (tipo_mensaje, tipo_conversacion, " +
                    "remitente_username, remitente_id, canal_nombre, canal_id, " +
                    "contenido_audio, transcripcion_audio, duracion_segundos, formato_audio, " +
                    "timestamp, direccion_ip_remitente) " +
                    "VALUES ('AUDIO', 'GRUPO', ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            
            stmt.setString(1, remitenteUsername);
            stmt.setObject(2, remitenteId);
            stmt.setString(3, grupoNombre);
            stmt.setObject(4, grupoId);
            stmt.setBytes(5, contenidoAudio);
            stmt.setString(6, transcripcion);
            stmt.setObject(7, duracionSegundos);
            stmt.setString(8, formato);
            stmt.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(10, direccionIP);
            
            int filasAfectadas = stmt.executeUpdate();
            return filasAfectadas > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al guardar log de audio en grupo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Obtener logs de mensajes de texto (últimos N registros)
     */
    public List<LogMensaje> obtenerLogsTexto(int limite) {
        String sql = "SELECT id, tipo_conversacion, remitente_username, destinatario_username, " +
                    "canal_nombre, contenido_texto, timestamp, direccion_ip_remitente " +
                    "FROM logs_mensajes WHERE tipo_mensaje = 'TEXTO' " +
                    "ORDER BY timestamp DESC LIMIT ?";
        
        List<LogMensaje> logs = new ArrayList<>();
        
        try (
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                LogMensaje log = new LogMensaje();
                log.id = rs.getLong("id");
                log.tipoMensaje = "TEXTO";
                log.tipoConversacion = rs.getString("tipo_conversacion");
                log.remitenteUsername = rs.getString("remitente_username");
                log.destinatarioUsername = rs.getString("destinatario_username");
                log.grupoNombre = rs.getString("canal_nombre");
                log.contenidoTexto = rs.getString("contenido_texto");
                log.timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                log.direccionIP = rs.getString("direccion_ip_remitente");
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener logs de texto: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    /**
     * Obtener logs de mensajes de audio con transcripciones (últimos N registros)
     */
    public List<LogMensaje> obtenerLogsAudio(int limite) {
        String sql = "SELECT id, tipo_conversacion, remitente_username, destinatario_username, " +
                    "canal_nombre, transcripcion_audio, duracion_segundos, formato_audio, " +
                    "timestamp, direccion_ip_remitente " +
                    "FROM logs_mensajes WHERE tipo_mensaje = 'AUDIO' " +
                    "ORDER BY timestamp DESC LIMIT ?";
        
        List<LogMensaje> logs = new ArrayList<>();
        
        try (
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                LogMensaje log = new LogMensaje();
                log.id = rs.getLong("id");
                log.tipoMensaje = "AUDIO";
                log.tipoConversacion = rs.getString("tipo_conversacion");
                log.remitenteUsername = rs.getString("remitente_username");
                log.destinatarioUsername = rs.getString("destinatario_username");
                log.grupoNombre = rs.getString("canal_nombre");
                log.transcripcionAudio = rs.getString("transcripcion_audio");
                log.duracionSegundos = rs.getLong("duracion_segundos");
                log.formatoAudio = rs.getString("formato_audio");
                log.timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                log.direccionIP = rs.getString("direccion_ip_remitente");
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener logs de audio: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    /**
     * Obtener todos los logs (texto y audio) ordenados por fecha
     */
    public List<LogMensaje> obtenerTodosLosLogs(int limite) {
        String sql = "SELECT id, tipo_mensaje, tipo_conversacion, remitente_username, " +
                    "destinatario_username, canal_nombre, contenido_texto, transcripcion_audio, " +
                    "duracion_segundos, formato_audio, timestamp, direccion_ip_remitente " +
                    "FROM logs_mensajes ORDER BY timestamp DESC LIMIT ?";
        
        List<LogMensaje> logs = new ArrayList<>();
        
        try (
             PreparedStatement stmt = conexion.prepareStatement(sql)) {
            
            stmt.setInt(1, limite);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                LogMensaje log = new LogMensaje();
                log.id = rs.getLong("id");
                log.tipoMensaje = rs.getString("tipo_mensaje");
                log.tipoConversacion = rs.getString("tipo_conversacion");
                log.remitenteUsername = rs.getString("remitente_username");
                log.destinatarioUsername = rs.getString("destinatario_username");
                log.grupoNombre = rs.getString("canal_nombre");
                log.contenidoTexto = rs.getString("contenido_texto");
                log.transcripcionAudio = rs.getString("transcripcion_audio");
                
                Object duracion = rs.getObject("duracion_segundos");
                log.duracionSegundos = duracion != null ? rs.getLong("duracion_segundos") : 0;
                
                log.formatoAudio = rs.getString("formato_audio");
                log.timestamp = rs.getTimestamp("timestamp").toLocalDateTime();
                log.direccionIP = rs.getString("direccion_ip_remitente");
                logs.add(log);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener todos los logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    /**
     * Clase interna para representar un log de mensaje
     */
    public static class LogMensaje {
        public Long id;
        public String tipoMensaje; // TEXTO o AUDIO
        public String tipoConversacion; // PRIVADO o GRUPO
        public String remitenteUsername;
        public String destinatarioUsername;
        public String grupoNombre;
        public String contenidoTexto;
        public String transcripcionAudio;
        public Long duracionSegundos;
        public String formatoAudio;
        public LocalDateTime timestamp;
        public String direccionIP;
        
        @Override
        public String toString() {
            if ("TEXTO".equals(tipoMensaje)) {
                String destino = "PRIVADO".equals(tipoConversacion) 
                    ? destinatarioUsername 
                    : "Grupo: " + grupoNombre;
                return String.format("[%s] %s → %s: %s", 
                    timestamp, remitenteUsername, destino, contenidoTexto);
            } else {
                String destino = "PRIVADO".equals(tipoConversacion) 
                    ? destinatarioUsername 
                    : "Grupo: " + grupoNombre;
                return String.format("[%s] %s → %s: [AUDIO %ds] %s", 
                    timestamp, remitenteUsername, destino, duracionSegundos, transcripcionAudio);
            }
        }
    }
    
    /**
     * Contar mensajes por tipo (TEXTO o AUDIO)
     * @param tipo Tipo de mensaje: "TEXTO" o "AUDIO"
     * @return Cantidad de mensajes del tipo especificado
     * @throws SQLException Si hay error en la base de datos
     */
    public int contarMensajesPorTipo(String tipo) throws SQLException {
        String sql = "SELECT COUNT(*) FROM logs_mensajes WHERE tipo_mensaje = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, tipo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        
        return 0;
    }
    
    /**
     * Contar transcripciones exitosas (excluyendo mensajes con errores)
     * @return Cantidad de transcripciones exitosas
     * @throws SQLException Si hay error en la base de datos
     */
    public int contarTranscripcionesExitosas() throws SQLException {
        String sql = "SELECT COUNT(*) FROM logs_mensajes " +
                    "WHERE tipo_mensaje = 'AUDIO' " +
                    "AND transcripcion_audio IS NOT NULL " +
                    "AND transcripcion_audio NOT LIKE '%[%]%'";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
}
