package com.chat.servidor.negocio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.chat.common.dto.ResponseDTO;
import com.chat.common.models.Canal;
import com.chat.common.models.Grupo;
import com.chat.common.models.Invitacion;
import com.chat.common.models.Invitacion.EstadoInvitacion;
import com.chat.common.models.Usuario;
import com.chat.servidor.datos.CanalDAO;
import com.chat.servidor.datos.GrupoDAO;
import com.chat.servidor.datos.InvitacionDAO;
import com.chat.servidor.datos.UsuarioDAO;

/**
 * Servicio para gestión de grupos
 */
public class ServicioGrupo {
    
    private final GrupoDAO grupoDAO;
    private final CanalDAO canalDAO;
    private final InvitacionDAO invitacionDAO;
    private final UsuarioDAO usuarioDAO;
    private final Connection conexion;
    
    public ServicioGrupo(Connection conexion) {
        this.conexion = conexion;
        this.grupoDAO = new GrupoDAO(conexion);
        this.canalDAO = new CanalDAO(conexion);
        this.invitacionDAO = new InvitacionDAO(conexion);
        this.usuarioDAO = new UsuarioDAO(conexion);
    }
    
    /**
     * Crear un nuevo grupo
     */
    public Grupo crearGrupo(String nombre, String descripcion, Long creadorId) throws SQLException {
        Grupo nuevoGrupo = new Grupo(nombre, descripcion, creadorId);
        return grupoDAO.crear(nuevoGrupo);
    }
    
    /**
     * Obtener grupo por ID
     */
    public Optional<Grupo> obtenerGrupo(Long id) throws SQLException {
        return grupoDAO.buscarPorId(id);
    }
    
    /**
     * Obtener todos los grupos activos
     */
    public List<Grupo> obtenerGruposActivos() throws SQLException {
        return grupoDAO.obtenerGruposActivos();
    }
    
    /**
     * Obtener grupos de un usuario
     */
    public List<Grupo> obtenerGruposDeUsuario(Long usuarioId) throws SQLException {
        return grupoDAO.obtenerGruposDeUsuario(usuarioId);
    }
    
    /**
     * Agregar miembro a un grupo
     */
    public void agregarMiembro(Long grupoId, Long usuarioId) throws SQLException {
        grupoDAO.agregarMiembro(grupoId, usuarioId);
    }
    
    /**
     * Crear grupo/canal con invitaciones
     */
    public ResponseDTO crearGrupoConInvitaciones(Long creadorId, String usernameCreador, 
                                                  String nombre, String descripcion, 
                                                  byte[] foto, List<String> usuariosInvitados) {
        try {
            // Crear el canal
            Canal nuevoCanal = new Canal(nombre, descripcion, creadorId, false);
            nuevoCanal.setFoto(foto);
            Canal canalCreado = canalDAO.crear(nuevoCanal);
            
            // Crear invitaciones para cada usuario
            for (String usernameInvitado : usuariosInvitados) {
                Optional<Usuario> usuarioOpt = usuarioDAO.buscarPorUsername(usernameInvitado);
                if (usuarioOpt.isPresent()) {
                    Usuario usuario = usuarioOpt.get();
                    
                    // Verificar si ya existe invitación pendiente
                    if (!invitacionDAO.existeInvitacionPendiente(canalCreado.getId(), usuario.getId())) {
                        Invitacion invitacion = new Invitacion(
                            canalCreado.getId(),
                            usuario.getId(),
                            creadorId
                        );
                        invitacionDAO.crear(invitacion);
                    }
                }
            }
            
            ResponseDTO response = ResponseDTO.exitoso("Grupo creado e invitaciones enviadas");
            response.addDato("canalId", canalCreado.getId());
            response.addDato("nombre", canalCreado.getNombre());
            return response;
            
        } catch (SQLException e) {
            return ResponseDTO.error("Error al crear grupo: " + e.getMessage());
        }
    }
    
    /**
     * Aceptar invitación a un canal
     */
    public ResponseDTO aceptarInvitacion(Long invitacionId, Long canalId, Long usuarioId) {
        try {
            // Actualizar estado de la invitación
            invitacionDAO.actualizarEstado(invitacionId, EstadoInvitacion.ACEPTADA);
            
            // Agregar usuario al canal
            canalDAO.agregarMiembro(canalId, usuarioId);
            
            return ResponseDTO.exitoso("Invitación aceptada correctamente");
            
        } catch (SQLException e) {
            return ResponseDTO.error("Error al aceptar invitación: " + e.getMessage());
        }
    }
    
    /**
     * Rechazar invitación a un canal
     */
    public ResponseDTO rechazarInvitacion(Long invitacionId) {
        try {
            invitacionDAO.actualizarEstado(invitacionId, EstadoInvitacion.RECHAZADA);
            return ResponseDTO.exitoso("Invitación rechazada");
            
        } catch (SQLException e) {
            return ResponseDTO.error("Error al rechazar invitación: " + e.getMessage());
        }
    }
    
    /**
     * Obtener invitaciones pendientes de un usuario
     */
    public ResponseDTO obtenerInvitacionesPendientes(Long usuarioId) {
        try {
            List<Invitacion> invitaciones = invitacionDAO.obtenerInvitacionesPendientes(usuarioId);
            
            ResponseDTO response = ResponseDTO.exitoso("Invitaciones obtenidas");
            response.addDato("invitaciones", invitaciones);
            return response;
            
        } catch (SQLException e) {
            return ResponseDTO.error("Error al obtener invitaciones: " + e.getMessage());
        }
    }
    
    /**
     * Obtener canales de un usuario
     */
    public List<Canal> obtenerCanalesDeUsuario(Long usuarioId) throws SQLException {
        return canalDAO.obtenerCanalesDeUsuario(usuarioId);
    }
}
