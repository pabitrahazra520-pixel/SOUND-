package com.example.audio

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Standard Audio EQ Biquad Filter (Robert Bristow-Johnson Audio EQ Cookbook)
 */
class BiquadFilter {
    private var b0 = 1.0
    private var b1 = 0.0
    private var b2 = 0.0
    private var a1 = 0.0
    private var a2 = 0.0

    private var x1 = 0.0
    private var x2 = 0.0
    private var y1 = 0.0
    private var y2 = 0.0

    fun reset() {
        x1 = 0.0
        x2 = 0.0
        y1 = 0.0
        y2 = 0.0
    }

    fun configurePeaking(sampleRate: Double, centerFreqHz: Double, gainDb: Double, q: Double = 1.414) {
        if (sampleRate <= 0.0 || centerFreqHz <= 0.0) return
        val w0 = 2.0 * PI * (centerFreqHz.coerceIn(10.0, sampleRate * 0.49)) / sampleRate
        val a = 10.0.pow(gainDb / 40.0)
        val alpha = sin(w0) / (2.0 * q.coerceAtLeast(0.1))
        val cosW0 = cos(w0)

        val a0 = 1.0 + alpha / a
        b0 = (1.0 + alpha * a) / a0
        b1 = (-2.0 * cosW0) / a0
        b2 = (1.0 - alpha * a) / a0
        a1 = (-2.0 * cosW0) / a0
        a2 = (1.0 - alpha / a) / a0
    }

    fun configureLowShelf(sampleRate: Double, cutoffFreqHz: Double, gainDb: Double, slope: Double = 1.0) {
        if (sampleRate <= 0.0 || cutoffFreqHz <= 0.0) return
        val w0 = 2.0 * PI * (cutoffFreqHz.coerceIn(10.0, sampleRate * 0.49)) / sampleRate
        val a = 10.0.pow(gainDb / 40.0)
        val alpha = (sin(w0) / 2.0) * sqrt((a + 1.0 / a) * (1.0 / slope - 1.0) + 2.0)
        val cosW0 = cos(w0)
        val sqrt2aAlpha = 2.0 * sqrt(a) * alpha

        val a0 = (a + 1.0) + (a - 1.0) * cosW0 + sqrt2aAlpha
        b0 = (a * ((a + 1.0) - (a - 1.0) * cosW0 + sqrt2aAlpha)) / a0
        b1 = (2.0 * a * ((a - 1.0) - (a + 1.0) * cosW0)) / a0
        b2 = (a * ((a + 1.0) - (a - 1.0) * cosW0 - sqrt2aAlpha)) / a0
        a1 = (-2.0 * ((a - 1.0) + (a + 1.0) * cosW0)) / a0
        a2 = ((a + 1.0) + (a - 1.0) * cosW0 - sqrt2aAlpha) / a0
    }

    fun process(input: Double): Double {
        val output = b0 * input + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
        x2 = x1
        x1 = input
        y2 = y1
        y1 = output
        return output
    }
}
