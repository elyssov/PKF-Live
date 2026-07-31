package com.pixelclassics.app.games.starwing

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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * STAR WING — vertical-shoot-em-up, formation enemies, capture-rescue.
 *
 * Trademark-safe re-imagining of a 1981 classic (Galaga). Our hero is
 * a triangular star-fighter — not Galaga's specific sprite. Antagonists
 * are «space wasps» that fly in за three-row formation, занимают свои
 * позиции в hive, и периодически пикируют atака. Bigger «mother»
 * occasionally dives with a capture-beam — caught fighter rejoins as a
 * double-ship if you kill her mid-capture.
 */
class StarWingGame : Game {
    override val id: String = "star_wing"
    override val titleResId: Int = R.string.game_star_wing
    override val backgroundColor: Color = Color.Black
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    override val introText: String = STAR_SWARM_INTRO_EN
    override fun introTextFor(lang: String): String = pickStarWingIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.TERMINAL_GREEN_CRT
    override val helpLines: List<String> = listOf(
        "◀ ▶ — strafe · ● FIRE — twin lasers",
        "Wasps fly in formation, then dive.  Mothers can capture you.",
        "Kill a mother mid-capture to free your captured fighter for a double-ship",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "◀ ▶ — манёвр · ● ОГОНЬ — спаренные лазеры",
        "Осы летят строем, потом пикируют.  Матки умеют захватывать тебя.",
        "Убей матку во время захвата — вернёшь истребитель и получишь сдвоенный",
    ) else helpLines

    private val W = 480f
    private val H = 720f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    // Player.
    private val shipW = 32f; private val shipH = 28f
    private var sx = W / 2f
    private val sy = H - 80f
    private var leftHeld = false
    private var rightHeld = false
    private var fireCd = 0f
    private var captured = false               // одна жизнь поглощена mother — есть шанс на double
    private var doubled = false                // спасли — играем двумя кораблями рядом

    private data class Bullet(var x: Float, var y: Float, var vy: Float, val fromEnemy: Boolean)
    private data class Enemy(
        var x: Float, var y: Float,
        var hivePosX: Float, var hivePosY: Float,
        var state: State, var kind: Kind,
        var diveT: Float = 0f, var divePath: DivePath = DivePath.STRAIGHT,
        var animT: Float = 0f, var alive: Boolean = true,
    ) {
        enum class State { ENTERING, HIVE, DIVING, RETURNING, CAPTURING }
        enum class Kind { WASP, DRONE, MOTHER }
        enum class DivePath { STRAIGHT, ARC_LEFT, ARC_RIGHT, SPIRAL }
    }
    private data class Star(var x: Float, var y: Float, val speed: Float, val br: Float)

    private val bullets = mutableListOf<Bullet>()
    private val enemies = mutableListOf<Enemy>()
    private val stars = ArrayList<Star>(60)

    private var stage = Stage.TITLE
    private enum class Stage { TITLE, PLAY, GAMEOVER }

    private var score = 0
    private var best = 0
    private var lives = 3
    private var wave = 1
    private var time = 0f
    private var spawnQueue = mutableListOf<EnemySpawn>()
    private var spawnTimer = 0f
    private data class EnemySpawn(val kind: Enemy.Kind, val hivePosX: Float, val hivePosY: Float, val enterFromLeft: Boolean)

