package com.pixelclassics.app.games.superbarsik

import com.pixelclassics.app.compat.BitmapFactory
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import com.pixelclassics.app.compat.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * SUPER БАРСИК BROS — kitchen-to-rooftop platformer in cat-world.
 *
 * Open homage to the 1985 platformer that revived home consoles. Barsik
 * (small red cat, big ears) runs across ten levels — Kitchen → Hallway →
 * Yard → Dumpster → Cellar → Attic → Roof → Neighbour's flat → Park →
 * Bridge — to collect a cart of treats for Muska, the proud she-cat on
 * the opposite balcony. Each level carries one signature treat; the
 * final stage pits him against Сачколов, the dog-catcher villain who
 * tumbles into a bubbling brown pit from which S, H, I, T bob up in
 * sculpted dung-letter form.
 *
 * Engine: tile grid (32px tiles, 32×15 visible, ~60 tiles per level),
 * scrolling camera, AABB collisions, three enemy archetypes, four
 * power-ups, ?-blocks, breakable bricks, flag-with-sardine end. All
 * artwork is procedural Compose drawing — placeholders for Кирины
 * sprites later.
 */
class SuperBarsikGame : Game {
    override val id: String = "super_barsik"
    override val titleResId: Int = R.string.game_super_barsik
    override val backgroundColor: Color = Color(0xFF6FB6FF)
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    override val introText: String = SUPER_BARSIK_INTRO_RU       // fallback for legacy callers
    override fun introTextFor(lang: String): String = pickSuperBarsikIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PARCHMENT
    override val helpLines: List<String> = listOf(
        "D-pad ◀ ▶ — walk · hold to RUN · ▲ or ● — jump",
        "Stomp tapki & krysy from above · grab the sardine on the pole",
        "Stand on a MAGENTA pipe, press ▼ — WARP ZONE (skip worlds!)",
        "Each fridge: \"not this one, Barsik\" — only the last has Whiskas",
    )

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private var spriteAssets: Map<String, ImageBitmap> = emptyMap()

    // ── Tile / world ──────────────────────────────────────────────────────
    private val TS = 32f                  // tile size in virtual px
    private val ROWS = 15                 // 15 × 32 = 480 (full screen height)
    private val GROUND_Y_TILES = 13       // top of the ground band

    private enum class TileKind { SKY, GROUND, BRICK, QBLOCK, QBLOCK_USED, PIPE_TL, PIPE_TR, PIPE_TL_WARP, PIPE_TR_WARP, PIPE_BL, PIPE_BR, FLAGPOLE, FLAGTOP, FRIDGE, HARD }

    private fun tileOf(c: Char): TileKind = when (c) {
        '.' -> TileKind.SKY
        'G' -> TileKind.GROUND
        'B' -> TileKind.BRICK
        '?' -> TileKind.QBLOCK
        '[' -> TileKind.PIPE_TL
        ']' -> TileKind.PIPE_TR
        '(' -> TileKind.PIPE_TL_WARP        // left half of warp-entry pipe top
        ')' -> TileKind.PIPE_TR_WARP        // right half
        '{' -> TileKind.PIPE_BL
        '}' -> TileKind.PIPE_BR
        'P' -> TileKind.FLAGPOLE
        'F' -> TileKind.FLAGTOP
        'R' -> TileKind.FRIDGE
        'H' -> TileKind.HARD
        '_' -> TileKind.QBLOCK_USED   // used ?-block stays SOLID (was falling into SKY)
        else -> TileKind.SKY
    }

    private fun solid(t: TileKind) = when (t) {
        TileKind.SKY, TileKind.FLAGPOLE, TileKind.FLAGTOP -> false
        else -> true
    }

    private fun isPipeTop(t: TileKind) = t == TileKind.PIPE_TL || t == TileKind.PIPE_TR ||
        t == TileKind.PIPE_TL_WARP || t == TileKind.PIPE_TR_WARP
    private fun isWarpTop(t: TileKind) = t == TileKind.PIPE_TL_WARP || t == TileKind.PIPE_TR_WARP

    // ── Enemies / items ───────────────────────────────────────────────────
    private enum class EnemyKind { TAPOK, KRYSA, VORONA, RUKAVICHKA }
    private data class Enemy(
        var x: Float, var y: Float, var vx: Float, var vy: Float,
        val kind: EnemyKind,
        var alive: Boolean = true,
        var stomped: Float = 0f,             // 0 = walking; >0 = squashed timer
        var inShell: Boolean = false,
        var shellVx: Float = 0f,
        var anim: Float = 0f,
        var emergingT: Float = 0f,           // RUKAVICHKA: time spent rising from a pipe (0 = done)
    )

    private enum class PowerUpKind { MILK, MOUSE, STAR, GREEN_BALL, SIGNATURE }
    private data class PowerUp(
        var x: Float, var y: Float, var vx: Float, var vy: Float,
        val kind: PowerUpKind,
        var alive: Boolean = true,
        var rising: Float = 1f,              // 1→0 as the item emerges from its block
        val signatureName: String = "",
    )

