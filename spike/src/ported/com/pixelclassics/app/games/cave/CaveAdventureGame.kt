package com.pixelclassics.app.games.cave

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.pixelclassics.app.R
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.ui.theme.TextAlign
import com.pixelclassics.app.ui.theme.drawDosText
import kotlin.math.max

/**
 * COLOSSAL CAVE ADVENTURE — chunky, compact homage to Crowther 1976 /
 * Woods 1977. Pure text. 20 hand-written locations, a parser that
 * understands {verb} + optional {noun}, a torch with battery, a few
 * traps and four treasures. Wins when all four are returned to the
 * entrance pile.
 *
 * Controls: type the command by tapping VERB chips at the bottom +
 * NOUN chips. No keyboard — every command can be assembled from chips.
 * (Mobile-friendly interactive fiction.)
 *
 * Save is hardcore: progress auto-saves on dispose; one TITLE prompt
 * offers «CONTINUE / NEW».
 *
 * Localisation: the world model speaks canonical tokens (verb tokens,
 * dir tokens, item ids); every player-visible label and line is picked
 * live via the launcher language (ctx.lang) — EN/RU sides inline.
 */
class CaveAdventureGame : Game {
    override val id: String = "cave"
    override val titleResId: Int = R.string.game_cave
    override val backgroundColor: Color = Color.Black
    override val introText: String = CAVE_QUEST_INTRO_EN
    override fun introTextFor(lang: String): String = pickCaveIntro(lang)
    override val introStyle: com.pixelclassics.app.ui.game.IntroStyle =
        com.pixelclassics.app.ui.game.IntroStyle.ASCII_DOS
    override fun helpLinesFor(lang: String): List<String> = if (lang == "ru") listOf(
        "Собирай команду из фишек ГЛАГОЛ + СУЩЕСТВИТЕЛЬНОЕ — мышью",
        "ИДИ/ВЗЯТЬ/ИСПОЛЬЗОВАТЬ/ОСМОТРЕТЬ · 20 комнат, 4 сокровища, ловушки",
        "Победа = принести все 4 сокровища ко входу в пещеру",
    ) else listOf(
        "Click VERB + NOUN chips to issue commands",
        "GO/TAKE/USE/LOOK · 20 rooms, 4 treasures, hidden traps",
        "Win = bring all 4 treasures back to the cave entrance",
    )
    override val helpLines: List<String> = helpLinesFor("en")

    private val W = 720f
    private val H = 540f
    override val virtualWidth: Float get() = W
    override val virtualHeight: Float get() = H

    // ── Live language ────────────────────────────────────────────────────
    private var lastCtx: GameContext? = null
    private fun ru(): Boolean = lastCtx?.lang == "ru"
    private fun t(en: String, ru: String): String = if (ru()) ru else en

    // ── World ────────────────────────────────────────────────────────────
    private data class Loc(
        val id: String,
        val nameEn: String, val nameRu: String,
        val descEn: String, val descRu: String,
        val exits: Map<String, String>,         // dir token → loc id (NORTH/SOUTH/EAST/WEST/UP/DOWN/IN/OUT)
        val items: MutableList<String> = mutableListOf(),
        val notes: String? = null,
    )
    private fun Loc.name() = if (ru()) nameRu else nameEn
    private fun Loc.desc() = if (ru()) descRu else descEn

    private data class Item(
        val id: String, val nameEn: String, val nameRu: String,
        val isTreasure: Boolean = false, val portable: Boolean = true,
    )
    private fun Item.name() = if (ru()) nameRu else nameEn

    private val items = mutableMapOf(
        "lamp" to Item("lamp", "brass lantern", "латунный фонарь"),
        "battery" to Item("battery", "spare battery", "запасная батарейка"),
        "axe" to Item("axe", "rusty axe", "ржавый топор"),
        "rope" to Item("rope", "coil of rope", "бухта верёвки"),
        "rod" to Item("rod", "black star-tipped rod", "чёрный стержень со звездой"),
        "key" to Item("key", "large bronze key", "большой бронзовый ключ"),
        "bottle" to Item("bottle", "glass flask of water", "стеклянная фляга с водой"),
        "food" to Item("food", "thin sandwich", "тощий бутерброд"),
        "gold_nugget" to Item("gold_nugget", "gold nugget", "золотой самородок", isTreasure = true),
        "silver_bar" to Item("silver_bar", "silver bar", "серебряный слиток", isTreasure = true),
        "diamond" to Item("diamond", "sharp-edged diamond", "острый алмаз", isTreasure = true),
        "emerald" to Item("emerald", "heavy emerald", "тяжёлый изумруд", isTreasure = true),
    )

    private val locs: LinkedHashMap<String, Loc> = linkedMapOf()

