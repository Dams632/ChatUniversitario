package com.chat.cliente.presentacion.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.chat.cliente.negocio.ServicioCliente;
import com.chat.cliente.presentacion.gui.utils.FontHelper;
import com.chat.common.dto.ResponseDTO;
import com.chat.common.models.Usuario;

/**
 * Frame para crear grupo/canal e invitar usuarios
 */
public class CrearGrupoFrame extends JFrame {
    
    private ServicioCliente servicioCliente;
    private String usernameCreador;
    
    // Componentes del formulario
    private JLabel lblFotoPreview;
    private JTextField txtNombre;
    private JTextArea txtDescripcion;
    private JPanel panelUsuarios;
    private List<JCheckBox> checkboxesUsuarios;
    
    // Datos
    private byte[] fotoGrupoBytes;
    private List<Usuario> todosUsuarios;
    
    public CrearGrupoFrame(ServicioCliente servicioCliente, String usernameCreador) {
        this.servicioCliente = servicioCliente;
        this.usernameCreador = usernameCreador;
        this.checkboxesUsuarios = new ArrayList<>();
        
        initComponents();
        cargarUsuarios();
    }
    
    private void initComponents() {
        setTitle("Crear Grupo/Canal");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 700);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Panel principal con scroll
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(new EmptyBorder(20, 30, 20, 30));
        
