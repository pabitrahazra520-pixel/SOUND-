package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.model.AppThemeMode

private val StudioDarkColorScheme = darkColorScheme(
    primary = LavenderAccent,
    onPrimary = DeepVioletContainer,
    primaryContainer = DeepVioletContainer,
    onPrimaryContainer = LavenderAccent,
    secondary = SoftViolet,
    onSecondary = Color(0xFF1E005E),
    secondaryContainer = Color(0xFF4A4458),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = BassAmber,
    onTertiary = Color(0xFF332D41),
    background = StudioDarkBg,
    onBackground = TextPrimary,
    surface = StudioCardBg,
    onSurface = TextPrimary,
    surfaceVariant = StudioCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = Color(0xFF44474A),
    outlineVariant = StudioBorder
)

private val AmoledDarkColorScheme = darkColorScheme(
    primary = LavenderAccent,
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1F004D),
    onPrimaryContainer = LavenderAccent,
    secondary = SoftViolet,
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF181818),
    onSecondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFFFFFFFF),
    onTertiary = Color(0xFF000000),
    background = AmoledDarkBg,
    onBackground = Color(0xFFFFFFFF),
    surface = AmoledCardBg,
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = AmoledCardElevated,
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF383838),
    outlineVariant = AmoledBorder
)

private val StudioLightColorScheme = lightColorScheme(
    primary = LightLavenderAccent,
    onPrimary = Color.White,
    primaryContainer = LightDeepVioletContainer,
    onPrimaryContainer = LightLavenderAccent,
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DEF8),
    onSecondaryContainer = Color(0xFF1D192B),
    tertiary = Color(0xFF7D5260),
    onTertiary = Color.White,
    background = StudioLightBg,
    onBackground = LightTextPrimary,
    surface = StudioLightCardBg,
    onSurface = LightTextPrimary,
    surfaceVariant = StudioLightCardElevated,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFCAC4D0),
    outlineVariant = StudioLightBorder
)

data class CustomThemeColors(
    val bg: Color,
    val cardBg: Color,
    val cardElevated: Color,
    val controlBg: Color,
    val border: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val accent: Color,
    val accentContainer: Color,
    val isDark: Boolean
)

val LocalCustomColors = staticCompositionLocalOf {
    CustomThemeColors(
        bg = StudioDarkBg,
        cardBg = StudioCardBg,
        cardElevated = StudioCardElevated,
        controlBg = StudioControlBg,
        border = StudioBorder,
        textPrimary = TextPrimary,
        textSecondary = TextSecondary,
        textMuted = TextMuted,
        accent = LavenderAccent,
        accentContainer = DeepVioletContainer,
        isDark = true
    )
}

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.DARK,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.SYSTEM -> systemDark
        AppThemeMode.DARK, AppThemeMode.AMOLED -> true
        AppThemeMode.LIGHT -> false
    }

    val (colorScheme, customColors) = when (themeMode) {
        AppThemeMode.AMOLED -> Pair(
            AmoledDarkColorScheme,
            CustomThemeColors(
                bg = AmoledDarkBg,
                cardBg = AmoledCardBg,
                cardElevated = AmoledCardElevated,
                controlBg = AmoledControlBg,
                border = AmoledBorder,
                textPrimary = Color(0xFFFFFFFF),
                textSecondary = Color(0xFFD4D4D8),
                textMuted = Color(0xFFA1A1AA),
                accent = LavenderAccent,
                accentContainer = Color(0xFF2B1254),
                isDark = true
            )
        )
        AppThemeMode.LIGHT -> Pair(
            StudioLightColorScheme,
            CustomThemeColors(
                bg = StudioLightBg,
                cardBg = StudioLightCardBg,
                cardElevated = StudioLightCardElevated,
                controlBg = StudioLightControlBg,
                border = StudioLightBorder,
                textPrimary = LightTextPrimary,
                textSecondary = LightTextSecondary,
                textMuted = LightTextMuted,
                accent = LightLavenderAccent,
                accentContainer = LightDeepVioletContainer,
                isDark = false
            )
        )
        AppThemeMode.DARK -> Pair(
            StudioDarkColorScheme,
            CustomThemeColors(
                bg = StudioDarkBg,
                cardBg = StudioCardBg,
                cardElevated = StudioCardElevated,
                controlBg = StudioControlBg,
                border = StudioBorder,
                textPrimary = TextPrimary,
                textSecondary = TextSecondary,
                textMuted = TextMuted,
                accent = LavenderAccent,
                accentContainer = DeepVioletContainer,
                isDark = true
            )
        )
        AppThemeMode.SYSTEM -> if (systemDark) {
            Pair(
                StudioDarkColorScheme,
                CustomThemeColors(
                    bg = StudioDarkBg,
                    cardBg = StudioCardBg,
                    cardElevated = StudioCardElevated,
                    controlBg = StudioControlBg,
                    border = StudioBorder,
                    textPrimary = TextPrimary,
                    textSecondary = TextSecondary,
                    textMuted = TextMuted,
                    accent = LavenderAccent,
                    accentContainer = DeepVioletContainer,
                    isDark = true
                )
            )
        } else {
            Pair(
                StudioLightColorScheme,
                CustomThemeColors(
                    bg = StudioLightBg,
                    cardBg = StudioLightCardBg,
                    cardElevated = StudioLightCardElevated,
                    controlBg = StudioLightControlBg,
                    border = StudioLightBorder,
                    textPrimary = LightTextPrimary,
                    textSecondary = LightTextSecondary,
                    textMuted = LightTextMuted,
                    accent = LightLavenderAccent,
                    accentContainer = LightDeepVioletContainer,
                    isDark = false
                )
            )
        }
    }

    CompositionLocalProvider(LocalCustomColors provides customColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}



