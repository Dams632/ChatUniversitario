# 💬 Chat Universitario

Sistema de chat cliente-servidor desarrollado en Java con arquitectura en capas, GUI Swing, base de datos MySQL/H2, y transcripción de audio mediante Vosk.

**Tecnologías:** Java 11+ | Maven 3.8+ | MySQL 8.0.33+ | H2 Database | Vosk

---

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación](#-instalación)
- [Configuración](#️-configuración)
- [Ejecución](#-ejecución)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Arquitectura](#️-arquitectura)
- [Gestión de Componentes](#-gestión-de-componentes)
- [Uso](#-uso)
- [Troubleshooting](#-troubleshooting)

---

## ✨ Características

### 🎯 Funcionalidades Principales

- ✅ **Chat en tiempo real** - Mensajes privados y grupales
- ✅ **Canales/Grupos** - Creación y gestión de canales públicos y privados
- ✅ **Sistema de invitaciones** - Invita usuarios a canales privados
- ✅ **Mensajes de voz** - Envío de audio con transcripción automática
- ✅ **Transcripción offline** - Vosk modelo español sin conexión a internet
- ✅ **Autenticación segura** - Registro e inicio de sesión con hash SHA-256
- ✅ **Persistencia dual** - MySQL (servidor) + H2 (cliente local)
- ✅ **Informes y estadísticas** - Panel de administración del servidor
- ✅ **Exportación de informes** - Generación de reportes TXT descargables

### 🏗️ Arquitectura Técnica

- **Patrón Observer** - Comunicación event-driven entre componentes
- **MVC + 3 Capas** - Presentación → Negocio → Datos
- **Multi-threading** - Manejo concurrente de múltiples clientes
- **Thread-safe sockets** - Single reader pattern con `BlockingQueue`
- **Connection Pooling** - HikariCP para optimización de conexiones
- **Inyección de dependencias** - Patrón constructor injection en DAOs

---

## 📦 Requisitos Previos

### Software Obligatorio

| Software | Versión Mínima | Enlace de Descarga |
|----------|----------------|-------------------|
| **Java JDK** | 11+ | [Oracle JDK 11](https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html) o [OpenJDK 11](https://adoptium.net/) |
| **Maven** | 3.8+ | [Maven Download](https://maven.apache.org/download.cgi) |
| **MySQL** | 8.0.33+ | [MySQL Community Server](https://dev.mysql.com/downloads/mysql/) |

### Verificar Instalación

```powershell
# Verificar Java
java -version
# Salida esperada: java version "11.x.x"

# Verificar Maven
mvn -version
# Salida esperada: Apache Maven 3.8.x

# Verificar MySQL
mysql --version
# Salida esperada: mysql  Ver 8.0.33

# Verificar Git
git --version
# Salida esperada: git version 2.x.x
```

### Recursos del Sistema
- **Disco**: 500 MB libres
- **Puertos**: 5000 (servidor), 3306 (MySQL)

---

## 🚀 Instalación

### 1. Preparar el Proyecto

Descomprime el proyecto en la ubicación deseada, por ejemplo:
```powershell
C:\Users\USER\Documents\ChatUniversitario
```

### 2. Configurar MySQL

#### Opción A: Instalación Local (Recomendada)

1. **Instalar MySQL Server**
   - Descargar de [MySQL Community Downloads](https://dev.mysql.com/downloads/mysql/)
   - Instalador: `mysql-installer-community-8.0.33.0.msi`
   - Durante instalación:
     - Tipo: **Developer Default**
     - Contraseña root: Anotar para configuración posterior

2. **Verificar servicio MySQL**
   ```powershell
   # Windows
   Get-Service MySQL80
   
   # Si no está corriendo
   Start-Service MySQL80
   ```

#### Opción B: Usar XAMPP/WAMP

Si prefieres usar XAMPP o WAMP:
- Iniciar Apache y MySQL desde el panel de control
- Puerto por defecto: 3306
- Usuario: `root`, Contraseña: (vacío o la que configuraste)

### 3. Inicializar Base de Datos

El proyecto incluye un script de inicialización automática:

```powershell
# Windows
.\inicializar-db.bat

# Alternativa manual
mysql -u root -p < database/init_mysql.sql
```

**Script ejecuta:**
- Crea base de datos `chat_universitario`
- Crea tablas: `usuarios`, `canales`, `miembros_canal`, `invitaciones_canal`, `mensajes`, `archivos_audio`, `logs_mensajes`
- Inserta usuarios de prueba (admin, juan, maria, carlos)

**Usuarios de Prueba:**

| Username | Password |
|----------|----------|
| admin | admin123 |



### 4. Compilar el Proyecto

```powershell
# Compilar todos los módulos
mvn clean compile

# Generar JARs ejecutables
mvn clean compile assembly:single -pl chat-servidor,chat-cliente

# Salida:
# ✅ chat-servidor/target/chat-servidor-1.0.0-jar-with-dependencies.jar
# ✅ chat-cliente/target/chat-cliente-1.0.0-jar-with-dependencies.jar
```

**Tiempos estimados:**
- Primera compilación: 3-5 minutos (descarga dependencias)
- Compilaciones subsecuentes: 30-60 segundos

---

## ⚙️ Configuración

### Configuración del Servidor

Editar `chat-servidor/src/main/resources/config.properties`:

```properties
# Configuración del Servidor MySQL
jdbc.url=jdbc:mysql://localhost:3306/chat_universitario
jdbc.username=root
jdbc.password=TU_CONTRASEÑA_MYSQL
jdbc.driver=com.mysql.cj.jdbc.Driver

# Pool de conexiones HikariCP
jdbc.pool.min=5
jdbc.pool.max=10

# Puerto del servidor TCP
server.port=5000

# Restricción de usuarios conectados
server.max.usuarios.conectados=100

# Configuración de logs
log.nivel=INFO
```

**⚠️ IMPORTANTE:** Cambiar `jdbc.password` con tu contraseña de MySQL.

### Configuración del Cliente

Editar `chat-cliente/src/main/resources/config.properties`:

```properties
# Servidor
server.host=localhost
server.port=5000

# Base de datos H2 embebida (se crea automáticamente)
h2.db.path=resources/database/chat_cliente_db
h2.db.user=sa
h2.db.password=

# Configuración de logs
log.retention.days=30
```

**Nota:** Cada cliente crea su propia base de datos H2 en:
```
~/.chat-universitario/database/cliente_<timestamp>.mv.db
```

### Configuración de Puertos

Si el puerto 5000 está en uso (ej: Docker Desktop):

1. **Cambiar en servidor:**
   ```properties
   # config.properties
   server.port=5001
   ```

2. **Cambiar en cliente:**
   ```properties
   # config.properties
   server.port=5001
   ```

3. **Recompilar:**
   ```powershell
   mvn clean compile assembly:single -pl chat-servidor,chat-cliente
   ```

---

## ▶️ Ejecución

### Opción 1: Scripts Batch (Recomendado - Windows)

#### 1. Iniciar Servidor

```powershell
.\run-servidor.bat
```

**Se abrirá:**
- Consola con logs del servidor
- GUI del servidor con:
  - Panel de conexiones activas (IP, usuario, hora)
  - Botón "📊 Informes del Servidor"

**Verificar:**
```
🚀 Servidor iniciado en el puerto 5000
Esperando conexiones...
```

#### 2. Iniciar Cliente(s)

```powershell
# Cliente único
.\run-cliente.bat


**Se abrirá:**
- GUI de inicio de sesión
- Opciones: Iniciar Sesión | Registrarse

### Opción 2: Ejecución Manual (Multiplataforma)

#### Servidor

```powershell
# Windows
java -jar chat-servidor\target\chat-servidor-1.0.0-jar-with-dependencies.jar

# Linux/Mac
java -jar chat-servidor/target/chat-servidor-1.0.0-jar-with-dependencies.jar
```

#### Cliente

```powershell
# Windows
java -jar chat-cliente\target\chat-cliente-1.0.0-jar-with-dependencies.jar

# Linux/Mac
java -jar chat-cliente/target/chat-cliente-1.0.0-jar-with-dependencies.jar
```



## 📁 Estructura del Proyecto

```
ChatUniversitario/
│
├── pom.xml                          # POM padre multi-módulo
├── README.md                        # Este archivo
│
├── 📜 Scripts de Ejecución
├── run-servidor.bat                 # Inicia servidor (Windows)
├── run-cliente.bat                  # Inicia cliente (Windows)
├── run-multiples-clientes.bat       # Múltiples clientes (testing)
├── inicializar-db.bat               # Inicializa base de datos MySQL
├── descargar-modelo-vosk.bat        # Descarga modelo Vosk español
│
├── 📂 database/
│   └── init_mysql.sql               # Script SQL de inicialización
│
├── 📦 chat-common/                  # Módulo compartido
│   ├── pom.xml
│   └── src/main/java/com/chat/common/
│       ├── dto/                     # Data Transfer Objects
│       │   ├── RequestDTO.java
│       │   ├── ResponseDTO.java
│       │   └── MensajeEntranteDTO.java
│       ├── models/                  # Modelos de dominio
│       │   ├── Usuario.java
│       │   ├── Canal.java
│       │   ├── Mensaje.java
│       │   └── ArchivoAudio.java
│       └── patterns/                # Patrones de diseño
│           ├── Observer.java
│           ├── Observable.java
│           ├── EventoChat.java
│           └── GestorEventos.java (Singleton)
│
├── 📦 chat-transcripcion/           # Módulo de transcripción Vosk
│   ├── pom.xml
│   └── src/main/java/com/chat/transcripcion/
│       ├── ServicioTranscripcion.java
│       └── resources/
│           └── vosk-model-small-es-0.42/  # Modelo Vosk español
│
├── 📦 chat-servidor/                # Aplicación servidor
│   ├── pom.xml
│   └── src/main/java/com/chat/servidor/
│       ├── ServidorChat.java        # Main + Singleton
│       ├── negocio/                 # Capa de negocio
│       │   ├── ServicioUsuario.java
│       │   ├── ServicioCanal.java
│       │   ├── ServicioMensaje.java
│       │   ├── ServicioAudio.java
│       │   ├── ServicioAutenticacion.java
│       │   ├── ServicioInformesLogs.java
│       │   └── ServicioEstadisticas.java
│       ├── datos/                   # Capa de datos (DAOs)
│       │   ├── ConexionDB.java
│       │   ├── UsuarioDAO.java
│       │   ├── CanalDAO.java
│       │   ├── MensajeDAO.java
│       │   ├── AudioDAO.java
│       │   └── LogMensajeDAO.java
│       ├── presentacion/
│       │   ├── ManejadorCliente.java  # Thread por cliente
│       │   └── gui/
│       │       ├── ServidorFrame.java
│       │       └── InformesFrame.java
│       └── resources/
│           └── config.properties
│
└── 📦 chat-cliente/                 # Aplicación cliente
    ├── pom.xml
    └── src/main/java/com/chat/cliente/
        ├── Main.java
        ├── negocio/
        │   └── ClienteRed.java      # Thread-safe socket
        ├── datos/
        │   ├── ConexionH2.java
        │   └── LogMensajeDAO.java
        ├── presentacion/
        │   └── gui/
        │       ├── LoginFrame.java
        │       ├── RegistroFrame.java
        │       ├── ChatFrame.java
        │       ├── CrearCanalDialog.java
        │       ├── InvitarUsuariosDialog.java
        │       ├── GrabadorAudio.java
        │       └── ReproductorAudio.java
        └── resources/
            └── config.properties
```

---

## 🏛️ Arquitectura

### Patrón de Arquitectura

```
┌─────────────────────────────────────────────────────────────┐
│                    ARQUITECTURA MVC + 3 CAPAS               │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  📱 PRESENTACIÓN (GUI)                                      │
│     ├─ Swing Frames/Dialogs                                │
│     ├─ Event Listeners                                      │
│     └─ Observer Pattern (actualizar())                      │
│                          ↕️                                  │
│  💼 NEGOCIO (Servicios)                                     │
│     ├─ Lógica de negocio                                    │
│     ├─ Validaciones                                         │
│     ├─ Transformaciones DTO ↔ Model                        │
│     └─ Coordinación de DAOs                                 │
│                          ↕️                                  │
│  💾 DATOS (DAOs)                                            │
│     ├─ Connection injection                                 │
│     ├─ SQL queries                                          │
│     ├─ CRUD operations                                      │
│     └─ Transacciones                                        │
│                          ↕️                                  │
│  🗄️  BASE DE DATOS                                          │
│     ├─ MySQL (servidor - compartida)                        │
│     └─ H2 (cliente - local embebida)                        │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

### Patrones de Diseño Implementados

#### 1. Observer Pattern (Event-Driven)

```java
// GestorEventos (Singleton)
GestorEventos.obtenerInstancia().publicarEvento(
    TipoEvento.MENSAJE_RECIBIDO, 
    mensajeDTO
);

// ChatFrame implementa Observer
@Override
public void actualizar(EventoChat evento) {
    if (evento.getTipo() == TipoEvento.MENSAJE_RECIBIDO) {
        // Actualizar GUI
    }
}
```

#### 2. Singleton Pattern

- `ServidorChat.getInstance()`
- `GestorEventos.obtenerInstancia()`

#### 3. DAO Pattern + Dependency Injection

```java
// Servicio instancia DAO con Connection inyectada
public class ServicioUsuario {
    private Connection conexion;
    
    public ServicioUsuario(Connection conexion) {
        this.conexion = conexion;
    }
    
    public Usuario obtenerPorId(int id) {
        UsuarioDAO dao = new UsuarioDAO(conexion);
        return dao.obtenerPorId(id);
    }
}
```

#### 4. Thread-Safe Socket Communication

```java
// Single reader thread pattern
private final BlockingQueue<ResponseDTO> colaRespuestas = new LinkedBlockingQueue<>();

// Solo UN thread lee del socket
private void iniciarEscucha() {
    new Thread(() -> {
        while (conectado) {
            ResponseDTO response = (ResponseDTO) entrada.readObject();
            if (response.getTipo() == TipoResponse.NOTIFICACION) {
                // Publicar evento
                GestorEventos.obtenerInstancia().publicarEvento(...);
            } else {
                // Poner en cola para thread que envió request
                colaRespuestas.put(response);
            }
        }
    }).start();
}

// Enviar request y esperar respuesta
public ResponseDTO enviarRequest(RequestDTO request) {
    synchronized (salida) {
        salida.writeObject(request);
    }
    return colaRespuestas.poll(10, TimeUnit.SECONDS);
}
```

---

## � Gestión de Componentes

### Arquitectura Modular

El proyecto está dividido en **4 componentes independientes**, cada uno generando su propio JAR:

| Componente | Tipo | Propósito |
|------------|------|-----------|
| **chat-common** | Librería | DTOs, modelos, patrones compartidos |
| **chat-transcripcion** | Servicio | Transcripción de audio con Vosk |
| **chat-servidor** | Ejecutable | Servidor TCP + MySQL |
| **chat-cliente** | Ejecutable | Cliente GUI + H2 |

### Compilar Todos los Componentes

```powershell
# Script automatizado (recomendado)
.\build-all-components.bat

# O con Maven directamente
mvn clean package -DskipTests
```

**JARs generados:**
```
chat-common/target/chat-common-1.0.0.jar
chat-transcripcion/target/chat-transcripcion-1.0.0-jar-with-dependencies.jar
chat-servidor/target/chat-servidor.jar
chat-cliente/target/chat-cliente.jar
```

### Compilar Un Componente Específico

```powershell
# Usando script
.\build-component.bat servidor
.\build-component.bat cliente
.\build-component.bat transcripcion
.\build-component.bat common

# O con Maven
mvn clean package -pl chat-servidor -DskipTests
mvn clean package -pl chat-cliente -DskipTests
```

### Ventajas del Diseño Modular

- ✅ **Independencia**: Cada componente se desarrolla por separado
- ✅ **Reutilización**: `chat-common` compartido entre cliente/servidor
- ✅ **Despliegue flexible**: Actualizar solo el JAR necesario
- ✅ **Testing aislado**: Probar cada componente independientemente
- ✅ **Bajo acoplamiento**: Cambios no afectan otros módulos

📚 **Documentación completa:** Ver [COMPONENTES.md](COMPONENTES.md)

---

## �📖 Uso

### Flujo Básico de Uso

#### 1. Registro de Usuario

1. Ejecutar cliente
2. Click en **"Registrarse"**
3. Completar formulario:
   - Username (único)
   - Email
   - Contraseña
4. Click **"Registrar"**

#### 2. Inicio de Sesión

1. Ingresar username y contraseña
2. Click **"Iniciar Sesión"**
3. Se abre `ChatFrame` principal

#### 3. Enviar Mensaje Privado

1. En la lista de usuarios, seleccionar destinatario
2. Escribir mensaje en área de texto inferior
3. Click **"Enviar"** o presionar Enter

#### 4. Crear Canal

1. Click botón **"Crear Canal"**
2. Completar formulario:
   - Nombre del canal
   - Tipo: Público / Privado
3. Click **"Crear"**

#### 5. Unirse a Canal

1. En lista de canales, doble click en canal público
2. Para privados: Necesitas invitación

#### 6. Enviar Audio

1. Click botón **"🎤 Grabar Audio"**
2. Grabar mensaje (máx 30 segundos)
3. Click **"Enviar"**
4. El servidor transcribe automáticamente con Vosk

#### 7. Ver Informes (Administrador)

1. En el servidor, click **"📊 Informes del Servidor"**
2. Navegar entre pestañas:
   - Usuarios Registrados
   - Canales y Usuarios
   - Usuarios Conectados
   - Audios
   - Logs de Mensajes
   - Logs del Sistema
3. Click **"📥 Exportar Informe"** para descargar TXT

---

## 🐛 Troubleshooting

### Problema: "Address already in use: NET_Bind"

**Causa:** Puerto 5000 ya está en uso (probablemente Docker Desktop).

**Solución 1 - Cambiar puerto:**
```properties
# chat-servidor/src/main/resources/config.properties
server.port=5001

# chat-cliente/src/main/resources/config.properties
server.port=5001
```
Recompilar: `mvn clean compile assembly:single -pl chat-servidor,chat-cliente`

**Solución 2 - Cerrar proceso:**
```powershell
# Identificar proceso
netstat -ano | findstr :5000

# Cerrar Docker Desktop o el proceso identificado
taskkill /PID <PID> /F
```

---

### Problema: "Cannot connect to database"

**Causa:** MySQL no está corriendo o credenciales incorrectas.

**Solución:**
```powershell
# Verificar servicio MySQL
Get-Service MySQL80

# Iniciar si está detenido
Start-Service MySQL80

# Verificar conexión
mysql -u root -p
# Ingresar contraseña

# Verificar que base de datos existe
SHOW DATABASES;
# Debe aparecer: chat_universitario
```

Si la base de datos no existe:
```powershell
.\inicializar-db.bat
```

---

### Problema: "ClassNotFoundException: com.mysql.cj.jdbc.Driver"

**Causa:** Dependencia MySQL no se descargó correctamente.

**Solución:**
```powershell
# Limpiar y recompilar
mvn clean install

# Forzar re-descarga de dependencias
mvn clean install -U
```

---

### Problema: Modelo Vosk no encontrado

**Causa:** Modelo de transcripción no descargado.

**Solución:**
```powershell
# Descargar modelo
.\descargar-modelo-vosk.bat

# Verificar que existe:
dir chat-transcripcion\src\main\resources\vosk-model-small-es-0.42
```

---

### Problema: GUI no se muestra (pantalla en blanco)

**Causa:** Problema de threading en Swing.

**Solución:**
Verificar que las actualizaciones de GUI se hacen en EDT:
```java
SwingUtilities.invokeLater(() -> {
    // Actualizar componentes Swing aquí
});
```

---

### Problema: "java.io.InternalError" al leer socket

**Causa:** Múltiples threads leyendo de `ObjectInputStream` simultáneamente.

**Solución:**
El proyecto ya implementa **single reader pattern**. Si modificas código de red, asegúrate de mantener un solo thread leyendo del socket.

---

### Problema: Clientes no pueden conectarse

**Verificaciones:**

1. **Servidor corriendo:**
   ```powershell
   netstat -ano | findstr :5000
   # Debe mostrar LISTENING
   ```

2. **Firewall:**
   - Windows Defender Firewall puede bloquear conexiones
   - Permitir Java en configuración de firewall

3. **Puerto correcto:**
   - Verificar que cliente y servidor usan mismo puerto

4. **Logs del servidor:**
   ```
   ✅ Cliente conectado desde /127.0.0.1:XXXXX
   ```

---

## 📚 Documentación Adicional

### Arquitectura y Diseño

- **[COMPONENTES.md](COMPONENTES.md)** - Guía completa de arquitectura modular
  - 4 componentes independientes (common, transcripcion, servidor, cliente)
  - Scripts de compilación por componente
  - Grafo de dependencias Maven
  - Comandos de compilación y ejecución
  - Versionado y distribución

- **[ARQUITECTURA_COMPONENTES.md](ARQUITECTURA_COMPONENTES.md)** - Diagramas técnicos
  - Flujo de compilación Maven Reactor
  - Árbol de dependencias detallado
  - Distribución de artefactos (JARs)
  - Escenarios de actualización
  - Troubleshooting de compilación

- **[MEJORES_PRACTICAS_COMPONENTES.md](MEJORES_PRACTICAS_COMPONENTES.md)** - Guía de desarrollo
  - Desarrollo por componentes (workflow recomendado)
  - Versionado semántico (SemVer)
  - Testing modular (Unit, Integration, UI)
  - Estrategias de despliegue (Dev, Producción, Alta Disponibilidad)
  - Mantenimiento y actualización sin downtime
  - Antipatrones a evitar

- **[SOLID_ANALYSIS.md](SOLID_ANALYSIS.md)** - Análisis de principios SOLID
  - Aplicación de SRP, OCP, LSP, ISP, DIP
  - Refactorización realizada (Validators, IServicioCliente)
  - Ejemplos de código antes/después
  - Métricas de mejora

### Scripts de Gestión

- **`build-all-components.bat`** - Compilar todos los módulos
- **`build-component.bat [componente]`** - Compilar módulo específico
- **`list-components.bat`** - Inventario de JARs compilados
- **`run-servidor.bat`** - Ejecutar servidor
- **`run-cliente.bat`** - Ejecutar cliente
- **`inicializar-db.bat`** - Inicializar MySQL

### Comandos Rápidos

```powershell
# Compilar todo el proyecto
.\build-all-components.bat

# Compilar solo servidor
.\build-component.bat servidor

# Compilar solo cliente
.\build-component.bat cliente

# Ver estado de componentes
.\list-components.bat

# Ejecutar sistema completo
.\inicializar-db.bat
.\run-servidor.bat
.\run-cliente.bat
```

---

