package com.pixelclassics.app.games.lunar

import com.pixelclassics.app.compat.pxfmt

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random

/**
 * LUNAR LANDER — blueprint edition.
 *
 * Aesthetic brief from Юджин: «не ностальгично а ПРЯМО КРАСИВО.
 * Может в той же стилистике что и морской бой? Бумажный вайб?»
 *
 * Result: cream paper background + blue grid + blueprint-line spacecraft
 * (cyan ink, hatch shading), pencil-rough moon terrain, NASA-style
 * instrument readout HUD. The only warm colour is the thrust plume —
 * everything else is technical-drawing blue. Real Moon gravity
 * (1.62 m/s²), three procedural landing pads per level, fuel budget
 * that runs out if you fight too hard, twelve hand-tuned stages.
 */
class LunarLanderGame : Game {
    override val id: String = "lunar"
    override val titleResId: Int = R.string.game_lunar
    override val backgroundColor: Color = Color(0xFFEEE6CC)   // aged drafting paper
    override val introText: String = LUNAR_INTRO_EN
    override fun introTextFor(lang: String): String = pickLunarIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.BLUEPRINT
    override val helpLines: List<String> = listOf(
        "▲ / ● FIRE — thrust (hold)",
        "◀ ▶ — rotate.  Land flat on a PAD, slow and level",
        "Off-pad landing: half score.  Tilted/fast = crash",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "▲ / ● ОГОНЬ — тяга (держать)",
        "◀ ▶ — поворот.  Садись на ПЛОЩАДКУ: медленно и ровно",
        "Посадка мимо площадки: полсчёта.  Крен/скорость = крушение",
    ) else helpLines

    // Bigger virtual canvas — the lander now sits in a wider sky, with more
    // landscape visible at once. Юджин: «уменьшим масштаб, отодвинем камеру».
    private val W = 960f
    private val H = 600f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H
    // Side-gutter controls: D-pad UP = thrust, LEFT/RIGHT = rotate. FIRE = thrust too.
    override val controls: GameControls = GameControls.DPAD_AND_FIRE

    // ── Physics ──────────────────────────────────────────────────────────
    // 5 planets — each is a (gravity multiplier, atmosphere wind, descriptor).
    // Numbers are visual physics, not real-units; 'Moon' is the baseline.
    private data class Planet(
        val name: String,
        val gravityMul: Float,
        val windMax: Float,         // px/s² horizontal disturbance (0 = no atmosphere)
        val scoreMul: Float,
    )
    private val planets = listOf(
        Planet("MOON",    1.00f, 0f,    1.0f),
        Planet("MARS",    1.50f, 12f,   1.4f),
        Planet("TITAN",   0.55f, 18f,   1.2f),
        Planet("EARTH",   2.20f, 28f,   2.4f),
        Planet("JUPITER", 4.00f, 45f,   3.8f),
    )
    private var planetIdx = 0
    private val planet: Planet get() = planets[planetIdx.coerceIn(0, planets.size - 1)]
    // Mission setup choices.
    private var setupFuel = 100f      // 50/75/100/125/150 % of maxFuelMass — heavier tank, more burn time, smaller reward
    private var setupPadCount = 2     // 1/2/3 pads
    private val baseGravity = 1.62f * 8f
    private val gravity get() = baseGravity * planet.gravityMul
    // ── Mass-based rocket physics. ──────────────────────────────────────────
    // Thrust is a constant FORCE; acceleration = F / m. As fuel burns off the
    // lander gets lighter and the engine bites harder. A real Apollo LM left
    // lunar orbit at ~67% fuel-mass and touched down on dregs — we mirror that
    // ratio with dryMass = 40, maxFuelMass = 80 (full tank fuel-fraction 67%).
    // thrustForce is tuned so that a FULL tank on the Moon gives TWR ≈ 2.5 —
    // comfortable; dry-tank Moon TWR ≈ 7.5. Heavier planets (Earth, Jupiter)
    // sit at TWR < 1 with a full tank — you must burn weight off the airframe
    // before the engine can win against gravity. That's the gameplay loop.
    private val dryMass = 40f
    private val maxFuelMass = 80f
    private var fuelMass = 0f
    private val thrustForce = 3888f
    private val mdot = 3.5f          // mass / sec consumed while thrust on
    private val mass get() = dryMass + fuelMass
    private val thrustAccel get() = thrustForce / mass
    private val fuelPct get() = (fuelMass / maxFuelMass * 100f).coerceIn(0f, 150f)
    private val twr get() = thrustAccel / gravity
    // Touchdown tolerances scale with √gravity — a heavier planet bleeds more
    // kinetic energy into the airframe at the same impact speed, but absolute
    // landing-speed budget can be looser without becoming arcadey.
    private val safeVy get() = 22f * kotlin.math.sqrt(planet.gravityMul)
    private val safeVx get() = 18f * kotlin.math.sqrt(planet.gravityMul)
    private val rotateRate = 1.8f      // rad/sec
    // Wind — drifts the lander horizontally on atmospheric planets.
    private var windT = 0f
    private val windAccel get() = planet.windMax * kotlin.math.sin(windT * 0.7f).toFloat()

