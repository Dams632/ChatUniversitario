package com.chat.servidor.presentacion;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.chat.common.dto.RequestDTO;
import com.chat.common.dto.ResponseDTO;
import com.chat.common.network.ProtocoloMensaje;
import com.chat.common.patterns.EventoChat;
import com.chat.common.patterns.Observable;
import com.chat.common.models.Usuario;
import com.chat.servidor.datos.LogMensajeDAO;
import com.chat.servidor.negocio.ServicioAutenticacion;
import com.chat.servidor.negocio.ServicioGrupo;
import com.chat.servidor.negocio.ServicioUsuario;
import com.chat.transcripcion.ServicioTranscripcion;

/**
 * Maneja las peticiones de un cliente específico
 */
public class ManejadorCliente extends Observable implements Runnable {
    
    private final Socket socket;
    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private final ServicioAutenticacion servicioAuth;
    private final ServicioUsuario servicioUsuario;
    private final ServicioGrupo servicioGrupo;
    private final LogMensajeDAO logMensajeDAO;
    private Long usuarioId;
    private String username;
    private boolean autenticado;
    private String horaConexion;
    
    public ManejadorCliente(Socket socket, Connection conexion) {
        this.socket = socket;
        this.servicioAuth = new ServicioAutenticacion(conexion);
        this.servicioUsuario = new ServicioUsuario(conexion);
        this.servicioGrupo = new ServicioGrupo(conexion);
        this.logMensajeDAO = new LogMensajeDAO(conexion); // ✅ Inyección de dependencias
        this.autenticado = false;
        this.horaConexion = java.time.LocalTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
    
    @Override
    public void run() {
        try {
            // Inicializar streams
            salida = new ObjectOutputStream(socket.getOutputStream());
            entrada = new ObjectInputStream(socket.getInputStream());
            
            System.out.println("Cliente conectado: " + socket.getInetAddress());
            
            // Procesar mensajes del cliente
            while (!socket.isClosed()) {
                try {
                    Object objeto = entrada.readObject();
                    
                    if (objeto instanceof ProtocoloMensaje) {
                        ProtocoloMensaje protocolo = (ProtocoloMensaje) objeto;
                        procesarMensaje(protocolo);
                    }
                    
                } catch (ClassNotFoundException e) {
                    System.err.println("Error al leer objeto: " + e.getMessage());
                }
            }
            
        } catch (IOException e) {
            System.err.println("Error de I/O con cliente: " + e.getMessage());
        } finally {
            desconectar();
        }
    }

    private Map<String, Object> crearDatosConexion(String evento) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("evento", evento);
        datos.put("ip", getDireccionIP());
        datos.put("usuario", username);
        datos.put("usuarioId", usuarioId);
        return datos;
    }

    private void notificarServidor(EventoChat.TipoEvento tipo, Object datos) {
        notificarObservadores(new EventoChat(tipo, datos));
    }
    
    /**
     * Procesar mensaje del protocolo
     */
    private void procesarMensaje(ProtocoloMensaje protocolo) {
        if (protocolo.getTipo() == ProtocoloMensaje.TipoProtocolo.REQUEST) {
            RequestDTO request = (RequestDTO) protocolo.getPayload();
            ResponseDTO response = procesarRequest(request);
            enviarResponse(response);
        }
    }
    
