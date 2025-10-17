package com.chat.cliente.presentacion.gui.builders;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import static com.chat.cliente.presentacion.gui.builders.SwingComponentBuilder.button;
import static com.chat.cliente.presentacion.gui.builders.SwingComponentBuilder.label;
import static com.chat.cliente.presentacion.gui.builders.SwingComponentBuilder.panel;
import static com.chat.cliente.presentacion.gui.builders.SwingComponentBuilder.passwordField;
import static com.chat.cliente.presentacion.gui.builders.SwingComponentBuilder.textField;

/**
 * Builder para construir la interfaz de LoginFrame
 * Separa la lógica de construcción de la UI de la lógica de negocio
 * Aplica el patrón Builder para reducir la complejidad del Frame principal
 */
public class LoginUIBuilder {
    
    // Componentes que necesitan ser accesibles
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblEstado;
    private JPanel mainPanel;
    
    // Callbacks
    private Runnable onLoginAction;
    private Runnable onRegisterAction;
    
    // Colores del tema
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color PRIMARY_DARK = new Color(41, 128, 185);
    private static final Color SUCCESS_COLOR = new Color(46, 204, 113);
    private static final Color TEXT_COLOR = new Color(52, 73, 94);
    private static final Color BORDER_COLOR = new Color(189, 195, 199);
    
    /**
     * Establecer callback para acción de login
     */
    public LoginUIBuilder onLogin(Runnable action) {
        this.onLoginAction = action;
        return this;
    }
    
    /**
     * Establecer callback para acción de registro
     */
    public LoginUIBuilder onRegister(Runnable action) {
        this.onRegisterAction = action;
        return this;
    }
    
    /**
     * Construir la interfaz completa
     */
    public LoginUIBuilder build() {
        mainPanel = createMainPanel();
        JPanel contentPanel = createContentPanel();
        
        addLogoSection(contentPanel);
        addUsernameSection(contentPanel);
        addPasswordSection(contentPanel);
        addLoginButton(contentPanel);
        addRegisterSection(contentPanel);
        addStatusLabel(contentPanel);
        
        // Agregar content panel al main panel
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(contentPanel, gbc);
        
        return this;
    }
    
