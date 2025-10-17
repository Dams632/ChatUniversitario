package com.chat.cliente.presentacion.gui.helpers;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JPanel;

import com.chat.cliente.presentacion.gui.components.BurbujaAudio;
import com.chat.cliente.presentacion.gui.components.BurbujaMensaje;

/**
 * Helper para renderizar burbujas de mensajes en el chat
 */
public class MensajeRenderer {
    
    /**
     * Agregar una burbuja de mensaje al panel de conversación
     */
    public static void agregarBurbujaMensaje(JPanel panelConversacion, String remitente, 
                                            String contenido, String timestamp, boolean esMio) {
        // Panel contenedor para alineación
        JPanel contenedorMensaje = new JPanel();
        contenedorMensaje.setLayout(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        contenedorMensaje.setOpaque(false);
        contenedorMensaje.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        // Panel de la burbuja
        BurbujaMensaje burbuja = new BurbujaMensaje(remitente, contenido, timestamp, esMio);
        contenedorMensaje.add(burbuja);
        
        panelConversacion.add(contenedorMensaje);
        panelConversacion.revalidate();
        panelConversacion.repaint();
    }
    
    /**
     * Agregar una burbuja de audio al panel de conversación
     */
    public static void agregarBurbujaAudio(JPanel panelConversacion, String remitente, 
                                          byte[] audioData, long duracionSegundos, 
                                          String timestamp, boolean esMio) {
        // Panel contenedor para alineación
        JPanel contenedorMensaje = new JPanel();
        contenedorMensaje.setLayout(new FlowLayout(esMio ? FlowLayout.RIGHT : FlowLayout.LEFT, 10, 5));
        contenedorMensaje.setOpaque(false);
        contenedorMensaje.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        
        // Panel de la burbuja de audio
        BurbujaAudio burbuja = new BurbujaAudio(remitente, audioData, duracionSegundos, timestamp, esMio);
        contenedorMensaje.add(burbuja);
        
        panelConversacion.add(contenedorMensaje);
        panelConversacion.revalidate();
        panelConversacion.repaint();
    }
}
