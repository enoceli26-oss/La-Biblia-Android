# 📖 La Biblia (RV1909 y RV1960) — Android

Aplicación nativa para Android desarrollada en Kotlin con lectura en voz alta, concordancia bíblica instantánea, versículos favoritos, compartir citas y modo oscuro.

## ✨ Características principales
- **Versiones bíblicas incluidas (100% offline):**
  - Reina-Valera 1909 (RV1909)
  - Reina-Valera 1960 (RV1960)
- **Lectura en voz alta (Text-to-Speech):**
  - Controles de **Play / Pausa**, **Anterior** y **Siguiente**.
  - Resaltado en tiempo real y desplazamiento automático por versículo.
  - Avance continuo al siguiente capítulo.
  - Selector de voces en español, velocidad (`0.5x` a `2.0x`) y tono desde Ajustes (`⚙`).
- **Concordancia Bíblica (`🔍`):**
  - Búsqueda instantánea en los 66 libros de la Biblia activa (sin importar mayúsculas ni tildes).
  - Resaltado visual de las palabras encontradas y salto directo al versículo al tocar cualquier resultado.
- **Favoritos (`★`) y Compartir:**
  - Mantén presionado cualquier versículo para guardarlo en **Favoritos** o **compartir la cita formateada** por WhatsApp, Telegram o correo.
- **Personalización:**
  - Modo oscuro por defecto (con opción a modo claro) y tamaño de letra ajustable (`14sp` – `30sp`).

## 📲 Descarga e Instalación
1. Entra a la sección de **Releases** en este repositorio desde tu teléfono Android.
2. Despliega **Assets** y descarga el archivo **`LaBiblia_RV1909_RV1960.apk`**.
3. Abre el archivo descargado y permite la instalación desde tu navegador.

## 🛠️ Compilación desde código fuente
Requisitos: JDK 21 y Android SDK (API 34).
- Ejecuta `./gradlew assembleDebug`
- El APK generado quedará en `app/build/outputs/apk/debug/app-debug.apk`.
