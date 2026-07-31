package com.pixelclassics.app.games.riverraid

/**
 * Long-form campfire intro for RIVER RUN (River Raid). Told as a museum
 * placard at the Computer History Museum, dated 2022, in front of the
 * actual original Activision cartridge, captioned by Carol Shaw herself.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
═══════════════════════════════════════════
   ACTIVISION  ::  RIVER RAID  ::  1982
   DESIGNER · CAROL SHAW (b.1955)
   FIRST WOMAN CREDITED ON A CONSOLE TITLE
═══════════════════════════════════════════
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> [Museum placard, Computer History
>  Museum, Mountain View, California,
>  exhibit "Women in Early Game
>  Development", 2022.]

> Carol Shaw graduated from Berkeley
> in 1977 with a Master's in Computer
> Science. She joined Atari that same
> year. She was the first female
> programmer at Atari. She was the
> first female programmer in the
> American console-game industry.
> She was, for several years, the
> only one.

> Her first game at Atari was 3-D
> TIC-TAC-TOE for the Atari 2600
> (1980). It was a 4x4x4 cube of
> tic-tac-toe with rotating
> perspectives. It sold 200,000
> cartridges. Atari's executives
> were polite but unimpressed: "Carol,
> we appreciate your work, but our
> next big push is action games."

> Carol left Atari in 1980 and
> joined Activision, the new third-
> party studio founded by ex-Atari
> programmers a year earlier. At
> Activision she had freedom of
> design: the founders had escaped
> Atari precisely to give programmers
> creative ownership.

> She announced her next project to
> her colleagues: a vertically-
> scrolling shoot-em-up. The plane
> would fly DOWN a river. Hostile
> ground forces would appear from
> the riverbanks. Fuel barrels in
> the centre of the river could be
> picked up to refuel mid-flight.
> Bridges would block your path —
> you would have to bomb them to
> proceed.

> The team raised an objection: how
> are you going to render a winding
> river that goes on for hundreds
> of miles, in 4 kilobytes of
> cartridge ROM, on a machine with
> 128 bytes of RAM?

> Carol's answer was the river-
> generation algorithm she designed
> in late 1981, on a yellow legal
> pad in her apartment in Sunnyvale.

> ── PART 1: THE ALGORITHM ──

> The river is procedurally
> generated. Not randomly —
> deterministically. Every player's
> river is the SAME river. Carol used
> a Linear Feedback Shift Register
> (LFSR) — a hardware primitive that
> generates a long pseudo-random
> sequence from a small seed. She
> seeded it once at game start with
> a fixed value. The LFSR produced
> the river's width, the obstacle
> placement, the bridge spacing,
> the bank curvature, all
> downstream of one seed.

> 256 bytes of cartridge ROM held
> the river generator. The other
> 3.75 kilobytes held the player's
> plane, the enemy sprites, the
> sound generator, the score
> display.

> RIVER RAID's river, as a result,
> is approximately 200 kilometres
> long. Every player flies the same
> 200 kilometres. Veterans
> memorised landmarks: "the second
> bridge after the wide bend",
> "the helicopter cluster in the
> narrow canyon". The river became
> a SHARED MAP that every player
> across America was simultaneously
> learning.

> ── PART 2: THE GAME ──

> You pilot a small grey plane.
> The plane scrolls upward
> automatically as you fall down
> the river. The river is wider in
> some places, narrower in others.
> Banks are deadly: touch the bank
> at speed and your plane
> disintegrates.

> The CONTROL is unusually
> sophisticated for 1982:
>   - LEFT / RIGHT: steer the
>     plane horizontally (across
>     the river).
>   - UP: ACCELERATE the plane.
>     The river scrolls faster.
>     This is risky but covers
>     more ground per unit of
>     fuel.
>   - DOWN: DECELERATE the plane.
>     The river scrolls slower.
>     This burns more fuel per
>     unit of distance, but lets
>     you carefully thread narrow
>     canyons.
>   - FIRE: shoot machine guns
>     forward.

> Enemies: boats, helicopters,
> jets, fuel depots (don't shoot
> them, fly over to refuel), and
> bridges (must be bombed to
> continue).

> ── PART 3: THE OUTCOME ──

> RIVER RAID shipped Christmas 1982.
> Sold 1.5 million cartridges. Won
> Console Game of the Year from
> ELECTRONIC GAMES MAGAZINE. Carol
> won INFOWORLD's Game Designer of
> the Year.

> She was 27 years old.

> She continued to make games at
> Activision until 1984. Then she
> retired from the game industry.
> She was, by her later account,
> burned out. She had spent six
> straight years working 70-hour
> weeks. She wanted a life.

> Carol moved to working at
> Tandem Computers (later acquired
> by Compaq, then HP). She left
> tech entirely in the mid-1990s.
> She spent the next decades
> raising her family and pursuing
> quilting — she has won several
> prizes at national quilt shows.

> She is still alive. She is in
> her early seventies. She does
> not give a lot of interviews.
> She does come, occasionally, to
> the Computer History Museum
> when invited, and she will sign
> a yellow legal pad if you
> bring one. Many do.

> ── PART 4: WHAT WE BUILT ──

> Below is RIVER RUN. Vertically-
> scrolling. A small plane. A river
> with banks. Boats, helicopters,
> jets. Fuel depots. Bridges. The
> mechanic of UP-accelerates and
> DOWN-decelerates is preserved.
> Our river is procedurally
> generated, like Carol's, but we
> use a different seed so the
> course is our own.

> No vehicle-game has ever quite
> equalled the rhythm of RIVER
> RAID. We have not equalled it
> either. We have tried to honour
> it.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> RIVER RAID and related marks belong
> to their respective rights-holders.
> Carol Shaw retains moral authorship
> credit. Our tribute is called
> RIVER RUN. Concerns:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> [Музейная табличка, Computer
>  History Museum, Маунтин-Вью,
>  Калифорния, экспозиция
>  «Женщины в раннем
>  game-development», 2022.]

> Кэрол Шоу закончила Беркли
> в 1977-м со степенью
> магистра в Computer
> Science. В тот же год
> пришла в Atari. Была
> первой женщиной-
> программистом в Atari.
> Была первой женщиной-
> программистом в
> американской консольной
> индустрии. Несколько лет
> была единственной.

> Её первая игра в Atari —
> 3-D TIC-TAC-TOE для Atari
> 2600 (1980). Куб
> крестиков-ноликов 4×4×4 с
> поворачивающимися
> ракурсами. Продано 200 000
> картриджей. Руководство
> Atari было вежливым, но
> не впечатлённым: «Кэрол,
> мы ценим твою работу, но
> наш следующий большой
> упор — экшн-игры.»

> Кэрол ушла из Atari в
> 1980-м и пришла в
> Activision, новую
> third-party студию,
> основанную бывшими
> программистами Atari
> годом раньше. В Activision
> у неё была свобода
> дизайна: основатели бежали
> из Atari именно для того,
> чтобы дать программистам
> творческую собственность.

> Она объявила коллегам свой
> следующий проект:
> вертикально-скроллящийся
> шутер. Самолёт летит
> ВНИЗ по реке. Враждебные
> наземные силы появляются
> с берегов. Топливные
> бочки в центре реки можно
> подобрать, чтобы заправиться
> в полёте. Мосты блокируют
> путь — нужно их бомбить,
> чтобы двигаться дальше.

> Команда возразила: как
> отрисовать извилистую
> реку, идущую сотни
> километров, в 4
> килобайтах ROM картриджа,
> на машине со 128 байтами
> RAM?

> Ответ Кэрол —
> алгоритм генерации реки,
> который она спроектировала
> в конце 1981-го, на жёлтом
> юридическом блокноте в
> своей квартире в
> Саннивейле.

> ── ЧАСТЬ 1: АЛГОРИТМ ──

> Река процедурно сгенерирована.
> Не случайно —
> детерминированно. Река
> каждого игрока — ТА ЖЕ
> река. Кэрол использовала
> Linear Feedback Shift
> Register (LFSR) —
> аппаратный примитив,
> генерирующий длинную
> псевдослучайную
> последовательность из
> маленького зерна. Она
> засеяла его один раз при
> старте игры
> фиксированным значением.
> LFSR произвёл ширину
> реки, размещение
> препятствий, интервалы
> мостов, кривизну берегов
> — всё из одного зерна.

> 256 байт ROM картриджа
> держали генератор реки.
> Остальные 3.75 килобайта
> — самолёт игрока,
> спрайты врагов, звуковой
> генератор, отображение
> очков.

> Река RIVER RAID,
> следовательно, около 200
> километров длиной. Каждый
> игрок пролетает те же
> 200 километров. Ветераны
> запоминали ориентиры:
> «второй мост после
> широкого поворота»,
> «вертолётная группа в
> узком каньоне». Река
> стала ОБЩЕЙ КАРТОЙ,
> которую каждый игрок по
> Америке одновременно
> изучал.

> ── ЧАСТЬ 2: ИГРА ──

> Ты пилотируешь маленький
> серый самолёт. Самолёт
> автоматически
> прокручивается вверх,
> пока ты падаешь вниз по
> реке. Река шире в одних
> местах, уже в других.
> Берега смертельны:
> коснёшься берега на
> скорости — самолёт
> разваливается.

> УПРАВЛЕНИЕ необычно
> сложное для 1982-го:
>   - ВЛЕВО / ВПРАВО —
>     рули самолёт
>     горизонтально (поперёк
>     реки).
>   - ВВЕРХ — РАЗГОН
>     самолёта. Река
>     прокручивается
>     быстрее. Рискованно,
>     но покрывает больше
>     пути на единицу
>     топлива.
>   - ВНИЗ — ТОРМОЗ.
>     Река прокручивается
>     медленнее. Жжёт
>     больше топлива на
>     единицу дистанции,
>     но позволяет
>     аккуратно протягивать
>     узкие каньоны.
>   - FIRE — стреляй из
>     пулемётов вперёд.

> Враги: лодки, вертолёты,
> реактивные истребители,
> топливные базы (по ним не
> стреляй, пролетай чтобы
> заправиться) и мосты
> (бомби, чтобы продолжить).

> ── ЧАСТЬ 3: ИТОГ ──

> RIVER RAID вышла на
> Рождество 1982-го.
> Продано 1.5 миллиона
> картриджей. Победила в
> номинации «Консольная игра
> года» от ELECTRONIC GAMES
> MAGAZINE. Кэрол получила
> «Game Designer of the
> Year» от INFOWORLD.

> Ей было 27.

> Продолжала делать игры в
> Activision до 1984-го.
> Потом ушла из игровой
> индустрии. По её поздним
> воспоминаниям —
> выгорела. Шесть лет
> подряд работала по 70
> часов в неделю. Хотела
> жизнь.

> Кэрол перешла в Tandem
> Computers (позже куплены
> Compaq, потом HP).
> Полностью ушла из
> технологий к середине
> 1990-х. Следующие
> десятилетия растила
> семью и занималась
> квилтингом — получила
> несколько призов на
> национальных выставках.

> Она жива. Ей за
> семьдесят. Не даёт много
> интервью. Иногда
> приходит в Computer
> History Museum, когда
> приглашают, и подпишет
> жёлтый юридический
> блокнот, если принесёшь.
> Многие приносят.

> ── ЧАСТЬ 4: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — RIVER RUN.
> Вертикально-скроллящийся.
> Маленький самолёт. Река с
> берегами. Лодки,
> вертолёты, истребители.
> Топливные базы. Мосты.
> Механика
> ВВЕРХ-разгоняет и
> ВНИЗ-тормозит сохранена.
> Наша река процедурно
> сгенерирована, как у
> Кэрол, но мы используем
> другое зерно — курс наш
> собственный.

> Ни одна
> транспортная-игра не
> сравнилась с ритмом
> RIVER RAID. Мы тоже не
> сравнились. Мы пытались
> его почтить.

> ── СНОСКА У КОСТРА ──

> RIVER RAID и связанные
> знаки принадлежат их
> соответствующим
> правообладателям. Кэрол
> Шоу сохраняет моральное
> авторство. Наша дань
> называется RIVER RUN.
> Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val RIVER_RAID_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val RIVER_RAID_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickRiverRaidIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> RIVER_RAID_INTRO_RU
    else -> RIVER_RAID_INTRO_EN
}

val RIVER_RAID_INTRO_TEXT: String = RIVER_RAID_INTRO_EN