    /**
     * Procesar request del cliente
     */
    private ResponseDTO procesarRequest(RequestDTO request) {
        try {
            switch (request.getTipo()) {
                case REGISTRO:
                    return manejarRegistro(request);
                    
                case LOGIN:
                    return manejarLogin(request);
                    
                case LOGOUT:
                    return manejarLogout(request);
                    
                case CREAR_GRUPO:
                    return manejarCrearGrupo(request);
                    
                case CREAR_GRUPO_CON_INVITACIONES:
                    return manejarCrearGrupoConInvitaciones(request);
                    
                case ACEPTAR_INVITACION:
                    return manejarAceptarInvitacion(request);
                    
                case RECHAZAR_INVITACION:
                    return manejarRechazarInvitacion(request);
                    
                case OBTENER_INVITACIONES_PENDIENTES:
                    return manejarObtenerInvitacionesPendientes(request);
                    
                case OBTENER_USUARIOS_ONLINE:
                    return manejarObtenerUsuariosOnline();
                    
                case OBTENER_TODOS_USUARIOS:
                    return manejarObtenerTodosUsuarios();
                    
                case OBTENER_GRUPOS:
                    return manejarObtenerGrupos(request);
                    
                case ENVIAR_MENSAJE:
                    return manejarEnviarMensaje(request);
                    
                case ENVIAR_MENSAJE_GRUPO:
                    return manejarEnviarMensajeGrupo(request);
                    
                case ENVIAR_MENSAJE_AUDIO:
                    return manejarEnviarMensajeAudio(request);
                    
                default:
                    return ResponseDTO.error("Tipo de request no soportado");
            }
        } catch (Exception e) {
            return ResponseDTO.error("Error en el servidor: " + e.getMessage());
        }
    }
    
    /**
     * Manejar registro de usuario
     */
    private ResponseDTO manejarRegistro(RequestDTO request) {
        try {
            String username = (String) request.getDato("username");
            String email = (String) request.getDato("email");
            String password = (String) request.getDato("password");
            String direccionIP = (String) request.getDato("direccionIP");
            byte[] foto = (byte[]) request.getDato("foto");
            
            servicioAuth.registrar(username, email, password, direccionIP, foto);
            
            ResponseDTO response = ResponseDTO.exitoso("Usuario registrado correctamente");
            response.setCodigo(ResponseDTO.CodigoEstado.CREADO);
            return response;
            
        } catch (Exception e) {
            return ResponseDTO.error(e.getMessage());
        }
    }
    
    /**
     * Manejar login de usuario
     */
    private ResponseDTO manejarLogin(RequestDTO request) {
        try {
            String usernameParam = (String) request.getDato("username");
            String password = (String) request.getDato("password");
            
            String token = servicioAuth.login(usernameParam, password);
            this.usuarioId = servicioAuth.obtenerUsuarioIdPorToken(token);
            this.username = usernameParam;
            this.autenticado = true;

            Map<String, Object> datos = crearDatosConexion("LOGIN_EXITOSO");
            datos.put("token", token);
            notificarServidor(EventoChat.TipoEvento.USUARIO_CONECTADO, datos);
            
            ResponseDTO response = ResponseDTO.exitoso("Login exitoso");
            response.addDato("token", token);
            response.addDato("usuarioId", usuarioId);
            
            // Notificar a todos los clientes que la lista de usuarios ha cambiado
            ServidorChat.getInstance().notificarActualizacionUsuarios();
            
            return response;
            
        } catch (Exception e) {
            ResponseDTO response = ResponseDTO.error(e.getMessage());
            response.setCodigo(ResponseDTO.CodigoEstado.NO_AUTORIZADO);
            return response;
        }
    }
    
    /**
     * Manejar logout de usuario
     */
    private ResponseDTO manejarLogout(RequestDTO request) {
        try {
            String token = request.getSessionToken();
            
            // Actualizar estado en línea del usuario a false
            if (usuarioId != null) {
                servicioUsuario.actualizarEstadoEnLinea(usuarioId, false);
                System.out.println("✓ Usuario " + username + " (ID: " + usuarioId + ") marcado como desconectado");
            }
            
            // Realizar logout en el sistema de autenticación
            servicioAuth.logout(token);

            Map<String, Object> datos = crearDatosConexion("LOGOUT");
            notificarServidor(EventoChat.TipoEvento.USUARIO_DESCONECTADO, datos);

            // Marcar como no autenticado
            this.autenticado = false;
            
            // Notificar a todos los clientes que la lista de usuarios ha cambiado
            ServidorChat.getInstance().notificarActualizacionUsuarios();
            
            return ResponseDTO.exitoso("Logout exitoso");
        } catch (Exception e) {
            return ResponseDTO.error(e.getMessage());
        }
    }
    
