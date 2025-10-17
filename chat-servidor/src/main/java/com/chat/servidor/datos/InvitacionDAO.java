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

import com.chat.common.models.Invitacion;
import com.chat.common.models.Invitacion.EstadoInvitacion;

/**
 * DAO para gestionar invitaciones a canales en MySQL
 */
public class InvitacionDAO {
    
    private final Connection conexion;
    
    public InvitacionDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Crear una nueva invitación
     */
    public Invitacion crear(Invitacion invitacion) throws SQLException {
        String sql = "INSERT INTO invitaciones_canal (canal_id, usuario_invitado_id, usuario_invitador_id, estado, fecha_invitacion) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setLong(1, invitacion.getCanalId());
            stmt.setLong(2, invitacion.getUsuarioInvitadoId());
            stmt.setLong(3, invitacion.getUsuarioInvitadorId());
            stmt.setString(4, invitacion.getEstado().name());
            stmt.setTimestamp(5, Timestamp.valueOf(invitacion.getFechaInvitacion()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        invitacion.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        
        return invitacion;
    }
    
    /**
     * Actualizar estado de una invitación
     */
    public void actualizarEstado(Long invitacionId, EstadoInvitacion nuevoEstado) throws SQLException {
        String sql = "UPDATE invitaciones_canal SET estado = ?, fecha_respuesta = ? WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setString(1, nuevoEstado.name());
            stmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setLong(3, invitacionId);
            
            stmt.executeUpdate();
        }
    }
    
    /**
     * Obtener invitaciones pendientes de un usuario
     */
    public List<Invitacion> obtenerInvitacionesPendientes(Long usuarioId) throws SQLException {
        String sql = "SELECT i.*, c.nombre AS nombre_canal, c.descripcion AS descripcion_canal, c.foto AS foto_canal, " +
                     "u_invitador.username AS username_invitador " +
                     "FROM invitaciones_canal i " +
                     "JOIN canales c ON i.canal_id = c.id " +
                     "JOIN usuarios u_invitador ON i.usuario_invitador_id = u_invitador.id " +
                     "WHERE i.usuario_invitado_id = ? AND i.estado = 'PENDIENTE' " +
                     "ORDER BY i.fecha_invitacion DESC";
        
        List<Invitacion> invitaciones = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Invitacion invitacion = mapearInvitacion(rs);
                    invitacion.setNombreCanal(rs.getString("nombre_canal"));
                    invitacion.setDescripcionCanal(rs.getString("descripcion_canal"));
                    invitacion.setFotoCanal(rs.getBytes("foto_canal"));
                    invitacion.setUsernameInvitador(rs.getString("username_invitador"));
                    invitaciones.add(invitacion);
                }
            }
        }
        
        return invitaciones;
    }
    
    /**
     * Buscar invitación por ID
     */
    public Optional<Invitacion> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM invitaciones_canal WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearInvitacion(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Verificar si ya existe una invitación pendiente
     */
    public boolean existeInvitacionPendiente(Long canalId, Long usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM invitaciones_canal WHERE canal_id = ? AND usuario_invitado_id = ? AND estado = 'PENDIENTE'";
        
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
     * Mapear ResultSet a objeto Invitacion
     */
    private Invitacion mapearInvitacion(ResultSet rs) throws SQLException {
        Invitacion invitacion = new Invitacion();
        invitacion.setId(rs.getLong("id"));
        invitacion.setCanalId(rs.getLong("canal_id"));
        invitacion.setUsuarioInvitadoId(rs.getLong("usuario_invitado_id"));
        invitacion.setUsuarioInvitadorId(rs.getLong("usuario_invitador_id"));
        invitacion.setEstado(EstadoInvitacion.valueOf(rs.getString("estado")));
        
        Timestamp fechaInv = rs.getTimestamp("fecha_invitacion");
        if (fechaInv != null) {
            invitacion.setFechaInvitacion(fechaInv.toLocalDateTime());
        }
        
        Timestamp fechaResp = rs.getTimestamp("fecha_respuesta");
        if (fechaResp != null) {
            invitacion.setFechaRespuesta(fechaResp.toLocalDateTime());
        }
        
        return invitacion;
    }
}
