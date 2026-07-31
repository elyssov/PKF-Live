package com.pixelclassics.app.games.pitfallclassic

import com.pixelclassics.app.compat.pxfmt

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
 * PITFALL CLASSIC — screen-based exploration as Activision 1982 did it.
 * Distinct from our endless-runner JUNGLE RUN. Harry runs left/right
 * across discrete scenes, jumps over rolling logs / pits / scorpions /
 * crocodile mouths, swings on vines over tar pits, and dives into
 * underground tunnels via holes.
 *
 * 64 procedurally seeded scenes (looping), 24 gold/silver bars +
 * money bags + diamond rings as treasures. 20-minute mission clock.
 *
 * D-pad → walk + climb. FIRE → jump (and grab the vine if one is
 * overhead at jump apex).
 */
class PitfallClassicGame : Game {
    override val id: String = "pitfall_classic"
    override val titleResId: Int = R.string.game_pitfall_classic
    override val backgroundColor: Color = Color(0xFF0E5A0E)
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    override val introText: String = PIT_JUMPER_INTRO_EN
    override fun introTextFor(lang: String): String = pickPitJumperIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.JUNGLE_PARCHMENT
    override val helpLines: List<String> = listOf(
        "D-pad — run · ● FIRE — jump (catch a vine at apex)",
        "▲ ▼ at a ladder — go to / return from the tunnels",
        "Find 24 treasures within 20 minutes — pits, fire, crocs, scorpions",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — бег · ● ОГОНЬ — прыжок (лиана ловится в верхней точке)",
        "▲ ▼ у лестницы — спуск в тоннели / возврат",
        "Найди 24 сокровища за 20 минут — ямы, огонь, крокодилы, скорпионы",
    ) else helpLines

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    // Layout — surface band, ground band, underground band.
    private val skyY = 0f
    private val canopyY = 60f
    private val surfaceY = 280f         // Harry stands on top of this
    private val groundY = 320f
    private val tunnelY = 380f          // underground floor

    private val playerW = 26f
    private val playerH = 44f

    private enum class Hazard { NONE, OPEN_PIT, TAR_PIT, ROLLING_LOG, SCORPION, CROC_TRIO, FIRE_PIT, LADDER }

    private data class Scene(
        val hazard: Hazard,
        val vineX: Float = -1f,    // -1 if no vine
        val treasure: Treasure? = null,
        val tunnelBlock: Boolean = false, // brick wall in tunnel?
    )

    private enum class Treasure { GOLD_BAR, SILVER_BAR, MONEY_BAG, DIAMOND_RING }

    private val sceneCount = 64
    private val scenes: List<Scene> by lazy { buildScenes() }

    private fun buildScenes(): List<Scene> {
        val rng = Random(20260527L)
        val list = ArrayList<Scene>(sceneCount)
        for (i in 0 until sceneCount) {
            val haz = when (rng.nextInt(10)) {
                0, 1 -> Hazard.OPEN_PIT
                2 -> Hazard.TAR_PIT
                3, 4 -> Hazard.ROLLING_LOG
                5 -> Hazard.SCORPION
                6 -> Hazard.CROC_TRIO
                7 -> Hazard.FIRE_PIT
                8 -> Hazard.LADDER
                else -> Hazard.NONE
            }
            val vineX = if (haz == Hazard.TAR_PIT || haz == Hazard.CROC_TRIO || rng.nextFloat() < 0.15f)
                W / 2f + (rng.nextFloat() - 0.5f) * 80f
            else -1f
            val treasure = if (rng.nextFloat() < 0.42f) Treasure.values().random() else null
            list += Scene(haz, vineX, treasure, tunnelBlock = rng.nextFloat() < 0.18f)
        }
        return list
    }

    // Player state.
    private var sceneIdx = 0
    private var underground = false
    private var px = 60f
    private var py = surfaceY - playerH
    private var pvy = 0f
    private var pdir = 1               // 1 right, -1 left
    private var grounded = true
    private var onVine = false
    private var vineAngle = 0f         // radians, 0 = vertical down, swings left/right
    private var vineAngularVel = 0f
    private var leftHeld = false
    private var rightHeld = false
    // D-pad gives auto-repeat pulses but NO release event, so we track a
    // freshness window instead of latching held-flags (the latch made Harry
    // walk forever with no way to stop — Юджин: «игра нерабочая»).
    private var dpadX = 0
    private var dpadFreshT = -1f
    private var walkFrame = 0f
    private var time = 0f

