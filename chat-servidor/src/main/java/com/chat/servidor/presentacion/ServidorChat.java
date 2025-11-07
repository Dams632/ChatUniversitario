package com.chat.servidor.presentacion;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.chat.common.models.Canal;
import com.chat.common.models.Usuario;
import com.chat.common.patterns.EventoChat;
import com.chat.common.patterns.Observer;
import com.chat.servidor.datos.ConexionDB;
import com.chat.servidor.negocio.ServicioAutenticacion;
import com.chat.servidor.negocio.ServicioCanal;
import com.chat.servidor.p2p.GestorServidores;
import com.chat.servidor.p2p.TablaARPServidores;
import com.chat.servidor.presentacion.gui.ServidorFrame;
import com.chat.transcripcion.ServicioTranscripcion;

/**
 * Servidor principal del chat universitario
 */
public class ServidorChat implements Observer {
    
    // Valores por defecto (pueden ser sobrescritos por config.properties)
    private static final int PUERTO_DEFAULT = 5000;
    private static final String HOST_DEFAULT = "0.0.0.0";
    private static final int MAX_USUARIOS_DEFAULT = 100;
    
    private static ServidorChat instancia;
    private ServerSocket serverSocket;
    private boolean ejecutando;
    private Connection conexionDB;
    private List<ManejadorCliente> clientesConectados;
    private ServidorFrame gui;
    private ServicioCanal servicioCanal;
    private ServicioAutenticacion servicioAutenticacion;
    private int puerto;
    private String host;
    private int maxUsuariosConectados;
    private TablaARPServidores tablaARPServidores;
    private GestorServidores gestorServidores;
    private int puertoP2P;
    private String identificadorServidor;
    private Set<InetSocketAddress> vecinosP2P;

    public static class ConexionRemotaInfo {
        private final String servidorId;
        private final String host;
        private final int puerto;
        private final long conectadoDesde;

        public ConexionRemotaInfo(String servidorId, String host, int puerto, long conectadoDesde) {
            this.servidorId = servidorId;
            this.host = host;
            this.puerto = puerto;
            this.conectadoDesde = conectadoDesde;
        }

        public String getServidorId() {
            return servidorId;
        }

        public String getHost() {
            return host;
        }

        public int getPuerto() {
            return puerto;
        }

        public long getConectadoDesde() {
            return conectadoDesde;
        }
    }

    public static class UsuarioRemotoInfo {
        private final String username;
        private final String servidorId;
        private final String host;
        private final int puerto;

        public UsuarioRemotoInfo(String username, String servidorId, String host, int puerto) {
            this.username = username;
            this.servidorId = servidorId;
            this.host = host;
            this.puerto = puerto;
        }

        public String getUsername() {
            return username;
        }

        public String getServidorId() {
            return servidorId;
        }

        public String getHost() {
            return host;
        }

        public int getPuerto() {
            return puerto;
        }
    }
    
    public ServidorChat() {
        this.clientesConectados = new ArrayList<>();
        this.puerto = PUERTO_DEFAULT;
        this.host = HOST_DEFAULT;
        this.maxUsuariosConectados = MAX_USUARIOS_DEFAULT;
        this.vecinosP2P = new HashSet<>();
        this.identificadorServidor = host + ":" + puerto;
        this.puertoP2P = puerto + 100;
        this.tablaARPServidores = new TablaARPServidores(this.identificadorServidor);
        this.gestorServidores = new GestorServidores(this, this.tablaARPServidores, this.identificadorServidor, this.puertoP2P);
        instancia = this;
    }
    
    /**
     * Constructor con puerto y host personalizados
     */
    public ServidorChat(String host, int puerto) {
        this.clientesConectados = new ArrayList<>();
        this.puerto = puerto;
        this.host = host;
        this.maxUsuariosConectados = MAX_USUARIOS_DEFAULT;
        this.vecinosP2P = new HashSet<>();
        this.identificadorServidor = host + ":" + puerto;
        this.puertoP2P = puerto + 100;
        this.tablaARPServidores = new TablaARPServidores(this.identificadorServidor);
        this.gestorServidores = new GestorServidores(this, this.tablaARPServidores, this.identificadorServidor, this.puertoP2P);
        instancia = this;
    }

    public void setMaxUsuariosConectados(int maxUsuariosConectados) {
        this.maxUsuariosConectados = Math.max(0, maxUsuariosConectados);
    }

