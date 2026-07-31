package com.pixelclassics.app.games.navalbattle

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawHandText
import com.pixelclassics.app.ui.theme.HandFont
import kotlin.random.Random

/** Classic 10x10 Battleship vs AI. Tap an enemy cell to fire. */
class NavalBattleGame : Game {
    override val id: String = "naval_battle"
    override val titleResId: Int = R.string.game_naval
    override val backgroundColor: Color = Color(0xFFE8DEB8)   // aged paper
    override val introText: String = NAVAL_INTRO_EN
    override fun introTextFor(lang: String): String = pickNavalIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PAPER_NOTEBOOK
    override val helpLines: List<String> = listOf(
        "Place your fleet first — drag/rotate ships",
        "Then take turns: click an enemy cell to fire",
        "Sink the whole fleet to win.  Russian rules: 1×4, 2×3, 3×2, 4×1.",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Сначала расставь флот — ставь и разворачивай корабли",
        "Дальше по очереди: кликни клетку противника — выстрел",
        "Потопи весь флот.  Советские правила: 1×4, 2×3, 3×2, 4×1.",
    ) else helpLines

    private val grid = 10
    private val cell = 26f
    private val gridSize = grid * cell
    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    // Classic Russian Battleship fleet: 1×4-deck, 2×3-deck, 3×2-deck, 4×1-deck = 10 ships, 20 cells.
    private val shipLens = intArrayOf(4, 3, 3, 2, 2, 2, 1, 1, 1, 1)
    private val emptyCell = 0; private val shipCell = 1; private val missCell = 2; private val hitCell = 3

    private var playerBoard = Array(grid) { IntArray(grid) }       // own ships
    private var enemyBoard = Array(grid) { IntArray(grid) }        // enemy ships (hidden)
    private var playerFire = Array(grid) { IntArray(grid) }        // shots on enemy: 0 not, 2 miss, 3 hit
    private var enemyFire = Array(grid) { IntArray(grid) }         // AI shots on player: 0 not, 2 miss, 3 hit
    private var enemyAlive = 0; private var playerAlive = 0
    private var playerTurn = true
    private var gameOver = false; private var win = false
    private var msg = "YOUR TURN"
    private var lastCtx: GameContext? = null
    private fun t(en: String, ru: String) = lastCtx?.tr(en, ru) ?: en
    private var aiThink = 0f
    private var best = 0
    private val aiTargets = ArrayDeque<Pair<Int, Int>>()

    private enum class Stage { TITLE, PLACING, PLAYING }
    private var stage = Stage.TITLE

    // Blank shots board for the placement screen — allocated once, not per frame.
    private val noShots = Array(grid) { IntArray(grid) }

    // Placement state.
    /** Second skin (Юджин, ревью 17.07): the exercise-book — squared paper,
     *  pink margin, ships outlined in wobbly ballpoint. Toggled any time. */
    private var notebook = false

    private var placeIdx = 0                          // index in shipLens being placed
    private var placeHoriz = true
    private val dockPitch = 26f                        // vertical spacing per ship row in the dock (fits 10 ships)
    private var time = 0f                              // for animations (wheel spin etc.)

    override fun init(ctx: GameContext) {
        lastCtx = ctx
        HandFont.ensure(ctx.assets)
        best = ctx.scores.get(id)
        startTitle()
    }

    private fun startTitle() {
        playerBoard = Array(grid) { IntArray(grid) }
        enemyBoard = Array(grid) { IntArray(grid) }
        playerFire = Array(grid) { IntArray(grid) }
        enemyFire = Array(grid) { IntArray(grid) }
        placeIdx = 0; placeHoriz = true
        gameOver = false; win = false
        stage = Stage.TITLE
        msg = t("PLACE FLEET", "РАССТАВЬ ФЛОТ")
    }

