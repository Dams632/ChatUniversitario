package com.chat.servidor.negocio;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.chat.common.models.Usuario;
import com.chat.servidor.datos.UsuarioDAO;

/**
 * Servicio para obtener estadísticas y reportes del servidor
 * Centraliza toda la lógica de negocio para informes y métricas
 */
public class ServicioEstadisticas {
    
    private final Connection conexion;
    private final UsuarioDAO usuarioDAO;
    
    public ServicioEstadisticas(Connection conexion) {
        this.conexion = conexion;
        this.usuarioDAO = new UsuarioDAO(conexion);
    }
    
    /**
     * Obtener historial de conexiones de usuarios
     * @param limite Número máximo de registros a obtener
     * @return Lista de conexiones ordenadas por fecha descendente
     */
    public List<ConexionUsuario> obtenerHistorialConexiones(int limite) throws SQLException {
        String sql = "SELECT username, ultima_conexion, en_linea, direccion_ip " +
                    "FROM usuarios " +
                    "WHERE ultima_conexion IS NOT NULL " +
                    "ORDER BY ultima_conexion DESC " +
                    "LIMIT ?";
        
        List<ConexionUsuario> conexiones = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ConexionUsuario conexionUsuario = new ConexionUsuario();
                    conexionUsuario.username = rs.getString("username");
                    conexionUsuario.ultimaConexion = rs.getTimestamp("ultima_conexion").toLocalDateTime();
                    conexionUsuario.enLinea = rs.getBoolean("en_linea");
                    conexionUsuario.direccionIp = rs.getString("direccion_ip");
                    conexiones.add(conexionUsuario);
                }
            }
        }
        
        return conexiones;
    }
    
    /**
     * Obtener estadísticas generales del servidor
     * @return Objeto con todas las métricas del servidor
     */
    public EstadisticasGenerales obtenerEstadisticasGenerales() throws SQLException {
        EstadisticasGenerales stats = new EstadisticasGenerales();
        
        // Total usuarios registrados
        try (PreparedStatement stmt = conexion.prepareStatement("SELECT COUNT(*) FROM usuarios");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.totalUsuarios = rs.getInt(1);
            }
        }
        
        // Usuarios en línea
        try (PreparedStatement stmt = conexion.prepareStatement("SELECT COUNT(*) FROM usuarios WHERE en_linea = TRUE");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.usuariosEnLinea = rs.getInt(1);
            }
        }
        
        // Total canales activos
        try (PreparedStatement stmt = conexion.prepareStatement("SELECT COUNT(*) FROM canales WHERE activo = TRUE");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.canalesActivos = rs.getInt(1);
            }
        }
        
        // Total canales (activos e inactivos)
        try (PreparedStatement stmt = conexion.prepareStatement("SELECT COUNT(*) FROM canales");
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                stats.totalCanales = rs.getInt(1);
            }
        }
        
        return stats;
    }
    
    /**
     * Obtener todos los usuarios registrados con información de conexión
     * @return Lista de usuarios para reportes
     */
    public List<UsuarioReporte> obtenerUsuariosParaReporte() throws SQLException {
        String sql = "SELECT id, username, email, fecha_registro, en_linea, direccion_ip " +
                    "FROM usuarios ORDER BY fecha_registro DESC";
        
        List<UsuarioReporte> usuarios = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                UsuarioReporte usuario = new UsuarioReporte();
                usuario.id = rs.getLong("id");
                usuario.username = rs.getString("username");
                usuario.email = rs.getString("email");
                usuario.fechaRegistro = rs.getTimestamp("fecha_registro").toLocalDateTime();
                usuario.enLinea = rs.getBoolean("en_linea");
                usuario.direccionIp = rs.getString("direccion_ip");
                usuarios.add(usuario);
            }
        }
        
        return usuarios;
    }
    
    /**
     * Obtener canales con información de miembros para reportes
     * @return Lista de canales con detalles
     */
    public List<CanalReporte> obtenerCanalesParaReporte() throws SQLException {
        String sql = "SELECT c.id, c.nombre, c.es_privado, u.username AS creador, " +
                    "c.fecha_creacion, " +
                    "(SELECT COUNT(*) FROM canal_miembros WHERE canal_id = c.id) AS total_miembros " +
                    "FROM canales c " +
                    "LEFT JOIN usuarios u ON c.creador_id = u.id " +
                    "WHERE c.activo = TRUE " +
                    "ORDER BY c.fecha_creacion DESC";
        
        List<CanalReporte> canales = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                CanalReporte canal = new CanalReporte();
                canal.id = rs.getLong("id");
                canal.nombre = rs.getString("nombre");
                canal.esPrivado = rs.getBoolean("es_privado");
                canal.creador = rs.getString("creador");
                canal.fechaCreacion = rs.getTimestamp("fecha_creacion").toLocalDateTime();
                canal.totalMiembros = rs.getInt("total_miembros");
                canales.add(canal);
            }
        }
        
        return canales;
    }
    
    /**
     * Obtener archivos de audio para reportes
     * @param limite Número máximo de registros
     * @return Lista de audios con información de remitente y destinatario
     */
    public List<AudioReporte> obtenerAudiosParaReporte(int limite) throws SQLException {
        String sql = "SELECT a.id, u1.username AS remitente, " +
                    "COALESCE(u2.username, c.nombre, 'Desconocido') AS destinatario, " +
                    "a.formato, a.duracion_segundos, " +
                    "LENGTH(a.contenido_audio) / 1024 AS tamano_kb, " +
                    "a.fecha_envio " +
                    "FROM archivos_audio a " +
                    "LEFT JOIN usuarios u1 ON a.remitente_id = u1.id " +
                    "LEFT JOIN usuarios u2 ON a.destinatario_id = u2.id " +
                    "LEFT JOIN canales c ON a.canal_id = c.id " +
                    "ORDER BY a.fecha_envio DESC " +
                    "LIMIT ?";
        
        List<AudioReporte> audios = new ArrayList<>();
        
        try (PreparedStatement stmt = conexion.prepareStatement(sql)) {
            stmt.setInt(1, limite);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    AudioReporte audio = new AudioReporte();
                    audio.id = rs.getLong("id");
                    audio.remitente = rs.getString("remitente");
                    audio.destinatario = rs.getString("destinatario");
                    audio.formato = rs.getString("formato");
                    audio.duracionSegundos = rs.getLong("duracion_segundos");
                    audio.tamanoKb = rs.getDouble("tamano_kb");
                    audio.fechaEnvio = rs.getTimestamp("fecha_envio").toLocalDateTime();
                    audios.add(audio);
                }
            }
        }
        
        return audios;
    }
    
    /**
     * Clase DTO para información de conexión de usuario
     */
    public static class ConexionUsuario {
        public String username;
        public LocalDateTime ultimaConexion;
        public boolean enLinea;
        public String direccionIp;
        
        public String getEstado() {
            return enLinea ? "[ONLINE]" : "[OFFLINE]";
        }
        
        public String getDireccionIpSegura() {
            return direccionIp != null ? direccionIp : "N/A";
        }
    }
    
    /**
     * Clase DTO para reportes de usuarios
     */
    public static class UsuarioReporte {
        public Long id;
        public String username;
        public String email;
        public LocalDateTime fechaRegistro;
        public boolean enLinea;
        public String direccionIp;
        
        public String getEstadoTexto() {
            return enLinea ? "🟢 En línea" : "⚫ Desconectado";
        }
    }
    
    /**
     * Clase DTO para reportes de canales
     */
    public static class CanalReporte {
        public Long id;
        public String nombre;
        public boolean esPrivado;
        public String creador;
        public LocalDateTime fechaCreacion;
        public int totalMiembros;
        
        public String getTipoTexto() {
            return esPrivado ? "🔒 Privado" : "🌐 Público";
        }
    }
    
    /**
     * Clase DTO para reportes de audios
     */
    public static class AudioReporte {
        public Long id;
        public String remitente;
        public String destinatario;
        public String formato;
        public long duracionSegundos;
        public double tamanoKb;
        public LocalDateTime fechaEnvio;
        
        public String getFormatoTexto() {
            return formato != null ? formato.toUpperCase() : "N/A";
        }
        
        public String getDuracionTexto() {
            return duracionSegundos + "s";
        }
        
        public String getTamanoTexto() {
            return String.format("%.2f KB", tamanoKb);
        }
    }
    
    /**
     * Clase DTO para estadísticas generales del servidor
     */
    public static class EstadisticasGenerales {
        private int totalUsuarios;
        private int usuariosEnLinea;
        private int totalCanales;
        private int canalesActivos;
        
        public int getTotalUsuarios() {
            return totalUsuarios;
        }
        
        public int getUsuariosEnLinea() {
            return usuariosEnLinea;
        }
        
        public int getUsuariosDesconectados() {
            return totalUsuarios - usuariosEnLinea;
        }
        
        public int getTotalCanales() {
            return totalCanales;
        }
        
        public int getCanalesActivos() {
            return canalesActivos;
        }
        
        public int getCanalesInactivos() {
            return totalCanales - canalesActivos;
        }
        
        public double getPorcentajeUsuariosEnLinea() {
            if (totalUsuarios == 0) return 0.0;
            return (usuariosEnLinea * 100.0) / totalUsuarios;
        }
    }
}
