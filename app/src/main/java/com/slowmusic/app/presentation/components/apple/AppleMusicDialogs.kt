package com.slowmusic.app.presentation.components.apple

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.vector.*
import androidx.compose.ui.layout.*
import androidx.compose.ui.platform.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.*
import coil.compose.AsyncImage
import com.slowmusic.app.domain.model.*
import com.slowmusic.app.presentation.theme.apple.*

/**
 * Apple Music Style Confirmation Dialog
 */
@Composable
fun AppleConfirmationDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = false
) {
    Dialog(onDismissRequest = onDismiss) {
        AppleGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            cornerRadius = 24.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    style = AppleTypography.title3,
                    color = AppleColors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = message,
                    style = AppleTypography.body,
                    color = AppleColors.textSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AppleColors.textPrimary
                        ),
                        border = BorderStroke(1.dp, AppleColors.glassBorder)
                    ) {
                        Text(
                            text = dismissText,
                            style = AppleTypography.headline
                        )
                    }
                    
                    // Confirm button
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDestructive) AppleColors.secondary else AppleColors.primary
                        )
                    ) {
                        Text(
                            text = confirmText,
                            style = AppleTypography.headline,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

/**
 * Apple Music Style Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleBottomSheet(
    sheetState: SheetState,
    onDismiss: () -> Unit,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = AppleColors.background,
        contentColor = AppleColors.textPrimary,
        dragHandle = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(AppleColors.textTertiary)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        },
        modifier = Modifier.navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Title
            if (title != null) {
                Text(
                    text = title,
                    style = AppleTypography.title3,
                    color = AppleColors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )
                
                Divider(
                    color = AppleColors.glassBorder,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
            
            content()
        }
    }
}

/**
 * Apple Music Style Song Options Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppleSongOptionsSheet(
    song: Song,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onPlayNext: () -> Unit,
    onAddToQueue: () -> Unit,
    onAddToPlaylist: () -> Unit,
    onDownload: () -> Unit,
    onShare: () -> Unit,
    onArtistClick: () -> Unit,
    onAlbumClick: () -> Unit,
    onNotFavorite: Boolean = true,
    onToggleFavorite: () -> Unit
) {
    AppleBottomSheet(
        sheetState = sheetState,
        onDismiss = onDismiss,
        title = song.title
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Song header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.albumArtUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = AppleTypography.headline,
                        color = AppleColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = AppleTypography.subheadline,
                        color = AppleColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Options
            AppleSheetOption(
                icon = Icons.Filled.PlayCircle,
                title = "Play Next",
                onClick = {
                    onPlayNext()
                    onDismiss()
                }
            )
            
            AppleSheetOption(
                icon = Icons.Filled.Queue,
                title = "Add to Queue",
                onClick = {
                    onAddToQueue()
                    onDismiss()
                }
            )
            
            AppleSheetOption(
                icon = Icons.Filled.PlaylistAdd,
                title = "Add to Playlist",
                onClick = {
                    onAddToPlaylist()
                    onDismiss()
                }
            )
            
            AppleSheetOption(
                icon = if (onNotFavorite) Icons.Filled.FavoriteBorder else Icons.Filled.Favorite,
                title = if (onNotFavorite) "Add to Favorites" else "Remove from Favorites",
                onClick = {
                    onToggleFavorite()
                    onDismiss()
                },
                iconTint = if (!onNotFavorite) AppleColors.secondary else AppleColors.textPrimary
            )
            
            AppleSheetOption(
                icon = Icons.Filled.Download,
                title = "Download",
                onClick = {
                    onDownload()
                    onDismiss()
                }
            )
            
            AppleSheetOption(
                icon = Icons.Filled.Share,
                title = "Share",
                onClick = {
                    onShare()
                    onDismiss()
                }
            )
            
            Divider(
                color = AppleColors.glassBorder,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            AppleSheetOption(
                icon = Icons.Filled.Person,
                title = "Go to Artist",
                onClick = {
                    onArtistClick()
                    onDismiss()
                }
            )
            
            AppleSheetOption(
                icon = Icons.Filled.Album,
                title = "Go to Album",
                onClick = {
                    onAlbumClick()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun AppleSheetOption(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color = AppleColors.textPrimary,
    subtitle: String? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppleTypography.body,
                    color = AppleColors.textPrimary
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AppleTypography.caption1,
                        color = AppleColors.textSecondary
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = AppleColors.textTertiary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Apple Music Style Share Sheet
 */
@Composable
fun AppleShareSheet(
    song: Song,
    onDismiss: () -> Unit,
    onShareLink: () -> Unit,
    onCopyLink: () -> Unit,
    onMessage: () -> Unit,
    onEmail: () -> Unit
) {
    AppleBottomSheet(
        sheetState = rememberModalBottomSheetState(),
        onDismiss = onDismiss,
        title = "Share"
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            // Song preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = song.albumArtUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = song.title,
                        style = AppleTypography.headline,
                        color = AppleColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = song.artist,
                        style = AppleTypography.subheadline,
                        color = AppleColors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Share options
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ShareOption(
                    icon = Icons.Filled.Link,
                    label = "Copy Link",
                    onClick = {
                        onCopyLink()
                        onDismiss()
                    }
                )
                
                ShareOption(
                    icon = Icons.Filled.Email,
                    label = "Email",
                    onClick = {
                        onEmail()
                        onDismiss()
                    }
                )
                
                ShareOption(
                    icon = Icons.Filled.Message,
                    label = "Message",
                    onClick = {
                        onMessage()
                        onDismiss()
                    }
                )
            }
            
            // More apps would go here
            Divider(
                color = AppleColors.glassBorder,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            AppleSheetOption(
                icon = Icons.Filled.Share,
                title = "More Options...",
                onClick = {
                    onShareLink()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun ShareOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(AppleColors.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = label,
            style = AppleTypography.caption1,
            color = AppleColors.textSecondary
        )
    }
}

/**
 * Apple Music Style Create Playlist Dialog
 */
@Composable
fun AppleCreatePlaylistDialog(
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    
    Dialog(onDismissRequest = onDismiss) {
        AppleGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            cornerRadius = 24.dp
        ) {
            Column {
                Text(
                    text = "New Playlist",
                    style = AppleTypography.title3,
                    color = AppleColors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                OutlinedTextField(
                    value = playlistName,
                    onValueChange = { playlistName = it },
                    placeholder = {
                        Text(
                            text = "Playlist Name",
                            color = AppleColors.textTertiary
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AppleColors.primary,
                        unfocusedBorderColor = AppleColors.glassBorder,
                        focusedTextColor = AppleColors.textPrimary,
                        unfocusedTextColor = AppleColors.textPrimary,
                        cursorColor = AppleColors.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AppleColors.glassBorder)
                    ) {
                        Text(
                            text = "Cancel",
                            style = AppleTypography.headline,
                            color = AppleColors.textPrimary
                        )
                    }
                    
                    Button(
                        onClick = {
                            if (playlistName.isNotBlank()) {
                                onCreate(playlistName)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = playlistName.isNotBlank()
                    ) {
                        Text(
                            text = "Create",
                            style = AppleTypography.headline,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
