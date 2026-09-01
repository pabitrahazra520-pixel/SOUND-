package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.BassPunchMode
import com.example.model.EqBand
import com.example.model.EqualizerSettings

@Entity(tableName = "presets")
data class PresetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val bandGains: String, // "0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0"
    val bassBoost: Float = 0f,
    val bassCutoff: Int = 80,
    val masterGain: Float = 0f,
    val virtualizer: Float = 0f,
    val stereoWidening: Float = 25f,
    val isCustom: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toBands(): List<EqBand> {
        val gains = bandGains.split(",").mapNotNull { it.trim().toFloatOrNull() }
        val freqs = EqualizerSettings.DEFAULT_FREQUENCIES
        return freqs.mapIndexed { index, freq ->
            val gain = if (index < gains.size) gains[index] else 0f
            EqBand(
                id = index,
                centerFreqHz = freq,
                label = EqualizerSettings.formatFreqLabel(freq),
                gainDb = gain.coerceIn(-15f, 15f)
            )
        }
    }

    companion object {
        fun fromBands(
            name: String,
            bands: List<EqBand>,
            bassBoost: Float = 0f,
            bassCutoff: Int = 80,
            masterGain: Float = 0f,
            virtualizer: Float = 0f,
            stereoWidening: Float = 25f,
            isCustom: Boolean = true
        ): PresetEntity {
            val gainsStr = bands.joinToString(",") { String.format(java.util.Locale.US, "%.1f", it.gainDb) }
            return PresetEntity(
                name = name,
                bandGains = gainsStr,
                bassBoost = bassBoost,
                bassCutoff = bassCutoff,
                masterGain = masterGain,
                virtualizer = virtualizer,
                stereoWidening = stereoWidening,
                isCustom = isCustom
            )
        }
    }
}