    private fun startBattle() {
        // If player hasn't finished placing — auto-fill the rest.
        if (placeIdx < shipLens.size) autoPlaceRemaining()
        placeShips(enemyBoard)
        enemyAlive = shipLens.sum(); playerAlive = shipLens.sum()
        playerTurn = true; gameOver = false; win = false; msg = t("YOUR TURN", "ТВОЙ ХОД")
        aiThink = 0f; aiTargets.clear()
        stage = Stage.PLAYING
    }

    private fun autoPlaceRemaining() {
        // Keep the player's manually placed ships if the remainder fits around
        // them; a pathological manual layout can make that impossible, so after
        // bounded retries fall back to re-rolling the whole fleet. Never loops
        // forever (the empty-board path always converges).
        val manual = Array(grid) { r -> IntArray(grid) { c -> playerBoard[r][c] } }
        repeat(50) {
            var ok = true
            for (i in placeIdx until shipLens.size) {
                if (!placeOneShip(playerBoard, shipLens[i])) { ok = false; break }
            }
            if (ok) { placeIdx = shipLens.size; return }
            for (r in 0 until grid) for (c in 0 until grid) playerBoard[r][c] = manual[r][c]
        }
        for (r in 0 until grid) for (c in 0 until grid) playerBoard[r][c] = emptyCell
        placeShips(playerBoard)
        placeIdx = shipLens.size
    }

    private fun placeOneShip(b: Array<IntArray>, len: Int): Boolean {
        repeat(1000) {
            val horiz = Random.nextBoolean()
            val r = Random.nextInt(if (horiz) grid else grid - len + 1)
            val c = Random.nextInt(if (horiz) grid - len + 1 else grid)
            if (canPlaceAt(b, r, c, len, horiz)) {
                for (i in 0 until len) {
                    if (horiz) b[r][c + i] = shipCell else b[r + i][c] = shipCell
                }
                return true
            }
        }
        return false
    }

    private fun canPlaceAt(b: Array<IntArray>, r: Int, c: Int, len: Int, horiz: Boolean): Boolean {
        for (i in 0 until len) {
            val rr = if (horiz) r else r + i
            val cc = if (horiz) c + i else c
            if (rr !in 0 until grid || cc !in 0 until grid) return false
            for (dr in -1..1) for (dc in -1..1) {
                val nr = rr + dr; val nc = cc + dc
                if (nr in 0 until grid && nc in 0 until grid && b[nr][nc] == shipCell) return false
            }
        }
        return true
    }

    private fun placeShips(b: Array<IntArray>) {
        // Retry from scratch if a random layout paints itself into a corner;
        // after bounded retries fall back to a fixed legal layout so this can
        // never spin forever.
        repeat(100) {
            if (shipLens.all { placeOneShip(b, it) }) return
            for (r in 0 until grid) for (c in 0 until grid) b[r][c] = emptyCell
        }
        for (r in 0 until grid) for (c in 0 until grid) b[r][c] = emptyCell
        // Rows 0/2/4/6, ships spaced ≥1 cell apart: always legal on 10×10.
        val fixed = listOf(
            Triple(0, 0, 4), Triple(2, 0, 3), Triple(2, 4, 3),
            Triple(4, 0, 2), Triple(4, 3, 2), Triple(4, 6, 2),
            Triple(6, 0, 1), Triple(6, 2, 1), Triple(6, 4, 1), Triple(6, 6, 1),
        )
        for ((r, c, len) in fixed) for (i in 0 until len) b[r][c + i] = shipCell
    }

    override fun update(dt: Float, ctx: GameContext) {
        lastCtx = ctx
        time += dt
        if (stage != Stage.PLAYING || gameOver || playerTurn) return
        aiThink += dt
        if (aiThink < 0.6f) return
        aiThink = 0f
        aiFire(ctx)
    }