    /**
     * Manejar creación de grupo
     */
    private ResponseDTO manejarCrearGrupo(RequestDTO request) {
        try {
            String nombre = (String) request.getDato("nombre");
            String descripcion = (String) request.getDato("descripcion");
            Long creadorId = request.getUsuarioId();
            
            var grupo = servicioGrupo.crearGrupo(nombre, descripcion, creadorId);
            
            ResponseDTO response = ResponseDTO.exitoso("Grupo creado correctamente");
            response.addDato("grupo", grupo);
            return response;
            
        } catch (Exception e) {
            return ResponseDTO.error(e.getMessage());
        }
    }
    
    /**
     * Manejar obtener usuarios online
     */
    private ResponseDTO manejarObtenerUsuariosOnline() {
        try {
            var usuarios = servicioUsuario.obtenerUsuariosEnLinea();
            
            ResponseDTO response = ResponseDTO.exitoso("Usuarios obtenidos");
            response.addDato("usuarios", usuarios);
            return response;
            
        } catch (Exception e) {
            return ResponseDTO.error(e.getMessage());
        }
    }
    
    /**
     * Manejar obtener todos los usuarios (conectados y desconectados)
     */
    private ResponseDTO manejarObtenerTodosUsuarios() {
        try {
            List<Usuario> usuarios = new ArrayList<>(servicioUsuario.obtenerTodosLosUsuarios());
            Set<String> usernamesRegistrados = new HashSet<>();
            for (Usuario usuario : usuarios) {
                if (usuario.getUsername() != null) {
                    usernamesRegistrados.add(usuario.getUsername());
                }
            }

            // Añade usuarios remotos conocidos por las conexiones P2P activas.
            ServidorChat servidor = ServidorChat.getInstance();
            if (servidor != null) {
                for (ServidorChat.UsuarioRemotoInfo remoto : servidor.obtenerUsuariosRemotos()) {
                    String usernameRemoto = remoto.getUsername();
                    if (usernameRemoto == null || usernameRemoto.isBlank() || usernamesRegistrados.contains(usernameRemoto)) {
                        continue;
                    }

                    Usuario usuarioRemoto = new Usuario();
                    usuarioRemoto.setUsername(usernameRemoto);
                    usuarioRemoto.setEnLinea(true);

                    String hostRemoto = remoto.getHost();
                    int puertoRemoto = remoto.getPuerto();
                    if (hostRemoto != null && !hostRemoto.isBlank()) {
                        usuarioRemoto.setDireccionIP(
                            puertoRemoto > 0 ? hostRemoto + ":" + puertoRemoto : hostRemoto
                        );
                    }

                    usuarios.add(usuarioRemoto);
                    usernamesRegistrados.add(usernameRemoto);
                }
            }
            
            ResponseDTO response = ResponseDTO.exitoso("Usuarios obtenidos");
            response.addDato("usuarios", usuarios);
            return response;
            
        } catch (Exception e) {
            return ResponseDTO.error(e.getMessage());
        }
    }
    
    /**
     * Manejar obtener grupos
     */
    private ResponseDTO manejarObtenerGrupos(RequestDTO request) {
        try {
            Long usuarioId = request.getUsuarioId();
            var canales = servicioGrupo.obtenerCanalesDeUsuario(usuarioId);
            
            ResponseDTO response = ResponseDTO.exitoso("Grupos obtenidos");
            response.addDato("grupos", canales); // Keep "grupos" key for backward compatibility
            return response;
            
        } catch (Exception e) {
            return ResponseDTO.error(e.getMessage());
        }
    }
    
