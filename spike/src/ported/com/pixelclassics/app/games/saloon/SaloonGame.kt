package com.pixelclassics.app.games.saloon

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
import kotlin.math.abs
import kotlin.random.Random

/**
 * SALOON — bartender game inspired by Tapper (Bally Midway, 1983).
 * Four horizontal bars. Patrons walk in from the right; the bartender on
 * the left pulls the tap to slide a mug toward them. The mug returns empty
 * and is caught automatically by the bartender at the same bar.
 *
 * Lose a life when:
 *   - a mug slides off the LEFT edge uncaught
 *   - a patron reaches the bar's LEFT edge (no drink in time)
 *
 * Controls (DPAD_AND_FIRE):
 *   ▲▼ — move between bars       ● FIRE — pull the tap (slide a mug)
 */
class SaloonGame : Game {
    override val id: String = "saloon"
    override val titleResId: Int = R.string.game_saloon
    override val backgroundColor: Color = Color(0xFF000000)
    override val controls: GameControls = GameControls.DPAD_AND_FIRE
    override val introText: String = SALOON_INTRO_EN
    override fun introTextFor(lang: String): String = pickSaloonIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.BAR_WOOD
    override val helpLines: List<String> = listOf(
        "▲▼ — switch bar  ·  ● FIRE — pull the tap",
        "Slide mugs along the bar to thirsty patrons",
        "Catch the empty mug coming back.  Don't let one fall off the bar.",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "▲▼ — смена стойки  ·  ● ОГОНЬ — налить из крана",
        "Пускай кружки по стойке жаждущим посетителям",
        "Лови пустую кружку на обратном пути.  Не роняй ни одной со стойки.",
    ) else helpLines

    private val W = 720f
    private val H = 540f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    private val bars = 4
    private val barTop = 110f
    private val barGap = 100f
    private val barH = 60f
    private val leftX = 50f                  // bartender column
    private val rightX = W - 50f             // patron entry
    private val tapX = 90f                   // where mugs spawn from the tap
    private val counterX = 140f              // patron threshold — beyond this they pinch you

    private data class Mug(var x: Float, var bar: Int, var dir: Int)   // dir 1 = sliding right, -1 = returning
    private data class Patron(var x: Float, var bar: Int)

    private val mugs = mutableListOf<Mug>()
    private val patrons = mutableListOf<Patron>()

