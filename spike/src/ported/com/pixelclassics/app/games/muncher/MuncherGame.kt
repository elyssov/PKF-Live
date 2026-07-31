package com.pixelclassics.app.games.muncher

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

/**
 * MUNCHER — eat-the-dots in a maze. Trademark-safe re-imagining of a
 * 1980 classic: the hero is a square muncher (not a yellow circle),
 * antagonists are "spooks" (not ghosts), the maze tiles are square
 * and the corridor pattern is our own.
 */
class MuncherGame : Game {
    override val id: String = "muncher"
    override val titleResId: Int = R.string.game_muncher
    override val backgroundColor: Color = Color.Black
    override val controls: GameControls = GameControls.DPAD
    override val introText: String = MUNCHER_INTRO_EN
    override fun introTextFor(lang: String): String = pickMuncherIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PARCHMENT
    override val helpLines: List<String> = listOf(
        "D-pad — steer · eat all dots to advance",
        "Power pellets (○) — spooks turn blue, eat them for bonus",
        "Tunnel edges (◀ ▶) wrap around the maze",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — руление · съешь все точки, чтобы пройти",
        "Энерджайзеры (○) — духи синеют, ешь их за бонус",
        "Края туннеля (◀ ▶) заворачивают через лабиринт",
    ) else helpLines

    // Maze: 21 columns × 23 rows. '#' wall, '.' dot, 'o' power-pill,
    // ' ' empty corridor, 'S' spook spawn, 'P' muncher spawn, 'T'
    // tunnel teleport edge. Our own layout — not Pac-Man's.
    private val mazeSrc = listOf(
        "#####################",
        "#.........#.........#",
        "#o###.###.#.###.###o#",
        "#...................#",
        "#.###.#.#####.#.###.#",
        "#.....#...#...#.....#",
        "#####.### # ###.#####",
        "#####.#       #.#####",
        "T....   # S #   ....T",
        "#####.# ##### #.#####",
        "#####.#       #.#####",
        "#####.# ##### #.#####",
        "#...................#",
        "#.###.#####.#####.###",
        "#...#.....P.....#...#",
        "###.#.###.#.###.#.###",
        "#.....#...#...#.....#",
        "#.#######.#.#######.#",
        "#o.................o#",
        "#.###.###.#.###.###.#",
        "#...................#",
        "#####################",
        "#####################",
    )
    private val cols = mazeSrc[0].length
    private val rows = mazeSrc.size
    private val tile = 22f
    private val hudH = 36f
    private val W = cols * tile               // 462
    private val H = rows * tile + hudH         // 506+ for hud
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private val boardY get() = hudH

    private var grid: Array<CharArray> = Array(rows) { CharArray(cols) }
    private var pellets = 0

    private data class Mob(var x: Float, var y: Float, var dx: Int, var dy: Int,
                           var frightened: Float = 0f, val color: Color, var alive: Boolean = true,
                           var decidedCell: Int = -1,
                           var respawnTimer: Float = 0f)

