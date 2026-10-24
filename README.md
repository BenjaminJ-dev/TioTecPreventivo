# TíoTec Preventivo

Aplicación Android desarrollada para promover hábitos de seguridad digital mediante una evaluación preventiva y recomendaciones prácticas.

## Integrantes

- BenjaminJ-dev
- Matías Bustos

## Funcionalidades

- Evaluación de hábitos de seguridad digital con preguntas de respuesta Sí/No.
- Validación de respuestas antes de mostrar el resultado, con avisos visuales para preguntas sin contestar.
- Recomendaciones preventivas según las respuestas de la evaluación.
- Lista de acciones preventivas que se pueden marcar como completadas.
- Persistencia local del estado de las acciones mediante Room.
- Recordatorio mediante notificación.
- Captura y vista previa de una foto usando la cámara del dispositivo.
- Navegación entre las pantallas de inicio, evaluación y resultados con transiciones animadas.

## Tecnologías utilizadas

- Kotlin
- Jetpack Compose y Material 3
- ViewModel
- Room
- Coroutines
- Activity Result API para permisos, notificaciones y cámara

## Requisitos

- Android Studio
- JDK 11
- Dispositivo o emulador con Android 7.0 (API 24) o posterior

## Cómo ejecutar el proyecto

1. Clona o descarga este repositorio.
2. Abre la carpeta del proyecto en Android Studio.
3. Espera a que termine la sincronización de Gradle.
4. Conecta un dispositivo Android o inicia un emulador.
5. Ejecuta la configuración **app** desde Android Studio.

La primera vez que se prueben algunas funciones, Android puede solicitar permisos para notificaciones o cámara.

## Estructura principal

- `app/src/main/java/com/example/tiotecpreventivo/ui/screens`: pantallas de inicio, evaluación y resultados.
- `app/src/main/java/com/example/tiotecpreventivo/ui/viewmodel`: estado y lógica de la interfaz.
- `app/src/main/java/com/example/tiotecpreventivo/data`: base de datos Room, DAO, entidades y repositorio.
