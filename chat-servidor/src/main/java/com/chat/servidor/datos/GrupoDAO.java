package com.chat.servidor.datos;

import com.chat.common.models.Grupo;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gestionar grupos en MySQL
 */
public class GrupoDAO {
    
    private Connection conexion;
    
    public GrupoDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Crear un nuevo grupo
     */
    public Grupo crear(Grupo grupo) throws SQLException {
        String sql = "INSERT INTO grupos (nombre, descripcion, creador_id, fecha_creacion, activo) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, grupo.getNombre());
            stmt.setString(2, grupo.getDescripcion());
            stmt.setLong(3, grupo.getCreadorId());
            stmt.setTimestamp(4, Timestamp.valueOf(grupo.getFechaCreacion()));
            stmt.setBoolean(5, grupo.isActivo());
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        grupo.setId(generatedKeys.getLong(1));
                        // Agregar el creador como miembro
                        agregarMiembro(grupo.getId(), grupo.getCreadorId());
                    }
                }
            }
        }
        
        return grupo;
    }
    
    /**
     * Buscar grupo por ID
     */
    public Optional<Grupo> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM grupos WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Grupo grupo = mapearGrupo(rs);
                    cargarMiembros(grupo);
                    return Optional.of(grupo);
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtener todos los grupos activos
     */
    public List<Grupo> obtenerGruposActivos() throws SQLException {
        String sql = "SELECT * FROM grupos WHERE activo = true";
        List<Grupo> grupos = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Grupo grupo = mapearGrupo(rs);
                cargarMiembros(grupo);
                grupos.add(grupo);
            }
        }
        
        return grupos;
    }
    
    /**
     * Obtener grupos de un usuario
     */
    public List<Grupo> obtenerGruposDeUsuario(Long usuarioId) throws SQLException {
        String sql = "SELECT g.* FROM grupos g " +
                     "INNER JOIN grupo_miembros gm ON g.id = gm.grupo_id " +
                     "WHERE gm.usuario_id = ? AND g.activo = true";
        List<Grupo> grupos = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, usuarioId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Grupo grupo = mapearGrupo(rs);
                    cargarMiembros(grupo);
                    grupos.add(grupo);
                }
            }
        }
        
        return grupos;
    }
    
    /**
     * Agregar un miembro al grupo
     */
    public void agregarMiembro(Long grupoId, Long usuarioId) throws SQLException {
        String sql = "INSERT INTO grupo_miembros (grupo_id, usuario_id, fecha_union) VALUES (?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, grupoId);
            stmt.setLong(2, usuarioId);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();
        }
    }
    
    /**
     * Cargar los miembros de un grupo
     */
    private void cargarMiembros(Grupo grupo) throws SQLException {
        String sql = "SELECT usuario_id FROM grupo_miembros WHERE grupo_id = ?";
        List<Long> miembros = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, grupo.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    miembros.add(rs.getLong("usuario_id"));
                }
            }
        }
        
        grupo.setMiembrosIds(miembros);
    }
    
    /**
     * Mapear ResultSet a objeto Grupo
     */
    private Grupo mapearGrupo(ResultSet rs) throws SQLException {
        Grupo grupo = new Grupo();
        grupo.setId(rs.getLong("id"));
        grupo.setNombre(rs.getString("nombre"));
        grupo.setDescripcion(rs.getString("descripcion"));
        grupo.setCreadorId(rs.getLong("creador_id"));
        grupo.setActivo(rs.getBoolean("activo"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            grupo.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return grupo;
    }
}
