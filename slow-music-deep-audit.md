# Slow Music — Deep UI/UX & Code Audit

Repo: `emmanuellaisa00/slow-music` · Kotlin, Jetpack Compose, MVVM, Hilt, Media3
Audit date: 2026-07-17

---

## TL;DR

The README promises a **Material Design 3 default + optional Apple Music glass style**, 30 screens, 11 gestures, 20+ animations. The actual codebase has **one working player screen (Apple Music style only)**, **a third half-built UI system nobody uses**, **two fully duplicated 700-line settings screens**, **error handling on 3 of 14 screens**, and **61% of image content descriptions set to `null`**. The app is more scaffold than product right now — wide, not deep.

---

## 1. Critical — The "dual UI" doesn't exist

- `NavigationGraph.kt` hardcodes `AppleMusicPlayerScreen` unconditionally. There is no Material-style player screen file anywhere in the repo.
- Only **one** place in the whole codebase branches on UI style at all: `MainActivity.kt:69-70`.
- There's also a third, apparently orphaned system: `screens/ios/IosGlassScreens.kt` + `navigation/IosNavigation.kt` + `navigation/IosGestureNavigation.kt` — a third design language that isn't the Material default or the Apple Music style described in the README.

**Impact:** the flagship feature on the README (toggle between two UI styles) is not real. Users who switch "UI Style" in settings likely see no change on the player — the single screen that matters most.

**Fix:** pick one system (Apple Music glass, since it's the only complete player) and delete the other two. Update the README to match reality. If dual UI stays a genuine goal, that's a separate scoped project — don't leave it half-declared.

---

## 2. Critical — Settings screen is duplicated, not themed

- `SettingsScreen.kt` — 753 lines
- `AppleMusicSettingsScreen.kt` — 701 lines

Two near-complete reimplementations of the same screen instead of one screen with swappable tokens/components. Same root cause as #1: the theming layer doesn't actually theme, it forks.

**Impact:** every settings feature must be built and bug-fixed twice. This is very likely where new features silently work in one style and not the other.

**Fix:** collapse to one `SettingsScreen` that reads from a theme object (colors, corner radius, card style, animation spec). This is the single highest-leverage refactor in the repo.

---

## 3. High — Error states missing on 11 of 14 screens

Screens with **no** error UI at all:

- `SplashScreen.kt`
- `LibraryScreen.kt`
- `ProfileScreen.kt`
- `ProfileSubscriptionEqualizerScreens.kt`
- `AppleMusicSettingsScreen.kt`
- `EqualizerControlScreen.kt`
- `SettingsScreen.kt`
- `IosGlassScreens.kt`
- `OnboardingScreen.kt`
- `AppleMusicPlayerScreen.kt`
- `QueueLyricsDownloadScreens.kt`

Only `HomeScreen.kt`, `SearchScreen.kt`, and `OnlineMusicScreens.kt` render an error state.

**Impact:** any repository/network failure on Library, Player, Queue, Downloads, or Settings likely shows a blank screen, an infinite spinner, or a silent no-op — not a message the user can act on. Player is the single most-used screen in a music app; it having zero error handling is the top priority fix.

**Fix:** use `HomeViewModel.kt` / `HomeScreen.kt` as the reference pattern (`uiState.error != null -> ErrorMessage(...)`) and apply it to at least Player, Library, Queue/Downloads first.

---

## 4. Medium-High — Accessibility: 65 of 106 content descriptions are `null`

61% of `contentDescription` values across the app are `null`, concentrated here:

| File | Null count |
|---|---|
| `AppleMusicSettingsScreen.kt` | 22 |
| `ProfileSubscriptionEqualizerScreens.kt` | 7 |
| `LibraryScreen.kt` | 5 |
| `AppleMusicDialogs.kt` | 5 |
| `AppleMusicEmptyStates.kt` | 4 |
| `AppearanceSelector.kt` | 4 |
| `ProfileScreen.kt` | 3 |
| `OnlineMusicScreens.kt` | 3 |
| `SettingsScreen.kt` | 2 |
| `SearchScreen.kt` | 2 |
| `OnboardingScreen.kt` | 2 |
| `AppleMusicNavigation.kt` | 2 |
| `QueueLyricsDownloadScreens.kt` | 1 |
| `AppleMusicPlayerScreen.kt` | 1 |
| `AppleMusicCards.kt` | 1 |
| `PlaybackArtworkBackdrop.kt` | 1 |

**Impact:** TalkBack/screen reader users can't identify icon-only controls (play/pause, like, more-options, nav icons) across most of the app. `null` is correct only for purely decorative images — worth auditing each one individually, since some of these 65 may be legitimately decorative, but the concentration in settings/dialogs/nav strongly suggests real interactive icons are unlabeled.

**Fix:** audit file-by-file starting with `AppleMusicSettingsScreen.kt` (22 instances) and the player screen (lowest count but highest usage frequency).

---

## 5. Medium — No design token / spacing system

832 inline `.dp` usages across the codebase, no central `Spacing`/`Dimens` object found. This is very likely *why* problem #1 and #2 happened — without shared tokens, "themeing" becomes "copy the screen and change the numbers."

**Fix:** extract a `Dimens` object (`Dimens.spacingXs/sm/md/lg`, `Dimens.cornerRadius`) and migrate incrementally, starting with the two settings screens as part of the #2 consolidation.

---

## 6. Low — README doesn't match repo

- Claims Material Design 3 default that doesn't exist as a screen.
- Claims Chromecast/AirPlay cast support — not verified in this pass, worth confirming it's implemented and not aspirational alongside the dual-UI claim.
- Two separate "rotate your GitHub token" warnings appear in repo history — no leaked token found in the diff itself, but worth manually confirming no `.env`, keystore, or credentials file was ever committed, since the warning was added deliberately.

**Fix:** after the UI consolidation (#1/#2), rewrite the README to describe what's actually shipped, not the target spec.

---

## Priority order

1. **Consolidate UI systems** — delete Material-default claim or build it for real; delete the orphaned iOS glass system if unused. (#1)
2. **Merge the two settings screens into one themed screen.** (#2)
3. **Add error states to Player, Library, Queue/Downloads** at minimum, then the rest. (#3)
4. **Fix `contentDescription = null`** on real interactive icons, starting with Settings and Player. (#4)
5. **Extract a spacing/dimens token system** as part of the settings consolidation, not as a separate pass. (#5)
6. **Rewrite README** once the above lands. (#6)
