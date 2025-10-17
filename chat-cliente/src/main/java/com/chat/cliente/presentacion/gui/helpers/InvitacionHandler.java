package com.chat.cliente.presentacion.gui.helpers;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import com.chat.cliente.negocio.ServicioCliente;
import com.chat.common.dto.ResponseDTO;
import com.chat.common.models.Invitacion;

/**
 * Maneja la lógica de invitaciones a grupos
 */
public class InvitacionHandler {
    
    private final ServicioCliente servicioCliente;
    private final Runnable actualizarListaCallback;
    
    public InvitacionHandler(ServicioCliente servicioCliente, Runnable actualizarListaCallback) {
        this.servicioCliente = servicioCliente;
        this.actualizarListaCallback = actualizarListaCallback;
    }
    
    /**
     * Mostrar diálogo con invitaciones pendientes
     */
    public void mostrarDialogoInvitaciones(List<Invitacion> invitaciones, JFrame parent) {
        JFrame dialogoInvitaciones = new JFrame("📬 Invitaciones Pendientes");
        dialogoInvitaciones.setSize(500, 600);
        dialogoInvitaciones.setLocationRelativeTo(parent);
        dialogoInvitaciones.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        JPanel contenedor = new JPanel();
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setBackground(Color.WHITE);
        contenedor.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        for (Invitacion invitacion : invitaciones) {
            JPanel panelInvitacion = crearPanelInvitacion(invitacion, dialogoInvitaciones);
            contenedor.add(panelInvitacion);
            contenedor.add(javax.swing.Box.createVerticalStrut(10));
        }
        
        JScrollPane scrollPane = new JScrollPane(contenedor);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null);
        
        dialogoInvitaciones.add(scrollPane);
        dialogoInvitaciones.setVisible(true);
    }
    
    /**
     * Crear panel para una invitación individual
     */
    private JPanel crearPanelInvitacion(Invitacion invitacion, JFrame dialogo) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(189, 195, 199), 1),
            new EmptyBorder(15, 15, 15, 15)
        ));
        
        // Crear icono de foto del canal
        ImageIcon fotoIcon = UIHelper.crearIconoFoto(invitacion.getFotoCanal(), 60, 60);
        JLabel lblFoto = new JLabel(fotoIcon);
        panel.add(lblFoto, BorderLayout.WEST);
        
        // Panel de información
        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS));
        panelInfo.setBackground(Color.WHITE);
        
        JLabel lblNombreCanal = new JLabel("👥 " + invitacion.getNombreCanal());
        lblNombreCanal.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNombreCanal.setForeground(new Color(52, 73, 94));
        
        JLabel lblInvitador = new JLabel("Invitado por: " + invitacion.getUsernameInvitador());
        lblInvitador.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblInvitador.setForeground(new Color(127, 140, 141));
        
        JLabel lblDescripcion = new JLabel("<html><i>" + 
            (invitacion.getDescripcionCanal() != null ? invitacion.getDescripcionCanal() : "Sin descripción") + 
            "</i></html>");
        lblDescripcion.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblDescripcion.setForeground(new Color(149, 165, 166));
        
        panelInfo.add(lblNombreCanal);
        panelInfo.add(javax.swing.Box.createVerticalStrut(5));
        panelInfo.add(lblInvitador);
        panelInfo.add(javax.swing.Box.createVerticalStrut(5));
        panelInfo.add(lblDescripcion);
        
        panel.add(panelInfo, BorderLayout.CENTER);
        
        // Panel de botones
        JPanel panelBotones = new JPanel();
        panelBotones.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        panelBotones.setBackground(Color.WHITE);
        
        JButton btnAceptar = new JButton("✓ Aceptar");
        btnAceptar.setBackground(new Color(39, 174, 96));
        btnAceptar.setForeground(Color.WHITE);
        btnAceptar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAceptar.setFocusPainted(false);
        btnAceptar.setBorderPainted(false);
        btnAceptar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAceptar.addActionListener(e -> aceptarInvitacion(invitacion, dialogo));
        
        JButton btnRechazar = new JButton("✗ Rechazar");
        btnRechazar.setBackground(new Color(231, 76, 60));
        btnRechazar.setForeground(Color.WHITE);
        btnRechazar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnRechazar.setFocusPainted(false);
        btnRechazar.setBorderPainted(false);
        btnRechazar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnRechazar.addActionListener(e -> rechazarInvitacion(invitacion, dialogo));
        
        panelBotones.add(btnAceptar);
        panelBotones.add(btnRechazar);
        
        panel.add(panelBotones, BorderLayout.SOUTH);
        
        return panel;
    }
    
    /**
     * Aceptar una invitación
     */
    private void aceptarInvitacion(Invitacion invitacion, JFrame dialogo) {
        new Thread(() -> {
            try {
                ResponseDTO response = servicioCliente.aceptarInvitacion(invitacion.getId(), invitacion.getCanalId());
                
                SwingUtilities.invokeLater(() -> {
                    if (response.isExito()) {
                        JOptionPane.showMessageDialog(dialogo,
                            "¡Te has unido al grupo " + invitacion.getNombreCanal() + "!",
                            "Invitación Aceptada",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        dialogo.dispose();
                        
                        // Llamar callback para recargar lista
                        if (actualizarListaCallback != null) {
                            actualizarListaCallback.run();
                        }
                    } else {
                        JOptionPane.showMessageDialog(dialogo,
                            "Error al aceptar invitación: " + response.getMensaje(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialogo,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
    
    /**
     * Rechazar una invitación
     */
    private void rechazarInvitacion(Invitacion invitacion, JFrame dialogo) {
        new Thread(() -> {
            try {
                ResponseDTO response = servicioCliente.rechazarInvitacion(invitacion.getId());
                
                SwingUtilities.invokeLater(() -> {
                    if (response.isExito()) {
                        JOptionPane.showMessageDialog(dialogo,
                            "Invitación rechazada",
                            "Invitación Rechazada",
                            JOptionPane.INFORMATION_MESSAGE);
                        
                        dialogo.dispose();
                    } else {
                        JOptionPane.showMessageDialog(dialogo,
                            "Error al rechazar invitación: " + response.getMensaje(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(dialogo,
                        "Error: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
            }
        }).start();
    }
}
