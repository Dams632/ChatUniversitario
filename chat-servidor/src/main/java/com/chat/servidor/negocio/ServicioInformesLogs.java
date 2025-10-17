package com.chat.servidor.negocio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.chat.servidor.datos.LogMensajeDAO;
import com.chat.servidor.datos.LogMensajeDAO.LogMensaje;

/**
 * Servicio de negocio para gestión de logs de mensajes
 * Capa intermedia entre Presentación y Datos
 * 
 * Responsabilidades:
 * - Obtener logs de mensajes (texto y audio)
 * - Obtener estadísticas de transcripciones
 * - Aplicar lógica de negocio sobre logs
 */
public class ServicioInformesLogs {
    
    private final LogMensajeDAO logDAO;
    
    /**
     * Constructor con inyección de dependencias
     * @param conexion Conexión a la base de datos
     */
    public ServicioInformesLogs(Connection conexion) {
        this.logDAO = new LogMensajeDAO(conexion);
    }
    
    /**
     * Obtener logs de mensajes de texto
     * @param limite Número máximo de logs a retornar
     * @return Lista de logs de texto
     * @throws SQLException Si hay error en la base de datos
     */
    public List<LogMensaje> obtenerLogsTexto(int limite) throws SQLException {
        return logDAO.obtenerLogsTexto(limite);
    }
    
    /**
     * Obtener logs de mensajes de audio con transcripciones
     * @param limite Número máximo de logs a retornar
     * @return Lista de logs de audio
     * @throws SQLException Si hay error en la base de datos
     */
    public List<LogMensaje> obtenerLogsAudio(int limite) throws SQLException {
        return logDAO.obtenerLogsAudio(limite);
    }
    
    /**
     * Obtener todos los logs mezclados (texto y audio)
     * @param limite Número máximo de logs a retornar
     * @return Lista de todos los logs ordenados por timestamp
     * @throws SQLException Si hay error en la base de datos
     */
    public List<LogMensaje> obtenerTodosLosLogs(int limite) throws SQLException {
        return logDAO.obtenerTodosLosLogs(limite);
    }
    
    /**
     * Obtener estadísticas de logs
     * @return Objeto con estadísticas
     * @throws SQLException Si hay error en la base de datos
     */
    public EstadisticasLogs obtenerEstadisticas() throws SQLException {
        return new EstadisticasLogs(logDAO);
    }
    
    /**
     * Clase interna para encapsular estadísticas de logs
     */
    public static class EstadisticasLogs {
        private final int totalMensajesTexto;
        private final int totalMensajesAudio;
        private final int totalTranscripcionesExitosas;
        private final int totalTranscripcionesFallidas;
        
        EstadisticasLogs(LogMensajeDAO logDAO) throws SQLException {
            this.totalMensajesTexto = logDAO.contarMensajesPorTipo("TEXTO");
            this.totalMensajesAudio = logDAO.contarMensajesPorTipo("AUDIO");
            this.totalTranscripcionesExitosas = logDAO.contarTranscripcionesExitosas();
            this.totalTranscripcionesFallidas = this.totalMensajesAudio - this.totalTranscripcionesExitosas;
        }
        
        public int getTotalMensajesTexto() {
            return totalMensajesTexto;
        }
        
        public int getTotalMensajesAudio() {
            return totalMensajesAudio;
        }
        
        public int getTotalTranscripcionesExitosas() {
            return totalTranscripcionesExitosas;
        }
        
        public int getTotalTranscripcionesFallidas() {
            return totalTranscripcionesFallidas;
        }
        
        public int getTotalMensajes() {
            return totalMensajesTexto + totalMensajesAudio;
        }
        
        public double getPorcentajeExito() {
            if (totalMensajesAudio == 0) return 0.0;
            return (totalTranscripcionesExitosas * 100.0) / totalMensajesAudio;
        }
    }
}
