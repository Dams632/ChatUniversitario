package com.chat.common.patterns;

/**
 * Gestor de eventos del chat usando patrón Observer (Singleton)
 * Permite a los componentes suscribirse y publicar eventos
 */
public class GestorEventos extends Observable {
    
    private static GestorEventos instancia;
    
    private GestorEventos() {}
    
    public static synchronized GestorEventos obtenerInstancia() {
        if (instancia == null) {
            instancia = new GestorEventos();
        }
        return instancia;
    }
    
    /**
     * Publicar un evento a todos los observadores
     */
    public void publicarEvento(EventoChat.TipoEvento tipo, Object datos) {
        EventoChat evento = new EventoChat(tipo, datos);
        notificarObservadores(evento);
    }
}
