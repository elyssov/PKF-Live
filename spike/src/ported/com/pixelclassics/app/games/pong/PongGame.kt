package com.pixelclassics.app.games.pong

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sign
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** 1:1 port of assets/games/pong.html. First to 11 wins. */
class PongGame : Game {
    override val id: String = "pong"
    override val titleResId: Int = R.string.game_pong
    override val backgroundColor: Color = Color.Black
    override val introText: String = PONG_INTRO_EN
    override fun introTextFor(lang: String): String = pickPongIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.BAR_WOOD
    override val helpLines: List<String> = listOf(
        "Drag your paddle (left side) up & down",
        "Right paddle is the CPU.  First to 11 wins.",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Веди свою ракетку (слева) мышью или стрелками",
        "Правая ракетка — CPU.  Игра до 11 очков.",
    ) else helpLines

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private val paddleH = 80f
    private val paddleW = 12f
    private val margin = 20f
    private val targetScore = 11

    private var bx = W / 2f
    private var by = H / 2f
    private var bvx = 5f
    private var bvy = 3f
    private val br = 8f

    private var p1y = H / 2f
    private var timeSec = 0f
    private var keyDir = 0
    private var keyFresh = -1f
    private var keyVel = 0f
    private var p2y = H / 2f
    private var p1score = 0
    private var p2score = 0

    private var started = false
    private var touchY: Float? = null
    private var winner = 0
    private var blink = 0f

    override fun init(ctx: GameContext) {
        p1y = H / 2f; p2y = H / 2f
        resetBall(1)
        p1score = 0; p2score = 0
        started = false; winner = 0
    }

    private fun resetBall(dir: Int) {
        bx = W / 2f; by = H / 2f
        val spd = 5f
        val ang = ((Random.nextFloat() - 0.5f) * (PI.toFloat() / 3f))
        bvx = cos(ang) * spd * dir
        bvy = sin(ang) * spd
    }

