# Anika AI — Voice Assistant Android App

An advanced native Android AI Voice Assistant powered by Jetpack Compose, Kotlin Coroutines, Material Design 3, and generative AI with comprehensive device action execution.

---

## 🌟 Key Features

- **Interactive Cyber-HUD Visualizer**: Real-time multi-layered animated glowing orb responding dynamically to Voice Listening, Speaking, Thinking, and Command Execution states.
- **Natural Voice Interaction**: Native speech recognition with full-duplex Android Text-to-Speech (TTS) integration and automatic maximum volume audio boost.
- **Customizable AI Engine & BYOK**:
  - Direct support for user-configured API keys (Gemini, OpenAI-compatible models, or custom inference endpoints).
  - Built-in connectivity testing with real-time latency and status feedback.
  - Persona customization with specialized modes (Standard, Analytical, Concise, Developer).
- **Device Action Automations**: Native intent handlers for flashlight control, volume manipulation, application launching (YouTube, WhatsApp, Maps), alarms, dialing, and settings.
- **Local Persistence**: Room database for conversation histories and local preference storage.

---

## 🚀 GitHub Actions — Automated APK Generation

This repository includes a complete CI/CD workflow located at `.github/workflows/android.yml`.

### How It Works:
1. **Push to `main` / `master`**: Automatically builds the Android APK and uploads it to GitHub Actions artifacts under **`Android-APK`**.
2. **Push a Release Tag (e.g. `v1.0.0`)**: Automatically builds the APK, generates release notes, and attaches the APK directly to the GitHub Release.
3. **Manual Trigger (`workflow_dispatch`)**: Run the workflow on-demand via the Actions tab in GitHub.

### Adding Gemini API Key as a Secret (Optional):
If you wish to pre-bake or supply your default Gemini API key during the CI build:
1. Go to your GitHub repository -> **Settings** -> **Secrets and variables** -> **Actions**.
2. Add a new secret named `GEMINI_API_KEY` with your Google AI Studio API key.
*(Note: Users can also enter or update their API key at any time within the app's Settings screen!)*

---

## 🛠 Local Build Instructions

### Prerequisites
- **JDK 21** (Eclipse Temurin recommended)
- **Android SDK** (API 34/36)
- **Gradle 9.3+** (or use the included `./gradlew`)

### Build Steps:
```bash
# 1. Clone the repository
git clone <your-repo-url>
cd <repo-folder>

# 2. Setup environment file
cp .env.example .env

# 3. Build Debug APK
./gradlew assembleDebug

# 4. Locate APK
# The APK will be generated at:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 📱 Architecture & Tech Stack

- **UI & Design**: Jetpack Compose, Material 3, Canvas HUD animations
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Async & Reactive**: Kotlin Coroutines & Flow (`StateFlow`, `collectAsStateWithLifecycle`)
- **Voice Pipeline**: Android `SpeechRecognizer` + `TextToSpeech`
- **Networking**: Retrofit 2 + Moshi + OkHttp
- **Local Database**: Room DB (SQLite) + AndroidX DataStore / SharedPreferences
- **Min SDK**: 24 (Android 7.0+)
- **Target SDK**: 36 (Android 16+)
