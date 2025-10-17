package com.chat.cliente.presentacion.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Diálogo para configurar la conexión al servidor
 * Permite al usuario ingresar la IP y puerto antes de conectarse
 */
public class ConexionServidorDialog extends JDialog {
    
    private JTextField txtHost;
    private JTextField txtPuerto;
    private JButton btnConectar;
    private JButton btnCancelar;
    private JLabel lblEstado;
    
    private String host;
    private int puerto;
    private boolean conectado = false;
    
    /**
     * Constructor
     */
    public ConexionServidorDialog(Frame parent) {
        super(parent, "Conectar al Servidor", true);
        
        // Valores por defecto
        this.host = "localhost";
        this.puerto = 5000;
        
        initComponents();
    }
    
    /**
     * Inicializar componentes
     */
    private void initComponents() {
        setSize(450, 320);
        setLocationRelativeTo(getParent());
        setResizable(false);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Panel principal con padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        mainPanel.setBackground(Color.WHITE);
        
        // Panel superior con título e ícono
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);
        
        // Panel central con campos de entrada
        JPanel centerPanel = createCenterPanel();
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        
        // Panel inferior con botones
        JPanel bottomPanel = createBottomPanel();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Manejar cierre de ventana
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
    }
    
    /**
     * Crear panel superior con título
     */
    private JPanel createTopPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        // Ícono de servidor
        JLabel lblIcono = new JLabel("🖥️");
        lblIcono.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        lblIcono.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblIcono, BorderLayout.WEST);
        
        // Título y descripción
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setBackground(Color.WHITE);
        
        JLabel lblTitulo = new JLabel("Configuración del Servidor");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(52, 73, 94));
        
        JLabel lblDescripcion = new JLabel("Ingresa la dirección del servidor de chat");
        lblDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblDescripcion.setForeground(new Color(127, 140, 141));
        
        textPanel.add(lblTitulo);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(lblDescripcion);
        
        panel.add(textPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Crear panel central con campos de entrada
     */
    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Campo Host/IP
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.3;
        JLabel lblHost = new JLabel("Dirección IP:");
        lblHost.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblHost, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtHost = new JTextField("localhost", 20);
        txtHost.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtHost.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.add(txtHost, gbc);
        
        // Campo Puerto
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel lblPuerto = new JLabel("Puerto:");
        lblPuerto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblPuerto, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        txtPuerto = new JTextField("5000", 20);
        txtPuerto.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPuerto.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.add(txtPuerto, gbc);
        
        // Etiqueta de estado
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(lblEstado, gbc);
        
        // Agregar listener para Enter
        ActionListener enterListener = e -> conectar();
        txtHost.addActionListener(enterListener);
        txtPuerto.addActionListener(enterListener);
        
        return panel;
    }
    
    /**
     * Crear panel inferior con botones
     */
    private JPanel createBottomPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setBackground(Color.WHITE);
        
        // Botón Cancelar
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnCancelar.setPreferredSize(new Dimension(110, 40));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setBackground(new Color(236, 240, 241));
        btnCancelar.setForeground(new Color(52, 73, 94));
        btnCancelar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> cancelar());
        
        // Botón Conectar
        btnConectar = new JButton("Conectar");
        btnConectar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConectar.setPreferredSize(new Dimension(110, 40));
        btnConectar.setFocusPainted(false);
        btnConectar.setBackground(new Color(52, 152, 219));
        btnConectar.setForeground(Color.WHITE);
        btnConectar.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        btnConectar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConectar.addActionListener(e -> conectar());
        
        // Efectos hover
        agregarEfectosHover();
        
        panel.add(btnCancelar);
        panel.add(btnConectar);
        
        return panel;
    }
    
    /**
     * Agregar efectos hover a los botones
     */
    private void agregarEfectosHover() {
        btnConectar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnConectar.setBackground(new Color(41, 128, 185));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnConectar.setBackground(new Color(52, 152, 219));
            }
        });
        
        btnCancelar.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnCancelar.setBackground(new Color(220, 224, 225));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnCancelar.setBackground(new Color(236, 240, 241));
            }
        });
    }
    
    /**
     * Validar y conectar
     */
    private void conectar() {
        String hostInput = txtHost.getText().trim();
        String puertoInput = txtPuerto.getText().trim();
        
        // Validar que no estén vacíos
        if (hostInput.isEmpty()) {
            mostrarError("Por favor ingresa la dirección IP del servidor");
            txtHost.requestFocus();
            return;
        }
        
        if (puertoInput.isEmpty()) {
            mostrarError("Por favor ingresa el puerto del servidor");
            txtPuerto.requestFocus();
            return;
        }
        
        // Validar puerto
        try {
            int puertoNum = Integer.parseInt(puertoInput);
            
            if (puertoNum < 1 || puertoNum > 65535) {
                mostrarError("El puerto debe estar entre 1 y 65535");
                txtPuerto.requestFocus();
                return;
            }
            
            // Guardar valores y cerrar
            this.host = hostInput;
            this.puerto = puertoNum;
            this.conectado = true;
            
            dispose();
            
        } catch (NumberFormatException e) {
            mostrarError("El puerto debe ser un número válido");
            txtPuerto.requestFocus();
        }
    }
    
    /**
     * Cancelar conexión
     */
    private void cancelar() {
        int opcion = JOptionPane.showConfirmDialog(
            this,
            "¿Estás seguro de que quieres salir?",
            "Confirmar Salida",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (opcion == JOptionPane.YES_OPTION) {
            this.conectado = false;
            System.exit(0);
        }
    }
    
    /**
     * Mostrar mensaje de error
     */
    private void mostrarError(String mensaje) {
        lblEstado.setText("❌ " + mensaje);
        lblEstado.setForeground(Color.RED);
    }
    
    /**
     * Obtener host configurado
     */
    public String getHost() {
        return host;
    }
    
    /**
     * Obtener puerto configurado
     */
    public int getPuerto() {
        return puerto;
    }
    
    /**
     * Verificar si se conectó exitosamente
     */
    public boolean isConectado() {
        return conectado;
    }
    
    /**
     * Método main para probar el diálogo
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            ConexionServidorDialog dialog = new ConexionServidorDialog(null);
            dialog.setVisible(true);
            
            if (dialog.isConectado()) {
                System.out.println("✓ Conectado a: " + dialog.getHost() + ":" + dialog.getPuerto());
            } else {
                System.out.println("✗ No se conectó");
            }
        });
    }
}
