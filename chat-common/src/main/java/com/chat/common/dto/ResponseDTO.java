package com.chat.common.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * DTO para respuestas del servidor al cliente
 */
public class ResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private boolean exito;
    private String mensaje;
    private Map<String, Object> datos;
    private CodigoEstado codigo;
    
    public enum CodigoEstado {
        OK(200),
        CREADO(201),
        ERROR_CLIENTE(400),
        NO_AUTORIZADO(401),
        NO_ENCONTRADO(404),
        ERROR_SERVIDOR(500);
        
        private final int valor;
        
        CodigoEstado(int valor) {
            this.valor = valor;
        }
        
        public int getValor() {
            return valor;
        }
    }
    
    public ResponseDTO() {
        this.datos = new HashMap<>();
    }
    
    public ResponseDTO(boolean exito, String mensaje, CodigoEstado codigo) {
        this();
        this.exito = exito;
        this.mensaje = mensaje;
        this.codigo = codigo;
    }
    
    public static ResponseDTO exitoso(String mensaje) {
        return new ResponseDTO(true, mensaje, CodigoEstado.OK);
    }
    
    public static ResponseDTO error(String mensaje) {
        return new ResponseDTO(false, mensaje, CodigoEstado.ERROR_SERVIDOR);
    }
    
    // Getters y Setters
    public boolean isExito() {
        return exito;
    }
    
    public void setExito(boolean exito) {
        this.exito = exito;
    }
    
    public String getMensaje() {
        return mensaje;
    }
    
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
    
    public Map<String, Object> getDatos() {
        return datos;
    }
    
    public void setDatos(Map<String, Object> datos) {
        this.datos = datos;
    }
    
    public void addDato(String clave, Object valor) {
        this.datos.put(clave, valor);
    }
    
    public Object getDato(String clave) {
        return this.datos.get(clave);
    }
    
    public CodigoEstado getCodigo() {
        return codigo;
    }
    
    public void setCodigo(CodigoEstado codigo) {
        this.codigo = codigo;
    }
    
    @Override
    public String toString() {
        return "ResponseDTO{" +
                "exito=" + exito +
                ", mensaje='" + mensaje + '\'' +
                ", codigo=" + codigo +
                '}';
    }
}