    private fun aiFire(ctx: GameContext) {
        var target: Pair<Int, Int>? = null
        while (target == null) {
            val t = if (aiTargets.isNotEmpty()) aiTargets.removeFirst()
            else Random.nextInt(grid) to Random.nextInt(grid)
            if (enemyFire[t.first][t.second] == 0) target = t
        }
        val (r, c) = target
        if (playerBoard[r][c] == shipCell) {
            enemyFire[r][c] = hitCell; playerAlive--; ctx.sound.boom()
            // Add neighbours to target queue.
            for ((dr, dc) in listOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)) {
                val nr = r + dr; val nc = c + dc
                if (nr in 0 until grid && nc in 0 until grid && enemyFire[nr][nc] == 0) aiTargets += nr to nc
            }
            msg = t("ENEMY HIT! AGAIN...", "ПО ТЕБЕ ПОПАЛИ! И СНОВА...")
            if (playerAlive == 0) { gameOver = true; ctx.sound.gameOver(); msg = t("ENEMY WON", "ПРОТИВНИК ПОБЕДИЛ"); return }
        } else {
            enemyFire[r][c] = missCell; playerTurn = true; msg = t("YOUR TURN", "ТВОЙ ХОД"); ctx.sound.wallHit()
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Aged-paper background — common to all stages.
        drawRect(backgroundColor, topLeft = Offset.Zero, size = Size(W, H))
        for (i in 0 until 200) {
            val sx = ((i * 173 + 37) % W.toInt()).toFloat()
            val sy = ((i * 131 + 17) % H.toInt()).toFloat()
            drawRect(Color(0x14000000), topLeft = Offset(sx, sy), size = Size(1f, 1f))
        }
        drawRect(Color(0x25000000), topLeft = Offset(0f, 0f), size = Size(W, 3f))
        drawRect(Color(0x25000000), topLeft = Offset(0f, H - 3f), size = Size(W, 3f))

        if (notebook) drawNotebookPaper(this)

        when (stage) {
            Stage.TITLE -> drawTitle(this, ctx)
            Stage.PLACING -> drawPlacement(this, ctx)
            Stage.PLAYING -> drawPlaying(this, ctx)
        }

        // Skin toggle — always visible, top-right corner.
        drawPaperButton(this, W - 156f, 8f, 148f, 24f,
            if (notebook) ctx.tr("SKIN: MAP", "СКИН: КАРТА") else ctx.tr("SKIN: TETRAD", "СКИН: ТЕТРАДЬ"))
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
        for (i in pts.indices) {
            val a = pts[i]; val b = pts[(i + 1) % pts.size]
            drawLine(color, a, b, strokeWidth = 1.7f)
        }
    }

    private fun drawPlaying(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Enemy board (left).
        val ebx = (W / 2f - gridSize) / 2f
        val eby = 60f
        drawHandText(ctx.tr("ENEMY WATERS", "ВОДЫ ПРОТИВНИКА"), x = ebx + gridSize / 2f, y = eby - 14f, color = Color(0xFF6A1B1B), size = 16f, align = TextAlign.CENTER)
        drawBoard(this, ebx, eby, enemyBoard, playerFire, hideShips = true)

        // Player board (right).
        val pbx = ebx + W / 2f
        drawHandText(ctx.tr("YOUR FLEET", "ТВОЙ ФЛОТ"), x = pbx + gridSize / 2f, y = eby - 14f, color = Color(0xFF1B5A1B), size = 16f, align = TextAlign.CENTER)
        drawBoard(this, pbx, eby, playerBoard, enemyFire, hideShips = false)

        // HUD.
        drawHandText(ctx.tr("DECKS  $enemyAlive", "ПАЛУБ  $enemyAlive"), x = ebx + gridSize / 2f, y = eby + gridSize + 24f, color = Color(0xFF6A1B1B), size = 13f, align = TextAlign.CENTER)
        drawHandText(ctx.tr("DECKS  $playerAlive", "ПАЛУБ  $playerAlive"), x = pbx + gridSize / 2f, y = eby + gridSize + 24f, color = Color(0xFF1B5A1B), size = 13f, align = TextAlign.CENTER)
        drawHandText(msg, x = W / 2f, y = H - 26f, color = if (playerTurn) Color(0xFF0F2A6A) else Color(0xFF6A1B1B), size = 18f, align = TextAlign.CENTER)

        if (gameOver) {
            drawRect(Color(0xDDE8DEB8), topLeft = Offset.Zero, size = Size(W, H))
            val title = if (win) ctx.tr("VICTORY!", "ПОБЕДА!") else ctx.tr("DEFEATED", "ПОРАЖЕНИЕ")
            val tcol = if (win) Color(0xFF1B5A1B) else Color(0xFF6A1B1B)
            drawHandText(title, x = W / 2f, y = H / 2f - 10f, color = tcol, size = 40f, align = TextAlign.CENTER)
            drawHandText(ctx.tr("CLICK TO PLAY AGAIN", "КЛИК — ЕЩЁ ПАРТИЯ"), x = W / 2f, y = H / 2f + 40f, color = Color(0xFF1A1A1A), size = 14f, align = TextAlign.CENTER)
        }
    }

