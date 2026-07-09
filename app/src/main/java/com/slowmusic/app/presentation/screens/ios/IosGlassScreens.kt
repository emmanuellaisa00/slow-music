package com.slowmusic.app.presentation.screens.ios

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.screens.home.HomeViewModel
import com.slowmusic.app.presentation.screens.library.LibraryViewModel
import com.slowmusic.app.presentation.screens.profile.ProfileViewModel
import com.slowmusic.app.presentation.screens.search.SearchViewModel
import com.slowmusic.app.presentation.screens.settings.SettingsViewModel

private val Void = Color(0xFF0B0C14)
private val Panel = Color(0xFF151726)
private val Stroke = Color.White.copy(alpha = 0.10f)
private val Violet = Color(0xFF7C6CFF)
private val Ember = Color(0xFFFF6F91)
private val Success = Color(0xFF6CFFB0)
private val Text = Color(0xFFF5F3FF)
private val Muted = Color(0xFF8A8AA3)

@Composable
private fun IosPage(
    title: String,
    trailing: @Composable RowScope.() -> Unit = {},
    content: LazyListScope.() -> Unit
) {
    Box(Modifier.fillMaxSize().background(Void)) {
        IosAurora()
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 10.dp, bottom = 120.dp)
        ) {
            item {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(title, color = Text, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold)
                    Row(content = trailing)
                }
            }
            content()
        }
    }
}

@Composable
private fun IosAurora() {
    Box(Modifier.fillMaxSize().background(
        Brush.radialGradient(listOf(Violet.copy(alpha = .35f), Color.Transparent), radius = 760f)
    ))
    Box(Modifier.fillMaxSize().background(
        Brush.radialGradient(listOf(Ember.copy(alpha = .22f), Color.Transparent), center = androidx.compose.ui.geometry.Offset(900f, 120f), radius = 720f)
    ))
}

@Composable
private fun GlassCard(modifier: Modifier = Modifier, radius: Int = 22, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.border(1.dp, Stroke, RoundedCornerShape(radius.dp)),
        shape = RoundedCornerShape(radius.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.07f))
    ) { Column(Modifier.background(Brush.linearGradient(listOf(Color.White.copy(.08f), Color.Transparent))).padding(14.dp), content = content) }
}

@Composable
private fun GlassIcon(icon: ImageVector, onClick: () -> Unit = {}) {
    Box(
        modifier = Modifier.size(40.dp).clip(RoundedCornerShape(14.dp)).background(Color.White.copy(alpha = .07f)).border(1.dp, Stroke, RoundedCornerShape(14.dp)).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) { Icon(icon, null, tint = Text, modifier = Modifier.size(21.dp)) }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit, onMore: () -> Unit = {}) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick).padding(horizontal = 20.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(song.albumArtUrl, null, Modifier.size(52.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(13.dp))
        Column(Modifier.weight(1f)) {
            Text(song.title, color = Text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${song.artist} • ${song.album}", color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        IconButton(onClick = onMore) { Icon(Icons.Filled.MoreVert, null, tint = Muted) }
    }
}

@Composable
private fun Section(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
        Text(title, color = Text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (action != null) Text(action, color = Violet, style = MaterialTheme.typography.labelMedium, modifier = Modifier.clickable(onClick = onAction))
    }
}

@Composable
fun IosGlassHomeScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onMore: (Song) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) { viewModel.loadContent() }
    IosPage(title = "Slow Music", trailing = { GlassIcon(Icons.Filled.Search, onNavigateToSearch) }) {
        item {
            GlassCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), radius = 28) {
                Text("JUMP BACK IN", color = Ember, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))
                val hero = state.recentlyPlayed.firstOrNull() ?: state.trendingSongs.firstOrNull()
                Text(hero?.title ?: "Discover your next sound", color = Text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(hero?.artist ?: "Cached discovery • full-song playback", color = Muted)
                Spacer(Modifier.height(12.dp))
                Button(onClick = { hero?.let { onSongClick(it, state.recentlyPlayed.ifEmpty { state.trendingSongs }) } }, colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Void)) { Text("Resume") }
            }
        }
        item { Section("Made for you") }
        item { LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) { items(state.recommendations.take(10).ifEmpty { state.trendingSongs.take(10) }) { song -> RailSong(song) { onSongClick(song, state.recommendations.ifEmpty { state.trendingSongs }) } } } }
        item { Section("Recently played") }
        items(state.recentlyPlayed.take(8)) { song -> SongRow(song, { onSongClick(song, state.recentlyPlayed) }, { onMore(song) }) }
        item { Section("Trending") }
        items(state.trendingSongs.take(8)) { song -> SongRow(song, { onSongClick(song, state.trendingSongs) }, { onMore(song) }) }
    }
}

