package com.pixelclassics.app.games.asteroids

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

/**
 * ASTEROIDS (Atari 1979) — vector-line clone.
 *
 * Wraparound space, rotate-and-thrust ship, three asteroid sizes that
 * split on hit, optional hyperspace teleport (risky), UFO every now
 * and then. Pure white lines on black, no fills. Three on-screen
 * buttons: ◀ THRUST ▶ + FIRE — Atari arcade had separate L/R, thrust,
 * fire, hyperspace; we collapse to four.
 */
class AsteroidsGame : Game {
    override val id: String = "asteroids"
    override val titleResId: Int = R.string.game_asteroids
    override val backgroundColor: Color = Color.Black
    override val introText: String = ASTEROIDS_INTRO_EN
    override fun introTextFor(lang: String): String = pickAsteroidsIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.VECTOR_WHITE_ON_BLACK
    override val helpLines: List<String> = listOf(
        "◀ ▶ — rotate · ▲ — thrust · ● FIRE — shoot",
        "Big rocks split into smaller, smaller, dust.  Watch for UFOs.",
        "Screen wraps around all four edges",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "◀ ▶ — поворот · ▲ — тяга · ● ОГОНЬ — стрельба",
        "Большие глыбы колются на меньшие, меньшие, пыль.  Берегись НЛО.",
        "Экран заворачивает со всех четырёх краёв",
    ) else helpLines

    private val W = 720f
    private val H = 540f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H
    // Controls in the side gutters: left D-pad rotates (◀▶) and thrusts (▲);
    // down (▼) = hyperspace jump; right FIRE shoots.
    override val controls: GameControls = GameControls.DPAD_AND_FIRE

