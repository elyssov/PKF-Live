package com.pixelclassics.app.games.pushbox

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

/**
 * PUSHBOX — Sokoban-style block-pushing puzzle, 30 levels.
 *
 * Avoid the trademarked name "Sokoban" (Falcon Co.) — gameplay is
 * public-domain. 30 hand-tuned grids, ascii-encoded:
 *   #=wall  .=goal   $=box    *=box on goal
 *   @=player  +=player on goal  space=floor
 */
class PushboxGame : Game {
    override val id: String = "pushbox"
    override val titleResId: Int = R.string.game_pushbox
    override val backgroundColor: Color = Color(0xFF1A1810)
    override val controls: GameControls = GameControls.DPAD
    override val introText: String = PUSHBOX_INTRO_EN
    override fun introTextFor(lang: String): String = pickPushboxIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PAPER_NOTEBOOK
    override val helpLines: List<String> = helpLinesFor("en")
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — толкай ящики на цели (.)",
        "Только толкать — тянуть нельзя.  Загнал в угол = рестарт.",
        "30 паззлов ручной выделки",
    ) else listOf(
        "D-pad — push boxes onto goals (.)",
        "Only push — no pull.  Cornered = restart.",
        "30 hand-tuned puzzles",
    )

    private val levels: List<List<String>> = listOf(
        // 1 — single push.
        listOf(
            "########",
            "#      #",
            "# .$@  #",
            "#      #",
            "########",
        ),
        // 2 — push two.
        listOf(
            "##########",
            "#        #",
            "# . $ $. #",
            "#   @    #",
            "##########",
        ),
        // 3 — corner trap test.
        listOf(
            "#######",
            "#.    #",
            "# $ @ #",
            "#     #",
            "#    .#",
            "#######",
        ),
        // 4 — three boxes line.
        listOf(
            "#########",
            "#.  .  .#",
            "# $ $ $ #",
            "#   @   #",
            "#########",
        ),
        // 5 — narrow corridor.
        listOf(
            "########",
            "#@$ .  #",
            "########",
        ),
        // 6 — turn corner. (Goal moved into the walkable row: the old (1,6)
        // goal was unreachable — the box could never leave row 2.)
        listOf(
            "########",
            "#  ##  #",
            "# @$ .##",
            "########",
        ),
        // 7 — squeeze.
        listOf(
            "#########",
            "# . . . #",
            "# $ $ $ #",
            "#       #",
            "#   @   #",
            "#########",
        ),
        // 8 — zig.
        listOf(
            "##########",
            "#@       #",
            "# # ## # #",
            "#  $  $  #",
            "#  .  .  #",
            "##########",
        ),
        // 9 — symmetric. (Bottom boxes moved up a row: sitting against the
        // bottom wall they could never be pushed up to any goal.)
        listOf(
            "#########",
            "#.     .#",
            "#  $@$  #",
            "#       #",
            "#. $ $ .#",
            "#       #",
            "#########",
        ),
        // 10 — H shape.
        listOf(
            "##########",
            "#.    .  #",
            "# $$ $@$ #",
            "#        #",
            "#.    .  #",
            "##########",
        ),
        // 11..20 — increasing difficulty (kept compact for brevity).
        listOf(
            "########",
            "#   .  #",
            "# # $# #",
            "# . $  #",
            "# $.@  #",
            "########",
        ),
        listOf(
            "#########",
            "#  ##   #",
            "# .  $  #",
            "# $.$.@ #",
            "#  .    #",
            "#########",
        ),
        // (Row-3 goals moved next to the bottom boxes: boxes on the bottom
        // wall row can only move horizontally, so the old goals were dead.)
        listOf(
            "##########",
            "# .   .  #",
            "# $ # $  #",
            "#   #   @#",
            "# $.  $. #",
            "##########",
        ),
        listOf(
            "########",
            "#  .   #",
            "# .$.  #",
            "#  $@$ #",
            "#  .   #",
            "########",
        ),
        // (The old second goal sat OUTSIDE the playfield wall, sealed on all
        // sides — no box could ever reach it.)
        listOf(
            "#########",
            "#@$ .   #",
            "## ######",
            " # $   .#",
            " ###    #",
            "   ######",
        ),
        listOf(
            "##########",
            "#   .  . #",
            "# # $ # ##",
            "#  $@$  #",
            "## # $ # #",
            "#  ..    #",
            "##########",
        ),
        // (Right-hand goals moved under the right boxes: pushed up/down at
        // col 6, a box could never then be shoved sideways onto col 5.)
        listOf(
            "########",
            "# .   .#",
            "# $## $#",
            "#  @   #",
            "# $## $#",
            "# .   .#",
            "########",
        ),
        listOf(
            "##########",
            "#  ##    #",
            "# .$.$@  #",
            "#  $  $  #",
            "#   .  . #",
            "##########",
        ),
        listOf(
            "#########",
            "#       #",
            "# .$.$. #",
            "# $@$ $ #",
            "#  . .  #",
            "#########",
        ),
        listOf(
            "##########",
            "# . . .. #",
            "# $$ $$  #",
            "#   @    #",
            "# $$ $$  #",
            "# . . .. #",
            "##########",
        ),
        // 21..30 — tougher.
        listOf(
            "########",
            "#      #",
            "# $$.. #",
            "# $.@$ #",
            "# .$$.##",
            "#  .   #",
            "########",
        ),
        // (Had 6 goals for 5 boxes, and the left pocket was sealed off from
        // the player — passage opened at (2,2), surplus goal removed.)
        listOf(
            "##########",
            "#@       #",
            "## $#$## #",
            "#  $.   .#",
            "# $..$  .#",
            "##########",
        ),
        listOf(
            "##########",
            "# .....  #",
            "# $$$$$  #",
            "#  @     #",
            "##########",
        ),
        // (The adjacent bottom-wall box pair was frozen; one box and the
        // matching extra goal removed.)
        listOf(
            "########",
            "#.   . #",
            "# $ $  #",
            "#@ $ $.#",
            "# $ . .#",
            "########",
        ),
        listOf(
            "##########",
            "# .  .  .#",
            "# $$.$$  #",
            "#  $@$   #",
            "# .   .  #",
            "##########",
        ),
        listOf(
            "##########",
            "# . . . .#",
            "# $ $ $ $#",
            "#        #",
            "# @      #",
            "##########",
        ),
        listOf(
            "##########",
            "#.@.    .#",
            "#$ $$ $$ #",
            "#  . .   #",
            "# $   $  #",
            "# .   .  #",
            "##########",
        ),
        listOf(
            "##########",
            "#        #",
            "#  .$.   #",
            "#  $.$@  #",
            "#  .$.   #",
            "#        #",
            "##########",
        ),
        listOf(
            "##########",
            "# .  .  .#",
            "# $  $  $#",
            "#  .  .  #",
            "#  $@ $  #",
            "# .  .  .#",
            "##########",
        ),
        listOf(
            "###########",
            "# ....... #",
            "# $$$$$$$ #",
            "#    @    #",
            "###########",
        ),
    )

    private val rows get() = grid.size
    private val cols get() = grid.maxOfOrNull { it.size } ?: 0
    private var grid: Array<CharArray> = Array(0) { CharArray(0) }
    private var px = 0
    private var py = 0
    private var moves = 0
    private var pushes = 0
    private var levelIdx = 0
    private var score = 0
    private var best = 0
    private var solved = false
    private val undoStack = ArrayDeque<UndoFrame>()
    private data class UndoFrame(val grid: Array<CharArray>, val px: Int, val py: Int, val moves: Int, val pushes: Int)

    private val tile = 36f
    private val virtualW = 720f
    private val virtualH = 540f
    override val virtualWidth: Float get() = virtualW
    override val virtualHeight: Float get() = virtualH

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        levelIdx = 0
        loadLevel()
    }

    private fun loadLevel() {
        val src = levels[levelIdx]
        grid = Array(src.size) { r -> CharArray(src.maxOf { it.length }) { c -> src[r].getOrElse(c) { ' ' } } }
        // Find player.
        for (r in 0 until rows) for (c in 0 until cols) when (grid[r][c]) {
            '@' -> { px = c; py = r; grid[r][c] = ' ' }
            '+' -> { px = c; py = r; grid[r][c] = '.' }
        }
        moves = 0; pushes = 0; solved = false
        undoStack.clear()
    }

    private fun pushFrame() {
        undoStack.addLast(UndoFrame(
            grid = Array(rows) { grid[it].copyOf() },
            px = px, py = py, moves = moves, pushes = pushes,
        ))
        if (undoStack.size > 100) undoStack.removeFirst()
    }

    private fun undo() {
        val f = undoStack.removeLastOrNull() ?: return
        for (r in 0 until rows) for (c in 0 until cols) grid[r][c] = f.grid[r][c]
        px = f.px; py = f.py; moves = f.moves; pushes = f.pushes
        solved = checkSolved()
    }

    private fun tryMove(dx: Int, dy: Int, ctx: GameContext): Boolean {
        if (solved) return false
        val nx = px + dx; val ny = py + dy
        if (nx !in 0 until cols || ny !in 0 until rows) return false
        val target = grid[ny][nx]
        if (target == '#') return false
        if (target == '$' || target == '*') {
            val bx = nx + dx; val by = ny + dy
            if (bx !in 0 until cols || by !in 0 until rows) return false
            val behind = grid[by][bx]
            if (behind == '#' || behind == '$' || behind == '*') return false
            pushFrame()
            grid[by][bx] = if (behind == '.') '*' else '$'
            grid[ny][nx] = if (target == '*') '.' else ' '
            px = nx; py = ny; moves++; pushes++
            ctx.sound.click()
        } else {
            pushFrame()
            px = nx; py = ny; moves++
            ctx.sound.menuSelect()
        }
        if (checkSolved()) {
            solved = true
            score += 500 + (1000 / (moves + 1))
            if (score > best) { best = score; ctx.scores.set(id, score) }
            ctx.sound.win()
        }
        return true
    }

    private fun checkSolved(): Boolean {
        for (r in 0 until rows) for (c in 0 until cols) if (grid[r][c] == '$') return false
        // Also must have at least one goal-on-floor that's empty?
        // Actually if all $ → all on goals.
        return true
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (dx == 0 && dy == 0) return   // ignore D-pad release; no phantom push on a neutral pad
        tryMove(dx, dy, ctx)
    }

    override fun update(dt: Float, ctx: GameContext) {}

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(backgroundColor, topLeft = Offset.Zero, size = Size(virtualW, virtualH))

        // HUD
        drawDosText("LEVEL ${levelIdx + 1}/${levels.size}", x = 14f, y = 22f, color = Color.White, size = 14f, bold = true)
        drawDosText("MOVES $moves · PUSHES $pushes", x = virtualW / 2f, y = 22f, color = Color(0xFFCCCCCC), size = 12f, bold = true, align = TextAlign.CENTER)
        drawDosText("SCORE $score · BEST $best", x = virtualW - 14f, y = 22f, color = Color(0xFFFFD600), size = 12f, bold = true, align = TextAlign.RIGHT)

        // Board centred.
        val boardW = cols * tile
        val boardH = rows * tile
        val offX = (virtualW - boardW) / 2f
        val offY = 50f + (virtualH - 50f - boardH) / 2f
        for (r in 0 until rows) for (c in 0 until cols) {
            val x = offX + c * tile; val y = offY + r * tile
            val ch = grid[r][c]
            when (ch) {
                '#' -> {
                    drawRect(Color(0xFF5A4520), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFF8B6914), topLeft = Offset(x + 2f, y + 2f), size = Size(tile - 4f, tile - 4f))
                    drawRect(Color(0xFFAA8838), topLeft = Offset(x + 2f, y + 2f), size = Size(tile - 4f, 2f))
                    drawRect(Color(0xFF402B10), topLeft = Offset(x + 2f, y + tile - 4f), size = Size(tile - 4f, 2f))
                }
                ' ' -> drawRect(Color(0xFF332B18), topLeft = Offset(x, y), size = Size(tile, tile))
                '.' -> {
                    drawRect(Color(0xFF332B18), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawCircle(Color(0xFFFFAA00), radius = 4f, center = Offset(x + tile / 2f, y + tile / 2f))
                    drawCircle(Color(0xFFFFCC00), radius = 2f, center = Offset(x + tile / 2f, y + tile / 2f))
                }
                '$' -> {
                    drawRect(Color(0xFF332B18), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFF6A4A20), topLeft = Offset(x + 4f, y + 4f), size = Size(tile - 8f, tile - 8f))
                    drawRect(Color(0xFFAA7A38), topLeft = Offset(x + 6f, y + 6f), size = Size(tile - 12f, tile - 12f))
                    // X-frame.
                    drawRect(Color(0xFF6A4A20), topLeft = Offset(x + 6f, y + tile / 2f - 1f), size = Size(tile - 12f, 2f))
                    drawRect(Color(0xFF6A4A20), topLeft = Offset(x + tile / 2f - 1f, y + 6f), size = Size(2f, tile - 12f))
                }
                '*' -> {
                    drawRect(Color(0xFF332B18), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFF4A8A28), topLeft = Offset(x + 4f, y + 4f), size = Size(tile - 8f, tile - 8f))
                    drawRect(Color(0xFF78D068), topLeft = Offset(x + 6f, y + 6f), size = Size(tile - 12f, tile - 12f))
                    drawDosText("✓", x = x + tile / 2f, y = y + tile / 2f + 8f, color = Color.White, size = 18f, bold = true, align = TextAlign.CENTER)
                }
            }
        }
        // Player.
        val pxp = offX + px * tile; val pyp = offY + py * tile
        drawRect(Color(0xFF2244AA), topLeft = Offset(pxp + 4f, pyp + 10f), size = Size(tile - 8f, tile - 14f))
        drawRect(Color(0xFFEEC09A), topLeft = Offset(pxp + 8f, pyp + 4f), size = Size(tile - 16f, 8f))
        drawRect(Color.Black, topLeft = Offset(pxp + 10f, pyp + 7f), size = Size(2f, 2f))
        drawRect(Color.Black, topLeft = Offset(pxp + tile - 12f, pyp + 7f), size = Size(2f, 2f))
        drawRect(Color(0xFF1A2244), topLeft = Offset(pxp + 8f, pyp + tile - 4f), size = Size(6f, 3f))
        drawRect(Color(0xFF1A2244), topLeft = Offset(pxp + tile - 14f, pyp + tile - 4f), size = Size(6f, 3f))

        // Undo + Reset + Next buttons top-right.
        drawSmallBtn(this, virtualW - 60f, 40f, "↶", Color.White)
        drawSmallBtn(this, virtualW - 60f, 76f, "↺", Color.White)

        if (solved) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(virtualW, virtualH))
            drawDosText(ctx.tr("SOLVED!", "РЕШЕНО!"), x = virtualW / 2f, y = virtualH / 2f - 20f, color = Color(0xFF78D068), size = 32f, bold = true, align = TextAlign.CENTER)
            drawDosText("$moves moves, $pushes pushes", x = virtualW / 2f, y = virtualH / 2f + 12f, color = Color.White, size = 14f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK — NEXT", "КЛИК — ДАЛЬШЕ"), x = virtualW / 2f, y = virtualH / 2f + 42f, color = Color(0xFFCCCCCC), size = 13f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun drawSmallBtn(scope: DrawScope, x: Float, y: Float, glyph: String, c: Color) = with(scope) {
        drawRect(Color(0xFF333333), topLeft = Offset(x, y), size = Size(36f, 32f))
        drawRect(Color(0xFF888888), topLeft = Offset(x, y), size = Size(36f, 2f))
        drawDosText(glyph, x = x + 18f, y = y + 22f, color = c, size = 18f, bold = true, align = TextAlign.CENTER)
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (solved) {
            levelIdx = (levelIdx + 1) % levels.size
            loadLevel()
            ctx.sound.menuSelect()
            return
        }
        // Undo button.
        if (x in (virtualW - 60f)..(virtualW - 24f) && y in 40f..72f) { undo(); ctx.sound.click(); return }
        // Reset button.
        if (x in (virtualW - 60f)..(virtualW - 24f) && y in 76f..108f) { loadLevel(); ctx.sound.click(); return }
    }
}
