package com.pixelclassics.app.games.navalbattle

/**
 * Long-form campfire intro for Naval Battle — told in the handwriting of
 * a graph-paper notebook page, dated «approximately 1985», recovered
 * from a Leningrad classroom desk during a renovation.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
╭────────────────────────────────────╮
│   TETRADKA · ЛЕНИНГРАД · ~1985     │
│   GRADE 7B · BACK DESK · MARGINS   │
╰────────────────────────────────────╯
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> Hello. I am a school notebook. I was
> bought for 18 kopecks in the autumn
> of 1984 at a state stationery shop on
> Sadovaya street. I was meant to hold
> chemistry homework. I hold mostly
> Sea Battle.

> ── PART 1: THE GAME OLDER THAN RADIO ──

> Sea Battle, in its paper-and-pencil
> form, is older than radio. Older than
> radar. Older than the entire concept
> of "naval intelligence" as a
> discipline.

> Two players. Two grids. Each player
> draws ships secretly on their grid,
> shows nothing to the opponent. Then
> they take turns calling coordinates:
> "A4". The opponent answers truthfully:
> miss, hit, or sunk. The first player
> to destroy the entire enemy fleet
> wins.

> The earliest documented version of
> the game appears in late 19th century
> Russia, in officers' clubs, played
> as a passtime during long trans-
> Siberian railway journeys. By the
> 1930s it was the standard distraction
> of bored schoolchildren across the
> entire Soviet bloc. It survived two
> world wars, the launch of the
> Sputnik, the dissolution of the
> USSR, and the rise of the Internet.

> By 2026 my fellow tetradkas are
> almost all gone — paper does not age
> well — but the game is in every
> child's brain who ever sat next to
> a smarter friend during a long maths
> lesson and quietly slipped a folded
> sheet of grid paper across the desk.

> ── PART 2: THE RUSSIAN FLEET ──

> The classical Soviet ruleset is
> precise:

>   ONE 4-deck battleship    (1 ship,
>                               4 cells)
>   TWO 3-deck cruisers      (2 ships,
>                               6 cells)
>   THREE 2-deck destroyers  (3 ships,
>                               6 cells)
>   FOUR  1-deck submarines  (4 ships,
>                               4 cells)
>                            -----------
>                            10 ships,
>                            20 cells
>                            on a 10x10
>                            grid (10%
>                            ocean
>                            occupancy)

> Ships are HORIZONTAL or VERTICAL,
> never diagonal. Ships do NOT TOUCH
> each other — not even at corners.
> The diagonal-touch rule turns out
> to be a deep gameplay tactic: when
> you sink a ship, you know the
> surrounding 8-cell border is
> guaranteed water, and can stop
> firing at it.

> Western "Battleship" (the Hasbro
> board game, 1967, plastic peg
> version) uses a similar but not
> identical fleet — typically one
> 5-deck and one 4-deck, three 3-
> decks, two 2-decks. Allows corner
> touches. The naval-engineering
> dialect is slightly different. The
> game is the same game.

> ── PART 3: WHAT IT FEELS LIKE ──

> What it feels like, played on graph
> paper in a notebook like me, is this:

> Your friend draws their grid in
> pencil. You draw yours. You fold the
> paper so neither can see the other's.
> Lessons begin. You both stare at the
> blackboard with appropriate diligence
> while quietly, under your desks, you
> mark grids.

> They call "B7". You check. It hits
> the corner of your 2-deck destroyer.
> You write a little dot in the cell
> they shot, and announce: "Хит."
> They mark the dot on their grid as
> a confirmed hit. They take another
> turn (Soviet rule: hits give you
> another shot).

> You call "F2". They check. Miss.
> They write the miss. You write your
> own miss on their grid.

> By the end of the lesson maybe one
> ship is sunk on each side. The whole
> thing might take a week of lessons
> to finish. The grids will be
> covered in dots. The teacher will
> never notice because the grids are
> drawn small in the corner of an
> ostensibly-chemistry assignment.

> ── PART 4: WHAT WE BUILT ──

> Below is Naval Battle. The classic
> Soviet ruleset. The classic 10x10
> grid. Your fleet (1×4, 2×3, 3×2,
> 4×1) drawn from the bottom of the
> screen. Your shot grid drawn at the
> top. Tap an enemy cell to fire.
> The AI tries to be a worthy
> opponent.

> The AI's strategy is the same as
> any thinking player: shoot in a
> diagonal "checkerboard" pattern
> first (every 2x2 has at most one
> cell of even a 1-deck), so within
> 50 shots you've located every
> possible ship. Once a hit lands,
> shift to neighbour-cell follow-up
> until the ship is sunk. After
> sinking, mark the 8-cell border
> as guaranteed water (so we don't
> waste shots there). Then return to
> checkerboard.

> This isn't a hard AI. It's an AI
> that plays the way every
> 13-year-old in Russia learns to
> play by the end of seventh grade.
> You can beat it. Most of the time
> you will, especially if you place
> your ships smartly along the
> edges (so the AI's checkerboard
> takes longer to reach them).

> ── PART 5: COMING SOON ──

> We are also building a standalone,
> full-feature Naval Battle as a
> separate Android app. More
> animations, more boards (asymmetric
> arenas, archipelagos), Wi-Fi
> two-player play, customisable
> fleet rules. The standalone version
> is going to be its own product.

> What you have here, inside The
> Pixel Campfire, is the museum-
> curated classroom-tetradka version.
> Pure, stripped, paper.

> ── PART 6: WHY IT STILL WORKS ──

> Because uncertainty is fun. Because
> you and your opponent share a
> hidden state that you reveal piece
> by piece, like a slow stripping of
> mutual mystery. Because every hit
> teaches you something about the
> ship you just hit. Because every
> miss teaches you something about
> where the ship isn't. Because the
> game is, fundamentally, a tiny
> course in Bayesian reasoning that
> a 13-year-old can play under a
> chemistry desk.

> No game has ever improved on that.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> BATTLESHIP is a Hasbro trademark for
> the plastic-peg board game. SEA
> BATTLE, the paper-and-pencil game,
> is a folk artefact — public domain,
> beyond ownership. We use the name
> "Naval Battle" to be respectful and
> distinct. If Hasbro has any concern:
> elyssov@gmail.com, gentle and quick.
""".trimIndent()

private val BODY_RU: String = """
> Привет. Я — школьная тетрадка.
> Меня купили за 18 копеек
> осенью 1984-го в
> государственном канцелярском
> магазине на Садовой улице.
> Я предназначалась для
> домашних заданий по химии.
> Я держу в основном Морской
> Бой.

> ── ЧАСТЬ 1: ИГРА СТАРШЕ РАДИО ──

> Морской бой в форме бумаги-
> и-карандаша старше радио.
> Старше радара. Старше всей
> концепции «военно-морской
> разведки» как дисциплины.

> Два игрока. Две сетки.
> Каждый игрок секретно
> рисует корабли на своей
> сетке, ничего не показывает
> противнику. Потом по очереди
> называют координаты: «А4».
> Противник правдиво отвечает:
> мимо, попал или потопил.
> Первый, кто уничтожает весь
> флот противника, выигрывает.

> Самая ранняя задокументированная
> версия игры появляется в
> России конца 19-го века, в
> офицерских клубах, как
> времяпрепровождение во время
> долгих транссибирских
> железнодорожных поездок. К
> 1930-м это стандартное
> развлечение скучающих
> школьников по всему
> советскому блоку. Игра
> пережила две мировые войны,
> запуск Спутника, распад
> СССР и расцвет Интернета.

> К 2026-му мои собратья-
> тетрадки почти все исчезли —
> бумага плохо стареет — но
> игра в мозгу у каждого
> ребёнка, который когда-либо
> сидел рядом с умным другом
> на длинном уроке математики
> и тихо подсунул через парту
> сложенный лист бумаги в
> клеточку.

> ── ЧАСТЬ 2: РУССКИЙ ФЛОТ ──

> Классический советский
> регламент точен:

>   ОДИН 4-палубный линкор    (1 корабль,
>                                4 клетки)
>   ДВА 3-палубных крейсера   (2 корабля,
>                                6 клеток)
>   ТРИ 2-палубных эсминца    (3 корабля,
>                                6 клеток)
>   ЧЕТЫРЕ 1-палубных подлодки (4 корабля,
>                                4 клетки)
>                             -----------
>                             10 кораблей,
>                             20 клеток
>                             на сетке
>                             10×10 (10%
>                             занятости
>                             океана)

> Корабли ГОРИЗОНТАЛЬНЫЕ или
> ВЕРТИКАЛЬНЫЕ, никогда не
> диагональные. Корабли НЕ
> КАСАЮТСЯ друг друга — даже
> по углам. Правило
> неприкосновения по диагонали
> оказывается глубокой
> игровой тактикой: когда ты
> топишь корабль, ты знаешь,
> что окружающая 8-клеточная
> граница гарантированно вода,
> и можешь прекратить по ней
> стрелять.

> Западный «Battleship»
> (настолка Hasbro 1967-го,
> пластиково-колышковая
> версия) использует
> похожий, но не идентичный
> флот — обычно один 5-палубный
> и один 4-палубный, три 3-
> палубных, два 2-палубных.
> Разрешает касание по углам.
> Военно-морской инженерный
> диалект слегка отличается.
> Игра — та же игра.

> ── ЧАСТЬ 3: КАК ЭТО ОЩУЩАЕТСЯ ──

> Как это ощущается, игранное
> на клетчатой бумаге в
> тетрадке вроде меня — вот
> так:

> Твой друг рисует свою сетку
> карандашом. Ты рисуешь
> свою. Складываешь бумагу,
> чтобы никто не видел чужой.
> Уроки начинаются. Вы оба
> смотрите на доску с
> подобающим прилежанием,
> пока тихо, под партами,
> размечаете сетки.

> Они называют «Б7». Ты
> проверяешь. Попадает в угол
> твоего 2-палубного эсминца.
> Ты ставишь маленькую точку
> в клетке, по которой
> стрельнули, и объявляешь:
> «Хит.» Они помечают точку
> на своей сетке как
> подтверждённое попадание.
> Они ходят ещё раз
> (советское правило: попадание
> даёт ещё выстрел).

> Ты называешь «Ф2». Они
> проверяют. Мимо. Они
> пишут мимо. Ты пишешь свой
> промах на их сетке.

> К концу урока, может, по
> одному кораблю потоплено с
> каждой стороны. Вся партия
> может занять неделю уроков.
> Сетки покроются точками.
> Учительница никогда не
> заметит, потому что сетки
> нарисованы маленькими в
> углу якобы-химического
> задания.

> ── ЧАСТЬ 4: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — Морской Бой.
> Классический советский
> регламент. Классическая
> сетка 10×10. Твой флот
> (1×4, 2×3, 3×2, 4×1)
> нарисован снизу экрана.
> Твоя сетка обстрела
> нарисована сверху. Кликни
> по клетке противника, чтобы
> выстрелить. AI пытается
> быть достойным противником.

> Стратегия AI — та же, что и
> у любого думающего игрока:
> стрелять сначала по
> диагональной «шахматной»
> схеме (каждый квадрат 2×2
> содержит максимум одну
> клетку даже 1-палубной), так
> что за 50 выстрелов ты
> найдёшь каждый возможный
> корабль. Как только
> попадание ложится,
> переключиться на дострел
> соседних клеток, пока
> корабль не потоплен. После
> потопления пометить
> 8-клеточную границу как
> гарантированную воду (чтобы
> не тратить туда выстрелы).
> Потом вернуться к
> шахматной.

> Это не сложный AI. Это AI,
> который играет так, как
> учится играть к концу 7-го
> класса каждый 13-летний в
> России. Ты можешь его
> победить. Большую часть
> времени — победишь,
> особенно если расставишь
> корабли умно вдоль краёв
> (чтобы AI'шной шахматной
> дольше до них добираться).

> ── ЧАСТЬ 5: СКОРО ──

> Мы также строим
> самостоятельный
> полнофункциональный
> Морской Бой как отдельное
> Android-приложение. Больше
> анимаций, больше досок
> (асимметричные арены,
> архипелаги), Wi-Fi игра на
> двоих, настраиваемые
> правила флота. Standalone-
> версия будет своим
> продуктом.

> Что у тебя здесь, внутри
> «Пиксельного костра» —
> музейно-кураторская версия
> классной тетрадки. Чистая,
> ободранная, бумажная.

> ── ЧАСТЬ 6: ПОЧЕМУ ДО СИХ ПОР РАБОТАЕТ ──

> Потому что неопределённость
> — это весело. Потому что
> вы с противником делите
> скрытое состояние, которое
> раскрываете по кусочкам,
> как медленное раздевание
> взаимной тайны. Потому что
> каждое попадание говорит
> тебе что-то о корабле, в
> который ты попал. Потому
> что каждый промах говорит
> что-то о том, где корабля
> нет. Потому что игра — это,
> фундаментально, крошечный
> курс байесовского
> рассуждения, в который
> 13-летний может сыграть
> под партой химии.

> Ни одна игра никогда этого
> не улучшила.

> ── СНОСКА У КОСТРА ──

> BATTLESHIP — торговая марка
> Hasbro для пластиково-
> колышковой настолки.
> МОРСКОЙ БОЙ, игра бумаги-
> карандаша — народный
> артефакт, public domain,
> вне владения. Мы используем
> название «Naval Battle»,
> чтобы быть уважительными и
> отличными. Если у Hasbro
> есть вопросы:
> elyssov@gmail.com, тихо и
> быстро.
""".trimIndent()

val NAVAL_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val NAVAL_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickNavalIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> NAVAL_INTRO_RU
    else -> NAVAL_INTRO_EN
}

val NAVAL_INTRO_TEXT: String = NAVAL_INTRO_EN
