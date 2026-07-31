package com.pixelclassics.app.games.tictactoe

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawHandText
import kotlin.random.Random

/**
 * Tic-Tac-Toe — pixel-art 3×3 grid, two modes:
 *   - VS CPU: simple priority AI (win → block → centre → corner → random).
 *     Not minimax — we want the human to win sometimes, otherwise it's no fun.
 *   - VS PLAYER: pass-and-play, hot-seat on one phone.
 *
 * Chunky pixel X / O drawn from blocks; clean grid lines; turn indicator at top.
 * Click a cell to play. Click anywhere when the round ends to start a new round.
 */
class TicTacToeGame : Game {
    override val id: String = "tic_tac_toe"
    override val titleResId: Int = R.string.game_tic_tac_toe
    override val backgroundColor: Color = Color(0xFFF6F1DC)
    override val introText: String = TIC_TAC_TOE_INTRO_EN
    override fun introTextFor(lang: String): String = pickTicTacToeIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PAPER_NOTEBOOK
    override val helpLines: List<String> = listOf(
        "Click a cell to place X or O",
        "Choose VS CPU or VS PLAYER (hot-seat) from the title",
        "Three in a row wins.  Solved game — best played with a friend.",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Кликни клетку — поставишь X или O",
        "На титуле выбери ПРОТИВ CPU или ВДВОЁМ (за одним экраном)",
        "Три в ряд — победа.  Игра решена — лучше всего играть с другом.",
    ) else helpLines

    private val W = 480f
    private val H = 640f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    // School-notebook ink: dark ballpoint for the grid/text, a red pen for the
    // crosses and a blue pen for the noughts — the way kids actually draw it.
    private val ink = Color(0xFF2A2A38)
    private val inkDim = Color(0xFF6E6A5C)
    private val cross = Color(0xFFB33327)
    private val circle = Color(0xFF243F86)
    private val highlight = Color(0xFF1E7E42)   // green pencil strike-through

    private enum class Phase { TITLE, PLAY, OVER }
    private enum class Mode { CPU, P2 }
    private enum class Mark { EMPTY, X, O }

    private val board = Array(3) { Array(3) { Mark.EMPTY } }
    private var phase = Phase.TITLE
    private var mode = Mode.CPU
    private var turn = Mark.X         // X always starts
    private var winner: Mark = Mark.EMPTY
    private var winLine: Triple<Int, Int, Int>? = null   // packed (r1c1, r2c2, r3c3) as row*3+col
    private var roundsX = 0
    private var roundsO = 0
    private var draws = 0
    private var time = 0f

    private val gridLeft = 60f
    private val gridTop = 200f
    private val cellSize = 120f

    override fun init(ctx: GameContext) {
        reset()
    }

    private fun reset() {
        for (r in 0..2) for (c in 0..2) board[r][c] = Mark.EMPTY
        turn = Mark.X
        winner = Mark.EMPTY
        winLine = null
        phase = Phase.PLAY
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (phase != Phase.PLAY) return
        // CPU plays as O after a short, kid-friendly delay.
        if (mode == Mode.CPU && turn == Mark.O && time > 0.4f) {
            val mv = cpuMove() ?: return
            place(mv.first, mv.second, ctx)
            time = 0f
        }
    }

    private fun place(r: Int, c: Int, ctx: GameContext) {
        if (phase != Phase.PLAY || board[r][c] != Mark.EMPTY) return
        board[r][c] = turn
        ctx.sound.click()
        // Restart the CPU "thinking" delay from the moment the human moved.
        if (mode == Mode.CPU && turn == Mark.X) time = 0f
        val win = findWin(turn)
        if (win != null) {
            winner = turn
            winLine = win
            phase = Phase.OVER
            if (turn == Mark.X) roundsX++ else roundsO++
            ctx.sound.win()
            return
        }
        if (isFull()) {
            phase = Phase.OVER
            draws++
            ctx.sound.menuSelect()
            return
        }
        turn = if (turn == Mark.X) Mark.O else Mark.X
    }

