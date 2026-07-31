package com.pixelclassics.app.games.towerclimb

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/** Vertical construction-yard platformer: girders, ladders, barrels, climb. */
class TowerClimbGame : Game {
    override val id: String = "tower_climb"
    override val titleResId: Int = R.string.game_tower_climb
    override val backgroundColor: Color = Color(0xFF080812)
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    override val introText: String = TOWER_CLIMB_INTRO_EN
    override fun introTextFor(lang: String): String = pickTowerClimbIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PARCHMENT
    override val helpLines: List<String> = listOf(
        "D-pad — run / climb ladders",
        "FIRE — jump",
        "Reach the trapped worker at the top",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — бег / лестницы",
        "ОГОНЬ — прыжок",
        "Доберись до запертого рабочего наверху",
    ) else helpLines

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private data class Girder(val y: Float, val slope: Float, val x1: Float = 46f, val x2: Float = 594f) {
        fun yAt(x: Float): Float = y + slope * (x - 320f)
    }
    private data class Ladder(val x: Float, val lower: Int, val upper: Int)
    private data class Barrel(var x: Float, var platform: Int, var dir: Int, var spin: Float = 0f)

    private val girders = listOf(
        Girder(82f, 0.055f),
        Girder(142f, -0.050f),
        Girder(202f, 0.055f),
        Girder(262f, -0.050f),
        Girder(322f, 0.055f),
        Girder(400f, -0.025f, 30f, 610f),
    )
    private val ladders = listOf(
        Ladder(108f, 5, 4),
        Ladder(510f, 4, 3),
        Ladder(158f, 3, 2),
        Ladder(486f, 2, 1),
        Ladder(224f, 1, 0),
    )

    private var px = 72f
    private var py = 0f
    private var vx = 0f
    private var vy = 0f
    private var onGround = true
    private var onLadder: Ladder? = null
    private var climbLock = 0f
    private var face = 1
    private var heldDx = 0
    private var heldDy = 0
    private var inputFresh = 0f
    private var time = 0f

    private val barrels = mutableListOf<Barrel>()
    private var barrelTimer = 0f
    private var score = 0
    private var best = 0
    private var level = 1
    private var lives = 3
    private var state = State.TITLE
    private var msg = ""
    private var msgT = 0f
    // When we entered TITLE/OVER/WON — inputs restart the game only after a
    // short cooldown, so a direction held at the moment of death can't skip
    // the GAME OVER screen.
    private var stateAt = 0f

    private enum class State { TITLE, PLAY, WON, OVER }

    private var lastCtx: GameContext? = null

    override fun init(ctx: GameContext) {
        lastCtx = ctx
        best = ctx.scores.get(id)
        state = State.TITLE
    }

    private fun restart() {
        score = 0
        lives = 3
        level = 1
        startLevel()
    }

    private fun startLevel() {
        px = 72f
        py = girders.last().yAt(px)
        vx = 0f
        vy = 0f
        onGround = true
        onLadder = null
        barrels.clear()
        barrelTimer = 1.0f
        msg = lastCtx?.tr("CLIMB!", "НАВЕРХ!") ?: "CLIMB!"
        msgT = 1.2f
        state = State.PLAY
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        heldDx = dx
        heldDy = dy
        inputFresh = time
        // Ignore D-pad release events (0,0) and require the screen to have
        // been visible for a beat before a press restarts.
        if ((dx != 0 || dy != 0) && (state == State.TITLE || state == State.OVER) && time - stateAt > 0.6f) restart()
    }

