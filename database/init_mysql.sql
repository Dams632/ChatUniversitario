# Script SQL para inicializar la base de datos MySQL del servidor

-- Crear base de datos
CREATE DATABASE IF NOT EXISTS chat_universitario;
USE chat_universitario;

-- Tabla de usuarios
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    foto MEDIUMBLOB,
    direccion_ip VARCHAR(45),
    en_linea BOOLEAN DEFAULT FALSE,
    fecha_registro DATETIME NOT NULL,
    ultima_conexion DATETIME,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_ip (direccion_ip)
);

-- Tabla de grupos
CREATE TABLE IF NOT EXISTS grupos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    creador_id BIGINT NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (creador_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_nombre (nombre)
);

-- Tabla de miembros de grupos
CREATE TABLE IF NOT EXISTS grupo_miembros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    grupo_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_union DATETIME NOT NULL,
    FOREIGN KEY (grupo_id) REFERENCES grupos(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE KEY unique_grupo_usuario (grupo_id, usuario_id),
    INDEX idx_grupo (grupo_id),
    INDEX idx_usuario (usuario_id)
);

-- Tabla de mensajes
CREATE TABLE IF NOT EXISTS mensajes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    remitente_id BIGINT NOT NULL,
    destinatario_id BIGINT,
    grupo_id BIGINT,
    contenido TEXT NOT NULL,
    fecha_envio DATETIME NOT NULL,
    leido BOOLEAN DEFAULT FALSE,
    tipo_mensaje VARCHAR(20) NOT NULL,
    FOREIGN KEY (remitente_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (destinatario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (grupo_id) REFERENCES grupos(id) ON DELETE CASCADE,
    INDEX idx_remitente (remitente_id),
    INDEX idx_destinatario (destinatario_id),
    INDEX idx_grupo (grupo_id),
    INDEX idx_fecha (fecha_envio)
);

-- Tabla de sesiones
CREATE TABLE IF NOT EXISTS sesiones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    token VARCHAR(255) UNIQUE NOT NULL,
    fecha_creacion DATETIME NOT NULL,
    fecha_expiracion DATETIME,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_token (token)
);

-- Tabla de canales
CREATE TABLE IF NOT EXISTS canales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    foto MEDIUMBLOB,
    creador_id BIGINT NOT NULL,
    es_privado BOOLEAN DEFAULT FALSE,
    fecha_creacion DATETIME NOT NULL,
    activo BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (creador_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_nombre (nombre),
    INDEX idx_privado (es_privado)
);

-- Tabla de miembros de canales
CREATE TABLE IF NOT EXISTS canal_miembros (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    canal_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    fecha_union DATETIME NOT NULL,
    FOREIGN KEY (canal_id) REFERENCES canales(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    UNIQUE KEY unique_canal_usuario (canal_id, usuario_id),
    INDEX idx_canal (canal_id),
    INDEX idx_usuario (usuario_id)
);

-- Tabla de solicitudes de canal
CREATE TABLE IF NOT EXISTS solicitudes_canal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    canal_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    usuario_username VARCHAR(50) NOT NULL,
    canal_nombre VARCHAR(100) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_solicitud DATETIME NOT NULL,
    fecha_respuesta DATETIME,
    mensaje_respuesta TEXT,
    FOREIGN KEY (canal_id) REFERENCES canales(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_estado (estado),
    INDEX idx_canal (canal_id),
    INDEX idx_usuario (usuario_id)
);

-- Tabla de invitaciones a canal
CREATE TABLE IF NOT EXISTS invitaciones_canal (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    canal_id BIGINT NOT NULL,
    usuario_invitado_id BIGINT NOT NULL,
    usuario_invitador_id BIGINT NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_invitacion DATETIME NOT NULL,
    fecha_respuesta DATETIME,
    FOREIGN KEY (canal_id) REFERENCES canales(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_invitado_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (usuario_invitador_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_estado (estado),
    INDEX idx_canal (canal_id),
    INDEX idx_usuario_invitado (usuario_invitado_id),
    INDEX idx_usuario_invitador (usuario_invitador_id)
);

-- Tabla de archivos de audio
CREATE TABLE IF NOT EXISTS archivos_audio (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    mensaje_id BIGINT,
    remitente_id BIGINT,
    destinatario_id BIGINT,
    canal_id BIGINT,
    nombre_archivo VARCHAR(255) NOT NULL,
    contenido_audio LONGBLOB NOT NULL,
    duracion_segundos BIGINT DEFAULT 0,
    formato VARCHAR(10) NOT NULL,
    tamano_bytes BIGINT NOT NULL,
    texto_transcrito TEXT,
    fecha_envio DATETIME NOT NULL,
    FOREIGN KEY (mensaje_id) REFERENCES mensajes(id) ON DELETE CASCADE,
    FOREIGN KEY (remitente_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (destinatario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (canal_id) REFERENCES canales(id) ON DELETE SET NULL,
    INDEX idx_mensaje (mensaje_id),
    INDEX idx_remitente (remitente_id),
    INDEX idx_destinatario (destinatario_id),
    INDEX idx_canal (canal_id),
    INDEX idx_fecha (fecha_envio)
);

-- Tabla de logs de mensajes (para auditoría)
CREATE TABLE IF NOT EXISTS logs_mensajes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tipo_mensaje VARCHAR(20) NOT NULL, -- 'TEXTO', 'AUDIO'
    tipo_conversacion VARCHAR(20) NOT NULL, -- 'PRIVADO', 'GRUPO'
    remitente_username VARCHAR(50) NOT NULL,
    remitente_id BIGINT,
    destinatario_username VARCHAR(50),
    destinatario_id BIGINT,
    canal_nombre VARCHAR(100),
    canal_id BIGINT,
    contenido_texto TEXT,
    contenido_audio LONGBLOB,
    transcripcion_audio TEXT,
    duracion_segundos BIGINT,
    formato_audio VARCHAR(10),
    timestamp DATETIME NOT NULL,
    direccion_ip_remitente VARCHAR(45),
    FOREIGN KEY (remitente_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (destinatario_id) REFERENCES usuarios(id) ON DELETE SET NULL,
    FOREIGN KEY (canal_id) REFERENCES canales(id) ON DELETE SET NULL,
    INDEX idx_tipo_mensaje (tipo_mensaje),
    INDEX idx_tipo_conversacion (tipo_conversacion),
    INDEX idx_remitente (remitente_username),
    INDEX idx_destinatario (destinatario_username),
    INDEX idx_canal (canal_nombre),
    INDEX idx_timestamp (timestamp),
    FULLTEXT INDEX idx_contenido (contenido_texto, transcripcion_audio)
);

-- Insertar usuarios de prueba (opcional)
-- INSERT INTO usuarios (username, email, password, fecha_registro) 
-- VALUES ('admin', 'admin@universidad.edu', SHA2('admin123', 256), NOW());
