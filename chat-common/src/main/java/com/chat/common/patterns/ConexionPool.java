package com.chat.common.patterns;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Patrón Object Pool para gestionar conexiones de base de datos
 * Reutiliza conexiones en lugar de crear nuevas cada vez
 */
public class ConexionPool {
    
    private final BlockingQueue<Connection> pool;
    private final String url;
    private final String user;
    private final String password;
    private final int maxSize;
    private int currentSize;
    
    public ConexionPool(String url, String user, String password, int maxSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxSize = maxSize;
        this.currentSize = 0;
        this.pool = new LinkedBlockingQueue<>(maxSize);
        
        // Inicializar pool con conexiones mínimas
        inicializarPool(maxSize / 2);
    }
    
    /**
     * Inicializar pool con conexiones
     */
    private void inicializarPool(int initialSize) {
        try {
            for (int i = 0; i < initialSize; i++) {
                pool.offer(crearNuevaConexion());
            }
        } catch (SQLException e) {
            System.err.println("Error al inicializar pool: " + e.getMessage());
        }
    }
    
    /**
     * Crear una nueva conexión
     */
    private Connection crearNuevaConexion() throws SQLException {
        Connection conn = DriverManager.getConnection(url, user, password);
        currentSize++;
        System.out.println("Nueva conexión creada. Total: " + currentSize);
        return conn;
    }
    
    /**
     * Obtener conexión del pool
     */
    public Connection obtenerConexion() throws SQLException {
        Connection conn = pool.poll();
        
        if (conn == null) {
            if (currentSize < maxSize) {
                conn = crearNuevaConexion();
            } else {
                try {
                    // Esperar por una conexión disponible
                    conn = pool.take();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new SQLException("Interrompido mientras esperaba conexión", e);
                }
            }
        }
        
        // Verificar si la conexión es válida
        if (conn != null && conn.isClosed()) {
            currentSize--;
            conn = crearNuevaConexion();
        }
        
        return conn;
    }
    
    /**
     * Devolver conexión al pool
     */
    public void liberarConexion(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    pool.offer(conn);
                } else {
                    currentSize--;
                }
            } catch (SQLException e) {
                System.err.println("Error al verificar conexión: " + e.getMessage());
            }
        }
    }
    
    /**
     * Cerrar todas las conexiones del pool
     */
    public void cerrarPool() {
        for (Connection conn : pool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
        pool.clear();
        currentSize = 0;
        System.out.println("Pool de conexiones cerrado");
    }
    
    public int getTamanoActual() {
        return pool.size();
    }
    
    public int getTamanoMaximo() {
        return maxSize;
    }
}
