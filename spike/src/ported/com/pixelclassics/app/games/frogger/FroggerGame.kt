package com.pixelclassics.app.games.frogger

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.clipRect
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.abs
import kotlin.math.floor
import kotlin.random.Random

/** Frogger — 5 home alcoves, river logs/turtles, road cars/trucks. */
class FroggerGame : Game {
    override val id: String = "frogger"
    override val titleResId: Int = R.string.game_frogger
    override val backgroundColor: Color = Color.Black

    private val cols = 13
    private val rows = 14
    private val cell = 36f
    private val W = cols * cell
    private val H = rows * cell + 28f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H
    override val controls: GameControls = GameControls.DPAD
    override val introText: String = FROGGER_INTRO_EN
    override fun introTextFor(lang: String): String = pickFroggerIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PARCHMENT
    override val helpLines: List<String> = listOf(
        "D-pad — hop one square",
        "Cross the road, ride the river (logs/turtles), home in the alcoves",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — прыжок на клетку",
        "Перейди дорогу, переплыви реку (брёвна/черепахи), займи ниши",
    ) else helpLines
    private val hudH = 28f
    private val boardY get() = hudH

    private val cBg = Color.Black
    private val cRoad = Color(0xFF1A1A1A)
    private val cLane = Color(0xFF333333)
    private val cGrass = Color(0xFF2D8B2D)
    private val cRiver = Color(0xFF0F1B6E)
    private val cLog = Color(0xFF8B4513)
    private val cTurtle = Color(0xFF1A5C1A)
    private val cFrog = Color(0xFF33CC33)
    private val cFrogDead = Color(0xFFCC3333)
    private val cCar1 = Color(0xFFCC2222)
    private val cCar2 = Color(0xFFCCCC22)
    private val cCar3 = Color.White
    private val cCar4 = Color(0xFFCC22CC)
    private val cTruck = Color(0xFF6666CC)
    private val cHome = Color(0xFF0A0A0A)

    private data class Item(var x: Float, val len: Int, val color: Color? = null, val dive: Boolean = false, val divePhase: Float = 0f)
    private data class Lane(val type: String, val dir: Int, val speed: Float, val items: MutableList<Item>)

    private var frogC = 6
    private var frogR = 13
    private var frogAlive = true
    private val lanes = arrayOfNulls<Lane>(rows)
    private var homeFilled = BooleanArray(5)
    private val homeCols = intArrayOf(1, 4, 6, 8, 11)
    private var lives = 3
    private var score = 0
    private var best = 0
    private var level = 1
    private var timer = 30f
    private var timerMax = 30f
    private var state = State.PLAY
    private var deathTimer = 0f
    private var winTimer = 0f
    private var hopFrom: Pair<Int, Int>? = null
    private var hopTo: Pair<Int, Int>? = null
    private var hopTime = 0f
    private val hopDur = 0.08f
    private var diveTime = 0f
    private var frogSubCol = 6f  // continuous riding position