    init {
        fun add(loc: Loc) { locs[loc.id] = loc }
        add(Loc("hut", "Forest hut", "Лесная хижина",
            "A small wooden hut. A path leads EAST into the thicket. This is a safe place — you pile up the treasures you find here.",
            "Маленькая деревянная хижина. Из неё ведёт тропа на ВОСТОК, в чащу. Это безопасное место — здесь ты складываешь найденные сокровища.",
            mapOf("EAST" to "grove", "OUT" to "grove")))
        add(Loc("grove", "Dark grove", "Тёмная роща",
            "Under the tall pines you can barely see the sky. EAST — a wide crack in the rock. WEST — back to the hut.",
            "Под высокими соснами почти не видно неба. ВОСТОК — большая трещина в скале. ЗАПАД — обратно к хижине.",
            mapOf("EAST" to "crack", "WEST" to "hut"),
            items = mutableListOf("lamp", "battery")))
        add(Loc("crack", "Crack in the rock", "Трещина в скале",
            "A narrow slit you can squeeze down through. The walls are damp. WEST — back to the grove.",
            "Узкая щель, в которую можно протиснуться вниз. Стены влажные. ЗАПАД — вернуться к роще.",
            mapOf("DOWN" to "antechamber", "WEST" to "grove")))
        add(Loc("antechamber", "Cave antechamber", "Предбанник пещеры",
            "A tall stone hall. UP — the way out. To the south — a low passage. To the east — a stone staircase.",
            "Высокий каменный зал. ВВЕРХ — выход. На юге — низкий ход. На востоке — каменная лестница.",
            mapOf("UP" to "crack", "SOUTH" to "low_passage", "EAST" to "stairs"),
            items = mutableListOf("axe")))
        add(Loc("low_passage", "Low passage", "Низкий ход",
            "You have to crawl. It is muddy. NORTH — back to the hall. SOUTH — it widens into a room of glowing crystals.",
            "Приходится ползти. Грязно. СЕВЕР — назад в зал. ЮГ — расширяется в комнату со светящимися кристаллами.",
            mapOf("NORTH" to "antechamber", "SOUTH" to "crystal_room")))
        add(Loc("crystal_room", "Crystal hall", "Кристальный зал",
            "The walls sparkle with amethysts. Something lies on the floor. Passages lead EAST and back NORTH.",
            "Стены сверкают аметистами. На полу что-то лежит. Из зала ведут ходы на ВОСТОК и обратно на СЕВЕР.",
            mapOf("NORTH" to "low_passage", "EAST" to "underground_river"),
            items = mutableListOf("rope", "emerald")))
        add(Loc("stairs", "Stone staircase", "Каменная лестница",
            "Worn steps lead DOWN. WEST — back to the hall.",
            "Истёртые ступени ведут ВНИЗ. ЗАПАД — обратно в зал.",
            mapOf("DOWN" to "treasury_door", "WEST" to "antechamber")))
        add(Loc("treasury_door", "Bronze door", "Бронзовая дверь",
            "The door is shut with an enormous lock. There are scratches on the floor — somebody walks here. UP — back up the stairs. SOUTH — a narrow way around. To enter you need a key; use OPEN DOOR.",
            "Дверь заперта на огромный замок. На полу видны царапины — здесь явно ходят. ВВЕРХ — обратно по лестнице. ЮГ — узкий обход. Чтобы войти — нужен ключ; используй ОТКРЫТЬ ДВЕРЬ.",
            mapOf("UP" to "stairs", "SOUTH" to "bat_cave"),
            notes = "DOOR"))
        add(Loc("treasury", "Treasury", "Сокровищница",
            "A magnificent hall. Treasure lies on a stone pedestal. WEST — back through the door.",
            "Великолепный зал. На каменном постаменте лежит сокровище. ЗАПАД — назад через дверь.",
            mapOf("WEST" to "treasury_door"),
            items = mutableListOf("gold_nugget", "silver_bar")))
        add(Loc("bat_cave", "Bat cave", "Пещера летучих мышей",
            "Hundreds of bats hang overhead. They sleep — as long as you are quiet. EAST — another passage. NORTH — back to the door.",
            "Над головой висят сотни летучих мышей. Они спят — пока ты тих. На ВОСТОК — ещё один ход. На СЕВЕР — назад к двери.",
            mapOf("NORTH" to "treasury_door", "EAST" to "narrow_bridge"),
            items = mutableListOf("key")))
        add(Loc("narrow_bridge", "Narrow stone bridge", "Узкий каменный мост",
            "The bridge spans a chasm. No bottom in sight below. EAST — onward. WEST — the bat cave. Crossing is terrifying — you need the ROD for balance.",
            "Мост перекинут через пропасть. Внизу не видно дна. ВОСТОК — продолжение. ЗАПАД — пещера летучих мышей. Перейти мост страшно — нужен СТЕРЖЕНЬ для равновесия.",
            mapOf("WEST" to "bat_cave", "EAST" to "deep_chamber"),
            notes = "BRIDGE"))
        add(Loc("deep_chamber", "Deep chamber", "Глубокий зал",
            "Echoing sounds. It is cold here. NORTH — a burrow, SOUTH — a narrow way to an underground river.",
            "Звуки эха. Здесь холодно. На СЕВЕР — нора, на ЮГ — узкий ход к подземной реке.",
            mapOf("WEST" to "narrow_bridge", "NORTH" to "dragon_lair", "SOUTH" to "underground_river"),
            items = mutableListOf("food")))
        add(Loc("dragon_lair", "Dragon's lair", "Логово дракона",
            "A huge green dragon sleeps on a pile of treasure. Wake it — and it's over. Use HIT DRAGON, if you are sure.",
            "Огромный зелёный дракон спит на куче сокровищ. Если разбудить — конец. Используй УДАРИТЬ ДРАКОНА, если уверен.",
            mapOf("SOUTH" to "deep_chamber"),
            items = mutableListOf("diamond"),
            notes = "DRAGON"))
        add(Loc("underground_river", "Underground river", "Подземная река",
            "Dark water flows through the hall. To the west it vanishes into a hole. NORTH — the crystal hall, EAST — the deep chamber. DOWN — dive into the river (risky).",
            "Тёмная вода течёт через зал. На западе она исчезает в дыре. На СЕВЕР — кристальный зал, на ВОСТОК — глубокий зал. ВНИЗ — нырнуть в реку (рискованно).",
            mapOf("NORTH" to "crystal_room", "EAST" to "deep_chamber", "DOWN" to "submerged_cave")))
        add(Loc("submerged_cave", "Flooded cave", "Затопленная пещера",
            "Dark and wet. Without a lantern you are lost. UP — back along the current.",
            "Темно, мокро. Если у тебя нет фонаря — пропадёшь. ВВЕРХ — вернуться по течению.",
            mapOf("UP" to "underground_river"),
            items = mutableListOf("rod"),
            notes = "DARK"))
        add(Loc("end_of_road", "Dead end", "Конец прохода",
            "A wall. Dead end. Back EAST.",
            "Стена. Тупик. Назад на ВОСТОК.",
            mapOf("EAST" to "antechamber")))
    }

