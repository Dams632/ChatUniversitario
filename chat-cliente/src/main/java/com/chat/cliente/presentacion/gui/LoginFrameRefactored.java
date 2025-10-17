package com.chat.cliente.presentacion.gui;

import com.chat.cliente.negocio.ServicioCliente;
import com.chat.cliente.presentacion.gui.builders.LoginUIBuilder;
import com.chat.cliente.presentacion.gui.validators.LoginValidator;
import com.chat.cliente.presentacion.gui.validators.ValidationResult;
import com.chat.common.dto.ResponseDTO;

import javax.swing.*;
import java.awt.*;

/**
 * Frame de Login para el cliente del chat (Refactorizado con Builder Pattern)
 * 
 * Mejoras aplicadas:
 * - Patrón Builder para construcción de UI (reduce de 455 a ~200 líneas)
 * - Separación de responsabilidades (UI vs Lógica)
 * - Mayor mantenibilidad y testabilidad
 * - Reutilización de componentes
 */
public class LoginFrameRefactored extends JFrame {
    
    // Componentes UI (proporcionados por el builder)
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JButton btnLogin;
    private JLabel lblEstado;
    
    // Servicios y callbacks
    private final ServicioCliente servicioCliente;
    private final LoginCallback callback;
    
    /**
     * Constructor
     */
    public LoginFrameRefactored(ServicioCliente servicioCliente, LoginCallback callback) {
        this.servicioCliente = servicioCliente;
        this.callback = callback;
        
        initComponents();
        conectarServidor();
    }
    
    /**
     * Inicializar componentes usando Builder Pattern
     */
    private void initComponents() {
        setTitle("Chat Universitario - Iniciar Sesión");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(450, 700);  // Aumentado de 600 a 700 para acomodar mensajes de error
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Usar LoginUIBuilder para construir toda la interfaz
        LoginUIBuilder uiBuilder = new LoginUIBuilder()
            .onLogin(this::login)
            .onRegister(this::abrirRegistro)
            .build();
        
        // Obtener componentes del builder
        txtUsername = uiBuilder.getTxtUsername();
        txtPassword = uiBuilder.getTxtPassword();
        btnLogin = uiBuilder.getBtnLogin();
        lblEstado = uiBuilder.getLblEstado();
        
        // Agregar panel principal al frame
        add(uiBuilder.getMainPanel());
    }
    