    private var motherDiveTimer = 0f

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        stage = Stage.TITLE
        seedStars()
    }

    private fun seedStars() {
        stars.clear()
        for (i in 0 until 60) {
            stars += Star(Random.nextFloat() * W, Random.nextFloat() * H,
                Random.nextFloat() * 80f + 20f, Random.nextFloat() * 0.7f + 0.3f)
        }
    }

    private fun startGame() {
        bullets.clear(); enemies.clear(); spawnQueue.clear()
        sx = W / 2f; captured = false; doubled = false
        score = 0; lives = 3; wave = 1
        prepareWave()
        stage = Stage.PLAY
    }

    private fun prepareWave() {
        spawnQueue.clear()
        spawnTimer = 0f
        // Hive grid: 3 rows × 8 cols.
        val hiveTop = 80f
        val hiveLeft = 60f
        val gap = (W - 2 * hiveLeft) / 7f
        for (col in 0 until 8) {
            for (row in 0 until 3) {
                val kind = when (row) {
                    0 -> Enemy.Kind.MOTHER
                    1 -> Enemy.Kind.DRONE
                    else -> Enemy.Kind.WASP
                }
                val hx = hiveLeft + col * gap
                val hy = hiveTop + row * 40f
                spawnQueue += EnemySpawn(kind, hx, hy, enterFromLeft = (col + row) % 2 == 0)
            }
        }
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        leftHeld = dx < 0; rightHeld = dx > 0
    }

    override fun onFire(ctx: GameContext) {
        if (stage == Stage.TITLE) { startGame(); return }
        if (stage == Stage.GAMEOVER) { startGame(); return }
        if (fireCd <= 0f) {
            bullets += Bullet(sx, sy - 16f, -640f, false)
            if (doubled) bullets += Bullet(sx + 28f, sy - 16f, -640f, false)
            fireCd = 0.18f
            ctx.sound.shoot()
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (stage != Stage.PLAY) return

        // Stars parallax.
        for (s in stars) {
            s.y += s.speed * dt
            if (s.y > H) { s.y = 0f; s.x = Random.nextFloat() * W }
        }

        if (fireCd > 0f) fireCd -= dt

        // Player movement.
        val vx = (if (leftHeld) -1f else 0f) + (if (rightHeld) 1f else 0f)
        sx = (sx + vx * 320f * dt).coerceIn(shipW / 2f, W - shipW / 2f)

        // Spawn wave entry — one ship every 0.18 sec until queue empty.
        if (spawnQueue.isNotEmpty()) {
            spawnTimer += dt
            if (spawnTimer >= 0.18f) {
                spawnTimer = 0f
                val sp = spawnQueue.removeAt(0)
                val startX = if (sp.enterFromLeft) -20f else W + 20f
                enemies += Enemy(startX, -20f, sp.hivePosX, sp.hivePosY, Enemy.State.ENTERING, sp.kind)
            }
        }

        // Gentle, smooth difficulty ramp: each wave a touch faster, hard-capped
        // so it climbs steadily and never jumps. Wave 1 is the calm baseline.
        val diff = (1f + (wave - 1) * 0.06f).coerceAtMost(1.8f)

        // Enemy AI.
        for (e in enemies) {
            if (!e.alive) continue
            e.animT += dt
            when (e.state) {
                Enemy.State.ENTERING -> {
                    val tx = e.hivePosX; val ty = e.hivePosY
                    val dx = tx - e.x; val dy = ty - e.y
                    val d = kotlin.math.hypot(dx, dy)
                    if (d < 4f) { e.x = tx; e.y = ty; e.state = Enemy.State.HIVE }
                    else { e.x += dx / d * 220f * dt; e.y += dy / d * 220f * dt }
                }
                Enemy.State.HIVE -> {
                    // Hive sway.
                    val sway = sin(time * 0.8f).toFloat() * 8f
                    e.x = e.hivePosX + sway
                    e.y = e.hivePosY
                }
                Enemy.State.DIVING -> {
                    e.diveT += dt
                    val k = e.kind
                    val sp = (if (k == Enemy.Kind.WASP) 220f else 165f) * diff
                    when (e.divePath) {
                        Enemy.DivePath.STRAIGHT -> {
                            val dxToPlayer = sx - e.x
                            e.x += dxToPlayer.coerceIn(-180f, 180f) * dt
                            e.y += sp * dt
                        }
                        Enemy.DivePath.ARC_LEFT -> {
                            e.x += (sin(e.diveT * 2.0f) * 120f - 60f) * dt
                            e.y += sp * dt
                        }
                        Enemy.DivePath.ARC_RIGHT -> {
                            e.x += (sin(e.diveT * 2.0f) * 120f + 60f) * dt
                            e.y += sp * dt
                        }
                        Enemy.DivePath.SPIRAL -> {
                            val r = 60f
                            e.x += cos(e.diveT * 4f) * r * dt
                            e.y += sp * dt + sin(e.diveT * 4f) * r * dt
                        }
                    }
                    // Random shot.
                    if (Random.nextFloat() < dt * 0.5f * diff) {
                        bullets += Bullet(e.x, e.y + 12f, 320f * diff, true)
                    }
                    // Returning home if got past bottom.
                    if (e.y > H + 30f) {
                        e.y = -20f
                        e.x = if (Random.nextBoolean()) -20f else W + 20f
                        e.state = Enemy.State.ENTERING
                    }
                }
                Enemy.State.CAPTURING -> {
                    e.diveT += dt
                    // Mother descends and emits a beam below her.
                    e.y += 90f * dt
                    if (abs(e.x - sx) < 18f && abs(e.y - sy) < 60f && !captured) {
                        // Capture!
                        captured = true; lives--
                        ctx.sound.die()
                        if (lives <= 0) { stage = Stage.GAMEOVER; ctx.sound.gameOver(); if (score > best) { best = score; ctx.scores.set(id, score) } }
                    }
                    if (e.y > H + 30f) {
                        // Mother escaped off-screen: the captured ship is lost with
                        // her — clear the state or the player is stuck "captured"
                        // forever (no future capture, no rescue possible).
                        captured = false
                        e.y = -20f; e.x = if (Random.nextBoolean()) -20f else W + 20f
                        e.state = Enemy.State.ENTERING
                    }
                }
                Enemy.State.RETURNING -> Unit
            }
        }
        enemies.removeAll { !it.alive }

        // Randomly pick a hive enemy to dive — calmer at wave 1, ramps with diff.
        if (Random.nextFloat() < dt * 0.9f * diff) {
            val candidates = enemies.filter { it.alive && it.state == Enemy.State.HIVE && it.kind != Enemy.Kind.MOTHER }
            if (candidates.isNotEmpty()) {
                val pick = candidates.random()
                pick.state = Enemy.State.DIVING
                pick.diveT = 0f
                pick.divePath = Enemy.DivePath.values().random()
            }
        }
        // Mother dive (capturing) once per ~12 sec.
        motherDiveTimer += dt
        if (motherDiveTimer >= 12f) {
            motherDiveTimer = 0f
            val mother = enemies.firstOrNull { it.alive && it.state == Enemy.State.HIVE && it.kind == Enemy.Kind.MOTHER }
            mother?.let { it.state = Enemy.State.CAPTURING; it.diveT = 0f }
        }

        // Bullets.
        val toRemoveB = mutableSetOf<Int>()
        for (i in bullets.indices) {
            val b = bullets[i]
            b.y += b.vy * dt
            if (b.y < -10f || b.y > H + 10f) toRemoveB += i
        }
        // Bullets vs enemies.
        for (ei in enemies.indices) {
            val e = enemies[ei]
            if (!e.alive) continue
            for (bi in bullets.indices) {
                if (bi in toRemoveB) continue
                val b = bullets[bi]
                if (b.fromEnemy) continue
                val hitRange = if (e.kind == Enemy.Kind.MOTHER) 22f else 16f
                if (abs(b.x - e.x) < hitRange && abs(b.y - e.y) < hitRange) {
                    toRemoveB += bi
                    e.alive = false
                    score += when (e.kind) {
                        Enemy.Kind.WASP -> 50
                        Enemy.Kind.DRONE -> 80
                        Enemy.Kind.MOTHER -> 200
                    }
                    // Mother killed mid-capture → save the captured ship → doubled.
                    if (e.kind == Enemy.Kind.MOTHER && e.state == Enemy.State.CAPTURING && captured) {
                        doubled = true; captured = false; score += 500
                        ctx.sound.win()
                    } else ctx.sound.explosion()
                    break
                }
            }
        }
        // Bullets vs player.
        for (bi in bullets.indices) {
            if (bi in toRemoveB) continue
            val b = bullets[bi]
            if (!b.fromEnemy) continue
            if (abs(b.x - sx) < shipW / 2f && abs(b.y - sy) < shipH / 2f) {
                toRemoveB += bi
                if (doubled) { doubled = false }
                else {
                    lives--
                    if (lives <= 0) { stage = Stage.GAMEOVER; ctx.sound.gameOver(); if (score > best) { best = score; ctx.scores.set(id, score) } }
                    else ctx.sound.die()
                }
                break
            }
        }
        // Plane vs enemy collision. A CAPTURING mother must not ALSO ram the
        // ship she is holding in her beam — that charged the player two lives
        // for one capture event.
        for (e in enemies) {
            if (!e.alive) continue
            if (e.state == Enemy.State.CAPTURING && captured) continue
            if (abs(e.x - sx) < shipW / 2f && abs(e.y - sy) < shipH / 2f) {
                e.alive = false
                if (doubled) doubled = false
                else {
                    lives--
                    if (lives <= 0) { stage = Stage.GAMEOVER; ctx.sound.gameOver(); if (score > best) { best = score; ctx.scores.set(id, score) } }
                    else ctx.sound.die()
                }
            }
        }
        if (toRemoveB.isNotEmpty()) {
            val survivors = bullets.filterIndexed { i, _ -> i !in toRemoveB }
            bullets.clear(); bullets.addAll(survivors)
        }
        // All enemies dead → next wave.
        if (spawnQueue.isEmpty() && enemies.none { it.alive }) {
            wave++; score += 200
            prepareWave()
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))
        // Stars.
        for (s in stars) drawRect(Color.White.copy(alpha = s.br), topLeft = Offset(s.x, s.y), size = Size(1f, 1f))

        if (stage == Stage.TITLE) {
            drawTitleScreen(this, ctx)
            return@with
        }

        // Enemies.
        for (e in enemies) {
            if (!e.alive) continue
            drawEnemy(this, e)
        }
        // Bullets.
        for (b in bullets) {
            val color = if (b.fromEnemy) Color(0xFFFF6644) else Color(0xFF44FFFF)
            drawRect(color, topLeft = Offset(b.x - 2f, b.y - 8f), size = Size(4f, 12f))
            drawRect(Color.White, topLeft = Offset(b.x - 1f, b.y - 6f), size = Size(2f, 8f))
        }
        // Player ship — triangle.
        if (!captured) {
            drawShip(this, sx, sy, Color(0xFF44CCFF))
            if (doubled) drawShip(this, sx + 28f, sy, Color(0xFFFFAA44))
        } else {
            // Show "captured" placeholder: blinking outline near mother's beam region.
            if ((time * 6f).toInt() % 2 == 0) drawShip(this, sx, sy, Color(0xFFAA2233))
        }

        // HUD.
        drawDosText("SCORE  $score", x = 8f, y = 22f, color = Color.White, size = 14f, bold = true)
        drawDosText("WAVE $wave", x = W / 2f, y = 22f, color = Color(0xFF44CCFF), size = 13f, bold = true, align = TextAlign.CENTER)
        for (i in 0 until lives) {
            drawShip(this, W - 16f - i * 22f, 18f, Color(0xFF44CCFF), scale = 0.5f)
        }
        drawDosText("BEST $best", x = W - 8f, y = 36f, color = Color(0xFFFFD600), size = 10f, bold = true, align = TextAlign.RIGHT)

        if (stage == Stage.GAMEOVER) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("GAME OVER", x = W / 2f, y = H / 2f - 14f, color = Color(0xFFFF3333), size = 32f, bold = true, align = TextAlign.CENTER)
            drawDosText("SCORE $score", x = W / 2f, y = H / 2f + 16f, color = Color.White, size = 18f, bold = true, align = TextAlign.CENTER)
            drawDosText("● FIRE — RESTART", x = W / 2f, y = H / 2f + 50f, color = Color(0xFFCCCCCC), size = 12f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun drawShip(scope: DrawScope, x: Float, y: Float, color: Color, scale: Float = 1f) = with(scope) {
        val w = shipW * scale; val h = shipH * scale
        // Hull triangle (pointing up).
        drawRect(color, topLeft = Offset(x - w / 2f + 2f, y), size = Size(w - 4f, h * 0.4f))
        drawRect(color, topLeft = Offset(x - w / 4f, y - h * 0.3f), size = Size(w / 2f, h * 0.3f))
        drawRect(color, topLeft = Offset(x - 2f, y - h * 0.5f), size = Size(4f, h * 0.2f))
        // Cockpit.
        drawRect(Color.White, topLeft = Offset(x - 2f, y - h * 0.2f), size = Size(4f, 4f))
        // Wings.
        drawRect(color, topLeft = Offset(x - w / 2f, y + h * 0.1f), size = Size(w, 4f))
        // Engine glow.
        drawRect(Color(0xFFFFAA00), topLeft = Offset(x - 4f, y + h * 0.4f), size = Size(3f, 4f))
        drawRect(Color(0xFFFFAA00), topLeft = Offset(x + 1f, y + h * 0.4f), size = Size(3f, 4f))
    }

    private fun drawEnemy(scope: DrawScope, e: Enemy) = with(scope) {
        when (e.kind) {
            Enemy.Kind.WASP -> {
                // Yellow-black striped wasp.
                drawRect(Color(0xFFFFD600), topLeft = Offset(e.x - 11f, e.y - 8f), size = Size(22f, 16f))
                drawRect(Color.Black, topLeft = Offset(e.x - 11f, e.y - 4f), size = Size(22f, 3f))
                drawRect(Color.Black, topLeft = Offset(e.x - 11f, e.y + 3f), size = Size(22f, 3f))
                // Wings flap.
                val flap = sin(e.animT * 12f).toFloat() * 4f
                drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(e.x - 16f, e.y - 4f - flap), size = Size(6f, 6f))
                drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(e.x + 10f, e.y - 4f + flap), size = Size(6f, 6f))
                // Eyes.
                drawRect(Color(0xFFFF4400), topLeft = Offset(e.x - 6f, e.y - 11f), size = Size(3f, 3f))
                drawRect(Color(0xFFFF4400), topLeft = Offset(e.x + 3f, e.y - 11f), size = Size(3f, 3f))
            }
            Enemy.Kind.DRONE -> {
                // Blue-purple drone.
                drawRect(Color(0xFF8844FF), topLeft = Offset(e.x - 12f, e.y - 8f), size = Size(24f, 16f))
                drawRect(Color(0xFFAA88FF), topLeft = Offset(e.x - 10f, e.y - 6f), size = Size(20f, 12f))
                drawRect(Color.White, topLeft = Offset(e.x - 4f, e.y - 2f), size = Size(8f, 4f))
                val flap = sin(e.animT * 10f).toFloat() * 3f
                drawRect(Color(0xFF6622CC), topLeft = Offset(e.x - 18f, e.y - 2f - flap), size = Size(6f, 4f))
                drawRect(Color(0xFF6622CC), topLeft = Offset(e.x + 12f, e.y - 2f + flap), size = Size(6f, 4f))
            }
            Enemy.Kind.MOTHER -> {
                // Bigger green mother with crown.
                val baseColor = if (e.state == Enemy.State.CAPTURING) Color(0xFFCC44FF) else Color(0xFF22DD44)
                drawRect(baseColor, topLeft = Offset(e.x - 16f, e.y - 10f), size = Size(32f, 20f))
                drawRect(Color(0xFF66FF66), topLeft = Offset(e.x - 14f, e.y - 8f), size = Size(28f, 16f))
                drawRect(Color.White, topLeft = Offset(e.x - 6f, e.y - 3f), size = Size(12f, 6f))
                drawRect(Color(0xFFFFD600), topLeft = Offset(e.x - 8f, e.y - 14f), size = Size(4f, 4f))
                drawRect(Color(0xFFFFD600), topLeft = Offset(e.x - 2f, e.y - 16f), size = Size(4f, 4f))
                drawRect(Color(0xFFFFD600), topLeft = Offset(e.x + 4f, e.y - 14f), size = Size(4f, 4f))
                // Tractor beam when capturing.
                if (e.state == Enemy.State.CAPTURING) {
                    for (k in 0 until 8) {
                        val ty = e.y + 10f + k * 20f
                        val tw = 6f + k * 2f
                        drawRect(Color(0xFFFF66FF).copy(alpha = 0.4f - k * 0.04f),
                            topLeft = Offset(e.x - tw / 2f, ty), size = Size(tw, 16f))
                    }
                }
            }
        }
    }

    private fun drawTitleScreen(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawDosText("S T A R   W I N G", x = W / 2f, y = H * 0.20f, color = Color(0xFF44CCFF), size = 32f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("INSPIRED BY 1981", "ПО МОТИВАМ 1981 ГОДА"), x = W / 2f, y = H * 0.26f, color = Color(0xFFAAAACC), size = 11f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Shoot wasps, drones, mothers.", "Сбивай ос, дронов и маток."), x = W / 2f, y = H * 0.40f, color = Color.White, size = 13f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("A mother can capture you with a beam —", "Матка может схватить тебя лучом —"), x = W / 2f, y = H * 0.46f, color = Color(0xFFCC88FF), size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("kill her mid-capture to save your fighter", "убей её во время захвата — спасёшь истребитель"), x = W / 2f, y = H * 0.50f, color = Color(0xFFCC88FF), size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("and get a double-ship.", "и получишь сдвоенный корабль."), x = W / 2f, y = H * 0.54f, color = Color(0xFFCC88FF), size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("D-pad — strafe.   ● FIRE — shoot.", "Крестовина — манёвр.   ● ОГОНЬ — стрельба."), x = W / 2f, y = H * 0.66f, color = Color(0xFFCCCCCC), size = 12f, align = TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText("● FIRE — START", x = W / 2f, y = H * 0.82f, color = Color(0xFF44CCFF), size = 16f, bold = true, align = TextAlign.CENTER)
        drawDosText("BEST $best", x = W / 2f, y = H * 0.92f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.CENTER)
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (stage == Stage.TITLE) startGame()
        else if (stage == Stage.GAMEOVER) startGame()
    }
}
