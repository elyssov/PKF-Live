package com.pixelclassics.app.games.pitfall

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

/** Pitfall — side-scroll jungle run, pits + crocs + vines + gold + scorpions.
 *
 *  Controls (DPAD_AND_FIRE):
 *    FIRE  — jump (also: dismiss legend / restart on game-over).
 *    D-pad — press LEFT or RIGHT to fire the revolver forward (6-shot,
 *            reloaded by rare ammo drums). Kept off FIRE so jump stays
 *            instant and the gun can't waste shots on every hop.
 */
class PitfallGame : Game {
    override val id: String = "pitfall"
    override val titleResId: Int = R.string.game_jungle_run
    override val backgroundColor: Color = Color(0xFF0E5A0E)
    override val controls: GameControls = GameControls.TWO_BUTTONS
    override val buttonLabels: Pair<String, String> = "JUMP" to "SHOOT"
    override val introText: String = PITFALL_INTRO_EN
    override fun introTextFor(lang: String): String = pickPitfallIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.JUNGLE_PARCHMENT
    override val helpLines: List<String> get() = listOf(
        "GREEN — jump over pits, crocs & logs",
        "RED — fire the revolver (6 shots, reload at drums)",
        "Goal: run as far as you can; grab gold, dodge hazards",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "ЗЕЛЁНАЯ — прыжок через ямы, крокодилов и брёвна",
        "КРАСНАЯ — выстрел из револьвера (6 патронов, барабаны — перезарядка)",
        "Цель: беги как можно дальше; хватай золото, уворачивайся",
    ) else helpLines

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private val groundY = 340f
    private val playerW = 28f
    private val playerH = 48f
    private val gravity = 1300f
    private val jumpV = -520f
    private val runSpeed = 200f

    private enum class Hazard { PIT, CROC, SNAKE, LOG, GOLD, HILL, PLATFORM, COIN, MEDKIT, AMMO, ENEMY, BAT }
    private data class Obstacle(
        var x: Float,
        val kind: Hazard,
        val w: Float,
        var alive: Boolean = true,
        val h: Float = 0f,
        val baseY: Float = 0f,      // BAT: vertical centre of the sine path
        val phase: Float = 0f,      // BAT: per-bat sine offset
    ) {
        var y: Float = 0f           // current top (set each frame for flyers)
    }

    private data class Bullet(var x: Float, val y: Float, var alive: Boolean = true)

    // Game phases: LEGEND screen first, then the running game.
    private enum class Stage { LEGEND, RUN }
    private var stage = Stage.LEGEND

