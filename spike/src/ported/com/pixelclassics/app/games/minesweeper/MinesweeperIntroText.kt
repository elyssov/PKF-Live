package com.pixelclassics.app.games.minesweeper

/**
 * Long-form campfire intro for MINES (Minesweeper). Voice: Robert Donner,
 * the Microsoft engineer who actually wrote Minesweeper in 1989, in a
 * dry late-career interview reflecting on the fact that the world remembers
 * his right-click tutorial as a cultural icon.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
█████████████████████████████████████████
█  MICROSOFT INTERNAL · 1989 · RFC      █
█  "WELCOME TOOLS GROUP" · R. DONNER    █
█  RE: ENTERTAINMENT PACK PROPOSAL      █
█████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> Here's the story. I'd been at
> Microsoft since 1988, working on
> the Windows team. The product was
> Windows 3.0, which was going to be
> the first version that anybody
> outside the company actually liked.
> The user-interface team was
> obsessed with one thing: the mouse.

> Most office workers in 1989 had
> never touched a mouse. Lotus 1-2-3
> ran in DOS. WordPerfect ran in DOS.
> Everybody typed commands. The
> mouse, to a typical accountant in
> Chicago in 1989, was a strange
> plastic rodent that came with the
> Macintosh and was responsible,
> somehow, for the perceived
> "playfulness" of Apple computers —
> and therefore, by extension,
> their NOT being serious-business
> tools.

> The Windows team needed to teach
> America's office workers two
> things at the same time:
>   1. The mouse is good. Try the
>      mouse.
>   2. The RIGHT button on the mouse
>      does a different thing from
>      the LEFT button. Pay attention
>      to this.

> Number 2 was the hard part.

> ── PART 1: SOLITAIRE TEACHES LEFT ──

> Wes Cherry, an intern at Microsoft,
> wrote SOLITAIRE for Windows 3.0
> to teach left-click and drag-and-
> drop. Cherry never got paid for
> it. (Microsoft has discussed this
> publicly in retrospectives. The
> intern was Wes Cherry. The
> ridiculous fact is true.)

> Solitaire shipped in every copy
> of Windows from 3.0 forward.
> Office workers worldwide spent
> billions of hours learning to
> drag a card from one pile to
> another. By the time Windows 95
> arrived, they had collectively
> internalised the mouse.

> Solitaire was Microsoft's most-
> widely-deployed software product
> for its first 15 years of
> existence, by usage hours. Not
> Word. Not Excel. Solitaire.

> ── PART 2: MINES TEACHES RIGHT ──

> So that's the left button. What
> about the right button?

> I wrote MINESWEEPER in late 1989,
> for what would become the
> Microsoft Entertainment Pack 1
> (Windows 3.0 add-on, retail ${'$'}29.95).
> The mechanic was based on a couple
> of older mainframe games — RELENTLESS
> LOGIC (1973), MINED-OUT (1983),
> CUBE (1981, an MS-DOS game by Conway).
> The idea: a grid. Mines hidden
> beneath cells. Click a cell.
> Either it's empty (revealing
> a number telling you how many
> mines are adjacent), or it's a
> mine and the game ends.

> The right-click mechanic was a
> "FLAG". The player would suspect
> a cell was a mine. They couldn't
> reveal it (that would set off the
> mine). Instead, they right-clicked
> the cell, planting a little flag
> on it. The flag does nothing
> mechanical except mark the cell
> visually and prevent accidental
> left-clicks. The flag is the
> player's notebook.

> The right-click in Minesweeper was
> the FIRST time many office workers
> were ever introduced to the right
> mouse button as a deliberate,
> useful, distinct action.

> They learned. Two years later,
> Windows 95 shipped with right-
> click context menus everywhere
> (right-click a file: properties,
> rename, delete, etc.), and
> office workers all over the world
> just KNEW what to do, because of
> Minesweeper.

> Charles Fitzgerald, who was on
> the Windows team in those years,
> told me in 1998: "Minesweeper
> taught my mother right-click.
> She still cannot tell you what
> right-click does in the abstract.
> But she'll find a mine and flag
> it. She knows."

> ── PART 3: THE MATHS ──

> Minesweeper is harder than it
> looks. It is NP-complete to
> determine whether an arbitrary
> Minesweeper position is solvable
> without guessing. (Richard Kaye,
> University of Birmingham, 2000,
> proved this in the journal
> Mathematical Intelligencer.)

> What that means in practice is:
> there exist positions in
> Minesweeper where the only way
> forward is to guess. There is no
> chain of deductive logic. You
> guess. Pure probability. 50/50.
> If you guess right, the game
> continues. If you guess wrong,
> you set off the last mine and
> die.

> The expert level (16x30 grid, 99
> mines) is solvable about 95% of
> the time by a perfect player.
> The remaining 5% are guess-
> dependent positions where you
> will lose to luck.

> This makes Minesweeper one of the
> few games where the player's
> SKILL CEILING is set by the
> mathematics of the puzzle and not
> by their reflexes.

> ── PART 4: WHAT WE BUILT ──

> Below is MINES. Three difficulty
> levels (EASY 9x9 with 10 mines,
> MEDIUM 12x12 with 25, HARD 16x16
> with 40). Click a cell to reveal.
> Long-press to flag. First click
> is guaranteed safe (we generate
> the mine field AFTER the first
> click — a tradition that Microsoft
> implemented from version 5 of
> Minesweeper onward).

> Win condition: all non-mine cells
> revealed.

> A win-screen with a small piece
> of confetti, because (a) my
> daughter complained that the
> original Microsoft Minesweeper
> had no win-screen, and (b) she
> was right.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> MINESWEEPER is a trademark of
> Microsoft Corporation in the
> context of their specific
> Microsoft Minesweeper software
> product. The mathematical mechanic
> — a grid, mines, adjacent-cell
> numbers, deduction — is older
> than Microsoft (RELENTLESS LOGIC
> 1973) and is in the public domain.
> Our tribute is called MINES.
> Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Вот история. Я был в Microsoft
> с 1988-го, работал в команде
> Windows. Продукт — Windows 3.0,
> первая версия, которая
> кому-то за пределами компании
> действительно нравилась.
> Команда UI была одержима
> одним: мышью.

> Большинство офисных
> работников в 1989-м никогда
> не трогали мышь. Lotus 1-2-3
> работал в DOS. WordPerfect
> работал в DOS. Все печатали
> команды. Мышь для типичного
> бухгалтера в Чикаго в
> 1989-м была странным
> пластиковым грызуном, что
> шёл с Macintosh и отвечал,
> как-то, за воспринимаемую
> «игривость» компьютеров
> Apple — и, следовательно,
> их НЕ-серьёзность как
> бизнес-инструмента.

> Команда Windows должна была
> научить американских офисных
> работников двум вещам
> одновременно:
>   1. Мышь — это хорошо.
>      Попробуй мышь.
>   2. ПРАВАЯ кнопка мыши
>      делает не то же, что
>      ЛЕВАЯ. Обрати на это
>      внимание.

> Номер 2 был сложной частью.

> ── ЧАСТЬ 1: SOLITAIRE УЧИТ ЛЕВОЙ ──

> Уэс Черри, стажёр в
> Microsoft, написал SOLITAIRE
> для Windows 3.0, чтобы
> научить левому клику и
> drag-and-drop. Черри так и
> не получил за это денег.
> (Microsoft публично обсуждали
> это в ретроспективах.
> Стажёром был Уэс Черри.
> Нелепый факт — правда.)

> Solitaire поставлялся в
> каждой копии Windows с 3.0.
> Офисные работники по всему
> миру провели миллиарды
> часов, учась тянуть карту
> из одной кучки в другую.
> К моменту Windows 95 они
> коллективно усвоили мышь.

> Solitaire был самым широко
> развёрнутым программным
> продуктом Microsoft в первые
> 15 лет существования, по
> часам использования. Не
> Word. Не Excel. Solitaire.

> ── ЧАСТЬ 2: MINES УЧИТ ПРАВОЙ ──

> Это левая кнопка. А правая?

> Я написал MINESWEEPER в
> конце 1989-го, для того,
> что станет Microsoft
> Entertainment Pack 1
> (Windows 3.0 add-on, цена
> 29.95). Механика основана на
> паре старых мейнфрейм-игр —
> RELENTLESS LOGIC (1973),
> MINED-OUT (1983), CUBE
> (1981, MS-DOS-игра Конвея).
> Идея: сетка. Мины спрятаны
> под клетками. Клик по
> клетке. Либо она пустая
> (открывает число, говорящее
> сколько мин рядом), либо
> мина и игра кончается.

> Механика правого клика —
> «ФЛАГ». Игрок подозревает,
> что клетка — мина. Открыть
> нельзя (мина сработает).
> Вместо этого правый клик
> ставит маленький флажок.
> Флажок ничего не делает
> механически — только
> визуально помечает клетку и
> предотвращает случайный
> левый клик. Флажок —
> блокнот игрока.

> Правый клик в Minesweeper
> был ПЕРВЫМ разом, когда
> многие офисные работники
> познакомились с правой
> кнопкой мыши как с
> намеренным, полезным,
> отдельным действием.

> Они научились. Через два
> года Windows 95 вышла с
> правокликовыми контекстными
> меню везде (правый клик по
> файлу: свойства,
> переименовать, удалить и
> т.д.), и офисные работники
> по всему миру просто
> ЗНАЛИ, что делать, благодаря
> Minesweeper.

> Чарльз Фитцджеральд, бывший
> в команде Windows в те годы,
> сказал мне в 1998-м:
> «Minesweeper научил мою
> маму правому клику. Она по
> сей день не может сказать,
> что правый клик делает в
> абстрактном смысле. Но она
> найдёт мину и пометит её.
> Она знает.»

> ── ЧАСТЬ 3: МАТЕМАТИКА ──

> Minesweeper сложнее, чем
> кажется. Определить,
> разрешима ли произвольная
> позиция Minesweeper без
> угадывания, — NP-полная
> задача. (Ричард Кэй,
> Бирмингемский университет,
> 2000-й, доказал это в
> Mathematical Intelligencer.)

> На практике это значит:
> существуют позиции в
> Minesweeper, где
> единственный путь вперёд —
> угадать. Нет цепочки
> дедуктивной логики. Ты
> угадываешь. Чистая
> вероятность. 50/50.
> Угадал — игра продолжается.
> Не угадал — подрываешь
> последнюю мину и умираешь.

> Экспертный уровень (16×30
> сетка, 99 мин) решаем около
> 95% времени совершенным
> игроком. Оставшиеся 5% —
> позиции, зависящие от
> угадывания, где ты
> проиграешь удаче.

> Это делает Minesweeper
> одной из немногих игр, где
> ПОТОЛОК НАВЫКА игрока
> поставлен математикой
> головоломки, а не его
> рефлексами.

> ── ЧАСТЬ 4: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — MINES. Три уровня
> сложности (ЛЁГКИЙ 9×9 с 10
> минами, СРЕДНИЙ 12×12 с 25,
> СЛОЖНЫЙ 16×16 с 40).
> Кликни клетку, чтобы
> открыть. Долгий тап — флаг.
> Первый клик гарантированно
> безопасен (мы генерируем
> минное поле ПОСЛЕ первого
> клика — традиция, которую
> Microsoft внедрила с
> версии 5 Minesweeper).

> Условие победы: все
> неминные клетки открыты.

> Win-экран с маленьким
> кусочком конфетти, потому
> что (а) моя дочь жаловалась,
> что у оригинального
> Microsoft Minesweeper не
> было win-экрана, и (б) она
> была права.

> ── СНОСКА У КОСТРА ──

> MINESWEEPER — торговая марка
> Microsoft Corporation в
> контексте их конкретного
> программного продукта
> Microsoft Minesweeper.
> Математическая механика —
> сетка, мины, числа смежных
> клеток, дедукция — старше
> Microsoft (RELENTLESS LOGIC
> 1973) и в public domain.
> Наша дань называется MINES.
> Вопросы: elyssov@gmail.com.
""".trimIndent()

val MINES_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val MINES_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickMinesIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> MINES_INTRO_RU
    else -> MINES_INTRO_EN
}

val MINES_INTRO_TEXT: String = MINES_INTRO_EN