    // ── Player state ─────────────────────────────────────────────────────
    private var currentLoc = "hut"
    private val inventory = mutableListOf<String>()
    private val transcript = mutableListOf<Line>()   // recent output (max ~40)
    private var lampOn = false
    private var lampLife = 200          // turns of life
    private var dragonDead = false
    private var bridgeBalanced = false
    private var doorOpen = false
    private var dead = false
    private var won = false
    private var turn = 0

    private data class Line(val text: String, val emphasis: Boolean = false)
    private fun say(text: String, emph: Boolean = false) { transcript += Line(text, emph); if (transcript.size > 40) transcript.removeAt(0) }

    // Verbs: canonical token + localized chip labels. execute() switches on
    // the token, so the language can change mid-game without breaking play.
    private data class Verb(val token: String, val en: String, val ru: String)
    private val verbList = listOf(
        Verb("GO", "GO", "ИДИ"),
        Verb("TAKE", "TAKE", "ВЗЯТЬ"),
        Verb("DROP", "DROP", "БРОСИТЬ"),
        Verb("LOOK", "LOOK", "ОСМОТРЕТЬ"),
        Verb("OPEN", "OPEN", "ОТКРЫТЬ"),
        Verb("LIGHT", "LIGHT", "ЗАЖЕЧЬ"),
        Verb("DOUSE", "DOUSE", "ПОГАСИТЬ"),
        Verb("USE", "USE", "ИСПОЛЬЗОВАТЬ"),
        Verb("EAT", "EAT", "СЪЕСТЬ"),
        Verb("HIT", "HIT", "УДАРИТЬ"),
        Verb("INVENTORY", "INVENTORY", "ИНВЕНТАРЬ"),
        Verb("SAVE", "SAVE", "СОХРАНИТЬ"),
    )
    private fun verbLabel(v: Verb) = if (ru()) v.ru else v.en

    private val dirTokens = listOf("NORTH", "SOUTH", "EAST", "WEST", "UP", "DOWN", "OUT")
    private val dirRu = mapOf(
        "NORTH" to "СЕВЕР", "SOUTH" to "ЮГ", "EAST" to "ВОСТОК", "WEST" to "ЗАПАД",
        "UP" to "ВВЕРХ", "DOWN" to "ВНИЗ", "OUT" to "ВЫЙТИ",
    )
    private fun dirLabel(token: String): String = if (ru()) dirRu[token] ?: token else token

