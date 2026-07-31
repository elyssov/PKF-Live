package com.pixelclassics.app.games.f21

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.game.IntroStyle
import kotlin.math.*

class F21Game : Game {
    override val id = "f21"
    override val titleResId = R.string.game_f21
    override val backgroundColor = Color.Black
    override val controls = GameControls.DPAD_AND_FIRE
    override val introText: String = F21_INTRO_EN
    override fun introTextFor(lang: String) = pickF21Intro(lang)
    override val introStyle = IntroStyle.TERMINAL_GREEN_CRT
    override val virtualWidth get() = W
    override val virtualHeight get() = H
    override val helpLines = listOf(
        "◀ ▶ roll · ▲ ▼ pitch  or  TILT phone to fly",
        "● FIRE — shoot · Throttle slider on screen",
        "Stay low & slow to remain invisible to radar",
    )

    companion object {
        const val W = 640f
        const val H = 480f
        const val DEG = (PI / 180.0).toFloat()
    }

    // ── State machine ──
    private enum class Stage { TITLE, BRIEFING, PLAY, MISSION_COMPLETE, GAMEOVER }
    private var stage = Stage.TITLE
    private var time = 0f
    private var score = 0
    private var best = 0

    // ── Gyroscope ──
    private var gyroListener: Any? = null
    private var gyroPitch = 0f   // phone tilt forward/back → aircraft pitch
    private var gyroRoll = 0f    // phone tilt left/right → aircraft roll
    private var gyroReady = false

    // ── Player aircraft state ──
    private var px = 0f; private var py = 0f; private var pz = 0f  // world position
    private var pitch = 0f       // nose up/down (radians)
    private var roll = 0f        // bank left/right
    private var yaw = 0f         // heading
    private var throttle = 0.4f  // 0..1
    private var speed = 0f       // actual speed (knots-ish)
    private var altitude = 2000f // feet
    private val maxAlt = 40000f
    private val minAlt = 50f

    // ── Stealth ──
    private var stealthMeter = 0f  // 0 = invisible, 1 = fully detected
    private var detected = false
    private var samLockTimer = 0f

    // ── Camera ──
    private var chaseCam = true  // true = behind aircraft, false = cockpit
    private var camPitch = 0f
    private var camYaw = 0f

    // ── Weapons ──
    private var missiles = 4
    private var bombs = 2
    private var ecmCharges = 3
    private var selectedWeapon = 0  // 0=missiles, 1=bombs
    private var fireCooldown = 0f

    // ── Projectiles ──
    private data class Projectile(var x: Float, var y: Float, var z: Float,
                                  var vx: Float, var vy: Float, var vz: Float,
                                  var life: Float, val isBomb: Boolean)
    private val projectiles = mutableListOf<Projectile>()

    // ── Terrain / world ──
    private data class RadarSite(var x: Float, var z: Float, val range: Float,
                                 var alive: Boolean = true, var detected: Boolean = false)
    private data class Target(var x: Float, var z: Float, val kind: TargetKind,
                              var alive: Boolean = true)
    private enum class TargetKind { BUILDING, RUNWAY, RADAR, SAM }

    private val radarSites = mutableListOf<RadarSite>()
    private val targets = mutableListOf<Target>()
    private val samMissiles = mutableListOf<Projectile>()
    private var missionTargets = 0
    private var missionDestroyed = 0

    // ── SAM tracking ──
    private data class SAMLauncher(var x: Float, var z: Float, var cooldown: Float = 0f,
                                   var alive: Boolean = true)
    private val samLaunchers = mutableListOf<SAMLauncher>()

    // ── D-pad input ──
    private var dpadX = 0
    private var dpadY = 0
    private var dpadFreshT = -1f

    // ── Touch state ──
    private var throttleDragging = false
    private var throttleTouchId = -1L
    private var viewBtnPressed = false

    // ── Stars (background) ──
    private data class Star3D(val x: Float, val y: Float, val z: Float, val br: Float)
    private val stars = ArrayList<Star3D>(80)

    // ── Ground grid lines for depth perception ──
    private val groundColor = Color(0xFF1A3A1A)
    private val skyTopColor = Color(0xFF000020)
    private val skyBottomColor = Color(0xFF1A1A40)
    private val hudGreen = Color(0xFF00FF00)
    private val hudAmber = Color(0xFFFFAA00)
    private val hudRed = Color(0xFFFF3333)
    private val bodyColor = Color(0xFF2A2A2A)
    private val bodyLight = Color(0xFF3A3A3A)
    private val bodyDark = Color(0xFF1A1A1A)
    private val starRed = Color(0xFFCC0000)