    /**
     * Manejar envío de mensaje
     */
    private ResponseDTO manejarEnviarMensaje(RequestDTO request) {
        try {
            String usernameDestino = (String) request.getDato("usernameDestino");
            String contenido = (String) request.getDato("contenido");
            
            if (!autenticado) {
                return ResponseDTO.error("Usuario no autenticado");
            }
            
            // Notificar al servidor para que reenvíe el mensaje al destinatario
            ServidorChat.getInstance().enviarMensajeAUsuario(
                username, 
                usernameDestino, 
                contenido
            );

            Map<String, Object> datos = new HashMap<>();
            datos.put("tipo", "MENSAJE_PRIVADO");
            datos.put("remitente", username);
            datos.put("destinatario", usernameDestino);
            datos.put("contenido", contenido);
            notificarServidor(EventoChat.TipoEvento.MENSAJE_ENVIADO, datos);
            
            // Guardar log del mensaje de texto privado
            try {
                String direccionIP = socket.getInetAddress().getHostAddress();
                logMensajeDAO.guardarLogTextoPrivado(
                    username, usuarioId, usernameDestino, null, contenido, direccionIP
                );
                System.out.println("📝 Log guardado: " + username + " → " + usernameDestino + ": " + contenido);
            } catch (Exception e) {
                System.err.println("⚠️ Error al guardar log de mensaje: " + e.getMessage());
            }
            
            return ResponseDTO.exitoso("Mensaje enviado correctamente");
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al enviar mensaje: " + e.getMessage());
        }
    }
    
    /**
     * Manejar envío de mensaje a grupo/canal
     */
    private ResponseDTO manejarEnviarMensajeGrupo(RequestDTO request) {
        try {
            Long canalId = (Long) request.getDato("canalId");
            String contenido = (String) request.getDato("contenido");
            String remitente = (String) request.getDato("remitente");
            
            if (!autenticado) {
                return ResponseDTO.error("Usuario no autenticado");
            }
            
            // Enviar el mensaje a todos los miembros del canal
            ServidorChat.getInstance().enviarMensajeACanal(canalId, remitente, contenido);

            Map<String, Object> datos = new HashMap<>();
            datos.put("tipo", "MENSAJE_GRUPAL");
            datos.put("canalId", canalId);
            datos.put("remitente", remitente);
            datos.put("contenido", contenido);
            notificarServidor(EventoChat.TipoEvento.BROADCAST_MENSAJE, datos);
            
            // Guardar log del mensaje de texto en grupo
            try {
                String direccionIP = socket.getInetAddress().getHostAddress();
                // Obtener nombre del canal (simplificado, puedes mejorarlo)
                String nombreCanal = "Canal_" + canalId;
                logMensajeDAO.guardarLogTextoGrupo(
                    remitente, usuarioId, nombreCanal, canalId, contenido, direccionIP
                );
                System.out.println("📝 Log guardado (grupo): " + remitente + " → " + nombreCanal + ": " + contenido);
            } catch (Exception e) {
                System.err.println("⚠️ Error al guardar log de mensaje grupal: " + e.getMessage());
            }
            
            return ResponseDTO.exitoso("Mensaje enviado al grupo");
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al enviar mensaje al grupo: " + e.getMessage());
        }
    }
    
