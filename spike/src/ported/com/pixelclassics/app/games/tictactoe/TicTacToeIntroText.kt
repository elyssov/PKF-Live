package com.pixelclassics.app.games.tictactoe

/**
 * Long-form campfire intro for TIC-TAC-TOE. Narrated by Alexander
 * Sandy Douglas, who in 1952 wrote OXO on the Cambridge EDSAC.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
██████████████████████████████████████████
█  EDSAC · CAMBRIDGE · 1952             █
█  OXO · A.S.DOUGLAS · PhD THESIS       █
█  35×16 CRT · ROTARY-DIAL INPUT        █
██████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> ── BEFORE PART 1: SO MUCH OLDER ──

> Before any of this — before
> Cambridge, before EDSAC, before
> the word "computer" referred to
> anything other than a human
> being who did arithmetic for a
> living — tic-tac-toe was already
> ancient.

> It is one of the OLDEST GAMES ON
> PAPER known to humanity. Older
> than chess (chess emerged in
> 6th-century India). Older than
> backgammon's modern form (10th-
> century Persia). Older than
> dominoes (12th-century China),
> older than playing cards (9th-
> century China), older than
> draughts/checkers (12th-century
> France).

> Archaeologists have found
> 3x3-grid scratches on Roman
> stone pavements from the
> 1st-century imperial period —
> the game was called TERNI
> LAPILLI ("three pebbles").
> Older grids have been found
> carved into the roof tiles of
> the temple of Kurna in Egypt,
> dated to roughly 1400 BCE.

> It is, by an enormous margin,
> the most widely-known game in
> human history.

> And then, in 1952, it became
> one of the very first games
> ever played on a computer.

> Here is the man who put it
> there.

> ── ──

> [Dr. Douglas, from his
>  Cambridge college rooms,
>  recorded interview, 1996.]

> "Oh, the noughts and crosses
> programme. Yes, that one. I
> was twenty-one when I wrote
> it. It was my doctoral
> thesis."

> Hello, dear listener of the
> twenty-first century. My name
> is Alexander Sandy Douglas. In
> the spring of 1952 I was a
> graduate student at the
> University of Cambridge,
> studying for a PhD in
> mathematics under Professor
> Maurice Wilkes.

> Professor Wilkes had recently
> finished building EDSAC, the
> Electronic Delay Storage
> Automatic Calculator — the
> first practical computer in
> the world to use the von
> Neumann stored-program
> architecture. EDSAC was a
> room-sized machine. Mercury
> delay lines for memory. About
> 3,000 vacuum tubes.

> ── PART 1: THE THESIS ──

> My doctoral topic was
> HUMAN-COMPUTER INTERACTION —
> a phrase that did not yet
> exist in the way it does
> today. I was studying how
> people might interact with
> machines in a structured
> way. Nobody knew. There were
> only a few computers in the
> world.

> I needed a CASE STUDY for my
> thesis. Something simple and
> well-defined that a computer
> could play with a human. The
> case study had to fit in
> EDSAC's memory — about 1,024
> thirty-five-bit words.

> EDSAC's INPUT for an
> interactive game was a
> dial — physically, the rotary
> dial of an old British
> telephone, repurposed and
> wired to EDSAC's electronics.
> You dialled a digit 1-9 to
> select one of nine cells.

> EDSAC's OUTPUT was a small
> cathode-ray tube monitoring
> screen, originally intended
> for debugging. By writing the
> screen dot by dot, I could
> draw a three-by-three grid
> with X and O markers in the
> cells.

> ── PART 2: NOUGHTS AND CROSSES ──

> I picked TIC-TAC-TOE (called
> NOUGHTS AND CROSSES in
> British English). The reasons:
>   1. Everyone knows the rules.
>   2. The game is small.
>   3. The game is finite.
>   4. The game is SOLVED — a
>      perfect player can
>      guarantee at least a
>      draw. This is important
>      because the COMPUTER
>      should be the perfect
>      player.

> I called the program OXO,
> which sounded friendlier than
> NOUGHTS AND CROSSES.

> The program implemented the
> standard MINIMAX search
> algorithm — evaluate every
> possible move, every
> possible response, all the
> way to the end of the game,
> then pick the move that
> minimises the human's best
> possible outcome.

> OXO ran in approximately
> 400 EDSAC words of memory.

> ── PART 3: WHAT I DISCOVERED ──

> The pattern of behaviour I
> observed in human players
> was fascinating.

> First — players learned
> quickly that they could not
> WIN against OXO. After
> their fifth or sixth game,
> they realised every game
> ended in a draw at best.
> The pattern of their
> behaviour SHIFTED at that
> point — from trying to win,
> to trying to find a
> WEAKNESS in OXO's play.

> They started experimenting.
> Unusual opening moves.
> Sacrificial gambits. Trying
> to PROVOKE OXO into a
> mistake. None of them ever
> made OXO blunder — minimax
> cannot blunder at 3x3 tic-
> tac-toe — but they all
> spent hours testing.

> The other pattern was the
> emergence of a "RUSH". A
> player who had drawn three
> games in a row would
> sometimes start playing
> RAPIDLY, as if accelerating
> through the game would
> somehow change the outcome.

> These observations became
> central to my thesis. Human
> behaviour against a perfect
> opponent follows
> predictable, even
> categorisable, emotional
> arcs. This was, in 1952,
> a brand-new observation.

> ── PART 4: WHAT HAPPENED NEXT ──

> I completed my PhD in 1954.
> I left Cambridge that year
> for a career in industrial
> computing — first at LEO
> Computers. I never wrote
> another video game.

> The original OXO program is
> lost. The Computer
> Conservation Society of
> Britain has reconstructed
> it from my thesis
> documentation and runs it on
> a reconstructed EDSAC
> emulator at the Cambridge
> Centre for Computing
> History; if you visit, you
> can play it.

> ── PART 5: WHAT WE BUILT ──

> Below is TIC-TAC-TOE. Our
> tribute. A 3x3 grid. Two
> game modes:
>   VS CPU — you against an
>     intentionally IMPERFECT
>     AI. Our AI plays a
>     simple priority-based
>     heuristic (win → block →
>     centre → corner →
>     random). This means YOU
>     CAN WIN. This is not
>     OXO. OXO was perfect.
>     Our AI is approachable.
>   VS PLAYER — you and a
>     friend take turns on the
>     same screen.

> Click a cell to place your
> mark. Three in a row wins.

> The game is, in the
> language of game theory,
> "solved". With perfect play
> on both sides, every game
> is a draw. The best
> tic-tac-toe is therefore
> played against a HUMAN —
> someone fallible, someone
> distractible, someone you
> love.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> Tic-tac-toe is a folk game
> dating to the Roman Empire.
> The mechanic is in the
> public domain by 2,000
> years' margin. OXO was
> A.S. Douglas's PhD thesis
> work in 1952; the
> reconstructed program is
> maintained by the Computer
> Conservation Society. Our
> tribute is called
> TIC-TAC-TOE. Concerns:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> ── ДО ЧАСТИ 1: НАМНОГО СТАРШЕ ──

> До всего этого — до
> Кембриджа, до EDSAC, до
> того, как слово
> «компьютер» означало
> что-либо, кроме человека,
> зарабатывавшего
> арифметикой на жизнь —
> крестики-нолики уже были
> древними.

> Это одна из СТАРЕЙШИХ ИГР
> НА БУМАГЕ, известных
> человечеству. Старше
> шахмат (шахматы появились
> в Индии 6-го века).
> Старше современной формы
> нард (Персия 10-го века).
> Старше домино (Китай
> 12-го века), старше
> игральных карт (Китай
> 9-го века), старше
> шашек (Франция 12-го
> века).

> Археологи нашли царапины
> сетки 3×3 на римских
> каменных мостовых
> 1-го века имперского
> периода — игра
> называлась TERNI
> LAPILLI («три камешка»).
> Более старые сетки
> найдены вырезанными на
> черепице храма Курна в
> Египте, датируется
> примерно 1400 до н.э.

> Это, с огромным отрывом,
> самая широко известная
> игра в истории
> человечества.

> А потом, в 1952-м, она
> стала одной из самых
> первых игр, когда-либо
> сыгранных на компьютере.

> Вот человек, что её туда
> положил.

> ── ──

> [Доктор Дуглас из своих
>  кембриджских комнат,
>  записанное интервью,
>  1996-й.]

> «О, программка
> крестиков-ноликов. Да, та
> самая. Мне был двадцать
> один, когда я её написал.
> Это была моя докторская
> диссертация.»

> Привет, дорогой слушатель
> двадцать первого века.
> Меня зовут Александер
> Сэнди Дуглас. Весной
> 1952-го я был аспирантом
> в Кембриджском университете,
> учился на PhD по
> математике под
> руководством профессора
> Мориса Уилкса.

> Профессор Уилкс недавно
> закончил строить EDSAC —
> Electronic Delay Storage
> Automatic Calculator —
> первый практический
> компьютер в мире,
> использующий
> фон-неймановскую
> архитектуру с хранимой
> программой. EDSAC —
> машина размером с
> комнату. Ртутные линии
> задержки для памяти.
> Около 3000 вакуумных
> ламп.

> ── ЧАСТЬ 1: ДИССЕРТАЦИЯ ──

> Моя докторская тема —
> ЧЕЛОВЕКО-МАШИННОЕ
> ВЗАИМОДЕЙСТВИЕ — фраза,
> не существовавшая в том
> виде, как сегодня. Я
> изучал, как люди могут
> взаимодействовать с
> машинами структурированно.
> Никто не знал. В мире
> было всего несколько
> компьютеров.

> Мне нужен был
> ИССЛЕДОВАТЕЛЬСКИЙ
> ПРИМЕР для диссертации.
> Что-то простое и
> точно определённое, во
> что компьютер мог играть
> с человеком. Пример
> должен был поместиться
> в память EDSAC — около
> 1024 35-битных слов.

> ВВОД EDSAC для
> интерактивной игры — это
> диск, физически —
> вращающийся диск старого
> британского телефона,
> перепрофилированный и
> подключённый к
> электронике EDSAC. Ты
> набирал цифру 1-9, чтобы
> выбрать одну из девяти
> клеток.

> ВЫВОД EDSAC — маленький
> монитор-катодная-трубка,
> изначально для дебага.
> Прописывая экран точка за
> точкой, я мог нарисовать
> сетку 3×3 с X и O в
> клетках.

> ── ЧАСТЬ 2: КРЕСТИКИ-НОЛИКИ ──

> Я выбрал
> крестики-нолики
> (по-британски —
> NOUGHTS AND CROSSES).
> Причины:
>   1. Все знают правила.
>   2. Игра маленькая.
>   3. Игра конечна.
>   4. Игра РЕШЕНА —
>      идеальный игрок может
>      гарантировать как
>      минимум ничью. Это
>      важно, потому что
>      КОМПЬЮТЕР должен быть
>      идеальным игроком.

> Я назвал программу OXO,
> что звучало дружелюбнее,
> чем NOUGHTS AND CROSSES.

> Программа реализовывала
> стандартный
> поисковый алгоритм
> МИНИМАКС — оценить
> каждый возможный ход,
> каждый возможный ответ,
> до самого конца игры,
> затем выбрать ход,
> минимизирующий лучший
> возможный исход для
> человека.

> OXO работал примерно в
> 400 словах памяти EDSAC.

> ── ЧАСТЬ 3: ЧТО Я ОТКРЫЛ ──

> Образец поведения,
> который я наблюдал у
> игроков-людей, был
> увлекательным.

> Первое — игроки быстро
> учились, что ВЫИГРАТЬ у
> OXO они не могут. После
> пятой-шестой партии они
> понимали, что лучшее —
> ничья. Образец
> поведения СМЕЩАЛСЯ в
> этот момент — с попыток
> выиграть на попытки
> найти СЛАБОСТЬ в игре
> OXO.

> Они начинали
> экспериментировать.
> Необычные первые ходы.
> Жертвенные гамбиты.
> Пытались
> СПРОВОЦИРОВАТЬ OXO на
> ошибку. Никто никогда
> не заставил OXO
> ошибиться — минимакс не
> может ошибиться в 3×3
> крестиках-ноликах — но
> все часами проверяли.

> Другой образец —
> появление «РАЗГОНА».
> Игрок, сделавший три
> ничьих подряд, иногда
> начинал играть БЫСТРО,
> как будто разгон по
> игре каким-то образом
> поменяет исход.

> Эти наблюдения стали
> центральными для моей
> диссертации. Поведение
> человека против
> идеального противника
> следует предсказуемым,
> даже категоризируемым
> эмоциональным дугам.
> Это было, в 1952-м,
> совершенно новое
> наблюдение.

> ── ЧАСТЬ 4: ЧТО БЫЛО ДАЛЬШЕ ──

> Я закончил PhD в 1954-м.
> В том же году ушёл из
> Кембриджа в карьеру в
> промышленных вычислениях
> — сначала в LEO
> Computers. Больше я
> никогда не писал
> видеоигр.

> Оригинальная программа
> OXO утеряна. Computer
> Conservation Society
> Британии реконструировало
> её из документации моей
> диссертации и запускает
> на реконструированном
> эмуляторе EDSAC в
> Кембриджском центре
> истории вычислений; если
> придёшь — можешь сыграть.

> ── ЧАСТЬ 5: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — TIC-TAC-TOE.
> Наша дань. Сетка 3×3.
> Два игровых режима:
>   VS CPU — ты против
>     намеренно
>     НЕИДЕАЛЬНОГО AI. Наш
>     AI играет простой
>     приоритетной эвристикой
>     (выигрыш → блок →
>     центр → угол →
>     случайно). Это
>     значит, что ТЫ МОЖЕШЬ
>     ВЫИГРАТЬ. Это не OXO.
>     OXO был идеален.
>     Наш AI доступен.
>   VS PLAYER — ты и друг
>     ходите по очереди на
>     одном экране.

> Кликни клетку, чтобы
> поставить метку. Три в
> ряд — победа.

> Игра, на языке теории
> игр, «решена». При
> идеальной игре с обеих
> сторон каждая партия —
> ничья. Лучшие
> крестики-нолики поэтому
> играются против
> ЧЕЛОВЕКА — кого-то
> ошибающегося,
> отвлекающегося, того,
> кого ты любишь.

> ── СНОСКА У КОСТРА ──

> Крестики-нолики —
> народная игра,
> восходящая к Римской
> империи. Механика — в
> public domain с запасом
> в 2000 лет. OXO —
> работа PhD-диссертации
> А.С. Дугласа в 1952-м;
> реконструированная
> программа поддерживается
> Computer Conservation
> Society. Наша дань
> называется TIC-TAC-TOE.
> Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val TIC_TAC_TOE_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val TIC_TAC_TOE_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickTicTacToeIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> TIC_TAC_TOE_INTRO_RU
    else -> TIC_TAC_TOE_INTRO_EN
}

val TIC_TAC_TOE_INTRO_TEXT: String = TIC_TAC_TOE_INTRO_EN