    // ═══════════════════════════════════════════════
    //                  INIT
    // ═══════════════════════════════════════════════

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        stage = Stage.TITLE
        initStars()
        ctx.activity?.let { startGyroscope(it) }
    }

    private fun initStars() {
        stars.clear()
        for (i in 0 until 80) {
            stars += Star3D(
                (kotlin.random.Random.nextDouble().toFloat() - 0.5f) * 4000f,
                (kotlin.random.Random.nextDouble().toFloat()) * 2000f + 200f,
                (kotlin.random.Random.nextDouble().toFloat() - 0.5f) * 4000f,
                kotlin.random.Random.nextDouble().toFloat() * 0.7f + 0.3f
            )
        }
    }

    private fun setupMission() {
        px = 0f; py = 0f; pz = 0f
        altitude = 5000f
        pitch = 0f; roll = 0f; yaw = 0f
        throttle = 0.5f; speed = 300f
        // Stale D-pad state + the time reset below made a direction held at
        // the moment of death stick for the length of the PREVIOUS session.
        dpadX = 0; dpadY = 0; dpadFreshT = -1f
        stealthMeter = 0f; detected = false; samLockTimer = 0f
        missiles = 4; bombs = 2; ecmCharges = 3
        selectedWeapon = 0; fireCooldown = 0f
        projectiles.clear(); samMissiles.clear()

        radarSites.clear(); targets.clear(); samLaunchers.clear()

        // Generate mission: enemy airfield with radar & SAM coverage
        // Radar sites in a defensive ring
        radarSites += RadarSite(3000f, 3000f, 2500f)
        radarSites += RadarSite(-2000f, 4000f, 2000f)
        radarSites += RadarSite(1000f, 6000f, 3000f)

        // SAM launchers near targets
        samLaunchers += SAMLauncher(2500f, 3500f)
        samLaunchers += SAMLauncher(500f, 5500f)

        // Targets to destroy
        targets += Target(2000f, 4500f, TargetKind.BUILDING)
        targets += Target(2200f, 4700f, TargetKind.RUNWAY)
        targets += Target(3000f, 3000f, TargetKind.RADAR)
        targets += Target(1000f, 6000f, TargetKind.RADAR)

        missionTargets = targets.size
        missionDestroyed = 0
    }

    // ═══════════════════════════════════════════════
    //                  GYROSCOPE
    // ═══════════════════════════════════════════════

    fun startGyroscope(context: Any) { /* no gyroscope in the arcade; D-pad flies the jet */ }

    fun stopGyroscope() {
        gyroListener = null
        gyroReady = false
    }

    // ═══════════════════════════════════════════════
    //                  UPDATE
    // ═══════════════════════════════════════════════

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        when (stage) {
            Stage.TITLE -> {}
            Stage.BRIEFING -> {}
            Stage.PLAY -> updatePlay(dt, ctx)
            Stage.MISSION_COMPLETE -> {}
            Stage.GAMEOVER -> {}
        }
    }

    private fun updatePlay(dt: Float, ctx: GameContext) {
        // ── D-pad input ──
        val dpadActive = time - dpadFreshT < 0.16f
        val dRoll = if (dpadActive) dpadX.toFloat() else 0f
        val dPitch = if (dpadActive) -dpadY.toFloat() * 0.5f else 0f

        // ── Gyroscope input ──
        var gRoll = 0f; var gPitch = 0f
        if (gyroReady) {
            gRoll = (gyroRoll * 2.0f).coerceIn(-1.2f, 1.2f)
            gPitch = ((gyroPitch + 0.5f) * 1.5f).coerceIn(-0.8f, 0.8f)
        }

        // ── Combined flight input ──
        val targetRoll = (dRoll * 0.8f + gRoll).coerceIn(-1.2f, 1.2f)
        val targetPitch = (dPitch + gPitch).coerceIn(-0.8f, 0.8f)
        roll += (targetRoll - roll) * 3f * dt
        pitch += (targetPitch - pitch) * 2f * dt

        // Yaw from roll (banked turn)
        yaw += sin(roll) * 1.5f * dt

        // Speed from throttle
        val targetSpeed = 150f + throttle * 650f  // 150..800 knots
        speed += (targetSpeed - speed) * 0.5f * dt

        // Position update
        val headX = sin(yaw) * cos(pitch)
        val headZ = cos(yaw) * cos(pitch)
        val headY = sin(pitch)
        px += headX * speed * 0.1f * dt
        pz += headZ * speed * 0.1f * dt
        altitude += headY * speed * 2f * dt
        altitude = altitude.coerceIn(minAlt, maxAlt)

        // ── Stealth calculation ──
        var maxDetection = 0f
        for (radar in radarSites) {
            if (!radar.alive) continue
            val dist = hypot(px - radar.x, pz - radar.z)
            if (dist < radar.range) {
                // Detection depends on: distance (closer=worse), altitude (lower=better),
                // speed (slower=better), aspect (head-on=better for stealth)
                var detection = 1f - (dist / radar.range)
                detection *= (altitude / 10000f).coerceIn(0.1f, 1f)  // low = stealthy
                detection *= (speed / 600f).coerceIn(0.2f, 1f)       // slow = stealthy
                // Afterburner = very visible
                if (throttle > 0.85f) detection *= 1.5f
                maxDetection = max(maxDetection, detection)
                radar.detected = detection > 0.3f
            } else {
                radar.detected = false
            }
        }
        stealthMeter += (maxDetection - stealthMeter) * 2f * dt
        stealthMeter = stealthMeter.coerceIn(0f, 1f)
        detected = stealthMeter > 0.6f

        // ── SAM threat ──
        if (detected) {
            samLockTimer += dt
        } else {
            samLockTimer = (samLockTimer - dt * 2f).coerceAtLeast(0f)
        }

        for (sam in samLaunchers) {
            if (!sam.alive) continue
            sam.cooldown = (sam.cooldown - dt).coerceAtLeast(0f)
            val dist = hypot(px - sam.x, pz - sam.z)
            if (detected && dist < 4000f && sam.cooldown <= 0f) {
                // Launch SAM!
                val dx = px - sam.x; val dz = pz - sam.z
                val d = hypot(dx, dz).coerceAtLeast(1f)
                samMissiles += Projectile(
                    sam.x, 0f, sam.z,
                    dx / d * 600f, altitude / 2f, dz / d * 600f,
                    8f, false
                )
                sam.cooldown = 5f
            }
        }

        // Update SAM missiles (home toward player)
        val samIter = samMissiles.iterator()
        while (samIter.hasNext()) {
            val m = samIter.next()
            val dx = px - m.x; val dz = pz - m.z; val dy = altitude - m.y
            val d = sqrt(dx * dx + dy * dy + dz * dz).coerceAtLeast(1f)
            m.vx += (dx / d) * 200f * dt
            m.vy += (dy / d) * 200f * dt
            m.vz += (dz / d) * 200f * dt
            val spd = sqrt(m.vx * m.vx + m.vy * m.vy + m.vz * m.vz)
            if (spd > 800f) { val s = 800f / spd; m.vx *= s; m.vy *= s; m.vz *= s }
            m.x += m.vx * dt; m.y += m.vy * dt; m.z += m.vz * dt
            m.life -= dt
            if (m.life <= 0f) { samIter.remove(); continue }
            // Hit check
            val hitDist = sqrt((m.x - px).pow(2) + (m.y - altitude).pow(2) + (m.z - pz).pow(2))
            if (hitDist < 80f) {
                samIter.remove()
                if (score > best) { best = score; ctx.scores.set(id, best) }
                stage = Stage.GAMEOVER
                return
            }
        }

        // ── Player projectiles ──
        fireCooldown = (fireCooldown - dt).coerceAtLeast(0f)
        val projIter = projectiles.iterator()
        while (projIter.hasNext()) {
            val p = projIter.next()
            p.x += p.vx * dt; p.y += p.vy * dt; p.z += p.vz * dt
            p.life -= dt
            if (p.life <= 0f || p.y < 0f) { projIter.remove(); continue }
            // Check hit on targets
            for (t in targets) {
                if (!t.alive) continue
                val td = hypot(p.x - t.x, p.z - t.z)
                if (td < 150f && (p.isBomb || p.y < 500f)) {
                    t.alive = false
                    missionDestroyed++
                    score += when (t.kind) {
                        TargetKind.BUILDING -> 500
                        TargetKind.RUNWAY -> 300
                        TargetKind.RADAR -> 1000
                        TargetKind.SAM -> 800
                    }
                    // Killing a RADAR target must actually silence the matching
                    // radar ring — the briefing promises exactly that.
                    if (t.kind == TargetKind.RADAR) {
                        radarSites.filter { hypot(it.x - t.x, it.z - t.z) < 1f }
                            .forEach { it.alive = false }
                    }
                    projIter.remove()
                    break
                }
            }
        }

        // Check mission complete
        if (missionDestroyed >= missionTargets) {
            if (score > best) { best = score; ctx.scores.set(id, best) }
            stage = Stage.MISSION_COMPLETE
        }

        // Out of ordnance with targets still standing → mission failed.
        // Without this the sortie was a permanent softlock (no fail state).
        if (stage == Stage.PLAY && missiles <= 0 && bombs <= 0 &&
            projectiles.isEmpty() && missionDestroyed < missionTargets
        ) {
            if (score > best) { best = score; ctx.scores.set(id, best) }
            stage = Stage.GAMEOVER
        }

        // Crash check
        if (altitude <= minAlt && pitch < -0.05f) {
            if (score > best) { best = score; ctx.scores.set(id, best) }
            stage = Stage.GAMEOVER
        }
    }

    // ═══════════════════════════════════════════════
    //                  DRAW
    // ═══════════════════════════════════════════════

    private fun DrawScope.txt(s: String, x: Float, y: Float, c: Color, sz: Float = 12f,
                              center: Boolean = false) {
        drawDosText(s, x, y, c, sz, align = if (center) TextAlign.CENTER else TextAlign.LEFT)
    }

    override fun draw(scope: DrawScope, ctx: GameContext) {
        when (stage) {
            Stage.TITLE -> drawTitle(scope)
            Stage.BRIEFING -> drawBriefing(scope)
            Stage.PLAY -> drawPlay(scope)
            Stage.MISSION_COMPLETE -> drawMissionComplete(scope)
            Stage.GAMEOVER -> drawGameOver(scope)
        }
    }

    private fun drawTitle(scope: DrawScope) {
        scope.drawRect(Color(0xFF000030), Offset.Zero, Size(W, H))
        scope.txt("F - 2 1", W / 2f, 100f, hudGreen, 32f, true)
        scope.txt("STEALTH FIGHTER", W / 2f, 150f, hudGreen, 18f, true)
        drawWireframeJet(scope, W / 2f, 280f, 3f)
        if ((time * 2f).toInt() % 2 == 0) {
            scope.txt("CLICK TO START MISSION", W / 2f, 400f, hudAmber, 14f, true)
        }
        scope.txt("BEST: $best", W / 2f, 450f, hudGreen, 11f, true)
    }

    private fun drawWireframeJet(scope: DrawScope, cx: Float, cy: Float, scale: Float) {
        val s = scale
        val wireColor = hudGreen
        val stroke = Stroke(1.5f)

        // Fuselage center line (nose to tail)
        scope.drawLine(wireColor, Offset(cx, cy - 30 * s), Offset(cx, cy + 25 * s), 1.5f)

        // Forward-swept wings (MiG-37B style)
        scope.drawLine(wireColor, Offset(cx, cy), Offset(cx - 40 * s, cy - 15 * s), 1.5f)
        scope.drawLine(wireColor, Offset(cx, cy), Offset(cx + 40 * s, cy - 15 * s), 1.5f)
        // Wing trailing edge
        scope.drawLine(wireColor, Offset(cx - 40 * s, cy - 15 * s), Offset(cx - 15 * s, cy + 5 * s), 1.5f)
        scope.drawLine(wireColor, Offset(cx + 40 * s, cy - 15 * s), Offset(cx + 15 * s, cy + 5 * s), 1.5f)

        // Canted vertical tails
        scope.drawLine(wireColor, Offset(cx - 12 * s, cy + 15 * s), Offset(cx - 18 * s, cy + 5 * s), 1.5f)
        scope.drawLine(wireColor, Offset(cx + 12 * s, cy + 15 * s), Offset(cx + 18 * s, cy + 5 * s), 1.5f)

        // Engine intakes (angular)
        scope.drawLine(wireColor, Offset(cx - 8 * s, cy + 10 * s), Offset(cx - 15 * s, cy + 25 * s), 1.5f)
        scope.drawLine(wireColor, Offset(cx + 8 * s, cy + 10 * s), Offset(cx + 15 * s, cy + 25 * s), 1.5f)

        // Nose facets
        scope.drawLine(wireColor, Offset(cx, cy - 30 * s), Offset(cx - 6 * s, cy - 10 * s), 1.5f)
        scope.drawLine(wireColor, Offset(cx, cy - 30 * s), Offset(cx + 6 * s, cy - 10 * s), 1.5f)
    }

    private fun drawBriefing(scope: DrawScope) {
        scope.drawRect(Color(0xFF001100), Offset.Zero, Size(W, H))
        scope.txt("MISSION BRIEFING", W / 2f, 50f, hudGreen, 22f, true)
        scope.txt("TARGET: Enemy airfield complex", 40f, 110f, hudGreen, 13f)
        scope.txt("Primary: Destroy radar installations", 40f, 140f, hudGreen, 13f)
        scope.txt("Secondary: Neutralise runway & HQ", 40f, 165f, hudGreen, 13f)
        scope.txt("Threats: 3 radar sites, 2 SAM batteries", 40f, 205f, hudAmber, 13f)
        scope.txt("Loadout: 4 AGM  2 bombs  3 ECM", 40f, 245f, hudGreen, 13f)
        scope.txt("Fly low, fly slow, stay invisible.", 40f, 295f, hudGreen, 13f)
        if ((time * 2f).toInt() % 2 == 0) {
            scope.txt("CLICK TO LAUNCH", W / 2f, 400f, hudAmber, 16f, true)
        }
    }

    private fun drawPlay(scope: DrawScope) {
        // ── Sky ──
        val horizonY = H * 0.45f + pitch * 200f
        // Sky above horizon
        scope.drawRect(skyTopColor, Offset.Zero, Size(W, horizonY.coerceIn(0f, H)))
        // Ground below horizon
        if (horizonY < H) {
            scope.drawRect(groundColor, Offset(0f, horizonY.coerceAtLeast(0f)),
                Size(W, (H - horizonY).coerceAtLeast(0f)))
        }

        // ── Ground perspective lines ──
        if (horizonY < H) {
            val lineColor = Color(0xFF0D2D0D)
            for (i in 1..12) {
                val t = i / 12f
                val ly = horizonY + (H - horizonY) * t * t
                scope.drawLine(lineColor, Offset(0f, ly), Offset(W, ly), 1f)
            }
            // Vertical perspective lines converging at horizon center
            val vanishX = W / 2f + sin(yaw) * 200f
            for (i in -6..6) {
                val groundX = vanishX + i * 120f
                scope.drawLine(lineColor, Offset(vanishX, horizonY), Offset(groundX, H), 1f)
            }
        }

        // ── Stars ──
        for (s in stars) {
            val sx = ((s.x - px) % 2000f + 3000f) % 2000f - 1000f
            val sz = ((s.z - pz) % 2000f + 3000f) % 2000f - 1000f
            if (sz < 50f) continue
            val screenX = W / 2f + (sx / sz) * 300f
            val screenY = horizonY - (s.y / sz) * 200f
            if (screenY < horizonY && screenX in 0f..W) {
                val br = (s.br * (1f - sz / 2000f)).coerceIn(0f, 1f)
                scope.drawCircle(Color.White.copy(alpha = br), 1.5f, Offset(screenX, screenY))
            }
        }

        // ── Radar sites on ground (red pulsing circles) ──
        for (radar in radarSites) {
            if (!radar.alive) continue
            val rx = radar.x - px; val rz = radar.z - pz
            if (rz < 100f) continue
            val sx2 = W / 2f + (rx / rz) * 300f
            val sy2 = horizonY + (1f / rz) * 50000f
            if (sx2 in -50f..W + 50f && sy2 > horizonY) {
                val pulse = (sin(time * 3f) * 0.3f + 0.7f)
                val rColor = if (radar.detected) hudRed.copy(alpha = pulse) else hudAmber.copy(alpha = 0.4f)
                scope.drawCircle(rColor, 8f / (rz / 500f).coerceAtLeast(1f), Offset(sx2, sy2))
            }
        }

        // ── Targets on ground ──
        for (t in targets) {
            if (!t.alive) continue
            val tx = t.x - px; val tz = t.z - pz
            if (tz < 100f) continue
            val sx2 = W / 2f + (tx / tz) * 300f
            val sy2 = horizonY + (1f / tz) * 50000f
            if (sx2 in -50f..W + 50f && sy2 > horizonY) {
                val tColor = when (t.kind) {
                    TargetKind.BUILDING -> Color(0xFF888888)
                    TargetKind.RUNWAY -> Color(0xFF444444)
                    TargetKind.RADAR -> hudAmber
                    TargetKind.SAM -> hudRed
                }
                val tSize = 6f / (tz / 500f).coerceAtLeast(1f)
                scope.drawRect(tColor, Offset(sx2 - tSize, sy2 - tSize), Size(tSize * 2, tSize * 2))
            }
        }

        // ── SAM missiles (incoming) ──
        for (m in samMissiles) {
            val mx = m.x - px; val mz = m.z - pz; val my = m.y
            if (mz < 50f) continue
            val sx2 = W / 2f + (mx / mz) * 300f
            val sy2 = horizonY - ((my - altitude) / mz) * 200f
            if (sx2 in 0f..W && sy2 in 0f..H) {
                scope.drawCircle(hudRed, 4f, Offset(sx2, sy2))
                scope.drawCircle(hudRed.copy(alpha = 0.3f), 8f, Offset(sx2, sy2))
            }
        }

        // ── Player projectiles ──
        for (p in projectiles) {
            val prx = p.x - px; val prz = p.z - pz
            if (prz < 50f) continue
            val sx2 = W / 2f + (prx / prz) * 300f
            val sy2 = horizonY - ((p.y - altitude) / prz) * 200f
            if (sx2 in 0f..W && sy2 in 0f..H) {
                scope.drawCircle(hudGreen, 3f, Offset(sx2, sy2))
            }
        }

        // ── Aircraft (chase cam) ──
        if (chaseCam) {
            drawAircraftChase(scope)
        } else {
            drawCockpit(scope)
        }

        // ── HUD ──
        drawHUD(scope)

        // ── Touch controls overlay ──
        drawTouchControls(scope)
    }

    // ── 3D aircraft model (flat-shaded, ~20 triangles) ──
    private fun drawAircraftChase(scope: DrawScope) {
        val cx = W / 2f
        val cy = H * 0.55f
        val s = 2.5f
        val bankAngle = roll

        // Flat-shaded polygons, affected by roll
        val cosR = cos(bankAngle); val sinR = sin(bankAngle)

        fun rot(x: Float, y: Float): Offset {
            return Offset(cx + (x * cosR - y * sinR) * s, cy + (x * sinR + y * cosR) * s * 0.6f)
        }

        // Shadow/silhouette color based on lighting
        val topColor = bodyLight
        val sideColor = bodyColor
        val bottomColor = bodyDark

        // ── Fuselage (top face) ──
        val path = Path()
        val nose = rot(0f, -35f)
        val fl = rot(-8f, -5f)
        val fr = rot(8f, -5f)
        val bl = rot(-10f, 25f)
        val br2 = rot(10f, 25f)
        val tail = rot(0f, 30f)

        // Top fuselage panel
        path.reset()
        path.moveTo(nose.x, nose.y)
        path.lineTo(fr.x, fr.y)
        path.lineTo(br2.x, br2.y)
        path.lineTo(tail.x, tail.y)
        path.lineTo(bl.x, bl.y)
        path.lineTo(fl.x, fl.y)
        path.close()
        scope.drawPath(path, topColor, style = Fill)
        scope.drawPath(path, Color(0xFF444444), style = Stroke(1f))

        // ── Forward-swept wings (left) ──
        val wl1 = rot(-8f, 0f)
        val wl2 = rot(-45f, -18f)
        val wl3 = rot(-18f, 8f)
        path.reset()
        path.moveTo(wl1.x, wl1.y)
        path.lineTo(wl2.x, wl2.y)
        path.lineTo(wl3.x, wl3.y)
        path.close()
        val leftWingColor = if (bankAngle > 0) bottomColor else topColor
        scope.drawPath(path, leftWingColor, style = Fill)
        scope.drawPath(path, Color(0xFF444444), style = Stroke(1f))

        // ── Forward-swept wings (right) ──
        val wr1 = rot(8f, 0f)
        val wr2 = rot(45f, -18f)
        val wr3 = rot(18f, 8f)
        path.reset()
        path.moveTo(wr1.x, wr1.y)
        path.lineTo(wr2.x, wr2.y)
        path.lineTo(wr3.x, wr3.y)
        path.close()
        val rightWingColor = if (bankAngle < 0) bottomColor else topColor
        scope.drawPath(path, rightWingColor, style = Fill)
        scope.drawPath(path, Color(0xFF444444), style = Stroke(1f))

        // ── Canted vertical tails ──
        val vtl1 = rot(-10f, 18f)
        val vtl2 = rot(-18f, 10f)
        val vtl3 = rot(-12f, 25f)
        path.reset()
        path.moveTo(vtl1.x, vtl1.y)
        path.lineTo(vtl2.x, vtl2.y)
        path.lineTo(vtl3.x, vtl3.y)
        path.close()
        scope.drawPath(path, sideColor, style = Fill)
        scope.drawPath(path, Color(0xFF444444), style = Stroke(1f))

        val vtr1 = rot(10f, 18f)
        val vtr2 = rot(18f, 10f)
        val vtr3 = rot(12f, 25f)
        path.reset()
        path.moveTo(vtr1.x, vtr1.y)
        path.lineTo(vtr2.x, vtr2.y)
        path.lineTo(vtr3.x, vtr3.y)
        path.close()
        scope.drawPath(path, sideColor, style = Fill)
        scope.drawPath(path, Color(0xFF444444), style = Stroke(1f))

        // ── Engine exhausts ──
        val el = rot(-6f, 28f)
        val er = rot(6f, 28f)
        val exAlpha = (0.3f + throttle * 0.7f).coerceIn(0f, 1f)
        scope.drawCircle(Color(0xFFFF6600).copy(alpha = exAlpha), 4f * s * 0.3f, el)
        scope.drawCircle(Color(0xFFFF6600).copy(alpha = exAlpha), 4f * s * 0.3f, er)

        // ── Red star on left wing ──
        val starPos = rot(-25f, -8f)
        scope.drawCircle(starRed, 4f, starPos)

        // ── Cockpit canopy ──
        val cp1 = rot(-4f, -15f)
        val cp2 = rot(4f, -15f)
        val cp3 = rot(3f, -5f)
        val cp4 = rot(-3f, -5f)
        path.reset()
        path.moveTo(cp1.x, cp1.y)
        path.lineTo(cp2.x, cp2.y)
        path.lineTo(cp3.x, cp3.y)
        path.lineTo(cp4.x, cp4.y)
        path.close()
        scope.drawPath(path, Color(0xFF1A3A5A), style = Fill)
        scope.drawPath(path, Color(0xFF3388BB), style = Stroke(1f))
    }

    private fun drawCockpit(scope: DrawScope) {
        // Cockpit view: instrument panel at bottom, canopy frame
        val panelTop = H * 0.65f

        // Canopy frame
        scope.drawLine(Color(0xFF333333), Offset(0f, 0f), Offset(W * 0.15f, panelTop), 3f)
        scope.drawLine(Color(0xFF333333), Offset(W, 0f), Offset(W * 0.85f, panelTop), 3f)
        scope.drawLine(Color(0xFF333333), Offset(W / 2f, 0f), Offset(W / 2f, panelTop * 0.3f), 2f)

        // Instrument panel background
        scope.drawRect(Color(0xFF1A1A1A), Offset(0f, panelTop), Size(W, H - panelTop))
        scope.drawLine(Color(0xFF333333), Offset(0f, panelTop), Offset(W, panelTop), 2f)

        // Artificial horizon indicator
        val ahCx = W * 0.3f; val ahCy = panelTop + (H - panelTop) * 0.4f
        val ahR = 40f
        scope.drawCircle(Color(0xFF0000AA), ahR, Offset(ahCx, ahCy))
        val ahHorizon = ahCy + pitch * 30f
        scope.drawRect(Color(0xFF006600), Offset(ahCx - ahR, ahHorizon),
            Size(ahR * 2, ahCy + ahR - ahHorizon))
        scope.drawCircle(Color.Transparent, ahR, Offset(ahCx, ahCy), style = Stroke(2f))
        // Wings indicator
        scope.drawLine(hudGreen, Offset(ahCx - 15f, ahCy), Offset(ahCx - 5f, ahCy), 2f)
        scope.drawLine(hudGreen, Offset(ahCx + 5f, ahCy), Offset(ahCx + 15f, ahCy), 2f)
        scope.drawCircle(hudGreen, 2f, Offset(ahCx, ahCy))

        scope.txt("ALT", W * 0.55f, panelTop + 18f, hudGreen, 9f)
        scope.txt("${altitude.toInt()}", W * 0.55f, panelTop + 38f, hudGreen, 14f)
        scope.txt("KTS", W * 0.55f, panelTop + 65f, hudGreen, 9f)
        scope.txt("${speed.toInt()}", W * 0.55f, panelTop + 85f, hudGreen, 14f)
        scope.txt("HDG", W * 0.75f, panelTop + 18f, hudGreen, 9f)
        val hdg = ((yaw / DEG + 360f) % 360f).toInt()
        scope.txt("${hdg}°", W * 0.75f, panelTop + 38f, hudGreen, 14f)
    }

    private fun drawHUD(scope: DrawScope) {
        val smx = W / 2f - 80f; val smy = 18f
        scope.txt("STEALTH", W / 2f, 10f, hudGreen, 9f, true)
        scope.drawRect(Color(0xFF333333), Offset(smx, smy), Size(160f, 10f))
        val stealthColor = when {
            stealthMeter < 0.3f -> hudGreen
            stealthMeter < 0.6f -> hudAmber
            else -> hudRed
        }
        scope.drawRect(stealthColor, Offset(smx, smy), Size(160f * (1f - stealthMeter), 10f))
        if (detected && (time * 4f).toInt() % 2 == 0) {
            scope.txt("! DETECTED !", W / 2f, 38f, hudRed, 14f, true)
        }

        scope.txt("ALT ${altitude.toInt()} ft", 55f, 50f, hudGreen, 10f)
        scope.txt("SPD ${speed.toInt()} kts", 55f, 66f, hudGreen, 10f)
        scope.txt("HDG ${((yaw / DEG + 360f) % 360f).toInt()}°", 55f, 82f, hudGreen, 10f)

        val wColor = if (selectedWeapon == 0) hudGreen else Color(0xFF336633)
        scope.txt("AGM: $missiles", W - 120f, 50f, wColor, 10f)
        val bColor = if (selectedWeapon == 1) hudGreen else Color(0xFF336633)
        scope.txt("BMB: $bombs", W - 120f, 66f, bColor, 10f)
        scope.txt("ECM: $ecmCharges", W - 120f, 82f, hudAmber, 10f)

        scope.txt("TGT: $missionDestroyed/$missionTargets", W - 120f, 102f, hudGreen, 10f)
        scope.txt("SCORE: $score", 55f, 102f, hudGreen, 10f)

        drawRadarMap(scope)

        if (samMissiles.isNotEmpty() && (time * 6f).toInt() % 2 == 0) {
            scope.txt("!! MISSILE !!", W / 2f, H * 0.4f, hudRed, 22f, true)
        }

        val vbx = W - 60f; val vby = H - 30f
        scope.drawRect(Color(0xFF333333), Offset(vbx - 25f, vby - 10f), Size(50f, 20f))
        scope.txt(if (chaseCam) "COCK" else "CHASE", vbx, vby - 3f, hudGreen, 8f, true)
    }

    private fun drawRadarMap(scope: DrawScope) {
        val rx = W - 75f; val ry = H - 90f; val rr = 55f

        // Radar background
        scope.drawCircle(Color(0xFF001A00), rr, Offset(rx, ry))
        scope.drawCircle(hudGreen.copy(alpha = 0.3f), rr, Offset(rx, ry), style = Stroke(1f))
        // Range rings
        scope.drawCircle(hudGreen.copy(alpha = 0.15f), rr * 0.5f, Offset(rx, ry), style = Stroke(0.5f))
        scope.drawCircle(hudGreen.copy(alpha = 0.15f), rr * 0.25f, Offset(rx, ry), style = Stroke(0.5f))

        // Sweep line
        val sweepAngle = time * 2f
        scope.drawLine(hudGreen.copy(alpha = 0.5f),
            Offset(rx, ry),
            Offset(rx + cos(sweepAngle) * rr, ry + sin(sweepAngle) * rr), 1f)

        val mapScale = rr / 8000f  // 8km radius view

        // Player (center dot)
        scope.drawCircle(hudGreen, 3f, Offset(rx, ry))

        // Radar sites
        for (radar in radarSites) {
            if (!radar.alive) continue
            val dx = (radar.x - px) * mapScale
            val dz = (radar.z - pz) * mapScale
            val rdx = dx * cos(yaw) + dz * sin(yaw)
            val rdz = -dx * sin(yaw) + dz * cos(yaw)
            if (rdx * rdx + rdz * rdz < rr * rr) {
                val color = if (radar.detected) hudRed else hudAmber
                scope.drawCircle(color, 3f, Offset(rx + rdx, ry - rdz))
                // Range circle
                scope.drawCircle(color.copy(alpha = 0.2f), radar.range * mapScale,
                    Offset(rx + rdx, ry - rdz), style = Stroke(0.5f))
            }
        }

        // Targets
        for (t in targets) {
            if (!t.alive) continue
            val dx = (t.x - px) * mapScale
            val dz = (t.z - pz) * mapScale
            val rdx = dx * cos(yaw) + dz * sin(yaw)
            val rdz = -dx * sin(yaw) + dz * cos(yaw)
            if (rdx * rdx + rdz * rdz < rr * rr) {
                scope.drawRect(hudGreen, Offset(rx + rdx - 2f, ry - rdz - 2f), Size(4f, 4f))
            }
        }

        // SAM missiles
        for (m in samMissiles) {
            val dx = (m.x - px) * mapScale
            val dz = (m.z - pz) * mapScale
            val rdx = dx * cos(yaw) + dz * sin(yaw)
            val rdz = -dx * sin(yaw) + dz * cos(yaw)
            if (rdx * rdx + rdz * rdz < rr * rr) {
                scope.drawCircle(hudRed, 2f, Offset(rx + rdx, ry - rdz))
            }
        }
    }

    private fun drawTouchControls(scope: DrawScope) {
        val tx = 25f; val ty1 = H * 0.35f; val ty2 = H * 0.75f
        val tHeight = ty2 - ty1
        scope.drawRect(Color(0xFF222222), Offset(tx - 12f, ty1), Size(24f, tHeight))
        scope.drawRect(Color(0xFF444444), Offset(tx - 12f, ty1), Size(24f, tHeight), style = Stroke(1f))
        val knobY = ty2 - throttle * tHeight
        scope.drawRect(hudGreen, Offset(tx - 10f, knobY - 5f), Size(20f, 10f))
        scope.txt("MAX", tx, ty1 - 8f, hudGreen, 7f, true)
        scope.txt("MIN", tx, ty2 + 10f, hudGreen, 7f, true)
        scope.txt("FIRE", W - 40f, H * 0.5f, hudGreen.copy(alpha = 0.3f), 10f, true)
        scope.txt("[W]", W - 40f, H * 0.38f, hudAmber.copy(alpha = 0.5f), 9f, true)
        scope.txt("[ECM]", W - 40f, H * 0.62f, hudAmber.copy(alpha = 0.5f), 9f, true)
    }

    private fun drawMissionComplete(scope: DrawScope) {
        scope.drawRect(Color(0xFF001100), Offset.Zero, Size(W, H))
        scope.txt("MISSION COMPLETE", W / 2f, 130f, hudGreen, 28f, true)
        scope.txt("All targets destroyed", W / 2f, 200f, hudGreen, 14f, true)
        scope.txt("Score: $score", W / 2f, 260f, hudAmber, 18f, true)
        scope.txt("Best: $best", W / 2f, 310f, hudGreen, 13f, true)
        val stealthBonus = if (stealthMeter < 0.3f) "GHOST — Perfect stealth!" else
            if (stealthMeter < 0.6f) "Shadow — Mostly undetected" else "Detected — improve stealth skills"
        scope.txt(stealthBonus, W / 2f, 360f, if (stealthMeter < 0.3f) hudGreen else hudAmber, 11f, true)
        if ((time * 2f).toInt() % 2 == 0) {
            scope.txt("CLICK TO CONTINUE", W / 2f, 430f, hudAmber, 14f, true)
        }
    }

    private fun drawGameOver(scope: DrawScope) {
        scope.drawRect(Color(0xFF1A0000), Offset.Zero, Size(W, H))
        scope.txt("MISSION FAILED", W / 2f, 160f, hudRed, 28f, true)
        val reason = if (altitude <= minAlt + 10) "Aircraft crashed" else "Shot down by SAM"
        scope.txt(reason, W / 2f, 230f, hudRed, 16f, true)
        scope.txt("Score: $score", W / 2f, 290f, hudAmber, 16f, true)
        scope.txt("Best: $best", W / 2f, 330f, hudGreen, 12f, true)
        if ((time * 2f).toInt() % 2 == 0) {
            scope.txt("CLICK TO RETRY", W / 2f, 420f, hudAmber, 14f, true)
        }
    }

    // ═══════════════════════════════════════════════
    //                INPUT
    // ═══════════════════════════════════════════════

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        dpadX = dx; dpadY = dy; dpadFreshT = time
    }

    override fun onFire(ctx: GameContext) {
        when (stage) {
            Stage.TITLE -> { stage = Stage.BRIEFING; time = 0f }
            Stage.BRIEFING -> { setupMission(); stage = Stage.PLAY; time = 0f }
            Stage.PLAY -> fireWeapon()
            Stage.MISSION_COMPLETE -> { stage = Stage.TITLE; time = 0f }
            Stage.GAMEOVER -> { stage = Stage.TITLE; time = 0f }
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        when (stage) {
            Stage.TITLE -> { stage = Stage.BRIEFING; time = 0f }
            Stage.BRIEFING -> { setupMission(); stage = Stage.PLAY; time = 0f }
            Stage.PLAY -> handlePlayTouch(x, y, id, true)
            Stage.MISSION_COMPLETE -> { stage = Stage.TITLE; time = 0f }
            Stage.GAMEOVER -> { stage = Stage.TITLE; time = 0f }
        }
    }

    override fun onPointerMove(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (stage == Stage.PLAY) handlePlayTouch(x, y, id, false)
    }

    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (id == throttleTouchId) {
            throttleDragging = false
            throttleTouchId = -1L
        }
    }

    private fun handlePlayTouch(x: Float, y: Float, id: Long, isDown: Boolean) {
        // Throttle slider (left strip, x < 50)
        if (x < 50f) {
            val ty1 = H * 0.35f; val ty2 = H * 0.75f
            if (y in ty1..ty2) {
                throttle = 1f - (y - ty1) / (ty2 - ty1)
                throttle = throttle.coerceIn(0f, 1f)
                throttleDragging = true
                throttleTouchId = id
            }
            return
        }

        if (throttleDragging && id == throttleTouchId) {
            val ty1 = H * 0.35f; val ty2 = H * 0.75f
            throttle = 1f - (y - ty1) / (ty2 - ty1)
            throttle = throttle.coerceIn(0f, 1f)
            return
        }

        // Right side actions
        if (x > W - 80f && isDown) {
            // View toggle (bottom right)
            if (y > H - 45f) {
                chaseCam = !chaseCam
                return
            }
            // Weapon switch
            if (y in H * 0.35f..H * 0.45f) {
                selectedWeapon = (selectedWeapon + 1) % 2
                return
            }
            // ECM
            if (y in H * 0.55f..H * 0.65f) {
                if (ecmCharges > 0) {
                    ecmCharges--
                    // ECM clears SAM missiles and reduces stealth meter
                    samMissiles.clear()
                    stealthMeter = (stealthMeter - 0.5f).coerceAtLeast(0f)
                    samLockTimer = 0f
                }
                return
            }
            // Fire weapon
            if (y in H * 0.45f..H * 0.55f) {
                fireWeapon()
                return
            }
        }

        // Click anywhere on right half to fire
        if (x > W * 0.6f && isDown && y > H * 0.2f && y < H * 0.8f) {
            fireWeapon()
        }
    }

    private fun fireWeapon() {
        if (fireCooldown > 0f) return
        val headX = sin(yaw) * cos(pitch)
        val headZ = cos(yaw) * cos(pitch)
        val headY = sin(pitch)

        when (selectedWeapon) {
            0 -> {
                if (missiles <= 0) return
                missiles--
                projectiles += Projectile(
                    px, altitude, pz,
                    headX * 1200f, headY * 1200f - 100f, headZ * 1200f,
                    5f, false
                )
                fireCooldown = 0.8f
            }
            1 -> {
                if (bombs <= 0) return
                bombs--
                projectiles += Projectile(
                    px, altitude, pz,
                    headX * speed * 0.1f, -200f, headZ * speed * 0.1f,
                    10f, true
                )
                fireCooldown = 1.2f
            }
        }
    }

    override fun dispose() {
        stopGyroscope()
    }

    // Backgrounding the app must stop sensor delivery (battery + pre-launch
    // report flag); resume re-registers only if we aren't already listening.
    override fun onPause(ctx: GameContext) { stopGyroscope() }
    override fun onResume(ctx: GameContext) {
        if (gyroListener == null) ctx.activity?.let { startGyroscope(it) }
    }
}
