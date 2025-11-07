package com.chat.servidor.p2p;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.chat.servidor.presentacion.ServidorChat;

/**
 * Administra las conexiones P2P entre servidores, sincronizando la tabla ARP
 * y reenviando mensajes entre instancias.
 */
public class GestorServidores {

    private final ServidorChat servidorChat;
    private final TablaARPServidores tablaARP;
    private final String servidorId;
    private final int puertoLocal;

    private final Map<String, ManejadorServidor> conexionesActivas;
    private final Set<InetSocketAddress> vecinosConfigurados;

    private volatile boolean ejecutando;
    private ServerSocket socketServidor;

    public GestorServidores(ServidorChat servidorChat, TablaARPServidores tablaARP, String servidorId, int puertoLocal) {
        this.servidorChat = Objects.requireNonNull(servidorChat, "servidorChat");
        this.tablaARP = Objects.requireNonNull(tablaARP, "tablaARP");
        this.servidorId = Objects.requireNonNull(servidorId, "servidorId");
        this.puertoLocal = puertoLocal;
        this.conexionesActivas = new ConcurrentHashMap<>();
    this.vecinosConfigurados = ConcurrentHashMap.newKeySet();
    }

    public void iniciar(Set<InetSocketAddress> vecinosIniciales) {
        if (ejecutando) {
            return;
        }
        ejecutando = true;
        if (vecinosIniciales != null) {
            vecinosConfigurados.addAll(vecinosIniciales);
        }
        iniciarAceptador();
        conectarAVecinos();
    }

    public void detener() {
        ejecutando = false;
        if (socketServidor != null) {
            try {
                socketServidor.close();
            } catch (IOException e) {
                System.err.println("Error al cerrar servidor P2P: " + e.getMessage());
            }
        }
        for (ManejadorServidor manejador : new ArrayList<>(conexionesActivas.values())) {
            manejador.cerrarSilencioso();
        }
        conexionesActivas.clear();
    }

    public void difundirUsuarioConectado(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("username", username);
        datos.put("servidorId", tablaARP.getServidorLocalId());
        MensajeP2P mensaje = new MensajeP2P(MensajeP2P.Tipo.USUARIO_CONECTADO, datos);
        broadcast(mensaje);
    }

    public void difundirUsuarioDesconectado(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("username", username);
        datos.put("servidorId", tablaARP.getServidorLocalId());
        MensajeP2P mensaje = new MensajeP2P(MensajeP2P.Tipo.USUARIO_DESCONECTADO, datos);
        broadcast(mensaje);
    }

