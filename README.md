# MinimalistPhone

MinimalistPhone is a distraction-reducing Android launcher built with Kotlin and Jetpack Compose.
It replaces the default home experience with a minimal interface focused on intention, app limits, and mindful usage.

## Features

- Minimal home screen with time/date and a curated list of pinned apps
- Swipe-to-search app discovery flow
- App delay flow before opening selected apps
- App blocking support for strict focus periods
- Focus mode session screen
- Screen time section
- Mindfulness prompts when opening the home screen
- Grayscale mode toggle
- Notification listener integration

## Tech Stack

- Kotlin + Jetpack Compose (Material 3)
- Android Navigation Compose
- Hilt (dependency injection)
- Room (local data storage)
- WorkManager
- DataStore Preferences
- Gradle Kotlin DSL

## Requirements

- Android Studio (latest stable recommended)
- JDK 17
- Android SDK 34
- Min SDK 26

## Getting Started

1. Clone the repository.
2. Open the project in Android Studio.
3. Let Gradle sync and download dependencies.
4. Run the `app` configuration on an emulator/device.

## Use As Launcher

1. Install and open the app.
2. Go to Android settings for default apps.
3. Set MinimalistPhone as the default Home app.

## Permissions Used

- `QUERY_ALL_PACKAGES`: list installed apps for search/pinning/management
- `PACKAGE_USAGE_STATS`: usage tracking and screen-time-related behavior
- `REORDER_TASKS`: launcher task behavior
- Notification listener service permission: notification control behavior

## Project Structure

```
app/src/main/java/com/minimalistphone
  data/           # Room entities, DAO, repository implementations
  di/             # Hilt modules
  domain/         # Models, repository contracts, use cases
  presentation/   # Compose screens, view models, navigation
  services/       # Notification and background services
  ui/theme/       # App theme definitions
```

## Build

From project root:

```bash
./gradlew assembleDebug
```

On Windows PowerShell:

```powershell
.\gradlew.bat assembleDebug
```

## Status

Active prototype / early-stage implementation.
