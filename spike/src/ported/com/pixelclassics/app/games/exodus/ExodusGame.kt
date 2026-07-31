package com.pixelclassics.app.games.exodus

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * EXODUS — Lemmings-style political allegory.
 *
 * People flee a Russian CITY (left) towards a VPN exit (right). A giant
 * Cheburashka labelled "РКН" advances slowly from the left, dragging a
 * fire trail behind it. Whoever doesn't reach VPN in time gets caught
 * by the censor and burns. Player places 5 tool types to guide the
 * crowd.
 *
 * Restored from v4.11-pre-rewrite — the WebView original used the
 * exact same metaphor; the v5.0 Compose port accidentally stripped
 * the politics out into a generic Lemmings clone. This is the fix.
 */
class ExodusGame : Game {
    override val id: String = "exodus"
    override val titleResId: Int = R.string.game_exodus
    override val backgroundColor: Color = Color(0xFF1A0A30)
    override val introText: String = EXODUS_INTRO_EN
    override fun introTextFor(lang: String): String = pickExodusIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PAPER_NOTEBOOK
    override val helpLines: List<String> = listOf(
        "Click a tool, then click the map to place it",
        "BRIDGE — over pits/rivers · LADDER — over walls · SIGN — turns people",
        "UMBRELLA — softens cliff falls · STOP — a person blocks others",
        "Save 30%+ of the crowd before RKN catches them",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Кликни инструмент, потом кликни карту — поставишь",
        "МОСТ — через ямы/реки · ЛЕСТНИЦА — через стены · ЗНАК — разворачивает",
        "ЗОНТ — смягчает падение со скал · СТОП — человек держит остальных",
        "Спаси 30%+ толпы, пока их не догнал РКН",
    ) else helpLines

    private val cols = 45
    private val rows = 18
    private val tile = 16f                     // bigger tile → easier to tap
    private val W = cols * tile                 // 720
    private val H = rows * tile + hudH + toolH  // 372
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    companion object {
        const val hudH = 28f
        const val toolH = 56f
    }

    private val mapY get() = hudH
    private val groundRow = 13                 // rows 13..17 → above-ground earth start
    private val EMPTY = 0
    private val GROUND = 1
    private val WALL = 2
    private val PIT = 3
    private val RIVER = 4
    private val CLIFF = 5
    private val EXIT = 6
    private val DOOR = 7
    private val BRIDGE = 8
    private val LADDER = 9
    private val SIGN_RIGHT = 10
    private val SIGN_LEFT = 11
    private val UMBRELLA = 12

    private data class Person(var x: Float, var y: Float, var dir: Int = 1, var alive: Boolean = true,
                              var saved: Boolean = false, var falling: Boolean = false,
                              var underUmbrella: Boolean = false, var climbing: Boolean = false,
                              var blocker: Boolean = false)

    private data class LevelDef(
        val people: Int,
        val tools: IntArray,            // counts of [BRIDGE, LADDER, SIGN, UMBRELLA, BLOCKER]
        val chebSpeed: Float,           // cells per second
        val tutorial: String,
        val tutorialRu: String,
        val build: (Array<IntArray>) -> Unit,
    )

    // Tool counts are budgeted so each level is provably solvable: sum(bridges)
    // must cover sum(pit widths) + sum(river widths) with a small safety margin,
    // ladders must cover walls, umbrellas cover cliffs. Юджин на v6.2.2: «на 2-м
    // уровне 3 палки на яму 4 — непроходимо». Fixed.
    private val levels: List<LevelDef> = listOf(
        LevelDef(10, intArrayOf(6, 0, 0, 0, 0), 0.32f, "Bridge the pit!", "Перекрой яму мостом!") { m ->
            fillGround(m); digPit(m, 20, 5); placeExit(m, 40); placeDoor(m, 3)
        },
        LevelDef(14, intArrayOf(5, 2, 0, 0, 0), 0.38f, "Bridges over pits. Ladders by walls.", "Мосты — на ямы. Лестницы — к стенам.") { m ->
            fillGround(m); digPit(m, 15, 4); buildWall(m, 26, 4); placeExit(m, 40); placeDoor(m, 3)
        },
        LevelDef(15, intArrayOf(4, 0, 3, 0, 0), 0.45f, "Signs turn the crowd.", "Знаки разворачивают толпу.") { m ->
            fillGround(m); for (x in 18..32) { m[groundRow - 4][x] = GROUND; m[groundRow - 3][x] = GROUND }
            for (y in groundRow - 3 until groundRow) m[y][17] = GROUND
            for (y in groundRow - 3 until groundRow) m[y][33] = GROUND
            digPit(m, 22, 4); placeExit(m, 40); placeDoor(m, 3)
        },
        LevelDef(18, intArrayOf(4, 0, 0, 3, 0), 0.50f, "Umbrellas catch the fall.", "Зонтики смягчают падение.") { m ->
            fillGround(m); for (x in 3 until 22) for (y in 8 until groundRow) m[y][x] = GROUND
            m[8][22] = CLIFF; digPit(m, 28, 3); placeExit(m, 40); placeDoor(m, 4)
        },
        LevelDef(20, intArrayOf(8, 3, 3, 0, 0), 0.55f, "River, wall, pit. Think.", "Река, стена, яма. Думай.") { m ->
            fillGround(m); digRiver(m, 13, 4); buildWall(m, 22, 3); digPit(m, 30, 4); placeExit(m, 40); placeDoor(m, 3)
        },
        LevelDef(22, intArrayOf(7, 3, 3, 3, 1), 0.65f, "Finale — every tool counts.", "Финал — важен каждый инструмент.") { m ->
            fillGround(m); for (x in 3 until 14) for (y in 8 until groundRow) m[y][x] = GROUND
            m[8][14] = CLIFF; digRiver(m, 18, 3); buildWall(m, 24, 4); digPit(m, 32, 3); placeExit(m, 40); placeDoor(m, 4)
        },
    )

