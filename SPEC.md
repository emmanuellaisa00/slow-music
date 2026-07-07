# 🎵 Slow Music - Complete Specification

## 📱 Overview

**Slow Music** is a modern Spotify-like music streaming Android app built with Kotlin, Jetpack Compose, and Clean Architecture.

### 🎨 UI Styles Available

| Style | Description |
|-------|-------------|
| **Default** | Material Design 3 - Standard Android look |
| **Apple Music** *(Optional)* | Glassmorphism, dynamic colors, 120Hz animations |

---

## 🎨 Apple Music UI Style (Optional)

### Features

| Feature | Description |
|---------|-------------|
| **Glassmorphism** | Subtle frosted glass effects on cards and overlays |
| **Liquid Glass** | Soft gradient overlays with subtle transparency |
| **Dynamic Colors** | Extract colors from album art |
| **120Hz Animations** | Smooth spring animations that adapt to display refresh rate |
| **Large Corners** | 24dp rounded corners on cards |
| **SF Pro Style** | Inter font family with Apple-style typography |

### Components

```
┌─────────────────────────────────────┐
│  Settings                           │
├─────────────────────────────────────┤
│                                     │
│  ┌─────────────────────────────┐     │
│  │  APPEARANCE                 │     │  ← Section Header
│  │                             │     │
│  │  Theme            System  > │     │  ← Glass Card
│  │  Navigation       Tabs    > │     │
│  │                             │     │
│  └─────────────────────────────┘     │
│                                     │
│  ┌─────────────────────────────┐     │
│  │  APPLE MUSIC STYLE           │     │
│  │                             │     │
│  │  ◐ Glassmorphism        [✓] │     │  ← Toggle
│  │  ◐ Liquid Glass        [✓] │     │
│  │  ◐ Dynamic Colors      [✓] │     │
│  │  ◐ Smooth Animations   [✓] │     │
│  │  ◐ Rounded Corners     24dp│     │
│  │                             │     │
│  └─────────────────────────────┘     │
│                                     │
└─────────────────────────────────────┘
```

---

## 📱 Screens (30 Total)

### Main Screens
1. **Home** - Browse music, trending, recommendations
2. **Search** - Search songs, artists, albums, voice search
3. **Library** - Favorites, playlists, downloads, local music
4. **Profile** - Account, subscription, settings

### Library Screens
5. **Favorites** - Liked songs
6. **Recently Played** - Listening history
7. **Most Played** - Top songs
8. **Downloads** - Offline music
9. **Local Music** - Device music
10. **Playlists** - User playlists
11. **Artists** - Followed artists
12. **Albums** - Saved albums

### Detail Screens
13. **Artist Details** - Artist info and songs
14. **Album Details** - Album info and tracks
15. **Playlist Details** - Playlist songs
16. **Genre Details** - Genre browse

### Settings Screens
17. **Settings** - Main settings
18. **Appearance** - Theme, navigation style
19. **Playback** - Crossfade, speed, equalizer
20. **Downloads** - Storage, quality
21. **Equalizer** - Audio settings
22. **Network** - Streaming options
23. **Subscription** - Premium plans
24. **Logs** - Debug logs (for testing)
25. **About** - App info

### Player Screens
26. **Full Player** - Now playing (default + Apple Music style)
27. **Queue** - Playback queue
28. **Lyrics** - Song lyrics

---

## 👆 Gestures (11 Total)

| Gesture | Screen | Action |
|---------|--------|--------|
| **Tap** | All | Select/Play |
| **Long Press** | Song items | Context menu |
| **Swipe Horizontal** | Tabs | Switch tabs |
| **Swipe** | Search history | Clear item |
| **Pull to Refresh** | Home, Search | Refresh |
| **Scroll** | All lists | Browse |
| **Drag** | Queue | Reorder |
| **Seek** | Player | Seek track |
| **Pinch** | Album art | Zoom |
| **Double Tap** | Song card | Quick play |
| **Swipe Down** | Player | Minimize |

---

## 🎬 Animations (20+)

### Navigation Transitions
| Animation | Duration | Effect |
|-----------|----------|--------|
| Screen transition | 300ms | Slide + Fade |
| Tab switch | 200ms | Crossfade |
| Bottom sheet | 250ms | Slide up |
| Dialog | 200ms | Scale + Fade |

### Component Animations
| Animation | Component |
|-----------|-----------|
| Mini player show/hide | Slide up/down |
| Play button | Scale on press |
| Like animation | Scale + fill |
| Loading spinner | Continuous rotation |
| Shimmer | Skeleton loading |
| Progress bar | Smooth interpolation |
| Album art rotation | Continuous (when playing) |
| List item stagger | Fade in sequence |
| Bottom nav selection | Scale + color |
| Spring physics | All toggle animations |

### Apple Music Specific
| Animation | Duration | Effect |
|-----------|----------|--------|
| Spring bounce | 400ms | Bouncy button press |
| Glass blur | 200ms | Fade in/out |
| Color transition | 500ms | Dynamic color change |
| Album glow | Loop | Subtle pulse |
| 120Hz motion | Native | Adaptive refresh |

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │ Screens │  │Components│  │ Theme   │  │
│  └────┬────┘  └────┬────┘  └────┬────┘  │
│       └───────────┼───────────┘        │
│            ViewModels                  │
├─────────────────────────────────────────┤
│              Domain Layer              │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │  Use    │  │ Models  │  │Repos    │  │
│  │ Cases   │  │         │  │Interfaces│ │
│  └─────────┘  └─────────┘  └─────────┘  │
├─────────────────────────────────────────┤
│               Data Layer               │
│  ┌─────────┐  ┌─────────┐  ┌─────────┐  │
│  │Remote   │  │Local    │  │Repository│ │
│  │(iTunes) │  │(Storage)│  │Impls    │  │
│  └─────────┘  └─────────┘  └─────────┘  │
├─────────────────────────────────────────┤
│            DI (Hilt)                    │
└─────────────────────────────────────────┘
```

---

## 📦 Dependencies

| Category | Library | Version |
|----------|---------|---------|
| UI | Jetpack Compose | BOM 2024.01 |
| Design | Material 3 | Latest |
| DI | Hilt | 2.50 |
| Network | Retrofit | 2.9.0 |
| Images | Coil | 2.5.0 |
| Media | Media3/ExoPlayer | 1.2.1 |
| Database | DataStore | 1.0.0 |
| Navigation | Navigation Compose | 2.7.6 |

---

## 🚀 Build Instructions

```bash
# Clone
git clone https://github.com/emmanuellaisa00/slow-music.git
cd slow-music

# Build
./gradlew assembleDebug

# Install
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## ⚠️ Important

**Rotate your GitHub token immediately!**
Token: Contact repository owner for access

Go to: GitHub → Settings → Developer Settings → Personal access tokens → Regenerate
