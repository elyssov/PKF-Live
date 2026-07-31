package com.pixelclassics.app.games.et

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.dosTextWidth
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.abs
import kotlin.math.sign
import kotlin.random.Random

/** Chunky Atari-2600 E.T. port — pits, neck, phone pieces, FBI, landing. */
class ETGame : Game {
    override val id: String = "et"
    override val titleResId: Int = R.string.game_et
    override val backgroundColor: Color = Color.Black
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    // No campfire plaque: the game opens with its own teletype (State.INTRO)
    // that prints the same story — with a plaque the player read it twice.
    override fun introTextFor(lang: String): String? = null
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.TERMINAL_GREEN_CRT
    override val helpLines: List<String> = listOf(
        "D-pad — walk (auto repeats while held)",
        "● FIRE — extend the neck (climb out of pits, place the call)",
        "Collect 3 phone parts. Reach the LANDING zone. Phone home.",
    )

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private val px = 16f                    // chunky pixel
    private val cols = (W / px).toInt()
    private val rows = (H / px).toInt()

    private val zoneNames = arrayOf("FOREST", "WASTELAND", "CITY", "DESERT", "LANDING", "VOID")
    private val zoneBg = arrayOf(
        Color(0xFF1A6010), Color(0xFF3A2A10), Color(0xFF383838),
        Color(0xFF6A4A20), Color(0xFF1828A0), Color(0xFF000000),
    )
    private val zoneFg = arrayOf(
        Color(0xFF30B020), Color(0xFF6A4A20), Color(0xFF808080),
        Color(0xFFC06820), Color(0xFF3040D0), Color(0xFF3A2A10),
    )

    private enum class State { INTRO, TITLE, PLAY, CALL, WIN, DEAD }

    // INTRO terminal — big-font typewriter "printing" inside a narrow column.
    // The story prints character by character like an old teletype; tap the
    // lower half to advance pages (or finish printing), tap the upper half
    // to go back. A throttled click() makes the "тррр" clatter.
    private val introFontSize = 26f          // big — Юджин «глаза сломал» fix
    private val introLineH = 32f             // line pitch at that size
    private val introColW = 360f             // narrow, portrait-ish column
    private val introColX = (W - introColW) / 2f
    private val introTop = 70f               // first baseline
    private val introBottom = H - 56f        // leave room for the prompt
    private val introLinesPerPage = ((introBottom - introTop) / introLineH).toInt()
    private val introCharsPerSec = 42f       // teletype print speed
    private val introTickEvery = 3           // click() every N printed chars
    private var introPages: List<List<String>> = emptyList()  // wrapped pages
    private var introLang: String? = null    // lang the pages were laid out for
    private var introPage = 0
    private var introElapsed = 0f            // seconds spent printing this page
    private var introTickedChars = 0         // chars that have already clicked

    private data class Pit(val x: Int, val y: Int, val w: Int, val h: Int)
    private data class Phone(val zone: Int, var x: Int, var y: Int, var collected: Boolean)
    private data class Candy(val zone: Int, val x: Int, val y: Int, var active: Boolean)
    private data class Agent(var zone: Int, var x: Int, var y: Int, var moveAcc: Float)

    private var state = State.TITLE
    private var zone = 0
    private var ex = cols / 2
    private var ey = rows / 2
    private var pitsByZone = HashMap<Int, MutableList<Pit>>()
    private var phones = mutableListOf<Phone>()
    private var candies = mutableListOf<Candy>()
    private var fbi = Agent(2, 5, 5, 0f)
    private var scientist = Agent(0, cols - 8, rows - 8, 0f)
    private var energy = 100f
    private var timer = 99f
    private var pitFalls = 0
    private var elliottRevives = 3
    private var inPit = false
    private var pitDepth = 0f
    private var neckExtended = false
    private var levitating = false
    private var score = 0
    private var best = 0
    private var msg = ""
    private var msgTimer = 0f
    private var titleBlink = 0f
    private var callTimer = 0f
    private var shipY = -3f
    private var inputDelay = 0f

