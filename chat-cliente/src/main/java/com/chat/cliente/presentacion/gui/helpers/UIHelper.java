package com.chat.cliente.presentacion.gui.helpers;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.ImageIcon;

/**
 * Utilidades para crear elementos de UI (iconos, avatares, etc.)
 */
public class UIHelper {
    
    /**
     * Crear icono circular de foto
     */
    public static ImageIcon crearIconoFoto(byte[] fotoBytes, int width, int height) {
        if (fotoBytes == null || fotoBytes.length == 0) {
            return crearIconoAvatarPorDefecto(width, height);
        }
        
        try {
            // Convertir bytes a imagen
            BufferedImage img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(fotoBytes));
            if (img == null) {
                return crearIconoAvatarPorDefecto(width, height);
            }
            
            // Escalar imagen
            Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            
            // Crear imagen circular
            BufferedImage circularImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            java.awt.Graphics2D g2 = circularImage.createGraphics();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Crear clip circular
            g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, width, height));
            g2.drawImage(scaledImg, 0, 0, null);
            g2.dispose();
            
            return new ImageIcon(circularImage);
            
        } catch (Exception e) {
            return crearIconoAvatarPorDefecto(width, height);
        }
    }
    
    /**
     * Crear icono de avatar por defecto
     */
    public static ImageIcon crearIconoAvatarPorDefecto(int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        java.awt.Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING, java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Fondo circular gris
        g2.setColor(new Color(189, 195, 199));
        g2.fillOval(0, 0, width, height);
        
        // Icono de usuario (silueta simple)
        g2.setColor(Color.WHITE);
        int centerX = width / 2;
        int centerY = height / 2;
        
        // Cabeza (círculo)
        g2.fillOval(centerX - width / 6, centerY - height / 4, width / 3, height / 3);
        
        // Cuerpo (arco)
        g2.fillArc(centerX - width / 3, centerY, width * 2 / 3, height / 2, 0, 180);
        
        g2.dispose();
        return new ImageIcon(img);
    }
}