    public void configurarRedServidores(String identificadorServidor, int puertoP2P, Set<InetSocketAddress> vecinos) {
        if (identificadorServidor == null || identificadorServidor.isBlank()) {
            this.identificadorServidor = this.host + ":" + this.puerto;
        } else {
            this.identificadorServidor = identificadorServidor;
        }
        this.puertoP2P = puertoP2P > 0 ? puertoP2P : this.puerto + 100;
        this.vecinosP2P = vecinos != null ? new HashSet<>(vecinos) : new HashSet<>();
        this.tablaARPServidores = new TablaARPServidores(this.identificadorServidor);
        this.gestorServidores = new GestorServidores(this, this.tablaARPServidores, this.identificadorServidor, this.puertoP2P);
        for (String usuario : obtenerUsuariosLocalesAutenticados()) {
            this.tablaARPServidores.registrarUsuarioLocal(usuario);
        }
    }

    public boolean conectarServidorP2P(String hostDestino, int puertoDestino) {
        if (gestorServidores == null) {
            System.err.println("Gestor de servidores no inicializado; conexión P2P manual no disponible");
            return false;
        }

        if (hostDestino == null || hostDestino.isBlank()) {
            System.err.println("Host de destino inválido para conexión P2P manual");
            return false;
        }

        if (puertoDestino <= 0 || puertoDestino > 65535) {
            System.err.println("Puerto de destino inválido para conexión P2P manual: " + puertoDestino);
            return false;
        }

        InetSocketAddress destino = new InetSocketAddress(hostDestino.trim(), puertoDestino);

        boolean conectado = gestorServidores.conectarManual(destino);
        if (conectado) {
            vecinosP2P.add(destino);
            registrarEvento("Enlace P2P manual iniciado con " + destino);
        }
        return conectado;
    }

    public int getMaxUsuariosConectados() {
        return maxUsuariosConectados;
    }

    public String getHost() {
        return host;
    }

    public int getPuerto() {
        return puerto;
    }

    public int getPuertoP2P() {
        return puertoP2P;
    }

    public List<ConexionRemotaInfo> obtenerConexionesP2PActivas() {
        if (gestorServidores == null) {
            return Collections.emptyList();
        }
        return gestorServidores.obtenerConexionesActivas().stream()
            .map(conexion -> {
                InetSocketAddress direccion = conexion.getDireccionRemota();
                String hostRemoto = direccion != null ? direccion.getHostString() : "desconocido";
                int puertoRemoto = direccion != null ? direccion.getPort() : -1;
                String servidorIdRemoto = conexion.getServidorId() != null ? conexion.getServidorId() : hostRemoto + ":" + puertoRemoto;
                return new ConexionRemotaInfo(servidorIdRemoto, hostRemoto, puertoRemoto, conexion.getConectadoDesde());
            })
            .collect(Collectors.toList());
    }

    public List<UsuarioRemotoInfo> obtenerUsuariosRemotos() {
        if (tablaARPServidores == null) {
            return Collections.emptyList();
        }

        Map<String, String> snapshot = tablaARPServidores.snapshot();
        List<UsuarioRemotoInfo> usuarios = new ArrayList<>();

        for (Map.Entry<String, String> entry : snapshot.entrySet()) {
            String username = entry.getKey();
            String servidorIdUsuario = entry.getValue();

            if (servidorIdUsuario == null || servidorIdUsuario.isBlank()) {
                continue;
            }

            if (identificadorServidor.equals(servidorIdUsuario)) {
                continue; // Es local
            }

            String hostRemoto = servidorIdUsuario;
            int puertoRemoto = -1;

            int idx = servidorIdUsuario.lastIndexOf(':');
            if (idx > 0 && idx < servidorIdUsuario.length() - 1) {
                hostRemoto = servidorIdUsuario.substring(0, idx);
                try {
                    puertoRemoto = Integer.parseInt(servidorIdUsuario.substring(idx + 1));
                } catch (NumberFormatException ignored) {
                    puertoRemoto = -1;
                }
            }

            usuarios.add(new UsuarioRemotoInfo(username, servidorIdUsuario, hostRemoto, puertoRemoto));
        }

        usuarios.sort(Comparator.comparing(UsuarioRemotoInfo::getServidorId).thenComparing(UsuarioRemotoInfo::getUsername));
        return usuarios;
    }