    /**
     * Conectar al servidor en segundo plano
     */
    private void conectarServidor() {
        new Thread(() -> {
            try {
                System.out.println("Conectando al servidor...");
                servicioCliente.conectar();
                System.out.println("✓ Conectado al servidor correctamente");
                
                SwingUtilities.invokeLater(() -> {
                    lblEstado.setText("Conectado al servidor");
                    lblEstado.setForeground(new Color(46, 204, 113));
                });
                
            } catch (Exception e) {
                System.err.println("✗ Error al conectar con el servidor: " + e.getMessage());
                
                SwingUtilities.invokeLater(() -> {
                    lblEstado.setText("Error: No se pudo conectar al servidor");
                    lblEstado.setForeground(Color.RED);
                    
                    JOptionPane.showMessageDialog(LoginFrameRefactored.this,
                        "No se pudo conectar con el servidor.\n\n" +
                        "Verifica que el servidor esté en ejecución.\n" +
                        "Error: " + e.getMessage(),
                        "Error de Conexión",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Realizar login (Lógica de negocio separada de UI)
     */
    private void login() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        
        // Validar credenciales (SRP - LoginValidator)
        LoginValidator validator = new LoginValidator();
        ValidationResult result = validator.validateCredentials(username, password);
        
        if (!result.isValid()) {
            mostrarError(result.getErrorMessage());
            enfocarCampoConError(result.getErrorMessage());
            return;
        }
        
        // Deshabilitar botón y mostrar progreso
        btnLogin.setEnabled(false);
        lblEstado.setText("⏳ Iniciando sesión...");
        lblEstado.setForeground(new Color(52, 152, 219));
        
        // Ejecutar login en hilo separado
        ejecutarLoginEnBackground(username, password);
    }
    
    /**
     * Ejecutar login en background usando SwingWorker
     */
    private void ejecutarLoginEnBackground(String username, String password) {
        SwingWorker<ResponseDTO, Void> worker = new SwingWorker<ResponseDTO, Void>() {
            @Override
            protected ResponseDTO doInBackground() throws Exception {
                return servicioCliente.login(username, password);
            }
            
            @Override
            protected void done() {
                try {
                    ResponseDTO response = get();
                    procesarRespuestaLogin(username, response);
                } catch (Exception e) {
                    mostrarError("❌ Error al conectar con el servidor: " + e.getMessage());
                    btnLogin.setEnabled(true);
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Procesar respuesta del login
     */
    private void procesarRespuestaLogin(String username, ResponseDTO response) {
        if (response.isExito()) {
            lblEstado.setText("✅ ¡Sesión iniciada!");
            lblEstado.setForeground(new Color(46, 204, 113));
            
            // Cerrar ventana y notificar callback
            SwingUtilities.invokeLater(() -> {
                if (callback != null) {
                    callback.onLoginExitoso(username, response);
                }
                dispose();
            });
        } else {
            mostrarErrorLogin(response.getMensaje());
            btnLogin.setEnabled(true);
        }
    }
    
    /**
     * Mostrar error específico de login
     */
    private void mostrarErrorLogin(String mensaje) {
        if (mensaje != null && mensaje.toLowerCase().contains("contraseña")) {
            mostrarError("❌ Contraseña incorrecta. Verifica e intenta nuevamente.");
            txtPassword.setText("");
            txtPassword.requestFocus();
        } else if (mensaje != null && mensaje.toLowerCase().contains("usuario")) {
            mostrarError("❌ Usuario no encontrado. Verifica el nombre de usuario.");
            txtUsername.requestFocus();
        } else {
            mostrarError("❌ " + (mensaje != null ? mensaje : "Error al iniciar sesión"));
        }
    }
    
    /**
     * Enfocar campo con error según el mensaje
     */
    private void enfocarCampoConError(String errorMessage) {
        if (errorMessage.contains("usuario")) {
            txtUsername.requestFocus();
        } else if (errorMessage.contains("contraseña")) {
            txtPassword.requestFocus();
        }
    }
    
    /**
     * Abrir ventana de registro
     */
    private void abrirRegistro() {
        RegistroClienteFrame registroFrame = new RegistroClienteFrame(
            servicioCliente, 
            new RegistroClienteFrame.RegistroCallback() {
                @Override
                public void onRegistroExitoso(String username) {
                    // Rellenar el campo de usuario con el recién registrado
                    txtUsername.setText(username);
                    txtPassword.setText("");
                    txtPassword.requestFocus();
                }
                
                @Override
                public void onVolverLogin() {
                    // Usuario volvió al login
                }
            }
        );
        
        registroFrame.setVisible(true);
        
        if (callback != null) {
            callback.onAbrirRegistro();
        }
    }
    
    /**
     * Mostrar mensaje de error con word wrap automático
     */
    private void mostrarError(String mensaje) {
        String html = "<html><div style='text-align: center; width: 260px;'>" 
            + mensaje + "</div></html>";
        lblEstado.setText(html);
        lblEstado.setForeground(Color.RED);
    }
    
    /**
     * Limpiar formulario
     */
    public void limpiar() {
        txtUsername.setText("");
        txtPassword.setText("");
        lblEstado.setText(" ");
        btnLogin.setEnabled(true);
    }
    
    /**
     * Interface para callbacks
     */
    public interface LoginCallback {
        void onLoginExitoso(String username, ResponseDTO response);
        void onAbrirRegistro();
    }
    
    /**
     * Método main para probar la ventana
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            ServicioCliente servicio = new ServicioCliente("localhost", 5000);
            
            LoginFrameRefactored frame = new LoginFrameRefactored(servicio, new LoginCallback() {
                @Override
                public void onLoginExitoso(String username, ResponseDTO response) {
                    System.out.println("Login exitoso: " + username);
                    JOptionPane.showMessageDialog(null, "¡Bienvenido " + username + "!");
                }
                
                @Override
                public void onAbrirRegistro() {
                    System.out.println("Abrir ventana de registro");
                }
            });
            
            frame.setVisible(true);
        });
    }
}
