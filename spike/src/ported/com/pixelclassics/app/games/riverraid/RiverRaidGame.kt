package com.pixelclassics.app.games.riverraid

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.abs
import kotlin.math.sin
import kotlin.random.Random

/**
 * RIVER RAID v2 — full rewrite per Юджин's feedback («проёб»).
 *
 * D-pad + fire button (DPAD_AND_FIRE). Slower base scroll. Better
 * enemy formations: boats hug banks, helis hover, jets dart down,
 * fuel barrels in centre of river. Procedural river — широкая
 * долина / узкое ущелье / расширение. Bridges каждые N секций —
 * пройти можно только взорвав.
 */
class RiverRaidGame : Game {
    override val id: String = "river_raid"
    override val titleResId: Int = R.string.game_river_raid
    override val backgroundColor: Color = Color.Black
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    override val introText: String = RIVER_RAID_INTRO_EN
    override fun introTextFor(lang: String): String = pickRiverRaidIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PARCHMENT
    override val helpLines: List<String> = listOf(
        "◀ ▶ — steer · ▲ ▼ — accelerate / brake · ● FIRE — guns",
        "Refuel from drums.  Bridges block your path — bomb them to pass",
        "Boats, helicopters, jets — escalating threat",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "◀ ▶ — руль · ▲ ▼ — газ / тормоз · ● ОГОНЬ — пушки",
        "Дозаправка от бочек.  Мосты перегораживают реку — бомби и проходи",
        "Катера, вертолёты, самолёты — угроза нарастает",
    ) else helpLines

    private val W = 480f
    private val H = 720f                  // вертикальный canvas, GameHost letterbox под телефон landscape
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    // Plane.
    private val planeW = 30f
    private val planeH = 36f
    private var planeX = W / 2f
    private var planeY = H - 110f
    private var planeVx = 0f
    private var planeVy = 0f
    private val planeMaxV = 220f

    // River strip: segments scrolling from top.
    private val segH = 16f
    private val baseRiverW = 240f
    private var riverWidth = baseRiverW
    private val riverCenters = ArrayDeque<Float>()    // x-center at each segment
    private val riverSegMeta = ArrayDeque<SegMeta>()  // optional bridge/refuel

    private enum class SegKind { OPEN, BRIDGE }
    private data class SegMeta(val kind: SegKind, val centerX: Float)

    // Entities.
    private data class Enemy(var x: Float, var y: Float, var vx: Float, var vy: Float,
                             val kind: Int, var alive: Boolean = true,
                             var animPhase: Float = 0f)
    private data class Bullet(var x: Float, var y: Float, var vy: Float)

    private val enemies = mutableListOf<Enemy>()
    private val bullets = mutableListOf<Bullet>()
    private val explosions = mutableListOf<Offset>()

    private var fuel = 100f
    private var respawnShield = 0f     // seconds of post-respawn mercy vs enemies/bridges
    private var score = 0
    private var lives = 3
    private var best = 0
    private var distance = 0f
    private var scrollSpeed = 80f
    private var gameOver = false
    private var time = 0f
    private var spawnAcc = 0f
    private var fireCd = 0f
    private var bridgeProgress = 0f       // counts segments to next bridge
    private var riverPhase = 0f           // serpentine phase advanced per generated segment

    private var leftHeld = false; private var rightHeld = false
    private var upHeld = false; private var downHeld = false
    private var firing = false

