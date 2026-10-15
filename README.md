# clockdin
> Mood journaling where each day is a painted orb in a 3D galaxy.

## Build Requirements

- Android Studio Ladybug (or newer)
- JDK 17
- Gradle 8.11 (wrapper included)
- minSdk 34, targetSdk 35

## Phase Build Status

| Phase | Status | Contents |
|-------|--------|----------|
| 1 | ✅ Done | Scaffold, DB, Repos, DataStore, Theme, Nav skeleton, Unit tests |
| 2 | ⏳ Next | Galaxy 3D engine, AGSL shaders, gestures |
| 3 | ⏳ | Orb painting screen |
| 4 | ⏳ | Save flow, Detail modal, photo handling |
| 5 | ⏳ | List, Calendar, Ribbon views |
| 6 | ⏳ | Settings, reminders, backup |

---

## Google Drive Backup — Setup Required

The `GoogleDriveAppDataBackupProvider` is implemented in Phase 6. Before it
will compile and run, you **must** perform the following one-time setup in
Google Cloud Console:

```
1. Create a Google Cloud project at console.cloud.google.com
2. Enable the "Google Drive API" for the project.
3. Go to APIs & Services → OAuth consent screen:
   - Choose "External" (or Internal for personal use)
   - Fill in app name "clockdin", support email, developer email
   - Add scope: https://www.googleapis.com/auth/drive.appdata
4. Go to APIs & Services → Credentials → Create Credentials → OAuth 2.0 Client ID:
   - Application type: Android
   - Package name: com.vk.clockdin
   - SHA-1 signing certificate: run the command below and paste the SHA-1

   keytool -list -v \
     -keystore ~/.android/debug.keystore \
     -alias androiddebugkey \
     -storepass android \
     -keypass android

5. Download the client ID string (looks like: XXXXXX.apps.googleusercontent.com)
6. Add it to app/src/main/res/values/strings.xml:

   <string name="google_oauth_client_id">PASTE_YOUR_CLIENT_ID_HERE</string>

7. Also add it to your google-services.json if using Firebase; otherwise the
   string resource alone is sufficient for the Credential Manager flow.
```

> **If you skip these steps**, the Drive backup feature will be disabled
> gracefully — the toggle will show a "Not configured" message and no crash
> will occur. All other features work completely offline.

---

## Architecture

```
app/
 ├── ui/             Compose screens, ViewModels, theme, navigation
 ├── domain/         Models, repository interfaces, use cases
 │   ├── model/
 │   ├── repository/
 │   └── usecase/
 └── data/           Room, DataStore, file I/O, backup providers
     ├── db/
     ├── datastore/
     └── repository/
```

MVVM + Clean Architecture. ViewModels expose `StateFlow<UiState>` and
one-off events via `Channel`. All screens are stateless composables.

## Design Language

- Background: `#090A0F` → `#000000` vertical gradient
- Only mood orb colors are saturated; all UI chrome is neutral gray/white
- Glassmorphic cards: translucent white (~5% alpha) + hairline border
- AGSL `RuntimeShader` for orb glow (API 33+)
- Spring physics for all interactions (`DampingRatioMediumBouncy`)