    // ── Builders ───────────────────────────────────────────────────────────

    private fun fillGround(m: Array<IntArray>) {
        for (c in 0 until cols) for (r in groundRow until rows) m[r][c] = GROUND
    }
    private fun digPit(m: Array<IntArray>, x: Int, w: Int) {
        for (c in x until (x + w).coerceAtMost(cols)) for (r in groundRow until rows) m[r][c] = PIT
    }
    private fun digRiver(m: Array<IntArray>, x: Int, w: Int) {
        for (c in x until (x + w).coerceAtMost(cols)) for (r in groundRow until rows) m[r][c] = RIVER
    }
    private fun buildWall(m: Array<IntArray>, x: Int, h: Int) {
        if (x !in 0 until cols) return
        for (r in (groundRow - h) until groundRow) m[r][x] = WALL
    }
    private fun placeExit(m: Array<IntArray>, x: Int) {
        if (x !in 0 until cols) return
        // 3-cell wide neon green VPN exit zone.
        for (dx in 0..2) if (x + dx in 0 until cols) m[groundRow - 1][x + dx] = EXIT
    }
    private fun placeDoor(m: Array<IntArray>, x: Int) {
        if (x !in 0 until cols) return
        m[groundRow - 1][x] = DOOR
    }

    // ── Game state ─────────────────────────────────────────────────────────

    private var board: Array<IntArray> = Array(rows) { IntArray(cols) }
    private val people = mutableListOf<Person>()
    private var levelIdx = 0
    private var time = 0f
    private var spawnAcc = 0f
    private var spawned = 0
    private var saved = 0
    private var lost = 0
    private var totalSaved = 0      // across the whole campaign (saved resets per level)
    private var doorCol = 4
    private var exitCol = 53
    private val toolCounts = intArrayOf(0, 0, 0, 0, 0)
    private var selectedTool = 0
    private var chebX = -5f                     // cells, may be negative
    private val chebW = 4                        // cells wide visual
    private var msg = ""
    private var lastCtx: GameContext? = null
    private fun t(en: String, ru: String) = lastCtx?.tr(en, ru) ?: en
    private var msgTimer = 0f
    // Hover preview — last drag-touch position. -1 = no preview.
    private var hoverC: Int = -1
    private var hoverR: Int = -1

    private enum class State { TITLE, INTRO, PLAYING, LEVELCOMPLETE, GAMEOVER, VICTORY }
    private var state = State.TITLE

    override fun init(ctx: GameContext) {
        lastCtx = ctx
        state = State.TITLE
        levelIdx = 0
        msg = ""
    }

    private fun startLevel() {
        val lv = levels[levelIdx.coerceIn(0, levels.size - 1)]
        board = Array(rows) { IntArray(cols) }
        lv.build(board)
        // Find door/exit positions.
        doorCol = (0 until cols).firstOrNull { c -> (0 until rows).any { r -> board[r][c] == DOOR } } ?: 4
        exitCol = (0 until cols).firstOrNull { c -> (0 until rows).any { r -> board[r][c] == EXIT } } ?: (cols - 7)

        toolCounts[0] = lv.tools[0]; toolCounts[1] = lv.tools[1]; toolCounts[2] = lv.tools[2]
        toolCounts[3] = lv.tools[3]; toolCounts[4] = lv.tools[4]
        selectedTool = (0..4).firstOrNull { toolCounts[it] > 0 } ?: 0

        people.clear(); spawnAcc = 0f; spawned = 0; saved = 0; lost = 0
        chebX = -5f
        state = State.INTRO
        msg = t("LEVEL ${levelIdx + 1}: ", "УРОВЕНЬ ${levelIdx + 1}: ") + t(lv.tutorial, lv.tutorialRu)
        msgTimer = 3.5f
    }

    // ── Update ─────────────────────────────────────────────────────────────

