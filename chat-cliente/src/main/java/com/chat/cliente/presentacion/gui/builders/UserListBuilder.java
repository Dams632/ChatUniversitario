package com.chat.cliente.presentacion.gui.builders;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import static com.chat.cliente.presentacion.gui.builders.SwingComponentBuilder.button;
import static com.chat.cliente.presentacion.gui.builders.SwingComponentBuilder.label;
import static com.chat.cliente.presentacion.gui.builders.SwingComponentBuilder.panel;
import com.chat.cliente.presentacion.gui.utils.FontHelper;
import com.chat.common.models.Canal;
import com.chat.common.models.Usuario;

/**
 * Builder para el panel de lista de usuarios y canales
 * Encapsula la construcción del panel izquierdo del chat
 */
public class UserListBuilder {
    
    // Componentes
    private JPanel mainPanel;
    private JList<Object> listaUsuarios;
    private DefaultListModel<Object> modeloListaUsuarios;
    private JButton btnActualizar;
    private JLabel lblUsuariosOnline;
    
    // Callbacks
    private Runnable onRefreshAction;
    private Consumer<Usuario> onUserSelectedAction;
    private Consumer<Canal> onChannelSelectedAction;
    
    // Configuración
    private String currentUsername;
    
    // Colores
    private static final Color BG_COLOR = Color.WHITE;
    private static final Color PRIMARY_COLOR = new Color(52, 152, 219);
    private static final Color TEXT_COLOR = new Color(52, 73, 94);
    private static final Color BORDER_COLOR = new Color(189, 195, 199);
    
    /**
     * Establecer usuario actual (para no seleccionarse a sí mismo)
     */
    public UserListBuilder currentUsername(String username) {
        this.currentUsername = username;
        return this;
    }
    
    /**
     * Callback cuando se presiona el botón de actualizar
     */
    public UserListBuilder onRefresh(Runnable action) {
        this.onRefreshAction = action;
        return this;
    }
    
    /**
     * Callback cuando se selecciona un usuario
     */
    public UserListBuilder onUserSelected(Consumer<Usuario> action) {
        this.onUserSelectedAction = action;
        return this;
    }
    
    /**
     * Callback cuando se selecciona un canal
     */
    public UserListBuilder onChannelSelected(Consumer<Canal> action) {
        this.onChannelSelectedAction = action;
        return this;
    }
    
    /**
     * Construir el panel completo
     */
    public UserListBuilder build() {
        mainPanel = createMainPanel();
        JPanel header = createHeader();
        JScrollPane scrollList = createUserList();
        
        mainPanel.add(header, BorderLayout.NORTH);
        mainPanel.add(scrollList, BorderLayout.CENTER);
        
        return this;
    }
    
    /**
     * Crear panel principal
     */
    private JPanel createMainPanel() {
        return panel()
            .layout(new BorderLayout(5, 5))
            .background(BG_COLOR)
            .border(10, 10, 10, 10)
            .build();
    }
    
    /**
     * Crear encabezado con contador y botón actualizar
     */
    private JPanel createHeader() {
        JPanel header = panel()
            .layout(new BorderLayout(5, 5))
            .background(BG_COLOR)
            .build();
        
        // Label de usuarios online
        lblUsuariosOnline = label("👥 Usuarios en línea (0)")
            .font(FontHelper.getBoldLabelFont(14))
            .foreground(TEXT_COLOR)
            .build();
        header.add(lblUsuariosOnline, BorderLayout.WEST);
        
        // Botón actualizar
        btnActualizar = button("🔄")
            .font("Segoe UI", Font.BOLD, 12)
            .background(PRIMARY_COLOR)
            .foreground(Color.WHITE)
            .focusPainted(false)
            .borderPainted(false)
            .cursor(Cursor.HAND_CURSOR)
            .build();
        btnActualizar.setToolTipText("Actualizar lista de usuarios");
        
        if (onRefreshAction != null) {
            btnActualizar.addActionListener(e -> onRefreshAction.run());
        }
        
        header.add(btnActualizar, BorderLayout.EAST);
        
        return header;
    }
    
    /**
     * Crear lista de usuarios con scroll
     */
    private JScrollPane createUserList() {
        modeloListaUsuarios = new DefaultListModel<>();
        listaUsuarios = new JList<>(modeloListaUsuarios);
        listaUsuarios.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        listaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listaUsuarios.setFixedCellHeight(40);
        listaUsuarios.setCellRenderer(new UsuarioListCellRenderer());
        
        // Listener para selección
        listaUsuarios.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                handleSelection();
            }
        });
        
        JScrollPane scroll = new JScrollPane(listaUsuarios);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1));
        
        return scroll;
    }
    
    /**
     * Manejar selección de usuario o canal
     */
    private void handleSelection() {
        Object seleccionado = listaUsuarios.getSelectedValue();
        
        if (seleccionado instanceof Usuario) {
            Usuario usuario = (Usuario) seleccionado;
            // No permitir chatear consigo mismo
            if (currentUsername != null && !usuario.getUsername().equals(currentUsername)) {
                if (onUserSelectedAction != null) {
                    onUserSelectedAction.accept(usuario);
                }
            }
        } else if (seleccionado instanceof Canal) {
            Canal canal = (Canal) seleccionado;
            if (onChannelSelectedAction != null) {
                onChannelSelectedAction.accept(canal);
            }
        }
    }
    
    /**
     * Renderer personalizado para la lista
     */
    private class UsuarioListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus) {
            
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            
            setFont(new Font("Segoe UI", Font.PLAIN, 13));
            setBorder(new EmptyBorder(8, 10, 8, 10));
            
            if (value instanceof Usuario) {
                Usuario usuario = (Usuario) value;
                String indicador = usuario.isEnLinea() ? "🟢" : "🔴";
                setText(indicador + " " + usuario.getUsername());
                setIcon(null);
                
                // Color de fondo
                if (isSelected) {
                    setBackground(new Color(52, 152, 219, 50));
                    setForeground(new Color(52, 73, 94));
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(52, 73, 94));
                }
                
            } else if (value instanceof Canal) {
                Canal canal = (Canal) value;
                String icono = canal.isEsPrivado() ? "�" : "�";
                setText(icono + " " + canal.getNombre());
                setIcon(null);
                
                if (isSelected) {
                    setBackground(new Color(46, 204, 113, 50));
                    setForeground(new Color(52, 73, 94));
                } else {
                    setBackground(Color.WHITE);
                    setForeground(new Color(52, 73, 94));
                }
                
            } else if (value instanceof String) {
                // Separadores o encabezados
                setText((String) value);
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                setForeground(Color.GRAY);
                setBackground(new Color(240, 240, 240));
                setEnabled(false);
            }
            
            return this;
        }
    }
    
    // Getters para componentes
    
    public JPanel getMainPanel() {
        return mainPanel;
    }
    
    public JList<Object> getListaUsuarios() {
        return listaUsuarios;
    }
    
    public DefaultListModel<Object> getModeloListaUsuarios() {
        return modeloListaUsuarios;
    }
    
    public JButton getBtnActualizar() {
        return btnActualizar;
    }
    
    public JLabel getLblUsuariosOnline() {
        return lblUsuariosOnline;
    }
}
