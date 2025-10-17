package com.chat.cliente.negocio;

import java.util.List;

import com.chat.common.dto.ResponseDTO;
import com.chat.common.models.Usuario;

/**
 * Interfaz para el servicio de cliente (Dependency Inversion Principle)
 * Permite inyectar implementaciones alternativas y facilita el testing
 */
public interface IServicioCliente {
    
    /**
     * Conectar al servidor
     */
    void conectar() throws Exception;
    
    /**
     * Login de usuario
     */
    ResponseDTO login(String username, String password);
    
    /**
     * Registrar usuario
     */
    ResponseDTO registrar(String username, String email, String password, String direccionIP);
    
    /**
     * Registrar usuario con foto
     */
    ResponseDTO registrar(String username, String email, String password, String direccionIP, byte[] foto);
    
    /**
     * Desconectar del servidor
     */
    void desconectar();
    
    /**
     * Obtener usuario actual
     */
    Usuario getUsuarioActual();
    
    /**
     * Obtener lista de usuarios conectados
     */
    List<Usuario> getUsuariosConectados();
    
    /**
     * Enviar mensaje a usuario
     */
    ResponseDTO enviarMensaje(String destinatario, String contenido);
    
    /**
     * Crear grupo
     */
    ResponseDTO crearGrupo(String nombreGrupo, List<String> miembros);
    
    /**
     * Obtener configuración
     */
    String getConfiguracion(String clave);
    
    /**
     * Verificar si está conectado
     */
    boolean isConectado();
}
