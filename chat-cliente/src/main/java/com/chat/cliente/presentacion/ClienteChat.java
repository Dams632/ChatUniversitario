package com.chat.cliente.presentacion;

import java.util.Scanner;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.chat.cliente.datos.ConexionH2;
import com.chat.cliente.negocio.ServicioCliente;
import com.chat.cliente.presentacion.gui.ChatPrincipalFrame;
import com.chat.cliente.presentacion.gui.ConexionServidorDialog;
import com.chat.cliente.presentacion.gui.LoginFrameRefactored;
import com.chat.cliente.presentacion.gui.LoginFrameRefactored.LoginCallback;
import com.chat.common.dto.ResponseDTO;

/**
 * Interfaz de consola para el cliente del chat
 */
public class ClienteChat {
    
    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;
    
    private ServicioCliente servicioCliente;
    private Scanner scanner;
    private boolean ejecutando;
    
    public ClienteChat() {
        this.servicioCliente = new ServicioCliente(HOST, PUERTO);
        this.scanner = new Scanner(System.in);
        this.ejecutando = false;
    }
    
    /**
     * Iniciar cliente
     */
    public void iniciar() {
        try {
            // Inicializar base de datos local
            System.out.println("Inicializando base de datos local...");
            ConexionH2.inicializarBaseDatos();
            
            // Conectar al servidor
            System.out.println("Conectando al servidor...");
            servicioCliente.conectar();
            
            ejecutando = true;
            mostrarMenuPrincipal();
            
        } catch (Exception e) {
            System.err.println("Error al iniciar cliente: " + e.getMessage());
            e.printStackTrace();
        } finally {
            detener();
        }
    }
    
