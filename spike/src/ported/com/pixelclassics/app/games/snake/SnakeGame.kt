package com.pixelclassics.app.games.snake

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.DosPalette
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.abs
import kotlin.random.Random

/**
 * Nokia-green Snake. 1:1 port of assets/games/snake.html, including swipe
 * controls, classic colour palette, escalating speed and tap-to-restart.
 */
class SnakeGame : Game {
    override val id: String = "snake"
    override val titleResId: Int = R.string.game_snake
    override val backgroundColor: Color = DosPalette.NokiaBg
    override val virtualWidth: Float = 640f
    override val virtualHeight: Float = 480f
    override val controls: GameControls = GameControls.DPAD
    override val introText: String = SNAKE_INTRO_EN
    override fun introTextFor(lang: String): String = pickSnakeIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.NOKIA_LCD
    override val helpLines: List<String> = helpLinesFor("en")
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — руление змейкой",
        "Ешь яблоки и расти.  Не кусай себя, не врезайся в стену.",
        "Выше уровень = выше скорость + препятствия",
    ) else listOf(
        "D-pad — steer the snake",
        "Eat apples to grow.  Don't bite yourself, don't hit the wall.",
        "Higher levels = faster speed + obstacles",
    )

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (gameOver) { restart(); return }
        when {
            dx > 0 && dir.x != -1 -> nextDir = P(1, 0)
            dx < 0 && dir.x != 1 -> nextDir = P(-1, 0)
            dy > 0 && dir.y != -1 -> nextDir = P(0, 1)
            dy < 0 && dir.y != 1 -> nextDir = P(0, -1)
        }
    }

    private val bezel = 28f
    private val cell = 20f
    private val lcdW = virtualWidth - 2f * bezel
    private val lcdH = virtualHeight - 2f * bezel
    private val cols = (lcdW / cell).toInt()
    private val rows = (lcdH / cell).toInt()
    // Centre the cells inside the LCD area (LCD may not divide evenly by cell).
    private val lcdX0 = bezel + (lcdW - cols * cell) / 2f
    private val lcdY0 = bezel + (lcdH - rows * cell) / 2f
    private val playW = cols * cell
    private val playH = rows * cell

    private data class P(var x: Int, var y: Int)

    private val snake = ArrayDeque<P>()
    private var dir = P(1, 0)
    private var nextDir = P(1, 0)
    private var food = P(0, 0)
    private var score = 0
    private var best = 0
    private var gameOver = false
    private var stepInterval = 0.16f          // seconds per move
    private var stepAcc = 0f

    // Level progression: every FOOD_PER_LEVEL apples -> next level (faster + more obstacles).
    private val obstacles = ArrayList<P>()
    private var level = 1
    private var foodThisLevel = 0
    private val FOOD_PER_LEVEL = 20

    private var touchStart: Offset? = null

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        restart()
    }

    private fun restart() {
        snake.clear()
        val cx = cols / 2
        val cy = rows / 2
        snake.addLast(P(cx, cy))
        snake.addLast(P(cx - 1, cy))
        snake.addLast(P(cx - 2, cy))
        dir = P(1, 0); nextDir = P(1, 0)
        score = 0
        gameOver = false
        stepInterval = 0.16f
        stepAcc = 0f
        level = 1
        foodThisLevel = 0
        obstacles.clear()
        buildObstacles()
        placeFood()
    }

    /** Number of obstacle squares for the current level. */
    private fun obstacleCount(): Int = (level - 1) * 3

    /** A cell is occupied if the snake or an obstacle sits on it. */
    private fun occupied(x: Int, y: Int): Boolean =
        snake.any { it.x == x && it.y == y } || obstacles.any { it.x == x && it.y == y }

    /** Rebuild obstacles for the current level, keeping clear of snake + head's path. */
    private fun buildObstacles() {
        obstacles.clear()
        val head = snake.firstOrNull()
        val target = obstacleCount()
        var guard = 0
        while (obstacles.size < target && guard < 2000) {
            guard++
            val nx = Random.nextInt(cols)
            val ny = Random.nextInt(rows)
            // Leave a 1-cell halo around the head so the snake never spawns trapped.
            if (head != null && abs(nx - head.x) <= 1 && abs(ny - head.y) <= 1) continue
            if (occupied(nx, ny)) continue
            obstacles.add(P(nx, ny))
        }
    }

    private fun placeFood() {
        // Bounded random tries, then a deterministic scan — a nearly full
        // board must never spin this loop forever (ANR).
        repeat(2000) {
            val nx = Random.nextInt(cols)
            val ny = Random.nextInt(rows)
            if (!occupied(nx, ny)) {
                food = P(nx, ny); return
            }
        }
        for (ny in 0 until rows) for (nx in 0 until cols) {
            if (!occupied(nx, ny)) { food = P(nx, ny); return }
        }
        // Board completely full — the player has effectively beaten Snake.
        gameOver = true
    }

    override fun update(dt: Float, ctx: GameContext) {
        if (gameOver) return
        stepAcc += dt
        if (stepAcc < stepInterval) return
        stepAcc = 0f

        dir = nextDir
        val head = snake.first()
        val nx = head.x + dir.x
        val ny = head.y + dir.y

        if (nx < 0 || nx >= cols || ny < 0 || ny >= rows ||
            snake.any { it.x == nx && it.y == ny } ||
            obstacles.any { it.x == nx && it.y == ny }
        ) {
            gameOver = true
            ctx.sound.die()
            if (score > best) { best = score; ctx.scores.set(id, score) }
            return
        }

        snake.addFirst(P(nx, ny))
        if (nx == food.x && ny == food.y) {
            score++
            if (stepInterval > 0.055f) stepInterval -= 0.0015f
            foodThisLevel++
            ctx.sound.eat()
            if (foodThisLevel >= FOOD_PER_LEVEL) {
                // Advance: faster movement + more obstacles on a fresh field.
                level++
                foodThisLevel = 0
                stepInterval = (stepInterval * 0.85f).coerceAtLeast(0.04f)
                buildObstacles()
                ctx.sound.levelUp()
            }
            placeFood()
            if (gameOver) {   // board filled completely — run over, keep the score
                if (score > best) { best = score; ctx.scores.set(id, score) }
            }
        } else {
            snake.removeLast()
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Nokia 3310 LCD frame — dark grey bezel + sunken inner panel + LCD area.
        drawRect(Color(0xFF2A2A2A), topLeft = Offset(0f, 0f), size = Size(virtualWidth, virtualHeight))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(bezel - 6f, bezel - 6f), size = Size(virtualWidth - 2f * (bezel - 6f), virtualHeight - 2f * (bezel - 6f)))
        // LCD background fills the *whole* sunken area, even outside playfield.
        drawRect(DosPalette.NokiaBg, topLeft = Offset(bezel, bezel), size = Size(lcdW, lcdH))

        // Half-tone dither — only inside playfield where the snake lives.
        for (gy in 0 until rows) for (gx in 0 until cols) {
            if ((gx + gy) % 2 == 0) drawRect(
                DosPalette.NokiaBgDark,
                topLeft = Offset(lcdX0 + gx * cell + 1f, lcdY0 + gy * cell + 1f),
                size = Size(2f, 2f),
            )
        }

        // Obstacles — cross-hatched / штрихованные squares. Hollow outline + an
        // X of two diagonals, so they read clearly different from the solid
        // snake blocks and the small apple.
        obstacles.forEach { o ->
            val ox = lcdX0 + o.x * cell
            val oy = lcdY0 + o.y * cell
            val inset = 2f
            val left = ox + inset; val top = oy + inset
            val w = cell - inset * 2f; val h = cell - inset * 2f
            val stroke = 2f
            // Border frame.
            drawRect(DosPalette.NokiaSnake, topLeft = Offset(left, top), size = Size(w, stroke))
            drawRect(DosPalette.NokiaSnake, topLeft = Offset(left, top + h - stroke), size = Size(w, stroke))
            drawRect(DosPalette.NokiaSnake, topLeft = Offset(left, top), size = Size(stroke, h))
            drawRect(DosPalette.NokiaSnake, topLeft = Offset(left + w - stroke, top), size = Size(stroke, h))
            // Cross-hatch X (two diagonals).
            drawLine(DosPalette.NokiaSnake, Offset(left, top), Offset(left + w, top + h), strokeWidth = stroke)
            drawLine(DosPalette.NokiaSnake, Offset(left + w, top), Offset(left, top + h), strokeWidth = stroke)
        }

        // Food — Nokia-snake apple: 3 squares in a chunky pattern.
        val fx = lcdX0 + food.x * cell; val fy = lcdY0 + food.y * cell
        drawRect(DosPalette.NokiaSnake, topLeft = Offset(fx + 5f, fy + 5f), size = Size(cell - 10f, cell - 10f))
        drawRect(DosPalette.NokiaBg, topLeft = Offset(fx + 8f, fy + 8f), size = Size(4f, 4f))

        // Snake body — chunky LCD blocks with inner highlight.
        snake.forEachIndexed { i, s ->
            val gap = 2f
            val x = lcdX0 + s.x * cell + gap
            val y = lcdY0 + s.y * cell + gap
            val sz = cell - gap * 2f
            drawRect(DosPalette.NokiaSnake, topLeft = Offset(x, y), size = Size(sz, sz))
            if (i == 0) {
                // Head — two eye-pixels in the LCD-bg colour.
                val eyeOff = 4f; val eyeSize = 3f
                val (ex1, ey1, ex2, ey2) = when {
                    dir.x == 1 -> floatArrayOf(x + sz - eyeOff - eyeSize, y + eyeOff, x + sz - eyeOff - eyeSize, y + sz - eyeOff - eyeSize)
                    dir.x == -1 -> floatArrayOf(x + eyeOff, y + eyeOff, x + eyeOff, y + sz - eyeOff - eyeSize)
                    dir.y == 1 -> floatArrayOf(x + eyeOff, y + sz - eyeOff - eyeSize, x + sz - eyeOff - eyeSize, y + sz - eyeOff - eyeSize)
                    else -> floatArrayOf(x + eyeOff, y + eyeOff, x + sz - eyeOff - eyeSize, y + eyeOff)
                }.toList().chunked(2).let {
                    floatArrayOf(it[0][0], it[0][1], it[1][0], it[1][1])
                }
                drawRect(DosPalette.NokiaBg, topLeft = Offset(ex1, ey1), size = Size(eyeSize, eyeSize))
                drawRect(DosPalette.NokiaBg, topLeft = Offset(ex2, ey2), size = Size(eyeSize, eyeSize))
            }
        }

        // Pixel-style HUD inside the LCD area (always visible — at the top inside the screen).
        drawDosText(
            "SCORE $score   HI $best",
            x = lcdX0 + 6f, y = lcdY0 + 16f,
            color = DosPalette.NokiaSnake, size = 14f, bold = true,
        )
        drawDosText(
            "LV $level",
            x = lcdX0 + playW - 6f, y = lcdY0 + 16f,
            color = DosPalette.NokiaSnake, size = 14f, bold = true, align = TextAlign.RIGHT,
        )

        if (gameOver) {
            // LCD inversion flash effect on game over — fill the whole LCD area.
            drawRect(DosPalette.NokiaSnake.copy(alpha = 0.92f), topLeft = Offset(bezel, bezel), size = Size(lcdW, lcdH))
            drawDosText(
                "GAME OVER",
                x = virtualWidth / 2f, y = virtualHeight / 2f - 20f,
                color = DosPalette.NokiaBg, size = 36f, bold = true, align = TextAlign.CENTER,
            )
            drawDosText(
                "SCORE  $score   LEVEL  $level",
                x = virtualWidth / 2f, y = virtualHeight / 2f + 18f,
                color = DosPalette.NokiaBg, size = 22f, bold = true, align = TextAlign.CENTER,
            )
            drawDosText(
                ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"),
                x = virtualWidth / 2f, y = virtualHeight / 2f + 56f,
                color = DosPalette.NokiaBg, size = 16f, bold = true, align = TextAlign.CENTER,
            )
        }

        // LCD pixel matrix — pale seams over EVERYTHING on the panel carve
        // snake, text and flash into visible liquid-crystal cells (Юджин,
        // 18.07: «не видно ячеек жидких кристаллов»). Drawn last on purpose.
        val seam = DosPalette.NokiaBg.copy(alpha = 0.38f)
        val pitch = 5f
        var sxp = bezel
        while (sxp <= bezel + lcdW) {
            drawRect(seam, topLeft = Offset(sxp, bezel), size = Size(1.2f, lcdH)); sxp += pitch
        }
        var syp = bezel
        while (syp <= bezel + lcdH) {
            drawRect(seam, topLeft = Offset(bezel, syp), size = Size(lcdW, 1.2f)); syp += pitch
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        touchStart = Offset(x, y)
    }

    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        val s = touchStart ?: return
        touchStart = null
        val dx = x - s.x
        val dy = y - s.y
        if (abs(dx) < 20f && abs(dy) < 20f) {
            if (gameOver) { restart(); ctx.sound.menuSelect() }
            return
        }
        if (abs(dx) > abs(dy)) {
            if (dx > 0 && dir.x != -1) nextDir = P(1, 0)
            else if (dx < 0 && dir.x != 1) nextDir = P(-1, 0)
        } else {
            if (dy > 0 && dir.y != -1) nextDir = P(0, 1)
            else if (dy < 0 && dir.y != 1) nextDir = P(0, -1)
        }
    }
}
