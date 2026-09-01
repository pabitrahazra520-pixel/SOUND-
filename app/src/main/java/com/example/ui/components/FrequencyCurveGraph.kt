package com.example.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.SpectrumState
import com.example.model.EqBand
import com.example.model.VisualizerStyle
import com.example.ui.theme.DeepVioletContainer
import com.example.ui.theme.GridLine
import com.example.ui.theme.LavenderAccent
import com.example.ui.theme.LocalCustomColors
import com.example.ui.theme.SoftViolet
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCardBg
import com.example.ui.theme.StudioCardElevated
import com.example.ui.theme.StudioControlBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.VuGreen
import com.example.ui.theme.VuRed
import com.example.ui.theme.VuYellow
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

@Composable
fun FrequencyCurveGraph(
    bands: List<EqBand>,
    spectrumState: SpectrumState,
    visualizerStyle: VisualizerStyle,
    isEqEnabled: Boolean,
    isBypassed: Boolean,
    onStyleSelected: (VisualizerStyle) -> Unit,
    modifier: Modifier = Modifier
) {
    val customColors = LocalCustomColors.current
    val effectiveEnabled = isEqEnabled && !isBypassed
    val primaryColor = customColors.accent
    val secondaryColor = SoftViolet
    val cardBg = customColors.cardBg
    val borderCol = customColors.border

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(cardBg)
            .border(1.dp, borderCol, RoundedCornerShape(28.dp))
            .padding(14.dp)
            .testTag("frequency_curve_graph")
    ) {
        // TOP HEADER: Visualizer Title & Style Selector Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (effectiveEnabled) VuGreen else customColors.textMuted)
                )
                Text(
                    text = "REAL-TIME AUDIO VISUALIZER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = customColors.textSecondary,
                    letterSpacing = 0.6.sp
                )
            }

            // Visualizer Style Switcher Chips
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                VisualizerStyle.values().forEach { style ->
                    val isSelected = visualizerStyle == style
                    Box(
                        modifier = Modifier
                            .height(26.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) customColors.accentContainer else customColors.controlBg)
                            .border(
                                0.5.dp,
                                if (isSelected) primaryColor.copy(alpha = 0.8f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { onStyleSelected(style) }
                            .padding(horizontal = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = style.title,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) primaryColor else customColors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // MAIN VISUALIZER CANVAS (Switches smoothly between styles)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(customColors.controlBg)
                .border(0.5.dp, customColors.border, RoundedCornerShape(18.dp))
        ) {
            Crossfade(targetState = visualizerStyle, label = "visualizer_crossfade") { style ->
                when (style) {
                    VisualizerStyle.CURVE -> CurveVisualizerView(
                        bands = bands,
                        spectrumLevels = spectrumState.bandLevels,
                        effectiveEnabled = effectiveEnabled,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        customColors = customColors
                    )
                    VisualizerStyle.SPECTRUM -> SpectrumRtaVisualizerView(
                        bands = bands,
                        spectrumLevels = spectrumState.bandLevels,
                        peakHoldLevels = spectrumState.peakHoldLevels,
                        effectiveEnabled = effectiveEnabled,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        customColors = customColors
                    )
                    VisualizerStyle.WAVEFORM -> OscilloscopeWaveformView(
                        waveform = spectrumState.waveform,
                        effectiveEnabled = effectiveEnabled,
                        leftLevel = spectrumState.leftLevel,
                        rightLevel = spectrumState.rightLevel,
                        primaryColor = primaryColor,
                        customColors = customColors
                    )
                    VisualizerStyle.RADIAL -> RadialSpectrumVisualizerView(
                        radialRays = spectrumState.radialRays,
                        energy = spectrumState.energy,
                        phase = spectrumState.phase,
                        effectiveEnabled = effectiveEnabled,
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        customColors = customColors
                    )
                    VisualizerStyle.ENERGY -> EnergyMatrixVisualizerView(
                        bandLevels = spectrumState.bandLevels,
                        leftLevel = spectrumState.leftLevel,
                        rightLevel = spectrumState.rightLevel,
                        effectiveEnabled = effectiveEnabled,
                        primaryColor = primaryColor,
                        customColors = customColors
                    )
                }
            }

            // Status Badge Overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(end = 10.dp, top = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (effectiveEnabled) customColors.accentContainer.copy(alpha = 0.85f) else customColors.cardElevated)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (!isEqEnabled) "EQ OFF" else if (isBypassed) "BYPASS" else "${visualizerStyle.title.uppercase()} ACTIVE",
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (!isEqEnabled || isBypassed) customColors.textMuted else primaryColor,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}

@Composable
private fun CurveVisualizerView(
    bands: List<EqBand>,
    spectrumLevels: List<Float>,
    effectiveEnabled: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    customColors: com.example.ui.theme.CustomThemeColors
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val paddingX = 22.dp.toPx()
        val paddingY = 18.dp.toPx()
        val graphWidth = width - (paddingX * 2)
        val graphHeight = height - (paddingY * 2)
        val centerY = paddingY + (graphHeight / 2f)

        // Grid lines (-15, -10, -5, 0, +5, +10, +15)
        val dbSteps = listOf(15, 10, 5, 0, -5, -10, -15)
        dbSteps.forEach { db ->
            val y = centerY - ((db / 15f) * (graphHeight / 2f))
            val isZero = db == 0
            val lineColor = if (isZero) GridLine.copy(alpha = 0.8f) else GridLine.copy(alpha = 0.35f)
            val strokeW = if (isZero) 1.2.dp.toPx() else 0.6.dp.toPx()

            drawLine(
                color = lineColor,
                start = Offset(paddingX, y),
                end = Offset(width - paddingX, y),
                strokeWidth = strokeW,
                pathEffect = if (!isZero) PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f) else null
            )
        }

        // Spectrum bars in background
        val numBars = spectrumLevels.size
        if (numBars > 0) {
            val barSpacing = graphWidth / numBars
            for (i in 0 until numBars) {
                val rawLevel = spectrumLevels.getOrElse(i) { 0.05f }
                val level = if (effectiveEnabled) rawLevel else (rawLevel * 0.25f)
                val barX = paddingX + (i * barSpacing) + (barSpacing / 2f)
                val barHeight = (level.coerceIn(0f, 1f) * (graphHeight * 0.85f))
                val barTop = paddingY + graphHeight - barHeight

                val barBrush = Brush.verticalGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.4f),
                        secondaryColor.copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    startY = barTop,
                    endY = paddingY + graphHeight
                )

                drawRoundRect(
                    brush = barBrush,
                    topLeft = Offset(barX - (barSpacing * 0.32f), barTop),
                    size = Size(barSpacing * 0.64f, barHeight),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
            }
        }

        // 12 EQ Band points
        val numBands = bands.size
        val points = ArrayList<Offset>()
        for (i in 0 until numBands) {
            val band = bands[i]
            val x = paddingX + (i.toFloat() / (numBands - 1).coerceAtLeast(1)) * graphWidth
            val gain = if (effectiveEnabled) band.gainDb else 0f
            val y = centerY - ((gain / 15f) * (graphHeight / 2f))
            points.add(Offset(x, y))
        }

        if (points.isNotEmpty()) {
            val curvePath = Path()
            val fillPath = Path()

            curvePath.moveTo(points[0].x, points[0].y)
            fillPath.moveTo(points[0].x, centerY)
            fillPath.lineTo(points[0].x, points[0].y)

            for (i in 0 until points.size - 1) {
                val p0 = points[max(0, i - 1)]
                val p1 = points[i]
                val p2 = points[i + 1]
                val p3 = points[min(points.size - 1, i + 2)]

                val cp1x = p1.x + (p2.x - p0.x) / 5f
                val cp1y = p1.y + (p2.y - p0.y) / 5f
                val cp2x = p2.x - (p3.x - p1.x) / 5f
                val cp2y = p2.y - (p3.y - p1.y) / 5f

                curvePath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
                fillPath.cubicTo(cp1x, cp1y, cp2x, cp2y, p2.x, p2.y)
            }

            fillPath.lineTo(points.last().x, centerY)
            fillPath.close()

            // Fill gradient
            val gradientBrush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (effectiveEnabled) 0.28f else 0.05f),
                    secondaryColor.copy(alpha = if (effectiveEnabled) 0.10f else 0.02f),
                    Color.Transparent
                ),
                startY = paddingY,
                endY = paddingY + graphHeight
            )
            drawPath(fillPath, gradientBrush)

            // Main Spline Stroke
            val curveColor = if (effectiveEnabled) primaryColor else customColors.textMuted
            if (effectiveEnabled) {
                drawPath(
                    curvePath,
                    color = curveColor.copy(alpha = 0.35f),
                    style = Stroke(width = 5.dp.toPx())
                )
            }
            drawPath(
                curvePath,
                color = curveColor,
                style = Stroke(width = 2.5.dp.toPx())
            )

            // Band control nodes
            points.forEachIndexed { index, pt ->
                val band = bands[index]
                val isBoosted = effectiveEnabled && band.gainDb > 0.5f
                val isCut = effectiveEnabled && band.gainDb < -0.5f
                val nodeColor = when {
                    !effectiveEnabled -> customColors.textMuted
                    isBoosted || isCut -> primaryColor
                    else -> secondaryColor
                }

                drawCircle(
                    color = nodeColor.copy(alpha = 0.25f),
                    radius = 5.dp.toPx(),
                    center = pt
                )
                drawCircle(
                    color = nodeColor,
                    radius = 3.dp.toPx(),
                    center = pt
                )
            }
        }
    }
}