    override fun onFire(ctx: GameContext) {
        when (state) {
            State.TITLE, State.OVER -> if (time - stateAt > 0.6f) restart()
            State.WON -> { level++; startLevel() }
            State.PLAY -> if (onGround && onLadder == null) {
                vy = -255f
                onGround = false
                ctx.sound.move()
            }
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (msgT > 0f) msgT -= dt
        if (state != State.PLAY) return

        lastCtx = ctx
        val liveInput = time - inputFresh < 0.18f
        val dx = if (liveInput) heldDx else 0
        val dy = if (liveInput) heldDy else 0
        if (dx != 0) face = dx

        updatePlayer(dt, dx, dy)
        updateBarrels(dt, ctx)
        checkGoal(ctx)
    }

    private fun updatePlayer(dt: Float, dx: Int, dy: Int) {
        if (climbLock > 0f) climbLock -= dt

        val ladder = nearestLadder()
        if (onLadder == null && dy != 0 && ladder != null && climbLock <= 0f) {
            onLadder = ladder
            px = ladder.x
            vx = 0f
            vy = 0f
            onGround = false
        }

        onLadder?.let { l ->
            val top = girders[l.upper].yAt(l.x)
            val bottom = girders[l.lower].yAt(l.x)
            py += dy * 135f * dt
            px = l.x
            if (py <= top) {
                py = top
                onLadder = null
                onGround = true
                climbLock = 0.18f
            } else if (py >= bottom) {
                py = bottom
                onLadder = null
                onGround = true
                climbLock = 0.18f
            }
            return
        }

        vx = dx * 150f
        px = (px + vx * dt).coerceIn(36f, W - 36f)
        vy += 620f * dt
        py += vy * dt

        val platform = platformUnder(px, py)
        if (platform != null && vy >= 0f && py >= platform.yAt(px) - 4f) {
            py = platform.yAt(px)
            vy = 0f
            onGround = true
        } else {
            onGround = false
        }
    }

    private fun nearestLadder(): Ladder? {
        for (l in ladders) {
            if (abs(px - l.x) > 18f) continue
            val top = girders[l.upper].yAt(l.x)
            val bottom = girders[l.lower].yAt(l.x)
            if (py in (top - 10f)..(bottom + 10f)) return l
        }
        return null
    }

    private fun platformUnder(x: Float, y: Float): Girder? {
        return girders.firstOrNull { g ->
            x >= g.x1 - 10f && x <= g.x2 + 10f && y <= g.yAt(x) + 18f && y >= g.yAt(x) - 32f
        }
    }

    private fun updateBarrels(dt: Float, ctx: GameContext) {
        barrelTimer -= dt
        val spawnEvery = (2.25f - level * 0.18f).coerceAtLeast(1.15f)
        if (barrelTimer <= 0f) {
            barrelTimer = spawnEvery
            barrels += Barrel(112f, 0, 1)
            ctx.sound.click()
        }

        val speed = 72f + level * 10f
        val it = barrels.iterator()
        while (it.hasNext()) {
            val b = it.next()
            val g = girders[b.platform]
            b.x += b.dir * speed * dt
            b.spin += dt * 8f

            val canDrop = ladders.firstOrNull { l -> l.upper == b.platform && abs(b.x - l.x) < 5f }
            if (canDrop != null && Random.nextFloat() < 0.018f + level * 0.002f) {
                b.platform = canDrop.lower
                b.x = canDrop.x
                b.dir = if (girders[b.platform].slope < 0f) -1 else 1
            } else if (b.x < g.x1 || b.x > g.x2) {
                b.platform++
                if (b.platform >= girders.size) {
                    it.remove()
                    continue
                }
                val ng = girders[b.platform]
                b.x = b.x.coerceIn(ng.x1 + 8f, ng.x2 - 8f)
                b.dir = if (ng.slope < 0f) -1 else 1
            }

            val by = girders[b.platform].yAt(b.x) - 10f
            if (abs(px - b.x) < 20f && abs(py - by) < 28f) {
                loseLife(ctx)
                return
            }
        }
    }

    private fun loseLife(ctx: GameContext) {
        lives--
        ctx.sound.die()
        if (lives <= 0) {
            state = State.OVER
            stateAt = time
            if (score > best) { best = score; ctx.scores.set(id, score) }
        } else {
            px = 72f
            py = girders.last().yAt(px)
            vx = 0f
            vy = 0f
            onLadder = null
            barrels.clear()
            barrelTimer = 1.1f
            msg = ctx.tr("TRY AGAIN", "ЕЩЁ РАЗ")
            msgT = 1.2f
        }
    }

    private fun checkGoal(ctx: GameContext) {
        if (py < 96f && px > 435f) {
            score += 1000 + level * 250 + lives * 100
            if (score > best) { best = score; ctx.scores.set(id, score) }
            state = State.WON
            stateAt = time
            ctx.sound.win()
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color(0xFF080812), topLeft = Offset.Zero, size = Size(W, H))
        drawSky()
        drawCrane()
        drawStructure()
        drawHazards()
        drawHero()
        drawHud()

        when (state) {
            State.TITLE -> drawCenterCard("IRON CLIMB", ctx.tr("FIRE TO START", "ОГОНЬ — СТАРТ"), Color(0xFFFFD060))
            State.WON -> drawCenterCard(ctx.tr("RESCUED!", "СПАСЁН!"), ctx.tr("FIRE FOR NEXT YARD", "ОГОНЬ — СЛЕДУЮЩИЙ ДВОР"), Color(0xFF80FF80))
            State.OVER -> drawCenterCard("GAME OVER", ctx.tr("FIRE TO RETRY", "ОГОНЬ — ЗАНОВО"), Color(0xFFFF6060))
            State.PLAY -> if (msgT > 0f) drawDosText(msg, x = W / 2f, y = 235f, color = Color.White, size = 18f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun DrawScope.drawSky() {
        for (i in 0 until 70) {
            val x = ((i * 97) % W.toInt()).toFloat()
            val y = ((i * 53) % 300).toFloat() + 10f
            drawRect(Color(0xFF3A3A58), topLeft = Offset(x, y), size = Size(2f, 2f))
        }
        drawRect(Color(0xFF101026), topLeft = Offset(0f, 420f), size = Size(W, 60f))
        drawRect(Color(0xFF181838), topLeft = Offset(20f, 350f), size = Size(52f, 70f))
        drawRect(Color(0xFF181838), topLeft = Offset(540f, 330f), size = Size(64f, 90f))
    }

    private fun DrawScope.drawCrane() {
        drawRect(Color(0xFF604020), topLeft = Offset(64f, 34f), size = Size(92f, 42f))
        drawRect(Color(0xFFC07818), topLeft = Offset(74f, 22f), size = Size(72f, 16f))
        drawCircle(Color(0xFF202020), radius = 13f, center = Offset(91f, 78f))
        drawCircle(Color(0xFF202020), radius = 13f, center = Offset(131f, 78f))
        drawRect(Color(0xFFE0A020), topLeft = Offset(98f, 42f), size = Size(28f, 20f))
        drawDosText("CLANG", x = 110f, y = 20f, color = Color(0xFFFFD060), size = 9f, bold = true, align = TextAlign.CENTER)
        drawRect(Color(0xFFFFC040), topLeft = Offset(470f, 50f), size = Size(28f, 36f))
        drawRect(Color(0xFF202020), topLeft = Offset(478f, 60f), size = Size(12f, 12f))
        drawDosText("HELP", x = 484f, y = 45f, color = Color.White, size = 10f, bold = true, align = TextAlign.CENTER)
    }

    private fun DrawScope.drawStructure() {
        for (g in girders) {
            val start = Offset(g.x1, g.yAt(g.x1))
            val end = Offset(g.x2, g.yAt(g.x2))
            drawLine(Color(0xFF101010), start = start + Offset(0f, 5f), end = end + Offset(0f, 5f), strokeWidth = 13f)
            drawLine(Color(0xFFE05038), start = start, end = end, strokeWidth = 11f)
            drawLine(Color(0xFFFFA050), start = start + Offset(0f, -3f), end = end + Offset(0f, -3f), strokeWidth = 2f)
            var x = g.x1 + 24f
            while (x < g.x2) {
                val y = g.yAt(x)
                drawLine(Color(0xFF802018), Offset(x - 10f, y + 4f), Offset(x + 10f, y - 4f), strokeWidth = 2f)
                x += 34f
            }
        }
        for (l in ladders) {
            val top = girders[l.upper].yAt(l.x)
            val bottom = girders[l.lower].yAt(l.x)
            drawLine(Color(0xFFFFD060), Offset(l.x - 9f, top), Offset(l.x - 9f, bottom), strokeWidth = 4f)
            drawLine(Color(0xFFFFD060), Offset(l.x + 9f, top), Offset(l.x + 9f, bottom), strokeWidth = 4f)
            var y = top + 10f
            while (y < bottom - 5f) {
                drawLine(Color(0xFFFFD060), Offset(l.x - 12f, y), Offset(l.x + 12f, y), strokeWidth = 3f)
                y += 14f
            }
        }
    }

    private fun DrawScope.drawHazards() {
        for (b in barrels) {
            val y = girders[b.platform].yAt(b.x) - 10f
            drawCircle(Color(0xFF301000), radius = 13f, center = Offset(b.x + 2f, y + 3f))
            drawCircle(Color(0xFFC07018), radius = 12f, center = Offset(b.x, y))
            drawCircle(Color(0xFF603010), radius = 6f, center = Offset(b.x, y))
            val spoke = sin(b.spin) * 8f
            drawLine(Color(0xFFFFC060), Offset(b.x - spoke, y - 9f), Offset(b.x + spoke, y + 9f), strokeWidth = 2f)
            drawLine(Color(0xFFFFC060), Offset(b.x - 9f, y + spoke), Offset(b.x + 9f, y - spoke), strokeWidth = 2f)
        }
    }

    private fun DrawScope.drawHero() {
        val x = px
        val y = py
        val walk = if (onGround) sin(time * 12f) * 3f else 0f
        drawRect(Color(0xFF101010), topLeft = Offset(x - 9f, y - 32f), size = Size(20f, 34f))
        drawRect(Color(0xFF3060D0), topLeft = Offset(x - 8f, y - 26f), size = Size(16f, 20f))
        drawRect(Color(0xFFFFD0A0), topLeft = Offset(x - 7f, y - 38f), size = Size(14f, 12f))
        drawRect(Color(0xFFFFD040), topLeft = Offset(x - 10f, y - 43f), size = Size(20f, 6f))
        drawRect(Color(0xFF804020), topLeft = Offset(x - 12f, y - 18f), size = Size(5f, 13f))
        drawRect(Color(0xFF804020), topLeft = Offset(x + 7f, y - 18f), size = Size(5f, 13f))
        drawRect(Color(0xFF202020), topLeft = Offset(x - 8f + walk, y - 4f), size = Size(6f, 6f))
        drawRect(Color(0xFF202020), topLeft = Offset(x + 2f - walk, y - 4f), size = Size(6f, 6f))
        val toolX = if (face > 0) x + 9f else x - 22f
        drawRect(Color(0xFFC0C0C0), topLeft = Offset(toolX, y - 27f), size = Size(13f, 4f))
    }

    private fun DrawScope.drawHud() {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, 28f))
        drawDosText("SCORE $score", x = 12f, y = 21f, color = Color.White, size = 13f, bold = true)
        drawDosText("YARD $level", x = W / 2f, y = 21f, color = Color(0xFFFFD060), size = 13f, bold = true, align = TextAlign.CENTER)
        drawDosText("LIVES $lives", x = W - 12f, y = 21f, color = Color.White, size = 13f, bold = true, align = TextAlign.RIGHT)
        drawDosText("BEST $best", x = W / 2f, y = 45f, color = Color(0xFF8080FF), size = 10f, align = TextAlign.CENTER)
    }

    private fun DrawScope.drawCenterCard(title: String, hint: String, color: Color) {
        drawRect(Color(0xCC000000), topLeft = Offset(120f, 170f), size = Size(400f, 130f))
        drawRect(color, topLeft = Offset(120f, 170f), size = Size(400f, 130f), style = Stroke(3f))
        drawDosText(title, x = W / 2f, y = 220f, color = color, size = 32f, bold = true, align = TextAlign.CENTER)
        drawDosText(hint, x = W / 2f, y = 258f, color = Color.White, size = 14f, align = TextAlign.CENTER)
    }
}