    /**
     * Manejar envío de mensaje de audio (privado o grupal)
     */
    private ResponseDTO manejarEnviarMensajeAudio(RequestDTO request) {
        try {
            if (!autenticado) {
                return ResponseDTO.error("Usuario no autenticado");
            }
            
            String usernameDestino = (String) request.getDato("usernameDestino");
            Long canalId = (Long) request.getDato("canalId");
            byte[] contenidoAudio = (byte[]) request.getDato("contenidoAudio");
            String formato = (String) request.getDato("formato");
            Long duracionSegundos = (Long) request.getDato("duracionSegundos");
            
            if (contenidoAudio == null || contenidoAudio.length == 0) {
                return ResponseDTO.error("Contenido de audio vacío");
            }
            
            // Determinar si es mensaje privado o grupal
            boolean esGrupal = (canalId != null);
            
            if (esGrupal) {
                // Enviar audio a grupo/canal
                ServidorChat.getInstance().enviarAudioACanal(
                    canalId, username, contenidoAudio, formato, duracionSegundos
                );

                Map<String, Object> datos = new HashMap<>();
                datos.put("tipo", "AUDIO_GRUPAL");
                datos.put("canalId", canalId);
                datos.put("remitente", username);
                datos.put("formato", formato);
                datos.put("duracion", duracionSegundos);
                notificarServidor(EventoChat.TipoEvento.AUDIO_GRUPO_RECIBIDO, datos);
            } else {
                // Enviar audio privado
                if (usernameDestino == null || usernameDestino.isEmpty()) {
                    return ResponseDTO.error("Usuario destino no especificado");
                }
                
                ServidorChat.getInstance().enviarAudioAUsuario(
                    username, usernameDestino, contenidoAudio, formato, duracionSegundos
                );

                Map<String, Object> datos = new HashMap<>();
                datos.put("tipo", "AUDIO_PRIVADO");
                datos.put("remitente", username);
                datos.put("destinatario", usernameDestino);
                datos.put("formato", formato);
                datos.put("duracion", duracionSegundos);
                notificarServidor(EventoChat.TipoEvento.AUDIO_RECIBIDO, datos);
            }
            
            // Transcribir y loggear el audio en segundo plano
            transcribirYLoggearAudio(contenidoAudio, formato, duracionSegundos, 
                                      usernameDestino, canalId, esGrupal);
            
            return ResponseDTO.exitoso("Audio enviado correctamente");
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al enviar audio: " + e.getMessage());
        }
    }
    
    /**
     * Transcribir audio y guardar log (ejecuta en segundo plano)
     */
    private void transcribirYLoggearAudio(byte[] contenidoAudio, String formato, Long duracionSegundos,
                                          String usernameDestino, Long canalId, boolean esGrupal) {
        // Ejecutar en un hilo separado para no bloquear el flujo principal
        new Thread(() -> {
            try {
                String transcripcion = "[Transcripción no disponible]";
                
                // Intentar transcribir el audio
                ServicioTranscripcion servicioTranscripcion = ServicioTranscripcion.obtenerInstancia();
                if (servicioTranscripcion.estaInicializado()) {
                    System.out.println("🎤 Transcribiendo audio de " + username + "...");
                    transcripcion = servicioTranscripcion.transcribir(contenidoAudio);
                    if (transcripcion == null || transcripcion.isEmpty()) {
                        transcripcion = "[Sin audio detectado]";
                    }
                } else {
                    System.out.println("⚠️ Servicio de transcripción no inicializado");
                }
                
                // Guardar log con transcripción
                String direccionIP = socket.getInetAddress().getHostAddress();
                
                if (esGrupal) {
                    // Log de audio grupal
                    String nombreCanal = "Canal_" + canalId;
                    logMensajeDAO.guardarLogAudioGrupo(
                        username, usuarioId, nombreCanal, canalId,
                        contenidoAudio, transcripcion, duracionSegundos, formato, direccionIP
                    );
                    System.out.println("🎵 Log audio guardado (grupo): " + username + 
                                     " → " + nombreCanal + ": \"" + transcripcion + "\"");
                } else {
                    // Log de audio privado
                    logMensajeDAO.guardarLogAudioPrivado(
                        username, usuarioId, usernameDestino, null,
                        contenidoAudio, transcripcion, duracionSegundos, formato, direccionIP
                    );
                    System.out.println("🎵 Log audio guardado: " + username + 
                                     " → " + usernameDestino + ": \"" + transcripcion + "\"");
                }
                
            } catch (Exception e) {
                System.err.println("❌ Error al transcribir/loggear audio: " + e.getMessage());
                e.printStackTrace();
            }
        }, "TranscripcionThread-" + username).start();
    }
    
