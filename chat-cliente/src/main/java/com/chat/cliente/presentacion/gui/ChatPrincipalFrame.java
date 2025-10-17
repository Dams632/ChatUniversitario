package com.chat.cliente.presentacion.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.chat.cliente.datos.ConexionH2;
import com.chat.cliente.negocio.ServicioCliente;
import com.chat.cliente.negocio.ServicioMensajeria;
import com.chat.cliente.presentacion.gui.helpers.ConversacionManager;
import com.chat.cliente.presentacion.gui.helpers.InvitacionHandler;
import com.chat.cliente.presentacion.gui.helpers.MensajeRenderer;
import com.chat.cliente.presentacion.gui.helpers.NotificacionManager;
import com.chat.cliente.presentacion.gui.helpers.UIHelper;
import com.chat.cliente.presentacion.gui.utils.FontHelper;
import com.chat.common.dto.ResponseDTO;
import com.chat.common.models.Canal;
import com.chat.common.models.MensajeEntranteDTO;
import com.chat.common.models.Usuario;
import com.chat.common.patterns.EventoChat;
import com.chat.common.patterns.GestorEventos;
import com.chat.common.patterns.Observer;
import com.chat.common.utils.FechaHoraUtil;

/**
 * Ventana principal del chat del cliente
 * Implementa Observer para recibir notificaciones de mensajes entrantes
 */
public class ChatPrincipalFrame extends JFrame implements Observer {
    
    private final ServicioCliente servicioCliente;
    private final ServicioMensajeria servicioMensajeria;
    private final String username;
    
    // Helpers
    private ConversacionManager conversacionManager;
    private InvitacionHandler invitacionHandler;
    
    // Panel izquierdo - Lista de usuarios y grupos
    private JList<Object> listaUsuarios;
    private DefaultListModel<Object> modeloListaUsuarios;
    private JButton btnActualizarUsuarios;
    private JLabel lblUsuariosOnline;
    
    // Panel derecho - Chat
    private JPanel panelMensajes; // Panel contenedor de burbujas
    private JScrollPane scrollChat;
    private JTextField txtMensaje;
    private JButton btnEnviar;
    private JButton btnAudio;
    private JLabel lblChatCon;
    
    private String usuarioSeleccionado;
    private List<Usuario> usuariosConectados;
    
    // Contador de mensajes no leídos por usuario/canal
    private java.util.Map<String, Integer> mensajesNoLeidos;
    
    /**
     * Constructor
     */
    public ChatPrincipalFrame(ServicioCliente servicioCliente, String username) {
        this.servicioCliente = servicioCliente;
        this.username = username;
        this.usuariosConectados = new ArrayList<>();
        this.mensajesNoLeidos = new java.util.HashMap<>();
        
        // Inicializar servicio de mensajería
        try {
            this.servicioMensajeria = new ServicioMensajeria();
        } catch (Exception e) {
            throw new RuntimeException("Error al inicializar servicio de mensajería", e);
        }
        
        initComponents();
        
        // Inicializar helpers (después de initComponents porque necesita scrollChat)
        this.conversacionManager = new ConversacionManager(servicioMensajeria, username, scrollChat);
        this.invitacionHandler = new InvitacionHandler(servicioCliente, this::cargarUsuariosConectados);
        
        cargarUsuariosConectados();
        suscribirseAEventos();
    }
    
    /**
     * Suscribirse al GestorEventos para recibir notificaciones
     */
    private void suscribirseAEventos() {
        GestorEventos.obtenerInstancia().agregarObservador(this);
        System.out.println("ChatPrincipalFrame suscrito a eventos");
    }
    
