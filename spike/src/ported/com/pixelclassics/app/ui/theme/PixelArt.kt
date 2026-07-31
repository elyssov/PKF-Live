package com.pixelclassics.app.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Small helpers that keep games away from the "smooth CSS-rectangle"
 * look. Every primitive here aligns to an integer pixel grid (`px`) and
 * uses period-appropriate palettes — so individual games can either go
 * coarser (CGA / Atari 2600 / NES) or carry chunky highlights and
 * shadows for a beveled feel.
 */
object PixelArt {

    /** Authentic console / micro palettes. */
    object Palettes {
        // NES (subset of 54 fixed colours).
        val NES_BLACK = Color(0xFF000000)
        val NES_WHITE = Color(0xFFFCFCFC)
        val NES_GRAY_DK = Color(0xFF7C7C7C)
        val NES_GRAY = Color(0xFFBCBCBC)
        val NES_BG_DK = Color(0xFF0000A8)
        val NES_BG = Color(0xFF0058F8)
        val NES_RED = Color(0xFFD82800)
        val NES_ORANGE = Color(0xFFFC7460)
        val NES_YELLOW = Color(0xFFFCBC3C)
        val NES_GREEN_DK = Color(0xFF005800)
        val NES_GREEN = Color(0xFF00A800)
        val NES_GREEN_LT = Color(0xFF58D854)
        val NES_BLUE = Color(0xFF3CBCFC)
        val NES_PURPLE = Color(0xFFB80058)
        val NES_BROWN = Color(0xFF884400)

        // Atari 2600 TIA — gritty, low saturation.
        val ATARI_BLACK = Color(0xFF000000)
        val ATARI_GRAY = Color(0xFF888888)
        val ATARI_WHITE = Color(0xFFCCCCCC)
        val ATARI_GREEN_DK = Color(0xFF1A6010)
        val ATARI_GREEN = Color(0xFF30B020)
        val ATARI_BROWN_DK = Color(0xFF3A2A10)
        val ATARI_BROWN = Color(0xFF6A4A20)
        val ATARI_ORANGE = Color(0xFFC06820)
        val ATARI_BLUE_DK = Color(0xFF1828A0)
        val ATARI_BLUE = Color(0xFF3050C0)
        val ATARI_RED = Color(0xFFC02020)
        val ATARI_YELLOW = Color(0xFFD0C010)

        // Nokia 3310 LCD.
        val NOKIA_BG = Color(0xFF9BBC0F)
        val NOKIA_BG_DK = Color(0xFF8BAC0F)
        val NOKIA_PIXEL = Color(0xFF0F380F)
        val NOKIA_PIXEL_LT = Color(0xFF306230)

        // CGA 4-colour palette 1.
        val CGA_BLACK = Color(0xFF000000)
        val CGA_CYAN = Color(0xFF55FFFF)
        val CGA_MAGENTA = Color(0xFFFF55FF)
        val CGA_WHITE = Color(0xFFFFFFFF)
    }

    /**
     * A "pixel" rectangle that snaps coords to integer multiples of `px`.
     * Use this everywhere instead of `drawRect` when you want crisp,
     * aligned blocks instead of subpixel-blurred ones.
     */
    fun DrawScope.pxRect(
        x: Float, y: Float, w: Float, h: Float, color: Color, px: Float = 1f,
    ) {
        val sx = (x / px).toInt() * px
        val sy = (y / px).toInt() * px
        val sw = (((w / px) + 0.5f).toInt() * px).coerceAtLeast(px)
        val sh = (((h / px) + 0.5f).toInt() * px).coerceAtLeast(px)
        drawRect(color, topLeft = Offset(sx, sy), size = Size(sw, sh))
    }

    /**
     * Beveled NES-style block: brighter top + left edge, darker bottom +
     * right edge, slight highlight pixel in the corner.
     */
    fun DrawScope.beveledBlock(
        x: Float, y: Float, size: Float, base: Color, edge: Float = 2f,
    ) {
        val light = base.lighten(0.35f)
        val dark = base.darken(0.40f)
        drawRect(base, topLeft = Offset(x, y), size = Size(size, size))
        drawRect(light, topLeft = Offset(x, y), size = Size(size, edge))
        drawRect(light, topLeft = Offset(x, y), size = Size(edge, size))
        drawRect(dark, topLeft = Offset(x, y + size - edge), size = Size(size, edge))
        drawRect(dark, topLeft = Offset(x + size - edge, y), size = Size(edge, size))
        // Tiny corner highlight.
        drawRect(Color.White.copy(alpha = 0.6f), topLeft = Offset(x + edge, y + edge), size = Size(edge, edge))
    }

    /**
     * 50/50 dither between two colours over a rect — classic CGA / EGA
     * shading trick that adds texture without smooth gradients.
     */
    fun DrawScope.dither(
        x: Float, y: Float, w: Float, h: Float, a: Color, b: Color, px: Float = 2f,
    ) {
        drawRect(a, topLeft = Offset(x, y), size = Size(w, h))
        var yy = 0f
        var row = 0
        while (yy < h) {
            var xx = if (row % 2 == 0) 0f else px
            while (xx < w) {
                drawRect(b, topLeft = Offset(x + xx, y + yy), size = Size(px, px))
                xx += px * 2f
            }
            yy += px; row++
        }
    }

    /**
     * Faint horizontal scanlines — applied last over the playfield to
     * cheaply imitate a CRT.
     */
    fun DrawScope.scanlines(
        x: Float, y: Float, w: Float, h: Float, alpha: Float = 0.18f, step: Float = 2f,
    ) {
        var yy = 0f
        while (yy < h) {
            drawRect(Color.Black.copy(alpha = alpha), topLeft = Offset(x, y + yy), size = Size(w, 1f))
            yy += step
        }
    }

    /**
     * Drop-shadow text — draw a black offset copy beneath, then the main
     * colour on top. Avoids the floating "CSS" look that pure white-on-
     * black text gets.
     */
    fun DrawScope.shadowedText(
        text: String, x: Float, y: Float, color: Color, size: Float, bold: Boolean = true,
        align: TextAlign = TextAlign.LEFT, shadowOffset: Float = 2f,
    ) {
        drawDosText(text, x + shadowOffset, y + shadowOffset, Color.Black, size, bold, align)
        drawDosText(text, x, y, color, size, bold, align)
    }
}

private fun Color.lighten(amount: Float): Color {
    val a = amount.coerceIn(0f, 1f)
    return Color(
        red + (1f - red) * a,
        green + (1f - green) * a,
        blue + (1f - blue) * a,
        alpha,
    )
}

private fun Color.darken(amount: Float): Color {
    val a = amount.coerceIn(0f, 1f)
    return Color(red * (1f - a), green * (1f - a), blue * (1f - a), alpha)
}
