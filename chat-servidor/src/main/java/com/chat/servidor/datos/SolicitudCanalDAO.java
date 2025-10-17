package com.chat.servidor.datos;

import com.chat.common.models.SolicitudCanal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gestionar solicitudes de canales en MySQL
 */
public class SolicitudCanalDAO {
    
    private final Connection conexion;
    
    public SolicitudCanalDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Crear una nueva solicitud
     */
    public SolicitudCanal crear(SolicitudCanal solicitud) throws SQLException {
        String sql = "INSERT INTO solicitudes_canal (canal_id, usuario_id, usuario_username, canal_nombre, estado, fecha_solicitud) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, solicitud.getCanalId());
            stmt.setLong(2, solicitud.getUsuarioId());
            stmt.setString(3, solicitud.getUsuarioUsername());
            stmt.setString(4, solicitud.getCanalNombre());
            stmt.setString(5, solicitud.getEstado().name());
            stmt.setTimestamp(6, Timestamp.valueOf(solicitud.getFechaSolicitud()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        solicitud.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        
        return solicitud;
    }
    
    /**
     * Buscar solicitud por ID
     */
    public Optional<SolicitudCanal> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM solicitudes_canal WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearSolicitud(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtener solicitudes pendientes de un canal
     */
    public List<SolicitudCanal> obtenerSolicitudesPendientesCanal(Long canalId) throws SQLException {
        String sql = "SELECT * FROM solicitudes_canal WHERE canal_id = ? AND estado = 'PENDIENTE'";
        List<SolicitudCanal> solicitudes = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, canalId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(mapearSolicitud(rs));
                }
            }
        }
        
        return solicitudes;
    }
    
    /**
     * Obtener solicitudes de un usuario
     */
    public List<SolicitudCanal> obtenerSolicitudesUsuario(Long usuarioId) throws SQLException {
        String sql = "SELECT * FROM solicitudes_canal WHERE usuario_id = ? ORDER BY fecha_solicitud DESC";
        List<SolicitudCanal> solicitudes = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    solicitudes.add(mapearSolicitud(rs));
                }
            }
        }
        
        return solicitudes;
    }
    
    /**
     * Actualizar estado de solicitud
     */
    public void actualizarEstado(Long solicitudId, SolicitudCanal.EstadoSolicitud estado, String mensajeRespuesta) throws SQLException {
        String sql = "UPDATE solicitudes_canal SET estado = ?, fecha_respuesta = ?, mensaje_respuesta = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, estado.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(3, mensajeRespuesta);
            stmt.setLong(4, solicitudId);
            stmt.executeUpdate();
        }
    }
    
    /**
     * Verificar si existe solicitud pendiente
     */
    public boolean existeSolicitudPendiente(Long canalId, Long usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM solicitudes_canal WHERE canal_id = ? AND usuario_id = ? AND estado = 'PENDIENTE'";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, canalId);
            stmt.setLong(2, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Mapear ResultSet a objeto SolicitudCanal
     */
    private SolicitudCanal mapearSolicitud(ResultSet rs) throws SQLException {
        SolicitudCanal solicitud = new SolicitudCanal();
        solicitud.setId(rs.getLong("id"));
        solicitud.setCanalId(rs.getLong("canal_id"));
        solicitud.setUsuarioId(rs.getLong("usuario_id"));
        solicitud.setUsuarioUsername(rs.getString("usuario_username"));
        solicitud.setCanalNombre(rs.getString("canal_nombre"));
        solicitud.setEstado(SolicitudCanal.EstadoSolicitud.valueOf(rs.getString("estado")));
        solicitud.setMensajeRespuesta(rs.getString("mensaje_respuesta"));
        
        Timestamp fechaSolicitud = rs.getTimestamp("fecha_solicitud");
        if (fechaSolicitud != null) {
            solicitud.setFechaSolicitud(fechaSolicitud.toLocalDateTime());
        }
        
        Timestamp fechaRespuesta = rs.getTimestamp("fecha_respuesta");
        if (fechaRespuesta != null) {
            solicitud.setFechaRespuesta(fechaRespuesta.toLocalDateTime());
        }
        
        return solicitud;
    }
}
