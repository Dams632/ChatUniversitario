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

import com.chat.common.models.Canal;

/**
 * DAO para gestionar canales en MySQL
 */
public class CanalDAO {
    
    private final Connection conexion;
    
    public CanalDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Crear un nuevo canal
     */
    public Canal crear(Canal canal) throws SQLException {
        String sql = "INSERT INTO canales (nombre, descripcion, foto, creador_id, es_privado, fecha_creacion, activo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, canal.getNombre());
            stmt.setString(2, canal.getDescripcion());
            stmt.setBytes(3, canal.getFoto());
            stmt.setLong(4, canal.getCreadorId());
            stmt.setBoolean(5, canal.isEsPrivado());
            stmt.setTimestamp(6, Timestamp.valueOf(canal.getFechaCreacion()));
            stmt.setBoolean(7, canal.isActivo());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        canal.setId(generatedKeys.getLong(1));
                        // Agregar el creador como miembro
                        agregarMiembro(canal.getId(), canal.getCreadorId());
                    }
                }
            }
        }
        
        return canal;
    }
    
    /**
     * Buscar canal por ID
     */
    public Optional<Canal> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM canales WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Canal canal = mapearCanal(rs);
                    cargarMiembros(canal);
                    return Optional.of(canal);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtener todos los canales activos
     */
    public List<Canal> obtenerCanalesActivos() throws SQLException {
        String sql = "SELECT * FROM canales WHERE activo = true";
        List<Canal> canales = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Canal canal = mapearCanal(rs);
                cargarMiembros(canal);
                canales.add(canal);
            }
        }
        
        return canales;
    }
    
    /**
     * Obtener canales públicos
     */
    public List<Canal> obtenerCanalesPublicos() throws SQLException {
        String sql = "SELECT * FROM canales WHERE activo = true AND es_privado = false";
        List<Canal> canales = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Canal canal = mapearCanal(rs);
                cargarMiembros(canal);
                canales.add(canal);
            }
        }
        
        return canales;
    }
    
    /**
     * Obtener canales de un usuario
     */
    public List<Canal> obtenerCanalesDeUsuario(Long usuarioId) throws SQLException {
        String sql = "SELECT c.* FROM canales c " +
                     "INNER JOIN canal_miembros cm ON c.id = cm.canal_id " +
                     "WHERE cm.usuario_id = ? AND c.activo = true";
        List<Canal> canales = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Canal canal = mapearCanal(rs);
                    cargarMiembros(canal);
                    canales.add(canal);
                }
            }
        }
        
        return canales;
    }
    
    /**
     * Agregar un miembro al canal
     */
    public void agregarMiembro(Long canalId, Long usuarioId) throws SQLException {
        String sql = "INSERT INTO canal_miembros (canal_id, usuario_id, fecha_union) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, canalId);
            stmt.setLong(2, usuarioId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }
    
    /**
     * Verificar si un usuario es miembro de un canal
     */
    public boolean esUsuarioMiembro(Long canalId, Long usuarioId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM canal_miembros WHERE canal_id = ? AND usuario_id = ?";
        
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
     * Cargar los miembros de un canal
     */
    private void cargarMiembros(Canal canal) throws SQLException {
        String sql = "SELECT usuario_id FROM canal_miembros WHERE canal_id = ?";
        List<Long> miembros = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, canal.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    miembros.add(rs.getLong("usuario_id"));
                }
            }
        }
        
        canal.setMiembrosIds(miembros);
    }
    
    /**
     * Mapear ResultSet a objeto Canal
     */
    private Canal mapearCanal(ResultSet rs) throws SQLException {
        Canal canal = new Canal();
        canal.setId(rs.getLong("id"));
        canal.setNombre(rs.getString("nombre"));
        canal.setDescripcion(rs.getString("descripcion"));
        canal.setFoto(rs.getBytes("foto"));
        canal.setCreadorId(rs.getLong("creador_id"));
        canal.setEsPrivado(rs.getBoolean("es_privado"));
        canal.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            canal.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return canal;
    }
}
