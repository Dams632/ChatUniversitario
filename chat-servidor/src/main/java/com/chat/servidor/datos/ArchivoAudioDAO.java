package com.chat.servidor.datos;

import com.chat.common.models.ArchivoAudio;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * DAO para gestionar archivos de audio en MySQL
 */
public class ArchivoAudioDAO {
    
    private final Connection conexion;
    
    public ArchivoAudioDAO(Connection conexion) {
        this.conexion = conexion;
    }
    
    /**
     * Guardar archivo de audio
     */
    public ArchivoAudio guardar(ArchivoAudio archivo) throws SQLException {
        String sql = "INSERT INTO archivos_audio (mensaje_id, nombre_archivo, contenido, duracion_segundos, formato, tamano_bytes, texto_transcrito, fecha_creacion) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            if (archivo.getMensajeId() != null) {
                stmt.setLong(1, archivo.getMensajeId());
            } else {
                stmt.setNull(1, Types.BIGINT);
            }
            
            stmt.setString(2, archivo.getNombreArchivo());
            stmt.setBytes(3, archivo.getContenido());
            stmt.setLong(4, archivo.getDuracionSegundos());
            stmt.setString(5, archivo.getFormato());
            stmt.setLong(6, archivo.getTamanoBytes());
            stmt.setString(7, archivo.getTextoTranscrito());
            stmt.setTimestamp(8, Timestamp.valueOf(archivo.getFechaCreacion()));
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        archivo.setId(generatedKeys.getLong(1));
                    }
                }
            }
        }
        
        return archivo;
    }
    
    /**
     * Buscar archivo por ID
     */
    public Optional<ArchivoAudio> buscarPorId(Long id) throws SQLException {
        String sql = "SELECT * FROM archivos_audio WHERE id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearArchivoAudio(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtener archivo por mensaje ID
     */
    public Optional<ArchivoAudio> buscarPorMensajeId(Long mensajeId) throws SQLException {
        String sql = "SELECT * FROM archivos_audio WHERE mensaje_id = ?";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setLong(1, mensajeId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapearArchivoAudio(rs));
                }
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtener todos los archivos de audio con texto transcrito
     */
    public List<ArchivoAudio> obtenerArchivosConTexto() throws SQLException {
        String sql = "SELECT * FROM archivos_audio WHERE texto_transcrito IS NOT NULL ORDER BY fecha_creacion DESC";
        List<ArchivoAudio> archivos = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                archivos.add(mapearArchivoAudio(rs));
            }
        }
        
        return archivos;
    }
    
    /**
     * Contar archivos de audio
     */
    public int contarArchivos() throws SQLException {
        String sql = "SELECT COUNT(*) FROM archivos_audio";
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        
        return 0;
    }
    
    /**
     * Mapear ResultSet a objeto ArchivoAudio
     */
    private ArchivoAudio mapearArchivoAudio(ResultSet rs) throws SQLException {
        ArchivoAudio archivo = new ArchivoAudio();
        archivo.setId(rs.getLong("id"));
        
        long mensajeId = rs.getLong("mensaje_id");
        if (!rs.wasNull()) {
            archivo.setMensajeId(mensajeId);
        }
        
        archivo.setNombreArchivo(rs.getString("nombre_archivo"));
        archivo.setContenido(rs.getBytes("contenido"));
        archivo.setDuracionSegundos(rs.getLong("duracion_segundos"));
        archivo.setFormato(rs.getString("formato"));
        archivo.setTamanoBytes(rs.getLong("tamano_bytes"));
        archivo.setTextoTranscrito(rs.getString("texto_transcrito"));
        
        Timestamp fechaCreacion = rs.getTimestamp("fecha_creacion");
        if (fechaCreacion != null) {
            archivo.setFechaCreacion(fechaCreacion.toLocalDateTime());
        }
        
        return archivo;
    }
}
