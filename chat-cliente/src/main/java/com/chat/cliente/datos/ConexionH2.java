package com.chat.cliente.datos;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor de conexión a base de datos H2 embebida
 */
public class ConexionH2 {
    
    private static Connection conexion = null;
    private static final String USER = "sa";
    private static final String PASSWORD = "";
    private static String dbIdentificador = null;
    private static String usernameActual = null;
    
    /**
     * Establecer el username del usuario actual
     * Esto determina qué base de datos se utilizará
     */
    public static void establecerUsuario(String username) {
        if (username != null && !username.trim().isEmpty()) {
            usernameActual = username;
            // Sanitizar el username para usar como nombre de archivo
            dbIdentificador = "cliente_" + username.replaceAll("[^a-zA-Z0-9_-]", "_");
            System.out.println("Base de datos asignada para usuario: " + username);
        }
    }
    
    /**
     * Obtener la ruta de la base de datos H2
     * Cada usuario tiene su propia base de datos basada en su username
     */
    private static String obtenerRutaBaseDatos() {
        // Usar directorio home del usuario para la base de datos
        String userHome = System.getProperty("user.home");
        String dirDatabase = userHome + File.separator + ".chat-universitario" + 
                            File.separator + "database";
        
        // Crear directorio si no existe
        File dir = new File(dirDatabase);
        if (!dir.exists()) {
            dir.mkdirs();
            System.out.println("Directorio de base de datos creado: " + dirDatabase);
        }
        
        // Si no hay username establecido, usar uno temporal
        if (dbIdentificador == null) {
            dbIdentificador = "cliente_temporal_" + System.currentTimeMillis();
            System.out.println("ADVERTENCIA: Usando base de datos temporal. Llama a establecerUsuario() después del login.");
        }
        
        // Retornar URL de conexión JDBC H2 con modo AUTO_SERVER para permitir múltiples conexiones
        String dbPath = dirDatabase + File.separator + dbIdentificador;
        return "jdbc:h2:file:" + dbPath + ";AUTO_SERVER=TRUE";
    }
    
    /**
     * Obtener conexión a la base de datos H2
     */
    public static Connection obtenerConexion() throws SQLException {
        if (conexion == null || conexion.isClosed()) {
            try {
                Class.forName("org.h2.Driver");
                String dbUrl = obtenerRutaBaseDatos();
                conexion = DriverManager.getConnection(dbUrl, USER, PASSWORD);
                System.out.println("Conexión a H2 establecida en: " + dbUrl);
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver H2 no encontrado", e);
            }
        }
        return conexion;
    }
    
    /**
     * Cerrar conexión
     */
    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                if (!conexion.isClosed()) {
                    conexion.close();
                    System.out.println("Conexión a H2 cerrada: " + dbIdentificador);
                }
                conexion = null;
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Obtener el identificador único de la base de datos de este cliente
     */
    public static String obtenerIdentificadorDB() {
        return dbIdentificador;
    }
    
    /**
     * Inicializar base de datos local
     */
    public static void inicializarBaseDatos() throws SQLException {
        Connection conn = obtenerConexion();
        
        try (Statement stmt = conn.createStatement()) {
            // Tabla de logs de mensajes
            String crearTablaLogs = 
                "CREATE TABLE IF NOT EXISTS logs_mensajes (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "remitente_username VARCHAR(50) NOT NULL," +
                "destinatario_username VARCHAR(50)," +
                "grupo_nombre VARCHAR(100)," +
                "contenido TEXT NOT NULL," +
                "fecha_envio TIMESTAMP NOT NULL," +
                "tipo_mensaje VARCHAR(20) NOT NULL" +
                ")";
            
            // Tabla de sesión del usuario
            String crearTablaSesion = 
                "CREATE TABLE IF NOT EXISTS sesion_usuario (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "usuario_id BIGINT," +
                "username VARCHAR(50)," +
                "token VARCHAR(255)," +
                "fecha_login TIMESTAMP" +
                ")";
            
            // Tabla de logs de audios
            String crearTablaAudios = 
                "CREATE TABLE IF NOT EXISTS logs_audios (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "remitente_username VARCHAR(50) NOT NULL," +
                "destinatario_username VARCHAR(50)," +
                "grupo_nombre VARCHAR(100)," +
                "contenido_audio BLOB NOT NULL," +
                "formato VARCHAR(20) NOT NULL," +
                "duracion_segundos BIGINT NOT NULL," +
                "fecha_envio TIMESTAMP NOT NULL" +
                ")";
            
            stmt.execute(crearTablaLogs);
            stmt.execute(crearTablaSesion);
            stmt.execute(crearTablaAudios);
            
            System.out.println("Base de datos local H2 inicializada");
        }
    }
}
