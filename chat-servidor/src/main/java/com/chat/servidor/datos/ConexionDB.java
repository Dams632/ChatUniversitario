package com.chat.servidor.datos;

import com.chat.common.patterns.ConexionPool;
import com.chat.common.utils.ConfiguracionManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Gestor de conexiones a la base de datos MySQL con Object Pool
 */
public class ConexionDB {
    
    private static ConexionPool pool;
    private static ConfiguracionManager config;
    private static String URL;
    private static String USER;
    private static String PASSWORD;
    private static Connection conexion = null;
    
    /**
     * Inicializar pool de conexiones
     */
    private static void inicializarPool() throws SQLException {
        if (pool == null) {
            // Cargar configuración
            config = new ConfiguracionManager("config.properties");
            
            URL = config.getPropiedad("jdbc.url", "jdbc:mysql://localhost:3306/chat_universitario");
            USER = config.getPropiedad("jdbc.username", "root");
            PASSWORD = config.getPropiedad("jdbc.password", "root");
            
            int poolMin = config.getPropiedadInt("jdbc.pool.min", 5);
            int poolMax = config.getPropiedadInt("jdbc.pool.max", 20);
            
            try {
                Class.forName(config.getPropiedad("jdbc.driver", "com.mysql.cj.jdbc.Driver"));
            } catch (ClassNotFoundException e) {
                throw new SQLException("Driver MySQL no encontrado", e);
            }
            
            pool = new ConexionPool(URL, USER, PASSWORD, poolMax);
            System.out.println("Pool de conexiones inicializado (max: " + poolMax + ")");
        }
    }
    
    /**
     * Obtener conexión del pool
     */
    public static Connection obtenerConexion() throws SQLException {
        if (pool == null) {
            inicializarPool();
        }
        return pool.obtenerConexion();
    }
    
    /**
     * Liberar conexión al pool
     */
    public static void liberarConexion(Connection conn) {
        if (pool != null && conn != null) {
            pool.liberarConexion(conn);
        }
    }
    
    /**
     * Cerrar pool de conexiones
     */
    public static void cerrarConexion() {
        if (pool != null) {
            pool.cerrarPool();
            pool = null;
        }
    }
    
    /**
     * Obtener configuración
     */
    public static ConfiguracionManager getConfig() {
        if (config == null) {
            config = new ConfiguracionManager("config.properties");
        }
        return config;
    }
    
    /**
     * Inicializar base de datos y tablas
     */
    public static void inicializarBaseDatos() throws SQLException {
        Connection conn = obtenerConexion();
        
        try (Statement stmt = conn.createStatement()) {
            // Tabla de usuarios
            String crearTablaUsuarios = 
                "CREATE TABLE IF NOT EXISTS usuarios (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "username VARCHAR(50) UNIQUE NOT NULL," +
                "email VARCHAR(100) UNIQUE NOT NULL," +
                "password VARCHAR(255) NOT NULL," +
                "en_linea BOOLEAN DEFAULT FALSE," +
                "fecha_registro DATETIME NOT NULL," +
                "ultima_conexion DATETIME" +
                ")";
            
            // Tabla de grupos
            String crearTablaGrupos = 
                "CREATE TABLE IF NOT EXISTS grupos (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "nombre VARCHAR(100) NOT NULL," +
                "descripcion TEXT," +
                "creador_id BIGINT NOT NULL," +
                "fecha_creacion DATETIME NOT NULL," +
                "activo BOOLEAN DEFAULT TRUE," +
                "FOREIGN KEY (creador_id) REFERENCES usuarios(id)" +
                ")";
            
            // Tabla de miembros de grupos
            String crearTablaGrupoMiembros = 
                "CREATE TABLE IF NOT EXISTS grupo_miembros (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "grupo_id BIGINT NOT NULL," +
                "usuario_id BIGINT NOT NULL," +
                "fecha_union DATETIME NOT NULL," +
                "FOREIGN KEY (grupo_id) REFERENCES grupos(id)," +
                "FOREIGN KEY (usuario_id) REFERENCES usuarios(id)," +
                "UNIQUE KEY unique_grupo_usuario (grupo_id, usuario_id)" +
                ")";
            
            // Tabla de mensajes
            String crearTablaMensajes = 
                "CREATE TABLE IF NOT EXISTS mensajes (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
                "remitente_id BIGINT NOT NULL," +
                "destinatario_id BIGINT," +
                "grupo_id BIGINT," +
                "contenido TEXT NOT NULL," +
                "fecha_envio DATETIME NOT NULL," +
                "leido BOOLEAN DEFAULT FALSE," +
                "tipo_mensaje VARCHAR(20) NOT NULL," +
                "FOREIGN KEY (remitente_id) REFERENCES usuarios(id)," +
                "FOREIGN KEY (destinatario_id) REFERENCES usuarios(id)," +
                "FOREIGN KEY (grupo_id) REFERENCES grupos(id)" +
                ")";
            
            stmt.execute(crearTablaUsuarios);
            stmt.execute(crearTablaGrupos);
            stmt.execute(crearTablaGrupoMiembros);
            stmt.execute(crearTablaMensajes);
            
            System.out.println("Base de datos inicializada correctamente");
        }
    }
}
