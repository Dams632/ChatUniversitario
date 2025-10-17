package com.chat.servidor.negocio;

import com.chat.common.models.ArchivoAudio;
import com.chat.common.models.Mensaje;
import com.chat.servidor.datos.ArchivoAudioDAO;
import com.chat.servidor.datos.MensajeDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Servicio para gestión de mensajería
 */
public class ServicioMensajeria {
    
    private final MensajeDAO mensajeDAO;
    private final ArchivoAudioDAO archivoAudioDAO;
    
    public ServicioMensajeria(Connection conexion) {
        this.mensajeDAO = new MensajeDAO(conexion);
        this.archivoAudioDAO = new ArchivoAudioDAO(conexion);
    }
    
    /**
     * Enviar mensaje privado
     */
    public Mensaje enviarMensajePrivado(Long remitenteId, String remitenteUsername, 
                                        Long destinatarioId, String contenido) throws SQLException {
        Mensaje mensaje = new Mensaje(remitenteId, remitenteUsername, contenido, Mensaje.TipoMensaje.PRIVADO);
        mensaje.setDestinatarioId(destinatarioId);
        return mensajeDAO.guardar(mensaje);
    }
    
    /**
     * Enviar mensaje a grupo/canal
     */
    public Mensaje enviarMensajeGrupo(Long remitenteId, String remitenteUsername, 
                                      Long grupoId, String contenido) throws SQLException {
        Mensaje mensaje = new Mensaje(remitenteId, remitenteUsername, contenido, Mensaje.TipoMensaje.GRUPO);
        mensaje.setGrupoId(grupoId);
        return mensajeDAO.guardar(mensaje);
    }
    
    /**
     * Broadcast mensaje a todos los usuarios
     */
    public Mensaje enviarBroadcast(Long remitenteId, String remitenteUsername, String contenido) throws SQLException {
        Mensaje mensaje = new Mensaje(remitenteId, remitenteUsername, contenido, Mensaje.TipoMensaje.SISTEMA);
        return mensajeDAO.guardar(mensaje);
    }
    
    /**
     * Enviar mensaje de audio
     */
    public Mensaje enviarMensajeAudio(Long remitenteId, String remitenteUsername, 
                                      Long destinatarioId, Long grupoId,
                                      ArchivoAudio archivoAudio) throws SQLException {
        // Crear mensaje
        Mensaje.TipoMensaje tipo = destinatarioId != null ? Mensaje.TipoMensaje.PRIVADO : Mensaje.TipoMensaje.GRUPO;
        Mensaje mensaje = new Mensaje(remitenteId, remitenteUsername, "[Archivo de audio]", tipo);
        
        if (destinatarioId != null) {
            mensaje.setDestinatarioId(destinatarioId);
        }
        if (grupoId != null) {
            mensaje.setGrupoId(grupoId);
        }
        
        // Guardar mensaje
        mensaje = mensajeDAO.guardar(mensaje);
        
        // Guardar archivo de audio asociado al mensaje
        archivoAudio.setMensajeId(mensaje.getId());
        archivoAudioDAO.guardar(archivoAudio);
        
        return mensaje;
    }
    
    /**
     * Obtener mensajes privados entre dos usuarios
     */
    public List<Mensaje> obtenerMensajesPrivados(Long usuario1Id, Long usuario2Id, int limite) throws SQLException {
        return mensajeDAO.obtenerMensajesPrivados(usuario1Id, usuario2Id, limite);
    }
    
    /**
     * Obtener mensajes de un grupo
     */
    public List<Mensaje> obtenerMensajesGrupo(Long grupoId, int limite) throws SQLException {
        return mensajeDAO.obtenerMensajesGrupo(grupoId, limite);
    }
    
    /**
     * Obtener todos los logs de mensajes
     */
    public List<Mensaje> obtenerLogs(int limite) throws SQLException {
        return mensajeDAO.obtenerTodosLosLogs(limite);
    }
    
    /**
     * Obtener archivos de audio con texto transcrito
     */
    public List<ArchivoAudio> obtenerArchivosAudioConTexto() throws SQLException {
        return archivoAudioDAO.obtenerArchivosConTexto();
    }
    
    /**
     * Contar total de mensajes
     */
    public int contarMensajes() throws SQLException {
        return mensajeDAO.contarMensajes();
    }
    
    /**
     * Contar archivos de audio
     */
    public int contarArchivosAudio() throws SQLException {
        return archivoAudioDAO.contarArchivos();
    }
}
