package com.pixelclassics.app.games.minesweeper

import com.pixelclassics.app.compat.nowMillis

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.abs
import kotlin.math.min
import kotlin.math.sin
import kotlin.random.Random

/** Win-95 Minesweeper. Tap = reveal, hold = flag, Easy/Medium/Hard. */
class MinesweeperGame : Game {
    override val id: String = "minesweeper"
    override val titleResId: Int = R.string.game_minesweeper
    override val backgroundColor: Color = Color(0xFFC0C0C0)
    override val introText: String = MINES_INTRO_EN
    override fun introTextFor(lang: String): String = pickMinesIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.WIN31_GREY
    override val helpLines: List<String> = listOf(
        "Click — reveal · hold — flag",
        "Numbers show mines adjacent.  Don't click a mine.",
        "Easy / Medium / Hard — pick from title",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Клик — открыть · зажать — флажок",
        "Цифры — сколько мин по соседству.  Не кликни мину.",
        "Легко / Средне / Сложно — выбор на титуле",
    ) else helpLines

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private val numColors = listOf(
        Color.Black,
        Color(0xFF0000FF), Color(0xFF008000), Color(0xFFFF0000), Color(0xFF000080),
        Color(0xFF800000), Color(0xFF008080), Color(0xFF000000), Color(0xFF808080),
    )

    private enum class Difficulty(val rows: Int, val cols: Int, val mines: Int, val label: String, val labelRu: String) {
        EASY(9, 9, 10, "EASY", "ЛЕГКО"),
        MEDIUM(12, 12, 25, "MEDIUM", "СРЕДНЕ"),
        HARD(16, 16, 40, "HARD", "СЛОЖНО"),
    }

    private var diff = Difficulty.EASY
    private var rows = diff.rows
    private var cols = diff.cols
    private var mines = diff.mines
    private var board: Array<IntArray> = Array(rows) { IntArray(cols) }   // -1 mine, else neighbour count
    private var revealed: Array<BooleanArray> = Array(rows) { BooleanArray(cols) }
    private var flagged: Array<BooleanArray> = Array(rows) { BooleanArray(cols) }
    private var firstClick = true
    private var gameOver = false
    private var gameWon = false
    private var seconds = 0f
    private var pressStart = 0L
    private var pressR = -1
    private var pressC = -1
    private var pressOrigin: Offset? = null
    private val longPressMs = 400L

    // Win celebration.
    private var winAnim = 0f
    private data class Confetti(var x: Float, var y: Float, var vy: Float, val color: Color, val w: Float)
    private val confetti = mutableListOf<Confetti>()

    // Layout — recomputed per draw to fit the board.
    private val headerH = 48f
    private val footerH = 40f
    private var cell = 24f
    private var boardX = 0f
    private var boardY = headerH + 6f
    private var boardW = 0f
    private var boardH = 0f

    override fun init(ctx: GameContext) {
        newGame()
    }

    private fun newGame() {
        rows = diff.rows; cols = diff.cols; mines = diff.mines
        board = Array(rows) { IntArray(cols) }
        revealed = Array(rows) { BooleanArray(cols) }
        flagged = Array(rows) { BooleanArray(cols) }
        firstClick = true; gameOver = false; gameWon = false; seconds = 0f
        winAnim = 0f; confetti.clear()
        pressR = -1; pressC = -1
    }

    private fun nextDiff(): Difficulty? = when (diff) {
        Difficulty.EASY -> Difficulty.MEDIUM
        Difficulty.MEDIUM -> Difficulty.HARD
        Difficulty.HARD -> null
    }

    private fun spawnConfetti() {
        confetti.clear()
        val palette = listOf(
            Color(0xFF0000FF), Color(0xFF008000), Color(0xFFFF0000),
            Color(0xFFFFC000), Color(0xFF00C0C0), Color(0xFFFF40FF),
        )
        repeat(64) {
            confetti += Confetti(
                Random.nextFloat() * W,
                -Random.nextFloat() * H,
                70f + Random.nextFloat() * 150f,
                palette.random(),
                5f + Random.nextFloat() * 7f,
            )
        }
    }

