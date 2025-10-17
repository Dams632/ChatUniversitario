package com.chat.cliente.presentacion.gui.builders;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import com.chat.cliente.presentacion.gui.utils.FontHelper;
import com.chat.common.models.Canal;
import com.chat.common.models.Usuario;

/**
 * Builder para la interfaz completa del chat principal.
 * 
 * Integra:
 * - Header con usuario actual y botones de acción
 * - UserListBuilder (panel izquierdo)
 * - MessagePanelBuilder (panel derecho)
 * - JSplitPane con divisor ajustable
 * 
 * Patrón: Builder + Facade (simplifica construcción de UI compleja)
 * Uso:
 * <pre>
 * ChatUIBuilder builder = new ChatUIBuilder()
 *     .username(username)
 *     .onLogout(this::cerrarSesion)
 *     .onCreateChannel(this::abrirCrearGrupo)
 *     .onNotifications(this::abrirNotificaciones)
 *     .onProfile(this::mostrarPerfil)
 *     .onRefreshUsers(this::cargarUsuariosConectados)
 *     .onUserSelected(this::handleUserSelected)
 *     .onChannelSelected(this::abrirChatGrupo)
 *     .onSendMessage(this::enviarMensaje)
 *     .onRecordAudio(this::grabarAudio)
 *     .build();
 * 
 * // Acceder componentes
 * JPanel mainPanel = builder.getMainPanel();
 * UserListBuilder userList = builder.getUserListBuilder();
 * MessagePanelBuilder messagePanel = builder.getMessagePanelBuilder();
 * </pre>
 */
public class ChatUIBuilder {
    
    // Componentes principales
    private JPanel mainPanel;
    private JPanel headerPanel;
    private JSplitPane splitPane;
    private UserListBuilder userListBuilder;
    private MessagePanelBuilder messagePanelBuilder;
    
    // Configuración
    private String username;
    
    // Callbacks del header
    private Runnable onLogoutCallback;
    private Runnable onCreateChannelCallback;
    private Runnable onNotificationsCallback;
    private Runnable onProfileCallback;
    
    // Callbacks de UserListBuilder
    private Runnable onRefreshUsersCallback;
    private java.util.function.Consumer<Usuario> onUserSelectedCallback;
    private java.util.function.Consumer<Canal> onChannelSelectedCallback;
    
    // Callbacks de MessagePanelBuilder
    private Runnable onSendMessageCallback;
    private Runnable onRecordAudioCallback;
    
    // Colores del tema
    private static final Color HEADER_BG = new Color(52, 73, 94);
    private static final Color HEADER_BUTTON_BG = new Color(41, 128, 185);
    private static final Color HEADER_BUTTON_HOVER = new Color(52, 152, 219);
    
    /**
     * Establece el nombre del usuario actual
     */
    public ChatUIBuilder username(String username) {
        this.username = username;
        return this;
    }
    
    /**
     * Callback para cerrar sesión
     */
    public ChatUIBuilder onLogout(Runnable callback) {
        this.onLogoutCallback = callback;
        return this;
    }
    
    /**
     * Callback para crear nuevo grupo/canal
     */
    public ChatUIBuilder onCreateChannel(Runnable callback) {
        this.onCreateChannelCallback = callback;
        return this;
    }
    
    /**
     * Callback para ver notificaciones/invitaciones
     */
    public ChatUIBuilder onNotifications(Runnable callback) {
        this.onNotificationsCallback = callback;
        return this;
    }
    
    /**
     * Callback para ver/editar perfil
     */
    public ChatUIBuilder onProfile(Runnable callback) {
        this.onProfileCallback = callback;
        return this;
    }
    
    /**
     * Callback para actualizar lista de usuarios
     */
    public ChatUIBuilder onRefreshUsers(Runnable callback) {
        this.onRefreshUsersCallback = callback;
        return this;
    }
    
    /**
     * Callback cuando se selecciona un usuario
     */
    public ChatUIBuilder onUserSelected(java.util.function.Consumer<Usuario> callback) {
        this.onUserSelectedCallback = callback;
        return this;
    }
    
    /**
     * Callback cuando se selecciona un canal/grupo
     */
    public ChatUIBuilder onChannelSelected(java.util.function.Consumer<Canal> callback) {
        this.onChannelSelectedCallback = callback;
        return this;
    }
    
    /**
     * Callback para enviar mensaje
     */
    public ChatUIBuilder onSendMessage(Runnable callback) {
        this.onSendMessageCallback = callback;
        return this;
    }
    
    /**
     * Callback para grabar audio
     */
    public ChatUIBuilder onRecordAudio(Runnable callback) {
        this.onRecordAudioCallback = callback;
        return this;
    }
    
