package com.pixelclassics.app.audio

import kotlin.math.PI
import kotlin.math.sin

/**
 * Loops a baked PCM melody buffer — browser twin of the Android/desktop
 * versions. Same render math; Web Audio loops the buffer natively.
 */
class MelodyPlayer(
    private val notesBeats: List<Pair<Float, Float>>,   // (Hz, beats); 0 Hz = rest
    private val bpm: Float = 140f,
    private val volume: Float = 0.05f,
    private val sampleRate: Int = 22050,
) {
    var enabled: Boolean = true

    private val buffer: FloatArray = render()
    private var source: AudioBufferSourceNode? = null

    private fun render(): FloatArray {
        val beatSec = 60f / bpm
        val totalBeats = notesBeats.sumOf { it.second.toDouble() }.toFloat()
        val totalSamples = (totalBeats * beatSec * sampleRate).toInt()
        val buf = FloatArray(totalSamples)
        var sampleCursor = 0
        for ((freq, beats) in notesBeats) {
            val durSamples = (beats * beatSec * sampleRate).toInt()
            if (freq <= 0f) { sampleCursor += durSamples; continue }
            val twoPiF = 2.0 * PI * freq
            val attack = (sampleRate * 0.010f).toInt()      // 10 ms attack
            val release = (sampleRate * 0.040f).toInt()     // 40 ms release
            for (i in 0 until durSamples) {
                if (sampleCursor + i >= totalSamples) break
                val t = i.toDouble() / sampleRate
                val s = if (sin(twoPiF * t) >= 0.0) 1.0 else -1.0
                val envAttack = (i.toFloat() / attack).coerceIn(0f, 1f)
                val envRelease = ((durSamples - i).toFloat() / release).coerceIn(0f, 1f)
                val env = envAttack * envRelease
                buf[sampleCursor + i] = (s * 0.4 * env * volume).toFloat().coerceIn(-1f, 1f)
            }
            sampleCursor += durSamples
        }
        return buf
    }

    fun start() {
        if (!enabled) return
        stop()
        source = WebAudio.loop(buffer, sampleRate)
    }

    fun stop() {
        try { source?.stop() } catch (_: Throwable) {}
        source = null
    }
}

object Melodies {
    /** Original upbeat arcade loop (Brick Buster / Arkanoid). C-major, 132 BPM. */
    val Arcade: List<Pair<Float, Float>> = run {
        val C5 = 523.25f; val D5 = 587.33f; val E5 = 659.25f; val F5 = 698.46f
        val G5 = 783.99f; val A5 = 880f; val C6 = 1046.5f
        listOf(
            C5 to 0.5f, E5 to 0.5f, G5 to 0.5f, C6 to 0.5f, A5 to 0.5f, G5 to 0.5f, E5 to 1f,
            F5 to 0.5f, A5 to 0.5f, C6 to 0.5f, A5 to 0.5f, G5 to 0.5f, E5 to 0.5f, D5 to 1f,
            C5 to 0.5f, D5 to 0.5f, E5 to 0.5f, F5 to 0.5f, G5 to 1f, E5 to 0.5f, C5 to 0.5f,
            G5 to 0.5f, F5 to 0.5f, E5 to 0.5f, D5 to 0.5f, C5 to 1f, 0f to 1f,
        )
    }

    /** Korobeiniki (Тетрис). A-minor, 140 BPM. */
    val Korobeiniki: List<Pair<Float, Float>> = run {
        val A4 = 440f; val B4 = 493.88f; val C5 = 523.25f
        val D5 = 587.33f; val E5 = 659.25f; val F5 = 698.46f
        val G5 = 783.99f; val A5 = 880f
        listOf(
            E5 to 1f, B4 to 0.5f, C5 to 0.5f, D5 to 1f, C5 to 0.5f, B4 to 0.5f,
            A4 to 1f, A4 to 0.5f, C5 to 0.5f, E5 to 1f, D5 to 0.5f, C5 to 0.5f,
            B4 to 1f, B4 to 0.5f, C5 to 0.5f, D5 to 1f, E5 to 1f,
            C5 to 1f, A4 to 1f, A4 to 1f, 0f to 1f,
            D5 to 1f, F5 to 0.5f, A5 to 1f, G5 to 0.5f, F5 to 0.5f,
            E5 to 1f, C5 to 0.5f, E5 to 1f, D5 to 0.5f, C5 to 0.5f,
            B4 to 1f, B4 to 0.5f, C5 to 0.5f, D5 to 1f, E5 to 1f,
            C5 to 1f, A4 to 1f, A4 to 1f, 0f to 1f,
        )
    }
}