    private var selectedVerb: String? = null    // verb token
    private var selectedNoun: String? = null    // noun token (dir token / item id / DOOR / DRAGON)

    private enum class Stage { CHOOSE, PLAY }
    private var stage = Stage.CHOOSE
    private var hasSave = false

    override fun init(ctx: GameContext) {
        lastCtx = ctx
        hasSave = ctx.settings.caveSave.isNotEmpty()
        stage = Stage.CHOOSE
    }

    override fun dispose() {
        lastCtx?.let { ctx ->
            // Persist only from actual play. Exiting from the CHOOSE/intro
            // screens must NOT overwrite a real save with a blank new-game
            // state (the world isn't loaded there yet).
            when {
                stage != Stage.PLAY -> Unit
                !dead && !won -> ctx.settings.caveSave = serialize()
                else -> ctx.settings.caveSave = ""
            }
        }
    }

    private fun serialize(): String {
        val sb = StringBuilder()
        sb.append("$currentLoc|$lampOn|$lampLife|$dragonDead|$bridgeBalanced|$doorOpen|$turn\n")
        sb.append(inventory.joinToString(",")).append('\n')
        for ((id, loc) in locs) sb.append("$id:${loc.items.joinToString(",")}").append('\n')
        return sb.toString()
    }

    private fun loadFrom(raw: String): Boolean = try {
        val lines = raw.lines()
        val head = lines[0].split('|')
        // A save referencing an unknown room (corruption / renamed id) must be
        // rejected here — accepting it means locs[currentLoc]!! NPEs later.
        if (head[0] !in locs) throw IllegalArgumentException("unknown room ${head[0]}")
        currentLoc = head[0]
        lampOn = head[1].toBoolean(); lampLife = head[2].toInt()
        dragonDead = head[3].toBoolean(); bridgeBalanced = head[4].toBoolean()
        doorOpen = head[5].toBoolean(); turn = head[6].toInt()
        inventory.clear()
        if (lines[1].isNotEmpty()) inventory.addAll(lines[1].split(','))
        for (i in 2 until lines.size) {
            val ln = lines[i]
            if (ln.isBlank()) continue
            val (id, itemList) = ln.split(':', limit = 2)
            val loc = locs[id] ?: continue
            loc.items.clear()
            if (itemList.isNotEmpty()) loc.items.addAll(itemList.split(','))
        }
        true
    } catch (_: Throwable) { false }

    private fun newGame() {
        currentLoc = "hut"; inventory.clear(); transcript.clear()
        lampOn = false; lampLife = 200; dragonDead = false; bridgeBalanced = false
        doorOpen = false; dead = false; won = false; turn = 0
        // Reset loc items.
        for ((id, srcLoc) in initialLocItems) {
            locs[id]?.items?.clear()
            locs[id]?.items?.addAll(srcLoc)
        }
        say(locs[currentLoc]!!.desc(), emph = true)
        say("")
    }
    private val initialLocItems: Map<String, List<String>> = locs.mapValues { it.value.items.toList() }

    // ── Command processor ────────────────────────────────────────────────
    private fun execute(verb: String, noun: String?) {
        if (dead || won) return
        turn++
        if (lampOn) {
            lampLife--
            if (lampLife <= 10 && lampLife > 0) say(t("The lantern starts to flicker — the battery is dying.", "Фонарь начинает мигать — батарейка садится."))
            if (lampLife <= 0) { lampOn = false; say(t("The lantern goes out.", "Фонарь погас.")) }
        }
        when (verb) {
            "GO" -> tryGo(noun)
            "TAKE" -> tryTake(noun)
            "DROP" -> tryDrop(noun)
            "LOOK" -> describe()
            "OPEN" -> tryOpen(noun)
            "LIGHT" -> { if ("lamp" in inventory) { lampOn = true; say(t("The lantern is lit.", "Фонарь зажжён.")) } else say(t("You have no lantern.", "У тебя нет фонаря.")) }
            "DOUSE" -> { if (lampOn) { lampOn = false; say(t("The lantern is doused.", "Фонарь погашен.")) } else say(t("The lantern is not burning anyway.", "Фонарь и так не горит.")) }
            "USE" -> tryUse(noun)
            "EAT" -> { if ("food" in inventory) { inventory.remove("food"); say(t("Sandwich eaten. No complaints about hunger.", "Бутерброд съеден. На голод не жалуешься.")) } else say(t("You have nothing edible.", "У тебя нет ничего съедобного.")) }
            "HIT" -> tryHit(noun)
            "INVENTORY" -> showInventory()
            "SAVE" -> { lastCtx?.settings?.caveSave = serialize(); say(t("Progress saved.", "Прогресс сохранён.")) }
        }
        // Auto-darkness check.
        val here = locs[currentLoc]!!
        if (here.notes == "DARK" && !lampOn) {
            say(t("Pitch darkness. One step — and you fall into an underground lake. THE END.",
                "Кромешная тьма. Шаг — и ты падаешь в подземное озеро. КОНЕЦ."), emph = true)
            dead = true
            lastCtx?.sound?.gameOver()
            lastCtx?.settings?.caveSave = ""
        }
        checkWin()
    }

