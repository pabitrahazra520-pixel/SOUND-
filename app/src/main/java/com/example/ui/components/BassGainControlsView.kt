package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BassPunchMode
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.SliderTrackInactive
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.VuGreen
import com.example.ui.theme.VuRed
import com.example.ui.theme.VuYellow
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun BassGainControlsView(
    bassPercent: Float,
    bassCutoffHz: Int,
    bassPunchMode: BassPunchMode,
    masterGainDb: Float,
    isLimiterEnabled: Boolean,
    stereoBalance: Float,
    virtualizerPercent: Float,
    stereoWideningPercent: Float,
    leftVuLevel: Float,
    rightVuLevel: Float,
    isEnabled: Boolean,
    isBypassed: Boolean,
    onBassChanged: (Float) -> Unit,
    onBassCutoffChanged: (Int) -> Unit,
    onBassPunchModeChanged: (BassPunchMode) -> Unit,
    onMasterGainChanged: (Float) -> Unit,
    onLimiterToggled: (Boolean) -> Unit,
    onStereoBalanceChanged: (Float) -> Unit,
    onVirtualizerChanged: (Float) -> Unit,
    onStereoWideningChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val effectiveEnabled = isEnabled && !isBypassed
    val primaryColor = customColors.accent
    val secondaryColor = SoftViolet
    val cardBg = customColors.cardBg
    val borderCol = customColors.border

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bass_gain_controls_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // ROW 1: BASS CONTROL & MASTER GAIN CONTROL (Two primary cards)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // CARD 1: BASS BOOST CONTROL
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(28.dp))
                    .padding(16.dp)
                    .testTag("bass_control_card"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Waves,
                            contentDescription = "Bass",
                            tint = if (effectiveEnabled) primaryColor else customColors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "BASS BOOST",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = customColors.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "${bassPercent.toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (effectiveEnabled) primaryColor else customColors.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rotary Dial for Bass Boost
                StudioRotaryKnob(
                    value = bassPercent / 100f,
                    displayValue = "${bassPercent.toInt()}%",
                    label = "SUB BASS",
                    accentColor = primaryColor,
                    isEnabled = effectiveEnabled,
                    onValueChanged = { onBassChanged(it * 100f) },
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Sub-Bass Cutoff Selector
                Text(
                    text = "CUTOFF FREQ",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = customColors.textMuted,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(50, 80, 120, 160).forEach { freq ->
                        val isSelected = bassCutoffHz == freq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected && effectiveEnabled) customColors.accentContainer else customColors.controlBg)
                                .border(
                                    0.5.dp,
                                    if (isSelected && effectiveEnabled) primaryColor.copy(alpha = 0.7f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = effectiveEnabled) { onBassCutoffChanged(freq) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${freq}Hz",
                                fontSize = 9.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected && effectiveEnabled) primaryColor else customColors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Bass Punch Mode Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    BassPunchMode.values().forEach { mode ->
                        val isSelected = bassPunchMode == mode
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(26.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected && effectiveEnabled) customColors.accentContainer else customColors.controlBg)
                                .border(
                                    0.5.dp,
                                    if (isSelected && effectiveEnabled) secondaryColor else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable(enabled = effectiveEnabled) { onBassPunchModeChanged(mode) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = mode.title,
                                fontSize = 8.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected && effectiveEnabled) primaryColor else customColors.textMuted
                            )
                        }
                    }
                }
            }

            // CARD 2: MASTER GAIN & VU METER CONTROL
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(28.dp))
                    .background(cardBg)
                    .border(1.dp, borderCol, RoundedCornerShape(28.dp))
                    .padding(16.dp)
                    .testTag("gain_control_card"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Gain",
                            tint = if (effectiveEnabled) primaryColor else customColors.textMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "MASTER GAIN",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = customColors.textSecondary,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = when {
                            masterGainDb > 0 -> "+${String.format(Locale.US, "%.1f", masterGainDb)} dB"
                            masterGainDb < 0 -> "${String.format(Locale.US, "%.1f", masterGainDb)} dB"
                            else -> "0.0 dB"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (effectiveEnabled) primaryColor else customColors.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Rotary Dial for Master Gain (-12dB to +15dB, normalized 0..1)
                val normalizedGain = (masterGainDb + 12f) / 27f
                StudioRotaryKnob(
                    value = normalizedGain,
                    displayValue = if (masterGainDb > 0) "+${masterGainDb.toInt()}dB" else "${masterGainDb.toInt()}dB",
                    label = "PRE-AMP",
                    accentColor = secondaryColor,
                    isEnabled = effectiveEnabled,
                    onValueChanged = { norm ->
                        val newGain = (norm * 27f) - 12f
                        onMasterGainChanged(newGain)
                    },
                    modifier = Modifier.size(96.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Stereo VU Meter (L & R Level bars)
                Text(
                    text = "STEREO VU METER",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = customColors.textMuted,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(4.dp))
                StereoVuMeter(
                    leftLevel = if (effectiveEnabled) leftVuLevel else 0.05f,
                    rightLevel = if (effectiveEnabled) rightVuLevel else 0.05f,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Peak Limiter / Anti-Clipping Switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(customColors.controlBg)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Limiter",
                            tint = if (isLimiterEnabled && effectiveEnabled) VuGreen else customColors.textMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "PEAK LIMITER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLimiterEnabled && effectiveEnabled) customColors.textPrimary else customColors.textMuted
                        )
                    }
                    Switch(
                        checked = isLimiterEnabled,
                        onCheckedChange = { onLimiterToggled(it) },
                        enabled = effectiveEnabled,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = customColors.accentContainer,
                            checkedTrackColor = primaryColor,
                            uncheckedThumbColor = customColors.textMuted,
                            uncheckedTrackColor = SliderTrackInactive
                        ),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // ROW 2: STEREO WIDENING & SOUNDSTAGE EXPANSION (NEW FEATURE)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBg)
                .border(1.dp, borderCol, RoundedCornerShape(28.dp))
                .padding(16.dp)
                .testTag("stereo_widening_card"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Stereo Widening",
                        tint = if (effectiveEnabled) primaryColor else customColors.textMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "STEREO WIDENING EFFECT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = customColors.textSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
                Text(
                    text = when {
                        stereoWideningPercent <= 0f -> "OFF (MONO/NATIVE)"
                        stereoWideningPercent < 35f -> "SUBTLE (${stereoWideningPercent.toInt()}%)"
                        stereoWideningPercent < 70f -> "WIDE STAGE (${stereoWideningPercent.toInt()}%)"
                        else -> "IMMERSIVE 3D (${stereoWideningPercent.toInt()}%)"
                    },
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (effectiveEnabled && stereoWideningPercent > 0) primaryColor else customColors.textMuted,
                    fontFamily = FontFamily.Monospace
                )
            }

            Slider(
                value = stereoWideningPercent,
                onValueChange = { onStereoWideningChanged(it) },
                valueRange = 0f..100f,
                enabled = effectiveEnabled,
                colors = SliderDefaults.colors(
                    thumbColor = primaryColor,
                    activeTrackColor = primaryColor,
                    inactiveTrackColor = SliderTrackInactive
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .testTag("stereo_widening_slider")
            )

            // Stereo Width Quick Preset Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    Pair("Off (0%)", 0f),
                    Pair("Subtle (25%)", 25f),
                    Pair("Studio (50%)", 50f),
                    Pair("Concert (75%)", 75f),
                    Pair("Max 3D (100%)", 100f)
                ).forEach { (label, value) ->
                    val isSelected = kotlin.math.abs(stereoWideningPercent - value) < 2f
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected && effectiveEnabled) customColors.accentContainer else customColors.controlBg)
                            .border(
                                0.5.dp,
                                if (isSelected && effectiveEnabled) primaryColor else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = effectiveEnabled) { onStereoWideningChanged(value) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 8.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected && effectiveEnabled) primaryColor else customColors.textMuted
                        )
                    }
                }
            }
        }

        // ROW 3: 3D VIRTUALIZER & STEREO BALANCE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(cardBg)
                .border(1.dp, borderCol, RoundedCornerShape(28.dp))
                .padding(16.dp)
                .testTag("spatial_balance_card"),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 3D Virtualizer Section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SurroundSound,
                            contentDescription = "3D Virtualizer",
                            tint = if (effectiveEnabled) primaryColor else customColors.textMuted,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "3D SPATIAL",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = customColors.textSecondary
                        )
                    }
                    Text(
                        text = "${virtualizerPercent.toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (effectiveEnabled) primaryColor else customColors.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = virtualizerPercent,
                    onValueChange = { onVirtualizerChanged(it) },
                    valueRange = 0f..100f,
                    enabled = effectiveEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = primaryColor,
                        activeTrackColor = primaryColor,
                        inactiveTrackColor = SliderTrackInactive
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("virtualizer_slider")
                )
            }

            // Stereo Balance Section
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "STEREO BALANCE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = customColors.textSecondary
                    )
                    Text(
                        text = when {
                            stereoBalance < -0.05f -> "L ${kotlin.math.abs((stereoBalance * 100).toInt())}%"
                            stereoBalance > 0.05f -> "R ${(stereoBalance * 100).toInt()}%"
                            else -> "CENTER"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (effectiveEnabled) primaryColor else customColors.textMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Slider(
                    value = stereoBalance,
                    onValueChange = { onStereoBalanceChanged(it) },
                    valueRange = -1f..1f,
                    enabled = effectiveEnabled,
                    colors = SliderDefaults.colors(
                        thumbColor = secondaryColor,
                        activeTrackColor = secondaryColor,
                        inactiveTrackColor = SliderTrackInactive
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(28.dp)
                        .testTag("balance_slider")
                )
            }
        }
    }
}