    // Per-scene mutable state.
    private var logX = 0f               // rolling log
    private var collected: HashSet<Int> = HashSet()    // scene indices already looted

    private var score = 0
    private var best = 0
    private var lives = 3
    private var timer = 20f * 60f       // 20 minutes
    private var gameOver = false
    private var won = false
    private var msg = ""
    private var msgTimer = 0f
    private var hitFlash = 0f
    private var deathPause = 0f
    private enum class Stage { TITLE, PLAY }
    private var stage = Stage.TITLE

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        stage = Stage.TITLE
        msg = ""
    }

    private var lastCtx: GameContext? = null
    private fun t(en: String, ru: String) = lastCtx?.tr(en, ru) ?: en

    private fun startRun() {
        sceneIdx = 0; underground = false
        px = 60f; py = surfaceY - playerH; pvy = 0f; pdir = 1; grounded = true
        onVine = false; vineAngle = 0f; vineAngularVel = 0f
        score = 0; lives = 3; timer = 20f * 60f
        gameOver = false; won = false; collected.clear()
        stage = Stage.PLAY
        logX = W + 50f
        msg = t("SCENE 1/$sceneCount", "СЦЕНА 1/$sceneCount"); msgTimer = 1.5f
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (stage != Stage.PLAY || gameOver) return
        if (dx != 0) { dpadX = dx; dpadFreshT = time }
        // Up/down on a ladder cell triggers level switch.
        if (dy != 0) {
            val sc = scenes[sceneIdx]
            if (sc.hazard == Hazard.LADDER && abs(px - W / 2f) < 40f) {
                if (dy > 0 && !underground) {
                    underground = true; py = tunnelY - playerH; grounded = true
                    ctx.sound.click()
                } else if (dy < 0 && underground) {
                    underground = false; py = surfaceY - playerH; grounded = true
                    ctx.sound.click()
                }
            }
        }
    }

    override fun onFire(ctx: GameContext) {
        if (stage != Stage.PLAY) { startRun(); return }
        if (gameOver) { startRun(); return }
        // Jump. v6.2.2 fix: bumped pvy/walkSpeed so a clean jump reliably
        // clears every hazard width. Старое (pvy=-360 + walk=140) давало
        // ~91 px дальности при 100-px ямах = непроходимо с первого шага.
        if (grounded && !onVine) {
            pvy = -430f; grounded = false
            ctx.sound.click()
        } else if (onVine) {
            // Let go — fly forward.
            onVine = false
            pvy = -120f
            ctx.sound.click()
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        lastCtx = ctx
        time += dt
        if (msgTimer > 0f) msgTimer -= dt
        if (stage != Stage.PLAY || gameOver) return

        timer -= dt
        if (timer <= 0f) {
            gameOver = true; ctx.sound.gameOver()
            if (score > best) { best = score; ctx.scores.set(id, score) }
            msg = t("TIME OUT · PRESS SPACE", "ВРЕМЯ ВЫШЛО · ЖМИ ПРОБЕЛ")
            return
        }
        if (deathPause > 0f) { deathPause -= dt; return }
        if (hitFlash > 0f) hitFlash -= dt

        val scene = scenes[sceneIdx]
        val walkSpeed = 165f

        // Derive walk-held from the d-pad freshness window (no release event).
        val ctrlActive = time - dpadFreshT < 0.16f
        leftHeld = ctrlActive && dpadX < 0
        rightHeld = ctrlActive && dpadX > 0

        // Vine swing physics.
        if (onVine) {
            // Pendulum: theta'' = -g/L sin(theta)
            val g = 600f; val L = 110f
            vineAngularVel += (-g / L) * sin(vineAngle) * dt
            vineAngularVel *= 0.999f
            vineAngle += vineAngularVel * dt
            val anchorX = scene.vineX
            val anchorY = canopyY + 6f
            px = anchorX + sin(vineAngle) * L - playerW / 2f
            py = anchorY + cos(vineAngle) * L - playerH / 2f
            pdir = if (vineAngularVel > 0f) 1 else -1
        } else {
            // Walking.
            if (leftHeld) { px -= walkSpeed * dt; pdir = -1; walkFrame += dt * 8f }
            if (rightHeld) { px += walkSpeed * dt; pdir = 1; walkFrame += dt * 8f }
            // Gravity.
            pvy += 1100f * dt
            py += pvy * dt
            val floorY = (if (underground) tunnelY else surfaceY) - playerH
            if (py >= floorY) { py = floorY; pvy = 0f; grounded = true }
            else grounded = false

            // Vine attach if jumping near it on surface.
            if (!underground && scene.vineX > 0f && !grounded) {
                val px2 = px + playerW / 2f
                if (abs(px2 - scene.vineX) < 18f && py < canopyY + 110f && pvy < -50f) {
                    onVine = true
                    // start swing from current x offset.
                    val anchorY = canopyY + 6f
                    vineAngle = kotlin.math.atan2((px2 - scene.vineX), (py + playerH / 2f - anchorY))
                    vineAngularVel = pdir * 1.4f
                }
            }
        }

        // Scene transition.
        if (px < -playerW) {
            sceneIdx = (sceneIdx - 1 + sceneCount) % sceneCount
            px = W - 4f; onVine = false; logX = W + 50f
            msg = t("SCENE ${sceneIdx + 1}/$sceneCount", "СЦЕНА ${sceneIdx + 1}/$sceneCount"); msgTimer = 1.2f
        } else if (px > W) {
            sceneIdx = (sceneIdx + 1) % sceneCount
            px = -playerW + 4f; onVine = false; logX = W + 50f
            msg = t("SCENE ${sceneIdx + 1}/$sceneCount", "СЦЕНА ${sceneIdx + 1}/$sceneCount"); msgTimer = 1.2f
        }

        // Scene hazards (surface only).
        if (!underground) {
            when (scene.hazard) {
                Hazard.OPEN_PIT -> {
                    val pitL = W / 2f - 36f; val pitR = W / 2f + 36f
                    if (px + playerW > pitL && px < pitR && py + playerH >= surfaceY - 2f) hitPlayer(ctx, ctx.tr("Fell in a pit", "Упал в яму"))
                }
                Hazard.TAR_PIT -> {
                    val pitL = W / 2f - 50f; val pitR = W / 2f + 50f
                    if (px + playerW > pitL && px < pitR && py + playerH >= surfaceY - 2f && !onVine) hitPlayer(ctx, ctx.tr("Sank in tar", "Утонул в смоле"))
                }
                Hazard.FIRE_PIT -> {
                    val pitL = W / 2f - 36f; val pitR = W / 2f + 36f
                    if (px + playerW > pitL && px < pitR && py + playerH >= surfaceY - 2f) hitPlayer(ctx, ctx.tr("Burned in fire", "Сгорел в огне"))
                }
                Hazard.CROC_TRIO -> {
                    // Three croc heads spaced across; mouths open/close.
                    for (k in 0 until 3) {
                        val cx = W / 2f - 80f + k * 80f
                        val mouthOpen = (((time * 1.6f) + k * 0.6f) % 2.5f) > 1.2f
                        if (mouthOpen && abs(px + playerW / 2f - cx) < 22f && py + playerH >= surfaceY - 6f && !onVine) {
                            hitPlayer(ctx, ctx.tr("Croc snapped", "Крокодил цапнул")); break
                        }
                    }
                }
                Hazard.SCORPION -> {
                    // Underground hazard mostly — surface duplicate too: small scorpion patrolling.
                    val sx = W / 2f + sin(time * 1.5f) * 100f
                    if (abs(px + playerW / 2f - sx) < 12f && py + playerH >= surfaceY - 6f) hitPlayer(ctx, ctx.tr("Scorpion stung", "Скорпион ужалил"))
                }
                Hazard.ROLLING_LOG -> {
                    logX -= 220f * dt
                    if (logX < -40f) logX = W + 50f
                    if (abs(px + playerW / 2f - logX) < 22f && py + playerH > surfaceY - 18f) hitPlayer(ctx, ctx.tr("Log hit you", "Бревно сбило"))
                }
                Hazard.LADDER -> Unit
                Hazard.NONE -> Unit
            }
        } else {
            // Underground: brick wall blocks if scene says so; player must go up and over.
            if (scene.tunnelBlock) {
                val wallX = W / 2f
                if (abs(px + playerW / 2f - wallX) < 14f) {
                    // bounce back from wall.
                    if (pdir > 0) px = wallX - 18f - playerW / 2f else px = wallX + 18f - playerW / 2f
                }
            }
        }

        // Treasure pickup.
        scene.treasure?.let { tr ->
            if (!collected.contains(sceneIdx)) {
                val tx = W / 2f + 60f
                val ty = if (underground) tunnelY - 24f else surfaceY - 26f
                if (underground == (sceneIdx % 5 == 0) && abs(px + playerW / 2f - tx) < 22f && abs(py + playerH - ty) < 28f) {
                    collected += sceneIdx
                    score += when (tr) {
                        Treasure.GOLD_BAR -> 5000
                        Treasure.SILVER_BAR -> 3000
                        Treasure.MONEY_BAG -> 2000
                        Treasure.DIAMOND_RING -> 4000
                    }
                    ctx.sound.score()
                    msg = when (tr) { Treasure.GOLD_BAR -> t("GOLD +5000", "ЗОЛОТО +5000")
                        Treasure.SILVER_BAR -> t("SILVER +3000", "СЕРЕБРО +3000")
                        Treasure.MONEY_BAG -> t("BAG +2000", "МЕШОК +2000")
                        Treasure.DIAMOND_RING -> t("DIAMOND +4000", "БРИЛЛИАНТ +4000") }
                    msgTimer = 1.6f
                    // 20 of the 27 spawned treasures is the win line: the seeded
                    // map leaves exactly 24 physically reachable, so demanding
                    // all 24 meant a 100%-perfect-run-only victory (and any
                    // future seed tweak could silently make the game unwinnable).
                    if (collected.size >= 20) {
                        won = true
                        gameOver = true   // actually END the run — the win screen is gated on gameOver
                        score += 5000
                        ctx.sound.win()
                        if (score > best) { best = score; ctx.scores.set(id, score) }
                    }
                }
            }
        }
    }

    private fun hitPlayer(ctx: GameContext, reason: String) {
        if (deathPause > 0f) return
        lives--
        hitFlash = 0.4f
        deathPause = 1f
        ctx.sound.die()
        msg = "$reason · ${lives} LIVES LEFT"; msgTimer = 2f
        if (lives <= 0) {
            gameOver = true
            ctx.sound.gameOver()
            if (score > best) { best = score; ctx.scores.set(id, score) }
        } else {
            px = 60f; py = surfaceY - playerH; pvy = 0f; underground = false; onVine = false
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        if (stage == Stage.TITLE) { drawTitleScreen(this, ctx); return@with }

        // Sky.
        drawRect(Color(0xFFD98A4A), topLeft = Offset.Zero, size = Size(W, canopyY))
        // Canopy band.
        drawRect(Color(0xFF1A5A1A), topLeft = Offset(0f, canopyY), size = Size(W, 60f))
        for (i in 0 until 8) {
            val cx = i * 80f + 40f
            drawRect(Color(0xFF0A4B0A), topLeft = Offset(cx - 30f, canopyY), size = Size(60f, 28f))
            drawRect(Color(0xFF128512), topLeft = Offset(cx - 20f, canopyY + 4f), size = Size(40f, 14f))
        }
        // Open sky between canopy clusters.
        drawRect(Color(0xFFD98A4A), topLeft = Offset(0f, canopyY + 60f), size = Size(W, 100f))
        // Mid-jungle.
        drawRect(Color(0xFF0E5A0E), topLeft = Offset(0f, canopyY + 60f), size = Size(W, surfaceY - canopyY - 60f - 40f))

        // Surface stripe.
        drawRect(Color(0xFF8B5A2B), topLeft = Offset(0f, surfaceY), size = Size(W, groundY - surfaceY))
        drawRect(Color(0xFF5A3010), topLeft = Offset(0f, surfaceY), size = Size(W, 3f))
        // Dirt-ground band below surface.
        drawRect(Color(0xFF4A2A10), topLeft = Offset(0f, groundY), size = Size(W, tunnelY - groundY))
        // Underground tunnel floor.
        drawRect(Color(0xFF2A1808), topLeft = Offset(0f, tunnelY), size = Size(W, H - tunnelY))
        drawRect(Color(0xFF4A2A10), topLeft = Offset(0f, tunnelY), size = Size(W, 2f))
        // Decorate underground: brick texture.
        for (r in 0 until 4) for (c in 0 until 32) {
            val bx = (c * 20f - (r % 2) * 10f)
            val by = tunnelY + 20f + r * 20f
            drawRect(Color(0xFF6A3818), topLeft = Offset(bx, by), size = Size(18f, 4f))
        }

        val scene = scenes[sceneIdx]

        // Hazards rendering.
        when (scene.hazard) {
            Hazard.OPEN_PIT -> {
                drawRect(Color.Black, topLeft = Offset(W / 2f - 36f, surfaceY), size = Size(72f, groundY - surfaceY))
            }
            Hazard.TAR_PIT -> {
                drawRect(Color(0xFF1A1A1A), topLeft = Offset(W / 2f - 50f, surfaceY), size = Size(100f, groundY - surfaceY))
                drawRect(Color(0xFF2A2A2A), topLeft = Offset(W / 2f - 44f, surfaceY + 4f), size = Size(88f, 4f))
            }
            Hazard.FIRE_PIT -> {
                drawRect(Color.Black, topLeft = Offset(W / 2f - 36f, surfaceY), size = Size(72f, groundY - surfaceY))
                // Flickering flame.
                val k = (time * 8f).toInt() % 2
                for (i in 0 until 5) {
                    drawRect(Color(0xFFFF6600), topLeft = Offset(W / 2f - 34f + i * 16f, surfaceY - 12f - k * 2f), size = Size(12f, 14f + k * 2f))
                    drawRect(Color(0xFFFFCC22), topLeft = Offset(W / 2f - 32f + i * 16f, surfaceY - 8f), size = Size(8f, 10f))
                }
            }
            Hazard.CROC_TRIO -> {
                // Atari-2600 chunk crocs (Юджин: развести Classic от Джангла —
                // «свои монстры»): single-green stepped jaws out of fat blocks,
                // square teeth, one block eye. 4px is the smallest unit.
                drawRect(Color(0xFF1B5A8A), topLeft = Offset(W / 2f - 120f, surfaceY), size = Size(240f, groundY - surfaceY))   // water
                drawRect(Color(0xFF4B89BB), topLeft = Offset(W / 2f - 120f, surfaceY + 2f), size = Size(240f, 3f))
                for (k in 0 until 3) {
                    val cx = W / 2f - 80f + k * 80f
                    val mouthOpen = (((time * 1.6f) + k * 0.6f) % 2.5f) > 1.2f
                    val g = Color(0xFF4C9C22)
                    // Lower jaw + body block sitting in the water.
                    drawRect(g, topLeft = Offset(cx - 28f, surfaceY - 8f), size = Size(56f, 8f))
                    // Snout step (upper jaw) — lifts when the mouth opens.
                    val jawLift = if (mouthOpen) 10f else 4f
                    drawRect(g, topLeft = Offset(cx - 28f, surfaceY - 8f - jawLift), size = Size(36f, 4f))
                    drawRect(g, topLeft = Offset(cx - 4f, surfaceY - 8f - jawLift + 4f), size = Size(12f, jawLift - 4f + 0.1f))
                    // Square 2600 teeth in the gap.
                    if (mouthOpen) {
                        for (tth in 0 until 4)
                            drawRect(Color.White, topLeft = Offset(cx - 26f + tth * 8f, surfaceY - 12f), size = Size(4f, 4f))
                    }
                    // Block eye on the brow.
                    drawRect(Color.White, topLeft = Offset(cx - 24f, surfaceY - 8f - jawLift - 4f), size = Size(4f, 4f))
                    drawRect(Color.Black, topLeft = Offset(cx - 23f, surfaceY - 8f - jawLift - 3f), size = Size(2f, 2f))
                }
            }
            Hazard.SCORPION -> {
                // 2600 chunk scorpion: bone-white blocks, stepped tail with a
                // square sting, two-frame leg shuffle.
                val sx = W / 2f + sin(time * 1.5f) * 100f
                val dir = if (cos(time * 1.5f) >= 0f) 1f else -1f
                val bone = Color(0xFFE8E0C8)
                val frame = (time * 6f).toInt() % 2
                // Body: three fat blocks.
                drawRect(bone, topLeft = Offset(sx - 12f, surfaceY - 10f), size = Size(24f, 6f))
                drawRect(bone, topLeft = Offset(sx - 8f, surfaceY - 13f), size = Size(16f, 4f))
                // Claws: two stubs ahead of travel.
                drawRect(bone, topLeft = Offset(sx + dir * 14f - 2f, surfaceY - 10f), size = Size(6f, 4f))
                drawRect(bone, topLeft = Offset(sx + dir * 17f - 2f, surfaceY - 13f), size = Size(4f, 4f))
                // Stepped tail rising behind, square sting on top.
                drawRect(bone, topLeft = Offset(sx - dir * 14f - 2f, surfaceY - 15f), size = Size(4f, 6f))
                drawRect(bone, topLeft = Offset(sx - dir * 17f - 2f, surfaceY - 19f), size = Size(4f, 6f))
                drawRect(Color(0xFFDD3311), topLeft = Offset(sx - dir * 17f - 2f, surfaceY - 23f), size = Size(4f, 4f))
                // Legs, two-frame shuffle.
                for (lg in 0 until 3) {
                    val lx = sx - 8f + lg * 8f + (if (frame == 0) 0f else 2f)
                    drawRect(bone, topLeft = Offset(lx, surfaceY - 4f), size = Size(2f, 4f))
                }
            }
            Hazard.ROLLING_LOG -> {
                drawRect(Color(0xFF6B3A1A), topLeft = Offset(logX - 22f, surfaceY - 18f), size = Size(44f, 16f))
                drawRect(Color(0xFFA0622D), topLeft = Offset(logX - 20f, surfaceY - 16f), size = Size(40f, 4f))
                drawRect(Color(0xFFA0622D), topLeft = Offset(logX - 20f, surfaceY - 8f), size = Size(40f, 4f))
                drawRect(Color(0xFF3A1A08), topLeft = Offset(logX - 2f, surfaceY - 8f), size = Size(2f, 2f))
            }
            Hazard.LADDER -> {
                drawRect(Color(0xFFCCAA44), topLeft = Offset(W / 2f - 12f, surfaceY), size = Size(24f, groundY - surfaceY))
                for (r in 0 until 4) drawRect(Color(0xFFAA7822), topLeft = Offset(W / 2f - 10f, surfaceY + 6f + r * 8f), size = Size(20f, 2f))
                // Hatch under.
                drawRect(Color.Black, topLeft = Offset(W / 2f - 14f, surfaceY - 2f), size = Size(28f, 4f))
            }
            Hazard.NONE -> Unit
        }

        // Vine (if any).
        if (scene.vineX > 0f && !underground) {
            val ax = scene.vineX; val ay = canopyY + 6f
            val L = 110f
            val tipX = ax + sin(if (onVine) vineAngle else 0f) * L
            val tipY = ay + cos(if (onVine) vineAngle else 0f) * L
            drawLine(Color(0xFF552211), Offset(ax, ay), Offset(tipX, tipY), strokeWidth = 3f)
            drawLine(Color(0xFF7A3D1F), Offset(ax, ay), Offset(tipX, tipY), strokeWidth = 1f)
            drawCircle(Color(0xFF7A3D1F), radius = 4f, center = Offset(tipX, tipY))
        }

        // Treasure (visible only if not yet collected and in correct band).
        scene.treasure?.let { tr ->
            if (!collected.contains(sceneIdx)) {
                val onUnderground = sceneIdx % 5 == 0
                if (onUnderground == underground) {
                    val tx = W / 2f + 60f
                    val ty = if (underground) tunnelY - 24f else surfaceY - 26f
                    val color = when (tr) {
                        Treasure.GOLD_BAR -> Color(0xFFFFD600)
                        Treasure.SILVER_BAR -> Color(0xFFCCCCCC)
                        Treasure.MONEY_BAG -> Color(0xFFAAFF66)
                        Treasure.DIAMOND_RING -> Color(0xFF66FFFF)
                    }
                    drawRect(Color.Black, topLeft = Offset(tx - 12f, ty), size = Size(24f, 22f))
                    drawRect(color, topLeft = Offset(tx - 10f, ty + 2f), size = Size(20f, 18f))
                    drawRect(Color.White.copy(alpha = 0.7f), topLeft = Offset(tx - 8f, ty + 4f), size = Size(16f, 2f))
                    val labels = mapOf(Treasure.GOLD_BAR to "AU", Treasure.SILVER_BAR to "AG",
                        Treasure.MONEY_BAG to "$", Treasure.DIAMOND_RING to "◆")
                    drawDosText(labels[tr] ?: "", x = tx, y = ty + 16f, color = Color.Black, size = 12f, bold = true, align = TextAlign.CENTER)
                }
            }
        }

        // Harry sprite.
        drawHarry(this, px, py, pdir, walkFrame, !grounded || onVine, hitFlash > 0f)

        // HUD.
        drawRect(Color.Black.copy(alpha = 0.5f), topLeft = Offset(0f, 0f), size = Size(W, 36f))
        drawDosText("SCORE  $score", x = 10f, y = 22f, color = Color.White, size = 14f, bold = true)
        drawDosText("SCENE ${sceneIdx + 1}/$sceneCount", x = W / 2f, y = 22f, color = Color.White, size = 12f, bold = true, align = TextAlign.CENTER)
        val min = (timer / 60f).toInt(); val sec = (timer % 60f).toInt()
        val tcol = if (timer < 60f) Color(0xFFFF5555) else Color.White
        drawDosText("TIME %02d:%02d".pxfmt(min, sec), x = W - 100f, y = 22f, color = tcol, size = 12f, bold = true, align = TextAlign.RIGHT)
        drawDosText("♥ x$lives", x = W - 10f, y = 22f, color = Color(0xFFFFAA00), size = 14f, bold = true, align = TextAlign.RIGHT)
        drawDosText("LOOT ${collected.size}/24", x = 10f, y = H - 8f, color = Color(0xFFFFD600), size = 11f, bold = true)
        drawDosText("BEST $best", x = W - 10f, y = H - 8f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.RIGHT)

        if (msgTimer > 0f) {
            val alpha = (msgTimer.coerceAtMost(1f))
            drawRect(Color.Black.copy(alpha = 0.6f * alpha), topLeft = Offset(0f, H / 2f - 20f), size = Size(W, 36f))
            drawDosText(msg, x = W / 2f, y = H / 2f + 4f, color = Color(0xFFFFD600).copy(alpha = alpha), size = 16f, bold = true, align = TextAlign.CENTER)
        }
        if (gameOver) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(if (won) ctx.tr("ALL TREASURES!", "ВСЕ СОКРОВИЩА!") else "GAME OVER", x = W / 2f, y = H / 2f - 20f,
                color = if (won) Color(0xFF55FF55) else Color(0xFFFF3333), size = 28f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("SCORE  $score   ·   FIRE — RESTART", "СЧЁТ  $score   ·   ОГОНЬ — РЕСТАРТ"), x = W / 2f, y = H / 2f + 16f, color = Color.White, size = 14f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun drawTitleScreen(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color(0xFF0E5A0E), topLeft = Offset.Zero, size = Size(W, H))
        drawRect(Color(0xFF1A5A1A), topLeft = Offset(0f, 60f), size = Size(W, 60f))
        drawDosText("P I T   J U M P E R", x = W / 2f, y = H * 0.30f, color = Color(0xFFFCBC3C), size = 38f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Activision 1982 · tribute edition", "Activision 1982 · трибьют"), x = W / 2f, y = H * 0.38f, color = Color(0xFFEEC09A), size = 12f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Find 24 treasures in the jungle within 20 minutes.", "Найди 24 сокровища в джунглях за 20 минут."), x = W / 2f, y = H * 0.50f, color = Color.White, size = 13f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("D-pad — run · ▲▼ at a ladder — tunnels.", "Крестовина — бег · ▲▼ у лестницы — тоннели."), x = W / 2f, y = H * 0.57f, color = Color.White, size = 12f, align = TextAlign.CENTER)
        drawDosText("● FIRE — jump (catch a vine at apex).", x = W / 2f, y = H * 0.63f, color = Color.White, size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Hazards: pits, tar, fire, crocodiles,", "Опасности: ямы, смола, огонь, крокодилы,"), x = W / 2f, y = H * 0.70f, color = Color(0xFFFFAA88), size = 11f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("scorpions, logs, brick walls in tunnels.", "скорпионы, брёвна, кладка в тоннелях."), x = W / 2f, y = H * 0.74f, color = Color(0xFFFFAA88), size = 11f, align = TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText("● FIRE — GO", x = W / 2f, y = H * 0.86f, color = Color(0xFFFCBC3C), size = 18f, bold = true, align = TextAlign.CENTER)
        drawDosText("BEST $best", x = W / 2f, y = H * 0.93f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.CENTER)
    }

    private fun drawHarry(scope: DrawScope, x: Float, y: Float, dir: Int, frame: Float, airborne: Boolean, hurt: Boolean) = with(scope) {
        if (hurt && (time * 12f).toInt() % 2 == 0) return@with
        val skin = Color(0xFFEEC09A)
        val shirt = Color(0xFFFCBC3C)
        val pant = Color(0xFF4A2818)
        val hair = Color(0xFFAA5A28)
        // Hair.
        drawRect(hair, topLeft = Offset(x + 6f, y), size = Size(playerW - 12f, 4f))
        // Head.
        drawRect(skin, topLeft = Offset(x + 8f, y + 4f), size = Size(playerW - 16f, 10f))
        val eyeOff = if (dir > 0) 0f else -2f
        drawRect(Color.Black, topLeft = Offset(x + 14f + eyeOff, y + 7f), size = Size(2f, 2f))
        // Shirt + belt.
        drawRect(shirt, topLeft = Offset(x + 5f, y + 14f), size = Size(playerW - 10f, 12f))
        drawRect(Color(0xFF604010), topLeft = Offset(x + 5f, y + 24f), size = Size(playerW - 10f, 2f))
        drawRect(Color(0xFFFCD060), topLeft = Offset(x + 12f, y + 24f), size = Size(4f, 2f))
        // Pants + legs.
        drawRect(pant, topLeft = Offset(x + 5f, y + 26f), size = Size(playerW - 10f, 10f))
        val step = (frame.toInt() % 2)
        if (airborne) {
            drawRect(pant, topLeft = Offset(x + 5f, y + 34f), size = Size(8f, 8f))
            drawRect(pant, topLeft = Offset(x + 13f, y + 34f), size = Size(8f, 8f))
            drawRect(shirt, topLeft = Offset(if (dir > 0) x - 2f else x + playerW - 3f, y + 8f), size = Size(5f, 10f))
        } else {
            val l1 = if (step == 0) 0f else 2f
            val l2 = if (step == 0) 2f else 0f
            drawRect(pant, topLeft = Offset(x + 5f, y + 36f - l1), size = Size(7f, 6f + l1))
            drawRect(pant, topLeft = Offset(x + 14f, y + 36f - l2), size = Size(7f, 6f + l2))
            drawRect(shirt, topLeft = Offset(x + 1f, y + 16f), size = Size(5f, 10f))
            drawRect(shirt, topLeft = Offset(x + playerW - 6f, y + 16f), size = Size(5f, 10f))
        }
        // Boots.
        drawRect(Color(0xFF1A0A04), topLeft = Offset(x + 5f, y + 41f), size = Size(7f, 3f))
        drawRect(Color(0xFF1A0A04), topLeft = Offset(x + 14f, y + 41f), size = Size(7f, 3f))
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (stage == Stage.TITLE) startRun()
    }
}