    override fun update(dt: Float, ctx: GameContext) {
        lastCtx = ctx
        time += dt
        if (msgTimer > 0f) msgTimer -= dt
        when (state) {
            State.TITLE, State.LEVELCOMPLETE, State.GAMEOVER, State.VICTORY -> return
            State.INTRO -> {
                if (msgTimer <= 0f) state = State.PLAYING
                return
            }
            State.PLAYING -> {}
        }

        val lv = levels[levelIdx]

        // Spawn — slower, so the crowd doesn't read as "flicker".
        if (spawned < lv.people) {
            spawnAcc += dt
            if (spawnAcc >= 1.4f) {
                spawnAcc = 0f
                people += Person((doorCol + 0.5f) * tile, (groundRow - 1) * tile + tile)
                spawned++
            }
        }

        // Advance Cheburashka.
        chebX += lv.chebSpeed * dt

        // Update each person.
        val it = people.iterator()
        while (it.hasNext()) {
            val p = it.next()
            if (!p.alive || p.saved) continue

            // Caught by РКН? Burns.
            if (p.x < chebX * tile + chebW * tile - 4f) {
                p.alive = false; lost++; ctx.sound.die()
                continue
            }

            // A blocker STANDS — that's the whole point of the STOP tool.
            if (p.blocker) continue

            val curC = (p.x / tile).toInt().coerceIn(0, cols - 1)
            val curR = (p.y / tile).toInt().coerceIn(0, rows - 1)
            val belowR = (curR + 1).coerceAtMost(rows - 1)
            // p.y is the FEET line: curR is the solid row the person stands ON.
            // The body occupies the row ABOVE — and that is where walls, signs,
            // cliffs, ladders and umbrellas actually live. Reading them at the
            // feet row made all five object types dead code.
            val bodyR = (curR - 1).coerceAtLeast(0)

            // Reached exit?
            if (curC in exitCol..(exitCol + 2)) {
                p.saved = true; saved++; ctx.sound.score()
                continue
            }

            // Falling state — no horizontal until lands.
            if (p.falling) {
                val fallSpeed = if (p.underUmbrella) 30f else 110f
                p.y += fallSpeed * dt
                // Fell past the world floor (pit shafts have no bottom).
                if (p.y > rows * tile + tile) {
                    p.alive = false; lost++; ctx.sound.die(); continue
                }
                val groundY = solidRowBelow(p.x, p.y) ?: -1
                if (groundY in 0 until rows) {
                    val landY = groundY * tile
                    if (p.y >= landY) {
                        p.y = landY; p.falling = false; p.underUmbrella = false
                    }
                }
                continue
            }

            // Climbing state — going up a ladder.
            if (p.climbing) {
                p.y -= 25f * dt
                val r = (p.y / tile).toInt()
                if (r <= 0 || (r in 0 until rows && board[r][curC] != LADDER && board[r + 1][curC] != LADDER)) {
                    p.climbing = false
                }
                continue
            }

            // Standing on something? A BRIDGE (on our own cell or the support cell
            // just below) spans a PIT/RIVER and is walkable. Without a bridge,
            // PIT and RIVER are both fatal. (Bug fix: people died on a placed
            // bridge because the death-check row and the placement row differed,
            // and RIVER wasn't fatal at all.)
            val standOn = if (belowR in 0 until rows) board[belowR][curC] else GROUND
            val onBridge = board[curR][curC] == BRIDGE || standOn == BRIDGE
            if (!onBridge && (standOn == PIT || standOn == RIVER)) {
                // A lemming FALLS into the pit visibly — no teleport-death.
                p.falling = true
                continue
            }

            // Sign on the body cell?
            val here = board[bodyR][curC]
            when (here) {
                SIGN_RIGHT -> p.dir = 1
                SIGN_LEFT -> p.dir = -1
            }

            // Walking — ~2.75 cells/sec. (The old constant said "18 cells/sec"
            // but fed pixels: 18 px/s = a glacier; Юджин: «лемминги сломаны».)
            val speed = 44f
            val nextX = p.x + p.dir * speed * dt
            val nextC = (nextX / tile).toInt().coerceIn(0, cols - 1)
            // Reach blocker?
            val blockerHere = people.any { other ->
                other !== p && other.blocker && other.alive && !other.saved &&
                    kotlin.math.abs(other.x - nextX) < tile * 0.8f &&
                    kotlin.math.abs(other.y - p.y) < tile
            }
            if (blockerHere) {
                p.dir = -p.dir; continue
            }
            // Wall ahead? (walls occupy body rows, not the feet row)
            val aheadCell = if (nextC in 0 until cols) board[bodyR][nextC] else WALL
            if (aheadCell == WALL) {
                // Ladder placed beside the wall?
                if (board[bodyR][curC] == LADDER || (bodyR > 0 && board[bodyR - 1][curC] == LADDER)) {
                    p.climbing = true; continue
                }
                p.dir = -p.dir; continue
            }
            // Cliff edge?
            if (here == CLIFF) {
                p.falling = true
                // Umbrella? Check tile this column above the body.
                if (bodyR > 0 && board[bodyR - 1][curC] == UMBRELLA) p.underUmbrella = true
                continue
            }

            // Bridge — counts as solid floor; walk through.
            p.x = nextX

            // Walked off the edge? (no floor below at new cell).
            val newBelow = if (belowR in 0 until rows) board[belowR][nextC] else GROUND
            if (newBelow == EMPTY) {
                p.falling = true
            }
        }
        people.removeAll { !it.alive && !it.saved }

        // Win/lose checks.
        val needed = (lv.people * 0.30f).toInt().coerceAtLeast(1)
        if (saved >= needed && (spawned >= lv.people && people.none { !it.saved })) {
            state = State.LEVELCOMPLETE
            msg = ctx.tr("All through the VPN! RKN weeps.", "Все ушли через VPN! РКН плачет.")
            ctx.sound.win()
        } else if (spawned >= lv.people && people.none { !it.saved } && saved < needed) {
            state = State.GAMEOVER
            msg = ctx.tr("RKN wins. Saved $saved of ${lv.people}, needed $needed.", "РКН победил. Спасено $saved из ${lv.people}, нужно $needed.")
            ctx.sound.gameOver()
        } else if (chebX * tile > exitCol * tile + tile * 3f) {
            // The censor crossed the whole screen — nobody else will make it.
            state = State.GAMEOVER
            msg = ctx.tr("RKN reached the VPN. Saved $saved of ${lv.people}.", "РКН дошёл до VPN. Спасено $saved из ${lv.people}.")
            ctx.sound.gameOver()
        }
    }

