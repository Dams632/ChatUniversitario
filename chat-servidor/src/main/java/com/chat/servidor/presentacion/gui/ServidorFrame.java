package com.chat.servidor.presentacion.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.JTabbedPane;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;

import com.chat.servidor.presentacion.ManejadorCliente;
import com.chat.servidor.presentacion.ServidorChat;
import com.chat.servidor.presentacion.ServidorChat.ConexionRemotaInfo;
import com.chat.servidor.presentacion.gui.utils.FontHelper;

/**
 * Ventana principal del servidor para gestionar conexiones de clientes
 */
public class ServidorFrame extends JFrame {
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern IP_PATTERN = Pattern.compile("^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)\\.?\\b){4}$");
    private static final DateTimeFormatter HORA_FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ServidorChat servidor;
    
    private JTable tablaClientes;
    private JTable tablaServidores;
    private DefaultTableModel modeloTablaClientes;
    private DefaultTableModel modeloTablaServidores;
    private JLabel lblEstado;
    private JLabel lblTotalClientes;
    private JButton btnDesconectar;
    private JButton btnDesconectarTodos;
    private JButton btnActualizar;
    private JTextArea txtLog;
    
    private List<ManejadorCliente> clientesConectados;
    
    /**
     * Constructor
     */
    public ServidorFrame(ServidorChat servidor, List<ManejadorCliente> clientesConectados) {
        this.servidor = servidor;
        this.clientesConectados = clientesConectados;
        initComponents();
    }
    
    /**
     * Inicializar componentes
     */
    private void initComponents() {
        setTitle("Servidor de Chat Universitario - Panel de Control");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Panel superior - Encabezado
        JPanel headerPanel = crearPanelEncabezado();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Panel central - Tabla de clientes
        JPanel centerPanel = crearPanelTabla();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Panel derecho - Controles y Log
        JPanel rightPanel = crearPanelDerecho();
        mainPanel.add(rightPanel, BorderLayout.EAST);
        
        add(mainPanel);
    }
    