    private enum class Phase { TITLE, PLAY, OVER }
    private var phase = Phase.TITLE
    private var bartender = 0                // current bar index
    private var score = 0
    private var best = 0
    private var lives = 3
    private var level = 1
    private var time = 0f
    private var spawnAcc = 0f

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
    }

    private fun restart() {
        mugs.clear(); patrons.clear()
        bartender = 0; score = 0; lives = 3; level = 1
        spawnAcc = 0f; time = 0f
        phase = Phase.PLAY
        // Two starting patrons on different bars so it doesn't feel empty.
        patrons += Patron(rightX - 40f, 0)
        patrons += Patron(rightX - 80f, 2)
    }

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (phase != Phase.PLAY) return
        if (dy != 0) bartender = (bartender + dy + bars) % bars
    }
    override fun onFire(ctx: GameContext) {
        when (phase) {
            Phase.TITLE -> restart()
            Phase.OVER -> restart()
            Phase.PLAY -> {
                // Pull the tap — spawn a mug sliding right on the bartender's bar.
                mugs += Mug(tapX, bartender, 1)
                ctx.sound.shoot()
            }
        }
    }
    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (phase != Phase.PLAY) restart()
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (phase != Phase.PLAY) return
        val mugSpeed = 220f + level * 12f
        val patronSpeed = 32f + level * 4f

        // Move mugs.
        val mIter = mugs.iterator()
        while (mIter.hasNext()) {
            val m = mIter.next()
            m.x += m.dir * mugSpeed * dt
            if (m.dir == 1) {
                // Sliding right — does it reach a patron?
                val target = patrons.filter { it.bar == m.bar }.minByOrNull { it.x }
                if (target != null && abs(m.x - target.x) < 18f && m.x >= target.x - 4f) {
                    // Served.
                    score += 50
                    patrons.remove(target)
                    m.x = target.x; m.dir = -1   // mug returns empty
                    ctx.sound.score()
                    continue
                }
                if (m.x > rightX) {
                    // Reached end of bar without a patron — game over (drink wasted).
                    loseLife(ctx); mIter.remove(); break
                }
            } else {
                // Returning — caught by bartender at the SAME bar.
                if (m.x <= tapX + 6f) {
                    if (m.bar == bartender) {
                        score += 15
                        ctx.sound.eat()
                    } else {
                        // Mug fell off the bar onto the floor.
                        loseLife(ctx); mIter.remove(); break
                    }
                    mIter.remove()
                }
            }
        }

        // Patrons advance toward the counter.
        val pIter = patrons.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.x -= patronSpeed * dt
            if (p.x <= counterX) {
                loseLife(ctx); pIter.remove(); break
            }
        }

        // Spawn fresh patrons every couple of seconds, more often at higher levels.
        spawnAcc += dt
        val spawnEvery = (2.2f - level * 0.12f).coerceAtLeast(0.7f)
        if (spawnAcc >= spawnEvery && patrons.size < 6 + level) {
            spawnAcc = 0f
            val bar = Random.nextInt(bars)
            patrons += Patron(rightX - 10f, bar)
        }

        // Level up every 500 points.
        val newLevel = score / 500 + 1
        if (newLevel > level) { level = newLevel; ctx.sound.levelUp() }
    }

    private fun loseLife(ctx: GameContext) {
        lives--
        ctx.sound.die()
        if (lives <= 0) {
            phase = Phase.OVER
            if (score > best) { best = score; ctx.scores.set(id, score) }
            ctx.sound.gameOver()
        }
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        // Dark wood-panel saloon backdrop.
        drawRect(Color(0xFF000000), topLeft = Offset.Zero, size = Size(W, H))
        // Decorative ceiling beams.
        drawRect(Color(0xFF555555), topLeft = Offset(0f, 0f), size = Size(W, 24f))
        drawDosText("S A L O O N", x = W / 2f, y = 18f, color = Color(0xFFFFFF55), size = 16f, bold = true, align = TextAlign.CENTER)

        // HUD bar.
        drawRect(Color.Black, topLeft = Offset(0f, 26f), size = Size(W, 36f))
        drawDosText("SCORE  $score", x = 16f, y = 52f, color = Color(0xFFFFFF55), size = 16f, bold = true)
        drawDosText("LV  $level", x = W / 2f, y = 52f, color = Color.White, size = 16f, bold = true, align = TextAlign.CENTER)
        drawDosText("♥ x $lives    BEST $best", x = W - 16f, y = 52f, color = Color(0xFFFF5555), size = 14f, bold = true, align = TextAlign.RIGHT)

        // Four bars — wooden counter with brass rail.
        for (b in 0 until bars) {
            val y = barTop + b * barGap
            drawRect(Color(0xFFAA5500), topLeft = Offset(0f, y), size = Size(W, barH))
            drawRect(Color(0xFFAA5500), topLeft = Offset(0f, y), size = Size(W, 5f))
            drawRect(Color(0xFFFFFF55), topLeft = Offset(0f, y + barH - 3f), size = Size(W, 3f))
            // Bar-end markers.
            drawDosText("#${b + 1}", x = 10f, y = y + barH / 2f + 6f, color = Color(0xFFFFFFFF), size = 18f, bold = true)
        }

        // Patrons (right side).
        for (p in patrons) {
            val y = barTop + p.bar * barGap + barH / 2f
            drawPatron(this, p.x, y)
        }
        // Mugs.
        for (m in mugs) {
            val y = barTop + m.bar * barGap + barH / 2f
            drawMug(this, m.x, y, full = m.dir == 1)
        }
        // Bartender — appears at the LEFT edge on the current bar.
        val by = barTop + bartender * barGap + barH / 2f
        drawBartender(this, leftX, by)
        // Tap indicator (small handle next to the bartender).
        drawRect(Color(0xFFFFFF55), topLeft = Offset(tapX - 4f, by - 22f), size = Size(8f, 12f))
        drawRect(Color(0xFFFFFF55), topLeft = Offset(tapX - 6f, by - 24f), size = Size(12f, 4f))

        if (phase == Phase.TITLE) drawTitle(this, ctx)
        if (phase == Phase.OVER) drawOver(this, ctx)
    }

    private fun drawTitle(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color.Black.copy(alpha = 0.78f), topLeft = Offset(0f, 0f), size = Size(W, H))
        drawDosText("S A L O O N", x = W / 2f, y = H * 0.30f, color = Color(0xFFFFFF55), size = 44f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("After Bally Midway's Tapper, 1983", "По мотивам Tapper (Bally Midway, 1983)"), x = W / 2f, y = H * 0.38f, color = Color(0xFFFFFF55), size = 13f, bold = true, align = TextAlign.CENTER)
        drawDosText("▲▼ — switch bar.  SPACE — pull the tap.", x = W / 2f, y = H * 0.55f, color = Color.White, size = 14f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Serve every patron.  Catch every empty mug.", "Обслужи всех.  Поймай каждую пустую кружку."), x = W / 2f, y = H * 0.62f, color = Color.White, size = 14f, align = TextAlign.CENTER)
        if ((time * 2f).toInt() % 2 == 0)
            drawDosText("● FIRE — START", x = W / 2f, y = H * 0.82f, color = Color(0xFFFFFF55), size = 18f, bold = true, align = TextAlign.CENTER)
    }
    private fun drawOver(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color.Black.copy(alpha = 0.82f), topLeft = Offset(0f, 0f), size = Size(W, H))
        drawDosText(ctx.tr("LAST CALL", "ПОСЛЕДНИЙ ЗАКАЗ"), x = W / 2f, y = H * 0.42f, color = Color(0xFFFF5555), size = 38f, bold = true, align = TextAlign.CENTER)
        drawDosText("SCORE  $score", x = W / 2f, y = H * 0.52f, color = Color.White, size = 18f, bold = true, align = TextAlign.CENTER)
        drawDosText("BEST   $best", x = W / 2f, y = H * 0.58f, color = Color(0xFFFFFF55), size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText("● FIRE — TRY AGAIN", x = W / 2f, y = H * 0.76f, color = Color.White, size = 16f, bold = true, align = TextAlign.CENTER)
    }

    private fun drawBartender(scope: DrawScope, cx: Float, cy: Float) = with(scope) {
        // Period-correct 1880s pixel bartender — bowler hat, walrus
        // moustache, bow tie, embroidered waistcoat, sleeve garter, towel
        // over the shoulder. Юджин 18.06: «бармен — сильно доработать».
        // Chunky 1983 arcade scale: readable silhouette first, tiny costume
        // details second. At 6px units the character read too much like a
        // modern detailed miniature; 5px units keeps the Tapper-era feel.
        val s = 5f
        val skin = Color(0xFFAAAAAA)
        val skinDark = Color(0xFFAAAAAA)
        val shadow = Color(0xFF000000)

        // Outline behind the head (gives the bowler a clean silhouette)
        drawRect(shadow, topLeft = Offset(cx - 3.2f * s, cy - 6.6f * s), size = Size(6.4f * s, 4.4f * s))
        // Bowler hat — dome + brim
        drawRect(Color(0xFF000000), topLeft = Offset(cx - 2.6f * s, cy - 6.4f * s), size = Size(5.2f * s, 1.6f * s))   // dome
        drawRect(Color(0xFF000000), topLeft = Offset(cx - 3.1f * s, cy - 4.8f * s), size = Size(6.2f * s, 0.6f * s))   // brim
        drawRect(Color(0xFFAA0000), topLeft = Offset(cx - 2.6f * s, cy - 5.2f * s), size = Size(5.2f * s, 0.4f * s))   // hatband
        // Face oval
        drawRect(skin, topLeft = Offset(cx - 2.5f * s, cy - 4.2f * s), size = Size(5f * s, 2.4f * s))
        drawRect(skinDark, topLeft = Offset(cx - 2.5f * s, cy - 1.8f * s), size = Size(5f * s, 0.4f * s))   // jawline
        // Eyebrows + eyes (dot eyes, slightly furrowed)
        drawRect(Color.Black, topLeft = Offset(cx - 1.7f * s, cy - 3.6f * s), size = Size(1.0f * s, 0.3f * s))
        drawRect(Color.Black, topLeft = Offset(cx + 0.7f * s, cy - 3.6f * s), size = Size(1.0f * s, 0.3f * s))
        drawCircle(Color.White, 0.4f * s, Offset(cx - 1.2f * s, cy - 3.1f * s))
        drawCircle(Color.White, 0.4f * s, Offset(cx + 1.2f * s, cy - 3.1f * s))
        drawCircle(Color.Black, 0.2f * s, Offset(cx - 1.2f * s, cy - 3.1f * s))
        drawCircle(Color.Black, 0.2f * s, Offset(cx + 1.2f * s, cy - 3.1f * s))
        // Nose
        drawRect(skinDark, topLeft = Offset(cx - 0.3f * s, cy - 2.8f * s), size = Size(0.6f * s, 0.8f * s))
        // Big handlebar moustache (the showpiece)
        drawRect(Color(0xFF000000), topLeft = Offset(cx - 2.6f * s, cy - 2.0f * s), size = Size(5.2f * s, 0.8f * s))   // bar
        drawRect(Color(0xFF000000), topLeft = Offset(cx - 3.0f * s, cy - 2.2f * s), size = Size(0.8f * s, 0.6f * s))   // left curl
        drawRect(Color(0xFF000000), topLeft = Offset(cx + 2.2f * s, cy - 2.2f * s), size = Size(0.8f * s, 0.6f * s))   // right curl
        drawRect(Color(0xFF000000), topLeft = Offset(cx - 3.4f * s, cy - 2.0f * s), size = Size(0.6f * s, 0.4f * s))   // tip
        drawRect(Color(0xFF000000), topLeft = Offset(cx + 2.8f * s, cy - 2.0f * s), size = Size(0.6f * s, 0.4f * s))
        // Mouth (tiny pink line under moustache)
        drawRect(Color(0xFFFF5555), topLeft = Offset(cx - 0.6f * s, cy - 1.0f * s), size = Size(1.2f * s, 0.2f * s))
        // Sideburns
        drawRect(Color(0xFF000000), topLeft = Offset(cx - 2.6f * s, cy - 3.4f * s), size = Size(0.3f * s, 1.6f * s))
        drawRect(Color(0xFF000000), topLeft = Offset(cx + 2.3f * s, cy - 3.4f * s), size = Size(0.3f * s, 1.6f * s))

        // Collar + bow tie (red)
        drawRect(Color.White, topLeft = Offset(cx - 2.0f * s, cy - 0.6f * s), size = Size(4.0f * s, 0.6f * s))
        drawRect(Color(0xFFAA0000), topLeft = Offset(cx - 0.8f * s, cy - 0.4f * s), size = Size(1.6f * s, 0.5f * s))
        drawRect(Color(0xFFAA0000), topLeft = Offset(cx - 0.3f * s, cy - 0.3f * s), size = Size(0.6f * s, 0.3f * s))   // bow knot

        // White dress shirt
        drawRect(Color.White, topLeft = Offset(cx - 3.2f * s, cy + 0.0f * s), size = Size(6.4f * s, 4.4f * s))
        drawRect(Color(0xFFAAAAAA), topLeft = Offset(cx - 3.2f * s, cy + 4.0f * s), size = Size(6.4f * s, 0.4f * s))   // hem shading
        // Embroidered waistcoat
        drawRect(Color(0xFF555555), topLeft = Offset(cx - 2.6f * s, cy + 0.4f * s), size = Size(5.2f * s, 3.6f * s))
        drawRect(Color(0xFF555555), topLeft = Offset(cx - 2.6f * s, cy + 0.4f * s), size = Size(5.2f * s, 0.3f * s))   // top trim
        drawRect(Color(0xFF555555), topLeft = Offset(cx - 2.6f * s, cy + 3.7f * s), size = Size(5.2f * s, 0.3f * s))   // bottom trim
        // Brocade pattern (diamonds)
        for (i in 0 until 3) {
            val yy = cy + (0.9f + i * 0.9f) * s
            drawRect(Color(0xFFAA5500), topLeft = Offset(cx - 1.4f * s, yy), size = Size(0.3f * s, 0.3f * s))
            drawRect(Color(0xFFAA5500), topLeft = Offset(cx + 1.1f * s, yy), size = Size(0.3f * s, 0.3f * s))
        }
        // Three brass buttons
        for (i in 0 until 3) drawCircle(Color(0xFFFFFF55), 0.25f * s, Offset(cx + 0.0f * s, cy + (0.9f + i * 1.0f) * s))
        // Watch chain (gold curve from button to pocket)
        drawRect(Color(0xFFFFFF55), topLeft = Offset(cx + 0.3f * s, cy + 1.1f * s), size = Size(1.0f * s, 0.15f * s))
        drawRect(Color(0xFFFFFF55), topLeft = Offset(cx + 1.2f * s, cy + 1.1f * s), size = Size(0.15f * s, 0.6f * s))

        // Sleeves (white shirt, garters above the elbow)
        drawRect(Color.White, topLeft = Offset(cx - 4.6f * s, cy + 0.2f * s), size = Size(1.4f * s, 3.0f * s))         // left sleeve
        drawRect(Color.White, topLeft = Offset(cx + 3.2f * s, cy + 0.2f * s), size = Size(1.4f * s, 3.0f * s))         // right sleeve
        drawRect(Color(0xFFAA0000), topLeft = Offset(cx - 4.6f * s, cy + 1.4f * s), size = Size(1.4f * s, 0.3f * s))   // garter L
        drawRect(Color(0xFFAA0000), topLeft = Offset(cx + 3.2f * s, cy + 1.4f * s), size = Size(1.4f * s, 0.3f * s))   // garter R
        // Hands (peach blocks)
        drawRect(skin, topLeft = Offset(cx - 4.7f * s, cy + 3.0f * s), size = Size(1.6f * s, 1.0f * s))
        drawRect(skin, topLeft = Offset(cx + 3.1f * s, cy + 3.0f * s), size = Size(1.6f * s, 1.0f * s))
        drawRect(skinDark, topLeft = Offset(cx - 4.7f * s, cy + 3.8f * s), size = Size(1.6f * s, 0.2f * s))
        drawRect(skinDark, topLeft = Offset(cx + 3.1f * s, cy + 3.8f * s), size = Size(1.6f * s, 0.2f * s))

        // Towel slung over the right shoulder
        drawRect(Color(0xFFFFFFFF), topLeft = Offset(cx + 1.6f * s, cy + 0.2f * s), size = Size(2.0f * s, 1.8f * s))
        drawRect(Color(0xFFAAAAAA), topLeft = Offset(cx + 1.6f * s, cy + 0.2f * s), size = Size(2.0f * s, 0.3f * s))
        // Apron tie below the waistcoat
        drawRect(Color(0xFFFFFF55), topLeft = Offset(cx - 3.0f * s, cy + 4.0f * s), size = Size(6.0f * s, 1.6f * s))
        drawRect(Color(0xFFAA5500), topLeft = Offset(cx - 3.0f * s, cy + 4.0f * s), size = Size(6.0f * s, 0.4f * s))
        // Apron pocket
        drawRect(Color(0xFFAA5500), topLeft = Offset(cx - 1.2f * s, cy + 4.6f * s), size = Size(2.4f * s, 0.8f * s))
    }
    private fun drawPatron(scope: DrawScope, cx: Float, cy: Float) = with(scope) {
        // A weary cowboy in a brown duster, mug-thirsty.
        val s = 5f
        drawRect(Color(0xFFAA5500), topLeft = Offset(cx - 3f * s, cy - 5f * s), size = Size(6f * s, 1.4f * s))    // hat
        drawRect(Color(0xFFFFFFFF), topLeft = Offset(cx - 2f * s, cy - 3.6f * s), size = Size(4f * s, 2f * s))   // face
        drawRect(Color(0xFFAA5500), topLeft = Offset(cx - 3f * s, cy - 1.6f * s), size = Size(6f * s, 5f * s))    // duster
        drawRect(Color(0xFFAAAAAA), topLeft = Offset(cx - 2.4f * s, cy + 0.4f * s), size = Size(4.8f * s, 0.8f * s))  // belt
    }
    private fun drawMug(scope: DrawScope, cx: Float, cy: Float, full: Boolean) = with(scope) {
        val w = 22f; val h = 18f
        drawRect(Color(0xFFFFFFFF), topLeft = Offset(cx - w / 2f, cy - h / 2f), size = Size(w, h))
        drawRect(Color(0xFFAA5500), topLeft = Offset(cx + w / 2f, cy - h / 4f), size = Size(4f, h / 2f))  // handle
        if (full) {
            drawRect(Color(0xFFFFFF55), topLeft = Offset(cx - w / 2f + 2f, cy - h / 2f + 3f), size = Size(w - 4f, h - 8f))   // beer
            drawRect(Color.White, topLeft = Offset(cx - w / 2f + 2f, cy - h / 2f + 2f), size = Size(w - 4f, 3f))             // foam
        }
    }
}
