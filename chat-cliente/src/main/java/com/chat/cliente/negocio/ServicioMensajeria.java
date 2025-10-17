package com.chat.cliente.negocio;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.chat.cliente.datos.ConexionH2;
import com.chat.cliente.datos.LogAudioDAO;
import com.chat.cliente.datos.LogAudioDAO.AudioConversacion;
import com.chat.cliente.datos.LogMensajeDAO;
import com.chat.cliente.datos.LogMensajeDAO.MensajeConversacion;

/**
 * Servicio de mensajería del cliente
 * Responsable de la persistencia local de mensajes en H2
 * Separa la lógica de datos de la capa de presentación
 */
public class ServicioMensajeria {
    
    private final LogMensajeDAO logDAO;
    private final LogAudioDAO audioDAO;
    
    public ServicioMensajeria() throws SQLException {
        this.logDAO = new LogMensajeDAO(ConexionH2.obtenerConexion());
        this.audioDAO = new LogAudioDAO(ConexionH2.obtenerConexion());
    }
    
    /**
     * Guardar mensaje de forma síncrona
     * @param remitente Usuario que envía el mensaje
     * @param destinatario Usuario que recibe el mensaje
     * @param contenido Contenido del mensaje
     * @throws SQLException Si hay error en la base de datos
     */
    public void guardarMensaje(String remitente, String destinatario, String contenido) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logDAO.guardarLog(remitente, destinatario, contenido, timestamp);
    }
    
    /**
     * Guardar mensaje de forma asíncrona (no bloquea el hilo actual)
     * Útil para no bloquear la UI al guardar mensajes
     * @param remitente Usuario que envía el mensaje
     * @param destinatario Usuario que recibe el mensaje
     * @param contenido Contenido del mensaje
     * @return CompletableFuture que se completa cuando el mensaje se guarda
     */
    public CompletableFuture<Void> guardarMensajeAsync(String remitente, String destinatario, String contenido) {
        return CompletableFuture.runAsync(() -> {
            try {
                guardarMensaje(remitente, destinatario, contenido);
                System.out.println("✓ Mensaje guardado en H2: " + remitente + " → " + destinatario);
            } catch (SQLException e) {
                System.err.println("✗ Error al guardar mensaje en H2: " + e.getMessage());
                throw new RuntimeException("Error al guardar mensaje", e);
            }
        });
    }
    
    /**
     * Cargar historial de mensajes entre dos usuarios
     * @param usuario1 Primer usuario de la conversación
     * @param usuario2 Segundo usuario de la conversación
     * @return Lista de mensajes ordenados por fecha
     * @throws SQLException Si hay error en la base de datos
     */
    public List<MensajeConversacion> cargarHistorial(String usuario1, String usuario2) throws SQLException {
        return logDAO.obtenerMensajesConversacion(usuario1, usuario2);
    }
    
    /**
     * Cargar historial de forma asíncrona
     * @param usuario1 Primer usuario de la conversación
     * @param usuario2 Segundo usuario de la conversación
     * @return CompletableFuture con la lista de mensajes
     */
    public CompletableFuture<List<MensajeConversacion>> cargarHistorialAsync(String usuario1, String usuario2) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return cargarHistorial(usuario1, usuario2);
            } catch (SQLException e) {
                System.err.println("✗ Error al cargar historial desde H2: " + e.getMessage());
                throw new RuntimeException("Error al cargar historial", e);
            }
        });
    }
    
    /**
     * Guardar mensaje grupal de forma síncrona
     * @param remitente Usuario que envía el mensaje
     * @param grupoId Identificador del grupo (ej: "GRUPO_123")
     * @param contenido Contenido del mensaje
     * @throws SQLException Si hay error en la base de datos
     */
    public void guardarMensajeGrupo(String remitente, String grupoId, String contenido) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        logDAO.guardarLogGrupo(remitente, grupoId, contenido, timestamp);
    }
    
    /**
     * Guardar mensaje grupal de forma asíncrona
     * @param remitente Usuario que envía el mensaje
     * @param grupoId Identificador del grupo (ej: "GRUPO_123")
     * @param contenido Contenido del mensaje
     * @return CompletableFuture que se completa cuando el mensaje se guarda
     */
    public CompletableFuture<Void> guardarMensajeGrupoAsync(String remitente, String grupoId, String contenido) {
        return CompletableFuture.runAsync(() -> {
            try {
                guardarMensajeGrupo(remitente, grupoId, contenido);
                System.out.println("✓ Mensaje grupal guardado en H2: " + remitente + " → " + grupoId);
            } catch (SQLException e) {
                System.err.println("✗ Error al guardar mensaje grupal en H2: " + e.getMessage());
                throw new RuntimeException("Error al guardar mensaje grupal", e);
            }
        });
    }
    
    /**
     * Cargar historial de mensajes de un grupo
     * @param grupoId Identificador del grupo (ej: "GRUPO_123")
     * @return Lista de mensajes ordenados por fecha
     * @throws SQLException Si hay error en la base de datos
     */
    public List<MensajeConversacion> cargarHistorialGrupo(String grupoId) throws SQLException {
        return logDAO.obtenerMensajesGrupo(grupoId);
    }
    
    /**
     * Cargar historial de grupo de forma asíncrona
     * @param grupoId Identificador del grupo (ej: "GRUPO_123")
     * @return CompletableFuture con la lista de mensajes
     */
    public CompletableFuture<List<MensajeConversacion>> cargarHistorialGrupoAsync(String grupoId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return cargarHistorialGrupo(grupoId);
            } catch (SQLException e) {
                System.err.println("✗ Error al cargar historial de grupo desde H2: " + e.getMessage());
                throw new RuntimeException("Error al cargar historial de grupo", e);
            }
        });
    }
    
    /**
     * Limpiar mensajes antiguos (más de X días)
     * @param dias Número de días de antigüedad para eliminar
     * @throws SQLException Si hay error en la base de datos
     */
    public void limpiarMensajesAntiguos(int dias) throws SQLException {
        logDAO.limpiarLogsAntiguos(dias);
    }
    
    // ============================================================
    // MÉTODOS PARA AUDIOS
    // ============================================================
    
    /**
     * Guardar audio de forma síncrona
     * @param remitente Usuario que envía el audio
     * @param destinatario Usuario que recibe el audio
     * @param contenidoAudio Bytes del audio
     * @param formato Formato del audio (ej: "WAV")
     * @param duracionSegundos Duración en segundos
     * @throws SQLException Si hay error en la base de datos
     */
    public void guardarAudio(String remitente, String destinatario, byte[] contenidoAudio,
                            String formato, long duracionSegundos) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        audioDAO.guardarLogAudio(remitente, destinatario, null, contenidoAudio, 
                                formato, duracionSegundos, timestamp);
    }
    
    /**
     * Guardar audio de forma asíncrona (no bloquea el hilo actual)
     * @param remitente Usuario que envía el audio
     * @param destinatario Usuario que recibe el audio
     * @param contenidoAudio Bytes del audio
     * @param formato Formato del audio (ej: "WAV")
     * @param duracionSegundos Duración en segundos
     * @return CompletableFuture que se completa cuando el audio se guarda
     */
    public CompletableFuture<Void> guardarAudioAsync(String remitente, String destinatario, 
                                                     byte[] contenidoAudio, String formato, 
                                                     long duracionSegundos) {
        return CompletableFuture.runAsync(() -> {
            try {
                guardarAudio(remitente, destinatario, contenidoAudio, formato, duracionSegundos);
                System.out.println("✓ Audio guardado en H2: " + remitente + " → " + destinatario);
            } catch (SQLException e) {
                System.err.println("✗ Error al guardar audio en H2: " + e.getMessage());
                throw new RuntimeException("Error al guardar audio", e);
            }
        });
    }
    
    /**
     * Guardar audio grupal de forma síncrona
     * @param remitente Usuario que envía el audio
     * @param grupoId Identificador del grupo (ej: "GRUPO_123")
     * @param contenidoAudio Bytes del audio
     * @param formato Formato del audio (ej: "WAV")
     * @param duracionSegundos Duración en segundos
     * @throws SQLException Si hay error en la base de datos
     */
    public void guardarAudioGrupo(String remitente, String grupoId, byte[] contenidoAudio,
                                 String formato, long duracionSegundos) throws SQLException {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        audioDAO.guardarLogAudio(remitente, null, grupoId, contenidoAudio, 
                                formato, duracionSegundos, timestamp);
    }
    
    /**
     * Guardar audio grupal de forma asíncrona
     * @param remitente Usuario que envía el audio
     * @param grupoId Identificador del grupo (ej: "GRUPO_123")
     * @param contenidoAudio Bytes del audio
     * @param formato Formato del audio (ej: "WAV")
     * @param duracionSegundos Duración en segundos
     * @return CompletableFuture que se completa cuando el audio se guarda
     */
    public CompletableFuture<Void> guardarAudioGrupoAsync(String remitente, String grupoId, 
                                                          byte[] contenidoAudio, String formato, 
                                                          long duracionSegundos) {
        return CompletableFuture.runAsync(() -> {
            try {
                guardarAudioGrupo(remitente, grupoId, contenidoAudio, formato, duracionSegundos);
                System.out.println("✓ Audio grupal guardado en H2: " + remitente + " → " + grupoId);
            } catch (SQLException e) {
                System.err.println("✗ Error al guardar audio grupal en H2: " + e.getMessage());
                throw new RuntimeException("Error al guardar audio grupal", e);
            }
        });
    }
    
    /**
     * Cargar audios de una conversación privada entre dos usuarios
     * @param usuario1 Primer usuario de la conversación
     * @param usuario2 Segundo usuario de la conversación
     * @return Lista de audios ordenados por fecha
     * @throws SQLException Si hay error en la base de datos
     */
    public List<AudioConversacion> cargarAudios(String usuario1, String usuario2) throws SQLException {
        return audioDAO.obtenerAudiosConversacion(usuario1, usuario2);
    }
    
    /**
     * Cargar audios de forma asíncrona
     * @param usuario1 Primer usuario de la conversación
     * @param usuario2 Segundo usuario de la conversación
     * @return CompletableFuture con la lista de audios
     */
    public CompletableFuture<List<AudioConversacion>> cargarAudiosAsync(String usuario1, String usuario2) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return cargarAudios(usuario1, usuario2);
            } catch (SQLException e) {
                System.err.println("✗ Error al cargar audios desde H2: " + e.getMessage());
                throw new RuntimeException("Error al cargar audios", e);
            }
        });
    }
    
    /**
     * Cargar audios de un grupo
     * @param grupoId Identificador del grupo (ej: "GRUPO_123")
     * @return Lista de audios ordenados por fecha
     * @throws SQLException Si hay error en la base de datos
     */
    public List<AudioConversacion> cargarAudiosGrupo(String grupoId) throws SQLException {
        return audioDAO.obtenerAudiosGrupo(grupoId);
    }
    
    /**
     * Cargar audios de grupo de forma asíncrona
     * @param grupoId Identificador del grupo (ej: "GRUPO_123")
     * @return CompletableFuture con la lista de audios
     */
    public CompletableFuture<List<AudioConversacion>> cargarAudiosGrupoAsync(String grupoId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return cargarAudiosGrupo(grupoId);
            } catch (SQLException e) {
                System.err.println("✗ Error al cargar audios de grupo desde H2: " + e.getMessage());
                throw new RuntimeException("Error al cargar audios de grupo", e);
            }
        });
    }
}
