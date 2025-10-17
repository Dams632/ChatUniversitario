package com.chat.common.patterns;

/**
 * Patrón Observer - Interface para observadores
 * Los observadores reciben notificaciones cuando ocurren eventos
 */
public interface Observer {
    /**
     * Método llamado cuando ocurre un evento
     * @param evento Evento con el tipo y datos asociados
     */
    void actualizar(EventoChat evento);
}
