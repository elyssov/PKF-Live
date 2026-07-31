package com.pixelclassics.app.games.battlecity

import com.pixelclassics.app.compat.BitmapFactory
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import com.pixelclassics.app.compat.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.GameControls
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * BATTLE CITY — NES 1990 spec rewrite. Юджин: «если беремся —
 * в точности как было в 1990-м».
 *
 * Changes from v1:
 * - controls = DPAD_AND_FIRE (use the common overlay).
 * - Bigger board: tile 40px, HUD moved to the TOP (full-width strip),
 *   not the right column. Board fills the rest.
 * - Tank barrel is a long-and-narrow «хобот» protruding from the
 *   chassis in the facing direction — easy to read where the tank
 *   aims. ★ pips removed from the dome (Юджин: «звезда хаоса»);
 *   stars now show in the HUD only.
 * - 35 stages: 5 named, 30 procedural seeded.
 * - 6 NES powerups: helmet (shield 10s), clock (freeze enemies 5s),
 *   shovel (steel around base 10s), star (tank upgrade), bomb (clear
 *   all field), tank (1up). Spawn when a marked enemy is killed.
 */
class BattleCityGame : Game {
    override val id: String = "battle_city"
    override val titleResId: Int = R.string.game_battle_city
    override val backgroundColor: Color = Color(0xFF111111)
    private val controlsMode = mutableStateOf(GameControls.NONE)
    override val controls: GameControls get() = controlsMode.value
    override val controlsState: State<GameControls> get() = controlsMode
    override val introText: String = BATTLE_CITY_INTRO_EN          // legacy callers
    override fun introTextFor(lang: String): String = pickBattleCityIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.PARCHMENT
    override val helpLines: List<String> = listOf(
        "D-pad — drive · ● FIRE — shoot",
        "Defend the command core (bottom centre).",
        "Powerups: ★ upgrade · ⏱ freeze · 🛡 shield · ⚒ steel base · 💣 clear · 🚛 1up",
    )
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Крестовина — езда · ● ОГОНЬ — выстрел",
        "Защити командное ядро (внизу по центру).",
        "Бонусы: ★ апгрейд · ⏱ заморозка · 🛡 щит · ⚒ стальная база · 💣 зачистка · 🚛 1up",
    ) else helpLines

    private val grid = 13
    private val tile = 40f
    private val boardSize = grid * tile        // 520
    private val hudH = 60f
    override val virtualWidth: Float get() = boardSize
    override val virtualHeight: Float get() = boardSize + hudH
    private val boardY get() = hudH

    private val board = Array(grid) { IntArray(grid) }
    // Tile codes: 0 open, 1 brick, 2 steel, 3 water, 4 forest, 5 ice, 9 eagle.

    private enum class Dir(val dx: Int, val dy: Int) { U(0, -1), D(0, 1), L(-1, 0), R(1, 0) }

    private data class Tank(
        var x: Float, var y: Float, var dir: Dir,
        var enemy: Boolean, var kind: Int,
        var hp: Int = 1, var cooldown: Float = 0f,
        var aiThink: Float = 0f, var alive: Boolean = true,
        var spawnTimer: Float = 0.6f,
        var marked: Boolean = false,           // killing a marked enemy drops a powerup
        var frozen: Boolean = false,
    ) {
        val size: Float get() = 36f
        val cx: Float get() = x + size / 2f
        val cy: Float get() = y + size / 2f
    }

    private data class Bullet(var x: Float, var y: Float, var dir: Dir, var fromEnemy: Boolean, var power: Boolean)
    private data class Explosion(var x: Float, var y: Float, var r: Float, val maxR: Float, var grow: Boolean)
    private data class PowerUp(var x: Float, var y: Float, val kind: Power)
    private enum class Power { HELMET, CLOCK, SHOVEL, STAR, BOMB, TANK }

    private var player = Tank(0f, 0f, Dir.U, false, 0)
    private val tanks = mutableListOf<Tank>()
    private val bullets = mutableListOf<Bullet>()
    private val explosions = mutableListOf<Explosion>()
    private val powerups = mutableListOf<PowerUp>()

    private var level = 1
    private val maxLevel = 35
    private var lives = 3
    private var enemiesLeft = 20
    private var enemiesOnField = 0
    private var spawnAcc = 0f
    private var spawnIdx = 0
    private var score = 0
    private var best = 0
    private var stars = 0
    private var eagleAlive = true
    private var gameOver = false
    private var won = false
    private var startTimer = 1.5f
    private var time = 0f
    private var shieldUntil = 0f
    private var freezeUntil = 0f
    private var shovelUntil = 0f
    private var shovelActive = false
    private val eagleAlcoveBricks = listOf(11 to 5, 11 to 6, 11 to 7, 12 to 5, 12 to 7)
    private enum class Mode { TITLE, CAMPAIGN, EDITOR, EDIT_TEST }
    private var mode = Mode.TITLE
    private val editorBoard = Array(grid) { IntArray(grid) }
    private var editorTile = 1
    private val editorTiles = intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 10)

    override fun init(ctx: GameContext) {
        best = ctx.scores.get(id)
        loadSprites(ctx)
        mode = Mode.TITLE
        controlsMode.value = GameControls.NONE
    }

    // ── Sprites (from the HD redraw sheet, sliced into assets/sprites/tanks) ──
    private var sprites: Map<String, ImageBitmap> = emptyMap()
    private fun loadSprites(ctx: GameContext) {
        if (sprites.isNotEmpty()) return
        val names = listOf(
            "player_up", "player_right", "player_down", "player_left",
            "player_scout_up", "player_teal_up", "player_heavy_up", "player_amber_up",
            "player_damaged",
            "enemy_up", "enemy_right", "enemy_down", "enemy_left", "enemy_boss",
            "enemy_cobalt_up", "enemy_crimson_up", "enemy_olive_up", "enemy_violet_up",
            "tile_masonry", "tile_titanium", "tile_water", "tile_canopy", "tile_ice",
            "tile_sand", "tile_drain", "tile_fence", "tile_cracked_titanium",
            "command_core", "command_core_destroyed", "power_repair", "power_shield",
            "power_overdrive", "power_emp", "explosion_large", "explosion_small",
            "shell_player", "shell_enemy", "rocket", "spawn_beacon", "editor_cursor",
        )
        sprites = names.mapNotNull { n ->
            runCatching {
                ctx.assets.open("sprites/tanks/steel_frontier/$n.png").use { n to BitmapFactory.decodeStream(it).asImageBitmap() }
            }.getOrNull()
        }.toMap()
    }
    private fun DrawScope.sprite(name: String, x: Float, y: Float, w: Float, h: Float, alpha: Float = 1f) {
        val img = sprites[name] ?: return
        drawImage(
            img,
            dstOffset = IntOffset(x.roundToInt(), y.roundToInt()),
            dstSize = IntSize(w.roundToInt(), h.roundToInt()),
            alpha = alpha,
        )
    }

    private fun restart() {
        mode = Mode.CAMPAIGN
        controlsMode.value = GameControls.DPAD_AND_FIRE
        lives = 3; score = 0; stars = 0; level = 1; gameOver = false; won = false
        loadLevel()
    }

    private fun loadLevel() {
        for (r in 0 until grid) for (c in 0 until grid) board[r][c] = 0
        val lv = ((level - 1) % maxLevel) + 1
        if (mode == Mode.EDIT_TEST) copyBoard(editorBoard, board)
        else if (lv <= 5) loadNamedLevel(lv) else loadProceduralLevel(lv)
        // Eagle alcove (rows 11-12, cols 5-7).
        board[11][5] = 1; board[11][6] = 1; board[11][7] = 1
        board[12][5] = 1; board[12][7] = 1
        board[12][6] = 9

        bullets.clear(); explosions.clear(); tanks.clear(); powerups.clear()
        eagleAlive = true
        enemiesLeft = 20; enemiesOnField = 0; spawnAcc = 0f; spawnIdx = 0
        startTimer = 1.5f
        shieldUntil = time + 3f       // spawn invulnerability
        freezeUntil = 0f; shovelUntil = 0f; shovelActive = false
        player = Tank(4f * tile, 12f * tile, Dir.U, false, 0, spawnTimer = 0f)
    }

    private fun copyBoard(from: Array<IntArray>, to: Array<IntArray>) {
        for (r in 0 until grid) for (c in 0 until grid) to[r][c] = from[r][c]
    }

    private fun startEditor() {
        mode = Mode.EDITOR
        controlsMode.value = GameControls.NONE
        for (r in 0 until grid) for (c in 0 until grid) board[r][c] = 0
        loadNamedLevel(1)
        copyBoard(board, editorBoard)
        editorTile = 1
    }

    private fun testEditorMap() {
        mode = Mode.EDIT_TEST
        controlsMode.value = GameControls.DPAD_AND_FIRE
        lives = 3; score = 0; stars = 0; level = 1; gameOver = false; won = false
        loadLevel()
    }

    private fun loadNamedLevel(lv: Int) {
        when (lv) {
            1 -> {
                for (c in 1..11) { board[3][c] = 1; board[9][c] = 1 }
                for (r in 4..8) board[r][6] = 1
                board[6][3] = 2; board[6][9] = 2
            }
            2 -> {
                for (r in 2..10) { board[r][2] = 1; board[r][10] = 1 }
                for (c in 4..8) { board[5][c] = 1; board[7][c] = 1 }
                board[6][6] = 3
            }
            3 -> {
                for (r in 1..11 step 2) for (c in 1..11 step 2) board[r][c] = 1
                board[5][6] = 2; board[7][6] = 2
                board[4][3] = 4; board[4][9] = 4; board[8][3] = 4; board[8][9] = 4
            }
            4 -> {
                for (c in 0 until grid) { board[2][c] = 1; board[10][c] = 1 }
                board[2][6] = 2; board[10][6] = 2
                for (r in 4..8) board[r][3] = 3
                for (r in 4..8) board[r][9] = 3
            }
            5 -> {
                for (r in 1..11) for (c in 1..11) if ((r + c) % 3 == 0) board[r][c] = 1
                board[6][6] = 2
                for (r in 3..9 step 3) board[r][6] = 5
            }
        }
    }

    private fun loadProceduralLevel(lv: Int) {
        val rng = Random(lv.toLong() * 7919L + 31337L)
        // Choose template.
        val templates = listOf<(Random) -> Unit>(
            { r -> for (rr in 1..11 step 2) for (cc in 1..11) if (r.nextFloat() < 0.6f) board[rr][cc] = 1 },
            { r -> for (rr in 2..10 step 4) for (cc in 1..11) board[rr][cc] = if (r.nextFloat() < 0.4f) 2 else 1 },
            { r -> for (col in 1..11 step 2) for (rr in 2..10) if (r.nextFloat() < 0.7f) board[rr][col] = 1
                board[6][6] = 2 },
            { r -> for (rr in 1..11) for (cc in 1..11) {
                val roll = r.nextFloat(); board[rr][cc] = when { roll < 0.30f -> 1; roll < 0.36f -> 2; roll < 0.40f -> 3; roll < 0.50f -> 4; else -> 0 }
            } },
            { r -> for (rr in 2..10) for (cc in 0..12) if ((rr + cc) % 2 == 0 && r.nextFloat() < 0.6f) board[rr][cc] = 1 },
        )
        templates[rng.nextInt(templates.size)](rng)
        // Steel Frontier materials become more common deeper in the campaign.
        for (rr in 1..10) for (cc in 0..12) {
            if (board[rr][cc] != 0) continue
            val roll = rng.nextFloat()
            board[rr][cc] = when {
                lv >= 30 && roll < 0.025f -> 10
                lv >= 24 && roll < 0.050f -> 8
                lv >= 18 && roll < 0.080f -> 7
                lv >= 12 && roll < 0.115f -> 6
                else -> 0
            }
        }
        // Ensure the eagle approach is mostly clear.
        for (rr in 10..12) for (cc in 4..8) if (board[rr][cc] == 2) board[rr][cc] = 1
    }

    private fun trySpawnEnemy() {
        if (enemiesLeft <= 0 || enemiesOnField >= 4) return
        val spawnX = intArrayOf(0, 6, 12)[spawnIdx % 3]
        spawnIdx++
        val sx = spawnX * tile
        val sy = 0f
        for (t in tanks) if (t.alive && abs(t.x - sx) < 36f && abs(t.y - sy) < 36f) return
        val kind = if (level >= 3) Random.nextInt(3) else Random.nextInt(2)
        val hp = if (kind == 2) 2 else 1
        val marked = Random.nextFloat() < 0.18f
        tanks += Tank(sx, sy, Dir.D, enemy = true, kind = kind, hp = hp, spawnTimer = 0.6f, marked = marked)
        enemiesLeft--
        enemiesOnField++
    }

    private fun tileAt(x: Float, y: Float): Int {
        val r = (y / tile).toInt()
        val c = (x / tile).toInt()
        if (r !in 0 until grid || c !in 0 until grid) return 2
        return board[r][c]
    }
    private fun isWalkable(t: Int): Boolean = t == 0 || t == 4 || t == 5 || t == 6 || t == 7

    private fun tankBlocked(tank: Tank, newX: Float, newY: Float): Boolean {
        val size = tank.size
        val xs = floatArrayOf(newX + 1f, newX + size - 1f, newX + 1f, newX + size - 1f)
        val ys = floatArrayOf(newY + 1f, newY + 1f, newY + size - 1f, newY + size - 1f)
        for (i in 0 until 4) {
            val t = tileAt(xs[i], ys[i])
            if (!isWalkable(t)) return true
        }
        for (other in tanks) {
            if (other === tank || !other.alive) continue
            if (newX + size > other.x && newX < other.x + other.size &&
                newY + size > other.y && newY < other.y + other.size) return true
        }
        if (tank !== player && player.alive && newX + size > player.x && newX < player.x + player.size &&
            newY + size > player.y && newY < player.y + player.size) return true
        return false
    }

    // ── Input (via GameControls.DPAD_AND_FIRE) ───────────────────────────
    private var moveDir: Dir? = null
    override fun onDirection(dx: Int, dy: Int, ctx: GameContext) {
        moveDir = when {
            dx > 0 -> Dir.R; dx < 0 -> Dir.L
            dy > 0 -> Dir.D; dy < 0 -> Dir.U
            else -> null
        }
    }
    override fun onFire(ctx: GameContext) {
        if (mode == Mode.TITLE || mode == Mode.EDITOR) return
        if (gameOver || won) {
            if (mode == Mode.EDIT_TEST) {
                mode = Mode.EDITOR
                controlsMode.value = GameControls.NONE
            } else restart()
            return
        }
        if (player.alive && player.cooldown <= 0f) {
            spawnPlayerBullet(ctx)
            player.cooldown = if (stars >= 1) 0.18f else 0.30f
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        time += dt
        if (mode == Mode.TITLE || mode == Mode.EDITOR) return
        if (gameOver || won) return
        if (startTimer > 0f) { startTimer -= dt; return }

        if (player.alive) {
            val dir = moveDir
            if (dir != null) {
                val sp = (110f + stars * 18f) * terrainSpeedMul(player)
                player.dir = dir
                val nx = player.x + dir.dx * sp * dt
                val ny = player.y + dir.dy * sp * dt
                if (!tankBlocked(player, nx, ny)) { player.x = nx; player.y = ny }
                else {
                    // Try grid snap to ease cornering.
                    // Snap to HALF-tile rails (tile = 40f); the old 16f grid was a
                    // leftover from the 16px-tile build and shoved the tank off-grid.
                    val half = tile / 2f
                    val snapped = if (dir.dx != 0) Offset(nx, (player.y / half).toInt() * half) else Offset((player.x / half).toInt() * half, ny)
                    if (!tankBlocked(player, snapped.x, snapped.y)) { player.x = snapped.x; player.y = snapped.y }
                }
            }
            if (player.cooldown > 0f) player.cooldown -= dt
        }

        // Enemy spawn.
        spawnAcc += dt
        if (spawnAcc >= 3.0f) { spawnAcc = 0f; trySpawnEnemy() }

        // Freeze.
        val frozen = time < freezeUntil

        // Enemy AI.
        for (t in tanks) {
            if (!t.alive) continue
            if (t.spawnTimer > 0f) { t.spawnTimer -= dt; continue }
            if (frozen) continue
            t.aiThink -= dt
            if (t.aiThink <= 0f) {
                t.aiThink = 0.6f + Random.nextFloat()
                t.dir = pickEnemyDir(t)
            }
            val sp = enemySpeed(t) * terrainSpeedMul(t)
            val nx = t.x + t.dir.dx * sp * dt
            val ny = t.y + t.dir.dy * sp * dt
            if (!tankBlocked(t, nx, ny)) { t.x = nx; t.y = ny }
            else t.aiThink = 0f
            t.cooldown -= dt
            if (t.cooldown <= 0f && Random.nextFloat() < dt * 1.2f) {
                spawnEnemyBullet(t); t.cooldown = 1.0f + Random.nextFloat()
            }
        }

        // Bullets.
        val dead = mutableSetOf<Int>()
        bullets@ for (idx in bullets.indices) {
            if (idx in dead) continue
            val b = bullets[idx]
            val sp = 280f
            b.x += b.dir.dx * sp * dt
            b.y += b.dir.dy * sp * dt
            if (b.x < 0f || b.x > boardSize || b.y < 0f || b.y > boardSize) { dead += idx; continue }
            val r = (b.y / tile).toInt().coerceIn(0, grid - 1)
            val c = (b.x / tile).toInt().coerceIn(0, grid - 1)
            val tt = board[r][c]
            when (tt) {
                1, 8 -> { board[r][c] = 0; dead += idx; ctx.sound.brickHit(); continue@bullets }
                2 -> {
                    if (b.power) { board[r][c] = 0; ctx.sound.brickBreak() } else ctx.sound.brickHit()
                    dead += idx; continue@bullets
                }
                9 -> { board[r][c] = 0; eagleAlive = false; gameOver = true; ctx.sound.bigExplosion();
                    if (score > best) { best = score; ctx.scores.set(id, score) }
                    explosions += Explosion(b.x, b.y, 6f, 38f, true); dead += idx; continue@bullets }
                10 -> { board[r][c] = 0; dead += idx; ctx.sound.brickBreak(); continue@bullets }
            }
            if (!b.fromEnemy) {
                for (t in tanks) {
                    if (!t.alive || t.spawnTimer > 0f) continue
                    if (b.x in t.x..(t.x + t.size) && b.y in t.y..(t.y + t.size)) {
                        t.hp--; dead += idx
                        if (t.hp <= 0) {
                            t.alive = false; enemiesOnField--
                            score += listOf(100, 200, 300)[t.kind]
                            ctx.sound.explosion()
                            explosions += Explosion(t.cx, t.cy, 4f, 26f, true)
                            if (t.marked) spawnRandomPowerUp()
                        } else ctx.sound.brickHit()
                        continue@bullets
                    }
                }
            } else {
                if (player.alive &&
                    b.x in player.x..(player.x + player.size) &&
                    b.y in player.y..(player.y + player.size)
                ) {
                    dead += idx
                    if (time < shieldUntil) {
                        ctx.sound.brickHit()
                        continue@bullets
                    }
                    explosions += Explosion(player.cx, player.cy, 4f, 30f, true)
                    ctx.sound.bigExplosion()
                    killPlayer(ctx)
                    continue@bullets
                }
            }
        }
        for (i in bullets.indices) {
            if (i in dead) continue
            for (j in (i + 1) until bullets.size) {
                if (j in dead) continue
                val a = bullets[i]; val b = bullets[j]
                // Only OPPOSING bullets annihilate — the star-2/3 twin shot
                // spawns two parallel player bullets 6px apart, which must not
                // destroy each other on the next frame.
                if (a.fromEnemy != b.fromEnemy && abs(a.x - b.x) < 8f && abs(a.y - b.y) < 8f) { dead += i; dead += j; break }
            }
        }
        if (dead.isNotEmpty()) {
            val survivors = bullets.filterIndexed { i, _ -> i !in dead }
            bullets.clear(); bullets.addAll(survivors)
        }

        // Pickups — AABB overlap between the player and the pickup's tile.
        // The previous check tested only whether the pickup's top-left corner
        // sat inside the player's body — it triggered only when player.x and
        // p.x lined up exactly to the pixel. Driving over a pickup with a
        // 2-pixel offset silently missed it, which is what Юджин saw as
        // "бонусы не работают, или не все".
        val pIt = powerups.iterator()
        while (pIt.hasNext()) {
            val p = pIt.next()
            if (player.alive &&
                player.x < p.x + tile && player.x + player.size > p.x &&
                player.y < p.y + tile && player.y + player.size > p.y
            ) {
                applyPower(p.kind, ctx); pIt.remove()
            }
        }

        // Shovel — toggle the alcove ON TRANSITIONS only. The previous code
        // ran every frame and regenerated brick from empty whenever shovel
        // wasn't active, which made the eagle effectively invulnerable
        // through the wall. NES spec: shovel upgrades brick→steel for 10s
        // (also restoring destroyed alcove cells to steel for that window),
        // then on expire steel→brick once. Destroyed cells stay destroyed.
        val nowShovel = time < shovelUntil
        if (nowShovel != shovelActive) {
            shovelActive = nowShovel
            for ((rr, cc) in eagleAlcoveBricks) {
                if (board[rr][cc] == 9) continue
                board[rr][cc] = if (nowShovel) 2 else 1
            }
        }

        // Explosions.
        val eIt = explosions.iterator()
        while (eIt.hasNext()) {
            val e = eIt.next()
            if (e.grow) { e.r += 90f * dt; if (e.r >= e.maxR) e.grow = false }
            else { e.r -= 60f * dt; if (e.r <= 0f) eIt.remove() }
        }

        if (enemiesLeft == 0 && tanks.none { it.alive }) {
            level++
            score += 100 + lives * 50
            if (score > best) { best = score; ctx.scores.set(id, score) }
            if (mode == Mode.EDIT_TEST) {
                mode = Mode.EDITOR
                controlsMode.value = GameControls.NONE
            } else if (level > maxLevel) won = true else loadLevel()
        }
    }

    private fun enemySpeed(t: Tank): Float = when (t.kind) { 0 -> 70f; 1 -> 120f; else -> 60f }

    /**
     * Terrain mobility factor under a tank's centre. Ice glides (×1.35),
     * sand bogs you down (×0.6), drain is heavier still (×0.4). Everything
     * else is neutral.
     */
    private fun terrainSpeedMul(t: Tank): Float = when (tileAt(t.cx, t.cy)) {
        5 -> 1.35f
        6 -> 0.6f
        7 -> 0.4f
        else -> 1f
    }

    private fun pickEnemyDir(t: Tank): Dir =
        if (Random.nextFloat() < 0.6f) {
            val dx = 6f * tile - t.x; val dy = 12f * tile - t.y
            if (abs(dx) > abs(dy)) if (dx > 0) Dir.R else Dir.L
            else if (dy > 0) Dir.D else Dir.U
        } else Dir.values().random()

    private fun spawnPlayerBullet(ctx: GameContext) {
        val (bx, by) = barrelPos(player)
        bullets += Bullet(bx, by, player.dir, fromEnemy = false, power = stars >= 3)
        if (stars >= 2) {
            val off = 6f
            val (ox, oy) = when (player.dir) {
                Dir.U, Dir.D -> off to 0f
                Dir.L, Dir.R -> 0f to off
            }
            bullets += Bullet(bx + ox, by + oy, player.dir, fromEnemy = false, power = stars >= 3)
        }
        ctx.sound.shoot()
    }
    private fun spawnEnemyBullet(t: Tank) {
        val (bx, by) = barrelPos(t)
        bullets += Bullet(bx, by, t.dir, fromEnemy = true, power = false)
    }
    private fun barrelPos(t: Tank): Pair<Float, Float> {
        val s = t.size
        return when (t.dir) {
            Dir.U -> t.cx to t.y - 2f
            Dir.D -> t.cx to t.y + s + 2f
            Dir.L -> t.x - 2f to t.cy
            Dir.R -> t.x + s + 2f to t.cy
        }
    }

    private fun killPlayer(ctx: GameContext) {
        player.alive = false
        lives--
        if (lives <= 0) {
            gameOver = true; ctx.sound.gameOver()
            if (score > best) { best = score; ctx.scores.set(id, score) }
            return
        }
        player = Tank(4f * tile, 12f * tile, Dir.U, false, 0, spawnTimer = 0f)
        stars = 0
        shieldUntil = time + 3f
    }

    private fun spawnRandomPowerUp() {
        val rng = Power.values().random()
        // Pick a walkable cell — power-ups that land in water/steel/wall are
        // unreachable. Try a few times, then fall back to the centre row.
        var rx = 6f * tile; var ry = 6f * tile
        for (attempt in 0 until 24) {
            val cc = 3 + Random.nextInt(grid - 6)
            val rr = 3 + Random.nextInt(grid - 6)
            if (isWalkable(board[rr][cc])) { rx = cc * tile; ry = rr * tile; break }
        }
        powerups += PowerUp(rx, ry, rng)
    }

    private fun applyPower(kind: Power, ctx: GameContext) {
        ctx.sound.powerUp()
        when (kind) {
            Power.HELMET -> shieldUntil = time + 10f
            Power.CLOCK -> freezeUntil = time + 5f
            Power.SHOVEL -> shovelUntil = time + 10f
            Power.STAR -> stars = (stars + 1).coerceAtMost(3)
            Power.BOMB -> {
                for (t in tanks) if (t.alive) {
                    t.alive = false; enemiesOnField--
                    explosions += Explosion(t.cx, t.cy, 4f, 26f, true)
                    score += listOf(100, 200, 300)[t.kind]
                }
            }
            Power.TANK -> lives++
        }
    }

    // ── Drawing ──────────────────────────────────────────────────────────

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        if (mode == Mode.TITLE) { drawTitle(ctx); return@with }
        if (mode == Mode.EDITOR) { drawEditor(); return@with }
        // HUD strip on top.
        drawRect(Color(0xFF1A1A1A), topLeft = Offset(0f, 0f), size = Size(virtualWidth, hudH))
        drawDosText(ctx.tr("STAGE $level", "ЭТАП $level"), x = 12f, y = 24f, color = Color.White, size = 14f, bold = true)
        drawDosText(ctx.tr("LEFT $enemiesLeft", "ВРАГОВ $enemiesLeft"), x = 12f, y = 46f, color = Color(0xFFCC4444), size = 12f, bold = true)
        drawDosText("SCORE $score", x = virtualWidth / 2f, y = 24f, color = Color(0xFF4FC3F7), size = 14f, bold = true, align = TextAlign.CENTER)
        drawDosText("BEST $best", x = virtualWidth / 2f, y = 46f, color = Color(0xFFFFD600), size = 12f, bold = true, align = TextAlign.CENTER)
        drawDosText("LIVES $lives", x = virtualWidth - 12f, y = 24f, color = Color(0xFF55FF55), size = 14f, bold = true, align = TextAlign.RIGHT)
        // Stars indicator.
        for (k in 0 until stars) drawRect(Color(0xFFFFAA00), topLeft = Offset(virtualWidth - 14f - k * 14f, 40f), size = Size(10f, 10f))
        // Active power timers.
        var px = 12f; val py = 58f
        if (time < shieldUntil) { drawDosText("🛡 ${(shieldUntil - time).toInt() + 1}s", x = px, y = py, color = Color(0xFF88FF88), size = 9f, bold = true); px += 60f }
        if (time < freezeUntil) { drawDosText("⏱ ${(freezeUntil - time).toInt() + 1}s", x = px, y = py, color = Color(0xFF88CCFF), size = 9f, bold = true); px += 60f }
        if (time < shovelUntil) drawDosText("⚒ ${(shovelUntil - time).toInt() + 1}s", x = px, y = py, color = Color(0xFFCCCCCC), size = 9f, bold = true)

        // Board background.
        drawRect(Color(0xFF707070), topLeft = Offset(0f, boardY), size = Size(boardSize, boardSize))
        for (r in 0 until grid) for (c in 0 until grid) {
            val t = board[r][c]
            val x = c * tile; val y = boardY + r * tile
            when (t) {
                0 -> drawRect(Color.Black, topLeft = Offset(x, y), size = Size(tile, tile))
                1 -> sprite("tile_masonry", x, y, tile, tile)
                2 -> sprite("tile_titanium", x, y, tile, tile)
                3 -> sprite("tile_water", x, y, tile, tile)
                4 -> drawRect(Color.Black, topLeft = Offset(x, y), size = Size(tile, tile))  // forest base; canopy drawn on top after tanks
                5 -> sprite("tile_ice", x, y, tile, tile)
                6 -> sprite("tile_sand", x, y, tile, tile)
                7 -> sprite("tile_drain", x, y, tile, tile)
                8 -> sprite("tile_fence", x, y, tile, tile)
                9 -> sprite(if (eagleAlive) "command_core" else "command_core_destroyed", x, y, tile, tile)
                10 -> sprite("tile_cracked_titanium", x, y, tile, tile)
            }
        }
        // Spawn beacon — telegraph the next enemy entry point for ~1.2s
        // before it appears (NES-style "incoming" flicker).
        if (!gameOver && !won && enemiesLeft > 0 && enemiesOnField < 4) {
            val timeLeft = (3.0f - spawnAcc).coerceAtLeast(0f)
            if (timeLeft < 1.2f) {
                val spawnX = intArrayOf(0, 6, 12)[spawnIdx % 3]
                val bx = spawnX * tile
                val by = boardY
                val flicker = (sin(time * 18f) * 0.4f + 0.6f).coerceIn(0f, 1f)
                val fade = (1f - timeLeft / 1.2f).coerceIn(0f, 1f)
                sprite("spawn_beacon", bx, by, tile, tile, alpha = fade * flicker)
            }
        }
        if (player.alive) drawTank(player)
        for (t in tanks) if (t.alive) drawTank(t)
        // Forest canopy overlay — drawn on top of tanks so they can hide in it.
        for (r in 0 until grid) for (c in 0 until grid) {
            if (board[r][c] == 4) sprite("tile_canopy", c * tile, boardY + r * tile, tile, tile)
        }
        // Bullets. Coordinates are in board-space; the board is drawn
        // offset by boardY, so the visual must include boardY too — otherwise
        // bullets hit a tank without the trail visually reaching it
        // (Юджин: "летит не туда, при этом попадаешь" v6.2.2).
        for (b in bullets) {
            sprite(if (b.fromEnemy) "shell_enemy" else if (b.power) "rocket" else "shell_player",
                b.x - 5f, b.y - 5f + boardY, 10f, 10f)
        }
        // Powerups.
        for (p in powerups) drawPowerUp(p)
        // Explosions — sprite frames (med/big by size, frame by grow/shrink).
        for (e in explosions) {
            val name = if (e.maxR > 30f) "explosion_large" else "explosion_small"
            val d = e.r * 2.4f
            sprite(name, e.x - d / 2f, e.y + boardY - d / 2f, d, d)
        }
        // Player shield aura.
        if (player.alive && time < shieldUntil) {
            val pulse = (sin(time * 12f) * 0.3f + 0.7f)
            drawRect(Color(0xFF88FF88).copy(alpha = pulse * 0.35f),
                topLeft = Offset(player.x - 4f, player.y - 4f + boardY),
                size = Size(player.size + 8f, player.size + 8f))
        }

        if (startTimer > 0f && !gameOver) {
            drawRect(Color(0x99000000), topLeft = Offset(0f, boardY), size = Size(boardSize, boardSize))
            drawDosText(ctx.tr("STAGE $level", "ЭТАП $level"), x = boardSize / 2f, y = boardY + boardSize / 2f, color = Color.White, size = 38f, bold = true, align = TextAlign.CENTER)
        }
        if (gameOver) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(virtualWidth, virtualHeight))
            drawDosText("GAME OVER", x = virtualWidth / 2f, y = virtualHeight / 2f - 10f, color = Color(0xFFFF3333), size = 36f, bold = true, align = TextAlign.CENTER)
            drawDosText("SCORE  $score", x = virtualWidth / 2f, y = virtualHeight / 2f + 24f, color = Color.White, size = 16f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("● FIRE — RESTART", "● ОГОНЬ — РЕСТАРТ"), x = virtualWidth / 2f, y = virtualHeight / 2f + 50f, color = Color(0xFFAAAAAA), size = 14f, align = TextAlign.CENTER)
        }
        if (won) {
            drawRect(Color(0xCC000000), topLeft = Offset.Zero, size = Size(virtualWidth, virtualHeight))
            drawDosText("VICTORY!", x = virtualWidth / 2f, y = virtualHeight / 2f - 10f, color = Color(0xFF55FF55), size = 38f, bold = true, align = TextAlign.CENTER)
            drawDosText(ctx.tr("ALL $maxLevel FRONTIERS SECURED", "ВСЕ $maxLevel РУБЕЖЕЙ ВЗЯТЫ"), x = virtualWidth / 2f, y = virtualHeight / 2f + 24f, color = Color.White, size = 16f, align = TextAlign.CENTER)
            drawDosText(ctx.tr("● FIRE — RESTART", "● ОГОНЬ — РЕСТАРТ"), x = virtualWidth / 2f, y = virtualHeight / 2f + 50f, color = Color(0xFFAAAAAA), size = 14f, align = TextAlign.CENTER)
        }
    }

    private fun DrawScope.drawTank(t: Tank) {
        val s = t.size; val x = t.x; val y = t.y + boardY
        if (t.spawnTimer > 0f) {
            val starColor = listOf(Color.White, Color(0xFFFCBC3C), Color(0xFFD82800), Color(0xFF3CBCFC))[((t.spawnTimer * 10f).toInt() % 4)]
            drawRect(Color.Black, topLeft = Offset(x, y), size = Size(s, s))
            drawRect(starColor, topLeft = Offset(x + 4f, y + 4f), size = Size(s - 8f, s - 8f))
            return
        }
        if (t.marked && (time * 6f).toInt() % 2 == 0) {
            drawRect(Color(0xFFFFAA00), topLeft = Offset(x - 2f, y - 2f), size = Size(s + 4f, s + 4f))
        }
        val dirPart = when (t.dir) { Dir.U -> "up"; Dir.L -> "left"; Dir.D -> "down"; Dir.R -> "right" }
        val degrees = when (t.dir) { Dir.U -> 0f; Dir.R -> 90f; Dir.D -> 180f; Dir.L -> 270f }
        val pivot = Offset(x + s / 2f, y + s / 2f)
        if (t.enemy) {
            if (t.kind == 2) { sprite("enemy_boss", x, y, s, s); return }
            // Tier-by-kind palette + late-campaign palette swap. Variants
            // exist only in `_up`, so we rotate to face the right way.
            val variant = when (t.kind) {
                0 -> if (level >= 26) "violet" else "olive"
                1 -> if (level >= 21) "crimson" else "cobalt"
                else -> null
            }
            val variantName = variant?.let { "enemy_${it}_up" }
            if (variantName != null && sprites.containsKey(variantName)) {
                rotate(degrees, pivot) { sprite(variantName, x, y, s, s) }
            } else {
                sprite("enemy_$dirPart", x, y, s, s)
            }
            return
        }
        // Player. Tier sprite by stars; classic NES invuln-blink uses the
        // damaged frame as the second blink half.
        val invuln = time < shieldUntil
        val showDamaged = invuln && ((time * 7.5f).toInt() % 2 == 0) && sprites.containsKey("player_damaged")
        if (showDamaged) {
            rotate(degrees, pivot) { sprite("player_damaged", x, y, s, s) }
            return
        }
        val tier = when (stars) { 1 -> "scout"; 2 -> "teal"; 3 -> "heavy"; else -> null }
        val tierName = tier?.let { "player_${it}_up" }
        if (tierName != null && sprites.containsKey(tierName)) {
            rotate(degrees, pivot) { sprite(tierName, x, y, s, s) }
        } else {
            sprite("player_$dirPart", x, y, s, s)
        }
    }

    private fun DrawScope.drawPowerUp(p: PowerUp) {
        val ay = p.y + boardY
        val pulse = (sin(time * 8f) * 0.3f + 0.7f)
        drawRect(Color.Black.copy(alpha = 0.5f * pulse), topLeft = Offset(p.x - 2f, ay - 2f), size = Size(tile + 4f, tile + 4f))
        // Base sprite + mini-glyph in the top-right corner so CLOCK ≠ BOMB
        // and SHOVEL ≠ TANK even before pickup (4 base sprites cover 6
        // power-ups — see asset list).
        val (name, glyph, glyphColor) = when (p.kind) {
            Power.HELMET -> Triple("power_shield",    "H",   Color(0xFF88FF88))
            Power.CLOCK  -> Triple("power_emp",       "Z",   Color(0xFF88CCFF))
            Power.SHOVEL -> Triple("power_repair",    "S",   Color(0xFFCCCCCC))
            Power.STAR   -> Triple("power_overdrive", "★",  Color(0xFFFFCC00))
            Power.BOMB   -> Triple("power_emp",       "B",   Color(0xFFFF6666))
            Power.TANK   -> Triple("power_repair",    "+1",  Color(0xFFFFFFFF))
        }
        sprite(name, p.x, ay, tile, tile)
        drawDosText(glyph, x = p.x + tile - 3f, y = ay + 12f, color = glyphColor, size = 12f, bold = true, align = TextAlign.RIGHT)
    }

    private fun DrawScope.drawTitle(ctx: GameContext) {
        drawRect(Color(0xFF07131B))
        for (i in 0 until 70) {
            val x = ((i * 83 + 17) % boardSize.toInt()).toFloat()
            val y = ((i * 137 + 31) % virtualHeight.toInt()).toFloat()
            drawRect(Color(0xFF0F3A46), Offset(x, y), Size(2f, 2f))
        }
        sprite("player_up", boardSize / 2f - 72f, 70f, 144f, 144f)
        drawDosText("STEEL FRONTIER", boardSize / 2f, 252f, Color(0xFFFFB02E), 34f, true, TextAlign.CENTER)
        drawDosText(ctx.tr("ORIGINAL TANK ARCADE", "ОРИГИНАЛЬНАЯ ТАНКОВАЯ АРКАДА"), boardSize / 2f, 282f, Color(0xFF55E6E6), 13f, true, TextAlign.CENTER)
        titleButton(330f, ctx.tr("CAMPAIGN  35 STAGES", "КАМПАНИЯ  35 ЭТАПОВ"), Color(0xFF126B63))
        titleButton(400f, ctx.tr("LEVEL EDITOR", "РЕДАКТОР УРОВНЕЙ"), Color(0xFF784A16))
        drawDosText(ctx.tr("Defend the command core. Break the frontier.", "Защити командное ядро. Прорви рубеж."), boardSize / 2f, 500f, Color.White, 12f, false, TextAlign.CENTER)
    }

    private fun DrawScope.titleButton(y: Float, text: String, color: Color) {
        drawRect(Color.Black, Offset(70f, y + 4f), Size(boardSize - 140f, 48f))
        drawRect(color, Offset(70f, y), Size(boardSize - 140f, 48f))
        drawDosText(text, boardSize / 2f, y + 31f, Color.White, 16f, true, TextAlign.CENTER)
    }

    private fun DrawScope.drawEditor() {
        drawRect(Color(0xFF111820))
        val paletteW = boardSize / editorTiles.size
        for (i in editorTiles.indices) {
            val x = i * paletteW
            drawEditorTile(editorTiles[i], x, 0f, paletteW)
            if (editorTile == editorTiles[i]) drawRect(Color(0xFFFFB02E), Offset(x, 0f), Size(paletteW, 4f))
        }
        drawDosText("CLEAR", 60f, 55f, Color(0xFFFF8A65), 11f, true, TextAlign.CENTER)
        drawDosText("TEST", boardSize / 2f, 55f, Color(0xFF7CFFB2), 11f, true, TextAlign.CENTER)
        drawDosText("MENU", boardSize - 60f, 55f, Color(0xFF80D8FF), 11f, true, TextAlign.CENTER)
        for (r in 0 until grid) for (c in 0 until grid) {
            drawEditorTile(editorBoard[r][c], c * tile, boardY + r * tile, tile)
            drawRect(Color.White.copy(alpha = 0.08f), Offset(c * tile, boardY + r * tile), Size(tile, 1f))
        }
    }

    private fun DrawScope.drawEditorTile(code: Int, x: Float, y: Float, size: Float) {
        val name = when (code) {
            1 -> "tile_masonry"; 2 -> "tile_titanium"; 3 -> "tile_water"; 4 -> "tile_canopy"
            5 -> "tile_ice"; 6 -> "tile_sand"; 7 -> "tile_drain"; 8 -> "tile_fence"
            9 -> "command_core"; 10 -> "tile_cracked_titanium"; else -> null
        }
        drawRect(Color.Black, Offset(x, y), Size(size, size))
        if (name != null) sprite(name, x, y, size, size)
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        when (mode) {
            Mode.TITLE -> when {
                y in 330f..378f -> restart()
                y in 400f..448f -> startEditor()
            }
            Mode.EDITOR -> {
                if (y < 38f) {
                    val paletteW = boardSize / editorTiles.size
                    val i = (x / paletteW).toInt().coerceIn(editorTiles.indices)
                    editorTile = editorTiles[i]
                } else if (y < boardY) {
                    when {
                        x < 130f -> for (r in 0 until grid) for (c in 0 until grid) editorBoard[r][c] = 0
                        x < boardSize - 130f -> testEditorMap()
                        else -> { mode = Mode.TITLE; controlsMode.value = GameControls.NONE }
                    }
                } else paintEditor(x, y)
            }
            else -> Unit
        }
    }

    override fun onPointerMove(x: Float, y: Float, id: Long, ctx: GameContext) {
        if (mode == Mode.EDITOR && y >= boardY) paintEditor(x, y)
    }

    private fun paintEditor(x: Float, y: Float) {
        val c = (x / tile).toInt()
        val r = ((y - boardY) / tile).toInt()
        if (r in 0 until grid && c in 0 until grid) editorBoard[r][c] = editorTile
    }

    override fun onBack(ctx: GameContext): Boolean {
        if (mode == Mode.TITLE) return false
        mode = if (mode == Mode.EDIT_TEST) Mode.EDITOR else Mode.TITLE
        controlsMode.value = GameControls.NONE
        return true
    }
}