    /**
     * Manejar creación de grupo con invitaciones
     */
    @SuppressWarnings("unchecked")
    private ResponseDTO manejarCrearGrupoConInvitaciones(RequestDTO request) {
        try {
            if (!autenticado) {
                return ResponseDTO.error("Usuario no autenticado");
            }
            
            String nombre = (String) request.getDato("nombre");
            String descripcion = (String) request.getDato("descripcion");
            byte[] foto = (byte[]) request.getDato("foto");
            java.util.List<String> usuariosInvitados = (java.util.List<String>) request.getDato("usuariosInvitados");
            
            // Crear canal/grupo
            ResponseDTO resultado = servicioGrupo.crearGrupoConInvitaciones(
                usuarioId, 
                username,
                nombre, 
                descripcion, 
                foto,
                usuariosInvitados
            );
            
            if (resultado.isExito()) {
                // Notificar a los usuarios invitados
                Long canalId = (Long) resultado.getDato("canalId");
                String descripcionCanal = (String) resultado.getDato("descripcion");
                byte[] fotoCanal = (byte[]) resultado.getDato("foto");
                for (String usernameInvitado : usuariosInvitados) {
                    ServidorChat.getInstance().enviarNotificacionInvitacion(
                        username,
                        usernameInvitado,
                        nombre,
                        descripcionCanal,
                        fotoCanal,
                        canalId
                    );
                }
            }
            
            return resultado;
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al crear grupo: " + e.getMessage());
        }
    }
    
    /**
     * Manejar aceptar invitación
     */
    private ResponseDTO manejarAceptarInvitacion(RequestDTO request) {
        try {
            if (!autenticado) {
                return ResponseDTO.error("Usuario no autenticado");
            }
            
            Long invitacionId = ((Number) request.getDato("invitacionId")).longValue();
            Long canalId = ((Number) request.getDato("canalId")).longValue();
            
            ResponseDTO resultado = servicioGrupo.aceptarInvitacion(invitacionId, canalId, usuarioId);
            
            return resultado;
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al aceptar invitación: " + e.getMessage());
        }
    }
    
    /**
     * Manejar rechazar invitación
     */
    private ResponseDTO manejarRechazarInvitacion(RequestDTO request) {
        try {
            if (!autenticado) {
                return ResponseDTO.error("Usuario no autenticado");
            }
            
            Long invitacionId = ((Number) request.getDato("invitacionId")).longValue();
            
            ResponseDTO resultado = servicioGrupo.rechazarInvitacion(invitacionId);
            
            return resultado;
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al rechazar invitación: " + e.getMessage());
        }
    }
    
    /**
     * Manejar obtener invitaciones pendientes
     */
    private ResponseDTO manejarObtenerInvitacionesPendientes(RequestDTO request) {
        try {
            if (!autenticado) {
                return ResponseDTO.error("Usuario no autenticado");
            }
            
            ResponseDTO resultado = servicioGrupo.obtenerInvitacionesPendientes(usuarioId);
            
            return resultado;
            
        } catch (Exception e) {
            return ResponseDTO.error("Error al obtener invitaciones: " + e.getMessage());
        }
    }
    
    /**
     * Enviar response al cliente
     */
    private void enviarResponse(ResponseDTO response) {
        try {
            ProtocoloMensaje protocolo = new ProtocoloMensaje(
                ProtocoloMensaje.TipoProtocolo.RESPONSE,
                response
            );
            salida.writeObject(protocolo);
            salida.flush();
        } catch (IOException e) {
            System.err.println("Error al enviar response: " + e.getMessage());
        }
    }
    
    /**
     * Enviar mensaje entrante al cliente (desde otro usuario)
     */
    public void recibirMensaje(String remitenteUsername, String contenido) {
        try {
            ResponseDTO notification = ResponseDTO.exitoso("Mensaje recibido");
            notification.addDato("tipo", "MENSAJE_ENTRANTE");
            notification.addDato("remitente", remitenteUsername);
            notification.addDato("contenido", contenido);
            
            ProtocoloMensaje protocolo = new ProtocoloMensaje(
                ProtocoloMensaje.TipoProtocolo.NOTIFICACION,
                notification
            );
            
            salida.writeObject(protocolo);
            salida.flush();
            
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje a " + username + ": " + e.getMessage());
        }
    }
    
