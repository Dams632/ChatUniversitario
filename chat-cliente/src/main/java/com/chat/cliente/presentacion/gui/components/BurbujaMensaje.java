package com.chat.cliente.presentacion.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

/**
 * Componente visual para representar una burbuja de mensaje en el chat
 * Muestra mensajes con diseño de burbuja estilo aplicaciones de mensajería modernas
 * 
 * Características:
 * - Burbujas azules a la derecha para mensajes propios
 * - Burbujas blancas a la izquierda para mensajes recibidos
 * - Bordes redondeados
 * - Word wrap automático
 */
public class BurbujaMensaje extends JPanel {
    
    // Constantes de diseño - COLORES MEJORADOS
    private static final Color COLOR_BURBUJA_PROPIA = new Color(16, 185, 129); // Verde moderno (Tailwind Green 500)
    private static final Color COLOR_TEXTO_PROPIO = Color.WHITE;
    private static final Color COLOR_BURBUJA_RECIBIDA = new Color(243, 244, 246); // Gris muy claro (Tailwind Gray 100)
    private static final Color COLOR_TEXTO_RECIBIDO = new Color(31, 41, 55); // Gris oscuro (Tailwind Gray 800)
    private static final Color COLOR_HEADER_PROPIO = new Color(240, 253, 244); // Verde muy claro
    private static final Color COLOR_HEADER_RECIBIDO = new Color(107, 114, 128); // Gris medio
    private static final Color COLOR_BORDE_PROPIO = new Color(5, 150, 105); // Verde más oscuro para borde
    private static final Color COLOR_BORDE_RECIBIDO = new Color(209, 213, 219); // Gris claro para borde
    
    private static final int PADDING_HORIZONTAL = 12;
    private static final int PADDING_VERTICAL = 8;
    private static final int BORDER_RADIUS = 15;
    private static final int ANCHO_MAXIMO = 350;
    private static final int ALTURA_BASE = 60;
    private static final int ALTURA_POR_LINEA = 18;
    private static final int CARACTERES_POR_LINEA = 50;
    
    private final String contenido;
    private final Color colorBurbuja;
    private final Color colorTexto;
    private final Color colorBorde;
    
    /**
     * Constructor de burbuja de mensaje
     * @param remitente Nombre del remitente del mensaje
     * @param contenido Texto del mensaje
     * @param timestamp Hora del mensaje (formato "HH:mm:ss")
     * @param esMio true si es mensaje propio, false si es recibido
     */
    public BurbujaMensaje(String remitente, String contenido, String timestamp, boolean esMio) {
        this.contenido = contenido;
        
        // Configurar colores según origen del mensaje
        if (esMio) {
            this.colorBurbuja = COLOR_BURBUJA_PROPIA;
            this.colorTexto = COLOR_TEXTO_PROPIO;
            this.colorBorde = COLOR_BORDE_PROPIO;
        } else {
            this.colorBurbuja = COLOR_BURBUJA_RECIBIDA;
            this.colorTexto = COLOR_TEXTO_RECIBIDO;
            this.colorBorde = COLOR_BORDE_RECIBIDO;
        }
        
        configurarComponente(remitente, timestamp, esMio);
    }
    
    /**
     * Configurar el componente visual
     */
    private void configurarComponente(String remitente, String timestamp, boolean esMio) {
        setOpaque(false);
        setLayout(new BorderLayout(5, 3));
        setBorder(new EmptyBorder(PADDING_VERTICAL, PADDING_HORIZONTAL, 
                                  PADDING_VERTICAL, PADDING_HORIZONTAL));
        
        // Calcular tamaño basado en contenido
        setPreferredSize(calcularTamanio());
        setMaximumSize(new Dimension(ANCHO_MAXIMO, Integer.MAX_VALUE));
        
        // Crear header con remitente y timestamp
        JLabel lblHeader = crearHeader(remitente, timestamp, esMio);
        add(lblHeader, BorderLayout.NORTH);
        
        // Crear área de texto del mensaje
        JTextArea txtContenido = crearAreaContenido();
        add(txtContenido, BorderLayout.CENTER);
    }
    
    /**
     * Crear label de encabezado con remitente y hora
     */
    private JLabel crearHeader(String remitente, String timestamp, boolean esMio) {
        JLabel lblHeader = new JLabel(remitente + " • " + timestamp);
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeader.setForeground(esMio ? COLOR_HEADER_PROPIO : COLOR_HEADER_RECIBIDO);
        return lblHeader;
    }
    
    /**
     * Crear área de texto para el contenido del mensaje
     */
    private JTextArea crearAreaContenido() {
        JTextArea txtContenido = new JTextArea(contenido);
        txtContenido.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtContenido.setForeground(colorTexto);
        txtContenido.setBackground(colorBurbuja);
        txtContenido.setOpaque(false);
        txtContenido.setEditable(false);
        txtContenido.setLineWrap(true);
        txtContenido.setWrapStyleWord(true);
        txtContenido.setBorder(null);
        return txtContenido;
    }
    
    /**
     * Calcular tamaño del componente basado en el contenido
     */
    private Dimension calcularTamanio() {
        int anchoTexto = ANCHO_MAXIMO - (PADDING_HORIZONTAL * 2);
        int lineas = Math.max(1, (contenido.length() + CARACTERES_POR_LINEA - 1) / CARACTERES_POR_LINEA);
        int altura = ALTURA_BASE + (lineas * ALTURA_POR_LINEA);
        return new Dimension(ANCHO_MAXIMO, altura);
    }
    
    /**
     * Pintar componente con bordes redondeados
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Dibujar burbuja redondeada con relleno
        g2.setColor(colorBurbuja);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
        
        // Dibujar borde suave (usa el color específico según si es mensaje propio o recibido)
        g2.setColor(colorBorde);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BORDER_RADIUS, BORDER_RADIUS);
        
        g2.dispose();
    }
}