    private fun tryGo(dirToken: String?) {
        if (dirToken == null) { say(t("Go where?", "Куда идти?")); return }
        val here = locs[currentLoc]!!
        val target = here.exits[dirToken]
        if (target == null) { say(t("You can't go that way.", "Туда не пройти.")); return }
        // Special gates.
        if (target == "treasury" && !doorOpen) { say(t("The door is locked.", "Дверь заперта.")); return }
        if (dirToken == "EAST" && currentLoc == "narrow_bridge" && !bridgeBalanced) {
            say(t("You stepped onto the bridge, lost your balance and fell. THE END.",
                "Ты шагнул на мост, потерял равновесие и упал. КОНЕЦ."), emph = true); dead = true; return
        }
        currentLoc = target
        say(locs[currentLoc]!!.name() + ". " + locs[currentLoc]!!.desc(), emph = true)
    }

    private fun tryTake(itemId: String?) {
        if (itemId == null) { say(t("Take what?", "Что взять?")); return }
        val here = locs[currentLoc]!!
        if (itemId !in here.items) { say(t("There is no such thing here.", "Здесь такого нет.")); return }
        here.items.remove(itemId)
        inventory += itemId
        say(t("Taken: ", "Взято: ") + (items[itemId]?.name() ?: itemId) + ".")
        // Special: dropping treasure at hut wins.
    }

    private fun tryDrop(itemId: String?) {
        if (itemId == null) { say(t("Drop what?", "Что бросить?")); return }
        if (itemId !in inventory) { say(t("You don't have that.", "У тебя такого нет.")); return }
        inventory.remove(itemId)
        locs[currentLoc]!!.items += itemId
        say(t("Put down: ", "Положено: ") + (items[itemId]?.name() ?: itemId) + ".")
    }

    private fun tryOpen(target: String?) {
        if (target == "DOOR") {
            if (currentLoc != "treasury_door") { say(t("There is no door to open here.", "Здесь нет двери, которую можно открыть.")) }
            else if (doorOpen) say(t("The door is already open.", "Дверь уже открыта."))
            else if ("key" in inventory) {
                doorOpen = true
                say(t("The key fits — the door creaks open to the EAST.", "Ключ подходит — дверь со скрипом открывается на ВОСТОК."))
                openDoor()
            } else say(t("The door is locked. You need a key.", "Дверь заперта. Нужен ключ."))
        } else say(t("That does not open.", "Это не открывается."))
    }
    private fun openDoor() {
        // Replace exits with mutable one — add EAST.
        val l = locs["treasury_door"]!!
        val newExits = l.exits.toMutableMap()
        newExits["EAST"] = "treasury"
        locs["treasury_door"] = l.copy(exits = newExits)
    }

    private fun tryUse(itemId: String?) {
        if (itemId == null) { say(t("Use what?", "Что использовать?")); return }
        if (itemId !in inventory) { say(t("You don't have that.", "У тебя такого нет.")); return }
        when (itemId) {
            "battery" -> { lampLife = 200; inventory.remove("battery"); say(t("Battery inserted. The lantern is at full strength again.", "Батарейка вставлена. Фонарь снова полон сил.")) }
            "rod" -> {
                if (currentLoc == "narrow_bridge") { bridgeBalanced = true; say(t("The black star-tipped rod grants uncanny balance. Now the bridge can be crossed.", "Чёрный стержень со звездой даёт удивительное равновесие. Теперь мост можно перейти.")) }
                else say(t("The star shimmers, but nothing happens.", "Звезда мерцает, но ничего не происходит."))
            }
            "rope" -> say(t("The rope will be useful later — nothing to tie it to yet.", "Верёвка пригодится позже — пока некуда привязать."))
            "axe" -> if (currentLoc == "dragon_lair" && !dragonDead) {
                if (kotlin.random.Random.nextDouble() < 0.5) { dragonDead = true; say(t("Incredible — the axe sinks deep into the dragon's neck. It is dead.", "Невероятно — топор глубоко вошёл дракону в шею. Он мёртв.")) }
                else { say(t("The dragon woke up and burned you with a single breath. THE END.", "Дракон проснулся и сжёг тебя одним выдохом. КОНЕЦ."), emph = true); dead = true }
            } else say(t("There is nobody here to swing an axe at.", "Здесь не на кого размахивать топором."))
            "bottle" -> { say(t("The water is cool. Good.", "Вода прохладная. Хорошо.")) }
            else -> say(t("Nothing works with this item.", "С этим предметом ничего не получается."))
        }
    }

