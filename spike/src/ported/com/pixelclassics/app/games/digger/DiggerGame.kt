package com.pixelclassics.app.games.digger

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
import kotlin.math.sign
import kotlin.random.Random

/**
 * Digger — chunky Boulder-Dash-cum-Digger-1983.
 *
 * Tunnel through dirt, push and dodge falling rocks, shoot fireballs
 * at red monsters, collect emeralds. 10 named stages, then procedural.
 * D-pad + FIRE on the GameHost overlay.
 */
class DiggerGame : Game {
    override val id: String = "digger"
    override val titleResId: Int = R.string.game_digger
    override val backgroundColor: Color = Color(0xFF111111)
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    override val introText: String = DIGGER_INTRO_EN
    override fun introTextFor(lang: String): String = pickDiggerIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.CGA_CYAN
    override val helpLines: List<String> = listOf(
        "D-pad — dig through dirt · ● FIRE — fireball",
        "Push rocks to crush monsters.  Don't get crushed yourself.",
        "Collect every emerald to clear the stage",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — копай сквозь грунт · ● ОГОНЬ — файербол",
        "Толкай камни на монстров.  Сам под камень не попадай.",
        "Собери все изумруды, чтобы пройти этап",
    ) else helpLines

    private val cols = 20
    private val rows = 14
    private val tile = 28f
    private val W = cols * tile
    private val H = rows * tile + 40f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H
    private val hudH = 40f

    private val EMPTY = 0
    private val DIRT = 1
    private val WALL = 2
    private val EMERALD = 3
    private val ROCK = 4

    private var board = Array(rows) { IntArray(cols) }
    private var px = 1
    private var py = 1
    private var facing: Int = 1   // last move dx (-1 left, 1 right, used by sprite + fireball)
    private var facingV: Int = 0  // last move dy (-1 up, 1 down)
    private var walkFrame = 0f
    private var score = 0
    private var best = 0
    private var lives = 3
    private var emeraldsLeft = 0
    private var level = 1
    private var gameOver = false
    private val monsters = mutableListOf<MutablePair>()

    private data class MutablePair(var r: Int, var c: Int)

    private data class Fireball(var r: Float, var c: Float, var dr: Int, var dc: Int, var life: Float)
    private val balls = mutableListOf<Fireball>()
    private var fireCooldown = 0f
    private val fireCdMax = 1.0f

    private var moveAcc = 0f
    private val moveInterval = 0.18f
    // Rock physics (Юджин, 18.07: «камень должен падать не сразу — иначе
    // исчезает возможность подловить монстра»).
    private val rockDelay = 0.85f
    private val shudder = HashMap<Int, Float>()   // cell key -> seconds hanging
    private val fallingRocks = HashSet<Int>()     // rocks already in free fall
    private var monAcc = 0f
    private val monInterval = 0.55f
    private var time = 0f
    private var msg: String = ""
    private var msgTimer = 0f
    private var heldDir: Pair<Int, Int>? = null

    // 10 named levels.
    private data class LevelDef(val name: String, val nameRu: String, val emeralds: Int, val rocks: Int, val mons: Int, val monInterval: Float, val builder: (Array<IntArray>, Random) -> Unit)

    private val named: List<LevelDef> = listOf(
        LevelDef("TUTORIAL MINE", "УЧЕБНАЯ ШАХТА", 15, 4, 1, 0.80f, ::buildOpenWithDirt),
        LevelDef("FALLING ROCKS", "КАМНЕПАД", 18, 20, 1, 0.70f, ::buildRockyOverhead),
        LevelDef("PACK HUNTERS", "СТАЯ ОХОТНИКОВ", 16, 6, 3, 0.45f, ::buildOpenWithDirt),
        LevelDef("TIGHT TUNNELS", "УЗКИЕ ТОННЕЛИ", 22, 4, 2, 0.55f, ::buildMaze),
        LevelDef("OPEN CAVE", "ОТКРЫТАЯ ПЕЩЕРА", 20, 8, 2, 0.55f, ::buildOpenCave),
        LevelDef("BONUS VAULT", "БОНУСНЫЙ СХРОН", 30, 4, 4, 0.50f, ::buildVault),
        LevelDef("BOULDER FIELD", "ПОЛЕ ВАЛУНОВ", 14, 24, 1, 0.70f, ::buildBoulderField),
        LevelDef("HUNGRY PACK", "ГОЛОДНАЯ СТАЯ", 18, 6, 6, 0.40f, ::buildOpenWithDirt),
        LevelDef("DIAMOND RUSH", "АЛМАЗНАЯ ЛИХОРАДКА", 40, 8, 2, 0.40f, ::buildOpenWithDirt),
        LevelDef("THE PIT", "ЯМА", 25, 12, 4, 0.35f, ::buildPit),
    )

