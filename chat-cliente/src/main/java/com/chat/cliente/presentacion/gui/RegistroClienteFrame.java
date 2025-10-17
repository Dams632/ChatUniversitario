package com.chat.cliente.presentacion.gui;

import com.chat.cliente.negocio.ServicioCliente;
import com.chat.cliente.presentacion.gui.validators.RegistrationValidator;
import com.chat.cliente.presentacion.gui.validators.ValidationResult;
import com.chat.common.dto.ResponseDTO;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Frame de Registro para nuevos usuarios del cliente
 */
public class RegistroClienteFrame extends JFrame {
    
    private JTextField txtUsername;
    private JTextField txtEmail;
    private JTextField txtDireccionIP;
    private JPasswordField txtPassword;
    private JPasswordField txtConfirmPassword;
    private JLabel lblFotoPreview;
    private JButton btnSeleccionarFoto;
    private JButton btnRegistrar;
    private JLabel lblEstado;
    
    private byte[] fotoBytes;
    private BufferedImage fotoImagen;
    
    private ServicioCliente servicioCliente;
    private RegistroCallback callback;
    
    /**
     * Constructor
     */
    public RegistroClienteFrame(ServicioCliente servicioCliente, RegistroCallback callback) {
        this.servicioCliente = servicioCliente;
        this.callback = callback;
        initComponents();
    }
    
