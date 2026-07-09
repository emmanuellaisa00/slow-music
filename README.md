# 🎵 Slow Music

> A modern Spotify-like music streaming Android app with optional Apple Music-style UI

![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple)
![Compose](https://img.shields.io/badge/Compose-BOM%202024.01-blue)
![Android](https://img.shields.io/badge/Android-API%2026+-green)
![License](https://img.shields.io/badge/License-MIT-orange)

---

## ✨ Features

### 🎨 Dual UI Styles

| Default UI | Apple Music UI (Optional) |
|------------|---------------------------|
| Material Design 3 | Glassmorphism |
| Standard animations | 120Hz Spring animations |
| Material colors | Dynamic colors from album art |
| Standard cards | Liquid glass effects |
| 8dp corners | 24dp rounded corners |

### 📱 Core Features

- 🎵 **Music Streaming** - Online discovery + Local files
- 🔍 **Search** - Songs, artists, albums, voice search
- ❤️ **Favorites** - Like songs
- 📋 **Playlists** - Create & manage
- ⬇️ **Downloads** - Offline listening
- 🎛️ **Equalizer** - Audio customization
- 🎙️ **Lyrics** - Song lyrics display
- 📤 **Share** - Share songs
- ⏰ **Sleep Timer** - Auto-stop
- 📺 **Widget** - Home screen player
- 📱 **Cast** - Chromecast/AirPlay
- 🎚️ **Audio Focus** - Handle interruptions

### 🏗️ Architecture

- **Clean Architecture** + MVVM
- **Hilt** for dependency injection
- **Jetpack Compose** for UI
- **Material Design 3**
- **Media3/ExoPlayer** for playback
- **Coil** for images
- **Retrofit** for networking
- **DataStore** for storage

---

## 🎨 Apple Music UI Style

Enable in **Settings → Appearance → UI Style → Apple Music**

### Visual Effects

```
┌─────────────────────────────────────┐
│  ┌─────────────────────────────┐    │
│  │  Glassmorphism Card         │    │  ← Frosted glass effect
│  │  ═══════════════════════    │    │
│  │  ◐ Glassmorphism       [✓] │    │  ← Spring animated toggle
│  │  ◐ Liquid Glass       [✓] │    │  ← Soft gradients
│  │  ◐ Dynamic Colors      [✓] │    │  ← Album art extraction
│  │  ◐ 120Hz Animations    [✓] │    │  ← Smooth spring physics
│  └─────────────────────────────┘    │
└─────────────────────────────────────┘
```

### Components

- **AppleGlassCard** - Frosted glass card with blur
- **LiquidGlassSurface** - Gradient overlay effect
- **AppleToggle** - Spring animated toggle
- **AppleSlider** - Smooth seek bar
- **AppleSegmentedControl** - Animated tabs
- **AppleNavigationBar** - Glass navigation
- **AppleProgressIndicator** - Animated progress

---

## 📱 Screens (30 Total)

| Category | Screens |
|----------|---------|
| Main | Home, Search, Library, Profile |
| Library | Favorites, Recent, Most Played, Downloads, Local, Playlists, Artists, Albums |
| Details | Artist, Album, Playlist, Genre |
| Settings | Settings, Appearance, Playback, Downloads, Equalizer, Network, Subscription, Logs, About |
| Player | Full Player, Queue, Lyrics |

---

## 👆 Gestures (11)

| Gesture | Action |
|---------|--------|
| Tap | Select/Play |
| Long Press | Context menu |
| Swipe Horizontal | Switch tabs |
| Pull to Refresh | Refresh content |
| Drag | Reorder queue |
| Seek | Seek track |
| Pinch | Zoom album art |
| Double Tap | Quick play |
| Swipe Down | Minimize player |

---

## 🎬 Animations (20+)

### Standard
- Screen transitions (300ms slide + fade)
- Tab switching (200ms crossfade)
- Bottom sheet (250ms)
- Mini player (slide up/down)
- Play button scale
- Like animation (scale + fill)
- Loading spinner

### Apple Music Style
- Spring physics (120Hz adaptive)
- Glass blur transitions (200ms)
- Dynamic color interpolation (500ms)
- Album glow pulse
- Staggered list animations
- Bouncy button press

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34

### Build

```bash
# Clone the repository
git clone https://github.com/emmanuellaisa00/slow-music.git
cd slow-music

# Open in Android Studio
# Or build via command line:
./gradlew assembleDebug

# APK location
app/build/outputs/apk/debug/app-debug.apk
```

### Install

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📂 Project Structure

```
slow-music/
├── app/src/main/java/com/slowmusic/app/
│   ├── domain/                 # Business logic
│   │   ├── model/             # Data classes
│   │   ├── repository/         # Repository interfaces
│   │   └── usecase/            # Use cases
│   ├── data/                   # Data layer
│   │   ├── remote/             # Online discovery API
│   │   └── repository/         # Repository implementations
│   ├── presentation/           # UI layer
│   │   ├── screens/            # All screens
│   │   │   ├── home/
│   │   │   ├── search/
│   │   │   ├── library/
│   │   │   ├── profile/
│   │   │   ├── settings/
│   │   │   └── player/
│   │   ├── components/         # Reusable components
│   │   │   └── apple/          # Apple Music components
│   │   ├── theme/              # Theme files
│   │   │   └── apple/          # Apple Music theme
│   │   └── navigation/
│   ├── di/                     # Hilt modules
│   ├── service/                # Media service
│   ├── widget/                 # Home screen widget
│   └── util/                   # Utilities
└── app/src/main/res/           # Resources
```

---

## ⚙️ Settings Options

### Appearance
- Theme (Light/Dark/System)
- Navigation Style (Tabs/Bottom Nav/Drawer)
- UI Style (Default/Apple Music)

### Apple Music Style
- Glassmorphism (on/off)
- Liquid Glass (on/off)
- Dynamic Colors (on/off)
- 120Hz Animations (on/off)
- Corner Radius (24dp)

### Playback
- Crossfade
- Playback Speed
- Auto-play Similar

### Audio
- Equalizer
- Audio Quality
- Audio Focus

### Downloads
- Wi-Fi Only
- Storage Management

---

## 🔐 Security

⚠️ **Important**: Rotate your GitHub token if you used one during setup.

Go to: **GitHub → Settings → Developer Settings → Personal access tokens → Regenerate**

---

## 📄 License

MIT License - See [LICENSE](LICENSE) for details.

---

## 🙏 Credits

- Online music discovery providers - Music data
- [Jetpack Compose](https://developer.android.com/jetpack/compose) - UI toolkit
- [Material Design](https://material.io/design) - Design system
- [Coil](https://github.com/coil-kt/coil) - Image loading
- [Media3](https://developer.android.com/media3) - Media playback

---

**Made with ❤️ and Kotlin**