    public List<String> obtenerUsuariosLocalesAutenticados() {
        synchronized (clientesConectados) {
            return clientesConectados.stream()
                .filter(ManejadorCliente::isAutenticado)
                .map(ManejadorCliente::getUsername)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        }
    }

    private boolean hayCapacidadParaNuevoCliente() {
        if (maxUsuariosConectados <= 0) {
            return true;
        }
        synchronized (clientesConectados) {
            return clientesConectados.size() < maxUsuariosConectados;
        }
    }

    private int registrarCliente(ManejadorCliente cliente) {
        synchronized (clientesConectados) {
            if (maxUsuariosConectados > 0 && clientesConectados.size() >= maxUsuariosConectados) {
                return -1;
            }
            cliente.agregarObservador(this);
            clientesConectados.add(cliente);
            return clientesConectados.size();
        }
    }

    public void removerCliente(ManejadorCliente cliente) {
        int totalRestante = -1;
        synchronized (clientesConectados) {
            if (clientesConectados.remove(cliente)) {
                cliente.removerObservador(this);
                totalRestante = clientesConectados.size();
            }
        }

        if (totalRestante >= 0) {
            System.out.println("Cliente removido. Total clientes activos: " + totalRestante);
            if (gui != null) {
                String ocupacion = maxUsuariosConectados > 0
                    ? totalRestante + "/" + maxUsuariosConectados
                    : String.valueOf(totalRestante);
                gui.agregarLog("Cliente desconectado. Total activos: " + ocupacion);
                gui.actualizarTabla();
            }

            if (cliente.isAutenticado()) {
                registrarDesconexionLocal(cliente.getUsername());
                notificarActualizacionUsuarios();
            }
        }
    }

    private void rechazarConexion(Socket socketCliente) {
        String ip = socketCliente.getInetAddress().getHostAddress();
        try {
            socketCliente.close();
        } catch (IOException e) {
            System.err.println("Error al rechazar conexión desde " + ip + ": " + e.getMessage());
        }

        System.out.println("Conexión rechazada desde " + ip + " (límite de " + maxUsuariosConectados + " clientes)");
        if (gui != null) {
            gui.agregarLog("Conexión rechazada (límite alcanzado) desde " + ip);
        }
    }

    @Override
    public void actualizar(EventoChat evento) {
        if (evento == null) {
            return;
        }

        switch (evento.getTipo()) {
            case USUARIO_CONECTADO:
                manejarEventoUsuarioConectado(evento);
                break;
            case USUARIO_DESCONECTADO:
                manejarEventoUsuarioDesconectado(evento);
                break;
            case MENSAJE_ENVIADO:
                manejarEventoMensajeEnviado(evento);
                break;
            case BROADCAST_MENSAJE:
                manejarEventoBroadcast(evento);
                break;
            case AUDIO_RECIBIDO:
                manejarEventoAudioPrivado(evento);
                break;
            case AUDIO_GRUPO_RECIBIDO:
                manejarEventoAudioGrupo(evento);
                break;
            default:
                registrarEvento("Evento recibido: " + evento.getTipo());
        }
    }

    private void manejarEventoUsuarioConectado(EventoChat evento) {
        Map<String, Object> datos = extraerDatos(evento);
        String usuario = (String) datos.getOrDefault("usuario", "Desconocido");
        String ip = (String) datos.getOrDefault("ip", "");
        registrarConexionLocal(usuario);
        registrarEvento("Cliente autenticado: " + usuario + (ip.isEmpty() ? "" : " desde " + ip));
    }

    private void manejarEventoUsuarioDesconectado(EventoChat evento) {
        Map<String, Object> datos = extraerDatos(evento);
        String usuario = (String) datos.getOrDefault("usuario", "Desconocido");
        String motivo = (String) datos.getOrDefault("evento", "DESCONEXION");
        registrarDesconexionLocal(usuario);
        registrarEvento("Cliente desconectado: " + usuario + " (" + motivo + ")");
    }

    private void manejarEventoMensajeEnviado(EventoChat evento) {
        Map<String, Object> datos = extraerDatos(evento);
        String remitente = (String) datos.getOrDefault("remitente", "?");
        String destinatario = (String) datos.getOrDefault("destinatario", "?");
        registrarEvento("Mensaje privado de " + remitente + " para " + destinatario);
    }