    private fun placeMines(safeR: Int, safeC: Int) {
        var placed = 0
        while (placed < mines) {
            val r = Random.nextInt(rows); val c = Random.nextInt(cols)
            if (board[r][c] == -1) continue
            if (abs(r - safeR) <= 1 && abs(c - safeC) <= 1) continue
            board[r][c] = -1; placed++
        }
        for (r in 0 until rows) for (c in 0 until cols) {
            if (board[r][c] == -1) continue
            var k = 0
            for (dr in -1..1) for (dc in -1..1) {
                val nr = r + dr; val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols && board[nr][nc] == -1) k++
            }
            board[r][c] = k
        }
    }

    private fun revealCell(r: Int, c: Int) {
        if (r !in 0 until rows || c !in 0 until cols) return
        if (revealed[r][c] || flagged[r][c]) return
        revealed[r][c] = true
        if (board[r][c] == 0) for (dr in -1..1) for (dc in -1..1) revealCell(r + dr, c + dc)
    }

    private fun checkWin() {
        var un = 0
        for (r in 0 until rows) for (c in 0 until cols) if (!revealed[r][c]) un++
        if (un == mines) gameWon = true
    }

    override fun update(dt: Float, ctx: GameContext) {
        if (!firstClick && !gameOver && !gameWon) seconds += dt
        if (gameWon) {
            winAnim += dt
            for (p in confetti) {
                p.y += p.vy * dt
                if (p.y > H + 10f) { p.y = -10f; p.x = Random.nextFloat() * W }
            }
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(backgroundColor, topLeft = Offset.Zero, size = Size(W, H))

        // Recompute layout for current board size.
        val availH = H - headerH - footerH - 16f
        val availW = W - 32f
        cell = min(availH / rows, availW / cols).coerceAtMost(32f).coerceAtLeast(14f)
        boardW = cols * cell
        boardH = rows * cell
        boardX = (W - boardW) / 2f
        boardY = headerH + 8f

        // Header.
        drawRect(Color(0xFF808080), topLeft = Offset(boardX, 8f), size = Size(boardW, headerH - 12f))
        drawRect(Color(0xFFC0C0C0), topLeft = Offset(boardX + 3f, 11f), size = Size(boardW - 6f, headerH - 18f))

        val flagsLeft = mines - countFlags()
        // Mine counter
        drawRect(Color.Black, topLeft = Offset(boardX + 10f, 15f), size = Size(60f, 26f))
        drawDosText(flagsLeft.toString().padStart(3, '0'), x = boardX + 40f, y = 36f, color = Color(0xFFFF0000), size = 22f, bold = true, align = TextAlign.CENTER)
        // Smiley
        val face = when { gameOver -> "X_X"; gameWon -> "B-)"; else -> ":-)" }
        val faceX = boardX + boardW / 2f - 22f
        val faceY = 15f
        drawRect(Color(0xFFC0C0C0), topLeft = Offset(faceX, faceY), size = Size(44f, 26f))
        drawRect(Color.White, topLeft = Offset(faceX, faceY), size = Size(44f, 2f))
        drawRect(Color.White, topLeft = Offset(faceX, faceY), size = Size(2f, 26f))
        drawRect(Color(0xFF808080), topLeft = Offset(faceX, faceY + 24f), size = Size(44f, 2f))
        drawRect(Color(0xFF808080), topLeft = Offset(faceX + 42f, faceY), size = Size(2f, 26f))
        drawDosText(face, x = faceX + 22f, y = faceY + 18f, color = Color.Black, size = 14f, bold = true, align = TextAlign.CENTER)
        // Timer
        drawRect(Color.Black, topLeft = Offset(boardX + boardW - 70f, 15f), size = Size(60f, 26f))
        drawDosText(seconds.toInt().toString().padStart(3, '0'), x = boardX + boardW - 40f, y = 36f, color = Color(0xFFFF0000), size = 22f, bold = true, align = TextAlign.CENTER)

        // Board cells.
        for (r in 0 until rows) for (c in 0 until cols) {
            val cx = boardX + c * cell; val cy = boardY + r * cell
            if (revealed[r][c]) {
                val isHit = gameOver && board[r][c] == -1 && !flagged[r][c]
                val bg = if (isHit) Color(0xFFFF0000) else Color(0xFFD0D0D0)
                drawRect(bg, topLeft = Offset(cx, cy), size = Size(cell, cell))
                drawRect(Color(0xFF808080), topLeft = Offset(cx, cy), size = Size(cell, 1f))
                drawRect(Color(0xFF808080), topLeft = Offset(cx, cy), size = Size(1f, cell))
                if (board[r][c] == -1) {
                    drawDosText("*", x = cx + cell / 2f, y = cy + cell * 0.75f, color = Color.Black, size = cell * 0.8f, bold = true, align = TextAlign.CENTER)
                } else if (board[r][c] > 0) {
                    drawDosText(
                        board[r][c].toString(),
                        x = cx + cell / 2f, y = cy + cell * 0.75f,
                        color = numColors[board[r][c]], size = cell * 0.6f, bold = true, align = TextAlign.CENTER,
                    )
                }
            } else {
                drawRect(Color(0xFFC0C0C0), topLeft = Offset(cx, cy), size = Size(cell, cell))
                drawRect(Color.White, topLeft = Offset(cx, cy), size = Size(cell, 2f))
                drawRect(Color.White, topLeft = Offset(cx, cy), size = Size(2f, cell))
                drawRect(Color(0xFF808080), topLeft = Offset(cx, cy + cell - 2f), size = Size(cell, 2f))
                drawRect(Color(0xFF808080), topLeft = Offset(cx + cell - 2f, cy), size = Size(2f, cell))
                if (flagged[r][c]) {
                    drawDosText("F", x = cx + cell / 2f, y = cy + cell * 0.75f, color = Color(0xFFFF0000), size = cell * 0.6f, bold = true, align = TextAlign.CENTER)
                }
            }
        }

        // Difficulty buttons in footer.
        val btnY = H - footerH + 6f
        val labels = Difficulty.values()
        val btnW = (W * 0.6f) / labels.size
        val btnH = footerH - 12f
        val baseX = (W - btnW * labels.size) / 2f
        for ((i, d) in labels.withIndex()) {
            val bx = baseX + i * btnW
            val pressed = d == diff
            val bg = if (pressed) Color(0xFFA0A0A0) else Color(0xFFC0C0C0)
            drawRect(bg, topLeft = Offset(bx, btnY), size = Size(btnW - 4f, btnH))
            val topLeftColor = if (pressed) Color(0xFF808080) else Color.White
            val bottomRightColor = if (pressed) Color.White else Color(0xFF808080)
            drawRect(topLeftColor, topLeft = Offset(bx, btnY), size = Size(btnW - 4f, 2f))
            drawRect(topLeftColor, topLeft = Offset(bx, btnY), size = Size(2f, btnH))
            drawRect(bottomRightColor, topLeft = Offset(bx, btnY + btnH - 2f), size = Size(btnW - 4f, 2f))
            drawRect(bottomRightColor, topLeft = Offset(bx + btnW - 6f, btnY), size = Size(2f, btnH))
            drawDosText(ctx.tr(d.label, d.labelRu), x = bx + (btnW - 4f) / 2f, y = btnY + btnH / 2f + 5f, color = Color.Black, size = 12f, bold = true, align = TextAlign.CENTER)
        }

        if (gameWon) drawWinOverlay(this, ctx)
    }

    // ── Win celebration overlay ──────────────────────────────────────────────
    private val winPanelW = 380f
    private val winPanelH = 210f
    private val winBtnW = 160f
    private val winBtnH = 36f
    private val winBtnGap = 16f
    private fun winPanelX() = (W - winPanelW) / 2f
    private fun winPanelY() = (H - winPanelH) / 2f
    private fun winBtnRow() = winPanelY() + winPanelH - winBtnH - 16f
    private fun winBtnLeftX() = W / 2f - (winBtnW * 2 + winBtnGap) / 2f

    private fun drawWinOverlay(scope: DrawScope, ctx: GameContext) = with(scope) {
        for (p in confetti) drawRect(p.color, topLeft = Offset(p.x, p.y), size = Size(p.w, p.w))
        drawRect(Color(0x99000000), topLeft = Offset.Zero, size = Size(W, H))
        val px = winPanelX(); val py = winPanelY()
        raised(this, px, py, winPanelW, winPanelH)
        val pulse = 1f + 0.06f * sin(winAnim * 6f).toFloat()
        drawDosText(ctx.tr("YOU WIN!", "ПОБЕДА!"), x = W / 2f, y = py + 50f, color = Color(0xFF008000), size = 34f * pulse, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("CONGRATULATIONS!", "ПОЗДРАВЛЯЮ!"), x = W / 2f, y = py + 82f, color = Color.Black, size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("${diff.label}  ·  ${seconds.toInt()}s  ·  $mines mines", "${diff.labelRu}  ·  ${seconds.toInt()}с  ·  мин: $mines"), x = W / 2f, y = py + 106f, color = Color(0xFF000080), size = 12f, bold = true, align = TextAlign.CENTER)
        val next = nextDiff()
        val byB = winBtnRow(); val bx0 = winBtnLeftX()
        raised(this, bx0, byB, winBtnW, winBtnH)
        drawDosText(if (next != null) ctx.tr("NEXT: ${next.label} ▶", "ДАЛЬШЕ: ${next.labelRu} ▶") else ctx.tr("MASTERED!", "ОСВОЕНО ВСЁ!"), x = bx0 + winBtnW / 2f, y = byB + winBtnH / 2f + 5f, color = if (next != null) Color(0xFF000080) else Color(0xFF606060), size = 12f, bold = true, align = TextAlign.CENTER)
        val bx1 = bx0 + winBtnW + winBtnGap
        raised(this, bx1, byB, winBtnW, winBtnH)
        drawDosText(ctx.tr("PLAY AGAIN", "ЕЩЁ РАЗ"), x = bx1 + winBtnW / 2f, y = byB + winBtnH / 2f + 5f, color = Color.Black, size = 12f, bold = true, align = TextAlign.CENTER)
    }

    private fun raised(scope: DrawScope, x: Float, y: Float, w: Float, h: Float) = with(scope) {
        drawRect(Color(0xFFC0C0C0), topLeft = Offset(x, y), size = Size(w, h))
        drawRect(Color.White, topLeft = Offset(x, y), size = Size(w, 2f))
        drawRect(Color.White, topLeft = Offset(x, y), size = Size(2f, h))
        drawRect(Color(0xFF808080), topLeft = Offset(x, y + h - 2f), size = Size(w, 2f))
        drawRect(Color(0xFF808080), topLeft = Offset(x + w - 2f, y), size = Size(2f, h))
    }

    private fun countFlags(): Int {
        var k = 0; for (r in 0 until rows) for (c in 0 until cols) if (flagged[r][c]) k++
        return k
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        pressOrigin = Offset(x, y)
        pressStart = nowMillis()
        // Win panel intercepts all taps while it's up.
        if (gameWon) {
            val byB = winBtnRow(); val bx0 = winBtnLeftX()
            if (y in byB..byB + winBtnH) {
                if (x in bx0..bx0 + winBtnW) { nextDiff()?.let { diff = it }; newGame(); ctx.sound.click(); return }
                val bx1 = bx0 + winBtnW + winBtnGap
                if (x in bx1..bx1 + winBtnW) { newGame(); ctx.sound.click(); return }
            }
            return
        }
        // Difficulty buttons?
        val btnY = H - footerH + 6f
        val labels = Difficulty.values()
        val btnW = (W * 0.6f) / labels.size
        val btnH = footerH - 12f
        val baseX = (W - btnW * labels.size) / 2f
        if (y in btnY..btnY + btnH) {
            for ((i, d) in labels.withIndex()) {
                val bx = baseX + i * btnW
                if (x in bx..bx + btnW - 4f) { diff = d; newGame(); ctx.sound.click(); return }
            }
        }
        // Smiley?
        val faceX = boardX + boardW / 2f - 22f
        if (x in faceX..faceX + 44f && y in 15f..41f) { newGame(); ctx.sound.click(); return }
        // Board cell?
        if (x < boardX || x >= boardX + boardW || y < boardY || y >= boardY + boardH) {
            pressR = -1; pressC = -1; return
        }
        pressR = ((y - boardY) / cell).toInt()
        pressC = ((x - boardX) / cell).toInt()
    }

    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (pressR < 0 || pressC < 0) return
        // Board may have shrunk (difficulty switch) since the press was recorded.
        if (pressR >= rows || pressC >= cols) { pressR = -1; pressC = -1; return }
        val dt = nowMillis() - pressStart
        if (gameOver || gameWon) { pressR = -1; pressC = -1; return }
        if (dt >= longPressMs) {
            flag(pressR, pressC, ctx)
        } else {
            click(pressR, pressC, ctx)
        }
        pressR = -1; pressC = -1
    }

    private fun click(r: Int, c: Int, ctx: GameContext) {
        if (flagged[r][c]) return
        if (firstClick) { firstClick = false; placeMines(r, c) }
        if (board[r][c] == -1) {
            revealed[r][c] = true; gameOver = true
            for (i in 0 until rows) for (j in 0 until cols) if (board[i][j] == -1) revealed[i][j] = true
            ctx.sound.boom(); return
        }
        revealCell(r, c); ctx.sound.click()
        checkWin()
        if (gameWon) {
            ctx.sound.win(); spawnConfetti()
            // Persist best: harder difficulty always outranks, faster time breaks ties.
            val score = (diff.ordinal + 1) * 1000 - seconds.toInt().coerceAtMost(999)
            ctx.scores.set(id, score)
        }
    }

    private fun flag(r: Int, c: Int, ctx: GameContext) {
        if (revealed[r][c]) return
        flagged[r][c] = !flagged[r][c]
        ctx.sound.flag()
    }
}