@Composable
private fun RailSong(song: Song, onClick: () -> Unit) {
    GlassCard(Modifier.width(150.dp).clickable(onClick = onClick), radius = 16) {
        AsyncImage(song.albumArtUrl, null, Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.height(8.dp))
        Text(song.title, color = Text, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.SemiBold)
        Text(song.artist, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun IosGlassSearchScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onArtistClick: (String) -> Unit,
    onAlbumClick: (String) -> Unit,
    onPlaylistClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val recent by viewModel.recentSelections.collectAsState()
    IosPage(title = "Search") {
        item {
            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::updateQuery,
                placeholder = { Text("Songs, artists, moods…") },
                leadingIcon = { Icon(Icons.Filled.Search, null) },
                trailingIcon = { Icon(Icons.Filled.Mic, null) },
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White.copy(.08f), unfocusedContainerColor = Color.White.copy(.06f))
            )
        }
        item { Section(if (state.query.isBlank()) "Recent selections" else "Results") }
        if (state.query.isBlank()) {
            items(recent.take(12)) { song -> SongRow(song, { onSongClick(song, recent) }) }
        } else {
            items(state.results.songs.take(20)) { song -> SongRow(song, { viewModel.showSelectedSong(song); onSongClick(song, state.results.songs) }) }
            item { Section("Albums") }
            items(state.results.albums.take(8)) { album -> Text(album.title, color = Text, modifier = Modifier.fillMaxWidth().clickable { onAlbumClick(album.id) }.padding(20.dp)) }
            item { Section("Playlists") }
            items(state.results.playlists.take(8)) { playlist -> Text(playlist.name, color = Text, modifier = Modifier.fillMaxWidth().clickable { onPlaylistClick(playlist.id) }.padding(20.dp)) }
        }
    }
}

@Composable
fun IosGlassLibraryScreen(
    onFavorites: () -> Unit,
    onDownloads: () -> Unit,
    onLocal: () -> Unit,
    onPlaylists: () -> Unit,
    onSettings: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val fav by viewModel.favorites.collectAsState()
    val downloads by viewModel.downloadedSongs.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    IosPage(title = "Your Library", trailing = { GlassIcon(Icons.Filled.Add) }) {
        item { GlassCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), radius = 24) { Text("${fav.size} favorites • ${downloads.size} downloads • ${playlists.size} playlists", color = Text, fontWeight = FontWeight.Bold) } }
        item { Section("Browse") }
        item { LibraryButton(Icons.Filled.Favorite, "Favorites", "${fav.size} songs", onFavorites) }
        item { LibraryButton(Icons.Filled.Download, "Downloaded", "${downloads.size} songs offline", onDownloads) }
        item { LibraryButton(Icons.Filled.Smartphone, "Local Music", "On this device", onLocal) }
        item { LibraryButton(Icons.Filled.QueueMusic, "Playlists", "${playlists.size} playlists", onPlaylists) }
        item { LibraryButton(Icons.Filled.Settings, "Settings", "Appearance and playback", onSettings) }
    }
}

@Composable
private fun LibraryButton(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    GlassCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp).clickable(onClick = onClick), radius = 18) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, tint = Violet)
            Spacer(Modifier.width(14.dp))
            Column { Text(title, color = Text, fontWeight = FontWeight.SemiBold); Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
fun IosGlassProfileScreen(
    onSettings: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val sub by viewModel.subscription.collectAsState()
    IosPage(title = "Profile", trailing = { GlassIcon(Icons.Filled.Settings, onSettings) }) {
        item {
            Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(96.dp).clip(CircleShape).background(Brush.linearGradient(listOf(Violet, Ember))), contentAlignment = Alignment.Center) { Text("L", color = Text, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold) }
                Spacer(Modifier.height(14.dp))
                Text("Music Lover", color = Text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(sub.type.name.lowercase().replaceFirstChar { it.uppercase() }, color = Muted)
            }
        }
        item { GlassCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), radius = 22) { Text("Full-song playback • cached discovery • local library mode", color = Text) } }
        item { Section("Shortcuts") }
        item { LibraryButton(Icons.Filled.Settings, "Settings", "Appearance, cache, playback", onSettings) }
    }
}

@Composable
fun IosGlassSettingsSkin(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().background(Void)) { IosAurora(); content() }
}

