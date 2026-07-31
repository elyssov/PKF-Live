package com.pixelclassics.app.audio

import kotlinx.browser.window
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin
import kotlin.random.Random

/**
 * Tiny PC-Speaker-style synth — browser twin of the Android AudioTrack and
 * desktop javax.sound versions. SAME synth math, same named SFX palette, so
 * the games sound identical on every platform; only the output stage is
 * Web Audio here.
 */
class SoundManager {
    var enabled: Boolean = true
    private val sampleRate = 22050

    enum class Wave { SQUARE, SINE, TRIANGLE, SAWTOOTH }

    fun beep(
        freq: Float,
        duration: Float,
        wave: Wave = Wave.SQUARE,
        vol: Float = 0.08f,
    ) {
        if (!enabled || duration <= 0f || vol <= 0f) return
        val samples = (sampleRate * duration).toInt().coerceAtLeast(64)
        val buf = FloatArray(samples)
        val twoPiF = 2.0 * PI * freq
        val k = if (vol > 0f) ln((vol.toDouble()) / 0.001).coerceAtLeast(1.0) / duration else 0.0
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val phase = twoPiF * t
            val s = when (wave) {
                Wave.SINE -> sin(phase)
                Wave.SQUARE -> if (sin(phase) >= 0.0) 1.0 else -1.0
                Wave.TRIANGLE -> {
                    val frac = (freq * t) - kotlin.math.floor(freq * t + 0.5)
                    4.0 * kotlin.math.abs(frac) - 1.0
                }
                Wave.SAWTOOTH -> 2.0 * ((freq * t) - kotlin.math.floor(0.5 + freq * t))
            }
            val env = vol * exp(-k * t)
            buf[i] = (s * env).toFloat().coerceIn(-1f, 1f)
        }
        WebAudio.play(buf, sampleRate)
    }

    fun noise(duration: Float, vol: Float = 0.06f) {
        if (!enabled || duration <= 0f || vol <= 0f) return
        val samples = (sampleRate * duration).toInt().coerceAtLeast(64)
        val buf = FloatArray(samples)
        val k = if (vol > 0f) ln((vol.toDouble()) / 0.001).coerceAtLeast(1.0) / duration else 0.0
        for (i in 0 until samples) {
            val t = i.toDouble() / sampleRate
            val s = Random.nextDouble(-1.0, 1.0) * 0.5
            val env = vol * exp(-k * t)
            buf[i] = (s * env).toFloat().coerceIn(-1f, 1f)
        }
        WebAudio.play(buf, sampleRate)
    }

    // ───── Named SFX (mirror of the Android SoundManager) ───────────────

    fun paddleHit() = beep(440f, 0.05f, Wave.SQUARE, 0.10f)
    fun wallHit() = beep(220f, 0.03f, Wave.SQUARE, 0.06f)
    fun score() = beep(880f, 0.15f, Wave.SINE, 0.10f)

    fun eat() = beep(660f, 0.06f, Wave.SQUARE, 0.08f)
    fun die() { beep(200f, 0.20f, Wave.SAWTOOTH, 0.10f); delayed(150) { beep(100f, 0.30f, Wave.SAWTOOTH, 0.08f) } }

    fun move() = beep(300f, 0.02f, Wave.SQUARE, 0.04f)
    fun rotate() = beep(500f, 0.03f, Wave.SQUARE, 0.06f)
    fun drop() = beep(150f, 0.06f, Wave.TRIANGLE, 0.08f)
    fun lineClear() {
        beep(523f, 0.08f, Wave.SQUARE, 0.10f)
        delayed(80) { beep(659f, 0.08f, Wave.SQUARE, 0.10f) }
        delayed(160) { beep(784f, 0.12f, Wave.SQUARE, 0.10f) }
    }
    fun tetris() {
        for (i in 0 until 4) delayed(i * 70) { beep(523f + i * 130f, 0.10f, Wave.SQUARE, 0.12f) }
    }
    fun levelUp() {
        beep(440f, 0.10f, Wave.SINE, 0.10f)
        delayed(100) { beep(660f, 0.10f, Wave.SINE, 0.10f) }
        delayed(200) { beep(880f, 0.15f, Wave.SINE, 0.12f) }
    }

    fun shoot() = beep(800f, 0.04f, Wave.SAWTOOTH, 0.06f)
    fun explosion() = noise(0.30f, 0.10f)
    fun bigExplosion() { noise(0.50f, 0.15f); beep(60f, 0.40f, Wave.SINE, 0.08f) }
    fun missileWarn() = beep(180f, 0.08f, Wave.SAWTOOTH, 0.04f)
    fun cityDestroyed() { noise(0.40f, 0.12f); beep(80f, 0.50f, Wave.SINE, 0.10f) }

    fun brickHit() = beep(520f, 0.04f, Wave.SQUARE, 0.07f)
    fun brickBreak() = beep(700f, 0.06f, Wave.SQUARE, 0.09f)
    fun paddleBounce() = beep(350f, 0.03f, Wave.TRIANGLE, 0.08f)
    fun loseLife() {
        beep(300f, 0.15f, Wave.SAWTOOTH, 0.10f)
        delayed(120) { beep(150f, 0.20f, Wave.SAWTOOTH, 0.08f) }
    }
    fun powerUp() {
        beep(440f, 0.06f, Wave.SINE, 0.10f)
        delayed(60) { beep(880f, 0.10f, Wave.SINE, 0.10f) }
    }

    fun click() = beep(600f, 0.02f, Wave.SQUARE, 0.05f)
    fun flag() = beep(400f, 0.04f, Wave.TRIANGLE, 0.06f)
    fun boom() { noise(0.60f, 0.20f); beep(50f, 0.50f, Wave.SINE, 0.10f) }
    fun win() {
        for (i in 0 until 5) delayed(i * 100) { beep(440f + i * 110f, 0.12f, Wave.SINE, 0.10f) }
    }

    fun stomp() = beep(55f, 0.15f, Wave.SINE, 0.12f)
    fun crunch() { noise(0.15f, 0.10f); beep(80f, 0.10f, Wave.SAWTOOTH, 0.08f) }

    fun gameOver() {
        beep(400f, 0.20f, Wave.SAWTOOTH, 0.10f)
        delayed(200) { beep(300f, 0.20f, Wave.SAWTOOTH, 0.10f) }
        delayed(400) { beep(200f, 0.40f, Wave.SAWTOOTH, 0.10f) }
    }

    fun menuSelect() = beep(500f, 0.04f, Wave.SQUARE, 0.06f)

    fun pcBeep() = beep(1000f, 0.06f, Wave.SQUARE, 0.05f)
    fun pcClick() = beep(2000f, 0.015f, Wave.SQUARE, 0.04f)
    fun pcError() {
        beep(200f, 0.15f, Wave.SQUARE, 0.06f)
        delayed(180) { beep(200f, 0.15f, Wave.SQUARE, 0.06f) }
    }
    fun pcBoot() {
        beep(1000f, 0.20f, Wave.SQUARE, 0.05f)
        delayed(250) { beep(500f, 0.10f, Wave.SQUARE, 0.04f) }
    }
    fun pcKey() = beep(1500f, 0.01f, Wave.SQUARE, 0.03f)

    private fun delayed(ms: Int, block: () -> Unit) {
        window.setTimeout({ block(); null }, ms)
    }
}
