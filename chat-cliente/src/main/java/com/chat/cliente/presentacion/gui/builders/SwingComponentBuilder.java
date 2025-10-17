package com.chat.cliente.presentacion.gui.builders;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.LayoutManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

/**
 * Builder para construir componentes Swing de forma fluida
 * Implementa el patrón Builder para simplificar la creación de GUIs complejas
 */
public class SwingComponentBuilder {
    
    /**
     * Builder para JLabel
     */
    public static class LabelBuilder {
        private final JLabel label;
        
        public LabelBuilder(String text) {
            this.label = new JLabel(text);
        }
        
        public LabelBuilder font(Font font) {
            label.setFont(font);
            return this;
        }
        
        public LabelBuilder font(String name, int style, int size) {
            label.setFont(new Font(name, style, size));
            return this;
        }
        
        public LabelBuilder foreground(Color color) {
            label.setForeground(color);
            return this;
        }
        
        public LabelBuilder alignmentX(float alignment) {
            label.setAlignmentX(alignment);
            return this;
        }
        
        public LabelBuilder horizontalAlignment(int alignment) {
            label.setHorizontalAlignment(alignment);
            return this;
        }
        
        public LabelBuilder preferredSize(int width, int height) {
            label.setPreferredSize(new Dimension(width, height));
            return this;
        }
        
        public LabelBuilder maxSize(int width, int height) {
            label.setMaximumSize(new Dimension(width, height));
            return this;
        }
        
        public LabelBuilder cursor(int cursorType) {
            label.setCursor(new Cursor(cursorType));
            return this;
        }
        
        public JLabel build() {
            return label;
        }
    }
    
    /**
     * Builder para JTextField
     */
    public static class TextFieldBuilder {
        private final JTextField textField;
        
        public TextFieldBuilder(int columns) {
            this.textField = new JTextField(columns);
        }
        
        public TextFieldBuilder font(String name, int style, int size) {
            textField.setFont(new Font(name, style, size));
            return this;
        }
        
        public TextFieldBuilder border(Color lineColor, int top, int left, int bottom, int right) {
            textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lineColor, 1),
                new EmptyBorder(top, left, bottom, right)
            ));
            return this;
        }
        
        public TextFieldBuilder maxSize(int width, int height) {
            textField.setMaximumSize(new Dimension(width, height));
            return this;
        }
        
        public TextFieldBuilder alignmentX(float alignment) {
            textField.setAlignmentX(alignment);
            return this;
        }
        
        public JTextField build() {
            return textField;
        }
    }
    
    /**
     * Builder para JPasswordField
     */
    public static class PasswordFieldBuilder {
        private final JPasswordField passwordField;
        
        public PasswordFieldBuilder(int columns) {
            this.passwordField = new JPasswordField(columns);
        }
        
        public PasswordFieldBuilder font(String name, int style, int size) {
            passwordField.setFont(new Font(name, style, size));
            return this;
        }
        
        public PasswordFieldBuilder border(Color lineColor, int top, int left, int bottom, int right) {
            passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lineColor, 1),
                new EmptyBorder(top, left, bottom, right)
            ));
            return this;
        }
        
        public PasswordFieldBuilder maxSize(int width, int height) {
            passwordField.setMaximumSize(new Dimension(width, height));
            return this;
        }
        
        public PasswordFieldBuilder alignmentX(float alignment) {
            passwordField.setAlignmentX(alignment);
            return this;
        }
        
        public JPasswordField build() {
            return passwordField;
        }
    }
    
    /**
     * Builder para JButton
     */
    public static class ButtonBuilder {
        private final JButton button;
        private Color originalColor;
        
        public ButtonBuilder(String text) {
            this.button = new JButton(text);
        }
        
        public ButtonBuilder font(String name, int style, int size) {
            button.setFont(new Font(name, style, size));
            return this;
        }
        
        public ButtonBuilder background(Color color) {
            button.setBackground(color);
            this.originalColor = color;
            return this;
        }
        
        public ButtonBuilder foreground(Color color) {
            button.setForeground(color);
            return this;
        }
        
        public ButtonBuilder focusPainted(boolean painted) {
            button.setFocusPainted(painted);
            return this;
        }
        
        public ButtonBuilder borderPainted(boolean painted) {
            button.setBorderPainted(painted);
            return this;
        }
        
        public ButtonBuilder maxSize(int width, int height) {
            button.setMaximumSize(new Dimension(width, height));
            return this;
        }
        
        public ButtonBuilder alignmentX(float alignment) {
            button.setAlignmentX(alignment);
            return this;
        }
        
        public ButtonBuilder cursor(int cursorType) {
            button.setCursor(new Cursor(cursorType));
            return this;
        }
        
        public ButtonBuilder hoverEffect() {
            if (originalColor != null) {
                button.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        button.setBackground(originalColor.darker());
                    }
                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        button.setBackground(originalColor);
                    }
                });
            }
            return this;
        }
        
        public JButton build() {
            return button;
        }
    }
    
    /**
     * Builder para JPanel
     */
    public static class PanelBuilder {
        private final JPanel panel;
        
        public PanelBuilder() {
            this.panel = new JPanel();
        }
        
        public PanelBuilder layout(LayoutManager layout) {
            panel.setLayout(layout);
            return this;
        }
        
        public PanelBuilder background(Color color) {
            panel.setBackground(color);
            return this;
        }
        
        public PanelBuilder border(int top, int left, int bottom, int right) {
            panel.setBorder(new EmptyBorder(top, left, bottom, right));
            return this;
        }
        
        public PanelBuilder compoundBorder(Color lineColor, int lineWidth, int top, int left, int bottom, int right) {
            panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(lineColor, lineWidth),
                new EmptyBorder(top, left, bottom, right)
            ));
            return this;
        }
        
        public PanelBuilder preferredSize(int width, int height) {
            panel.setPreferredSize(new Dimension(width, height));
            return this;
        }
        
        public PanelBuilder alignmentX(float alignment) {
            panel.setAlignmentX(alignment);
            return this;
        }
        
        public JPanel build() {
            return panel;
        }
    }
    
    /**
     * Métodos factory estáticos para iniciar builders
     */
    public static LabelBuilder label(String text) {
        return new LabelBuilder(text);
    }
    
    public static TextFieldBuilder textField(int columns) {
        return new TextFieldBuilder(columns);
    }
    
    public static PasswordFieldBuilder passwordField(int columns) {
        return new PasswordFieldBuilder(columns);
    }
    
    public static ButtonBuilder button(String text) {
        return new ButtonBuilder(text);
    }
    
    public static PanelBuilder panel() {
        return new PanelBuilder();
    }
}
