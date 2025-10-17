package com.chat.cliente.negocio;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.chat.common.dto.RequestDTO;
import com.chat.common.dto.ResponseDTO;
import com.chat.common.models.MensajeEntranteDTO;
import com.chat.common.network.ProtocoloMensaje;
import com.chat.common.patterns.EventoChat;
import com.chat.common.patterns.GestorEventos;

/**
 * Cliente de red para comunicación con el servidor
 * Usa el patrón Observer para notificar eventos
 * El hilo de escucha maneja TODAS las lecturas del socket
 */
public class ClienteRed {
    
    private Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private String host;
    private int puerto;
    private boolean conectado;
    private Thread hiloEscucha;
    private final GestorEventos gestorEventos;
    private final BlockingQueue<ResponseDTO> colaRespuestas;
    
    public ClienteRed(String host, int puerto) {
        this.host = host;
        this.puerto = puerto;
        this.conectado = false;
        this.gestorEventos = GestorEventos.obtenerInstancia();
        this.colaRespuestas = new LinkedBlockingQueue<>();
    }
    
    /**
     * Conectar al servidor
     */
    public void conectar() throws IOException {
        socket = new Socket(host, puerto);
        salida = new ObjectOutputStream(socket.getOutputStream());
        entrada = new ObjectInputStream(socket.getInputStream());
        conectado = true;
        System.out.println("Conectado al servidor: " + host + ":" + puerto);
        
        // Iniciar hilo de escucha inmediatamente
        iniciarEscucha();
    }
    
