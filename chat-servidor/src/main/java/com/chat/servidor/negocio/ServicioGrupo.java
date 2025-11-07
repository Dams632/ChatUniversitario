package com.chat.servidor.negocio;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
    public ServicioGrupo(Connection conexion) {
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
        List<String> invitadosLocales = new ArrayList<>();
        List<String> invitadosNoLocales = new ArrayList<>();

        try {
            // Crear el canal
            Canal nuevoCanal = new Canal(nombre, descripcion, creadorId, false);
            nuevoCanal.setFoto(foto);
            Canal canalCreado = canalDAO.crear(nuevoCanal);
            
            // Crear invitaciones para cada usuario
            if (usuariosInvitados != null) {
                for (String usernameInvitado : usuariosInvitados) {
                    if (usernameInvitado == null || usernameInvitado.isBlank()) {
                        continue;
                    }

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
                            invitadosLocales.add(usernameInvitado);
                        }
                    } else {
                        invitadosNoLocales.add(usernameInvitado);
                    }
                }
            }
            
            ResponseDTO response = ResponseDTO.exitoso("Grupo creado e invitaciones enviadas");
            response.addDato("canalId", canalCreado.getId());
            response.addDato("nombre", canalCreado.getNombre());
            response.addDato("usuariosLocales", invitadosLocales);
            response.addDato("usuariosPendientes", invitadosNoLocales);
            response.addDato("descripcion", descripcion);
            response.addDato("foto", foto);
            return response;
            
        } catch (SQLException e) {
            return ResponseDTO.error("Error al crear grupo: " + e.getMessage());
        }
    }

    /**
     * Registrar (o sincronizar) una invitación que se originó en otro servidor P2P.
     * Devuelve true si se creó una nueva invitación, false si ya existía o hubo un problema.
     */
    public boolean registrarInvitacionRemota(Long canalId, String nombreCanal, String descripcionCanal,
                                             byte[] fotoCanal, String usernameInvitado, String usernameInvitador) {
        try {
            if (canalId == null || usernameInvitado == null || usernameInvitado.isBlank()) {
                return false;
            }

            Optional<Usuario> invitadoOpt = usuarioDAO.buscarPorUsername(usernameInvitado);
            if (invitadoOpt.isEmpty()) {
                System.err.println("No se pudo registrar invitación remota: usuario invitado no existe " + usernameInvitado);
                return false;
            }

            Long invitadoId = invitadoOpt.get().getId();

            Optional<Usuario> invitadorOpt = usernameInvitador != null && !usernameInvitador.isBlank()
                ? usuarioDAO.buscarPorUsername(usernameInvitador)
                : Optional.empty();

            Long invitadorId = null;
            if(invitadorOpt.isPresent()){
                invitadorId=invitadorOpt.get().getId();
            }else{
                invitadorId=-1L;
                System.out.println("Invitador remoto (p2p): "+ usernameInvitador + "no existe");
            }

            if (invitacionDAO.existeInvitacionPendiente(canalId, invitadoId)) {
                return false;
            }

            Invitacion invitacion = new Invitacion(canalId, invitadoId, invitadorId);
            invitacion.setNombreCanal(nombreCanal);
            invitacion.setDescripcionCanal(descripcionCanal);
            invitacion.setFotoCanal(fotoCanal);
            invitacion.setUsernameInvitado(usernameInvitado);
            invitacion.setUsernameInvitador(usernameInvitador);

            invitacionDAO.crear(invitacion);
            return true;

        } catch (SQLException e) {
            System.err.println("Error al registrar invitación remota: " + e.getMessage());
            return false;
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
