package com.chat.cliente.presentacion.gui.components;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Componente para reproducir audio
 */
public class AudioPlayer extends JPanel {
    
    private final byte[] audioData;
    private final long duracionSegundos;
    private Clip clip;
    private Timer progressTimer;
    private boolean playing = false;
    
    private JButton btnPlay;
    private JProgressBar progressBar;
    private JLabel lblDuration;
    
    /**
     * Constructor
     */
    public AudioPlayer(byte[] audioData, long duracionSegundos) {
        this.audioData = audioData;
        this.duracionSegundos = duracionSegundos;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        setOpaque(false);
        
        // Botón play/pause
        btnPlay = new JButton("▶");
        btnPlay.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnPlay.setBackground(new Color(52, 152, 219));
        btnPlay.setForeground(Color.WHITE);
        btnPlay.setFocusPainted(false);
        btnPlay.setBorderPainted(false);
        btnPlay.setPreferredSize(new Dimension(30, 30));
        btnPlay.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPlay.addActionListener(e -> togglePlayPause());
        
        // Efecto hover
        btnPlay.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnPlay.setBackground(new Color(41, 128, 185));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnPlay.setBackground(new Color(52, 152, 219));
            }
        });
        
        add(btnPlay);
        
        // Barra de progreso
        progressBar = new JProgressBar(0, 100);
        progressBar.setPreferredSize(new Dimension(150, 20));
        progressBar.setStringPainted(false);
        progressBar.setForeground(new Color(52, 152, 219));
        progressBar.setBackground(new Color(236, 240, 241));
        add(progressBar);
        
        // Etiqueta de duración
        lblDuration = new JLabel(formatearTiempo(duracionSegundos));
        lblDuration.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDuration.setForeground(new Color(127, 140, 141));
        add(lblDuration);
        
        // Timer para actualizar progreso
        progressTimer = new Timer(100, e -> actualizarProgreso());
    }
    
    private void togglePlayPause() {
        if (playing) {
            pausar();
        } else {
            reproducir();
        }
    }
    
    private void reproducir() {
        try {
            if (clip == null || !clip.isOpen()) {
                // Crear clip de audio
                AudioFormat format = AudioRecorder.getAudioFormat();
                ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
                AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize());
                
                clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                
                // Listener para cuando termine la reproducción
                clip.addLineListener(event -> {
                    if (event.getType() == javax.sound.sampled.LineEvent.Type.STOP) {
                        if (clip.getMicrosecondPosition() >= clip.getMicrosecondLength()) {
                            SwingUtilities.invokeLater(() -> {
                                detener();
                            });
                        }
                    }
                });
            }
            
            clip.start();
            playing = true;
            btnPlay.setText("⏸");
            progressTimer.start();
            
        } catch (LineUnavailableException | IOException e) {
            System.err.println("Error al reproducir audio: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void pausar() {
        if (clip != null && clip.isRunning()) {
            clip.stop();
            playing = false;
            btnPlay.setText("▶");
            progressTimer.stop();
        }
    }
    
    private void detener() {
        if (clip != null) {
            clip.stop();
            clip.setMicrosecondPosition(0);
            playing = false;
            btnPlay.setText("▶");
            progressTimer.stop();
            progressBar.setValue(0);
            lblDuration.setText(formatearTiempo(duracionSegundos));
        }
    }
    
    private void actualizarProgreso() {
        if (clip != null && playing) {
            long currentPos = clip.getMicrosecondPosition();
            long totalLength = clip.getMicrosecondLength();
            
            if (totalLength > 0) {
                int progress = (int) ((currentPos * 100) / totalLength);
                progressBar.setValue(progress);
                
                long remainingSeconds = (totalLength - currentPos) / 1_000_000;
                lblDuration.setText(formatearTiempo(remainingSeconds));
            }
        }
    }
    
    private String formatearTiempo(long segundos) {
        long mins = segundos / 60;
        long secs = segundos % 60;
        return String.format("%d:%02d", mins, secs);
    }
    
    /**
     * Limpiar recursos
     */
    public void dispose() {
        if (progressTimer != null) {
            progressTimer.stop();
        }
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }
}
