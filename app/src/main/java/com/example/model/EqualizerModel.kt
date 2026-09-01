package com.example.model

data class EqBand(
    val id: Int,
    val centerFreqHz: Int,
    val label: String,
    val gainDb: Float = 0f, // -15.0f to +15.0f dB
    val minGainDb: Float = -15f,
    val maxGainDb: Float = 15f
) {
    val displayGain: String
        get() = when {
            gainDb > 0 -> "+${String.format("%.1f", gainDb)} dB"
            gainDb < 0 -> "${String.format("%.1f", gainDb)} dB"
            else -> "0.0 dB"
        }
}

enum class BassPunchMode(val title: String) {
    DEEP("Deep Sub"),
    PUNCH("Punchy"),
    HARMONIC("Harmonics")
}

enum class VisualizerStyle(val title: String, val iconName: String) {
    CURVE("Curve", "Graphic Spline"),
    SPECTRUM("Spectrum", "12-Band RTA"),
    WAVEFORM("Waveform", "Oscilloscope"),
    RADIAL("Radial", "Circular Ring"),
    ENERGY("Energy", "Particle Matrix")
}

enum class AppThemeMode(val title: String) {
    SYSTEM("Auto System"),
    DARK("Studio Dark"),
    AMOLED("Pure AMOLED"),
    LIGHT("Studio Light")
}

data class EqualizerSettings(
    val isEnabled: Boolean = true,
    val bands: List<EqBand> = DEFAULT_12_BANDS,
    val bassBoostPercent: Float = 35f, // 0 to 100%
    val bassCutoffHz: Int = 80, // 50, 80, 120, 160 Hz
    val bassPunchMode: BassPunchMode = BassPunchMode.PUNCH,
    val masterGainDb: Float = 0f, // -12f to +15f dB
    val isLimiterEnabled: Boolean = true,
    val stereoBalance: Float = 0f, // -1.0f (Left) to 1.0f (Right)
    val virtualizerPercent: Float = 20f, // 0 to 100%
    val stereoWideningPercent: Float = 30f, // 0 to 100% (Stereo stage expansion)
    val visualizerStyle: VisualizerStyle = VisualizerStyle.CURVE,
    val themeMode: AppThemeMode = AppThemeMode.DARK,
    val selectedPresetName: String = "Flat"
) {
    companion object {
        val DEFAULT_FREQUENCIES = listOf(
            32, 64, 125, 250, 500, 1000, 2000, 4000, 8000, 12000, 16000, 20000
        )

        fun formatFreqLabel(freqHz: Int): String {
            return if (freqHz >= 1000) {
                "${freqHz / 1000}k"
            } else {
                "${freqHz}"
            }
        }

        val DEFAULT_12_BANDS = DEFAULT_FREQUENCIES.mapIndexed { index, freq ->
            EqBand(
                id = index,
                centerFreqHz = freq,
                label = formatFreqLabel(freq),
                gainDb = 0f
            )
        }
    }
}
