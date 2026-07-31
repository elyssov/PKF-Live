package com.pixelclassics.app.audio

import org.khronos.webgl.Float32Array
import org.khronos.webgl.set

/**
 * The one Web Audio doorway for the whole arcade: a single AudioContext,
 * PCM FloatArrays in, sound out. External declarations map straight onto
 * the browser globals.
 */
external class AudioContext {
    val destination: AudioDestinationNode
    val state: String
    fun createBuffer(numberOfChannels: Int, length: Int, sampleRate: Float): AudioBuffer
    fun createBufferSource(): AudioBufferSourceNode
    fun resume()
}

external class AudioDestinationNode

external class AudioBuffer {
    fun copyToChannel(source: Float32Array, channelNumber: Int)
}

external class AudioBufferSourceNode {
    var buffer: AudioBuffer?
    var loop: Boolean
    fun connect(destination: AudioDestinationNode)
    fun start()
    fun stop()
}

object WebAudio {
    private var ctx: AudioContext? = null

    private fun context(): AudioContext {
        val c = ctx ?: AudioContext().also { ctx = it }
        // Autoplay policy: the context wakes on the first user-gesture-driven
        // sound; resume() is a no-op when already running.
        if (c.state == "suspended") c.resume()
        return c
    }

    private fun toJs(samples: FloatArray): Float32Array {
        val arr = Float32Array(samples.size)
        for (i in samples.indices) arr[i] = samples[i]
        return arr
    }

    fun play(samples: FloatArray, sampleRate: Int) {
        try {
            val c = context()
            val buf = c.createBuffer(1, samples.size, sampleRate.toFloat())
            buf.copyToChannel(toJs(samples), 0)
            val src = c.createBufferSource()
            src.buffer = buf
            src.connect(c.destination)
            src.start()
        } catch (_: Throwable) {
            // Audio failure is non-fatal — the game silently keeps running.
        }
    }

    /** Looping source for melodies; returns a handle to stop. */
    fun loop(samples: FloatArray, sampleRate: Int): AudioBufferSourceNode? =
        try {
            val c = context()
            val buf = c.createBuffer(1, samples.size, sampleRate.toFloat())
            buf.copyToChannel(toJs(samples), 0)
            val src = c.createBufferSource()
            src.buffer = buf
            src.loop = true
            src.connect(c.destination)
            src.start()
            src
        } catch (_: Throwable) { null }
}