    private fun findWin(m: Mark): Triple<Int, Int, Int>? {
        val lines = listOf(
            Triple(0 to 0, 0 to 1, 0 to 2),
            Triple(1 to 0, 1 to 1, 1 to 2),
            Triple(2 to 0, 2 to 1, 2 to 2),
            Triple(0 to 0, 1 to 0, 2 to 0),
            Triple(0 to 1, 1 to 1, 2 to 1),
            Triple(0 to 2, 1 to 2, 2 to 2),
            Triple(0 to 0, 1 to 1, 2 to 2),
            Triple(0 to 2, 1 to 1, 2 to 0),
        )
        for (l in lines) {
            val (a, b, c) = l
            if (board[a.first][a.second] == m &&
                board[b.first][b.second] == m &&
                board[c.first][c.second] == m
            ) return Triple(a.first * 3 + a.second, b.first * 3 + b.second, c.first * 3 + c.second)
        }
        return null
    }
    private fun isFull(): Boolean { for (r in 0..2) for (c in 0..2) if (board[r][c] == Mark.EMPTY) return false; return true }

    private fun cpuMove(): Pair<Int, Int>? {
        // 1) Take a winning move.
        winningMoveFor(Mark.O)?.let { return it }
        // 2) Block the opponent's winning move.
        winningMoveFor(Mark.X)?.let { return it }
        // 3) Centre.
        if (board[1][1] == Mark.EMPTY) return 1 to 1
        // 4) A corner.
        val corners = listOf(0 to 0, 0 to 2, 2 to 0, 2 to 2).filter { board[it.first][it.second] == Mark.EMPTY }
        if (corners.isNotEmpty()) return corners.random()
        // 5) Any free cell.
        for (r in 0..2) for (c in 0..2) if (board[r][c] == Mark.EMPTY) return r to c
        return null
    }
    private fun winningMoveFor(m: Mark): Pair<Int, Int>? {
        for (r in 0..2) for (c in 0..2) {
            if (board[r][c] != Mark.EMPTY) continue
            board[r][c] = m
            val w = findWin(m)
            board[r][c] = Mark.EMPTY
            if (w != null) return r to c
        }
        return null
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        when (phase) {
            Phase.TITLE -> {
                // Two big buttons in the middle: VS CPU / VS PLAYER.
                val midY = H * 0.55f
                val btnW = 160f
                val btnH = 64f
                if (y in (midY - btnH / 2f)..(midY + btnH / 2f)) {
                    if (x in (W / 2f - 180f)..(W / 2f - 180f + btnW)) { mode = Mode.CPU; reset(); return }
                    if (x in (W / 2f + 20f)..(W / 2f + 20f + btnW)) { mode = Mode.P2; reset(); return }
                }
                return
            }
            Phase.PLAY -> {
                if (mode == Mode.CPU && turn == Mark.O) return
                // toInt() truncates toward zero: a tap up to one cell LEFT of /
                // ABOVE the grid would land in row/col 0 — reject those first.
                if (x < gridLeft || y < gridTop) return
                val c = ((x - gridLeft) / cellSize).toInt()
                val r = ((y - gridTop) / cellSize).toInt()
                if (r in 0..2 && c in 0..2) place(r, c, ctx)
                return
            }
            Phase.OVER -> { reset(); return }
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawNotebookPaper(this)
        if (phase == Phase.TITLE) { drawTitle(this, ctx); return@with }

        // Header — scoreboard / turn indicator, all in pen.
        drawHandText(ctx.tr("TIC-TAC-TOE", "КРЕСТИКИ-НОЛИКИ"), x = W / 2f, y = 58f, color = ink, size = 30f, align = TextAlign.CENTER)
        val modeLabel = if (mode == Mode.CPU) ctx.tr("vs CPU", "против CPU") else ctx.tr("vs PLAYER", "вдвоём")
        drawHandText(modeLabel, x = W / 2f, y = 88f, color = inkDim, size = 15f, align = TextAlign.CENTER)
        // Score boxes.
        drawHandText("X  $roundsX", x = 26f, y = 132f, color = cross, size = 18f)
        drawHandText(ctx.tr("draws  $draws", "ничьи  $draws"), x = W / 2f, y = 132f, color = inkDim, size = 15f, align = TextAlign.CENTER)
        drawHandText("O  $roundsO", x = W - 26f, y = 132f, color = circle, size = 18f, align = TextAlign.RIGHT)

        if (phase == Phase.PLAY) {
            val turnCol = if (turn == Mark.X) cross else circle
            drawHandText(if (turn == Mark.X) ctx.tr("X's turn", "ходят X") else ctx.tr("O's turn", "ходят O"),
                x = W / 2f, y = 172f, color = turnCol, size = 19f, align = TextAlign.CENTER)
        }

        // Grid: the hand-drawn # (kids draw the hash, not a boxed grid).
        drawHandGrid(this)

        // Marks — red-pen crosses, blue-pen noughts, deterministic wobble.
        val half = cellSize * 0.30f
        for (r in 0..2) for (c in 0..2) {
            val cx = gridLeft + c * cellSize + cellSize / 2f
            val cy = gridTop + r * cellSize + cellSize / 2f
            when (board[r][c]) {
                Mark.X -> drawHandX(this, cx, cy, half, seed = r * 3 + c + 1, color = cross)
                Mark.O -> drawHandO(this, cx, cy, half, seed = r * 3 + c + 41, color = circle)
                Mark.EMPTY -> Unit
            }
        }
        // Winning line: a green-pencil strike-through end to end.
        winLine?.let { wl ->
            val a = wl.first; val b = wl.third
            val ax = gridLeft + (a % 3) * cellSize + cellSize / 2f
            val ay = gridTop + (a / 3) * cellSize + cellSize / 2f
            val bx = gridLeft + (b % 3) * cellSize + cellSize / 2f
            val by = gridTop + (b / 3) * cellSize + cellSize / 2f
            handStroke(this, ax, ay, bx, by, seed = 777, color = highlight, width = 5.5f)
        }

        if (phase == Phase.OVER) {
            val gw = cellSize * 3f
            val msg = when (winner) {
                Mark.X -> ctx.tr("X wins!", "X победили!")
                Mark.O -> ctx.tr("O wins!", "O победили!")
                Mark.EMPTY -> ctx.tr("draw", "ничья")
            }
            val col = when (winner) { Mark.X -> cross; Mark.O -> circle; else -> inkDim }
            drawRect(backgroundColor.copy(alpha = 0.82f), topLeft = Offset(0f, gridTop + gw + 20f), size = Size(W, 100f))
            drawHandText(msg, x = W / 2f, y = gridTop + gw + 62f, color = col, size = 34f, align = TextAlign.CENTER)
            if ((time * 2f).toInt() % 2 == 0)
                drawHandText(ctx.tr("tap — play again", "тап — ещё раз"), x = W / 2f, y = gridTop + gw + 94f, color = ink, size = 15f, align = TextAlign.CENTER)
        }
    }

    private fun drawTitle(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawHandText(ctx.tr("TIC - TAC - TOE", "КРЕСТИКИ-НОЛИКИ"), x = W / 2f, y = H * 0.18f, color = ink, size = 40f, align = TextAlign.CENTER)
        drawHandText(ctx.tr("EDSAC, Cambridge, 1952", "EDSAC, Кембридж, 1952"), x = W / 2f, y = H * 0.26f, color = inkDim, size = 15f, align = TextAlign.CENTER)
        // A doodled X and O flanking the title, for flavour.
        drawHandX(this, W * 0.16f, H * 0.185f, 26f, seed = 5, color = cross)
        drawHandO(this, W * 0.84f, H * 0.185f, 26f, seed = 9, color = circle)
        drawHandText(ctx.tr("Pick a mode:", "Выбери режим:"), x = W / 2f, y = H * 0.42f, color = ink, size = 19f, align = TextAlign.CENTER)
        // Two hand-boxed buttons.
        val midY = H * 0.55f
        val btnW = 160f; val btnH = 64f
        val b1x = W / 2f - 180f
        pencilRect(b1x, midY - btnH / 2f, btnW, btnH, seed = 11, color = ink)
        drawHandText(ctx.tr("vs CPU", "против CPU"), x = b1x + btnW / 2f, y = midY + 6f, color = ink, size = 20f, align = TextAlign.CENTER)
        val b2x = W / 2f + 20f
        pencilRect(b2x, midY - btnH / 2f, btnW, btnH, seed = 23, color = ink)
        drawHandText(ctx.tr("vs PLAYER", "вдвоём"), x = b2x + btnW / 2f, y = midY + 6f, color = ink, size = 20f, align = TextAlign.CENTER)
        drawHandText(ctx.tr("(hot-seat on one phone)", "(за одним телефоном)"), x = b2x + btnW / 2f, y = midY + btnH / 2f + 26f, color = inkDim, size = 12f, align = TextAlign.CENTER)
        drawHandText(ctx.tr("Round: X $roundsX · O $roundsO · Draws $draws", "Счёт: X $roundsX · O $roundsO · Ничьи $draws"), x = W / 2f, y = H * 0.86f, color = inkDim, size = 13f, align = TextAlign.CENTER)
    }

    /** The school exercise book: 18px squares + the pink margin line. */
    private fun drawNotebookPaper(scope: DrawScope) = with(scope) {
        drawRect(Color(0xFFF6F1DC), topLeft = Offset.Zero, size = Size(W, H))
        val sq = Color(0x2E4A6ACC)
        var gx = 0f
        while (gx < W) { drawRect(sq, topLeft = Offset(gx, 0f), size = Size(1f, H)); gx += 18f }
        var gy = 0f
        while (gy < H) { drawRect(sq, topLeft = Offset(0f, gy), size = Size(W, 1f)); gy += 18f }
        drawRect(Color(0x55D96A7B), topLeft = Offset(46f, 0f), size = Size(2f, H))
    }

    /** Wobbly ballpoint rectangle — deterministic jitter so it doesn't dance. */
    private fun DrawScope.pencilRect(x: Float, y: Float, w: Float, h: Float, seed: Int, color: Color) {
        val rnd = Random(seed)
        fun j() = (rnd.nextFloat() - 0.5f) * 2.6f
        val pts = listOf(
            Offset(x + j(), y + j()), Offset(x + w / 2f + j(), y + j()), Offset(x + w + j(), y + j()),
            Offset(x + w + j(), y + h / 2f + j()), Offset(x + w + j(), y + h + j()),
            Offset(x + w / 2f + j(), y + h + j()), Offset(x + j(), y + h + j()), Offset(x + j(), y + h / 2f + j()),
        )
        for (i in pts.indices) drawLine(color, pts[i], pts[(i + 1) % pts.size], strokeWidth = 1.7f)
    }

    /** A short wobbly pen stroke: subdivided with perpendicular jitter. */
    private fun handStroke(scope: DrawScope, x1: Float, y1: Float, x2: Float, y2: Float, seed: Int, color: Color, width: Float = 3.4f) = with(scope) {
        val rnd = Random(seed)
        fun j() = (rnd.nextFloat() - 0.5f) * 3.4f
        val n = 4
        var px = x1 + j(); var py = y1 + j()
        for (k in 1..n) {
            val t = k.toFloat() / n
            val nx = x1 + (x2 - x1) * t + (if (k < n) j() else 0f)
            val ny = y1 + (y2 - y1) * t + (if (k < n) j() else 0f)
            drawLine(color, Offset(px, py), Offset(nx, ny), strokeWidth = width)
            px = nx; py = ny
        }
    }

    /** The hand-drawn # over the play area (two verticals, two horizontals). */
    private fun drawHandGrid(scope: DrawScope) {
        val gw = cellSize * 3f
        val over = 10f
        for (i in 1..2) {
            val vx = gridLeft + i * cellSize
            handStroke(scope, vx, gridTop - over, vx, gridTop + gw + over, seed = 100 + i, color = ink, width = 3.6f)
            val hy = gridTop + i * cellSize
            handStroke(scope, gridLeft - over, hy, gridLeft + gw + over, hy, seed = 200 + i, color = ink, width = 3.6f)
        }
    }

    /** Red-pen cross: two crossing strokes. */
    private fun drawHandX(scope: DrawScope, cx: Float, cy: Float, half: Float, seed: Int, color: Color) {
        handStroke(scope, cx - half, cy - half, cx + half, cy + half, seed, color, 4.4f)
        handStroke(scope, cx + half, cy - half, cx - half, cy + half, seed + 31, color, 4.4f)
    }

    /** Blue-pen nought: a jittered ring that overshoots its start like a real one. */
    private fun drawHandO(scope: DrawScope, cx: Float, cy: Float, rad: Float, seed: Int, color: Color) = with(scope) {
        val rnd = Random(seed)
        val n = 18
        val start = rnd.nextFloat() * 0.6f
        var prev = Offset.Zero
        var has = false
        var i = 0
        while (i <= n + 2) {
            val a = start + i * (6.2832f * 1.05f / n)
            val rr = rad + (rnd.nextFloat() - 0.5f) * 5f
            val p = Offset(cx + kotlin.math.cos(a) * rr, cy + kotlin.math.sin(a) * rr * 0.94f)
            if (has) drawLine(color, prev, p, strokeWidth = 4.4f)
            prev = p; has = true; i++
        }
    }
}