    private fun solidRowBelow(px: Float, py: Float): Int? {
        val c = (px / tile).toInt().coerceIn(0, cols - 1)
        var r = (py / tile).toInt().coerceAtLeast(0)
        while (r < rows) {
            val t = board[r][c]
            if (t == GROUND || t == WALL || t == BRIDGE) return r
            r++
        }
        return null
    }

    // ── Draw ───────────────────────────────────────────────────────────────

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(backgroundColor, topLeft = Offset.Zero, size = Size(W, H))

        // HUD
        drawRect(Color(0xFF000000), topLeft = Offset(0f, 0f), size = Size(W, hudH))
        val lv = if (levelIdx in levels.indices) levels[levelIdx] else levels.first()
        val needed = (lv.people * 0.30f).toInt().coerceAtLeast(1)
        drawDosText("LV ${levelIdx + 1}", x = 8f, y = 18f, color = Color.White, size = 13f, bold = true)
        drawDosText(ctx.tr("SAVED $saved/$needed", "СПАСЕНО $saved/$needed"), x = 80f, y = 18f, color = Color(0xFF55FF55), size = 13f, bold = true)
        drawDosText(ctx.tr("LOST $lost", "ПОТЕРЯНО $lost"), x = 240f, y = 18f, color = Color(0xFFFF5555), size = 13f, bold = true)
        drawDosText(ctx.tr("OUT $spawned/${lv.people}", "ВЫШЛО $spawned/${lv.people}"), x = W - 8f, y = 18f, color = Color(0xFFFFD600), size = 13f, bold = true, align = TextAlign.RIGHT)

        if (state == State.TITLE) {
            drawTitleScreen(this, ctx)
            return@with
        }

        // Sky background.
        drawRect(Color(0xFF2A1850), topLeft = Offset(0f, hudH), size = Size(W, (groundRow * tile)))
        // Pixel stars.
        for (i in 0 until 60) {
            val sx = ((i * 137 + 7) % W.toInt()).toFloat()
            val sy = ((i * 89) % ((groundRow - 2) * tile).toInt()).toFloat() + hudH
            drawRect(Color.White.copy(alpha = 0.6f), topLeft = Offset(sx, sy), size = Size(1f, 1f))
        }

