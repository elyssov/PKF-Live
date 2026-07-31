package com.pixelclassics.app.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import com.pixelclassics.app.compat.AssetStore
import org.jetbrains.skia.Data
import org.jetbrains.skia.Font
import org.jetbrains.skia.FontEdging
import org.jetbrains.skia.FontMgr
import org.jetbrains.skia.Paint
import org.jetbrains.skia.TextLine
import org.jetbrains.skia.Typeface

/**
 * Crisp monospace rendering inside a DrawScope — browser (Skia) twin of the
 * Android/desktop versions. The browser has no system fonts, so the mono
 * face (JetBrains Mono, OFL) ships as a preloaded asset next to Neucha.
 */
enum class TextAlign { LEFT, CENTER, RIGHT }

private fun fromAsset(path: String): Typeface? =
    AssetStore.files[path]?.let { bytes ->
        runCatching { FontMgr.default.makeFromData(Data.makeFromBytes(bytes)) }.getOrNull()
    }

private val typefaceRegular: Typeface by lazy {
    fromAsset("fonts/JetBrainsMono-Regular.ttf") ?: Typeface.makeEmpty()
}
private val typefaceBold: Typeface by lazy {
    fromAsset("fonts/JetBrainsMono-Bold.ttf") ?: typefaceRegular
}

private val fontCache = HashMap<Long, Font>()

private fun fontFor(size: Float, bold: Boolean): Font {
    val key = (size.toRawBits().toLong() shl 1) or (if (bold) 1L else 0L)
    return fontCache.getOrPut(key) {
        Font(if (bold) typefaceBold else typefaceRegular, size).apply {
            edging = FontEdging.ALIAS
            isSubpixel = false
        }
    }
}

fun DrawScope.drawDosText(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    size: Float = 14f,
    bold: Boolean = false,
    align: TextAlign = TextAlign.LEFT,
) {
    val font = fontFor(size, bold)
    val line = TextLine.make(text, font)
    val drawX = when (align) {
        TextAlign.LEFT -> x
        TextAlign.CENTER -> x - line.width / 2f
        TextAlign.RIGHT -> x - line.width
    }
    val paint = Paint().apply {
        this.color = color.toArgb()
        isAntiAlias = false
    }
    drawContext.canvas.nativeCanvas.drawTextLine(line, drawX, y, paint)
}

/** Approximate pixel width of a string in the DOS font at a given size. */
fun dosTextWidth(text: String, size: Float = 14f, bold: Boolean = false): Float =
    TextLine.make(text, fontFor(size, bold)).width

/**
 * Schoolboy handwriting (Neucha, OFL) for the paper-notebook skins.
 * The TTF rides in via the Android asset port (assets/fonts/).
 */
object HandFont {
    val typeface: Typeface? by lazy { fromAsset("fonts/Neucha-Regular.ttf") }
    fun ensure(assets: com.pixelclassics.app.compat.AssetManager) { typeface }
}

private val handFontCache = HashMap<Long, Font>()

private fun handFontFor(size: Float): Font {
    val key = size.toRawBits().toLong()
    return handFontCache.getOrPut(key) {
        Font(HandFont.typeface ?: typefaceRegular, size).apply {
            edging = FontEdging.ANTI_ALIAS   // pencil strokes want smooth edges
        }
    }
}

fun DrawScope.drawHandText(
    text: String,
    x: Float,
    y: Float,
    color: Color,
    size: Float = 16f,
    align: TextAlign = TextAlign.LEFT,
) {
    val line = TextLine.make(text, handFontFor(size))
    val drawX = when (align) {
        TextAlign.LEFT -> x
        TextAlign.CENTER -> x - line.width / 2f
        TextAlign.RIGHT -> x - line.width
    }
    val paint = Paint().apply {
        this.color = color.toArgb()
        isAntiAlias = true
    }
    drawContext.canvas.nativeCanvas.drawTextLine(line, drawX, y, paint)
}
