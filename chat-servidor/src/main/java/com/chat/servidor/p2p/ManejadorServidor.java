package com.chat.servidor.p2p;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Atiende la conexión con otro servidor en la red P2P.
 */
class ManejadorServidor implements Runnable {

    private final GestorServidores gestor;
    private final Socket socket;
    private final boolean iniciador;
    private final AtomicBoolean activo;
    private final long instanteConexion;

    private ObjectOutputStream salida;
    private ObjectInputStream entrada;
    private volatile String servidorRemotoId;

    ManejadorServidor(GestorServidores gestor, Socket socket, boolean iniciador) {
        this.gestor = gestor;
        this.socket = socket;
        this.iniciador = iniciador;
        this.activo = new AtomicBoolean(true);
        this.instanteConexion = System.currentTimeMillis();
    }

    @Override
    public void run() {
        try {
            salida = new ObjectOutputStream(socket.getOutputStream());
            salida.flush();
            entrada = new ObjectInputStream(socket.getInputStream());

            if (iniciador) {
                gestor.enviarHolaInicial(this);
            }

            while (activo.get()) {
                Object dato = entrada.readObject();
                if (dato instanceof GestorServidores.MensajeP2P) {
                    gestor.manejarMensaje(this, (GestorServidores.MensajeP2P) dato);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            if (activo.get()) {
                gestor.notificarFalloConexion(this, e);
            }
        } finally {
            cerrarSilencioso();
        }
    }

    synchronized void enviarMensaje(GestorServidores.MensajeP2P mensaje) throws IOException {
        if (!activo.get()) {
            throw new IOException("La conexión con el servidor remoto está cerrada");
        }
        salida.writeObject(mensaje);
        salida.flush();
    }

    void cerrarSilencioso() {
        if (!activo.getAndSet(false)) {
            return;
        }
        try {
            if (entrada != null) {
                entrada.close();
            }
        } catch (IOException ignored) {
        }
        try {
            if (salida != null) {
                salida.close();
            }
        } catch (IOException ignored) {
        }
        try {
            socket.close();
        } catch (IOException ignored) {
        }
    }

    String getServidorRemotoId() {
        return servidorRemotoId;
    }

    void setServidorRemotoId(String servidorRemotoId) {
        this.servidorRemotoId = servidorRemotoId;
    }

    InetSocketAddress getDireccionRemota() {
        SocketAddress remoto = socket.getRemoteSocketAddress();
        return remoto instanceof InetSocketAddress ? (InetSocketAddress) remoto : null;
    }

    long getInstanteConexion() {
        return instanteConexion;
    }
}