    private var lastCtx: GameContext? = null

    override fun init(ctx: GameContext) {
        lastCtx = ctx
        best = ctx.scores.get(id)
        lives = 3; score = 0; level = 1
        gameOver = false
        buildLevel()
    }

    private fun buildLevel() {
        board = Array(rows) { IntArray(cols) }
        shudder.clear(); fallingRocks.clear()
        val rng = Random(level * 7919L + 31337L)
        val def = if (level <= named.size) named[level - 1]
                  else proceduralDef(level, rng)
        def.builder(board, rng)
        // Always frame the board with walls.
        for (r in 0 until rows) { board[r][0] = WALL; board[r][cols - 1] = WALL }
        for (c in 0 until cols) { board[0][c] = WALL; board[rows - 1][c] = WALL }
        // Place emeralds + rocks on top of the builder layout.
        placeRandomly(EMERALD, def.emeralds, rng) { it == DIRT }
        placeRandomly(ROCK, def.rocks, rng) { it == DIRT }
        emeraldsLeft = countCells(EMERALD)

        px = 1; py = 1; board[py][px] = EMPTY
        facing = 1; facingV = 0
        monsters.clear()
        for (i in 0 until def.mons) {
            for (attempt in 0 until 200) {
                val r = rng.nextInt(rows / 2, rows - 2)
                val c = rng.nextInt(cols / 2, cols - 2)
                if (board[r][c] == DIRT || board[r][c] == EMPTY) {
                    board[r][c] = EMPTY; monsters += MutablePair(r, c); break
                }
            }
        }
        monAcc = 0f; moveAcc = 0f; fireCooldown = 0f; balls.clear()
        msg = lastCtx?.tr("STAGE $level — ${def.name}", "ЭТАП $level — ${def.nameRu}") ?: "STAGE $level — ${def.name}"; msgTimer = 2.5f
    }

    private fun countCells(kind: Int): Int {
        var k = 0
        for (r in 0 until rows) for (c in 0 until cols) if (board[r][c] == kind) k++
        return k
    }

    private fun placeRandomly(kind: Int, count: Int, rng: Random, accept: (Int) -> Boolean) {
        var left = count; var safety = 1000
        while (left > 0 && safety-- > 0) {
            val r = rng.nextInt(2, rows - 2); val c = rng.nextInt(2, cols - 2)
            if (accept(board[r][c])) { board[r][c] = kind; left-- }
        }
    }

    // ── Level builders ────────────────────────────────────────────────────