    private void manejarEventoBroadcast(EventoChat evento) {
        Map<String, Object> datos = extraerDatos(evento);
        String remitente = (String) datos.getOrDefault("remitente", "?");
        Long canalId = (Long) datos.getOrDefault("canalId", -1L);
        registrarEvento("Mensaje grupal de " + remitente + " en canal " + canalId);
    }

    private void manejarEventoAudioPrivado(EventoChat evento) {
        Map<String, Object> datos = extraerDatos(evento);
        String remitente = (String) datos.getOrDefault("remitente", "?");
        String destinatario = (String) datos.getOrDefault("destinatario", "?");
        registrarEvento("Audio privado de " + remitente + " para " + destinatario);
    }

    private void manejarEventoAudioGrupo(EventoChat evento) {
        Map<String, Object> datos = extraerDatos(evento);
        String remitente = (String) datos.getOrDefault("remitente", "?");
        Long canalId = (Long) datos.getOrDefault("canalId", -1L);
        registrarEvento("Audio grupal de " + remitente + " en canal " + canalId);
    }

    private Map<String, Object> extraerDatos(EventoChat evento) {
        Object datos = evento.getDatos();
        if (datos instanceof Map<?, ?>) {
            @SuppressWarnings("unchecked")
            Map<String, Object> casted = (Map<String, Object>) datos;
            return casted;
        }
        return Collections.emptyMap();
    }

    private void registrarEvento(String mensaje) {
        System.out.println(mensaje);
        if (gui != null) {
            gui.agregarLog(mensaje);
        }
    }

    private void registrarConexionLocal(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (tablaARPServidores != null) {
            tablaARPServidores.registrarUsuarioLocal(username);
        }
        if (gestorServidores != null) {
            gestorServidores.difundirUsuarioConectado(username);
        }
    }

    private void registrarDesconexionLocal(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (tablaARPServidores != null && tablaARPServidores.esUsuarioLocal(username)) {
            tablaARPServidores.eliminarUsuario(username);
        }
        if (gestorServidores != null) {
            gestorServidores.difundirUsuarioDesconectado(username);
        }
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
        if (entregarMensajeLocal(remitenteUsername, destinatarioUsername, contenido)) {
            return;
        }

        if (tablaARPServidores != null && gestorServidores != null) {
            String servidorDestino = tablaARPServidores.obtenerServidorDe(destinatarioUsername);
            if (servidorDestino != null && !tablaARPServidores.getServidorLocalId().equals(servidorDestino)) {
                if (gestorServidores.reenviarMensajePrivado(remitenteUsername, destinatarioUsername, contenido)) {
                    return;
                }
            }
        }

        System.err.println("Usuario destinatario no encontrado: " + destinatarioUsername);
    }

    public boolean entregarMensajeLocal(String remitenteUsername, String destinatarioUsername, String contenido) {
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
            return true;
        }
        return false;
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
        if (entregarAudioLocal(remitenteUsername, destinatarioUsername, contenidoAudio, formato, duracionSegundos)) {
            return;
        }

        if (tablaARPServidores != null && gestorServidores != null) {
            String servidorDestino = tablaARPServidores.obtenerServidorDe(destinatarioUsername);
            if (servidorDestino != null && !tablaARPServidores.getServidorLocalId().equals(servidorDestino)) {
                if (gestorServidores.reenviarAudioPrivado(remitenteUsername, destinatarioUsername,
                        contenidoAudio, formato, duracionSegundos)) {
                    return;
                }
            }
        }