    private fun drawTitle(scope: DrawScope, ctx: GameContext) = with(scope) {
        val cx = W / 2f
        val cy = H * 0.34f
        // Ship's wheel.
        val r = 95f
        val ringW = 8f
        // Outer wood rim (8 spokes + handles).
        for (k in 0 until 8) {
            val ang = k * (PI / 4) + (time * 0.25)
            val sx = (cx + cos(ang) * (r + 16f)).toFloat()
            val sy = (cy + sin(ang) * (r + 16f)).toFloat()
            // Handle ball at the end of each spoke.
            drawCircle(Color(0xFF4A2818), radius = 7f, center = Offset(sx, sy))
            drawCircle(Color(0xFF6A3820), radius = 6f, center = Offset(sx, sy))
            drawCircle(Color(0xFF8A582A), radius = 3f, center = Offset(sx - 1f, sy - 1f))
            // Spoke.
            val ex = (cx + cos(ang) * (r - 4f)).toFloat()
            val ey = (cy + sin(ang) * (r - 4f)).toFloat()
            drawLine(Color(0xFF4A2818), Offset(cx, cy), Offset(ex, ey), strokeWidth = 5f)
            drawLine(Color(0xFF8A582A), Offset(cx, cy), Offset(ex, ey), strokeWidth = 2f)
        }
        // Wheel rim — dark outer + lighter inner.
        drawCircle(Color(0xFF2A1408), radius = r + 3f, center = Offset(cx, cy), style = Stroke(width = ringW + 2f))
        drawCircle(Color(0xFF6A3820), radius = r, center = Offset(cx, cy), style = Stroke(width = ringW))
        drawCircle(Color(0xFF8A582A), radius = r - 2f, center = Offset(cx, cy), style = Stroke(width = 2f))
        // Hub.
        drawCircle(Color(0xFF2A1408), radius = 14f, center = Offset(cx, cy))
        drawCircle(Color(0xFFFCD060), radius = 10f, center = Offset(cx, cy))
        drawCircle(Color(0xFF8A582A), radius = 4f, center = Offset(cx, cy))

        drawHandText("NAVAL BATTLE", x = cx, y = H * 0.58f, color = Color(0xFF0F2A6A), size = 38f, align = TextAlign.CENTER)
        drawHandText(ctx.tr("SINK THE ENEMY FLEET", "ПОТОПИ ФЛОТ ПРОТИВНИКА"), x = cx, y = H * 0.65f, color = Color(0xFF6A1B1B), size = 16f, align = TextAlign.CENTER)

        // Buttons.
        drawPaperButton(this, cx - 130f, H * 0.78f, 260f, 36f, "▶ PLACE FLEET")
        drawPaperButton(this, cx - 90f, H * 0.88f, 180f, 28f, "🎲 AUTO")

        if (best > 0) drawHandText("BEST  $best", x = cx, y = H - 18f, color = Color(0xFF1A1A1A), size = 11f, align = TextAlign.CENTER)
    }

