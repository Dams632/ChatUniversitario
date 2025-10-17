package com.chat.cliente.negocio;

import java.io.IOException;

import com.chat.common.dto.RequestDTO;
import com.chat.common.dto.ResponseDTO;

/**
 * Servicio del cliente para gestionar la comunicación con el servidor
 */
public class ServicioCliente {
    
    private final ClienteRed clienteRed;
    private String sessionToken;
    private Long usuarioId;
    private String username;
    
    public ServicioCliente(String host, int puerto) {
        this.clienteRed = new ClienteRed(host, puerto);
    }
    
    /**
     * Conectar al servidor
     */
    public void conectar() throws IOException {
        clienteRed.conectar();
    }
    
    /**
     * Registrar nuevo usuario
     */
    public ResponseDTO registrar(String username, String email, String password, String direccionIP) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.REGISTRO);
            request.addDato("username", username);
            request.addDato("email", email);
            request.addDato("password", password);
            request.addDato("direccionIP", direccionIP);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al registrar: " + e.getMessage());
        }
    }
    
    /**
     * Registrar nuevo usuario con foto
     */
    public ResponseDTO registrar(String username, String email, String password, String direccionIP, byte[] foto) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.REGISTRO);
            request.addDato("username", username);
            request.addDato("email", email);
            request.addDato("password", password);
            request.addDato("direccionIP", direccionIP);
            request.addDato("foto", foto);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al registrar: " + e.getMessage());
        }
    }
    
    /**
     * Iniciar sesión
     */
    public ResponseDTO login(String username, String password) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.LOGIN);
            request.addDato("username", username);
            request.addDato("password", password);
            
            ResponseDTO response = clienteRed.enviarRequest(request);
            
            if (response.isExito()) {
                this.sessionToken = (String) response.getDato("token");
                this.usuarioId = ((Number) response.getDato("usuarioId")).longValue();
                this.username = username;
            }
            
            return response;
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al iniciar sesión: " + e.getMessage());
        }
    }
    
    /**
     * Cerrar sesión
     */
    public ResponseDTO logout() {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.LOGOUT);
            request.setSessionToken(sessionToken);
            
            ResponseDTO response = clienteRed.enviarRequest(request);
            
            if (response.isExito()) {
                this.sessionToken = null;
                this.usuarioId = null;
                this.username = null;
                
                // Cerrar la conexión con el servidor después del logout
                clienteRed.desconectar();
                System.out.println("✓ Conexión cerrada después del logout");
            }
            
            return response;
            
        } catch (Exception e) {
            // Asegurar que se cierre la conexión incluso si hay error
            try {
                clienteRed.desconectar();
            } catch (Exception ex) {
                System.err.println("Error al desconectar: " + ex.getMessage());
            }
            return ResponseDTO.error("Error al cerrar sesión: " + e.getMessage());
        }
    }
    
    /**
     * Crear grupo
     */
    public ResponseDTO crearGrupo(String nombre, String descripcion) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.CREAR_GRUPO);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            request.addDato("nombre", nombre);
            request.addDato("descripcion", descripcion);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al crear grupo: " + e.getMessage());
        }
    }
    
    /**
     * Obtener usuarios en línea
     */
    public ResponseDTO obtenerUsuariosOnline() {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.OBTENER_USUARIOS_ONLINE);
            request.setSessionToken(sessionToken);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al obtener usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Obtener todos los usuarios (conectados y desconectados)
     */
    public ResponseDTO obtenerTodosLosUsuarios() {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.OBTENER_TODOS_USUARIOS);
            request.setSessionToken(sessionToken);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al obtener usuarios: " + e.getMessage());
        }
    }
    
    /**
     * Obtener grupos del usuario
     */
    public ResponseDTO obtenerGrupos() {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.OBTENER_GRUPOS);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al obtener grupos: " + e.getMessage());
        }
    }
    
    /**
     * Crear grupo/canal con invitaciones
     */
    public ResponseDTO crearGrupoConInvitaciones(String nombre, String descripcion, byte[] foto, java.util.List<String> usuariosInvitados) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.CREAR_GRUPO_CON_INVITACIONES);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            request.addDato("nombre", nombre);
            request.addDato("descripcion", descripcion);
            request.addDato("foto", foto);
            request.addDato("usuariosInvitados", usuariosInvitados);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al crear grupo con invitaciones: " + e.getMessage());
        }
    }
    
    /**
     * Enviar mensaje privado a otro usuario
     */
    public ResponseDTO enviarMensajePrivado(String usernameDestino, String contenido) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.ENVIAR_MENSAJE);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            request.addDato("usernameDestino", usernameDestino);
            request.addDato("contenido", contenido);
            request.addDato("tipoMensaje", "PRIVADO");
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al enviar mensaje: " + e.getMessage());
        }
    }
    
    /**
     * Enviar mensaje a un grupo/canal
     */
    public ResponseDTO enviarMensajeGrupo(Long canalId, String contenido) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.ENVIAR_MENSAJE_GRUPO);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            request.addDato("canalId", canalId);
            request.addDato("contenido", contenido);
            request.addDato("remitente", username);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al enviar mensaje al grupo: " + e.getMessage());
        }
    }
    
    /**
     * Enviar audio privado a un usuario
     */
    public ResponseDTO enviarAudioPrivado(String usernameDestino, byte[] contenidoAudio, 
                                          String formato, long duracionSegundos) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.ENVIAR_MENSAJE_AUDIO);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            request.addDato("usernameDestino", usernameDestino);
            request.addDato("contenidoAudio", contenidoAudio);
            request.addDato("formato", formato);
            request.addDato("duracionSegundos", duracionSegundos);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al enviar audio: " + e.getMessage());
        }
    }
    
    /**
     * Enviar audio a un grupo/canal
     */
    public ResponseDTO enviarAudioGrupo(Long canalId, byte[] contenidoAudio, 
                                        String formato, long duracionSegundos) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.ENVIAR_MENSAJE_AUDIO);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            request.addDato("canalId", canalId);
            request.addDato("contenidoAudio", contenidoAudio);
            request.addDato("formato", formato);
            request.addDato("duracionSegundos", duracionSegundos);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al enviar audio al grupo: " + e.getMessage());
        }
    }
    
    /**
     * Obtener invitaciones pendientes
     */
    public ResponseDTO obtenerInvitacionesPendientes() {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.OBTENER_INVITACIONES_PENDIENTES);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al obtener invitaciones: " + e.getMessage());
        }
    }
    
    /**
     * Aceptar invitación a un grupo/canal
     */
    public ResponseDTO aceptarInvitacion(Long invitacionId, Long canalId) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.ACEPTAR_INVITACION);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            request.addDato("invitacionId", invitacionId);
            request.addDato("canalId", canalId);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al aceptar invitación: " + e.getMessage());
        }
    }
    
    /**
     * Rechazar invitación a un grupo/canal
     */
    public ResponseDTO rechazarInvitacion(Long invitacionId) {
        try {
            RequestDTO request = new RequestDTO(RequestDTO.TipoRequest.RECHAZAR_INVITACION);
            request.setSessionToken(sessionToken);
            request.setUsuarioId(usuarioId);
            request.addDato("invitacionId", invitacionId);
            
            return clienteRed.enviarRequest(request);
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al rechazar invitación: " + e.getMessage());
        }
    }
    
    /**
     * Desconectar del servidor
     */
    public void desconectar() {
        clienteRed.desconectar();
    }
    
    // Getters
    public String getSessionToken() {
        return sessionToken;
    }
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public boolean isConectado() {
        return clienteRed.isConectado();
    }
}