    /**
     * Iniciar hilo de escucha para TODAS las respuestas del servidor
     * Este hilo es el ÚNICO que lee del ObjectInputStream
     * - Respuestas síncronas → cola
     * - Notificaciones asíncronas → Observer pattern
     */
    private void iniciarEscucha() {
        hiloEscucha = new Thread(() -> {
            while (conectado && socket != null && !socket.isClosed()) {
                try {
                    Object objeto = entrada.readObject();
                    
                    if (objeto instanceof ProtocoloMensaje) {
                        ProtocoloMensaje protocolo = (ProtocoloMensaje) objeto;
                        
                        if (protocolo.getTipo() == ProtocoloMensaje.TipoProtocolo.RESPONSE) {
                            // Respuesta síncrona a un request
                            ResponseDTO response = (ResponseDTO) protocolo.getPayload();
                            colaRespuestas.offer(response);
                            
                        } else if (protocolo.getTipo() == ProtocoloMensaje.TipoProtocolo.NOTIFICACION) {
                            // Notificación asíncrona (mensaje entrante o actualización de usuarios)
                            ResponseDTO notificacion = (ResponseDTO) protocolo.getPayload();
                            
                            String tipo = (String) notificacion.getDato("tipo");
                            if ("MENSAJE_ENTRANTE".equals(tipo)) {
                                String remitente = (String) notificacion.getDato("remitente");
                                String contenido = (String) notificacion.getDato("contenido");
                                
                                // Crear DTO con los datos del mensaje
                                MensajeEntranteDTO mensajeDTO = new MensajeEntranteDTO(remitente, contenido);
                                
                                // Publicar evento usando el patrón Observer
                                gestorEventos.publicarEvento(
                                    EventoChat.TipoEvento.MENSAJE_RECIBIDO, 
                                    mensajeDTO
                                );
                                
                                System.out.println("Evento MENSAJE_RECIBIDO publicado: " + remitente);
                            } else if ("MENSAJE_GRUPO".equals(tipo)) {
                                // Mensaje grupal recibido
                                Long canalId = ((Number) notificacion.getDato("canalId")).longValue();
                                String remitente = (String) notificacion.getDato("remitente");
                                String contenido = (String) notificacion.getDato("contenido");
                                
                                // Crear mapa con datos del mensaje grupal
                                java.util.Map<String, Object> datosMensaje = new java.util.HashMap<>();
                                datosMensaje.put("canalId", canalId);
                                datosMensaje.put("remitente", remitente);
                                datosMensaje.put("contenido", contenido);
                                
                                // Publicar evento
                                gestorEventos.publicarEvento(
                                    EventoChat.TipoEvento.MENSAJE_GRUPO_RECIBIDO, 
                                    datosMensaje
                                );
                                
                                System.out.println("Evento MENSAJE_GRUPO_RECIBIDO publicado de: " + remitente + " en canal: " + canalId);
                            } else if ("USUARIOS_ACTUALIZADOS".equals(tipo)) {
                                // La lista de usuarios en línea ha cambiado
                                gestorEventos.publicarEvento(
                                    EventoChat.TipoEvento.USUARIOS_ACTUALIZADOS,
                                    null
                                );
                                
                                System.out.println("Evento USUARIOS_ACTUALIZADOS publicado");
                            } else if ("INVITACION_RECIBIDA".equals(tipo)) {
                                // Invitación a grupo/canal recibida
                                String usernameInvitador = (String) notificacion.getDato("usernameInvitador");
                                String nombreCanal = (String) notificacion.getDato("nombreCanal");
                                String descripcionCanal = (String) notificacion.getDato("descripcionCanal");
                                byte[] fotoCanal = (byte[]) notificacion.getDato("fotoCanal");
                                Long canalId = ((Number) notificacion.getDato("canalId")).longValue();
                                
                                // Crear mapa con datos de la invitación
                                java.util.Map<String, Object> datosInvitacion = new java.util.HashMap<>();
                                datosInvitacion.put("usernameInvitador", usernameInvitador);
                                datosInvitacion.put("nombreCanal", nombreCanal);
                                datosInvitacion.put("descripcionCanal", descripcionCanal);
                                datosInvitacion.put("fotoCanal", fotoCanal);
                                datosInvitacion.put("canalId", canalId);
                                
                                // Publicar evento
                                gestorEventos.publicarEvento(
                                    EventoChat.TipoEvento.INVITACION_RECIBIDA,
                                    datosInvitacion
                                );
                                
                                System.out.println("Evento INVITACION_RECIBIDA publicado de: " + usernameInvitador);
                            } else if ("AUDIO_ENTRANTE".equals(tipo)) {
                                // Audio privado recibido
                                String remitente = (String) notificacion.getDato("remitente");
                                byte[] contenidoAudio = (byte[]) notificacion.getDato("contenidoAudio");
                                String formato = (String) notificacion.getDato("formato");
                                Long duracionSegundos = ((Number) notificacion.getDato("duracionSegundos")).longValue();
                                
                                // Crear DTO con los datos del audio
                                com.chat.common.dto.AudioEntranteDTO audioDTO = 
                                    new com.chat.common.dto.AudioEntranteDTO(
                                        remitente, contenidoAudio, formato, duracionSegundos,
                                        com.chat.common.utils.FechaHoraUtil.formatearHoraActual()
                                    );
                                
                                // Publicar evento
                                gestorEventos.publicarEvento(
                                    EventoChat.TipoEvento.AUDIO_RECIBIDO,
                                    audioDTO
                                );
                                
                                System.out.println("Evento AUDIO_RECIBIDO publicado de: " + remitente + 
                                                 " (formato: " + formato + ", duración: " + duracionSegundos + "s)");
                            } else if ("AUDIO_GRUPO".equals(tipo)) {
                                // Audio grupal recibido
                                Long canalId = ((Number) notificacion.getDato("canalId")).longValue();
                                String remitente = (String) notificacion.getDato("remitente");
                                byte[] contenidoAudio = (byte[]) notificacion.getDato("contenidoAudio");
                                String formato = (String) notificacion.getDato("formato");
                                Long duracionSegundos = ((Number) notificacion.getDato("duracionSegundos")).longValue();
                                
                                // Crear DTO con los datos del audio grupal
                                com.chat.common.dto.AudioEntranteDTO audioDTO = 
                                    new com.chat.common.dto.AudioEntranteDTO(
                                        remitente, contenidoAudio, formato, duracionSegundos,
                                        com.chat.common.utils.FechaHoraUtil.formatearHoraActual()
                                    );
                                audioDTO.setCanalId(canalId);
                                
                                // Publicar evento
                                gestorEventos.publicarEvento(
                                    EventoChat.TipoEvento.AUDIO_GRUPO_RECIBIDO,
                                    audioDTO
                                );
                                
                                System.out.println("Evento AUDIO_GRUPO_RECIBIDO publicado de: " + remitente + 
                                                 " en canal: " + canalId +
                                                 " (formato: " + formato + ", duración: " + duracionSegundos + "s)");
                            } else if ("NOTIFICACION_SERVIDOR".equals(tipo)) {
                                // Notificación broadcast del servidor (usuarios)
                                String mensaje = (String) notificacion.getDato("mensaje");
                                String timestamp = (String) notificacion.getDato("timestamp");
                                
                                // Crear objeto con los datos
                                java.util.Map<String, Object> datosNotificacion = new java.util.HashMap<>();
                                datosNotificacion.put("mensaje", mensaje);
                                datosNotificacion.put("timestamp", timestamp);
                                
                                // Publicar evento
                                gestorEventos.publicarEvento(
                                    EventoChat.TipoEvento.NOTIFICACION_SERVIDOR,
                                    datosNotificacion
                                );
                                
                                System.out.println("Notificación del servidor recibida: " + mensaje);
                            } else if ("NOTIFICACION_SERVIDOR_GRUPO".equals(tipo)) {
                                // Notificación broadcast del servidor (grupos)
                                Long canalId = ((Number) notificacion.getDato("canalId")).longValue();
                                String nombreCanal = (String) notificacion.getDato("nombreCanal");
                                String mensaje = (String) notificacion.getDato("mensaje");
                                String timestamp = (String) notificacion.getDato("timestamp");
                                
                                // Crear objeto con los datos
                                java.util.Map<String, Object> datosNotificacion = new java.util.HashMap<>();
                                datosNotificacion.put("canalId", canalId);
                                datosNotificacion.put("nombreCanal", nombreCanal);
                                datosNotificacion.put("mensaje", mensaje);
                                datosNotificacion.put("timestamp", timestamp);
                                
                                // Publicar evento
                                gestorEventos.publicarEvento(
                                    EventoChat.TipoEvento.NOTIFICACION_SERVIDOR_GRUPO,
                                    datosNotificacion
                                );
                                
                                System.out.println("Notificación del servidor (grupo) recibida para " + nombreCanal + ": " + mensaje);
                            }
                        }
                    }
                    
                } catch (EOFException e) {
                    System.out.println("Conexión cerrada por el servidor");
                    conectado = false;
                    
                    // Publicar evento de desconexión forzada
                    gestorEventos.publicarEvento(
                        EventoChat.TipoEvento.DESCONEXION_FORZADA,
                        "El servidor ha cerrado la conexión"
                    );
                    
                    break;
                } catch (IOException | ClassNotFoundException e) {
                    if (conectado) {
                        System.err.println("Error al escuchar mensajes: " + e.getMessage());
                        conectado = false;
                        
                        // Publicar evento de desconexión forzada
                        gestorEventos.publicarEvento(
                            EventoChat.TipoEvento.DESCONEXION_FORZADA,
                            "Conexión perdida con el servidor"
                        );
                    }
                    break;
                }
            }
            System.out.println("Hilo de escucha terminado");
        });
        
        hiloEscucha.setDaemon(true);
        hiloEscucha.start();
        System.out.println("Hilo de escucha iniciado");
    }
    
