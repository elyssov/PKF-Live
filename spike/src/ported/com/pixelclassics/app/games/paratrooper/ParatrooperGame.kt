package com.pixelclassics.app.games.paratrooper

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pixelclassics.app.R
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/** Aim the AA gun. Stop 4 paratroopers reaching the base. */
class ParatrooperGame : Game {
    override val id: String = "paratrooper"
    override val titleResId: Int = R.string.game_paratrooper
    override val backgroundColor: Color = Color(0xFF42A5F5)
    override val introText: String = PARATROOPER_INTRO_EN
    override fun introTextFor(lang: String): String = pickParatrooperIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.CGA_CYAN

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private val gunX get() = W / 2f
    private val gunY get() = H - 30f
    private val barrelLen = 30f
    private val groundY = H - 20f

    private data class Bullet(var x: Float, var y: Float, var vx: Float, var vy: Float)
    private data class Heli(var x: Float, var y: Float, var vx: Float, var dropTimer: Float, var dropped: Boolean)
    private data class Para(var x: Float, var y: Float, var vy: Float,
                            var chute: Boolean, var landed: Boolean,
                            var runSpeed: Float = 0f, var frame: Float = 0f)
    private data class Crate(var x: Float, var y: Float, var vy: Float)

    private val bullets = mutableListOf<Bullet>()
    private val helis = mutableListOf<Heli>()
    private val paras = mutableListOf<Para>()
    private val crates = mutableListOf<Crate>()
    private var gunAngle = -PI.toFloat() / 2f
    private var score = 0
    private var best = 0
    private var level = 1
    private var leftLanded = 0
    private var rightLanded = 0
    private var spawnTimer = 0f
    private var gameOver = false
    private var time = 0f

    /** Max bullets allowed in the air at once. Grows by catching dropped crates. */
    private var maxBullets = 1

    // Control-style picker (Юджин: choose tap vs arrows on start, with a popup).
    private enum class Phase { CHOOSE, CONFIRM, PLAY }
    private var phase = Phase.CONFIRM
    private enum class Style { TAP, ARROWS }
    private var pendingStyle = Style.ARROWS
    private var style = Style.ARROWS

    private val controlsMode = mutableStateOf(GameControls.NONE)
    override val controls: GameControls get() = controlsMode.value
    override val controlsState: State<GameControls> get() = controlsMode

    // ARROWS-style aiming: hold ◀▶ to rotate (freshness — auto-repeat has no key-up).
    private var rotDir = 0
    private var rotFreshT = 0f
    private val rotSpeed = 1.9f

    override val helpLines: List<String> get() = if (style == Style.ARROWS)
        listOf("◀ ▶  rotate the cannon", "●  fire (hold to keep firing)", "Stop 4 troopers from reaching the base")
    else
        listOf("Click the sky to aim & fire", "Catch crates for +1 ammo", "Stop 4 troopers from reaching the base")
    override fun helpLinesFor(lang: String): List<String> = if (lang != "ru") helpLines
    else if (style == Style.ARROWS)
        listOf("◀ ▶  вращение пушки", "●  огонь (держи — стреляет очередью)", "Не дай 4 десантникам добраться до базы")
    else
        listOf("Кликни небо — прицел и выстрел", "Лови ящики: +1 снаряд", "Не дай 4 десантникам добраться до базы")

