package com.chat.cliente.presentacion.gui.helpers;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import com.chat.cliente.datos.LogAudioDAO.AudioConversacion;
import com.chat.cliente.datos.LogMensajeDAO.MensajeConversacion;
import com.chat.cliente.negocio.ServicioMensajeria;
import com.chat.common.models.Canal;
import com.chat.common.utils.FechaHoraUtil;

/**
 * Gestiona el historial y carga de conversaciones del chat
 */
public class ConversacionManager {
    
    private final Map<String, JPanel> historialConversaciones;
    private final ServicioMensajeria servicioMensajeria;
    private final String username;
    private final JScrollPane scrollChat;
    
    public ConversacionManager(ServicioMensajeria servicioMensajeria, String username, JScrollPane scrollChat) {
        this.historialConversaciones = new HashMap<>();
        this.servicioMensajeria = servicioMensajeria;
        this.username = username;
        this.scrollChat = scrollChat;
    }
    
    /**
     * Abrir chat con un usuario privado
     */
    public JPanel abrirChatPrivado(String usuarioDestino) {
        JPanel panelMensajes;
        
        // Verificar si ya existe historial en memoria
        if (historialConversaciones.containsKey(usuarioDestino)) {
            panelMensajes = historialConversaciones.get(usuarioDestino);
        } else {
            // Crear nueva conversación y cargar desde H2
            panelMensajes = crearPanelConversacion();
            historialConversaciones.put(usuarioDestino, panelMensajes);
            cargarMensajesPrivadosDesdeH2(usuarioDestino, panelMensajes);
        }
        
        actualizarScroll(panelMensajes);
        return panelMensajes;
    }
    
    /**
     * Abrir chat grupal con un canal
     */
    public JPanel abrirChatGrupo(Canal canal) {
        String identificadorGrupo = "GRUPO_" + canal.getId();
        JPanel panelMensajes;
        
        // Verificar si ya existe historial en memoria
        if (historialConversaciones.containsKey(identificadorGrupo)) {
            panelMensajes = historialConversaciones.get(identificadorGrupo);
        } else {
            // Crear nueva conversación y cargar desde H2
            panelMensajes = crearPanelConversacion();
            historialConversaciones.put(identificadorGrupo, panelMensajes);
            cargarMensajesGrupoDesdeH2(identificadorGrupo, panelMensajes);
        }
        
        actualizarScroll(panelMensajes);
        return panelMensajes;
    }
    
    /**
     * Obtener panel de conversación por identificador (usuario o grupo)
     */
    public JPanel obtenerConversacion(String identificador) {
        return historialConversaciones.get(identificador);
    }
    