@Composable
private fun SpectrumRtaVisualizerView(
    bands: List<EqBand>,
    spectrumLevels: List<Float>,
    peakHoldLevels: List<Float>,
    effectiveEnabled: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    customColors: com.example.ui.theme.CustomThemeColors
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val paddingX = 16.dp.toPx()
        val paddingY = 16.dp.toPx()
        val graphWidth = width - (paddingX * 2)
        val graphHeight = height - (paddingY * 2)
        val numBars = 12
        val barWidth = graphWidth / numBars

        // Horizontal Grid Lines
        repeat(5) { step ->
            val y = paddingY + (graphHeight / 4f) * step
            drawLine(
                color = GridLine.copy(alpha = 0.4f),
                start = Offset(paddingX, y),
                end = Offset(width - paddingX, y),
                strokeWidth = 0.8.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
            )
        }

        // Draw 12 Spectrum Bars + Peak Hold Markers
        for (i in 0 until numBars) {
            val rawLevel = spectrumLevels.getOrElse(i) { 0.05f }
            val peakLevel = peakHoldLevels.getOrElse(i) { 0.05f }
            val level = if (effectiveEnabled) rawLevel else (rawLevel * 0.25f)
            val pLevel = if (effectiveEnabled) peakLevel else (peakLevel * 0.25f)

            val barX = paddingX + (i * barWidth) + (barWidth * 0.12f)
            val barW = barWidth * 0.76f
            val barH = (level.coerceIn(0f, 1f) * graphHeight).coerceAtLeast(3.dp.toPx())
            val barTop = paddingY + graphHeight - barH

            // Bar Gradient
            val barBrush = Brush.verticalGradient(
                colors = listOf(
                    primaryColor,
                    secondaryColor,
                    customColors.accentContainer
                ),
                startY = barTop,
                endY = paddingY + graphHeight
            )

            drawRoundRect(
                brush = barBrush,
                topLeft = Offset(barX, barTop),
                size = Size(barW, barH),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
            )

            // Peak Hold Falling Marker
            val peakY = paddingY + graphHeight - (pLevel.coerceIn(0f, 1f) * graphHeight)
            drawRoundRect(
                color = if (effectiveEnabled) primaryColor else customColors.textMuted,
                topLeft = Offset(barX, peakY - 2.dp.toPx()),
                size = Size(barW, 2.5.dp.toPx()),
                cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
            )
        }
    }
}

