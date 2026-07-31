package com.pixelclassics.app.ui.game

import androidx.compose.ui.graphics.Color

/**
 * Visual theme for a game's long-form campfire intro screen — the typewriter-
 * rendered story that plays the first time the player launches a game.
 * Each style is a distinct historical mood; one per cultural era.
 */
enum class IntroStyle(
    val bg: Color,
    val ink: Color,
    val inkDim: Color,
    val accent: Color,
    val frameMark: Color,
    val charsPerSec: Float,
    val showScanlines: Boolean,
    val monospace: Boolean,
) {
    /** Atari 1979 green CRT terminal. Phosphor green on jet black. */
    TERMINAL_GREEN_CRT(
        bg = Color.Black,
        ink = Color(0xFF30D050),
        inkDim = Color(0xFF1A8030),
        accent = Color(0xFFD0C010),
        frameMark = Color(0xFF0E3018),
        charsPerSec = 45f, showScanlines = true, monospace = true,
    ),
    /** Nokia 3310 LCD: dark olive pixels on a sickly LCD green. */
    NOKIA_LCD(
        bg = Color(0xFF9DAE83),
        ink = Color(0xFF2B3320),
        inkDim = Color(0xFF4F5A3A),
        accent = Color(0xFF1F2A12),
        frameMark = Color(0xFF50603A),
        charsPerSec = 40f, showScanlines = false, monospace = true,
    ),
    /** Chinese Brick Game: dark dots on beige LCD. */
    BRICK_GAME(
        bg = Color(0xFFC9C09A),
        ink = Color(0xFF1F1A0A),
        inkDim = Color(0xFF6E5A36),
        accent = Color(0xFF8A2B2B),
        frameMark = Color(0xFF6E6A55),
        charsPerSec = 38f, showScanlines = false, monospace = true,
    ),
    /** A 1972 California bar — dim wooden panels, brass accents. */
    BAR_WOOD(
        bg = Color(0xFF2A1808),
        ink = Color(0xFFE7CF52),
        inkDim = Color(0xFFA0833A),
        accent = Color(0xFFCC2222),
        frameMark = Color(0xFF6B3A1A),
        charsPerSec = 42f, showScanlines = false, monospace = true,
    ),
    /** Atari 1979 vector display — white phosphor lines on perfect black. */
    VECTOR_WHITE_ON_BLACK(
        bg = Color.Black,
        ink = Color.White,
        inkDim = Color(0xFF888888),
        accent = Color(0xFFCCCCCC),
        frameMark = Color(0xFF333333),
        charsPerSec = 45f, showScanlines = false, monospace = true,
    ),
    /** Soviet-era school notebook — graph paper with cursive ink. */
    PAPER_NOTEBOOK(
        bg = Color(0xFFE8DEB8),
        ink = Color(0xFF1A1A4A),
        inkDim = Color(0xFF5A5A8A),
        accent = Color(0xFFD52B1E),
        frameMark = Color(0xFF8888AA),
        charsPerSec = 42f, showScanlines = false, monospace = true,
    ),
    /** Apollo-era blueprint — drafting paper with technical-drawing ink. */
    BLUEPRINT(
        bg = Color(0xFFEEE6CC),
        ink = Color(0xFF234A8C),
        inkDim = Color(0xFF6080BB),
        accent = Color(0xFF0F5C3E),
        frameMark = Color(0xFF234A8C),
        charsPerSec = 40f, showScanlines = false, monospace = true,
    ),
    /** PDP-11 BSD terminal — white-on-black UNIX shell. */
    ASCII_DOS(
        bg = Color.Black,
        ink = Color(0xFFCCCCCC),
        inkDim = Color(0xFF888888),
        accent = Color(0xFF00FF00),
        frameMark = Color(0xFF444444),
        charsPerSec = 50f, showScanlines = true, monospace = true,
    ),
    /** IBM PC CGA — cyan-and-magenta of 1982. */
    CGA_CYAN(
        bg = Color(0xFF000020),
        ink = Color(0xFF55FFFF),
        inkDim = Color(0xFFFF55FF),
        accent = Color.White,
        frameMark = Color(0xFFAAAAAA),
        charsPerSec = 44f, showScanlines = false, monospace = true,
    ),
    /** Atari-2600-era jungle — earthy greens and browns, dense like canopy. */
    JUNGLE_PARCHMENT(
        bg = Color(0xFF1A4F1A),
        ink = Color(0xFFFCBC3C),
        inkDim = Color(0xFFEEC09A),
        accent = Color(0xFFFFAA88),
        frameMark = Color(0xFF5A3010),
        charsPerSec = 40f, showScanlines = false, monospace = true,
    ),
    /** Windows 3.1 grey — flat office colours from 1989. */
    WIN31_GREY(
        bg = Color(0xFFC0C0C0),
        ink = Color(0xFF000000),
        inkDim = Color(0xFF505050),
        accent = Color(0xFF0000A0),
        frameMark = Color(0xFF808080),
        charsPerSec = 42f, showScanlines = false, monospace = true,
    ),
    /** Cold-war defence-console amber-on-black — Missile Command tone. */
    COLD_WAR_AMBER(
        bg = Color.Black,
        ink = Color(0xFFFFAA00),
        inkDim = Color(0xFF885500),
        accent = Color(0xFFFF3333),
        frameMark = Color(0xFF553300),
        charsPerSec = 44f, showScanlines = true, monospace = true,
    ),
    /** Default — parchment, the original campfire-modal aesthetic. */
    PARCHMENT(
        bg = Color(0xFFF3E4C2),
        ink = Color(0xFF2A1B0A),
        inkDim = Color(0xFF6E5A36),
        accent = Color(0xFFB4520F),
        frameMark = Color(0xFF6E5A36),
        charsPerSec = 42f, showScanlines = false, monospace = false,
    ),
}
