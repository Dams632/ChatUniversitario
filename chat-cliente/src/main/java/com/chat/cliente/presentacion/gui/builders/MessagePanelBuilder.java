package com.chat.cliente.presentacion.gui.builders;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

/**
 * Builder para el panel de mensajes del chat.
 * 
 * Construye:
 * - Header del chat (destinatario actual)
 * - Panel de conversación con burbujas de mensajes
 * - Scroll automático
 * - Campo de texto para escribir
 * - Botones de enviar y grabar audio
 * 
 * Patrón: Builder + Template Method
 * Uso:
 * <pre>
 * MessagePanelBuilder builder = new MessagePanelBuilder()
 *     .chatHeaderText("Chat con Juan")
 *     .onSendMessage(this::enviarMensaje)
 *     .onRecordAudio(this::grabarAudio)
 *     .build();
 * 
 * // Acceder componentes
 * JPanel panel = builder.getMainPanel();
 * JPanel messagesPanel = builder.getPanelMensajes();
 * JTextField textField = builder.getTxtMensaje();
 * </pre>
 */
public class MessagePanelBuilder {
    
    // Componentes principales
    private JPanel mainPanel;
    private JPanel headerChat;
    private JLabel lblChatCon;
    private JPanel panelMensajes;
    private JScrollPane scrollChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JButton btnAudio;
    
    // Configuración
    private String chatHeaderText = "Selecciona un usuario para chatear";
    private boolean enabledByDefault = false;
    private Runnable onSendMessageCallback;
    private Runnable onRecordAudioCallback;
    
    // Colores del tema
    private static final Color HEADER_BG = new Color(52, 152, 219);
    private static final Color MESSAGES_BG = new Color(236, 240, 241);
    private static final Color SEND_BUTTON_BG = new Color(46, 204, 113);
    private static final Color SEND_BUTTON_HOVER = new Color(39, 174, 96);
    private static final Color AUDIO_BUTTON_BG = new Color(155, 89, 182);
    private static final Color AUDIO_BUTTON_HOVER = new Color(142, 68, 173);
    private static final Color BORDER_COLOR = new Color(189, 195, 199);
    
    /**
     * Establece el texto del header del chat
     */
    public MessagePanelBuilder chatHeaderText(String text) {
        this.chatHeaderText = text;
        return this;
    }
    
    /**
     * Habilita/deshabilita los controles por defecto
     */
    public MessagePanelBuilder enabledByDefault(boolean enabled) {
        this.enabledByDefault = enabled;
        return this;
    }
    
    /**
     * Callback al enviar mensaje (Enter o botón)
     */
    public MessagePanelBuilder onSendMessage(Runnable callback) {
        this.onSendMessageCallback = callback;
        return this;
    }
    
    /**
     * Callback al grabar audio
     */
    public MessagePanelBuilder onRecordAudio(Runnable callback) {
        this.onRecordAudioCallback = callback;
        return this;
    }
    
    /**
     * Construye el panel completo
     */
    public MessagePanelBuilder build() {
        mainPanel = SwingComponentBuilder.panel()
            .layout(new BorderLayout(5, 5))
            .background(Color.WHITE)
            .border(10, 10, 10, 10)
            .build();
        
        // Header del chat
        buildHeader();
        mainPanel.add(headerChat, BorderLayout.NORTH);
        
        // Panel de conversación con scroll
        buildMessagesArea();
        mainPanel.add(scrollChat, BorderLayout.CENTER);
        
        // Panel de envío (texto + botones)
        JPanel panelEnvio = buildSendPanel();
        mainPanel.add(panelEnvio, BorderLayout.SOUTH);
        
        return this;
    }
    
    /**
     * Construye el header con el nombre del destinatario
     */
    private void buildHeader() {
        headerChat = SwingComponentBuilder.panel()
            .layout(new BorderLayout())
            .background(HEADER_BG)
            .border(10, 10, 10, 10)
            .build();
        
        lblChatCon = SwingComponentBuilder.label(chatHeaderText)
            .font("Segoe UI", Font.BOLD, 16)
            .foreground(Color.WHITE)
            .build();
        
        headerChat.add(lblChatCon, BorderLayout.WEST);
    }
    