    /**
     * Crear panel principal con gradiente
     */
    private JPanel createMainPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY_COLOR, 0, getHeight(), PRIMARY_DARK);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new GridBagLayout());
        return panel;
    }
    
    /**
     * Crear panel de contenido blanco
     */
    private JPanel createContentPanel() {
        JPanel contentPanel = panel()
            .background(Color.WHITE)
            .compoundBorder(BORDER_COLOR, 1, 40, 40, 40, 40)
            .preferredSize(350, 550)  // Aumentado de 450 a 550 para mensajes de error
            .build();
        
        // BoxLayout DEBE recibir el panel al que se aplicará
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        return contentPanel;
    }
    
    /**
     * Agregar sección de logo y título
     */
    private void addLogoSection(JPanel container) {
        // Logo - Usar font que soporte emojis
        JLabel lblLogo = SwingComponentBuilder.label("💬")
            .font("Segoe UI Emoji", Font.PLAIN, 72)  // Font específico para emojis, tamaño aumentado
            .alignmentX(Component.CENTER_ALIGNMENT)
            .build();
        container.add(lblLogo);
        container.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Título
        JLabel lblTitulo = SwingComponentBuilder.label("Chat Universitario")
            .font("Segoe UI", Font.BOLD, 24)
            .foreground(TEXT_COLOR)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .build();
        container.add(lblTitulo);
        container.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Subtítulo
        JLabel lblSubtitulo = label("Inicia sesión para continuar")
            .font("Segoe UI", Font.PLAIN, 13)
            .foreground(Color.GRAY)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .build();
        container.add(lblSubtitulo);
        container.add(Box.createRigidArea(new Dimension(0, 20)));
    }
    
    /**
     * Agregar sección de username
     */
    private void addUsernameSection(JPanel container) {
        // Label
        JLabel lblUsername = label("Usuario")
            .font("Segoe UI", Font.BOLD, 13)
            .foreground(TEXT_COLOR)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .build();
        container.add(lblUsername);
        container.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Campo de texto
        txtUsername = textField(20)
            .font("Segoe UI", Font.PLAIN, 14)
            .border(BORDER_COLOR, 10, 10, 10, 10)
            .maxSize(270, 40)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .build();
        container.add(txtUsername);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
    }
    
    /**
     * Agregar sección de password
     */
    private void addPasswordSection(JPanel container) {
        // Label
        JLabel lblPassword = label("Contraseña")
            .font("Segoe UI", Font.BOLD, 13)
            .foreground(TEXT_COLOR)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .build();
        container.add(lblPassword);
        container.add(Box.createRigidArea(new Dimension(0, 5)));
        
        // Campo de contraseña
        txtPassword = passwordField(20)
            .font("Segoe UI", Font.PLAIN, 14)
            .border(BORDER_COLOR, 10, 10, 10, 10)
            .maxSize(270, 40)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .build();
        
        // Enter para login
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && onLoginAction != null) {
                    onLoginAction.run();
                }
            }
        });
        
        container.add(txtPassword);
        container.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    /**
     * Agregar botón de login
     */
    private void addLoginButton(JPanel container) {
        btnLogin = button("Iniciar Sesión")
            .font("Segoe UI", Font.BOLD, 14)
            .background(SUCCESS_COLOR)
            .foreground(Color.WHITE)
            .focusPainted(false)
            .borderPainted(false)
            .maxSize(270, 45)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .cursor(Cursor.HAND_CURSOR)
            .hoverEffect()
            .build();
        
        if (onLoginAction != null) {
            btnLogin.addActionListener(e -> onLoginAction.run());
        }
        
        container.add(btnLogin);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Separador
        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(270, 1));
        separator.setForeground(BORDER_COLOR);
        container.add(separator);
        container.add(Box.createRigidArea(new Dimension(0, 10)));
    }
    
    /**
     * Agregar sección de registro
     */
    private void addRegisterSection(JPanel container) {
        JPanel registroPanel = panel()
            .layout(new FlowLayout(FlowLayout.CENTER, 5, 0))
            .background(Color.WHITE)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .build();
        
        JLabel lblNoTieneCuenta = label("¿No tiene cuenta?")
            .font("Segoe UI", Font.PLAIN, 13)
            .foreground(Color.GRAY)
            .build();
        
        JLabel lblCrearCuenta = label("Cree una")
            .font("Segoe UI", Font.BOLD, 13)
            .foreground(PRIMARY_COLOR)
            .cursor(Cursor.HAND_CURSOR)
            .build();
        
        // Efectos hover y click
        lblCrearCuenta.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                lblCrearCuenta.setText("<html><u>Cree una</u></html>");
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                lblCrearCuenta.setText("Cree una");
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (onRegisterAction != null) {
                    onRegisterAction.run();
                }
            }
        });
        
        registroPanel.add(lblNoTieneCuenta);
        registroPanel.add(lblCrearCuenta);
        container.add(registroPanel);
        container.add(Box.createRigidArea(new Dimension(0, 15)));
    }
    
    /**
     * Agregar label de estado
     */
    private void addStatusLabel(JPanel container) {
        lblEstado = label(" ")
            .font("Segoe UI", Font.PLAIN, 11)
            .foreground(Color.RED)
            .alignmentX(Component.CENTER_ALIGNMENT)
            .horizontalAlignment(SwingConstants.CENTER)
            .preferredSize(270, 40)
            .maxSize(270, 40)
            .build();
        container.add(lblEstado);
    }
    
    // Getters para componentes
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JTextField getTxtUsername() {
        return txtUsername;
    }
    
    public JPasswordField getTxtPassword() {
        return txtPassword;
    }
    
    public JButton getBtnLogin() {
        return btnLogin;
    }
    
    public JLabel getLblEstado() {
        return lblEstado;
    }
}
