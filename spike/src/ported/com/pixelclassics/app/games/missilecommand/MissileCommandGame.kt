package com.pixelclassics.app.games.missilecommand

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Missile Command — 3 batteries, 6 cities, MIRV from wave 3, exploding bullets. */
class MissileCommandGame : Game {
    override val id: String = "missile"
    override val titleResId: Int = R.string.game_missile
    override val backgroundColor: Color = Color(0xFF000020)
    override val introText: String = MISSILE_INTRO_EN
    override fun introTextFor(lang: String): String = pickMissileIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.COLD_WAR_AMBER
    override val helpLines: List<String> = listOf(
        "Click the sky — your closest battery fires there",
        "Protect 6 cities.  Watch for MIRV splits from wave 3.",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Кликни небо — ближайшая батарея стреляет туда",
        "Защити 6 городов.  С 3-й волны боеголовки делятся (РГЧ).",
    ) else helpLines

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private val groundY = H - 35f

    private data class Battery(val x: Float, val y: Float, var ammo: Int)
    private data class City(val x: Float, val y: Float, var alive: Boolean)
    private data class Missile(var x: Float, var y: Float, var vx: Float, var vy: Float,
                               var mirv: Boolean, var mirvSplit: Boolean, var splitY: Float)
    private data class Rocket(var x: Float, var y: Float, val targetX: Float, val targetY: Float,
                              var vx: Float, var vy: Float)
    private data class Explosion(var x: Float, var y: Float, var r: Float, val maxR: Float, var growing: Boolean, val color: Color)

