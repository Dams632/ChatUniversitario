package com.chat.servidor.negocio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.chat.common.models.Canal;
import com.chat.common.models.SolicitudCanal;
import com.chat.servidor.datos.CanalDAO;
import com.chat.servidor.datos.SolicitudCanalDAO;

/**
 * Servicio para gestión de canales
 */
public class ServicioCanal {
    
    private final CanalDAO canalDAO;
    private final SolicitudCanalDAO solicitudDAO;
    
    public ServicioCanal(Connection conexion) {
        this.canalDAO = new CanalDAO(conexion);
        this.solicitudDAO = new SolicitudCanalDAO(conexion);
    }
    
    /**
     * Crear un nuevo canal
     */
    public Canal crearCanal(String nombre, String descripcion, Long creadorId, boolean esPrivado) throws SQLException {
        Canal nuevoCanal = new Canal(nombre, descripcion, creadorId, esPrivado);
        return canalDAO.crear(nuevoCanal);
    }
    
    /**
     * Obtener canal por ID
     */
    public Optional<Canal> obtenerCanal(Long id) throws SQLException {
        return canalDAO.buscarPorId(id);
    }
    
    /**
     * Obtener todos los canales activos
     */
    public List<Canal> obtenerCanalesActivos() throws SQLException {
        return canalDAO.obtenerCanalesActivos();
    }
    
    /**
     * Obtener canales públicos
     */
    public List<Canal> obtenerCanalesPublicos() throws SQLException {
        return canalDAO.obtenerCanalesPublicos();
    }
    
    /**
     * Obtener canales de un usuario
     */
    public List<Canal> obtenerCanalesDeUsuario(Long usuarioId) throws SQLException {
        return canalDAO.obtenerCanalesDeUsuario(usuarioId);
    }
    
    /**
     * Solicitar unirse a un canal privado
     */
    public SolicitudCanal solicitarUnirseCanal(Long canalId, Long usuarioId, String canalNombre, String usuarioUsername) throws SQLException {
        // Verificar si ya existe una solicitud pendiente
        if (solicitudDAO.existeSolicitudPendiente(canalId, usuarioId)) {
            throw new IllegalStateException("Ya existe una solicitud pendiente para este canal");
        }
        
        // Verificar si ya es miembro
        if (canalDAO.esUsuarioMiembro(canalId, usuarioId)) {
            throw new IllegalStateException("Ya eres miembro de este canal");
        }
        
        SolicitudCanal solicitud = new SolicitudCanal(canalId, usuarioId, canalNombre, usuarioUsername);
        return solicitudDAO.crear(solicitud);
    }
    
    /**
     * Aceptar solicitud de canal
     */
    public void aceptarSolicitud(Long solicitudId) throws SQLException {
        Optional<SolicitudCanal> solicitudOpt = solicitudDAO.buscarPorId(solicitudId);
        
        if (!solicitudOpt.isPresent()) {
            throw new IllegalArgumentException("Solicitud no encontrada");
        }
        
        SolicitudCanal solicitud = solicitudOpt.get();
        
        // Actualizar estado de solicitud
        solicitudDAO.actualizarEstado(solicitudId, SolicitudCanal.EstadoSolicitud.ACEPTADA, "Aceptado");
        
        // Agregar usuario al canal
        canalDAO.agregarMiembro(solicitud.getCanalId(), solicitud.getUsuarioId());
    }
    
    /**
     * Rechazar solicitud de canal
     */
    public void rechazarSolicitud(Long solicitudId, String motivo) throws SQLException {
        solicitudDAO.actualizarEstado(solicitudId, SolicitudCanal.EstadoSolicitud.RECHAZADA, motivo);
    }
    
    /**
     * Obtener solicitudes pendientes de un canal
     */
    public List<SolicitudCanal> obtenerSolicitudesPendientes(Long canalId) throws SQLException {
        return solicitudDAO.obtenerSolicitudesPendientesCanal(canalId);
    }
    
    /**
     * Obtener solicitudes de un usuario
     */
    public List<SolicitudCanal> obtenerSolicitudesUsuario(Long usuarioId) throws SQLException {
        return solicitudDAO.obtenerSolicitudesUsuario(usuarioId);
    }
    
    /**
     * Obtener todos los canales (para broadcast)
     */
    public List<Canal> obtenerTodosLosCanales() throws SQLException {
        return canalDAO.obtenerCanalesActivos();
    }
}
