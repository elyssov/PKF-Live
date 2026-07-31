package com.pixelclassics.app.games.arkanoid

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
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

/** Arkanoid: 8 looping levels, silver bricks, 5 power-ups (W/G/M/S/L). */
class ArkanoidGame : Game {
    override val id: String = "arkanoid"
    override val titleResId: Int = R.string.game_arkanoid
    override val backgroundColor: Color = Color(0xFF0A0A2E)

    private val W = 640f
    private val H = 480f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H
    // Controls live in the side gutters: the left D-pad steers the paddle, the
    // right FIRE button launches the ball, fires the gun power-up and releases a
    // caught ball. No on-canvas drag — the paddle moves by the joystick only.
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    override val introText: String = ARKANOID_INTRO_EN
    override fun introTextFor(lang: String): String = pickArkanoidIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PARCHMENT

    override val helpLines: List<String> get() = listOf(
        "◀ ▶  — move the paddle",
        "●  — launch / shoot the gun / release a held ball",
        "Smash every brick. Power-ups: W G M S L F C",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "◀ ▶  — движение ракетки",
        "●  — запуск / стрельба / отпустить пойманный шар",
        "Разбей все кирпичи. Бонусы: W G M S L F C",
    ) else helpLines

    // Side D-pad steering. The overlay auto-repeats onDirection ~every 100ms while
    // held; we keep a freshness window so the paddle glides while held and stops
    // shortly after release (no release event is delivered by the control model).
    private var steerDir = 0
    private var steerFreshT = -1f

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (dx != 0) { steerDir = dx; steerFreshT = time }
    }

    override fun onFire(ctx: GameContext) {
        when (state) {
            State.GAMEOVER -> { restart(); state = State.READY }
            State.READY, State.BETWEEN -> launch()
            // A caught ball (Catch power-up) must be releasable, otherwise it
            // rides the paddle forever and the game soft-locks.
            State.PLAYING -> if (balls.any { it.onPaddle }) launch() else if (gunUntil > time) shoot(ctx)
        }
    }

    private val brickColors = listOf(
        Color(0xFFE63946), Color(0xFFF4845F), Color(0xFFF7B32B), Color(0xFF2EC4B6),
        Color(0xFF3A86FF), Color(0xFF8338EC), Color(0xFFFF006E), Color(0xFF06D6A0),
        Color(0xFF118AB2), Color(0xFFFFD166), Color(0xFFEF476F), Color(0xFF26547C),
    )
    private val silver = Color(0xFFB0B0C0)

    private data class Brick(var x: Float, var y: Float, var w: Float, var h: Float,
                             var color: Color, var hits: Int, var maxHits: Int,
                             var alive: Boolean = true, var unbreakable: Boolean = false)

    private data class Ball(var x: Float, var y: Float, var vx: Float, var vy: Float,
                            var size: Float, var onPaddle: Boolean)

    private data class PowerUp(var x: Float, var y: Float, val kind: Char, val color: Color)
    private data class Bullet(var x: Float, var y: Float)

    private var paddleX = W / 2f
    private var paddleW = 100f
    private val paddleH = 12f
    private val paddleBaseW = 100f

    private val balls = mutableListOf<Ball>()
    private val bricks = mutableListOf<Brick>()
    private val powerups = mutableListOf<PowerUp>()
    private val bullets = mutableListOf<Bullet>()

    private var wideUntil = 0f
    private var gunUntil = 0f
    private var slowUntil = 0f
    private var fastUntil = 0f
    private var catchUntil = 0f
    private var time = 0f
    private val hudH = 32f

    private var score = 0
    private var lives = 3
    private var level = 1
    private var best = 0
    private val maxLevel = 8
    private val baseSpeed = 240f
    private val powerChance = 0.20f

    private enum class State { READY, PLAYING, BETWEEN, GAMEOVER }
    private var state = State.READY

    private var melody: com.pixelclassics.app.audio.MelodyPlayer? = null

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        melody = com.pixelclassics.app.audio.MelodyPlayer(com.pixelclassics.app.audio.Melodies.Arcade, bpm = 132f)
        if (ctx.settings.musicEnabled) melody?.start()
        restart()
    }

    override fun dispose() { melody?.stop(); melody = null }
    override fun onPause(ctx: GameContext) { melody?.stop() }
    override fun onResume(ctx: GameContext) { if (ctx.settings.musicEnabled) melody?.start() }

    private fun restart() {
        score = 0; lives = 3; level = 1
        paddleW = paddleBaseW; paddleX = W / 2f
        wideUntil = 0f; gunUntil = 0f; slowUntil = 0f; fastUntil = 0f; catchUntil = 0f
        powerups.clear(); bullets.clear()
        buildLevel(); resetBalls(); state = State.READY
    }

    private fun resetBalls() {
        balls.clear()
        balls += Ball(paddleX, paddleY() - 8f, 0f, 0f, 7f, onPaddle = true)
    }

    private fun paddleY() = H - 30f

    private fun ballSpeed(): Float {
        var s = baseSpeed + (level - 1) * 18f
        val cycle = (level - 1) / maxLevel
        s += cycle * 60f
        if (time < slowUntil) s *= 0.5f
        return s
    }

    private fun buildLevel() {
        powerups.clear(); bullets.clear()
        val cols = 12
        val bw = (W - 20f) / cols
        val bh = 18f
        val topY = hudH + 12f
        bricks.clear()

        fun add(r: Int, c: Int, hits: Int, col: Color? = null) {
            if (c < 0 || c >= cols || r < 0 || r >= 12) return
            val color = col ?: brickColors[r % brickColors.size]
            val unbreak = hits >= 99
            bricks += Brick(10f + c * bw, topY + r * (bh + 3f), bw - 3f, bh, color, hits, hits, unbreakable = unbreak)
        }

        if (level <= maxLevel) {
            buildNamedLevel(level, cols, ::add)
            return
        }
        // Procedural levels from #9 onwards — seeded by level so the same
        // number always reproduces the same board. Mix of 8 layout shapes,
        // increasing silver-brick share and hit-count with progression.
        val rng = kotlin.random.Random(level.toLong() * 7919L + 31337L)
        val shapeIdx = rng.nextInt(8)
        val intensity = ((level - maxLevel) / 6f).coerceAtMost(1.5f)
        val silverRate = (0.04f + 0.02f * intensity).coerceAtMost(0.20f)
        val toughRate = (0.30f + 0.06f * intensity).coerceAtMost(0.70f)
        when (shapeIdx) {
            0 -> shapeMirror(cols, rng, toughRate, silverRate, ::add)
            1 -> shapePyramid(cols, rng, toughRate, silverRate, ::add)
            2 -> shapeScatter(cols, rng, toughRate, silverRate, ::add)
            3 -> shapeFortress(cols, rng, toughRate, silverRate, ::add)
            4 -> shapeZigzag(cols, rng, toughRate, silverRate, ::add)
            5 -> shapeDiagonal(cols, rng, toughRate, silverRate, ::add)
            6 -> shapeRings(cols, rng, toughRate, silverRate, ::add)
            else -> shapeChaos(cols, rng, toughRate, silverRate, ::add)
        }
    }

    private fun buildNamedLevel(lv: Int, cols: Int, add: (Int, Int, Int, Color?) -> Unit) {
        val hasSilver = lv >= 3
        when (lv) {
            1 -> for (r in 0 until 4) for (c in 0 until cols) add(r, c, if (r < 1) 2 else 1, null)
            2 -> for (r in 0 until 6) for (c in 0 until cols) if ((r + c) % 2 == 0) add(r, c, if (r < 2) 2 else 1, null)
            3 -> {
                val mid = cols / 2
                for (r in 0..6) {
                    val half = if (r < 4) r else 6 - r
                    for (c in mid - half..mid + half) add(r, c, if (r == 0 || r == 6) 2 else 1, null)
                }
                if (hasSilver) add(3, mid, 99, silver)
            }
            4 -> {
                for (r in 0 until 8) for (c in 0 until cols)
                    if (r == 0 || r == 7 || c == 0 || c == cols - 1)
                        add(r, c, if (r == 0 || r == 7) 2 else 1, null)
                if (hasSilver) for ((r, c) in listOf(0 to 0, 0 to cols - 1, 7 to 0, 7 to cols - 1)) add(r, c, 99, silver)
            }
            5 -> {
                for (r in 0 until 8) {
                    val off = if (r % 2 == 0) 0 else 2
                    var c = off
                    while (c < cols) {
                        add(r, c, if (r < 3) 2 else 1, null)
                        if (c + 1 < cols) add(r, c + 1, 1, null)
                        c += 4
                    }
                }
                if (hasSilver) { var c = 0; while (c < cols) { add(4, c, 99, silver); c += 5 } }
            }
            6 -> {
                val mid = cols / 2
                for (r in 0..6) {
                    val half = if (r < 4) 3 - r else r - 3
                    val spread = cols / 2 - half
                    for (c in mid - spread..mid + spread) if (c in 0 until cols) add(r, c, if (r < 2 || r > 5) 2 else 1, null)
                }
                if (hasSilver) { add(0, mid, 99, silver); add(6, mid - 2, 99, silver); add(6, mid + 2, 99, silver) }
            }
            7 -> for (r in 0 until 10) for (c in 0 until cols) {
                val rnd = Random.nextFloat()
                if (rnd < 0.6f) add(r, c, if (Random.nextFloat() < 0.35f) 2 else 1, null)
                else if (hasSilver && rnd < 0.68f) add(r, c, 99, silver)
            }
            8 -> {
                for (r in 0 until 8) for (c in 0 until cols) add(r, c, 2, null)
                var c = 0; while (c < cols) { add(0, c, 99, silver); c += 3 }
            }
        }
    }

    private fun hits(rng: kotlin.random.Random, toughRate: Float): Int =
        if (rng.nextFloat() < toughRate) 2 else 1

    private fun shapeMirror(cols: Int, rng: kotlin.random.Random, t: Float, s: Float, add: (Int, Int, Int, Color?) -> Unit) {
        val rows = 6 + rng.nextInt(3)
        val half = cols / 2
        for (r in 0 until rows) for (c in 0 until half) {
            if (rng.nextFloat() < 0.7f) {
                val h = if (rng.nextFloat() < s) 99 else hits(rng, t)
                val col = if (h >= 99) silver else null
                add(r, c, h, col)
                add(r, cols - 1 - c, h, col)
            }
        }
    }
    private fun shapePyramid(cols: Int, rng: kotlin.random.Random, t: Float, s: Float, add: (Int, Int, Int, Color?) -> Unit) {
        val mid = cols / 2
        val rows = 7 + rng.nextInt(2)
        for (r in 0 until rows) {
            val half = if (r <= rows / 2) r else rows - 1 - r
            for (c in mid - half..mid + half) {
                val h = if (rng.nextFloat() < s) 99 else hits(rng, t)
                add(r, c, h, if (h >= 99) silver else null)
            }
        }
    }
    private fun shapeScatter(cols: Int, rng: kotlin.random.Random, t: Float, s: Float, add: (Int, Int, Int, Color?) -> Unit) {
        val rows = 9
        for (r in 0 until rows) for (c in 0 until cols) {
            val roll = rng.nextFloat()
            if (roll < 0.45f) {
                val h = if (rng.nextFloat() < s) 99 else hits(rng, t)
                add(r, c, h, if (h >= 99) silver else null)
            }
        }
    }
    private fun shapeFortress(cols: Int, rng: kotlin.random.Random, t: Float, s: Float, add: (Int, Int, Int, Color?) -> Unit) {
        val rows = 8
        val mid = cols / 2
        for (r in 0 until rows) for (c in 0 until cols) {
            val onEdge = r == 0 || r == rows - 1 || c == 0 || c == cols - 1
            // Real entry gap: the bottom wall skips three middle columns, so
            // the ball can actually get inside and the level stays winnable.
            val inGap = r == rows - 1 && c in (mid - 1)..(mid + 1)
            if (onEdge && !inGap) {
                add(r, c, 99, silver)   // unbreakable wall
            } else if (!onEdge && rng.nextFloat() < 0.55f) {
                add(r, c, hits(rng, t), null)
            }
        }
    }
    private fun shapeZigzag(cols: Int, rng: kotlin.random.Random, t: Float, s: Float, add: (Int, Int, Int, Color?) -> Unit) {
        val rows = 8
        for (r in 0 until rows) {
            val phase = r % 2
            var c = phase
            while (c < cols) {
                val h = if (rng.nextFloat() < s) 99 else hits(rng, t)
                add(r, c, h, if (h >= 99) silver else null)
                c += 2
            }
        }
    }
    private fun shapeDiagonal(cols: Int, rng: kotlin.random.Random, t: Float, s: Float, add: (Int, Int, Int, Color?) -> Unit) {
        val rows = 9
        for (r in 0 until rows) for (c in 0 until cols) {
            if ((r + c) % 3 == 0) {
                val h = if (rng.nextFloat() < s) 99 else hits(rng, t)
                add(r, c, h, if (h >= 99) silver else null)
            }
        }
    }
    private fun shapeRings(cols: Int, rng: kotlin.random.Random, t: Float, s: Float, add: (Int, Int, Int, Color?) -> Unit) {
        val rows = 9
        val mid = cols / 2
        for (r in 0 until rows) for (c in 0 until cols) {
            val ring = maxOf(kotlin.math.abs(r - rows / 2), kotlin.math.abs(c - mid))
            if (ring % 2 == 0 && rng.nextFloat() < 0.85f) {
                val h = if (rng.nextFloat() < s) 99 else hits(rng, t)
                add(r, c, h, if (h >= 99) silver else null)
            }
        }
    }
    private fun shapeChaos(cols: Int, rng: kotlin.random.Random, t: Float, s: Float, add: (Int, Int, Int, Color?) -> Unit) {
        val rows = 10
        for (r in 0 until rows) for (c in 0 until cols) {
            val roll = rng.nextFloat()
            when {
                roll < 0.55f -> {
                    val h = if (rng.nextFloat() < s) 99 else hits(rng, t)
                    add(r, c, h, if (h >= 99) silver else null)
                }
            }
        }
    }

    private fun maybePower(b: Brick) {
        if (Random.nextFloat() > powerChance) return
        val pick = listOf(
            'W' to Color(0xFF2196F3), 'G' to Color(0xFFF44336), 'M' to Color(0xFF4CAF50),
            'S' to Color(0xFFFF9800), 'L' to Color(0xFFE91E90), 'F' to Color(0xFF00E5FF),
            'C' to Color(0xFFCDDC39),
        ).random()
        powerups += PowerUp(b.x + b.w / 2f, b.y + b.h, pick.first, pick.second)
    }

    private fun applyPower(pu: PowerUp, ctx: GameContext) {
        ctx.sound.powerUp()
        when (pu.kind) {
            'W' -> { wideUntil = time + 15f; paddleW = paddleBaseW * 2f }
            'G' -> { gunUntil = time + 10f }
            'M' -> {
                val newOnes = mutableListOf<Ball>()
                for (b in balls) {
                    if (b.onPaddle) continue
                    val spd = sqrt(b.vx * b.vx + b.vy * b.vy)
                    for (a in intArrayOf(-1, 1)) {
                        val ang = atan2(b.vy, b.vx) + a * 0.4f
                        newOnes += Ball(b.x, b.y, cos(ang) * spd, sin(ang) * spd, b.size, false)
                    }
                }
                balls.addAll(newOnes)
            }
            'S' -> {
                slowUntil = time + 10f
                for (b in balls) if (!b.onPaddle) { b.vx *= 0.5f; b.vy *= 0.5f }
            }
            'L' -> lives++
            'F' -> fastUntil = time + 15f   // faster paddle
            'C' -> catchUntil = time + 20f  // ball sticks to the paddle until ● pressed
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        if (state == State.GAMEOVER) return
        time += dt
        // Side D-pad steering (glide while held; auto-stop shortly after release).
        val paddleSpeed = 380f * (if (time < fastUntil) 1.7f else 1f)
        if (steerDir != 0 && time - steerFreshT < 0.16f) paddleX += steerDir * paddleSpeed * dt
        else steerDir = 0
        // Expire powerups.
        if (wideUntil > 0f && time > wideUntil) { wideUntil = 0f; paddleW = paddleBaseW }
        if (gunUntil > 0f && time > gunUntil) gunUntil = 0f
        if (fastUntil > 0f && time > fastUntil) fastUntil = 0f
        if (slowUntil > 0f && time > slowUntil) {
            slowUntil = 0f
            for (b in balls) if (!b.onPaddle) {
                val spd = sqrt(b.vx * b.vx + b.vy * b.vy); val tgt = ballSpeed()
                if (spd > 0f) { val r = tgt / spd; b.vx *= r; b.vy *= r }
            }
        }
        val hw = paddleW / 2f
        paddleX = paddleX.coerceIn(hw, W - hw)
        val py = paddleY()

        val it = balls.iterator()
        val dead = mutableListOf<Ball>()
        while (it.hasNext()) {
            val b = it.next()
            if (b.onPaddle) { b.x = paddleX; b.y = py - b.size; continue }
            val cur = sqrt(b.vx * b.vx + b.vy * b.vy)
            val tgt = ballSpeed()
            if (cur > 0f && abs(cur - tgt) > 20f) { val r = tgt / cur; b.vx *= r; b.vy *= r }
            b.x += b.vx * dt
            b.y += b.vy * dt
            if (b.x - b.size < 0f) { b.x = b.size; b.vx = abs(b.vx); ctx.sound.wallHit() }
            if (b.x + b.size > W) { b.x = W - b.size; b.vx = -abs(b.vx); ctx.sound.wallHit() }
            if (b.y - b.size < hudH) { b.y = hudH + b.size; b.vy = abs(b.vy); ctx.sound.wallHit() }
            if (b.y - b.size > H) { dead += b; continue }
            // Paddle collision.
            if (b.vy > 0f && b.y + b.size >= py && b.y + b.size <= py + paddleH + 4f &&
                b.x >= paddleX - hw - 2f && b.x <= paddleX + hw + 2f) {
                b.y = py - b.size
                if (time < catchUntil) {
                    // Sticky paddle: hold the ball until the red button launches it.
                    b.onPaddle = true; b.vx = 0f; b.vy = 0f
                    ctx.sound.paddleBounce()
                    continue
                }
                val hitPos = ((b.x - paddleX) / hw).coerceIn(-1f, 1f)
                val ang = hitPos * (PI.toFloat() * 0.38f) - PI.toFloat() / 2f
                val sp = ballSpeed()
                b.vx = cos(ang) * sp; b.vy = sin(ang) * sp
                if (b.vy > -0.5f) b.vy = -sp * 0.7f
                ctx.sound.paddleBounce()
            }
            // Brick collision.
            for (br in bricks) {
                if (!br.alive) continue
                if (b.x + b.size > br.x && b.x - b.size < br.x + br.w &&
                    b.y + b.size > br.y && b.y - b.size < br.y + br.h) {
                    if (!br.unbreakable) {
                        br.hits--
                        if (br.hits <= 0) {
                            br.alive = false
                            score += 10 * br.maxHits
                            ctx.sound.brickBreak()
                            maybePower(br)
                        } else { score += 5; ctx.sound.brickHit() }
                    } else ctx.sound.brickHit()
                    val ovL = b.x + b.size - br.x
                    val ovR = br.x + br.w - (b.x - b.size)
                    val ovT = b.y + b.size - br.y
                    val ovB = br.y + br.h - (b.y - b.size)
                    if (min(ovL, ovR) < min(ovT, ovB)) b.vx = -b.vx else b.vy = -b.vy
                    break
                }
            }
        }
        balls.removeAll(dead)
        if (balls.isEmpty()) {
            lives--
            ctx.sound.loseLife()
            if (lives <= 0) {
                state = State.GAMEOVER
                ctx.sound.gameOver()
                if (score > best) { best = score; ctx.scores.set(id, score) }
            } else {
                state = State.BETWEEN
                wideUntil = 0f; gunUntil = 0f; slowUntil = 0f; fastUntil = 0f; catchUntil = 0f; paddleW = paddleBaseW
                resetBalls()
            }
            return
        }
        // Powerups falling.
        val puIt = powerups.iterator()
        while (puIt.hasNext()) {
            val pu = puIt.next()
            pu.y += 150f * dt
            if (pu.y + 8f >= py && pu.y - 8f <= py + paddleH &&
                pu.x + 14f >= paddleX - hw && pu.x - 14f <= paddleX + hw) {
                applyPower(pu, ctx); puIt.remove()
            } else if (pu.y > H) puIt.remove()
        }
        // Bullets.
        val blIt = bullets.iterator()
        while (blIt.hasNext()) {
            val bl = blIt.next()
            bl.y -= 400f * dt
            if (bl.y < hudH) { blIt.remove(); continue }
            var hit = false
            for (br in bricks) {
                if (!br.alive) continue
                if (bl.x >= br.x && bl.x <= br.x + br.w &&
                    bl.y >= br.y && bl.y <= br.y + br.h) {
                    if (!br.unbreakable) {
                        br.hits--
                        if (br.hits <= 0) {
                            br.alive = false
                            score += 10 * br.maxHits
                            ctx.sound.brickBreak()
                            maybePower(br)
                        } else { score += 5; ctx.sound.brickHit() }
                    } else ctx.sound.brickHit()
                    hit = true; break
                }
            }
            if (hit) blIt.remove()
        }
        // Level complete?
        if (bricks.all { !it.alive || it.unbreakable }) {
            level++; ctx.sound.levelUp()
            state = State.BETWEEN
            wideUntil = 0f; gunUntil = 0f; slowUntil = 0f; fastUntil = 0f; catchUntil = 0f; paddleW = paddleBaseW
            buildLevel(); resetBalls()
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Deep space background.
        drawRect(backgroundColor, topLeft = Offset.Zero, size = Size(W, H))
        // Star field — deterministic so it doesn't flicker.
        val starSeed = 31
        for (i in 0 until 80) {
            val sx = ((i * 173 + starSeed) % W.toInt()).toFloat()
            val sy = (((i * 131 + 17) % (H - 100f).toInt())).toFloat() + hudH + 4f
            val twinkle = ((time * 1.5f + i * 0.7f) % 1f) > 0.55f
            drawRect(if (twinkle) Color.White else Color(0xFFAAAACC), topLeft = Offset(sx, sy), size = Size(1f, 1f))
        }
        // Chunky neon side borders.
        drawRect(Color(0xFF24247A), topLeft = Offset(0f, 0f), size = Size(6f, H))
        drawRect(Color(0xFF6060D8), topLeft = Offset(0f, 0f), size = Size(2f, H))
        drawRect(Color(0xFF24247A), topLeft = Offset(W - 6f, 0f), size = Size(6f, H))
        drawRect(Color(0xFF6060D8), topLeft = Offset(W - 2f, 0f), size = Size(2f, H))
        drawRect(Color(0xFF24247A), topLeft = Offset(0f, hudH), size = Size(W, 6f))
        drawRect(Color(0xFF6060D8), topLeft = Offset(0f, hudH), size = Size(W, 2f))

        // Bricks — beveled with darker shadow on the right-bottom.
        for (br in bricks) {
            if (!br.alive) continue
            // Outer dark border.
            drawRect(Color.Black, topLeft = Offset(br.x, br.y), size = Size(br.w, br.h))
            val inset = 1f
            drawRect(br.color, topLeft = Offset(br.x + inset, br.y + inset), size = Size(br.w - 2 * inset, br.h - 2 * inset))
            // Damage darken.
            if (br.maxHits > 1 && br.hits < br.maxHits) {
                drawRect(Color(0x50000000), topLeft = Offset(br.x + inset, br.y + inset), size = Size(br.w - 2 * inset, br.h - 2 * inset))
            }
            // Highlight top-left, shadow bottom-right.
            drawRect(Color(0x99FFFFFF), topLeft = Offset(br.x + inset, br.y + inset), size = Size(br.w - 2 * inset, 2f))
            drawRect(Color(0x99FFFFFF), topLeft = Offset(br.x + inset, br.y + inset), size = Size(2f, br.h - 2 * inset))
            drawRect(Color(0x99000000), topLeft = Offset(br.x + inset, br.y + br.h - inset - 2f), size = Size(br.w - 2 * inset, 2f))
            drawRect(Color(0x99000000), topLeft = Offset(br.x + br.w - inset - 2f, br.y + inset), size = Size(2f, br.h - 2 * inset))
            // Silver shimmer on unbreakable.
            if (br.unbreakable) {
                val phase = ((time * 2f + br.x * 0.1f) % 1f)
                drawRect(Color.White.copy(alpha = 0.3f * (1f - phase)), topLeft = Offset(br.x + 2f, br.y + 2f), size = Size((br.w - 4f) * phase, 1f))
            }
        }

        // Powerups — chunky capsule.
        for (pu in powerups) {
            drawRect(Color.Black, topLeft = Offset(pu.x - 15f, pu.y - 8f), size = Size(30f, 16f))
            drawRect(pu.color, topLeft = Offset(pu.x - 14f, pu.y - 7f), size = Size(28f, 14f))
            drawRect(Color.White.copy(alpha = 0.4f), topLeft = Offset(pu.x - 14f, pu.y - 7f), size = Size(28f, 2f))
            drawRect(Color.Black.copy(alpha = 0.4f), topLeft = Offset(pu.x - 14f, pu.y + 5f), size = Size(28f, 2f))
            drawDosText(pu.kind.toString(), x = pu.x, y = pu.y + 4f, color = Color.White, size = 14f, bold = true, align = TextAlign.CENTER)
        }

        // Bullets.
        for (bl in bullets) {
            drawRect(Color(0xFFFFFF80), topLeft = Offset(bl.x - 2f, bl.y - 8f), size = Size(4f, 10f))
            drawRect(Color(0xFFFF4444), topLeft = Offset(bl.x - 1f, bl.y - 8f), size = Size(2f, 10f))
        }

        // Vaus paddle — chunky 3-segment battle-tank look.
        val hw = paddleW / 2f
        val py = paddleY()
        drawRect(Color.Black, topLeft = Offset(paddleX - hw - 1f, py - 1f), size = Size(paddleW + 2f, paddleH + 2f))
        drawRect(Color(0xFF6080B0), topLeft = Offset(paddleX - hw, py), size = Size(paddleW, paddleH))
        drawRect(Color(0xFFD0D8FF), topLeft = Offset(paddleX - hw, py), size = Size(paddleW, 3f))
        drawRect(Color(0xFF203868), topLeft = Offset(paddleX - hw, py + paddleH - 3f), size = Size(paddleW, 3f))
        // Three vaus-style emitters in the centre.
        val emit = Color(0xFFFFC040)
        drawRect(emit, topLeft = Offset(paddleX - hw + paddleW * 0.3f, py + 2f), size = Size(4f, paddleH - 4f))
        drawRect(emit, topLeft = Offset(paddleX - hw + paddleW * 0.5f - 2f, py + 2f), size = Size(4f, paddleH - 4f))
        drawRect(emit, topLeft = Offset(paddleX - hw + paddleW * 0.7f - 4f, py + 2f), size = Size(4f, paddleH - 4f))
        if (gunUntil > time) {
            drawRect(Color.Black, topLeft = Offset(paddleX - hw + 1f, py - 7f), size = Size(7f, 9f))
            drawRect(Color(0xFFF44336), topLeft = Offset(paddleX - hw + 2f, py - 6f), size = Size(5f, 8f))
            drawRect(Color.Black, topLeft = Offset(paddleX + hw - 8f, py - 7f), size = Size(7f, 9f))
            drawRect(Color(0xFFF44336), topLeft = Offset(paddleX + hw - 7f, py - 6f), size = Size(5f, 8f))
        }

        // Balls — chunky white square with red core.
        for (b in balls) {
            drawRect(Color.Black, topLeft = Offset(b.x - b.size - 1f, b.y - b.size - 1f), size = Size((b.size + 1f) * 2f, (b.size + 1f) * 2f))
            drawRect(Color.White, topLeft = Offset(b.x - b.size, b.y - b.size), size = Size(b.size * 2f, b.size * 2f))
            drawRect(Color(0xFFFFC040), topLeft = Offset(b.x - b.size + 1f, b.y - b.size + 1f), size = Size(3f, 3f))
        }

        // HUD.
        drawDosText("SCORE  " + score.toString().padStart(6, '0'), x = 10f, y = 20f, color = Color(0xFF4FC3F7), size = 14f, bold = true)
        drawDosText("LEVEL  $level", x = W / 2f, y = 20f, color = Color(0xFF4FC3F7), size = 14f, bold = true, align = TextAlign.CENTER)
        for (i in 0 until lives) drawRect(Color.White, topLeft = Offset(W - 16f - i * 16f, 12f), size = Size(10f, 10f))
        drawDosText("BEST $best", x = W - 80f, y = 20f, color = Color(0xFFFFD600), size = 11f, bold = true, align = TextAlign.RIGHT)
        // Music mute toggle (tap it).
        drawDosText(if (ctx.settings.musicEnabled) "♪ON" else "♪OFF", x = 168f, y = 20f,
            color = if (ctx.settings.musicEnabled) Color(0xFF66FF66) else Color(0xFF888888), size = 11f, bold = true)

        // Active power overlays — bigger badges with countdown bar.
        var bx = 10f; val by2 = 32f
        fun powBadge(label: String, color: Color, remain: Float, max: Float) {
            drawRect(Color.Black, topLeft = Offset(bx - 2f, by2 - 14f), size = Size(48f, 22f))
            drawRect(color, topLeft = Offset(bx, by2 - 12f), size = Size(44f, 18f))
            drawRect(Color.Black, topLeft = Offset(bx, by2 + 4f), size = Size(44f, 4f))
            drawRect(Color(0xFFFFEE40), topLeft = Offset(bx, by2 + 4f), size = Size(44f * (remain / max).coerceIn(0f, 1f), 4f))
            drawDosText(label, x = bx + 22f, y = by2 + 2f, color = Color.White, size = 12f, bold = true, align = TextAlign.CENTER)
            bx += 54f
        }
        if (wideUntil > time) powBadge("WIDE", Color(0xFF2196F3), wideUntil - time, 15f)
        if (gunUntil > time) powBadge("GUN", Color(0xFFF44336), gunUntil - time, 10f)
        if (slowUntil > time) powBadge("SLOW", Color(0xFFFF9800), slowUntil - time, 10f)
        if (fastUntil > time) powBadge("FAST", Color(0xFF00E5FF), fastUntil - time, 15f)
        if (catchUntil > time) powBadge("HOLD", Color(0xFFCDDC39), catchUntil - time, 20f)

        // Ready hint.
        if ((state == State.READY || state == State.BETWEEN) && balls.any { it.onPaddle }) {
            drawDosText(ctx.tr("PRESS ● TO LAUNCH", "● — ЗАПУСК ШАРА"), x = W / 2f, y = H * 0.55f, color = Color(0x99FFFFFF), size = 18f, align = TextAlign.CENTER)
        }

        if (state == State.GAMEOVER) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText("GAME OVER", x = W / 2f, y = H / 2f - 20f, color = Color(0xFFFF1744), size = 40f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Score $score · Level $level", "Счёт $score · Уровень $level"), x = W / 2f, y = H / 2f + 20f, color = Color(0xFF4FC3F7), size = 18f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("PRESS ● TO RESTART", "● — РЕСТАРТ"), x = W / 2f, y = H / 2f + 50f, color = Color(0xFFCCCCCC), size = 14f, align = TextAlign.CENTER)
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        // The only canvas tap target is the music-mute glyph in the HUD.
        // Paddle = joystick; launch / shoot / restart = the red FIRE button.
        if (x in 160f..212f && y in 6f..30f) {
            val on = !ctx.settings.musicEnabled
            ctx.settings.musicEnabled = on
            if (on) melody?.start() else melody?.stop()
            ctx.sound.click()
        }
    }

    private fun launch() {
        val stuck = balls.filter { it.onPaddle }
        if (stuck.isEmpty()) return
        for (b in stuck) {
            b.onPaddle = false
            val ang = -PI.toFloat() / 2f + (Random.nextFloat() - 0.5f) * 0.5f
            val sp = ballSpeed()
            b.vx = cos(ang) * sp; b.vy = sin(ang) * sp
        }
        state = State.PLAYING
    }

    private fun shoot(ctx: GameContext) {
        val hw = paddleW / 2f
        val py = paddleY()
        bullets += Bullet(paddleX - hw + 4f, py - 4f)
        bullets += Bullet(paddleX + hw - 4f, py - 4f)
        ctx.sound.shoot()
    }
}