    /**
     * Recibir mensaje grupal del canal
     */
    public void recibirMensajeGrupo(Long canalId, String remitenteUsername, String contenido) {
        try {
            ResponseDTO notification = ResponseDTO.exitoso("Mensaje grupal recibido");
            notification.addDato("tipo", "MENSAJE_GRUPO");
            notification.addDato("canalId", canalId);
            notification.addDato("remitente", remitenteUsername);
            notification.addDato("contenido", contenido);
            
            ProtocoloMensaje protocolo = new ProtocoloMensaje(
                ProtocoloMensaje.TipoProtocolo.NOTIFICACION,
                notification
            );
            
            salida.writeObject(protocolo);
            salida.flush();
            
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje grupal a " + username + ": " + e.getMessage());
        }
    }
    
    /**
     * Enviar audio al cliente (privado o grupal)
     */
    public void recibirAudio(String remitenteUsername, byte[] contenidoAudio, 
                             String formato, Long duracionSegundos, Long canalId) {
        try {
            ResponseDTO notification = ResponseDTO.exitoso("Audio recibido");
            
            if (canalId != null) {
                // Audio grupal
                notification.addDato("tipo", "AUDIO_GRUPO");
                notification.addDato("canalId", canalId);
            } else {
                // Audio privado
                notification.addDato("tipo", "AUDIO_ENTRANTE");
            }
            
            notification.addDato("remitente", remitenteUsername);
            notification.addDato("contenidoAudio", contenidoAudio);
            notification.addDato("formato", formato);
            notification.addDato("duracionSegundos", duracionSegundos);
            
            ProtocoloMensaje protocolo = new ProtocoloMensaje(
                ProtocoloMensaje.TipoProtocolo.NOTIFICACION,
                notification
            );
            
            salida.writeObject(protocolo);
            salida.flush();
            
        } catch (IOException e) {
            System.err.println("Error al enviar audio a " + username + ": " + e.getMessage());
        }
    }
    
    /**
     * Enviar invitación a canal/grupo al cliente
     */
    public void recibirInvitacion(String usernameInvitador, String nombreCanal, 
                                    String descripcionCanal, byte[] fotoCanal, Long canalId) {
        try {
            ResponseDTO notification = ResponseDTO.exitoso("Invitación recibida");
            notification.addDato("tipo", "INVITACION_RECIBIDA");
            notification.addDato("usernameInvitador", usernameInvitador);
            notification.addDato("nombreCanal", nombreCanal);
            notification.addDato("descripcionCanal", descripcionCanal);
            notification.addDato("fotoCanal", fotoCanal);
            notification.addDato("canalId", canalId);
            
            ProtocoloMensaje protocolo = new ProtocoloMensaje(
                ProtocoloMensaje.TipoProtocolo.NOTIFICACION,
                notification
            );
            
            salida.writeObject(protocolo);
            salida.flush();
            
        } catch (IOException e) {
            System.err.println("Error al enviar invitación a " + username + ": " + e.getMessage());
        }
    }
    
    /**
     * Notificar al cliente que la lista de usuarios en línea ha cambiado
     */
    public void notificarActualizacionUsuarios() {
        try {
            ResponseDTO notification = ResponseDTO.exitoso("Actualización de usuarios");
            notification.addDato("tipo", "USUARIOS_ACTUALIZADOS");
            
            ProtocoloMensaje protocolo = new ProtocoloMensaje(
                ProtocoloMensaje.TipoProtocolo.NOTIFICACION,
                notification
            );
            
            salida.writeObject(protocolo);
            salida.flush();
            
            System.out.println("Notificación de actualización de usuarios enviada a " + username);
            
        } catch (IOException e) {
            System.err.println("Error al notificar actualización a " + username + ": " + e.getMessage());
        }
    }
    
