# clockdin
> Mood journaling where each day's mood is a painted orb in a 3D galaxy.

## Overview
**clockdin** is an offline-first Android app built with Kotlin, Jetpack Compose, Material 3, and Room. Each day's emotional state is expressed through a custom painted or radial-gradient orb, rendered into a persistent 3D galaxy particle field organized via a deterministic Fibonacci spiral.

## Build Requirements
- Android Studio Ladybug (or newer)
- JDK 17
- Gradle 8.10+ (wrapper included)
- minSdk 34 (Android 14+), compileSdk / targetSdk 35

## Architecture & Tech Stack
- **UI:** Jetpack Compose, Material 3, Single-Activity, Edge-to-Edge enforced.
- **Design System:** Ultra-premium dark theme (`#090A0F` → `#000000`), glassmorphic cards, saturated mood colors, spring physics for all interactions.
- **Database:** Room (SQLite) with KSP, kotlinx.serialization TypeConverters.
- **DI:** Dagger Hilt with KSP.
- **Storage:** Offline-first in `context.filesDir` (orbs in `filesDir/orbs/`, downscaled photos in `filesDir/photos/`).
- **Preferences:** Jetpack DataStore Preferences.
- **Background Tasks:** WorkManager for daily reminders.
- **Image Loading:** Coil Compose.

```
app/src/main/java/com/vk/clockdin/
 ├── data/
 │   ├── local/          Room Database, DAOs, Entities, Converters, DefaultPalette
 │   │   ├── converter/
 │   │   ├── dao/
 │   │   └── entity/
 │   └── repository/     Repository implementations (Room & DataStore)
 ├── di/                 Hilt Dependency Injection modules
 ├── domain/
 │   ├── math/           3D Fibonacci sphere & perspective projection
 │   ├── model/          Domain models (UserSettings, etc.)
 │   ├── repository/     Domain repository interfaces
 │   └── usecase/        Domain use cases (CalculateStreakUseCase, etc.)
 └── ui/
     ├── navigation/     Navigation routes & NavHost
     ├── splash/         Intro animated splash screen
     └── theme/          Color, Theme, Type, Glassmorphism, Spring Motion
```

## Phase Status

| Phase | Description | Status |
|---|---|---|
| **Phase 1** | Project scaffold, Hilt, Room, DataStore, Theme, Navigation skeleton, Unit tests | ✅ Completed |
| **Phase 2** | Galaxy 3D engine, stars/dust/beads, AGSL glow shader, gestures, Galaxy Home | ✅ Completed |
| **Phase 3** | Orb painting screen (brush + radial gradient builder), palette selector | ⏳ Up Next |
| **Phase 4** | Save flow sheet, Bézier trajectory animation, Detail modal, photo viewer | ⏳ Upcoming |
| **Phase 5** | Alternative perspectives (Journal list, Calendar month grid, Ribbon timeline) | ⏳ Upcoming |
| **Phase 6** | Palette editor, WorkManager reminder, offline zip backup/restore, Drive provider | ⏳ Upcoming |

---

## Google Drive Backup — Setup Guide (Phase 6)
The Google Drive backup provider uses Credential Manager and the Drive REST API (`drive.appdata` scope). To enable Drive backup in Phase 6:
1. Create a Google Cloud project in the [Google Cloud Console](https://console.cloud.google.com).
2. Enable the **Google Drive API**.
3. Configure the **OAuth Consent Screen** (scope `https://www.googleapis.com/auth/drive.appdata`).
4. Create an **Android OAuth 2.0 Client ID** with package name `com.vk.clockdin` and your debug/release keystore SHA-1.
5. Provide the client ID in `strings.xml` or runtime configuration.
*(If unconfigured, the feature safely disables itself without crashing, keeping the app 100% functional offline.)*