    private enum class State { PLAY, DYING, WIN, GAMEOVER }

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        lives = 3; score = 0; level = 1; homeFilled = BooleanArray(5); state = State.PLAY
        initLevel()
    }

    private fun initLevel() {
        timerMax = (30f - (level - 1) * 2f).coerceAtLeast(15f)
        timer = timerMax
        initLanes()
        resetFrog()
    }

    private fun resetFrog() {
        frogC = 6; frogR = 13; frogSubCol = 6f; frogAlive = true
        hopFrom = null; hopTo = null; hopTime = 0f
    }

    private fun initLanes() {
        for (i in lanes.indices) lanes[i] = null
        val base = (0.6f + level * 0.15f)
        lanes[2] = makeLane("log", 1, base * 0.6f, listOf(3 to 4, 3 to 4, 3 to 4))
        lanes[3] = makeLane("turtle", -1, base * 0.8f, listOf(2 to 3, 2 to 3, 2 to 3), divePattern = booleanArrayOf(false, true, false))
        lanes[4] = makeLane("log", 1, base * 0.5f, listOf(5 to 3, 5 to 3))
        lanes[5] = makeLane("turtle", -1, base * 0.9f, listOf(3 to 3, 3 to 3, 3 to 3), divePattern = booleanArrayOf(level > 1, false, level > 2))
        lanes[6] = makeLane("log", 1, base * 1.0f, listOf(2 to 4, 2 to 4, 2 to 4))
        lanes[8] = makeLane("car", -1, base * 0.6f, listOf(1 to 5, 1 to 5, 1 to 5), color = cCar1)
        lanes[9] = makeLane("car", 1, base * 0.8f, listOf(1 to 4, 1 to 4, 1 to 4), color = cCar2)
        lanes[10] = makeLane("car", -1, base * 0.5f, listOf(2 to 6, 2 to 6), color = cTruck)
        lanes[11] = makeLane("car", 1, base * 1.1f, listOf(1 to 3, 1 to 3, 1 to 3, 1 to 3), color = cCar3)
        lanes[12] = makeLane("car", -1, base * 0.7f, listOf(1 to 4, 1 to 5, 1 to 4), color = cCar4)
    }

    private fun makeLane(type: String, dir: Int, sp: Float, lensGaps: List<Pair<Int, Int>>, color: Color? = null, divePattern: BooleanArray? = null): Lane {
        val items = mutableListOf<Item>()
        var pos = 0f
        for ((i, lg) in lensGaps.withIndex()) {
            val (len, gap) = lg
            val dive = divePattern?.getOrNull(i) ?: false
            items += Item(pos, len, color, dive, Random.nextFloat() * 6.28f)
            pos += (len + gap).toFloat()
        }
        return Lane(type, dir, sp, items)
    }

    private fun handleMove(dx: Int, dy: Int, ctx: GameContext) {
        if (state == State.GAMEOVER) { init(ctx); return }
        if (state != State.PLAY || !frogAlive || hopTime > 0f && hopTime < hopDur) return
        var nc = frogC; var nr = frogR
        if (abs(dx) > abs(dy)) nc += if (dx > 0) 1 else -1
        else nr += if (dy > 0) 1 else -1
        if (nc < 0 || nc >= cols) return
        if (nr < 0 || nr > 13) return
        hopFrom = frogC to frogR
        hopTo = nc to nr
        hopTime = 0f
        frogC = nc; frogR = nr
        frogSubCol = nc.toFloat()
        ctx.sound.click()
    }

    override fun update(dt: Float, ctx: GameContext) {
        diveTime += dt
        when (state) {
            State.DYING -> {
                deathTimer -= dt
                if (deathTimer <= 0f) afterDeath(ctx)
                return
            }
            State.WIN -> {
                winTimer -= dt
                if (winTimer <= 0f) {
                    level++; homeFilled = BooleanArray(5); state = State.PLAY; initLevel()
                }
                return
            }
            State.GAMEOVER -> return
            State.PLAY -> {}
        }
        if (hopTime < hopDur) hopTime = (hopTime + dt).coerceAtMost(hopDur)

        for (r in 2..12) {
            val lane = lanes[r] ?: continue
            for (item in lane.items) {
                item.x += lane.dir * lane.speed * dt
            }
            val total = lane.items.sumOf { it.len + 4 }
            for (item in lane.items) {
                if (lane.dir > 0 && item.x > cols + 2f) item.x -= total + cols
                if (lane.dir < 0 && item.x + item.len < -2f) item.x += total + cols
            }
        }

        timer -= dt
        if (timer <= 0f) { killFrog(ctx); return }

        if (hopTime < hopDur) return
        checkCollisions(dt, ctx)
    }

    private fun checkCollisions(dt: Float, ctx: GameContext) {
        val r = frogR
        when {
            r == 0 -> checkHome(ctx)
            r == 1 || r == 7 || r == 13 -> Unit
            r in 2..6 -> {
                // Continuous overlap — the old floor()-quantized check made
                // log edges lie by up to a whole cell (Юджин: «проверь коллизии»).
                val lane = lanes[r] ?: return killFrog(ctx)
                var onSomething = false
                val centre = frogSubCol + 0.5f
                for (item in lane.items) {
                    if (centre >= item.x && centre <= item.x + item.len) {
                        if (lane.type == "turtle" && item.dive) {
                            val phase = kotlin.math.sin(diveTime * 1.5f + item.divePhase)
                            if (phase > 0.6f) { killFrog(ctx); return }
                        }
                        onSomething = true
                        frogSubCol += lane.dir * lane.speed * dt
                        frogC = frogSubCol.toInt().coerceIn(0, cols - 1)
                        break
                    }
                }
                if (!onSomething) { killFrog(ctx); return }
                if (frogSubCol < 0f || frogSubCol >= cols) killFrog(ctx)
            }
            r in 8..12 -> {
                // Frog hitbox is the middle 70% of its cell — brushing a car
                // bumper by a pixel no longer kills.
                val lane = lanes[r] ?: return
                val fLeft = frogC + 0.15f
                val fRight = frogC + 0.85f
                for (item in lane.items) {
                    if (fRight > item.x && fLeft < item.x + item.len) { killFrog(ctx); return }
                }
            }
        }
    }

    private fun checkHome(ctx: GameContext) {
        for (i in homeCols.indices) {
            if (frogC == homeCols[i]) {
                if (homeFilled[i]) { killFrog(ctx); return }
                homeFilled[i] = true
                score += 50 + (timer * 10f).toInt()
                ctx.sound.score()
                if (homeFilled.all { it }) {
                    state = State.WIN; winTimer = 2f; score += 1000; ctx.sound.win()
                    if (score > best) { best = score; ctx.scores.set(id, score) }
                    return
                }
                timer = timerMax; resetFrog()
                return
            }
        }
        killFrog(ctx)
    }

    private fun killFrog(ctx: GameContext) {
        if (!frogAlive) return
        frogAlive = false; state = State.DYING; deathTimer = 1.2f
        ctx.sound.die()
    }

    private fun afterDeath(ctx: GameContext) {
        lives--
        if (lives <= 0) {
            state = State.GAMEOVER; ctx.sound.gameOver()
            if (score > best) { best = score; ctx.scores.set(id, score) }
        } else {
            state = State.PLAY; timer = timerMax; resetFrog()
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(cBg, topLeft = Offset.Zero, size = Size(W, H))

        // HUD bar.
        drawRect(Color.Black, topLeft = Offset(0f, 0f), size = Size(W, hudH))
        drawDosText("SCORE  $score", x = 6f, y = 18f, color = Color.White, size = 12f, bold = true)
        drawDosText("LV  $level", x = W / 2f, y = 18f, color = Color.White, size = 12f, bold = true, align = TextAlign.CENTER)
        drawDosText("BEST  $best", x = W - 6f, y = 18f, color = Color(0xFFFFD600), size = 12f, bold = true, align = TextAlign.RIGHT)

        // Background rows.
        drawRowFill(0, cRiver)
        drawHomeAlcoves(this)
        drawRowFill(1, cGrass)
        for (r in 2..6) drawRowFill(r, cRiver)
        drawRowFill(7, cGrass)
        for (r in 8..12) {
            drawRowFill(r, cRoad)
            if (r < 12) for (c in 0 until cols) drawRect(
                cLane,
                topLeft = Offset(c * cell + cell * 0.3f, boardY + (r + 1) * cell - 3f),
                size = Size(cell * 0.4f, 3f),
            )
        }
        drawRowFill(13, cGrass)

        // Lane items + frog + homes — clipped to the playfield so wrapping
        // tiles never leak into the HUD/letterbox bars after death.
        clipRect(left = 0f, top = boardY, right = W, bottom = boardY + rows * cell + cell) {
            for (r in 2..12) {
                val lane = lanes[r] ?: continue
                for (item in lane.items) drawLaneItem(this, lane, item, r)
            }

            // Filled homes.
            for (i in homeCols.indices) if (homeFilled[i]) drawFrog(this, homeCols[i].toFloat(), 0f, Color(0xFF44AA44))
        }

        // Frog.
        if (frogAlive) {
            var dc = frogC.toFloat(); var dr = frogR.toFloat()
            if (hopTime < hopDur && hopFrom != null && hopTo != null) {
                val t = (hopTime / hopDur).coerceIn(0f, 1f).let { 1f - (1f - it) * (1f - it) }
                dc = hopFrom!!.first + (hopTo!!.first - hopFrom!!.first) * t
                dr = hopFrom!!.second + (hopTo!!.second - hopFrom!!.second) * t
            }
            drawFrog(this, dc, dr, cFrog)
        } else if (state == State.DYING) {
            val fx = frogC * cell; val fy = boardY + frogR * cell
            drawRect(cFrogDead, topLeft = Offset(fx + 3f, fy + 3f), size = Size(cell - 6f, cell - 6f))
        }

        // Timer bar.
        val barX = cell * 2f
        val barY = H - 12f
        val barW = W - cell * 4f
        drawRect(Color(0xFF333333), topLeft = Offset(barX, barY), size = Size(barW, 6f))
        val frac = (timer / timerMax).coerceIn(0f, 1f)
        val timerColor = if (frac > 0.3f) Color(0xFF33CC33) else cFrogDead
        drawRect(timerColor, topLeft = Offset(barX, barY), size = Size(barW * frac, 6f))

        // Lives.
        for (i in 0 until lives - 1) drawRect(
            cFrog,
            topLeft = Offset(6f + i * cell * 0.6f, H - 16f),
            size = Size(cell * 0.4f, cell * 0.4f),
        )

        if (state == State.GAMEOVER) {
            drawRect(Color(0xB3000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("GAME OVER", x = W / 2f, y = H / 2f - 20f, color = cFrogDead, size = 32f, bold = true, align = TextAlign.CENTER)
            drawDosText("SCORE $score", x = W / 2f, y = H / 2f + 20f, color = Color.White, size = 18f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"), x = W / 2f, y = H / 2f + 50f, color = cCar2, size = 14f, align = TextAlign.CENTER)
        }
    }

    private fun DrawScope.drawRowFill(r: Int, color: Color) {
        drawRect(color, topLeft = Offset(0f, boardY + r * cell), size = Size(W, cell))
    }

    private fun drawHomeAlcoves(scope: DrawScope) = with(scope) {
        drawRect(cGrass, topLeft = Offset(0f, boardY), size = Size(W, cell))
        for (i in homeCols.indices) {
            val hx = homeCols[i] * cell
            drawRect(cHome, topLeft = Offset(hx + 4f, boardY + 4f), size = Size(cell - 8f, cell - 8f))
            drawRect(cRiver, topLeft = Offset(hx + 6f, boardY + 6f), size = Size(cell - 12f, cell - 12f))
        }
    }

    private fun drawLaneItem(scope: DrawScope, lane: Lane, item: Item, row: Int) = with(scope) {
        val x = item.x * cell
        val y = boardY + row * cell
        val w = item.len * cell
        when (lane.type) {
            "log" -> {
                // End caps (rounded look via slight inset).
                drawRect(Color(0xFF6B3A1A), topLeft = Offset(x + 2f, y + 4f), size = Size(w - 4f, cell - 8f))
                drawRect(cLog, topLeft = Offset(x + 3f, y + 5f), size = Size(w - 6f, cell - 10f))
                // Wood grain — 3 horizontal streaks per segment.
                for (i in 0 until item.len) {
                    val sx = x + i * cell
                    drawRect(Color(0xFFA0622D), topLeft = Offset(sx + 4f, y + 8f), size = Size(cell - 8f, 1f))
                    drawRect(Color(0xFFA0622D), topLeft = Offset(sx + 4f, y + cell - 11f), size = Size(cell - 8f, 1f))
                    drawRect(Color(0xFF5A2D10), topLeft = Offset(sx + 4f, y + cell / 2f - 1f), size = Size(cell - 8f, 1f))
                    // Bark knot.
                    if (i == item.len / 2)
                        drawRect(Color(0xFF3A1A08), topLeft = Offset(sx + cell / 2f - 1f, y + cell / 2f - 1f), size = Size(2f, 2f))
                }
            }
            "turtle" -> {
                val phase = kotlin.math.sin(diveTime * 1.5f + item.divePhase)
                val underwater = item.dive && phase > 0.6f
                if (!underwater) {
                    val tc = if (item.dive && phase > 0.3f) Color(0xFF0D4D0D) else cTurtle
                    val shell = if (item.dive && phase > 0.3f) Color(0xFF184418) else Color(0xFF2A7A2A)
                    for (i in 0 until item.len) {
                        val sx = x + i * cell
                        // Shell base.
                        drawRect(Color(0xFF000000), topLeft = Offset(sx + 4f, y + 4f), size = Size(cell - 8f, cell - 8f))
                        drawRect(tc, topLeft = Offset(sx + 5f, y + 5f), size = Size(cell - 10f, cell - 10f))
                        // Hex shell pattern.
                        drawRect(shell, topLeft = Offset(sx + cell / 2f - 4f, y + 7f), size = Size(8f, 3f))
                        drawRect(shell, topLeft = Offset(sx + 6f, y + cell / 2f - 1f), size = Size(3f, 3f))
                        drawRect(shell, topLeft = Offset(sx + cell - 9f, y + cell / 2f - 1f), size = Size(3f, 3f))
                        drawRect(shell, topLeft = Offset(sx + cell / 2f - 4f, y + cell - 10f), size = Size(8f, 3f))
                        // Head/tail.
                        val headDir = lane.dir
                        if (headDir < 0) drawRect(tc, topLeft = Offset(sx + 1f, y + cell / 2f - 2f), size = Size(4f, 4f))
                        else drawRect(tc, topLeft = Offset(sx + cell - 5f, y + cell / 2f - 2f), size = Size(4f, 4f))
                    }
                } else {
                    // Bubbles.
                    for (i in 0 until item.len) {
                        val sx = x + i * cell
                        drawRect(Color(0xCC4488DD), topLeft = Offset(sx + cell * 0.3f, y + 8f), size = Size(3f, 3f))
                        drawRect(Color(0xCC4488DD), topLeft = Offset(sx + cell * 0.6f, y + 14f), size = Size(2f, 2f))
                        drawRect(Color(0xCC4488DD), topLeft = Offset(sx + cell * 0.45f, y + 20f), size = Size(2f, 2f))
                    }
                }
            }
            "car" -> {
                val col = item.color ?: cCar1
                val darker = Color(col.red * 0.5f, col.green * 0.5f, col.blue * 0.5f, 1f)
                // Body shadow.
                drawRect(Color.Black, topLeft = Offset(x + 3f, y + 5f), size = Size(w - 6f, cell - 10f))
                drawRect(col, topLeft = Offset(x + 4f, y + 6f), size = Size(w - 8f, cell - 12f))
                // Hood/trunk darker stripe.
                drawRect(darker, topLeft = Offset(x + 4f, y + cell - 8f), size = Size(w - 8f, 2f))
                // Highlights.
                drawRect(Color.White.copy(alpha = 0.35f), topLeft = Offset(x + 4f, y + 6f), size = Size(w - 8f, 2f))
                // Windshield + window — at the front.
                val wx = if (lane.dir > 0) x + w - 14f else x + 4f
                drawRect(Color(0xFF223377), topLeft = Offset(wx + 1f, y + 9f), size = Size(8f, cell - 18f))
                drawRect(Color(0xFF4488CC), topLeft = Offset(wx + 1f, y + 9f), size = Size(8f, 2f))
                // Wheels — front and back.
                drawRect(Color.Black, topLeft = Offset(x + 6f, y + cell - 7f), size = Size(7f, 5f))
                drawRect(Color(0xFF606060), topLeft = Offset(x + 7f, y + cell - 6f), size = Size(5f, 3f))
                drawRect(Color.Black, topLeft = Offset(x + w - 13f, y + cell - 7f), size = Size(7f, 5f))
                drawRect(Color(0xFF606060), topLeft = Offset(x + w - 12f, y + cell - 6f), size = Size(5f, 3f))
                // Headlight at the leading edge.
                val hx = if (lane.dir > 0) x + w - 5f else x + 2f
                drawRect(Color(0xFFFFFF88), topLeft = Offset(hx, y + cell / 2f - 1f), size = Size(3f, 3f))
            }
        }
    }

    private fun drawFrog(scope: DrawScope, col: Float, row: Float, color: Color) = with(scope) {
        val fx = col * cell
        val fy = boardY + row * cell
        // Hop-bob: when off-grid, lift slightly.
        val frac = col - col.toInt()
        val bob = if (frac > 0.05f && frac < 0.95f) -2f else 0f
        // Outline.
        drawRect(Color(0xFF073507), topLeft = Offset(fx + 3f, fy + 3f + bob), size = Size(cell - 6f, cell - 6f))
        // Body
        drawRect(color, topLeft = Offset(fx + 4f, fy + 4f + bob), size = Size(cell - 8f, cell - 8f))
        // Darker belly stripe at bottom.
        drawRect(Color(0xFF22AA22), topLeft = Offset(fx + 6f, fy + cell - 10f + bob), size = Size(cell - 12f, 3f))
        // Legs — small stub squares on the corners.
        drawRect(Color(0xFF073507), topLeft = Offset(fx + 2f, fy + cell - 7f + bob), size = Size(3f, 4f))
        drawRect(Color(0xFF073507), topLeft = Offset(fx + cell - 5f, fy + cell - 7f + bob), size = Size(3f, 4f))
        // Eyes — two raised pixels (white sclera + black pupil) on top.
        drawRect(Color.White, topLeft = Offset(fx + 5f, fy + 3f + bob), size = Size(5f, 5f))
        drawRect(Color.White, topLeft = Offset(fx + cell - 10f, fy + 3f + bob), size = Size(5f, 5f))
        drawRect(Color.Black, topLeft = Offset(fx + 6f, fy + 4f + bob), size = Size(2f, 2f))
        drawRect(Color.Black, topLeft = Offset(fx + cell - 9f, fy + 4f + bob), size = Size(2f, 2f))
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        touchStart = Offset(x, y)
    }

    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        val s = touchStart ?: return
        touchStart = null
        val dx = x - s.x; val dy = y - s.y
        if (abs(dx) < 18f && abs(dy) < 18f) {
            val ddx = x - W / 2f; val ddy = y - H / 2f
            if (abs(ddx) > abs(ddy)) handleMove(ddx.toInt(), 0, ctx)
            else handleMove(0, ddy.toInt(), ctx)
        } else handleMove(dx.toInt(), dy.toInt(), ctx)
    }

    private var touchStart: Offset? = null

    // Tile-hopper: one hop per physical press — hosts must not auto-repeat.
    override val discreteDpad: Boolean get() = true

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (dx == 0 && dy == 0) return   // ignore D-pad release; otherwise the frog auto-hops up
        handleMove(dx, dy, ctx)
    }
}