    private enum class Stage { TITLE, PLAY }
    private var stage = Stage.TITLE

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id); stage = Stage.TITLE
    }

    private fun startRun() {
        planeX = W / 2f; planeY = H - 110f; planeVx = 0f; planeVy = 0f
        riverCenters.clear(); riverSegMeta.clear(); enemies.clear(); bullets.clear(); explosions.clear()
        for (i in 0 until ((H / segH).toInt() + 4)) { riverCenters.addLast(W / 2f); riverSegMeta.addLast(SegMeta(SegKind.OPEN, 0f)) }
        riverWidth = baseRiverW
        fuel = 100f; score = 0; lives = 3; distance = 0f
        scrollSpeed = 80f; gameOver = false; spawnAcc = 0f; fireCd = 0f
        bridgeProgress = 0f
        riverPhase = 0f
        stage = Stage.PLAY
    }

    private var dpadX = 0
    private var dpadY = 0
    private var dpadFreshT = -1f
    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        // Freshness window — the d-pad gives auto-repeat pulses with NO release
        // event, so latching held-flags made the plane drift and ignore input.
        dpadX = dx; dpadY = dy; dpadFreshT = time
    }

    override fun onFire(ctx: GameContext) {
        if (stage == Stage.TITLE) { startRun(); return }
        if (gameOver) { startRun(); return }
        firing = true
        if (fireCd <= 0f) {
            bullets += Bullet(planeX, planeY - 14f, vy = -560f)
            fireCd = 0.18f
            ctx.sound.shoot()
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (stage != Stage.PLAY || gameOver) return

        if (fireCd > 0f) fireCd -= dt
        // Derive held-state from d-pad freshness (no release event → no drift).
        val ctrlActive = time - dpadFreshT < 0.16f
        leftHeld = ctrlActive && dpadX < 0; rightHeld = ctrlActive && dpadX > 0
        upHeld = ctrlActive && dpadY < 0; downHeld = ctrlActive && dpadY > 0
        // Player movement.
        val accX = (if (leftHeld) -1f else 0f) + (if (rightHeld) 1f else 0f)
        val accY = (if (upHeld) -1f else 0f) + (if (downHeld) 1f else 0f)
        planeVx = accX * planeMaxV
        planeVy = accY * planeMaxV * 0.7f
        planeX = (planeX + planeVx * dt).coerceIn(planeW / 2f, W - planeW / 2f)
        planeY = (planeY + planeVy * dt).coerceIn(H * 0.30f, H - planeH / 2f - 8f)

        // World scrolls.
        scrollSpeed = (80f + (score / 250f) * 4f).coerceAtMost(240f)
        distance += scrollSpeed * dt
        spawnAcc += scrollSpeed * dt

        // Add new segments at the top as we scroll down.
        while (spawnAcc >= segH) {
            spawnAcc -= segH
            // Vary width first so the serpentine clamp uses the current channel width.
            riverWidth = (riverWidth + (Random.nextFloat() - 0.5f) * 8f).coerceIn(140f, 280f)
            // Serpentine river centre: two sine waves so the channel visibly snakes
            // left/right with bigger amplitude and more frequent bends.
            riverPhase += 0.34f
            val margin = riverWidth / 2f + 12f
            val span = (W / 2f) - margin            // max offset that keeps banks on-screen
            val swing = span * 0.85f                 // strong amplitude (≈85% of available room)
            val target = W / 2f +
                swing * sin(riverPhase) +
                swing * 0.35f * sin(riverPhase * 2.3f + 0.7f)
            // Ease toward target so segments stay continuous (no kinks/teleports).
            val lastCenter = riverCenters.first()
            val nx = (lastCenter + (target - lastCenter) * 0.5f).coerceIn(margin, W - margin)
            riverCenters.addFirst(nx)
            // Bridge every ~ 25 segments.
            bridgeProgress++
            val isBridge = bridgeProgress >= 25f
            if (isBridge) {
                riverSegMeta.addFirst(SegMeta(SegKind.BRIDGE, nx))
                bridgeProgress = 0f
            } else {
                riverSegMeta.addFirst(SegMeta(SegKind.OPEN, 0f))
            }
            if (riverCenters.size > (H / segH).toInt() + 4) { riverCenters.removeLast(); riverSegMeta.removeLast() }

            // Spawn enemies on new segment.
            spawnEnemyMaybe(nx)
        }

        // Existing entities scroll down with the world.
        for (e in enemies) e.y += scrollSpeed * dt
        enemies.removeAll { it.y > H + 30f || !it.alive }

        // Bullets.
        for (b in bullets) b.y += b.vy * dt
        bullets.removeAll { it.y < -10f }

        // Fuel drain.
        fuel -= dt * 5f
        if (fuel <= 0f) { lives = 0; die(ctx); return }

        // Collisions: bullets vs enemies.
        val deadB = mutableSetOf<Int>()
        for (ei in enemies.indices.reversed()) {
            val e = enemies[ei]
            if (!e.alive) continue
            // Bridges (kind 4) are handled ONLY by the dedicated block below —
            // in this generic loop a bridge hit indexed listOf(40,90,150)[4]
            // and crashed with IndexOutOfBounds.
            if (e.kind != 4) for (bi in bullets.indices) {
                if (bi in deadB) continue
                val b = bullets[bi]
                if (abs(b.x - e.x) < 18f && abs(b.y - e.y) < 14f) {
                    deadB += bi; explosions += Offset(e.x, e.y)
                    if (e.kind == 3) { fuel = (fuel + 32f).coerceAtMost(100f); score += 80; ctx.sound.eat() }
                    else { score += listOf(40, 90, 150)[e.kind]; ctx.sound.explosion() }
                    e.alive = false; break
                }
            }
            // Bridge as enemy (kind 4) — destroying gives big bonus + next stage.
            if (e.kind == 4 && e.alive) {
                for (bi in bullets.indices) {
                    if (bi in deadB) continue
                    val b = bullets[bi]
                    if (abs(b.x - e.x) < 60f && abs(b.y - e.y) < 12f) {
                        deadB += bi; explosions += Offset(e.x, e.y)
                        score += 500; ctx.sound.bigExplosion()
                        e.alive = false; break
                    }
                }
            }
        }
        if (deadB.isNotEmpty()) {
            val survivors = bullets.filterIndexed { i, _ -> i !in deadB }
            bullets.clear(); bullets.addAll(survivors)
        }

        // Plane vs enemy.
        if (respawnShield > 0f) respawnShield -= dt
        for (e in enemies) {
            if (!e.alive) continue
            if (respawnShield > 0f && e.kind != 3) continue   // mercy window (fuel still collectable)
            if (abs(e.x - planeX) < 20f && abs(e.y - planeY) < 22f) {
                if (e.kind == 3) { fuel = (fuel + 32f).coerceAtMost(100f); e.alive = false; ctx.sound.eat() }
                else { explosions += Offset(planeX, planeY); die(ctx); return }
            }
            if (e.kind == 4 && abs(e.y - planeY) < 14f && abs(e.x - planeX) < riverWidth / 2f + planeW / 2f) {
                explosions += Offset(planeX, planeY); die(ctx); return
            }
        }
        // Plane vs riverbank.
        val rowIdx = ((planeY) / segH).toInt().coerceIn(0, riverCenters.size - 1)
        val rc = riverCenters[rowIdx]
        if (planeX - planeW / 2f < rc - riverWidth / 2f || planeX + planeW / 2f > rc + riverWidth / 2f) {
            explosions += Offset(planeX, planeY); die(ctx)
        }
    }

    private fun spawnEnemyMaybe(centerX: Float) {
        val rng = Random.nextFloat()
        when {
            rng < 0.10f -> {
                // Helicopter
                enemies += Enemy(centerX + (Random.nextFloat() - 0.5f) * 80f, -12f, vx = (Random.nextFloat() - 0.5f) * 60f, vy = 0f, kind = 2)
            }
            rng < 0.18f -> {
                // Jet
                enemies += Enemy(centerX + (Random.nextFloat() - 0.5f) * riverWidth * 0.6f, -12f, vx = 0f, vy = 0f, kind = 1)
            }
            rng < 0.28f -> {
                // Boat
                enemies += Enemy(centerX + (Random.nextFloat() - 0.5f) * riverWidth * 0.5f, -12f, vx = 0f, vy = 0f, kind = 0)
            }
            rng < 0.38f -> {
                // Fuel
                enemies += Enemy(centerX, -12f, vx = 0f, vy = 0f, kind = 3)
            }
            else -> Unit
        }
        // If this segment is a bridge — add bridge entity at y = top (so it scrolls down across the plane).
        val meta = riverSegMeta.firstOrNull()
        if (meta?.kind == SegKind.BRIDGE) {
            enemies += Enemy(centerX, -12f, vx = 0f, vy = 0f, kind = 4)
        }
    }

    private fun die(ctx: GameContext) {
        ctx.sound.bigExplosion()
        lives--
        if (lives <= 0) {
            gameOver = true; ctx.sound.gameOver()
            if (score > best) { best = score; ctx.scores.set(id, score) }
        } else {
            planeY = H - 110f; fuel = (fuel + 30f).coerceAtMost(100f)
            // Respawn in the CURRENT river channel, not at screen center — the
            // serpentine river often has grass at W/2, which killed the fresh
            // plane on the next frame and cascaded the remaining lives away.
            val rowIdx = (planeY / segH).toInt().coerceIn(0, riverCenters.size - 1)
            planeX = riverCenters[rowIdx]
            respawnShield = 1.5f
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        if (stage == Stage.TITLE) { drawTitleScreen(this, ctx); return@with }

        // Banks (green).
        drawRect(Color(0xFF2A6818), topLeft = Offset.Zero, size = Size(W, H))
        // Grass texture.
        for (i in 0 until 60) {
            val gx = ((i * 137 + 7) % W.toInt()).toFloat()
            val gy = (((i * 89 + 13) + time * 30f) % H.toInt()).toFloat()
            drawRect(Color(0xFF4CAF50), topLeft = Offset(gx, gy), size = Size(1f, 3f))
        }
        // River — drawn from segments.
        for ((i, rc) in riverCenters.withIndex()) {
            val y = i * segH
            drawRect(Color(0xFF1A4078), topLeft = Offset(rc - riverWidth / 2f, y), size = Size(riverWidth, segH + 1f))
            // bank ridges.
            drawRect(Color(0xFF154E14), topLeft = Offset(rc - riverWidth / 2f - 4f, y), size = Size(4f, segH + 1f))
            drawRect(Color(0xFF154E14), topLeft = Offset(rc + riverWidth / 2f, y), size = Size(4f, segH + 1f))
            // Water ripples.
            if ((i + (time * 4f).toInt()) % 4 == 0)
                drawRect(Color(0xFF4488CC), topLeft = Offset(rc - 6f, y + 4f), size = Size(12f, 1f))
        }
        // Bridge spans.
        for ((i, meta) in riverSegMeta.withIndex()) if (meta.kind == SegKind.BRIDGE) {
            val y = i * segH
            // Bridge deck across whole width.
            drawRect(Color(0xFF885028), topLeft = Offset(0f, y - 4f), size = Size(W, 14f))
            drawRect(Color(0xFFAA7038), topLeft = Offset(0f, y - 3f), size = Size(W, 4f))
            for (k in 0 until 12) drawRect(Color(0xFF5A2A10), topLeft = Offset(k * 40f, y - 3f), size = Size(2f, 12f))
        }

        // Enemies.
        for (e in enemies) when (e.kind) {
            0 -> {
                drawRect(Color(0xFF444444), topLeft = Offset(e.x - 14f, e.y - 5f), size = Size(28f, 10f))
                drawRect(Color(0xFF888888), topLeft = Offset(e.x - 12f, e.y - 7f), size = Size(24f, 8f))
                drawRect(Color(0xFF555555), topLeft = Offset(e.x - 3f, e.y - 12f), size = Size(6f, 6f))
                drawRect(Color(0xFFFFFF80), topLeft = Offset(e.x - 1f, e.y - 10f), size = Size(2f, 2f))
            }
            1 -> {
                drawRect(Color(0xFF888888), topLeft = Offset(e.x - 16f, e.y - 2f), size = Size(32f, 6f))
                drawRect(Color(0xFFAAAAAA), topLeft = Offset(e.x - 5f, e.y - 10f), size = Size(10f, 16f))
                drawRect(Color(0xFFFF4444), topLeft = Offset(e.x - 1f, e.y + 6f), size = Size(2f, 2f))
            }
            2 -> {
                val rot = (((time * 25f).toInt()) % 2)
                drawRect(Color(0xFF888888), topLeft = Offset(e.x - 14f + rot * 4f, e.y - 12f), size = Size(28f - rot * 4f, 2f))
                drawRect(Color(0xFFCC44CC), topLeft = Offset(e.x - 10f, e.y - 6f), size = Size(20f, 12f))
                drawRect(Color(0xFF222244), topLeft = Offset(e.x + 6f, e.y - 4f), size = Size(8f, 4f))
            }
            3 -> {
                drawRect(Color(0xFF884400), topLeft = Offset(e.x - 12f, e.y - 14f), size = Size(24f, 24f))
                drawRect(Color(0xFFCCCC22), topLeft = Offset(e.x - 10f, e.y - 12f), size = Size(20f, 20f))
                drawDosText("F", x = e.x, y = e.y + 4f, color = Color(0xFF884400), size = 14f, bold = true, align = TextAlign.CENTER)
            }
            4 -> {
                // Bridge highlighted target.
                drawRect(Color(0xFFFF6666), topLeft = Offset(0f, e.y - 4f), size = Size(W, 12f))
                drawDosText(ctx.tr("BRIDGE — bomb it", "МОСТ — бомби его"), x = W / 2f, y = e.y + 5f, color = Color.White, size = 11f, bold = true, align = TextAlign.CENTER)
            }
        }
        // Bullets.
        for (b in bullets) {
            drawRect(Color.White, topLeft = Offset(b.x - 2f, b.y - 8f), size = Size(4f, 10f))
            drawRect(Color(0xFFFFFF44), topLeft = Offset(b.x - 1f, b.y - 8f), size = Size(2f, 10f))
        }
        // Plane — clean top-down JET (Atari-era simple, but readable: pointed
        // nose, swept wings, tail fin, cockpit). Same footprint as before.
        run {
            val px = planeX; val py = planeY
            val noseY = py - planeH / 2f          // tip of nose (top)
            val tailY = py + planeH / 2f          // tail (bottom)
            val body = Color(0xFFBFC4CC)          // light steel
            val bodyHi = Color(0xFFE6E9EE)
            val bodyShade = Color(0xFF8A9099)
            val accent = Color(0xFFC0392B)         // tail-fin / markings red
            // Swept wings (delta), trailing edge swept back toward tail.
            val wing = Path().apply {
                moveTo(px - 28f, py + 8f)          // left wingtip (rear)
                lineTo(px - 4f, py - 6f)           // root leading
                lineTo(px - 4f, py + 6f)           // root trailing
                lineTo(px - 18f, py + 12f)         // left trailing edge
                close()
            }
            val wingR = Path().apply {
                moveTo(px + 28f, py + 8f)
                lineTo(px + 4f, py - 6f)
                lineTo(px + 4f, py + 6f)
                lineTo(px + 18f, py + 12f)
                close()
            }
            drawPath(wing, bodyShade)
            drawPath(wingR, bodyShade)
            // Tail plane (smaller swept fins near rear).
            val tailL = Path().apply {
                moveTo(px - 12f, tailY - 2f); lineTo(px - 2f, tailY - 12f)
                lineTo(px - 2f, tailY - 4f); close()
            }
            val tailR = Path().apply {
                moveTo(px + 12f, tailY - 2f); lineTo(px + 2f, tailY - 12f)
                lineTo(px + 2f, tailY - 4f); close()
            }
            drawPath(tailL, bodyShade); drawPath(tailR, bodyShade)
            // Fuselage: pointed nose triangle + rectangular body.
            val nose = Path().apply {
                moveTo(px, noseY)                  // pointed nose
                lineTo(px - 5f, noseY + 12f)
                lineTo(px + 5f, noseY + 12f)
                close()
            }
            drawPath(nose, bodyHi)
            drawRect(body, topLeft = Offset(px - 5f, noseY + 11f), size = Size(10f, planeH - 13f))
            // Center highlight stripe for a bit of shading.
            drawRect(bodyHi, topLeft = Offset(px - 1.5f, noseY + 12f), size = Size(3f, planeH - 16f))
            // Vertical tail fin (small accent at the rear).
            drawRect(accent, topLeft = Offset(px - 2f, tailY - 8f), size = Size(4f, 7f))
            // Cockpit canopy.
            drawRect(Color(0xFF1B3A66), topLeft = Offset(px - 3f, noseY + 13f), size = Size(6f, 6f))
            drawRect(Color(0xFF5B9BD5), topLeft = Offset(px - 2f, noseY + 14f), size = Size(4f, 2f))
            // Engine exhaust flicker out the tail.
            val ex = (((time * 20f).toInt()) % 2) + 2f
            drawRect(Color(0xFFFF8800), topLeft = Offset(px - 3f, tailY - 1f), size = Size(2f, ex))
            drawRect(Color(0xFFFFEE44), topLeft = Offset(px + 1f, tailY - 1f), size = Size(2f, ex))
        }

        // Explosions.
        for (e in explosions) {
            drawRect(Color(0xFFFFAA22), topLeft = Offset(e.x - 14f, e.y - 14f), size = Size(28f, 28f))
            drawRect(Color(0xFFFFEE44), topLeft = Offset(e.x - 8f, e.y - 8f), size = Size(16f, 16f))
        }
        explosions.clear()

        // HUD.
        drawRect(Color.Black.copy(alpha = 0.6f), topLeft = Offset.Zero, size = Size(W, 32f))
        drawDosText("SCORE  $score", x = 6f, y = 22f, color = Color.White, size = 13f, bold = true)
        drawDosText("LV $lives", x = W / 2f, y = 22f, color = Color(0xFFFFAA22), size = 13f, bold = true, align = TextAlign.CENTER)
        val fc = if (fuel < 30f) Color(0xFFFF5555) else Color(0xFF22CC22)
        drawDosText("FUEL", x = W - 130f, y = 22f, color = fc, size = 11f, bold = true)
        drawRect(Color(0xFF333333), topLeft = Offset(W - 100f, 14f), size = Size(90f, 8f))
        drawRect(fc, topLeft = Offset(W - 100f, 14f), size = Size((fuel.coerceAtLeast(0f) / 100f) * 90f, 8f))
        drawDosText("BEST $best", x = W - 6f, y = 22f, color = Color(0xFFFFD600), size = 10f, bold = true, align = TextAlign.RIGHT)

        if (gameOver) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("MISSION FAILED", "ЗАДАНИЕ ПРОВАЛЕНО"), x = W / 2f, y = H / 2f - 10f, color = Color(0xFFFF3333), size = 22f, bold = true, align = TextAlign.CENTER)
            drawDosText("Score  $score", x = W / 2f, y = H / 2f + 16f, color = Color.White, size = 14f, align = TextAlign.CENTER)
            drawDosText("● FIRE — RESTART", x = W / 2f, y = H / 2f + 44f, color = Color(0xFFCCCCCC), size = 12f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun drawTitleScreen(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))
        drawDosText("R I V E R   R A I D", x = W / 2f, y = H * 0.22f, color = Color(0xFF44CC22), size = 28f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("v2 · Activision 1982 spirit", "v2 · в духе Activision 1982"), x = W / 2f, y = H * 0.28f, color = Color(0xFF88FF88), size = 11f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Fly down the river. Don't hit the banks.", "Лети вдоль реки. Не касайся берегов."), x = W / 2f, y = H * 0.42f, color = Color.White, size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Shoot boats, jets, helicopters.", "Сбивай катера, самолёты, вертолёты."), x = W / 2f, y = H * 0.48f, color = Color.White, size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Yellow «F» — fuel. Bomb bridges.", "Жёлтая «F» — топливо. Мосты — бомбить."), x = W / 2f, y = H * 0.54f, color = Color.White, size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("D-pad — steer. ● FIRE — guns.", "Крестовина — руль. ● ОГОНЬ — пушки."), x = W / 2f, y = H * 0.66f, color = Color(0xFFCCCCCC), size = 12f, align = TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText("● FIRE — START", x = W / 2f, y = H * 0.82f, color = Color(0xFF44CC22), size = 16f, bold = true, align = TextAlign.CENTER)
        drawDosText("BEST $best", x = W / 2f, y = H * 0.92f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.CENTER)
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (stage == Stage.TITLE) startRun()
        else if (gameOver) startRun()
    }
}