        System.err.println("Usuario destinatario no encontrado para audio: " + destinatarioUsername);
    }

    public boolean entregarAudioLocal(String remitenteUsername, String destinatarioUsername,
                                      byte[] contenidoAudio, String formato, Long duracionSegundos) {
        if (contenidoAudio == null || contenidoAudio.length == 0) {
            return false;
        }

        ManejadorCliente destinatario = null;

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
            return true;
        }
        return false;
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
        long autenticados = 0;
        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isAutenticado()) {
                    cliente.notificarActualizacionUsuarios();
                    autenticados++;
                }
            }
        }
        System.out.println("Notificación de actualización de usuarios enviada a " + autenticados + " clientes");
        if (gui != null) {
            SwingUtilities.invokeLater(this::actualizarPanelServidor);
        }
    }

    private void actualizarPanelServidor() {
        if (gui != null) {
            gui.actualizarTabla();
        }
    }
    
    /**
     * Enviar notificación de invitación a un usuario
     */
    public void enviarNotificacionInvitacion(String usernameInvitador, String usernameInvitado, 
                                              String nombreCanal, String descripcionCanal, 
                                              byte[] fotoCanal, Long canalId) {
        if (entregarInvitacionLocal(usernameInvitador, usernameInvitado, nombreCanal, descripcionCanal, fotoCanal, canalId)) {
            return;
        }

        if (tablaARPServidores != null && gestorServidores != null) {
            boolean reenviado = gestorServidores.enviarInvitacionGrupo(
                usernameInvitador,
                usernameInvitado,
                nombreCanal,
                descripcionCanal,
                fotoCanal,
                canalId
            );

            if (reenviado) {
                return;
            }
        }

        System.out.println("Usuario no conectado o sin ruta P2P para invitación: " + usernameInvitado);
    }

    public boolean entregarInvitacionLocal(String usernameInvitador, String usernameInvitado,
                                            String nombreCanal, String descripcionCanal,
                                            byte[] fotoCanal, Long canalId) {
        ManejadorCliente destinatario = null;

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
            System.out.println("Invitación entregada de " + usernameInvitador + " a " + usernameInvitado +
                " para el canal: " + nombreCanal);
            return true;
        }

        return false;
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
            this.servicioAutenticacion = new ServicioAutenticacion(conexionDB);
            
            // Inicializar servicio de transcripción de audio
            inicializarServicioTranscripcion();

            if (gestorServidores != null) {
                gestorServidores.iniciar(vecinosP2P);
                System.out.println("Red P2P habilitada. Identificador local: " + identificadorServidor + 
                    " | Puerto P2P: " + puertoP2P + " | Vecinos configurados: " + (vecinosP2P != null ? vecinosP2P.size() : 0));
            }
            
            // Crear socket del servidor con host y puerto específicos
            InetAddress direccion;
            if (host.equals("0.0.0.0")) {
                // Escuchar en todas las interfaces
                serverSocket = new ServerSocket(puerto);
                direccion = InetAddress.getLocalHost();
            } else {
                // Escuchar en una interfaz específica
                direccion = InetAddress.getByName(host);
                serverSocket = new ServerSocket();
                serverSocket.bind(new InetSocketAddress(direccion, puerto));
            }
            
            ejecutando = true;
            
            System.out.println("===========================================");
            System.out.println("   SERVIDOR DE CHAT UNIVERSITARIO");
            System.out.println("===========================================");
            System.out.println("Servidor iniciado en: " + direccion.getHostAddress() + ":" + puerto);
            System.out.println("Esperando conexiones de clientes...");
            System.out.println("===========================================\n");
            
            // Lanzar GUI en el hilo de Swing
            if (gui != null) {
                gui.agregarLog("Servidor iniciado en " + direccion.getHostAddress() + ":" + puerto);
                gui.agregarLog("Esperando conexiones...");
            }
            
            // Aceptar conexiones de clientes
            while (ejecutando) {
                try {
                    Socket socketCliente = serverSocket.accept();

                    if (!hayCapacidadParaNuevoCliente()) {
                        rechazarConexion(socketCliente);
                        continue;
                    }

                    ManejadorCliente manejador = new ManejadorCliente(socketCliente, conexionDB);
                    int totalClientes = registrarCliente(manejador);

                    if (totalClientes == -1) {
                        rechazarConexion(socketCliente);
                        continue;
                    }

                    Thread threadCliente = new Thread(manejador);
                    threadCliente.start();

                    String ip = socketCliente.getInetAddress().getHostAddress();
                    System.out.println("Nuevo cliente conectado desde " + ip + ". Total clientes: " + totalClientes);

                    if (gui != null) {
                        String ocupacion = maxUsuariosConectados > 0
                            ? totalClientes + "/" + maxUsuariosConectados
                            : String.valueOf(totalClientes);
                        gui.agregarLog("Cliente conectado desde " + ip + " (" + ocupacion + ")");
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

    public Usuario crearUsuarioDesdeServidor(String username, String email, String password, String direccionIP, byte[] foto) throws SQLException {
        if (servicioAutenticacion == null) {
            throw new IllegalStateException("Servicio de autenticación no inicializado");
        }
        String ipRegistrada = (direccionIP == null || direccionIP.trim().isEmpty()) ? "CONSOLE" : direccionIP.trim();
        Usuario usuario = servicioAutenticacion.registrar(username, email, password, ipRegistrada, foto);
        registrarEvento("Usuario creado desde panel del servidor: " + username + " (IP: " + ipRegistrada + ")");
        return usuario;
    }
    
    /**
     * Enviar mensaje broadcast a todos los usuarios conectados
     */
    public int enviarMensajeBroadcastUsuarios(String mensaje) {
        String origen = this.identificadorServidor != null ? this.identificadorServidor : (this.host + ":" + this.puerto);
        int usuariosNotificados = entregarBroadcastUsuariosLocal(mensaje, origen);

        if (gestorServidores != null) {
            gestorServidores.difundirBroadcastUsuarios(mensaje, origen);
        }
        
        System.out.println("Mensaje broadcast enviado (origen " + origen + ") a " + usuariosNotificados + " usuarios locales");
        if (gui != null) {
            gui.agregarLog("Broadcast enviado (" + origen + ") a " + usuariosNotificados + " usuarios locales");
        }
        
        return usuariosNotificados;
    }

    public int entregarBroadcastUsuariosLocal(String mensaje, String servidorOrigen) {
        int usuariosNotificados = 0;

        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isAutenticado()) {
                    cliente.recibirNotificacionServidor(mensaje, servidorOrigen);
                    usuariosNotificados++;
                }
            }
        }

        return usuariosNotificados;
    }
    
    /**
     * Enviar mensaje broadcast a todos los canales/grupos
     */
    public int enviarMensajeBroadcastCanales(String mensaje) {
        String servidorOrigen = this.identificadorServidor != null ? this.identificadorServidor : (this.host + ":" + this.puerto);
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
                                cliente.recibirNotificacionServidorGrupo(canal.getId(), canal.getNombre(), mensaje, servidorOrigen);
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
            
            if (gestorServidores != null) {
                gestorServidores.detener();
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
    private static ConfiguracionCargada cargarConfiguracion() {
        Properties props = new Properties();
        ArrayList<File> candidatos = new ArrayList<>();

        File archivoSistema = crearArchivoDesdeRuta(System.getProperty("configFile"));
        if (archivoSistema != null) {
            candidatos.add(archivoSistema);
        }

        File archivoEnv = crearArchivoDesdeRuta(System.getenv("CHAT_SERVER_CONFIG"));
        if (archivoEnv != null) {
            candidatos.add(archivoEnv);
        }

    agregarSiNoNulo(candidatos, crearArchivoDesdeRuta("config.properties"));
    agregarSiNoNulo(candidatos, crearArchivoDesdeRuta("chat-servidor.properties"));
    agregarSiNoNulo(candidatos, crearArchivoDesdeRuta("config" + File.separator + "chat-servidor.properties"));
    agregarSiNoNulo(candidatos, crearArchivoDesdeRuta("config" + File.separator + "config.properties"));

        for (File candidato : candidatos) {
            if (candidato != null && candidato.exists() && candidato.isFile()) {
                try (FileInputStream fis = new FileInputStream(candidato)) {
                    props.load(fis);
                    return new ConfiguracionCargada(props, "file:" + candidato.getAbsolutePath());
                } catch (IOException e) {
                    System.err.println("No se pudo leer configuración desde " + candidato.getAbsolutePath() + ": " + e.getMessage());
                }
            }
        }

    File archivoDev = crearArchivoDesdeRuta("src/main/resources/config.properties");
        if (archivoDev.exists() && archivoDev.isFile()) {
            try (FileInputStream fis = new FileInputStream(archivoDev)) {
                props.load(fis);
                return new ConfiguracionCargada(props, "file:" + archivoDev.getAbsolutePath());
            } catch (IOException e) {
                System.err.println("No se pudo leer configuración desde " + archivoDev.getAbsolutePath() + ": " + e.getMessage());
            }
        }

        try (InputStream recurso = ServidorChat.class.getClassLoader().getResourceAsStream("config.properties")) {
            if (recurso != null) {
                props.load(recurso);
                return new ConfiguracionCargada(props, "classpath:config.properties");
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer configuración desde el classpath: " + e.getMessage());
        }

        return new ConfiguracionCargada(props, "valores por defecto");
    }

    private static int obtenerEntero(Properties config, String clave, int valorPorDefecto) {
        String valor = config.getProperty(clave);
        if (valor == null || valor.isBlank()) {
            return valorPorDefecto;
        }
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            System.err.println("Valor inválido para " + clave + " (" + valor + "): usando " + valorPorDefecto);
            return valorPorDefecto;
        }
    }

    private static final class ConfiguracionCargada {
        private final Properties propiedades;
        private final String origen;

        private ConfiguracionCargada(Properties propiedades, String origen) {
            this.propiedades = propiedades;
            this.origen = origen;
        }
    }

    private static void agregarSiNoNulo(List<File> lista, File archivo) {
        if (archivo != null) {
            lista.add(archivo);
        }
    }

    private static File crearArchivoDesdeRuta(String ruta) {
        if (ruta == null) {
            return null;
        }
        String limpia = ruta.trim();
        if (limpia.isEmpty()) {
            return null;
        }
        if ((limpia.startsWith("\"") && limpia.endsWith("\"")) || (limpia.startsWith("'") && limpia.endsWith("'"))) {
            limpia = limpia.substring(1, limpia.length() - 1);
        }
        limpia = limpia.replace("\"", "").trim();
        if (limpia.isEmpty()) {
            return null;
        }
        return new File(limpia);
    }

    private static Set<InetSocketAddress> parseServidoresVecinos(String peersConfig, int puertoPorDefecto) {
        Set<InetSocketAddress> vecinos = new HashSet<>();
        if (peersConfig == null || peersConfig.isBlank()) {
            return vecinos;
        }

        String[] entradas = peersConfig.split("[,;\\s]+");
        for (String entrada : entradas) {
            if (entrada == null || entrada.isBlank()) {
                continue;
            }
            String[] partes = entrada.split(":");
            String hostConfig = partes[0].trim();
            if (hostConfig.isEmpty()) {
                continue;
            }
            int puertoConfig = puertoPorDefecto;
            if (partes.length > 1) {
                try {
                    puertoConfig = Integer.parseInt(partes[1].trim());
                } catch (NumberFormatException e) {
                    System.err.println("Puerto P2P inválido en entrada '" + entrada + "': " + e.getMessage());
                    continue;
                }
            }
            vecinos.add(new InetSocketAddress(hostConfig, puertoConfig));
        }

        return vecinos;
    }

    public static void main(String[] args) {
        ConfiguracionCargada configuracion = cargarConfiguracion();
        Properties config = configuracion.propiedades;

        // Leer configuración del servidor
        String host = config.getProperty("server.host", HOST_DEFAULT);
        int puerto = obtenerEntero(config, "server.port", PUERTO_DEFAULT);
        int maxUsuarios = obtenerEntero(config, "server.max.usuarios.conectados", MAX_USUARIOS_DEFAULT);
        int puertoP2P = obtenerEntero(config, "server.p2p.port", puerto + 100);
        String identificadorServidor = config.getProperty("server.id", host + ":" + puerto).trim();
        if (identificadorServidor.isEmpty()) {
            identificadorServidor = host + ":" + puerto;
        }
        String peersConfig = config.getProperty("server.peers", "");
        Set<InetSocketAddress> vecinosConfigurados = parseServidoresVecinos(peersConfig, puertoP2P);

        System.out.println("Configuración cargada desde: " + configuracion.origen);
        System.out.println("  Host: " + host);
        System.out.println("  Puerto: " + puerto);
        System.out.println("  Máximo usuarios conectados: " + (maxUsuarios <= 0 ? "Sin límite" : maxUsuarios));
        System.out.println("  Id Servidor: " + identificadorServidor);
        System.out.println("  Puerto P2P: " + puertoP2P);
        System.out.println("  Vecinos P2P: " + (vecinosConfigurados.isEmpty() ? "Ninguno" : vecinosConfigurados));
        System.out.println();

        // Crear instancia del servidor con la configuración
        ServidorChat servidor = new ServidorChat(host, puerto);
        servidor.setMaxUsuariosConectados(maxUsuarios);
        servidor.configurarRedServidores(identificadorServidor, puertoP2P, vecinosConfigurados);
        
        // Configurar Look and Feel
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("No se pudo establecer el Look and Feel del sistema");
            }
            
            // Crear y mostrar GUI
            servidor.gui = new ServidorFrame(servidor, servidor.clientesConectados);
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
