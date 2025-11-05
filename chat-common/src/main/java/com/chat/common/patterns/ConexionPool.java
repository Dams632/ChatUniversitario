package com.chat.common.patterns;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
    private final AtomicInteger conexionesAbiertas;
    
    public ConexionPool(String url, String user, String password, int maxSize) {
        this.url = url;
        this.user = user;
        this.password = password;
        this.maxSize = maxSize;
    this.conexionesAbiertas = new AtomicInteger(0);
        this.pool = new LinkedBlockingQueue<>(maxSize);
        
        // Inicializar pool con conexiones mínimas
        inicializarPool(maxSize / 2);
    }
    
    /**
     * Inicializar pool con conexiones
     */
    private void inicializarPool(int initialSize) {
        int limite = Math.min(initialSize, maxSize);
        for (int i = 0; i < limite; i++) {
            if (!incrementarSiHayCapacidad()) {
                break;
            }
            try {
                pool.offer(crearNuevaConexion());
            } catch (SQLException e) {
                disminuirConteo();
                System.err.println("Error al inicializar pool: " + e.getMessage());
            }
        }
    }

    /**
     * Incrementa el conteo de conexiones si no se supera el máximo configurado.
     */
    private boolean incrementarSiHayCapacidad() {
        while (true) {
            int actual = conexionesAbiertas.get();
            if (actual >= maxSize) {
                return false;
            }
            if (conexionesAbiertas.compareAndSet(actual, actual + 1)) {
                return true;
            }
        }
    }

    /**
     * Disminuye el conteo de conexiones abiertas asegurando que no sea negativo.
     */
    private void disminuirConteo() {
        conexionesAbiertas.updateAndGet(actual -> actual > 0 ? actual - 1 : 0);
    }

    /**
     * Verifica si la conexión es utilizable.
     */
    private boolean conexionValida(Connection conn) throws SQLException {
        return conn != null && !conn.isClosed();
    }

    /**
     * Cierra de forma silenciosa una conexión.
     */
    private void cerrarSilencioso(Connection conn) {
        if (conn == null) {
            return;
        }
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println("Error al inicializar pool: " + e.getMessage());
        }
    }
    
    /**
     * Crear una nueva conexión
     */
    private Connection crearNuevaConexion() throws SQLException {
        Connection conn = DriverManager.getConnection(url, user, password);
        System.out.println("Nueva conexión creada. Total abiertas: " + conexionesAbiertas.get());
        return conn;
    }
    
    /**
     * Obtener conexión del pool
     */
    public Connection obtenerConexion() throws SQLException {
        Connection conn = pool.poll();

        if (conn != null) {
            if (conexionValida(conn)) {
                return conn;
            }
            disminuirConteo();
            cerrarSilencioso(conn);
            return obtenerConexion();
        }

        if (!incrementarSiHayCapacidad()) {
            throw new SQLException("No hay conexiones disponibles (límite del pool alcanzado: " + maxSize + ")");
        }

        try {
            return crearNuevaConexion();
        } catch (SQLException e) {
            disminuirConteo();
            throw e;
        }
    }
    
    /**
     * Devolver conexión al pool
     */
    public void liberarConexion(Connection conn) {
        if (conn != null) {
            try {
                if (!conn.isClosed()) {
                    if (!pool.offer(conn)) {
                        // Si la cola está llena descartamos la conexión
                        cerrarSilencioso(conn);
                        disminuirConteo();
                    }
                } else {
                    disminuirConteo();
                }
            } catch (SQLException e) {
                System.err.println("Error al verificar conexión: " + e.getMessage());
                disminuirConteo();
                cerrarSilencioso(conn);
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
        conexionesAbiertas.set(0);
        System.out.println("Pool de conexiones cerrado");
    }
    
    public int getTamanoActual() {
        return conexionesAbiertas.get();
    }
    
    public int getTamanoMaximo() {
        return maxSize;
    }
}
