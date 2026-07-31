package com.pixelclassics.app.games.tetris

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.hypot
import kotlin.random.Random

/**
 * BLOCKFALL — a faithful Chinese "Brick Game" handheld in TRUE PORTRAIT.
 *
 * The whole device is drawn: beige chassis, green dot-matrix LCD with a tall
 * 10x20 well on the left and a SCORE / HI-SCORE / NEXT / SPEED / LEVEL / sound
 * readout column on the right, and a tappable button cluster on the chassis
 * below the screen (D-pad + ROTATE + START + SOUND + MODE). Buttons live on the
 * chassis, never on the play field.
 *
 * MODE cycles four built-in games rendered in the same dot grid, exactly like
 * the originals: block-fall puzzle, RACE, SNAKE and SHOOTER. The game is locked to
 * portrait via Game.portrait (GameHost flips the Activity).
 */
class TetrisGame : Game {
    override val id: String = "tetris"
    override val titleResId: Int = R.string.game_tetris
    override val backgroundColor: Color = Color(0xFF1B1B16)
    override val portrait: Boolean = true
    override val introText: String = TETRIS_INTRO_TEXT
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.BRICK_GAME

    // Portrait virtual canvas (~1:2.09, close to a modern phone).
    private val W = 440f
    private val H = 920f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    // ── Dot-matrix LCD geometry ───────────────────────────────────────────────
    private val cols = 10
    private val rows = 20
    private val lcdX = 18f; private val lcdY = 64f; private val lcdW = 404f; private val lcdH = 560f
    private val cell = 26f                            // bigger well — Юджин: ширину можем позволить больше
    private val wellX = lcdX + 14f                    // 32
    private val wellY = lcdY + 16f                    // 80
    private val wellW = cols * cell                   // 260
    private val wellH = rows * cell                   // 520
    private val panelX = wellX + wellW + 12f          // readout strip left edge (304)
    private val panelR = lcdX + lcdW - 10f            // readout strip right edge (412)

    // LCD palette.
    private val lcdBg = Color(0xFF9DAE83)            // classic green LCD (B&W mode)
    private val lcdColorBg = Color(0xFF12131C)       // 8-bit dark screen (colour mode)
    private val lcdInkColor = Color(0xFFCBDAB0)      // readout ink on the dark colour screen
    private val dotOn = Color(0xFF2B3320)
    private val dotOff = Color(0x18000000)
    private val body = Color(0xFFB7B196)
    private val bodyDark = Color(0xFF6E6A55)
    private val btnFace = Color(0xFFE7CF52)
    private val btnFaceDn = Color(0xFFFFF089)
    private val btnRim = Color(0xFF8A7A18)

    // ── Game selection ────────────────────────────────────────────────────────
    private enum class Mini(val title: String, val no: Int, val key: String) {
        TETRIS("TETRA FALL", 1, "tetris"),
        RACE("RACE", 9, "tetris_race"),
        SNAKE("SNAKE", 11, "tetris_snake"),
        SHOOTER("SHOOTER", 12, "tetris_shoot"),
    }
    private val minis = Mini.values()
    private var mini = Mini.TETRIS
    private enum class Phase { SELECT, PLAY, OVER }
    private var phase = Phase.SELECT

    private var score = 0
    private var best = 0
    private var level = 1
    private var speed = 0
    private var time = 0f
    private var tickAcc = 0f
    private var bw = false          // LIGHT button: colour 8-bit ↔ monochrome LCD
    private var paused = false      // handheld START/PAUSE

    // Held-button DAS (auto-repeat for L/R/D).
    private var heldBtn: String? = null
    private var heldId = -1L
    private var heldSince = 0f
    private var lastRepeat = 0f
    private val dasDelay = 0.16f
    private val dasRate = 0.05f

    private var melody: com.pixelclassics.app.audio.MelodyPlayer? = null