    /**
     * Construye el área de mensajes con scroll
     */
    private void buildMessagesArea() {
        // Panel de conversación con burbujas
        panelMensajes = new JPanel();
        panelMensajes.setLayout(new BoxLayout(panelMensajes, BoxLayout.Y_AXIS));
        panelMensajes.setBackground(MESSAGES_BG);
        panelMensajes.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Scroll pane
        scrollChat = new JScrollPane(panelMensajes);
        scrollChat.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);
    }
    
    /**
     * Construye el panel de envío (texto + botones)
     */
    private JPanel buildSendPanel() {
        JPanel panelEnvio = SwingComponentBuilder.panel()
            .layout(new BorderLayout(5, 5))
            .background(Color.WHITE)
            .border(10, 0, 0, 0)
            .build();
        
        // Campo de texto
        txtMensaje = new JTextField();
        txtMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMensaje.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        txtMensaje.setEnabled(enabledByDefault);
        
        // Enter para enviar
        if (onSendMessageCallback != null) {
            txtMensaje.addActionListener(e -> onSendMessageCallback.run());
        }
        
        panelEnvio.add(txtMensaje, BorderLayout.CENTER);
        
        // Botones (audio + enviar)
        JPanel panelBotones = buildButtonsPanel();
        panelEnvio.add(panelBotones, BorderLayout.EAST);
        
        return panelEnvio;
    }
    
    /**
     * Construye el panel de botones (audio + enviar)
     */
    private JPanel buildButtonsPanel() {
        JPanel panelBotones = SwingComponentBuilder.panel()
            .layout(new FlowLayout(FlowLayout.RIGHT, 5, 0))
            .background(Color.WHITE)
            .build();
        
        // Botón de audio
        btnAudio = SwingComponentBuilder.button("♫ AUDIO")
            .font("Segoe UI", Font.BOLD, 12)
            .background(AUDIO_BUTTON_BG)
            .foreground(Color.WHITE)
            .build();
        
        btnAudio.setPreferredSize(new Dimension(50, 45));
        btnAudio.setEnabled(enabledByDefault);
        btnAudio.setToolTipText(" ♫ Grabar audio");
        
        btnAudio.setFocusPainted(false);
        btnAudio.setBorderPainted(false);
        btnAudio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (onRecordAudioCallback != null) {
            btnAudio.addActionListener(e -> onRecordAudioCallback.run());
        }
        
        // Hover effect para audio
        btnAudio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnAudio.isEnabled()) {
                    btnAudio.setBackground(AUDIO_BUTTON_HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAudio.setBackground(AUDIO_BUTTON_BG);
            }
        });
        
        panelBotones.add(btnAudio);
        
        // Botón de enviar
        btnEnviar = SwingComponentBuilder.button("📤 Enviar")
            .font("Segoe UI", Font.BOLD, 14)
            .background(SEND_BUTTON_BG)
            .foreground(Color.WHITE)
            .build();
        
        btnEnviar.setPreferredSize(new Dimension(120, 45));
        btnEnviar.setEnabled(enabledByDefault);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorderPainted(false);
        btnEnviar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        if (onSendMessageCallback != null) {
            btnEnviar.addActionListener(e -> onSendMessageCallback.run());
        }
        
        // Hover effect para enviar
        btnEnviar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnEnviar.isEnabled()) {
                    btnEnviar.setBackground(SEND_BUTTON_HOVER);
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEnviar.setBackground(SEND_BUTTON_BG);
            }
        });
        
        panelBotones.add(btnEnviar);
        
        return panelBotones;
    }
    
    // === Getters para acceso a componentes ===
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JLabel getLblChatCon() {
        return lblChatCon;
    }
    
    public JPanel getPanelMensajes() {
        return panelMensajes;
    }
    
    public JScrollPane getScrollChat() {
        return scrollChat;
    }
    
    public JTextField getTxtMensaje() {
        return txtMensaje;
    }
    
    public JButton getBtnEnviar() {
        return btnEnviar;
    }
    
    public JButton getBtnAudio() {
        return btnAudio;
    }
    
    /**
     * Habilita/deshabilita todos los controles de entrada
     */
    public void setEnabled(boolean enabled) {
        txtMensaje.setEnabled(enabled);
        btnEnviar.setEnabled(enabled);
        btnAudio.setEnabled(enabled);
    }
    
    /**
     * Actualiza el texto del header del chat
     */
    public void updateChatHeader(String text) {
        lblChatCon.setText(text);
    }
    
    /**
     * Limpia el campo de texto
     */
    public void clearTextField() {
        txtMensaje.setText("");
    }
    
    /**
     * Hace scroll al final del área de mensajes
     */
    public void scrollToBottom() {
        SwingUtilities.invokeLater(() -> {
            JScrollBar vertical = scrollChat.getVerticalScrollBar();
            vertical.setValue(vertical.getMaximum());
        });
    }
}
