package com.pixelclassics.app.games.rogue

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

/** ASCII rogue-lite — explore a 5-level dungeon, fight monsters, find the Amulet. */
class RogueGame : Game {
    override val id: String = "rogue"
    override val titleResId: Int = R.string.game_rogue
    override val backgroundColor: Color = Color.Black

    private val cols = 40
    private val rows = 22
    private val cell = 16f
    private val W = cols * cell
    private val H = rows * cell + 36f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H
    override val controls: GameControls = GameControls.DPAD
    override val introText: String = ROGUE_INTRO_EN
    override fun introTextFor(lang: String): String = pickRogueIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.ASCII_DOS

    override val helpLines: List<String> = listOf(
        "D-pad — move (one step = one turn for everyone)",
        "Walk INTO a monster to attack.  ! potion (+6HP)  $ gold  > stairs",
        "Five levels. Find the Amulet of Yendor (&). Die = restart.",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — шаг (один шаг = один ход для всех)",
        "Шагни В монстра — атака.  ! зелье (+6HP)  $ золото  > лестница",
        "Пять уровней. Найди Амулет Йендора (&). Погиб = заново.",
    ) else helpLines

    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        if (dx == 0 && dy == 0) return   // ignore D-pad release; a neutral must not pass a turn
        if (stage != Stage.PLAY) return  // no invisible moves while CHOOSE/HELP overlays are up
        move(dx, dy, ctx)
    }
    private val hudH = 36f

    private val WALL = '#'
    private val FLOOR = '.'
    private val DOOR = '+'
    private val POTION = '!'
    private val GOLD = '$'
    private val STAIRS = '>'
    private val AMULET = '&'

    private var map = Array(rows) { CharArray(cols) { ' ' } }
    private var px = 0
    private var py = 0
    private var hp = 20
    private var maxHp = 20
    private var atk = 4
    private var gold = 0
    private var level = 1
    private val maxLevel = 5
    private var won = false
    private var dead = false
    private var msg = ""
    private var lastCtx: GameContext? = null
    private fun t(en: String, ru: String) = lastCtx?.tr(en, ru) ?: en
    private val monsters = mutableListOf<Monster>()

    private data class Monster(var x: Int, var y: Int, var hp: Int, val sym: Char, val atk: Int, val maxHp: Int, val tag: String = "", val nickname: String = "")
    private val monsterNicks = listOf("Slick","Snarl","Grim","Hex","Vex","Mok","Rax","Zog","Krill","Brog","Thrak","Vorn","Skarn","Drek","Pyx")
    private fun symClass(s: Char): String = when (s) {
        'B' -> "Bat"; 'O' -> "Orc"; 'R' -> "Rat"; 'S' -> "Snake"; else -> "Thing"
    }
    private fun symId(m: Monster): String = m.tag.ifEmpty { "${m.sym}?" }

    /** Strict EGA text-mode colours per monster class — the 1984 DOS port
     *  painted every letter its own colour (Юджин: «перегнать в EGA»). */
    private fun egaMonster(s: Char): Color = when (s) {
        'B' -> Color(0xFFFF55FF)   // bat — bright magenta
        'O' -> Color(0xFF55FF55)   // orc — bright green
        'R' -> Color(0xFFFF5555)   // rat — bright red
        'S' -> Color(0xFF00AAAA)   // snake — dark cyan
        else -> Color(0xFFFF5555)
    }
    private fun monsterFullName(m: Monster): String = "${symClass(m.sym)} ${m.nickname} (${symId(m)})"

    private enum class Stage { CHOOSE, HELP, PLAY }
    private var stage = Stage.CHOOSE
    private var hasSave = false

    override fun init(ctx: GameContext) {
        lastCtx = ctx
        hasSave = ctx.settings.rogueSave.isNotEmpty()
        stage = if (hasSave) Stage.CHOOSE else Stage.HELP
        // Defaults — actual data is loaded on Continue, or regen on New.
        level = 1; hp = 20; maxHp = 20; atk = 4; gold = 0; dead = false; won = false; msg = ""
        if (!hasSave) gen()
    }

    override fun dispose() {
        // Auto-save iff alive and mid-quest.
        // We can't access ctx from dispose() — store last ctx ref.
        // Only from PLAY: exiting from the CHOOSE screen must not overwrite a
        // real save with the blank not-yet-loaded world.
        lastCtx?.let { ctx ->
            when {
                stage != Stage.PLAY -> Unit
                !dead && !won -> ctx.settings.rogueSave = serialize()
                else -> ctx.settings.rogueSave = ""
            }
        }
    }

    private fun serialize(): String {
        val sb = StringBuilder()
        sb.append("$level|$hp|$maxHp|$atk|$gold|$px|$py\n")
        for (r in 0 until rows) sb.append(map[r].concatToString()).append('\n')
        sb.append('\n')
        for (m in monsters) sb.append("${m.x},${m.y},${m.hp},${m.sym},${m.atk},${m.maxHp}\n")
        return sb.toString()
    }

    private fun loadFrom(raw: String): Boolean {
        return try {
            val lines = raw.lines()
            val head = lines[0].split('|')
            level = head[0].toInt(); hp = head[1].toInt(); maxHp = head[2].toInt()
            atk = head[3].toInt(); gold = head[4].toInt()
            px = head[5].toInt(); py = head[6].toInt()
            map = Array(rows) { CharArray(cols) { ' ' } }
            for (r in 0 until rows) {
                val ln = lines[1 + r]
                for (c in 0 until cols.coerceAtMost(ln.length)) map[r][c] = ln[c]
            }
            monsters.clear()
            for (i in 1 + rows + 1 until lines.size) {
                val ln = lines[i]
                if (ln.isBlank()) continue
                val p = ln.split(',')
                monsters += Monster(p[0].toInt(), p[1].toInt(), p[2].toInt(), p[3][0], p[4].toInt(), p[5].toInt())
            }
            // Range-validate: a truncated/merged save can parse fine but carry
            // coordinates outside the map — that crashes monstersTurn later.
            if (px !in 0 until cols || py !in 0 until rows) throw IllegalArgumentException("player OOB")
            if (monsters.any { it.x !in 0 until cols || it.y !in 0 until rows || it.hp <= 0 }) throw IllegalArgumentException("monster OOB")
            if (level !in 1..maxLevel || hp <= 0) throw IllegalArgumentException("bad stats")
            dead = false; won = false; msg = t("Resume — level $level.", "Продолжаем — уровень $level.")
            true
        } catch (_: Throwable) { false }
    }

    private fun gen() {
        map = Array(rows) { CharArray(cols) { ' ' } }
        monsters.clear()
        // Generate 6 rooms.
        val rooms = mutableListOf<IntArray>()    // [x, y, w, h]
        repeat(6) {
            for (attempt in 0 until 30) {
                val w = Random.nextInt(5, 9); val h = Random.nextInt(4, 7)
                val x = Random.nextInt(1, cols - w - 1); val y = Random.nextInt(1, rows - h - 1)
                val overlap = rooms.any { r -> x <= r[0] + r[2] && x + w >= r[0] && y <= r[1] + r[3] && y + h >= r[1] }
                if (!overlap) { rooms += intArrayOf(x, y, w, h); break }
            }
        }
        for (room in rooms) {
            val (x, y, w, h) = room.toList()
            for (yy in y until y + h) for (xx in x until x + w) map[yy][xx] = FLOOR
            for (xx in x until x + w) { map[y][xx] = WALL; map[y + h - 1][xx] = WALL }
            for (yy in y until y + h) { map[yy][x] = WALL; map[yy][x + w - 1] = WALL }
        }
        // Corridors connect centres.
        for (i in 0 until rooms.size - 1) {
            val a = rooms[i]; val b = rooms[i + 1]
            val ax = a[0] + a[2] / 2; val ay = a[1] + a[3] / 2
            val bx = b[0] + b[2] / 2; val by = b[1] + b[3] / 2
            var x = ax; var y = ay
            while (x != bx) { if (map[y][x] == ' ' || map[y][x] == WALL) map[y][x] = FLOOR; x += if (bx > x) 1 else -1 }
            while (y != by) { if (map[y][x] == ' ' || map[y][x] == WALL) map[y][x] = FLOOR; y += if (by > y) 1 else -1 }
        }
        // Place player in room 0.
        val first = rooms.first()
        px = first[0] + 1; py = first[1] + 1; map[py][px] = FLOOR
        // Items in random rooms.
        for (room in rooms.drop(1)) {
            if (Random.nextFloat() < 0.7f) {
                val ix = Random.nextInt(room[0] + 1, room[0] + room[2] - 1)
                val iy = Random.nextInt(room[1] + 1, room[1] + room[3] - 1)
                map[iy][ix] = if (Random.nextFloat() < 0.5f) GOLD else POTION
            }
        }
        // Monsters.
        val symCounters = HashMap<Char, Int>()
        for (room in rooms.drop(1)) {
            for (i in 0 until 1 + level / 2) {
                val mx = Random.nextInt(room[0] + 1, room[0] + room[2] - 1)
                val my = Random.nextInt(room[1] + 1, room[1] + room[3] - 1)
                val sym = listOf('B', 'O', 'R', 'S').random()
                val baseHp = when (sym) { 'B' -> 4; 'O' -> 8; 'R' -> 3; 'S' -> 6; else -> 5 }
                val atk = when (sym) { 'B' -> 2; 'O' -> 4; 'R' -> 1; 'S' -> 3; else -> 2 }
                val idx = (symCounters[sym] ?: 0) + 1
                symCounters[sym] = idx
                monsters += Monster(mx, my, baseHp + level, sym, atk + level / 2, baseHp + level,
                    tag = "${sym}${idx}", nickname = monsterNicks.random())
            }
        }
        // Stairs / Amulet.
        val last = rooms.last()
        val sx = last[0] + last[2] / 2; val sy = last[1] + last[3] / 2
        map[sy][sx] = if (level == maxLevel) AMULET else STAIRS

        // Wall corridors — every FLOOR tile gets WALL neighbours where there
        // was only void (' '). Юджин: "где стены коридоров?" — без них коридор
        // визуально читается как «можно идти куда угодно», а игрок упирается
        // в невидимые границы. Теперь стены видны.
        for (r in 0 until rows) for (c in 0 until cols) {
            if (map[r][c] != FLOOR) continue
            for (dr in -1..1) for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val nr = r + dr; val nc = c + dc
                if (nr in 0 until rows && nc in 0 until cols && map[nr][nc] == ' ') {
                    map[nr][nc] = WALL
                }
            }
        }

        msg = t("Welcome to level $level.", "Добро пожаловать на уровень $level.")
    }

    private fun move(dx: Int, dy: Int, ctx: GameContext) {
        if (dead || won) { init(ctx); return }
        val nx = px + dx; val ny = py + dy
        if (nx !in 0 until cols || ny !in 0 until rows) return
        // Attack monster?
        val mi = monsters.indexOfFirst { it.x == nx && it.y == ny }
        if (mi >= 0) {
            val m = monsters[mi]
            m.hp -= atk
            ctx.sound.brickHit()
            if (m.hp <= 0) {
                monsters.removeAt(mi)
                gold += 5 + level * 2
                msg = t("You slay ${monsterFullName(m)}!", "Ты сразил: ${monsterFullName(m)}!")
            } else msg = t("You hit ${symId(m)} (${m.hp}HP).", "Удар по ${symId(m)} (${m.hp}HP).")
            monstersTurn(ctx)
            return
        }
        val t = map[ny][nx]
        when (t) {
            WALL, ' ' -> return
            POTION -> { hp = (hp + 6).coerceAtMost(maxHp); msg = t("Potion! +6 HP.", "Зелье! +6 HP."); map[ny][nx] = FLOOR; ctx.sound.score() }
            GOLD -> { gold += 8; msg = t("Gold +8.", "Золото +8."); map[ny][nx] = FLOOR; ctx.sound.eat() }
            STAIRS -> { level++; maxHp += 4; hp = maxHp; atk++; gen(); ctx.sound.win(); return }
            AMULET -> {
                won = true; msg = t("You found the Amulet of Yendor!", "Ты нашёл Амулет Йендора!"); ctx.sound.win()
                if (gold > ctx.scores.get(id)) ctx.scores.set(id, gold)   // victory must record the score too
                return
            }
        }
        px = nx; py = ny
        monstersTurn(ctx)
    }

    private fun monstersTurn(ctx: GameContext) {
        for (m in monsters) {
            val dx = kotlin.math.sign((px - m.x).toFloat()).toInt()
            val dy = kotlin.math.sign((py - m.y).toFloat()).toInt()
            val tryDx = m.x + dx; val tryDy = m.y + dy
            val nx: Int; val ny: Int
            if (Random.nextBoolean() && tryDx in 0 until cols && map[m.y][tryDx] == FLOOR && !monsters.any { it !== m && it.x == tryDx && it.y == m.y }) {
                nx = tryDx; ny = m.y
            } else if (tryDy in 0 until rows && map[tryDy][m.x] == FLOOR && !monsters.any { it !== m && it.x == m.x && it.y == tryDy }) {
                nx = m.x; ny = tryDy
            } else { nx = m.x; ny = m.y }
            if (nx == px && ny == py) {
                hp -= m.atk
                msg = "${symId(m)} hits you (-${m.atk}HP)."
                ctx.sound.die()
                if (hp <= 0) { dead = true; ctx.sound.gameOver(); val best = ctx.scores.get(id); if (gold > best) ctx.scores.set(id, gold); return }
                continue
            }
            m.x = nx; m.y = ny
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        lastCtx = ctx
        lastCtx = ctx
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        when (stage) {
            Stage.CHOOSE -> { drawChoose(this, ctx); return@with }
            Stage.HELP -> { drawHelp(this, ctx); return@with }
            Stage.PLAY -> {}
        }
        drawRect(Color(0xFF111111), topLeft = Offset.Zero, size = Size(W, hudH))
        drawDosText("HP $hp/$maxHp", x = 10f, y = 22f, color = if (hp > maxHp / 3) Color(0xFF55FF55) else Color(0xFFFF5555), size = 14f, bold = true)
        drawDosText("ATK $atk", x = 130f, y = 22f, color = Color(0xFFFFFF55), size = 14f, bold = true)
        drawDosText("LV $level", x = 220f, y = 22f, color = Color(0xFF55FFFF), size = 14f, bold = true)
        drawDosText("$ $gold", x = 300f, y = 22f, color = Color(0xFFFFFF55), size = 14f, bold = true)
        drawDosText(msg, x = W - 10f, y = 22f, color = Color.White, size = 12f, align = TextAlign.RIGHT)

        for (r in 0 until rows) for (c in 0 until cols) {
            val ch = map[r][c]
            val y = hudH + r * cell + cell - 2f
            val x = c * cell + cell / 2f
            val color = when (ch) {
                WALL -> Color(0xFFAAAAAA)
                FLOOR -> Color(0xFF555555)
                POTION -> Color(0xFFFF55FF)
                GOLD -> Color(0xFFFFFF55)
                STAIRS -> Color(0xFF55FFFF)
                AMULET -> Color(0xFFFFFFFF)
                else -> Color.DarkGray
            }
            drawDosText(if (ch == ' ') "." else ch.toString(), x = x, y = y, color = color, size = cell, bold = ch != FLOOR, align = TextAlign.CENTER)
        }
        for (m in monsters) {
            val x = m.x * cell + cell / 2f; val y = hudH + m.y * cell + cell - 2f
            drawDosText(m.sym.toString(), x = x, y = y, color = egaMonster(m.sym), size = cell, bold = true, align = TextAlign.CENTER)
        }
        val px2 = px * cell + cell / 2f; val py2 = hudH + py * cell + cell - 2f
        drawDosText("@", x = px2, y = py2, color = Color(0xFFFFFF55), size = cell, bold = true, align = TextAlign.CENTER)

        if (dead) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("YOU DIED", "ТЫ ПОГИБ"), x = W / 2f, y = H / 2f - 12f, color = Color(0xFFFF3333), size = 32f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Gold $gold · Level $level", "Золото $gold · Уровень $level"), x = W / 2f, y = H / 2f + 20f, color = Color.White, size = 16f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("CLICK TO RESTART", "КЛИК — РЕСТАРТ"), x = W / 2f, y = H / 2f + 50f, color = Color(0xFFCCCCCC), size = 14f, align = TextAlign.CENTER)
        }
        if (won) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(W, H))
            drawDosText(ctx.tr("YOU WIN", "ПОБЕДА"), x = W / 2f, y = H / 2f - 12f, color = Color(0xFFFFD600), size = 36f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Amulet of Yendor!", "Амулет Йендора!"), x = W / 2f, y = H / 2f + 20f, color = Color.White, size = 16f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("Gold $gold · CLICK TO PLAY AGAIN", "Золото $gold · КЛИК — ЕЩЁ РАЗ"), x = W / 2f, y = H / 2f + 50f, color = Color(0xFFCCCCCC), size = 14f, align = TextAlign.CENTER)
        }
    }

    private var touchStart: Offset? = null
    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        when (stage) {
            Stage.CHOOSE -> {
                val cw = W * 0.32f
                val cont = Offset(W / 2f - cw - 10f, H / 2f - 22f)
                val nw = Offset(W / 2f + 10f, H / 2f - 22f)
                if (hasSave && x in cont.x..cont.x + cw && y in cont.y..cont.y + 44f) {
                    if (loadFrom(ctx.settings.rogueSave)) { stage = Stage.PLAY; ctx.sound.menuSelect(); return }
                    // Corrupt save: wipe it and hide CONTINUE instead of a dead button.
                    ctx.settings.rogueSave = ""; hasSave = false; ctx.sound.pcError(); return
                }
                if (x in nw.x..nw.x + cw && y in nw.y..nw.y + 44f) {
                    level = 1; hp = 20; maxHp = 20; atk = 4; gold = 0; dead = false; won = false
                    ctx.settings.rogueSave = ""; gen(); stage = Stage.HELP; ctx.sound.menuSelect(); return
                }
                return
            }
            Stage.HELP -> { stage = Stage.PLAY; ctx.sound.menuSelect(); return }
            Stage.PLAY -> {}
        }
        touchStart = Offset(x, y)
    }
    override fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (stage != Stage.PLAY) return
        val s = touchStart ?: return
        touchStart = null
        if (dead || won) { init(ctx); return }
        val dx = x - s.x; val dy = y - s.y
        if (abs(dx) < 18f && abs(dy) < 18f) return
        if (abs(dx) > abs(dy)) move(if (dx > 0) 1 else -1, 0, ctx)
        else move(0, if (dy > 0) 1 else -1, ctx)
    }

    private fun drawChoose(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))
        drawDosText("D U N G E O N", x = W / 2f, y = H * 0.20f, color = Color(0xFF00FF00), size = 36f, bold = true, align = TextAlign.CENTER)
        drawDosText(ctx.tr("Dungeon. Five levels. The Amulet of Yendor at the end.", "Подземелье. Пять уровней. В конце — Амулет Йендора."), x = W / 2f, y = H * 0.30f, color = Color(0xFFAAAAAA), size = 12f, align = TextAlign.CENTER)
        drawDosText(ctx.tr("No saves between deaths. Die — start over.", "Смерть не прощается. Погиб — начинай сначала."), x = W / 2f, y = H * 0.36f, color = Color(0xFFFF6666), size = 11f, align = TextAlign.CENTER)
        val cw = W * 0.32f
        // Continue button.
        val cx = W / 2f - cw - 10f
        drawRect(if (hasSave) Color(0xFF003300) else Color(0xFF1A1A1A),
            topLeft = Offset(cx, H / 2f - 22f), size = Size(cw, 44f))
        drawRect(if (hasSave) Color(0xFF00FF00) else Color(0xFF333333),
            topLeft = Offset(cx, H / 2f - 22f), size = Size(cw, 2f))
        drawDosText(ctx.tr("CONTINUE", "ПРОДОЛЖИТЬ"), x = cx + cw / 2f, y = H / 2f + 4f,
            color = if (hasSave) Color(0xFF00FF00) else Color(0xFF555555), size = 16f, bold = true, align = TextAlign.CENTER)
        // New game.
        val nx = W / 2f + 10f
        drawRect(Color(0xFF000033), topLeft = Offset(nx, H / 2f - 22f), size = Size(cw, 44f))
        drawRect(Color(0xFF4488FF), topLeft = Offset(nx, H / 2f - 22f), size = Size(cw, 2f))
        drawDosText(ctx.tr("NEW GAME", "НОВАЯ ИГРА"), x = nx + cw / 2f, y = H / 2f + 4f, color = Color(0xFF4488FF), size = 16f, bold = true, align = TextAlign.CENTER)
        if (hasSave) drawDosText("(save from a prior run exists)", x = W / 2f, y = H * 0.7f, color = Color(0xFFAAAAAA), size = 11f, align = TextAlign.CENTER)
    }

    private fun drawHelp(scope: DrawScope, ctx: GameContext) = with(scope) {
        drawRect(Color.Black, topLeft = Offset.Zero, size = Size(W, H))
        drawDosText(ctx.tr("H O W   T O   P L A Y", "К А К   И Г Р А Т Ь"), x = W / 2f, y = 36f, color = Color(0xFF00FF00), size = 24f, bold = true, align = TextAlign.CENTER)
        val lines = listOf(
            ctx.tr("@  — you, adventurer.", "@  — ты, искатель приключений.") to Color(0xFF00FF00),
            ctx.tr("#  — wall.   .  — room or corridor floor.", "#  — стена.   .  — пол комнаты или коридора.") to Color(0xFFAAAAAA),
            ctx.tr("!  — potion. +6 HP when you step on it.", "!  — зелье. +6 HP, если наступить.") to Color(0xFFFF55FF),
            ctx.tr("$  — gold. +8 to score.", "$  — золото. +8 к счёту.") to Color(0xFFFFD600),
            ctx.tr(">  — stairs down. Step in to descend.", ">  — лестница вниз. Наступи, чтобы спуститься.") to Color(0xFF55FFFF),
            ctx.tr("&  — Amulet of Yendor. Spawns on level 5.", "&  — Амулет Йендора. Появляется на 5-м уровне.") to Color(0xFFFFAA00),
            ctx.tr("B Bat / O Orc / R Rat / S Snake — walk INTO one to attack.", "B мышь / O орк / R крыса / S змея — шагни В монстра для атаки.") to Color(0xFFFF5555),
            "" to Color.White,
            ctx.tr("Each move is one turn — every monster moves too.", "Каждый шаг — ход: монстры ходят тоже.") to Color.White,
            ctx.tr("Beat five levels — you win. Die — restart.", "Пройди пять уровней — победа. Погиб — заново.") to Color.White,
            ctx.tr("Auto-save on exit. No manual save — it's a roguelike.", "Автосейв при выходе. Ручного нет — это рогалик.") to Color(0xFFFFD600),
        )
        for ((i, kv) in lines.withIndex()) {
            drawDosText(kv.first, x = 20f, y = 80f + i * 22f, color = kv.second, size = 12f, bold = true)
        }
        drawDosText(ctx.tr("Click — descend", "Тап — вниз"), x = W / 2f, y = H - 30f, color = Color(0xFF00FF00), size = 14f, bold = true, align = TextAlign.CENTER)
    }
}