    /**
     * Desconectar del servidor
     */
    public void desconectar() {
        try {
            conectado = false;
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null && !socket.isClosed()) socket.close();
            System.out.println("Desconectado del servidor");
        } catch (IOException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }
    
    /**
     * Enviar request al servidor y esperar response
     * NO lee directamente del socket - espera que el hilo de escucha ponga la respuesta en la cola
     */
    public ResponseDTO enviarRequest(RequestDTO request) throws IOException {
        if (!conectado) {
            throw new IOException("No conectado al servidor");
        }
        
        // Limpiar cola por si acaso
        colaRespuestas.clear();
        
        // Crear protocolo y enviar
        ProtocoloMensaje protocolo = new ProtocoloMensaje(
            ProtocoloMensaje.TipoProtocolo.REQUEST,
            request
        );
        
        synchronized (salida) {
            salida.writeObject(protocolo);
            salida.flush();
        }
        
        // Esperar response de la cola (el hilo de escucha la pondrá ahí)
        try {
            ResponseDTO response = colaRespuestas.poll(10, java.util.concurrent.TimeUnit.SECONDS);
            
            if (response == null) {
                throw new IOException("Timeout esperando respuesta del servidor");
            }
            
            return response;
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrumpido esperando respuesta", e);
        }
    }
    
    public boolean isConectado() {
        return conectado;
    }
}
