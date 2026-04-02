# 📱 HabitPal — Habit Tracker Android App

> A production-grade Android habit tracking app built with Kotlin, Clean Architecture, and modern Jetpack libraries. Track daily habits, visualize streaks, and build consistency.

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9%2B-7F52FF?logo=kotlin)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2024%2B-3DDC84?logo=android)](https://developer.android.com/)
[![Hilt](https://img.shields.io/badge/DI-Hilt-orange)](https://dagger.dev/hilt/)
[![Room](https://img.shields.io/badge/DB-Room-blue)](https://developer.android.com/training/data-storage/room)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-brightgreen)](https://developer.android.com/topic/architecture)

---

## 📸 Screenshots

| Home Screen | Habit Detail | Progress Charts |
|-------------|-------------|-----------------|
| _Coming soon_ | _Coming soon_ | _Coming soon_ |

---

## ✨ Features

- ✅ **Create & manage habits** — add, edit, and delete daily habits
- 📊 **Progress visualization** — charts powered by MPAndroidChart showing streaks and completion rates
- 🔔 **Streak tracking** — stay consistent with visual habit streaks
- 💾 **Offline-first** — all data stored locally with Room database, no internet required
- 🎨 **Material Design UI** — clean, modern interface following Material 3 guidelines
- 🌙 **Splash screen** — smooth app launch with AndroidX SplashScreen
- 📍 **Location support** — location-aware features via Google Play Services
- 🖼️ **Image loading** — efficient image handling with Coil

---

## 🏗️ Architecture

HabitPal follows **Clean Architecture** with **MVVM** pattern, separating the codebase into three distinct layers:

```
app/
└── src/main/java/com/example/habitpal/
    ├── data/           ← Room DB, Retrofit API, Repositories (impl)
    ├── domain/         ← Use Cases, Repository interfaces, Models
    ├── presentation/   ← Fragments, ViewModels, Adapters, UI logic
    ├── di/             ← Hilt dependency injection modules
    ├── util/           ← Extension functions, helpers
    ├── HabitPalApplication.kt
    └── MainActivity.kt
```

### Architecture Flow

```
UI (Fragment/Activity)
        ↓ observes
   ViewModel (LiveData)
        ↓ calls
    Use Cases (Domain)
        ↓ calls
   Repository Interface
        ↓ implemented by
  Repository Impl (Data)
        ↓ reads/writes
   Room Database / Retrofit API
```

---

## 🛠️ Tech Stack

| Category | Library | Purpose |
|----------|---------|---------|
| **Language** | Kotlin | Primary language |
| **Architecture** | MVVM + Clean Architecture | Separation of concerns |
| **DI** | Hilt | Dependency injection |
| **Database** | Room + KSP | Local persistence |
| **Networking** | Retrofit + OkHttp + Moshi | API calls and JSON parsing |
| **Navigation** | Navigation Component + SafeArgs | Fragment navigation |
| **Async** | Kotlin Coroutines | Background operations |
| **Lifecycle** | ViewModel + LiveData + Lifecycle | Reactive UI updates |
| **Charts** | MPAndroidChart | Habit progress visualization |
| **Images** | Coil | Efficient image loading |
| **Storage** | DataStore | Preferences and settings |
| **UI** | ViewBinding + RecyclerView + ConstraintLayout | Modern view layer |
| **Location** | Google Play Services | Location-based features |
| **Splash** | AndroidX SplashScreen | Launch experience |
| **Testing** | JUnit + Espresso | Unit and UI tests |

---

## 📁 Project Structure

```
HabitPal-App/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/habitpal/
│   │   │   │   ├── data/           ← DB entities, DAOs, API service, repo impl
│   │   │   │   ├── domain/         ← Use cases, models, repository contracts
│   │   │   │   ├── presentation/   ← UI: fragments, ViewModels, adapters
│   │   │   │   ├── di/             ← Hilt modules (DB, network, repo bindings)
│   │   │   │   ├── util/           ← Helpers and extension functions
│   │   │   │   ├── HabitPalApplication.kt
│   │   │   │   └── MainActivity.kt
│   │   │   ├── res/                ← Layouts, drawables, strings, navigation graph
│   │   │   └── AndroidManifest.xml
│   │   └── test/                   ← Unit tests
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
└── settings.gradle.kts
```

---

## ⚡ Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- JDK 17
- Android SDK API 24+
- An Android device or emulator (API 24+)

### Step 1 — Clone the repository

```bash
git clone https://github.com/kunal9812/HabitPal-App.git
cd HabitPal-App
```

### Step 2 — Open in Android Studio

Open Android Studio → **File → Open** → select the `HabitPal-App` folder.

Wait for Gradle to sync automatically.

### Step 3 — Build & Run

Click the **▶ Run** button or use:

```bash
./gradlew assembleDebug
```

The app will launch on your connected device or emulator.

---

## 🔑 Key Implementation Highlights

### Dependency Injection with Hilt
The entire app uses Hilt for DI — ViewModels, repositories, database, and network clients are all injected, making the codebase testable and modular.

### Room Database
Habits and their completion records are persisted locally using Room with KSP code generation. Coroutines Flows are used for reactive data updates.

### Retrofit + Moshi Networking
Retrofit handles any remote API calls with Moshi for type-safe JSON parsing, and OkHttp for request logging and interceptors.

### Navigation Component
Fragment navigation is handled entirely through the Navigation Component with SafeArgs for type-safe argument passing between screens.

### MPAndroidChart
Habit streaks and completion rates are visualized through interactive charts, giving users clear visual feedback on their consistency.

### DataStore Preferences
User preferences and app settings are stored using Jetpack DataStore (replacing SharedPreferences) for asynchronous, coroutine-friendly access.

---

## 🗺️ Roadmap

- [ ] Reminder notifications (WorkManager + AlarmManager)
- [ ] Widget support for home screen habit check-ins
- [ ] Cloud sync with Firebase
- [ ] Dark mode toggle
- [ ] Export habit data as CSV

---

## 🛠️ Tech Concepts Demonstrated

This project demonstrates real-world Android development skills including:

- **Clean Architecture** — strict layer separation, dependency rule enforced
- **MVVM** — UI fully driven by ViewModel + LiveData, no business logic in Fragments
- **Dependency Injection** — Hilt modules for database, network, and repo bindings
- **Coroutines** — all DB and network operations off the main thread
- **Room + KSP** — compile-time verified database queries
- **Safe navigation** — type-safe argument passing with SafeArgs
- **ViewBinding** — null-safe view access, no `findViewById`

---

## 📚 About

Built as a personal project to practice industry-standard Android development patterns.

> Developed by [Kunal Yadav](https://github.com/kunal9812) — B.Tech Computer Science, Manav Rachna University

---

## 🤝 Connect

- 💼 [LinkedIn](https://www.linkedin.com/in/kunal-yadav-ba4510364/)
- 🐙 [GitHub](https://github.com/kunal9812)
- 📧 kunalyadav49101@gmail.com

---

## ⭐ If you found this project useful or learned something from it, please consider starring the repo!
