package com.pixelclassics.app.engine

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

/**
 * Computes a scale-to-fit (letterbox) mapping between a virtual coordinate
 * rectangle and a physical canvas size.
 *
 * Game draws and game receives input both go through this mapping, so the
 * exact same code works pixel-for-pixel from a 480p phone in landscape to
 * a 2K tablet.
 */
data class VirtualTransform(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float,
    val virtualWidth: Float,
    val virtualHeight: Float,
) {
    /** Physical -> virtual. */
    fun toVirtual(physical: Offset): Offset {
        if (scale <= 0f) return Offset.Zero
        val vx = (physical.x - offsetX) / scale
        val vy = (physical.y - offsetY) / scale
        return Offset(
            vx.coerceIn(0f, virtualWidth),
            vy.coerceIn(0f, virtualHeight),
        )
    }

    /** True if the physical point falls inside the virtual rect. */
    fun contains(physical: Offset): Boolean {
        val vx = (physical.x - offsetX) / scale
        val vy = (physical.y - offsetY) / scale
        return vx in 0f..virtualWidth && vy in 0f..virtualHeight
    }
}

fun computeVirtualTransform(
    canvasSize: Size,
    virtualWidth: Float,
    virtualHeight: Float,
): VirtualTransform {
    if (canvasSize.width <= 0f || canvasSize.height <= 0f) {
        return VirtualTransform(1f, 0f, 0f, virtualWidth, virtualHeight)
    }
    val sx = canvasSize.width / virtualWidth
    val sy = canvasSize.height / virtualHeight
    val scale = minOf(sx, sy)
    val drawnW = virtualWidth * scale
    val drawnH = virtualHeight * scale
    val ox = (canvasSize.width - drawnW) / 2f
    val oy = (canvasSize.height - drawnH) / 2f
    return VirtualTransform(scale, ox, oy, virtualWidth, virtualHeight)
}
