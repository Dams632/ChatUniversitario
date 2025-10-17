# Módulo de Transcripción de Audio

Este módulo utiliza **Vosk** (https://alphacephei.com/vosk/) para transcribir audio a texto en español.

## 📥 Descargar Modelo de Vosk

**IMPORTANTE**: Debes descargar el modelo de lenguaje español antes de usar este módulo.

### Pasos:

1. **Descarga el modelo**:
   - URL: https://alphacephei.com/vosk/models
   - Modelo recomendado: **vosk-model-small-es-0.42** (~40 MB)
   - Link directo: https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip

2. **Descomprime el archivo**:
   ```
   vosk-model-small-es-0.42.zip
   ```

3. **Coloca la carpeta descomprimida en**:
   ```
   chat-transcripcion/src/main/resources/models/vosk-model-small-es-0.42/
   ```

4. **Estructura final**:
   ```
   chat-transcripcion/
   └── src/
       └── main/
           └── resources/
               └── models/
                   └── vosk-model-small-es-0.42/
                       ├── am/
                       ├── conf/
                       ├── graph/
                       └── ...
   ```

## 🧪 Probar el Módulo

### Opción 1: Compilar y ejecutar test
```bash
cd chat-transcripcion
mvn clean compile
mvn exec:java -Dexec.mainClass="com.chat.transcripcion.TestTranscripcion"
```

### Opción 2: Crear JAR y ejecutar
```bash
cd ChatUniversitario
mvn clean package -pl chat-transcripcion
java -jar chat-transcripcion/target/chat-transcripcion-1.0.0-jar-with-dependencies.jar
```

## 📝 Usar en el Proyecto

### Agregar dependencia en servidor

En `chat-servidor/pom.xml`:

```xml
<dependency>
    <groupId>com.chat</groupId>
    <artifactId>chat-transcripcion</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Código de ejemplo

```java
// Inicializar (una sola vez al iniciar servidor)
ServicioTranscripcion servicio = ServicioTranscripcion.obtenerInstancia();
servicio.inicializar("ruta/al/modelo/vosk-model-small-es-0.42");

// Transcribir audio
byte[] audioWAV = ...; // bytes del archivo WAV
String texto = servicio.transcribir(audioWAV);
System.out.println("Transcripción: " + texto);

// Cerrar al terminar
servicio.cerrar();
```

## ⚙️ Requisitos del Audio

- **Formato**: WAV
- **Sample Rate**: 16000 Hz (se convierte automáticamente si es diferente)
- **Canales**: Mono (se convierte automáticamente si es estéreo)
- **Bits**: 16 bits

El servicio convierte automáticamente otros formatos WAV a los requisitos de Vosk.

## 🔍 Alternativas de Modelos

| Modelo | Tamaño | Precisión | Velocidad |
|--------|--------|-----------|-----------|
| vosk-model-small-es-0.42 | 40 MB | Media | Rápida |
| vosk-model-es-0.42 | 1.4 GB | Alta | Media |

Para este proyecto universitario, el modelo **small** es suficiente.

## 🐛 Troubleshooting

### Error: "No se encuentra el modelo"
- Verifica que descargaste y descomprimiste el modelo
- Asegúrate de que la ruta sea correcta
- Revisa que existan los subdirectorios `am/`, `conf/`, `graph/`

### Error: "UnsatisfiedLinkError"
- Las librerías nativas de Vosk no son compatibles con tu sistema
- Vosk soporta: Windows (x64), Linux (x64), macOS (x64, ARM)

### Audio sin transcripción
- El audio debe contener voz clara
- Evita ruido de fondo excesivo
- El modelo está entrenado en español

## 📚 Referencias

- Documentación Vosk: https://alphacephei.com/vosk/
- Repositorio GitHub: https://github.com/alphacep/vosk-api
- Modelos disponibles: https://alphacephei.com/vosk/models
