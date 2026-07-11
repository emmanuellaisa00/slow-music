package com.slowmusic.app.presentation.navigation

import androidx.navigation.NavHostController

/**
 * Centralised iOS-style navigation helpers.
 *
 * Android/default mode still uses the normal Material shell. iOS Glass uses these
 * route classifiers so tabs behave like a TabView, details behave like pushes,
 * and immersive screens (player, lyrics, queue, settings sheets) behave like
 * modal presentations.
 */
private val rootTabRoutes = setOf(
    Screen.Home.route,
    Screen.Search.route,
    Screen.Library.route,
    Screen.Profile.route
)

private val hiddenChromeRoutes = setOf(
    Screen.Splash.route,
    Screen.Onboarding.route
)

fun String?.isRootTabRoute(): Boolean = this in rootTabRoutes

fun String?.isLibraryChildRoute(): Boolean = this?.startsWith("library/") == true

fun String?.isDetailPushRoute(): Boolean {
    val route = this ?: return false
    return route.startsWith("artist/") ||
        route.startsWith("album/") ||
        route.startsWith("playlist/") ||
        route.startsWith("genre/") ||
        route.startsWith("song/") ||
        route == Screen.Artists.route ||
        route == Screen.Albums.route ||
        route == Screen.Playlists.route ||
        route == Screen.LocalMusic.route ||
        route == Screen.Favorites.route ||
        route == Screen.RecentPlays.route ||
        route == Screen.MostPlayed.route ||
        route == Screen.Downloads.route
}

fun String?.isModalRoute(): Boolean {
    val route = this ?: return false
    return route == Screen.Player.route ||
        route == Screen.Queue.route ||
        route == Screen.Lyrics.route ||
        route == Screen.Subscription.route ||
        route.startsWith("settings") ||
        route.startsWith("legal/") ||
        route.startsWith("permissions/") ||
        route.startsWith("cast/") ||
        route.startsWith("playlist/add/")
}

fun String?.showsIosGlassTabBar(): Boolean {
    val route = this ?: return false
    return route !in hiddenChromeRoutes && !route.isModalRoute()
}

fun String?.showsIosGlassMiniPlayer(): Boolean {
    val route = this ?: return false
    return route !in hiddenChromeRoutes && route != Screen.Player.route
}

fun NavHostController.navigateRootTab(route: String) {
    navigate(route) {
        popUpTo(Screen.Home.route) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.navigatePush(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}

fun NavHostController.navigateModal(route: String) {
    navigate(route) {
        launchSingleTop = true
    }
}