    /**
     * Desconectar cliente
     */
    public void desconectar() {
        try {
            // Actualizar estado en línea a false si el usuario estaba autenticado
            if (autenticado && usuarioId != null) {
                try {
                    servicioUsuario.actualizarEstadoEnLinea(usuarioId, false);
                    System.out.println("Estado en línea actualizado a false para: " + username);
                    
                    // Notificar a todos los clientes que la lista de usuarios ha cambiado
                    ServidorChat.getInstance().notificarActualizacionUsuarios();

                    Map<String, Object> datos = crearDatosConexion("DESCONEXION_FORZADA");
                    notificarServidor(EventoChat.TipoEvento.USUARIO_DESCONECTADO, datos);
                } catch (Exception e) {
                    System.err.println("Error al actualizar estado en línea: " + e.getMessage());
                }
            }
            
            if (entrada != null) entrada.close();
            if (salida != null) salida.close();
            if (socket != null && !socket.isClosed()) socket.close();

            ServidorChat servidor = ServidorChat.getInstance();
            if (servidor != null) {
                servidor.removerCliente(this);
            }
            System.out.println("Cliente desconectado: " + (username != null ? username : socket.getInetAddress()));
        } catch (IOException e) {
            System.err.println("Error al desconectar: " + e.getMessage());
        }
    }
    
    // Getters y métodos de estado
    
    public Long getUsuarioId() {
        return usuarioId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public boolean isAutenticado() {
        return autenticado;
    }
    
    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }
    
    public boolean isConectado() {
        return socket != null && !socket.isClosed();
    }
    
    public String getDireccionIP() {
        if (socket != null) {
            // Incluir puerto para diferenciar clientes en la misma máquina
            return socket.getInetAddress().getHostAddress() + ":" + socket.getPort();
        }
        return "Desconocido";
    }
    
    public String getDireccionIPSinPuerto() {
        return socket != null ? socket.getInetAddress().getHostAddress() : "Desconocido";
    }
    
    public int getPuertoCliente() {
        return socket != null ? socket.getPort() : 0;
    }
    
    public String getHoraConexion() {
        return horaConexion;
    }
    
    /**
     * Enviar notificación del servidor al usuario (mensaje broadcast)
     */
    public void recibirNotificacionServidor(String mensaje, String servidorOrigen) {
        try {
            ResponseDTO notification = ResponseDTO.exitoso("Notificación del servidor");
            notification.addDato("tipo", "NOTIFICACION_SERVIDOR");
            notification.addDato("mensaje", mensaje);
            notification.addDato("timestamp", com.chat.common.utils.FechaHoraUtil.formatearHoraActual());
            if (servidorOrigen != null && !servidorOrigen.isBlank()) {
                notification.addDato("servidorOrigen", servidorOrigen);
            }
            
            ProtocoloMensaje protocolo = new ProtocoloMensaje(
                ProtocoloMensaje.TipoProtocolo.NOTIFICACION,
                notification
            );
            
            salida.writeObject(protocolo);
            salida.flush();
            
        } catch (IOException e) {
            System.err.println("Error al enviar notificación del servidor a " + username + ": " + e.getMessage());
        }
    }
    
    /**
     * Enviar notificación del servidor en un grupo/canal (mensaje broadcast a canal)
     */
    public void recibirNotificacionServidorGrupo(Long canalId, String nombreCanal, String mensaje, String servidorOrigen) {
        try {
            ResponseDTO notification = ResponseDTO.exitoso("Notificación del servidor");
            notification.addDato("tipo", "NOTIFICACION_SERVIDOR_GRUPO");
            notification.addDato("canalId", canalId);
            notification.addDato("nombreCanal", nombreCanal);
            notification.addDato("mensaje", mensaje);
            notification.addDato("timestamp", com.chat.common.utils.FechaHoraUtil.formatearHoraActual());
            if (servidorOrigen != null && !servidorOrigen.isBlank()) {
                notification.addDato("servidorOrigen", servidorOrigen);
            }
            
            ProtocoloMensaje protocolo = new ProtocoloMensaje(
                ProtocoloMensaje.TipoProtocolo.NOTIFICACION,
                notification
            );
            
            salida.writeObject(protocolo);
            salida.flush();
            
        } catch (IOException e) {
            System.err.println("Error al enviar notificación del servidor (grupo) a " + username + ": " + e.getMessage());
        }
    }
}
