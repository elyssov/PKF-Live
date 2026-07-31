package com.pixelclassics.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Authentic IBM-PC / Norton-Commander / EGA-VGA color palette.
 * Values mirror the originals from assets/games/menu.html so the native
 * skin looks identical to the WebView build it replaces.
 */
object DosPalette {
    val Black = Color(0xFF000000)
    val Gray = Color(0xFFAAAAAA)
    val White = Color(0xFFFFFFFF)
    val DimGray = Color(0xFF555555)
    val DarkGray = Color(0xFF333333)

    val NcBlue = Color(0xFF0000AA)
    val NcCyan = Color(0xFF00AAAA)
    val NcCyanDim = Color(0xFF005555)
    val NcYellow = Color(0xFFFFFF55)
    val NcGreen = Color(0xFF55FF55)
    val NcRed = Color(0xFFAA0000)
    val FileCyan = Color(0xFF00FFFF)

    // Nokia / Gameboy green for Snake.
    val NokiaBg = Color(0xFF9BBC0F)
    val NokiaBgDark = Color(0xFF8BAC0F)
    val NokiaSnake = Color(0xFF0F380F)
    val NokiaFood = Color(0xFF306230)
}