        // Tiles.
        for (r in 0 until rows) for (c in 0 until cols) {
            val x = c * tile; val y = mapY + r * tile
            when (board[r][c]) {
                GROUND -> {
                    drawRect(Color(0xFF6A3F1A), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFF8B5A2B), topLeft = Offset(x, y), size = Size(tile, 2f))
                }
                WALL -> {
                    drawRect(Color(0xFF5A5A78), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFF8888AA), topLeft = Offset(x + 1f, y + 1f), size = Size(tile - 2f, 2f))
                }
                PIT -> {
                    drawRect(Color(0xFF0A0010), topLeft = Offset(x, y), size = Size(tile, tile))
                    drawRect(Color(0xFFFF2200), topLeft = Offset(x + 2f, y + tile - 3f), size = Size(tile - 4f, 1f))
                }
                RIVER -> {
                    drawRect(Color(0xFF1858A0), topLeft = Offset(x, y), size = Size(tile, tile))
                    val wave = ((time * 2f + c).toInt() % 2)
                    drawRect(Color(0xFF60A0E0), topLeft = Offset(x + wave * 2f, y + 2f), size = Size(4f, 1f))
                }
                CLIFF -> drawRect(Color(0xFF6A3F1A), topLeft = Offset(x, y), size = Size(tile / 2f, tile))
                BRIDGE -> {
                    drawRect(Color(0xFFCC8844), topLeft = Offset(x, y + 2f), size = Size(tile, 3f))
                    drawRect(Color(0xFF6A3F1A), topLeft = Offset(x, y + 1f), size = Size(tile, 1f))
                }
                LADDER -> {
                    drawRect(Color(0xFFAA8822), topLeft = Offset(x + 1f, y), size = Size(2f, tile))
                    drawRect(Color(0xFFAA8822), topLeft = Offset(x + tile - 3f, y), size = Size(2f, tile))
                    drawRect(Color(0xFFCCAA44), topLeft = Offset(x + 1f, y + tile / 2f - 1f), size = Size(tile - 2f, 2f))
                }
                SIGN_RIGHT, SIGN_LEFT -> {
                    drawRect(Color(0xFF8B5A2B), topLeft = Offset(x + tile / 2f - 1f, y), size = Size(2f, tile))
                    val s = if (board[r][c] == SIGN_RIGHT) "→" else "←"
                    drawDosText(s, x = x + tile / 2f, y = y + tile / 2f + 4f, color = Color(0xFFFFD600), size = 12f, bold = true, align = TextAlign.CENTER)
                }
                UMBRELLA -> {
                    drawRect(Color(0xFFCC2222), topLeft = Offset(x, y), size = Size(tile, 2f))
                    drawRect(Color(0xFF1A1A1A), topLeft = Offset(x + tile / 2f - 1f, y + 1f), size = Size(2f, tile - 2f))
                }
                DOOR -> {
                    // Russian-tricolor door (CITY origin — political signal kept; label EN).
                    drawRect(Color.White, topLeft = Offset(x, y - tile), size = Size(tile, tile))
                    drawRect(Color(0xFFD52B1E), topLeft = Offset(x, y - tile + tile * 2f / 3f), size = Size(tile, tile / 3f))
                    drawRect(Color(0xFF0039A6), topLeft = Offset(x, y - tile + tile / 3f), size = Size(tile, tile / 3f))
                    drawDosText("HOME", x = x + tile / 2f, y = y - tile - 2f, color = Color.White, size = 9f, bold = true, align = TextAlign.CENTER)
                }
                EXIT -> {
                    val pulse = (sin(time * 4f) * 0.3f + 0.7f)
                    drawRect(Color(0xFF00DD44).copy(alpha = pulse), topLeft = Offset(x, y - tile), size = Size(tile, tile))
                    drawDosText("VPN", x = x + tile / 2f, y = y - 2f, color = Color(0xFF00FF44), size = 8f, bold = true, align = TextAlign.CENTER)
                }
            }
        }

        // Hover preview — show where the next tool will land.
        if (state == State.PLAYING && hoverC in 0 until cols && hoverR in 0 until rows) {
            val hx = hoverC * tile; val hy = mapY + hoverR * tile
            val cur = board[hoverR][hoverC]
            val canPlace = when (selectedTool) {
                0 -> cur == PIT || cur == RIVER
                1 -> cur == EMPTY
                2 -> cur == EMPTY
                3 -> cur == EMPTY
                4 -> true  // blocker — applies to a person, not tile
                else -> false
            } && toolCounts[selectedTool] > 0
            val color = if (canPlace) Color(0x6655FF55) else Color(0x66FF5555)
            drawRect(color, topLeft = Offset(hx, hy), size = Size(tile, tile))
            drawRect(if (canPlace) Color(0xFF55FF55) else Color(0xFFFF5555),
                topLeft = Offset(hx, hy), size = Size(tile, 2f))
            drawRect(if (canPlace) Color(0xFF55FF55) else Color(0xFFFF5555),
                topLeft = Offset(hx, hy + tile - 2f), size = Size(tile, 2f))
        }

        // CITY building behind DOOR.
        run {
            val cx = (doorCol - 2f) * tile
            drawRect(Color(0xFF2A2A4A), topLeft = Offset(cx, mapY + (groundRow - 5) * tile), size = Size(tile * 4f, tile * 5f))
            // Windows.
            for (rr in 0 until 4) for (cc in 0 until 3) {
                val wx = cx + 4f + cc * 12f
                val wy = mapY + (groundRow - 4f) * tile + rr * 10f
                drawRect(Color(0xFFFFCC44).copy(alpha = 0.7f), topLeft = Offset(wx, wy), size = Size(4f, 4f))
            }
        }

        // Fire trail directly behind ЧЕБ.
        val fireStartX = (chebX - 5f) * tile
        val fireEndX = chebX * tile
        if (fireEndX > 0) {
            for (i in 0 until 20) {
                val t = (i / 20f)
                val fx = fireStartX + (fireEndX - fireStartX) * t
                val fy = mapY + (groundRow - 1) * tile + 4f + sin((time * 6f + i).toDouble()).toFloat() * 2f
                val a = (1f - t) * 0.8f
                drawRect(Color(0xFFFF4400).copy(alpha = a), topLeft = Offset(fx, fy), size = Size(tile, tile - 4f))
                drawRect(Color(0xFFFFCC00).copy(alpha = a), topLeft = Offset(fx + 2f, fy + 4f), size = Size(tile - 4f, tile - 12f))
            }
        }

        // Cheburashka-РКН.
        drawCheburashka(this, ctx, chebX * tile, mapY + (groundRow - 5) * tile)

        // People.
        for (p in people) if (p.alive && !p.saved) drawPerson(this, p)

        // Tool bar at the bottom.
        drawToolBar(this, ctx)

        // Status message (intro).
        if (msgTimer > 0f && msg.isNotEmpty()) {
            drawRect(Color(0xCC000000), topLeft = Offset(0f, mapY + groundRow * tile / 2f - 14f), size = Size(W, 28f))
            drawDosText(msg, x = W / 2f, y = mapY + groundRow * tile / 2f + 4f, color = Color(0xFFFCD060), size = 12f, bold = true, align = TextAlign.CENTER)
        }

        if (state == State.LEVELCOMPLETE) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("LEVEL CLEAR", "УРОВЕНЬ ПРОЙДЕН"), x = W / 2f, y = H / 2f - 20f, color = Color(0xFF55FF55), size = 24f, bold = true, align = TextAlign.CENTER)
            drawDosText(msg, x = W / 2f, y = H / 2f + 10f, color = Color.White, size = 14f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK — NEXT", "КЛИК — ДАЛЬШЕ"), x = W / 2f, y = H / 2f + 40f, color = Color(0xFFCCCCCC), size = 12f, bold = true, align = TextAlign.CENTER)
        }
        if (state == State.GAMEOVER) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("RKN WINS", "РКН ПОБЕДИЛ"), x = W / 2f, y = H / 2f - 20f, color = Color(0xFFFF3333), size = 24f, bold = true, align = TextAlign.CENTER)
            drawDosText(msg, x = W / 2f, y = H / 2f + 10f, color = Color.White, size = 13f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK — RETRY", "КЛИК — ЗАНОВО"), x = W / 2f, y = H / 2f + 40f, color = Color(0xFFCCCCCC), size = 12f, bold = true, align = TextAlign.CENTER)
        }
        if (state == State.VICTORY) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("EXODUS COMPLETE", "ИСХОД ЗАВЕРШЁН"), x = W / 2f, y = H / 2f - 20f, color = Color(0xFF55FF55), size = 24f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Total saved: $totalSaved", "Всего спасено: $totalSaved"), x = W / 2f, y = H / 2f + 10f, color = Color.White, size = 14f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK — PLAY AGAIN", "КЛИК — ЕЩЁ РАЗ"), x = W / 2f, y = H / 2f + 40f, color = Color(0xFFCCCCCC), size = 12f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun drawTitleScreen(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color(0xFF1A0A30), topLeft = Offset(0f, hudH), size = Size(W, H - hudH))
        drawDosText("E X O D U S", x = W / 2f, y = H * 0.30f, color = Color(0xFF55FF55), size = 40f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Lemmings vs the Cheburnet", "Лемминги против Чебурнета"), x = W / 2f, y = H * 0.42f, color = Color(0xFFFCD060), size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Save people. Lead them to the VPN.", "Спасай людей. Веди их к VPN."), x = W / 2f, y = H * 0.52f, color = Color.White, size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Place bridges, ladders, signs, umbrellas.", "Ставь мосты, лестницы, знаки, зонтики."), x = W / 2f, y = H * 0.58f, color = Color.White, size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Don't let RKN catch them.", "Не дай РКН их догнать."), x = W / 2f, y = H * 0.64f, color = Color(0xFFFF6666), size = 12f, bold = true, align = TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText(ctx.tr("CLICK — START", "КЛИК — СТАРТ"), x = W / 2f, y = H * 0.80f, color = Color.White, size = 18f, bold = true, align = TextAlign.CENTER)
    }

    private fun drawToolBar(scope: DrawScope, ctx: GameContext) = with(scope) {
        val by = H - toolH
        drawRect(Color(0xFF111122), topLeft = Offset(0f, by), size = Size(W, toolH))
        val names = listOf(ctx.tr("BRIDGE", "МОСТ"), ctx.tr("LADDER", "ЛЕСТН"), ctx.tr("SIGN", "ЗНАК"), ctx.tr("UMBR", "ЗОНТ"), ctx.tr("STOP", "СТОП"))
        val icons = listOf("═", "║", "→", "☂", "✋")
        val colors = listOf(
            Color(0xFFCC8844), Color(0xFFAA8822), Color(0xFFFFD600),
            Color(0xFFCC2222), Color(0xFF44CCFF),
        )
        val bw = W / 5f
        for (i in 0 until 5) {
            val bx = i * bw + 4f; val sw = bw - 8f
            val pressed = selectedTool == i
            drawRect(Color.Black, topLeft = Offset(bx - 1f, by + 3f), size = Size(sw + 2f, toolH - 8f))
            drawRect(if (pressed) Color(0xFF333366) else Color(0xFF222244),
                topLeft = Offset(bx, by + 4f), size = Size(sw, toolH - 10f))
            drawRect(colors[i], topLeft = Offset(bx, by + 4f), size = Size(sw, 3f))
            drawDosText(icons[i], x = bx + sw / 2f, y = by + 22f, color = colors[i], size = 16f, bold = true, align = TextAlign.CENTER)
            drawDosText(names[i], x = bx + sw / 2f, y = by + 38f, color = Color.White, size = 9f, bold = true, align = TextAlign.CENTER)
            val cnt = toolCounts[i]
            drawDosText("x$cnt", x = bx + sw - 4f, y = by + 50f, color = if (cnt > 0) Color(0xFF55FF55) else Color(0xFF555555), size = 11f, bold = true, align = TextAlign.RIGHT)
        }
    }

    private fun drawPerson(scope: DrawScope, p: Person) = with(scope) {
        val bx = p.x; val by = mapY + p.y
        val bounce = if (!p.falling && !p.climbing) ((time * 6f + p.x * 0.1f).toInt() % 2) * 1f else 0f
        // Hair.
        drawRect(Color(0xFF22DD44), topLeft = Offset(bx - 3f, by - tile - 7f + bounce), size = Size(6f, 3f))
        // Skin.
        drawRect(Color(0xFFEEC09A), topLeft = Offset(bx - 3f, by - tile - 4f + bounce), size = Size(6f, 4f))
        drawRect(Color.Black, topLeft = Offset(bx - 2f, by - tile - 3f + bounce), size = Size(1f, 1f))
        drawRect(Color.Black, topLeft = Offset(bx + 1f, by - tile - 3f + bounce), size = Size(1f, 1f))
        // Body.
        drawRect(Color(0xFF2244AA), topLeft = Offset(bx - 4f, by - tile + bounce), size = Size(8f, 7f))
        drawRect(Color(0xFF4466CC), topLeft = Offset(bx - 3f, by - tile + bounce), size = Size(6f, 6f))
        // Legs.
        drawRect(Color(0xFF1A2244), topLeft = Offset(bx - 3f, by - tile + 7f + bounce), size = Size(2f, 3f))
        drawRect(Color(0xFF1A2244), topLeft = Offset(bx + 1f, by - tile + 7f + bounce), size = Size(2f, 3f))
        // Umbrella overlay when slow-falling.
        if (p.falling && p.underUmbrella) {
            drawRect(Color(0xFFCC2222), topLeft = Offset(bx - 6f, by - tile - 9f), size = Size(12f, 2f))
            drawRect(Color(0xFF1A1A1A), topLeft = Offset(bx, by - tile - 9f), size = Size(1f, 6f))
        }
        if (p.blocker) {
            drawRect(Color(0xFF44CCFF).copy(alpha = 0.4f), topLeft = Offset(bx - 6f, by - tile - 10f), size = Size(12f, tile + 6f))
        }
    }

    private fun drawCheburashka(scope: DrawScope, ctx: GameContext, x: Float, y: Float) = with(scope) {
        // 4-tile wide × 5-tile tall chunky pixel Cheburashka.
        val w = chebW * tile  // body width
        val h = 5 * tile
        val ux = x; val uy = y
        // HUGE round ears — two circles up top.
        val earR = tile * 1.2f
        drawCircle(Color(0xFF6A3818), radius = earR + 2f, center = Offset(ux + tile * 0.8f, uy + tile * 0.3f))
        drawCircle(Color(0xFF8B4513), radius = earR, center = Offset(ux + tile * 0.8f, uy + tile * 0.3f))
        drawCircle(Color(0xFF6A3818), radius = earR + 2f, center = Offset(ux + w - tile * 0.8f, uy + tile * 0.3f))
        drawCircle(Color(0xFF8B4513), radius = earR, center = Offset(ux + w - tile * 0.8f, uy + tile * 0.3f))
        // Head (round).
        drawCircle(Color(0xFF6A3818), radius = tile * 1.4f + 2f, center = Offset(ux + w / 2f, uy + tile * 1.4f))
        drawCircle(Color(0xFF8B4513), radius = tile * 1.4f, center = Offset(ux + w / 2f, uy + tile * 1.4f))
        // Big creepy eyes.
        drawCircle(Color.White, radius = 4f, center = Offset(ux + w / 2f - 6f, uy + tile * 1.2f))
        drawCircle(Color.White, radius = 4f, center = Offset(ux + w / 2f + 6f, uy + tile * 1.2f))
        drawCircle(Color.Black, radius = 2.5f, center = Offset(ux + w / 2f - 5f + sin(time * 3f) * 1f, uy + tile * 1.2f))
        drawCircle(Color.Black, radius = 2.5f, center = Offset(ux + w / 2f + 7f + sin(time * 3f) * 1f, uy + tile * 1.2f))
        // Snout.
        drawCircle(Color(0xFFAA6A2A), radius = 3f, center = Offset(ux + w / 2f, uy + tile * 1.6f))
        drawCircle(Color.Black, radius = 1.5f, center = Offset(ux + w / 2f, uy + tile * 1.5f))
        // Body — dressed in red sash with white «РКН».
        drawRect(Color(0xFF8B4513), topLeft = Offset(ux + tile * 0.4f, uy + tile * 2.4f), size = Size(w - tile * 0.8f, tile * 2.4f))
        drawRect(Color(0xFFCC0000), topLeft = Offset(ux + tile * 0.4f, uy + tile * 2.8f), size = Size(w - tile * 0.8f, tile * 0.8f))
        drawDosText(ctx.tr("RKN", "РКН"), x = ux + w / 2f, y = uy + tile * 3.4f, color = Color.White, size = 10f, bold = true, align = TextAlign.CENTER)
        // Arms — animated walking.
        val swing = sin(time * 4f).toFloat() * 3f
        drawRect(Color(0xFF8B4513), topLeft = Offset(ux, uy + tile * 2.6f + swing), size = Size(tile * 0.4f, tile * 1.4f))
        drawRect(Color(0xFF8B4513), topLeft = Offset(ux + w - tile * 0.4f, uy + tile * 2.6f - swing), size = Size(tile * 0.4f, tile * 1.4f))
        // Feet (stomping).
        val step = ((time * 3f).toInt() % 2)
        drawRect(Color(0xFF000000), topLeft = Offset(ux + tile * 0.6f - step * 2f, uy + tile * 4.6f), size = Size(tile * 1.2f, tile * 0.5f))
        drawRect(Color(0xFF000000), topLeft = Offset(ux + w - tile * 1.8f + step * 2f, uy + tile * 4.6f), size = Size(tile * 1.2f, tile * 0.5f))
    }

    // ── Input ──────────────────────────────────────────────────────────────

    override fun onPointerMove(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (state != State.PLAYING) { hoverC = -1; hoverR = -1; return }
        if (y >= H - toolH || y < mapY) { hoverC = -1; hoverR = -1; return }
        hoverC = (x / tile).toInt().coerceIn(0, cols - 1)
        hoverR = ((y - mapY) / tile).toInt().coerceIn(0, rows - 1)
    }

    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        hoverC = -1; hoverR = -1
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        when (state) {
            State.TITLE -> { startLevel(); return }
            State.LEVELCOMPLETE -> {
                totalSaved += saved
                ctx.scores.set(this.id, totalSaved)   // record campaign progress (id = game id, not the pointer id!)
                levelIdx++
                if (levelIdx >= levels.size) { state = State.VICTORY } else startLevel()
                return
            }
            State.GAMEOVER -> { startLevel(); return }
            State.VICTORY -> { levelIdx = 0; saved = 0; lost = 0; totalSaved = 0; startLevel(); return }
            State.INTRO -> { msgTimer = 0f; state = State.PLAYING; return }
            State.PLAYING -> {}
        }

        // Tool bar.
        val by = H - toolH
        if (y >= by) {
            val bw = W / 5f
            val idx = (x / bw).toInt().coerceIn(0, 4)
            if (toolCounts[idx] > 0) { selectedTool = idx; ctx.sound.click() }
            return
        }

        // Place selected tool on map.
        val c = (x / tile).toInt().coerceIn(0, cols - 1)
        val r = ((y - mapY) / tile).toInt().coerceIn(0, rows - 1)
        if (toolCounts[selectedTool] <= 0) return
        val cur = board[r][c]
        when (selectedTool) {
            0 -> if (cur == PIT || cur == RIVER) { board[r][c] = BRIDGE; toolCounts[0]--; ctx.sound.click() }
            1 -> if (cur == EMPTY) {
                // Place ladder above wall (search for nearest wall to the right of col).
                board[r][c] = LADDER; toolCounts[1]--; ctx.sound.click()
            }
            2 -> if (cur == EMPTY) {
                // Sign — direction depends on which half of the cell user tapped.
                val fracX = (x / tile) - c
                board[r][c] = if (fracX > 0.5f) SIGN_RIGHT else SIGN_LEFT
                toolCounts[2]--; ctx.sound.click()
            }
            3 -> if (cur == EMPTY) { board[r][c] = UMBRELLA; toolCounts[3]--; ctx.sound.click() }
            4 -> {
                // Convert nearest person to a blocker — but never an invisible
                // saved one parked at the exit (that made levels unwinnable).
                val nearest = people.filter { it.alive && !it.saved && !it.blocker }
                    .minByOrNull { kotlin.math.abs(it.x - x) + kotlin.math.abs((mapY + it.y) - y) }
                if (nearest != null && !nearest.blocker) {
                    nearest.blocker = true; toolCounts[4]--; ctx.sound.click()
                }
            }
        }
    }
}
