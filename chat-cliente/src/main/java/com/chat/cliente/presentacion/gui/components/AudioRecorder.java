package com.chat.cliente.presentacion.gui.components;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;

/**
 * Componente para grabar audio desde el micrófono
 */
public class AudioRecorder extends JDialog {
    
    private static final AudioFormat AUDIO_FORMAT = new AudioFormat(
        16000,  // Sample rate
        16,     // Sample size in bits
        1,      // Channels (mono)
        true,   // Signed
        false   // Little endian
    );
    
    private TargetDataLine targetLine;
    private Thread recordingThread;
    private ByteArrayOutputStream audioOutputStream;
    private boolean recording = false;
    private long startTime;
    private long recordingDuration = 0;
    
    private JButton btnRecord;
    private JButton btnStop;
    private JButton btnCancel;
    private JLabel lblStatus;
    private JLabel lblTimer;
    private Timer timer;
    
    private byte[] recordedAudio;
    private boolean cancelled = false;
    
    /**
     * Constructor
     */
    public AudioRecorder(JFrame parent) {
        super(parent, "Grabar Audio", true);
        initComponents();
        setLocationRelativeTo(parent);
    }
    
    private void initComponents() {
        setSize(400, 250);
        setResizable(false);
        
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        
        // Panel superior - Estado
        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        statusPanel.setBackground(Color.WHITE);
        
        lblStatus = new JLabel("Presiona REC para comenzar a grabar");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblStatus.setAlignmentX(CENTER_ALIGNMENT);
        statusPanel.add(lblStatus);
        
        statusPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        
        lblTimer = new JLabel("00:00");
        lblTimer.setFont(new Font("Segoe UI", Font.BOLD, 36));
        lblTimer.setForeground(new Color(52, 152, 219));
        lblTimer.setAlignmentX(CENTER_ALIGNMENT);
        statusPanel.add(lblTimer);
        
        mainPanel.add(statusPanel, BorderLayout.CENTER);
        
        // Panel inferior - Botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        btnRecord = crearBoton("🎤 REC", new Color(231, 76, 60));
        btnRecord.addActionListener(e -> iniciarGrabacion());
        buttonPanel.add(btnRecord);
        
        btnStop = crearBoton("⏹ STOP", new Color(39, 174, 96));
        btnStop.setEnabled(false);
        btnStop.addActionListener(e -> detenerGrabacion());
        buttonPanel.add(btnStop);
        
        btnCancel = crearBoton("✖ Cancelar", new Color(149, 165, 166));
        btnCancel.addActionListener(e -> cancelar());
        buttonPanel.add(btnCancel);
        
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Timer para actualizar el contador
        timer = new Timer(100, e -> actualizarTimer());
    }
    
    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setBackground(color);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorderPainted(false);
        boton.setPreferredSize(new Dimension(110, 35));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                if (boton.isEnabled()) {
                    boton.setBackground(color.darker());
                }
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(color);
            }
        });
        
        return boton;
    }
    
    private void iniciarGrabacion() {
        try {
            // Configurar línea de audio
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, AUDIO_FORMAT);
            
            if (!AudioSystem.isLineSupported(info)) {
                JOptionPane.showMessageDialog(this,
                    "Tu micrófono no soporta el formato de audio requerido",
                    "Error de Audio",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            targetLine = (TargetDataLine) AudioSystem.getLine(info);
            targetLine.open(AUDIO_FORMAT);
            targetLine.start();
            
            audioOutputStream = new ByteArrayOutputStream();
            recording = true;
            startTime = System.currentTimeMillis();
            
            // Actualizar UI
            btnRecord.setEnabled(false);
            btnStop.setEnabled(true);
            lblStatus.setText("🔴 Grabando...");
            lblStatus.setForeground(new Color(231, 76, 60));
            timer.start();
            
            // Thread para grabar audio
            recordingThread = new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (recording) {
                    int bytesRead = targetLine.read(buffer, 0, buffer.length);
                    audioOutputStream.write(buffer, 0, bytesRead);
                }
            });
            recordingThread.start();
            
        } catch (LineUnavailableException e) {
            JOptionPane.showMessageDialog(this,
                "No se pudo acceder al micrófono: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void detenerGrabacion() {
        if (!recording) return;
        
        recording = false;
        timer.stop();
        
        // Detener línea de audio
        if (targetLine != null) {
            targetLine.stop();
            targetLine.close();
        }
        
        // Esperar a que termine el thread
        try {
            if (recordingThread != null) {
                recordingThread.join(1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        // Guardar audio grabado
        recordedAudio = audioOutputStream.toByteArray();
        recordingDuration = (System.currentTimeMillis() - startTime) / 1000;
        
        try {
            audioOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // Cerrar diálogo
        dispose();
    }
    
    private void cancelar() {
        if (recording) {
            recording = false;
            timer.stop();
            
            if (targetLine != null) {
                targetLine.stop();
                targetLine.close();
            }
        }
        
        cancelled = true;
        dispose();
    }
    
    private void actualizarTimer() {
        if (!recording) return;
        
        long elapsed = System.currentTimeMillis() - startTime;
        long seconds = elapsed / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        lblTimer.setText(String.format("%02d:%02d", minutes, seconds));
        
        // Limitar a 2 minutos
        if (minutes >= 2) {
            SwingUtilities.invokeLater(this::detenerGrabacion);
            JOptionPane.showMessageDialog(this,
                "Grabación detenida. Máximo 2 minutos permitidos.",
                "Límite alcanzado",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Obtener audio grabado
     */
    public byte[] getRecordedAudio() {
        return recordedAudio;
    }
    
    /**
     * Obtener duración de la grabación en segundos
     */
    public long getRecordingDuration() {
        return recordingDuration;
    }
    
    /**
     * Verificar si se canceló la grabación
     */
    public boolean isCancelled() {
        return cancelled;
    }
    
    /**
     * Obtener formato de audio
     */
    public static AudioFormat getAudioFormat() {
        return AUDIO_FORMAT;
    }
}