    /**
     * Mostrar menú principal
     */
    private void mostrarMenuPrincipal() {
        while (ejecutando) {
            System.out.println("\n===========================================");
            System.out.println("       CHAT UNIVERSITARIO - CLIENTE");
            System.out.println("===========================================");
            System.out.println("1. Registrarse");
            System.out.println("2. Iniciar Sesión");
            System.out.println("3. Salir");
            System.out.println("===========================================");
            System.out.print("Seleccione una opción: ");
            
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1":
                    registrarse();
                    break;
                case "2":
                    iniciarSesion();
                    break;
                case "3":
                    ejecutando = false;
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }
    
    /**
     * Registrar nuevo usuario
     */
    private void registrarse() {
        System.out.println("\n--- REGISTRO DE USUARIO ---");
        
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Email: ");
        String email = scanner.nextLine().trim();
        
        System.out.print("Dirección IP: ");
        String direccionIP = scanner.nextLine().trim();
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        ResponseDTO response = servicioCliente.registrar(username, email, password, direccionIP);
        
        if (response.isExito()) {
            System.out.println("✓ " + response.getMensaje());
        } else {
            System.out.println("✗ Error: " + response.getMensaje());
        }
    }
    
    /**
     * Iniciar sesión
     */
    private void iniciarSesion() {
        System.out.println("\n--- INICIAR SESIÓN ---");
        
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        
        ResponseDTO response = servicioCliente.login(username, password);
        
        if (response.isExito()) {
            System.out.println("✓ " + response.getMensaje());
            System.out.println("Bienvenido, " + servicioCliente.getUsername() + "!");
            mostrarMenuUsuario();
        } else {
            System.out.println("✗ Error: " + response.getMensaje());
        }
    }
    
    /**
     * Mostrar menú de usuario autenticado
     */
    private void mostrarMenuUsuario() {
        boolean enSesion = true;
        
        while (enSesion && ejecutando) {
            System.out.println("\n===========================================");
            System.out.println("   Usuario: " + servicioCliente.getUsername());
            System.out.println("===========================================");
            System.out.println("1. Ver usuarios en línea");
            System.out.println("2. Crear grupo");
            System.out.println("3. Ver mis grupos");
            System.out.println("4. Enviar mensaje (próximamente)");
            System.out.println("5. Cerrar sesión");
            System.out.println("===========================================");
            System.out.print("Seleccione una opción: ");
            
            String opcion = scanner.nextLine().trim();
            
            switch (opcion) {
                case "1":
                    verUsuariosOnline();
                    break;
                case "2":
                    crearGrupo();
                    break;
                case "3":
                    verGrupos();
                    break;
                case "4":
                    System.out.println("Función en desarrollo...");
                    break;
                case "5":
                    cerrarSesion();
                    enSesion = false;
                    break;
                default:
                    System.out.println("Opción inválida");
            }
        }
    }
    
    /**
     * Ver usuarios en línea
     */
    private void verUsuariosOnline() {
        ResponseDTO response = servicioCliente.obtenerUsuariosOnline();
        
        if (response.isExito()) {
            System.out.println("\n--- USUARIOS EN LÍNEA ---");
            Object usuarios = response.getDato("usuarios");
            System.out.println(usuarios);
        } else {
            System.out.println("✗ Error: " + response.getMensaje());
        }
    }
    
    /**
     * Crear grupo
     */
    private void crearGrupo() {
        System.out.println("\n--- CREAR GRUPO ---");
        
        System.out.print("Nombre del grupo: ");
        String nombre = scanner.nextLine().trim();
        
        System.out.print("Descripción: ");
        String descripcion = scanner.nextLine().trim();
        
        ResponseDTO response = servicioCliente.crearGrupo(nombre, descripcion);
        
        if (response.isExito()) {
            System.out.println("✓ " + response.getMensaje());
        } else {
            System.out.println("✗ Error: " + response.getMensaje());
        }
    }
    
    /**
     * Ver grupos
     */
    private void verGrupos() {
        ResponseDTO response = servicioCliente.obtenerGrupos();
        
        if (response.isExito()) {
            System.out.println("\n--- MIS GRUPOS ---");
            Object grupos = response.getDato("grupos");
            System.out.println(grupos);
        } else {
            System.out.println("✗ Error: " + response.getMensaje());
        }
    }
    
    /**
     * Cerrar sesión
     */
    private void cerrarSesion() {
        ResponseDTO response = servicioCliente.logout();
        
        if (response.isExito()) {
            System.out.println("✓ Sesión cerrada correctamente");
        } else {
            System.out.println("✗ Error: " + response.getMensaje());
        }
    }
    
    /**
     * Detener cliente
     */
    private void detener() {
        servicioCliente.desconectar();
        ConexionH2.cerrarConexion();
        scanner.close();
        System.out.println("\nCliente cerrado");
    }
    
    /**
     * Método principal
     */
    public static void main(String[] args) {
        // Inicializar base de datos local
        System.out.println("===========================================");
        System.out.println("   CHAT UNIVERSITARIO - INICIALIZANDO");
        System.out.println("===========================================");
        
        try {
            System.out.println("Inicializando base de datos local H2...");
            ConexionH2.inicializarBaseDatos();
            System.out.println("✓ Base de datos inicializada correctamente");
        } catch (Exception e) {
            System.err.println("✗ Error al inicializar base de datos: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Lanzar GUI de conexión al servidor
        SwingUtilities.invokeLater(() -> {
            try {
                // Configurar Look and Feel del sistema
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("No se pudo establecer el Look and Feel del sistema");
            }
            
            System.out.println("Mostrando diálogo de configuración de servidor...");
            
            // Mostrar diálogo de conexión
            ConexionServidorDialog dialogConexion = new ConexionServidorDialog(null);
            dialogConexion.setVisible(true);
            
            // Verificar si el usuario conectó
            if (!dialogConexion.isConectado()) {
                System.out.println("✗ Usuario canceló la conexión");
                System.exit(0);
                return;
            }
            
            // Obtener configuración del servidor
            String host = dialogConexion.getHost();
            int puerto = dialogConexion.getPuerto();
            
            System.out.println("✓ Configuración del servidor: " + host + ":" + puerto);
            
            // Crear instancia de ServicioCliente con la configuración proporcionada
            ServicioCliente servicioCliente = new ServicioCliente(host, puerto);
            
            // Agregar shutdown hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("\nCerrando cliente...");
                servicioCliente.desconectar();
                ConexionH2.cerrarConexion();
            }));
            
            System.out.println("Iniciando interfaz gráfica de login...");
            
            // Crear callback para después del login exitoso
            LoginCallback callback = new LoginCallback() {
                @Override
                public void onLoginExitoso(String username, ResponseDTO response) {
                    System.out.println("✓ Login exitoso: " + username);
                    System.out.println("  Token: " + response.getDatos());
                    
                    try {
                        // Establecer el usuario para la base de datos H2
                        System.out.println("Configurando base de datos para usuario: " + username);
                        ConexionH2.establecerUsuario(username);
                        
                        // Cerrar conexión anterior si existe y reconectar con la BD del usuario
                        ConexionH2.cerrarConexion();
                        ConexionH2.inicializarBaseDatos();
                        System.out.println("✓ Base de datos del usuario inicializada");
                    } catch (Exception e) {
                        System.err.println("✗ Error al inicializar base de datos del usuario: " + e.getMessage());
                        e.printStackTrace();
                    }
                    
                    // Abrir ventana principal del chat
                    SwingUtilities.invokeLater(() -> {
                        ChatPrincipalFrame chatFrame = new ChatPrincipalFrame(servicioCliente, username);
                        chatFrame.setVisible(true);
                        System.out.println("✓ Ventana de chat principal abierta");
                    });
                }
                
                @Override
                public void onAbrirRegistro() {
                    System.out.println("→ Abriendo ventana de registro...");
                    // La ventana de registro se abre desde el LoginFrameRefactored
                }
            };
            
            LoginFrameRefactored loginFrame = new LoginFrameRefactored(servicioCliente, callback);
            loginFrame.setVisible(true);
            System.out.println("✓ Interfaz gráfica iniciada");
        });
    }
}
