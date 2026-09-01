package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqBand
import com.example.ui.theme.DeepVioletContainer
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.SliderTrackInactive
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioControlBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

@Composable
fun Equalizer12BandFaders(
    bands: List<EqBand>,
    isEqEnabled: Boolean,
    isBypassed: Boolean,
    onBandGainChanged: (bandId: Int, gainDb: Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(StudioCardBg)
            .border(1.dp, StudioBorder, RoundedCornerShape(28.dp))
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .testTag("equalizer_12_band_faders")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "12-BAND GRAPHIC EQUALIZER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Double-tap to 0 dB",
                fontSize = 10.sp,
                color = TextMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            bands.forEach { band ->
                BandVerticalFader(
                    band = band,
                    isEnabled = isEqEnabled && !isBypassed,
                    onGainChanged = { gain -> onBandGainChanged(band.id, gain) }
                )
            }
        }
    }
}

@Composable
fun BandVerticalFader(
    band: EqBand,
    isEnabled: Boolean,
    onGainChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val isBoost = band.gainDb > 0.2f
    val isCut = band.gainDb < -0.2f
    val isModified = isBoost || isCut
    val activeColor = when {
        !isEnabled -> TextMuted
        isModified -> LavenderAccent
        else -> SoftViolet
    }

    Column(
        modifier = modifier
            .width(52.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(StudioControlBg)
            .padding(vertical = 10.dp, horizontal = 2.dp)
            .testTag("band_fader_${band.centerFreqHz}"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gain value badge
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isEnabled && isModified) DeepVioletContainer else StudioCardElevated)
                .border(
                    0.5.dp,
                    if (isEnabled && isModified) LavenderAccent.copy(alpha = 0.6f) else Color.Transparent,
                    RoundedCornerShape(6.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (isEnabled) {
                    when {
                        band.gainDb > 0 -> "+${String.format(Locale.US, "%.1f", band.gainDb)}"
                        band.gainDb < 0 -> String.format(Locale.US, "%.1f", band.gainDb)
                        else -> "0.0"
                    }
                } else "0.0",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = if (isEnabled) (if (isModified) LavenderAccent else TextPrimary) else TextMuted,
                fontFamily = FontFamily.Monospace,
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Vertical Fader Track & Thumb
        BoxWithConstraints(
            modifier = Modifier
                .width(42.dp)
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            val trackHeight = maxHeight
            // Calculate thumb position: gainDb is -15..+15 -> normalized 0..1 (0 is +15dB top, 1 is -15dB bottom)
            val normalizedPos = (15f - band.gainDb) / 30f

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(40.dp)
                    .pointerInput(band.id, isEnabled) {
                        if (!isEnabled) return@pointerInput
                        detectTapGestures(
                            onDoubleTap = {
                                onGainChanged(0f)
                            },
                            onTap = { offset ->
                                val norm = (offset.y / size.height).coerceIn(0f, 1f)
                                val newGain = 15f - (norm * 30f)
                                onGainChanged(newGain.coerceIn(-15f, 15f))
                            }
                        )
                    }
                    .pointerInput(band.id, isEnabled) {
                        if (!isEnabled) return@pointerInput
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaNorm = dragAmount.y / size.height
                            val deltaGain = -(deltaNorm * 30f)
                            val newGain = (band.gainDb + deltaGain).coerceIn(-15f, 15f)
                            onGainChanged(newGain)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                // Background Track Slot
                Box(
                    modifier = Modifier
                        .width(6.dp)
                        .height(trackHeight - 20.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SliderTrackInactive)
                )

                // 0 dB center tick mark
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(1.5.dp)
                        .background(TextMuted.copy(alpha = 0.6f))
                )

                // Level fill from center
                val fillHeightFactor = kotlin.math.abs(band.gainDb) / 15f
                val isAboveCenter = band.gainDb >= 0
                if (isEnabled && fillHeightFactor > 0.02f) {
                    Box(
                        modifier = Modifier
                            .width(6.dp)
                            .height((trackHeight - 20.dp) / 2f * fillHeightFactor)
                            .align(Alignment.Center)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = if (isAboveCenter) listOf(LavenderAccent, SoftViolet) else listOf(SoftViolet, DeepVioletContainer)
                                )
                            )
                    )
                }

                // Fader Thumb Handle
                val thumbOffset = ((normalizedPos - 0.5f) * (trackHeight.value - 36.dp.value)).dp
                Box(
                    modifier = Modifier
                        .size(width = 34.dp, height = 22.dp)
                        .align(Alignment.Center)
                        .padding(top = thumbOffset)
                        .shadow(4.dp, RoundedCornerShape(8.dp))
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    StudioCardElevated,
                                    Color(0xFF282A2E),
                                    Color(0xFF1E2023)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            if (isEnabled && isModified) LavenderAccent else StudioBorder,
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    // Center grip line / accent
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) {
                            Box(
                                modifier = Modifier
                                    .width(1.5.dp)
                                    .height(10.dp)
                                    .background(if (isEnabled && isModified) LavenderAccent else TextMuted)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Frequency Label (32Hz, 64Hz, etc.)
        Text(
            text = band.label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (isEnabled) TextPrimary else TextMuted,
            textAlign = TextAlign.Center
        )
        Text(
            text = if (band.centerFreqHz >= 1000) "kHz" else "Hz",
            fontSize = 8.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