        // Título
        JLabel lblTitulo = new JLabel("➕ Crear Nuevo Grupo/Canal");
        lblTitulo.setFont(FontHelper.getBoldLabelFont(24));
        lblTitulo.setForeground(new Color(52, 73, 94));
        lblTitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblTitulo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JLabel lblSubtitulo = new JLabel("Complete la información y seleccione los miembros");
        lblSubtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSubtitulo.setForeground(new Color(127, 140, 141));
        lblSubtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblSubtitulo);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Foto del grupo
        JLabel lblFotoLabel = new JLabel("Foto del Grupo/Canal");
        lblFotoLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblFotoLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblFotoLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        JPanel fotoPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        fotoPanel.setBackground(Color.WHITE);
        
        lblFotoPreview = new JLabel("👥");
        lblFotoPreview.setPreferredSize(new Dimension(100, 100));
        lblFotoPreview.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 2));
        lblFotoPreview.setHorizontalAlignment(JLabel.CENTER);
        lblFotoPreview.setFont(FontHelper.getLabelFont(48));
        lblFotoPreview.setOpaque(true);
        lblFotoPreview.setBackground(new Color(236, 240, 241));
        fotoPanel.add(lblFotoPreview);
        
        JButton btnSeleccionarFoto = new JButton("Seleccionar Foto");
        btnSeleccionarFoto.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnSeleccionarFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSeleccionarFoto.addActionListener(e -> seleccionarFoto());
        fotoPanel.add(btnSeleccionarFoto);
        
        mainPanel.add(fotoPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Nombre del grupo
        JLabel lblNombre = new JLabel("Nombre del Grupo/Canal *");
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNombre.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblNombre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        txtNombre = new JTextField();
        txtNombre.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtNombre.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        txtNombre.setMaximumSize(new Dimension(500, 35));
        mainPanel.add(txtNombre);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        // Descripción
        JLabel lblDescripcion = new JLabel("Descripción *");
        lblDescripcion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblDescripcion.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblDescripcion);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        
        txtDescripcion = new JTextArea(3, 20);
        txtDescripcion.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtDescripcion.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(8, 10, 8, 10)
        ));
        txtDescripcion.setLineWrap(true);
        txtDescripcion.setWrapStyleWord(true);
        JScrollPane scrollDescripcion = new JScrollPane(txtDescripcion);
        scrollDescripcion.setMaximumSize(new Dimension(500, 80));
        mainPanel.add(scrollDescripcion);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Lista de usuarios para invitar
        JLabel lblUsuarios = new JLabel("Seleccionar Miembros para Invitar");
        lblUsuarios.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsuarios.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainPanel.add(lblUsuarios);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        panelUsuarios = new JPanel();
        panelUsuarios.setLayout(new BoxLayout(panelUsuarios, BoxLayout.Y_AXIS));
        panelUsuarios.setBackground(Color.WHITE);
        
        JScrollPane scrollUsuarios = new JScrollPane(panelUsuarios);
        scrollUsuarios.setPreferredSize(new Dimension(500, 200));
        scrollUsuarios.setMaximumSize(new Dimension(500, 200));
        scrollUsuarios.setBorder(BorderFactory.createLineBorder(new Color(189, 195, 199), 1));
        mainPanel.add(scrollUsuarios);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        // Botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        botonesPanel.setBackground(Color.WHITE);
        botonesPanel.setMaximumSize(new Dimension(500, 50));
        
        JButton btnCrear = crearBoton("Crear y Enviar Invitaciones", new Color(46, 204, 113));
        btnCrear.addActionListener(e -> crearGrupoYEnviarInvitaciones());
        botonesPanel.add(btnCrear);
        
        JButton btnCancelar = crearBoton("Cancelar", new Color(231, 76, 60));
        btnCancelar.addActionListener(e -> dispose());
        botonesPanel.add(btnCancelar);
        
        mainPanel.add(botonesPanel);
        
        // Scroll principal
        JScrollPane scrollMain = new JScrollPane(mainPanel);
        scrollMain.setBorder(null);
        scrollMain.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollMain);
    }
    
    /**
     * Cargar todos los usuarios del sistema
     */
    private void cargarUsuarios() {
        new Thread(() -> {
            try {
                ResponseDTO response = servicioCliente.obtenerTodosLosUsuarios();
                
                if (response.isExito()) {
                    @SuppressWarnings("unchecked")
                    List<Usuario> usuarios = (List<Usuario>) response.getDato("usuarios");
                    todosUsuarios = usuarios;
                    
                    SwingUtilities.invokeLater(() -> {
                        checkboxesUsuarios.clear();
                        panelUsuarios.removeAll();
                        
                        for (Usuario usuario : usuarios) {
                            // No incluir al creador
                            if (!usuario.getUsername().equals(usernameCreador)) {
                                JCheckBox checkbox = new JCheckBox(
                                    usuario.getUsername() + 
                                    (usuario.isEnLinea() ? " [En Línea]" : " [Desconectado]")
                                );
                                checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                                checkbox.setBackground(Color.WHITE);
                                checkbox.setBorder(new EmptyBorder(5, 10, 5, 10));
                                checkbox.putClientProperty("usuario", usuario);
                                
                                checkboxesUsuarios.add(checkbox);
                                panelUsuarios.add(checkbox);
                            }
                        }
                        
                        panelUsuarios.revalidate();
                        panelUsuarios.repaint();
                    });
                }
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Error al cargar usuarios: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Seleccionar foto del grupo
     */
    private void seleccionarFoto() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Seleccionar foto del grupo");
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
     * Cargar foto del grupo
     */
    private void cargarFoto(File archivo) {
        try {
            BufferedImage imagen = ImageIO.read(archivo);
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            String formato = obtenerFormato(archivo.getName());
            ImageIO.write(imagen, formato, baos);
            fotoGrupoBytes = baos.toByteArray();
            
            Image imagenEscalada = imagen.getScaledInstance(90, 90, Image.SCALE_SMOOTH);
            lblFotoPreview.setIcon(new ImageIcon(imagenEscalada));
            lblFotoPreview.setText(null);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "Error al cargar la foto",
                "Error",
                JOptionPane.ERROR_MESSAGE);
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
     * Crear grupo y enviar invitaciones
     */
    private void crearGrupoYEnviarInvitaciones() {
        String nombre = txtNombre.getText().trim();
        String descripcion = txtDescripcion.getText().trim();
        
        // Validaciones
        if (nombre.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El nombre del grupo es obligatorio",
                "Validación",
                JOptionPane.WARNING_MESSAGE);
            txtNombre.requestFocus();
            return;
        }
        
        if (descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "La descripción es obligatoria",
                "Validación",
                JOptionPane.WARNING_MESSAGE);
            txtDescripcion.requestFocus();
            return;
        }
        
        // Obtener usuarios seleccionados
        List<String> usuariosInvitados = new ArrayList<>();
        for (JCheckBox checkbox : checkboxesUsuarios) {
            if (checkbox.isSelected()) {
                Usuario usuario = (Usuario) checkbox.getClientProperty("usuario");
                usuariosInvitados.add(usuario.getUsername());
            }
        }
        
        if (usuariosInvitados.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar al menos un usuario para invitar",
                "Validación",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Crear grupo y enviar invitaciones
        new Thread(() -> {
            try {
                // TODO: Implementar creación de grupo y envío de invitaciones
                ResponseDTO response = servicioCliente.crearGrupoConInvitaciones(
                    nombre, descripcion, fotoGrupoBytes, usuariosInvitados
                );
                
                SwingUtilities.invokeLater(() -> {
                    if (response.isExito()) {
                        JOptionPane.showMessageDialog(this,
                            "Grupo creado exitosamente.\n" +
                            "Se han enviado " + usuariosInvitados.size() + " invitaciones.",
                            "Éxito",
                            JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Error: " + response.getMensaje(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
                
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this,
                        "Error al crear grupo: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Crear botón
     */
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setPreferredSize(new Dimension(230, 40));
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
}