    public boolean reenviarMensajePrivado(String remitente, String destinatario, String contenido) {
        if (destinatario == null || destinatario.isBlank()) {
            return false;
        }
        String servidorDestino = tablaARP.obtenerServidorDe(destinatario);
        if (servidorDestino == null || servidorDestino.isBlank()) {
            return false;
        }
        if (tablaARP.getServidorLocalId().equals(servidorDestino)) {
            return servidorChat.entregarMensajeLocal(remitente, destinatario, contenido);
        }
        ManejadorServidor manejador = conexionesActivas.get(servidorDestino);
        if (manejador == null) {
            System.err.println("No hay conexión activa con servidor destino " + servidorDestino + " para usuario " + destinatario);
            return false;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("remitente", remitente);
        datos.put("destinatario", destinatario);
    datos.put("contenido", contenido);
        try {
            manejador.enviarMensaje(new MensajeP2P(MensajeP2P.Tipo.MENSAJE_PRIVADO, datos));
            return true;
        } catch (IOException e) {
            System.err.println("Error al enviar mensaje privado al servidor " + servidorDestino + ": " + e.getMessage());
            manejarDesconexion(manejador);
            return false;
        }
    }

    public boolean reenviarAudioPrivado(String remitente, String destinatario, byte[] contenidoAudio,
                                        String formato, Long duracionSegundos) {
        if (destinatario == null || destinatario.isBlank()) {
            return false;
        }
        String servidorDestino = tablaARP.obtenerServidorDe(destinatario);
        if (servidorDestino == null || servidorDestino.isBlank()) {
            return false;
        }
        if (tablaARP.getServidorLocalId().equals(servidorDestino)) {
            return servidorChat.entregarAudioLocal(remitente, destinatario, contenidoAudio, formato, duracionSegundos);
        }
        ManejadorServidor manejador = conexionesActivas.get(servidorDestino);
        if (manejador == null) {
            System.err.println("No hay conexión activa con servidor destino " + servidorDestino + " para audio de " + destinatario);
            return false;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("remitente", remitente);
        datos.put("destinatario", destinatario);
        datos.put("contenidoAudio", contenidoAudio);
        datos.put("formato", formato);
        datos.put("duracionSegundos", duracionSegundos);
        try {
            manejador.enviarMensaje(new MensajeP2P(MensajeP2P.Tipo.AUDIO_PRIVADO, datos));
            return true;
        } catch (IOException e) {
            System.err.println("Error al reenviar audio privado al servidor " + servidorDestino + ": " + e.getMessage());
            manejarDesconexion(manejador);
            return false;
        }
    }

    void manejarMensaje(ManejadorServidor origen, MensajeP2P mensaje) {
        switch (mensaje.getTipo()) {
            case HELLO:
                procesarHola(origen, mensaje);
                break;
            case HELLO_ACK:
                procesarHolaAck(origen, mensaje);
                break;
            case USUARIO_CONECTADO:
                procesarUsuarioConectado(origen, mensaje);
                break;
            case USUARIO_DESCONECTADO:
                procesarUsuarioDesconectado(origen, mensaje);
                break;
            case MENSAJE_PRIVADO:
                procesarMensajePrivado(origen, mensaje);
                break;
            case AUDIO_PRIVADO:
                procesarAudioPrivado(origen, mensaje);
                break;
            case INVITACION_GRUPO:
                procesarInvitacionGrupo(origen, mensaje);
                break;
            case BROADCAST_USUARIOS:
                procesarBroadcastUsuarios(origen, mensaje);
                break;
            default:
                System.out.println("Tipo de mensaje P2P no soportado: " + mensaje.getTipo());
        }
    }

    public void difundirBroadcastUsuarios(String mensaje, String servidorOrigen) {
        if (mensaje == null || mensaje.isBlank()) {
            return;
        }
        Map<String, Object> datos = new HashMap<>();
        datos.put("mensaje", mensaje);
        datos.put("servidorOrigen", servidorOrigen != null ? servidorOrigen : servidorId);
        broadcast(new MensajeP2P(MensajeP2P.Tipo.BROADCAST_USUARIOS, datos));
    }

    public boolean enviarInvitacionGrupo(String usernameInvitador, String usernameInvitado,
                                         String nombreCanal, String descripcionCanal,
                                         byte[] fotoCanal, Long canalId) {
        if (usernameInvitado == null || usernameInvitado.isBlank()) {
            return false;
        }

        String servidorDestino = tablaARP.obtenerServidorDe(usernameInvitado);
        if (servidorDestino == null || servidorDestino.isBlank()) {
            return false;
        }

        if (tablaARP.getServidorLocalId().equals(servidorDestino)) {
            return servidorChat.entregarInvitacionLocal(
                usernameInvitador,
                usernameInvitado,
                nombreCanal,
                descripcionCanal,
                fotoCanal,
                canalId
            );
        }

        ManejadorServidor manejador = conexionesActivas.get(servidorDestino);
        if (manejador == null) {
            System.err.println("No hay conexión activa con servidor destino " + servidorDestino +
                " para invitación de " + usernameInvitado);
            return false;
        }

        Map<String, Object> datos = new HashMap<>();
        datos.put("usernameInvitador", usernameInvitador);
        datos.put("usernameInvitado", usernameInvitado);
        datos.put("nombreCanal", nombreCanal);
        datos.put("descripcionCanal", descripcionCanal);
        datos.put("fotoCanal", fotoCanal);
        datos.put("canalId", canalId);

        try {
            manejador.enviarMensaje(new MensajeP2P(MensajeP2P.Tipo.INVITACION_GRUPO, datos));
            System.out.println("Invitación a canal reenviada a servidor " + servidorDestino +
                " para usuario " + usernameInvitado);
            return true;
        } catch (IOException e) {
            System.err.println("Error al reenviar invitación a servidor " + servidorDestino +
                ": " + e.getMessage());
            manejarDesconexion(manejador);
            return false;
        }
    }

    private void procesarInvitacionGrupo(ManejadorServidor origen, MensajeP2P mensaje) {
        String usernameInvitado = mensaje.getString("usernameInvitado");
        String usernameInvitador = mensaje.getString("usernameInvitador");
        String nombreCanal = mensaje.getString("nombreCanal");
        String descripcionCanal = mensaje.getString("descripcionCanal");
        byte[] fotoCanal = mensaje.getBytes("fotoCanal");
        Long canalId = mensaje.getLong("canalId");

        boolean entregado = servidorChat.entregarInvitacionLocal(
            usernameInvitador,
            usernameInvitado,
            nombreCanal,
            descripcionCanal,
            fotoCanal,
            canalId
        );

        if (!entregado) {
            System.out.println("Usuario " + usernameInvitado +
                " no está conectado en este servidor para recibir invitación al canal " + nombreCanal);
        }
    }

    private void procesarBroadcastUsuarios(ManejadorServidor origen, MensajeP2P mensaje) {
        String texto = mensaje.getString("mensaje");
        String origenServidor = mensaje.getString("servidorOrigen");
        if (texto == null || texto.isBlank()) {
            return;
        }

        int entregados = servidorChat.entregarBroadcastUsuariosLocal(texto, origenServidor);
        System.out.println("Broadcast recibido de " + (origenServidor != null ? origenServidor : origen.getServidorRemotoId()) +
            " entregado a " + entregados + " usuarios locales");
    }

    void notificarFalloConexion(ManejadorServidor manejador, Exception causa) {
        if (causa != null) {
            System.err.println("Conexión P2P interrumpida: " + causa.getMessage());
        }
        manejarDesconexion(manejador);
    }

    void manejarDesconexion(ManejadorServidor manejador) {
        String remotoId = manejador.getServidorRemotoId();
        if (remotoId != null) {
            conexionesActivas.remove(remotoId, manejador);
            tablaARP.eliminarUsuariosDeServidor(remotoId);
            servidorChat.notificarActualizacionUsuarios();
            System.out.println("Servidor remoto desconectado: " + remotoId);
        }
        manejador.cerrarSilencioso();
    }

    void registrarConexion(String servidorRemotoId, ManejadorServidor manejador) {
        if (servidorRemotoId == null || servidorRemotoId.isBlank()) {
            return;
        }
        manejador.setServidorRemotoId(servidorRemotoId);
        ManejadorServidor previo = conexionesActivas.put(servidorRemotoId, manejador);
        if (previo != null && previo != manejador) {
            previo.cerrarSilencioso();
        }
        System.out.println("Servidor P2P conectado: " + servidorRemotoId + " (" + conexionesActivas.size() + " enlaces)");
    }

    void enviarHolaInicial(ManejadorServidor manejador) {
        Map<String, Object> datos = new HashMap<>();
        datos.put("servidorId", servidorId);
        datos.put("usuarios", servidorChat.obtenerUsuariosLocalesAutenticados());
        try {
            manejador.enviarMensaje(new MensajeP2P(MensajeP2P.Tipo.HELLO, datos));
        } catch (IOException e) {
            System.err.println("No se pudo enviar HELLO inicial al servidor remoto: " + e.getMessage());
            manejarDesconexion(manejador);
        }
    }

    private void procesarHola(ManejadorServidor origen, MensajeP2P mensaje) {
        String remotoId = mensaje.getString("servidorId");
        List<String> usuarios = mensaje.getStringList("usuarios");
        registrarConexion(remotoId, origen);
        if (remotoId != null) {
            tablaARP.registrarUsuariosRemotos(remotoId, usuarios);
        }
        servidorChat.notificarActualizacionUsuarios();

        Map<String, Object> datos = new HashMap<>();
        datos.put("servidorId", servidorId);
        datos.put("usuarios", servidorChat.obtenerUsuariosLocalesAutenticados());
        try {
            origen.enviarMensaje(new MensajeP2P(MensajeP2P.Tipo.HELLO_ACK, datos));
        } catch (IOException e) {
            System.err.println("Error al responder HELLO a " + remotoId + ": " + e.getMessage());
            manejarDesconexion(origen);
        }
    }

    private void procesarHolaAck(ManejadorServidor origen, MensajeP2P mensaje) {
        String remotoId = mensaje.getString("servidorId");
        List<String> usuarios = mensaje.getStringList("usuarios");
        registrarConexion(remotoId, origen);
        if (remotoId != null) {
            tablaARP.registrarUsuariosRemotos(remotoId, usuarios);
        }
        servidorChat.notificarActualizacionUsuarios();
    }

    private void procesarUsuarioConectado(ManejadorServidor origen, MensajeP2P mensaje) {
        String remotoId = mensaje.getString("servidorId");
        if (remotoId == null) {
            remotoId = origen.getServidorRemotoId();
        }
        String username = mensaje.getString("username");
        if (username != null && remotoId != null) {
            tablaARP.registrarUsuarioRemoto(username, remotoId);
            servidorChat.notificarActualizacionUsuarios();
        }
    }

    private void procesarUsuarioDesconectado(ManejadorServidor origen, MensajeP2P mensaje) {
        String remotoId = mensaje.getString("servidorId");
        if (remotoId == null) {
            remotoId = origen.getServidorRemotoId();
        }
        String username = mensaje.getString("username");
        if (username != null && remotoId != null) {
            String actual = tablaARP.obtenerServidorDe(username);
            if (remotoId.equals(actual)) {
                tablaARP.eliminarUsuario(username);
                servidorChat.notificarActualizacionUsuarios();
            }
        }
    }

    private void procesarMensajePrivado(ManejadorServidor origen, MensajeP2P mensaje) {
        String destinatario = mensaje.getString("destinatario");
        String remitente = mensaje.getString("remitente");
        String contenido = mensaje.getString("contenido");
        if (!servidorChat.entregarMensajeLocal(remitente, destinatario, contenido)) {
            String remoto = origen.getServidorRemotoId();
            System.err.println("No se pudo entregar mensaje remoto desde " + remoto + " para " + destinatario + "); usuario no encontrado en este nodo");
        }
    }

    private void procesarAudioPrivado(ManejadorServidor origen, MensajeP2P mensaje) {
        String destinatario = mensaje.getString("destinatario");
        String remitente = mensaje.getString("remitente");
        byte[] contenidoAudio = mensaje.getBytes("contenidoAudio");
        String formato = mensaje.getString("formato");
        Long duracion = mensaje.getLong("duracionSegundos");
        if (!servidorChat.entregarAudioLocal(remitente, destinatario, contenidoAudio, formato, duracion)) {
            String remoto = origen.getServidorRemotoId();
            System.err.println("No se pudo entregar audio remoto desde " + remoto + " para " + destinatario + "); usuario no encontrado en este nodo");
        }
    }

    private void iniciarAceptador() {
        Thread hilo = new Thread(() -> {
            try (ServerSocket server = new ServerSocket(puertoLocal)) {
                socketServidor = server;
                System.out.println("Gestor P2P escuchando en puerto " + puertoLocal);
                while (ejecutando) {
                    try {
                        Socket socket = server.accept();
                        crearManejador(socket, false);
                    } catch (IOException e) {
                        if (ejecutando) {
                            System.err.println("Error aceptando conexión P2P: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                System.err.println("No se pudo iniciar el servidor P2P en puerto " + puertoLocal + ": " + e.getMessage());
            }
        }, "p2p-acceptor" + puertoLocal);
        hilo.setDaemon(true);
        hilo.start();
    }

    private void conectarAVecinos() {
        for (InetSocketAddress vecino : new HashSet<>(vecinosConfigurados)) {
            if (esDireccionLocal(vecino)) {
                continue;
            }
            new Thread(() -> {
                try {
                    Socket socket = new Socket();
                    socket.connect(vecino);
                    crearManejador(socket, true);
                } catch (IOException e) {
                    System.err.println("No se pudo conectar al servidor vecino " + vecino + ": " + e.getMessage());
                }
            }, "p2p-connector-" + vecino).start();
        }
    }

    public boolean conectarManual(InetSocketAddress destino) {
        if (destino == null) {
            return false;
        }

        if (!ejecutando) {
            System.err.println("Gestor P2P no está activo, no se puede conectar a " + destino);
            return false;
        }

        if (esDireccionLocal(destino)) {
            System.err.println("Se ignoró la conexión manual hacia la propia dirección " + destino);
            return false;
        }

        vecinosConfigurados.add(destino);

        try {
            Socket socket = new Socket();
            socket.connect(destino, 4000);
            crearManejador(socket, true);
            System.out.println("Conexión P2P manual iniciada hacia " + destino);
            return true;
        } catch (IOException e) {
            vecinosConfigurados.remove(destino);
            System.err.println("No se pudo establecer conexión manual con " + destino + ": " + e.getMessage());
            return false;
        }
    }

    private boolean esDireccionLocal(InetSocketAddress vecino) {
        return vecino.getPort() == puertoLocal;
    }

    private void crearManejador(Socket socket, boolean iniciador) {
    ManejadorServidor manejador = new ManejadorServidor(this, socket, iniciador);
    Thread hilo = new Thread(manejador, "p2p-handler-" + socket.getRemoteSocketAddress());
        hilo.setDaemon(true);
        hilo.start();
    }

    private void broadcast(MensajeP2P mensaje) {
        for (ManejadorServidor manejador : new ArrayList<>(conexionesActivas.values())) {
            try {
                manejador.enviarMensaje(mensaje);
            } catch (IOException e) {
                manejarDesconexion(manejador);
            }
        }
    }

    public List<ConexionRemota> obtenerConexionesActivas() {
        List<ConexionRemota> activas = new ArrayList<>();
        for (Map.Entry<String, ManejadorServidor> entry : conexionesActivas.entrySet()) {
            ManejadorServidor manejador = entry.getValue();
            if (manejador == null) {
                continue;
            }
            activas.add(new ConexionRemota(entry.getKey(), manejador.getDireccionRemota(), manejador.getInstanteConexion()));
        }
        return activas;
    }

    public static class ConexionRemota {
        private final String servidorId;
        private final InetSocketAddress direccionRemota;
        private final long conectadoDesde;

        ConexionRemota(String servidorId, InetSocketAddress direccionRemota, long conectadoDesde) {
            this.servidorId = servidorId;
            this.direccionRemota = direccionRemota;
            this.conectadoDesde = conectadoDesde;
        }

        public String getServidorId() {
            return servidorId;
        }

        public InetSocketAddress getDireccionRemota() {
            return direccionRemota;
        }

        public long getConectadoDesde() {
            return conectadoDesde;
        }
    }

    /**
     * Mensaje serializado utilizado entre servidores P2P.
     */
    static class MensajeP2P implements Serializable {
        private static final long serialVersionUID = 1L;

        enum Tipo {
            HELLO,
            HELLO_ACK,
            USUARIO_CONECTADO,
            USUARIO_DESCONECTADO,
            MENSAJE_PRIVADO,
            AUDIO_PRIVADO,
            INVITACION_GRUPO,
            BROADCAST_USUARIOS
        }

        private final Tipo tipo;
        private final Map<String, Object> payload;

        MensajeP2P(Tipo tipo, Map<String, Object> payload) {
            this.tipo = Objects.requireNonNull(tipo, "tipo");
            this.payload = payload == null ? Collections.emptyMap() : new HashMap<>(payload);
        }

        public Tipo getTipo() {
            return tipo;
        }

        public Map<String, Object> getPayload() {
            return Collections.unmodifiableMap(payload);
        }

        String getString(String clave) {
            Object valor = payload.get(clave);
            return valor instanceof String ? (String) valor : null;
        }

        List<String> getStringList(String clave) {
            Object valor = payload.get(clave);
            if (valor instanceof List<?>) {
                return ((List<?>) valor).stream()
                    .filter(String.class::isInstance)
                    .map(String.class::cast)
                    .collect(Collectors.toList());
            }
            return Collections.emptyList();
        }

        byte[] getBytes(String clave) {
            Object valor = payload.get(clave);
            return valor instanceof byte[] ? (byte[]) valor : null;
        }

        Long getLong(String clave) {
            Object valor = payload.get(clave);
            if (valor instanceof Long) {
                return (Long) valor;
            }
            if (valor instanceof Number) {
                return ((Number) valor).longValue();
            }
            return null;
        }
    }
}