    private fun buildOpenWithDirt(b: Array<IntArray>, rng: Random) {
        for (r in 0 until rows) for (c in 0 until cols) b[r][c] = DIRT
    }
    private fun buildRockyOverhead(b: Array<IntArray>, rng: Random) {
        for (r in 0 until rows) for (c in 0 until cols) b[r][c] = if (r < 3) EMPTY else DIRT
    }
    private fun buildMaze(b: Array<IntArray>, rng: Random) {
        for (r in 0 until rows) for (c in 0 until cols) b[r][c] = DIRT
        // Carve a serpentine corridor.
        var dir = 1
        for (r in 2 until rows - 2 step 2) {
            val from = if (dir > 0) 2 else cols - 3
            val to = if (dir > 0) cols - 3 else 2
            var c = from
            while (c != to) { b[r][c] = EMPTY; c += dir }
            b[r][to] = EMPTY
            if (r + 1 < rows - 1) b[r + 1][to] = EMPTY
            dir = -dir
        }
    }
    private fun buildOpenCave(b: Array<IntArray>, rng: Random) {
        for (r in 0 until rows) for (c in 0 until cols) b[r][c] = EMPTY
        for (r in 0 until rows) for (c in 0 until cols) if (rng.nextFloat() < 0.30f) b[r][c] = DIRT
    }
    private fun buildVault(b: Array<IntArray>, rng: Random) {
        for (r in 0 until rows) for (c in 0 until cols) b[r][c] = DIRT
        for (r in 4..rows - 5) for (c in 4..cols - 5) b[r][c] = EMPTY
        for (r in 4..rows - 5) for (c in 4..cols - 5) if (rng.nextFloat() < 0.5f) b[r][c] = EMERALD
    }
    private fun buildBoulderField(b: Array<IntArray>, rng: Random) {
        for (r in 0 until rows) for (c in 0 until cols) b[r][c] = DIRT
        // Heavily seed rocks on the top rows; gravity makes them rain.
        for (r in 1..4) for (c in 2 until cols - 2) if (rng.nextFloat() < 0.45f) b[r][c] = ROCK
    }
    private fun buildPit(b: Array<IntArray>, rng: Random) {
        for (r in 0 until rows) for (c in 0 until cols) b[r][c] = DIRT
        // Central pit of empty + ring of walls around it.
        val midR = rows / 2; val midC = cols / 2
        for (r in midR - 2..midR + 2) for (c in midC - 4..midC + 4) b[r][c] = EMPTY
        for (c in midC - 5..midC + 5) { b[midR - 3][c] = WALL; b[midR + 3][c] = WALL }
        for (r in midR - 3..midR + 3) { b[r][midC - 5] = WALL; b[r][midC + 5] = WALL }
        // Two gaps in the ring.
        b[midR][midC - 5] = EMPTY; b[midR][midC + 5] = EMPTY
    }
    private fun proceduralDef(lvl: Int, rng: Random): LevelDef {
        val name = "DEPTH ${lvl - named.size}"
        val nameRu = "ГЛУБИНА ${lvl - named.size}"
        val em = 18 + lvl * 2
        val rocks = 6 + lvl
        val mons = (1 + lvl / 3).coerceAtMost(8)
        val mi = (0.55f - lvl * 0.015f).coerceAtLeast(0.25f)
        val builder = listOf<(Array<IntArray>, Random) -> Unit>(
            ::buildOpenWithDirt, ::buildRockyOverhead, ::buildMaze,
            ::buildOpenCave, ::buildBoulderField,
        ).random(rng)
        return LevelDef(name, nameRu, em, rocks, mons, mi, builder)
    }

