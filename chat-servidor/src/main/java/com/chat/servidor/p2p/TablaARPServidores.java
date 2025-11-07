package com.chat.servidor.p2p;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tabla ARP lógica que asocia usuarios con el servidor donde están conectados.
 * Permite consultar rápidamente la ubicación de un cliente dentro de la red P2P.
 */
public class TablaARPServidores {

    private final ConcurrentHashMap<String, String> usuariosPorServidor;
    private final String servidorLocalId;

    public TablaARPServidores(String servidorLocalId) {
        this.servidorLocalId = Objects.requireNonNull(servidorLocalId, "El identificador del servidor local es obligatorio");
        this.usuariosPorServidor = new ConcurrentHashMap<>();
    }

    public String getServidorLocalId() {
        return servidorLocalId;
    }

    public void registrarUsuarioLocal(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        usuariosPorServidor.put(username, servidorLocalId);
    }

    public void registrarUsuarioRemoto(String username, String servidorId) {
        if (username == null || username.isBlank()) {
            return;
        }
        if (servidorId == null || servidorId.isBlank()) {
            return;
        }
        if (servidorLocalId.equals(servidorId)) {
            registrarUsuarioLocal(username);
            return;
        }
        usuariosPorServidor.compute(username, (key, valorActual) -> {
            if (servidorLocalId.equals(valorActual)) {
                return valorActual;
            }
            return servidorId;
        });
    }

    public void registrarUsuariosRemotos(String servidorId, Collection<String> usuarios) {
        if (servidorId == null || servidorId.isBlank() || usuarios == null) {
            return;
        }
        eliminarUsuariosDeServidor(servidorId);
        for (String usuario : usuarios) {
            registrarUsuarioRemoto(usuario, servidorId);
        }
    }

    public void eliminarUsuario(String username) {
        if (username == null || username.isBlank()) {
            return;
        }
        usuariosPorServidor.remove(username);
    }

    public void eliminarUsuariosDeServidor(String servidorId) {
        if (servidorId == null || servidorId.isBlank()) {
            return;
        }
        usuariosPorServidor.entrySet().removeIf(entry -> servidorId.equals(entry.getValue()) && !servidorLocalId.equals(servidorId));
    }

    public String obtenerServidorDe(String username) {
        if (username == null || username.isBlank()) {
            return null;
        }
        return usuariosPorServidor.get(username);
    }

    public boolean esUsuarioLocal(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        return servidorLocalId.equals(usuariosPorServidor.get(username));
    }

    public Map<String, String> snapshot() {
        return Collections.unmodifiableMap(new HashMap<>(usuariosPorServidor));
    }
}
