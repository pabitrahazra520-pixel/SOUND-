package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.MediaPlayer
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.Virtualizer
import android.net.Uri
import android.util.Log
import com.example.model.BassPunchMode
import com.example.model.EqualizerSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentTrackTitle: String = "Neon Cyberpunk EDM",
    val currentPositionMs: Long = 0,
    val durationMs: Long = 60000,
    val isCustomFile: Boolean = false,
    val audioSessionId: Int = 0
)

data class SpectrumState(
    val bandLevels: List<Float> = List(12) { 0.05f },
    val leftLevel: Float = 0f,
    val rightLevel: Float = 0f,
    val peakHoldLevels: List<Float> = List(12) { 0.05f },
    val waveform: List<Float> = List(48) { 0f },
    val radialRays: List<Float> = List(24) { 0.05f },
    val energy: Float = 0.05f,
    val phase: Float = 0f
)

enum class DemoTrack(val title: String, val description: String) {
    EDM_BASS("Neon Cyberpunk EDM", "Punchy 40Hz Sub-Bass & 12kHz Synth"),
    TRAP_BEATS("Deep Bassline & Trap Beats", "Heavy 808 Sub-rumble & Hi-hats"),
    ACOUSTIC_STUDIO("Acoustic & Studio Vocals", "Warm Mid-range & Crystal Clarity"),
    FREQUENCY_SWEEP("20Hz - 20kHz Sine Sweep", "Audiophile 12-Band Calibration Sweep")
}

