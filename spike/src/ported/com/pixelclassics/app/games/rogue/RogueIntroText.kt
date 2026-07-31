package com.pixelclassics.app.games.rogue

/**
 * Long-form campfire intro for DUNGEON — narrated by an ASCII '@'
 * character living inside a PDP-11 terminal in Berkeley, 1980.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
═════════════════════════════════════════
   PDP-11/70  ::  BERKELEY  ::  1980.05
   ROGUE.MEM/TAIL  --  STDIN ATTACHED
═════════════════════════════════════════
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> @

> That's me. I'm an at-sign on a
> terminal. I have been an at-sign on
> a terminal since the spring of 1980.

> Let me tell you who built me, where
> I live, and why I have ASCII children
> in every modern game franchise you
> have ever heard of.

> ── PART 1: THE PDP-11 BASEMENT ──

> University of California, Berkeley.
> Computer Science Department. Basement
> of Cory Hall. Three undergraduates —
> Michael Toy, Glenn Wichman, Ken
> Arnold — share a VT100 terminal
> connected to a PDP-11/70 mainframe
> running BSD UNIX.

> Toy and Wichman, two years earlier
> as freshmen at the University of
> California, Santa Cruz, had spent
> hundreds of hours playing a text
> adventure called Adventure (Crowther
> and Woods, 1976). They had loved
> Adventure but found it frustrating
> that you could not REPLAY it
> meaningfully — once you knew the
> map, the game was solved.

> They wondered: what if every time
> you played, the dungeon was
> DIFFERENT? Re-generated randomly?
> Same rules, same monsters, same
> items, but a brand-new floor plan?

> They started writing me on the VT100
> in their dorm room. Toy did the C
> code. Wichman did the level
> generation algorithm. Ken Arnold,
> the systems wizard, later wrote the
> CURSES library that I used to draw
> myself on the terminal — and which
> would go on to underpin half the
> world's terminal applications for
> the next forty years.

> I was finished in early 1980. They
> compiled me and submitted me to the
> BSD distribution. By 1984 I was
> shipping with every BSD UNIX system
> in the world.

> ── PART 2: WHAT I AM ──

> I am five floors of randomly-
> generated dungeon. Each floor has
> between four and nine rectangular
> rooms, connected by corridors.
> Floors contain MONSTERS (letters),
> POTIONS, GOLD, and STAIRS (>
> descending to the next floor).

> On floor five, somewhere, there is
> an AMULET. Pick it up. Walk back up
> to floor one. Exit the dungeon. You
> win.

> Or — die at any point. The dungeon
> erases. Restart from scratch with a
> brand-new five-floor maze.

> No save game. No load. No reset.
> If your hit points reach zero, you
> are gone. The map you learned, the
> monsters you slew, the gold you
> hoarded — all of it returns to
> entropy. Begin again.

> This is called PERMADEATH. I
> invented it.

> ── PART 3: THE TURN-BASED CLOCK ──

> The game is turn-based, not real-
> time. Every time YOU take an action
> — move one square, attack, drink a
> potion, descend stairs — EVERY
> MONSTER also takes one action. The
> whole world tick-tocks forward in
> lockstep with your decisions.

> This gives you infinite thinking
> time on any single turn, but every
> decision is permanent.

> ── PART 4: THE OTHER MAJOR THING ──

> I was open-sourced under BSD in
> 1986, after the team graduated. The
> source code went onto the BSD
> Software Distribution tape. Anyone
> could read me, modify me, build
> derivatives.

> What followed was the explosion of
> an entire genre of games that
> historians now call "roguelikes" —
> games that share my key features:
> procedurally-generated maps, turn-
> based movement, permadeath, ASCII
> or grid graphics, deep mechanical
> complexity.

> The direct descendants:
>   NETHACK (1987) — me, with seven
>     times the complexity.
>   ANGBAND (1990) — me, in Middle-
>     earth.
>   ADOM (1994) — me, with character
>     classes, races, alignments.

> The spiritual descendants —
> commercial mass-market success:
>   DIABLO (1996) — Blizzard's iconic
>     action-RPG. David Brevik calls
>     it "Rogue with graphics".
>   DWARF FORTRESS (2006) —
>     civilisation simulator.
>   SPELUNKY (2009), FTL (2012),
>   HADES (2020), SLAY THE SPIRE
>   (2019), DEAD CELLS (2018) — all
>   of them carry a thread of my DNA.

> If you have ever played a game with
> "randomly generated", "permadeath",
> or "run-based" in its marketing
> copy, you have played one of my
> children.

> ── PART 5: HOW TO READ ME ──

> The screen below is a terminal. The
> walls are # (hash). The floor is .
> (dot). The corridors are also .
> connected between rooms. YOU are @
> (at-sign). MONSTERS are uppercase
> letters. ITEMS are punctuation.

> WALK INTO a monster to attack it.
> WALK ONTO an item to pick it up.
> WALK INTO > (stairs down) to descend.
> WALK INTO & (the amulet) to pick it
> up, then walk back UP to floor one
> via the stairs to win.

> Five floors. Permadeath. No load.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> ROGUE is open source under BSD
> licence. We call our tribute DUNGEON
> only because the modern game market
> is full of "Rogue [something]" titles
> (Rogue Legacy, Rogue Heroes, Rogue
> Knight, etc.) and we did not want
> to add to the confusion. Toy,
> Wichman, and Arnold — wherever they
> are in 2026 — thank you. We will
> always speak your names at the
> campfire.
""".trimIndent()

private val BODY_RU: String = """
> @

> Это я. Я — собачка на
> терминале. Я собачка на
> терминале с весны 1980-го.

> Расскажу тебе, кто меня
> построил, где я живу, и
> почему у меня ASCII-дети
> в каждой современной
> игровой франшизе, о
> которой ты слышал.

> ── ЧАСТЬ 1: ПОДВАЛ С PDP-11 ──

> Калифорнийский университет,
> Беркли. Кафедра Computer
> Science. Подвал Cory Hall.
> Три студента — Майкл Той,
> Гленн Уикман, Кен Арнольд —
> делят VT100-терминал,
> подключённый к мейнфрейму
> PDP-11/70 с BSD UNIX.

> Той и Уикман двумя годами
> ранее, ещё первокурсниками
> в Калифорнийском
> университете в
> Санта-Крузе, провели сотни
> часов в текстовом
> приключении Adventure
> (Кроутер и Вудс, 1976).
> Они любили Adventure, но
> их расстраивало, что её
> нельзя ОСМЫСЛЕННО переигрывать
> — раз знаешь карту, игра
> решена.

> Они задумались: что если
> каждый раз, когда играешь,
> подземелье — ДРУГОЕ?
> Сгенерировано заново
> случайно? Те же правила,
> те же монстры, те же
> предметы, но совершенно
> новая планировка?

> Они начали писать меня на
> VT100 в общежитии. Той
> писал C-код. Уикман —
> алгоритм генерации
> уровней. Кен Арнольд,
> системный волшебник,
> позже написал библиотеку
> CURSES, которой я
> рисовал себя на
> терминале — и которая
> станет основой половины
> мировых терминальных
> приложений на следующие
> сорок лет.

> Меня закончили в начале
> 1980-го. Скомпилировали
> и подали в BSD-дистрибутив.
> К 1984-му я поставлялся с
> каждой BSD UNIX-системой в
> мире.

> ── ЧАСТЬ 2: ЧТО Я ──

> Я — пять этажей случайно
> сгенерированного
> подземелья. На каждом
> этаже от четырёх до
> девяти прямоугольных
> комнат, соединённых
> коридорами. На этажах —
> МОНСТРЫ (буквы), ЗЕЛЬЯ,
> ЗОЛОТО и ЛЕСТНИЦЫ (>
> вниз на следующий этаж).

> На пятом этаже где-то
> есть АМУЛЕТ. Подбери его.
> Вернись наверх на первый
> этаж. Выйди из подземелья.
> Победа.

> Или — умри в любой
> момент. Подземелье
> стирается. Начинаешь с
> нуля с новым
> пятиэтажным лабиринтом.

> Сейв нет. Загрузки нет.
> Сброса нет. Если HP
> ушли в ноль — тебя нет.
> Карта, что ты выучил,
> монстры, что убил,
> золото, что копил — всё
> возвращается в энтропию.
> Начинай заново.

> Это называется
> ПЕРМАСМЕРТЬ. Её
> изобрёл я.

> ── ЧАСТЬ 3: ХОДОВОЙ ТАЙМЕР ──

> Игра пошаговая, не
> реального времени. Каждый
> раз, когда ТЫ делаешь
> действие — двинул на
> клетку, атаковал, выпил
> зелье, спустился по
> лестнице — КАЖДЫЙ МОНСТР
> тоже делает одно действие.
> Весь мир тик-такает в
> такт с твоими решениями.

> Это даёт тебе бесконечное
> время на размышление на
> любом ходу, но каждое
> решение необратимо.

> ── ЧАСТЬ 4: ДРУГОЕ ВАЖНОЕ ──

> Меня открыли под BSD в
> 1986-м, после выпуска
> команды. Исходник попал
> на ленту BSD Software
> Distribution. Любой мог
> меня читать, изменять,
> делать производные.

> Что последовало — взрыв
> целого жанра, который
> историки сейчас зовут
> «рогаликами» — игры,
> разделяющие мои ключевые
> черты: процедурно
> сгенерированные карты,
> пошаговое движение,
> пермасмерть, ASCII или
> сеточная графика,
> глубокая механическая
> сложность.

> Прямые потомки:
>   NETHACK (1987) — я с
>     семикратной сложностью.
>   ANGBAND (1990) — я в
>     Средиземье.
>   ADOM (1994) — я с
>     классами, расами,
>     мировоззрениями.

> Духовные потомки —
> коммерческий
> массмаркет-успех:
>   DIABLO (1996) — культовая
>     action-RPG от Blizzard.
>     Дэвид Бревик зовёт её
>     «Rogue с графикой».
>   DWARF FORTRESS (2006) —
>     симулятор цивилизации.
>   SPELUNKY (2009), FTL
>   (2012), HADES (2020),
>   SLAY THE SPIRE (2019),
>   DEAD CELLS (2018) — все
>   они несут нить моей ДНК.

> Если ты когда-либо играл
> в игру с
> «процедурно сгенерированной»,
> «пермасмертью» или
> «забегом» в
> маркетинговом тексте —
> ты играл одного из моих
> детей.

> ── ЧАСТЬ 5: КАК МЕНЯ ЧИТАТЬ ──

> Экран ниже — терминал.
> Стены — # (решётка). Пол
> — . (точка). Коридоры —
> тоже . соединяющие
> комнаты. ТЫ — @
> (собачка). МОНСТРЫ —
> заглавные буквы.
> ПРЕДМЕТЫ — пунктуация.

> ИДИ В монстра, чтобы
> атаковать. ИДИ НА предмет,
> чтобы поднять. ИДИ В >
> (лестница вниз), чтобы
> спуститься. ИДИ В &
> (амулет), чтобы поднять,
> потом обратно ВВЕРХ на
> первый этаж по лестницам
> — победа.

> Пять этажей. Пермасмерть.
> Загрузки нет.

> ── СНОСКА У КОСТРА ──

> ROGUE — open source под
> BSD-лицензией. Наша
> дань называется DUNGEON
> только потому, что
> современный игровой
> рынок полон «Rogue
> [что-то]» тайтлов (Rogue
> Legacy, Rogue Heroes,
> Rogue Knight и т.д.), и
> мы не хотели добавлять
> путаницы. Той, Уикман и
> Арнольд — где бы вы ни
> были в 2026-м — спасибо.
> Мы всегда будем
> произносить ваши имена у
> костра.
""".trimIndent()

val ROGUE_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val ROGUE_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickRogueIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> ROGUE_INTRO_RU
    else -> ROGUE_INTRO_EN
}

val ROGUE_INTRO_TEXT: String = ROGUE_INTRO_EN