    // Picker button geometry (shared by draw + hit-test).
    private val cbW = 440f; private val cbH = 60f
    private val cbX get() = W / 2f - cbW / 2f
    private val cbAY = 196f; private val cbBY = 300f
    private val confW = 170f; private val confH = 52f
    private val confY = 372f
    private val startX get() = W / 2f - confW - 12f
    private val backX get() = W / 2f + 12f

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        pendingStyle = Style.ARROWS
        style = Style.ARROWS
        phase = Phase.CONFIRM
        controlsMode.value = GameControls.NONE
    }

    private fun startPlay() {
        style = pendingStyle
        controlsMode.value = if (style == Style.ARROWS) GameControls.DPAD_AND_FIRE else GameControls.NONE
        restart()
        phase = Phase.PLAY
    }

    private fun restart() {
        bullets.clear(); helis.clear(); paras.clear(); crates.clear()
        gunAngle = -PI.toFloat() / 2f
        score = 0; level = 1; leftLanded = 0; rightLanded = 0; spawnTimer = 0f
        maxBullets = 1
        gameOver = false
    }

    private fun shoot(tx: Float, ty: Float, ctx: GameContext) {
        // Tap mode: aim toward the tap, then fire.
        val dx = tx - gunX; val dy = ty - gunY
        gunAngle = atan2(dy, dx).coerceIn(-PI.toFloat() + 0.1f, -0.1f)
        fireAlongAngle(ctx)
    }

    private fun fireAlongAngle(ctx: GameContext) {
        // Bullet limit: only maxBullets may be airborne at once.
        if (bullets.size >= maxBullets) return
        val speed = 480f
        bullets += Bullet(
            gunX + cos(gunAngle) * barrelLen,
            gunY + sin(gunAngle) * barrelLen,
            cos(gunAngle) * speed,
            sin(gunAngle) * speed,
        )
        ctx.sound.shoot()
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (phase != Phase.PLAY) return
        rotDir = dx; rotFreshT = time
    }

    override fun onFire(ctx: GameContext) {
        if (phase != Phase.PLAY || gameOver) return
        fireAlongAngle(ctx)
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (phase != Phase.PLAY || gameOver) return
        if (style == Style.ARROWS && rotDir != 0 && time - rotFreshT < 0.16f) {
            gunAngle = (gunAngle + rotDir * rotSpeed * dt).coerceIn(-PI.toFloat() + 0.1f, -0.1f)
        }
        spawnTimer += dt
        val spawnInterval = (2.0f - level * 0.15f).coerceAtLeast(1.0f)
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f; spawnHeli()
        }

        val helIt = helis.iterator()
        while (helIt.hasNext()) {
            val h = helIt.next()
            h.x += h.vx * dt * 60f
            h.dropTimer -= dt
            if (h.dropTimer <= 0f && !h.dropped && h.x > 50f && h.x < W - 50f) {
                h.dropped = true
                paras += Para(h.x, h.y + 15f, 30f + Random.nextFloat() * 30f, chute = true, landed = false)
            }
            if ((h.vx > 0 && h.x > W + 100f) || (h.vx < 0 && h.x < -100f)) helIt.remove()
        }

        val paraIt = paras.iterator()
        while (paraIt.hasNext()) {
            val p = paraIt.next()
            if (!p.landed) {
                p.y += (if (p.chute) p.vy else p.vy * 4f) * dt * 1f
                if (p.y >= groundY - 5f) {
                    if (!p.chute) { paraIt.remove(); continue }
                    p.y = groundY - 5f; p.landed = true
                    p.runSpeed = 36f + Random.nextFloat() * 30f
                }
            } else {
                val dir = if (p.x < gunX) 1 else -1
                p.x += dir * p.runSpeed * dt
                p.frame += dt * 9f
                if (abs(p.x - gunX) < 15f) {
                    if (p.x < gunX) leftLanded++ else rightLanded++
                    paraIt.remove()
                    if (leftLanded >= 4 || rightLanded >= 4) {
                        gameOver = true; ctx.sound.gameOver()
                        if (score > best) { best = score; ctx.scores.set(id, score) }
                    }
                }
            }
        }

        // Crates fall from downed helis; catching one (lands at the gun) adds a bullet.
        val crateIt = crates.iterator()
        while (crateIt.hasNext()) {
            val c = crateIt.next()
            c.y += c.vy * dt
            if (c.y >= gunY - 14f) {
                // Caught if it comes down on or near the gun column.
                if (abs(c.x - gunX) < 40f) {
                    maxBullets++
                    ctx.sound.score()
                }
                crateIt.remove()
            }
        }

        val bulIt = bullets.iterator()
        bullets@ while (bulIt.hasNext()) {
            val b = bulIt.next()
            b.x += b.vx * dt; b.y += b.vy * dt
            if (b.x < 0f || b.x > W || b.y < 0f || b.y > H) { bulIt.remove(); continue }
            for (j in helis.indices.reversed()) {
                val h = helis[j]
                if (abs(b.x - h.x) < 30f && abs(b.y - h.y) < 15f) {
                    // Downed helicopter drops a crate. Catch it to gain a bullet.
                    crates += Crate(h.x, h.y, 90f)
                    helis.removeAt(j); bulIt.remove(); score += 20
                    ctx.sound.explosion(); continue@bullets
                }
            }
            for (j in paras.indices.reversed()) {
                val p = paras[j]
                if (abs(b.x - p.x) < 10f && abs(b.y - p.y) < 14f) {
                    if (!p.landed && p.chute) {
                        p.chute = false; p.vy = 180f; score += 5
                    } else {
                        paras.removeAt(j); score += if (p.landed) 15 else 10
                    }
                    bulIt.remove(); ctx.sound.explosion(); continue@bullets
                }
            }
        }

        level = score / 100 + 1
    }

    private fun spawnHeli() {
        val fromLeft = Random.nextBoolean()
        helis += Heli(
            x = if (fromLeft) -60f else W + 60f,
            y = 30f + Random.nextFloat() * 60f,
            vx = (if (fromLeft) 1 else -1) * (1.5f + level * 0.3f),
            dropTimer = 1.0f + Random.nextFloat() * 0.7f,
            dropped = false,
        )
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Pixel sky bands.
        drawRect(Color(0xFF1A237E), topLeft = Offset.Zero, size = Size(W, H * 0.25f))
        drawRect(Color(0xFF2962CC), topLeft = Offset(0f, H * 0.25f), size = Size(W, H * 0.20f))
        drawRect(Color(0xFF42A5F5), topLeft = Offset(0f, H * 0.45f), size = Size(W, H * 0.55f))
        // Pixel clouds — drift slowly.
        val cOff = (time * 8f) % W
        for (i in 0 until 5) {
            val cx = (i * 140f - cOff + W) % W
            val cy = 40f + (i % 2) * 30f
            drawRect(Color.White.copy(alpha = 0.85f), topLeft = Offset(cx, cy), size = Size(36f, 8f))
            drawRect(Color.White.copy(alpha = 0.85f), topLeft = Offset(cx + 8f, cy - 6f), size = Size(24f, 6f))
            drawRect(Color.White.copy(alpha = 0.85f), topLeft = Offset(cx + 4f, cy + 8f), size = Size(28f, 4f))
        }
        // Sun.
        drawRect(Color(0xFFFCE040), topLeft = Offset(W - 80f, 40f), size = Size(40f, 40f))
        drawRect(Color(0xFFFCB400), topLeft = Offset(W - 76f, 44f), size = Size(32f, 32f))
        // Ground with grass texture.
        drawRect(Color(0xFF2A6818), topLeft = Offset(0f, groundY), size = Size(W, 20f))
        drawRect(Color(0xFF4CAF50), topLeft = Offset(0f, groundY), size = Size(W, 4f))
        // Grass blades.
        for (i in 0 until 80) {
            val gx = ((i * 173 + 11) % W.toInt()).toFloat()
            drawRect(Color(0xFF66C766), topLeft = Offset(gx, groundY + 1f), size = Size(1f, 3f))
        }

        if (phase != Phase.PLAY) { drawMenu(this, ctx); return@with }

        // Gun base — sandbags.
        drawRect(Color(0xFF6A5028), topLeft = Offset(gunX - 26f, gunY - 4f), size = Size(52f, 14f))
        for (i in 0 until 3) {
            drawRect(Color(0xFF8A6A38), topLeft = Offset(gunX - 24f + i * 16f, gunY - 2f), size = Size(14f, 6f))
            drawRect(Color(0xFF4A3818), topLeft = Offset(gunX - 24f + i * 16f, gunY + 4f), size = Size(14f, 1f))
        }
        // Gun mount.
        drawRect(Color(0xFF263238), topLeft = Offset(gunX - 16f, gunY - 12f), size = Size(32f, 12f))
        drawRect(Color(0xFF455A64), topLeft = Offset(gunX - 14f, gunY - 10f), size = Size(28f, 8f))
        drawCircle(Color(0xFF263238), radius = 14f, center = Offset(gunX, gunY - 6f))
        drawCircle(Color(0xFF455A64), radius = 12f, center = Offset(gunX, gunY - 6f))
        // Barrel.
        drawLine(
            color = Color(0xFF1A2024),
            start = Offset(gunX, gunY - 6f),
            end = Offset(gunX + cos(gunAngle) * (barrelLen + 4f), gunY - 6f + sin(gunAngle) * (barrelLen + 4f)),
            strokeWidth = 8f,
        )
        drawLine(
            color = Color(0xFF546E7A),
            start = Offset(gunX, gunY - 6f),
            end = Offset(gunX + cos(gunAngle) * barrelLen, gunY - 6f + sin(gunAngle) * barrelLen),
            strokeWidth = 5f,
        )

        // Helicopters.
        for (h in helis) {
            drawOval(
                Color(0xFF37474F),
                topLeft = Offset(h.x - 25f, h.y - 10f),
                size = Size(50f, 20f),
            )
            drawRect(
                Color(0xFF37474F),
                topLeft = Offset(if (h.vx > 0) h.x - 35f else h.x + 15f, h.y - 3f),
                size = Size(20f, 6f),
            )
            val rotorW = 30f * sin((time * 20f).toDouble()).toFloat()
            drawLine(
                color = Color(0xFF546E7A),
                start = Offset(h.x - rotorW, h.y - 12f),
                end = Offset(h.x + rotorW, h.y - 12f),
                strokeWidth = 2f,
            )
        }

        // Paratroopers — a real little soldier, not a red domino (Юджин:
        // «солдатики избыточно примитивны»): helmet, torso, arms up on the
        // risers while descending, legs that run once landed.
        for (p in paras) {
            val uniform = if (p.landed) Color(0xFFC62828) else Color(0xFFE53935)
            val dark = Color(0xFF7B1A12)
            val skin = Color(0xFFE8B88A)
            if (p.chute && !p.landed) {
                // Canopy with panel seams + two shroud lines to the shoulders.
                drawArc(Color.White, startAngle = 180f, sweepAngle = 180f, useCenter = true,
                    topLeft = Offset(p.x - 13f, p.y - 31f), size = Size(26f, 26f))
                drawLine(Color(0xFFBDBDBD), Offset(p.x, p.y - 31f), Offset(p.x, p.y - 18f), strokeWidth = 1f)
                drawLine(Color(0xFFBDBDBD), Offset(p.x - 8f, p.y - 28f), Offset(p.x - 4f, p.y - 18f), strokeWidth = 1f)
                drawLine(Color(0xFFBDBDBD), Offset(p.x + 8f, p.y - 28f), Offset(p.x + 4f, p.y - 18f), strokeWidth = 1f)
                drawLine(Color(0xFFCFCFCF), Offset(p.x - 12f, p.y - 19f), Offset(p.x - 3f, p.y - 4f), strokeWidth = 1f)
                drawLine(Color(0xFFCFCFCF), Offset(p.x + 12f, p.y - 19f), Offset(p.x + 3f, p.y - 4f), strokeWidth = 1f)
                // Arms raised to the risers.
                drawLine(uniform, Offset(p.x - 2f, p.y - 3f), Offset(p.x - 4f, p.y - 7f), strokeWidth = 2f)
                drawLine(uniform, Offset(p.x + 2f, p.y - 3f), Offset(p.x + 4f, p.y - 7f), strokeWidth = 2f)
            } else if (!p.landed) {
                // Free-fall: arms flailing out.
                drawLine(uniform, Offset(p.x - 2f, p.y - 3f), Offset(p.x - 6f, p.y - 5f), strokeWidth = 2f)
                drawLine(uniform, Offset(p.x + 2f, p.y - 3f), Offset(p.x + 6f, p.y - 5f), strokeWidth = 2f)
            } else {
                // Landed: arms pumping as he runs for the base.
                val swing = if (p.frame.toInt() % 2 == 0) 3f else -3f
                drawLine(uniform, Offset(p.x - 2f, p.y - 3f), Offset(p.x - 4f, p.y + swing), strokeWidth = 2f)
                drawLine(uniform, Offset(p.x + 2f, p.y - 3f), Offset(p.x + 4f, p.y - swing), strokeWidth = 2f)
            }
            // Head + helmet brim.
            drawCircle(skin, radius = 3f, center = Offset(p.x, p.y - 8f))
            drawArc(dark, startAngle = 180f, sweepAngle = 180f, useCenter = true,
                topLeft = Offset(p.x - 4f, p.y - 12.5f), size = Size(8f, 8f))
            drawRect(dark, topLeft = Offset(p.x - 4f, p.y - 9f), size = Size(8f, 1.5f))
            // Torso with a belt.
            drawRect(uniform, topLeft = Offset(p.x - 2.5f, p.y - 5f), size = Size(5.5f, 7f))
            drawRect(dark, topLeft = Offset(p.x - 2.5f, p.y - 0.5f), size = Size(5.5f, 1.5f))
            // Legs: running stride on the ground, dangling in the air.
            if (p.landed) {
                val stride = if (p.frame.toInt() % 2 == 0) 3f else -3f
                drawLine(dark, Offset(p.x - 1f, p.y + 2f), Offset(p.x - 1f + stride, p.y + 6f), strokeWidth = 2f)
                drawLine(dark, Offset(p.x + 1f, p.y + 2f), Offset(p.x + 1f - stride, p.y + 6f), strokeWidth = 2f)
            } else {
                drawLine(dark, Offset(p.x - 1.5f, p.y + 2f), Offset(p.x - 2f, p.y + 6f), strokeWidth = 2f)
                drawLine(dark, Offset(p.x + 1.5f, p.y + 2f), Offset(p.x + 2f, p.y + 6f), strokeWidth = 2f)
            }
        }

        // Falling supply crates — catch at the gun for an extra bullet.
        for (c in crates) {
            drawRect(Color(0xFF8D5A1E), topLeft = Offset(c.x - 9f, c.y - 9f), size = Size(18f, 18f))
            drawRect(Color(0xFFB5803A), topLeft = Offset(c.x - 7f, c.y - 7f), size = Size(14f, 14f))
            // Crate slats.
            drawLine(Color(0xFF5A3A12), start = Offset(c.x - 9f, c.y), end = Offset(c.x + 9f, c.y), strokeWidth = 2f)
            drawLine(Color(0xFF5A3A12), start = Offset(c.x, c.y - 9f), end = Offset(c.x, c.y + 9f), strokeWidth = 2f)
        }

        // Bullets.
        for (b in bullets) drawCircle(Color(0xFFFFEB3B), radius = 3f, center = Offset(b.x, b.y))

        // Landing counts.
        if (leftLanded > 0)
            drawDosText("$leftLanded/4", x = gunX - 60f, y = H - 5f, color = Color(0xFFC62828), size = 14f, bold = true, align = TextAlign.CENTER)
        if (rightLanded > 0)
            drawDosText("$rightLanded/4", x = gunX + 60f, y = H - 5f, color = Color(0xFFC62828), size = 14f, bold = true, align = TextAlign.CENTER)

        // HUD.
        drawDosText("SCORE  $score", x = 10f, y = 22f, color = Color.White, size = 16f, bold = true)
        drawDosText("LEVEL  $level", x = W - 10f, y = 22f, color = Color.White, size = 16f, bold = true, align = TextAlign.RIGHT)
        drawDosText("BEST  $best", x = W / 2f, y = 22f, color = Color(0xFFFFD600), size = 14f, bold = true, align = TextAlign.CENTER)
        // Ammo capacity: how many bullets may be airborne at once.
        drawDosText("AMMO  ${bullets.size}/$maxBullets", x = 10f, y = 42f, color = Color(0xFFFFEB3B), size = 14f, bold = true)

        if (gameOver) {
            drawRect(Color(0xB3000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("GAME OVER", x = W / 2f, y = H / 2f - 20f, color = Color(0xFFFF3333), size = 36f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Score $score", "Счёт $score"), x = W / 2f, y = H / 2f + 20f, color = Color.White, size = 20f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"), x = W / 2f, y = H / 2f + 50f, color = Color(0xFF999999), size = 14f, align = TextAlign.CENTER)
        }
    }

    private fun DrawScope.drawOval(color: Color, topLeft: Offset, size: Size) {
        drawArc(
            color = color,
            startAngle = 0f, sweepAngle = 360f, useCenter = true,
            topLeft = topLeft, size = size,
        )
    }

    // ── Control-style picker UI ───────────────────────────────────────────────
    private fun drawMenu(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color(0x66001022), topLeft = Offset.Zero, size = Size(W, H))
        drawDosText("PARATROOPER", x = W / 2f, y = 78f, color = Color.White, size = 40f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Defend the base — stop the falling troopers.", "Защити базу — останови падающих десантников."), x = W / 2f, y = 110f, color = Color(0xFFB0C4DE), size = 13f, align = TextAlign.CENTER)
        if (phase == Phase.CHOOSE) {
            drawDosText(ctx.tr("CHOOSE YOUR CONTROLS", "ВЫБЕРИ УПРАВЛЕНИЕ"), x = W / 2f, y = 164f, color = Color(0xFFFFD600), size = 16f, bold = true, align = TextAlign.CENTER)
            menuButton(this, cbX, cbAY, cbW, cbH, ctx.tr("CLICK  TO  AIM", "КЛИК = ПРИЦЕЛ"), Color(0xFF1B5E20))
            drawDosText(ctx.tr("Click anywhere in the sky — the cannon aims there & fires.", "Кликни в любое место неба — пушка целится туда и стреляет."), x = W / 2f, y = cbAY + cbH + 18f, color = Color(0xFFAFC8AF), size = 11f, align = TextAlign.CENTER)
            menuButton(this, cbX, cbBY, cbW, cbH, "◀ ▶  ARROWS  +  SPACE", Color(0xFF0D47A1))
            drawDosText(ctx.tr("◀ ▶ arrow keys rotate the cannon; SPACE fires.", "Стрелки ◀ ▶ вращают пушку; SPACE стреляет."), x = W / 2f, y = cbBY + cbH + 18f, color = Color(0xFFA8BEDD), size = 11f, align = TextAlign.CENTER)
        } else {
            val tap = pendingStyle == Style.TAP
            drawDosText(if (tap) ctx.tr("CLICK TO AIM", "КЛИК = ПРИЦЕЛ") else ctx.tr("ARROWS + FIRE", "СТРЕЛКИ + ОГОНЬ"), x = W / 2f, y = 168f, color = Color(0xFFFFD600), size = 20f, bold = true, align = TextAlign.CENTER)
            val lines = if (tap) listOf(
                ctx.tr("Click anywhere in the sky.", "Кликни в любое место неба."),
                ctx.tr("The cannon snaps to that angle and fires one shell.", "Пушка мгновенно целится туда и делает выстрел."),
                ctx.tr("Only a few shells may be airborne at once —", "В воздухе одновременно лишь несколько снарядов —"),
                ctx.tr("catch falling supply crates at the gun for +1 ammo.", "лови падающие ящики у пушки: +1 снаряд."),
            ) else listOf(
                ctx.tr("Use the arrow keys: ◀ ▶ rotate the cannon.", "Стрелки: ◀ ▶ вращают пушку."),
                ctx.tr("Press the red ● button to fire along the barrel.", "SPACE — выстрел вдоль ствола."),
                ctx.tr("Hold ● to keep firing. Same ammo limit applies —", "Держи ● — очередь. Лимит снарядов тот же —"),
                ctx.tr("catch falling supply crates at the gun for +1 ammo.", "лови падающие ящики у пушки: +1 снаряд."),
            )
            var yy = 212f
            for (l in lines) { drawDosText(l, x = W / 2f, y = yy, color = Color.White, size = 12f, align = TextAlign.CENTER); yy += 24f }
            menuButton(this, startX, confY, confW, confH, ctx.tr("START ▶", "СТАРТ ▶"), Color(0xFF1B5E20))
            menuButton(this, backX, confY, confW, confH, "◀ BACK", Color(0xFF5D4037))
        }
    }

    private fun menuButton(scope: DrawScope, x: Float, y: Float, w: Float, h: Float, label: String, bg: Color) = with(scope) {
        drawRect(Color.Black.copy(alpha = 0.35f), topLeft = Offset(x + 3f, y + 4f), size = Size(w, h))
        drawRect(bg, topLeft = Offset(x, y), size = Size(w, h))
        drawRect(Color.White.copy(alpha = 0.6f), topLeft = Offset(x, y), size = Size(w, 3f))
        drawRect(Color.White.copy(alpha = 0.6f), topLeft = Offset(x, y), size = Size(3f, h))
        drawDosText(label, x = x + w / 2f, y = y + h / 2f + 6f, color = Color.White, size = if (h >= 58f) 18f else 16f, bold = true, align = TextAlign.CENTER)
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        when (phase) {
            Phase.CHOOSE -> {
                if (x in cbX..cbX + cbW) {
                    if (y in cbAY..cbAY + cbH) { pendingStyle = Style.TAP; phase = Phase.CONFIRM; ctx.sound.click() }
                    else if (y in cbBY..cbBY + cbH) { pendingStyle = Style.ARROWS; phase = Phase.CONFIRM; ctx.sound.click() }
                }
            }
            Phase.CONFIRM -> {
                if (y in confY..confY + confH) {
                    if (x in startX..startX + confW) { ctx.sound.click(); startPlay() }
                    else if (x in backX..backX + confW) { ctx.sound.click(); phase = Phase.CHOOSE }
                }
            }
            Phase.PLAY -> {
                if (gameOver) { restart(); return }
                if (style == Style.TAP) shoot(x, y, ctx)
                // ARROWS: field taps ignored — aim with the side joystick + fire button.
            }
        }
    }
}