    private val tips = arrayOf(
        "TIP: TRY NOT FALLING IN PIT",
        "TIP: GAME GETS EASIER WHEN YOU STOP",
        "TIP: PITS ARE A FEATURE NOT A BUG",
        "TIP: E.T. PHONE HOME. E.T. FALL IN HOLE.",
        "TIP: BURIED IN ALAMOGORDO NM",
    )

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        // The terminal teletype intro is THE feature of this game (Юджин:
        // «байка у костра — главная фишка ExTer»). It plays on EVERY launch —
        // the once-only etIntroSeen gate hid the story after the first run
        // («где описание в ЭкстЕре»), and Space/arrows skip it in a second.
        state = State.INTRO
        val lang = ctx.settings.lang
        if (introPages.isEmpty() || introLang != lang) {
            introLang = lang
            introPages = layoutIntro(pickETIntro(lang))
        }
        introPage = 0
        introElapsed = 0f
        introTickedChars = 0
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (dx == 0 && dy == 0) return   // ignore D-pad release; no input-delay churn on a neutral
        if (state == State.INTRO) {
            // Arrows page the teletype: forward/down advances, back/up rewinds.
            if (dx > 0 || dy > 0) introAdvance(ctx)
            else if (introPage > 0) { introPage--; introElapsed = 0f; introTickedChars = 0 }
            return
        }
        moveDir(dx, dy, ctx)
    }

    override fun onFire(ctx: GameContext) {
        when (state) {
            State.INTRO -> introAdvance(ctx)   // Space/FIRE pages the teletype
            State.TITLE, State.DEAD, State.WIN -> newGame()
            State.PLAY -> extendNeck(ctx)
            State.CALL -> {}
        }
    }

    /**
     * Builds the paginated, word-wrapped intro. The narrative content is left
     * fully intact — we only strip the wide-terminal decoration (the `> `
     * gutter markers and the ╔╚║ box borders) so the prose fits the narrow
     * portrait column, then re-wrap each paragraph to [introColW] and pack the
     * wrapped lines into screen-sized pages. Blank source lines become
     * paragraph breaks; a paragraph never starts a page on its last orphan.
     */
    private fun layoutIntro(source: String): List<List<String>> {
        // 1) Normalise raw lines into clean paragraphs.
        val paragraphs = mutableListOf<String>()
        val cur = StringBuilder()
        fun flush() { if (cur.isNotBlank()) paragraphs += cur.toString().trim(); cur.setLength(0) }
        for (raw in source.lines()) {
            var s = raw.trim()
            // Drop the decorative box border lines entirely.
            if (s.startsWith("╔") || s.startsWith("╚") || s.startsWith("║")) {
                flush(); if (s.startsWith("║")) paragraphs += s.trim('║', ' '); continue
            }
            // Strip the teletype gutter marker.
            if (s.startsWith(">")) s = s.removePrefix(">").trim()
            if (s.isEmpty()) { flush(); continue }
            // A section header like "── ЧАСТЬ 1 ──" is its own paragraph.
            if (s.startsWith("──")) { flush(); paragraphs += s; flush(); continue }
            if (cur.isNotEmpty()) cur.append(' ')
            cur.append(s)
        }
        flush()

        // 2) Word-wrap each paragraph to the column, then pack into pages.
        val pages = mutableListOf<MutableList<String>>()
        var page = mutableListOf<String>()
        fun pushPage() { if (page.isNotEmpty()) { pages += page; page = mutableListOf() } }
        for (para in paragraphs) {
            val wrapped = wrapToColumn(para)
            // Don't strand a paragraph header at the very bottom of a page.
            if (page.size + wrapped.size > introLinesPerPage && page.isNotEmpty()) pushPage()
            for (line in wrapped) {
                if (page.size >= introLinesPerPage) pushPage()
                page += line
            }
            // Blank spacer between paragraphs (cheap visual breathing room).
            if (page.isNotEmpty() && page.size < introLinesPerPage) page += ""
        }
        pushPage()
        return pages
    }

    /** Greedy monospace word-wrap of one paragraph to [introColW]. */
    private fun wrapToColumn(text: String): List<String> {
        val out = mutableListOf<String>()
        val line = StringBuilder()
        for (word in text.split(' ')) {
            if (word.isEmpty()) continue
            val candidate = if (line.isEmpty()) word else "$line $word"
            if (dosTextWidth(candidate, introFontSize, bold = true) <= introColW) {
                line.clear(); line.append(candidate)
            } else {
                if (line.isNotEmpty()) out += line.toString()
                line.clear(); line.append(word)
            }
        }
        if (line.isNotEmpty()) out += line.toString()
        return if (out.isEmpty()) listOf("") else out
    }

    /** Total printable glyphs on the current page (drives the typewriter). */
    private fun introPageChars(): Int {
        val pg = introPages.getOrNull(introPage) ?: return 0
        return pg.sumOf { it.length } + (pg.size - 1).coerceAtLeast(0) // +newlines
    }

    private fun introPrinted(): Int = (introElapsed * introCharsPerSec).toInt()

    private fun introPageDone(): Boolean = introPrinted() >= introPageChars()

    private fun newGame() {
        zone = 0; ex = cols / 2; ey = rows / 2
        energy = 100f; timer = 99f; pitFalls = 0; inPit = false; pitDepth = 0f
        neckExtended = false; levitating = false; elliottRevives = 3
        score = 0; msg = ""; msgTimer = 0f; inputDelay = 0f
        pitsByZone.clear()
        for (z in intArrayOf(1, 3, 5)) {
            val list = mutableListOf<Pit>()
            for (i in 0 until 3 + Random.nextInt(3)) {
                list += Pit(
                    Random.nextInt(2, cols - 7),
                    Random.nextInt(4, rows - 6),
                    3 + Random.nextInt(2),
                    2 + Random.nextInt(2),
                )
            }
            pitsByZone[z] = list
        }
        phones.clear()
        for (z in intArrayOf(1, 3, 5)) {
            val pos = randomFreeCell(z)
            phones += Phone(z, pos.first, pos.second, false)
        }
        candies.clear()
        for (z in 0 until 6) for (i in 0 until 2) {
            val pos = randomFreeCell(z)
            candies += Candy(z, pos.first, pos.second, true)
        }
        fbi = Agent(2, 5, 5, 0f)
        scientist = Agent(0, cols - 8, rows - 8, 0f)
        state = State.PLAY
    }

    private fun randomFreeCell(zoneId: Int): Pair<Int, Int> {
        val pits = pitsByZone[zoneId] ?: emptyList()
        repeat(60) {
            val x = Random.nextInt(3, cols - 6)
            val y = Random.nextInt(5, rows - 8)
            val inPit = pits.any { p -> x + 2 >= p.x && x <= p.x + p.w && y + 4 >= p.y && y <= p.y + p.h }
            if (!inPit) return x to y
        }
        return cols / 2 to rows / 2
    }

    private fun pitsHere(): List<Pit> = pitsByZone[zone] ?: emptyList()

    private fun showMessage(s: String, dur: Float = 2f) { msg = s; msgTimer = dur }
    private fun showTip() { msg = tips.random(); msgTimer = 3f }

    private fun checkPit(): Boolean {
        for (p in pitsHere()) {
            if (ex + 2 >= p.x && ex <= p.x + p.w && ey + 7 >= p.y && ey + 2 <= p.y + p.h) return true
        }
        return false
    }

    private fun fallInPit(ctx: GameContext) {
        inPit = true; pitDepth = 0f; levitating = false; neckExtended = false
        pitFalls++; energy -= 5f; score -= 10
        ctx.sound.die()
        if (pitFalls % 3 == 0) showTip()
    }

    private fun moveDir(dx: Int, dy: Int, ctx: GameContext) {
        if (inputDelay > 0f) return
        if (state == State.TITLE || state == State.DEAD || state == State.WIN) { newGame(); return }
        if (state != State.PLAY) return
        if (inPit) {
            if (dy < 0) { neckExtended = true; ctx.sound.click() }
            return
        }
        ex += dx * 2; ey += dy * 2
        neckExtended = false
        inputDelay = 0.08f
        if (ey < 3) ey = 3
        if (ey > rows - 10) ey = rows - 10
        if (ex < 0) { zone = (zone + 5) % 6; ex = cols - 5; ctx.sound.menuSelect() }
        else if (ex > cols - 4) { zone = (zone + 1) % 6; ex = 1; ctx.sound.menuSelect() }
        if (checkPit()) { fallInPit(ctx); return }
        for (p in phones) {
            if (!p.collected && p.zone == zone && abs(ex - p.x) < 3 && abs(ey + 4 - p.y) < 4) {
                p.collected = true; score += 100; ctx.sound.score()
                val k = phones.count { it.collected }
                showMessage(if (k == 3) "ALL PIECES! GO TO LANDING!" else "PHONE PIECE $k/3")
            }
        }
        for (c in candies) {
            if (c.active && c.zone == zone && abs(ex - c.x) < 3 && abs(ey + 4 - c.y) < 4) {
                c.active = false; energy = (energy + 20f).coerceAtMost(100f); score += 25; ctx.sound.eat()
            }
        }
        if (zone == 4 && phones.all { it.collected } && neckExtended) {
            state = State.CALL; callTimer = 0f; shipY = -3f; showMessage("CALLING HOME...", 3f); ctx.sound.win()
        }
    }

    private fun extendNeck(ctx: GameContext) {
        if (state != State.PLAY) return
        neckExtended = !neckExtended
        ctx.sound.click()
        if (!inPit && neckExtended && zone == 4 && phones.all { it.collected }) {
            state = State.CALL
            callTimer = 0f
            shipY = -3f
            showMessage("CALLING HOME...", 3f)
            ctx.sound.win()
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        titleBlink += dt
        if (msgTimer > 0f) msgTimer -= dt
        if (inputDelay > 0f) inputDelay -= dt
        if (state == State.INTRO) {
            val before = introPrinted()
            introElapsed += dt
            val total = introPageChars()
            val after = introPrinted().coerceAtMost(total)
            // Teletype clatter: tick once every few freshly-printed glyphs,
            // and fall silent the moment the page is fully printed.
            if (after < total) {
                while (introTickedChars + introTickEvery <= after) {
                    introTickedChars += introTickEvery
                    ctx.sound.click()
                }
            } else if (before < total) {
                introTickedChars = total       // page just finished — go quiet
            }
        }
        when (state) {
            State.PLAY -> {
                timer -= dt
                if (inPit && neckExtended) {
                    levitating = true; pitDepth -= dt * 2f; energy -= dt * 4f
                    if (pitDepth <= -4f) { inPit = false; levitating = false; neckExtended = false; pitDepth = 0f; ey -= 3; if (ey < 3) ey = 3 }
                }
                updateAgent(fbi, dt, ctx, isFbi = true)
                updateAgent(scientist, dt, ctx, isFbi = false)
                energy -= dt * 0.5f
                if (energy <= 0f) {
                    if (elliottRevives > 0) {
                        elliottRevives--; energy = 50f; showMessage("ELLIOTT REVIVED E.T.! ($elliottRevives LEFT)"); ctx.sound.score()
                    } else { die(ctx) }
                }
                if (timer <= 0f) die(ctx)
            }
            State.CALL -> {
                callTimer += dt
                shipY += dt * 1.5f
                if (callTimer > 3.5f) {
                    state = State.WIN
                    score += (timer * 10f).toInt() + (energy * 5f).toInt()
                    if (score > best) { best = score; ctx.scores.set(id, score) }
                }
            }
            else -> {}
        }
    }

    private fun die(ctx: GameContext) {
        state = State.DEAD; ctx.sound.gameOver()
        if (score > best) { best = score; ctx.scores.set(id, score) }
    }

    private fun updateAgent(a: Agent, dt: Float, ctx: GameContext, isFbi: Boolean) {
        a.moveAcc += dt
        val interval = if (isFbi) 0.25f else 0.4f
        if (a.moveAcc < interval) return
        a.moveAcc = 0f
        if (a.zone == zone) {
            a.x += sign((ex - a.x).toFloat()).toInt() * if (Random.nextFloat() > 0.3f) 1 else 0
            a.y += sign((ey - a.y).toFloat()).toInt() * if (Random.nextFloat() > 0.3f) 1 else 0
            a.x = a.x.coerceIn(1, cols - 4)
            a.y = a.y.coerceIn(3, rows - 9)
            if (!inPit && abs(ex - a.x) < 4 && abs(ey - a.y) < 5) {
                if (isFbi) {
                    val stolen = phones.firstOrNull { it.collected }
                    if (stolen != null) {
                        stolen.collected = false
                        val pos = randomFreeCell(stolen.zone)
                        stolen.x = pos.first; stolen.y = pos.second
                        showMessage("AGENT TOOK YOUR STUFF"); score -= 50
                    }
                    ex += if (ex > a.x) 4 else -4; inputDelay = 0.5f
                } else {
                    zone = 2; ex = cols / 2; ey = rows / 2
                    timer = (timer - 10f).coerceAtLeast(0f)
                    showMessage("SCIENCE HAPPENED. -10 SEC.")
                    inputDelay = 0.5f; a.zone = 0
                }
                ctx.sound.die()
            }
        } else if (Random.nextFloat() > 0.85f) {
            a.zone = (a.zone + (if (Random.nextBoolean()) 1 else -1) + 6) % 6
            a.x = Random.nextInt(2, cols - 5); a.y = Random.nextInt(4, rows - 9)
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        when (state) {
            State.INTRO -> drawIntro(this)
            State.TITLE -> drawTitle(this)
            State.DEAD -> drawDead(this)
            State.WIN -> drawWin(this)
            State.PLAY, State.CALL -> drawPlay(this)
        }
    }

    private fun drawIntro(scope: DrawScope) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))
        val green = Color(0xFF30D050)
        val greenDim = Color(0xFF1A8030)
        val page = introPages.getOrNull(introPage) ?: emptyList()

        // Faint frame around the narrow "terminal" column.
        val frame = Color(0xFF0E3018)
        drawRect(frame, topLeft = Offset(introColX - 10f, introTop - introLineH), size = Size(introColW + 20f, 2f))
        drawRect(frame, topLeft = Offset(introColX - 10f, introBottom + 6f), size = Size(introColW + 20f, 2f))

        // Typewriter reveal: only the first `printed` glyphs of the page show,
        // counting characters across lines (newlines count as one glyph each).
        val printed = introPrinted()
        var budget = printed
        var y = introTop
        for ((idx, full) in page.withIndex()) {
            val take = full.length.coerceAtMost(budget.coerceAtLeast(0))
            val shown = if (take >= full.length) full else full.substring(0, take)
            val isHeader = full.startsWith("──")
            if (shown.isNotEmpty()) {
                drawDosText(
                    shown,
                    x = introColX,
                    y = y,
                    color = if (isHeader) greenDim else green,
                    size = introFontSize,
                    bold = true,
                )
            }
            // Blinking print cursor on the line currently being typed.
            if (take < full.length && (titleBlink * 3f).toInt() % 2 == 0) {
                val cw = dosTextWidth(shown, introFontSize, bold = true)
                drawDosText("_", x = introColX + cw, y = y, color = green, size = introFontSize, bold = true)
            }
            budget -= full.length
            if (idx < page.size - 1) budget -= 1 // newline glyph
            y += introLineH
            if (budget < 0) break
        }

        // CRT scanlines over the whole screen.
        var sy = 0f
        while (sy < H) { drawRect(Color.Black.copy(alpha = 0.20f), topLeft = Offset(0f, sy), size = Size(W, 1f)); sy += 3f }

        // Bottom prompt + page counter.
        drawRect(Color.Black, topLeft = Offset(0f, H - 40f), size = Size(W, 40f))
        val last = introPage >= introPages.size - 1
        val done = introPageDone()
        val hint = when {
            !done -> "CLICK BELOW TO PRINT ALL · ABOVE TO GO BACK"
            last -> "CLICK BELOW TO BEGIN"
            else -> "CLICK BELOW: NEXT  ·  CLICK ABOVE: BACK"
        }
        if (!last || (titleBlink * 2f).toInt() % 2 == 0) {
            drawDosText(hint, x = W / 2f, y = H - 22f, color = Color(0xFF208030), size = 11f, bold = true, align = TextAlign.CENTER)
        }
        drawDosText(
            "${introPage + 1}/${introPages.size}",
            x = W / 2f, y = H - 6f, color = greenDim, size = 10f, bold = true, align = TextAlign.CENTER,
        )
    }

    private fun drawCell(scope: DrawScope, cx: Int, cy: Int, color: Color, w: Int = 1, h: Int = 1) = with(scope) {
        drawRect(color, topLeft = Offset(cx * px, cy * px), size = Size(w * px, h * px))
    }

    private fun drawPlay(scope: DrawScope) = with(scope) {
        drawRect(zoneBg[zone], topLeft = Offset.Zero, size = Size(W, H))
        // Sparse texture
        for (x in 0 until cols step 4) for (y in 3 until rows - 2 step 3) {
            if ((x + y + zone * 3) % 11 < 2) drawCell(this, x, y, zoneFg[zone], 2, 1)
        }
        // Zone label.
        drawRect(Color.Black, topLeft = Offset(0f, 0f), size = Size(W, 2 * px))
        drawDosText(zoneNames[zone], x = W / 2f, y = px * 1.7f, color = zoneFg[zone], size = 13f, bold = true, align = TextAlign.CENTER)

        // Pits: ugly and unfair-looking on purpose, but visible enough to read.
        for (p in pitsHere()) {
            drawRect(
                Color(0x84000000),
                topLeft = Offset(p.x * px, p.y * px),
                size = Size(p.w * px, p.h * px),
            )
            drawRect(
                Color(0x8030B020),
                topLeft = Offset(p.x * px, p.y * px),
                size = Size(p.w * px, 2f),
            )
        }

        // Candies.
        for (c in candies) if (c.active && c.zone == zone) drawCell(this, c.x, c.y, Color(0xFFC06820), 1, 1)
        // Phone pieces (flicker).
        for (p in phones) if (!p.collected && p.zone == zone) {
            if ((titleBlink * 5f).toInt() % 2 == 0) drawCell(this, p.x, p.y, Color(0xFFE8E828), 1, 1)
        }

        // Ship.
        if (state == State.CALL) {
            val sx = cols / 2 - 4
            val sy = shipY.toInt().coerceAtLeast(0)
            drawCell(this, sx + 3, sy, Color.Gray, 3, 1)
            drawCell(this, sx + 1, sy + 1, Color.Gray, 7, 1)
            drawCell(this, sx, sy + 2, Color.Gray, 9, 1)
        }

        // E.T.
        if (inPit) {
            val py = ey - 1 + pitDepth.toInt()
            drawCell(this, ex, py + 3, Color(0xFF30B020), 3, 5)
            val headY = if (neckExtended) py else py + 2
            drawCell(this, ex + 1, headY, Color(0xFF30B020), 1, 1)
            if (neckExtended) drawCell(this, ex + 1, py + 1, Color(0xFF30B020), 1, 2)
        } else {
            drawCell(this, ex, ey + 2, Color(0xFF30B020), 3, 6)
            if (neckExtended) {
                drawCell(this, ex + 1, ey - 1, Color(0xFF30B020), 1, 1)
                drawCell(this, ex + 1, ey, Color(0xFF30B020), 1, 2)
            } else drawCell(this, ex + 1, ey + 1, Color(0xFF30B020), 1, 1)
        }

        // Agents.
        if (fbi.zone == zone) drawCell(this, fbi.x, fbi.y, Color(0xFF3050C0), 3, 7)
        if (scientist.zone == zone) drawCell(this, scientist.x, scientist.y, Color(0xFFC0C0C0), 3, 7)

        // HUD bottom.
        drawRect(Color.Black, topLeft = Offset(0f, H - 2 * px), size = Size(W, 2 * px))
        drawDosText("NRG", x = px * 2f, y = H - px * 0.4f, color = Color.White, size = 10f)
        drawRect(
            if (energy > 30f) Color(0xFF30B020) else Color(0xFFC02020),
            topLeft = Offset(px * 4.5f, H - px * 1.6f),
            size = Size(energy.coerceAtLeast(0f) / 100f * W * 0.2f, px),
        )
        drawDosText("T:${timer.toInt()}", x = W - px * 5f, y = H - px * 0.4f, color = Color(0xFFD0C010), size = 10f)
        val collected = phones.count { it.collected }
        drawDosText("PH:$collected/3", x = W / 2f, y = H - px * 0.4f, color = Color(0xFFE8E828), size = 10f, align = TextAlign.CENTER)
        drawDosText("SC:$score", x = px * 4f, y = px * 1.5f, color = Color(0xFFD0C010), size = 10f, align = TextAlign.CENTER)
        drawDosText("EL:$elliottRevives", x = W - px * 4f, y = px * 1.5f, color = Color(0xFFD070A0), size = 10f, align = TextAlign.CENTER)
        drawDosText("BEST $best", x = W / 2f, y = px * 1.5f, color = Color(0xFFFFD600), size = 10f, bold = true, align = TextAlign.CENTER)

        if (msgTimer > 0f) {
            drawRect(Color(0xCC000000), topLeft = Offset(0f, H * 0.4f), size = Size(W, px * 2f))
            drawDosText(msg, x = W / 2f, y = H * 0.4f + px * 1.4f, color = Color.White, size = 11f, align = TextAlign.CENTER)
        }
        if (inPit) {
            drawRect(Color(0xCC000000), topLeft = Offset(0f, 2 * px), size = Size(W, px * 2f))
            drawDosText("IN PIT! HOLD UP OR FIRE TO LEVITATE", x = W / 2f, y = px * 3.2f, color = Color(0xFFC02020), size = 10f, align = TextAlign.CENTER)
        }
        if (state == State.CALL && (titleBlink * 5f).toInt() % 2 == 0) {
            drawDosText("CALLING HOME...", x = W / 2f, y = H * 0.3f, color = Color(0xFF30B020), size = 18f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun drawTitle(scope: DrawScope) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))
        val cx = cols / 2 - 1; val cy = rows / 2 - 4
        drawCell(this, cx, cy + 2, Color(0xFF30B020), 3, 6)
        drawCell(this, cx + 1, cy + 1, Color(0xFF30B020), 1, 1)
        if ((titleBlink * 2f).toInt() % 2 == 0) {
            drawCell(this, cx + 1, cy - 1, Color(0xFF30B020), 1, 1)
            drawCell(this, cx + 1, cy, Color(0xFF30B020), 1, 1)
        }
        drawDosText("ExTer", x = W / 2f, y = px * 3f, color = Color(0xFF30B020), size = 30f, bold = true, align = TextAlign.CENTER)
        drawDosText("THE EXTRA-TERRESTRIAL IN THE PITS", x = W / 2f, y = px * 5f, color = Color(0xFF30B020), size = 12f, align = TextAlign.CENTER)
        if ((titleBlink * 1.5f).toInt() % 2 == 0) drawDosText("A TRIBUTE TO THE WORST GAME OF 1982", x = W / 2f, y = px * 7f, color = Color(0xFFD0C010), size = 9f, align = TextAlign.CENTER)
        // Disclaimer — always visible on the title (intro can be skipped once seen).
        val dis = Color(0xFF6FA070)
        drawDosText("Affectionate tribute to the 1982 game cartridge —", x = W / 2f, y = H - px * 8.2f, color = dis, size = 8f, align = TextAlign.CENTER)
        drawDosText("the most famous flop in video-game history.", x = W / 2f, y = H - px * 7.4f, color = dis, size = 8f, align = TextAlign.CENTER)
        drawDosText("Not affiliated with any film or rights-holder; the movie", x = W / 2f, y = H - px * 6.4f, color = dis, size = 8f, align = TextAlign.CENTER)
        drawDosText("appears only as one in-game artefact, by reference.", x = W / 2f, y = H - px * 5.6f, color = dis, size = 8f, align = TextAlign.CENTER)
        drawDosText("Any concern? elyssov@gmail.com — we'll resolve it.", x = W / 2f, y = H - px * 4.6f, color = dis, size = 8f, align = TextAlign.CENTER)
        if ((titleBlink * 2f).toInt() % 2 == 0) drawDosText("CLICK TO START", x = W / 2f, y = H - px * 3.2f, color = Color.White, size = 13f, align = TextAlign.CENTER)
        drawDosText("D-PAD: MOVE   FIRE: NECK / PHONE HOME", x = W / 2f, y = H - px * 1.8f, color = Color.Gray, size = 9f, align = TextAlign.CENTER)
        drawDosText("BEST  $best", x = W / 2f, y = H - px * 0.7f, color = Color(0xFFFFD600), size = 10f, bold = true, align = TextAlign.CENTER)
    }

    private fun drawDead(scope: DrawScope) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))
        drawDosText("GAME OVER", x = W / 2f, y = H * 0.2f, color = Color(0xFFC02020), size = 28f, bold = true, align = TextAlign.CENTER)
        drawDosText("E.T. PHONED HOME.", x = W / 2f, y = H * 0.35f, color = Color.Gray, size = 12f, align = TextAlign.CENTER)
        drawDosText("NOBODY ANSWERED.", x = W / 2f, y = H * 0.42f, color = Color.Gray, size = 12f, align = TextAlign.CENTER)
        val gx = cols / 2 - 2; val gy = (rows * 0.6f).toInt()
        drawCell(this, gx, gy, Color.Gray, 5, 1)
        drawCell(this, gx + 1, gy - 3, Color.Gray, 3, 3)
        drawCell(this, gx, gy - 2, Color.Gray, 5, 1)
        drawDosText("BURIED IN ALAMOGORDO NM", x = W / 2f, y = H * 0.78f, color = Color(0xFF6A4A20), size = 10f, align = TextAlign.CENTER)
        drawDosText("SCORE  $score", x = W / 2f, y = H * 0.84f, color = Color(0xFFD0C010), size = 14f, align = TextAlign.CENTER)
        drawDosText("PIT FALLS  $pitFalls", x = W / 2f, y = H * 0.89f, color = Color(0xFFC06820), size = 10f, align = TextAlign.CENTER)
        if ((titleBlink * 1.5f).toInt() % 2 == 0) drawDosText("CLICK TO RETRY (WHY?)", x = W / 2f, y = H * 0.95f, color = Color.White, size = 11f, align = TextAlign.CENTER)
    }

    private fun drawWin(scope: DrawScope) = with(scope) {
        drawRect(Color(0xFF1828A0), topLeft = Offset.Zero, size = Size(W, H))
        drawDosText("CONGRATULATIONS", x = W / 2f, y = H * 0.5f, color = Color(0xFF30B020), size = 22f, bold = true, align = TextAlign.CENTER)
        drawDosText("YOU BEAT THE WORST GAME", x = W / 2f, y = H * 0.6f, color = Color.White, size = 11f, align = TextAlign.CENTER)
        drawDosText("SCORE  $score", x = W / 2f, y = H * 0.7f, color = Color(0xFFD0C010), size = 14f, align = TextAlign.CENTER)
        if ((titleBlink * 1.5f).toInt() % 2 == 0) drawDosText("CLICK TO PLAY AGAIN (DONT)", x = W / 2f, y = H * 0.85f, color = Color.White, size = 11f, align = TextAlign.CENTER)
    }

    private var touchStart: Offset? = null
    private var lastTap = 0f

    /** Teletype advance: finish printing → next page → title. Shared by the
     *  lower-half tap and the FIRE/Space button. */
    private fun introAdvance(ctx: GameContext) {
        if (!introPageDone()) {
            // Snap the current page to fully printed.
            introElapsed = introPageChars() / introCharsPerSec + 0.001f
            introTickedChars = introPageChars()
        } else if (introPage < introPages.size - 1) {
            introPage++; introElapsed = 0f; introTickedChars = 0
        } else {
            ctx.settings.etIntroSeen = true
            state = State.TITLE
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (state == State.INTRO) {
            // Lower half = advance (or finish printing); upper half = go back.
            if (y >= H / 2f) {
                introAdvance(ctx)
            } else {
                if (introPage > 0) { introPage--; introElapsed = 0f; introTickedChars = 0 }
            }
            return
        }
        touchStart = Offset(x, y)
    }

    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        val s = touchStart ?: return
        touchStart = null
        val dx = x - s.x; val dy = y - s.y
        if (abs(dx) < 18f && abs(dy) < 18f) {
            // Double-tap = neck.
            val now = titleBlink
            if (now - lastTap < 0.4f) { extendNeck(ctx); lastTap = 0f; return }
            lastTap = now
            if (state == State.TITLE || state == State.DEAD || state == State.WIN) { newGame(); return }
            return
        }
        val sdx = if (abs(dx) > abs(dy)) sign(dx).toInt() else 0
        val sdy = if (abs(dy) >= abs(dx)) sign(dy).toInt() else 0
        moveDir(sdx, sdy, ctx)
    }
}