    private val batteries = mutableListOf<Battery>()
    private val cities = mutableListOf<City>()
    private val missiles = mutableListOf<Missile>()
    private val rockets = mutableListOf<Rocket>()
    private val explosions = mutableListOf<Explosion>()
    private var score = 0
    private var best = 0
    private var wave = 1
    private var gameOver = false
    private var missilesThisWave = 0
    private var launched = 0
    private var spawnTimer = 0f
    private var diff = 1   // 0 cadet, 1 commander, 2 armageddon
    private var picking = true   // difficulty pick overlay
    private var time = 0f

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        picking = true
    }

    private fun startWith(d: Int) {
        diff = d
        picking = false
        batteries.clear(); cities.clear(); missiles.clear(); rockets.clear(); explosions.clear()
        batteries += Battery(W * 0.12f, groundY, 10)
        batteries += Battery(W * 0.50f, groundY, 10)
        batteries += Battery(W * 0.88f, groundY, 10)
        for (p in floatArrayOf(0.22f, 0.32f, 0.40f, 0.60f, 0.70f, 0.78f)) cities += City(W * p, groundY, true)
        score = 0; wave = 1; gameOver = false; launched = 0; spawnTimer = 0f
        // 25.05: 20% fewer missiles in the early waves, equal to old by ~wave 10.
        missilesThisWave = (baseMsl() + wave * mslLvl() * 0.8f).toInt()
    }

    // Speeds and wave sizes tuned 25.05 — Юджин: «на старте чуть-чуть
    // замедлил бы и боеголовки и ракеты, а потом постепенно наращивал».
    private fun baseSpd(): Float = listOf(25f, 40f, 65f)[diff]
    private fun spdLvl(): Float = listOf(5f, 9f, 14f)[diff]
    private fun baseMsl(): Int = listOf(5, 8, 12)[diff]
    private fun mslLvl(): Int = listOf(2, 3, 5)[diff]
    private fun diffName(): String = listOf("CADET", "COMMANDER", "ARMAGEDDON")[diff]

    private fun nearestBattery(tx: Float): Battery? =
        batteries.filter { it.ammo > 0 }.minByOrNull { kotlin.math.abs(it.x - tx) }

    private fun fire(tx: Float, ty: Float, ctx: GameContext) {
        if (gameOver) { gameOver = false; picking = true; return }
        val b = nearestBattery(tx) ?: return
        val dx = tx - b.x; val dy = ty - b.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 1f) return   // tap on the muzzle itself → NaN velocity rocket
        b.ammo--
        val speed = 420f
        rockets += Rocket(b.x, b.y, tx, ty, dx / dist * speed, dy / dist * speed)
        ctx.sound.shoot()
    }

    private fun spawnMissile() {
        val targetXs = cities.filter { it.alive }.map { it.x } +
            batteries.filter { it.ammo > 0 }.map { it.x }
        if (targetXs.isEmpty()) return
        val pickX = targetXs.random()
        val sx = Random.nextFloat() * W
        val dx = pickX - sx; val dy = groundY
        val dist = sqrt(dx * dx + dy * dy)
        val sp = baseSpd() + wave * spdLvl() + Random.nextFloat() * 15f
        val mirv = wave >= 3 && Random.nextFloat() < 0.2f
        missiles += Missile(sx, -5f, dx / dist * sp, dy / dist * sp, mirv, false, H * (0.3f + Random.nextFloat() * 0.3f))
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (picking || gameOver) return
        updateCrosshair(dt)
        if (launched < missilesThisWave) {
            spawnTimer += dt
            // 25.05: more breathing room early; same density by wave ~13.
            val interval = (1.6f - wave * 0.10f).coerceAtLeast(0.30f)
            if (spawnTimer >= interval) { spawnTimer = 0f; spawnMissile(); launched++ }
        }

        // Splitting MIRV warheads adds to `missiles` mid-iteration, which
        // crashes ArrayList iterators with CME. Queue new children and
        // append once the loop is done.
        val mirvSpawned = mutableListOf<Missile>()
        val mIt = missiles.iterator()
        msl@ while (mIt.hasNext()) {
            val m = mIt.next()
            m.x += m.vx * dt; m.y += m.vy * dt
            if (m.mirv && !m.mirvSplit && m.y > m.splitY) {
                m.mirvSplit = true
                val curSp = sqrt(m.vx * m.vx + m.vy * m.vy)
                for (k in 0 until 2) {
                    val alive = cities.filter { it.alive }
                    if (alive.isEmpty()) break
                    val tgt = alive.random()
                    val dx = tgt.x - m.x; val dy = groundY - m.y
                    val dist = sqrt(dx * dx + dy * dy)
                    val sp = curSp * 1.1f
                    mirvSpawned += Missile(m.x, m.y, dx / dist * sp, dy / dist * sp, false, true, 0f)
                }
            }
            if (m.y >= groundY) {
                for (city in cities) if (city.alive && kotlin.math.abs(m.x - city.x) < 20f) {
                    city.alive = false; addExplosion(m.x, groundY, 50f, Color(0xFFFF4400))
                    ctx.sound.cityDestroyed()
                }
                for (bat in batteries) if (bat.ammo > 0 && kotlin.math.abs(m.x - bat.x) < 15f) {
                    bat.ammo = 0; addExplosion(m.x, groundY, 35f, Color(0xFFFF8800))
                }
                addExplosion(m.x, groundY, 30f, Color(0xFFFF2200))
                mIt.remove()
                if (cities.none { it.alive }) { gameOver = true; ctx.sound.gameOver(); if (score > best) { best = score; ctx.scores.set(id, score) } }
                continue@msl
            }
            for (exp in explosions) {
                val dx = m.x - exp.x; val dy = m.y - exp.y
                if (dx * dx + dy * dy < exp.r * exp.r) {
                    mIt.remove(); score += 25; ctx.sound.explosion(); continue@msl
                }
            }
        }
        if (mirvSpawned.isNotEmpty()) missiles.addAll(mirvSpawned)

        val rIt = rockets.iterator()
        while (rIt.hasNext()) {
            val r = rIt.next()
            r.x += r.vx * dt; r.y += r.vy * dt
            val dx = r.x - r.targetX; val dy = r.y - r.targetY
            // Proximity OR flown past the target (a large-dt frame can step
            // clean over the 9px proximity ring — without the dot-product
            // check such a rocket never detonates and lives forever).
            val passed = dx * r.vx + dy * r.vy >= 0f
            if (dx * dx + dy * dy < 80f || passed) {
                addExplosion(r.targetX, r.targetY, 50f, Color(0xFFFF8800))
                rIt.remove(); ctx.sound.bigExplosion()
            }
        }

        val eIt = explosions.iterator()
        while (eIt.hasNext()) {
            val e = eIt.next()
            if (e.growing) {
                e.r += 110f * dt
                if (e.r >= e.maxR) e.growing = false
                for (city in cities) if (city.alive) {
                    val dx = city.x - e.x; val dy = city.y - e.y
                    if (dx * dx + dy * dy < e.r * e.r) city.alive = false
                }
                for (bat in batteries) if (bat.ammo > 0) {
                    val dx = bat.x - e.x; val dy = bat.y - e.y
                    val rad = e.r * 0.7f
                    if (dx * dx + dy * dy < rad * rad) bat.ammo = 0
                }
            } else {
                e.r -= 40f * dt
                if (e.r <= 0f) eIt.remove()
            }
        }

        // Lose check AFTER explosion splash damage too — a city dying inside a
        // blast (including friendly fire) must end the game, not only a direct
        // missile ground impact.
        if (!gameOver && cities.none { it.alive }) {
            gameOver = true; ctx.sound.gameOver()
            if (score > best) { best = score; ctx.scores.set(id, score) }
        }

        if (launched >= missilesThisWave && missiles.isEmpty() && !gameOver) {
            wave++
            missilesThisWave = (baseMsl() + wave * mslLvl() * 0.8f).toInt()
            launched = 0
            for (b in batteries) b.ammo = 10
            score += cities.count { it.alive } * 50
        }
    }

    private fun addExplosion(x: Float, y: Float, maxR: Float, color: Color) {
        explosions += Explosion(x, y, 3f, maxR, true, color)
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Deep-space sky, two solid bands instead of gradient — keeps the look chunky.
        drawRect(Color(0xFF000020), topLeft = Offset.Zero, size = Size(W, H * 0.55f))
        drawRect(Color(0xFF0A0040), topLeft = Offset(0f, H * 0.55f), size = Size(W, H * 0.45f))

        // Pixel stars (deterministic).
        for (i in 0 until 60) {
            val sx = ((i * 173 + 37) % W.toInt()).toFloat()
            val sy = ((i * 131 + 17) % (groundY - 40f).toInt()).toFloat()
            val on = ((time * 1.5f + i) % 1.5f) > 0.4f
            drawRect(if (on) Color.White else Color(0xFFAAAAAA), topLeft = Offset(sx, sy + 30f), size = Size(1f, 1f))
        }

        // Mountain horizon — chunky pixel silhouette.
        val mountainY = groundY - 28f
        drawRect(Color(0xFF1A0A30), topLeft = Offset(0f, mountainY), size = Size(W, groundY - mountainY))
        for (mx in 0..(W / 24f).toInt()) {
            val baseX = mx * 24f
            val height = (12f + ((mx * 47) % 18))
            // Pyramid stack of pixel blocks.
            for (lvl in 0 until (height / 4f).toInt()) {
                val w = 24f - lvl * 4f
                val y = mountainY - lvl * 4f
                drawRect(Color(0xFF2A1040), topLeft = Offset(baseX + lvl * 2f, y), size = Size(w, 4f))
                drawRect(Color(0xFF3A1858), topLeft = Offset(baseX + lvl * 2f, y), size = Size(w, 1f))
            }
        }

        // Ground band — desert.
        drawRect(Color(0xFF6A5018), topLeft = Offset(0f, groundY - 4f), size = Size(W, H - groundY + 4f))
        drawRect(Color(0xFF8B6914), topLeft = Offset(0f, groundY - 2f), size = Size(W, 2f))
        drawRect(Color(0xFF5A4510), topLeft = Offset(0f, H - 12f), size = Size(W, 12f))

        // Cities — varied skylines.
        for ((idx, city) in cities.withIndex()) {
            if (!city.alive) {
                drawRect(Color(0xFF333333), topLeft = Offset(city.x - 14f, groundY - 5f), size = Size(28f, 5f))
                drawRect(Color(0xFF1A1A1A), topLeft = Offset(city.x - 14f, groundY - 5f), size = Size(28f, 1f))
                continue
            }
            // Three-building skyline with windows.
            val pattern = idx % 3
            fun bld(bx: Float, w: Float, h: Float) {
                drawRect(Color(0xFF003355), topLeft = Offset(bx, groundY - h), size = Size(w, h))
                drawRect(Color(0xFF006688), topLeft = Offset(bx + 1f, groundY - h + 1f), size = Size(w - 2f, h - 2f))
                // Windows — two columns of 3 tall.
                var wy = groundY - h + 3f
                while (wy < groundY - 3f) {
                    drawRect(Color(0xFFFFEE88), topLeft = Offset(bx + 2f, wy), size = Size(2f, 2f))
                    drawRect(Color(0xFFFFEE88), topLeft = Offset(bx + w - 4f, wy), size = Size(2f, 2f))
                    wy += 5f
                }
            }
            when (pattern) {
                0 -> { bld(city.x - 14f, 8f, 18f); bld(city.x - 4f, 10f, 26f); bld(city.x + 7f, 7f, 14f) }
                1 -> { bld(city.x - 12f, 9f, 22f); bld(city.x - 2f, 7f, 16f); bld(city.x + 6f, 8f, 20f) }
                else -> { bld(city.x - 14f, 7f, 14f); bld(city.x - 5f, 11f, 30f); bld(city.x + 7f, 7f, 18f) }
            }
        }

        // Batteries — proper turret look.
        for (bat in batteries) {
            val col = if (bat.ammo > 0) Color(0xFF558855) else Color(0xFF444444)
            val colDk = if (bat.ammo > 0) Color(0xFF2A4A2A) else Color(0xFF222222)
            // Base.
            drawRect(colDk, topLeft = Offset(bat.x - 14f, groundY - 10f), size = Size(28f, 10f))
            drawRect(col, topLeft = Offset(bat.x - 13f, groundY - 9f), size = Size(26f, 9f))
            // Turret dome (half-circle as pixel stack).
            drawRect(colDk, topLeft = Offset(bat.x - 9f, groundY - 16f), size = Size(18f, 6f))
            drawRect(col, topLeft = Offset(bat.x - 8f, groundY - 15f), size = Size(16f, 5f))
            drawRect(colDk, topLeft = Offset(bat.x - 5f, groundY - 20f), size = Size(10f, 4f))
            drawRect(col, topLeft = Offset(bat.x - 4f, groundY - 19f), size = Size(8f, 3f))
            // Antenna.
            if (bat.ammo > 0) drawRect(Color(0xFF88FF88), topLeft = Offset(bat.x - 1f, groundY - 26f), size = Size(2f, 6f))
            // Ammo count.
            if (bat.ammo > 0) drawDosText(bat.ammo.toString(), x = bat.x, y = groundY - 32f, color = Color(0xFF88FF88), size = 11f, bold = true, align = TextAlign.CENTER)
        }

        // Enemy missile trails — chunky red dashes.
        for (m in missiles) {
            val dx = m.vx; val dy = m.vy
            val len = sqrt(dx * dx + dy * dy)
            if (len > 0f) {
                val ux = dx / len; val uy = dy / len
                // Trail dashes.
                for (k in 0 until 6) {
                    val t = (k * 7f)
                    drawRect(Color(0xFFFF4444), topLeft = Offset(m.x - ux * t - 1f, m.y - uy * t - 1f), size = Size(2f, 2f))
                }
            }
            // Warhead.
            drawRect(Color.Black, topLeft = Offset(m.x - 3f, m.y - 3f), size = Size(6f, 6f))
            drawRect(Color(0xFFFF4444), topLeft = Offset(m.x - 2f, m.y - 2f), size = Size(4f, 4f))
            drawRect(Color(0xFFFFAA00), topLeft = Offset(m.x - 1f, m.y - 1f), size = Size(2f, 2f))
        }
        // Player rockets — green vector trail.
        for (r in rockets) {
            val dx = r.vx; val dy = r.vy
            val len = sqrt(dx * dx + dy * dy)
            if (len > 0f) {
                val ux = dx / len; val uy = dy / len
                for (k in 0 until 8) {
                    val t = (k * 6f)
                    drawRect(Color(0xFF88FF88), topLeft = Offset(r.x - ux * t, r.y - uy * t), size = Size(1f, 1f))
                }
            }
            drawRect(Color.White, topLeft = Offset(r.x - 2f, r.y - 2f), size = Size(4f, 4f))
            // Crosshair — chunky pixel plus.
            drawRect(Color(0xFF88FF88), topLeft = Offset(r.targetX - 6f, r.targetY - 1f), size = Size(4f, 2f))
            drawRect(Color(0xFF88FF88), topLeft = Offset(r.targetX + 2f, r.targetY - 1f), size = Size(4f, 2f))
            drawRect(Color(0xFF88FF88), topLeft = Offset(r.targetX - 1f, r.targetY - 6f), size = Size(2f, 4f))
            drawRect(Color(0xFF88FF88), topLeft = Offset(r.targetX - 1f, r.targetY + 2f), size = Size(2f, 4f))
        }
        // Explosions — a layered plasma bloom: soft halo, warm corona, a
        // white-hot core, the classic Missile-Command expanding ring outline,
        // and sparks that rotate as the blast grows.
        for (e in explosions) {
            val r = e.r
            val c = Offset(e.x, e.y)
            drawCircle(e.color.copy(alpha = 0.12f), radius = r * 1.28f, center = c)
            drawCircle(e.color.copy(alpha = 0.30f), radius = r, center = c)
            drawCircle(e.color.copy(alpha = 0.55f), radius = r * 0.72f, center = c)
            drawCircle(Color(0xFFFFF2B0).copy(alpha = 0.9f), radius = r * 0.42f, center = c)
            drawCircle(Color(0xFFFFFFFF), radius = r * 0.20f, center = c)
            drawCircle(Color(0xFFFFEFA0).copy(alpha = 0.85f), radius = r, center = c,
                style = androidx.compose.ui.graphics.drawscope.Stroke(2f))
            val n = 10
            for (k in 0 until n) {
                val ang = k * (2f * PI.toFloat() / n) + r * 0.06f
                val rad = r * (0.80f + 0.16f * ((k % 3) - 1))
                drawCircle(Color(0xFFFFF060), 1.6f, Offset(e.x + cos(ang) * rad, e.y + sin(ang) * rad))
            }
        }

        drawCrosshair(this)

        // HUD.
        drawDosText("SCORE  $score", x = 10f, y = 22f, color = Color(0xFF00CCFF), size = 14f, bold = true)
        drawDosText("WAVE $wave  [${diffName()}]", x = W - 10f, y = 22f, color = Color(0xFF00CCFF), size = 14f, bold = true, align = TextAlign.RIGHT)
        val totalAmmo = batteries.sumOf { it.ammo }
        val ammoColor = when { totalAmmo > 5 -> Color(0xFF44FF44); totalAmmo > 0 -> Color(0xFFFFAA00); else -> Color(0xFFFF4444) }
        drawDosText("MISSILES  $totalAmmo", x = W / 2f, y = 22f, color = ammoColor, size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText("BEST  $best", x = W / 2f, y = 40f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.CENTER)

        if (picking) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("MISSILE COMMAND", x = W / 2f, y = H * 0.28f, color = Color(0xFFFF2222), size = 32f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CHOOSE DIFFICULTY", "ВЫБЕРИ СЛОЖНОСТЬ"), x = W / 2f, y = H * 0.36f, color = Color(0xFF78909C), size = 14f, align = TextAlign.CENTER)
            for ((i, label) in listOf("CADET", "COMMANDER", "ARMAGEDDON").withIndex()) {
                val y = H * 0.48f + i * 50f
                drawRect(Color(0xFF1A0A0A), topLeft = Offset(W / 2f - 130f, y - 18f), size = Size(260f, 32f))
                drawRect(Color(0xFFFF4444), topLeft = Offset(W / 2f - 130f, y - 18f), size = Size(260f, 1f))
                drawDosText(label, x = W / 2f, y = y + 4f, color = Color(0xFFFF4444), size = 16f, bold = true, align = TextAlign.CENTER)
            }
            drawDosText("BEST  $best", x = W / 2f, y = H - 16f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.CENTER)
        }

        if (gameOver) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("THE END", "КОНЕЦ"), x = W / 2f, y = H / 2f - 40f, color = Color(0xFFFF2222), size = 36f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Score  $score", "Счёт  $score"), x = W / 2f, y = H / 2f, color = Color(0xFF00CCFF), size = 20f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Wave  $wave", "Волна  $wave"), x = W / 2f, y = H / 2f + 28f, color = Color(0xFF00CCFF), size = 20f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"), x = W / 2f, y = H / 2f + 65f, color = Color(0xFF666666), size = 14f, align = TextAlign.CENTER)
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (picking) {
            for (i in 0 until 3) {
                val y0 = H * 0.48f + i * 50f - 18f
                if (x in (W / 2f - 130f)..(W / 2f + 130f) && y in y0..y0 + 32f) {
                    startWith(i); ctx.sound.menuSelect(); return
                }
            }
            return
        }
        if (gameOver) { gameOver = false; picking = true; ctx.sound.menuSelect(); return }
        crossVisible = false   // mouse fire hides the keyboard crosshair
        fire(x, y, ctx)
    }

    // ── Keyboard crosshair (Юджин, ревью 17.07) ──────────────────────────
    // The arcade original aimed with a trackball; the mouse tap above is its
    // heir. Arrows drive an on-screen crosshair that accelerates the longer
    // you hold (the "Shift-турбо" feel without needing a second modifier);
    // FIRE launches at the crosshair.
    private var crossX = 0f
    private var crossY = 0f
    private var crossVisible = false
    private var crossDx = 0
    private var crossDy = 0
    private var crossFresh = -1f
    private var crossHoldT = 0f

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (picking || gameOver) return
        if (dx == 0 && dy == 0) return
        if (!crossVisible) { crossVisible = true; crossX = W / 2f; crossY = H * 0.4f }
        crossDx = dx; crossDy = dy; crossFresh = time
    }

    override fun onFire(ctx: GameContext) {
        if (picking) { startWith(diff); ctx.sound.menuSelect(); return }
        if (gameOver) { gameOver = false; picking = true; ctx.sound.menuSelect(); return }
        if (crossVisible) fire(crossX, crossY, ctx)
    }

    /** Call from update(): glide the crosshair while a direction is held. */
    private fun updateCrosshair(dt: Float) {
        if (!crossVisible) return
        val holding = (crossDx != 0 || crossDy != 0) && time - crossFresh < 0.16f
        if (holding) {
            crossHoldT += dt
            val speed = 260f + (crossHoldT * 700f).coerceAtMost(640f)   // accelerates while held
            crossX = (crossX + crossDx * speed * dt).coerceIn(8f, W - 8f)
            crossY = (crossY + crossDy * speed * dt).coerceIn(8f, H * 0.75f)
        } else {
            crossHoldT = 0f
        }
    }

    /** Call from draw(): the crosshair reticle. */
    private fun drawCrosshair(scope: androidx.compose.ui.graphics.drawscope.DrawScope) = with(scope) {
        if (!crossVisible || picking || gameOver) return@with
        val c = Color(0xFF00FFAA)
        drawRect(c, topLeft = Offset(crossX - 9f, crossY - 1f), size = Size(6f, 2f))
        drawRect(c, topLeft = Offset(crossX + 3f, crossY - 1f), size = Size(6f, 2f))
        drawRect(c, topLeft = Offset(crossX - 1f, crossY - 9f), size = Size(2f, 6f))
        drawRect(c, topLeft = Offset(crossX - 1f, crossY + 3f), size = Size(2f, 6f))
    }
}