    /**
     * Método del patrón Observer - se llama cuando ocurre un evento
     */
    @Override
    public void actualizar(EventoChat evento) {
        if (evento.getTipo() == EventoChat.TipoEvento.MENSAJE_RECIBIDO) {
            MensajeEntranteDTO mensaje = (MensajeEntranteDTO) evento.getDatos();
            
            // Ejecutar en el hilo de Swing
            SwingUtilities.invokeLater(() -> {
                String remitente = mensaje.getRemitente();
                
                // Si el mensaje NO es del chat actualmente abierto
                if (usuarioSeleccionado == null || !remitente.equals(usuarioSeleccionado)) {
                    // Reproducir notificación
                    NotificacionManager.reproducirNotificacion();
                    
                    // Incrementar contador de mensajes no leídos
                    mensajesNoLeidos.put(remitente, mensajesNoLeidos.getOrDefault(remitente, 0) + 1);
                    
                    // Actualizar lista visual
                    listaUsuarios.repaint();
                }
                
                mostrarMensajeRecibido(remitente, mensaje.getContenido());
            });
        } else if (evento.getTipo() == EventoChat.TipoEvento.MENSAJE_GRUPO_RECIBIDO) {
            // Mensaje grupal recibido
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> datosMensaje = (java.util.Map<String, Object>) evento.getDatos();
            Long canalId = (Long) datosMensaje.get("canalId");
            String remitente = (String) datosMensaje.get("remitente");
            String contenido = (String) datosMensaje.get("contenido");
            
            // Ejecutar en el hilo de Swing
            SwingUtilities.invokeLater(() -> {
                String canalKey = "CANAL_" + canalId;
                
                // Si el mensaje NO es del canal actualmente abierto
                if (usuarioSeleccionado == null || !canalKey.equals(usuarioSeleccionado)) {
                    // Reproducir notificación
                    NotificacionManager.reproducirNotificacion();
                    
                    // Incrementar contador de mensajes no leídos
                    mensajesNoLeidos.put(canalKey, mensajesNoLeidos.getOrDefault(canalKey, 0) + 1);
                    
                    // Actualizar lista visual
                    listaUsuarios.repaint();
                }
                
                mostrarMensajeGrupo(canalId, remitente, contenido);
            });
        } else if (evento.getTipo() == EventoChat.TipoEvento.USUARIOS_ACTUALIZADOS) {
            // La lista de usuarios ha cambiado, recargar
            SwingUtilities.invokeLater(() -> {
                cargarUsuariosConectados();
            });
        } else if (evento.getTipo() == EventoChat.TipoEvento.INVITACION_RECIBIDA) {
            // Invitación recibida, mostrar notificación
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "¡Has recibido una nueva invitación a un grupo!\n\nHaz clic en 🔔 Invitaciones para verla.",
                    "Nueva Invitación",
                    JOptionPane.INFORMATION_MESSAGE);
                // Recargar usuarios/grupos
                cargarUsuariosConectados();
            });
        } else if (evento.getTipo() == EventoChat.TipoEvento.AUDIO_RECIBIDO) {
            // Audio privado recibido
            com.chat.common.dto.AudioEntranteDTO audioDTO = 
                (com.chat.common.dto.AudioEntranteDTO) evento.getDatos();
            
            SwingUtilities.invokeLater(() -> {
                mostrarAudioRecibido(audioDTO);
            });
        } else if (evento.getTipo() == EventoChat.TipoEvento.AUDIO_GRUPO_RECIBIDO) {
            // Audio grupal recibido
            com.chat.common.dto.AudioEntranteDTO audioDTO = 
                (com.chat.common.dto.AudioEntranteDTO) evento.getDatos();
            
            SwingUtilities.invokeLater(() -> {
                mostrarAudioGrupo(audioDTO);
            });
        } else if (evento.getTipo() == EventoChat.TipoEvento.NOTIFICACION_SERVIDOR) {
            // Notificación broadcast del servidor
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> datos = (java.util.Map<String, Object>) evento.getDatos();
            String mensaje = (String) datos.get("mensaje");
            String timestamp = (String) datos.get("timestamp");
            
            SwingUtilities.invokeLater(() -> {
                mostrarNotificacionServidor(mensaje, timestamp);
            });
        } else if (evento.getTipo() == EventoChat.TipoEvento.NOTIFICACION_SERVIDOR_GRUPO) {
            // Notificación broadcast del servidor en grupo
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> datos = (java.util.Map<String, Object>) evento.getDatos();
            Long canalId = (Long) datos.get("canalId");
            String nombreCanal = (String) datos.get("nombreCanal");
            String mensaje = (String) datos.get("mensaje");
            String timestamp = (String) datos.get("timestamp");
            
            SwingUtilities.invokeLater(() -> {
                mostrarNotificacionServidorGrupo(canalId, nombreCanal, mensaje, timestamp);
            });
        } else if (evento.getTipo() == EventoChat.TipoEvento.DESCONEXION_FORZADA) {
            // El servidor ha cerrado la conexión, desconectar automáticamente
            String mensaje = (String) evento.getDatos();
            SwingUtilities.invokeLater(() -> {
                // Remover observador del gestor de eventos
                GestorEventos.obtenerInstancia().removerObservador(this);
                
                // Cerrar conexión H2
                ConexionH2.cerrarConexion();
                
                // Cerrar esta ventana
                dispose();
                
                // Mostrar mensaje y volver al login
                JOptionPane.showMessageDialog(null,
                    mensaje != null ? mensaje : "El servidor ha cerrado la conexión.",
                    "Conexión Cerrada",
                    JOptionPane.WARNING_MESSAGE);
                
                // Abrir ventana de login
                new LoginFrameRefactored(servicioCliente, new LoginFrameRefactored.LoginCallback() {
                    @Override
                    public void onLoginExitoso(String usuario, ResponseDTO response) {
                        new ChatPrincipalFrame(servicioCliente, usuario).setVisible(true);
                    }
                    
                    @Override
                    public void onAbrirRegistro() {
                        // No implementado - el usuario solo puede hacer login después de desconexión
                    }
                }).setVisible(true);
            });
        }
    }
    
    /**
     * Mostrar mensaje recibido en el área de chat
     */
    private void mostrarMensajeRecibido(String remitente, String contenido) {
        String timestamp = FechaHoraUtil.formatearHoraActual();
        
        // Obtener o crear conversación
        JPanel conversacion = conversacionManager.obtenerConversacion(remitente);
        if (conversacion == null) {
            conversacion = conversacionManager.abrirChatPrivado(remitente);
        }
        
        MensajeRenderer.agregarBurbujaMensaje(conversacion, remitente, contenido, timestamp, false);
        
        // Guardar en base de datos H2 (async)
        servicioMensajeria.guardarMensajeAsync(remitente, username, contenido);
        
        // Si estamos viendo el chat con este usuario, actualizar la vista
        if (remitente.equals(usuarioSeleccionado)) {
            scrollChat.revalidate();
            scrollChat.repaint();
            SwingUtilities.invokeLater(() -> {
                scrollChat.getVerticalScrollBar().setValue(
                    scrollChat.getVerticalScrollBar().getMaximum()
                );
            });
        }
        
        System.out.println("Mensaje recibido de " + remitente + ": " + contenido);
    }
    
    /**
     * Mostrar mensaje grupal recibido
     */
    private void mostrarMensajeGrupo(Long canalId, String remitente, String contenido) {
        String timestamp = FechaHoraUtil.formatearHoraActual();
        String identificadorGrupo = "GRUPO_" + canalId;
        
        // Obtener conversación (se creará si no existe al abrir el chat)
        JPanel conversacion = conversacionManager.obtenerConversacion(identificadorGrupo);
        if (conversacion == null) {
            // Si no existe, crear panel básico sin cargar historial
            conversacion = crearPanelConversacion();
        }
        
        // No mostrar como "mío" si yo envié el mensaje
        boolean esMio = remitente.equals(username);
        MensajeRenderer.agregarBurbujaMensaje(conversacion, remitente, contenido, timestamp, esMio);
        
        // Guardar en base de datos H2 (async)
        servicioMensajeria.guardarMensajeGrupoAsync(remitente, identificadorGrupo, contenido);
        
        // Si estamos viendo el chat de este grupo, actualizar la vista
        if (identificadorGrupo.equals(usuarioSeleccionado)) {
            scrollChat.revalidate();
            scrollChat.repaint();
            SwingUtilities.invokeLater(() -> {
                scrollChat.getVerticalScrollBar().setValue(
                    scrollChat.getVerticalScrollBar().getMaximum()
                );
            });
        }
        
        System.out.println("Mensaje grupal recibido de " + remitente + " en canal " + canalId + ": " + contenido);
    }
    
    /**
     * Mostrar audio privado recibido
     */
    private void mostrarAudioRecibido(com.chat.common.dto.AudioEntranteDTO audioDTO) {
        String remitente = audioDTO.getRemitente();
        byte[] audioData = audioDTO.getContenidoAudio();
        long duracion = audioDTO.getDuracionSegundos();
        String timestamp = audioDTO.getTimestamp();
        
        // Obtener o crear conversación
        JPanel conversacion = conversacionManager.obtenerConversacion(remitente);
        if (conversacion == null) {
            conversacion = conversacionManager.abrirChatPrivado(remitente);
        }
        
        // Agregar burbuja de audio
        com.chat.cliente.presentacion.gui.helpers.MensajeRenderer.agregarBurbujaAudio(
            conversacion, remitente, audioData, duracion, timestamp, false
        );
        
        // Persistir audio en H2
        servicioMensajeria.guardarAudioAsync(
            remitente, username, audioData, audioDTO.getFormato(), duracion
        );
        
        // Si estamos viendo el chat de este usuario, actualizar la vista
        if (remitente.equals(usuarioSeleccionado)) {
            scrollChat.revalidate();
            scrollChat.repaint();
            SwingUtilities.invokeLater(() -> {
                scrollChat.getVerticalScrollBar().setValue(
                    scrollChat.getVerticalScrollBar().getMaximum()
                );
            });
        }
        
        System.out.println("Audio recibido de " + remitente + " (duración: " + duracion + "s)");
    }
    
    /**
     * Mostrar notificación broadcast del servidor
     */
    private void mostrarNotificacionServidor(String mensaje, String timestamp) {
        // Mostrar diálogo con la notificación
        JOptionPane.showMessageDialog(this,
            mensaje,
            "📢 Notificación del Servidor",
            JOptionPane.INFORMATION_MESSAGE);
        
        System.out.println("Notificación del servidor: " + mensaje);
    }
    
    /**
     * Mostrar notificación broadcast del servidor en un grupo
     */
    private void mostrarNotificacionServidorGrupo(Long canalId, String nombreCanal, 
                                                  String mensaje, String timestamp) {
        String identificadorGrupo = "GRUPO_" + canalId;
        
        // Obtener conversación del grupo
        JPanel conversacion = conversacionManager.obtenerConversacion(identificadorGrupo);
        if (conversacion == null) {
            conversacion = crearPanelConversacion();
            // No lo agregamos al manager porque es temporal
        }
        
        // Agregar mensaje del servidor en el chat del grupo como burbuja especial
        com.chat.cliente.presentacion.gui.helpers.MensajeRenderer.agregarBurbujaMensaje(
            conversacion, "🖥️ SERVIDOR", mensaje, timestamp, false
        );
        
        // Si estamos viendo el chat de este grupo, actualizar la vista
        if (identificadorGrupo.equals(usuarioSeleccionado)) {
            scrollChat.revalidate();
            scrollChat.repaint();
            SwingUtilities.invokeLater(() -> {
                scrollChat.getVerticalScrollBar().setValue(
                    scrollChat.getVerticalScrollBar().getMaximum()
                );
            });
        }
        
        // También mostrar diálogo
        JOptionPane.showMessageDialog(this,
            "Notificación para " + nombreCanal + ":\n\n" + mensaje,
            "📢 Notificación del Servidor (Grupo)",
            JOptionPane.INFORMATION_MESSAGE);
        
        System.out.println("Notificación del servidor para grupo " + nombreCanal + ": " + mensaje);
    }
    
    /**
     * Mostrar audio grupal recibido
     */
    private void mostrarAudioGrupo(com.chat.common.dto.AudioEntranteDTO audioDTO) {
        Long canalId = audioDTO.getCanalId();
        String remitente = audioDTO.getRemitente();
        byte[] audioData = audioDTO.getContenidoAudio();
        long duracion = audioDTO.getDuracionSegundos();
        String timestamp = audioDTO.getTimestamp();
        String identificadorGrupo = "GRUPO_" + canalId;
        
        // Obtener conversación
        JPanel conversacion = conversacionManager.obtenerConversacion(identificadorGrupo);
        if (conversacion == null) {
            conversacion = crearPanelConversacion();
        }
        
        // No mostrar como "mío" si yo envié el audio
        boolean esMio = remitente.equals(username);
        
        // Agregar burbuja de audio
        com.chat.cliente.presentacion.gui.helpers.MensajeRenderer.agregarBurbujaAudio(
            conversacion, remitente, audioData, duracion, timestamp, esMio
        );
        
        // Persistir audio grupal en H2
        servicioMensajeria.guardarAudioGrupoAsync(
            remitente, identificadorGrupo, audioData, audioDTO.getFormato(), duracion
        );
        
        // Si estamos viendo el chat de este grupo, actualizar la vista
        if (identificadorGrupo.equals(usuarioSeleccionado)) {
            scrollChat.revalidate();
            scrollChat.repaint();
            SwingUtilities.invokeLater(() -> {
                scrollChat.getVerticalScrollBar().setValue(
                    scrollChat.getVerticalScrollBar().getMaximum()
                );
            });
        }
        
        System.out.println("Audio grupal recibido de " + remitente + " en canal " + canalId + 
                         " (duración: " + duracion + "s)");
    }
    
    /**
     * Inicializar componentes
     */
    private void initComponents() {
        setTitle("Chat Universitario - " + username);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Panel superior - Encabezado
        JPanel headerPanel = crearPanelEncabezado();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel central - Split entre usuarios y chat
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(250);
        splitPane.setDividerSize(2);
        
        // Panel izquierdo - Lista de usuarios
        JPanel panelUsuarios = crearPanelUsuarios();
        splitPane.setLeftComponent(panelUsuarios);
        
        // Panel derecho - Chat
        JPanel panelChat = crearPanelChat();
        splitPane.setRightComponent(panelChat);
        
        mainPanel.add(splitPane, BorderLayout.CENTER);
        
        add(mainPanel);
    }
    
    /**
     * Crear panel de encabezado
     */
    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Usuario actual
        JLabel lblUsuario = new JLabel("👤 " + username);
        lblUsuario.setFont(FontHelper.getBoldLabelFont(20));
        lblUsuario.setForeground(Color.WHITE);
        panel.add(lblUsuario, BorderLayout.WEST);
        
        // Botones de acción
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        botonesPanel.setOpaque(false);
        
        JButton btnCrearGrupo = crearBotonHeader("➕👥 Crear Grupo");
        btnCrearGrupo.addActionListener(e -> abrirCrearGrupo());
        botonesPanel.add(btnCrearGrupo);
        
        JButton btnNotificaciones = crearBotonHeader("🔔 Invitaciones");
        btnNotificaciones.addActionListener(e -> abrirNotificaciones());
        botonesPanel.add(btnNotificaciones);
        
        JButton btnPerfil = crearBotonHeader("⚙️ Perfil");
        btnPerfil.addActionListener(e -> mostrarPerfil());
        botonesPanel.add(btnPerfil);
        
        JButton btnCerrarSesion = crearBotonHeader("🚪 Cerrar Sesión");
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        botonesPanel.add(btnCerrarSesion);
        
        panel.add(botonesPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Crear panel de usuarios conectados
     */
    private JPanel crearPanelUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Encabezado de usuarios
        JPanel headerUsuarios = new JPanel(new BorderLayout(5, 5));
        headerUsuarios.setBackground(Color.WHITE);
        
        lblUsuariosOnline = new JLabel("👥 Usuarios en línea (0)");
        lblUsuariosOnline.setFont(FontHelper.getBoldLabelFont(14));
        lblUsuariosOnline.setForeground(new Color(52, 73, 94));
        headerUsuarios.add(lblUsuariosOnline, BorderLayout.WEST);
        
        btnActualizarUsuarios = new JButton("🔄");
        btnActualizarUsuarios.setFont(FontHelper.getButtonFont(12));
        btnActualizarUsuarios.setBackground(new Color(52, 152, 219));
        btnActualizarUsuarios.setForeground(Color.WHITE);
        btnActualizarUsuarios.setFocusPainted(false);
        btnActualizarUsuarios.setBorderPainted(false);
        btnActualizarUsuarios.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnActualizarUsuarios.setToolTipText("Actualizar lista de usuarios");
        btnActualizarUsuarios.addActionListener(e -> cargarUsuariosConectados());
        headerUsuarios.add(btnActualizarUsuarios, BorderLayout.EAST);
        
        panel.add(headerUsuarios, BorderLayout.NORTH);
        
        // Lista de usuarios
        modeloListaUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloListaUsuarios);
        listaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaUsuarios.setFixedCellHeight(40);
        listaUsuarios.setCellRenderer(new UsuarioListCellRenderer());
        
        // Listener para selección de usuario o grupo
        listaUsuarios.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Object seleccionado = listaUsuarios.getSelectedValue();
                if (seleccionado instanceof Usuario) {
                    Usuario usuario = (Usuario) seleccionado;
                    if (!usuario.getUsername().equals(username)) {
                        abrirChatCon(usuario.getUsername());
                    }
                } else if (seleccionado instanceof Canal) {
                    Canal canal = (Canal) seleccionado;
                    abrirChatGrupo(canal);
                }
            }
        });
        
        JScrollPane scrollUsuarios = new JScrollPane(listaUsuarios);
        scrollUsuarios.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollUsuarios, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crear panel de chat
     */
    private JPanel crearPanelChat() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Encabezado del chat
        JPanel headerChat = new JPanel(new BorderLayout());
        headerChat.setBackground(new Color(52, 152, 219));
        headerChat.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        lblChatCon = new JLabel("Selecciona un usuario para chatear");
        lblChatCon.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblChatCon.setForeground(Color.WHITE);
        headerChat.add(lblChatCon, BorderLayout.WEST);
        
        panel.add(headerChat, BorderLayout.NORTH);
        
        // Panel de mensajes con burbujas
        panelMensajes = crearPanelConversacion();
        
        scrollChat = new JScrollPane(panelMensajes);
        scrollChat.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        scrollChat.getVerticalScrollBar().setUnitIncrement(16);
        panel.add(scrollChat, BorderLayout.CENTER);
        
        // Panel de envío de mensajes
        JPanel panelEnvio = new JPanel(new BorderLayout(5, 5));
        panelEnvio.setBackground(Color.WHITE);
        panelEnvio.setBorder(new EmptyBorder(10, 0, 0, 0));
        
        txtMensaje = new JTextField();
        txtMensaje.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtMensaje.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        txtMensaje.setEnabled(false);
        
        // Enter para enviar
        txtMensaje.addActionListener(e -> enviarMensaje());
        
        panelEnvio.add(txtMensaje, BorderLayout.CENTER);
        
        // Panel de botones (audio y enviar)
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBotones.setBackground(Color.WHITE);
        
        // Botón de audio - Usar texto en lugar de emoji
        JButton btnAudio = new JButton("♫ AUDIO");
        btnAudio.setFont(FontHelper.getButtonFont(12));
        btnAudio.setBackground(new Color(155, 89, 182));
        btnAudio.setForeground(Color.WHITE);
        btnAudio.setFocusPainted(false);
        btnAudio.setBorderPainted(false);
        btnAudio.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnAudio.setPreferredSize(new Dimension(50, 45));
        btnAudio.setEnabled(false);
        btnAudio.setToolTipText(" ♫ Grabar audio");
        btnAudio.addActionListener(e -> grabarAudio());
        
        // Efecto hover botón audio
        btnAudio.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnAudio.isEnabled()) {
                    btnAudio.setBackground(new Color(142, 68, 173));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnAudio.setBackground(new Color(155, 89, 182));
            }
        });
        
        panelBotones.add(btnAudio);
        
        btnEnviar = new JButton("📤 Enviar");
        btnEnviar.setFont(FontHelper.getButtonFont(14));
        btnEnviar.setBackground(new Color(46, 204, 113));
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setFocusPainted(false);
        btnEnviar.setBorderPainted(false);
        btnEnviar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEnviar.setPreferredSize(new Dimension(120, 45));
        btnEnviar.setEnabled(false);
        btnEnviar.addActionListener(e -> enviarMensaje());
        
        // Efecto hover
        btnEnviar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (btnEnviar.isEnabled()) {
                    btnEnviar.setBackground(new Color(39, 174, 96));
                }
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnEnviar.setBackground(new Color(46, 204, 113));
            }
        });
        
        panelBotones.add(btnEnviar);
        
        panelEnvio.add(panelBotones, BorderLayout.EAST);
        
        // Guardar referencia al botón de audio para habilitarlo/deshabilitarlo
        this.btnAudio = btnAudio;
        
        panel.add(panelEnvio, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Crear botón para el header
     */
    private JButton crearBotonHeader(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(FontHelper.getButtonFont(12));
        boton.setBackground(new Color(41, 128, 185));
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(52, 152, 219));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(41, 128, 185));
            }
        });
        
        return boton;
    }
    
    /**
     * Cargar todos los usuarios y grupos
     */
    private void cargarUsuariosConectados() {
        btnActualizarUsuarios.setEnabled(false);
        
        new Thread(() -> {
            try {
                //Obtener usuarios y grupos
                ResponseDTO responseUsuarios = servicioCliente.obtenerTodosLosUsuarios();
                ResponseDTO responseGrupos = servicioCliente.obtenerGrupos();
                
                final List<Usuario> usuarios = new ArrayList<>();
                final List<Canal> canales = new ArrayList<>();
                
                if (responseUsuarios.isExito()) {
                    @SuppressWarnings("unchecked")
                    List<Usuario> listaUsuarios = (List<Usuario>) responseUsuarios.getDato("usuarios");
                    usuarios.addAll(listaUsuarios);
                    usuariosConectados = usuarios;
                }
                
                if (responseGrupos.isExito()) {
                    @SuppressWarnings("unchecked")
                    List<Canal> listaCanales = (List<Canal>) responseGrupos.getDato("grupos");
                    canales.addAll(listaCanales);
                }
                    
                SwingUtilities.invokeLater(() -> {
                    modeloListaUsuarios.clear();
                    int countOnline = 0;
                    int countTotal = 0;
                    
                    // Primero agregar canales/grupos
                    for (Canal canal : canales) {
                        modeloListaUsuarios.addElement(canal);
                    }
                    
                    // Luego agregar usuarios
                    for (Usuario usuario : usuarios) {
                        if (!usuario.getUsername().equals(username)) {
                            modeloListaUsuarios.addElement(usuario);
                            countTotal++;
                            if (usuario.isEnLinea()) {
                                countOnline++;
                            }
                        }
                    }
                    lblUsuariosOnline.setText("👥 Usuarios (" + countOnline + " en línea / " + countTotal + " total) - Grupos: " + canales.size());
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Error al cargar usuarios: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            } finally {
                SwingUtilities.invokeLater(() -> btnActualizarUsuarios.setEnabled(true));
            }
        }).start();
    }
    
    /**
     * Abrir chat con un usuario
     */
    private void abrirChatCon(String usuarioDestino) {
        this.usuarioSeleccionado = usuarioDestino;
        lblChatCon.setText("💬 Chat con " + usuarioDestino);
        txtMensaje.setEnabled(true);
        btnEnviar.setEnabled(true);
        btnAudio.setEnabled(true);
        txtMensaje.requestFocus();
        
        // Resetear contador de mensajes no leídos
        mensajesNoLeidos.remove(usuarioDestino);
        listaUsuarios.repaint();
        
        // Usar ConversacionManager para obtener/crear conversación
        panelMensajes = conversacionManager.abrirChatPrivado(usuarioDestino);
    }
    
    /**
     * Abrir chat grupal con un canal
     */
    private void abrirChatGrupo(Canal canal) {
        String identificadorGrupo = "GRUPO_" + canal.getId();
        this.usuarioSeleccionado = identificadorGrupo;
        
        lblChatCon.setText("👥 Grupo: " + canal.getNombre());
        txtMensaje.setEnabled(true);
        btnEnviar.setEnabled(true);
        btnAudio.setEnabled(true);
        txtMensaje.requestFocus();
        
        // Resetear contador de mensajes no leídos
        String canalKey = "CANAL_" + canal.getId();
        mensajesNoLeidos.remove(canalKey);
        listaUsuarios.repaint();
        
        // Usar ConversacionManager para obtener/crear conversación de grupo
        panelMensajes = conversacionManager.abrirChatGrupo(canal);
    }
    
    /**
     * Enviar mensaje
     */
    private void enviarMensaje() {
        String mensaje = txtMensaje.getText().trim();
        
        if (mensaje.isEmpty()) {
            return;
        }
        
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "Por favor selecciona un usuario primero",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Deshabilitar entrada mientras se envía
        txtMensaje.setEnabled(false);
        btnEnviar.setEnabled(false);
        
        String mensajeFinal = mensaje;
        
        new Thread(() -> {
            try {
                ResponseDTO response;
                
                // Detectar si es un grupo (empieza con "GRUPO_")
                if (usuarioSeleccionado.startsWith("GRUPO_")) {
                    // Extraer el ID del canal
                    Long canalId = Long.parseLong(usuarioSeleccionado.substring(6));
                    response = servicioCliente.enviarMensajeGrupo(canalId, mensajeFinal);
                } else {
                    // Mensaje privado
                    response = servicioCliente.enviarMensajePrivado(usuarioSeleccionado, mensajeFinal);
                }
                
                SwingUtilities.invokeLater(() -> {
                    if (response.isExito()) {
                        txtMensaje.setText("");
                        
                        // Solo agregar mensaje localmente si es chat privado
                        // Los mensajes grupales llegarán como notificación del servidor
                        if (!usuarioSeleccionado.startsWith("GRUPO_")) {
                            // Mensaje privado: agregar inmediatamente
                            String timestamp = FechaHoraUtil.formatearHoraActual();
                            JPanel conversacion = conversacionManager.obtenerConversacion(usuarioSeleccionado);
                            MensajeRenderer.agregarBurbujaMensaje(conversacion, "Tú", mensajeFinal, timestamp, true);
                            
                            // Guardar en base de datos H2 (async)
                            servicioMensajeria.guardarMensajeAsync(username, usuarioSeleccionado, mensajeFinal);
                            
                            // Scroll al final
                            scrollChat.revalidate();
                            scrollChat.repaint();
                            SwingUtilities.invokeLater(() -> {
                                scrollChat.getVerticalScrollBar().setValue(
                                    scrollChat.getVerticalScrollBar().getMaximum()
                                );
                            });
                        }
                        // Para mensajes grupales, el servidor enviará la notificación
                        // y se mostrará mediante mostrarMensajeGrupo()
                        
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Error al enviar mensaje: " + response.getMensaje(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                    txtMensaje.setEnabled(true);
                    btnEnviar.setEnabled(true);
                    txtMensaje.requestFocus();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Error al enviar mensaje: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    
                    txtMensaje.setEnabled(true);
                    btnEnviar.setEnabled(true);
                });
            }
        }).start();
    }
    
    /**
     * Grabar y enviar audio
     */
    private void grabarAudio() {
        if (usuarioSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "Por favor selecciona un usuario o grupo primero",
                "Advertencia",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Abrir diálogo de grabación
        com.chat.cliente.presentacion.gui.components.AudioRecorder recorder = 
            new com.chat.cliente.presentacion.gui.components.AudioRecorder(this);
        recorder.setVisible(true);
        
        // Verificar si se canceló
        if (recorder.isCancelled() || recorder.getRecordedAudio() == null) {
            return;
        }
        
        byte[] audioData = recorder.getRecordedAudio();
        long duracion = recorder.getRecordingDuration();
        
        // Deshabilitar botones mientras se envía
        btnAudio.setEnabled(false);
        btnEnviar.setEnabled(false);
        txtMensaje.setEnabled(false);
        
        new Thread(() -> {
            try {
                ResponseDTO response;
                boolean esGrupo = usuarioSeleccionado.startsWith("GRUPO_");
                
                if (esGrupo) {
                    // Enviar audio a grupo
                    Long canalId = Long.parseLong(usuarioSeleccionado.substring(6));
                    response = servicioCliente.enviarAudioGrupo(canalId, audioData, "wav", duracion);
                } else {
                    // Enviar audio privado
                    response = servicioCliente.enviarAudioPrivado(usuarioSeleccionado, audioData, "wav", duracion);
                }
                
                SwingUtilities.invokeLater(() -> {
                    if (response.isExito()) {
                        // Solo agregar audio localmente si es chat privado
                        if (!esGrupo) {
                            String timestamp = com.chat.common.utils.FechaHoraUtil.formatearHoraActual();
                            JPanel conversacion = conversacionManager.obtenerConversacion(usuarioSeleccionado);
                            com.chat.cliente.presentacion.gui.helpers.MensajeRenderer.agregarBurbujaAudio(
                                conversacion, "Tú", audioData, duracion, timestamp, true
                            );
                            
                            // Persistir audio en H2
                            servicioMensajeria.guardarAudioAsync(
                                username, usuarioSeleccionado, audioData, "wav", duracion
                            );
                            
                            // Revalidar el panel de mensajes
                            conversacion.revalidate();
                            conversacion.repaint();
                            
                            // Forzar actualización del scroll
                            scrollChat.revalidate();
                            scrollChat.repaint();
                            
                            // Hacer scroll al final con un pequeño delay para asegurar que se renderizó
                            javax.swing.Timer timer = new javax.swing.Timer(100, evt -> {
                                scrollChat.getVerticalScrollBar().setValue(
                                    scrollChat.getVerticalScrollBar().getMaximum()
                                );
                            });
                            timer.setRepeats(false);
                            timer.start();
                        }
                        // Para audios grupales, el servidor enviará la notificación
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Error al enviar audio: " + response.getMensaje(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                    
                    btnAudio.setEnabled(true);
                    btnEnviar.setEnabled(true);
                    txtMensaje.setEnabled(true);
                    txtMensaje.requestFocus();
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Error al enviar audio: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                    
                    btnAudio.setEnabled(true);
                    btnEnviar.setEnabled(true);
                    txtMensaje.setEnabled(true);
                    txtMensaje.requestFocus();
                });
            }
        }).start();
    }
    
    /**
     * Mostrar perfil
     */
    private void mostrarPerfil() {
        JOptionPane.showMessageDialog(this,
            "Usuario: " + username + "\n\nFunción de perfil en desarrollo...",
            "Perfil",
            JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Abrir ventana de creación de grupo/canal
     */
    private void abrirCrearGrupo() {
        SwingUtilities.invokeLater(() -> {
            CrearGrupoFrame crearGrupoFrame = new CrearGrupoFrame(servicioCliente, username);
            crearGrupoFrame.setVisible(true);
        });
    }
    
    /**
     * Abrir ventana de notificaciones/invitaciones
     */
    private void abrirNotificaciones() {
        new Thread(() -> {
            try {
                ResponseDTO response = servicioCliente.obtenerInvitacionesPendientes();
                
                if (response.isExito()) {
                    @SuppressWarnings("unchecked")
                    java.util.List<com.chat.common.models.Invitacion> invitaciones = 
                        (java.util.List<com.chat.common.models.Invitacion>) response.getDato("invitaciones");
                    
                    SwingUtilities.invokeLater(() -> {
                        if (invitaciones == null || invitaciones.isEmpty()) {
                            JOptionPane.showMessageDialog(this,
                                "No tienes invitaciones pendientes",
                                "Invitaciones",
                                JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            invitacionHandler.mostrarDialogoInvitaciones(invitaciones, this);
                        }
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this,
                            "Error al obtener invitaciones: " + response.getMensaje(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    });
                }
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Cerrar sesión y volver al login
     */
    private void cerrarSesion() {
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Deseas cerrar sesión?",
            "Confirmar",
            JOptionPane.YES_NO_OPTION);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            // Desuscribirse de eventos antes de cerrar
            GestorEventos.obtenerInstancia().removerObservador(this);
            
            // Hacer logout en el servidor (esto cerrará la conexión)
            ResponseDTO logoutResponse = servicioCliente.logout();
            
            if (!logoutResponse.isExito()) {
                System.err.println("Error al hacer logout: " + logoutResponse.getMensaje());
            }
            
            // Cerrar esta ventana
            dispose();
            
            // Crear NUEVO ServicioCliente para la siguiente sesión (con nueva conexión)
            try {
                com.chat.cliente.negocio.ServicioCliente nuevoServicioCliente = 
                    new com.chat.cliente.negocio.ServicioCliente("localhost", 5000);
                
                // Abrir ventana de login con el nuevo servicio
                SwingUtilities.invokeLater(() -> {
                    LoginFrameRefactored loginFrame = new LoginFrameRefactored(nuevoServicioCliente, new LoginFrameRefactored.LoginCallback() {
                        @Override
                        public void onLoginExitoso(String usernameNuevo, ResponseDTO response) {
                            System.out.println("✓ Login exitoso: " + usernameNuevo);
                            
                            try {
                                // Configurar base de datos para el nuevo usuario
                                ConexionH2.establecerUsuario(usernameNuevo);
                                ConexionH2.cerrarConexion();
                                ConexionH2.inicializarBaseDatos();
                                System.out.println("✓ Base de datos del usuario inicializada");
                            } catch (Exception e) {
                                System.err.println("✗ Error al inicializar base de datos: " + e.getMessage());
                            }
                            
                            // Abrir nueva ventana de chat con el NUEVO servicio
                            SwingUtilities.invokeLater(() -> {
                                ChatPrincipalFrame chatFrame = new ChatPrincipalFrame(nuevoServicioCliente, usernameNuevo);
                                chatFrame.setVisible(true);
                            });
                        }
                        
                        @Override
                        public void onAbrirRegistro() {
                            System.out.println("→ Abriendo ventana de registro...");
                        }
                    });
                    
                    loginFrame.setVisible(true);
                    System.out.println("✓ Ventana de login abierta");
                });
            } catch (Exception e) {
                System.err.println("✗ Error al crear nueva conexión: " + e.getMessage());
                JOptionPane.showMessageDialog(null,
                    "Error al reconectar con el servidor: " + e.getMessage(),
                    "Error de Conexión",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Crear panel de conversación vacío
     */
    private JPanel crearPanelConversacion() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(236, 240, 241));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        return panel;
    }
    
    /**
     * Renderer personalizado para la lista de usuarios
     * Muestra todos los usuarios con su estado (En Línea / Desconectado) y foto de perfil
     */
    private class UsuarioListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            
            JLabel label = (JLabel) super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
            
            // Manejar canales/grupos
            if (value instanceof Canal) {
                Canal canal = (Canal) value;
                String canalKey = "CANAL_" + canal.getId();
                
                // Crear icono de foto del canal
                ImageIcon fotoIcon = UIHelper.crearIconoFoto(canal.getFoto(), 32, 32);
                label.setIcon(fotoIcon);
                label.setIconTextGap(10);
                
                // Obtener mensajes no leídos
                int noLeidos = mensajesNoLeidos.getOrDefault(canalKey, 0);
                
                // Construir texto con HTML para badge de color
                String textoHtml = "<html>👥 " + canal.getNombre() + " [Grupo]";
                if (noLeidos > 0) {
                    textoHtml += " <span style='color:#ef4444; font-weight:bold;'>● (" + noLeidos + ")</span>";
                }
                textoHtml += "</html>";
                
                label.setText(textoHtml);
                label.setFont(new Font("Segoe UI", Font.BOLD, 13));
                label.setBorder(new EmptyBorder(5, 5, 5, 5));
                
                if (isSelected) {
                    label.setBackground(new Color(155, 89, 182)); // Púrpura para grupos seleccionados
                    label.setForeground(Color.WHITE);
                } else {
                    label.setBackground(Color.WHITE);
                    label.setForeground(new Color(142, 68, 173)); // Púrpura oscuro
                }
                
                return label;
            }
            
            // Manejar usuarios
            boolean enLinea = false;
            String nombreUsuario = value.toString();
            byte[] fotoBytes = null;
            
            if (value instanceof Usuario) {
                Usuario usuario = (Usuario) value;
                nombreUsuario = usuario.getUsername();
                enLinea = usuario.isEnLinea();
                fotoBytes = usuario.getFoto();
            } else {
                // Si es solo el username string, buscar en la lista de usuarios
                for (Usuario u : usuariosConectados) {
                    if (u.getUsername().equals(value.toString())) {
                        enLinea = u.isEnLinea();
                        fotoBytes = u.getFoto();
                        break;
                    }
                }
            }
            
            // Crear icono de foto de perfil
            ImageIcon fotoIcon = UIHelper.crearIconoFoto(fotoBytes, 32, 32);
            label.setIcon(fotoIcon);
            label.setIconTextGap(10);
            
            // Formato: "● username [En Línea]" o "● username [Desconectado]"
            String indicadorEmoji = enLinea ? "●" : "●";
            String estadoTexto = enLinea ? "[En Línea]" : "[Desconectado]";
            
            // Obtener mensajes no leídos
            int noLeidos = mensajesNoLeidos.getOrDefault(nombreUsuario, 0);
            
            // Construir el texto con HTML para usar colores
            String colorIndicador = enLinea ? "#10b981" : "#ef4444"; // Verde/Rojo
            String colorBadge = "#ef4444"; // Rojo para badge
            String textoHtml = "<html><span style='color:" + colorIndicador + ";'>" + indicadorEmoji + "</span> " 
                             + nombreUsuario + " " + estadoTexto;
            if (noLeidos > 0) {
                textoHtml += " <span style='color:" + colorBadge + "; font-weight:bold;'>● (" + noLeidos + ")</span>";
            }
            textoHtml += "</html>";
            
            label.setText(textoHtml);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            label.setBorder(new EmptyBorder(5, 5, 5, 5));
            
            if (isSelected) {
                label.setBackground(new Color(52, 152, 219));
                label.setForeground(Color.WHITE);
            } else {
                label.setBackground(Color.WHITE);
                // Color diferente según el estado
                if (enLinea) {
                    label.setForeground(new Color(39, 174, 96)); // Verde oscuro para en línea
                } else {
                    label.setForeground(new Color(127, 140, 141)); // Gris para desconectado
                }
            }
            
            return label;
        }
    }
}