    private fun tryHit(target: String?) {
        if (target == "DRAGON" && currentLoc == "dragon_lair") {
            if ("axe" in inventory) tryUse("axe")
            else say(t("With bare hands? The dragon will devour you. You need a weapon.", "Голыми руками? Дракон тебя сожрёт. Нужно оружие."))
        } else say(t("That is pointless.", "Это бессмысленно."))
    }

    private fun describe() {
        val here = locs[currentLoc]!!
        say(here.name() + ". " + here.desc(), emph = true)
        if (here.items.isNotEmpty()) {
            say(t("You see: ", "Ты видишь: ") + here.items.joinToString(", ") { items[it]?.name() ?: it } + ".")
        }
        val exits = here.exits.keys.joinToString(", ") { dirLabel(it) }
        say(t("Exits: ", "Выходы: ") + exits + ".")
    }

    private fun showInventory() {
        if (inventory.isEmpty()) say(t("Your hands are empty.", "В руках пусто."))
        else say(t("You carry: ", "При тебе: ") + inventory.joinToString(", ") { items[it]?.name() ?: it } + ".")
        if ("lamp" in inventory) say(t("Lantern: ", "Фонарь: ") +
            (if (lampOn) t("burning", "горит") else t("doused", "погашен")) +
            " (" + max(0, lampLife) + t(" turns", " ходов") + ").")
    }

    private fun checkWin() {
        if (currentLoc == "hut") {
            val treasures = locs["hut"]!!.items.count { items[it]?.isTreasure == true }
            if (treasures >= 4) {
                won = true
                say(t("YOU HAVE BROUGHT ALL FOUR TREASURES HOME! Congratulations — the game is complete.",
                    "ТЫ ПРИНЁС ДОМОЙ ВСЕ ЧЕТЫРЕ СОКРОВИЩА! Поздравляю — игра пройдена."), emph = true)
                lastCtx?.sound?.win()
                lastCtx?.settings?.caveSave = ""
            }
        }
    }

    override fun update(dt: Float, ctx: GameContext) {
        lastCtx = ctx
    }

    // ── Drawing ──────────────────────────────────────────────────────────
    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        lastCtx = ctx
        drawRect(backgroundColor, topLeft = Offset.Zero, size = Size(W, H))
        if (stage == Stage.CHOOSE) { drawChoose(this); return@with }

        // Transcript area.
        val transcriptH = H - 200f
        drawRect(Color(0xFF000000), topLeft = Offset(8f, 8f), size = Size(W - 16f, transcriptH))
        drawRect(Color(0xFF55FFFF), topLeft = Offset(8f, 8f), size = Size(W - 16f, 1f))
        drawRect(Color(0xFF55FFFF), topLeft = Offset(8f, transcriptH + 8f - 1f), size = Size(W - 16f, 1f))
        // Render the last screenful of PHYSICAL lines. Entries wrap to 2-4
        // lines each; giving every entry a single fixed slot made wrapped
        // text overprint the entries below it.
        val lineH = 20f
        val maxLines = (transcriptH / lineH - 1f).toInt()
        val physical = mutableListOf<Pair<String, Boolean>>()
        for (ln in transcript) for (w in wrap(ln.text, 60)) physical += w to ln.emphasis
        val start = (physical.size - maxLines).coerceAtLeast(0)
        for (i in start until physical.size) {
            val (text, emph) = physical[i]
            val y = 24f + (i - start) * lineH
            val color = if (emph) Color(0xFFFF55FF) else Color(0xFFFFFFFF)
            drawDosText(text, x = 16f, y = y, color = color, size = 15f, bold = true)
        }

        // Location title at top.
        val here = locs[currentLoc]!!
        drawRect(Color(0xFFFF55FF), topLeft = Offset(0f, 0f), size = Size(W, 8f))
        drawDosText("📍 ${here.name()}    🔦 ${if (lampOn) "ON $lampLife" else "off"}    🎒 ${inventory.size}/12   ${t("turn", "ход")} $turn",
            x = W / 2f, y = 4f, color = Color(0xFFFF55FF), size = 10f, bold = true, align = TextAlign.CENTER)