class AudioEngine(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val TAG = "AudioEngine"

    private var mediaPlayer: MediaPlayer? = null
    private var equalizerFx: Equalizer? = null
    private var bassBoostFx: BassBoost? = null
    private var loudnessFx: LoudnessEnhancer? = null
    private var virtualizerFx: Virtualizer? = null

    private var activeSessionId = 0
    private var currentSettings = EqualizerSettings()
    private var isBypassed = false

    private val _playerState = MutableStateFlow(PlayerState())
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _spectrumState = MutableStateFlow(SpectrumState())
    val spectrumState: StateFlow<SpectrumState> = _spectrumState.asStateFlow()

    private var spectrumJob: Job? = null
    private var currentDemoTrack = DemoTrack.EDM_BASS
    private var customAudioUri: Uri? = null

    // Real-time software filters for DSP processing
    private val leftFilters = Array(12) { BiquadFilter() }
    private val rightFilters = Array(12) { BiquadFilter() }
    private val leftBassFilter = BiquadFilter()
    private val rightBassFilter = BiquadFilter()

    private val peakHold = FloatArray(12) { 0f }
    private val currentLevels = FloatArray(12) { 0f }

    init {
        initFilters()
        startSpectrumGenerator()
        setupAudioSession(0)
    }

    private fun initFilters() {
        val sampleRate = 44100.0
        EqualizerSettings.DEFAULT_FREQUENCIES.forEachIndexed { index, freq ->
            leftFilters[index].configurePeaking(sampleRate, freq.toDouble(), 0.0)
            rightFilters[index].configurePeaking(sampleRate, freq.toDouble(), 0.0)
        }
        leftBassFilter.configureLowShelf(sampleRate, 80.0, 0.0)
        rightBassFilter.configureLowShelf(sampleRate, 80.0, 0.0)
    }

    fun setupAudioSession(sessionId: Int) {
        try {
            releaseEffects()
            activeSessionId = sessionId

            if (sessionId >= 0) {
                // Initialize Hardware Equalizer
                try {
                    equalizerFx = Equalizer(0, sessionId).apply {
                        enabled = currentSettings.isEnabled && !isBypassed
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Hardware Equalizer init failed: ${e.message}")
                }

                // Initialize BassBoost
                try {
                    bassBoostFx = BassBoost(0, sessionId).apply {
                        if (strengthSupported) {
                            val strength = (currentSettings.bassBoostPercent * 10).toInt().coerceIn(0, 1000).toShort()
                            setStrength(strength)
                            enabled = currentSettings.isEnabled && !isBypassed
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Hardware BassBoost init failed: ${e.message}")
                }

                // Initialize LoudnessEnhancer (Master Gain boost)
                try {
                    loudnessFx = LoudnessEnhancer(sessionId).apply {
                        val gainMb = (max(0f, currentSettings.masterGainDb) * 100).toInt()
                        setTargetGain(gainMb)
                        enabled = currentSettings.isEnabled && !isBypassed
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Hardware LoudnessEnhancer init failed: ${e.message}")
                }

                // Initialize Virtualizer
                try {
                    virtualizerFx = Virtualizer(0, sessionId).apply {
                        if (strengthSupported) {
                            val strength = (currentSettings.virtualizerPercent * 10).toInt().coerceIn(0, 1000).toShort()
                            setStrength(strength)
                            enabled = currentSettings.isEnabled && !isBypassed
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Hardware Virtualizer init failed: ${e.message}")
                }

                applySettingsToHardwareEffects(currentSettings)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error setting up audio session: ${e.message}")
        }
    }

    fun applySettings(settings: EqualizerSettings, bypassed: Boolean = false) {
        currentSettings = settings
        isBypassed = bypassed

        val sampleRate = 44100.0
        val effectiveEnabled = settings.isEnabled && !isBypassed

        // Update DSP Biquad Filters
        settings.bands.forEachIndexed { index, band ->
            val gain = if (effectiveEnabled) band.gainDb.toDouble() else 0.0
            leftFilters[index].configurePeaking(sampleRate, band.centerFreqHz.toDouble(), gain)
            rightFilters[index].configurePeaking(sampleRate, band.centerFreqHz.toDouble(), gain)
        }

        // Bass filter
        val bassGain = if (effectiveEnabled) {
            (settings.bassBoostPercent / 100f * 15f).toDouble()
        } else 0.0
        leftBassFilter.configureLowShelf(sampleRate, settings.bassCutoffHz.toDouble(), bassGain)
        rightBassFilter.configureLowShelf(sampleRate, settings.bassCutoffHz.toDouble(), bassGain)

        // Hardware effects update
        applySettingsToHardwareEffects(settings)
    }

    private fun applySettingsToHardwareEffects(settings: EqualizerSettings) {
        val effectiveEnabled = settings.isEnabled && !isBypassed

        try {
            bassBoostFx?.let { bb ->
                bb.enabled = effectiveEnabled
                if (bb.strengthSupported && effectiveEnabled) {
                    val strength = (settings.bassBoostPercent * 10).toInt().coerceIn(0, 1000).toShort()
                    bb.setStrength(strength)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "BassBoost apply failed: ${e.message}")
        }

        try {
            loudnessFx?.let { le ->
                le.enabled = effectiveEnabled
                if (effectiveEnabled) {
                    val gainMb = (max(0f, settings.masterGainDb) * 100).toInt()
                    le.setTargetGain(gainMb)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "LoudnessEnhancer apply failed: ${e.message}")
        }

        try {
            virtualizerFx?.let { vz ->
                vz.enabled = effectiveEnabled
                if (vz.strengthSupported && effectiveEnabled) {
                    // Combine virtualizer and stereo widening effect
                    val combinedWidth = ((settings.virtualizerPercent * 0.6f + settings.stereoWideningPercent * 0.4f) * 10f).toInt().coerceIn(0, 1000).toShort()
                    vz.setStrength(combinedWidth)
                    vz.enabled = true
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Virtualizer apply failed: ${e.message}")
        }

        try {
            equalizerFx?.let { eq ->
                eq.enabled = effectiveEnabled
                if (effectiveEnabled) {
                    val numBands = eq.numberOfBands.toInt()
                    val (minLevel, maxLevel) = eq.bandLevelRange.let { it[0] to it[1] }

                    for (i in 0 until numBands) {
                        val centerFreq = eq.getCenterFreq(i.toShort()) / 1000 // to Hz
                        // Find closest band in 12 bands
                        val closestBand = settings.bands.minByOrNull { abs(it.centerFreqHz - centerFreq) }
                        if (closestBand != null) {
                            val mappedLevel = ((closestBand.gainDb / 15f) * maxLevel).toInt().toShort()
                                .coerceIn(minLevel, maxLevel)
                            eq.setBandLevel(i.toShort(), mappedLevel)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Equalizer apply failed: ${e.message}")
        }
    }

    fun playDemoTrack(track: DemoTrack) {
        currentDemoTrack = track
        customAudioUri = null
        stopPlayer()

        scope.launch(Dispatchers.IO) {
            try {
                val demoFile = generateDemoAudioFile(track)
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(demoFile.absolutePath)
                    isLooping = true
                    prepare()
                }

                mediaPlayer = mp
                setupAudioSession(mp.audioSessionId)

                mp.start()
                _playerState.value = PlayerState(
                    isPlaying = true,
                    currentTrackTitle = track.title,
                    currentPositionMs = 0,
                    durationMs = mp.duration.toLong(),
                    isCustomFile = false,
                    audioSessionId = mp.audioSessionId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error playing demo track: ${e.message}", e)
            }
        }
    }

    fun playCustomAudio(uri: Uri, title: String) {
        customAudioUri = uri
        stopPlayer()

        scope.launch(Dispatchers.IO) {
            try {
                val mp = MediaPlayer().apply {
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
                    )
                    setDataSource(context, uri)
                    isLooping = true
                    prepare()
                }

                mediaPlayer = mp
                setupAudioSession(mp.audioSessionId)

                mp.start()
                _playerState.value = PlayerState(
                    isPlaying = true,
                    currentTrackTitle = title,
                    currentPositionMs = 0,
                    durationMs = mp.duration.toLong(),
                    isCustomFile = true,
                    audioSessionId = mp.audioSessionId
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error playing custom audio: ${e.message}", e)
            }
        }
    }

    fun togglePlayPause() {
        val mp = mediaPlayer
        if (mp != null) {
            if (mp.isPlaying) {
                mp.pause()
                _playerState.value = _playerState.value.copy(isPlaying = false)
            } else {
                mp.start()
                _playerState.value = _playerState.value.copy(isPlaying = true)
            }
        } else {
            // Start default demo track
            playDemoTrack(currentDemoTrack)
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { mp ->
            try {
                mp.seekTo(positionMs.toInt())
                _playerState.value = _playerState.value.copy(currentPositionMs = positionMs)
            } catch (e: Exception) {
                Log.w(TAG, "Seek failed: ${e.message}")
            }
        }
    }

    fun stopPlayer() {
        try {
            mediaPlayer?.let { mp ->
                if (mp.isPlaying) {
                    mp.stop()
                }
                mp.release()
            }
            mediaPlayer = null
            _playerState.value = _playerState.value.copy(isPlaying = false, currentPositionMs = 0)
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping player: ${e.message}")
        }
    }

    private fun startSpectrumGenerator() {
        spectrumJob?.cancel()
        spectrumJob = scope.launch(Dispatchers.Default) {
            var timeStep = 0f
            val waveformSize = 48
            val radialRaysCount = 24

            while (isActive) {
                val isPlaying = mediaPlayer?.isPlaying == true
                val baseEnergy = if (isPlaying) 0.68f else 0.05f

                timeStep += 0.16f
                val bandCount = 12

                // Calculate dynamic band levels based on current EQ gains, bass boost, stereo widening
                val newLevels = MutableList(bandCount) { i ->
                    val band = currentSettings.bands[i]
                    val gainFactor = if (currentSettings.isEnabled && !isBypassed) {
                        val bassExtra = if (i <= 2) (currentSettings.bassBoostPercent / 100f * 6.5f) else 0f
                        val widthExtra = if (i in 4..9) (currentSettings.stereoWideningPercent / 100f * 2.0f) else 0f
                        val gainDb = band.gainDb + bassExtra + widthExtra
                        10f.pow(gainDb / 20f)
                    } else {
                        1f
                    }

                    if (isPlaying) {
                        // Dynamic rhythmic pulse
                        val beat = sin(timeStep * (1.2f + i * 0.15f)) * 0.35f + 0.55f
                        val subRumble = if (i <= 2) sin(timeStep * 3.2f) * 0.28f else 0f
                        val randJitter = (Random.nextFloat() - 0.5f) * 0.14f
                        val rawLevel = (beat + subRumble + randJitter) * baseEnergy * gainFactor.coerceIn(0.2f, 2.5f)
                        rawLevel.coerceIn(0.08f, 0.98f)
                    } else {
                        0.05f + (Random.nextFloat() * 0.02f)
                    }
                }

                // Peak hold decay
                for (i in 0 until bandCount) {
                    if (newLevels[i] > peakHold[i]) {
                        peakHold[i] = newLevels[i]
                    } else {
                        peakHold[i] = max(0.05f, peakHold[i] - 0.03f)
                    }
                    currentLevels[i] = newLevels[i]
                }

                // Calculate L / R VU meter levels
                val leftFactor = (1f - currentSettings.stereoBalance.coerceAtLeast(0f))
                val rightFactor = (1f + currentSettings.stereoBalance.coerceAtMost(0f))
                val avgLevel = newLevels.average().toFloat()
                val masterGainFactor = 10f.pow(currentSettings.masterGainDb / 20f).coerceIn(0.2f, 2.0f)

                val leftLevel = (avgLevel * leftFactor * masterGainFactor).coerceIn(0f, 1f)
                val rightLevel = (avgLevel * rightFactor * masterGainFactor).coerceIn(0f, 1f)

                // Generate real-time Oscilloscope Waveform points (-1.0 to +1.0)
                val waveformPoints = List(waveformSize) { idx ->
                    if (isPlaying) {
                        val progress = idx.toFloat() / waveformSize
                        val f1 = sin((progress * 4f * PI.toFloat()) + timeStep * 3f) * 0.45f
                        val f2 = sin((progress * 10f * PI.toFloat()) - timeStep * 4.5f) * 0.25f
                        val f3 = if (currentSettings.bassBoostPercent > 20f) sin((progress * 2f * PI.toFloat()) + timeStep * 2f) * 0.35f else 0f
                        val noise = (Random.nextFloat() - 0.5f) * 0.08f
                        val sample = (f1 + f2 + f3 + noise) * avgLevel * masterGainFactor
                        sample.coerceIn(-0.95f, 0.95f)
                    } else {
                        (sin((idx.toFloat() / waveformSize * 2f * PI.toFloat()) + timeStep) * 0.04f)
                    }
                }

                // Generate Radial Visualizer Rays (0.0 to 1.0)
                val radialRays = List(radialRaysCount) { rayIdx ->
                    val bandMapIdx = (rayIdx % bandCount)
                    val baseBand = newLevels[bandMapIdx]
                    val angleFactor = sin(rayIdx.toFloat() / radialRaysCount * 2f * PI.toFloat() + timeStep * 1.5f) * 0.2f
                    val wideningSpread = if (rayIdx % 2 == 0) (currentSettings.stereoWideningPercent / 100f * 0.15f) else 0f
                    ((baseBand + angleFactor + wideningSpread) * masterGainFactor).coerceIn(0.08f, 1f)
                }

                _spectrumState.value = SpectrumState(
                    bandLevels = newLevels,
                    leftLevel = leftLevel,
                    rightLevel = rightLevel,
                    peakHoldLevels = peakHold.toList(),
                    waveform = waveformPoints,
                    radialRays = radialRays,
                    energy = avgLevel,
                    phase = timeStep
                )

                // Update position
                mediaPlayer?.let { mp ->
                    try {
                        if (mp.isPlaying) {
                            _playerState.value = _playerState.value.copy(
                                currentPositionMs = mp.currentPosition.toLong(),
                                durationMs = mp.duration.toLong()
                            )
                        }
                    } catch (_: Exception) {}
                }

                delay(33) // ~30 fps
            }
        }
    }

    private fun generateDemoAudioFile(track: DemoTrack): File {
        val file = File(context.cacheDir, "demo_${track.name}.wav")
        if (file.exists() && file.length() > 1000) return file

        val sampleRate = 44100
        val durationSeconds = 16 // 16s seamless musical loop
        val numSamples = sampleRate * durationSeconds
        val numChannels = 2
        val sampleSize = 2 // 16-bit PCM

        val pcmData = ByteArray(numSamples * numChannels * sampleSize)
        val bpm = 124.0
        val beatDuration = 60.0 / bpm

        var bufferIndex = 0

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val beatTime = (t % beatDuration) / beatDuration
            val barTime = (t % (beatDuration * 4)) / (beatDuration * 4)

            var sampleL = 0.0
            var sampleR = 0.0

            when (track) {
                DemoTrack.EDM_BASS -> {
                    // Kick drum (heavy 45Hz sub bass with punchy transient)
                    val kickEnvelope = exp(-beatTime * 14.0)
                    val kickFreq = 120.0 * exp(-beatTime * 20.0) + 42.0
                    val kick = sin(2.0 * PI * kickFreq * t) * kickEnvelope

                    // Rolling Bassline (80Hz - 160Hz)
                    val bassNote = when (((t / (beatDuration / 2)).toInt()) % 8) {
                        0, 1 -> 55.0 // A1
                        2, 3 -> 65.4 // C2
                        4, 5 -> 73.4 // D2
                        else -> 49.0 // G1
                    }
                    val bassEnvelope = ((1.0 - beatTime * 0.8)).coerceIn(0.0, 1.0)
                    val bass = (sin(2.0 * PI * bassNote * t) + 0.4 * sin(2.0 * PI * bassNote * 2.0 * t)) * bassEnvelope * 0.4

                    // Synth Arp (500Hz - 2kHz)
                    val arpStep = ((t / (beatDuration / 4)).toInt()) % 16
                    val arpFreq = 220.0 * (2.0.pow(arpStep % 8 / 12.0))
                    val synth = sin(2.0 * PI * arpFreq * t) * 0.15 * (1.0 - (t % (beatDuration / 4)) / (beatDuration / 4))

                    // Hi-Hat (10kHz - 16kHz white noise shimmer on off-beats)
                    val offBeat = ((beatTime + 0.5) % 1.0)
                    val hatEnvelope = exp(-offBeat * 25.0)
                    val hatNoise = (Random.nextDouble() * 2.0 - 1.0) * hatEnvelope * 0.12

                    sampleL = kick * 0.7 + bass * 0.6 + synth * 0.4 + hatNoise * 0.5
                    sampleR = kick * 0.7 + bass * 0.6 + synth * 0.3 + hatNoise * 0.6
                }

                DemoTrack.TRAP_BEATS -> {
                    // Deep 808 Sub Boom (35Hz)
                    val subEnv = exp(-barTime * 3.5)
                    val sub = sin(2.0 * PI * 36.0 * t + sin(2.0 * PI * 18.0 * t) * 0.2) * subEnv * 0.8

                    // Snare on beat 2 & 4
                    val isSnare = ((t / beatDuration).toInt() % 2 == 1)
                    val snareEnv = if (isSnare) exp(-beatTime * 18.0) else 0.0
                    val snare = (sin(2.0 * PI * 220.0 * t) * 0.4 + (Random.nextDouble() * 2.0 - 1.0) * 0.6) * snareEnv * 0.5

                    // Fast 16kHz Trap Hi-Hat rolls
                    val fastBeat = (t % (beatDuration / 4)) / (beatDuration / 4)
                    val trapHiHat = (Random.nextDouble() * 2.0 - 1.0) * exp(-fastBeat * 30.0) * 0.18

                    sampleL = sub * 0.8 + snare * 0.5 + trapHiHat * 0.4
                    sampleR = sub * 0.8 + snare * 0.5 + trapHiHat * 0.6
                }

                DemoTrack.ACOUSTIC_STUDIO -> {
                    // Warm acoustic guitar chords + gentle percussive groove
                    val chordRoot = when (((t / (beatDuration * 2)).toInt()) % 4) {
                        0 -> 130.81 // C3
                        1 -> 164.81 // E3
                        2 -> 174.61 // F3
                        else -> 196.00 // G3
                    }
                    val strumTime = (t % (beatDuration / 2)) / (beatDuration / 2)
                    val guitarEnv = exp(-strumTime * 5.0)
                    val guitar = (sin(2.0 * PI * chordRoot * t) +
                            0.6 * sin(2.0 * PI * chordRoot * 1.5 * t) +
                            0.4 * sin(2.0 * PI * chordRoot * 2.0 * t) +
                            0.2 * sin(2.0 * PI * chordRoot * 3.0 * t)) * guitarEnv * 0.35

                    // Vocal hum harmony in mid range (800Hz - 2.5kHz)
                    val vocal = sin(2.0 * PI * (chordRoot * 2.0) * t) * 0.2 * (sin(2.0 * PI * 0.5 * t) * 0.2 + 0.8)

                    sampleL = guitar * 0.6 + vocal * 0.4
                    sampleR = guitar * 0.5 + vocal * 0.5
                }

                DemoTrack.FREQUENCY_SWEEP -> {
                    // Continuous Logarithmic Frequency Sine Sweep from 20 Hz to 20,000 Hz
                    val sweepProgress = (t % 8.0) / 8.0 // 8s sweep loop
                    val freq = 20.0 * (1000.0.pow(sweepProgress)) // 20Hz -> 20,000Hz
                    val sweepTone = sin(2.0 * PI * freq * t) * 0.5
                    sampleL = sweepTone
                    sampleR = sweepTone
                }
            }

            // Master Limiter / Clipping preventer
            val clippedL = sampleL.coerceIn(-0.95, 0.95)
            val clippedR = sampleR.coerceIn(-0.95, 0.95)

            val shortL = (clippedL * 32767).toInt().toShort()
            val shortR = (clippedR * 32767).toInt().toShort()

            pcmData[bufferIndex++] = (shortL.toInt() and 0xFF).toByte()
            pcmData[bufferIndex++] = ((shortL.toInt() shr 8) and 0xFF).toByte()
            pcmData[bufferIndex++] = (shortR.toInt() and 0xFF).toByte()
            pcmData[bufferIndex++] = ((shortR.toInt() shr 8) and 0xFF).toByte()
        }

        writeWavFile(file, pcmData, sampleRate, numChannels, sampleSize * 8)
        return file
    }

    private fun writeWavFile(file: File, pcmData: ByteArray, sampleRate: Int, channels: Int, bitsPerSample: Int) {
        val totalAudioLen = pcmData.size.toLong()
        val totalDataLen = totalAudioLen + 36
        val byteRate = (sampleRate * channels * bitsPerSample / 8).toLong()

        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = ((totalDataLen shr 8) and 0xff).toByte()
        header[6] = ((totalDataLen shr 16) and 0xff).toByte()
        header[7] = ((totalDataLen shr 24) and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte()
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // PCM
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (sampleRate and 0xff).toByte()
        header[25] = ((sampleRate shr 8) and 0xff).toByte()
        header[26] = ((sampleRate shr 16) and 0xff).toByte()
        header[27] = ((sampleRate shr 24) and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = ((byteRate shr 8) and 0xff).toByte()
        header[30] = ((byteRate shr 16) and 0xff).toByte()
        header[31] = ((byteRate shr 24) and 0xff).toByte()
        header[32] = (channels * bitsPerSample / 8).toByte() // block align
        header[33] = 0
        header[34] = bitsPerSample.toByte()
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = ((totalAudioLen shr 8) and 0xff).toByte()
        header[42] = ((totalAudioLen shr 16) and 0xff).toByte()
        header[43] = ((totalAudioLen shr 24) and 0xff).toByte()

        FileOutputStream(file).use { out ->
            out.write(header)
            out.write(pcmData)
        }
    }

    private fun releaseEffects() {
        try {
            equalizerFx?.release()
            equalizerFx = null
        } catch (_: Exception) {}
        try {
            bassBoostFx?.release()
            bassBoostFx = null
        } catch (_: Exception) {}
        try {
            loudnessFx?.release()
            loudnessFx = null
        } catch (_: Exception) {}
        try {
            virtualizerFx?.release()
            virtualizerFx = null
        } catch (_: Exception) {}
    }

    fun release() {
        spectrumJob?.cancel()
        stopPlayer()
        releaseEffects()
    }
}
