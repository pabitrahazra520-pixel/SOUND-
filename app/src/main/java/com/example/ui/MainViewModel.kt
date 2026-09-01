package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.audio.DemoTrack
import com.example.audio.PlayerState
import com.example.audio.SpectrumState
import com.example.data.AppDatabase
import com.example.data.PresetEntity
import com.example.data.PresetRepository
import com.example.model.BassPunchMode
import com.example.model.EqualizerSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = PresetRepository(database.presetDao())
    val audioEngine = AudioEngine(application, viewModelScope)

    val allPresets: StateFlow<List<PresetEntity>> = repository.allPresets
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _settings = MutableStateFlow(EqualizerSettings())
    val settings: StateFlow<EqualizerSettings> = _settings.asStateFlow()

    private val _isBypassed = MutableStateFlow(false)
    val isBypassed: StateFlow<Boolean> = _isBypassed.asStateFlow()

    val playerState: StateFlow<PlayerState> = audioEngine.playerState
    val spectrumState: StateFlow<SpectrumState> = audioEngine.spectrumState

    init {
        viewModelScope.launch {
            repository.ensureDefaultPresets()
        }
        audioEngine.applySettings(_settings.value, _isBypassed.value)
    }

    fun setBandGain(bandId: Int, gainDb: Float) {
        val updatedBands = _settings.value.bands.map { band ->
            if (band.id == bandId) {
                band.copy(gainDb = gainDb.coerceIn(-15f, 15f))
            } else {
                band
            }
        }
        val newSettings = _settings.value.copy(
            bands = updatedBands,
            selectedPresetName = "Custom"
        )
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setBassBoost(percent: Float) {
        val newSettings = _settings.value.copy(
            bassBoostPercent = percent.coerceIn(0f, 100f),
            selectedPresetName = if (_settings.value.selectedPresetName.startsWith("Bass")) _settings.value.selectedPresetName else "Custom"
        )
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setBassCutoff(cutoffHz: Int) {
        val newSettings = _settings.value.copy(bassCutoffHz = cutoffHz)
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setBassPunchMode(mode: BassPunchMode) {
        val newSettings = _settings.value.copy(bassPunchMode = mode)
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setMasterGain(gainDb: Float) {
        val newSettings = _settings.value.copy(masterGainDb = gainDb.coerceIn(-12f, 15f))
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setLimiterEnabled(enabled: Boolean) {
        val newSettings = _settings.value.copy(isLimiterEnabled = enabled)
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setStereoBalance(balance: Float) {
        val newSettings = _settings.value.copy(stereoBalance = balance.coerceIn(-1f, 1f))
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setVirtualizer(percent: Float) {
        val newSettings = _settings.value.copy(virtualizerPercent = percent.coerceIn(0f, 100f))
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setStereoWidening(percent: Float) {
        val newSettings = _settings.value.copy(stereoWideningPercent = percent.coerceIn(0f, 100f))
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setVisualizerStyle(style: com.example.model.VisualizerStyle) {
        _settings.value = _settings.value.copy(visualizerStyle = style)
    }

    fun setThemeMode(mode: com.example.model.AppThemeMode) {
        _settings.value = _settings.value.copy(themeMode = mode)
    }

    fun toggleEqEnabled() {
        val newSettings = _settings.value.copy(isEnabled = !_settings.value.isEnabled)
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun setBypassed(bypassed: Boolean) {
        _isBypassed.value = bypassed
        audioEngine.applySettings(_settings.value, bypassed)
    }

    fun resetToFlat() {
        val flatBands = EqualizerSettings.DEFAULT_12_BANDS
        val newSettings = _settings.value.copy(
            bands = flatBands,
            bassBoostPercent = 0f,
            masterGainDb = 0f,
            stereoBalance = 0f,
            virtualizerPercent = 0f,
            stereoWideningPercent = 0f,
            selectedPresetName = "Flat"
        )
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun selectPreset(preset: PresetEntity) {
        val newBands = preset.toBands()
        val newSettings = _settings.value.copy(
            bands = newBands,
            bassBoostPercent = preset.bassBoost,
            bassCutoffHz = preset.bassCutoff,
            masterGainDb = preset.masterGain,
            virtualizerPercent = preset.virtualizer,
            stereoWideningPercent = preset.stereoWidening,
            selectedPresetName = preset.name
        )
        _settings.value = newSettings
        audioEngine.applySettings(newSettings, _isBypassed.value)
    }

    fun saveCustomPreset(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val entity = PresetEntity.fromBands(
                name = name.trim(),
                bands = _settings.value.bands,
                bassBoost = _settings.value.bassBoostPercent,
                bassCutoff = _settings.value.bassCutoffHz,
                masterGain = _settings.value.masterGainDb,
                virtualizer = _settings.value.virtualizerPercent,
                stereoWidening = _settings.value.stereoWideningPercent,
                isCustom = true
            )
            repository.insert(entity)
            _settings.value = _settings.value.copy(selectedPresetName = name.trim())
        }
    }

    fun renameCustomPreset(preset: PresetEntity, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            val updated = preset.copy(name = newName.trim())
            repository.update(updated)
            if (_settings.value.selectedPresetName == preset.name) {
                _settings.value = _settings.value.copy(selectedPresetName = newName.trim())
            }
        }
    }

    fun overwritePresetWithCurrent(preset: PresetEntity) {
        viewModelScope.launch {
            val updated = PresetEntity.fromBands(
                name = preset.name,
                bands = _settings.value.bands,
                bassBoost = _settings.value.bassBoostPercent,
                bassCutoff = _settings.value.bassCutoffHz,
                masterGain = _settings.value.masterGainDb,
                virtualizer = _settings.value.virtualizerPercent,
                stereoWidening = _settings.value.stereoWideningPercent,
                isCustom = preset.isCustom
            ).copy(id = preset.id)
            repository.update(updated)
            _settings.value = _settings.value.copy(selectedPresetName = preset.name)
        }
    }

    fun deleteCustomPreset(presetId: Int) {
        viewModelScope.launch {
            repository.deleteCustomPreset(presetId)
            if (_settings.value.selectedPresetName != "Flat") {
                _settings.value = _settings.value.copy(selectedPresetName = "Custom")
            }
        }
    }

    fun playDemoTrack(track: DemoTrack) {
        audioEngine.playDemoTrack(track)
    }

    fun playCustomAudio(uri: Uri, title: String) {
        audioEngine.playCustomAudio(uri, title)
    }

    fun togglePlayPause() {
        audioEngine.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        audioEngine.seekTo(positionMs)
    }

    fun setAudioSessionId(sessionId: Int) {
        audioEngine.setupAudioSession(sessionId)
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.release()
    }
}