    private data class Coin(val x: Float, val y: Float, var alive: Boolean = true, var anim: Float = 0f)
    private data class Bullet(var x: Float, var y: Float, var vx: Float, var vy: Float, var alive: Boolean = true, var bounces: Int = 0)
    private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float, val color: Color, val size: Float = 3f, val text: String? = null)

    // ── Levels ────────────────────────────────────────────────────────────
    // Live launcher language — the narrative is authored RU-first, the EN side
    // ships alongside; both picked per frame so the NC picker rules them all.
    private fun ru(): Boolean = GameContextHolder.ctx?.lang == "ru"
    private fun t(en: String, ru: String): String = if (ru()) ru else en

    private data class LevelDef(
        val name: String,
        val nameEn: String,
        val skyColor: Color,
        val groundColor: Color,
        val brickColor: Color,
        val signatureTreat: String,
        val treatEn: String,
        val tiles: List<String>,
        val enemies: List<Triple<EnemyKind, Int, Int>>,    // kind, tx, ty
        val coins: List<Pair<Int, Int>>,                    // tx, ty
        val signatureAt: Pair<Int, Int>,                    // tx, ty for the level-specific treat (inside a ?-block)
        val warpTarget: Int = -1,                           // level index a warp pipe here jumps to (-1 = none)
    )

    private val levels: List<LevelDef> by lazy { buildLevels() }

    private fun buildLevels(): List<LevelDef> {
        // Helper: build a flat ground line of `width` tiles, then carve.
        fun ground(width: Int, top: String = "."): MutableList<StringBuilder> {
            val rows = ArrayList<StringBuilder>(ROWS)
            for (r in 0 until ROWS) {
                val sb = StringBuilder()
                for (c in 0 until width) {
                    sb.append(if (r >= GROUND_Y_TILES) 'G' else top[0])
                }
                rows.add(sb)
            }
            return rows
        }
        fun put(rows: MutableList<StringBuilder>, tx: Int, ty: Int, ch: Char) {
            if (ty in 0 until ROWS && tx in 0 until rows[ty].length) rows[ty].set(tx, ch)
        }
        fun gap(rows: MutableList<StringBuilder>, tx: Int, w: Int) {
            for (c in tx until tx + w) for (r in GROUND_Y_TILES until ROWS) put(rows, c, r, '.')
        }
        fun platform(rows: MutableList<StringBuilder>, tx: Int, ty: Int, w: Int, ch: Char = 'B') {
            for (c in tx until tx + w) put(rows, c, ty, ch)
        }
        fun pipe(rows: MutableList<StringBuilder>, tx: Int, h: Int) {
            val top = GROUND_Y_TILES - h
            put(rows, tx, top, '['); put(rows, tx + 1, top, ']')
            for (r in top + 1 until GROUND_Y_TILES) {
                put(rows, tx, r, '{'); put(rows, tx + 1, r, '}')
            }
        }
        // Warp-entry pipe: same body, magenta top, stand on it + DPAD ▼ to descend
        // and trigger the bonus.
        fun warpPipe(rows: MutableList<StringBuilder>, tx: Int, h: Int) {
            val top = GROUND_Y_TILES - h
            put(rows, tx, top, '('); put(rows, tx + 1, top, ')')
            for (r in top + 1 until GROUND_Y_TILES) {
                put(rows, tx, r, '{'); put(rows, tx + 1, r, '}')
            }
        }
        fun stairs(rows: MutableList<StringBuilder>, tx: Int, height: Int, dir: Int = 1) {
            var step = if (dir > 0) 0 else height - 1
            for (i in 0 until height) {
                val h = (if (dir > 0) i + 1 else height - i)
                for (j in 0 until h) put(rows, tx + i, GROUND_Y_TILES - 1 - j, 'H')
            }
            step++
        }
        fun flag(rows: MutableList<StringBuilder>, tx: Int) {
            for (r in GROUND_Y_TILES - 9 until GROUND_Y_TILES) put(rows, tx, r, 'P')
            put(rows, tx, GROUND_Y_TILES - 10, 'F')
        }
        fun fridge(rows: MutableList<StringBuilder>, tx: Int) {
            for (r in GROUND_Y_TILES - 4 until GROUND_Y_TILES) {
                put(rows, tx, r, 'R'); put(rows, tx + 1, r, 'R')
            }
        }

        val list = ArrayList<LevelDef>(10)

        // L1 — KITCHEN, signature: САРДИНА
        run {
            val width = 60
            val rows = ground(width)
            platform(rows, 12, 9, 4, '?')
            put(rows, 14, 9, 'B')
            platform(rows, 19, 6, 3, 'B')
            pipe(rows, 27, 3); warpPipe(rows, 34, 3)
            stairs(rows, 42, 4, 1)
            flag(rows, 53); fridge(rows, 56)
            list += LevelDef(
                name = "КУХНЯ", nameEn = "KITCHEN",
                skyColor = Color(0xFFFFCC88), groundColor = Color(0xFFB8884C), brickColor = Color(0xFFE0A060),
                signatureTreat = "САРДИНА", treatEn = "SARDINE",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.TAPOK, 22, 12), Triple(EnemyKind.TAPOK, 36, 12), Triple(EnemyKind.KRYSA, 45, 12)),
                coins = listOf(13 to 7, 14 to 7, 15 to 7, 20 to 4, 21 to 4),
                signatureAt = 13 to 9,
                warpTarget = 3,        // КУХНЯ warp → ПОМОЙКА (skip 2 worlds)
            )
        }
        // L2 — HALLWAY, signature: СМЕТАНА
        run {
            val width = 60
            val rows = ground(width)
            platform(rows, 10, 10, 2, 'B'); platform(rows, 13, 8, 3, '?')
            gap(rows, 20, 3)
            pipe(rows, 27, 3)
            platform(rows, 34, 7, 4, 'B')
            platform(rows, 40, 5, 4, '?')
            stairs(rows, 47, 3, 1)
            flag(rows, 53); fridge(rows, 56)
            list += LevelDef(
                name = "ПРИХОЖАЯ", nameEn = "HALLWAY",
                skyColor = Color(0xFFE8D8B8), groundColor = Color(0xFF80604C), brickColor = Color(0xFFC09060),
                signatureTreat = "СМЕТАНА", treatEn = "SOUR CREAM",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.TAPOK, 18, 12), Triple(EnemyKind.KRYSA, 30, 12), Triple(EnemyKind.TAPOK, 38, 6), Triple(EnemyKind.VORONA, 43, 5)),
                coins = listOf(14 to 6, 15 to 6, 35 to 5, 36 to 5, 37 to 5),
                signatureAt = 41 to 5,
            )
        }
        // L3 — YARD, signature: КОЛБАСКА
        run {
            val width = 64
            val rows = ground(width)
            platform(rows, 8, 7, 4, 'B')
            gap(rows, 14, 3)
            platform(rows, 20, 9, 1, '?')
            platform(rows, 24, 9, 1, '?')
            pipe(rows, 30, 3)
            gap(rows, 36, 4)
            platform(rows, 42, 6, 5, 'B')
            platform(rows, 50, 8, 2, '?')
            flag(rows, 57); fridge(rows, 60)
            list += LevelDef(
                name = "ДВОР", nameEn = "BACKYARD",
                skyColor = Color(0xFF7FC4FF), groundColor = Color(0xFF6CA040), brickColor = Color(0xFF9C7048),
                signatureTreat = "КОЛБАСКА", treatEn = "SAUSAGE",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.TAPOK, 11, 12), Triple(EnemyKind.KRYSA, 28, 12), Triple(EnemyKind.VORONA, 38, 5), Triple(EnemyKind.TAPOK, 46, 5), Triple(EnemyKind.KRYSA, 52, 12)),
                coins = listOf(9 to 6, 10 to 6, 11 to 6, 43 to 5, 44 to 5, 45 to 5),
                signatureAt = 51 to 8,
            )
        }
        // L4 — DUMPSTER, signature: КУРИНАЯ КОСТОЧКА
        run {
            val width = 66
            val rows = ground(width)
            platform(rows, 9, 9, 3, 'B'); platform(rows, 9, 6, 1, '?')
            gap(rows, 15, 3)
            platform(rows, 22, 7, 4, '?')
            pipe(rows, 30, 3)
            gap(rows, 36, 4)
            platform(rows, 42, 8, 3, 'B'); platform(rows, 42, 5, 3, '?')
            warpPipe(rows, 46, 3)
            stairs(rows, 50, 4, 1)
            flag(rows, 59); fridge(rows, 62)
            list += LevelDef(
                name = "ПОМОЙКА", nameEn = "DUMPSTER",
                skyColor = Color(0xFF8088A8), groundColor = Color(0xFF5A4030), brickColor = Color(0xFF704030),
                signatureTreat = "КУРИНАЯ КОСТОЧКА", treatEn = "CHICKEN BONE",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.KRYSA, 12, 12), Triple(EnemyKind.TAPOK, 26, 12), Triple(EnemyKind.VORONA, 33, 5), Triple(EnemyKind.KRYSA, 45, 12), Triple(EnemyKind.TAPOK, 53, 12)),
                coins = listOf(23 to 4, 24 to 4, 25 to 4, 42 to 2, 43 to 2, 44 to 2),
                signatureAt = 43 to 5,
                warpTarget = 6,        // ПОМОЙКА warp → КРЫША (skip 2 worlds)
            )
        }
        // L5 — CELLAR (underground), signature: МЯТА СУШЁНАЯ
        run {
            val width = 64
            val rows = ground(width)
            for (r in 0 until GROUND_Y_TILES - 7) for (c in 0 until width) put(rows, c, r, '.')
            for (r in 0 until GROUND_Y_TILES - 7) for (c in 0 until width) put(rows, c, r, 'H')
            platform(rows, 8, 9, 5, '?')
            platform(rows, 16, 7, 3, 'B')
            gap(rows, 22, 2)
            pipe(rows, 28, 3)
            platform(rows, 34, 8, 4, 'B')
            platform(rows, 40, 5, 3, '?')
            flag(rows, 56); fridge(rows, 59)
            list += LevelDef(
                name = "ПОДВАЛ", nameEn = "BASEMENT",
                skyColor = Color(0xFF181028), groundColor = Color(0xFF3A2A1A), brickColor = Color(0xFF604030),
                signatureTreat = "МЯТА СУШЁНАЯ", treatEn = "DRIED CATNIP",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.KRYSA, 13, 12), Triple(EnemyKind.TAPOK, 24, 12), Triple(EnemyKind.KRYSA, 32, 12), Triple(EnemyKind.TAPOK, 44, 12), Triple(EnemyKind.VORONA, 50, 6)),
                coins = listOf(9 to 7, 10 to 7, 11 to 7, 35 to 6, 36 to 6),
                signatureAt = 10 to 9,
            )
        }
        // L6 — ATTIC, signature: МОЛОЧНЫЙ КОКТЕЙЛЬ
        run {
            val width = 68
            val rows = ground(width)
            platform(rows, 7, 10, 3, '?')
            platform(rows, 12, 7, 4, 'B')
            gap(rows, 18, 3)
            platform(rows, 24, 5, 4, '?')
            pipe(rows, 31, 3)
            platform(rows, 38, 9, 2, 'B')
            platform(rows, 43, 6, 5, 'B')
            platform(rows, 52, 4, 3, '?')
            flag(rows, 60); fridge(rows, 63)
            list += LevelDef(
                name = "ЧЕРДАК", nameEn = "ATTIC",
                skyColor = Color(0xFFB89880), groundColor = Color(0xFF6A4830), brickColor = Color(0xFF80604A),
                signatureTreat = "МОЛОЧНЫЙ КОКТЕЙЛЬ", treatEn = "MILKSHAKE",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.TAPOK, 14, 6), Triple(EnemyKind.VORONA, 22, 4), Triple(EnemyKind.KRYSA, 35, 12), Triple(EnemyKind.TAPOK, 45, 5), Triple(EnemyKind.VORONA, 55, 3)),
                coins = listOf(8 to 8, 9 to 8, 25 to 4, 26 to 4, 27 to 4, 53 to 3, 54 to 3),
                signatureAt = 53 to 4,
            )
        }
        // L7 — ROOF, signature: РЫБИЙ ХВОСТ
        run {
            val width = 70
            val rows = ground(width)
            // tilted roof: stairs up then down
            stairs(rows, 5, 5, 1); stairs(rows, 12, 5, -1)
            gap(rows, 20, 4)
            platform(rows, 27, 7, 4, 'B')
            platform(rows, 27, 4, 1, '?')
            gap(rows, 34, 3)
            warpPipe(rows, 37, 3)
            stairs(rows, 40, 4, 1); stairs(rows, 47, 4, -1)
            platform(rows, 54, 5, 3, '?')
            flag(rows, 62); fridge(rows, 65)
            list += LevelDef(
                name = "КРЫША", nameEn = "ROOFTOP",
                skyColor = Color(0xFFFF9C5A), groundColor = Color(0xFF884030), brickColor = Color(0xFFA85040),
                signatureTreat = "РЫБИЙ ХВОСТ", treatEn = "FISH TAIL",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.VORONA, 10, 4), Triple(EnemyKind.TAPOK, 30, 6), Triple(EnemyKind.VORONA, 43, 4), Triple(EnemyKind.KRYSA, 56, 12)),
                coins = listOf(28 to 5, 29 to 5, 30 to 5, 55 to 4, 56 to 4),
                signatureAt = 55 to 5,
                warpTarget = 8,        // КРЫША warp → ПАРК (skip 1 world, still face the boss)
            )
        }
        // L8 — NEIGHBOUR'S FLAT, signature: ПЕЧЕНЬКА
        run {
            val width = 68
            val rows = ground(width)
            platform(rows, 8, 10, 3, 'B'); platform(rows, 8, 7, 1, '?')
            platform(rows, 14, 7, 4, 'B'); platform(rows, 14, 4, 4, 'B')
            gap(rows, 22, 3)
            pipe(rows, 29, 3)
            platform(rows, 35, 6, 4, '?')
            platform(rows, 42, 9, 2, 'B'); platform(rows, 42, 5, 3, 'B')
            stairs(rows, 50, 5, 1)
            flag(rows, 60); fridge(rows, 63)
            list += LevelDef(
                name = "СОСЕДСКАЯ КВАРТИРА", nameEn = "NEIGHBOUR'S FLAT",
                skyColor = Color(0xFFE0B0B8), groundColor = Color(0xFF704848), brickColor = Color(0xFF986870),
                signatureTreat = "ПЕЧЕНЬКА", treatEn = "COOKIE",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.KRYSA, 13, 12), Triple(EnemyKind.TAPOK, 25, 12), Triple(EnemyKind.VORONA, 32, 4), Triple(EnemyKind.KRYSA, 38, 5), Triple(EnemyKind.TAPOK, 44, 4), Triple(EnemyKind.VORONA, 52, 3)),
                coins = listOf(15 to 6, 16 to 6, 17 to 6, 43 to 4, 44 to 4),
                signatureAt = 36 to 6,   // the ?-row really is 6 — (36,5) pointed at empty sky

            )
        }
        // L9 — PARK, signature: КУРИНЫЙ ПАШТЕТ
        run {
            val width = 72
            val rows = ground(width)
            platform(rows, 9, 8, 3, '?')
            gap(rows, 16, 3)
            platform(rows, 23, 6, 4, 'B')
            pipe(rows, 30, 4)
            gap(rows, 36, 3)
            platform(rows, 42, 7, 4, '?')
            platform(rows, 48, 5, 3, 'B')
            platform(rows, 54, 8, 2, 'B')
            flag(rows, 64); fridge(rows, 67)
            list += LevelDef(
                name = "ПАРК", nameEn = "PARK",
                skyColor = Color(0xFF6CCC60), groundColor = Color(0xFF40703C), brickColor = Color(0xFF887050),
                signatureTreat = "КУРИНЫЙ ПАШТЕТ", treatEn = "CHICKEN PATE",
                tiles = rows.map { it.toString() },
                enemies = listOf(Triple(EnemyKind.KRYSA, 14, 12), Triple(EnemyKind.TAPOK, 26, 5), Triple(EnemyKind.VORONA, 33, 4), Triple(EnemyKind.KRYSA, 40, 12), Triple(EnemyKind.TAPOK, 50, 4), Triple(EnemyKind.VORONA, 58, 3), Triple(EnemyKind.KRYSA, 55, 7)),
                coins = listOf(24 to 5, 25 to 5, 26 to 5, 43 to 6, 44 to 6),
                signatureAt = 45 to 7,   // the ?-row really is 7 — (45,6) pointed at empty sky

            )
        }
        // L10 — BRIDGE OVER DUMP (Сачколов boss). The fridge here is the boss arena.
        run {
            val width = 64
            val rows = ground(width)
            // Bridge arena: large empty stage with bricks for boss
            platform(rows, 16, 7, 4, 'B')
            platform(rows, 26, 7, 4, 'B')
            // No flag — the "fridge" is replaced by the boss collision marker
            for (c in 50 until 56) for (r in GROUND_Y_TILES - 5 until GROUND_Y_TILES) put(rows, c, r, 'H')
            list += LevelDef(
                name = "МОСТ НАД СВАЛКОЙ", nameEn = "BRIDGE OVER THE DUMP",
                skyColor = Color(0xFF704028), groundColor = Color(0xFF402818), brickColor = Color(0xFF604030),
                signatureTreat = "БАНКА ВИСКАСА", treatEn = "CAN OF WHISKAS",
                tiles = rows.map { it.toString() },
                enemies = emptyList(),
                coins = emptyList(),
                signatureAt = -1 to -1,
            )
        }

        return list
    }

    // ── Runtime state ─────────────────────────────────────────────────────
    private enum class Stage { TITLE, PLAY, LEVEL_INTRO, FRIDGE_NOTE, WIN }
    private var stage = Stage.TITLE
    private var levelIdx = 0
    private lateinit var current: LevelDef
    private var levelWidthTiles = 0
    private var tiles: Array<CharArray> = emptyArray()
    private var enemies = ArrayList<Enemy>()
    private var coins = ArrayList<Coin>()
    private var powerUps = ArrayList<PowerUp>()
    private var bullets = ArrayList<Bullet>()
    private var particles = ArrayList<Particle>()
    private var cameraX = 0f
    private var time = 0f
    private var levelTimeLeft = 0f
    private var msg = ""
    private var msgTimer = 0f
    private var fridgeNoteTimer = 0f
    private var levelIntroTimer = 0f
    private val pipeTops = ArrayList<Pair<Int, Int>>()    // (tx, ty) of each pipe's top-left
    private val pipeSpawnTimers = ArrayList<Float>()
    private var warpFlashT = 0f                            // 0..1 fade after a warp trigger
    private var fromWarp = false                           // show WARP-ZONE banner on the next level intro

    private var score = 0
    private var best = 0
    private var lives = 3
    private var coinsCollected = 0
    private var treatsTotal = 0     // signature treats collected (out of levels.size)

    // Player
    private var px = 0f
    private var py = 0f
    private var pvx = 0f
    private var pvy = 0f
    private var grounded = false
    private var coyoteT = 1f              // time since last grounded — coyote-jump window
    private var jumpBufferT = -1f         // pending jump press — fires on landing (jump buffer)
    private var pdir = 1
    private enum class Size { SMALL, BIG, FIRE }
    private var psize = Size.SMALL
    private var invincibleT = 0f         // post-hit invincibility flicker
    private var starT = 0f               // star invincibility (kills enemies)
    private var deathPause = 0f
    private var winCelebration = 0f
    private var flagSlide = false

    private val playerW get() = if (psize == Size.SMALL) 22f else 26f
    private val playerH get() = if (psize == Size.SMALL) 28f else 50f

    // Input
    private var dpadX = 0
    private var dpadHoldT = 0f           // how long the dpad has been held in one direction
    private var fireHoldT = -1f          // time the fire was pressed (for variable jump)
    private var fireDown = false
    private var fireShootCooldown = 0f

    // ── Boss (Сачколов) ───────────────────────────────────────────────────
    private var bossHp = 0
    private var bossX = 0f
    private var bossY = 0f
    private var bossVx = 0f
    private var bossVy = 0f
    private var bossPattern = 0f
    private var bossFallT = -1f
    private var bossPhase = 0             // 0 = active, 1 = falling, 2 = letters
    private val sachki = ArrayList<Bullet>()
    private val bossLetters = ArrayList<Particle>()  // S/H/I/T as they bob up

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        loadSpriteAssets(ctx)
        stage = Stage.TITLE
        msg = ""
        time = 0f
    }

    private fun loadSpriteAssets(ctx: GameContext) {
        if (spriteAssets.isNotEmpty()) return
        val names = listOf(
            "barsik_small_walk_0", "barsik_small_walk_1", "barsik_small_walk_2", "barsik_small_walk_3",
            "barsik_small_jump", "barsik_small_idle",
            "barsik_big_walk_0", "barsik_big_walk_1", "barsik_big_walk_2", "barsik_big_walk_3",
            "barsik_big_jump", "barsik_big_idle",
            "barsik_fire_walk_0", "barsik_fire_walk_1", "barsik_fire_walk_2", "barsik_fire_walk_3",
            "barsik_fire_jump", "barsik_fire_idle",
            "muska_idle", "tapok_walk_0", "tapok_walk_1",
            "krysa_pot_walk_0", "krysa_pot_walk_1", "krysa_pot_shell",
            "vorona_flap_0", "vorona_flap_1",
            "rukavichka_idle_0", "rukavichka_idle_1",
            "milk_carton", "mouse_running_0", "mouse_running_1",
            "star_sardine", "green_yarn", "crunch_kibble",
        )
        spriteAssets = names.mapNotNull { name ->
            runCatching {
                ctx.assets.open("games/super_barsik/$name.png").use {
                    name to BitmapFactory.decodeStream(it).asImageBitmap()
                }
            }.getOrNull()
        }.toMap()
    }

    private fun startGame() {
        score = 0
        lives = 3
        coinsCollected = 0
        treatsTotal = 0
        levelIdx = 0
        loadLevel(0)
        stage = Stage.LEVEL_INTRO
        levelIntroTimer = 2.2f
    }

    private fun loadLevel(i: Int) {
        current = levels[i]
        levelWidthTiles = current.tiles[0].length
        tiles = Array(ROWS) { r -> CharArray(levelWidthTiles) { c -> current.tiles[r][c] } }
        enemies.clear()
        coins.clear()
        powerUps.clear()
        bullets.clear()
        particles.clear()
        sachki.clear()
        bossLetters.clear()
        pipeTops.clear()
        pipeSpawnTimers.clear()
        warpFlashT = 0f
        // Scan tiles for pipe tops (regular and warp) so рукавички can climb out.
        for (rr in 0 until ROWS) for (cc in 0 until current.tiles[0].length) {
            val k = tileOf(current.tiles[rr][cc])
            if (k == TileKind.PIPE_TL || k == TileKind.PIPE_TL_WARP) {
                pipeTops.add(cc to rr)
                pipeSpawnTimers.add(2.5f + Random.nextFloat() * 2.5f)
            }
        }
        bossPhase = 0
        bossFallT = -1f
        for ((kind, tx, ty) in current.enemies) {
            enemies += Enemy(tx * TS + (TS - 24f) / 2f, ty * TS, vx = (if (kind == EnemyKind.VORONA) 0f else -40f), vy = 0f, kind = kind)
        }
        for ((tx, ty) in current.coins) coins += Coin(tx * TS + (TS - 14f) / 2f, ty * TS + 6f)
        cameraX = 0f
        px = TS * 2f
        py = TS * (GROUND_Y_TILES - 2) - 2f
        pvx = 0f; pvy = 0f
        grounded = true
        coyoteT = 1f
        jumpBufferT = -1f
        fromWarp = false
        pdir = 1
        psize = if (i == 0) Size.SMALL else psize           // keep power-up between levels
        invincibleT = 0f
        starT = 0f
        deathPause = 0f
        winCelebration = 0f
        flagSlide = false
        levelTimeLeft = if (i == 9) 360f else 300f
        msg = ""
        // Boss spawn
        if (i == 9) {
            bossHp = 3
            bossX = (levelWidthTiles - 8) * TS
            bossY = TS * (GROUND_Y_TILES - 2) - 4f
            bossVx = 0f; bossVy = 0f
            bossPattern = 0f
        }
    }

    // ── Input ─────────────────────────────────────────────────────────────
    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        when (stage) {
            Stage.TITLE -> if (dx != 0 || dy != 0) startGame()
            Stage.FRIDGE_NOTE -> {}
            Stage.WIN -> {}
            Stage.LEVEL_INTRO -> {}
            Stage.PLAY -> {
                if (dx != 0) {
                    if (dpadX != dx) dpadHoldT = 0f
                    dpadX = dx
                } else {
                    dpadX = 0; dpadHoldT = 0f
                }
                if (dy < 0) tryJump()
                if (dy > 0 && grounded) tryEnterWarp(ctx)
            }
        }
    }

    private fun tryEnterWarp(ctx: GameContext) {
        if (warpFlashT > 0f) return
        val belowTy = ((py + playerH + 1f) / TS).toInt()
        val leftTx = (px / TS).toInt()
        val rightTx = ((px + playerW - 1f) / TS).toInt()
        val centreTx = ((px + playerW / 2f) / TS).toInt()
        if (!isWarpTop(tileAt(leftTx, belowTy)) && !isWarpTop(tileAt(centreTx, belowTy)) && !isWarpTop(tileAt(rightTx, belowTy))) return
        val target = current.warpTarget
        if (target in levels.indices && target > levelIdx) {
            // A real Mario-style WARP ZONE: descend the magenta pipe and skip
            // ahead a couple of worlds. Power-up size carries over.
            score += 2000
            ctx.sound.click()
            levelIdx = target
            loadLevel(target)
            fromWarp = true
            stage = Stage.LEVEL_INTRO
            levelIntroTimer = 2.4f
            return
        }
        // No warp target wired for this level — fall back to a coin/score bonus.
        warpFlashT = 1.0f
        score += 3000
        coinsCollected += 15
        if (coinsCollected >= 100) { lives += 1; coinsCollected -= 100 }
        msg = t("★ WARP BONUS +3000 ★", "★ ВАРП-БОНУС +3000 ★")
        msgTimer = 3f
        // Visual: shower of "✦" particles spraying up
        for (i in 0 until 24) particle(
            px + playerW / 2f, py + playerH / 2f,
            Color(0xFFFFCC44),
            vx = (Random.nextFloat() - 0.5f) * 260f,
            vy = -180f - Random.nextFloat() * 220f,
            text = "✦",
        )
        ctx.sound.click()
    }

    override fun onFire(ctx: GameContext) {
        when (stage) {
            Stage.TITLE -> startGame()
            Stage.FRIDGE_NOTE -> {
                fridgeNoteTimer = 0f
                if (levelIdx >= levels.size - 1) {
                    // Won the boss already? shouldn't happen, fridge note skipped on L10
                } else {
                    levelIdx++
                    loadLevel(levelIdx)
                    stage = Stage.LEVEL_INTRO
                    levelIntroTimer = 2.0f
                }
            }
            Stage.WIN -> {
                stage = Stage.TITLE
            }
            Stage.LEVEL_INTRO -> { levelIntroTimer = 0f; stage = Stage.PLAY }
            Stage.PLAY -> {
                if (deathPause > 0f) return
                fireDown = true
                fireHoldT = 0f
                tryJump()
                if (psize == Size.FIRE && fireShootCooldown <= 0f) {
                    bullets += Bullet(px + (if (pdir > 0) playerW else -10f), py + playerH / 2f - 6f,
                        vx = 360f * pdir, vy = -120f)
                    fireShootCooldown = 0.45f
                    ctx.sound.click()
                }
            }
        }
    }

    fun onFireUp() { fireDown = false; fireHoldT = -1f }
    override fun onSecondary(ctx: GameContext) { /* unused */ }

    private fun tryJump() {
        // Coyote time: still jumpable for ~0.1s after walking off a ledge. If
        // even that has lapsed (mid-air), remember the press and fire it the
        // instant Барсик lands (jump buffer). Both are pure precision aids —
        // the heights stay FIXED (walk ~3.75 tiles, run ~4.8), so a tap is
        // never random (Юджин 18.06: «то запрыгиваю, то нет»).
        if (!grounded && coyoteT > 0.10f) { jumpBufferT = 0f; return }
        val running = abs(pvx) > 165f
        pvy = if (running) -680f else -600f
        grounded = false
        coyoteT = 1f          // consume — no mid-air double jump
        jumpBufferT = -1f
    }

    override fun onBack(ctx: GameContext): Boolean {
        if (stage != Stage.TITLE) { stage = Stage.TITLE; return true }
        return false
    }

    // ── Physics / update ──────────────────────────────────────────────────
    override fun update(dt: Float, ctx: GameContext) {
        GameContextHolder.ctx = ctx
        time += dt
        when (stage) {
            Stage.TITLE -> return
            Stage.LEVEL_INTRO -> {
                levelIntroTimer -= dt
                if (levelIntroTimer <= 0f) stage = Stage.PLAY
                return
            }
            Stage.FRIDGE_NOTE -> {
                fridgeNoteTimer += dt
                return
            }
            Stage.WIN -> {
                winCelebration += dt
                return
            }
            Stage.PLAY -> { /* fallthrough */ }
        }

        if (deathPause > 0f) {
            deathPause -= dt
            if (deathPause <= 0f) {
                lives -= 1
                if (lives <= 0) {
                    if (score > best) { best = score; ctx.scores.set(id, score) }
                    stage = Stage.TITLE
                } else {
                    loadLevel(levelIdx)
                }
            }
            return
        }

        levelTimeLeft = max(0f, levelTimeLeft - dt)
        if (levelTimeLeft == 0f) { killPlayer(ctx); return }

        if (fireShootCooldown > 0f) fireShootCooldown -= dt
        if (invincibleT > 0f) invincibleT -= dt
        if (starT > 0f) starT -= dt
        if (msgTimer > 0f) msgTimer -= dt
        // Fire-hold timer still tracked for future use but no jump boost — see tryJump().
        if (fireDown && fireHoldT >= 0f) fireHoldT += dt

        // Player horizontal — slower base speed so the camera can keep up
        // and the levels read at human pace (Юджин 18.06: «скорость экрана и
        // котика разбалансированно»).
        // Calmer pace — Юджин 20.06: «несётся слишком быстро, неуправляем».
        // Walk is the default; RUN only after a deliberate ~0.7s hold. accel
        // softened so a tap doesn't lurch.
        val targetWalk = 118f
        val targetRun = 205f
        if (flagSlide) {
            pvx = 0f
        } else if (dpadX != 0) {
            dpadHoldT += dt
            val target = if (dpadHoldT > 0.7f) targetRun else targetWalk
            val accel = 650f
            pvx += accel * dpadX * dt
            if (dpadX > 0 && pvx > target) pvx = target
            if (dpadX < 0 && pvx < -target) pvx = -target
            pdir = dpadX
        } else {
            // Friction
            val f = if (grounded) 1200f else 400f
            if (pvx > 0f) pvx = max(0f, pvx - f * dt) else if (pvx < 0f) pvx = min(0f, pvx + f * dt)
        }

        // Gravity — softened a touch so the new fixed jumps arc cleanly
        pvy += 1300f * dt
        if (pvy > 720f) pvy = 720f

        // Horizontal move + tile collide
        movePlayer(pvx * dt, 0f)
        // Vertical move + tile collide
        val wasGrounded = grounded
        grounded = false
        movePlayer(0f, pvy * dt)
        if (!grounded && wasGrounded && pvy >= 0f) {
            // walked off ledge — keep falling
        }
        // Coyote-time + jump-buffer bookkeeping (precise platforming).
        if (grounded) coyoteT = 0f else coyoteT += dt
        if (jumpBufferT >= 0f) {
            jumpBufferT += dt
            if (grounded) tryJump()                          // landed in the buffer window → jump now
            else if (jumpBufferT > 0.12f) jumpBufferT = -1f  // window lapsed
        }
        // Fell into a pit?
        if (py > H + 80f) { killPlayer(ctx); return }

        // Camera follows player — Барсик sits closer to centre of screen
        // (45% from left), giving the player more lookahead. Right-only
        // progress lock as in classic platformers.
        val targetCam = (px - W * 0.45f).coerceIn(0f, levelWidthTiles * TS - W)
        if (targetCam > cameraX) cameraX = targetCam

        // Hard-pen Барсик to the visible window. The camera is right-locked, so
        // without a left clamp he could walk back off the left edge and vanish;
        // without a right clamp he could overrun the end of the world. Both read
        // as «вылетает за пределы экрана» (Юджин 20.06).
        if (px < cameraX) { px = cameraX; if (pvx < 0f) pvx = 0f }
        val rightLimit = levelWidthTiles * TS - playerW
        if (px > rightLimit) { px = rightLimit; if (pvx > 0f) pvx = 0f }

        // Pipe-top spawns — every few seconds each pipe coughs up a рукавичка.
        // Only spawn pipes that are visible (close to viewport) and not over-
        // populated.
        if (pipeTops.isNotEmpty()) {
            val maxLiveRukavichki = 4
            val liveRukavichki = enemies.count { it.alive && it.kind == EnemyKind.RUKAVICHKA }
            for (i in pipeTops.indices) {
                pipeSpawnTimers[i] -= dt
                if (pipeSpawnTimers[i] <= 0f) {
                    pipeSpawnTimers[i] = 4.5f + Random.nextFloat() * 2.5f
                    if (liveRukavichki >= maxLiveRukavichki) continue
                    val (tx, ty) = pipeTops[i]
                    val pipeWorldX = tx * TS
                    // Spawn only if the pipe is anywhere near the viewport (avoid
                    // off-screen spam).
                    if (pipeWorldX < cameraX - 100f || pipeWorldX > cameraX + W + 100f) continue
                    enemies += Enemy(
                        x = tx * TS + 4f,
                        y = ty * TS + 16f,                       // start half-buried in the pipe
                        vx = if (px < tx * TS) 40f else -40f,    // walk toward player
                        vy = 0f,
                        kind = EnemyKind.RUKAVICHKA,
                        emergingT = 1.0f,
                    )
                }
            }
        }
        if (warpFlashT > 0f) warpFlashT = max(0f, warpFlashT - dt * 1.2f)

        // Enemies
        for (e in enemies) {
            if (!e.alive) continue
            e.anim += dt
            if (e.stomped > 0f) { e.stomped -= dt; if (e.stomped <= 0f) e.alive = false; continue }
            if (e.kind == EnemyKind.VORONA) {
                // Vorona patrols an aerial arc
                e.x += e.vx * dt
                e.vy = sin(time * 1.6f + e.x * 0.005f) * 60f
                e.y += e.vy * dt
                // bounds
                if (e.x < cameraX - 60f) { e.vx = abs(e.vx).coerceAtLeast(45f); }
                if (e.x > cameraX + W + 60f) { e.vx = -abs(e.vx).coerceAtLeast(45f); }
                if (e.vx == 0f) e.vx = -55f
            } else if (e.kind == EnemyKind.RUKAVICHKA && e.emergingT > 0f) {
                // Climbing out of a pipe top — half-buried sprite rising into view.
                e.emergingT -= dt * 1.5f
                e.y -= 24f * dt
                if (e.emergingT <= 0f) e.emergingT = 0f
            } else if (e.kind == EnemyKind.KRYSA && e.inShell) {
                e.x += e.shellVx * dt
                e.vy += 1500f * dt
                e.y += e.vy * dt
                // basic floor collide
                val (cx, cy) = tileXY(e.x + 12f, e.y + 24f)
                val t = tileAt(cx, cy)
                if (solid(t)) { e.y = cy * TS - 24f; e.vy = 0f }
                // wall bounce — flip if hits solid
                val front = tileAt(((e.x + (if (e.shellVx > 0) 24f else -1f)) / TS).toInt(), ((e.y + 12f) / TS).toInt())
                if (solid(front)) { e.shellVx = -e.shellVx; e.x += e.shellVx * dt }
                // Shells take out other enemies they touch
                for (o in enemies) if (o !== e && o.alive && o.stomped == 0f && abs(o.x - e.x) < 20f && abs(o.y - e.y) < 20f) { o.alive = false; particle(o.x, o.y, Color(0xFFFFCC44)) }
            } else {
                e.x += e.vx * dt
                e.vy += 1500f * dt
                e.y += e.vy * dt
                // floor
                val (cx, cy) = tileXY(e.x + 12f, e.y + 24f)
                val t = tileAt(cx, cy)
                if (solid(t)) { e.y = cy * TS - 24f; e.vy = 0f }
                // wall ahead — flip
                val ahead = tileAt(((e.x + (if (e.vx > 0) 24f else -1f)) / TS).toInt(), ((e.y + 12f) / TS).toInt())
                if (solid(ahead)) e.vx = -e.vx
                // ledge ahead — flip (so they don't walk off platforms)
                val belowAhead = tileAt(((e.x + (if (e.vx > 0) 24f else 0f)) / TS).toInt(), ((e.y + 32f) / TS).toInt())
                if (!solid(belowAhead) && grounded) {/* allow walk off in some cases */ }
            }
            // collide player
            if (rectsOverlap(px, py, playerW, playerH, e.x, e.y, 22f, 22f)) handleEnemyCollision(e, ctx)
        }

        // Coins
        for (c in coins) {
            if (!c.alive) continue
            c.anim += dt
            if (rectsOverlap(px, py, playerW, playerH, c.x, c.y, 14f, 18f)) {
                c.alive = false
                score += 100
                coinsCollected += 1
                if (coinsCollected % 100 == 0) lives += 1
                ctx.sound.click()
            }
        }

        // Power-ups
        for (p in powerUps) {
            if (!p.alive) continue
            if (p.rising > 0f) {
                p.rising -= dt * 2.4f
                p.y -= 36f * dt
                if (p.rising <= 0f) p.rising = 0f
                continue
            }
            if (p.kind == PowerUpKind.SIGNATURE || p.kind == PowerUpKind.STAR || p.kind == PowerUpKind.MILK) {
                p.vx = if (p.vx == 0f) 110f else p.vx
                p.x += p.vx * dt
                p.vy += 1500f * dt
                p.y += p.vy * dt
                val (cx, cy) = tileXY(p.x + 8f, p.y + 18f)
                if (solid(tileAt(cx, cy))) { p.y = cy * TS - 18f; p.vy = if (p.kind == PowerUpKind.STAR) -360f else 0f }
                val ahead = tileAt(((p.x + (if (p.vx > 0) 16f else -1f)) / TS).toInt(), ((p.y + 8f) / TS).toInt())
                if (solid(ahead)) p.vx = -p.vx
            } else if (p.kind == PowerUpKind.MOUSE) {
                // Mouse runs away from player
                val away = if (p.x < px) -1f else 1f
                p.vx = 180f * away
                p.x += p.vx * dt
                p.vy += 1500f * dt
                p.y += p.vy * dt
                val (cx, cy) = tileXY(p.x + 8f, p.y + 18f)
                if (solid(tileAt(cx, cy))) { p.y = cy * TS - 18f; p.vy = 0f }
                val ahead = tileAt(((p.x + (if (p.vx > 0) 16f else -1f)) / TS).toInt(), ((p.y + 8f) / TS).toInt())
                if (solid(ahead)) { p.vx = -p.vx }
            } else if (p.kind == PowerUpKind.GREEN_BALL) {
                p.vx = if (p.vx == 0f) 90f else p.vx
                p.x += p.vx * dt
                p.vy += 1500f * dt
                p.y += p.vy * dt
                val (cx, cy) = tileXY(p.x + 8f, p.y + 18f)
                if (solid(tileAt(cx, cy))) { p.y = cy * TS - 18f; p.vy = -260f }
                val ahead = tileAt(((p.x + (if (p.vx > 0) 16f else -1f)) / TS).toInt(), ((p.y + 8f) / TS).toInt())
                if (solid(ahead)) p.vx = -p.vx
            }
            if (rectsOverlap(px, py, playerW, playerH, p.x, p.y, 18f, 18f)) collectPowerUp(p, ctx)
        }

        // Bullets
        for (b in bullets) {
            if (!b.alive) continue
            b.x += b.vx * dt
            b.vy += 900f * dt
            b.y += b.vy * dt
            val (cx, cy) = tileXY(b.x, b.y)
            if (solid(tileAt(cx, cy))) {
                if (b.vy > 0f) { b.y = cy * TS - 8f; b.vy = -260f; b.bounces += 1 }
                else b.alive = false
                if (b.bounces > 3) b.alive = false
            }
            if (b.x < cameraX - 30f || b.x > cameraX + W + 30f) b.alive = false
            for (e in enemies) if (e.alive && e.stomped == 0f && rectsOverlap(b.x - 6f, b.y - 6f, 12f, 12f, e.x, e.y, 22f, 22f)) {
                b.alive = false; e.alive = false; score += 200; particle(e.x, e.y, Color(0xFFFFCC44))
            }
            if (bossPhase == 0 && rectsOverlap(b.x - 6f, b.y - 6f, 12f, 12f, bossX, bossY, 36f, 56f)) {
                b.alive = false; hitBoss(ctx)
            }
        }
        bullets.removeAll { !it.alive }

        // Particles
        for (pt in particles) {
            pt.x += pt.vx * dt; pt.y += pt.vy * dt
            pt.vy += 800f * dt
            pt.life -= dt
        }
        particles.removeAll { it.life <= 0f }

        // Boss update on level 10
        if (levelIdx == 9) updateBoss(dt, ctx)

        // ── Level-completion check ───────────────────────────────────────
        if (levelIdx != 9) {
            // Touch the flag column — slide down then walk to fridge.
            val flagPoleX = findFlagColumnX()
            if (!flagSlide && flagPoleX > 0f && px + playerW > flagPoleX && px < flagPoleX + TS) {
                flagSlide = true
                pvx = 0f
                msgTimer = 0f
                ctx.sound.click()
                score += 2000
                // Bonus per remaining time
                score += (levelTimeLeft.toInt()) * 10
            }
            if (flagSlide) {
                pvy = 0f
                // Feet on the GROUND row (13), not one tile above it — Barsik
                // was walking his victory stretch 32px up in the air.
                py = min(py + 160f * dt, TS * GROUND_Y_TILES - playerH)
                if (py >= TS * GROUND_Y_TILES - playerH - 0.5f) {
                    // Walk right toward fridge automatically
                    pvx = 120f
                    px += pvx * dt
                    val fridgeX = findFridgeX()
                    if (fridgeX > 0f && px + playerW >= fridgeX) {
                        stage = Stage.FRIDGE_NOTE
                        fridgeNoteTimer = 0f
                    }
                }
            }
        }
    }

    private fun findFlagColumnX(): Float {
        for (c in 0 until levelWidthTiles) for (r in 0 until ROWS) if (tiles[r][c] == 'P') return c * TS
        return -1f
    }

    private fun findFridgeX(): Float {
        for (c in 0 until levelWidthTiles) for (r in 0 until ROWS) if (tiles[r][c] == 'R') return c * TS
        return -1f
    }

    private fun killPlayer(ctx: GameContext) {
        if (deathPause > 0f) return
        if (psize != Size.SMALL && invincibleT <= 0f) {
            // Take a hit instead of dying
            psize = Size.SMALL
            invincibleT = 1.6f
            ctx.sound.click()
            return
        }
        ctx.sound.gameOver()
        deathPause = 1.4f
        for (i in 0 until 12) particle(px + playerW / 2f, py + playerH / 2f, Color(0xFFFFAA55), vx = (Random.nextFloat() - 0.5f) * 200f, vy = -200f - Random.nextFloat() * 200f)
    }

    private fun handleEnemyCollision(e: Enemy, ctx: GameContext) {
        if (starT > 0f) { e.alive = false; score += 200; particle(e.x, e.y, Color(0xFFFFCC44)); return }
        val stompedFromAbove = pvy > 0f && py + playerH - 10f < e.y + 6f
        if (stompedFromAbove && e.kind != EnemyKind.VORONA) {
            if (e.kind == EnemyKind.KRYSA && !e.inShell) {
                e.inShell = true; e.shellVx = 0f; e.vx = 0f
            } else if (e.kind == EnemyKind.KRYSA && e.inShell) {
                e.shellVx = if (px < e.x) 360f else -360f
            } else {
                e.stomped = 0.3f
            }
            pvy = -340f
            score += 100
            ctx.sound.click()
        } else if (stompedFromAbove && e.kind == EnemyKind.VORONA) {
            // Voronas can be stomped too — same idea.
            e.alive = false
            pvy = -340f
            score += 200
            ctx.sound.click()
        } else {
            if (e.kind == EnemyKind.KRYSA && e.inShell && abs(e.shellVx) < 0.1f) {
                // Touching a stationary shell kicks it
                e.shellVx = if (px < e.x) 360f else -360f
            } else if (invincibleT <= 0f) {
                killPlayer(ctx)
            }
        }
    }

    private fun collectPowerUp(p: PowerUp, ctx: GameContext) {
        p.alive = false
        when (p.kind) {
            PowerUpKind.MILK -> { if (psize == Size.SMALL) { psize = Size.BIG; py -= 22f }; score += 500; ctx.sound.click() }
            PowerUpKind.MOUSE -> { psize = if (psize == Size.SMALL) Size.BIG else Size.FIRE; if (psize == Size.BIG) py -= 22f; score += 700; ctx.sound.click() }
            PowerUpKind.STAR -> { starT = 9f; score += 1000; ctx.sound.click() }
            PowerUpKind.GREEN_BALL -> { lives += 1; score += 200; ctx.sound.click() }
            PowerUpKind.SIGNATURE -> {
                treatsTotal += 1
                score += 1500
                msg = "★ ${p.signatureName} ★"
                msgTimer = 2.5f
                ctx.sound.click()
            }
        }
    }

    private fun spawnFromQBlock(tx: Int, ty: Int, ctx: GameContext) {
        // Replace ? with used-? sprite, drop the appropriate item out the top.
        if (ty in 0 until ROWS && tx in 0 until levelWidthTiles && tiles[ty][tx] == '?') tiles[ty][tx] = '_'
        val signature = current.signatureAt
        val isSignature = (tx == signature.first && ty == signature.second)
        val px0 = tx * TS + 8f
        val py0 = ty * TS - 4f
        val item = when {
            isSignature -> PowerUp(px0, py0, vx = 90f, vy = 0f, kind = PowerUpKind.SIGNATURE, signatureName = current.signatureTreat)
            psize == Size.SMALL -> PowerUp(px0, py0, vx = 90f, vy = 0f, kind = PowerUpKind.MILK)
            else -> PowerUp(px0, py0, vx = 0f, vy = 0f, kind = PowerUpKind.MOUSE)
        }
        powerUps += item
        ctx.sound.click()
    }

    private fun particle(x: Float, y: Float, c: Color, vx: Float = (Random.nextFloat() - 0.5f) * 120f, vy: Float = -180f - Random.nextFloat() * 80f, text: String? = null) {
        particles += Particle(x, y, vx, vy, life = 0.9f + Random.nextFloat() * 0.5f, color = c, text = text)
    }

    // ── Tile sampling ────────────────────────────────────────────────────
    private fun tileXY(x: Float, y: Float): Pair<Int, Int> = (x / TS).toInt() to (y / TS).toInt()
    private fun tileAt(tx: Int, ty: Int): TileKind {
        if (tx < 0 || tx >= levelWidthTiles || ty < 0 || ty >= ROWS) return TileKind.SKY
        return tileOf(tiles[ty][tx])
    }

    // Move player by (dx, dy) sub-stepping for clean collisions.
    private fun movePlayer(dx: Float, dy: Float) {
        val steps = max(1, (max(abs(dx), abs(dy)) / 8f).toInt())
        val sx = dx / steps; val sy = dy / steps
        repeat(steps) {
            if (sx != 0f) {
                px += sx
                // Wall collide
                if (sx > 0f) {
                    val front = px + playerW
                    val tx = (front / TS).toInt()
                    val ty1 = (py / TS).toInt(); val ty2 = ((py + playerH - 1f) / TS).toInt()
                    if (solid(tileAt(tx, ty1)) || solid(tileAt(tx, ty2))) {
                        px = tx * TS - playerW
                        pvx = 0f
                    }
                } else if (sx < 0f) {
                    val tx = (px / TS).toInt()
                    val ty1 = (py / TS).toInt(); val ty2 = ((py + playerH - 1f) / TS).toInt()
                    if (solid(tileAt(tx, ty1)) || solid(tileAt(tx, ty2))) {
                        px = (tx + 1) * TS
                        pvx = 0f
                    }
                    // Hard wall at the left edge of the level — Барсик must
                    // not back out of the world (Юджин 18.06: «убегает назад
                    // за обрез экрана»).
                    if (px < 0f) { px = 0f; pvx = 0f }
                }
            }
            if (sy != 0f) {
                py += sy
                if (sy > 0f) {
                    val bottom = py + playerH
                    val ty = (bottom / TS).toInt()
                    val tx1 = (px / TS).toInt(); val tx2 = ((px + playerW - 1f) / TS).toInt()
                    if (solid(tileAt(tx1, ty)) || solid(tileAt(tx2, ty))) {
                        py = ty * TS - playerH
                        pvy = 0f
                        grounded = true
                    }
                } else if (sy < 0f) {
                    val ty = (py / TS).toInt()
                    val tx1 = (px / TS).toInt(); val tx2 = ((px + playerW - 1f) / TS).toInt()
                    val left = tileAt(tx1, ty); val right = tileAt(tx2, ty)
                    if (solid(left) || solid(right)) {
                        py = (ty + 1) * TS
                        pvy = 0f
                        // Head-bump: trigger ?-block if applicable
                        val ctxLocal = GameContextHolder.ctx
                        for (sample in listOf(tx1 to ty, tx2 to ty)) {
                            val k = tileAt(sample.first, sample.second)
                            if (k == TileKind.QBLOCK && ctxLocal != null) {
                                spawnFromQBlock(sample.first, sample.second, ctxLocal)
                            } else if (k == TileKind.BRICK && psize != Size.SMALL) {
                                if (sample.second in 0 until ROWS && sample.first in 0 until levelWidthTiles) {
                                    tiles[sample.second][sample.first] = '.'
                                    for (i in 0 until 4) particle(sample.first * TS + 16f, sample.second * TS + 8f, Color(0xFFC09060))
                                    score += 50
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun rectsOverlap(ax: Float, ay: Float, aw: Float, ah: Float, bx: Float, by: Float, bw: Float, bh: Float): Boolean =
        ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by

    // ── Boss ─────────────────────────────────────────────────────────────
    private fun updateBoss(dt: Float, ctx: GameContext) {
        if (bossPhase == 1) {
            bossFallT += dt
            bossY += 460f * dt
            bossVx *= 0.99f
            bossX += bossVx * dt
            if (bossY > H - 40f) {
                bossPhase = 2
                bossFallT = 0f
                // Splash
                for (i in 0 until 20) particle(bossX, H - 30f, Color(0xFF553015), vx = (Random.nextFloat() - 0.5f) * 280f, vy = -200f - Random.nextFloat() * 120f)
                ctx.sound.gameOver()
            }
            return
        }
        if (bossPhase == 2) {
            bossFallT += dt
            // Pop one letter at intervals
            val pops = listOf("S", "H", "I", "T")
            val idx = (bossFallT / 0.55f).toInt()
            if (idx in pops.indices && bossLetters.size <= idx) {
                bossLetters += Particle(
                    cameraX + W * (0.3f + idx * 0.13f), H - 16f, vx = 0f, vy = -180f,
                    life = 8f, color = Color(0xFF6B3920), text = pops[idx], size = 26f
                )
                ctx.sound.click()
            }
            for (p in bossLetters) {
                p.x = p.x   // pinned horizontally
                p.y += p.vy * dt
                p.vy += 380f * dt
                if (p.y > H - 38f) { p.y = H - 38f; p.vy = -abs(p.vy) * 0.55f }
            }
            if (bossFallT > 4.5f) {
                stage = Stage.WIN
                winCelebration = 0f
                if (score > best) { best = score; ctx.scores.set(id, score) }
            }
            return
        }
        // Active phase
        bossPattern += dt
        // Drift slowly
        bossVx = sin(bossPattern * 0.8f) * 40f
        bossX += bossVx * dt
        // Stomp: Barsik can bounce on Сачколов's head. Without this the boss
        // is ONLY damageable by FIRE bullets — and L10 has no ?-blocks, so a
        // player arriving SMALL/BIG could never win (softlock finale).
        if (rectsOverlap(px, py, playerW, playerH, bossX, bossY, 36f, 56f)) {
            if (pvy > 0f && py + playerH < bossY + 24f) {
                pvy = -420f
                hitBoss(ctx)
            } else if (invincibleT <= 0f && starT <= 0f) {
                killPlayer(ctx)
            }
        }
        // Throw sachok (net) periodically
        if (bossPattern.rem(1.6f) < dt) {
            val dx = px - bossX
            val ang = -PI.toFloat() / 4f
            sachki += Bullet(bossX, bossY + 12f, vx = (-160f) + (if (dx < 0) -60f else 60f), vy = -260f)
        }
        for (s in sachki) {
            s.x += s.vx * dt
            s.vy += 800f * dt
            s.y += s.vy * dt
            if (s.y > H - 20f) s.alive = false
            if (rectsOverlap(s.x - 12f, s.y - 12f, 24f, 24f, px, py, playerW, playerH)) {
                s.alive = false
                killPlayer(ctx)
            }
        }
        sachki.removeAll { !it.alive }
    }

    private fun hitBoss(ctx: GameContext) {
        bossHp -= 1
        score += 1500
        particle(bossX, bossY, Color(0xFFFFCC44))
        ctx.sound.click()
        if (bossHp <= 0) {
            bossPhase = 1
            bossVx = -160f
            bossVy = -240f
        }
    }

    // ── Rendering ────────────────────────────────────────────────────────
    override fun draw(scope: DrawScope, ctx: GameContext) {
        GameContextHolder.ctx = ctx
        when (stage) {
            Stage.TITLE -> drawTitle(scope)
            Stage.LEVEL_INTRO -> { drawWorld(scope); drawLevelIntro(scope) }
            Stage.FRIDGE_NOTE -> { drawWorld(scope); drawFridgeNote(scope) }
            Stage.WIN -> drawWin(scope)
            Stage.PLAY -> drawWorld(scope)
        }
    }

    private fun drawTitle(scope: DrawScope) = with(scope) {
        drawRect(Color(0xFFFFE0A0), topLeft = Offset(0f, 0f), size = Size(W, H))
        // Decorative pasta-trim stripes
        drawRect(Color(0xFFE05030), topLeft = Offset(0f, 0f), size = Size(W, 20f))
        drawRect(Color(0xFF40A040), topLeft = Offset(0f, H - 20f), size = Size(W, 20f))
        shadowedTitle("SUPER БАРСИК", 320f, 100f, Color(0xFFE05030), 40f)
        shadowedTitle("BROS.", 320f, 140f, Color(0xFF40A040), 32f)
        // Subtitle
        drawDosText(t("a homage to the 1985 platformer", "оммаж платформеру 1985 года,"), 320f, 180f, Color(0xFF402010), 14f, false, TextAlign.CENTER)
        drawDosText(t("that revived the home console", "который воскресил домашние консоли"), 320f, 200f, Color(0xFF402010), 14f, false, TextAlign.CENTER)

        // Draw a small Barsik silhouette
        drawBarsik(this, 290f, 240f, dir = 1, size = Size.SMALL, anim = time, animOverride = 0f)
        // Muska silhouette on the right
        drawMuska(this, 360f, 240f)

        drawDosText(t("PRESS SPACE TO START", "ПРОБЕЛ — СТАРТ"), 320f, 360f, Color(0xFF402010), 16f, true, TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText("MAMMA MIA. POEKHALI.", 320f, 390f, Color(0xFFC02020), 14f, true, TextAlign.CENTER)
        drawDosText("HI ${best}", 8f, 22f, Color(0xFF402010), 12f, true)
    }

    private fun shadowedTitle(text: String, x: Float, y: Float, c: Color, size: Float) = with(this) {}

    private fun DrawScope.shadowedTitle(text: String, x: Float, y: Float, c: Color, size: Float) {
        drawDosText(text, x + 3f, y + 3f, Color(0xFF200000), size, true, TextAlign.CENTER)
        drawDosText(text, x, y, c, size, true, TextAlign.CENTER)
    }

    // The narrator's voice between levels. Антонио — the Brooklyn plumber who
    // refuses to say his own name (the lawyers from Kyoto, you understand) and
    // who handed Барсику the torch in the campfire intro. His off-screen voice
    // is the game's signature feature, so it threads through every level card,
    // not just the one-time intro (Юджин 20.06: «верни рассказчика»). Each line
    // is a wink at the 1985 cartridge, kept as a hint — never the names.
    private val antonioAsides = listOf(
        "♪ Antonio: one jump changes everything, amico." to "♪ Антонио: один прыжок меняет всё, амико.",
        "♪ Antonio: pipes are my trade. Now yours too." to "♪ Антонио: трубы — моя профессия. Теперь и твоя.",
        "♪ Antonio: no turtles here — rats in saucepans. Jump." to "♪ Антонио: черепах нет — есть крысы в кастрюлях. Прыгай.",
        "♪ Antonio: from the Alamogordo pit — straight here, bambino." to "♪ Антонио: из ямы в Аламогордо — прямо сюда, бамбино.",
        "♪ Antonio: it's dark underground, but the Whiskas waits." to "♪ Антонио: под землёй темно, но Вискас ждёт.",
        "♪ Antonio: drink the milk — you grow twice the size." to "♪ Антонио: выпей молока — станешь вдвое больше.",
        "♪ Antonio: Muska is on the balcony across. Don't fail her." to "♪ Антонио: Муська на балконе напротив. Не подведи.",
        "♪ Antonio: \"wrong castle, signore. Keep going.\"" to "♪ Антонио: «не та крепость, синьор. Иди дальше.»",
        "♪ Antonio: almost, meow-bambino. I believe in you." to "♪ Антонио: почти, мяу-бамбино. Я в тебя верю.",
        "♪ Antonio: the finale. Netcatcher — into the pit. Andiamo!" to "♪ Антонио: финал. Сачколова — в яму. Поехали!",
    )

    private fun drawLevelIntro(scope: DrawScope) = with(scope) {
        drawRect(Color.Black.copy(alpha = 0.7f), topLeft = Offset(0f, H * 0.30f), size = Size(W, H * 0.40f))
        drawDosText(t("LEVEL", "УРОВЕНЬ") + " ${levelIdx + 1}", 320f, H * 0.43f, Color(0xFFFFCC44), 28f, true, TextAlign.CENTER)
        drawDosText(if (ru()) current.name else current.nameEn, 320f, H * 0.50f, Color.White, 22f, true, TextAlign.CENTER)
        drawDosText(t("today's hunt: ${current.treatEn}", "сегодня охотимся за: ${current.signatureTreat}"), 320f, H * 0.58f, Color(0xFFFFD8A0), 14f, false, TextAlign.CENTER)
        if (fromWarp)
            drawDosText(t("✦ WARP ZONE — WORLDS SKIPPED! ✦", "✦ ВАРП-ЗОНА — ПЕРЕПРЫГНУЛИ МИРЫ! ✦"), 320f, H * 0.36f, Color(0xFFFF66FF), 14f, true, TextAlign.CENTER)
        val aside = antonioAsides.getOrElse(levelIdx) { antonioAsides.first() }
        drawDosText(t(aside.first, aside.second), 320f, H * 0.66f, Color(0xFFA8D8FF), 13f, false, TextAlign.CENTER)
    }

    private fun drawFridgeNote(scope: DrawScope) = with(scope) {
        drawRect(Color(0xFFE8E2C8), topLeft = Offset(W * 0.10f, H * 0.20f), size = Size(W * 0.80f, H * 0.60f))
        drawRect(Color(0xFF402010), topLeft = Offset(W * 0.10f, H * 0.20f), size = Size(W * 0.80f, 4f))
        drawRect(Color(0xFF402010), topLeft = Offset(W * 0.10f, H * 0.80f - 4f), size = Size(W * 0.80f, 4f))
        drawDosText(t("a note on the fridge:", "записка из холодильника:"), 320f, H * 0.30f, Color(0xFF402010), 14f, false, TextAlign.CENTER)
        drawDosText(t("\"your Whiskas is in another fridge,", "«твой Вискас в другом холодильнике,"), 320f, H * 0.42f, Color(0xFFA03020), 18f, true, TextAlign.CENTER)
        drawDosText(t("Barsik!\"", "Барсик!»"), 320f, H * 0.50f, Color(0xFFA03020), 18f, true, TextAlign.CENTER)
        drawDosText(t("— Antonio (not my name, but it sounds right)", "— Антонио (имя не моё, но звучит правильно)"), 320f, H * 0.62f, Color(0xFF402010), 13f, false, TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText(t("PRESS SPACE", "ЖМИ ПРОБЕЛ"), 320f, H * 0.74f, Color(0xFF402010), 14f, true, TextAlign.CENTER)
    }

    private fun drawWin(scope: DrawScope) = with(scope) {
        // Night roof under moon
        drawRect(Color(0xFF101030), topLeft = Offset(0f, 0f), size = Size(W, H))
        // Moon
        drawCircle(Color(0xFFFFEEC8), 80f, Offset(W * 0.18f, H * 0.22f))
        drawCircle(Color(0xFFE8DCB0), 60f, Offset(W * 0.18f, H * 0.22f))
        // Stars
        val rng = Random(42)
        for (i in 0 until 30) {
            val sx = rng.nextFloat() * W
            val sy = rng.nextFloat() * H * 0.4f
            drawRect(Color.White, topLeft = Offset(sx, sy), size = Size(2f, 2f))
        }
        // Roof horizon
        drawRect(Color(0xFF2A1810), topLeft = Offset(0f, H - 80f), size = Size(W, 80f))
        drawRect(Color(0xFF402818), topLeft = Offset(0f, H - 84f), size = Size(W, 4f))
        // Two cats on roof
        drawBarsik(this, W * 0.30f, H - 110f, 1, Size.SMALL, time, animOverride = 0f)
        drawMuska(this, W * 0.55f, H - 110f)
        // Cart of Whiskas between them
        drawRect(Color(0xFF402010), topLeft = Offset(W * 0.42f, H - 96f), size = Size(36f, 28f))
        drawRect(Color(0xFFC02020), topLeft = Offset(W * 0.42f + 4f, H - 96f + 4f), size = Size(28f, 14f))
        drawDosText("Whiskas", W * 0.42f + 18f, H - 96f + 14f, Color.White, 8f, true, TextAlign.CENTER)
        drawCircle(Color.Black, 5f, Offset(W * 0.42f + 6f, H - 68f))
        drawCircle(Color.Black, 5f, Offset(W * 0.42f + 30f, H - 68f))
        // Title
        drawDosText("FIN.", W / 2f, 90f, Color(0xFFFFCC44), 40f, true, TextAlign.CENTER)
        drawDosText(t("Barsik and Muska. On the roof. Under the moon.", "Барсик и Муська. На крыше. Под луной."), W / 2f, 140f, Color(0xFFFFE0A8), 14f, false, TextAlign.CENTER)
        drawDosText(t("SCORE", "ОЧКИ") + ": $score", W / 2f, 180f, Color.White, 16f, true, TextAlign.CENTER)
        drawDosText(t("treats collected", "вкусняшек собрано") + ": $treatsTotal / ${levels.size}", W / 2f, 200f, Color(0xFFFFE0A8), 14f, false, TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText(t("PRESS SPACE", "ЖМИ ПРОБЕЛ"), W / 2f, H - 30f, Color.White, 14f, true, TextAlign.CENTER)
    }

    private fun drawWorld(scope: DrawScope) = with(scope) {
        drawRect(current.skyColor, topLeft = Offset(0f, 0f), size = Size(W, H))
        // Simple parallax: 3 cloud/decor shapes
        val parallax = cameraX * 0.3f
        for (i in 0 until 5) {
            val cx = (i * 200f - (parallax % 200f))
            drawRect(Color.White.copy(alpha = 0.65f), topLeft = Offset(cx, 50f + (i % 2) * 30f), size = Size(60f, 18f))
            drawRect(Color.White.copy(alpha = 0.55f), topLeft = Offset(cx + 8f, 42f + (i % 2) * 30f), size = Size(44f, 12f))
        }

        // Tiles
        val startTx = (cameraX / TS).toInt().coerceAtLeast(0)
        val endTx = ((cameraX + W) / TS).toInt().coerceAtMost(levelWidthTiles - 1)
        for (c in startTx..endTx) for (r in 0 until ROWS) {
            val kx = c * TS - cameraX
            val ky = r * TS
            when (tiles[r][c]) {
                'G' -> drawGroundTile(this, kx, ky, current.groundColor)
                'H' -> drawHardTile(this, kx, ky, current.groundColor)
                'B' -> drawBrickTile(this, kx, ky, current.brickColor)
                '?' -> drawQuestionTile(this, kx, ky, time)
                '_' -> drawUsedBlockTile(this, kx, ky)
                '[' -> { drawPipeTopLeft(this, kx, ky) }
                ']' -> { drawPipeTopRight(this, kx, ky) }
                '(' -> { drawPipeTopLeftWarp(this, kx, ky, time) }
                ')' -> { drawPipeTopRightWarp(this, kx, ky, time) }
                '{' -> { drawPipeBodyLeft(this, kx, ky) }
                '}' -> { drawPipeBodyRight(this, kx, ky) }
                'P' -> { drawFlagpole(this, kx, ky) }
                'F' -> { drawFlagTop(this, kx, ky) }
                'R' -> { drawFridgeTile(this, kx, ky) }
                else -> { /* sky */ }
            }
        }

        // Coins
        for (co in coins) if (co.alive) drawCoin(this, co.x - cameraX, co.y, co.anim)
        // Power-ups
        for (p in powerUps) if (p.alive) drawPowerUp(this, p, cameraX)
        // Enemies
        for (e in enemies) if (e.alive) drawEnemy(this, e, cameraX, time)
        // Bullets
        for (b in bullets) if (b.alive) drawBullet(this, b.x - cameraX, b.y)
        // Particles
        for (p in particles) {
            if (p.text != null) drawDosText(p.text, p.x - cameraX, p.y, p.color, p.size, true, TextAlign.CENTER)
            else drawRect(p.color, topLeft = Offset(p.x - cameraX - p.size / 2f, p.y - p.size / 2f), size = Size(p.size, p.size))
        }
        // Boss
        if (levelIdx == 9 && bossPhase != 2) drawBoss(this)
        // Boss letters (S H I T) — drawn in world but pinned screen-side; they were created with absolute screen coords
        for (l in bossLetters) drawDosText(l.text ?: "", l.x - cameraX, l.y, l.color, l.size, true, TextAlign.CENTER)
        // Sachki
        for (s in sachki) if (s.alive) drawSachok(this, s.x - cameraX, s.y, time)

        // Player
        val flicker = invincibleT > 0f && ((time * 14f).toInt() % 2 == 0)
        if (!flicker) drawBarsik(this, px - cameraX, py, pdir, psize, time, animOverride = if (flagSlide) 0f else null, star = starT > 0f)

        // HUD
        drawHud(this)

        // Transient message (timer is decremented in update — draw has no dt).
        if (msgTimer > 0f) {
            drawDosText(msg, W / 2f, 56f, Color(0xFFFFCC44), 16f, true, TextAlign.CENTER)
        }
    }

    private fun drawHud(scope: DrawScope) = with(scope) {
        drawRect(Color.Black.copy(alpha = 0.45f), topLeft = Offset(0f, 0f), size = Size(W, 28f))
        drawDosText("SCORE ${score.toString().padStart(6, '0')}", 6f, 18f, Color.White, 12f, true)
        drawDosText("LIFE x$lives", 130f, 18f, Color(0xFFFFCC44), 12f, true)
        drawDosText(t("KIBBLE", "Х/КОРМ") + " x${coinsCollected.toString().padStart(2, '0')}", 220f, 18f, Color(0xFFFFFFAA), 12f, true)
        drawDosText("TIME ${levelTimeLeft.toInt().toString().padStart(3, '0')}", 340f, 18f, Color(0xFFFFAAAA), 12f, true)
        drawDosText("WORLD ${levelIdx + 1}-1", 440f, 18f, Color.White, 12f, true)
        drawDosText(t("TODAY: ${current.treatEn}", "СЕГОДНЯ: ${current.signatureTreat}"), 540f, 18f, Color(0xFFFFE0A8), 11f, true)
    }

    // ── Sprite primitives (procedural — placeholders for Кира's art) ─────
    private fun drawGroundTile(s: DrawScope, x: Float, y: Float, base: Color) = with(s) {
        drawRect(base, topLeft = Offset(x, y), size = Size(TS, TS))
        drawRect(lighter(base, 0.25f), topLeft = Offset(x, y), size = Size(TS, 3f))
        drawRect(darker(base, 0.35f), topLeft = Offset(x, y + TS - 3f), size = Size(TS, 3f))
        // pixel noise
        drawRect(darker(base, 0.20f), topLeft = Offset(x + 6f, y + 10f), size = Size(3f, 3f))
        drawRect(darker(base, 0.20f), topLeft = Offset(x + 22f, y + 18f), size = Size(3f, 3f))
    }
    private fun drawHardTile(s: DrawScope, x: Float, y: Float, base: Color) = with(s) {
        val c = darker(base, 0.20f)
        drawRect(c, topLeft = Offset(x, y), size = Size(TS, TS))
        drawRect(lighter(c, 0.25f), topLeft = Offset(x, y), size = Size(TS, 2f))
        drawRect(darker(c, 0.4f), topLeft = Offset(x, y + TS - 2f), size = Size(TS, 2f))
    }
    private fun drawBrickTile(s: DrawScope, x: Float, y: Float, base: Color) = with(s) {
        drawRect(base, topLeft = Offset(x, y), size = Size(TS, TS))
        drawRect(darker(base, 0.45f), topLeft = Offset(x, y + 14f), size = Size(TS, 2f))
        drawRect(darker(base, 0.45f), topLeft = Offset(x + 16f, y), size = Size(2f, 14f))
        drawRect(darker(base, 0.45f), topLeft = Offset(x + 8f, y + 16f), size = Size(2f, 14f))
        drawRect(lighter(base, 0.2f), topLeft = Offset(x, y), size = Size(TS, 2f))
    }
    private fun drawQuestionTile(s: DrawScope, x: Float, y: Float, t: Float) = with(s) {
        val pulse = 0.85f + 0.15f * sin(t * 4f)
        drawRect(Color(0xFFD8A040), topLeft = Offset(x, y), size = Size(TS, TS))
        drawRect(Color(0xFFA06820), topLeft = Offset(x, y + TS - 3f), size = Size(TS, 3f))
        drawRect(Color(0xFFFFCC60), topLeft = Offset(x, y), size = Size(TS, 3f))
        drawDosText("?", x + TS / 2f, y + 24f, Color.Black.copy(alpha = pulse), 22f, true, TextAlign.CENTER)
    }
    private fun drawUsedBlockTile(s: DrawScope, x: Float, y: Float) = with(s) {
        drawRect(Color(0xFF704030), topLeft = Offset(x, y), size = Size(TS, TS))
        drawRect(Color(0xFF402010), topLeft = Offset(x, y + TS - 3f), size = Size(TS, 3f))
    }
    private fun drawPipeTopLeftWarp(s: DrawScope, x: Float, y: Float, t: Float) = with(s) {
        val pulse = 0.6f + 0.4f * sin(t * 6f)
        drawRect(Color(0xFFB040C0), topLeft = Offset(x, y + 4f), size = Size(TS, TS - 4f))
        drawRect(Color(0xFFE060FF).copy(alpha = pulse), topLeft = Offset(x, y), size = Size(TS - 4f, 4f))
        drawRect(Color(0xFF601866), topLeft = Offset(x + 6f, y + 4f), size = Size(4f, TS - 4f))
        drawDosText("▼", x + TS / 2f, y + 24f, Color.White.copy(alpha = pulse), 14f, true, TextAlign.CENTER)
    }
    private fun drawPipeTopRightWarp(s: DrawScope, x: Float, y: Float, t: Float) = with(s) {
        val pulse = 0.6f + 0.4f * sin(t * 6f)
        drawRect(Color(0xFFB040C0), topLeft = Offset(x, y + 4f), size = Size(TS, TS - 4f))
        drawRect(Color(0xFFE060FF).copy(alpha = pulse), topLeft = Offset(x + 4f, y), size = Size(TS - 4f, 4f))
        drawRect(Color(0xFF601866), topLeft = Offset(x + TS - 10f, y + 4f), size = Size(4f, TS - 4f))
    }
    private fun drawPipeTopLeft(s: DrawScope, x: Float, y: Float) = with(s) {
        drawRect(Color(0xFF40A040), topLeft = Offset(x, y + 4f), size = Size(TS, TS - 4f))
        drawRect(Color(0xFF60D060), topLeft = Offset(x, y), size = Size(TS - 4f, 4f))
        drawRect(Color(0xFF205020), topLeft = Offset(x + 6f, y + 4f), size = Size(4f, TS - 4f))
    }
    private fun drawPipeTopRight(s: DrawScope, x: Float, y: Float) = with(s) {
        drawRect(Color(0xFF40A040), topLeft = Offset(x, y + 4f), size = Size(TS, TS - 4f))
        drawRect(Color(0xFF60D060), topLeft = Offset(x + 4f, y), size = Size(TS - 4f, 4f))
        drawRect(Color(0xFF205020), topLeft = Offset(x + TS - 10f, y + 4f), size = Size(4f, TS - 4f))
    }
    private fun drawPipeBodyLeft(s: DrawScope, x: Float, y: Float) = with(s) {
        drawRect(Color(0xFF40A040), topLeft = Offset(x, y), size = Size(TS, TS))
        drawRect(Color(0xFF205020), topLeft = Offset(x + 6f, y), size = Size(4f, TS))
    }
    private fun drawPipeBodyRight(s: DrawScope, x: Float, y: Float) = with(s) {
        drawRect(Color(0xFF40A040), topLeft = Offset(x, y), size = Size(TS, TS))
        drawRect(Color(0xFF205020), topLeft = Offset(x + TS - 10f, y), size = Size(4f, TS))
    }
    private fun drawFlagpole(s: DrawScope, x: Float, y: Float) = with(s) {
        drawRect(Color(0xFFB0B0B0), topLeft = Offset(x + 14f, y), size = Size(4f, TS))
    }
    private fun drawFlagTop(s: DrawScope, x: Float, y: Float) = with(s) {
        // Sardine on top of the pole — silver oval with a horizontal stripe
        drawRect(Color(0xFFB0B0B0), topLeft = Offset(x + 14f, y + 8f), size = Size(4f, TS - 8f))
        drawCircle(Color(0xFFC8C8D0), 12f, Offset(x + 16f, y + 14f))
        drawCircle(Color(0xFF707080), 5f, Offset(x + 10f, y + 14f))   // head
        drawRect(Color(0xFFE8E8F0), topLeft = Offset(x + 12f, y + 13f), size = Size(14f, 2f))
    }
    private fun drawFridgeTile(s: DrawScope, x: Float, y: Float) = with(s) {
        drawRect(Color(0xFFEEEEEE), topLeft = Offset(x, y), size = Size(TS, TS))
        drawRect(Color(0xFFBBBBBB), topLeft = Offset(x + TS - 4f, y), size = Size(4f, TS))
        drawRect(Color(0xFF606060), topLeft = Offset(x + TS - 8f, y + 10f), size = Size(3f, 12f))
    }

    private fun drawCoin(s: DrawScope, x: Float, y: Float, anim: Float) = with(s) {
        val ww = 10f + 4f * sin(anim * 6f)
        drawRect(Color(0xFFFFCC44), topLeft = Offset(x + (14f - ww) / 2f, y), size = Size(ww, 16f))
        drawRect(Color(0xFFFFEE88), topLeft = Offset(x + (14f - ww) / 2f, y + 2f), size = Size(ww, 3f))
    }
    private fun drawBullet(s: DrawScope, x: Float, y: Float) = with(s) {
        drawRect(Color(0xFF604020), topLeft = Offset(x - 6f, y - 4f), size = Size(12f, 8f))
        drawRect(Color(0xFF402810), topLeft = Offset(x - 3f, y + 2f), size = Size(6f, 2f))
    }
    private fun drawSachok(s: DrawScope, x: Float, y: Float, t: Float) = with(s) {
        // a butterfly net
        drawRect(Color(0xFF704030), topLeft = Offset(x, y), size = Size(2f, 16f))
        drawCircle(Color(0xFFE8E8E8).copy(alpha = 0.7f), 10f, Offset(x + 1f, y - 4f))
        drawCircle(Color.Transparent, 8f, Offset(x + 1f, y - 4f))
    }

    private fun DrawScope.drawSpriteAsset(
        name: String,
        x: Float,
        y: Float,
        w: Float,
        h: Float,
        flipX: Boolean = false,
        alpha: Float = 1f,
    ): Boolean {
        val img = spriteAssets[name] ?: return false
        if (flipX) {
            scale(scaleX = -1f, scaleY = 1f, pivot = Offset(x + w / 2f, y + h / 2f)) {
                drawImage(
                    img,
                    dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
                    dstSize = IntSize(w.roundToInt(), h.roundToInt()),
                    alpha = alpha,
                )
            }
        } else {
            drawImage(
                img,
                dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
                dstSize = IntSize(w.roundToInt(), h.roundToInt()),
                alpha = alpha,
            )
        }
        return true
    }

    private fun drawPowerUp(s: DrawScope, p: PowerUp, cam: Float) = with(s) {
        val x = p.x - cam
        val y = p.y
        when (p.kind) {
            PowerUpKind.MILK -> {
                if (drawSpriteAsset("milk_carton", x, y, 16f, 18f)) return@with
                drawRect(Color(0xFFEEEEEE), topLeft = Offset(x, y), size = Size(16f, 18f))
                drawRect(Color(0xFFEEDDAA), topLeft = Offset(x + 1f, y + 1f), size = Size(14f, 5f))
                drawRect(Color(0xFFC8C8C8), topLeft = Offset(x, y + 16f), size = Size(16f, 2f))
            }
            PowerUpKind.MOUSE -> {
                val frame = ((time * 8f).toInt() and 1)
                if (drawSpriteAsset("mouse_running_$frame", x, y, 16f, 18f)) return@with
                drawRect(Color(0xFFA0A0A0), topLeft = Offset(x, y + 4f), size = Size(16f, 12f))
                drawCircle(Color(0xFFA0A0A0), 5f, Offset(x + 14f, y + 6f))
                drawCircle(Color(0xFFA0A0A0), 5f, Offset(x + 2f, y + 6f))
                drawCircle(Color.Black, 2f, Offset(x + 14f, y + 10f))
                drawRect(Color(0xFF806060), topLeft = Offset(x - 3f, y + 12f), size = Size(6f, 2f))
            }
            PowerUpKind.STAR -> {
                if (drawSpriteAsset("star_sardine", x, y, 20f, 14f)) return@with
                val pulse = 0.7f + 0.3f * sin(time * 8f)
                drawRect(Color(0xFFC0C0E8), topLeft = Offset(x, y + 2f), size = Size(20f, 8f))
                drawRect(Color(0xFFE0E0FF).copy(alpha = pulse), topLeft = Offset(x + 4f, y), size = Size(12f, 14f))
                drawDosText("★", x + 10f, y + 12f, Color(0xFFFFEE88), 16f, true, TextAlign.CENTER)
            }
            PowerUpKind.GREEN_BALL -> {
                if (drawSpriteAsset("green_yarn", x, y, 18f, 18f)) return@with
                drawCircle(Color(0xFF50C040), 9f, Offset(x + 8f, y + 9f))
                drawCircle(Color(0xFF305830), 7f, Offset(x + 8f, y + 9f))
            }
            PowerUpKind.SIGNATURE -> {
                if (drawSpriteAsset("crunch_kibble", x, y, 14f, 16f)) return@with
                // Pulsing red gift box with crown
                val pulse = 0.85f + 0.15f * sin(time * 6f)
                drawRect(Color(0xFFC02828).copy(alpha = pulse), topLeft = Offset(x - 2f, y), size = Size(20f, 18f))
                drawRect(Color(0xFFFFCC44), topLeft = Offset(x + 6f, y), size = Size(4f, 18f))
                drawRect(Color(0xFFFFCC44), topLeft = Offset(x - 2f, y + 8f), size = Size(20f, 4f))
            }
        }
    }

    private fun drawEnemy(s: DrawScope, e: Enemy, cam: Float, t: Float) = with(s) {
        val x = e.x - cam
        val y = e.y
        when (e.kind) {
            EnemyKind.TAPOK -> {
                // A slipper: brown sole + dirty white inner
                val anim = ((e.anim * 8f).toInt() % 2) * 1f
                if (drawSpriteAsset("tapok_walk_${anim.toInt()}", x, y, 22f, 22f, flipX = e.vx > 0f)) {
                    if (e.stomped > 0f) {
                        drawRect(Color(0xFF402010), topLeft = Offset(x - 2f, y + 18f), size = Size(28f, 4f))
                    }
                    return@with
                }
                drawRect(Color(0xFF402010), topLeft = Offset(x - 2f, y + 14f), size = Size(28f, 8f))
                drawRect(Color(0xFFC0A88C), topLeft = Offset(x, y + 6f), size = Size(22f, 12f))
                drawRect(Color(0xFF806848), topLeft = Offset(x, y + 6f), size = Size(22f, 2f))
                drawRect(Color(0xFFE8C0A0), topLeft = Offset(x + 2f + anim, y + 8f), size = Size(8f, 4f))
                drawCircle(Color.Black, 2f, Offset(x + 4f, y + 12f))
                drawCircle(Color.Black, 2f, Offset(x + 10f, y + 12f))
                if (e.stomped > 0f) {
                    drawRect(Color(0xFF402010), topLeft = Offset(x - 2f, y + 18f), size = Size(28f, 4f))
                }
            }
            EnemyKind.KRYSA -> {
                // Rat sitting in a pot
                val frame = ((e.anim * 6f).toInt() and 1)
                val sprite = if (e.inShell) "krysa_pot_shell" else "krysa_pot_walk_$frame"
                if (drawSpriteAsset(sprite, x - 2f, y, 28f, 24f, flipX = e.vx > 0f)) return@with
                drawRect(Color(0xFF505050), topLeft = Offset(x - 2f, y + 4f), size = Size(28f, 20f))
                drawRect(Color(0xFF303030), topLeft = Offset(x - 4f, y + 4f), size = Size(32f, 4f))
                drawRect(Color(0xFF707070), topLeft = Offset(x - 2f, y + 22f), size = Size(28f, 2f))
                if (!e.inShell) {
                    drawCircle(Color(0xFFB08868), 6f, Offset(x + 12f, y + 4f))
                    drawCircle(Color(0xFFFFC0A0), 2f, Offset(x + 16f, y + 5f))
                    drawCircle(Color.Black, 1.5f, Offset(x + 14f, y + 4f))
                } else {
                    drawDosText("Zz", x + 12f, y + 18f, Color(0xFFCCCCCC), 10f, true, TextAlign.CENTER)
                }
            }
            EnemyKind.VORONA -> {
                val flap = (sin(t * 12f + e.x * 0.01f) * 4f)
                val frame = ((t * 10f).toInt() and 1)
                if (drawSpriteAsset("vorona_flap_$frame", x - 4f, y, 28f, 16f, flipX = e.vx < 0f)) return@with
                // Body (wedge)
                drawRect(Color(0xFF202020), topLeft = Offset(x, y + 5f), size = Size(22f, 11f))
                drawRect(Color.Black, topLeft = Offset(x + 2f, y + 4f), size = Size(18f, 4f))
                // Wings flapping
                drawRect(Color(0xFF303030), topLeft = Offset(x - 6f, y + 6f - flap), size = Size(10f, 5f))
                drawRect(Color(0xFF303030), topLeft = Offset(x + 18f, y + 6f - flap), size = Size(10f, 5f))
                drawRect(Color.Black, topLeft = Offset(x - 4f, y + 8f - flap * 0.5f), size = Size(6f, 2f))
                drawRect(Color.Black, topLeft = Offset(x + 20f, y + 8f - flap * 0.5f), size = Size(6f, 2f))
                // Beak
                drawRect(Color(0xFFFFAA00), topLeft = Offset(x + 20f, y + 9f), size = Size(5f, 3f))
                // Eye (white sclera + black pupil)
                drawCircle(Color.White, 2.5f, Offset(x + 16f, y + 8f))
                drawCircle(Color.Black, 1.5f, Offset(x + 17f, y + 8f))
                // Tail feather
                drawRect(Color(0xFF101010), topLeft = Offset(x - 4f, y + 12f), size = Size(6f, 3f))
            }
            EnemyKind.RUKAVICHKA -> {
                // A bristle grooming-glove rising from a pipe — a fluffy
                // half-cylinder with stiff bristles on top. The cat fears it.
                val emerge = e.emergingT
                val bodyTop = y + (if (emerge > 0f) 14f * emerge else 0f)
                val frame = ((t * 4f).toInt() and 1)
                if (drawSpriteAsset("rukavichka_idle_$frame", x, bodyTop, 22f, 24f)) {
                    if (e.stomped > 0f) drawRect(Color(0xFF902040), topLeft = Offset(x, bodyTop + 18f), size = Size(22f, 4f))
                    return@with
                }
                // Mitten body
                drawRect(Color(0xFFC83048), topLeft = Offset(x, bodyTop + 6f), size = Size(22f, 16f))
                drawRect(Color(0xFF902040), topLeft = Offset(x, bodyTop + 18f), size = Size(22f, 4f))
                drawRect(Color(0xFFE85C70), topLeft = Offset(x + 2f, bodyTop + 8f), size = Size(18f, 3f))
                // Wrist cuff
                drawRect(Color(0xFFE0E0E0), topLeft = Offset(x - 1f, bodyTop + 18f), size = Size(24f, 3f))
                drawRect(Color(0xFFB0B0B0), topLeft = Offset(x - 1f, bodyTop + 20f), size = Size(24f, 1f))
                // Bristles on top (5 stiff teeth)
                if (emerge < 0.3f) {
                    for (i in 0 until 5) {
                        val bx = x + 2f + i * 4f
                        drawRect(Color(0xFF402010), topLeft = Offset(bx, bodyTop), size = Size(2f, 6f))
                    }
                }
                // Angry beady eyes
                if (emerge < 0.5f) {
                    drawCircle(Color.Black, 1.5f, Offset(x + 7f, bodyTop + 12f))
                    drawCircle(Color.Black, 1.5f, Offset(x + 15f, bodyTop + 12f))
                    drawRect(Color(0xFF601820), topLeft = Offset(x + 9f, bodyTop + 16f), size = Size(4f, 1f))
                }
                if (e.stomped > 0f) drawRect(Color(0xFF902040), topLeft = Offset(x, bodyTop + 18f), size = Size(22f, 4f))
            }
        }
    }

    private fun drawBoss(s: DrawScope) = with(s) {
        val x = bossX - cameraX
        val y = bossY
        // Body
        drawRect(Color(0xFF606060), topLeft = Offset(x, y), size = Size(36f, 56f))
        drawRect(Color(0xFF404040), topLeft = Offset(x, y + 50f), size = Size(36f, 6f))
        // Head
        drawCircle(Color(0xFFD0A080), 14f, Offset(x + 18f, y - 8f))
        // Hat (dog-catcher cap)
        drawRect(Color(0xFF202020), topLeft = Offset(x + 4f, y - 18f), size = Size(28f, 8f))
        drawRect(Color(0xFF202020), topLeft = Offset(x + 2f, y - 12f), size = Size(32f, 4f))
        drawRect(Color(0xFFC02020), topLeft = Offset(x + 6f, y - 16f), size = Size(8f, 4f))
        // Net in hand
        drawRect(Color(0xFF604020), topLeft = Offset(x + 32f, y - 4f), size = Size(2f, 18f))
        drawCircle(Color(0xFFE0E0E0).copy(alpha = 0.5f), 8f, Offset(x + 33f, y - 8f))
        // Mean eyes
        drawCircle(Color.Black, 2f, Offset(x + 14f, y - 10f))
        drawCircle(Color.Black, 2f, Offset(x + 22f, y - 10f))
        // HP pips
        for (i in 0 until bossHp) drawRect(Color(0xFFE04040), topLeft = Offset(x + i * 12f, y - 32f), size = Size(8f, 4f))
        // Brown sludge at the bottom for the bridge level
        if (levelIdx == 9) {
            val sludgeY = H - 30f
            drawRect(Color(0xFF402010), topLeft = Offset(0f, sludgeY), size = Size(W, 30f))
            for (i in 0 until 30) {
                val bx = (i * 26f + (time * 30f).rem(26f))
                val by = sludgeY - 2f + sin(time * 3f + i.toFloat()) * 3f
                drawCircle(Color(0xFF604030), 4f, Offset(bx, by))
            }
        }
    }

    private fun drawBarsik(s: DrawScope, x: Float, y: Float, dir: Int, size: Size, anim: Float, animOverride: Float? = null, star: Boolean = false) = with(s) {
        val w = if (size == Size.SMALL) 22f else 26f
        val h = if (size == Size.SMALL) 28f else 50f
        val family = when (size) {
            Size.SMALL -> "barsik_small"
            Size.BIG -> "barsik_big"
            Size.FIRE -> "barsik_fire"
        }
        val frameName = when {
            animOverride != null -> "${family}_idle"
            !grounded -> "${family}_jump"
            abs(pvx) < 4f -> "${family}_idle"
            else -> "${family}_walk_${((anim * 10f).toInt() and 3)}"
        }
        val spriteAlpha = if (star && (anim * 14f).toInt() % 2 == 0) 0.68f else 1f
        if (drawSpriteAsset(frameName, x, y, w, h, flipX = dir < 0, alpha = spriteAlpha)) return@with
        val orange = if (star && (anim * 14f).toInt() % 2 == 0) Color(0xFFE8E0A0) else Color(0xFFEC8230)
        val orangeDim = darker(orange, 0.3f)
        val orangeDark = darker(orange, 0.55f)
        val white = Color(0xFFFFE8C8)
        val phase = (animOverride ?: anim) * 8f
        val bob = (sin(phase) * 1.5f).toInt().toFloat()

        // Outline (one pixel inset on all sides) — gives a NES-style silhouette
        drawRect(orangeDark, topLeft = Offset(x - 1f, y + 4f + bob), size = Size(w + 2f, h - bob))
        // Body
        drawRect(orange, topLeft = Offset(x + 2f, y + 6f + bob), size = Size(w - 4f, h - 6f - bob))
        // Stripey shading (3 horizontal tabby stripes)
        drawRect(orangeDim, topLeft = Offset(x + 4f, y + h / 3f + bob), size = Size(w - 8f, 1f))
        drawRect(orangeDim, topLeft = Offset(x + 4f, y + h / 2f + bob), size = Size(w - 8f, 1f))
        drawRect(orangeDim, topLeft = Offset(x + 5f, y + 2f * h / 3f + bob), size = Size(w - 10f, 1f))
        // Belly patch
        drawRect(white, topLeft = Offset(x + 4f, y + h / 2f + bob), size = Size(w - 8f, h / 2f - 6f - bob))
        // Feet (two paw blobs at the bottom — animate alternation when walking)
        val walking = animOverride == null && (abs(phase) > 0.1f)
        val pawShift = if (walking) ((phase / PI).toInt() % 2) else 0
        drawRect(orangeDark, topLeft = Offset(x + 2f, y + h - 4f), size = Size(6f, 4f))
        drawRect(orangeDark, topLeft = Offset(x + w - 8f, y + h - 4f), size = Size(6f, 4f))
        drawRect(Color(0xFFFFC09A), topLeft = Offset(x + 3f + pawShift, y + h - 3f), size = Size(4f, 2f))
        drawRect(Color(0xFFFFC09A), topLeft = Offset(x + w - 7f - pawShift, y + h - 3f), size = Size(4f, 2f))
        // Head
        drawRect(orangeDark, topLeft = Offset(x - 1f, y + 3f + bob), size = Size(w + 2f, 16f))
        drawRect(orange, topLeft = Offset(x, y + 4f + bob), size = Size(w, 14f))
        // White muzzle patch
        drawRect(white, topLeft = Offset(x + w / 2f - 4f, y + 12f + bob), size = Size(8f, 5f))
        // Ears (big!)
        val earL = Path().apply {
            moveTo(x, y + 4f + bob); lineTo(x + 2f, y - 4f + bob); lineTo(x + 8f, y + 4f + bob); close()
        }
        val earR = Path().apply {
            moveTo(x + w - 8f, y + 4f + bob); lineTo(x + w - 2f, y - 4f + bob); lineTo(x + w, y + 4f + bob); close()
        }
        drawPath(earL, orange); drawPath(earR, orange)
        drawCircle(Color(0xFFFFB48C), 2.5f, Offset(x + 4f, y - 1f + bob))
        drawCircle(Color(0xFFFFB48C), 2.5f, Offset(x + w - 4f, y - 1f + bob))
        // Eyes — bigger, white sclera + black pupil
        val eyeY = y + 10f + bob
        drawCircle(Color.White, 2.5f, Offset(x + 6f, eyeY))
        drawCircle(Color.White, 2.5f, Offset(x + w - 6f, eyeY))
        val pupilDx = if (dir > 0) 0.8f else -0.8f
        drawCircle(Color.Black, 1.5f, Offset(x + 6f + pupilDx, eyeY))
        drawCircle(Color.Black, 1.5f, Offset(x + w - 6f + pupilDx, eyeY))
        // Nose (pink triangle)
        drawCircle(Color(0xFFE03060), 1.8f, Offset(x + w / 2f, y + 14f + bob))
        // Smile — two short curves under the nose
        drawRect(orangeDark, topLeft = Offset(x + w / 2f - 3f, y + 16f + bob), size = Size(2f, 1f))
        drawRect(orangeDark, topLeft = Offset(x + w / 2f + 1f, y + 16f + bob), size = Size(2f, 1f))
        // Whiskers (three each side)
        for (i in 0 until 3) {
            drawRect(orangeDark, topLeft = Offset(x - 5f, y + 12f + bob + i * 2f), size = Size(5f, 1f))
            drawRect(orangeDark, topLeft = Offset(x + w, y + 12f + bob + i * 2f), size = Size(5f, 1f))
        }
        // Tail — curls up and animates
        val tailUp = sin(phase) * 4f
        drawRect(orangeDark, topLeft = Offset(x + (if (dir > 0) -5f else w), y + h / 2f - tailUp - 1f), size = Size(6f, h / 3f + 2f))
        drawRect(orange, topLeft = Offset(x + (if (dir > 0) -4f else w + 1f), y + h / 2f - tailUp), size = Size(4f, h / 3f))
        // Fire mode hint (small flame symbol below)
        if (size == Size.FIRE) {
            drawRect(Color(0xFFFFCC44), topLeft = Offset(x, y + h - 6f), size = Size(w, 2f))
            drawDosText("☆", x + w / 2f, y - 6f + bob, Color(0xFFFFCC44), 10f, true, TextAlign.CENTER)
        }
    }

    private fun drawMuska(s: DrawScope, x: Float, y: Float) = with(s) {
        val w = 22f; val h = 28f
        if (drawSpriteAsset("muska_idle", x, y, w, h)) return@with
        val gray = Color(0xFF888888)
        val grayDim = Color(0xFF555555)
        drawRect(gray, topLeft = Offset(x + 2f, y + 6f), size = Size(w - 4f, h - 6f))
        drawRect(grayDim, topLeft = Offset(x + 2f, y + h - 6f), size = Size(w - 4f, 4f))
        drawRect(Color(0xFFC8C8C8), topLeft = Offset(x + 4f, y + h / 2f), size = Size(w - 8f, h / 2f - 6f))
        drawRect(gray, topLeft = Offset(x, y + 4f), size = Size(w, 12f))
        val earL = Path().apply { moveTo(x, y + 4f); lineTo(x + 2f, y - 2f); lineTo(x + 6f, y + 4f); close() }
        val earR = Path().apply { moveTo(x + w - 6f, y + 4f); lineTo(x + w - 2f, y - 2f); lineTo(x + w, y + 4f); close() }
        drawPath(earL, gray); drawPath(earR, gray)
        drawCircle(Color(0xFFFFB4D0), 1.5f, Offset(x + 3f, y + 1f))
        drawCircle(Color(0xFFFFB4D0), 1.5f, Offset(x + w - 3f, y + 1f))
        drawCircle(Color(0xFF40A0E0), 2f, Offset(x + 6f, y + 10f))
        drawCircle(Color(0xFF40A0E0), 2f, Offset(x + w - 6f, y + 10f))
        // Pink bow on the head
        drawRect(Color(0xFFFF80B0), topLeft = Offset(x + w / 2f - 4f, y), size = Size(8f, 4f))
        drawRect(Color(0xFFC04880), topLeft = Offset(x + w / 2f - 1f, y), size = Size(2f, 4f))
    }

    private fun lighter(c: Color, a: Float) = Color(c.red + (1 - c.red) * a, c.green + (1 - c.green) * a, c.blue + (1 - c.blue) * a, c.alpha)
    private fun darker(c: Color, a: Float) = Color(c.red * (1 - a), c.green * (1 - a), c.blue * (1 - a), c.alpha)

    override fun dispose() {
        // The process-lifetime holder must not retain the Activity-bearing
        // GameContext (and its view tree) after the game screen is gone.
        GameContextHolder.ctx = null
    }
}

private object GameContextHolder { var ctx: GameContext? = null }
