package com.chat.cliente.datos;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO para logs de audios en H2
 */
public class LogAudioDAO {
    
    private final Connection conexion;
    
    public LogAudioDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Guardar audio en logs
     */
    public void guardarLogAudio(String remitente, String destinatario, String grupoNombre,
                                byte[] contenidoAudio, String formato, long duracionSegundos,
                                Timestamp fechaEnvio) throws SQLException {
        String sql = "INSERT INTO logs_audios (remitente_username, destinatario_username, " +
                     "grupo_nombre, contenido_audio, formato, duracion_segundos, fecha_envio) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, remitente);
            stmt.setString(2, destinatario);
            stmt.setString(3, grupoNombre);
            stmt.setBytes(4, contenidoAudio);
            stmt.setString(5, formato);
            stmt.setLong(6, duracionSegundos);
            stmt.setTimestamp(7, fechaEnvio);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Obtener audios de una conversación privada entre dos usuarios
     */
    public List<AudioConversacion> obtenerAudiosConversacion(String usuario1, String usuario2) throws SQLException {
        String sql = "SELECT remitente_username, contenido_audio, formato, duracion_segundos, fecha_envio " +
                     "FROM logs_audios " +
                     "WHERE grupo_nombre IS NULL " +
                     "  AND ((remitente_username = ? AND destinatario_username = ?) " +
                     "   OR (remitente_username = ? AND destinatario_username = ?)) " +
                     "ORDER BY fecha_envio ASC";
        
        List<AudioConversacion> audios = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, usuario1);
            stmt.setString(2, usuario2);
            stmt.setString(3, usuario2);
            stmt.setString(4, usuario1);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String remitente = rs.getString("remitente_username");
                    byte[] contenidoAudio = rs.getBytes("contenido_audio");
                    String formato = rs.getString("formato");
                    long duracionSegundos = rs.getLong("duracion_segundos");
                    Timestamp fechaEnvio = rs.getTimestamp("fecha_envio");
                    
                    audios.add(new AudioConversacion(remitente, contenidoAudio, formato, 
                                                     duracionSegundos, fechaEnvio));
                }
            }
        }
        
        return audios;
    }
    
    /**
     * Obtener audios de un grupo
     */
    public List<AudioConversacion> obtenerAudiosGrupo(String grupoNombre) throws SQLException {
        String sql = "SELECT remitente_username, contenido_audio, formato, duracion_segundos, fecha_envio " +
                     "FROM logs_audios " +
                     "WHERE grupo_nombre = ? " +
                     "ORDER BY fecha_envio ASC";
        
        List<AudioConversacion> audios = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, grupoNombre);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String remitente = rs.getString("remitente_username");
                    byte[] contenidoAudio = rs.getBytes("contenido_audio");
                    String formato = rs.getString("formato");
                    long duracionSegundos = rs.getLong("duracion_segundos");
                    Timestamp fechaEnvio = rs.getTimestamp("fecha_envio");
                    
                    audios.add(new AudioConversacion(remitente, contenidoAudio, formato, 
                                                     duracionSegundos, fechaEnvio));
                }
            }
        }
        
        return audios;
    }
    
    /**
     * Clase interna para representar un audio de conversación
     */
    public static class AudioConversacion {
        private final String remitente;
        private final byte[] contenidoAudio;
        private final String formato;
        private final long duracionSegundos;
        private final Timestamp fechaEnvio;
        
        public AudioConversacion(String remitente, byte[] contenidoAudio, String formato,
                                long duracionSegundos, Timestamp fechaEnvio) {
            this.remitente = remitente;
            this.contenidoAudio = contenidoAudio;
            this.formato = formato;
            this.duracionSegundos = duracionSegundos;
            this.fechaEnvio = fechaEnvio;
        }
        
        public String getRemitente() {
            return remitente;
        }
        
        public byte[] getContenidoAudio() {
            return contenidoAudio;
        }
        
        public String getFormato() {
            return formato;
        }
        
        public long getDuracionSegundos() {
            return duracionSegundos;
        }
        
        public Timestamp getFechaEnvio() {
            return fechaEnvio;
        }
    }
}