    // ── Tetris state ──────────────────────────────────────────────────────────
    private val pieceColors = listOf(
        Color(0xFF00E5FF), Color(0xFFFFD600), Color(0xFFAA00FF),
        Color(0xFFFF8800), Color(0xFF2979FF), Color(0xFF00E676), Color(0xFFFF1744),
    )
    private val shapes: List<List<Array<IntArray>>> = listOf(
        listOf(
            arrayOf(intArrayOf(0,0,0,0), intArrayOf(1,1,1,1), intArrayOf(0,0,0,0), intArrayOf(0,0,0,0)),
            arrayOf(intArrayOf(0,0,1,0), intArrayOf(0,0,1,0), intArrayOf(0,0,1,0), intArrayOf(0,0,1,0)),
            arrayOf(intArrayOf(0,0,0,0), intArrayOf(0,0,0,0), intArrayOf(1,1,1,1), intArrayOf(0,0,0,0)),
            arrayOf(intArrayOf(0,1,0,0), intArrayOf(0,1,0,0), intArrayOf(0,1,0,0), intArrayOf(0,1,0,0)),
        ),
        List(4) { arrayOf(intArrayOf(1,1), intArrayOf(1,1)) },
        listOf(
            arrayOf(intArrayOf(0,1,0), intArrayOf(1,1,1), intArrayOf(0,0,0)),
            arrayOf(intArrayOf(0,1,0), intArrayOf(0,1,1), intArrayOf(0,1,0)),
            arrayOf(intArrayOf(0,0,0), intArrayOf(1,1,1), intArrayOf(0,1,0)),
            arrayOf(intArrayOf(0,1,0), intArrayOf(1,1,0), intArrayOf(0,1,0)),
        ),
        listOf(
            arrayOf(intArrayOf(0,0,1), intArrayOf(1,1,1), intArrayOf(0,0,0)),
            arrayOf(intArrayOf(0,1,0), intArrayOf(0,1,0), intArrayOf(0,1,1)),
            arrayOf(intArrayOf(0,0,0), intArrayOf(1,1,1), intArrayOf(1,0,0)),
            arrayOf(intArrayOf(1,1,0), intArrayOf(0,1,0), intArrayOf(0,1,0)),
        ),
        listOf(
            arrayOf(intArrayOf(1,0,0), intArrayOf(1,1,1), intArrayOf(0,0,0)),
            arrayOf(intArrayOf(0,1,1), intArrayOf(0,1,0), intArrayOf(0,1,0)),
            arrayOf(intArrayOf(0,0,0), intArrayOf(1,1,1), intArrayOf(0,0,1)),
            arrayOf(intArrayOf(0,1,0), intArrayOf(0,1,0), intArrayOf(1,1,0)),
        ),
        listOf(
            arrayOf(intArrayOf(0,1,1), intArrayOf(1,1,0), intArrayOf(0,0,0)),
            arrayOf(intArrayOf(0,1,0), intArrayOf(0,1,1), intArrayOf(0,0,1)),
            arrayOf(intArrayOf(0,0,0), intArrayOf(0,1,1), intArrayOf(1,1,0)),
            arrayOf(intArrayOf(1,0,0), intArrayOf(1,1,0), intArrayOf(0,1,0)),
        ),
        listOf(
            arrayOf(intArrayOf(1,1,0), intArrayOf(0,1,1), intArrayOf(0,0,0)),
            arrayOf(intArrayOf(0,0,1), intArrayOf(0,1,1), intArrayOf(0,1,0)),
            arrayOf(intArrayOf(0,0,0), intArrayOf(1,1,0), intArrayOf(0,1,1)),
            arrayOf(intArrayOf(0,1,0), intArrayOf(1,1,0), intArrayOf(1,0,0)),
        ),
    )
    private val kicksJLSTZ = arrayOf(
        arrayOf(intArrayOf(0,0), intArrayOf(-1,0), intArrayOf(-1,1), intArrayOf(0,-2), intArrayOf(-1,-2)),
        arrayOf(intArrayOf(0,0), intArrayOf(1,0), intArrayOf(1,-1), intArrayOf(0,2), intArrayOf(1,2)),
        arrayOf(intArrayOf(0,0), intArrayOf(1,0), intArrayOf(1,1), intArrayOf(0,-2), intArrayOf(1,-2)),
        arrayOf(intArrayOf(0,0), intArrayOf(-1,0), intArrayOf(-1,-1), intArrayOf(0,2), intArrayOf(-1,2)),
    )
    private val kicksI = arrayOf(
        arrayOf(intArrayOf(0,0), intArrayOf(-2,0), intArrayOf(1,0), intArrayOf(-2,-1), intArrayOf(1,2)),
        arrayOf(intArrayOf(0,0), intArrayOf(-1,0), intArrayOf(2,0), intArrayOf(-1,2), intArrayOf(2,-1)),
        arrayOf(intArrayOf(0,0), intArrayOf(2,0), intArrayOf(-1,0), intArrayOf(2,1), intArrayOf(-1,-2)),
        arrayOf(intArrayOf(0,0), intArrayOf(1,0), intArrayOf(-2,0), intArrayOf(1,-2), intArrayOf(-2,1)),
    )
    private val speedCurve = floatArrayOf(
        0.800f, 0.717f, 0.633f, 0.550f, 0.467f, 0.383f, 0.300f, 0.217f, 0.133f, 0.100f,
        0.083f, 0.083f, 0.083f, 0.067f, 0.067f, 0.067f, 0.050f, 0.050f, 0.050f, 0.033f,
    )
    private val tBoard = Array(rows) { IntArray(cols) { -1 } }   // -1 empty else piece index
    private var bag = mutableListOf<Int>()
    private var curPiece = 0; private var curRot = 0; private var curX = 0; private var curY = 0
    private var nextPiece = 0
    private var lines = 0
    private var lockTimer = 0f; private var grounded = false
    private val lockDelay = 0.5f

    // ── Race state ────────────────────────────────────────────────────────────
    // Player is an F1 silhouette (1-3-1-1-3 cells top-to-bottom). 3 wide, 5 tall.
    // Юджин: "машинка — кубиком?? Серьёзно? Нарисуй как болид F1 сверху".
    private var carCol = 3                       // F1 nose column (carCol..carCol+2 is the chassis span)
    private val carTopRow = rows - 5
    // F1 silhouette: 5 rows × 3 cols, '#' = solid cell.
    private val F1_SHAPE: Array<String> = arrayOf(
        " # ",      // row 0 — nose tip
        "###",      // row 1 — front wing
        " # ",      // row 2 — cockpit
        " # ",      // row 3 — engine
        "###",      // row 4 — rear wing
    )
    private data class RaceCar(var col: Int, var row: Int)
    private val raceCars = mutableListOf<RaceCar>()
    private var raceSpawn = 0

    // ── Snake state ───────────────────────────────────────────────────────────
    private val snake = ArrayDeque<Int>()        // packed row*cols+col, head = first
    private var snDir = 1; private var snDy = 0   // current dir (dx, dy)
    private var snPendDx = 1; private var snPendDy = 0
    private var snFood = 0

    // ── Shooter state ─────────────────────────────────────────────────────────
    private var shipCol = 4
    private val invaders = mutableListOf<Int>()  // packed row*cols+col
    private var invDir = 1
    private var bulletCol = -1; private var bulletRow = -1
    private var shootStepAcc = 0f

