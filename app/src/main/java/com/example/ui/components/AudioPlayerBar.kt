package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.DemoTrack
import com.example.audio.PlayerState
import com.example.ui.theme.DeepVioletContainer
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SliderTrackInactive
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioControlBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun AudioPlayerBar(
    playerState: PlayerState,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onSelectDemoTrack: (DemoTrack) -> Unit,
    onSelectCustomAudio: (Uri, String) -> Unit,
    onOpenSessionDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showTrackMenu by remember { mutableStateOf(false) }

    val audioFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            var fileName = "Custom Audio Track"
            try {
                context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (_: Exception) {}
            onSelectCustomAudio(it, fileName)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(StudioCardBg)
            .border(1.dp, StudioBorder, RoundedCornerShape(28.dp))
            .padding(16.dp)
            .testTag("audio_player_bar")
    ) {
        // Track Header & Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Track Icon & Title
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showTrackMenu = true },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (playerState.isPlaying) DeepVioletContainer else StudioControlBg)
                        .border(
                            0.8.dp,
                            if (playerState.isPlaying) LavenderAccent else StudioBorder,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Audiotrack,
                        contentDescription = "Audio Track",
                        tint = if (playerState.isPlaying) LavenderAccent else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerState.currentTrackTitle,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (playerState.isCustomFile) "Local Device Audio • Tap to change" else "Built-in Studio Demo • Tap to change",
                        fontSize = 10.sp,
                        color = if (playerState.isPlaying) LavenderAccent else TextMuted,
                        maxLines = 1
                    )
                }

                // Track Switch Dropdown Menu
                DropdownMenu(
                    expanded = showTrackMenu,
                    onDismissRequest = { showTrackMenu = false },
                    modifier = Modifier.background(StudioCardBg)
                ) {
                    DemoTrack.values().forEach { track ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(text = track.title, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(text = track.description, fontSize = 10.sp, color = TextSecondary)
                                }
                            },
                            onClick = {
                                showTrackMenu = false
                                onSelectDemoTrack(track)
                            }
                        )
                    }
                }
            }

            // Action Buttons: Load File, System Session, Play/Pause
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Open File Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StudioControlBg)
                        .border(0.8.dp, StudioBorder, CircleShape)
                        .clickable { audioFilePickerLauncher.launch("audio/*") }
                        .testTag("open_audio_file_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Open Audio File",
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Audio Session Hook Button
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(StudioControlBg)
                        .border(0.8.dp, StudioBorder, CircleShape)
                        .clickable { onOpenSessionDialog() }
                        .testTag("audio_session_hook_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsInputComponent,
                        contentDescription = "Audio Session Settings",
                        tint = SoftViolet,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Main Play/Pause Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .shadow(if (playerState.isPlaying) 6.dp else 0.dp, CircleShape, spotColor = LavenderAccent)
                        .clip(CircleShape)
                        .background(
                            if (playerState.isPlaying) LavenderAccent else StudioControlBg
                        )
                        .border(0.8.dp, if (playerState.isPlaying) LavenderAccent else StudioBorder, CircleShape)
                        .clickable { onTogglePlayPause() }
                        .testTag("play_pause_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                        tint = if (playerState.isPlaying) DeepVioletContainer else TextPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Seek Bar & Time Display
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = formatDuration(playerState.currentPositionMs),
                fontSize = 10.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )

            val progress = if (playerState.durationMs > 0) {
                (playerState.currentPositionMs.toFloat() / playerState.durationMs.toFloat()).coerceIn(0f, 1f)
            } else 0f

            Slider(
                value = progress,
                onValueChange = { norm ->
                    val newPos = (norm * playerState.durationMs).toLong()
                    onSeekTo(newPos)
                },
                colors = SliderDefaults.colors(
                    thumbColor = LavenderAccent,
                    activeTrackColor = LavenderAccent,
                    inactiveTrackColor = SliderTrackInactive
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp)
                    .testTag("track_seek_bar")
            )

            Text(
                text = formatDuration(playerState.durationMs),
                fontSize = 10.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