@Composable
fun StudioRotaryKnob(
    value: Float,
    displayValue: String,
    label: String,
    accentColor: Color,
    isEnabled: Boolean,
    onValueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val normalized = value.coerceIn(0f, 1f)
    val startAngle = 135f
    val sweepAngle = 270f
    val currentAngle = startAngle + (normalized * sweepAngle)

    Box(
        modifier = modifier
            .pointerInput(isEnabled) {
                if (!isEnabled) return@pointerInput
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val delta = -dragAmount.y / 200f
                    val newValue = (normalized + delta).coerceIn(0f, 1f)
                    onValueChanged(newValue)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = (size.minDimension / 2f) - 10.dp.toPx()

            // Outer Track (Inactive)
            drawArc(
                color = customColors.controlBg,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2)
            )

            // Active Arc
            if (isEnabled && normalized > 0f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        0.0f to accentColor.copy(alpha = 0.6f),
                        0.75f to accentColor,
                        1.0f to accentColor
                    ),
                    startAngle = startAngle,
                    sweepAngle = normalized * sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2)
                )
            }

            // Knob inner body
            val knobRadius = radius - 8.dp.toPx()
            drawCircle(
                color = customColors.cardElevated,
                radius = knobRadius,
                center = center
            )
            drawCircle(
                color = customColors.border,
                radius = knobRadius,
                center = center,
                style = Stroke(width = 1.dp.toPx())
            )

            // Pointer indicator dot
            val angleRad = Math.toRadians(currentAngle.toDouble())
            val indicatorDist = knobRadius - 6.dp.toPx()
            val indicatorX = center.x + (indicatorDist * cos(angleRad)).toFloat()
            val indicatorY = center.y + (indicatorDist * sin(angleRad)).toFloat()

            drawCircle(
                color = if (isEnabled) accentColor else customColors.textMuted,
                radius = 3.dp.toPx(),
                center = Offset(indicatorX, indicatorY)
            )
        }

        // Center Readout Text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = displayValue,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) accentColor else customColors.textMuted,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Medium,
                color = customColors.textMuted,
                letterSpacing = 0.3.sp
            )
        }
    }
}

@Composable
fun StereoVuMeter(
    leftLevel: Float,
    rightLevel: Float,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val totalSegments = 16

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(customColors.controlBg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Left Channel
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "L",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = customColors.textMuted,
                fontFamily = FontFamily.Monospace
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val litSegments = (leftLevel.coerceIn(0f, 1f) * totalSegments).toInt()
                for (i in 0 until totalSegments) {
                    val isLit = i <= litSegments
                    val color = when {
                        !isLit -> customColors.cardBg
                        i >= totalSegments - 2 -> VuRed
                        i >= totalSegments - 5 -> VuYellow
                        else -> VuGreen
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(color)
                    )
                }
            }
        }

        // Right Channel
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "R",
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = customColors.textMuted,
                fontFamily = FontFamily.Monospace
            )
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                val litSegments = (rightLevel.coerceIn(0f, 1f) * totalSegments).toInt()
                for (i in 0 until totalSegments) {
                    val isLit = i <= litSegments
                    val color = when {
                        !isLit -> customColors.cardBg
                        i >= totalSegments - 2 -> VuRed
                        i >= totalSegments - 5 -> VuYellow
                        else -> VuGreen
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(5.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(color)
                    )
                }
            }
        }
    }
}



