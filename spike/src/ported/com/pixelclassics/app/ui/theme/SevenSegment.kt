package com.pixelclassics.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * 7-segment LCD-style digit renderer — the "sticks" font that real
 * Brick Game handhelds, calculators, and alarm clocks used. Each digit
 * is composed of seven trapezoidal segments labelled a-g:
 *
 *     aaaaa
 *    f     b
 *    f     b
 *     ggggg
 *    e     c
 *    e     c
 *     ddddd
 *
 * Inactive segments are still rendered faintly (the "phantom segments"
 * of a real LCD), so the readout has that authentic 8.8.8.8. ghost look.
 */
object SevenSegment {

    /** Segment masks: which of a/b/c/d/e/f/g are on for each digit 0-9 and a few letters. */
    private val masks: Map<Char, Int> = mapOf(
        // bit order: a=1, b=2, c=4, d=8, e=16, f=32, g=64
        '0' to 0b0111111, '1' to 0b0000110, '2' to 0b1011011, '3' to 0b1001111,
        '4' to 0b1100110, '5' to 0b1101101, '6' to 0b1111101, '7' to 0b0000111,
        '8' to 0b1111111, '9' to 0b1101111,
        // Capital letters that look reasonable on 7-segment.
        'A' to 0b1110111, 'B' to 0b1111100, 'C' to 0b0111001, 'D' to 0b1011110,
        'E' to 0b1111001, 'F' to 0b1110001, 'G' to 0b0111101, 'H' to 0b1110110,
        'I' to 0b0000110, 'J' to 0b0011110, 'L' to 0b0111000, 'N' to 0b1010100,
        'O' to 0b0111111, 'P' to 0b1110011, 'R' to 0b1010000, 'S' to 0b1101101,
        'T' to 0b1111000, 'U' to 0b0111110, 'Y' to 0b1101110,
        ' ' to 0b0000000, '-' to 0b1000000, '_' to 0b0001000,
    )

    /**
     * Draw a single 7-segment glyph at top-left position (x, y) with the
     * given total height. Width is roughly height/2.
     *
     * @param on      colour of lit segments
     * @param ghost   colour of unlit (phantom) segments — set alpha=0 to hide
     */
    fun draw(
        scope: DrawScope,
        ch: Char,
        x: Float,
        y: Float,
        height: Float,
        on: Color,
        ghost: Color = on.copy(alpha = 0.08f),
    ) = with(scope) {
        val mask = masks[ch.uppercaseChar()] ?: 0
        val w = height * 0.56f
        val t = height * 0.10f   // segment thickness
        val gap = height * 0.018f // tiny gap between segments
        val midY = y + height / 2f
        // Coordinates for each segment as a trapezoid path.
        fun segPath(seg: Int): Path = Path().apply {
            when (seg) {
                0 -> { // a — top horizontal
                    moveTo(x + t + gap, y)
                    lineTo(x + w - t - gap, y)
                    lineTo(x + w - t - gap - t / 2f, y + t)
                    lineTo(x + t + gap + t / 2f, y + t)
                    close()
                }
                1 -> { // b — top-right vertical
                    moveTo(x + w, y + t + gap)
                    lineTo(x + w, y + height / 2f - gap)
                    lineTo(x + w - t, y + height / 2f - gap - t / 2f)
                    lineTo(x + w - t, y + t + gap + t / 2f)
                    close()
                }
                2 -> { // c — bottom-right vertical
                    moveTo(x + w, y + height / 2f + gap)
                    lineTo(x + w, y + height - t - gap)
                    lineTo(x + w - t, y + height - t - gap - t / 2f)
                    lineTo(x + w - t, y + height / 2f + gap + t / 2f)
                    close()
                }
                3 -> { // d — bottom horizontal
                    moveTo(x + t + gap, y + height)
                    lineTo(x + w - t - gap, y + height)
                    lineTo(x + w - t - gap - t / 2f, y + height - t)
                    lineTo(x + t + gap + t / 2f, y + height - t)
                    close()
                }
                4 -> { // e — bottom-left vertical
                    moveTo(x, y + height / 2f + gap)
                    lineTo(x, y + height - t - gap)
                    lineTo(x + t, y + height - t - gap - t / 2f)
                    lineTo(x + t, y + height / 2f + gap + t / 2f)
                    close()
                }
                5 -> { // f — top-left vertical
                    moveTo(x, y + t + gap)
                    lineTo(x, y + height / 2f - gap)
                    lineTo(x + t, y + height / 2f - gap - t / 2f)
                    lineTo(x + t, y + t + gap + t / 2f)
                    close()
                }
                6 -> { // g — middle horizontal
                    moveTo(x + t + gap, midY - t / 2f)
                    lineTo(x + w - t - gap, midY - t / 2f)
                    lineTo(x + w - t - gap - t / 2f, midY)
                    lineTo(x + w - t - gap, midY + t / 2f)
                    lineTo(x + t + gap, midY + t / 2f)
                    lineTo(x + t + gap + t / 2f, midY)
                    close()
                }
            }
        }
        for (i in 0..6) {
            val lit = (mask shr i) and 1 == 1
            drawPath(segPath(i), color = if (lit) on else ghost)
        }
    }

    /** Width of a single glyph at the given height. */
    fun glyphWidth(height: Float): Float = height * 0.56f

    /** Approximate horizontal advance per glyph (glyph width + small gutter). */
    fun glyphAdvance(height: Float): Float = height * 0.66f

    /** Draw a whole string left-to-right, optionally right-aligned. */
    fun drawText(
        scope: DrawScope,
        text: String,
        x: Float,
        y: Float,
        height: Float,
        on: Color,
        ghost: Color = on.copy(alpha = 0.08f),
        rightAligned: Boolean = false,
    ) {
        val advance = glyphAdvance(height)
        val startX = if (rightAligned) x - advance * text.length + (advance - glyphWidth(height)) else x
        for ((i, ch) in text.withIndex()) draw(scope, ch, startX + i * advance, y, height, on, ghost)
    }
}
