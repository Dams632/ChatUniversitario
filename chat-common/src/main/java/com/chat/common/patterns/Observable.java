package com.chat.common.patterns;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Clase base para sujetos observables
 * Implementación thread-safe usando CopyOnWriteArrayList
 */
public class Observable {
    private final List<Observer> observadores = new CopyOnWriteArrayList<>();
    
    public void agregarObservador(Observer observer) {
        observadores.add(observer);
    }
    
    public void removerObservador(Observer observer) {
        observadores.remove(observer);
    }
    
    protected void notificarObservadores(EventoChat evento) {
        for (Observer observer : observadores) {
            observer.actualizar(evento);
        }
    }
    
    public int contarObservadores() {
        return observadores.size();
    }
}