@Composable
fun IosGlassBottomNav(
    items: List<com.slowmusic.app.presentation.navigation.BottomNavItem>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(Panel.copy(alpha = 0.72f))
            .border(1.dp, Color.White.copy(alpha = 0.16f), RoundedCornerShape(26.dp))
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.screen.route
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onNavigate(item.screen.route) }
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.title,
                    tint = if (selected) Text else Muted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(3.dp))
                Box(Modifier.size(4.dp).clip(CircleShape).background(if (selected) Violet else Color.Transparent))
            }
        }
    }
}

@Composable
fun IosGlassMiniPlayer(
    song: Song,
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onNext: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        radius = 20
    ) {
        Box(Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.height(48.dp)) {
                AsyncImage(song.albumArtUrl, null, Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(song.title, color = Text, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(song.artist, color = Muted, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onPlayPause, modifier = Modifier.size(34.dp)) { Icon(if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow, null, tint = Text) }
                IconButton(onClick = onNext, modifier = Modifier.size(34.dp)) { Icon(Icons.Filled.SkipNext, null, tint = Text) }
            }
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth().height(2.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = .12f)))
            Box(Modifier.align(Alignment.BottomStart).fillMaxWidth(progress.coerceIn(0f, 1f)).height(2.dp).clip(RoundedCornerShape(2.dp)).background(Brush.horizontalGradient(listOf(Violet, Ember))))
        }
    }
}

@Composable
fun IosGlassQueueScreen(
    currentSong: Song?,
    queue: List<Song>,
    onSongClick: (Song) -> Unit,
    onRemoveFromQueue: (Int) -> Unit,
    onMoveQueueItem: (Int, Int) -> Unit,
    onClearQueue: () -> Unit,
    onSaveAsPlaylist: () -> Unit,
    onNavigateBack: () -> Unit
) {
    IosPage(title = "Queue", trailing = { GlassIcon(Icons.Filled.Close, onNavigateBack) }) {
        currentSong?.let { now ->
            item {
                GlassCard(Modifier.fillMaxWidth().padding(horizontal = 20.dp), radius = 22) {
                    Text("NOW PLAYING", color = Violet, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    SongRow(now, onClick = { onSongClick(now) })
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Up next", color = Text, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Row { Text("Save", color = Violet, modifier = Modifier.clickable(onClick = onSaveAsPlaylist)); Spacer(Modifier.width(16.dp)); Text("Clear", color = Ember, modifier = Modifier.clickable(onClick = onClearQueue)) }
            }
        }
        items(queue) { song ->
            val index = queue.indexOf(song)
            Row(Modifier.fillMaxWidth().padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = { if (index > 0) onMoveQueueItem(index, index - 1) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Filled.KeyboardArrowUp, null, tint = Muted) }
                    IconButton(onClick = { if (index < queue.lastIndex) onMoveQueueItem(index, index + 1) }, modifier = Modifier.size(30.dp)) { Icon(Icons.Filled.KeyboardArrowDown, null, tint = Muted) }
                }
                Box(Modifier.weight(1f)) { SongRow(song, { onSongClick(song) }, { onRemoveFromQueue(index) }) }
            }
        }
    }
}

@Composable
fun IosGlassLyricsScreen(
    song: Song,
    lyrics: String?,
    progress: Float,
    onSeekToProgress: (Float) -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(Modifier.fillMaxSize().background(Void)) {
        AsyncImage(song.albumArtUrl, null, Modifier.fillMaxSize().blur(58.dp), contentScale = ContentScale.Crop)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Black.copy(alpha = .22f), Void.copy(alpha = .88f), Void))))
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(top = 20.dp, start = 28.dp, end = 28.dp, bottom = 100.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column { Text("LYRICS", color = Muted, style = MaterialTheme.typography.labelSmall); Text(song.title, color = Text, fontWeight = FontWeight.Bold) }
                    GlassIcon(Icons.Filled.Close, onNavigateBack)
                }
            }
            val rawLines = lyrics?.lines()?.filter { it.isNotBlank() }.orEmpty().ifEmpty { listOf("Lyrics are not available yet", "When lyrics are found they will appear here") }
            items(rawLines) { line ->
                val index = rawLines.indexOf(line)
                val active = index == ((rawLines.lastIndex.coerceAtLeast(0)) * progress).toInt().coerceIn(0, rawLines.lastIndex.coerceAtLeast(0))
                Text(
                    line.replace(Regex("""\[\d{1,2}:\d{2}(?:\.\d{1,3})?]"""), ""),
                    color = if (active) Text else Text.copy(alpha = .32f),
                    style = if (active) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth().clickable { onSeekToProgress(index.toFloat() / rawLines.lastIndex.coerceAtLeast(1)) }
                )
            }
        }
    }
}