    override fun update(dt: Float, ctx: GameContext) {
        timeSec += dt
        blink = (blink + dt) % 1.0f
        if (!started || winner != 0) return

        val ph = paddleH / 2f

        // Original ran 60 fps fixed steps with px-per-frame speeds — scale by dt*60.
        // Paddles must use the SAME clock as the ball, otherwise the CPU paddle
        // is twice as fast on a 120 Hz screen and half as fast at 30 fps.
        val k = dt * 60f

        touchY?.let { target ->
            p1y += (target - p1y) * (0.15f * k).coerceAtMost(1f)
        }

        // Heavy-dial keyboard: accelerate while a direction is held (D-pad
        // repeats keep `keyFresh` warm), coast to a stop when released.
        val holdingKey = keyDir != 0 && timeSec - keyFresh < 0.16f
        if (holdingKey) {
            keyVel = (keyVel + keyDir * 2000f * dt).coerceIn(-520f, 520f)
        } else {
            keyVel *= (1f - 7f * dt).coerceAtLeast(0f)
            if (abs(keyVel) < 4f) keyVel = 0f
        }
        if (keyVel != 0f) p1y += keyVel * dt

        val aiSpeed = 2.5f
        val diff = by - p2y
        if (bx > W * 0.35f && abs(diff) > 15f) {
            if (Random.nextFloat() > 0.05f) {
                p2y += sign(diff) * min(aiSpeed * k, abs(diff) * (0.08f * k).coerceAtMost(1f))
            }
        }

        p1y = p1y.coerceIn(ph, H - ph)
        p2y = p2y.coerceIn(ph, H - ph)

        val prevBx = bx
        bx += bvx * k
        by += bvy * k

        if (by - br < 0f) { by = br; bvy = abs(bvy); ctx.sound.wallHit() }
        if (by + br > H) { by = H - br; bvy = -abs(bvy); ctx.sound.wallHit() }

        // Swept capture: compare against the PREVIOUS frame position too, so a
        // big step (low fps / hitched frame) can't carry the ball through the
        // 12px paddle band between two tests.
        val lx = margin + paddleW
        if (bvx < 0f && bx - br < lx && prevBx - br >= margin &&
            by > p1y - ph && by < p1y + ph
        ) {
            bx = lx + br
            val hitPos = (by - p1y) / ph
            val angle = hitPos * PI.toFloat() / 4f
            val spd = min(sqrt(bvx * bvx + bvy * bvy) + 0.2f, 9f)
            bvx = cos(angle) * spd
            bvy = sin(angle) * spd
            if (bvx < 1f) bvx = 1f
            ctx.sound.paddleHit()
        }

        val rx = W - margin - paddleW
        if (bvx > 0f && bx + br > rx && prevBx + br <= W - margin &&
            by > p2y - ph && by < p2y + ph
        ) {
            bx = rx - br
            val hitPos = (by - p2y) / ph
            val angle = PI.toFloat() - hitPos * PI.toFloat() / 4f
            val spd = min(sqrt(bvx * bvx + bvy * bvy) + 0.2f, 9f)
            bvx = -abs(cos(angle) * spd)
            bvy = sin(angle) * spd
            ctx.sound.paddleHit()
        }

        if (bx < 0f) { p2score++; ctx.sound.score(); resetBall(1) }
        if (bx > W) { p1score++; ctx.sound.score(); resetBall(-1) }

        if (p1score >= targetScore) { winner = 1; ctx.sound.win(); ctx.scores.set(id, p1score) }
        if (p2score >= targetScore) { winner = 2; ctx.sound.gameOver() }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Atari B&W TV: pure black field, white paddles, big chunky digits.
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))

        // Outer frame (thin white band — Atari "TV" border).
        val frame = 4f
        drawRect(Color(0xFFCCCCCC), topLeft = Offset(0f, 0f), size = Size(W, frame))
        drawRect(Color(0xFFCCCCCC), topLeft = Offset(0f, H - frame), size = Size(W, frame))

        if (!started) {
            drawAtariDigits("PONG", W / 2f - 110f, H * 0.18f, 12f, Color.White)
            drawDosText(
                ctx.tr("FIRST TO 11 WINS", "ИГРА ДО 11 ОЧКОВ"),
                x = W / 2f, y = H * 0.40f,
                color = Color(0xFFCCCCCC), size = 18f, bold = true, align = TextAlign.CENTER,
            )
            drawDosText(
                ctx.tr("SLIDE TO MOVE PADDLE", "ВЕДИ РАКЕТКУ МЫШЬЮ"),
                x = W / 2f, y = H * 0.50f,
                color = Color(0xFF888888), size = 14f, bold = true, align = TextAlign.CENTER,
            )
            if (blink < 0.5f) {
                drawDosText(
                    ctx.tr("CLICK TO START", "КЛИК — СТАРТ"),
                    x = W / 2f, y = H * 0.72f,
                    color = Color.White, size = 22f, bold = true, align = TextAlign.CENTER,
                )
            }
            return@with
        }

        // Center dashed line — chunky white blocks, not anti-aliased CSS dashes.
        var dy = frame
        while (dy < H - frame) {
            drawRect(Color(0xFFCCCCCC), topLeft = Offset(W / 2f - 3f, dy), size = Size(6f, 14f))
            dy += 28f
        }

        // Paddles — pure white, pixel-aligned.
        val ph = paddleH / 2f
        drawRect(Color.White, topLeft = Offset(margin, p1y - ph), size = Size(paddleW, paddleH))
        drawRect(Color.White, topLeft = Offset(W - margin - paddleW, p2y - ph), size = Size(paddleW, paddleH))

        // Ball — solid white square.
        val bs = br * 2f
        drawRect(Color.White, topLeft = Offset(bx - br, by - br), size = Size(bs, bs))

        // Big block-segment scores.
        drawAtariDigits("$p1score", W * 0.30f - digitWidth("$p1score") / 2f, frame + 18f, 10f, Color.White)
        drawAtariDigits("$p2score", W * 0.70f - digitWidth("$p2score") / 2f, frame + 18f, 10f, Color.White)

        if (winner != 0) {
            drawRect(Color(0xDD000000), topLeft = Offset(0f, 0f), size = Size(W, H))
            val winText = if (winner == 1) "YOU WIN" else "GAME OVER"
            drawAtariDigits(winText, W / 2f - winText.length * 6.5f * 8f / 2f, H / 2f - 50f, 6.5f, Color.White)
            drawDosText(ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"), x = W / 2f, y = H / 2f + 40f, color = Color(0xFFCCCCCC), size = 18f, bold = true, align = TextAlign.CENTER)
        }
    }

    /** 7-segment / chunky digit + letter renderer in pure white squares. */
    private val glyph: Map<Char, Array<String>> = mapOf(
        '0' to arrayOf("XXXX", "X..X", "X..X", "X..X", "X..X", "X..X", "XXXX"),
        '1' to arrayOf(".XX.", "X.X.", "..X.", "..X.", "..X.", "..X.", "XXXX"),
        '2' to arrayOf("XXXX", "...X", "...X", "XXXX", "X...", "X...", "XXXX"),
        '3' to arrayOf("XXXX", "...X", "...X", "XXXX", "...X", "...X", "XXXX"),
        '4' to arrayOf("X..X", "X..X", "X..X", "XXXX", "...X", "...X", "...X"),
        '5' to arrayOf("XXXX", "X...", "X...", "XXXX", "...X", "...X", "XXXX"),
        '6' to arrayOf("XXXX", "X...", "X...", "XXXX", "X..X", "X..X", "XXXX"),
        '7' to arrayOf("XXXX", "...X", "...X", "..X.", "..X.", ".X..", ".X.."),
        '8' to arrayOf("XXXX", "X..X", "X..X", "XXXX", "X..X", "X..X", "XXXX"),
        '9' to arrayOf("XXXX", "X..X", "X..X", "XXXX", "...X", "...X", "XXXX"),
        'A' to arrayOf("XXXX", "X..X", "X..X", "XXXX", "X..X", "X..X", "X..X"),
        'E' to arrayOf("XXXX", "X...", "X...", "XXXX", "X...", "X...", "XXXX"),
        'G' to arrayOf("XXXX", "X...", "X...", "X.XX", "X..X", "X..X", "XXXX"),
        'I' to arrayOf("XXXX", "..X.", "..X.", "..X.", "..X.", "..X.", "XXXX"),
        'M' to arrayOf("X..X", "XXXX", "XXXX", "X..X", "X..X", "X..X", "X..X"),
        'N' to arrayOf("X..X", "XX.X", "XX.X", "X.XX", "X.XX", "X..X", "X..X"),
        'O' to arrayOf("XXXX", "X..X", "X..X", "X..X", "X..X", "X..X", "XXXX"),
        'P' to arrayOf("XXXX", "X..X", "X..X", "XXXX", "X...", "X...", "X..."),
        'R' to arrayOf("XXXX", "X..X", "X..X", "XXXX", "XX..", "X.X.", "X..X"),
        'S' to arrayOf("XXXX", "X...", "X...", "XXXX", "...X", "...X", "XXXX"),
        'T' to arrayOf("XXXX", "..X.", "..X.", "..X.", "..X.", "..X.", "..X."),
        'U' to arrayOf("X..X", "X..X", "X..X", "X..X", "X..X", "X..X", "XXXX"),
        'V' to arrayOf("X..X", "X..X", "X..X", "X..X", "X..X", ".XX.", "..X."),
        'W' to arrayOf("X..X", "X..X", "X..X", "X..X", "XXXX", "XXXX", "X..X"),
        'Y' to arrayOf("X..X", "X..X", "X..X", "XXXX", "..X.", "..X.", "..X."),
        ' ' to arrayOf("....", "....", "....", "....", "....", "....", "...."),
    )

    private fun digitWidth(s: String, pxSize: Float = 10f): Float = s.length * pxSize * 5f

    private fun DrawScope.drawAtariDigits(s: String, x0: Float, y0: Float, pxSize: Float, color: Color) {
        var x = x0
        for (ch in s) {
            val g = glyph[ch.uppercaseChar()] ?: continue
            for (r in g.indices) for (c in g[r].indices) {
                if (g[r][c] == 'X') {
                    drawRect(color, topLeft = Offset(x + c * pxSize, y0 + r * pxSize), size = Size(pxSize, pxSize))
                }
            }
            x += pxSize * 5f
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (!started) { started = true; return }
        if (winner != 0) { init(ctx); started = true; return }
        touchY = y
    }

    override fun onPointerMove(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (started && winner == 0) touchY = y
    }

    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        // keep paddle where it is until the next touch
    }

    // ── 1972 controls (Юджин, ревью 17.07) ──────────────────────────────
    // Mouse hover = the potentiometer knob: absolute, instant (desktop asks
    // for hover events via hoverPointer). Arrows = the "heavy analogue dial"
    // fallback: the paddle accelerates while held and coasts to a stop.
    override val hoverPointer: Boolean get() = true

    // Android 31.07: the touch potentiometer. The desktop hover contract has
    // no meaning on a phone, and drag-on-glass hides the tiny museum screen
    // under the finger — so the gutter joystick (the heavy dial: onDirection
    // + inertia) is the primary control, and drag still works ANYWHERE on
    // the canvas, not just inside the photographed tube.
    override val controls: GameControls = GameControls.DPAD
    override val wholeCanvasTouch: Boolean get() = true

    override fun onFire(ctx: GameContext) {
        if (!started) { started = true; return }
        if (winner != 0) { init(ctx); started = true }
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (dy != 0) { keyDir = dy; keyFresh = timeSec; touchY = null }
    }
}
