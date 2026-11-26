# 📖 La Biblia (RV1909 y RV1960) — Android

Aplicación nativa para Android desarrollada en Kotlin con lectura en voz alta, concordancia bíblica instantánea, versículos favoritos y modo oscuro.

## ✨ Características principales
- **Versiones bíblicas incluidas (100% offline):**
  - Reina-Valera 1909 (RV1909)
  - Reina-Valera 1960 (RV1960)
- **Lectura en voz alta (Text-to-Speech):**
  - Controles de **Play / Pausa**, **Anterior** y **Siguiente**.
  - Resaltado en tiempo real y desplazamiento automático (*auto-scroll*) por versículo.
  - Avance continuo al siguiente capítulo.
  - Selector de voces en español, velocidad (`0.5x` a `2.0x`) y tono desde Ajustes (`⚙`).
- **Concordancia Bíblica (`🔍`):** Búsqueda rápida en toda la Biblia ignorando mayúsculas y tildes.
- **Favoritos (`★`) y Compartir:** Mantén presionado cualquier versículo para guardarlo en favoritos o compartir la cita formateada.
- **Personalización:** Modo oscuro por defecto (con opción a modo claro) y tamaño de letra ajustable (`14sp` – `30sp`).

## 📲 Descarga e Instalación
1. Ve a la sección de [**Releases**](https://github.com) desde tu teléfono Android.
2. Descarga el archivo **`LaBiblia_RV1909_RV1960.apk`**.
3. Abre el archivo descargado y permite la instalación desde tu navegador.

## 🛠️ Compilación desde código fuente
Requisitos: JDK 21 y Android SDK (API 34).
```bash
./gradlew assembleDebug
```
El APK generado quedará en `app/build/outputs/apk/debug/app-debug.apk`.