    override fun init(ctx: GameContext) {
        phase = Phase.SELECT
        mini = Mini.TETRIS
        best = ctx.scores.get(mini.key)
        // Construct unconditionally (only pre-renders a PCM buffer) so the SND
        // button can turn music ON even when it was off at launch.
        melody = com.pixelclassics.app.audio.MelodyPlayer(
            com.pixelclassics.app.audio.Melodies.Korobeiniki, bpm = 140f, volume = 0.04f,
        ).also { if (ctx.settings.musicEnabled) it.start() }
    }

    override fun dispose() { melody?.stop(); melody = null }
    override fun onPause(ctx: GameContext) { melody?.stop() }
    override fun onResume(ctx: GameContext) { if (ctx.settings.musicEnabled) melody?.start() }

    override val helpLines: List<String> get() = listOf(
        "MODE — choose game (Tetra Fall / Race / Snake / Shooter)",
        "◀ ▶ ▲ ▼ — move,  ROTATE — rotate / fire,  START — go",
        "It's a real Brick-Game handheld — buttons are on the device",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "MODE — выбор игры (Tetra Fall / Race / Snake / Shooter)",
        "◀ ▶ ▲ ▼ — движение,  ROTATE — поворот / огонь,  START — старт",
        "Это настоящий Brick Game — кнопки прямо на корпусе",
    ) else helpLines

    // ── Lifecycle ───────────────────────────────────────────────────────────--
    private fun cycle(dir: Int, ctx: GameContext) {
        val i = (mini.ordinal + dir + minis.size) % minis.size
        mini = minis[i]
        best = ctx.scores.get(mini.key)
        ctx.sound.move()
    }

    private fun startMini(ctx: GameContext) {
        score = 0; level = 1; speed = 0; tickAcc = 0f; time = 0f
        when (mini) {
            Mini.TETRIS -> {
                for (r in 0 until rows) for (c in 0 until cols) tBoard[r][c] = -1
                bag.clear(); lines = 0; lockTimer = 0f; grounded = false
                nextPiece = nextFromBag(); spawn(ctx)
            }
            Mini.RACE -> { carCol = 4; raceCars.clear(); raceSpawn = 0 }
            Mini.SNAKE -> {
                snake.clear()
                val sr = rows / 2
                for (c in 4 downTo 2) snake.addLast(sr * cols + c)
                snDir = 1; snDy = 0; snPendDx = 1; snPendDy = 0
                placeFood()
            }
            Mini.SHOOTER -> { shipCol = 4; bulletCol = -1; bulletRow = -1; invDir = 1; shootStepAcc = 0f; spawnWave() }
        }
        best = ctx.scores.get(mini.key)
        phase = Phase.PLAY
        ctx.sound.click()
    }

    private fun gameOver(ctx: GameContext) {
        phase = Phase.OVER
        if (score > best) { best = score; ctx.scores.set(mini.key, score) }
        ctx.sound.gameOver()
    }

