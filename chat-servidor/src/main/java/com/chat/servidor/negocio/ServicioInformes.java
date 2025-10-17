package com.chat.servidor.negocio;

import com.chat.common.models.ArchivoAudio;
import com.chat.common.models.Canal;
import com.chat.common.models.Mensaje;
import com.chat.common.models.Usuario;
import com.chat.servidor.datos.CanalDAO;
import com.chat.servidor.datos.UsuarioDAO;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para generar informes del servidor
 */
public class ServicioInformes {
    
    private final UsuarioDAO usuarioDAO;
    private final CanalDAO canalDAO;
    private final ServicioMensajeria servicioMensajeria;
    
    public ServicioInformes(Connection conexion) {
        this.usuarioDAO = new UsuarioDAO(conexion);
        this.canalDAO = new CanalDAO(conexion);
        this.servicioMensajeria = new ServicioMensajeria(conexion);
    }
    
    /**
     * Informe de usuarios registrados
     */
    public Map<String, Object> generarInformeUsuariosRegistrados() throws SQLException {
        Map<String, Object> informe = new HashMap<>();
        
        // Obtener todos los usuarios (simulado, necesitarías implementar este método en UsuarioDAO)
        List<Usuario> usuariosOnline = usuarioDAO.obtenerUsuariosEnLinea();
        
        informe.put("totalUsuariosOnline", usuariosOnline.size());
        informe.put("usuarios", usuariosOnline);
        informe.put("fecha", java.time.LocalDateTime.now());
        
        return informe;
    }
    
    /**
     * Informe de canales con usuarios vinculados
     */
    public Map<String, Object> generarInformeCanalesConUsuarios() throws SQLException {
        Map<String, Object> informe = new HashMap<>();
        
        List<Canal> canales = canalDAO.obtenerCanalesActivos();
        
        informe.put("totalCanales", canales.size());
        informe.put("canales", canales);
        
        // Calcular estadísticas
        int totalMiembros = 0;
        int canalesPrivados = 0;
        int canalesPublicos = 0;
        
        for (Canal canal : canales) {
            totalMiembros += canal.getMiembrosIds().size();
            if (canal.isEsPrivado()) {
                canalesPrivados++;
            } else {
                canalesPublicos++;
            }
        }
        
        informe.put("totalMiembros", totalMiembros);
        informe.put("canalesPrivados", canalesPrivados);
        informe.put("canalesPublicos", canalesPublicos);
        informe.put("fecha", java.time.LocalDateTime.now());
        
        return informe;
    }
    
    /**
     * Informe de usuarios conectados
     */
    public Map<String, Object> generarInformeUsuariosConectados() throws SQLException {
        Map<String, Object> informe = new HashMap<>();
        
        List<Usuario> usuariosConectados = usuarioDAO.obtenerUsuariosEnLinea();
        
        informe.put("totalConectados", usuariosConectados.size());
        informe.put("usuariosConectados", usuariosConectados);
        informe.put("fecha", java.time.LocalDateTime.now());
        
        return informe;
    }
    
    /**
     * Informe de mensajes de audio con texto
     */
    public Map<String, Object> generarInformeMensajesAudio() throws SQLException {
        Map<String, Object> informe = new HashMap<>();
        
        List<ArchivoAudio> archivos = servicioMensajeria.obtenerArchivosAudioConTexto();
        
        informe.put("totalArchivos", archivos.size());
        informe.put("archivos", archivos);
        informe.put("fecha", java.time.LocalDateTime.now());
        
        return informe;
    }
    
    /**
     * Informe de logs de mensajes
     */
    public Map<String, Object> generarInformeLogs(int limite) throws SQLException {
        Map<String, Object> informe = new HashMap<>();
        
        List<Mensaje> logs = servicioMensajeria.obtenerLogs(limite);
        int totalMensajes = servicioMensajeria.contarMensajes();
        int totalAudios = servicioMensajeria.contarArchivosAudio();
        
        informe.put("totalMensajes", totalMensajes);
        informe.put("totalAudios", totalAudios);
        informe.put("ultimosLogs", logs);
        informe.put("fecha", java.time.LocalDateTime.now());
        
        return informe;
    }
}
