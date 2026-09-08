# SynC - Campus Synchronization Platform

SynC is a modern Android application designed to centralize campus communication and synchronization. It provides a unified platform for students, faculty, and administrators to interact through announcements, polls, resource sharing, and lost-and-found tracking.

## 🚀 Key Features

- **Authentication & Security**: Secure registration and login flows with JWT-based session management stored in `EncryptedSharedPreferences`.
- **Role-Based Access**: Specialized UI and capabilities for `admin` vs `student` roles.
- **Campus Feed**: Real-time announcements and events with category filtering.
- **Interactive Polls**: Dynamic voting system with real-time result visualization.
- **Learning Materials**: Peer-to-peer repository for sharing course files with multipart upload and integrated download support.
- **Lost & Found**: Community-driven database for reporting and tracking lost or found items.
- **Profile Management**: Customizable user profiles with real-time avatar updates.

## 🛠 Tech Stack

- **UI Framework**: Jetpack Compose with Material 3 (Glassmorphic design).
- **Architecture**: MVVM (Model-ViewModel-View).
- **Networking**: Retrofit & OkHttp with custom Interceptors for Auth.
- **Persistence**: EncryptedSharedPreferences for secure local storage.
- **Reactive Streams**: Kotlin Coroutines & Flow for state management.
- **Image Loading**: Coil for efficient image fetching and caching.
- **Dependency Management**: Gradle Version Catalog (`libs.versions.toml`).

## ⚙️ Development Setup

### Current API Configuration
The app is currently configured to connect to a backend via an Ngrok tunnel:
- **Base URL**: `https://vitality-rewind-vehicular.ngrok-free.dev/`

### Prerequisites
1. **Android Studio**: Ladybug or later.
2. **Backend**: Node.js/Go backend with SQLite (running on `localhost:8080`).
3. **Tunneling**: Ngrok running on your computer to bridge local development.

### How to Run
1. Clone the repository.
2. Open in Android Studio.
3. Ensure the backend is active on your host machine.
4. Ensure Ngrok is running: `ngrok http 8080`.
5. Sync Gradle and run on a physical device or emulator.

## 📝 Backend Integration
The app expects a specific REST API contract. For detailed specifications on endpoints, multipart handling, and attribution logic, refer to the `backend_integration_spec.artifact.md` in the artifacts directory.

## 🔐 Admin Testing
For administrative testing, use:
- **Username**: `admin`
- **Password**: `admin`
*(Note: Ensure these exist in your local backend database)*

---
Developed as part of the SynC Campus ecosystem.