    // ── Player input ──────────────────────────────────────────────────────

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (gameOver) return
        heldDir = dx to dy
        if (dx != 0 || dy != 0) {
            facing = if (dx != 0) sign(dx.toFloat()).toInt() else facing
            facingV = if (dy != 0) sign(dy.toFloat()).toInt() else 0
            tryMove(dx, dy, ctx)
        }
    }

    override fun onFire(ctx: GameContext) {
        if (gameOver) { init(ctx); return }
        if (fireCooldown > 0f) return
        // Fire in the last-move direction (default: right).
        val dx = if (facingV != 0) 0 else facing
        val dy = if (facingV != 0) facingV else 0
        balls += Fireball(py.toFloat(), px.toFloat(), dy, dx, 0f)
        fireCooldown = fireCdMax
        ctx.sound.shoot()
    }

    private fun tryMove(dx: Int, dy: Int, ctx: GameContext) {
        val nx = px + dx; val ny = py + dy
        if (nx !in 0 until cols || ny !in 0 until rows) return
        when (board[ny][nx]) {
            DIRT -> { board[ny][nx] = EMPTY; px = nx; py = ny; ctx.sound.menuSelect() }
            EMPTY -> { px = nx; py = ny }
            EMERALD -> { board[ny][nx] = EMPTY; px = nx; py = ny; score += 25; emeraldsLeft--; ctx.sound.eat() }
            ROCK -> if (dy == 0) {
                val nnx = nx + dx
                if (nnx in 1 until cols - 1 && board[ny][nnx] == EMPTY) {
                    board[ny][nnx] = ROCK; board[ny][nx] = EMPTY; px = nx; py = ny
                }
            }
            else -> {}
        }
        if (monsters.any { it.r == py && it.c == px }) die(ctx)
        if (emeraldsLeft == 0) {
            level++; score += 100 + lives * 25; ctx.sound.win(); buildLevel()
        }
    }

    private fun gravityStep(ctx: GameContext) {
        for (r in rows - 2 downTo 0) for (c in 0 until cols) {
            val key = r * cols + c
            if (board[r][c] == ROCK && board[r + 1][c] == EMPTY) {
                // An undermined rock shudders before it drops — the pause is
                // the whole strategy: bait a monster under it, or dig the
                // emerald out from beneath and dodge sideways in time.
                if (key !in fallingRocks) {
                    val hung = (shudder[key] ?: 0f) + moveInterval
                    if (hung < rockDelay) { shudder[key] = hung; continue }
                    shudder.remove(key)
                }
                fallingRocks.remove(key)
                board[r + 1][c] = ROCK; board[r][c] = EMPTY
                fallingRocks.add((r + 1) * cols + c)
                if (r + 1 == py && c == px) { die(ctx) }
                val mi = monsters.indexOfFirst { it.r == r + 1 && it.c == c }
                if (mi >= 0) { monsters.removeAt(mi); score += 100 }
            } else {
                fallingRocks.remove(key)
                shudder.remove(key)
            }
        }
    }

    private fun monsterStep(ctx: GameContext) {
        for (m in monsters) {
            val dr = sign((py - m.r).toFloat()).toInt()
            val dc = sign((px - m.c).toFloat()).toInt()
            val candidates = listOf(m.r + dr to m.c, m.r to m.c + dc).shuffled()
            for ((nr, nc) in candidates) {
                if (nr in 1 until rows - 1 && nc in 1 until cols - 1 &&
                    (board[nr][nc] == EMPTY || board[nr][nc] == DIRT)
                ) {
                    m.r = nr; m.c = nc
                    if (nr == py && nc == px) die(ctx)
                    break
                }
            }
        }
    }

    private fun fireballStep(dt: Float) {
        val it = balls.iterator()
        balls@ while (it.hasNext()) {
            val b = it.next()
            b.life += dt
            // Advance ~8 cells/sec.
            val speed = 9f
            b.r += b.dr * speed * dt
            b.c += b.dc * speed * dt
            val rr = b.r.toInt(); val cc = b.c.toInt()
            if (rr !in 0 until rows || cc !in 0 until cols) { it.remove(); continue }
            // Hit wall / rock?
            when (board[rr][cc]) {
                WALL, ROCK -> { it.remove(); continue@balls }
                DIRT -> { board[rr][cc] = EMPTY; it.remove(); continue@balls }
                else -> {}
            }
            // Hit monster?
            val mi = monsters.indexOfFirst { it.r == rr && it.c == cc }
            if (mi >= 0) {
                monsters.removeAt(mi); score += 150
                it.remove(); continue@balls
            }
            if (b.life > 1.0f) it.remove()
        }
    }

    private fun die(ctx: GameContext) {
        lives--
        ctx.sound.die()
        if (lives <= 0) {
            gameOver = true
            ctx.sound.gameOver()
            if (score > best) { best = score; ctx.scores.set(id, score) }
            return
        }
        px = 1; py = 1; board[1][1] = EMPTY
    }

    override fun update(dt: Float, ctx: GameContext) {
        lastCtx = ctx
        time += dt
        if (msgTimer > 0f) msgTimer -= dt
        if (gameOver) return
        if (fireCooldown > 0f) fireCooldown -= dt
        if (heldDir != null && (heldDir!!.first != 0 || heldDir!!.second != 0)) walkFrame += dt * 6f
        moveAcc += dt
        if (moveAcc >= moveInterval) {
            moveAcc = 0f
            gravityStep(ctx)
        }
        monAcc += dt
        if (monAcc >= monInterval) { monAcc = 0f; monsterStep(ctx) }
        fireballStep(dt)
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // HUD
        drawRect(Color(0xFF222222), topLeft = Offset.Zero, size = Size(W, hudH))
        drawDosText("SCORE  $score", x = 10f, y = 24f, color = Color(0xFF55FF55), size = 14f, bold = true)
        drawDosText("LV  $level", x = W / 2f, y = 24f, color = Color(0xFF55FF55), size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText("♥$lives  ★$best", x = W - 10f, y = 24f, color = Color(0xFFFFD600), size = 14f, bold = true, align = TextAlign.RIGHT)

        // Tiles
        for (r in 0 until rows) for (c in 0 until cols) {
            val x = c * tile; val y = hudH + r * tile
            when (board[r][c]) {
                EMPTY -> drawRect(Color(0xFF1A0A00), topLeft = Offset(x, y), size = Size(tile, tile))
                DIRT -> {
                    drawRect(Color(0xFF6B3A1A), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFF8B5A2B), topLeft = Offset(x + 1f, y + 1f), size = Size(tile - 2f, tile - 2f))
                    for (i in 0 until 3) for (j in 0 until 3) {
                        if ((i + j + c + r) % 2 == 0) drawRect(
                            Color(0xFF5A2A10),
                            topLeft = Offset(x + 4f + i * 7f, y + 4f + j * 7f), size = Size(2f, 2f),
                        )
                    }
                }
                WALL -> {
                    drawRect(Color(0xFF777777), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFFAAAAAA), topLeft = Offset(x + 1f, y + 1f), size = Size(tile - 2f, tile - 2f))
                    drawRect(Color(0xFFCCCCCC), topLeft = Offset(x + 1f, y + 1f), size = Size(tile - 2f, 2f))
                    drawRect(Color(0xFF555555), topLeft = Offset(x + 1f, y + tile - 3f), size = Size(tile - 2f, 2f))
                    drawRect(Color(0xFF555555), topLeft = Offset(x + tile - 3f, y + 1f), size = Size(2f, tile - 2f))
                    drawRect(Color(0xFF888888), topLeft = Offset(x + 4f, y + 4f), size = Size(tile - 8f, tile - 8f))
                }
                EMERALD -> {
                    drawRect(Color(0xFF1A0A00), topLeft = Offset(x, y), size = Size(tile, tile))
                    val cx = x + tile / 2f; val cy = y + tile / 2f
                    drawRect(Color(0xFF008844), topLeft = Offset(x + 4f, y + 4f), size = Size(tile - 8f, tile - 8f))
                    drawRect(Color(0xFF00CC66), topLeft = Offset(x + 6f, y + 6f), size = Size(tile - 12f, tile - 12f))
                    drawRect(Color(0xFF66FFAA), topLeft = Offset(cx - 3f, cy - 5f), size = Size(2f, 2f))
                    drawRect(Color(0xFFCCFFDD), topLeft = Offset(cx - 4f, cy - 6f), size = Size(1f, 1f))
                    if (((time * 2f + c + r) % 3f) > 2.5f) drawRect(Color.White, topLeft = Offset(cx + 1f, cy + 1f), size = Size(2f, 2f))
                }
                ROCK -> {
                    drawRect(Color(0xFF1A0A00), topLeft = Offset(x, y), size = Size(tile, tile))
                    // A hanging rock trembles — the visible warning to whoever
                    // is standing (or lured) underneath.
                    val ox = if (shudder.containsKey(r * cols + c))
                        kotlin.math.sin(time * 42f) * 1.6f else 0f
                    drawRect(Color(0xFF606060), topLeft = Offset(x + 2f + ox, y + 2f), size = Size(tile - 4f, tile - 4f))
                    drawRect(Color(0xFFAAAAAA), topLeft = Offset(x + 4f + ox, y + 4f), size = Size(tile - 8f, tile - 8f))
                    drawRect(Color(0xFFDDDDDD), topLeft = Offset(x + 5f + ox, y + 5f), size = Size(4f, 4f))
                    drawRect(Color(0xFF404040), topLeft = Offset(x + tile - 8f + ox, y + tile - 8f), size = Size(4f, 4f))
                }
            }
        }
        // Player — directional sprite with walk frame.
        drawMiner(this, px, py)
        // Monsters
        for (m in monsters) drawMonster(this, m.r, m.c)
        // Fireballs
        for (b in balls) {
            val bx = b.c * tile + tile / 2f; val by = hudH + b.r * tile + tile / 2f
            drawRect(Color(0xFFFFEE00), topLeft = Offset(bx - 5f, by - 5f), size = Size(10f, 10f))
            drawRect(Color(0xFFFF6600), topLeft = Offset(bx - 3f, by - 3f), size = Size(6f, 6f))
            drawRect(Color.White, topLeft = Offset(bx - 1f, by - 1f), size = Size(2f, 2f))
        }
        // Stage name banner.
        if (msgTimer > 0f) {
            val alpha = (msgTimer / 2.5f).coerceIn(0f, 1f)
            drawRect(Color.Black.copy(alpha = 0.5f * alpha), topLeft = Offset(0f, H / 2f - 22f), size = Size(W, 40f))
            drawDosText(msg, x = W / 2f, y = H / 2f + 4f, color = Color(0xFFFFD600).copy(alpha = alpha), size = 20f, bold = true, align = TextAlign.CENTER)
        }
        if (gameOver) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("GAME OVER", x = W / 2f, y = H / 2f - 20f, color = Color(0xFFFF3333), size = 32f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Score $score · Level $level", "Счёт $score · Уровень $level"), x = W / 2f, y = H / 2f + 20f, color = Color.White, size = 16f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("PRESS SPACE TO RESTART", "ЖМИ ПРОБЕЛ — РЕСТАРТ"), x = W / 2f, y = H / 2f + 48f, color = Color(0xFFCCCCCC), size = 14f, align = TextAlign.CENTER)
        }
    }

    private fun drawMiner(scope: DrawScope, mc: Int, mr: Int) = with(scope) {
        val x = mc * tile; val y = hudH + mr * tile
        val faceLeft = facing < 0
        val step = (walkFrame.toInt() % 2)
        // Body — blue jumpsuit with suspenders.
        drawRect(Color(0xFF1A3A6A), topLeft = Offset(x + 4f, y + 12f), size = Size(tile - 8f, tile - 16f))
        drawRect(Color(0xFF2A5AAA), topLeft = Offset(x + 5f, y + 13f), size = Size(tile - 10f, tile - 18f))
        // Suspenders.
        drawRect(Color(0xFFFFAA00), topLeft = Offset(x + 6f, y + 12f), size = Size(2f, 6f))
        drawRect(Color(0xFFFFAA00), topLeft = Offset(x + tile - 8f, y + 12f), size = Size(2f, 6f))
        // Helmet — yellow.
        drawRect(Color(0xFF884400), topLeft = Offset(x + 4f, y + 4f), size = Size(tile - 8f, 8f))
        drawRect(Color(0xFFFFCC22), topLeft = Offset(x + 5f, y + 5f), size = Size(tile - 10f, 7f))
        // Lamp on helmet (faces forward).
        val lampX = if (faceLeft) x + 4f else x + tile - 9f
        drawRect(Color(0xFFFFEE88), topLeft = Offset(lampX, y + 6f), size = Size(5f, 4f))
        drawRect(Color(0xFFFFFFFF), topLeft = Offset(lampX + 1f, y + 7f), size = Size(2f, 1f))
        // Eye + nose direction-sensitive.
        val eyeX = if (faceLeft) x + 8f else x + tile - 12f
        drawRect(Color.White, topLeft = Offset(eyeX, y + 14f), size = Size(3f, 3f))
        drawRect(Color.Black, topLeft = Offset(eyeX + (if (faceLeft) 0f else 1f), y + 15f), size = Size(2f, 2f))
        // Legs — animated.
        val leg1 = if (step == 0) 0f else 2f
        val leg2 = if (step == 0) 2f else 0f
        drawRect(Color(0xFF1A1A2A), topLeft = Offset(x + 8f, y + tile - 6f - leg1), size = Size(4f, 4f + leg1))
        drawRect(Color(0xFF1A1A2A), topLeft = Offset(x + tile - 12f, y + tile - 6f - leg2), size = Size(4f, 4f + leg2))
        // Boots
        drawRect(Color(0xFF000000), topLeft = Offset(x + 7f, y + tile - 3f), size = Size(6f, 3f))
        drawRect(Color(0xFF000000), topLeft = Offset(x + tile - 13f, y + tile - 3f), size = Size(6f, 3f))
        // Sweat drop occasionally.
        if (((time * 2f) % 5f) > 4.5f) drawRect(Color(0xFF88CCFF), topLeft = Offset(if (faceLeft) x + 4f else x + tile - 6f, y + 16f), size = Size(2f, 3f))
    }

    private fun drawMonster(scope: DrawScope, mr: Int, mc: Int) = with(scope) {
        val x = mc * tile; val y = hudH + mr * tile
        val wob = (((time * 4f + mc + mr).toInt() % 2) * 1f)
        drawRect(Color(0xFF990033), topLeft = Offset(x + 3f, y + 4f + wob), size = Size(tile - 6f, tile - 8f))
        drawRect(Color(0xFFFF3366), topLeft = Offset(x + 4f, y + 5f + wob), size = Size(tile - 8f, tile - 10f))
        drawRect(Color.White, topLeft = Offset(x + 8f, y + 8f + wob), size = Size(3f, 3f))
        drawRect(Color.White, topLeft = Offset(x + 17f, y + 8f + wob), size = Size(3f, 3f))
        drawRect(Color.Black, topLeft = Offset(x + 9f, y + 9f + wob), size = Size(2f, 2f))
        drawRect(Color.Black, topLeft = Offset(x + 18f, y + 9f + wob), size = Size(2f, 2f))
        drawRect(Color.White, topLeft = Offset(x + 10f, y + 16f + wob), size = Size(2f, 3f))
        drawRect(Color.White, topLeft = Offset(x + 16f, y + 16f + wob), size = Size(2f, 3f))
    }
}