@Composable
private fun OscilloscopeWaveformView(
    waveform: List<Float>,
    effectiveEnabled: Boolean,
    leftLevel: Float,
    rightLevel: Float,
    primaryColor: Color,
    customColors: com.example.ui.theme.CustomThemeColors
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val paddingX = 14.dp.toPx()
        val paddingY = 14.dp.toPx()
        val graphWidth = width - (paddingX * 2)
        val graphHeight = height - (paddingY * 2)
        val centerY = paddingY + (graphHeight / 2f)

        // Oscilloscope Green/Violet Center Crosshairs & Circular Grid
        drawLine(
            color = GridLine.copy(alpha = 0.7f),
            start = Offset(paddingX, centerY),
            end = Offset(width - paddingX, centerY),
            strokeWidth = 1.dp.toPx()
        )
        drawLine(
            color = GridLine.copy(alpha = 0.5f),
            start = Offset(width / 2f, paddingY),
            end = Offset(width / 2f, height - paddingY),
            strokeWidth = 1.dp.toPx()
        )

        if (waveform.isNotEmpty()) {
            val wavePath = Path()
            val stepX = graphWidth / (waveform.size - 1).coerceAtLeast(1)

            waveform.forEachIndexed { i, sample ->
                val x = paddingX + (i * stepX)
                val amp = if (effectiveEnabled) sample else (sample * 0.2f)
                val y = centerY - (amp * (graphHeight / 2f * 0.9f))

                if (i == 0) {
                    wavePath.moveTo(x, y)
                } else {
                    wavePath.lineTo(x, y)
                }
            }

            // Waveform Glow Layer
            if (effectiveEnabled) {
                drawPath(
                    wavePath,
                    color = primaryColor.copy(alpha = 0.4f),
                    style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Main Waveform Beam
            drawPath(
                wavePath,
                color = if (effectiveEnabled) primaryColor else customColors.textMuted,
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
private fun RadialSpectrumVisualizerView(
    radialRays: List<Float>,
    energy: Float,
    phase: Float,
    effectiveEnabled: Boolean,
    primaryColor: Color,
    secondaryColor: Color,
    customColors: com.example.ui.theme.CustomThemeColors
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val center = Offset(width / 2f, height / 2f)
        val baseRadius = (min(width, height) / 2f) * 0.42f
        val rayCount = radialRays.size

        // Center Pulsating Core
        val pulseFactor = if (effectiveEnabled) (energy * 10.dp.toPx()) else 0f
        val coreRadius = baseRadius * 0.45f + pulseFactor

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primaryColor.copy(alpha = if (effectiveEnabled) 0.5f else 0.1f),
                    secondaryColor.copy(alpha = if (effectiveEnabled) 0.2f else 0.05f),
                    Color.Transparent
                ),
                center = center,
                radius = coreRadius * 1.5f
            ),
            radius = coreRadius * 1.5f,
            center = center
        )

        drawCircle(
            color = if (effectiveEnabled) primaryColor else customColors.textMuted,
            radius = coreRadius,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Outward Radial Frequency Rays
        for (i in 0 until rayCount) {
            val angle = ((i.toFloat() / rayCount) * 2f * PI + phase * 0.4f).toFloat()
            val rayAmp = radialRays.getOrElse(i) { 0.1f }
            val rayLength = (baseRadius * 0.9f) * (if (effectiveEnabled) rayAmp else 0.1f)

            val startX = center.x + (coreRadius + 3.dp.toPx()) * cos(angle)
            val startY = center.y + (coreRadius + 3.dp.toPx()) * sin(angle)
            val endX = center.x + (coreRadius + 3.dp.toPx() + rayLength) * cos(angle)
            val endY = center.y + (coreRadius + 3.dp.toPx() + rayLength) * sin(angle)

            drawLine(
                color = if (effectiveEnabled) (if (i % 2 == 0) primaryColor else secondaryColor) else customColors.textMuted,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = 3.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun EnergyMatrixVisualizerView(
    bandLevels: List<Float>,
    leftLevel: Float,
    rightLevel: Float,
    effectiveEnabled: Boolean,
    primaryColor: Color,
    customColors: com.example.ui.theme.CustomThemeColors
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val paddingX = 18.dp.toPx()
        val paddingY = 16.dp.toPx()
        val graphWidth = width - (paddingX * 2)
        val graphHeight = height - (paddingY * 2)
        val cols = 12
        val rows = 8
        val colWidth = graphWidth / cols
        val rowHeight = graphHeight / rows

        for (c in 0 until cols) {
            val raw = bandLevels.getOrElse(c) { 0.05f }
            val level = if (effectiveEnabled) raw else (raw * 0.25f)
            val activeRows = (level.coerceIn(0f, 1f) * rows).toInt()

            for (r in 0 until rows) {
                val blockY = paddingY + graphHeight - ((r + 1) * rowHeight)
                val blockX = paddingX + (c * colWidth) + (colWidth * 0.15f)
                val blockW = colWidth * 0.7f
                val blockH = rowHeight * 0.75f
                val isLit = r < activeRows

                val blockColor = when {
                    !isLit -> customColors.cardElevated.copy(alpha = 0.4f)
                    r >= 6 -> VuRed
                    r >= 4 -> VuYellow
                    else -> primaryColor
                }

                drawRoundRect(
                    color = blockColor,
                    topLeft = Offset(blockX, blockY),
                    size = Size(blockW, blockH),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}


