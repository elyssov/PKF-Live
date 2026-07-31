package com.pixelclassics.app.games.pitfallclassic

/**
 * Long-form campfire intro for PIT JUMPER (Pitfall! the 1982 Activision
 * original). Narrated by David Crane himself, in a relaxed interview
 * looking back from retirement.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
████████████████████████████████████████
█  ACTIVISION · PITFALL! · 1982         █
█  DAVID CRANE · INTERVIEW · 2024       █
█  IN HIS HOME OFFICE · LA, CALIFORNIA  █
████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> [Casual setting. Crane sips coffee.]

> Yeah, sure. Pitfall.

> Look, here's what nobody fully
> understood about the Atari 2600
> at the time. The hardware was
> brutal. 128 BYTES of RAM, not
> kilobytes. A processor that had
> to draw each scanline of the TV
> in real time, hand-off-the-rails.
> Programmers literally counted
> CPU cycles per scanline, because
> if you didn't finish your
> calculations before the next
> horizontal sync, the screen
> garbled.

> ── PART 1: THE FOUR ──

> I joined Atari in 1979 — straight
> out of DeVry Institute of
> Technology. I was 25. They paid
> me ${'$'}35,000 a year, which felt
> like a fortune. I worked on
> several titles, some of them
> shipped, some of them didn't.

> The big problem at Atari was
> that programmers got no credit.
> Atari's policy was that the
> games were corporate products,
> like cereal boxes — you don't
> credit individuals on a cereal
> box. So my name didn't appear
> on the games I made. Neither
> did Larry Kaplan's, or Alan
> Miller's, or Bob Whitehead's.

> We were also paid flat salaries
> regardless of how much our
> games sold. Larry's "Bowling"
> sold over a million cartridges.
> Atari grossed maybe ${'$'}20 million
> off it. Larry made ${'$'}35,000 that
> year, same as everyone else.

> One day in late 1979 we sat
> down — the four of us — and
> figured the math. Atari was
> grossing roughly ${'$'}7 million per
> programmer per year, treating
> us like assembly-line workers
> while everyone else in the
> creative industries (films,
> books, music) had royalty
> structures.

> We quit.

> All four of us, in October
> 1979, walked into management's
> office on the same day and
> resigned. We left to found
> Activision: the first third-party
> game studio. Our pitch to
> investors was simple — give
> programmers names, credit, and
> royalties, and they will
> produce better games than the
> corporate machine. Investors
> agreed. October 1979 to
> June 1980 we built Activision
> from a borrowed office to a
> shipped product.

> ── PART 2: PITFALL TAKES SHAPE ──

> By late 1981 I had shipped a few
> Activision titles. None had been
> blockbusters. The company needed
> a hit, urgently — Atari had
> sued us aggressively the moment
> we launched (the suit was
> eventually settled out of
> court, but it sucked up a lot
> of legal budget) and we needed
> to fund the defence.

> I had a vague idea for "guy
> in a jungle." Started prototyping
> in spring 1981. The guy was
> Harry — running figure, animation
> cycles I'd been working on for
> a year. I had crocodiles
> swimming in water. I had a vine
> swing — actually, the vine
> swing came LATER, originally I
> had a simple two-screen tunnel
> system. The vine swing got
> added when I realised the
> player needed something
> RHYTHMIC, something with
> timing-to-music, to break up
> the monotony of running and
> jumping.

> The breakthrough was the
> MAP. I had been thinking
> "8-10 screens, hand-drawn." A
> friend who worked at IBM
> Research showed me a math
> trick: a Linear Feedback
> Shift Register can generate
> long deterministic pseudo-
> random sequences from a small
> seed. If I encoded the LFSR
> seed and current state in a
> few bytes of cartridge ROM,
> I could generate the SAME 256
> screens every time the game
> ran. Procedural generation,
> deterministic, fast.

> 256 screens of jungle. From
> 1,024 bytes of cartridge ROM
> for the map system. Plus the
> game logic, sprite data,
> sound. The total cartridge
> was 4 kilobytes. Standard
> 2600 size.

> ── PART 3: WHAT'S IN A SCREEN ──

> Each of the 256 screens has:
>   - A background colour (sky
>     varies by environmental
>     "zone" — green, light
>     green, lighter green for
>     surface jungle; black or
>     brown for tunnels).
>   - 0-1 pits (small black gap
>     in the surface; falling in
>     loses a small amount of
>     "score").
>   - 0-1 tar pits (wider, deadly;
>     you must vine-swing).
>   - 0-1 quicksand pits (slowly
>     opening and closing; jump
>     across when small).
>   - 0-1 logs (rolling
>     horizontally; jump over).
>   - 0-1 scorpions (in tunnel
>     screens; lethal touch).
>   - 0-1 crocodiles (three of
>     them with snapping
>     mouths, in water; vine
>     across them).
>   - 0-1 vines (hanging from
>     the canopy; jump and grab).
>   - 0-1 treasures (gold bar
>     +5000, silver bar +3000,
>     money bag +2000, diamond
>     ring +4000).

> 32 treasures total, spread
> across the 256 screens. You
> have 20 minutes (real time)
> to collect as many as
> possible. The screens loop
> end-to-end, so you can keep
> running indefinitely until the
> clock expires.

> ── PART 4: THE NUMBERS ──

> Pitfall shipped April 1982.

> 4 million cartridges sold in the
> first six months. Activision's
> single biggest hit. The
> company's revenue for 1982
> jumped from ${'$'}6 million in 1981
> to ${'$'}66 million in 1982.

> 4 million children opened
> Pitfall on Christmas morning
> 1982. By 1984 the cartridge
> was on every American kid's
> "had it" list, the way
> "Stranger Things" is on every
> 2026 kid's "watched it" list.

> Pitfall! made me, personally,
> very rich. I was 28 years old
> when the royalty cheques
> started arriving. I bought a
> small Cessna with the first
> year's payments. I learned
> to fly it. Atari's lawyers,
> wherever they ended up,
> probably had less of a year.

> ── PART 5: WHAT WE BUILT ──

> Below is PIT JUMPER. Our tribute.
> Harry-like protagonist (we
> aren't calling him Harry —
> that name is trademarked).
> Procedurally-generated 64-scene
> jungle that loops. Pits, tar
> pits, fire pits, crocodiles,
> scorpions, rolling logs.
> Underground tunnel system
> accessed via ladders. Vines
> swinging from the canopy.
> 24 treasures scattered across
> the scenes. 20-minute mission
> clock.

> Direction pad to run.
> ▲▼ on a ladder to descend
> into or climb out of the
> underground tunnels. ● FIRE
> to jump. At the apex of a
> jump, if a vine is overhead,
> you grab it and swing.

> Welcome to the jungle.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> PITFALL! and related marks belong
> to their respective rights-holders.
> David Crane retains moral
> authorship credit and remains
> alive, retired in California,
> occasionally interviewed at
> game conventions. Our tribute
> is called PIT JUMPER. Concerns:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> [Свободная обстановка. Крейн
> пьёт кофе.]

> Да, конечно. Pitfall.

> Слушай, вот что никто
> до конца не понимал про
> Atari 2600 в то время.
> Железо было жестоким. 128
> БАЙТ RAM, не килобайт.
> Процессор, рисующий каждую
> строку ТВ в реальном
> времени, кустарно. Программисты
> буквально считали такты
> процессора на строку,
> потому что если ты не
> успел закончить расчёт до
> следующей горизонтальной
> синхронизации, экран
> мусорил.

> ── ЧАСТЬ 1: ЧЕТВЕРО ──

> Я пришёл в Atari в 1979-м
> — сразу из DeVry Institute
> of Technology. Мне было 25.
> Платили 35 000 в год, что
> казалось состоянием. Работал
> над несколькими тайтлами,
> часть вышла, часть нет.

> Большая проблема Atari —
> программистам не давали
> кредитов. Политика Atari:
> игры — корпоративные
> продукты, как коробки с
> хлопьями — на коробке
> хлопьев индивидов не
> упоминают. Так что моё
> имя не появлялось на играх,
> что я делал. И Ларри
> Каплана, и Алана Миллера,
> и Боба Уайтхеда — тоже.

> Платили плоский оклад,
> независимо от того, сколько
> наши игры продались.
> Ларриевский «Bowling»
> продался больше миллиона
> картриджей. Atari выручила
> с него миллионов 20.
> Ларри получил 35 000 за тот
> год, как и все.

> Однажды в конце 1979-го
> мы сели — мы вчетвером —
> и посчитали. Atari делала
> примерно 7 миллионов на
> программиста в год, обращаясь
> с нами как со
> сборщиками-конвейерщиками,
> в то время как все остальные
> в творческих индустриях
> (кино, книги, музыка)
> имели роялти.

> Мы уволились.

> Все четверо, в октябре
> 1979-го, вошли в кабинет
> руководства в один день и
> подали заявления. Ушли
> основывать Activision —
> первую third-party
> игровую студию. Наш питч
> инвесторам был прост:
> дайте программистам имена,
> кредиты и роялти, и они
> сделают игры лучше, чем
> корпоративная машина.
> Инвесторы согласились. С
> октября 1979-го по июнь
> 1980-го мы построили
> Activision — от
> заимствованного офиса до
> отгруженного продукта.

> ── ЧАСТЬ 2: PITFALL ФОРМИРУЕТСЯ ──

> К концу 1981-го я отгрузил
> несколько тайтлов
> Activision. Ни один не
> был блокбастером. Компании
> срочно нужен был хит —
> Atari агрессивно засудила
> нас в момент запуска (иск
> в итоге урегулировали вне
> суда, но он съел большой
> кусок юр-бюджета), и
> нужно было финансировать
> защиту.

> У меня была смутная идея
> «парень в джунглях». Начал
> прототипить весной 1981-го.
> Парень — Гарри — бегущая
> фигура, циклы анимации,
> над которыми я работал год.
> Были крокодилы, плавающие
> в воде. Был свинг на лиане
> — на самом деле, свинг
> появился ПОЗЖЕ, изначально
> у меня была простая
> система туннелей на двух
> экранах. Свинг добавили,
> когда я понял, что игроку
> нужно что-то РИТМИЧЕСКОЕ,
> что-то с тайминг-к-музыке,
> чтобы разбить монотонность
> бега и прыжков.

> Прорывом была КАРТА. Я
> думал «8-10 экранов,
> нарисованных вручную».
> Друг, работавший в IBM
> Research, показал мне
> матфокус: Linear Feedback
> Shift Register может
> генерировать длинные
> детерминированные
> псевдослучайные
> последовательности из
> маленького зерна. Если я
> закодирую зерно LFSR и
> текущее состояние в
> несколько байт ROM
> картриджа, я могу
> генерировать ТЕ ЖЕ 256
> экранов каждый раз, когда
> игра запускается.
> Процедурная генерация,
> детерминированная, быстрая.

> 256 экранов джунглей. Из
> 1024 байт ROM картриджа
> на систему карт. Плюс
> игровая логика, данные
> спрайтов, звук. Весь
> картридж — 4 килобайта.
> Стандартный размер 2600.

> ── ЧАСТЬ 3: ЧТО ВНУТРИ ЭКРАНА ──

> У каждого из 256 экранов:
>   - Фоновый цвет (небо
>     варьируется по
>     окружающей «зоне» —
>     зелёное, светло-
>     зелёное, ещё светлее
>     для поверхностных
>     джунглей; чёрный или
>     коричневый для
>     туннелей).
>   - 0-1 ямы (маленький
>     чёрный провал в
>     поверхности; падение
>     теряет немного «очков»).
>   - 0-1 смоляные ямы
>     (шире, смертельные;
>     надо качнуться на
>     лиане).
>   - 0-1 зыбучие пески
>     (медленно открываются
>     и закрываются; прыгай,
>     пока узкие).
>   - 0-1 брёвен (катятся
>     горизонтально;
>     перепрыгни).
>   - 0-1 скорпионов (на
>     туннельных экранах;
>     смертельное касание).
>   - 0-1 крокодилов (трое
>     с щёлкающими пастями,
>     в воде; перекинься на
>     лиане).
>   - 0-1 лиан (свисают с
>     полога; прыгай и
>     хватай).
>   - 0-1 сокровищ (золотой
>     слиток +5000,
>     серебряный слиток
>     +3000, мешок денег
>     +2000, бриллиантовое
>     кольцо +4000).

> 32 сокровища всего,
> разбросанных по 256
> экранам. У тебя 20 минут
> (реального времени)
> собрать как можно больше.
> Экраны замыкаются
> конец-в-начало, так что
> можешь бежать бесконечно,
> пока часы не истекут.

> ── ЧАСТЬ 4: ЦИФРЫ ──

> Pitfall вышел в апреле
> 1982-го.

> 4 миллиона картриджей
> продано за первые шесть
> месяцев. Самый большой
> хит Activision. Выручка
> компании за 1982-й
> прыгнула с 6 миллионов в
> 1981-м до 66 миллионов в
> 1982-м.

> 4 миллиона детей открыли
> Pitfall рождественским
> утром 1982-го. К 1984-му
> картридж был в списке
> «у меня есть» каждого
> американского ребёнка,
> как «Очень странные
> дела» в списке
> «я смотрел» каждого
> ребёнка в 2026-м.

> Pitfall! сделал меня
> лично очень богатым. Мне
> было 28, когда начали
> приходить чеки роялти. Я
> купил маленькую Cessna с
> платежей первого года.
> Научился её водить.
> Юристы Atari, где бы они
> ни оказались, вероятно,
> провели год хуже.

> ── ЧАСТЬ 5: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — PIT JUMPER. Наша
> дань. Гарри-подобный
> протагонист (Гарри его
> не зовём — имя торговое).
> Процедурно сгенерированные
> 64 сцены джунглей,
> замкнутые в цикл. Ямы,
> смоляные ямы, огненные
> ямы, крокодилы, скорпионы,
> катящиеся брёвна. Подземная
> туннельная система,
> доступная через лестницы.
> Лианы, свисающие с полога.
> 24 сокровища, разбросанные
> по сценам. 20-минутный
> миссионный таймер.

> D-pad — бежать. ▲▼ на
> лестнице — спуститься в
> подземные туннели или
> вылезти из них. ● FIRE —
> прыжок. В апогее прыжка,
> если лиана сверху, ты её
> хватаешь и качаешься.

> Добро пожаловать в
> джунгли.

> ── СНОСКА У КОСТРА ──

> PITFALL! и связанные
> знаки принадлежат их
> соответствующим
> правообладателям. Дэвид
> Крейн сохраняет моральное
> авторство и остаётся
> живым, на пенсии в
> Калифорнии, иногда даёт
> интервью на игровых
> конвентах. Наша дань
> называется PIT JUMPER.
> Вопросы: elyssov@gmail.com.
""".trimIndent()

val PIT_JUMPER_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val PIT_JUMPER_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickPitJumperIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> PIT_JUMPER_INTRO_RU
    else -> PIT_JUMPER_INTRO_EN
}

val PIT_JUMPER_INTRO_TEXT: String = PIT_JUMPER_INTRO_EN
