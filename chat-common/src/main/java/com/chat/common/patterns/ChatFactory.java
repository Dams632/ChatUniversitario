package com.chat.common.patterns;

import com.chat.common.dto.RequestDTO;
import com.chat.common.dto.ResponseDTO;
import com.chat.common.models.*;
import com.chat.common.network.ProtocoloMensaje;

/**
 * Patrón Factory para crear objetos del dominio
 */
public class ChatFactory {
    
    /**
     * Factory Method para crear usuarios
     */
    public static Usuario crearUsuario(String username, String email, String password) {
        return new Usuario(username, email, password);
    }
    
    /**
     * Factory Method para crear mensajes
     */
    public static Mensaje crearMensaje(Long remitenteId, String remitenteUsername, 
                                       String contenido, Mensaje.TipoMensaje tipo) {
        return new Mensaje(remitenteId, remitenteUsername, contenido, tipo);
    }
    
    /**
     * Factory Method para crear mensajes privados
     */
    public static Mensaje crearMensajePrivado(Long remitenteId, String remitenteUsername,
                                               Long destinatarioId, String contenido) {
        Mensaje mensaje = new Mensaje(remitenteId, remitenteUsername, contenido, Mensaje.TipoMensaje.PRIVADO);
        mensaje.setDestinatarioId(destinatarioId);
        return mensaje;
    }
    
    /**
     * Factory Method para crear mensajes de grupo
     */
    public static Mensaje crearMensajeGrupo(Long remitenteId, String remitenteUsername,
                                             Long grupoId, String contenido) {
        Mensaje mensaje = new Mensaje(remitenteId, remitenteUsername, contenido, Mensaje.TipoMensaje.GRUPO);
        mensaje.setGrupoId(grupoId);
        return mensaje;
    }
    
    /**
     * Factory Method para crear mensajes del sistema
     */
    public static Mensaje crearMensajeSistema(String contenido) {
        return new Mensaje(0L, "Sistema", contenido, Mensaje.TipoMensaje.SISTEMA);
    }
    
    /**
     * Factory Method para crear grupos
     */
    public static Grupo crearGrupo(String nombre, String descripcion, Long creadorId) {
        return new Grupo(nombre, descripcion, creadorId);
    }
    
    /**
     * Factory Method para crear canales
     */
    public static Canal crearCanal(String nombre, String descripcion, Long creadorId, boolean esPrivado) {
        return new Canal(nombre, descripcion, creadorId, esPrivado);
    }
    
    /**
     * Factory Method para crear solicitudes de canal
     */
    public static SolicitudCanal crearSolicitudCanal(Long canalId, Long usuarioId, 
                                                      String canalNombre, String usuarioUsername) {
        return new SolicitudCanal(canalId, usuarioId, canalNombre, usuarioUsername);
    }
    
    /**
     * Factory Method para crear archivos de audio
     */
    public static ArchivoAudio crearArchivoAudio(String nombreArchivo, byte[] contenido, String formato) {
        return new ArchivoAudio(nombreArchivo, contenido, formato);
    }
    
    /**
     * Factory Method para crear requests
     */
    public static RequestDTO crearRequest(RequestDTO.TipoRequest tipo) {
        return new RequestDTO(tipo);
    }
    
    /**
     * Factory Method para crear responses exitosas
     */
    public static ResponseDTO crearResponseExitosa(String mensaje) {
        return ResponseDTO.exitoso(mensaje);
    }
    
    /**
     * Factory Method para crear responses de error
     */
    public static ResponseDTO crearResponseError(String mensaje) {
        return ResponseDTO.error(mensaje);
    }
    
    /**
     * Factory Method para crear protocolos de mensaje
     */
    public static ProtocoloMensaje crearProtocolo(ProtocoloMensaje.TipoProtocolo tipo, Object payload) {
        return new ProtocoloMensaje(tipo, payload);
    }
}
