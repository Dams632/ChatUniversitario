package com.chat.servidor.presentacion.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import com.chat.servidor.datos.ConexionDB;
import com.chat.servidor.datos.LogMensajeDAO.LogMensaje;
import com.chat.servidor.negocio.ServicioInformesLogs;
import com.chat.servidor.negocio.ServicioEstadisticas;
import com.chat.servidor.presentacion.gui.utils.FontHelper;

/**
 * Ventana de informes del servidor
 * REFACTORIZADO: Usa ServicioInformesLogs (capa de negocio) en lugar de SQL directo
 */
public class InformesFrame extends JFrame {
    
    private Connection conexion;
    private JTabbedPane tabbedPane;
    
    // Servicios de negocio (✅ Separación de capas)
    private ServicioInformesLogs servicioLogs;
    private ServicioEstadisticas servicioEstadisticas;
    
    // Tablas
    private JTable tablaUsuariosRegistrados;
    private DefaultTableModel modeloUsuariosRegistrados;
    
    private JTable tablaCanalesUsuarios;
    private DefaultTableModel modeloCanalesUsuarios;
    
    private JTable tablaUsuariosConectados;
    private DefaultTableModel modeloUsuariosConectados;
    
    private JTable tablaAudios;
    private DefaultTableModel modeloAudios;
    
    private JTable tablaLogsMensajes;
    private DefaultTableModel modeloLogsMensajes;
    
    private JTextArea txtLogs;
    
