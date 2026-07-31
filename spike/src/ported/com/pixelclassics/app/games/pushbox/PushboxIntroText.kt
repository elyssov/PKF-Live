package com.pixelclassics.app.games.pushbox

/**
 * Long-form campfire intro for PUSHBOX (Sokoban). Voice: an elderly
 * warehouse-keeper in a small Japanese village, narrating the philosophy
 * of his lifetime in storage.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
█░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░█
█  THINKING RABBIT · SOKOBAN · 1982    █
█  HIROYUKI IMABAYASHI · TAKARAZUKA    █
█  PC-8801 · MZ-80 · 48K BASIC + ASM   █
█░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░█
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> Konnichiwa.

> Sit. Tea is over there, help
> yourself. The warehouse is quiet
> today, so I have time.

> Let me tell you about box-pushing.

> ── PART 1: 倉庫番 ──

> The Japanese word SOKOBAN —
> 倉庫番 — means, literally,
> "warehouse keeper". It refers to
> the dispatcher who works the
> floor of a goods storage
> warehouse, organising crates,
> assigning labels, stacking,
> shifting, sorting. In a small
> traditional warehouse — a wooden-
> floored building behind a
> merchant's shop, where the
> day's deliveries are stored
> overnight — the warehouse keeper
> works alone, pushing crates one
> at a time into their assigned
> positions for the next morning.

> Pushing, not lifting. Lifting
> is for short distances. For
> long distances across a wooden
> warehouse floor, you push. You
> set your weight against the
> crate, you push, you walk
> alongside as it slides.

> You cannot PULL a crate. If you
> push a crate into a corner and
> the crate is now stuck against
> two walls, you cannot pull it
> back out. You must abandon
> that crate and arrange the
> warehouse around it.

> This is the essential lesson of
> the warehouse keeper's craft:
> EVERY PUSH IS IRREVERSIBLE.
> Plan the entire night's
> arrangement before you push
> your first crate. Once the
> first crate has moved, your
> future is constrained.

> ── PART 2: A PROGRAMMER'S GAME ──

> Hiroyuki Imabayashi, a young
> programmer in Takarazuka,
> Hyōgo Prefecture, was looking
> for a project for his small
> studio, THINKING RABBIT, in
> 1981. He had been working on
> port-conversions of Western
> arcade games for the Japanese
> PC market — PC-8801, MZ-80,
> NEC PC-6001 — and he wanted to
> make something ORIGINAL. Not
> another clone of an American
> arcade.

> He thought about what made
> a game uniquely satisfying. He
> thought about the engineering
> puzzles his father had set him
> as a child: arrange these
> blocks in this order without
> moving any of them
> backwards. He thought about
> warehouse work — his uncle
> kept a small warehouse in
> Osaka, and Imabayashi had
> visited it as a boy.

> He sat down at his PC-8801 and
> wrote SOKOBAN in three months,
> in BASIC with some assembly
> inserts for the rendering.
> Released December 1982 in
> Japan, retail ¥3,800 (then
> roughly ${'$'}16 USD).

> Sold 200,000 copies in Japan
> in its first year. Won the
> Famitsu "Award of Excellence"
> for its 1983 cohort. Got
> ported to over 50 systems in
> the next decade.

> ── PART 3: THE NP-HARDNESS ──

> The mathematical fact you may
> not realise: SOKOBAN is
> PSPACE-COMPLETE. The class of
> SOKOBAN puzzles is, in
> computer science complexity
> theory, in PSPACE — meaning
> that determining whether a
> given SOKOBAN puzzle is
> solvable can require an
> amount of computation that
> grows exponentially with the
> size of the puzzle.

> In practical terms: there
> exist SOKOBAN levels whose
> optimal solutions are not
> findable by any algorithm
> currently known, except by
> brute-force search through
> astronomically large state
> spaces.

> The level pack SOKOBAN II
> released by Imabayashi in
> 1984 contains 60 puzzles. The
> 60th level's known optimal
> solution requires 700+ moves.
> No SOKOBAN solver in 1984
> could find that solution
> automatically. Players found
> it by hand, by intuition, by
> long evenings of trial and
> error.

> This is a game where HUMAN
> INTUITION beats AUTOMATIC
> SEARCH. There is something
> deeply beautiful about that.

> ── PART 4: WHY YOU LOVE IT ──

> Because every move is permanent.
> Because there is no chance, no
> randomness, no surprise — only
> the puzzle as it stands, and
> your responsibility to find
> the solution. Because the
> moments of insight, when you
> finally see the order in
> which the crates must be
> pushed, are physically
> satisfying — like solving an
> equation.

> Because, in those moments of
> insight, you understand WHY
> the warehouse keeper of Hyōgo
> Prefecture worked alone at
> night. He needed silence to
> plan. He needed total focus to
> commit. He could not afford
> a wrong push.

> SOKOBAN is a meditation on
> consequence. Every move is a
> commitment.

> ── PART 5: WHAT WE BUILT ──

> Below is PUSHBOX. Thirty
> hand-tuned puzzles, from the
> trivial (level 1: push one
> crate one square) to the
> baroque (level 30: a 50-step
> dance with seven crates in a
> cramped warehouse).

> Direction pad to walk. Walk
> INTO a crate to push it (if
> there is empty space beyond,
> the crate slides; if not,
> your push does nothing).
> Push a crate ONTO a target,
> the crate locks there
> (visually highlighted).

> Win condition: every target
> square has a crate on it.

> Restart any level at any
> time. The game does not
> shame you for restarting.
> The warehouse keeper of
> Hyōgo Prefecture would have
> approved.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> SOKOBAN is a trademark of
> THINKING RABBIT and Falcon Co.
> in Japan. The mechanic —
> push-only crate puzzles on a
> grid — is in the public
> domain as far as game-design
> patents go. Our tribute is
> called PUSHBOX. Imabayashi-
> san, if you ever read this —
> hat off. Concerns:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Коннитива.

> Садись. Чай вон там, налей
> себе. Сегодня на складе
> тихо, у меня есть время.

> Я расскажу тебе о
> толкании ящиков.

> ── ЧАСТЬ 1: 倉庫番 ──

> Японское слово СОКОБАН —
> 倉庫番 — буквально значит
> «хранитель склада». Это
> диспетчер, работающий на
> полу склада товаров,
> организующий ящики, наклеивающий
> ярлыки, складывающий,
> переставляющий, сортирующий.
> На маленьком традиционном
> складе — деревянное здание
> за лавкой купца, где
> дневные поставки хранятся
> до утра — хранитель склада
> работает один, толкая
> ящики по одному в их
> назначенные позиции к
> следующему утру.

> Толкать, а не поднимать.
> Поднимают на короткие
> расстояния. На длинные —
> через деревянный пол —
> толкают. Упираешься всем
> весом в ящик, толкаешь,
> идёшь рядом, пока он
> скользит.

> Тянуть ящик нельзя. Если
> ты толкнул ящик в угол и
> ящик теперь упёрся в две
> стены — обратно его не
> вытянешь. Придётся
> бросить этот ящик и
> расставить склад вокруг
> него.

> Это основной урок ремесла
> хранителя склада: КАЖДЫЙ
> ТОЛЧОК НЕОБРАТИМ.
> Планируй всю ночную
> расстановку до того, как
> толкнёшь первый ящик. Как
> только первый ящик
> двинулся, твоё будущее
> ограничено.

> ── ЧАСТЬ 2: ПРОГРАММИСТСКАЯ ИГРА ──

> Хироюки Имабаяси, молодой
> программист в Такарадзуке,
> префектура Хёго, искал
> проект для своей маленькой
> студии THINKING RABBIT в
> 1981-м. Он занимался
> портированием западных
> аркадных игр для японского
> ПК-рынка — PC-8801, MZ-80,
> NEC PC-6001 — и хотел
> сделать что-то
> ОРИГИНАЛЬНОЕ. Не очередной
> клон американской аркады.

> Он думал о том, что делает
> игру уникально приятной.
> Он думал о инженерных
> задачках, которые отец
> ставил ему в детстве:
> расставь эти кубики в этом
> порядке, не двигая ни один
> назад. Думал о складской
> работе — его дядя держал
> маленький склад в Осаке,
> и Имабаяси посещал его
> мальчишкой.

> Он сел за свой PC-8801 и
> написал SOKOBAN за три
> месяца, на BASIC с
> ассемблерными вставками
> для рендеринга. Выпущен в
> декабре 1982-го в Японии,
> цена ¥3800 (тогда около
> 16 долларов).

> Продано 200 000 копий в
> Японии за первый год.
> Получил «Award of
> Excellence» от Famitsu для
> когорты 1983-го. Портирован
> на более чем 50 систем за
> следующее десятилетие.

> ── ЧАСТЬ 3: NP-СЛОЖНОСТЬ ──

> Математический факт, о
> котором ты можешь не
> знать: SOKOBAN —
> PSPACE-COMPLETE. Класс
> головоломок SOKOBAN, в
> теории сложности
> компьютерных наук,
> находится в PSPACE —
> значит, что определение,
> разрешима ли данная
> головоломка SOKOBAN, может
> требовать объём вычислений,
> растущий экспоненциально с
> размером головоломки.

> На практике: существуют
> уровни SOKOBAN, чьи
> оптимальные решения не
> находятся ни одним
> известным сегодня
> алгоритмом, кроме перебора
> через астрономически
> большие пространства
> состояний.

> Пакет уровней SOKOBAN II,
> выпущенный Имабаяси в
> 1984-м, содержит 60
> головоломок. Известное
> оптимальное решение
> 60-го уровня требует 700+
> ходов. Ни один solver
> SOKOBAN в 1984-м не мог
> найти это решение
> автоматически. Игроки
> нашли его руками, по
> интуиции, долгими вечерами
> проб и ошибок.

> Это игра, где ЧЕЛОВЕЧЕСКАЯ
> ИНТУИЦИЯ побеждает
> АВТОМАТИЧЕСКИЙ ПОИСК. В
> этом есть глубокая
> красота.

> ── ЧАСТЬ 4: ПОЧЕМУ ТЫ ЕЁ ЛЮБИШЬ ──

> Потому что каждый ход
> необратим. Потому что нет
> случайности, нет
> неожиданности — только
> головоломка как она есть,
> и твоя ответственность
> найти решение. Потому что
> моменты озарения, когда
> ты наконец видишь
> порядок, в котором надо
> толкать ящики, физически
> приятны — как решение
> уравнения.

> Потому что в эти моменты
> озарения ты понимаешь,
> ПОЧЕМУ хранитель склада
> Хёго работал ночью один.
> Ему нужна тишина для
> плана. Ему нужна полная
> сосредоточенность для
> исполнения. Он не мог
> позволить себе неверный
> толчок.

> SOKOBAN — медитация о
> последствиях. Каждый ход
> — обязательство.

> ── ЧАСТЬ 5: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — PUSHBOX. Тридцать
> вручную настроенных
> головоломок, от тривиальных
> (уровень 1: толкни один
> ящик на одну клетку) до
> барочных (уровень 30:
> 50-шаговый танец с семью
> ящиками в тесном складе).

> D-pad — идти. Иди НА ящик,
> чтобы толкнуть (если за
> ним пустое место — ящик
> скользит; если нет —
> толчок ничего не делает).
> Толкни ящик НА цель — ящик
> там фиксируется (визуально
> подсвечено).

> Условие победы: на каждой
> целевой клетке стоит
> ящик.

> Перезапусти любой уровень
> в любой момент. Игра не
> стыдит тебя за рестарт.
> Хранитель склада Хёго
> одобрил бы.

> ── СНОСКА У КОСТРА ──

> SOKOBAN — торговая марка
> THINKING RABBIT и Falcon
> Co. в Японии. Механика —
> только-толкать головоломки
> с ящиками на сетке — в
> public domain, что
> касается патентов
> game-design. Наша дань
> называется PUSHBOX.
> Имабаяси-сан, если ты это
> когда-нибудь прочитаешь —
> шляпа долой. Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val PUSHBOX_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val PUSHBOX_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickPushboxIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> PUSHBOX_INTRO_RU
    else -> PUSHBOX_INTRO_EN
}

val PUSHBOX_INTRO_TEXT: String = PUSHBOX_INTRO_EN