    /**
     * Inicializar componentes
     */
    private void initComponents() {
        setTitle("Chat Universitario - Crear Cuenta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(500, 750);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Panel principal con degradado
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                int w = getWidth();
                int h = getHeight();
                Color color1 = new Color(155, 89, 182);
                Color color2 = new Color(142, 68, 173);
                GradientPaint gp = new GradientPaint(0, 0, color1, 0, h, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, w, h);
            }
        };
        mainPanel.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        
        // Panel de contenido
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(30, 40, 30, 40)
        ));
        contentPanel.setPreferredSize(new Dimension(400, 670));
        
        // Título
        JLabel lblTitulo = new JLabel("Crear Cuenta Nueva");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(52, 73, 94));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblTitulo);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JLabel lblSubtitulo = new JLabel("Complete la información para registrarse");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSubtitulo.setForeground(Color.GRAY);
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblSubtitulo);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Campo username
        agregarCampo(contentPanel, "Usuario", txtUsername = crearTextField());
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Campo email
        agregarCampo(contentPanel, "Correo Electrónico", txtEmail = crearTextField());
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Campo dirección IP
        agregarCampo(contentPanel, "Dirección IP", txtDireccionIP = crearTextField());
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Campo password
        agregarCampo(contentPanel, "Contraseña", txtPassword = crearPasswordField());
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Campo confirmar password
        agregarCampo(contentPanel, "Confirmar Contraseña", txtConfirmPassword = crearPasswordField());
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Foto de perfil
        JLabel lblFoto = new JLabel("Foto de Perfil (Opcional)");
        lblFoto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFoto.setForeground(new Color(52, 73, 94));
        lblFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        contentPanel.add(lblFoto);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Panel de foto
        JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        fotoPanel.setBackground(Color.WHITE);
        fotoPanel.setMaximumSize(new Dimension(320, 90));
        
        lblFotoPreview = new JLabel("Sin foto");
        lblFotoPreview.setPreferredSize(new Dimension(80, 80));
        lblFotoPreview.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));
        lblFotoPreview.setHorizontalAlignment(SwingConstants.CENTER);
        lblFotoPreview.setBackground(new Color(236, 240, 241));
        lblFotoPreview.setOpaque(true);
        lblFotoPreview.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        lblFotoPreview.setForeground(Color.GRAY);
        
        btnSeleccionarFoto = new JButton("Seleccionar");
        btnSeleccionarFoto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSeleccionarFoto.setBackground(new Color(149, 165, 166));
        btnSeleccionarFoto.setForeground(Color.WHITE);
        btnSeleccionarFoto.setFocusPainted(false);
        btnSeleccionarFoto.setBorderPainted(false);
        btnSeleccionarFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSeleccionarFoto.addActionListener(e -> seleccionarFoto());
        
        fotoPanel.add(lblFotoPreview);
        fotoPanel.add(btnSeleccionarFoto);
        contentPanel.add(fotoPanel);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Botón registrar
        btnRegistrar = crearBoton("Crear Cuenta", new Color(155, 89, 182));
        btnRegistrar.addActionListener(e -> registrar());
        contentPanel.add(btnRegistrar);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        // Enlace volver al login
        JLabel lblVolverLogin = new JLabel("<html><u>Volver al inicio de sesión</u></html>");
        lblVolverLogin.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblVolverLogin.setForeground(new Color(52, 152, 219));
        lblVolverLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblVolverLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblVolverLogin.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                volverLogin();
            }
        });
        contentPanel.add(lblVolverLogin);
        
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Estado con word wrap
        lblEstado = new JLabel(" ");
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEstado.setForeground(Color.RED);
        lblEstado.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblEstado.setHorizontalAlignment(SwingConstants.CENTER);
        lblEstado.setPreferredSize(new Dimension(320, 40)); // Altura fija para 2-3 líneas
        lblEstado.setMaximumSize(new Dimension(320, 40));
        contentPanel.add(lblEstado);
        
        gbc.gridx = 0;
        gbc.gridy = 0;
        mainPanel.add(contentPanel, gbc);
        
        add(mainPanel);
    }
    
    /**
     * Agregar campo de texto con label
     */
    private void agregarCampo(JPanel panel, String label, JComponent campo) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lbl.setForeground(new Color(52, 73, 94));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(lbl);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(campo);
    }
    
    /**
     * Crear text field
     */
    private JTextField crearTextField() {
        JTextField field = new JTextField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        field.setMaximumSize(new Dimension(320, 35));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }
    
    /**
     * Crear password field
     */
    private JPasswordField crearPasswordField() {
        JPasswordField field = new JPasswordField();
        field.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        field.setMaximumSize(new Dimension(320, 35));
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        return field;
    }
    
    /**
     * Crear botón
     */
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setMaximumSize(new Dimension(320, 45));
        boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(color.darker());
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
            }
        });
        
        return boton;
    }
    
    /**
     * Seleccionar foto
     */
    private void seleccionarFoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar foto de perfil");
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Imágenes (JPG, PNG, GIF)", "jpg", "jpeg", "png", "gif"
        );
        fileChooser.setFileFilter(filter);
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fileChooser.getSelectedFile();
            cargarFoto(archivo);
        }
    }
    
    /**
     * Cargar foto
     */
    private void cargarFoto(File archivo) {
        try {
            fotoImagen = ImageIO.read(archivo);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String formato = obtenerFormato(archivo.getName());
            ImageIO.write(fotoImagen, formato, baos);
            fotoBytes = baos.toByteArray();
            
            Image imagenEscalada = fotoImagen.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
            lblFotoPreview.setIcon(new ImageIcon(imagenEscalada));
            lblFotoPreview.setText(null);
            
        } catch (Exception e) {
            mostrarError("Error al cargar la foto");
        }
    }
    
    /**
     * Obtener formato de imagen
     */
    private String obtenerFormato(String nombreArchivo) {
        int puntoIndex = nombreArchivo.lastIndexOf('.');
        if (puntoIndex > 0) {
            return nombreArchivo.substring(puntoIndex + 1).toLowerCase();
        }
        return "jpg";
    }
    
    /**
     * Registrar usuario
     */
    private void registrar() {
        String username = txtUsername.getText().trim();
        String email = txtEmail.getText().trim();
        String password = new String(txtPassword.getPassword());
        String confirmPassword = new String(txtConfirmPassword.getPassword());
        String direccionIP = txtDireccionIP.getText().trim();
        
        // Validaciones del lado del cliente usando RegistrationValidator (SRP)
        RegistrationValidator validator = new RegistrationValidator();
        ValidationResult result = validator.validateRegistrationData(username, email, password, confirmPassword, direccionIP);
        
        if (!result.isValid()) {
            mostrarError(result.getErrorMessage());
            // Enfocar el campo correspondiente basado en el error
            if (result.getErrorMessage().contains("usuario")) {
                txtUsername.requestFocus();
            } else if (result.getErrorMessage().contains("email")) {
                txtEmail.requestFocus();
            } else if (result.getErrorMessage().contains("contraseña")) {
                txtPassword.requestFocus();
                if (result.getErrorMessage().contains("coinciden")) {
                    txtPassword.setText("");
                    txtConfirmPassword.setText("");
                }
            } else if (result.getErrorMessage().contains("IP")) {
                txtDireccionIP.requestFocus();
            }
            return;
        }
        
        // Registrar
        btnRegistrar.setEnabled(false);
        lblEstado.setText("⏳ Creando cuenta...");
        lblEstado.setForeground(new Color(52, 152, 219));
        
        SwingWorker<ResponseDTO, Void> worker = new SwingWorker<ResponseDTO, Void>() {
            @Override
            protected ResponseDTO doInBackground() throws Exception {
                // Registrar con dirección IP y foto
                if (fotoBytes != null && fotoBytes.length > 0) {
                    return servicioCliente.registrar(username, email, password, direccionIP, fotoBytes);
                } else {
                    return servicioCliente.registrar(username, email, password, direccionIP);
                }
            }
            
            @Override
            protected void done() {
                try {
                    ResponseDTO response = get();
                    
                    if (response.isExito()) {
                        lblEstado.setText("✅ ¡Cuenta creada exitosamente!");
                        lblEstado.setForeground(new Color(46, 204, 113));
                        
                        JOptionPane.showMessageDialog(
                            RegistroClienteFrame.this,
                            "✅ Cuenta creada exitosamente.\n\n" +
                            "Usuario: " + username + "\n" +
                            "Ahora puede iniciar sesión con sus credenciales.",
                            "Registro Exitoso",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                        
                        if (callback != null) {
                            callback.onRegistroExitoso(username);
                        }
                        
                        dispose();
                    } else {
                        // Mejorar mensajes de error específicos
                        String mensaje = response.getMensaje();
                        
                        if (mensaje != null && (mensaje.toLowerCase().contains("ya existe") 
                                             || mensaje.toLowerCase().contains("duplicado"))) {
                            mostrarError("❌ El usuario '" + username + "' ya está registrado. Por favor elige otro nombre.");
                            txtUsername.requestFocus();
                            txtUsername.selectAll();
                        } else if (mensaje != null && mensaje.toLowerCase().contains("email")) {
                            mostrarError("❌ El email ya está registrado o no es válido.");
                            txtEmail.requestFocus();
                        } else {
                            mostrarError("❌ " + (mensaje != null ? mensaje : "Error al crear la cuenta"));
                        }
                        
                        btnRegistrar.setEnabled(true);
                    }
                    
                } catch (Exception e) {
                    mostrarError("❌ Error al conectar con el servidor: " + e.getMessage());
                    btnRegistrar.setEnabled(true);
                    e.printStackTrace();
                }
            }
        };
        
        worker.execute();
    }
    
    /**
     * Validar email
     */
    private boolean validarEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(regex);
    }
    
    /**
     * Validar dirección IP (formato IPv4)
     */
    private boolean validarIP(String ip) {
        String regex = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
        return ip.matches(regex);
    }
    
    /**
     * Volver al login
     */
    private void volverLogin() {
        if (callback != null) {
            callback.onVolverLogin();
        }
        dispose();
    }
    
    /**
     * Mostrar error
     */
    private void mostrarError(String mensaje) {
        String html = "<html><div style='text-align: center; width: 310px;'>" + mensaje + "</div></html>";
        lblEstado.setText(html);
        lblEstado.setForeground(Color.RED);
    }
    
    /**
     * Interface para callbacks
     */
    public interface RegistroCallback {
        void onRegistroExitoso(String username);
        void onVolverLogin();
    }
}
