# ⚔️ Workout Level Up — Android App

A Solo Leveling-inspired workout tracker built with **Kotlin + Jetpack Compose**, featuring a Room database,
ViewModel, a monthly calendar, a daily exercise checklist, streaks, badges, and local workout reminders.
It is intended for individuals following the included training plan. Workout records are saved on the device.

---

## 🏗️ Architecture

```
app/
└── src/main/java/com/example/workoutlevelup/
    ├── data/
    │   ├── WorkoutDay.kt          ← Room entity
    │   ├── WorkoutDayDao.kt       ← DAO queries
    │   └── AppDatabase.kt         ← Room singleton
    ├── model/
    │   └── WorkoutPlan.kt         ← 4-level training plan + badge definitions
    ├── repository/
    │   └── WorkoutRepository.kt   ← Data access abstraction + streak logic
    ├── viewmodel/
    │   └── WorkoutViewModel.kt    ← StateFlow-based UI state + business logic
    ├── ui/
    │   ├── theme/
    │   │   ├── Color.kt
    │   │   ├── Theme.kt
    │   │   └── Type.kt
    │   ├── screens/
    │   │   ├── CalendarScreen.kt  ← Monthly calendar view
    │   │   ├── DailyWorkoutScreen.kt ← Exercise checklist + live timer
    │   │   └── BadgesScreen.kt    ← Streak / badges / level progress
    │   └── navigation/
    │       └── AppNavigation.kt   ← Bottom nav + NavHost
    ├── notifications/
    │   └── WorkoutReminderWorker.kt ← WorkManager daily notification
    ├── WorkoutApplication.kt      ← Initialises WorkManager on startup
    └── MainActivity.kt            ← Entry point
```

---

## 📋 Training Plan (4 Levels)

| Level | Rank            | Unlock condition  |
|-------|-----------------|-------------------|
| 1     | E-Rank Initiate | Start              |
| 2     | E-Rank Hunter   | 14 completed days  |
| 3     | B-Rank Hunter   | 30 completed days  |
| 4     | S-Rank Hunter ⚡ | 60 completed days  |

---

## 🏅 Badge System

| Badge          | Requirement                  |
|----------------|------------------------------|
| First Step     | Complete 1 workout           |
| Week Warrior   | Complete 7 workouts          |
| On Fire 🔥     | 3-day streak                 |
| Unstoppable 💪 | 7-day streak                 |
| Iron Will 🦾   | 30-day streak                |
| E-Rank Hunter  | Reach Level 2                |
| B-Rank Hunter  | Reach Level 3                |
| S-Rank Hunter ⚡| Reach Level 4               |
| Half Century   | Complete 50 workouts         |
| Centurion 🏆   | Complete 100 workouts        |
| Consistent     | Complete 30 workouts total   |

---

## 🔔 Notifications

The first daily reminder is scheduled for 18:00 local time through **WorkManager**.
Android can defer background work, and a repeating reminder can drift from that time.
To change the time, edit `WorkoutApplication.kt`:

```kotlin
WorkoutReminderWorker.scheduleDailyReminder(context, hourOfDay = 18, minute = 0)
```

On Android 13+ the app asks for notification permission on first launch. Workout logging works if you decline.
You can change the permission in Android Settings.

---

## 🚀 Setup

1. Open the repository root in Android Studio with support for Android Gradle Plugin 8.4.
2. Select JDK 17 as the Gradle JDK and install Android SDK Platform 34.
3. Sync Gradle and run on an emulator or device with API 24 or later.

From Windows PowerShell, run `./gradlew.bat :app:assembleDebug`. On macOS or Linux, run
`./gradlew :app:assembleDebug`. The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

The minimum SDK is 24 (Android 7.0) and the target SDK is 34.
The project uses Kotlin 1.9.24 and Compose BOM 2024.02.00.

---

## Verification

The [screen index](docs/verification/SCREEN-INDEX.md) separates emulator captures from browser screenshots.
The [public Android screen gallery](https://workout-level-up-portfolio.vercel.app/gallery.html) shows the emulator captures.
Try the [public interactive demo](https://workout-level-up-portfolio.vercel.app/). It is a browser simulation
with separate local storage and no Android notification delivery.

The [debug APK](docs/verification/downloads/workout-level-up-debug.apk) is available for evaluation.
Its [build manifest](docs/verification/downloads/build-info.json) records the source commit, byte length,
and SHA-256 digest. See the [verification results](docs/verification/RESULTS.md) for coverage and limitations.