        // Chips area — verbs + dirs/items as nouns.
        val chipsY = transcriptH + 20f
        drawChips(this, chipsY)
    }

    private fun wrap(text: String, width: Int): List<String> {
        if (text.length <= width) return listOf(text)
        val words = text.split(' ')
        val lines = mutableListOf<StringBuilder>()
        var cur = StringBuilder()
        for (w in words) {
            if (cur.length + w.length + 1 > width) { lines += cur; cur = StringBuilder() }
            if (cur.isNotEmpty()) cur.append(' ')
            cur.append(w)
        }
        if (cur.isNotEmpty()) lines += cur
        return lines.map { it.toString() }
    }

    // A chip shows a localized label but carries a canonical token, so the
    // draw pass and the command processor never depend on the display language.
    private data class Chip(val label: String, val token: String, val x: Float, val y: Float, val w: Float, val h: Float)

    private fun currentNouns(): List<Pair<String, String>> = buildList {
        for (d in dirTokens) if (locs[currentLoc]!!.exits.containsKey(d)) add(dirLabel(d) to d)
        for (id in locs[currentLoc]!!.items) add(itemLabel(id) to id)
        for (id in inventory) add("🎒" + itemLabel(id) to id)
        add(t("DOOR", "ДВЕРЬ") to "DOOR"); add(t("DRAGON", "ДРАКОНА") to "DRAGON")
    }

    // Single source of truth for chip rectangles — draw AND hit-test call this,
    // so a tap always lands on the chip you see. (The old code wrapped rows
    // differently in draw vs hit, so verbs overlapped and taps missed —
    // Юджин: «на старте ничего не смог сделать».)
    private fun layoutChips(baseY: Float): Pair<List<Chip>, List<Chip>> {
        val rowH = 28f; val pad = 6f
        val vchips = mutableListOf<Chip>()
        var x = 8f; var y = baseY
        for (v in verbList) {
            val label = verbLabel(v)
            val w = label.length * 8f + 18f
            if (x + w > W - 120f) { x = 8f; y += rowH + pad }   // leave the EXECUTE corner clear
            vchips += Chip(label, v.token, x, y, w, rowH)
            x += w + pad
        }
        val nchips = mutableListOf<Chip>()
        var nx = 8f; var ny = y + rowH + pad + 4f
        for ((label, token) in currentNouns()) {
            val w = label.length * 7.5f + 16f
            if (nx + w > W - 12f) { nx = 8f; ny += rowH + pad }
            nchips += Chip(label, token, nx, ny, w, rowH)
            nx += w + pad
        }
        return vchips to nchips
    }
    private fun execRect(baseY: Float) = Chip(t("RUN", "ВЫПОЛНИТЬ"), "RUN", W - 110f, baseY, 100f, 28f)

    private fun drawChips(scope: DrawScope, baseY: Float) = with(scope) {
        val (vchips, nchips) = layoutChips(baseY)
        for (c in vchips) {
            val isSel = c.token == selectedVerb
            drawRect(if (isSel) Color(0xFF55FFFF) else Color(0xFF000000), topLeft = Offset(c.x, c.y), size = Size(c.w, c.h))
            drawRect(Color(0xFFFF55FF), topLeft = Offset(c.x, c.y), size = Size(c.w, 1f))
            drawDosText(c.label, x = c.x + c.w / 2f, y = c.y + 18f, color = if (isSel) Color.White else Color(0xFFFF55FF), size = 12f, bold = true, align = TextAlign.CENTER)
        }
        for (c in nchips) {
            val isSel = c.token == selectedNoun
            drawRect(if (isSel) Color(0xFF55FFFF) else Color(0xFF000000), topLeft = Offset(c.x, c.y), size = Size(c.w, c.h))
            drawRect(Color(0xFF55FFFF), topLeft = Offset(c.x, c.y), size = Size(c.w, 1f))
            drawDosText(c.label, x = c.x + c.w / 2f, y = c.y + 18f, color = if (isSel) Color.White else Color(0xFF55FFFF), size = 11f, bold = true, align = TextAlign.CENTER)
        }
        val e = execRect(baseY)
        val canExec = selectedVerb != null && (!selectedVerbNeedsNoun() || selectedNoun != null) && !dead && !won
        drawRect(if (canExec) Color(0xFF55FFFF) else Color(0xFF000000), topLeft = Offset(e.x, e.y), size = Size(e.w, e.h))
        drawRect(Color(0xFFFFFFFF), topLeft = Offset(e.x, e.y), size = Size(e.w, 2f))
        drawDosText("RUN", x = e.x + e.w / 2f, y = e.y + 18f, color = Color.White, size = 12f, bold = true, align = TextAlign.CENTER)
        // Current assembled command, so it's obvious what EXECUTE will run.
        val vLabel = verbList.firstOrNull { it.token == selectedVerb }?.let { verbLabel(it) }
        val nLabel = selectedNoun?.let { tok -> nchips.firstOrNull { it.token == tok }?.label ?: tok }
        val cmd = (vLabel ?: "…") + (nLabel?.let { " → $it" } ?: "")
        drawDosText(cmd, x = e.x + e.w / 2f, y = baseY + 44f, color = Color(0xFFFFFFFF), size = 10f, bold = true, align = TextAlign.CENTER)
    }

    private fun selectedVerbNeedsNoun(): Boolean = when (selectedVerb) {
        "LOOK", "INVENTORY", "SAVE", "DOUSE", "LIGHT" -> false
        else -> true
    }

    private fun itemLabel(id: String): String = items[id]?.name()?.split(' ')?.last()?.uppercase() ?: id.uppercase()

    private fun drawChoose(scope: DrawScope) = with(scope) {
        drawDosText("ADVENTURE", x = W / 2f, y = H * 0.18f, color = Color(0xFFFF55FF), size = 36f, bold = true, align = TextAlign.CENTER)
        drawDosText(t("Crowther / Woods 1976-77 — a brief tribute", "Краутер / Вудс 1976-77 — краткий оммаж"),
            x = W / 2f, y = H * 0.26f, color = Color(0xFF55FFFF), size = 12f, bold = true, align = TextAlign.CENTER)
        drawDosText(t("20 rooms. 12 items. 4 treasures.", "20 комнат. 12 предметов. 4 сокровища."),
            x = W / 2f, y = H * 0.38f, color = Color.White, size = 13f, align = TextAlign.CENTER)
        drawDosText(t("Bring all four back to the entrance.", "Принеси все четыре обратно ко входу."),
            x = W / 2f, y = H * 0.44f, color = Color.White, size = 13f, align = TextAlign.CENTER)
        drawDosText(t("No keyboard — assemble a command from a verb", "Клавиатура не нужна — собери команду из глагола"),
            x = W / 2f, y = H * 0.54f, color = Color(0xFFFFFFFF), size = 12f, align = TextAlign.CENTER)
        drawDosText(t("and a noun below, then click RUN.", "и существительного внизу, затем нажми RUN."),
            x = W / 2f, y = H * 0.59f, color = Color(0xFFFFFFFF), size = 12f, align = TextAlign.CENTER)
        val cw = W * 0.30f
        drawRect(if (hasSave) Color(0xFF000000) else Color(0xFF000000),
            topLeft = Offset(W / 2f - cw - 10f, H * 0.74f), size = Size(cw, 44f))
        drawRect(if (hasSave) Color(0xFF55FFFF) else Color(0xFF000000),
            topLeft = Offset(W / 2f - cw - 10f, H * 0.74f), size = Size(cw, 2f))
        drawDosText(t("CONTINUE", "ПРОДОЛЖИТЬ"), x = W / 2f - cw / 2f - 10f, y = H * 0.74f + 28f,
            color = if (hasSave) Color(0xFF55FFFF) else Color(0xFF55FFFF), size = 14f, bold = true, align = TextAlign.CENTER)
        drawRect(Color(0xFF000000), topLeft = Offset(W / 2f + 10f, H * 0.74f), size = Size(cw, 44f))
        drawRect(Color(0xFFFF55FF), topLeft = Offset(W / 2f + 10f, H * 0.74f), size = Size(cw, 2f))
        drawDosText(t("NEW GAME", "НОВАЯ ИГРА"), x = W / 2f + cw / 2f + 10f, y = H * 0.74f + 28f,
            color = Color(0xFFFF55FF), size = 14f, bold = true, align = TextAlign.CENTER)
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        lastCtx = ctx
        if (stage == Stage.CHOOSE) {
            val cw = W * 0.30f
            if (hasSave && x in (W / 2f - cw - 10f)..(W / 2f - 10f) && y in (H * 0.74f)..(H * 0.74f + 44f)) {
                if (loadFrom(ctx.settings.caveSave)) { stage = Stage.PLAY; describe(); ctx.sound.menuSelect(); return }
            }
            if (x in (W / 2f + 10f)..(W / 2f + cw + 10f) && y in (H * 0.74f)..(H * 0.74f + 44f)) {
                ctx.settings.caveSave = ""; newGame(); stage = Stage.PLAY; ctx.sound.menuSelect(); return
            }
            return
        }
        if (dead || won) {
            // Tap to restart.
            ctx.settings.caveSave = ""
            stage = Stage.CHOOSE
            hasSave = false
            return
        }
        val transcriptH = H - 200f
        val chipsY = transcriptH + 20f
        // EXECUTE.
        val e = execRect(chipsY)
        if (x in e.x..(e.x + e.w) && y in e.y..(e.y + e.h)) {
            val v = selectedVerb ?: return
            if (selectedVerbNeedsNoun() && selectedNoun == null) return
            execute(v, selectedNoun)
            selectedVerb = null; selectedNoun = null
            ctx.sound.menuSelect()
            return
        }
        val (vchips, nchips) = layoutChips(chipsY)
        for (c in vchips) if (x in c.x..(c.x + c.w) && y in c.y..(c.y + c.h)) { selectedVerb = c.token; ctx.sound.click(); return }
        for (c in nchips) if (x in c.x..(c.x + c.w) && y in c.y..(c.y + c.h)) { selectedNoun = c.token; ctx.sound.click(); return }
    }
}