    // ── Update dispatch ─────────────────────────────────────────────────────--
    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        // DAS auto-repeat for the left/right movement buttons.
        heldBtn?.let { b ->
            if ((b == "L" || b == "R") && time - heldSince >= dasDelay && time - lastRepeat >= dasRate) {
                press(b, ctx, repeat = true); lastRepeat = time
            }
        }
        if (phase != Phase.PLAY || paused) return
        when (mini) {
            Mini.TETRIS -> updateTetris(dt, ctx)
            Mini.RACE -> updateRace(dt, ctx)
            Mini.SNAKE -> updateSnake(dt, ctx)
            Mini.SHOOTER -> updateShooter(dt, ctx)
        }
    }

    // ── Tetris logic ──────────────────────────────────────────────────────────
    private fun nextFromBag(): Int { if (bag.isEmpty()) { bag.addAll(0..6); bag.shuffle() }; return bag.removeAt(bag.size - 1) }
    private fun spawn(ctx: GameContext) {
        curPiece = nextPiece; nextPiece = nextFromBag(); curRot = 0
        val s = shapes[curPiece][0]; curX = (cols - s[0].size) / 2; curY = -1
        if (collides(curPiece, curRot, curX, curY)) { curY = 0; if (collides(curPiece, curRot, curX, curY)) { gameOver(ctx); return } }
        grounded = false; lockTimer = 0f
    }
    private fun collides(piece: Int, rot: Int, px: Int, py: Int): Boolean {
        val s = shapes[piece][rot]
        for (r in s.indices) for (c in s[r].indices) {
            if (s[r][c] == 0) continue
            val nx = px + c; val ny = py + r
            if (nx < 0 || nx >= cols || ny >= rows) return true
            if (ny >= 0 && tBoard[ny][nx] != -1) return true
        }
        return false
    }
    private fun tMove(dx: Int, ctx: GameContext) {
        if (!collides(curPiece, curRot, curX + dx, curY)) { curX += dx; ctx.sound.move(); if (grounded) lockTimer = 0f }
    }
    private fun tRotate(ctx: GameContext) {
        if (curPiece == 1) return
        val newRot = (curRot + 1) % 4
        val kicks = if (curPiece == 0) kicksI else kicksJLSTZ
        for (k in kicks[curRot]) {
            if (!collides(curPiece, newRot, curX + k[0], curY - k[1])) {
                curX += k[0]; curY -= k[1]; curRot = newRot; if (grounded) lockTimer = 0f; ctx.sound.rotate(); return
            }
        }
    }
    private fun tSoft(ctx: GameContext) { if (!collides(curPiece, curRot, curX, curY + 1)) { curY++; score++; grounded = false; lockTimer = 0f } }
    private fun tHard(ctx: GameContext) { var d = 0; while (!collides(curPiece, curRot, curX, curY + 1)) { curY++; d++ }; score += d * 2; ctx.sound.drop(); tLock(ctx) }
    private fun tLock(ctx: GameContext) {
        val s = shapes[curPiece][curRot]
        for (r in s.indices) for (c in s[r].indices) {
            if (s[r][c] == 0) continue
            val ny = curY + r; val nx = curX + c
            if (ny < 0) { gameOver(ctx); return }
            tBoard[ny][nx] = curPiece
        }
        var cleared = 0; var r = rows - 1
        while (r >= 0) {
            if ((0 until cols).all { tBoard[r][it] != -1 }) {
                for (rr in r downTo 1) for (c in 0 until cols) tBoard[rr][c] = tBoard[rr - 1][c]
                for (c in 0 until cols) tBoard[0][c] = -1; cleared++
            } else r--
        }
        if (cleared > 0) {
            score += intArrayOf(0, 100, 300, 500, 800)[cleared] * level
            lines += cleared; level = lines / 10 + 1; speed = level - 1
            if (cleared == 4) ctx.sound.tetris() else ctx.sound.lineClear()
            if (score > best) { best = score; ctx.scores.set(mini.key, score) }
        }
        spawn(ctx)
    }
    private fun ghostY(): Int { var gy = curY; while (!collides(curPiece, curRot, curX, gy + 1)) gy++; return gy }
    private fun updateTetris(dt: Float, ctx: GameContext) {
        tickAcc += dt
        val interval = speedCurve[(level - 1).coerceIn(0, speedCurve.size - 1)]
        if (tickAcc >= interval) {
            tickAcc = 0f
            if (!collides(curPiece, curRot, curX, curY + 1)) { curY++; grounded = false } else grounded = true
        }
        if (grounded) {
            if (collides(curPiece, curRot, curX, curY + 1)) { lockTimer += dt; if (lockTimer >= lockDelay) { tLock(ctx); grounded = false; lockTimer = 0f } }
            else { grounded = false; lockTimer = 0f }
        }
    }

    // ── Race logic ────────────────────────────────────────────────────────────
    private fun updateRace(dt: Float, ctx: GameContext) {
        speed = (score / 8)
        tickAcc += dt
        val interval = (0.32f - speed * 0.02f).coerceAtLeast(0.08f)
        if (tickAcc < interval) return
        tickAcc = 0f
        for (rc in raceCars) rc.row++
        // Score for cars that passed the bottom; remove them.
        val passed = raceCars.count { it.row > rows }
        if (passed > 0) { score += passed; ctx.sound.move() }
        raceCars.removeAll { it.row > rows }
        // Spawn a new oncoming car aligned to a 2-wide lane. The player's F1
        // silhouette is 5 rows tall, so we MUST guarantee at least a 6-row gap
        // ahead of any spawn in the SAME lane — otherwise you get a same-lane
        // wall and the player physically can't dodge. Юджин: «машинка 5 длиной,
        // о зазоре подумала?». Now: spawn slower at low speeds (every ~7 ticks),
        // and refuse a spawn into a lane whose top-most car is still within 6
        // rows of the ceiling.
        raceSpawn++
        val spawnPeriod = (7 - speed / 2).coerceAtLeast(3)
        if (raceSpawn % spawnPeriod == 0 || raceCars.isEmpty()) {
            val lanes = intArrayOf(0, 2, 4, 6, 8)
            val safeLanes = lanes.filter { lane ->
                raceCars.none { it.col == lane && it.row < 6 }
            }
            if (safeLanes.isNotEmpty()) raceCars += RaceCar(safeLanes.random(), -1)
        }
        // Collision with player — cell-precise to the F1 silhouette so the
        // empty corners don't read as deadly. (Юджин: «коллизии нужны чётче».)
        for (rc in raceCars) {
            for (dr in 0..1) for (dc in 0..1) {
                val obR = rc.row + dr; val obC = rc.col + dc
                val pr = obR - carTopRow
                val pc = obC - carCol
                if (pr in F1_SHAPE.indices && pc in 0 until F1_SHAPE[pr].length && F1_SHAPE[pr][pc] == '#') {
                    gameOver(ctx); return
                }
            }
        }
        level = speed + 1
    }
    private fun raceMove(dir: Int) { carCol = (carCol + dir * 2).coerceIn(0, cols - 3) }

    // ── Snake logic ───────────────────────────────────────────────────────────
    private fun placeFood() {
        val free = ArrayList<Int>()
        for (i in 0 until rows * cols) if (!snake.contains(i)) free += i
        snFood = if (free.isEmpty()) -1 else free.random()
    }
    private fun updateSnake(dt: Float, ctx: GameContext) {
        speed = (score / 5)
        tickAcc += dt
        val interval = (0.22f - speed * 0.012f).coerceAtLeast(0.07f)
        if (tickAcc < interval) return
        tickAcc = 0f
        snDir = snPendDx; snDy = snPendDy
        val head = snake.first()
        val hc = head % cols; val hr = head / cols
        val nc = hc + snDir; val nr = hr + snDy
        if (nc < 0 || nc >= cols || nr < 0 || nr >= rows) { gameOver(ctx); return }
        val np = nr * cols + nc
        val grow = np == snFood
        if (snake.contains(np) && !(np == snake.last() && !grow)) { gameOver(ctx); return }
        snake.addFirst(np)
        if (grow) { score += 10; level = score / 50 + 1; ctx.sound.eat(); placeFood() }
        else snake.removeLast()
    }
    private fun snakeTurn(dx: Int, dy: Int) {
        if (dx != 0 && snDir != 0) return       // can't reverse along the same axis instantly
        if (dy != 0 && snDy != 0) return
        if (dx == -snDir && dy == -snDy) return
        snPendDx = dx; snPendDy = dy
    }

    // ── Shooter logic ─────────────────────────────────────────────────────────
    private fun spawnWave() {
        invaders.clear()
        val waveRows = (2 + level).coerceAtMost(5)
        for (r in 0 until waveRows) for (c in 1 until cols - 1) if ((r + c) % 2 == 0) invaders += r * cols + c
        invDir = 1
    }
    private fun updateShooter(dt: Float, ctx: GameContext) {
        // Bullet travels up every frame-ish.
        if (bulletRow >= 0) {
            shootStepAcc += dt
            if (shootStepAcc >= 0.03f) {
                shootStepAcc = 0f
                bulletRow--
                if (bulletRow < 0) { bulletCol = -1 }
                else {
                    val hit = bulletRow * cols + bulletCol
                    if (invaders.remove(hit)) { score += 5; bulletRow = -1; bulletCol = -1; ctx.sound.explosion() }
                }
            }
        }
        // Invaders step left/right and descend.
        tickAcc += dt
        val interval = (0.6f - level * 0.04f).coerceAtLeast(0.18f)
        if (tickAcc >= interval) {
            tickAcc = 0f
            val colsUsed = invaders.map { it % cols }
            val hitEdge = (invDir > 0 && colsUsed.any { it >= cols - 1 }) || (invDir < 0 && colsUsed.any { it <= 0 })
            if (hitEdge) {
                invDir = -invDir
                val moved = invaders.map { it + cols }.toMutableList()   // descend a row
                invaders.clear(); invaders.addAll(moved)
            } else {
                val moved = invaders.map { it + invDir }.toMutableList()
                invaders.clear(); invaders.addAll(moved)
            }
            if (invaders.any { it / cols >= rows - 1 }) { gameOver(ctx); return }
            if (invaders.isEmpty()) { level++; score += 20; spawnWave(); ctx.sound.levelUp() }
        }
    }
    private fun shipMove(dir: Int) { shipCol = (shipCol + dir).coerceIn(0, cols - 1) }
    private fun fire(ctx: GameContext) { if (bulletRow < 0) { bulletCol = shipCol; bulletRow = rows - 2; shootStepAcc = 0f; ctx.sound.shoot() } }

    // ── Input ─────────────────────────────────────────────────────────────────
    private fun toggleMute(ctx: GameContext) {
        val on = !ctx.settings.musicEnabled
        ctx.settings.musicEnabled = on
        if (on) melody?.start() else melody?.stop()
        ctx.sound.click()
    }

    // Keyboard/D-pad routing (Юджин: «в тетрисе стрелки тоже должны
    // работать») — arrows map onto the device buttons, FIRE = ROTATE/START.
    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (dx == 0 && dy == 0) { heldBtn = null; return }
        when {
            dx < 0 -> press("L", ctx)
            dx > 0 -> press("R", ctx)
            dy < 0 -> press("UP", ctx)
            dy > 0 -> press("DOWN", ctx)
        }
    }

    override fun onFire(ctx: GameContext) {
        press(if (phase == Phase.PLAY) "ROT" else "START", ctx)
    }

    private fun press(b: String, ctx: GameContext, repeat: Boolean = false) {
        // SOUND + LIGHT work in any phase.
        if (b == "SND") { if (!repeat) toggleMute(ctx); return }
        if (b == "LIGHT") {
            if (!repeat) {
                // While paused, LIGHT doubles as "exit to BRIX game-select" —
                // Юджин: «нужна явная кнопка выхода из текущей мини-игры».
                if (phase == Phase.PLAY && paused) {
                    // Commit the run's score before abandoning the mini-game —
                    // otherwise a new best set in Race/Snake/Shooter is lost.
                    if (score > best) { best = score; ctx.scores.set(mini.key, score) }
                    phase = Phase.SELECT; paused = false; ctx.sound.move()
                } else {
                    bw = !bw; ctx.sound.click()
                }
            }
            return
        }
        when (phase) {
            Phase.SELECT -> if (!repeat) when (b) {
                "L" -> cycle(-1, ctx); "R" -> cycle(1, ctx)
                "ROT", "START", "UP" -> startMini(ctx)
            }
            Phase.OVER -> if (!repeat) when (b) {
                "START" -> { phase = Phase.SELECT; ctx.sound.move() }   // back to game select
                "ROT", "UP" -> startMini(ctx)                            // quick replay
            }
            Phase.PLAY -> {
                if (b == "START") { if (!repeat) { paused = !paused; ctx.sound.click() }; return }
                if (paused) return
                when (mini) {
                    // Tetris: crosspad moves; UP & big button rotate; DOWN drops the piece.
                    Mini.TETRIS -> when (b) {
                        "L" -> tMove(-1, ctx); "R" -> tMove(1, ctx)
                        "UP", "ROT" -> if (!repeat) tRotate(ctx)
                        "DOWN" -> if (!repeat) tHard(ctx)
                    }
                    Mini.RACE -> when (b) { "L" -> raceMove(-1); "R" -> raceMove(1) }
                    Mini.SNAKE -> if (!repeat) when (b) {
                        "L" -> snakeTurn(-1, 0); "R" -> snakeTurn(1, 0); "UP" -> snakeTurn(0, -1); "DOWN" -> snakeTurn(0, 1)
                    }
                    Mini.SHOOTER -> when (b) {
                        "L" -> shipMove(-1); "R" -> shipMove(1)
                        "ROT", "UP" -> if (!repeat) fire(ctx)
                    }
                }
            }
        }
    }

    // Button hit targets (cx, cy, r) on the chassis below the LCD.
    private val btnSnd = Triple(108f, 656f, 22f)
    private val btnStart = Triple(220f, 656f, 22f)
    private val btnLight = Triple(332f, 656f, 22f)
    private val btnUp = Triple(120f, 716f, 30f)
    private val btnDown = Triple(120f, 820f, 30f)
    private val btnLeft = Triple(66f, 768f, 30f)
    private val btnRight = Triple(174f, 768f, 30f)
    private val btnRot = Triple(344f, 772f, 50f)

    private fun hit(x: Float, y: Float, t: Triple<Float, Float, Float>): Boolean =
        hypot(x - t.first, y - t.second) <= t.third

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        val b = when {
            hit(x, y, btnLeft) -> "L"; hit(x, y, btnRight) -> "R"
            hit(x, y, btnUp) -> "UP"; hit(x, y, btnDown) -> "DOWN"
            hit(x, y, btnRot) -> "ROT"; hit(x, y, btnStart) -> "START"
            hit(x, y, btnLight) -> "LIGHT"; hit(x, y, btnSnd) -> "SND"
            else -> return
        }
        press(b, ctx)
        if (b == "L" || b == "R") { heldBtn = b; heldId = id; heldSince = time; lastRepeat = time }
    }
    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        // Only the finger that owns the held button releases it — a second
        // finger tapping ROTATE must not cancel a held LEFT/RIGHT repeat.
        if (id == heldId) { heldBtn = null; heldId = -1L }
    }

    // ── Drawing ─────────────────────────────────────────────────────────────--
    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Chassis.
        drawRect(body, topLeft = Offset.Zero, size = Size(W, H))
        drawRect(bodyDark, topLeft = Offset(0f, 0f), size = Size(W, 6f))
        drawRect(Color(0x33000000), topLeft = Offset(0f, H - 8f), size = Size(W, 8f))

        // "SUPER" header pill.
        drawRect(bodyDark, topLeft = Offset(W / 2f - 52f, 22f), size = Size(104f, 22f))
        drawDosText("SUPER", x = W / 2f, y = 38f, color = Color(0xFFE7CF52), size = 13f, bold = true, align = TextAlign.CENTER)
        // Decorative brick-corner ornaments around the screen.
        drawCornerBricks(this)

        // LCD bezel + screen.
        drawRect(Color(0xFF3A3A30), topLeft = Offset(lcdX - 8f, lcdY - 8f), size = Size(lcdW + 16f, lcdH + 16f))
        drawRect(Color.Black, topLeft = Offset(lcdX - 3f, lcdY - 3f), size = Size(lcdW + 6f, lcdH + 6f))
        drawRect(if (bw) lcdBg else lcdColorBg, topLeft = Offset(lcdX, lcdY), size = Size(lcdW, lcdH))

        // Faint phantom dot grid in the well (the classic ghost cells).
        for (r in 0 until rows) for (c in 0 until cols) cellAt(c, r, false)

        // Active game content.
        when (mini) {
            Mini.TETRIS -> drawTetris(this)
            Mini.RACE -> drawRace(this)
            Mini.SNAKE -> drawSnake(this)
            Mini.SHOOTER -> drawShooter(this)
        }

        // Readout column.
        drawReadouts(this, ctx)

        // SELECT / OVER / PAUSE overlays inside the LCD.
        if (phase == Phase.SELECT) drawSelect(this, ctx)
        if (phase == Phase.OVER) drawOver(this, ctx)
        if (phase == Phase.PLAY && paused) {
            drawRect((if (bw) lcdBg else lcdColorBg).copy(alpha = 0.82f), topLeft = Offset(lcdX, lcdY), size = Size(lcdW, lcdH))
            val ink = if (bw) dotOn else lcdInkColor
            drawDosText(ctx.tr("PAUSE", "ПАУЗА"), x = lcdX + lcdW / 2f, y = lcdY + lcdH / 2f - 30f, color = ink, size = 30f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("START — RESUME", "START — ПРОДОЛЖИТЬ"), x = lcdX + lcdW / 2f, y = lcdY + lcdH / 2f + 20f, color = ink, size = 14f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("LIGHT — BACK TO MENU", "LIGHT — В МЕНЮ"), x = lcdX + lcdW / 2f, y = lcdY + lcdH / 2f + 50f, color = ink, size = 14f, bold = true, align = TextAlign.CENTER)
        }

        // Chassis face + buttons.
        drawButtons(this, ctx)
        drawDosText("B R I X", x = W / 2f, y = H - 40f, color = bodyDark, size = 26f, bold = true, align = TextAlign.CENTER)
        drawDosText("BRICK GAME · 4 in 1", x = W / 2f, y = H - 18f, color = Color(0xFF8A2B2B), size = 11f, bold = true, align = TextAlign.CENTER)
    }

    private fun DrawScope.cellAt(col: Int, row: Int, on: Boolean, color: Color = dotOn) {
        val x = wellX + col * cell; val y = wellY + row * cell
        if (!on) {
            // Faint phantom cell — dark on the green LCD, light on the dark colour screen.
            drawRect(if (bw) Color(0x16000000) else Color(0x10FFFFFF), topLeft = Offset(x + 3f, y + 3f), size = Size(cell - 6f, cell - 6f), style = Stroke(width = 1f))
            return
        }
        // B&W mode: monochrome dark brick on green (authentic LCD). Colour mode: the
        // object's colour as an 8-bit brick on the dark screen.
        val cc = if (bw) dotOn.copy(alpha = color.alpha) else color
        val ring = if (bw) Color(0x66000000) else Color(0x40FFFFFF)
        drawRect(ring, topLeft = Offset(x + 1f, y + 1f), size = Size(cell - 2f, cell - 2f), style = Stroke(width = 2.6f))
        drawRect(cc, topLeft = Offset(x + 2.5f, y + 2.5f), size = Size(cell - 5f, cell - 5f), style = Stroke(width = 2.2f))
        drawRect(cc, topLeft = Offset(x + 6f, y + 6f), size = Size(cell - 12f, cell - 12f))
    }

    private fun DrawScope.miniCell(x: Float, y: Float, s: Float, on: Boolean) {
        if (on) {
            val ink = if (bw) dotOn else lcdInkColor
            drawRect(ink, topLeft = Offset(x + 1f, y + 1f), size = Size(s - 2f, s - 2f), style = Stroke(width = 1.6f))
            drawRect(ink, topLeft = Offset(x + 3f, y + 3f), size = Size(s - 6f, s - 6f))
        }
    }

    private fun DrawScope.drawTetris(scope: DrawScope) {
        for (r in 0 until rows) for (c in 0 until cols) { val p = tBoard[r][c]; if (p != -1) cellAt(c, r, true, pieceColors[p]) }
        if (phase == Phase.PLAY) {
            val gy = ghostY()
            if (gy != curY) { val s = shapes[curPiece][curRot]; for (r in s.indices) for (c in s[r].indices) if (s[r][c] != 0) cellAt(curX + c, gy + r, true, pieceColors[curPiece].copy(alpha = 0.28f)) }
            val s = shapes[curPiece][curRot]
            for (r in s.indices) for (c in s[r].indices) { if (s[r][c] == 0) continue; val ny = curY + r; if (ny >= 0) cellAt(curX + c, ny, true, pieceColors[curPiece]) }
        }
    }
    private fun DrawScope.drawRace(scope: DrawScope) {
        if (phase != Phase.PLAY) return
        // The well's outer border already reads as the track — no extra brick
        // fence on the play area (Юджин: «ограждение не кубиками, не зажимай машину»).
        // Lane dashes down the centre (every 3 rows).
        for (r in 0 until rows step 3) cellAt(cols / 2, r, true, Color(0x55FFFFFF))
        // Oncoming cars — 2×2 reds.
        for (rc in raceCars) for (dr in 0..1) for (dc in 0..1) { val r = rc.row + dr; val c = rc.col + dc; if (r in 0 until rows) cellAt(c, r, true, Color(0xFFFF1744)) }
        // Player F1 silhouette — pattern 1-3-1-1-3.
        for (pr in F1_SHAPE.indices) for (pc in 0 until F1_SHAPE[pr].length) {
            if (F1_SHAPE[pr][pc] == '#') cellAt(carCol + pc, carTopRow + pr, true, Color(0xFF00E5FF))
        }
    }
    private fun DrawScope.drawSnake(scope: DrawScope) {
        if (phase != Phase.PLAY && phase != Phase.OVER) return
        for (p in snake) cellAt(p % cols, p / cols, true, Color(0xFF00E676))
        if (snFood >= 0) cellAt(snFood % cols, snFood / cols, true, Color(0xFFFF8800))
    }
    private fun DrawScope.drawShooter(scope: DrawScope) {
        if (phase != Phase.PLAY) return
        for (p in invaders) cellAt(p % cols, p / cols, true, Color(0xFFAA00FF))
        if (bulletRow >= 0) cellAt(bulletCol, bulletRow, true, Color(0xFFFFD600))
        cellAt(shipCol, rows - 1, true, Color(0xFF00E5FF))
    }

    private fun DrawScope.drawReadouts(scope: DrawScope, ctx: GameContext) {
        val ink = if (bw) dotOn else lcdInkColor
        val ghost = ink.copy(alpha = if (bw) 0.10f else 0.14f)
        var y = wellY + 16f
        // Labels stay in the small DOS-pixel font (they are descriptive, not numeric).
        fun label(t: String) {
            drawDosText(t, x = panelX, y = y, color = ink, size = 11f, bold = true)
            y += 14f
        }
        // Numeric values render in real 7-segment LCD glyphs — the "sticks
        // font" Юджин asked for (calculator / alarm-clock / Brick-Game look).
        fun segValue(t: String, height: Float = 22f) {
            com.pixelclassics.app.ui.theme.SevenSegment.drawText(
                this, t, panelX, y, height, ink, ghost,
            )
            y += height + 10f
        }
        label(ctx.tr("SCORE", "СЧЁТ"));    segValue(score.toString().padStart(6, '0'))
        label(ctx.tr("HI-SCORE", "РЕКОРД")); segValue(best.toString().padStart(6, '0'))
        label(ctx.tr("NEXT", "ДАЛЕЕ"))
        if (mini == Mini.TETRIS && phase == Phase.PLAY) {
            val ns = shapes[nextPiece][0]; val ps = 12f
            val ox = panelX + 6f; val oy = y
            for (r in ns.indices) for (c in ns[r].indices) if (ns[r][c] != 0) miniCell(ox + c * ps, oy + r * ps, ps, true)
            y += 56f
        } else { drawDosText(mini.title, x = panelX, y = y + 12f, color = ink, size = 12f, bold = true); y += 56f }
        label(ctx.tr("SPEED", "СКОР")); segValue(speed.toString().padStart(2, '0'), height = 20f)
        label(ctx.tr("LEVEL", "УРОВ")); segValue(level.toString().padStart(2, '0'), height = 20f)
        drawDosText(if (ctx.settings.musicEnabled) "SND ON" else "SND OFF", x = panelX, y = y + 14f, color = ink, size = 11f, bold = true)
        drawDosText(if (bw) "LCD" else "COLOR", x = panelX, y = y + 32f, color = ink, size = 11f, bold = true)
    }

    private fun DrawScope.drawSelect(scope: DrawScope, ctx: GameContext) {
        val ink = if (bw) dotOn else lcdInkColor
        drawRect((if (bw) lcdBg else lcdColorBg).copy(alpha = 0.9f), topLeft = Offset(lcdX, lcdY), size = Size(lcdW, lcdH))
        drawDosText(ctx.tr("SELECT GAME", "ВЫБОР ИГРЫ"), x = lcdX + lcdW / 2f, y = lcdY + 110f, color = ink, size = 20f, bold = true, align = TextAlign.CENTER)
        drawDosText("#${mini.no}", x = lcdX + lcdW / 2f, y = lcdY + 150f, color = ink, size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText(mini.title, x = lcdX + lcdW / 2f, y = lcdY + 220f, color = ink, size = 36f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("◀ ▶  change game", "◀ ▶  выбор игры"), x = lcdX + lcdW / 2f, y = lcdY + 290f, color = ink, size = 12f, align = TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText(ctx.tr("START / ROTATE — play", "START / ROTATE — играть"), x = lcdX + lcdW / 2f, y = lcdY + 360f, color = ink, size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText("HI  ${best.toString().padStart(6, '0')}", x = lcdX + lcdW / 2f, y = lcdY + 450f, color = ink, size = 12f, bold = true, align = TextAlign.CENTER)
    }
    private fun DrawScope.drawOver(scope: DrawScope, ctx: GameContext) {
        val ink = if (bw) dotOn else lcdInkColor
        drawRect((if (bw) lcdBg else lcdColorBg).copy(alpha = 0.9f), topLeft = Offset(lcdX, lcdY), size = Size(lcdW, lcdH))
        drawDosText(ctx.tr("GAME OVER", "ИГРА ОКОНЧЕНА"), x = lcdX + lcdW / 2f, y = lcdY + 180f, color = ink, size = 26f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("SCORE", "СЧЁТ") + " ${score.toString().padStart(6, '0')}", x = lcdX + lcdW / 2f, y = lcdY + 230f, color = ink, size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("HI   ", "РЕК  ") + " ${best.toString().padStart(6, '0')}", x = lcdX + lcdW / 2f, y = lcdY + 254f, color = ink, size = 14f, bold = true, align = TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText(ctx.tr("ROTATE — again   START — menu", "ROTATE — ещё раз   START — меню"), x = lcdX + lcdW / 2f, y = lcdY + 330f, color = ink, size = 12f, bold = true, align = TextAlign.CENTER)
    }

    private fun DrawScope.drawCornerBricks(scope: DrawScope) {
        val c = Color(0xFF2E7D32)
        fun brickAt(bx: Float, by: Float) { drawRect(c, topLeft = Offset(bx, by), size = Size(8f, 8f), style = Stroke(width = 2f)) }
        // Brick ornament clusters flanking the SUPER header.
        for (i in 0 until 3) for (j in 0 until 3) {
            brickAt(lcdX + 2f + j * 11f, 20f + i * 11f)
            brickAt(lcdX + lcdW - 35f + j * 11f, 20f + i * 11f)
        }
    }

    private fun DrawScope.drawButtons(scope: DrawScope, ctx: GameContext) {
        // Small action buttons: SOUND, START/PAUSE, LIGHT (colour↔B&W).
        roundBtn(btnSnd, if (ctx.settings.musicEnabled) "SND" else "MUTE", heldBtn == "SND")
        roundBtn(btnStart, if (phase == Phase.PLAY) "PAUSE" else "START", false)
        roundBtn(btnLight, if (bw) "B/W" else "CLR", false)
        // D-pad.
        roundBtn(btnUp, "▲", heldBtn == "UP")
        roundBtn(btnDown, "▼", heldBtn == "DOWN")
        roundBtn(btnLeft, "◀", heldBtn == "L")
        roundBtn(btnRight, "▶", heldBtn == "R")
        // Big action button: rotate (Tetris) / fire (Shooter) / confirm.
        val rotLabel = if (phase == Phase.PLAY && mini == Mini.SHOOTER) "FIRE" else "ROTATE"
        roundBtn(btnRot, rotLabel, false)
    }
    private fun DrawScope.roundBtn(t: Triple<Float, Float, Float>, label: String, down: Boolean) {
        val c = Offset(t.first, t.second); val r = t.third
        drawCircle(Color(0x55000000), radius = r + 3f, center = Offset(c.x, c.y + 3f))
        drawCircle(btnRim, radius = r + 1f, center = c)
        drawCircle(if (down) btnFaceDn else btnFace, radius = r, center = c)
        drawCircle(Color.White.copy(alpha = 0.3f), radius = r * 0.34f, center = Offset(c.x - r * 0.3f, c.y - r * 0.3f))
        val fs = if (r >= 44f) 14f else if (r >= 28f) 18f else 9f
        drawDosText(label, x = c.x, y = c.y + fs * 0.36f, color = Color(0xFF4A3A00), size = fs, bold = true, align = TextAlign.CENTER)
    }
}
