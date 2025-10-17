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
import javax.swing.border.EmptyBorder;

/**
 * Componente visual para representar una burbuja de audio en el chat
 */
public class BurbujaAudio extends JPanel {
    
    // Constantes de diseño
    private static final Color COLOR_BURBUJA_PROPIA = new Color(52, 152, 219); // Azul
    private static final Color COLOR_BURBUJA_RECIBIDA = Color.WHITE;
    private static final Color COLOR_HEADER_PROPIO = new Color(220, 220, 220);
    private static final Color COLOR_HEADER_RECIBIDO = new Color(120, 120, 120);
    private static final Color COLOR_BORDE = new Color(189, 195, 199);
    
    private static final int PADDING_HORIZONTAL = 12;
    private static final int PADDING_VERTICAL = 8;
    private static final int BORDER_RADIUS = 15;
    private static final int ANCHO_BURBUJA = 280;
    private static final int ALTURA_BURBUJA = 90;
    
    private final Color colorBurbuja;
    private final AudioPlayer audioPlayer;
    
    /**
     * Constructor de burbuja de audio
     * @param remitente Nombre del remitente
     * @param audioData Datos del audio en bytes
     * @param duracionSegundos Duración del audio en segundos
     * @param timestamp Hora del mensaje (formato "HH:mm:ss")
     * @param esMio true si es audio propio, false si es recibido
     */
    public BurbujaAudio(String remitente, byte[] audioData, long duracionSegundos, 
                        String timestamp, boolean esMio) {
        
        // Configurar colores según origen
        this.colorBurbuja = esMio ? COLOR_BURBUJA_PROPIA : COLOR_BURBUJA_RECIBIDA;
        
        // Crear reproductor de audio
        this.audioPlayer = new AudioPlayer(audioData, duracionSegundos);
        
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
        
        setPreferredSize(new Dimension(ANCHO_BURBUJA, ALTURA_BURBUJA));
        setMaximumSize(new Dimension(ANCHO_BURBUJA, ALTURA_BURBUJA));
        
        // Crear header con remitente y timestamp
        JLabel lblHeader = crearHeader(remitente, timestamp, esMio);
        add(lblHeader, BorderLayout.NORTH);
        
        // Panel central con ícono de audio
        JPanel panelCentral = new JPanel(new BorderLayout(5, 0));
        panelCentral.setOpaque(false);
        
        // Ícono de micrófono
        JLabel lblIcono = new JLabel("🎤");
        lblIcono.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        panelCentral.add(lblIcono, BorderLayout.WEST);
        
        // Agregar reproductor de audio
        panelCentral.add(audioPlayer, BorderLayout.CENTER);
        
        add(panelCentral, BorderLayout.CENTER);
    }
    
    /**
     * Crear label de encabezado con remitente y hora
     */
    private JLabel crearHeader(String remitente, String timestamp, boolean esMio) {
        JLabel lblHeader = new JLabel(remitente + " • " + timestamp + " • Audio");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHeader.setForeground(esMio ? COLOR_HEADER_PROPIO : COLOR_HEADER_RECIBIDO);
        return lblHeader;
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
        
        // Dibujar borde suave
        g2.setColor(COLOR_BORDE);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, BORDER_RADIUS, BORDER_RADIUS);
        
        g2.dispose();
    }
    
    /**
     * Limpiar recursos cuando se destruye el componente
     */
    public void dispose() {
        if (audioPlayer != null) {
            audioPlayer.dispose();
        }
    }
}