    private var px = 80f
    private var py = groundY - playerH
    private var pvy = 0f
    private var grounded = true
    private var invuln = 0f            // post-hit mercy window, seconds
    // Юджин, 18.07: приседание — скорость вдвое, выстрел идёт низко и
    // достаёт скорпиона и змею (стоя пуля летит поверх их голов).
    private var crouch = false
    private val obstacles = mutableListOf<Obstacle>()
    private val bullets = mutableListOf<Bullet>()
    private var distance = 0f
    private var nextSpawn = 280f
    private var score = 0
    private var bonusScore = 0
    private var best = 0
    private var lives = 3
    private var ammo = 6
    private var muzzle = 0f          // muzzle-flash timer for draw
    private var timer = 99f
    private var gameOver = false
    private var time = 0f

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        restart()
        stage = Stage.LEGEND
    }

    private fun restart() {
        px = 80f; py = groundY - playerH; pvy = 0f; grounded = true; crouch = false
        obstacles.clear(); bullets.clear(); distance = 0f; nextSpawn = 280f
        score = 0; bonusScore = 0; lives = 3; ammo = 6; muzzle = 0f; timer = 99f; gameOver = false
        // Initial obstacles.
        for (i in 0 until 6) {
            obstacles += newObstacle(W + i * 180f)
        }
    }

    private fun newObstacle(x: Float): Obstacle {
        val roll = Random.nextFloat()
        return when {
            roll < 0.18f -> Obstacle(x, Hazard.PIT, 70f)
            roll < 0.27f -> Obstacle(x, Hazard.CROC, 80f)
            roll < 0.36f -> Obstacle(x, Hazard.SNAKE, 36f)
            roll < 0.44f -> Obstacle(x, Hazard.LOG, 36f)
            roll < 0.50f -> Obstacle(x, Hazard.GOLD, 26f)
            roll < 0.56f -> Obstacle(x, Hazard.ENEMY, 30f)                 // killable ground scorpion
            roll < 0.64f -> Obstacle(                                       // killable Castlevania bat
                x, Hazard.BAT, 26f, baseY = 150f + Random.nextFloat() * 90f, phase = Random.nextFloat() * 6.283f,
            )
            roll < 0.66f -> Obstacle(x, Hazard.MEDKIT, 24f)                // rare health pickup
            roll < 0.69f -> Obstacle(x, Hazard.AMMO, 26f)                  // rare ammo drum
            roll < 0.80f -> Obstacle(x, Hazard.HILL, 90f, h = 30f + Random.nextFloat() * 20f)
            roll < 0.92f -> Obstacle(x, Hazard.PLATFORM, 130f, h = 70f + Random.nextFloat() * 30f)
            else -> Obstacle(x, Hazard.COIN, 22f, h = 60f + Random.nextFloat() * 40f)
        }
    }

    private fun jump(ctx: GameContext) {
        if (stage == Stage.LEGEND) { stage = Stage.RUN; ctx.sound.click(); return }
        if (gameOver) { restart(); stage = Stage.RUN; return }
        if (grounded) { pvy = jumpV; grounded = false; ctx.sound.click() }
    }

    /** Fire the revolver if loaded; otherwise dry-click. */
    private fun shoot(ctx: GameContext) {
        if (stage != Stage.RUN || gameOver) return
        if (ammo <= 0) { ctx.sound.pcError(); return }
        ammo--
        muzzle = 0.08f
        val low = crouch && grounded
        bullets += Bullet(px + playerW, py + playerH * (if (low) 0.85f else 0.4f))
        ctx.sound.shoot()
    }

    override fun onFire(ctx: GameContext) = jump(ctx)          // GREEN button (left)

    override fun onSecondary(ctx: GameContext) = shoot(ctx)    // RED button (right)

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        crouch = dy > 0
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (stage != Stage.RUN || gameOver) return
        timer -= dt
        if (muzzle > 0f) muzzle -= dt
        if (timer <= 0f) { lives = 0; gameOver = true; ctx.sound.gameOver(); saveBest(ctx); return }

        // Scroll world. Crouching Harry half-steps — the price of aiming low.
        val sp = if (crouch && grounded) runSpeed * 0.5f else runSpeed
        for (o in obstacles) o.x -= sp * dt
        distance += sp * dt
        score = (distance / 4f).toInt() + bonusScore

        // Move bullets (fast, forward only).
        for (b in bullets) b.x += 520f * dt
        bullets.removeAll { it.x > W + 20f || !it.alive }

        // Spawn new.
        if (obstacles.last().x < W) obstacles += newObstacle(obstacles.last().x + 180f + Random.nextFloat() * 80f)
        obstacles.removeAll { it.x + 100f < 0f }

        // Place flyers' current Y (sine wave) and give static kinds a sensible top.
        for (o in obstacles) {
            o.y = when (o.kind) {
                Hazard.BAT -> o.baseY + sin(time * 3f + o.phase).toFloat() * 50f
                Hazard.ENEMY -> groundY - 22f
                else -> o.y
            }
        }

        // Gravity.
        pvy += gravity * dt
        py += pvy * dt
        // Find what we're standing on: ground OR a platform / hill we're inside.
        var floor = groundY
        for (o in obstacles) {
            if (!o.alive) continue
            val overlap = (px + playerW) > o.x && px < (o.x + o.w)
            if (overlap) when (o.kind) {
                Hazard.PLATFORM -> {
                    val platTop = groundY - o.h
                    if (py + playerH <= platTop + 6f && pvy >= 0f && platTop < floor) floor = platTop
                }
                Hazard.HILL -> {
                    val hillTop = groundY - o.h
                    if (py + playerH <= hillTop + 6f && pvy >= 0f && hillTop < floor) floor = hillTop
                }
                else -> {}
            }
        }
        if (py + playerH >= floor) { py = floor - playerH; pvy = 0f; grounded = true } else grounded = false

        // Bullet vs killable enemy — swept over the bullet's frame travel so a
        // large-dt frame (~36px relative step) can't jump clean over a 26px
        // target between two collision tests.
        for (b in bullets) {
            if (!b.alive) continue
            val bPrev = b.x - 520f * dt
            for (o in obstacles) {
                if (!o.alive) continue
                if (o.kind != Hazard.ENEMY && o.kind != Hazard.BAT && o.kind != Hazard.SNAKE) continue
                val top = if (o.kind == Hazard.BAT) o.y else groundY - 22f
                val bot = top + 22f
                if (b.x >= o.x && bPrev <= o.x + o.w && b.y >= top && b.y <= bot) {
                    o.alive = false; b.alive = false
                    bonusScore += if (o.kind == Hazard.BAT) 250 else 150
                    ctx.sound.explosion()
                    break
                }
            }
        }
        bullets.removeAll { !it.alive }

        // Hazard / pickup collisions. Hazards respect the post-hit mercy
        // window — without it the still-overlapping obstacle re-triggered
        // hit() every frame and drained all three lives in ~50 ms.
        if (invuln > 0f) invuln -= dt
        val vulnerable = invuln <= 0f
        for (o in obstacles) {
            if (!o.alive) continue
            val overlap = (px + playerW) > o.x && px < (o.x + o.w)
            when (o.kind) {
                Hazard.PIT -> if (vulnerable && overlap && py + playerH >= groundY) { hit(ctx); return }
                Hazard.CROC -> if (vulnerable && overlap && py + playerH >= groundY - 6f) { hit(ctx); return }
                Hazard.SNAKE -> if (vulnerable && overlap && py + playerH >= groundY - 12f) { hit(ctx); return }
                Hazard.LOG -> if (vulnerable && overlap && py + playerH >= groundY - 6f) { hit(ctx); return }
                Hazard.ENEMY -> if (vulnerable && overlap && py + playerH >= groundY - 18f) { hit(ctx); return }
                Hazard.BAT -> {
                    // Vertical overlap too — bats fly, so jumping over works.
                    if (vulnerable && overlap && py < o.y + 22f && py + playerH > o.y) { hit(ctx); return }
                }
                Hazard.GOLD -> if (overlap && py < groundY - 6f && py + playerH > groundY - 60f) {
                    o.alive = false; bonusScore += 500; ctx.sound.score()
                }
                Hazard.MEDKIT -> if (overlap && py + playerH >= groundY - 26f) {
                    o.alive = false; lives++; ctx.sound.powerUp()
                }
                Hazard.AMMO -> if (overlap && py + playerH >= groundY - 26f) {
                    o.alive = false; ammo = 6; ctx.sound.eat()
                }
                Hazard.COIN -> {
                    val cy = groundY - o.h
                    if (overlap && py < cy + 24f && py + playerH > cy - 24f) {
                        o.alive = false
                        // Extra life exactly on crossing a 1000-point boundary
                        // (the old check compared against the PRE-pickup score
                        // and granted farmable lives inside a 100-pt window).
                        val newScore = score + 100
                        bonusScore += 100
                        if (newScore / 1000 > score / 1000) { lives++; ctx.sound.powerUp() }
                        else ctx.sound.eat()
                    }
                }
                else -> {}
            }
        }
    }

    private fun hit(ctx: GameContext) {
        lives--
        ctx.sound.die()
        if (lives <= 0) { gameOver = true; saveBest(ctx); ctx.sound.gameOver() }
        else { px = 80f; py = groundY - playerH; pvy = 0f; grounded = true; invuln = 1.2f }
    }

    private fun saveBest(ctx: GameContext) {
        if (score > best) { best = score; ctx.scores.set(id, score) }
    }

    // ───── Icon helpers (shared by the legend and the world) ────────────────

    private fun DrawScope.drawMedkit(x: Float, top: Float, w: Float) {
        drawRect(Color(0xFFEFEFEF), topLeft = Offset(x, top), size = Size(w, 18f))
        drawRect(Color(0xFFD03030), topLeft = Offset(x + w / 2f - 2f, top + 3f), size = Size(4f, 12f))
        drawRect(Color(0xFFD03030), topLeft = Offset(x + 4f, top + 7f), size = Size(w - 8f, 4f))
    }

    private fun DrawScope.drawAmmo(x: Float, top: Float, w: Float) {
        // Brass revolver drum / clip.
        drawRect(Color(0xFF9A6B1A), topLeft = Offset(x, top + 4f), size = Size(w, 12f))
        drawRect(Color(0xFFD4A53A), topLeft = Offset(x + 2f, top + 6f), size = Size(w - 4f, 3f))
        for (k in 0 until 3) drawRect(Color(0xFFFFE08A), topLeft = Offset(x + 3f + k * 7f, top), size = Size(3f, 6f))
    }

    private fun DrawScope.drawScorpion(x: Float, top: Float, w: Float) {
        drawRect(Color(0xFF8A1F1F), topLeft = Offset(x, top + 10f), size = Size(w, 8f))
        drawRect(Color(0xFFB23030), topLeft = Offset(x + 2f, top + 11f), size = Size(w - 4f, 3f))
        drawRect(Color(0xFF8A1F1F), topLeft = Offset(x + w - 4f, top + 2f), size = Size(4f, 8f))   // tail up
        drawRect(Color.Black, topLeft = Offset(x + 3f, top + 12f), size = Size(2f, 2f))            // pincer/eye
    }

    private fun DrawScope.drawBat(x: Float, top: Float, w: Float, flap: Float) {
        val wing = (sin(flap).toFloat()) * 4f
        drawRect(Color(0xFF1A1030), topLeft = Offset(x + w / 2f - 4f, top + 6f), size = Size(8f, 8f))   // body
        drawRect(Color(0xFF2A2050), topLeft = Offset(x, top + 8f - wing), size = Size(w / 2f - 2f, 5f)) // L wing
        drawRect(Color(0xFF2A2050), topLeft = Offset(x + w / 2f + 2f, top + 8f - wing), size = Size(w / 2f - 2f, 5f))
        drawRect(Color(0xFFCC2222), topLeft = Offset(x + w / 2f - 3f, top + 8f), size = Size(2f, 2f))   // eyes
        drawRect(Color(0xFFCC2222), topLeft = Offset(x + w / 2f + 1f, top + 8f), size = Size(2f, 2f))
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Atari 2600 chunky sky bands.
        drawRect(Color(0xFF5A86C7), topLeft = Offset.Zero, size = Size(W, 30f))
        drawRect(Color(0xFF77AAD9), topLeft = Offset(0f, 30f), size = Size(W, 30f))
        // Distant mountains.
        val off2 = (distance * 0.15f) % 60f
        for (i in -1..12) {
            val mx = i * 60f - off2
            drawRect(Color(0xFF2D4A2D), topLeft = Offset(mx, 60f), size = Size(60f, 30f))
            drawRect(Color(0xFF3F663F), topLeft = Offset(mx + 10f, 70f), size = Size(40f, 20f))
            drawRect(Color(0xFFE8E0C0), topLeft = Offset(mx + 22f, 70f), size = Size(16f, 6f))   // snowcap
        }
        // Jungle canopy band (mid).
        drawRect(Color(0xFF1A5A1A), topLeft = Offset(0f, 90f), size = Size(W, 50f))
        // Jungle tree silhouettes (parallax, denser).
        val off = (distance * 0.3f) % 70f
        for (i in -1..10) {
            val x = i * 70f - off
            // Trunk.
            drawRect(Color(0xFF3A2A14), topLeft = Offset(x + 30f, 140f), size = Size(8f, 100f))
            drawRect(Color(0xFF553820), topLeft = Offset(x + 31f, 140f), size = Size(2f, 100f))
            // Foliage clumps (3 layers).
            drawRect(Color(0xFF073507), topLeft = Offset(x + 12f, 120f), size = Size(44f, 28f))
            drawRect(Color(0xFF0A4B0A), topLeft = Offset(x + 8f, 100f), size = Size(52f, 26f))
            drawRect(Color(0xFF128512), topLeft = Offset(x + 16f, 92f), size = Size(36f, 16f))
            // Spotty highlights.
            drawRect(Color(0xFF1FBA1F), topLeft = Offset(x + 22f, 102f), size = Size(4f, 4f))
            drawRect(Color(0xFF1FBA1F), topLeft = Offset(x + 32f, 122f), size = Size(4f, 4f))
        }
        // Ground.
        drawRect(Color(0xFF8B5A2B), topLeft = Offset(0f, groundY), size = Size(W, H - groundY))
        drawRect(Color(0xFF5A3010), topLeft = Offset(0f, groundY), size = Size(W, 4f))
        // Dirt clumps texture.
        val dOff = (distance * 1f) % 24f
        for (i in -1..40) {
            val dx = i * 24f - dOff
            drawRect(Color(0xFFA86833), topLeft = Offset(dx, groundY + 8f), size = Size(8f, 2f))
            drawRect(Color(0xFF6B3F1A), topLeft = Offset(dx + 12f, groundY + 18f), size = Size(6f, 2f))
        }

        if (stage == Stage.LEGEND) { drawLegend(ctx); return }

        // Obstacles.
        for (o in obstacles) {
            if (!o.alive) continue
            when (o.kind) {
                Hazard.PIT -> {
                    drawRect(Color.Black, topLeft = Offset(o.x, groundY), size = Size(o.w, H - groundY))
                }
                Hazard.CROC -> {
                    val sw = sin(time * 4f).toFloat()
                    val mouthOpen = sw > 0f
                    drawRect(Color(0xFF77AA22), topLeft = Offset(o.x, groundY - 16f), size = Size(o.w, 14f))
                    drawRect(Color(0xFF558811), topLeft = Offset(o.x + 6f, groundY - 4f), size = Size(o.w - 12f, 4f))
                    if (mouthOpen) drawRect(Color(0xFFEE6622), topLeft = Offset(o.x + 8f, groundY - 12f), size = Size(20f, 8f))
                }
                Hazard.SNAKE -> {
                    // Recognisable snake silhouette — coiled body + raised head with tongue.
                    // Юджин: «змей бы сделал чуть-чуть более похожими на змей».
                    val sx = o.x; val gy = groundY
                    // Coiled body (S-curve done with 3 stacked rects of different widths).
                    drawRect(Color(0xFF1F8A2F), topLeft = Offset(sx + 2f, gy - 6f),  size = Size(o.w - 4f, 5f))   // belly
                    drawRect(Color(0xFF2EAA40), topLeft = Offset(sx + 6f, gy - 10f), size = Size(o.w - 10f, 4f)) // mid
                    drawRect(Color(0xFF35C04D), topLeft = Offset(sx + 4f, gy - 13f), size = Size(o.w - 18f, 3f)) // upper-back curl
                    // Scale pattern — alternating dark dots along the back.
                    var k = 0f
                    while (k < o.w - 8f) { drawRect(Color(0xFF115F22), topLeft = Offset(sx + 4f + k, gy - 5f), size = Size(2f, 2f)); k += 5f }
                    // Raised head (forward end).
                    val hx = sx + o.w - 10f
                    drawRect(Color(0xFF2EAA40), topLeft = Offset(hx, gy - 18f), size = Size(8f, 4f))         // head
                    drawRect(Color(0xFF35C04D), topLeft = Offset(hx + 1f, gy - 19f), size = Size(6f, 2f))    // crown
                    drawRect(Color(0xFF2EAA40), topLeft = Offset(hx + 3f, gy - 14f), size = Size(2f, 4f))    // neck
                    // Eye + forked tongue (animated flicker).
                    drawRect(Color(0xFFFFE066), topLeft = Offset(hx + 5f, gy - 17f), size = Size(2f, 2f))
                    drawRect(Color.Black, topLeft = Offset(hx + 6f, gy - 17f), size = Size(1f, 1f))
                    if ((time * 6f).toInt() % 2 == 0) {
                        drawRect(Color(0xFFE12626), topLeft = Offset(hx + 8f, gy - 16f), size = Size(3f, 1f))
                        drawRect(Color(0xFFE12626), topLeft = Offset(hx + 10f, gy - 17f), size = Size(2f, 1f))
                        drawRect(Color(0xFFE12626), topLeft = Offset(hx + 10f, gy - 15f), size = Size(2f, 1f))
                    }
                }
                Hazard.LOG -> {
                    drawRect(Color(0xFF6B4022), topLeft = Offset(o.x, groundY - 16f), size = Size(o.w, 14f))
                    drawRect(Color(0xFF8B6034), topLeft = Offset(o.x + 4f, groundY - 12f), size = Size(o.w - 8f, 4f))
                }
                Hazard.GOLD -> {
                    drawRect(Color(0xFFFFD700), topLeft = Offset(o.x + 4f, groundY - 28f), size = Size(o.w - 8f, 12f))
                    drawRect(Color(0xFFFFFFAA), topLeft = Offset(o.x + 6f, groundY - 26f), size = Size(8f, 2f))
                }
                Hazard.MEDKIT -> drawMedkit(o.x, groundY - 24f, o.w)
                Hazard.AMMO -> drawAmmo(o.x, groundY - 24f, o.w)
                Hazard.ENEMY -> drawScorpion(o.x, groundY - 22f, o.w)
                Hazard.BAT -> drawBat(o.x, o.y, o.w, time * 12f + o.phase)
                Hazard.HILL -> {
                    // Pixel hill — sloped dirt mound.
                    val top = groundY - o.h
                    for (k in 0..o.h.toInt() step 3) {
                        val inset = (o.h.toInt() - k) * 0.5f
                        drawRect(Color(0xFF8B5A2B), topLeft = Offset(o.x + inset, top + k), size = Size(o.w - 2 * inset, 3f))
                    }
                    drawRect(Color(0xFFA07840), topLeft = Offset(o.x + 6f, top), size = Size(o.w - 12f, 2f))
                    // Grass tufts on top.
                    for (k in 0 until 3) drawRect(Color(0xFF22AA22), topLeft = Offset(o.x + 10f + k * 22f, top - 4f), size = Size(2f, 4f))
                }
                Hazard.PLATFORM -> {
                    // Wooden plank on two stilts.
                    val top = groundY - o.h
                    drawRect(Color(0xFF6B3A1A), topLeft = Offset(o.x, top - 6f), size = Size(o.w, 6f))
                    drawRect(Color(0xFFA0622D), topLeft = Offset(o.x, top - 4f), size = Size(o.w, 2f))
                    // Stilts.
                    drawRect(Color(0xFF552211), topLeft = Offset(o.x + 6f, top), size = Size(4f, o.h))
                    drawRect(Color(0xFF552211), topLeft = Offset(o.x + o.w - 10f, top), size = Size(4f, o.h))
                    // Bracing.
                    drawLine(Color(0xFF552211), Offset(o.x + 10f, top + 4f), Offset(o.x + o.w - 14f, groundY - 4f), strokeWidth = 1.5f)
                }
                Hazard.COIN -> {
                    val cy = groundY - o.h
                    val pulse = (sin(time * 4f) * 0.3f + 0.7f)
                    drawRect(Color(0xFFFFD600).copy(alpha = pulse), topLeft = Offset(o.x + 2f, cy - 12f), size = Size(o.w - 4f, 24f))
                    drawRect(Color(0xFFFFFFAA), topLeft = Offset(o.x + 6f, cy - 10f), size = Size(8f, 4f))
                    drawDosText("$", x = o.x + o.w / 2f, y = cy + 6f, color = Color(0xFF8B6914), size = 18f, bold = true, align = com.pixelclassics.app.ui.theme.TextAlign.CENTER)
                }
            }
        }

        // Bullets.
        for (b in bullets) {
            drawRect(Color(0xFFFFE066), topLeft = Offset(b.x - 6f, b.y - 1.5f), size = Size(8f, 3f))
            drawRect(Color.White, topLeft = Offset(b.x, b.y - 1f), size = Size(3f, 2f))
        }

        // Harry — Atari 2600 chunky sprite. Crouching squashes him toward
        // his boots: wider, low, revolver at snake-eye level.
        val squash = crouch && grounded
        scale(
            if (squash) 1.10f else 1f, if (squash) 0.62f else 1f,
            pivot = Offset(px + playerW / 2f, py + playerH),
        ) {
        val shirt = Color(0xFFFCBC3C)   // yellow shirt
        val skin = Color(0xFFEEC09A)
        val hair = Color(0xFFAA5A28)
        val pant = Color(0xFF4A2818)
        val boot = Color(0xFF2A1408)
        // Hair (cap).
        drawRect(hair, topLeft = Offset(px + 6f, py), size = Size(playerW - 12f, 4f))
        drawRect(Color(0xFF884420), topLeft = Offset(px + 6f, py), size = Size(playerW - 12f, 1f))
        // Head.
        drawRect(skin, topLeft = Offset(px + 8f, py + 4f), size = Size(playerW - 16f, 10f))
        // Eyes.
        drawRect(Color.Black, topLeft = Offset(px + 10f, py + 7f), size = Size(2f, 2f))
        drawRect(Color.Black, topLeft = Offset(px + 16f, py + 7f), size = Size(2f, 2f))
        // Shirt (torso).
        drawRect(shirt, topLeft = Offset(px + 5f, py + 14f), size = Size(playerW - 10f, 14f))
        drawRect(Color(0xFFAA7820), topLeft = Offset(px + 5f, py + 14f), size = Size(playerW - 10f, 1f))
        // Belt.
        drawRect(Color(0xFF604010), topLeft = Offset(px + 5f, py + 26f), size = Size(playerW - 10f, 2f))
        drawRect(Color(0xFFFCD060), topLeft = Offset(px + 12f, py + 26f), size = Size(4f, 2f))   // buckle
        // Pants.
        drawRect(pant, topLeft = Offset(px + 5f, py + 28f), size = Size(playerW - 10f, 12f))
        // Revolver in the off-hand, pointing right; muzzle flash when firing.
        drawRect(Color(0xFF3A3A3A), topLeft = Offset(px + playerW - 2f, py + 20f), size = Size(8f, 3f))
        if (muzzle > 0f) drawRect(Color(0xFFFFE066), topLeft = Offset(px + playerW + 6f, py + 19f), size = Size(5f, 5f))
        // Arms — rough animation.
        val leg = ((time * 14f).toInt() % 2) == 0
        if (grounded) {
            // Swinging arms.
            if (leg) {
                drawRect(shirt, topLeft = Offset(px + 1f, py + 16f), size = Size(5f, 10f))
                drawRect(shirt, topLeft = Offset(px + playerW - 6f, py + 16f), size = Size(5f, 10f))
            } else {
                drawRect(shirt, topLeft = Offset(px + 1f, py + 18f), size = Size(5f, 10f))
                drawRect(shirt, topLeft = Offset(px + playerW - 6f, py + 14f), size = Size(5f, 10f))
            }
            // Legs running.
            if (leg) {
                drawRect(pant, topLeft = Offset(px + 5f, py + 40f), size = Size(8f, 6f))
                drawRect(boot, topLeft = Offset(px + 4f, py + 45f), size = Size(9f, 3f))
                drawRect(pant, topLeft = Offset(px + 15f, py + 40f), size = Size(8f, 8f))
                drawRect(boot, topLeft = Offset(px + 14f, py + 45f), size = Size(9f, 3f))
            } else {
                drawRect(pant, topLeft = Offset(px + 5f, py + 40f), size = Size(8f, 8f))
                drawRect(boot, topLeft = Offset(px + 4f, py + 45f), size = Size(9f, 3f))
                drawRect(pant, topLeft = Offset(px + 15f, py + 40f), size = Size(8f, 6f))
                drawRect(boot, topLeft = Offset(px + 14f, py + 45f), size = Size(9f, 3f))
            }
        } else {
            // Mid-jump pose — arms up, legs tucked.
            drawRect(shirt, topLeft = Offset(px - 2f, py + 8f), size = Size(5f, 10f))
            drawRect(shirt, topLeft = Offset(px + playerW - 3f, py + 8f), size = Size(5f, 10f))
            drawRect(pant, topLeft = Offset(px + 5f, py + 36f), size = Size(8f, 8f))
            drawRect(pant, topLeft = Offset(px + 15f, py + 36f), size = Size(8f, 8f))
            drawRect(boot, topLeft = Offset(px + 4f, py + 42f), size = Size(9f, 3f))
            drawRect(boot, topLeft = Offset(px + 14f, py + 42f), size = Size(9f, 3f))
        }
        }

        // HUD.
        drawDosText("SCORE  $score", x = 10f, y = 24f, color = Color.White, size = 16f, bold = true)
        drawDosText("TIME  ${timer.toInt()}", x = W / 2f, y = 24f, color = if (timer < 15f) Color(0xFFFF5555) else Color.White, size = 16f, bold = true, align = TextAlign.CENTER)
        drawDosText("LIVES  $lives", x = W - 10f, y = 24f, color = Color.White, size = 16f, bold = true, align = TextAlign.RIGHT)
        drawDosText("BEST  $best", x = W - 10f, y = 44f, color = Color(0xFFFFD600), size = 12f, bold = true, align = TextAlign.RIGHT)
        // Ammo gauge — six chambers, spent ones dim.
        drawDosText("AMMO", x = 10f, y = 44f, color = Color(0xFFE0C060), size = 12f, bold = true)
        for (k in 0 until 6) {
            val c = if (k < ammo) Color(0xFFFFD060) else Color(0x55FFD060)
            drawRect(c, topLeft = Offset(64f + k * 9f, 38f), size = Size(6f, 8f))
        }
        drawDosText(ctx.tr("GREEN = JUMP   RED = SHOOT   DOWN = CROUCH", "ЗЕЛЁНАЯ = ПРЫЖОК   КРАСНАЯ = ВЫСТРЕЛ   ВНИЗ = ПРИГНУТЬСЯ"), x = W / 2f, y = H - 12f, color = Color(0x99FFFFFF), size = 12f, align = TextAlign.CENTER)

        if (gameOver) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("GAME OVER", x = W / 2f, y = H / 2f - 20f, color = Color(0xFFFF3333), size = 36f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Score  $score", "Счёт  $score"), x = W / 2f, y = H / 2f + 20f, color = Color.White, size = 18f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"), x = W / 2f, y = H / 2f + 50f, color = Color(0xFFCCCCCC), size = 14f, align = TextAlign.CENTER)
        }
    }

    /** Pre-start briefing: every pickup / hazard with its real icon and verdict. */
    private fun DrawScope.drawLegend(ctx: GameContext) {
        drawRect(Color(0xE6000000), topLeft = Offset.Zero, size = Size(W, H))
        drawDosText("JUNGLE RUN", x = W / 2f, y = 52f, color = Color(0xFFFCBC3C), size = 34f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Run as far as you can — grab gold, dodge hazards, survive!", "Беги как можно дальше — хватай золото, уворачивайся, выживай!"), x = W / 2f, y = 80f, color = Color(0xFFAEE0AE), size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("PICK-UPS & HAZARDS", "НАХОДКИ И ОПАСНОСТИ"), x = W / 2f, y = 104f, color = Color.White, size = 14f, bold = true, align = TextAlign.CENTER)

        val iconX = 130f
        var rowY = 120f
        val step = 34f
        // entry: (draw icon at iconX,rowY) , label, verdict, verdictColor
        fun row(label: String, verdict: String, vColor: Color, icon: DrawScope.(Float) -> Unit) {
            icon(rowY)
            drawDosText(label, x = iconX + 50f, y = rowY + 12f, color = Color.White, size = 15f, align = TextAlign.LEFT)
            drawDosText(verdict, x = W - 70f, y = rowY + 12f, color = vColor, size = 15f, bold = true, align = TextAlign.RIGHT)
            rowY += step
        }
        val take = Color(0xFF55DD55)
        val avoid = Color(0xFFFF5555)

        row(ctx.tr("med-kit  (+1 life)", "аптечка  (+1 жизнь)"), ctx.tr("GRAB", "БЕРИ"), take) { y -> drawMedkit(iconX, y, 24f) }
        row(ctx.tr("ammo drum", "барабан патронов"), ctx.tr("GRAB", "БЕРИ"), take) { y -> drawAmmo(iconX, y, 26f) }
        row(ctx.tr("gold / coin", "золото / монета"), ctx.tr("GRAB", "БЕРИ"), take) { y ->
            drawRect(Color(0xFFFFD700), topLeft = Offset(iconX + 2f, y + 4f), size = Size(20f, 12f))
            drawRect(Color(0xFFFFFFAA), topLeft = Offset(iconX + 4f, y + 6f), size = Size(8f, 2f))
        }
        row(ctx.tr("snake", "змея"), ctx.tr("CROUCH+SHOOT / JUMP", "ПРИГНИСЬ И СТРЕЛЯЙ / ПРЫГАЙ"), avoid) { y ->
            drawRect(Color(0xFFD03030), topLeft = Offset(iconX, y + 4f), size = Size(36f, 14f))
            drawRect(Color.White, topLeft = Offset(iconX + 26f, y + 8f), size = Size(2f, 2f))
        }
        row(ctx.tr("pit / crocodile", "яма / крокодил"), ctx.tr("JUMP OVER", "ПРЫГАЙ"), avoid) { y ->
            drawRect(Color.Black, topLeft = Offset(iconX, y + 12f), size = Size(16f, 10f))
            drawRect(Color(0xFF77AA22), topLeft = Offset(iconX + 18f, y + 8f), size = Size(20f, 8f))
        }
        row(ctx.tr("scorpion", "скорпион"), ctx.tr("CROUCH+SHOOT / JUMP", "ПРИГНИСЬ И СТРЕЛЯЙ / ПРЫГАЙ"), take) { y -> drawScorpion(iconX, y, 30f) }
        row(ctx.tr("bat", "летучая мышь"), ctx.tr("SHOOT / JUMP", "СТРЕЛЯЙ / ПРЫГАЙ"), take) { y -> drawBat(iconX, y, 26f, time * 12f) }

        drawDosText(ctx.tr("GREEN = JUMP     RED = SHOOT", "ЗЕЛЁНАЯ = ПРЫЖОК     КРАСНАЯ = ВЫСТРЕЛ"), x = W / 2f, y = H - 54f, color = Color(0xFFAEE0AE), size = 14f, bold = true, align = TextAlign.CENTER)
        val blink = if ((time * 2f).toInt() % 2 == 0) Color(0xFFFFFFFF) else Color(0x66FFFFFF)
        drawDosText(ctx.tr("CLICK TO START", "КЛИК — СТАРТ"), x = W / 2f, y = H - 26f, color = blink, size = 16f, bold = true, align = TextAlign.CENTER)
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) = jump(ctx)
}
