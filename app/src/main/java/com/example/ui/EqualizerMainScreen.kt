package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.AppThemeMode
import com.example.model.VisualizerStyle
import com.example.ui.components.AudioPlayerBar
import com.example.ui.components.BassGainControlsView
import com.example.ui.components.Equalizer12BandFaders
import com.example.ui.components.FrequencyCurveGraph
import com.example.ui.components.PresetSelectorBar
import com.example.ui.components.SystemSessionDialog
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.VuGreen
import com.example.ui.theme.VuYellow

@Composable
fun EqualizerMainScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    val isBypassed by viewModel.isBypassed.collectAsStateWithLifecycle()
    val presets by viewModel.allPresets.collectAsStateWithLifecycle()
    val playerState by viewModel.playerState.collectAsStateWithLifecycle()
    val spectrumState by viewModel.spectrumState.collectAsStateWithLifecycle()

    var showSessionDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(customColors.bg),
        containerColor = customColors.bg,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars)
        ) {
            // STUDIO TOP HEADER & THEME SWITCHER
            StudioTopHeader(
                isEnabled = settings.isEnabled,
                isBypassed = isBypassed,
                presetName = settings.selectedPresetName,
                themeMode = settings.themeMode,
                onTogglePower = { viewModel.toggleEqEnabled() },
                onOpenThemeDialog = { showThemeDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            )

            // SCROLLABLE CONTENT BODY
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // 1. DYNAMIC FREQUENCY CURVE & REAL-TIME AUDIO VISUALIZER
                FrequencyCurveGraph(
                    bands = settings.bands,
                    spectrumState = spectrumState,
                    visualizerStyle = settings.visualizerStyle,
                    isEqEnabled = settings.isEnabled,
                    isBypassed = isBypassed,
                    onStyleSelected = { viewModel.setVisualizerStyle(it) }
                )

                // 2. 12-BAND GRAPHIC EQUALIZER FADERS
                Equalizer12BandFaders(
                    bands = settings.bands,
                    isEqEnabled = settings.isEnabled,
                    isBypassed = isBypassed,
                    onBandGainChanged = { bandId, gainDb ->
                        viewModel.setBandGain(bandId, gainDb)
                    }
                )

                // 3. BASS CONTROL, MASTER GAIN, STEREO WIDENING, 3D SPATIAL & LIMITER
                BassGainControlsView(
                    bassPercent = settings.bassBoostPercent,
                    bassCutoffHz = settings.bassCutoffHz,
                    bassPunchMode = settings.bassPunchMode,
                    masterGainDb = settings.masterGainDb,
                    isLimiterEnabled = settings.isLimiterEnabled,
                    stereoBalance = settings.stereoBalance,
                    virtualizerPercent = settings.virtualizerPercent,
                    stereoWideningPercent = settings.stereoWideningPercent,
                    leftVuLevel = spectrumState.leftLevel,
                    rightVuLevel = spectrumState.rightLevel,
                    isEnabled = settings.isEnabled,
                    isBypassed = isBypassed,
                    onBassChanged = { viewModel.setBassBoost(it) },
                    onBassCutoffChanged = { viewModel.setBassCutoff(it) },
                    onBassPunchModeChanged = { viewModel.setBassPunchMode(it) },
                    onMasterGainChanged = { viewModel.setMasterGain(it) },
                    onLimiterToggled = { viewModel.setLimiterEnabled(it) },
                    onStereoBalanceChanged = { viewModel.setStereoBalance(it) },
                    onVirtualizerChanged = { viewModel.setVirtualizer(it) },
                    onStereoWideningChanged = { viewModel.setStereoWidening(it) }
                )

                // 4. PRESETS & CUSTOM PROFILES MANAGEMENT (Save, Rename, Delete, Reset, Bypass)
                PresetSelectorBar(
                    presets = presets,
                    selectedPresetName = settings.selectedPresetName,
                    isBypassed = isBypassed,
                    onPresetSelected = { viewModel.selectPreset(it) },
                    onSaveCustomPreset = { viewModel.saveCustomPreset(it) },
                    onRenameCustomPreset = { id, newName ->
                        presets.find { it.id == id }?.let { entity ->
                            viewModel.renameCustomPreset(entity, newName)
                        }
                    },
                    onDeleteCustomPreset = { viewModel.deleteCustomPreset(it) },
                    onResetFlat = { viewModel.resetToFlat() },
                    onToggleBypass = { viewModel.setBypassed(!isBypassed) }
                )

                // 5. BUILT-IN AUDIO PLAYER & DEMO TRACK CONTROLLER
                AudioPlayerBar(
                    playerState = playerState,
                    onTogglePlayPause = { viewModel.togglePlayPause() },
                    onSeekTo = { viewModel.seekTo(it) },
                    onSelectDemoTrack = { viewModel.playDemoTrack(it) },
                    onSelectCustomAudio = { uri, title -> viewModel.playCustomAudio(uri, title) },
                    onOpenSessionDialog = { showSessionDialog = true }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showSessionDialog) {
        SystemSessionDialog(
            currentSessionId = playerState.audioSessionId,
            onDismiss = { showSessionDialog = false },
            onSetSessionId = { sessionId ->
                viewModel.setAudioSessionId(sessionId)
            }
        )
    }

    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentMode = settings.themeMode,
            onModeSelected = {
                viewModel.setThemeMode(it)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
}

@Composable
fun StudioTopHeader(
    isEnabled: Boolean,
    isBypassed: Boolean,
    presetName: String,
    themeMode: AppThemeMode,
    onTogglePower: () -> Unit,
    onOpenThemeDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val primaryColor = customColors.accent

    Row(
        modifier = modifier.testTag("studio_top_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Brand
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(customColors.accentContainer)
                    .border(1.dp, primaryColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Equalizer,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "12 BAND EQUALIZER",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = customColors.textPrimary,
                        letterSpacing = 0.3.sp
                    )
                    // Status indicator dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(if (isEnabled && !isBypassed) VuGreen else VuYellow)
                    )
                }
                Text(
                    text = "DSP ENGINE • $presetName",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor,
                    letterSpacing = 0.6.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Actions: Theme Selector & Master Power Toggle
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Theme Mode Button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(customColors.controlBg)
                    .border(1.dp, customColors.border, CircleShape)
                    .clickable { onOpenThemeDialog() }
                    .testTag("theme_selector_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (themeMode) {
                        AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                        AppThemeMode.DARK -> Icons.Default.NightlightRound
                        AppThemeMode.AMOLED -> Icons.Default.DarkMode
                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                    },
                    contentDescription = "Theme Mode",
                    tint = primaryColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Master Power Toggle
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isEnabled) customColors.accentContainer else customColors.controlBg)
                    .border(
                        1.dp,
                        if (isEnabled) primaryColor else customColors.border,
                        RoundedCornerShape(18.dp)
                    )
                    .clickable { onTogglePower() }
                    .padding(horizontal = 12.dp)
                    .testTag("master_power_toggle"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PowerSettingsNew,
                        contentDescription = "Master Power",
                        tint = if (isEnabled) primaryColor else customColors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEnabled) "ACTIVE" else "BYPASS",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEnabled) primaryColor else customColors.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

data class ThemeOption(
    val mode: AppThemeMode,
    val title: String,
    val subtitle: String
)

@Composable
fun ThemeSelectionDialog(
    currentMode: AppThemeMode,
    onModeSelected: (AppThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    val customColors = LocalCustomColors.current
    val options = listOf(
        ThemeOption(AppThemeMode.DARK, "Sophisticated Dark", "Studio charcoal with violet accents"),
        ThemeOption(AppThemeMode.AMOLED, "AMOLED Pure Black", "Zero battery pure black for OLED displays"),
        ThemeOption(AppThemeMode.SYSTEM, "System Auto Mode", "Follows device dark/light setting automatically"),
        ThemeOption(AppThemeMode.LIGHT, "Modern Studio Light", "Clean crisp daytime studio look")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = "Theme",
                    tint = customColors.accent
                )
                Text(
                    text = "Select Display Theme",
                    fontWeight = FontWeight.Bold,
                    color = customColors.textPrimary
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                options.forEach { option ->
                    val isSelected = currentMode == option.mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) customColors.accentContainer else customColors.controlBg)
                            .border(
                                1.dp,
                                if (isSelected) customColors.accent else customColors.border,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { onModeSelected(option.mode) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = option.title,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) customColors.accent else customColors.textPrimary
                            )
                            Text(
                                text = option.subtitle,
                                fontSize = 10.sp,
                                color = customColors.textMuted
                            )
                        }
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Equalizer,
                                contentDescription = "Active",
                                tint = customColors.accent,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = customColors.textSecondary)
            }
        },
        containerColor = customColors.cardBg,
        shape = RoundedCornerShape(24.dp)
    )
}