    /**
     * Crear un panel vacío para conversación
     */
    private JPanel crearPanelConversacion() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        return panel;
    }
    
    /**
     * Actualizar el scrollpane con el panel de conversación
     */
    private void actualizarScroll(JPanel panelMensajes) {
        scrollChat.setViewportView(panelMensajes);
        scrollChat.revalidate();
        scrollChat.repaint();
        
        // Scroll al final
        SwingUtilities.invokeLater(() -> {
            scrollChat.getVerticalScrollBar().setValue(
                scrollChat.getVerticalScrollBar().getMaximum()
            );
        });
    }
    
    /**
     * Cargar mensajes privados desde H2
     */
    private void cargarMensajesPrivadosDesdeH2(String otroUsuario, JPanel panelConversacion) {
        // Cargar mensajes de texto
        servicioMensajeria.cargarHistorialAsync(username, otroUsuario)
            .thenAccept(mensajes -> {
                SwingUtilities.invokeLater(() -> {
                    for (MensajeConversacion msg : mensajes) {
                        String remitente = msg.getRemitente();
                        String timestamp = FechaHoraUtil.formatearHoraDesdeTimestamp(msg.getFechaEnvio());
                        
                        boolean esMio = remitente.equals(username);
                        String nombreMostrar = esMio ? "Tú" : remitente;
                        
                        MensajeRenderer.agregarBurbujaMensaje(panelConversacion, nombreMostrar, 
                                            msg.getContenido(), timestamp, esMio);
                    }
                    
                    actualizarScroll(panelConversacion);
                    
                    if (!mensajes.isEmpty()) {
                        System.out.println("✓ Cargados " + mensajes.size() + " mensajes del historial con " + otroUsuario);
                    }
                });
            })
            .exceptionally(ex -> {
                System.err.println("✗ Error al cargar historial: " + ex.getMessage());
                return null;
            });
        
        // Cargar audios
        servicioMensajeria.cargarAudiosAsync(username, otroUsuario)
            .thenAccept(audios -> {
                SwingUtilities.invokeLater(() -> {
                    for (AudioConversacion audio : audios) {
                        String remitente = audio.getRemitente();
                        String timestamp = FechaHoraUtil.formatearHoraDesdeTimestamp(audio.getFechaEnvio());
                        
                        boolean esMio = remitente.equals(username);
                        String nombreMostrar = esMio ? "Tú" : remitente;
                        
                        MensajeRenderer.agregarBurbujaAudio(panelConversacion, nombreMostrar,
                                            audio.getContenidoAudio(), audio.getDuracionSegundos(), 
                                            timestamp, esMio);
                    }
                    
                    actualizarScroll(panelConversacion);
                    
                    if (!audios.isEmpty()) {
                        System.out.println("✓ Cargados " + audios.size() + " audios del historial con " + otroUsuario);
                    }
                });
            })
            .exceptionally(ex -> {
                System.err.println("✗ Error al cargar audios: " + ex.getMessage());
                return null;
            });
    }
    
    /**
     * Cargar mensajes de grupo desde H2
     */
    private void cargarMensajesGrupoDesdeH2(String grupoId, JPanel panelConversacion) {
        // Cargar mensajes de texto
        servicioMensajeria.cargarHistorialGrupoAsync(grupoId)
            .thenAccept(mensajes -> {
                SwingUtilities.invokeLater(() -> {
                    for (MensajeConversacion msg : mensajes) {
                        String remitente = msg.getRemitente();
                        String timestamp = FechaHoraUtil.formatearHoraDesdeTimestamp(msg.getFechaEnvio());
                        
                        boolean esMio = remitente.equals(username);
                        String nombreMostrar = esMio ? "Tú" : remitente;
                        
                        MensajeRenderer.agregarBurbujaMensaje(panelConversacion, nombreMostrar, 
                                            msg.getContenido(), timestamp, esMio);
                    }
                    
                    actualizarScroll(panelConversacion);
                    
                    if (!mensajes.isEmpty()) {
                        System.out.println("✓ Cargados " + mensajes.size() + " mensajes del historial del grupo " + grupoId);
                    }
                });
            })
            .exceptionally(ex -> {
                System.err.println("✗ Error al cargar historial de grupo: " + ex.getMessage());
                return null;
            });
        
        // Cargar audios
        servicioMensajeria.cargarAudiosGrupoAsync(grupoId)
            .thenAccept(audios -> {
                SwingUtilities.invokeLater(() -> {
                    for (AudioConversacion audio : audios) {
                        String remitente = audio.getRemitente();
                        String timestamp = FechaHoraUtil.formatearHoraDesdeTimestamp(audio.getFechaEnvio());
                        
                        boolean esMio = remitente.equals(username);
                        String nombreMostrar = esMio ? "Tú" : remitente;
                        
                        MensajeRenderer.agregarBurbujaAudio(panelConversacion, nombreMostrar,
                                            audio.getContenidoAudio(), audio.getDuracionSegundos(), 
                                            timestamp, esMio);
                    }
                    
                    actualizarScroll(panelConversacion);
                    
                    if (!audios.isEmpty()) {
                        System.out.println("✓ Cargados " + audios.size() + " audios del historial del grupo " + grupoId);
                    }
                });
            })
            .exceptionally(ex -> {
                System.err.println("✗ Error al cargar audios de grupo: " + ex.getMessage());
                return null;
            });
    }
}
