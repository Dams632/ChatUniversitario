package com.chat.servidor.presentacion;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.chat.common.models.Canal;
import com.chat.servidor.datos.ConexionDB;
import com.chat.servidor.negocio.ServicioCanal;
import com.chat.servidor.presentacion.gui.ServidorFrame;
import com.chat.transcripcion.ServicioTranscripcion;

/**
 * Servidor principal del chat universitario
 */
public class ServidorChat {
    
    private static final int PUERTO = 5000;
    private static ServidorChat instancia;
    private ServerSocket serverSocket;
    private boolean ejecutando;
    private Connection conexionDB;
    private List<ManejadorCliente> clientesConectados;
    private ServidorFrame gui;
    private ServicioCanal servicioCanal;
    
    public ServidorChat() {
        this.clientesConectados = new ArrayList<>();
        instancia = this;
    }
    
    /**
     * Obtener instancia del servidor (singleton)
     */
    public static ServidorChat getInstance() {
        return instancia;
    }
    
    /**
     * Enviar mensaje de un usuario a otro
     */
    public void enviarMensajeAUsuario(String remitenteUsername, String destinatarioUsername, String contenido) {
        ManejadorCliente destinatario = null;
        
        // Buscar el manejador del destinatario
        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isAutenticado() && destinatarioUsername.equals(cliente.getUsername())) {
                    destinatario = cliente;
                    break;
                }
            }
        }
        
        if (destinatario != null) {
            destinatario.recibirMensaje(remitenteUsername, contenido);
            System.out.println("Mensaje enviado de " + remitenteUsername + " a " + destinatarioUsername);
        } else {
            System.err.println("Usuario destinatario no encontrado: " + destinatarioUsername);
        }
    }
    
    /**
     * Enviar mensaje a todos los miembros de un canal/grupo
     */
    public void enviarMensajeACanal(Long canalId, String remitenteUsername, String contenido) {
        try {
            // Obtener el canal a través del servicio (respeta arquitectura 3-layer)
            Canal canal = servicioCanal.obtenerCanal(canalId).orElse(null);
            
            if (canal == null) {
                System.err.println("Canal no encontrado: " + canalId);
                return;
            }
            
            List<Long> miembrosIds = canal.getMiembrosIds();
            int mensajesEnviados = 0;
            
            // Enviar el mensaje a cada miembro conectado
            synchronized (clientesConectados) {
                for (ManejadorCliente cliente : clientesConectados) {
                    if (cliente.isAutenticado() && cliente.getUsuarioId() != null) {
                        // Verificar si este cliente es miembro del canal
                        if (miembrosIds.contains(cliente.getUsuarioId())) {
                            cliente.recibirMensajeGrupo(canalId, remitenteUsername, contenido);
                            mensajesEnviados++;
                        }
                    }
                }
            }
            
            System.out.println("Mensaje grupal enviado por " + remitenteUsername + 
                             " al canal " + canal.getNombre() + 
                             " (" + mensajesEnviados + " miembros en línea)");
            
        } catch (SQLException e) {
            System.err.println("Error al enviar mensaje al canal: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Enviar audio de un usuario a otro
     */
    public void enviarAudioAUsuario(String remitenteUsername, String destinatarioUsername, 
                                    byte[] contenidoAudio, String formato, Long duracionSegundos) {
        ManejadorCliente destinatario = null;
        
        // Buscar el manejador del destinatario
        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isAutenticado() && destinatarioUsername.equals(cliente.getUsername())) {
                    destinatario = cliente;
                    break;
                }
            }
        }
        
        if (destinatario != null) {
            destinatario.recibirAudio(remitenteUsername, contenidoAudio, formato, duracionSegundos, null);
            System.out.println("Audio enviado de " + remitenteUsername + " a " + destinatarioUsername + 
                             " (formato: " + formato + ", duración: " + duracionSegundos + "s)");
        } else {
            System.err.println("Usuario destinatario no encontrado: " + destinatarioUsername);
        }
    }
    
    /**
     * Enviar audio a todos los miembros de un canal/grupo
     */
    public void enviarAudioACanal(Long canalId, String remitenteUsername, 
                                  byte[] contenidoAudio, String formato, Long duracionSegundos) {
        try {
            // Obtener el canal a través del servicio (respeta arquitectura 3-layer)
            Canal canal = servicioCanal.obtenerCanal(canalId).orElse(null);
            
            if (canal == null) {
                System.err.println("Canal no encontrado: " + canalId);
                return;
            }
            
            List<Long> miembrosIds = canal.getMiembrosIds();
            int audiosEnviados = 0;
            
            // Enviar el audio a cada miembro conectado
            synchronized (clientesConectados) {
                for (ManejadorCliente cliente : clientesConectados) {
                    if (cliente.isAutenticado() && cliente.getUsuarioId() != null) {
                        // Verificar si este cliente es miembro del canal
                        if (miembrosIds.contains(cliente.getUsuarioId())) {
                            cliente.recibirAudio(remitenteUsername, contenidoAudio, formato, duracionSegundos, canalId);
                            audiosEnviados++;
                        }
                    }
                }
            }
            
            System.out.println("Audio grupal enviado por " + remitenteUsername + 
                             " al canal " + canal.getNombre() + 
                             " (" + audiosEnviados + " miembros en línea, formato: " + formato + 
                             ", duración: " + duracionSegundos + "s)");
            
        } catch (SQLException e) {
            System.err.println("Error al enviar audio al canal: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Notificar a todos los clientes conectados que la lista de usuarios ha cambiado
     * Se llama cuando un usuario hace login o logout
     */
    public void notificarActualizacionUsuarios() {
        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isAutenticado()) {
                    cliente.notificarActualizacionUsuarios();
                }
            }
        }
        System.out.println("Notificación de actualización de usuarios enviada a " + 
                           clientesConectados.stream().filter(ManejadorCliente::isAutenticado).count() + 
                           " clientes");
    }
    
    /**
     * Enviar notificación de invitación a un usuario
     */
    public void enviarNotificacionInvitacion(String usernameInvitador, String usernameInvitado, 
                                              String nombreCanal, String descripcionCanal, 
                                              byte[] fotoCanal, Long canalId) {
        ManejadorCliente destinatario = null;
        
        // Buscar el manejador del destinatario
        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isAutenticado() && usernameInvitado.equals(cliente.getUsername())) {
                    destinatario = cliente;
                    break;
                }
            }
        }
        
        if (destinatario != null) {
            destinatario.recibirInvitacion(usernameInvitador, nombreCanal, descripcionCanal, fotoCanal, canalId);
            System.out.println("Invitación enviada de " + usernameInvitador + " a " + usernameInvitado + 
                             " para el canal: " + nombreCanal);
        } else {
            System.out.println("Usuario no conectado, invitación quedará pendiente: " + usernameInvitado);
        }
    }
    
    /**
     * Inicializar servicio de transcripción de audio
     */
    private void inicializarServicioTranscripcion() {
        System.out.println("\n🎤 Inicializando servicio de transcripción de audio...");
        
        // Posibles ubicaciones del modelo de Vosk
        String[] posiblesRutas = {
            // Directorio actual (copiado junto al JAR)
            "vosk-model-small-es-0.42",
            // Desde target/ (cuando se ejecuta el JAR)
            "../../chat-transcripcion/src/main/resources/vosk-model-small-es-0.42",
            "../../chat-transcripcion/target/classes/vosk-model-small-es-0.42",
            // Desde raíz del proyecto (desarrollo)
            "chat-transcripcion/src/main/resources/vosk-model-small-es-0.42",
            "chat-transcripcion/target/classes/vosk-model-small-es-0.42",
            "../chat-transcripcion/src/main/resources/vosk-model-small-es-0.42",
            // Otras ubicaciones
            "models/vosk-model-small-es-0.42"
        };
        
        String rutaModelo = null;
        for (String ruta : posiblesRutas) {
            File dirModelo = new File(ruta);
            System.out.println("   Buscando en: " + dirModelo.getAbsolutePath());
            if (dirModelo.exists() && dirModelo.isDirectory()) {
                rutaModelo = dirModelo.getAbsolutePath();
                break;
            }
        }
        
        if (rutaModelo == null) {
            System.err.println("❌ ADVERTENCIA: Modelo de Vosk no encontrado");
            System.err.println("   El servidor funcionará, pero NO habrá transcripciones de audio");
            System.err.println("   Para habilitar transcripciones:");
            System.err.println("   1. Ejecuta: .\\descargar-modelo-vosk.bat");
            System.err.println("   2. O descarga manualmente desde: https://alphacephei.com/vosk/models");
            System.err.println("   3. Modelo recomendado: vosk-model-small-es-0.42.zip (~40MB)\n");
            
            if (gui != null) {
                gui.agregarLog("⚠️ Transcripción de audio NO disponible (modelo no encontrado)");
            }
            return;
        }
        
        // Inicializar servicio
        ServicioTranscripcion servicio = ServicioTranscripcion.obtenerInstancia();
        boolean inicializado = servicio.inicializar(rutaModelo);
        
        if (inicializado) {
            System.out.println("✅ Servicio de transcripción inicializado correctamente");
            System.out.println("   Modelo: " + rutaModelo + "\n");
            
            if (gui != null) {
                gui.agregarLog("✅ Transcripción de audio habilitada");
            }
        } else {
            System.err.println("❌ Error al inicializar servicio de transcripción");
            System.err.println("   Los logs de audio no tendrán transcripciones\n");
            
            if (gui != null) {
                gui.agregarLog("❌ Error al inicializar transcripción de audio");
            }
        }
    }
    
    /**
     * Iniciar el servidor
     */
    public void iniciar() {
        try {
            // Inicializar base de datos
            System.out.println("Inicializando base de datos...");
            ConexionDB.inicializarBaseDatos();
            conexionDB = ConexionDB.obtenerConexion();
            
            // Inicializar servicios
            this.servicioCanal = new ServicioCanal(conexionDB);
            
            // Inicializar servicio de transcripción de audio
            inicializarServicioTranscripcion();
            
            // Crear socket del servidor
            serverSocket = new ServerSocket(PUERTO);
            ejecutando = true;
            
            System.out.println("===========================================");
            System.out.println("   SERVIDOR DE CHAT UNIVERSITARIO");
            System.out.println("===========================================");
            System.out.println("Servidor iniciado en puerto: " + PUERTO);
            System.out.println("Esperando conexiones de clientes...");
            System.out.println("===========================================\n");
            
            // Lanzar GUI en el hilo de Swing
            if (gui != null) {
                gui.agregarLog("Servidor iniciado en puerto " + PUERTO);
                gui.agregarLog("Esperando conexiones...");
            }
            
            // Aceptar conexiones de clientes
            while (ejecutando) {
                try {
                    Socket socketCliente = serverSocket.accept();
                    
                    // Crear manejador para el cliente
                    ManejadorCliente manejador = new ManejadorCliente(socketCliente, conexionDB);
                    clientesConectados.add(manejador);
                    
                    // Ejecutar en un nuevo thread
                    Thread threadCliente = new Thread(manejador);
                    threadCliente.start();
                    
                    String ip = socketCliente.getInetAddress().getHostAddress();
                    System.out.println("Nuevo cliente conectado desde " + ip + ". Total clientes: " + clientesConectados.size());
                    
                    // Actualizar GUI
                    if (gui != null) {
                        gui.agregarLog("Cliente conectado desde " + ip);
                        gui.actualizarTabla();
                    }
                    
                } catch (IOException e) {
                    if (ejecutando) {
                        System.err.println("Error al aceptar cliente: " + e.getMessage());
                    }
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error de base de datos: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error al iniciar servidor: " + e.getMessage());
            e.printStackTrace();
        } finally {
            detener();
        }
    }
    
    /**
     * Enviar mensaje broadcast a todos los usuarios conectados
     */
    public int enviarMensajeBroadcastUsuarios(String mensaje) {
        int usuariosNotificados = 0;
        
        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isAutenticado()) {
                    cliente.recibirNotificacionServidor(mensaje);
                    usuariosNotificados++;
                }
            }
        }
        
        System.out.println("Mensaje broadcast enviado a " + usuariosNotificados + " usuarios");
        if (gui != null) {
            gui.agregarLog("Broadcast enviado a " + usuariosNotificados + " usuarios");
        }
        
        return usuariosNotificados;
    }
    
    /**
     * Enviar mensaje broadcast a todos los canales/grupos
     */
    public int enviarMensajeBroadcastCanales(String mensaje) {
        try {
            List<Canal> canales = servicioCanal.obtenerTodosLosCanales();
            int canalesNotificados = 0;
            
            for (Canal canal : canales) {
                List<Long> miembrosIds = canal.getMiembrosIds();
                
                // Enviar a cada miembro del canal
                synchronized (clientesConectados) {
                    for (ManejadorCliente cliente : clientesConectados) {
                        if (cliente.isAutenticado() && cliente.getUsuarioId() != null) {
                            if (miembrosIds.contains(cliente.getUsuarioId())) {
                                cliente.recibirNotificacionServidorGrupo(canal.getId(), canal.getNombre(), mensaje);
                            }
                        }
                    }
                }
                canalesNotificados++;
            }
            
            System.out.println("Mensaje broadcast enviado a " + canalesNotificados + " canales");
            if (gui != null) {
                gui.agregarLog("Broadcast enviado a " + canalesNotificados + " canales/grupos");
            }
            
            return canalesNotificados;
            
        } catch (Exception e) {
            System.err.println("Error al enviar broadcast a canales: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Enviar mensaje broadcast global (usuarios + canales)
     */
    public void enviarMensajeBroadcastGlobal(String mensaje) {
        int usuarios = enviarMensajeBroadcastUsuarios(mensaje);
        int canales = enviarMensajeBroadcastCanales(mensaje);
        
        System.out.println("Broadcast global completado: " + usuarios + " usuarios, " + canales + " canales");
        if (gui != null) {
            gui.agregarLog("Broadcast global: " + usuarios + " usuarios, " + canales + " canales");
        }
    }
    
    /**
     * Detener el servidor
     */
    public void detener() {
        try {
            ejecutando = false;
            
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            
            ConexionDB.cerrarConexion();
            
            System.out.println("\nServidor detenido");
            
        } catch (IOException e) {
            System.err.println("Error al detener servidor: " + e.getMessage());
        }
    }
    
    /**
     * Método principal
     */
    public static void main(String[] args) {
        // Crear instancia del servidor
        ServidorChat servidor = new ServidorChat();
        
        // Configurar Look and Feel
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("No se pudo establecer el Look and Feel del sistema");
            }
            
            // Crear y mostrar GUI
            servidor.gui = new ServidorFrame(servidor.clientesConectados);
            servidor.gui.setVisible(true);
            servidor.gui.agregarLog("Interfaz gráfica iniciada");
        });
        
        // Agregar shutdown hook para cerrar correctamente
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nCerrando servidor...");
            servidor.detener();
        }));
        
        // Iniciar servidor en hilo separado para no bloquear la GUI
        new Thread(() -> servidor.iniciar()).start();
    }
}