    private val munch = Mob(0f, 0f, 1, 0, color = Color(0xFFFCC830))
    private val spooks = mutableListOf<Mob>()
    private var queuedDir: Pair<Int, Int>? = null
    private var time = 0f
    private var score = 0
    private var best = 0
    private var lives = 3
    private var level = 1
    private var won = false
    private var dead = false
    // Post-death mercy window: spooks can't hurt the muncher while it runs.
    private var grace = 0f

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        lives = 3; score = 0; level = 1
        startLevel()
    }

    private fun startLevel() {
        grid = Array(rows) { mazeSrc[it].toCharArray() }
        pellets = 0
        for (r in 0 until rows) for (c in 0 until cols) if (grid[r][c] == '.') pellets++
        // Place muncher and spooks at their spawn tiles.
        val pSpawn = findCell('P') ?: (10 to 14)
        munch.x = (pSpawn.first + 0.5f) * tile
        munch.y = boardY + (pSpawn.second + 0.5f) * tile
        munch.dx = 1; munch.dy = 0
        val sSpawn = findCell('S') ?: (10 to 8)
        spooks.clear()
        val cols4 = listOf(Color(0xFFFF1744), Color(0xFFFF8DA1), Color(0xFF00B0FF), Color(0xFFFFB000))
        for (i in 0 until 4) {
            val (pc, pr) = penCell(sSpawn, i)
            spooks += Mob(
                (pc + 0.5f) * tile,
                boardY + (pr + 0.5f) * tile,
                dx = if (i % 2 == 0) -1 else 1, dy = 0,
                color = cols4[i],
            )
        }
        queuedDir = null
        won = false; dead = false
        grace = 0f
    }

    /** After a life is lost: everyone back to spawn + a short mercy window. */
    private fun resetAfterDeath() {
        val pSpawn = findCell('P') ?: (10 to 14)
        munch.x = (pSpawn.first + 0.5f) * tile
        munch.y = boardY + (pSpawn.second + 0.5f) * tile
        munch.dx = 1; munch.dy = 0
        queuedDir = null
        val sSpawn = findCell('S') ?: (10 to 8)
        for ((i, s) in spooks.withIndex()) {
            val (pc, pr) = penCell(sSpawn, i)
            s.x = (pc + 0.5f) * tile
            s.y = boardY + (pr + 0.5f) * tile
            s.dx = if (i % 2 == 0) -1 else 1; s.dy = 0
            s.frightened = 0f
            s.alive = true; s.respawnTimer = 0f
            s.decidedCell = -1
        }
        grace = 2f
    }

    /** Free pen cells around the 'S' spawn — the old (i-1.5)·tile spread
     *  parked spook #3 inside the wall at column 12. */
    private fun penCell(s: Pair<Int, Int>, i: Int): Pair<Int, Int> = when (i) {
        0 -> s.first - 1 to s.second
        1 -> s.first to s.second
        2 -> s.first + 1 to s.second
        else -> s.first to s.second - 1
    }

    private fun findCell(ch: Char): Pair<Int, Int>? {
        for (r in 0 until rows) for (c in 0 until cols) if (grid[r][c] == ch) return c to r
        return null
    }

    private fun cellAt(x: Float, y: Float): Pair<Int, Int> {
        val c = (x / tile).toInt().coerceIn(0, cols - 1)
        val r = ((y - boardY) / tile).toInt().coerceIn(0, rows - 1)
        return c to r
    }

    private fun isWall(c: Int, r: Int): Boolean {
        if (r !in 0 until rows) return true
        // Tunnel edges allow wrap.
        if (c !in 0 until cols) return grid[r][0] != 'T' && grid[r][cols - 1] != 'T'
        return grid[r][c] == '#'
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (dead || won) return
        if (dx == 0 && dy == 0) return   // ignore D-pad release: Muncher keeps gliding (Pac-Man rule)
        queuedDir = dx to dy
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (dead || won) return
        if (grace > 0f) grace -= dt

        val speed = 90f + level * 6f
        // Apply queued direction at intersection (when muncher is roughly centred on a cell).
        val (mc, mr) = cellAt(munch.x, munch.y)
        val centreX = (mc + 0.5f) * tile
        val centreY = boardY + (mr + 0.5f) * tile
        // Generous tolerance — Юджин жалуется что "жмёшь вниз, едет вправо".
        // Симптом: игрок жмёт новое направление вне зоны центра → queuedDir
        // не применяется → мунчер продолжает по СТАРОМУ dir. Большая зона +
        // мгновенный 180° разворот без ожидания центра = отзывчивое управление.
        val tol = (tile / 3f).coerceAtLeast(6f)
        val nearCentre = abs(munch.x - centreX) < tol && abs(munch.y - centreY) < tol
        queuedDir?.let { (qdx, qdy) ->
            val isReverse = qdx == -munch.dx && qdy == -munch.dy
            val canTurn = !isWall(mc + qdx, mr + qdy)
            if (canTurn && (nearCentre || isReverse)) {
                munch.dx = qdx; munch.dy = qdy
                // Snap to the rail so a turn doesn't shave the corner.
                if (qdx != 0) munch.y = centreY
                if (qdy != 0) munch.x = centreX
                if (isReverse) { munch.x = centreX; munch.y = centreY }
                queuedDir = null
            }
        }
        // Stop at wall.
        val nextC = mc + munch.dx; val nextR = mr + munch.dy
        val movingForward = (munch.dx != 0 || munch.dy != 0)
        if (movingForward && isWall(nextC, nextR) && nearCentre) {
            munch.x = centreX; munch.y = centreY
        } else {
            munch.x += munch.dx * speed * dt
            munch.y += munch.dy * speed * dt
        }
        // Tunnel wrap.
        if (munch.x < -tile / 2f) munch.x = W + tile / 2f
        if (munch.x > W + tile / 2f) munch.x = -tile / 2f

        // Eat dot / pill.
        if (nearCentre) {
            when (grid[mr][mc]) {
                '.' -> { grid[mr][mc] = ' '; score += 10; pellets--; ctx.sound.eat() }
                'o' -> {
                    grid[mr][mc] = ' '; score += 50; ctx.sound.powerUp()
                    for (s in spooks) if (s.alive) s.frightened = 7f
                }
            }
        }
        if (pellets <= 0 && !won) {
            won = true; level++; score += 200
            if (score > best) { best = score; ctx.scores.set(id, score) }
            ctx.sound.win()
        }

        // Spooks update.
        for (s in spooks) {
            if (!s.alive) {
                s.respawnTimer -= dt
                if (s.respawnTimer <= 0f) {
                    val spawn = findCell('S') ?: (10 to 8)
                    s.x = (spawn.first + 0.5f) * tile
                    s.y = boardY + (spawn.second + 0.5f) * tile
                    s.dx = if (Random.nextBoolean()) 1 else -1
                    s.dy = 0
                    s.alive = true
                    s.frightened = 0f
                    s.decidedCell = -1
                }
                continue
            }
            s.frightened = (s.frightened - dt).coerceAtLeast(0f)
            val sSpd = if (s.frightened > 0f) speed * 0.55f else speed * 0.9f
            val (sc, sr) = cellAt(s.x, s.y)
            val sCentreX = (sc + 0.5f) * tile
            val sCentreY = boardY + (sr + 0.5f) * tile
            val sTol = (sSpd * dt + 1f).coerceAtLeast(4f)
            val sNearCentre = abs(s.x - sCentreX) < sTol && abs(s.y - sCentreY) < sTol
            // Decide ONCE per cell. The old code re-decided (and re-snapped to
            // the cell centre) every frame while inside the tolerance zone —
            // at ~1.4px/frame vs a 4px zone the snap always won, freezing all
            // spooks at their spawn centres. (Юджин: «призраки стоят на месте».)
            val cellKey = sr * cols + sc
            if (sNearCentre && s.decidedCell != cellKey) {
                s.decidedCell = cellKey
                // Choose direction at intersection. Prefer not to reverse unless dead-end.
                val candidates = mutableListOf<Pair<Int, Int>>()
                for ((cdx, cdy) in listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)) {
                    if (cdx == -s.dx && cdy == -s.dy) continue
                    if (!isWall(sc + cdx, sr + cdy)) candidates += cdx to cdy
                }
                if (candidates.isEmpty()) { s.dx = -s.dx; s.dy = -s.dy }
                else {
                    val target = if (s.frightened > 0f) {
                        // Flee — choose direction away from player.
                        candidates.maxByOrNull {
                            val nx = (sc + it.first + 0.5f) * tile
                            val ny = boardY + (sr + it.second + 0.5f) * tile
                            abs(nx - munch.x) + abs(ny - munch.y)
                        }!!
                    } else {
                        // Chase — choose direction toward player (greedy).
                        candidates.minByOrNull {
                            val nx = (sc + it.first + 0.5f) * tile
                            val ny = boardY + (sr + it.second + 0.5f) * tile
                            abs(nx - munch.x) + abs(ny - munch.y)
                        }!!
                    }
                    s.dx = target.first; s.dy = target.second
                    s.x = sCentreX; s.y = sCentreY
                }
            }
            if (isWall(sc + s.dx, sr + s.dy) && sNearCentre) {
                s.x = sCentreX; s.y = sCentreY
            } else {
                s.x += s.dx * sSpd * dt
                s.y += s.dy * sSpd * dt
            }
            if (s.x < -tile / 2f) s.x = W + tile / 2f
            if (s.x > W + tile / 2f) s.x = -tile / 2f

            // Collision with muncher (skipped during the post-death mercy window).
            if (s.alive && grace <= 0f && abs(s.x - munch.x) < tile * 0.6f && abs(s.y - munch.y) < tile * 0.6f) {
                if (s.frightened > 0f) {
                    s.alive = false
                    s.respawnTimer = 4f
                    score += 200
                    ctx.sound.bigExplosion()
                } else {
                    lives--
                    if (lives <= 0) {
                        dead = true; ctx.sound.gameOver()
                        if (score > best) { best = score; ctx.scores.set(id, score) }
                    } else {
                        resetAfterDeath()
                        ctx.sound.die()
                    }
                    break
                }
            }
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))

        // HUD.
        drawDosText("SCORE  $score", x = 8f, y = 22f, color = Color(0xFFFFD600), size = 14f, bold = true)
        drawDosText("LV  $level", x = W / 2f, y = 22f, color = Color.White, size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText("♥ x $lives    BEST $best", x = W - 8f, y = 22f, color = Color(0xFFFF6666), size = 12f, bold = true, align = TextAlign.RIGHT)

        // Maze.
        for (r in 0 until rows) for (c in 0 until cols) {
            val x = c * tile; val y = boardY + r * tile
            val ch = grid[r][c]
            when (ch) {
                '#' -> {
                    drawRect(Color(0xFF0000B0), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFF4040FF), topLeft = Offset(x + 2f, y + 2f), size = Size(tile - 4f, tile - 4f))
                    drawRect(Color(0xFF0000B0), topLeft = Offset(x + 4f, y + 4f), size = Size(tile - 8f, tile - 8f))
                }
                'T' -> drawRect(Color.Black, topLeft = Offset(x, y), size = Size(tile, tile))
                '.' -> drawRect(Color(0xFFFFCCAA), topLeft = Offset(x + tile / 2f - 1f, y + tile / 2f - 1f), size = Size(3f, 3f))
                'o' -> {
                    val pulse = (kotlin.math.sin(time * 6f) * 0.3f + 0.7f)
                    drawRect(Color(0xFFFFFFFF).copy(alpha = pulse), topLeft = Offset(x + tile / 2f - 4f, y + tile / 2f - 4f), size = Size(8f, 8f))
                    drawRect(Color(0xFFFFCC00), topLeft = Offset(x + tile / 2f - 3f, y + tile / 2f - 3f), size = Size(6f, 6f))
                }
            }
        }

        // Spooks.
        for (s in spooks) {
            if (!s.alive) {
                // Just eyes returning to spawn.
                drawRect(Color.White, topLeft = Offset(s.x - 6f, s.y - 4f), size = Size(4f, 5f))
                drawRect(Color.White, topLeft = Offset(s.x + 2f, s.y - 4f), size = Size(4f, 5f))
                drawRect(Color(0xFF0000FF), topLeft = Offset(s.x - 5f, s.y - 3f), size = Size(2f, 3f))
                drawRect(Color(0xFF0000FF), topLeft = Offset(s.x + 3f, s.y - 3f), size = Size(2f, 3f))
                continue
            }
            val c = if (s.frightened > 0f) {
                if (s.frightened < 2f && (time * 4f).toInt() % 2 == 0) Color.White else Color(0xFF2244AA)
            } else s.color
            // Round-top blob, wavy bottom.
            val sx = s.x - 9f; val sy = s.y - 10f
            drawRect(c, topLeft = Offset(sx, sy + 6f), size = Size(18f, 12f))
            drawRect(c, topLeft = Offset(sx + 2f, sy + 2f), size = Size(14f, 6f))
            drawRect(c, topLeft = Offset(sx + 4f, sy), size = Size(10f, 4f))
            // Wavy bottom (zig-zag) — 3 little triangles.
            val tri = ((time * 4f).toInt() % 2) * 2f
            for (k in 0 until 3) {
                drawRect(c, topLeft = Offset(sx + k * 6f, sy + 18f), size = Size(6f, 2f + tri))
            }
            // Eyes.
            drawRect(Color.White, topLeft = Offset(sx + 3f, sy + 4f), size = Size(4f, 5f))
            drawRect(Color.White, topLeft = Offset(sx + 11f, sy + 4f), size = Size(4f, 5f))
            val pdx = sign(munch.x - s.x).toInt(); val pdy = sign(munch.y - s.y).toInt()
            drawRect(Color.Black, topLeft = Offset(sx + 4f + pdx, sy + 5f + pdy.coerceAtLeast(0)), size = Size(2f, 3f))
            drawRect(Color.Black, topLeft = Offset(sx + 12f + pdx, sy + 5f + pdy.coerceAtLeast(0)), size = Size(2f, 3f))
        }

        // Muncher — square hero with toothy bite, direction-sensitive.
        val mx = munch.x; val my = munch.y
        // Animate bite based on time + movement.
        val biteOpen = ((time * 8f).toInt() % 2) == 0 && (munch.dx != 0 || munch.dy != 0)
        // Lime-green chomper-bot, NOT a yellow circle — keeps it clearly distinct
        // from the obvious arcade reference at low resolution.
        drawRect(Color(0xFF2F6E16), topLeft = Offset(mx - 9f, my - 9f), size = Size(18f, 18f))
        drawRect(Color(0xFF7FD23A), topLeft = Offset(mx - 8f, my - 8f), size = Size(16f, 16f))
        drawRect(Color(0xFFA6F060), topLeft = Offset(mx - 8f, my - 8f), size = Size(16f, 3f))
        if (biteOpen) {
            // Open mouth — a black triangle pointing in move direction.
            when {
                munch.dx > 0 -> {
                    drawRect(Color.Black, topLeft = Offset(mx, my - 4f), size = Size(9f, 8f))
                    drawRect(Color.Black, topLeft = Offset(mx + 4f, my - 2f), size = Size(5f, 4f))
                }
                munch.dx < 0 -> {
                    drawRect(Color.Black, topLeft = Offset(mx - 9f, my - 4f), size = Size(9f, 8f))
                    drawRect(Color.Black, topLeft = Offset(mx - 9f, my - 2f), size = Size(5f, 4f))
                }
                munch.dy > 0 -> {
                    drawRect(Color.Black, topLeft = Offset(mx - 4f, my), size = Size(8f, 9f))
                    drawRect(Color.Black, topLeft = Offset(mx - 2f, my + 4f), size = Size(4f, 5f))
                }
                munch.dy < 0 -> {
                    drawRect(Color.Black, topLeft = Offset(mx - 4f, my - 9f), size = Size(8f, 9f))
                    drawRect(Color.Black, topLeft = Offset(mx - 2f, my - 9f), size = Size(4f, 5f))
                }
            }
            // Two little teeth — at the mouth edge.
            when {
                munch.dx > 0 -> { drawRect(Color.White, topLeft = Offset(mx, my - 4f), size = Size(2f, 2f)); drawRect(Color.White, topLeft = Offset(mx, my + 2f), size = Size(2f, 2f)) }
                munch.dx < 0 -> { drawRect(Color.White, topLeft = Offset(mx - 2f, my - 4f), size = Size(2f, 2f)); drawRect(Color.White, topLeft = Offset(mx - 2f, my + 2f), size = Size(2f, 2f)) }
                munch.dy > 0 -> { drawRect(Color.White, topLeft = Offset(mx - 4f, my), size = Size(2f, 2f)); drawRect(Color.White, topLeft = Offset(mx + 2f, my), size = Size(2f, 2f)) }
                munch.dy < 0 -> { drawRect(Color.White, topLeft = Offset(mx - 4f, my - 2f), size = Size(2f, 2f)); drawRect(Color.White, topLeft = Offset(mx + 2f, my - 2f), size = Size(2f, 2f)) }
            }
        }
        // Robot visor eye (cyan) — reinforces "chomper-bot", not the arcade circle.
        drawRect(Color.Black, topLeft = Offset(mx - 5f, my - 6f), size = Size(10f, 4f))
        drawRect(Color(0xFF33FFD0), topLeft = Offset(mx - 4f, my - 5f), size = Size(8f, 2f))

        if (won) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("LEVEL CLEAR", "УРОВЕНЬ ПРОЙДЕН"), x = W / 2f, y = H / 2f - 20f, color = Color(0xFFFFD600), size = 28f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK TO CONTINUE", "КЛИК — ДАЛЬШЕ"), x = W / 2f, y = H / 2f + 20f, color = Color.White, size = 14f, bold = true, align = TextAlign.CENTER)
        }
        if (dead) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("GAME OVER", x = W / 2f, y = H / 2f - 20f, color = Color(0xFFFF3333), size = 32f, bold = true, align = TextAlign.CENTER)
            drawDosText("SCORE  $score", x = W / 2f, y = H / 2f + 10f, color = Color.White, size = 18f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"), x = W / 2f, y = H / 2f + 40f, color = Color(0xFFCCCCCC), size = 14f, align = TextAlign.CENTER)
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (dead) { init(ctx); return }
        if (won) { startLevel(); return }
    }
}