    /**
     * Constructor
     */
    public InformesFrame() {
        try {
            this.conexion = ConexionDB.obtenerConexion();
            this.servicioLogs = new ServicioInformesLogs(conexion); // ✅ Inyección de dependencias
            this.servicioEstadisticas = new ServicioEstadisticas(conexion); // ✅ Inyección de dependencias
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                "Error al conectar con la base de datos: " + e.getMessage(),
                "Error de Conexión",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        initComponents();
        cargarDatos();
    }
    
    /**
     * Inicializar componentes
     */
    private void initComponents() {
        setTitle("📊 Informes del Servidor - Chat Universitario");
        setSize(1200, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(new Color(236, 240, 241));
        
        // Header
        JPanel headerPanel = crearHeader();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabs
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        tabbedPane.addTab("👥 Usuarios Registrados", crearPanelUsuariosRegistrados());
        tabbedPane.addTab("📢 Canales y Usuarios", crearPanelCanalesUsuarios());
        tabbedPane.addTab("🟢 Usuarios Conectados", crearPanelUsuariosConectados());
        tabbedPane.addTab("🎤 Audios", crearPanelAudios());
        tabbedPane.addTab(" Logs de Mensajes", crearPanelLogsMensajes());
        tabbedPane.addTab("� Logs del Sistema", crearPanelLogs());
        
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        
        // Panel inferior con botones
        JPanel bottomPanel = crearPanelInferior();
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    /**
     * Crear header
     */
    private JPanel crearHeader() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(41, 128, 185));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        JLabel lblTitulo = new JLabel("📊 Informes y Estadísticas del Servidor");
        lblTitulo.setFont(FontHelper.getBoldLabelFont(22));
        lblTitulo.setForeground(Color.WHITE);
        panel.add(lblTitulo, BorderLayout.WEST);
        
        JLabel lblFecha = new JLabel(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
        lblFecha.setFont(FontHelper.getLabelFont(14));
        lblFecha.setForeground(Color.WHITE);
        panel.add(lblFecha, BorderLayout.EAST);
        
        return panel;
    }
    
    /**
     * Panel de usuarios registrados
     */
    private JPanel crearPanelUsuariosRegistrados() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel lblTitulo = new JLabel("Usuarios Registrados en el Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Tabla
        String[] columnas = {"ID", "Username", "Email", "Fecha Registro", "Estado", "IP Registro"};
        modeloUsuariosRegistrados = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaUsuariosRegistrados = crearTabla(modeloUsuariosRegistrados);
        JScrollPane scrollPane = new JScrollPane(tablaUsuariosRegistrados);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Panel de canales con usuarios
     */
    private JPanel crearPanelCanalesUsuarios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel lblTitulo = new JLabel("Canales/Grupos con Usuarios Vinculados");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Tabla
        String[] columnas = {"Canal ID", "Nombre Canal", "Tipo", "Creador", "Total Miembros", "Fecha Creación"};
        modeloCanalesUsuarios = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaCanalesUsuarios = crearTabla(modeloCanalesUsuarios);
        JScrollPane scrollPane = new JScrollPane(tablaCanalesUsuarios);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Panel de usuarios conectados
     */
    private JPanel crearPanelUsuariosConectados() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel lblTitulo = new JLabel("Usuarios Actualmente Conectados");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Tabla
        String[] columnas = {"ID", "Username", "Email", "Estado", "Última Conexión"};
        modeloUsuariosConectados = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaUsuariosConectados = crearTabla(modeloUsuariosConectados);
        JScrollPane scrollPane = new JScrollPane(tablaUsuariosConectados);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Panel de mensajes de audio
     */
    private JPanel crearPanelAudios() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título con descripción
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        
        JLabel lblTitulo = new JLabel("Mensajes de Audio Enviados");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        lblTitulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(lblTitulo);
        
        JLabel lblDescripcion = new JLabel("Nota: El texto mostrado es el formato del audio, no transcripción de voz");
        lblDescripcion.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblDescripcion.setForeground(new Color(127, 140, 141));
        lblDescripcion.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        headerPanel.add(lblDescripcion);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Tabla
        String[] columnas = {"ID", "Remitente", "Destinatario", "Formato", "Duración (seg)", "Tamaño (KB)", "Fecha Envío"};
        modeloAudios = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaAudios = crearTabla(modeloAudios);
        JScrollPane scrollPane = new JScrollPane(tablaAudios);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Panel de logs de mensajes con transcripciones
     */
    private JPanel crearPanelLogsMensajes() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel lblTitulo = new JLabel("Logs de Mensajes de Texto y Audio con Transcripciones");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Tabla
        String[] columnas = {
            "ID", "Tipo", "Conversación", "Remitente", "Destinatario/Canal", 
            "Contenido/Transcripción", "Fecha", "IP"
        };
        
        modeloLogsMensajes = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        tablaLogsMensajes = crearTabla(modeloLogsMensajes);
        
        // Ajustar anchos de columnas
        tablaLogsMensajes.getColumnModel().getColumn(0).setPreferredWidth(50);  // ID
        tablaLogsMensajes.getColumnModel().getColumn(1).setPreferredWidth(80);  // Tipo
        tablaLogsMensajes.getColumnModel().getColumn(2).setPreferredWidth(100); // Conversación
        tablaLogsMensajes.getColumnModel().getColumn(3).setPreferredWidth(120); // Remitente
        tablaLogsMensajes.getColumnModel().getColumn(4).setPreferredWidth(120); // Destinatario
        tablaLogsMensajes.getColumnModel().getColumn(5).setPreferredWidth(300); // Contenido
        tablaLogsMensajes.getColumnModel().getColumn(6).setPreferredWidth(150); // Fecha
        tablaLogsMensajes.getColumnModel().getColumn(7).setPreferredWidth(120); // IP
        
        JScrollPane scrollPane2 = new JScrollPane(tablaLogsMensajes);
        panel.add(scrollPane2, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Panel de logs
     */
    private JPanel crearPanelLogs() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        // Título
        JLabel lblTitulo = new JLabel("Logs de Actividad del Sistema");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitulo.setForeground(new Color(52, 73, 94));
        panel.add(lblTitulo, BorderLayout.NORTH);
        
        // Área de texto
        txtLogs = new JTextArea();
        txtLogs.setFont(new Font("Consolas", Font.PLAIN, 12));
        txtLogs.setEditable(false);
        txtLogs.setLineWrap(true);
        txtLogs.setWrapStyleWord(true);
        txtLogs.setBackground(new Color(44, 62, 80));
        txtLogs.setForeground(new Color(236, 240, 241));
        
        JScrollPane scrollPane = new JScrollPane(txtLogs);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        return panel;
    }
    
    /**
     * Panel inferior con botones
     */
    private JPanel crearPanelInferior() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        panel.setBackground(new Color(236, 240, 241));
        
        JButton btnActualizar = crearBoton("🔄 Actualizar", new Color(52, 152, 219));
        btnActualizar.addActionListener(e -> cargarDatos());
        panel.add(btnActualizar);
        
        JButton btnExportar = crearBoton("📄 Exportar a TXT", new Color(46, 204, 113));
        btnExportar.addActionListener(e -> exportarInforme());
        panel.add(btnExportar);
        
        JButton btnCerrar = crearBoton("❌ Cerrar", new Color(231, 76, 60));
        btnCerrar.addActionListener(e -> dispose());
        panel.add(btnCerrar);
        
        return panel;
    }
    
    /**
     * Crear botón estilizado
     */
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(FontHelper.getButtonFont(12));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setPreferredSize(new Dimension(180, 35));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
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
     * Crear tabla estilizada
     */
    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        tabla.setForeground(new Color(44, 62, 80)); // Color de texto oscuro para legibilidad
        tabla.setBackground(Color.WHITE); // Fondo blanco
        tabla.setRowHeight(25);
        tabla.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        tabla.getTableHeader().setBackground(new Color(52, 152, 219));
        tabla.getTableHeader().setForeground(Color.WHITE);
        tabla.setSelectionBackground(new Color(52, 152, 219, 50));
        tabla.setSelectionForeground(new Color(44, 62, 80)); // Color de texto en selección
        tabla.setGridColor(new Color(189, 195, 199));
        return tabla;
    }
    
    /**
     * Cargar todos los datos
     */
    private void cargarDatos() {
        new Thread(() -> {
            cargarUsuariosRegistrados();
            cargarCanalesUsuarios();
            cargarUsuariosConectados();
            cargarAudios();
            cargarLogsMensajes();
            cargarLogs();
            
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this,
                    "Datos actualizados correctamente",
                    "Actualización Completa",
                    JOptionPane.INFORMATION_MESSAGE);
            });
        }).start();
    }
    
    /**
     * Cargar usuarios registrados
     * REFACTORIZADO: Usa ServicioEstadisticas (capa de negocio)
     */
    private void cargarUsuariosRegistrados() {
        try {
            modeloUsuariosRegistrados.setRowCount(0);
            
            // ✅ Usar servicio en lugar de SQL directo
            List<ServicioEstadisticas.UsuarioReporte> usuarios = 
                servicioEstadisticas.obtenerUsuariosParaReporte();
            
            for (ServicioEstadisticas.UsuarioReporte usuario : usuarios) {
                Object[] fila = {
                    usuario.id,
                    usuario.username,
                    usuario.email,
                    new SimpleDateFormat("dd/MM/yyyy HH:mm")
                        .format(java.sql.Timestamp.valueOf(usuario.fechaRegistro)),
                    usuario.getEstadoTexto(),
                    usuario.direccionIp
                };
                modeloUsuariosRegistrados.addRow(fila);
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar usuarios registrados: " + e.getMessage());
            e.printStackTrace(); // Ver stack trace completo
        }
    }
    
    /**
     * Cargar canales con usuarios
     * REFACTORIZADO: Usa ServicioEstadisticas (capa de negocio)
     */
    private void cargarCanalesUsuarios() {
        try {
            modeloCanalesUsuarios.setRowCount(0);
            
            // ✅ Usar servicio en lugar de SQL directo
            List<ServicioEstadisticas.CanalReporte> canales = 
                servicioEstadisticas.obtenerCanalesParaReporte();
            
            for (ServicioEstadisticas.CanalReporte canal : canales) {
                Object[] fila = {
                    canal.id,
                    canal.nombre,
                    canal.getTipoTexto(),
                    canal.creador,
                    canal.totalMiembros,
                    new SimpleDateFormat("dd/MM/yyyy HH:mm")
                        .format(java.sql.Timestamp.valueOf(canal.fechaCreacion))
                };
                modeloCanalesUsuarios.addRow(fila);
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar canales: " + e.getMessage());
            e.printStackTrace(); // Ver stack trace completo
        }
    }
    
    /**
     * Cargar usuarios conectados
     * REFACTORIZADO: Usa ServicioUsuario (capa de negocio)
     */
    private void cargarUsuariosConectados() {
        try {
            modeloUsuariosConectados.setRowCount(0);
            
            // ✅ Usar servicio en lugar de SQL directo
            List<ServicioEstadisticas.ConexionUsuario> usuarios = 
                servicioEstadisticas.obtenerHistorialConexiones(Integer.MAX_VALUE);
            
            // Filtrar solo usuarios en línea
            for (ServicioEstadisticas.ConexionUsuario usuario : usuarios) {
                if (usuario.enLinea) {
                    Object[] fila = {
                        0L, // ID no disponible en ConexionUsuario
                        usuario.username,
                        "", // Email no disponible
                        "🟢 Conectado",
                        new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                            .format(java.sql.Timestamp.valueOf(usuario.ultimaConexion))
                    };
                    modeloUsuariosConectados.addRow(fila);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar usuarios conectados: " + e.getMessage());
            e.printStackTrace(); // Ver stack trace completo
        }
    }
    
    /**
     * Cargar mensajes de audio
     * REFACTORIZADO: Usa ServicioEstadisticas (capa de negocio)
     */
    private void cargarAudios() {
        try {
            modeloAudios.setRowCount(0);
            
            // ✅ Usar servicio en lugar de SQL directo
            List<ServicioEstadisticas.AudioReporte> audios = 
                servicioEstadisticas.obtenerAudiosParaReporte(500);
            
            for (ServicioEstadisticas.AudioReporte audio : audios) {
                Object[] fila = {
                    audio.id,
                    audio.remitente,
                    audio.destinatario,
                    audio.getFormatoTexto(),
                    audio.getDuracionTexto(),
                    audio.getTamanoTexto(),
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        .format(java.sql.Timestamp.valueOf(audio.fechaEnvio))
                };
                modeloAudios.addRow(fila);
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar audios: " + e.getMessage());
            e.printStackTrace(); // Ver stack trace completo
        }
    }
    
    /**
     * Cargar logs de mensajes con transcripciones
     * REFACTORIZADO: Usa ServicioInformesLogs (capa de negocio)
     */
    private void cargarLogsMensajes() {
        try {
            modeloLogsMensajes.setRowCount(0);
            
            // ✅ Usar capa de negocio en lugar de SQL directo
            List<LogMensaje> logs = servicioLogs.obtenerTodosLosLogs(500);
            
            for (LogMensaje log : logs) {
                String contenido = log.contenidoTexto != null 
                    ? log.contenidoTexto 
                    : (log.transcripcionAudio != null 
                        ? log.transcripcionAudio 
                        : "[Sin contenido]");
                
                // Limitar longitud del contenido para la tabla
                if (contenido.length() > 100) {
                    contenido = contenido.substring(0, 97) + "...";
                }
                
                // Agregar indicador según tipo (usando símbolos compatibles)
                String tipoIcon = log.tipoMensaje.equals("TEXTO") ? "TXT" : "AUD";
                String conversacionIcon = log.tipoConversacion.equals("PRIVADO") ? "PVT" : "GRP";
                
                String destino = log.destinatarioUsername != null 
                    ? log.destinatarioUsername
                    : (log.grupoNombre != null ? log.grupoNombre : "N/A");
                
                Object[] fila = {
                    log.id,
                    tipoIcon,
                    conversacionIcon,
                    log.remitenteUsername,
                    destino,
                    contenido,
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(java.sql.Timestamp.valueOf(log.timestamp)),
                    log.direccionIP
                };
                modeloLogsMensajes.addRow(fila);
            }
            
        } catch (Exception e) {
            System.err.println("Error al cargar logs de mensajes: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Cargar logs del sistema
     * REFACTORIZADO: Usa ServicioInformesLogs (capa de negocio) para estadísticas
     */
    private void cargarLogs() {
        try {
            txtLogs.setText("");
            StringBuilder logs = new StringBuilder();
            
            // ===== LOGS DE MENSAJES Y TRANSCRIPCIONES =====
            logs.append("═══════════════════════════════════════════════════════════\n");
            logs.append("           LOGS DE MENSAJES Y TRANSCRIPCIONES\n");
            logs.append("═══════════════════════════════════════════════════════════\n\n");
            
            // ✅ Usar servicio en lugar de SQL directo
            List<LogMensaje> logsMensajes = servicioLogs.obtenerTodosLosLogs(100);
            
            for (LogMensaje msg : logsMensajes) {
                String tipo = msg.tipoMensaje.equals("TEXTO") ? "[TXT]" : "[AUD]";
                String conversacion = msg.tipoConversacion.equals("PRIVADO") ? "[PVT]" : "[GRP]";
                String destino = msg.destinatarioUsername != null 
                    ? msg.destinatarioUsername 
                    : (msg.grupoNombre != null ? msg.grupoNombre : "N/A");
                
                String contenido = msg.contenidoTexto != null 
                    ? msg.contenidoTexto 
                    : (msg.transcripcionAudio != null ? msg.transcripcionAudio : "[Sin contenido]");
                
                // Limitar a 60 caracteres
                if (contenido.length() > 60) {
                    contenido = contenido.substring(0, 60);
                }
                
                logs.append(String.format("[%s] %s%s %s -> %s: %s... (IP: %s)\n",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(java.sql.Timestamp.valueOf(msg.timestamp)),
                    tipo,
                    conversacion,
                    msg.remitenteUsername,
                    destino,
                    contenido,
                    msg.direccionIP != null ? msg.direccionIP : "N/A"
                ));
            }
            logs.append("\nTotal de logs de mensajes: ").append(logsMensajes.size()).append("\n\n");
            
            // ===== LOGS DE CONEXIONES =====
            logs.append("═══════════════════════════════════════════════════════════\n");
            logs.append("                 HISTORIAL DE CONEXIONES\n");
            logs.append("═══════════════════════════════════════════════════════════\n\n");
            
            // ✅ Usar servicio en lugar de SQL directo
            List<ServicioEstadisticas.ConexionUsuario> conexiones = 
                servicioEstadisticas.obtenerHistorialConexiones(50);
            
            for (ServicioEstadisticas.ConexionUsuario conn : conexiones) {
                logs.append(String.format("[%s] %s - %s (IP: %s)\n",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                        .format(java.sql.Timestamp.valueOf(conn.ultimaConexion)),
                    conn.username,
                    conn.getEstado(),
                    conn.getDireccionIpSegura()
                ));
            }
            
            // ===== ESTADÍSTICAS GENERALES =====
            logs.append("\n═══════════════════════════════════════════════════════════\n");
            logs.append("                 ESTADISTICAS GENERALES\n");
            logs.append("═══════════════════════════════════════════════════════════\n\n");
            
            // ✅ Usar servicios para todas las estadísticas
            ServicioInformesLogs.EstadisticasLogs statsLogs = servicioLogs.obtenerEstadisticas();
            ServicioEstadisticas.EstadisticasGenerales statsGenerales = 
                servicioEstadisticas.obtenerEstadisticasGenerales();
            
            // Estadísticas de usuarios
            logs.append("[USERS] Total de usuarios registrados: ")
                .append(statsGenerales.getTotalUsuarios()).append("\n");
            logs.append("[ONLINE] Usuarios en linea: ")
                .append(statsGenerales.getUsuariosEnLinea()).append("\n");
            
            // Estadísticas de canales
            logs.append("[CHANNELS] Canales activos: ")
                .append(statsGenerales.getCanalesActivos()).append("\n");
            
            // Estadísticas de mensajes
            logs.append("[TEXT] Mensajes de texto registrados: ")
                .append(statsLogs.getTotalMensajesTexto()).append("\n");
            logs.append("[AUDIO] Mensajes de audio registrados: ")
                .append(statsLogs.getTotalMensajesAudio()).append("\n");
            logs.append("[TRANSCRIBED] Transcripciones exitosas: ")
                .append(statsLogs.getTotalTranscripcionesExitosas()).append("\n");
            logs.append("[FAILED] Transcripciones fallidas: ")
                .append(statsLogs.getTotalTranscripcionesFallidas()).append("\n");
            logs.append("[SUCCESS RATE] Porcentaje de exito: ")
                .append(String.format("%.1f%%", statsLogs.getPorcentajeExito())).append("\n");
            
            txtLogs.setText(logs.toString());
            txtLogs.setCaretPosition(0);
            
        } catch (Exception e) {
            txtLogs.setText("Error al cargar logs: " + e.getMessage());
        }
    }
    
    /**
     * Exportar informe a archivo de texto
     * Genera un informe completo con todos los datos del servidor
     */
    private void exportarInforme() {
        try {
            // Obtener carpeta de Descargas del usuario
            String rutaDescargas = System.getProperty("user.home") + java.io.File.separator + "Downloads";
            java.io.File carpetaDescargas = new java.io.File(rutaDescargas);
            
            // Crear carpeta si no existe (por si acaso)
            if (!carpetaDescargas.exists()) {
                carpetaDescargas.mkdirs();
            }
            
            // Generar nombre de archivo con timestamp
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date());
            String nombreArchivo = "informe_servidor_" + timestamp + ".txt";
            java.io.File archivoInforme = new java.io.File(carpetaDescargas, nombreArchivo);
            
            // Usar try-with-resources para cerrar automáticamente
            try (java.io.PrintWriter writer = new java.io.PrintWriter(archivoInforme, "UTF-8")) {
                
                // ===== ENCABEZADO =====
                writer.println("═══════════════════════════════════════════════════════════");
                writer.println("          INFORME DEL SERVIDOR - CHAT UNIVERSITARIO");
                writer.println("═══════════════════════════════════════════════════════════");
                writer.println("Fecha de generación: " + new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date()));
                writer.println();
                
                // ===== ESTADÍSTICAS GENERALES =====
                writer.println("\n╔═══════════════════════════════════════════════════════╗");
                writer.println("║           ESTADÍSTICAS GENERALES DEL SERVIDOR         ║");
                writer.println("╚═══════════════════════════════════════════════════════╝\n");
                
                try {
                    ServicioEstadisticas.EstadisticasGenerales statsGenerales = 
                        servicioEstadisticas.obtenerEstadisticasGenerales();
                    ServicioInformesLogs.EstadisticasLogs statsLogs = 
                        servicioLogs.obtenerEstadisticas();
                    
                    writer.println("📊 USUARIOS:");
                    writer.println("   • Total registrados: " + statsGenerales.getTotalUsuarios());
                    writer.println("   • En línea: " + statsGenerales.getUsuariosEnLinea());
                    writer.println("   • Desconectados: " + statsGenerales.getUsuariosDesconectados());
                    writer.println("   • Porcentaje en línea: " + String.format("%.1f%%", statsGenerales.getPorcentajeUsuariosEnLinea()));
                    
                    writer.println("\n📢 CANALES:");
                    writer.println("   • Total canales: " + statsGenerales.getTotalCanales());
                    writer.println("   • Canales activos: " + statsGenerales.getCanalesActivos());
                    writer.println("   • Canales inactivos: " + statsGenerales.getCanalesInactivos());
                    
                    writer.println("\n💬 MENSAJES:");
                    writer.println("   • Mensajes de texto: " + statsLogs.getTotalMensajesTexto());
                    writer.println("   • Mensajes de audio: " + statsLogs.getTotalMensajesAudio());
                    writer.println("   • Total mensajes: " + (statsLogs.getTotalMensajesTexto() + statsLogs.getTotalMensajesAudio()));
                    
                    writer.println("\n🎤 TRANSCRIPCIONES:");
                    writer.println("   • Exitosas: " + statsLogs.getTotalTranscripcionesExitosas());
                    writer.println("   • Fallidas: " + statsLogs.getTotalTranscripcionesFallidas());
                    writer.println("   • Tasa de éxito: " + String.format("%.1f%%", statsLogs.getPorcentajeExito()));
                    
                } catch (Exception e) {
                    writer.println("   [Error al obtener estadísticas: " + e.getMessage() + "]");
                }
                
                // ===== USUARIOS REGISTRADOS =====
                writer.println("\n\n╔═══════════════════════════════════════════════════════╗");
                writer.println("║              USUARIOS REGISTRADOS                     ║");
                writer.println("╚═══════════════════════════════════════════════════════╝\n");
                
                if (modeloUsuariosRegistrados.getRowCount() == 0) {
                    writer.println("   [No hay usuarios registrados]");
                } else {
                    for (int i = 0; i < modeloUsuariosRegistrados.getRowCount(); i++) {
                        writer.println(String.format("• ID: %s | %s <%s>",
                            modeloUsuariosRegistrados.getValueAt(i, 0),
                            modeloUsuariosRegistrados.getValueAt(i, 1),
                            modeloUsuariosRegistrados.getValueAt(i, 2)
                        ));
                        writer.println(String.format("  Registro: %s | %s | IP: %s",
                            modeloUsuariosRegistrados.getValueAt(i, 3),
                            modeloUsuariosRegistrados.getValueAt(i, 4),
                            modeloUsuariosRegistrados.getValueAt(i, 5) != null ? 
                                modeloUsuariosRegistrados.getValueAt(i, 5) : "N/A"
                        ));
                        writer.println();
                    }
                }
                writer.println("Total: " + modeloUsuariosRegistrados.getRowCount() + " usuarios");
                
                // ===== CANALES =====
                writer.println("\n\n╔═══════════════════════════════════════════════════════╗");
                writer.println("║              CANALES Y COMUNIDADES                    ║");
                writer.println("╚═══════════════════════════════════════════════════════╝\n");
                
                if (modeloCanalesUsuarios.getRowCount() == 0) {
                    writer.println("   [No hay canales activos]");
                } else {
                    for (int i = 0; i < modeloCanalesUsuarios.getRowCount(); i++) {
                        writer.println(String.format("• [%s] %s",
                            modeloCanalesUsuarios.getValueAt(i, 2), // Tipo
                            modeloCanalesUsuarios.getValueAt(i, 1)  // Nombre
                        ));
                        writer.println(String.format("  ID: %s | Creador: %s | Miembros: %s | Creado: %s",
                            modeloCanalesUsuarios.getValueAt(i, 0),
                            modeloCanalesUsuarios.getValueAt(i, 3),
                            modeloCanalesUsuarios.getValueAt(i, 4),
                            modeloCanalesUsuarios.getValueAt(i, 5)
                        ));
                        writer.println();
                    }
                }
                writer.println("Total: " + modeloCanalesUsuarios.getRowCount() + " canales");
                
                // ===== USUARIOS CONECTADOS =====
                writer.println("\n\n╔═══════════════════════════════════════════════════════╗");
                writer.println("║              USUARIOS CONECTADOS AHORA                ║");
                writer.println("╚═══════════════════════════════════════════════════════╝\n");
                
                if (modeloUsuariosConectados.getRowCount() == 0) {
                    writer.println("   [No hay usuarios conectados]");
                } else {
                    for (int i = 0; i < modeloUsuariosConectados.getRowCount(); i++) {
                        writer.println(String.format("• %s <%s> - Última conexión: %s",
                            modeloUsuariosConectados.getValueAt(i, 1),
                            modeloUsuariosConectados.getValueAt(i, 2),
                            modeloUsuariosConectados.getValueAt(i, 4)
                        ));
                    }
                }
                writer.println("\nTotal: " + modeloUsuariosConectados.getRowCount() + " usuarios en línea");
                
                // ===== MENSAJES DE AUDIO =====
                writer.println("\n\n╔═══════════════════════════════════════════════════════╗");
                writer.println("║              HISTORIAL DE AUDIOS                      ║");
                writer.println("╚═══════════════════════════════════════════════════════╝\n");
                
                if (modeloAudios.getRowCount() == 0) {
                    writer.println("   [No hay mensajes de audio]");
                } else {
                    int maxAudios = Math.min(50, modeloAudios.getRowCount()); // Limitar a 50
                    for (int i = 0; i < maxAudios; i++) {
                        writer.println(String.format("• %s → %s",
                            modeloAudios.getValueAt(i, 1), // Remitente
                            modeloAudios.getValueAt(i, 2)  // Destinatario
                        ));
                        writer.println(String.format("  Formato: %s | Duración: %s | Tamaño: %s | Fecha: %s",
                            modeloAudios.getValueAt(i, 3),
                            modeloAudios.getValueAt(i, 4),
                            modeloAudios.getValueAt(i, 5),
                            modeloAudios.getValueAt(i, 6)
                        ));
                        writer.println();
                    }
                    if (modeloAudios.getRowCount() > 50) {
                        writer.println("   ... y " + (modeloAudios.getRowCount() - 50) + " audios más");
                    }
                }
                writer.println("Total: " + modeloAudios.getRowCount() + " audios");
                
                // ===== LOGS DE MENSAJES CON TRANSCRIPCIONES =====
                writer.println("\n\n╔═══════════════════════════════════════════════════════╗");
                writer.println("║         LOGS DE MENSAJES Y TRANSCRIPCIONES            ║");
                writer.println("╚═══════════════════════════════════════════════════════╝\n");
                
                if (modeloLogsMensajes.getRowCount() == 0) {
                    writer.println("   [No hay logs de mensajes]");
                } else {
                    int maxLogs = Math.min(100, modeloLogsMensajes.getRowCount()); // Limitar a 100
                    for (int i = 0; i < maxLogs; i++) {
                        writer.println(String.format("[%s][%s] %s → %s",
                            modeloLogsMensajes.getValueAt(i, 1), // Tipo (TXT/AUD)
                            modeloLogsMensajes.getValueAt(i, 2), // Conversación (PVT/GRP)
                            modeloLogsMensajes.getValueAt(i, 3), // Remitente
                            modeloLogsMensajes.getValueAt(i, 4)  // Destinatario
                        ));
                        writer.println(String.format("  %s | IP: %s",
                            modeloLogsMensajes.getValueAt(i, 6), // Timestamp
                            modeloLogsMensajes.getValueAt(i, 7)  // IP
                        ));
                        writer.println(String.format("  Contenido: %s",
                            modeloLogsMensajes.getValueAt(i, 5)  // Contenido/Transcripción
                        ));
                        writer.println();
                    }
                    if (modeloLogsMensajes.getRowCount() > 100) {
                        writer.println("   ... y " + (modeloLogsMensajes.getRowCount() - 100) + " mensajes más");
                    }
                }
                writer.println("Total: " + modeloLogsMensajes.getRowCount() + " mensajes registrados");
                
                // ===== LOGS DEL SISTEMA =====
                writer.println("\n\n╔═══════════════════════════════════════════════════════╗");
                writer.println("║              LOGS DEL SISTEMA COMPLETOS               ║");
                writer.println("╚═══════════════════════════════════════════════════════╝\n");
                
                writer.println(txtLogs.getText());
                
                // ===== PIE =====
                writer.println("\n\n═══════════════════════════════════════════════════════════");
                writer.println("              FIN DEL INFORME - CHAT UNIVERSITARIO");
                writer.println("═══════════════════════════════════════════════════════════");
            }
            
            // Mostrar mensaje de éxito con la ruta completa
            JOptionPane.showMessageDialog(this,
                "✅ Informe exportado exitosamente\n\n" +
                "Archivo: " + nombreArchivo + "\n" +
                "Ubicación: " + archivoInforme.getAbsolutePath(),
                "Exportación Completa",
                JOptionPane.INFORMATION_MESSAGE);
            
            // Abrir la carpeta de Descargas en el explorador
            try {
                java.awt.Desktop.getDesktop().open(carpetaDescargas);
            } catch (Exception e) {
                // Si no puede abrir el explorador, no importa
            }
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                "❌ Error al exportar informe:\n" + e.getMessage(),
                "Error de Exportación",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
}
