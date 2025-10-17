package com.chat.common.utils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad para formateo de fechas y horas
 * Centraliza la lógica de formato para mantener consistencia
 */
public class FechaHoraUtil {
    
    // Formatters reutilizables (thread-safe)
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    
    /**
     * Formatear hora actual en formato HH:mm:ss
     * @return String con la hora formateada (ej: "14:30:45")
     */
    public static String formatearHoraActual() {
        return LocalTime.now().format(TIME_FORMATTER);
    }
    
    /**
     * Formatear LocalTime en formato HH:mm:ss
     * @param time Objeto LocalTime a formatear
     * @return String con la hora formateada
     */
    public static String formatearHora(LocalTime time) {
        return time.format(TIME_FORMATTER);
    }
    
    /**
     * Formatear timestamp de SQL a String de hora
     * @param timestamp Timestamp de SQL
     * @return String con la hora formateada
     */
    public static String formatearHoraDesdeTimestamp(Timestamp timestamp) {
        return timestamp.toLocalDateTime().toLocalTime().format(TIME_FORMATTER);
    }
    
    /**
     * Formatear timestamp de millis a String de hora
     * @param millis Milisegundos desde epoch
     * @return String con la hora formateada
     */
    public static String formatearHoraDesdeMillis(long millis) {
        Timestamp timestamp = new Timestamp(millis);
        return formatearHoraDesdeTimestamp(timestamp);
    }
    
    /**
     * Formatear fecha y hora completa
     * @param dateTime Objeto LocalDateTime
     * @return String con fecha y hora formateadas (ej: "16/10/2025 14:30:45")
     */
    public static String formatearFechaHora(LocalDateTime dateTime) {
        return dateTime.format(DATE_TIME_FORMATTER);
    }
    
    /**
     * Formatear solo fecha
     * @param dateTime Objeto LocalDateTime
     * @return String con la fecha formateada (ej: "16/10/2025")
     */
    public static String formatearFecha(LocalDateTime dateTime) {
        return dateTime.format(DATE_FORMATTER);
    }
    
    /**
     * Obtener timestamp actual
     * @return Timestamp de SQL con la hora actual
     */
    public static Timestamp obtenerTimestampActual() {
        return new Timestamp(System.currentTimeMillis());
    }
}