    private var x = W / 2f
    private var y = 60f
    private var vx = 0f
    private var vy = 0f
    private var angle = 0f          // 0 = up, +radians = clockwise
    private var thrustOn = false
    private var leftHeld = false
    private var rightHeld = false

    // The side D-pad emits auto-repeat pulses while held with NO release event,
    // so we record the last input + timestamp and derive 'held' in update() by
    // checking the pulse is fresh (within ~160 ms). FIRE counts as thrust.
    private var dpadX = 0
    private var dpadY = 0
    private var dpadFreshT = -1f
    private var fireFreshT = -1f

    private data class TerrainPoint(val x: Float, val y: Float)
    private var terrain: List<TerrainPoint> = emptyList()
    private data class Pad(val x1: Float, val x2: Float, val y: Float, val bonus: Int)
    private var pads: List<Pad> = emptyList()

    private var level = 1
    private var score = 0
    private var best = 0
    private var crashed = false
    private var landed = false
    private var landedBonus = 0
    private var landedMsg: String = ""
    private var crashMsg: String = ""
    private var time = 0f

    private enum class Stage { TITLE, SETUP, PLAY, LANDED, CRASHED }
    private var stage = Stage.TITLE
    /** Combined score multiplier from the chosen mission profile. */
    private val missionMultiplier: Float get() {
        val fuelFactor = when {
            setupFuel <= 60f -> 1.6f   // less fuel → less mass → higher reward
            setupFuel <= 90f -> 1.2f
            setupFuel <= 110f -> 1.0f
            else -> 0.8f                // overloaded — easier, smaller reward
        }
        val padFactor = when (setupPadCount) { 1 -> 1.5f; 2 -> 1.0f; 3 -> 0.7f; else -> 1.0f }
        return planet.scoreMul * fuelFactor * padFactor
    }

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        stage = Stage.TITLE
        level = 1; score = 0
    }

    private fun startLevel() {
        // Procedural terrain: 30-40 segments of jagged ridges, with N
        // flat landing pads (player-chosen via SETUP).
        val rng = Random((level * 7919L + planetIdx * 1009L) xor 0x6E15CA73L)
        val segCount = 32
        val padCount = setupPadCount.coerceAtLeast(1)
        val pts = ArrayList<TerrainPoint>(segCount + 1)
        val padIdx = IntArray(padCount) { rng.nextInt(3, segCount - 3) }
        var yy = H * 0.7f + rng.nextFloat() * 30f
        for (i in 0..segCount) {
            val xx = i * (W / segCount)
            // Jagged elsewhere; flat through pad spans.
            val onPad = padIdx.any { p -> i in p..p + (4 - level / 6).coerceAtLeast(1) }
            if (!onPad) {
                yy += (rng.nextFloat() - 0.45f) * (60f - level * 1.2f)
                yy = yy.coerceIn(H * 0.35f, H * 0.92f)
            }
            pts += TerrainPoint(xx, yy)
        }
        terrain = pts
        // Pads — flatten the actual y over the span, store bonus.
        val padsList = ArrayList<Pad>(padCount)
        for ((k, p) in padIdx.withIndex()) {
            val span = (4 - level / 6).coerceAtLeast(1)
            val yFlat = terrain[p].y
            for (j in p..p + span) if (j in terrain.indices) (terrain as MutableList)[j] = TerrainPoint(terrain[j].x, yFlat)
            val x1 = terrain[p].x
            val x2 = terrain[p + span].x
            val bonus = when {
                k == 0 && level > 2 -> 500  // hardest pad = highest bonus
                else -> 100 + level * 50
            }
            padsList += Pad(x1, x2, yFlat, bonus)
        }
        pads = padsList

        // Reset spacecraft.
        x = W / 2f + (rng.nextFloat() - 0.5f) * 200f
        y = 60f
        vx = (rng.nextFloat() - 0.5f) * 20f
        vy = 10f
        angle = 0f
        fuelMass = (setupFuel / 100f) * maxFuelMass
        thrustOn = false; leftHeld = false; rightHeld = false
        dpadX = 0; dpadY = 0; dpadFreshT = -1f; fireFreshT = -1f
        crashed = false; landed = false
        windT = 0f
        stage = Stage.PLAY
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (stage != Stage.PLAY) return
        // Derive held-state from the side D-pad / FIRE (fresh = pulse within ~160 ms).
        val ctrlActive = time - dpadFreshT < 0.16f
        leftHeld = ctrlActive && dpadX < 0
        rightHeld = ctrlActive && dpadX > 0
        thrustOn = (ctrlActive && dpadY < 0) || (time - fireFreshT < 0.16f)
        if (leftHeld) angle -= rotateRate * dt
        if (rightHeld) angle += rotateRate * dt
        angle = angle.coerceIn(-PI.toFloat() / 2f, PI.toFloat() / 2f)
        // Thrust along the lander's local up. F = const, a = F / m; the engine
        // bites harder as fuel burns off.
        if (thrustOn && fuelMass > 0f) {
            val a = thrustAccel
            val tx = sin(angle) * a
            val ty = -cos(angle) * a
            vx += tx * dt; vy += ty * dt
            fuelMass = (fuelMass - mdot * dt).coerceAtLeast(0f)
        }
        windT += dt
        // Wind pushes you sideways on atmospheric planets.
        vx += windAccel * dt
        vy += gravity * dt
        x += vx * dt; y += vy * dt
        // Walls — wrap to opposite side (you'll see it in HUD).
        if (x < 0f) x += W
        if (x > W) x -= W

        // Collision against terrain segments — sample the terrain across the
        // full footpad span so legs CAN'T pass through a ridge to one side of
        // the lander. We use the HIGHEST terrain point under the foot span
        // (= the first surface a leg can touch).
        val footHalf = 22f
        val xL = (x - footHalf).coerceIn(0f, W)
        val xR = (x + footHalf).coerceIn(0f, W)
        val ySampleL = findSegmentAt(xL)?.let { interpolateY(it, xL) } ?: H
        val ySampleC = findSegmentAt(x)?.let { interpolateY(it, x) } ?: H
        val ySampleR = findSegmentAt(xR)?.let { interpolateY(it, xR) } ?: H
        val terrainY = minOf(ySampleL, ySampleC, ySampleR)
        val standingOnPad = pads.firstOrNull { x in it.x1..it.x2 }
        // Approximately flat under both feet (height delta within a leg's
        // forgiveness) — allows landing on rough ground at half the pad score.
        val groundFlatEnough = kotlin.math.max(ySampleL, ySampleR) - kotlin.math.min(ySampleL, ySampleR) < 14f

        // 38f = footpad reach from centre (bot+22 in drawLanderBlueprint) — the
        // lander rests ON its legs, not sinking through the pad.
        if (y + 38f >= terrainY) {
            val safeVelocity = abs(vy) < safeVy && abs(vx) < safeVx && abs(angle) < 0.25f
            val onPad = standingOnPad != null
            val landingOK = onPad && safeVelocity
            val offPadOK = !onPad && safeVelocity && groundFlatEnough
            when {
                landingOK -> {
                    landed = true
                    val pad = standingOnPad!!
                    val mult = missionMultiplier
                    val bonus = ((pad.bonus + fuelPct * 2f) * mult).toInt()
                    score += bonus
                    if (score > best) { best = score; ctx.scores.set(id, score) }
                    landedBonus = bonus
                    landedMsg = ctx.tr("TOUCHDOWN on ${planet.name} — bonus +$bonus  (×%.1f)", "ПОСАДКА: ${planet.name} — бонус +$bonus  (×%.1f)").pxfmt(mult)
                    stage = Stage.LANDED
                    ctx.sound.win()
                }
                offPadOK -> {
                    landed = true
                    val mult = missionMultiplier
                    val bonus = (((100 + level * 50) / 2 + fuelPct) * mult).toInt()
                    score += bonus
                    if (score > best) { best = score; ctx.scores.set(id, score) }
                    landedBonus = bonus
                    landedMsg = ctx.tr("ROUGH LANDING — half score: +$bonus  (×%.1f)", "ЖЁСТКАЯ ПОСАДКА — полсчёта: +$bonus  (×%.1f)").pxfmt(mult)
                    stage = Stage.LANDED
                    ctx.sound.win()
                }
                else -> {
                    crashed = true; stage = Stage.CRASHED
                    // Decide the reason NOW — the velocities are zeroed below,
                    // so computing it at draw time always blamed the pad.
                    crashMsg = when {
                        abs(vy) >= safeVy -> ctx.tr("vertical descent too fast", "слишком быстрый спуск")
                        abs(vx) >= safeVx -> ctx.tr("horizontal drift too fast", "слишком быстрый боковой снос")
                        abs(angle) >= 0.25f -> ctx.tr("lander tilted at touchdown", "модуль сел с креном")
                        else -> ctx.tr("missed the landing pad", "мимо посадочной площадки")
                    }
                    ctx.sound.bigExplosion()
                }
            }
            // Snap so the footpads rest on the surface — never sink.
            y = terrainY - 38f
            vx = 0f; vy = 0f
        }
    }

    private fun findSegmentAt(xx: Float): Int? {
        for (i in 0 until terrain.size - 1) {
            if (xx in terrain[i].x..terrain[i + 1].x) return i
        }
        return null
    }
    private fun interpolateY(seg: Int, xx: Float): Float {
        val a = terrain[seg]; val b = terrain[seg + 1]
        val t = if (b.x == a.x) 0f else (xx - a.x) / (b.x - a.x)
        return a.y + (b.y - a.y) * t
    }

    // ── Drawing ──────────────────────────────────────────────────────────

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Drafting paper.
        drawRect(backgroundColor, topLeft = Offset.Zero, size = Size(W, H))
        // Speckles (paper age).
        for (i in 0 until 220) {
            val sx = ((i * 173 + 37) % W.toInt()).toFloat()
            val sy = ((i * 131 + 17) % H.toInt()).toFloat()
            drawRect(Color(0x14000000), topLeft = Offset(sx, sy), size = Size(1f, 1f))
        }
        // Millimetre grid.
        val gridColor = Color(0xFF6080BB).copy(alpha = 0.22f)
        val majorColor = Color(0xFF6080BB).copy(alpha = 0.40f)
        var gx = 0f
        while (gx < W) { drawRect(if (gx.toInt() % 50 == 0) majorColor else gridColor, topLeft = Offset(gx, 0f), size = Size(1f, H)); gx += 10f }
        var gy = 0f
        while (gy < H) { drawRect(if (gy.toInt() % 50 == 0) majorColor else gridColor, topLeft = Offset(0f, gy), size = Size(W, 1f)); gy += 10f }
        // Decorative title-block (bottom-right corner of a blueprint).
        drawRect(Color(0xFFEEE6CC), topLeft = Offset(W - 180f, H - 64f), size = Size(180f, 64f))
        drawRect(Color(0xFF234A8C), topLeft = Offset(W - 180f, H - 64f), size = Size(180f, 1f))
        drawRect(Color(0xFF234A8C), topLeft = Offset(W - 180f, H - 64f), size = Size(1f, 64f))
        drawDosText("APOLLO LM", x = W - 170f, y = H - 48f, color = Color(0xFF234A8C), size = 12f, bold = true)
        drawDosText("LUNAR EXCURSION MODULE", x = W - 170f, y = H - 34f, color = Color(0xFF234A8C), size = 8f)
        drawDosText("DWG  LM-${level.toString().padStart(2, '0')}", x = W - 170f, y = H - 20f, color = Color(0xFF234A8C), size = 9f, bold = true)
        drawDosText("SCALE 1:120", x = W - 80f, y = H - 20f, color = Color(0xFF234A8C), size = 9f)
        drawDosText("REV ${level - 1}", x = W - 30f, y = H - 8f, color = Color(0xFF234A8C), size = 8f)

        if (stage == Stage.TITLE) {
            drawTitleScreen(this, ctx)
            return@with
        }
        if (stage == Stage.SETUP) {
            drawSetup(this, ctx)
            return@with
        }

        // Dynamic zoom: camera starts wide (small ship in a big sky), then
        // crops in as the lander descends so the touchdown moment is BIG.
        // (Юджин: «в начале мелкий, ближе к касанию — крупный».)
        val terrainYNow = terrainYUnderShip()
        val altitudeNorm = ((terrainYNow - y - 38f) / (H * 0.65f)).coerceIn(0f, 1f)
        // ease-out: slow at high altitude, snap in near the ground.
        val ease = 1f - altitudeNorm * altitudeNorm
        val zoomFactor = 1f + ease * 0.7f
        val pivot = Offset(x, y + 20f)
        scale(scaleX = zoomFactor, scaleY = zoomFactor, pivot = pivot) {
            drawTerrain(this)
            drawLanderBlueprint(this, x, y, angle, thrustOn && fuelMass > 0f)
            // Wind animation — wavy horizontal lines, length = strength.
            val windStr = kotlin.math.abs(windAccel)
            if (windStr > 1f) {
                val wcols = Color(0xFF7C8FA6).copy(alpha = 0.55f)
                for (i in 0 until 6) {
                    val wy = 80f + i * 70f
                    val baseX = ((time * 30f + i * 47f) % W) - W * 0.2f
                    val len = (windStr * 1.6f).coerceAtMost(140f)
                    val dir = if (windAccel >= 0f) 1f else -1f
                    drawLine(wcols, start = Offset(baseX, wy), end = Offset(baseX + dir * len, wy + sin(time * 4f + i.toFloat()) * 3f), strokeWidth = 1.5f)
                }
            }
        }

        // HUD instruments (top-left) — drawn OUTSIDE the camera zoom so they
        // stay legible regardless of altitude.
        drawHud(this)

        if (stage == Stage.LANDED) {
            drawRect(Color(0xCCEEE6CC), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("LANDED", "СЕЛ"), x = W / 2f, y = H / 2f - 44f, color = Color(0xFF0F5C3E), size = 52f, bold = true, align = TextAlign.CENTER)
            drawDosText(landedMsg, x = W / 2f, y = H / 2f + 6f, color = Color(0xFF234A8C), size = 18f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK — NEXT LEVEL", "КЛИК — ДАЛЬШЕ"), x = W / 2f, y = H / 2f + 48f, color = Color(0xFF1A1A1A), size = 22f, bold = true, align = TextAlign.CENTER)
        } else if (stage == Stage.CRASHED) {
            drawRect(Color(0xCCEEE6CC), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("CRASHED", "РАЗБИЛСЯ"), x = W / 2f, y = H / 2f - 44f, color = Color(0xFFAA1010), size = 52f, bold = true, align = TextAlign.CENTER)
            drawDosText(crashMsg.ifEmpty { ctx.tr("crashed", "разбился") }, x = W / 2f, y = H / 2f + 6f, color = Color(0xFF234A8C), size = 18f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK — RETRY", "КЛИК — ЗАНОВО"), x = W / 2f, y = H / 2f + 48f, color = Color(0xFF1A1A1A), size = 22f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun drawSetup(scope: DrawScope, ctx: GameContext) = with(scope) {
        val ink = Color(0xFF234A8C)
        drawDosText("M I S S I O N   P R O F I L E", x = W / 2f, y = H * 0.12f, color = ink, size = 32f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Choose where you're landing — and how loaded you are.", "Выбери, куда садишься — и насколько гружён."), x = W / 2f, y = H * 0.18f, color = Color(0xFF6080BB), size = 13f, align = TextAlign.CENTER)
        // Three rows: PLANET / FUEL / PADS. ▲▼ — row, ◀▶ — value, FIRE — GO.
        val rowY = floatArrayOf(H * 0.34f, H * 0.46f, H * 0.58f)
        val labels = arrayOf(ctx.tr("PLANET", "ПЛАНЕТА"), ctx.tr("FUEL", "ТОПЛИВО"), ctx.tr("PADS", "ПЛОЩАДКИ"))
        val values = arrayOf(planet.name, "${setupFuel.toInt()} %", "$setupPadCount")
        for (i in 0..2) {
            val active = i == setupRow
            val col = if (active) Color(0xFF0F5C3E) else ink
            val marker = if (active) "►" else " "
            drawDosText("$marker  ${labels[i]}", x = W * 0.35f, y = rowY[i], color = col, size = 24f, bold = true, align = TextAlign.RIGHT)
            drawDosText(values[i], x = W * 0.50f, y = rowY[i], color = col, size = 24f, bold = true)
            drawDosText("◀ ▶", x = W * 0.78f, y = rowY[i], color = col.copy(alpha = 0.6f), size = 18f, bold = true)
        }
        val mult = missionMultiplier
        drawDosText(ctx.tr("SCORE MULTIPLIER  ×%.2f", "МНОЖИТЕЛЬ СЧЁТА  ×%.2f").pxfmt(mult), x = W / 2f, y = H * 0.72f, color = ink, size = 22f, bold = true, align = TextAlign.CENTER)
        drawDosText("▲▼ — row · ◀▶ — value · SPACE / CLICK — GO", x = W / 2f, y = H * 0.84f, color = Color(0xFF6080BB), size = 14f, align = TextAlign.CENTER)
    }

    private fun drawTitleScreen(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawDosText("L U N A R   L A N D E R", x = W / 2f, y = H * 0.16f, color = Color(0xFF234A8C), size = 46f, bold = true, align = TextAlign.CENTER)
        drawDosText("BLUEPRINT EDITION", x = W / 2f, y = H * 0.25f, color = Color(0xFF6080BB), size = 18f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Land the module on a flat pad.", "Посади модуль на ровную площадку."), x = W / 2f, y = H * 0.37f, color = Color(0xFF234A8C), size = 19f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Vertical speed   < 22 m/s", "Вертикальная скорость   < 22 м/с"), x = W / 2f, y = H * 0.44f, color = Color(0xFF234A8C), size = 17f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Horizontal speed < 18 m/s", "Горизонтальная скорость < 18 м/с"), x = W / 2f, y = H * 0.49f, color = Color(0xFF234A8C), size = 17f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Tilt angle ≈ zero", "Крен ≈ ноль"), x = W / 2f, y = H * 0.54f, color = Color(0xFF234A8C), size = 17f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("CONTROLS — side buttons:", "УПРАВЛЕНИЕ — боковые кнопки:"), x = W / 2f, y = H * 0.63f, color = Color(0xFF234A8C), size = 18f, bold = true, align = TextAlign.CENTER)
        drawDosText("▲ / FIRE — thrust (hold)", x = W / 2f, y = H * 0.69f, color = Color(0xFF234A8C), size = 17f, align = TextAlign.CENTER)
        drawDosText("◀  ▶ — rotate left / right", x = W / 2f, y = H * 0.74f, color = Color(0xFF234A8C), size = 17f, align = TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText(ctx.tr("CLICK TO LAUNCH", "КЛИК — ЗАПУСК"), x = W / 2f, y = H * 0.88f, color = Color(0xFF0F5C3E), size = 24f, bold = true, align = TextAlign.CENTER)
    }

    private fun drawTerrain(scope: DrawScope) = with(scope) {
        if (terrain.isEmpty()) return@with
        val penInk = Color(0xFF234A8C)
        val penLight = Color(0xFF234A8C).copy(alpha = 0.30f)
        // Hatch under the surface (pencil shading).
        val path = Path().apply {
            moveTo(terrain.first().x, H)
            lineTo(terrain.first().x, terrain.first().y)
            for (i in 1 until terrain.size) lineTo(terrain[i].x, terrain[i].y)
            lineTo(terrain.last().x, H)
            close()
        }
        drawPath(path, color = penLight)
        // Diagonal hatch lines beneath.
        val hatchSpacing = 14f
        var hx = -H
        while (hx < W) {
            drawLine(
                Color(0xFF234A8C).copy(alpha = 0.18f),
                start = Offset(hx, H),
                end = Offset(hx + H, 0f),
                strokeWidth = 1f,
            )
            hx += hatchSpacing
        }
        // Surface line — bold blueprint stroke.
        for (i in 0 until terrain.size - 1) {
            val a = terrain[i]; val b = terrain[i + 1]
            drawLine(penInk, start = Offset(a.x, a.y), end = Offset(b.x, b.y), strokeWidth = 2f)
        }
        // Landing pads — dashed bracket lines + label.
        for ((idx, pad) in pads.withIndex()) {
            drawLine(
                Color(0xFF0F5C3E),
                start = Offset(pad.x1, pad.y),
                end = Offset(pad.x2, pad.y),
                strokeWidth = 4f,
            )
            drawLine(
                Color(0xFF0F5C3E),
                start = Offset(pad.x1, pad.y - 14f),
                end = Offset(pad.x1, pad.y + 6f),
                strokeWidth = 2f,
            )
            drawLine(
                Color(0xFF0F5C3E),
                start = Offset(pad.x2, pad.y - 14f),
                end = Offset(pad.x2, pad.y + 6f),
                strokeWidth = 2f,
            )
            drawDosText("PAD-${idx + 1}  +${pad.bonus}", x = (pad.x1 + pad.x2) / 2f, y = pad.y - 6f, color = Color(0xFF0F5C3E), size = 10f, bold = true, align = TextAlign.CENTER)
        }
    }

    private fun drawLanderBlueprint(scope: DrawScope, cx: Float, cy: Float, ang: Float, thrust: Boolean) = with(scope) {
        rotate(degrees = (ang * 180f / PI.toFloat()), pivot = Offset(cx, cy)) {
            val ink = Color(0xFF234A8C)
            val pen = Stroke(width = 1.6f)
            val penThick = Stroke(width = 2.4f)

            // ── Descent stage: a clean rectangular body centred on (cx,cy). ──
            // Footprint kept ~the same: half-width 20, half-height 16 (collision uses y+18).
            val hw = 20f      // half width of the descent body
            val top = cy - 12f
            val bot = cy + 16f
            // Wipe paper under the body so grid doesn't show through.
            drawRect(backgroundColor, topLeft = Offset(cx - hw, top), size = Size(hw * 2f, bot - top))
            // Body outline (box).
            drawLine(ink, Offset(cx - hw, top), Offset(cx + hw, top), strokeWidth = penThick.width)
            drawLine(ink, Offset(cx + hw, top), Offset(cx + hw, bot), strokeWidth = penThick.width)
            drawLine(ink, Offset(cx + hw, bot), Offset(cx - hw, bot), strokeWidth = penThick.width)
            drawLine(ink, Offset(cx - hw, bot), Offset(cx - hw, top), strokeWidth = penThick.width)
            // Faint technical hatch inside the descent body.
            var hy = top + 3f
            while (hy < bot) {
                drawLine(ink.copy(alpha = 0.14f), Offset(cx - hw + 2f, hy), Offset(cx + hw - 2f, hy), strokeWidth = 1f)
                hy += 4f
            }
            // A panel seam + RCS quad markers on the body sides.
            drawLine(ink.copy(alpha = 0.45f), Offset(cx - hw, cy + 2f), Offset(cx + hw, cy + 2f), strokeWidth = 1f)
            drawRect(ink.copy(alpha = 0.5f), topLeft = Offset(cx - hw - 4f, cy - 4f), size = Size(4f, 8f))
            drawRect(ink.copy(alpha = 0.5f), topLeft = Offset(cx + hw, cy - 4f), size = Size(4f, 8f))

            // ── Ascent cabin: stepped octagon block on top. ──
            val cw = 12f
            val cTop = top - 16f
            val cab = Path().apply {
                moveTo(cx - cw, top)
                lineTo(cx - cw, cTop + 4f)
                lineTo(cx - cw + 4f, cTop)
                lineTo(cx + cw - 4f, cTop)
                lineTo(cx + cw, cTop + 4f)
                lineTo(cx + cw, top)
                close()
            }
            drawPath(cab, color = backgroundColor)
            drawPath(cab, color = ink, style = penThick)
            // Round porthole window with crosshair.
            val wcy = (cTop + top) / 2f
            drawCircle(backgroundColor, radius = 4f, center = Offset(cx, wcy))
            drawCircle(ink, radius = 4f, center = Offset(cx, wcy), style = pen)
            drawLine(ink, Offset(cx - 4f, wcy), Offset(cx + 4f, wcy), strokeWidth = 1f)
            // Antenna mast + dish blob.
            drawLine(ink, Offset(cx, cTop), Offset(cx, cTop - 12f), strokeWidth = 1.5f)
            drawCircle(ink, radius = 2.4f, center = Offset(cx, cTop - 13f), style = pen)

            // ── Thruster nozzle underneath (descent engine bell). ──
            val nTop = bot
            val nBot = bot + 10f
            val noz = Path().apply {
                moveTo(cx - 7f, nTop)
                lineTo(cx - 5f, nTop)        // throat
                lineTo(cx - 8f, nBot)        // bell flare
                lineTo(cx + 8f, nBot)
                lineTo(cx + 5f, nTop)
                lineTo(cx + 7f, nTop)
                close()
            }
            drawPath(noz, color = backgroundColor)
            drawPath(noz, color = ink, style = pen)

            // ── Four splayed landing legs with footpads (drawLine blueprint). ──
            val legY = bot + 22f
            data class Leg(val ox: Float, val pad: Float)
            val legs = listOf(
                Leg(cx - hw + 2f, cx - hw - 14f),   // outer-left
                Leg(cx + hw - 2f, cx + hw + 14f),   // outer-right
                Leg(cx - 7f, cx - 7f),              // inner-left (near vertical)
                Leg(cx + 7f, cx + 7f),              // inner-right
            )
            for (leg in legs) {
                drawLine(ink, Offset(leg.ox, bot - 2f), Offset(leg.pad, legY), strokeWidth = 2f)
                // strut brace back to the body.
                drawLine(ink.copy(alpha = 0.55f), Offset((leg.ox + leg.pad) / 2f, (bot + legY) / 2f), Offset(leg.ox, bot - 8f), strokeWidth = 1f)
                // footpad.
                drawLine(ink, Offset(leg.pad - 5f, legY), Offset(leg.pad + 5f, legY), strokeWidth = 2.6f)
            }

            // ── Thrust plume — the only warm colour. Animated flicker, from nozzle. ──
            if (thrust) {
                val flick = (time * 30f).toInt() % 3
                val plumeLen = 24f + flick * 3f
                val outer = Path().apply {
                    moveTo(cx - 8f, nBot)
                    lineTo(cx, nBot + plumeLen)
                    lineTo(cx + 8f, nBot)
                    close()
                }
                drawPath(outer, color = Color(0xFFFF7700).copy(alpha = 0.55f))
                val inner = Path().apply {
                    moveTo(cx - 4f, nBot)
                    lineTo(cx, nBot + plumeLen * 0.7f)
                    lineTo(cx + 4f, nBot)
                    close()
                }
                drawPath(inner, color = Color(0xFFFFEE40))
            }

            // Reference dimension line + tag (technical-drawing flourish).
            drawLine(ink.copy(alpha = 0.6f), Offset(cx + hw + 8f, cTop), Offset(cx + hw + 8f, bot), strokeWidth = 0.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(2f, 2f), 0f))
            drawDosText("LM-1", x = cx + hw + 14f, y = cy, color = ink, size = 10f, bold = true)
        }
    }

    private fun drawHud(scope: DrawScope) = with(scope) {
        val ink = Color(0xFF234A8C)
        // Instrument panel — top-left. Bigger box + bigger type (Юджин: «почти сломал глаза»).
        val px = 14f; val py = 12f
        val panW = 248f; val panH = 198f
        drawRect(Color(0xFFEEE6CC), topLeft = Offset(px - 2f, py - 2f), size = Size(panW, panH))
        drawRect(ink, topLeft = Offset(px - 2f, py - 2f), size = Size(panW, 1f))
        drawRect(ink, topLeft = Offset(px - 2f, py + panH - 3f), size = Size(panW, 1f))
        drawRect(ink, topLeft = Offset(px - 2f, py - 2f), size = Size(1f, panH))
        drawRect(ink, topLeft = Offset(px + panW - 2f, py - 2f), size = Size(1f, panH))
        drawDosText("INSTRUMENT PANEL", x = px + panW / 2f - 2f, y = py + 16f, color = ink, size = 15f, bold = true, align = TextAlign.CENTER)
        drawDosText("FUEL  ${"%4.0f".pxfmt(fuelPct)} %", x = px + 8f, y = py + 40f, color = ink, size = 16f, bold = true)
        // Fuel bar.
        drawRect(ink.copy(alpha = 0.20f), topLeft = Offset(px + 8f, py + 46f), size = Size(panW - 24f, 8f))
        val fc = if (fuelPct > 30f) Color(0xFF0F5C3E) else Color(0xFFAA1010)
        drawRect(fc, topLeft = Offset(px + 8f, py + 46f), size = Size((panW - 24f) * (fuelPct / 100f).coerceIn(0f, 1f), 8f))
        drawDosText("V↓ ${"%5.1f".pxfmt(vy / 8f)} m/s", x = px + 8f, y = py + 76f,
            color = if (abs(vy) >= safeVy) Color(0xFFAA1010) else ink, size = 16f, bold = true)
        drawDosText("V→ ${"%5.1f".pxfmt(vx / 8f)} m/s", x = px + 8f, y = py + 98f,
            color = if (abs(vx) >= safeVx) Color(0xFFAA1010) else ink, size = 16f, bold = true)
        drawDosText("ANG ${"%5.1f".pxfmt(angle * 180f / PI.toFloat())}°", x = px + 8f, y = py + 120f,
            color = if (abs(angle) >= 0.25f) Color(0xFFAA1010) else ink, size = 16f, bold = true)
        drawDosText("ALT ${"%5.0f".pxfmt(max(0f, (terrainYUnderShip() - y - 38f) / 8f))} m", x = px + 8f, y = py + 142f, color = ink, size = 16f, bold = true)
        // TWR — thrust-to-weight at the current mass. < 1 means the engine
        // can't beat gravity on this planet — you MUST shed fuel mass first.
        val twrNow = twr
        val twrColor = when {
            twrNow >= 1.5f -> Color(0xFF0F5C3E)
            twrNow >= 1.0f -> ink
            else -> Color(0xFFAA1010)
        }
        drawDosText("TWR ${"%5.2f".pxfmt(twrNow)}", x = px + 8f, y = py + 164f, color = twrColor, size = 16f, bold = true)
        drawDosText("MASS ${"%4.0f".pxfmt(mass)} t", x = px + 8f, y = py + 186f, color = ink, size = 14f, bold = true)

        // ── Attitude indicator + THRUST lamp (shows the EFFECT of gutter controls). ──
        drawAttitudeIndicator(this, W - 70f, py + 56f, 40f)

        // Top-right: level + score (bigger type).
        drawDosText("MISSION ${level.toString().padStart(2, '0')}", x = W - 130f, y = py + 16f, color = ink, size = 16f, bold = true, align = TextAlign.RIGHT)
        drawDosText("SCORE  $score", x = W - 130f, y = py + 36f, color = ink, size = 14f, bold = true, align = TextAlign.RIGHT)
        drawDosText("BEST   $best", x = W - 130f, y = py + 56f, color = Color(0xFF0F5C3E), size = 14f, bold = true, align = TextAlign.RIGHT)
    }

    /**
     * Compact attitude / thrust indicator so the player can SEE what the side
     * D-pad is doing: a circle with a needle pointing along the lander's tilt,
     * a glowing arrow when thrusting, and ◀▶ lamps lit while rotating.
     */
    private fun drawAttitudeIndicator(scope: DrawScope, cx: Float, cy: Float, r: Float) = with(scope) {
        val ink = Color(0xFF234A8C)
        drawCircle(backgroundColor, radius = r + 2f, center = Offset(cx, cy))
        drawCircle(ink, radius = r, center = Offset(cx, cy), style = Stroke(width = 1.4f))
        // Tick marks (top + sides).
        drawLine(ink.copy(alpha = 0.5f), Offset(cx, cy - r), Offset(cx, cy - r + 6f), strokeWidth = 1f)
        drawLine(ink.copy(alpha = 0.5f), Offset(cx - r, cy), Offset(cx - r + 6f, cy), strokeWidth = 1f)
        drawLine(ink.copy(alpha = 0.5f), Offset(cx + r, cy), Offset(cx + r - 6f, cy), strokeWidth = 1f)
        // Tilt needle — points along the lander's local 'up' (rotated by angle).
        val tipX = cx + sin(angle) * (r - 4f)
        val tipY = cy - cos(angle) * (r - 4f)
        val needleCol = if (abs(angle) >= 0.25f) Color(0xFFAA1010) else Color(0xFF0F5C3E)
        drawLine(needleCol, Offset(cx, cy), Offset(tipX, tipY), strokeWidth = 3f)
        drawCircle(needleCol, radius = 3f, center = Offset(cx, cy))
        // THRUST lamp — glows warm + an up-arrow when the engine is firing.
        val firing = thrustOn && fuelMass > 0f
        if (firing) {
            drawCircle(Color(0xFFFF7700).copy(alpha = 0.30f), radius = r + 2f, center = Offset(cx, cy))
            // up-arrow through the centre.
            drawLine(Color(0xFFFF7700), Offset(cx, cy + r - 8f), Offset(cx, cy - r + 8f), strokeWidth = 3f)
            drawLine(Color(0xFFFF7700), Offset(cx - 6f, cy - r + 14f), Offset(cx, cy - r + 8f), strokeWidth = 3f)
            drawLine(Color(0xFFFF7700), Offset(cx + 6f, cy - r + 14f), Offset(cx, cy - r + 8f), strokeWidth = 3f)
        }
        drawDosText("THRUST", x = cx, y = cy + r + 16f,
            color = if (firing) Color(0xFFCC5500) else ink.copy(alpha = 0.5f),
            size = 12f, bold = true, align = TextAlign.CENTER)
        // Rotation lamps ◀ ▶ flank the dial, lit while rotating.
        drawDosText("◀", x = cx - r - 8f, y = cy + 5f,
            color = if (leftHeld) Color(0xFF0F5C3E) else ink.copy(alpha = 0.35f),
            size = 18f, bold = true, align = TextAlign.CENTER)
        drawDosText("▶", x = cx + r + 8f, y = cy + 5f,
            color = if (rightHeld) Color(0xFF0F5C3E) else ink.copy(alpha = 0.35f),
            size = 18f, bold = true, align = TextAlign.CENTER)
    }

    private fun terrainYUnderShip(): Float {
        val seg = findSegmentAt(x) ?: return H
        return interpolateY(seg, x)
    }

    // ── Input ────────────────────────────────────────────────────────────
    // Steering is via the side gutters: D-pad UP = thrust, LEFT/RIGHT = rotate,
    // FIRE = thrust. The D-pad auto-repeats with no release, so we just stamp
    // the pulse here and let update() derive held-state via the freshness window.
    private var setupRow = 0
    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (stage == Stage.SETUP) {
            if (dy < 0) { setupRow = (setupRow + 2) % 3 }
            if (dy > 0) { setupRow = (setupRow + 1) % 3 }
            if (dx != 0) {
                when (setupRow) {
                    0 -> planetIdx = (planetIdx + dx + planets.size) % planets.size
                    1 -> setupFuel = (setupFuel + dx * 25f).coerceIn(50f, 150f)
                    2 -> setupPadCount = (setupPadCount + dx).coerceIn(1, 3)
                }
                ctx.sound.click()
            }
            return
        }
        if (stage != Stage.PLAY) return
        dpadX = dx; dpadY = dy; dpadFreshT = time
    }
    override fun onFire(ctx: GameContext) {
        if (stage == Stage.SETUP) { startLevel(); return }
        if (stage != Stage.PLAY) return
        fireFreshT = time
    }

    // Tapping the canvas launches / advances / retries; all steering is in the gutters.
    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        when (stage) {
            Stage.TITLE -> { stage = Stage.SETUP }
            Stage.SETUP -> startLevel()
            Stage.LANDED -> { level++; stage = Stage.SETUP }
            Stage.CRASHED -> { stage = Stage.SETUP }
            Stage.PLAY -> {}
        }
    }
}
