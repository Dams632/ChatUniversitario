package com.chat.servidor.presentacion.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.chat.servidor.presentacion.ManejadorCliente;
import com.chat.servidor.presentacion.gui.utils.FontHelper;

/**
 * Ventana principal del servidor para gestionar conexiones de clientes
 */
public class ServidorFrame extends JFrame {
    
    private JTable tablaClientes;
    private DefaultTableModel modeloTabla;
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
    public ServidorFrame(List<ManejadorCliente> clientesConectados) {
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
        String[] columnas = {"ID", "Usuario", "IP", "Estado", "Hora Conexión"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaClientes = new JTable(modeloTabla);
        tablaClientes.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaClientes.setRowHeight(30);
        tablaClientes.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaClientes.getTableHeader().setBackground(new Color(52, 152, 219));
        tablaClientes.getTableHeader().setForeground(Color.BLACK);
        tablaClientes.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaClientes.setSelectionBackground(new Color(52, 152, 219, 50));
        
        // Ajustar anchos de columnas
        tablaClientes.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaClientes.getColumnModel().getColumn(1).setPreferredWidth(150);
        tablaClientes.getColumnModel().getColumn(2).setPreferredWidth(120);
        tablaClientes.getColumnModel().getColumn(3).setPreferredWidth(100);
        tablaClientes.getColumnModel().getColumn(4).setPreferredWidth(150);
        
        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crear panel derecho con controles y log
     */
    private JPanel crearPanelDerecho() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(236, 240, 241));
        panel.setPreferredSize(new Dimension(250, 0));
        
        // Panel de controles
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
        
        // Botón actualizar
        btnActualizar = crearBoton("🔄 Actualizar", new Color(52, 152, 219));
        btnActualizar.addActionListener(e -> actualizarTabla());
        controlesPanel.add(btnActualizar);
        
        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Botón desconectar seleccionado
        btnDesconectar = crearBoton("🚫 Desconectar Cliente", new Color(231, 76, 60));
        btnDesconectar.addActionListener(e -> desconectarClienteSeleccionado());
        controlesPanel.add(btnDesconectar);
        
        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Botón desconectar todos
        btnDesconectarTodos = crearBoton("⛔ Desconectar Todos", new Color(192, 57, 43));
        btnDesconectarTodos.addActionListener(e -> desconectarTodos());
        controlesPanel.add(btnDesconectarTodos);
        
        controlesPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Separador
        JLabel lblBroadcast = new JLabel("Mensajes Broadcast");
        lblBroadcast.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblBroadcast.setForeground(new Color(52, 73, 94));
        lblBroadcast.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlesPanel.add(lblBroadcast);
        
        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Botón broadcast usuarios
        JButton btnBroadcastUsuarios = crearBoton("📢 Enviar a Usuarios", new Color(41, 128, 185));
        btnBroadcastUsuarios.addActionListener(e -> enviarBroadcastUsuarios());
        controlesPanel.add(btnBroadcastUsuarios);
        
        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Botón broadcast canales
        JButton btnBroadcastCanales = crearBoton("📢 Enviar a Canales", new Color(142, 68, 173));
        btnBroadcastCanales.addActionListener(e -> enviarBroadcastCanales());
        controlesPanel.add(btnBroadcastCanales);
        
        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Botón broadcast global
        JButton btnBroadcastGlobal = crearBoton("📣 Broadcast Global", new Color(243, 156, 18));
        btnBroadcastGlobal.addActionListener(e -> enviarBroadcastGlobal());
        controlesPanel.add(btnBroadcastGlobal);
        
        controlesPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Separador
        JLabel lblInformes = new JLabel("Informes");
        lblInformes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblInformes.setForeground(new Color(52, 73, 94));
        lblInformes.setAlignmentX(Component.LEFT_ALIGNMENT);
        controlesPanel.add(lblInformes);
        
        controlesPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Botón informes
        JButton btnInformes = crearBoton("📊 Ver Informes", new Color(155, 89, 182));
        btnInformes.addActionListener(e -> abrirInformes());
        controlesPanel.add(btnInformes);
        
        panel.add(controlesPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Panel de log
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
        modeloTabla.setRowCount(0);
        
        synchronized (clientesConectados) {
            int id = 1;
            for (ManejadorCliente cliente : clientesConectados) {
                if (cliente.isConectado()) {
                    Object[] fila = {
                        id++,
                        cliente.getUsername() != null ? cliente.getUsername() : "No autenticado",
                        cliente.getDireccionIP(),
                        cliente.isAutenticado() ? "Autenticado" : "Conectado",
                        cliente.getHoraConexion()
                    };
                    modeloTabla.addRow(fila);
                }
            }
        }
        
        lblTotalClientes.setText("Clientes: " + modeloTabla.getRowCount());
        agregarLog("Tabla actualizada. Clientes activos: " + modeloTabla.getRowCount());
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
        
        String username = (String) modeloTabla.getValueAt(filaSeleccionada, 1);
        String ip = (String) modeloTabla.getValueAt(filaSeleccionada, 2);
        
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
            String timestamp = java.time.LocalTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            txtLog.append("[" + timestamp + "] " + mensaje + "\n");
            txtLog.setCaretPosition(txtLog.getDocument().getLength());
        });
    }
}