    /**
     * Crear panel de encabezado
     */
    private JPanel crearPanelEncabezado() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(52, 73, 94));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel lblTitulo = new JLabel("🖥️ Servidor de Chat Universitario");
        lblTitulo.setFont(FontHelper.getBoldLabelFont(24));
        lblTitulo.setForeground(Color.WHITE);
        panel.add(lblTitulo, BorderLayout.WEST);
        
        // Panel de estado
        JPanel estadoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        estadoPanel.setOpaque(false);
        
        lblEstado = new JLabel("● En línea");
        lblEstado.setFont(FontHelper.getBoldLabelFont(14));
        lblEstado.setForeground(new Color(46, 204, 113));
        estadoPanel.add(lblEstado);
        
        lblTotalClientes = new JLabel("Clientes: 0");
        lblTotalClientes.setFont(FontHelper.getBoldLabelFont(14));
        lblTotalClientes.setForeground(Color.WHITE);
        estadoPanel.add(lblTotalClientes);
        
        panel.add(estadoPanel, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Crear panel de tabla de clientes
     */
    private JPanel crearPanelTabla() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));
        
        // Título de sección
        JLabel lblTitulo = new JLabel("Clientes Conectados");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Tabla
        String[] columnasClientes = {"#", "Usuario", "IP", "Estado", "Hora Conexión"};
        modeloTablaClientes = new DefaultTableModel(columnasClientes, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaClientes = new JTable(modeloTablaClientes);
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaClientes.setRowHeight(30);
        tablaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaClientes.getTableHeader().setBackground(new Color(52, 152, 219));
        tablaClientes.getTableHeader().setForeground(Color.BLACK);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.setSelectionBackground(new Color(52, 152, 219, 50));
        
        // Ajustar anchos de columnas
        tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(80);
        tablaClientes.getColumnModel().getColumn(1).setPreferredWidth(160);
        tablaClientes.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaClientes.getColumnModel().getColumn(3).setPreferredWidth(110);
        tablaClientes.getColumnModel().getColumn(4).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        
        // Tabla de servidores
        String[] columnasServidores = {"Servidor", "IP", "Puerto", "Desde"};
        modeloTablaServidores = new DefaultTableModel(columnasServidores, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        tablaServidores = new JTable(modeloTablaServidores);
        tablaServidores.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaServidores.setRowHeight(30);
        tablaServidores.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaServidores.getTableHeader().setBackground(new Color(46, 204, 113));
        tablaServidores.getTableHeader().setForeground(Color.BLACK);
        tablaServidores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        tablaServidores.getColumnModel().getColumn(0).setPreferredWidth(180);
        tablaServidores.getColumnModel().getColumn(1).setPreferredWidth(140);
        tablaServidores.getColumnModel().getColumn(2).setPreferredWidth(100);
        tablaServidores.getColumnModel().getColumn(3).setPreferredWidth(140);

        JScrollPane scrollServidores = new JScrollPane(tablaServidores);
        scrollServidores.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pestañas.addTab("Clientes", scrollPane);
        pestañas.addTab("Servidores P2P", scrollServidores);

        panel.add(pestañas, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crear panel derecho con controles y log
     */
    private JPanel crearPanelDerecho() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setPreferredSize(new Dimension(280, 0));
        contenedor.setBackground(new Color(236, 240, 241));

        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        pestañas.addTab("Panel", crearTabGestion());
        pestañas.addTab("Conexión P2P", crearTabConexionP2P());

        contenedor.add(pestañas, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearTabGestion() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(236, 240, 241));

        JPanel controlesPanel = new JPanel();
        controlesPanel.setLayout(new BoxLayout(controlesPanel, BoxLayout.Y_AXIS));
        controlesPanel.setBackground(Color.WHITE);
        controlesPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel lblControles = new JLabel("Controles");
        lblControles.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblControles.setForeground(new Color(52, 73, 94));
        lblControles.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlesPanel.add(lblControles);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JLabel lblUsuarios = new JLabel("Gestión de Usuarios");
        lblUsuarios.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblUsuarios.setForeground(new Color(52, 73, 94));
        lblUsuarios.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlesPanel.add(lblUsuarios);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnCrearUsuario = crearBoton("➕ Crear Usuario", new Color(39, 174, 96));
        btnCrearUsuario.addActionListener(e -> mostrarDialogoCrearUsuario());
        controlesPanel.add(btnCrearUsuario);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        btnActualizar = crearBoton("🔄 Actualizar", new Color(52, 152, 219));
        btnActualizar.addActionListener(e -> actualizarTabla());
        controlesPanel.add(btnActualizar);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        btnDesconectar = crearBoton("🚫 Desconectar Cliente", new Color(231, 76, 60));
        btnDesconectar.addActionListener(e -> desconectarClienteSeleccionado());
        controlesPanel.add(btnDesconectar);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        btnDesconectarTodos = crearBoton("⛔ Desconectar Todos", new Color(192, 57, 43));
        btnDesconectarTodos.addActionListener(e -> desconectarTodos());
        controlesPanel.add(btnDesconectarTodos);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblBroadcast = new JLabel("Mensajes Broadcast");
        lblBroadcast.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBroadcast.setForeground(new Color(52, 73, 94));
        lblBroadcast.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlesPanel.add(lblBroadcast);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnBroadcastUsuarios = crearBoton("📢 Enviar a Usuarios", new Color(41, 128, 185));
        btnBroadcastUsuarios.addActionListener(e -> enviarBroadcastUsuarios());
        controlesPanel.add(btnBroadcastUsuarios);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnBroadcastCanales = crearBoton("📢 Enviar a Canales", new Color(142, 68, 173));
        btnBroadcastCanales.addActionListener(e -> enviarBroadcastCanales());
        controlesPanel.add(btnBroadcastCanales);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnBroadcastGlobal = crearBoton("📣 Broadcast Global", new Color(243, 156, 18));
        btnBroadcastGlobal.addActionListener(e -> enviarBroadcastGlobal());
        controlesPanel.add(btnBroadcastGlobal);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        JLabel lblInformes = new JLabel("Informes");
        lblInformes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInformes.setForeground(new Color(52, 73, 94));
        lblInformes.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlesPanel.add(lblInformes);

        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnInformes = crearBoton("📊 Ver Informes", new Color(155, 89, 182));
        btnInformes.addActionListener(e -> abrirInformes());
        controlesPanel.add(btnInformes);

        panel.add(controlesPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        JPanel logPanel = new JPanel(new BorderLayout(5, 5));
        logPanel.setBackground(Color.WHITE);
        logPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel lblLog = new JLabel("Log de Eventos");
        lblLog.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblLog.setForeground(new Color(52, 73, 94));
        logPanel.add(lblLog, BorderLayout.NORTH);

        txtLog = new JTextArea();
        txtLog.setFont(new Font("Consolas", Font.PLAIN, 11));
        txtLog.setEditable(false);
        txtLog.setLineWrap(true);
        txtLog.setWrapStyleWord(true);
        txtLog.setBackground(new Color(44, 62, 80));
        txtLog.setForeground(new Color(236, 240, 241));

        JScrollPane scrollLog = new JScrollPane(txtLog);
        scrollLog.setPreferredSize(new Dimension(230, 200));
        logPanel.add(scrollLog, BorderLayout.CENTER);

        panel.add(logPanel);

        return panel;
    }

    private JPanel crearTabConexionP2P() {
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(new Color(236, 240, 241));

        JPanel tarjeta = new JPanel(new GridBagLayout());
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 0);

        JLabel lblTitulo = new JLabel("Conectar a otro servidor");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitulo.setForeground(new Color(52, 73, 94));
        tarjeta.add(lblTitulo, gbc);

        gbc.gridy++;
        JLabel lblDescripcion = new JLabel("Ingresa la IP y puerto del servidor remoto para sincronizar P2P.");
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDescripcion.setForeground(new Color(96, 106, 116));
        tarjeta.add(lblDescripcion, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(10, 0, 6, 10);
        tarjeta.add(new JLabel("Dirección IP"), gbc);

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1;
        gbcField.gridy = gbc.gridy;
        gbcField.weightx = 1.0;
        gbcField.insets = new Insets(10, 0, 6, 0);
        gbcField.fill = GridBagConstraints.HORIZONTAL;

        String hostPorDefecto = servidor.getHost();
        if (hostPorDefecto == null || hostPorDefecto.isBlank() || "0.0.0.0".equals(hostPorDefecto)) {
            hostPorDefecto = "127.0.0.1";
        }
        JTextField txtHost = new JTextField(hostPorDefecto);
        txtHost.setColumns(12);
        tarjeta.add(txtHost, gbcField);

        gbc.gridy++;
        gbc.insets = new Insets(6, 0, 6, 10);
        tarjeta.add(new JLabel("Puerto P2P"), gbc);

        GridBagConstraints gbcPort = new GridBagConstraints();
        gbcPort.gridx = 1;
        gbcPort.gridy = gbc.gridy;
        gbcPort.weightx = 1.0;
        gbcPort.insets = new Insets(6, 0, 6, 0);
        gbcPort.fill = GridBagConstraints.HORIZONTAL;

        int puertoPorDefecto = servidor.getPuertoP2P() > 0 ? servidor.getPuertoP2P() : servidor.getPuerto() + 100;
        JTextField txtPuerto = new JTextField(String.valueOf(puertoPorDefecto));
        txtPuerto.setColumns(8);
        tarjeta.add(txtPuerto, gbcPort);

        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 0, 6, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        JButton btnConectar = crearBoton("🔗 Conectar", new Color(46, 204, 113));
        btnConectar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        tarjeta.add(btnConectar, gbc);

        gbc.gridy++;
        gbc.insets = new Insets(6, 0, 0, 0);
        gbc.fill = GridBagConstraints.NONE;
        JLabel lblEstadoConexion = new JLabel("Esperando datos de conexión.");
        lblEstadoConexion.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        lblEstadoConexion.setForeground(new Color(127, 140, 141));
        tarjeta.add(lblEstadoConexion, gbc);

        btnConectar.addActionListener(e -> intentarConexionP2P(txtHost.getText(), txtPuerto.getText(), lblEstadoConexion));
        txtPuerto.addActionListener(e -> intentarConexionP2P(txtHost.getText(), txtPuerto.getText(), lblEstadoConexion));

        JLabel lblAyuda = new JLabel("Puerto local P2P: " + servidor.getHost() + ":" + servidor.getPuertoP2P());
        lblAyuda.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblAyuda.setForeground(new Color(96, 106, 116));
        GridBagConstraints gbcAyuda = new GridBagConstraints();
        gbcAyuda.gridx = 0;
        gbcAyuda.gridy = gbc.gridy + 1;
        gbcAyuda.gridwidth = 2;
        gbcAyuda.anchor = GridBagConstraints.WEST;
        gbcAyuda.insets = new Insets(10, 0, 0, 0);
        tarjeta.add(lblAyuda, gbcAyuda);

        contenedor.add(tarjeta);
        contenedor.add(Box.createVerticalGlue());

        return contenedor;
    }

    private void intentarConexionP2P(String hostTexto, String puertoTexto, JLabel lblEstadoConexion) {
        String host = hostTexto != null ? hostTexto.trim() : "";
        String puertoStr = puertoTexto != null ? puertoTexto.trim() : "";

        if (host.isEmpty()) {
            lblEstadoConexion.setForeground(new Color(192, 57, 43));
            lblEstadoConexion.setText("Ingresa la IP o hostname del servidor.");
            JOptionPane.showMessageDialog(this, "Debes ingresar una dirección IP válida.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!"localhost".equalsIgnoreCase(host) && !IP_PATTERN.matcher(host).matches()) {
            lblEstadoConexion.setForeground(new Color(192, 57, 43));
            lblEstadoConexion.setText("La dirección debe ser una IP válida (ej. 192.168.1.10).");
            JOptionPane.showMessageDialog(this, "La dirección debe ser una IP válida o localhost.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (puertoStr.isEmpty()) {
            lblEstadoConexion.setForeground(new Color(192, 57, 43));
            lblEstadoConexion.setText("Ingresa el puerto del servidor.");
            JOptionPane.showMessageDialog(this, "Debes ingresar un puerto.", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int puerto;
        try {
            puerto = Integer.parseInt(puertoStr);
        } catch (NumberFormatException ex) {
            lblEstadoConexion.setForeground(new Color(192, 57, 43));
            lblEstadoConexion.setText("El puerto debe ser un número válido.");
            JOptionPane.showMessageDialog(this, "El puerto debe ser un número válido.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (puerto <= 0 || puerto > 65535) {
            lblEstadoConexion.setForeground(new Color(192, 57, 43));
            lblEstadoConexion.setText("El puerto debe estar entre 1 y 65535.");
            JOptionPane.showMessageDialog(this, "El puerto debe estar entre 1 y 65535.", "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        boolean conectado = servidor.conectarServidorP2P(host, puerto);
        if (conectado) {
            lblEstadoConexion.setForeground(new Color(39, 174, 96));
            lblEstadoConexion.setText("Conectado con " + host + ":" + puerto + ".");
            agregarLog("Conexión P2P manual establecida con " + host + ":" + puerto);
            JOptionPane.showMessageDialog(this, "Conexión P2P establecida con " + host + ":" + puerto + ".", "Conectado", JOptionPane.INFORMATION_MESSAGE);
        } else {
            lblEstadoConexion.setForeground(new Color(192, 57, 43));
            lblEstadoConexion.setText("No se pudo conectar con " + host + ":" + puerto + ".");
            JOptionPane.showMessageDialog(this, "No se pudo establecer la conexión P2P con " + host + ":" + puerto + ".", "Conexión fallida", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarDialogoCrearUsuario() {
    JTextField txtUsername = new JTextField();
    JTextField txtEmail = new JTextField();
    JTextField txtIp = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JPasswordField txtConfirmar = new JPasswordField();
        JLabel lblFotoPreview = new JLabel("Sin foto");
        lblFotoPreview.setHorizontalAlignment(JLabel.CENTER);
        lblFotoPreview.setPreferredSize(new Dimension(90, 90));
    lblFotoPreview.setOpaque(true);
    lblFotoPreview.setBackground(new Color(236, 240, 241));
    lblFotoPreview.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
    lblFotoPreview.setFont(new Font("Segoe UI", Font.ITALIC, 10));
    lblFotoPreview.setForeground(Color.GRAY);
        final byte[][] fotoSeleccionada = new byte[1][];

    JButton btnSeleccionarFoto = new JButton("Seleccionar foto...");
    btnSeleccionarFoto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    btnSeleccionarFoto.setBackground(new Color(149, 165, 166));
    btnSeleccionarFoto.setForeground(Color.WHITE);
    btnSeleccionarFoto.setFocusPainted(false);
    btnSeleccionarFoto.setBorderPainted(false);
    btnSeleccionarFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
    btnSeleccionarFoto.addActionListener(evt -> seleccionarFoto(lblFotoPreview, fotoSeleccionada));

    Dimension fieldSize = new Dimension(240, 26);
    txtUsername.setPreferredSize(fieldSize);
    txtEmail.setPreferredSize(fieldSize);
    txtIp.setPreferredSize(fieldSize);
    txtPassword.setPreferredSize(fieldSize);
    txtConfirmar.setPreferredSize(fieldSize);

    JPanel panel = new JPanel(new GridBagLayout());
    panel.setBackground(Color.WHITE);
    panel.setBorder(new EmptyBorder(10, 5, 10, 5));

    GridBagConstraints gbcLabel = new GridBagConstraints();
    gbcLabel.gridx = 0;
    gbcLabel.anchor = GridBagConstraints.WEST;
    gbcLabel.insets = new Insets(6, 8, 6, 8);

    GridBagConstraints gbcField = new GridBagConstraints();
    gbcField.gridx = 1;
    gbcField.weightx = 1.0;
    gbcField.fill = GridBagConstraints.HORIZONTAL;
    gbcField.insets = new Insets(6, 8, 6, 8);

    int fila = 0;

    gbcLabel.gridy = fila;
    gbcField.gridy = fila;
    panel.add(new JLabel("Usuario:"), gbcLabel);
    panel.add(txtUsername, gbcField);

    fila++;
    gbcLabel.gridy = fila;
    gbcField.gridy = fila;
    panel.add(new JLabel("Correo electrónico:"), gbcLabel);
    panel.add(txtEmail, gbcField);

    fila++;
    gbcLabel.gridy = fila;
    gbcField.gridy = fila;
    panel.add(new JLabel("Dirección IP:"), gbcLabel);
    panel.add(txtIp, gbcField);

    fila++;
    gbcLabel.gridy = fila;
    gbcField.gridy = fila;
    panel.add(new JLabel("Contraseña:"), gbcLabel);
    panel.add(txtPassword, gbcField);

    fila++;
    gbcLabel.gridy = fila;
    gbcField.gridy = fila;
    panel.add(new JLabel("Confirmar contraseña:"), gbcLabel);
    panel.add(txtConfirmar, gbcField);

    fila++;
    gbcLabel.gridy = fila;
    gbcField.gridy = fila;
    panel.add(new JLabel("Foto de perfil (opcional):"), gbcLabel);

    JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
    fotoPanel.setBackground(Color.WHITE);
    fotoPanel.add(lblFotoPreview);
    fotoPanel.add(btnSeleccionarFoto);
    panel.add(fotoPanel, gbcField);

        int resultado = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Crear nuevo usuario",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE
        );

        if (resultado != JOptionPane.OK_OPTION) {
            return;
        }

        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String ip = txtIp.getText().trim();
        char[] passwordChars = txtPassword.getPassword();
        char[] confirmarChars = txtConfirmar.getPassword();
        String password = new String(passwordChars);
        String confirmar = new String(confirmarChars);
        Arrays.fill(passwordChars, '\0');
        Arrays.fill(confirmarChars, '\0');

        if (username.isEmpty() || email.isEmpty() || ip.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios", "Datos incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String validationError = validarDatosRegistro(username, email, ip, password, confirmar);
        if (validationError != null) {
            JOptionPane.showMessageDialog(this, validationError, "Validación", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            servidor.crearUsuarioDesdeServidor(username, email, password, ip, fotoSeleccionada[0]);
            JOptionPane.showMessageDialog(this, "Usuario creado correctamente", "Usuario creado", JOptionPane.INFORMATION_MESSAGE);
            agregarLog("Usuario creado desde panel: " + username);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error al crear usuario", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "No se pudo crear el usuario: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private String validarDatosRegistro(String username, String email, String ip, String password, String confirmar) {
        if (username.length() < MIN_USERNAME_LENGTH) {
            return "El usuario debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres";
        }

        if (username.length() > MAX_USERNAME_LENGTH) {
            return "El usuario no puede superar " + MAX_USERNAME_LENGTH + " caracteres";
        }

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "El usuario solo puede contener letras, números, guiones y guiones bajos";
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return "Ingresa un correo electrónico válido";
        }

        if (!IP_PATTERN.matcher(ip).matches()) {
            return "Ingresa una dirección IP válida (ej. 192.168.1.10)";
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            return "La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres";
        }

        if (!password.equals(confirmar)) {
            return "Las contraseñas no coinciden";
        }

        return null;
    }

    private void seleccionarFoto(JLabel lblFotoPreview, byte[][] fotoSeleccionada) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar foto de perfil");
        chooser.setFileFilter(new FileNameExtensionFilter("Imágenes", "png", "jpg", "jpeg", "gif"));
        chooser.setAcceptAllFileFilterUsed(false);

        int resultado = chooser.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File archivo = chooser.getSelectedFile();
        try {
            BufferedImage imagen = ImageIO.read(archivo);
            if (imagen == null) {
                throw new IOException("Formato de imagen no soportado");
            }

            String nombre = archivo.getName().toLowerCase();
            String formato = (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) ? "jpg" : "png";
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                ImageIO.write(imagen, formato, baos);
                fotoSeleccionada[0] = baos.toByteArray();
            }

            Image escalada = imagen.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            lblFotoPreview.setIcon(new ImageIcon(escalada));
            lblFotoPreview.setText("");
            lblFotoPreview.setToolTipText(archivo.getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la imagen: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    /**
     * Crear botón con estilo
     */
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(FontHelper.getButtonFont(12));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setMaximumSize(new Dimension(220, 40));
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
            }
        });
        
        return boton;
    }
    
    /**
     * Actualizar tabla de clientes
     */
    public void actualizarTabla() {
        modeloTablaClientes.setRowCount(0);
        modeloTablaServidores.setRowCount(0);

        int clientesActivos = 0;
        List<ConexionRemotaInfo> conexionesP2P = servidor.obtenerConexionesP2PActivas();
        java.util.Set<String> hostsP2P = conexionesP2P.stream()
            .map(ConexionRemotaInfo::getHost)
            .filter(host -> host != null && !host.isBlank())
            .collect(java.util.stream.Collectors.toSet());

        synchronized (clientesConectados) {
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isConectado()) {
                    String hostCliente = cliente.getDireccionIPSinPuerto();
                    if (!cliente.isAutenticado() && hostCliente != null && hostsP2P.contains(hostCliente)) {
                        // Conexión sin autenticar desde un host que ya está enlazado como servidor P2P.
                        // Se asume que corresponde al socket P2P de otro servidor; no se muestra en la tabla de clientes.
                        continue;
                    }

                    Object[] fila = {
                        clientesActivos + 1,
                        cliente.getUsername() != null ? cliente.getUsername() : "No autenticado",
                        cliente.getDireccionIP(),
                        cliente.isAutenticado() ? "Autenticado" : "Conectado",
                        cliente.getHoraConexion()
                    };
                    modeloTablaClientes.addRow(fila);
                    clientesActivos++;
                }
            }
        }

        for (ConexionRemotaInfo conexion : conexionesP2P) {
            String hostRemoto = conexion.getHost() != null && !conexion.getHost().isBlank()
                ? conexion.getHost()
                : "-";
            String puertoRemoto = conexion.getPuerto() > 0 ? String.valueOf(conexion.getPuerto()) : "-";

            Object[] fila = {
                conexion.getServidorId(),
                hostRemoto,
                puertoRemoto,
                formatearHora(conexion.getConectadoDesde())
            };
            modeloTablaServidores.addRow(fila);
        }

        lblTotalClientes.setText("Clientes: " + clientesActivos + " | Servidores: " + conexionesP2P.size());
    }
    
    /**
     * Desconectar cliente seleccionado
     */
    private void desconectarClienteSeleccionado() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this,
                "Por favor selecciona un cliente de la tabla",
                "Ningún cliente seleccionado",
                JOptionPane.WARNING_MESSAGE);
            return;
        }

        String username = (String) modeloTablaClientes.getValueAt(filaSeleccionada, 1);
        String ip = (String) modeloTablaClientes.getValueAt(filaSeleccionada, 2);
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Deseas desconectar al cliente?\n\nUsuario: " + username + "\nIP: " + ip,
            "Confirmar desconexión",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            synchronized (clientesConectados) {
                for (ManejadorCliente cliente : clientesConectados) {
                    if (cliente.getDireccionIP().equals(ip) && cliente.isConectado()) {
                        cliente.desconectar();
                        agregarLog("Cliente desconectado: " + username + " (" + ip + ")");
                        actualizarTabla();
                        break;
                    }
                }
            }
        }
    }
    
    /**
     * Desconectar todos los clientes
     */
    private void desconectarTodos() {
        if (clientesConectados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "No hay clientes conectados",
                "Sin clientes",
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        int confirmacion = JOptionPane.showConfirmDialog(this,
            "¿Deseas desconectar a TODOS los clientes?\n\nTotal: " + clientesConectados.size(),
            "Confirmar desconexión masiva",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
        
        if (confirmacion == JOptionPane.YES_OPTION) {
            synchronized (clientesConectados) {
                int desconectados = 0;
                for (ManejadorCliente cliente : clientesConectados) {
                    if (cliente.isConectado()) {
                        cliente.desconectar();
                        desconectados++;
                    }
                }
                agregarLog("Desconectados " + desconectados + " clientes");
            }
            actualizarTabla();
        }
    }
    
    /**
     * Abrir ventana de informes
     */
    private void abrirInformes() {
        InformesFrame informes = new InformesFrame();
        informes.setVisible(true);
        agregarLog("Ventana de informes abierta");
    }
    
    /**
     * Enviar broadcast a todos los usuarios
     */
    private void enviarBroadcastUsuarios() {
        String mensaje = JOptionPane.showInputDialog(this,
            "Ingresa el mensaje a enviar a todos los usuarios:",
            "Broadcast a Usuarios",
            JOptionPane.PLAIN_MESSAGE);
        
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            int usuarios = com.chat.servidor.presentacion.ServidorChat.getInstance()
                .enviarMensajeBroadcastUsuarios(mensaje.trim());
            
            JOptionPane.showMessageDialog(this,
                "Mensaje enviado a " + usuarios + " usuarios conectados",
                "Broadcast Completado",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Enviar broadcast a todos los canales
     */
    private void enviarBroadcastCanales() {
        String mensaje = JOptionPane.showInputDialog(this,
            "Ingresa el mensaje a enviar a todos los canales/grupos:",
            "Broadcast a Canales",
            JOptionPane.PLAIN_MESSAGE);
        
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            int canales = com.chat.servidor.presentacion.ServidorChat.getInstance()
                .enviarMensajeBroadcastCanales(mensaje.trim());
            
            JOptionPane.showMessageDialog(this,
                "Mensaje enviado a " + canales + " canales/grupos",
                "Broadcast Completado",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Enviar broadcast global (usuarios + canales)
     */
    private void enviarBroadcastGlobal() {
        String mensaje = JOptionPane.showInputDialog(this,
            "Ingresa el mensaje a enviar a TODOS (usuarios y canales):",
            "Broadcast Global",
            JOptionPane.PLAIN_MESSAGE);
        
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            com.chat.servidor.presentacion.ServidorChat.getInstance()
                .enviarMensajeBroadcastGlobal(mensaje.trim());
            
            JOptionPane.showMessageDialog(this,
                "Mensaje broadcast enviado a todos los usuarios y canales",
                "Broadcast Global Completado",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Agregar mensaje al log
     */
    public void agregarLog(String mensaje) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().format(HORA_FORMATO);
            txtLog.append("[" + timestamp + "] " + mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }

    private String formatearHora(long epochMillis) {
        if (epochMillis <= 0) {
            return "-";
        }
        LocalDateTime fecha = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault());
        return HORA_FORMATO.format(fecha);
    }
}