    /**
     * Construye la interfaz completa
     */
    public ChatUIBuilder build() {
        mainPanel = new JPanel(new BorderLayout());
        
        // Header
        headerPanel = buildHeader();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel izquierdo: usuarios y canales
        userListBuilder = new UserListBuilder()
            .currentUsername(username != null ? username : "Usuario")
            .onRefresh(onRefreshUsersCallback)
            .onUserSelected(onUserSelectedCallback)
            .onChannelSelected(onChannelSelectedCallback)
            .build();
        
        // Panel derecho: conversación
        messagePanelBuilder = new MessagePanelBuilder()
            .chatHeaderText("Selecciona un usuario para chatear")
            .enabledByDefault(false)
            .onSendMessage(onSendMessageCallback)
            .onRecordAudio(onRecordAudioCallback)
            .build();
        
        // SplitPane con divisor ajustable
        splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            userListBuilder.getMainPanel(),
            messagePanelBuilder.getMainPanel()
        );
        splitPane.setDividerLocation(300);
        splitPane.setOneTouchExpandable(true);
        splitPane.setBorder(null);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        return this;
    }
    
    /**
     * Construye el panel del header con botones de acción
     */
    private JPanel buildHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(HEADER_BG);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Usuario actual
        JLabel lblUsuario = new JLabel("👤 " + (username != null ? username : "Usuario"));
        lblUsuario.setFont(FontHelper.getBoldLabelFont(20));
        lblUsuario.setForeground(Color.WHITE);
        panel.add(lblUsuario, BorderLayout.WEST);
        
        // Botones de acción
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonesPanel.setOpaque(false);
        
        // Botón crear grupo
        if (onCreateChannelCallback != null) {
            JButton btnCrearGrupo = createHeaderButton("➕👥 Crear Grupo");
            btnCrearGrupo.addActionListener(e -> onCreateChannelCallback.run());
            botonesPanel.add(btnCrearGrupo);
        }
        
        // Botón notificaciones
        if (onNotificationsCallback != null) {
            JButton btnNotificaciones = createHeaderButton("🔔 Invitaciones");
            btnNotificaciones.addActionListener(e -> onNotificationsCallback.run());
            botonesPanel.add(btnNotificaciones);
        }
        
        // Botón perfil
        if (onProfileCallback != null) {
            JButton btnPerfil = createHeaderButton("⚙️ Perfil");
            btnPerfil.addActionListener(e -> onProfileCallback.run());
            botonesPanel.add(btnPerfil);
        }
        
        // Botón cerrar sesión
        if (onLogoutCallback != null) {
            JButton btnCerrarSesion = createHeaderButton("🚪 Cerrar Sesión");
            btnCerrarSesion.addActionListener(e -> onLogoutCallback.run());
            botonesPanel.add(btnCerrarSesion);
        }
        
        panel.add(botonesPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Crea un botón estilizado para el header
     */
    private JButton createHeaderButton(String text) {
        JButton button = new JButton(text);
        button.setFont(FontHelper.getButtonFont(14));
        button.setBackground(HEADER_BUTTON_BG);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(160, 40));
        
        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(HEADER_BUTTON_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(HEADER_BUTTON_BG);
            }
        });
        
        return button;
    }
    
    // === Getters para acceso a componentes ===
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JPanel getHeaderPanel() {
        return headerPanel;
    }
    
    public JSplitPane getSplitPane() {
        return splitPane;
    }
    
    public UserListBuilder getUserListBuilder() {
        return userListBuilder;
    }
    
    public MessagePanelBuilder getMessagePanelBuilder() {
        return messagePanelBuilder;
    }
    
    /**
     * Acceso directo a componentes frecuentemente usados
     */
    public DefaultListModel<Object> getModeloListaUsuarios() {
        return userListBuilder.getModeloListaUsuarios();
    }
    
    public JList<Object> getListaUsuarios() {
        return userListBuilder.getListaUsuarios();
    }
    
    public JPanel getPanelMensajes() {
        return messagePanelBuilder.getPanelMensajes();
    }
    
    public JTextField getTxtMensaje() {
        return messagePanelBuilder.getTxtMensaje();
    }
    
    public JButton getBtnEnviar() {
        return messagePanelBuilder.getBtnEnviar();
    }
    
    public JButton getBtnAudio() {
        return messagePanelBuilder.getBtnAudio();
    }
    
    public JLabel getLblChatCon() {
        return messagePanelBuilder.getLblChatCon();
    }
    
    public JScrollPane getScrollChat() {
        return messagePanelBuilder.getScrollChat();
    }
    
    /**
     * Actualiza el divisor del split pane
     */
    public void setDividerLocation(int location) {
        splitPane.setDividerLocation(location);
    }
    
    /**
     * Habilita/deshabilita controles del chat
     */
    public void setChatEnabled(boolean enabled) {
        messagePanelBuilder.setEnabled(enabled);
    }
    
    /**
     * Actualiza el header del chat actual
     */
    public void updateChatHeader(String text) {
        messagePanelBuilder.updateChatHeader(text);
    }
    
    /**
     * Hace scroll al final del chat
     */
    public void scrollToBottom() {
        messagePanelBuilder.scrollToBottom();
    }
    
    /**
     * Limpia el campo de texto
     */
    public void clearTextField() {
        messagePanelBuilder.clearTextField();
    }
}