    // D-pad sends auto-repeat pulses while held with no release event, so we
    // keep a freshness window and derive held-state in update().
    private var dpadX = 0
    private var dpadY = 0
    private var dpadFreshT = -1f

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        dpadX = dx; dpadY = dy; dpadFreshT = time
        // Down no longer teleports — random hyperspace into an asteroid was a
        // death trap on a casually-pressed direction. Down is a no-op (brake-ish).
    }

    override fun onFire(ctx: GameContext) {
        if (stage == Stage.PLAY && !gameOver && respawnTimer <= 0f) fireBullet(ctx)
    }

    private data class Roid(var x: Float, var y: Float, var vx: Float, var vy: Float,
                            var size: Int, val shape: FloatArray)  // shape: 10 radii for outline
    private data class Bullet(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float)
    private data class Particle(var x: Float, var y: Float, var vx: Float, var vy: Float, var life: Float)

    private val roids = mutableListOf<Roid>()
    private val bullets = mutableListOf<Bullet>()
    private val particles = mutableListOf<Particle>()

    private var shipX = W / 2f
    private var shipY = H / 2f
    private var shipVx = 0f
    private var shipVy = 0f
    private var shipAng = -PI.toFloat() / 2f
    private var leftHeld = false
    private var rightHeld = false
    private var thrustOn = false
    private var fireCd = 0f
    private var hyperCd = 0f

    private var lives = 3
    private var score = 0
    private var best = 0
    private var level = 1
    private var respawnTimer = 0f
    private var invulnerable = 0f
    private var gameOver = false
    private var time = 0f
    private enum class Stage { TITLE, PLAY }
    private var stage = Stage.TITLE

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id); stage = Stage.TITLE
    }

    private fun startGame() {
        lives = 3; score = 0; level = 1; gameOver = false
        spawnLevel(); respawnShip(); stage = Stage.PLAY
    }

    private fun spawnLevel() {
        roids.clear(); bullets.clear(); particles.clear()
        val count = 4 + level
        for (i in 0 until count) {
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val sp = 20f + level * 5f + Random.nextFloat() * 30f
            val rx = if (Random.nextBoolean()) Random.nextFloat() * W else 0f
            val ry = if (rx == 0f) Random.nextFloat() * H else 0f
            roids += Roid(rx, ry, cos(ang) * sp, sin(ang) * sp, 3, randomShape())
        }
    }
    private fun randomShape(): FloatArray = FloatArray(10) { 0.7f + Random.nextFloat() * 0.5f }

    private fun respawnShip() {
        shipX = W / 2f; shipY = H / 2f; shipVx = 0f; shipVy = 0f; shipAng = -PI.toFloat() / 2f
        invulnerable = 2.5f
    }

    private fun hyperspace() {
        if (hyperCd > 0f) return
        hyperCd = 3f
        shipX = Random.nextFloat() * W; shipY = Random.nextFloat() * H
        shipVx = 0f; shipVy = 0f
        // 1-in-8 chance of malfunction → instant death.
        if (Random.nextInt(8) == 0) destroyShip(null)
    }

    private fun fireBullet(ctx: GameContext) {
        if (fireCd > 0f) return
        if (bullets.size >= 4) return
        val sp = 360f
        bullets += Bullet(
            shipX + cos(shipAng) * 14f,
            shipY + sin(shipAng) * 14f,
            cos(shipAng) * sp + shipVx,
            sin(shipAng) * sp + shipVy,
            life = 0f,
        )
        fireCd = 0.20f
        ctx.sound.shoot()
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (stage != Stage.PLAY || gameOver) return

        if (fireCd > 0f) fireCd -= dt
        if (hyperCd > 0f) hyperCd -= dt
        if (invulnerable > 0f) invulnerable -= dt

        // Derive held-state from the side D-pad (fresh = pressed within ~160ms).
        val ctrlActive = time - dpadFreshT < 0.16f
        leftHeld = ctrlActive && dpadX < 0
        rightHeld = ctrlActive && dpadX > 0
        thrustOn = ctrlActive && dpadY < 0
        if (leftHeld) shipAng -= 3.0f * dt
        if (rightHeld) shipAng += 3.0f * dt
        if (thrustOn) {
            shipVx += cos(shipAng) * 160f * dt
            shipVy += sin(shipAng) * 160f * dt
            if (Random.nextFloat() < 0.6f) particles += Particle(
                shipX - cos(shipAng) * 10f, shipY - sin(shipAng) * 10f,
                -cos(shipAng) * 100f + Random.nextFloat() * 40f - 20f,
                -sin(shipAng) * 100f + Random.nextFloat() * 40f - 20f, 0f,
            )
        }
        // Drag.
        shipVx *= 0.995f; shipVy *= 0.995f
        shipX = (shipX + shipVx * dt).wrap(W)
        shipY = (shipY + shipVy * dt).wrap(H)

        // Roids.
        for (r in roids) {
            r.x = (r.x + r.vx * dt).wrap(W); r.y = (r.y + r.vy * dt).wrap(H)
        }

        // Bullets.
        val bIt = bullets.iterator()
        while (bIt.hasNext()) {
            val b = bIt.next()
            b.x = (b.x + b.vx * dt).wrap(W); b.y = (b.y + b.vy * dt).wrap(H)
            b.life += dt
            if (b.life > 0.9f) { bIt.remove(); continue }
        }

        // Bullet-vs-roid (index-pass to avoid CME).
        val deadB = mutableSetOf<Int>()
        val splitOuts = mutableListOf<Roid>()
        for (ri in roids.indices.reversed()) {
            val r = roids[ri]
            val radius = roidRadius(r)
            for (bi in bullets.indices) {
                if (bi in deadB) continue
                val b = bullets[bi]
                if (hypot(b.x - r.x, b.y - r.y) < radius) {
                    deadB += bi
                    score += when (r.size) { 3 -> 20; 2 -> 50; else -> 100 }
                    ctx.sound.explosion()
                    if (r.size > 1) {
                        for (k in 0 until 2) {
                            val ang = Random.nextFloat() * 2f * PI.toFloat()
                            val sp = 40f + Random.nextFloat() * 40f
                            splitOuts += Roid(r.x, r.y, cos(ang) * sp, sin(ang) * sp, r.size - 1, randomShape())
                        }
                    }
                    // Debris particles.
                    for (k in 0 until 8) {
                        val ang = Random.nextFloat() * 2f * PI.toFloat()
                        val sp = 60f + Random.nextFloat() * 80f
                        particles += Particle(r.x, r.y, cos(ang) * sp, sin(ang) * sp, 0f)
                    }
                    roids.removeAt(ri); break
                }
            }
        }
        roids.addAll(splitOuts)
        if (deadB.isNotEmpty()) {
            val survivors = bullets.filterIndexed { i, _ -> i !in deadB }
            bullets.clear(); bullets.addAll(survivors)
        }

        // Roid-vs-ship.
        if (invulnerable <= 0f && respawnTimer <= 0f) {
            for (r in roids) {
                if (hypot(shipX - r.x, shipY - r.y) < roidRadius(r) + 10f) {
                    destroyShip(ctx); break
                }
            }
        }

        // Particles.
        val pIt = particles.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            p.x += p.vx * dt; p.y += p.vy * dt
            p.life += dt
            if (p.life > 0.8f) pIt.remove()
        }

        if (respawnTimer > 0f) {
            respawnTimer -= dt
            if (respawnTimer <= 0f && !gameOver) respawnShip()
        }
        if (roids.isEmpty()) { level++; spawnLevel() }
    }

    private fun destroyShip(ctx: GameContext?) {
        ctx?.sound?.bigExplosion()
        for (k in 0 until 16) {
            val ang = Random.nextFloat() * 2f * PI.toFloat()
            val sp = 80f + Random.nextFloat() * 120f
            particles += Particle(shipX, shipY, cos(ang) * sp, sin(ang) * sp, 0f)
        }
        lives--
        if (lives <= 0) {
            gameOver = true
            if (score > best) { best = score; ctx?.scores?.set(id, score) }
        } else respawnTimer = 1.5f
    }

    private fun roidRadius(r: Roid): Float = when (r.size) { 3 -> 36f; 2 -> 22f; else -> 12f }

    /** Vector-tube stroke: wide translucent halo under a thin bright core. */
    private fun DrawScope.glowLine(a: Offset, b: Offset, color: Color = Color.White) {
        drawLine(color.copy(alpha = 0.22f), a, b, strokeWidth = 4.5f)
        drawLine(color, a, b, strokeWidth = 1.2f)
    }

    private fun Float.wrap(max: Float): Float {
        var v = this
        if (v < 0f) v += max
        if (v >= max) v -= max
        return v
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))
        if (stage == Stage.TITLE) {
            drawDosText("ROCK STORM", x = W / 2f, y = H * 0.30f, color = Color.White, size = 38f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("ROTATE  ◀ ▶     THRUST  ▲", "ПОВОРОТ  ◀ ▶     ТЯГА  ▲"), x = W / 2f, y = H * 0.52f, color = Color.White, size = 13f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("FIRE  ●  — shoot the rocks", "ОГОНЬ  ●  — расстреливай глыбы"), x = W / 2f, y = H * 0.59f, color = Color(0xFFAAAAAA), size = 12f, align = TextAlign.CENTER)
            if ((time * 2f).toInt() % 2 == 0)
                drawDosText(ctx.tr("CLICK TO START", "КЛИК — СТАРТ"), x = W / 2f, y = H * 0.80f, color = Color.White, size = 18f, bold = true, align = TextAlign.CENTER)
            drawDosText("BEST $best", x = W / 2f, y = H * 0.92f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.CENTER)
            return@with
        }

        // Particles.
        for (p in particles) {
            val a = (1f - p.life / 0.8f).coerceIn(0f, 1f)
            drawRect(Color.White.copy(alpha = a), topLeft = Offset(p.x - 1f, p.y - 1f), size = Size(2f, 2f))
        }

        // Bullets — the vector gun burned dots twice as bright.
        for (b in bullets) {
            drawRect(Color.White.copy(alpha = 0.35f), topLeft = Offset(b.x - 2.5f, b.y - 2.5f), size = Size(5f, 5f))
            drawRect(Color.White, topLeft = Offset(b.x - 1f, b.y - 1f), size = Size(2f, 2f))
        }

        // Asteroids — 10-vertex jagged outline. Every stroke is drawn twice:
        // a wide translucent halo + a thin bright core, plus hot vertex dots —
        // the Quadrascan vector-tube look (Юджин: «воспроизвести ТОТ экран»).
        for (r in roids) {
            val baseR = roidRadius(r)
            val path = Path()
            for (i in 0 until 10) {
                val ang = i * (2f * PI.toFloat() / 10f)
                val rr = baseR * r.shape[i]
                val px = r.x + cos(ang) * rr; val py = r.y + sin(ang) * rr
                if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            path.close()
            drawPath(path, color = Color.White.copy(alpha = 0.22f), style = Stroke(width = 4.5f))
            drawPath(path, color = Color.White, style = Stroke(width = 1.2f))
            for (i in 0 until 10) {
                val ang = i * (2f * PI.toFloat() / 10f)
                val rr = baseR * r.shape[i]
                drawRect(Color.White.copy(alpha = 0.85f),
                    topLeft = Offset(r.x + cos(ang) * rr - 1f, r.y + sin(ang) * rr - 1f), size = Size(2f, 2f))
            }
        }

        // Ship — vector triangle, blinks when invulnerable.
        if (respawnTimer <= 0f && (invulnerable <= 0f || (time * 8f).toInt() % 2 == 0)) {
            val tipX = shipX + cos(shipAng) * 14f
            val tipY = shipY + sin(shipAng) * 14f
            val backLeftX = shipX + cos(shipAng + 2.4f) * 11f
            val backLeftY = shipY + sin(shipAng + 2.4f) * 11f
            val backRightX = shipX + cos(shipAng - 2.4f) * 11f
            val backRightY = shipY + sin(shipAng - 2.4f) * 11f
            glowLine(Offset(tipX, tipY), Offset(backLeftX, backLeftY))
            glowLine(Offset(tipX, tipY), Offset(backRightX, backRightY))
            glowLine(Offset(backLeftX, backLeftY), Offset(backRightX, backRightY))
            if (thrustOn && (time * 12f).toInt() % 2 == 0) {
                val rx = shipX + cos(shipAng + PI.toFloat()) * 8f
                val ry = shipY + sin(shipAng + PI.toFloat()) * 8f
                val rx2 = shipX + cos(shipAng + PI.toFloat()) * 18f
                val ry2 = shipY + sin(shipAng + PI.toFloat()) * 18f
                glowLine(Offset(rx, ry), Offset(rx2, ry2), Color(0xFFFFAA00))
            }
        }

        // HUD
        drawDosText("SCORE  $score", x = 14f, y = 22f, color = Color.White, size = 14f, bold = true)
        // (glowLine defined below keeps the tube look on ship + lives icons)
        drawDosText("WAVE  $level", x = W / 2f, y = 22f, color = Color.White, size = 14f, bold = true, align = TextAlign.CENTER)
        for (i in 0 until lives) {
            val lx = W - 16f - i * 14f
            val ly = 16f
            glowLine(Offset(lx, ly - 7f), Offset(lx - 5f, ly + 6f))
            glowLine(Offset(lx, ly - 7f), Offset(lx + 5f, ly + 6f))
            glowLine(Offset(lx - 5f, ly + 6f), Offset(lx + 5f, ly + 6f))
        }
        drawDosText("BEST $best", x = W - 16f, y = 36f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.RIGHT)

        if (gameOver) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("GAME OVER", x = W / 2f, y = H / 2f - 14f, color = Color.White, size = 36f, bold = true, align = TextAlign.CENTER)
            drawDosText("SCORE  $score", x = W / 2f, y = H / 2f + 22f, color = Color.White, size = 18f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"), x = W / 2f, y = H / 2f + 50f, color = Color(0xFFAAAAAA), size = 14f, align = TextAlign.CENTER)
        }
    }

    // Tapping the canvas starts / restarts; all steering is via the side controls.
    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (stage == Stage.TITLE) { startGame(); return }
        if (gameOver) { startGame(); return }
    }
}