    private fun drawPaperButton(scope: DrawScope, x: Float, y: Float, w: Float, h: Float, text: String) = with(scope) {
        drawRect(Color(0xFFF5EBC4), topLeft = Offset(x, y), size = Size(w, h))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(x, y), size = Size(w, 1f))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(x, y + h - 1f), size = Size(w, 1f))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(x, y), size = Size(1f, h))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(x + w - 1f, y), size = Size(1f, h))
        drawHandText(text, x = x + w / 2f, y = y + h / 2f + 5f, color = Color(0xFF0F2A6A), size = 14f, align = TextAlign.CENTER)
    }

    private fun drawPlacement(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Board on the left half.
        val bx = 50f
        val by = 70f
        drawBoardSimple(this, bx, by, playerBoard, noShots, hideShips = false)

        drawHandText(ctx.tr("PLACE FLEET", "РАССТАВЬ ФЛОТ"), x = bx + gridSize / 2f, y = by - 16f, color = Color(0xFF1B5A1B), size = 16f, align = TextAlign.CENTER)

        // Ships dock on the right.
        val dx = W - 240f
        val dy = 70f
        drawHandText(ctx.tr("SHIPS", "КОРАБЛИ"), x = dx + 110f, y = dy - 16f, color = Color(0xFF6A1B1B), size = 16f, align = TextAlign.CENTER)
        for ((i, len) in shipLens.withIndex()) {
            val placed = i < placeIdx
            val sy = dy + i * dockPitch
            val sw = len * cell
            val color = when {
                placed -> Color(0xFF8B8B8B)
                i == placeIdx -> Color(0xFF4A88CC)  // highlighted
                else -> Color(0xFF606870)
            }
            drawRect(color, topLeft = Offset(dx, sy), size = Size(sw, 22f))
            drawRect(Color(0xFF1A1A1A), topLeft = Offset(dx, sy), size = Size(sw, 1f))
            // Rivets.
            for (rv in 0 until len) drawRect(Color.White.copy(alpha = 0.5f), topLeft = Offset(dx + rv * cell + 4f, sy + 4f), size = Size(2f, 2f))
            // Length label.
            drawHandText("$len", x = dx + sw + 12f, y = sy + 16f, color = Color(0xFF1A1A1A), size = 13f)
            if (placed) drawHandText("✓", x = dx - 12f, y = sy + 16f, color = Color(0xFF1B5A1B), size = 14f, align = TextAlign.CENTER)
            else if (i == placeIdx) drawHandText("→", x = dx - 16f, y = sy + 16f, color = Color(0xFF4A88CC), size = 18f, align = TextAlign.CENTER)
        }

        // Control buttons under the dock.
        val btnY = dy + shipLens.size * dockPitch + 14f
        drawPaperButton(this, dx, btnY, 220f, 28f, if (placeHoriz) ctx.tr("↔ HORIZONTAL", "↔ ГОРИЗОНТАЛЬНО") else ctx.tr("↕ VERTICAL", "↕ ВЕРТИКАЛЬНО"))
        drawPaperButton(this, dx, btnY + 36f, 220f, 28f, "🎲 AUTO REST")
        if (placeIdx >= shipLens.size) drawPaperButton(this, dx, btnY + 72f, 220f, 32f, "▶ BATTLE")

        drawHandText(ctx.tr("Click a cell — the ship's bow goes there.", "Кликни клетку — туда встанет нос корабля."), x = W / 2f, y = H - 38f, color = Color(0xFF606060), size = 11f, align = TextAlign.CENTER)
        drawHandText(ctx.tr("\"HORIZONTAL / VERTICAL\" flips the orientation.", "«ГОРИЗОНТАЛЬНО / ВЕРТИКАЛЬНО» меняет разворот."), x = W / 2f, y = H - 20f, color = Color(0xFF606060), size = 11f, align = TextAlign.CENTER)
    }

    private fun drawBoardSimple(scope: DrawScope, bx: Float, by: Float, ships: Array<IntArray>, shots: Array<IntArray>, hideShips: Boolean) = with(scope) {
        // Reuse existing drawBoard rendering (no shots phase needed for placement,
        // shots is just an empty array of the right shape).
        drawBoard(this, bx, by, ships, shots, hideShips)
    }

    private fun drawBoard(scope: DrawScope, bx: Float, by: Float, ships: Array<IntArray>, shots: Array<IntArray>, hideShips: Boolean) = with(scope) {
        // Outer frame with double-rule.
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(bx - 3f, by - 3f), size = Size(gridSize + 6f, gridSize + 6f))
        drawRect(Color(0xFFF5EBC4), topLeft = Offset(bx - 2f, by - 2f), size = Size(gridSize + 4f, gridSize + 4f))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(bx, by), size = Size(gridSize, 1f))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(bx, by + gridSize - 1f), size = Size(gridSize, 1f))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(bx, by), size = Size(1f, gridSize))
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(bx + gridSize - 1f, by), size = Size(1f, gridSize))

        // Grid lines — pencil grey, broken so it looks drawn.
        for (i in 1 until grid) {
            val gy = by + i * cell
            var xPos = bx
            while (xPos < bx + gridSize) {
                drawRect(Color(0xFF606060), topLeft = Offset(xPos, gy), size = Size(8f, 1f))
                xPos += 12f
            }
            val gx = bx + i * cell
            var yPos = by
            while (yPos < by + gridSize) {
                drawRect(Color(0xFF606060), topLeft = Offset(gx, yPos), size = Size(1f, 8f))
                yPos += 12f
            }
        }

        // Column / row labels.
        for (i in 0 until grid) {
            val ch = ('A' + i).toString()
            drawHandText(ch, x = bx + i * cell + cell / 2f, y = by - 6f, color = Color(0xFF1A1A1A), size = 10f, align = TextAlign.CENTER)
            drawHandText((i + 1).toString(), x = bx - 8f, y = by + i * cell + cell / 2f + 4f, color = Color(0xFF1A1A1A), size = 10f, align = TextAlign.CENTER)
        }

        // Cell contents.
        for (r in 0 until grid) for (c in 0 until grid) {
            val x = bx + c * cell; val y = by + r * cell
            if (!hideShips && ships[r][c] == shipCell && shots[r][c] != hitCell) {
                if (notebook) {
                    // Exercise-book skin: the ship cell is outlined in wobbly
                    // ballpoint and hatched — a seventh-grader's back desk.
                    val ink = Color(0xFF2B4590)
                    pencilRect(x + 2f, y + 3f, cell - 4f, cell - 6f, seed = r * 31 + c, color = ink)
                    drawLine(ink.copy(alpha = 0.55f), Offset(x + 4f, y + cell - 6f), Offset(x + cell - 6f, y + 5f), strokeWidth = 1.2f)
                } else {
                    // Ship hull — pencil-style filled grey block with rivet dots.
                    drawRect(Color(0xFF606870), topLeft = Offset(x + 3f, y + 4f), size = Size(cell - 6f, cell - 8f))
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(x + 3f, y + 4f), size = Size(cell - 6f, 1f))
                    drawRect(Color(0xFF888888), topLeft = Offset(x + 4f, y + 5f), size = Size(2f, 1f))
                    drawRect(Color(0xFF888888), topLeft = Offset(x + cell - 7f, y + 5f), size = Size(2f, 1f))
                }
            }
            when (shots[r][c]) {
                missCell -> {
                    // Hand-drawn dot for miss.
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(x + cell / 2f - 2f, y + cell / 2f - 2f), size = Size(4f, 4f))
                    drawRect(Color(0xFF606060), topLeft = Offset(x + cell / 2f - 3f, y + cell / 2f - 1f), size = Size(2f, 1f))
                }
                hitCell -> {
                    // Red X over the ship — two diagonal pencil strokes.
                    val cx = x + cell / 2f; val cy = y + cell / 2f
                    drawRect(Color(0xFFAA1010), topLeft = Offset(x + 3f, y + 4f), size = Size(cell - 6f, cell - 8f))
                    drawRect(Color(0xFFFCE040), topLeft = Offset(x + 4f, y + 5f), size = Size(cell - 8f, 2f))
                    drawHandText("X", x = cx, y = cy + 6f, color = Color(0xFF1A1A1A), size = cell * 0.7f, align = TextAlign.CENTER)
                }
            }
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        // Skin toggle, any stage.
        if (x >= W - 156f && x <= W - 8f && y >= 8f && y <= 32f) {
            notebook = !notebook; ctx.sound.click(); return
        }
        if (gameOver) { startTitle(); return }
        when (stage) {
            Stage.TITLE -> handleTitleTap(x, y, ctx)
            Stage.PLACING -> handlePlacementTap(x, y, ctx)
            Stage.PLAYING -> handleBattleTap(x, y, ctx)
        }
    }

    private fun handleTitleTap(x: Float, y: Float, ctx: GameContext) {
        val cx = W / 2f
        // Manual placement button.
        if (x in (cx - 130f)..(cx + 130f) && y in (H * 0.78f)..(H * 0.78f + 36f)) {
            stage = Stage.PLACING; placeIdx = 0; placeHoriz = true
            ctx.sound.menuSelect(); return
        }
        // Auto button.
        if (x in (cx - 90f)..(cx + 90f) && y in (H * 0.88f)..(H * 0.88f + 28f)) {
            startBattle(); ctx.sound.menuSelect()
        }
    }

    private fun handlePlacementTap(x: Float, y: Float, ctx: GameContext) {
        val bx = 50f; val by = 70f
        val dx = W - 240f; val dy = 70f
        val btnY = dy + shipLens.size * dockPitch + 14f

        // Rotate button.
        if (x in dx..dx + 220f && y in btnY..btnY + 28f) {
            placeHoriz = !placeHoriz; ctx.sound.click(); return
        }
        // Auto-place rest.
        if (x in dx..dx + 220f && y in (btnY + 36f)..(btnY + 64f)) {
            autoPlaceRemaining(); ctx.sound.menuSelect(); return
        }
        // To Battle.
        if (placeIdx >= shipLens.size && x in dx..dx + 220f && y in (btnY + 72f)..(btnY + 104f)) {
            startBattle(); ctx.sound.win(); return
        }
        // Tap on the board to place current ship.
        if (placeIdx < shipLens.size && x >= bx && x < bx + gridSize && y >= by && y < by + gridSize) {
            val c = ((x - bx) / cell).toInt()
            val r = ((y - by) / cell).toInt()
            val len = shipLens[placeIdx]
            if (canPlaceAt(playerBoard, r, c, len, placeHoriz)) {
                for (i in 0 until len) {
                    if (placeHoriz) playerBoard[r][c + i] = shipCell else playerBoard[r + i][c] = shipCell
                }
                placeIdx++
                ctx.sound.click()
            } else {
                ctx.sound.wallHit()
            }
        }
    }

    private fun handleBattleTap(x: Float, y: Float, ctx: GameContext) {
        if (!playerTurn) return
        val ebx = (W / 2f - gridSize) / 2f; val eby = 60f
        if (x < ebx || x >= ebx + gridSize || y < eby || y >= eby + gridSize) return
        val c = ((x - ebx) / cell).toInt(); val r = ((y - eby) / cell).toInt()
        if (playerFire[r][c] != 0) return
        if (enemyBoard[r][c] == shipCell) {
            playerFire[r][c] = hitCell; enemyAlive--; msg = t("HIT! GO AGAIN.", "ПОПАДАНИЕ! ХОДИ ЕЩЁ."); ctx.sound.boom()
            if (enemyAlive == 0) { gameOver = true; win = true; msg = t("VICTORY", "ПОБЕДА"); ctx.sound.win(); if (1000 > best) { best = 1000; ctx.scores.set(this.id, 1000) } }
        } else {
            playerFire[r][c] = missCell; msg = t("MISS — ENEMY TURN", "МИМО — ХОД ПРОТИВНИКА"); playerTurn = false; aiThink = 0f; ctx.sound.wallHit()
        }
    }
}
