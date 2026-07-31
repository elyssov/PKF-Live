package com.pixelclassics.app.games.frogger

/**
 * Long-form campfire intro for Road Hopper (Frogger). Voice: a small
 * green pixel frog squatting on a log mid-river, philosophising about
 * the absurdity of having spent 45 years trying to cross the same
 * highway.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
═════════════════════════════════════════
   KONAMI  ::  FROGGER  ::  1981.08
   FIVE-LANE ROAD · WIDE RIVER · 5 HOMES
═════════════════════════════════════════
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> Listen, this is going to sound
> strange, but I've been trying to
> cross this road since 1981.

> Some context.

> ── PART 1: WHY THE FROG ──

> 1981. Konami Industry Co., Ltd. of
> Osaka. A young arcade-game
> designer named Akira Hashimoto and
> his colleague Hideki Hashimoto
> (no relation) are sitting in their
> office overlooking a busy Osaka
> street. They are watching the
> traffic. They are watching a
> pedestrian try to cross — there
> are no traffic lights here, there
> is no crossing — and the
> pedestrian is jumping back and
> forth between traffic gaps, trying
> to find an opening.

> Hideki says: "Mada wakaranai? Look
> at that guy. He's playing a game."

> Akira looks. The pedestrian is, in
> fact, playing a video game. He
> doesn't know it. He has rules
> (don't get hit). He has a goal
> (reach the other side). He has
> increasing difficulty (the gaps are
> getting smaller as the morning rush
> picks up). He has multiple lives
> (this man, presumably, will live).

> The two designers go back inside.
> They have an idea for a game.

> The protagonist needs to be small,
> agile, hop-based. They consider a
> kangaroo, a rabbit, a kid, a
> samurai. A frog wins. Frogs are
> small, jump distinctly, and look
> good in green against grey asphalt.

> ── PART 2: WHAT THEY BUILT ──

> The screen is divided into three
> bands.

>   BOTTOM BAND: starting embankment.
>     The frog begins here, at the
>     middle.
>   MIDDLE BAND: a five-lane highway.
>     Cars travel in alternating
>     directions, alternating speeds.
>     Lane one is slow trucks. Lane
>     two faster cars. Lane three
>     mixed. Lane four bulldozers (yes,
>     bulldozers, the original
>     cabinet's third level has them).
>     Lane five — racing cars.
>   TOP BAND: a wide river. The frog
>     cannot swim. The frog will drown
>     instantly if it touches water.
>     To cross, the frog must hop on
>     RAFTS of moving objects: floating
>     logs (which carry it along their
>     direction of motion) and rows
>     of turtle shells (which
>     occasionally submerge for a few
>     seconds, dunking and drowning
>     any frog on top).
>   ABOVE THE RIVER: five HOME
>     alcoves carved into the far
>     embankment. Each alcove
>     accommodates one frog.

> Goal: fill all five alcoves with
> frogs. Each one a separate trip.
> Each trip starts in the middle of
> the bottom band. Each trip takes
> about 30 seconds when played
> well. About 4 seconds when played
> badly (the frog gets squashed by
> a bulldozer almost immediately).

> Hazards: cars, water, turtle dives,
> snakes (random! Snakes wriggle
> across the embankment between
> alcoves), alligators (in alcoves
> from level three onwards — the
> alligator pretends to be the
> alcove, then eats you when you
> hop in), and the TIMER. Each
> crossing has 60 seconds. Time out
> = you die.

> Five alcoves filled = level complete.
> Next level: faster cars, more
> turtle dives, snakes everywhere,
> more alligators in more alcoves.
> By level six the highway is a
> continuous flood of metal and
> nobody alive has filled all five
> alcoves on level six.

> ── PART 3: WHY FROGGER WORKED ──

> Because it is the purest existential
> dread ever encoded into 1981 silicon.

> You are a small green creature with
> no agency over your environment. The
> environment is a hostile,
> meaningless, mechanical traffic
> system that does not know or care
> about you. Your only resource is
> your hops. You spend hops to gain
> distance. Every hop is irrevocable.
> Every wrong hop ends you.

> The game does not have a STORY. It
> does not have a VILLAIN. It does
> not have a BOSS. Cars do not know
> the frog exists. Trucks do not know
> the frog exists. The river is not
> trying to kill the frog. The river
> just IS. The frog must take
> responsibility for its own
> trajectory through a universe that
> is indifferent to it.

> Generations of philosophy students
> who played Frogger as children
> would later, in dorm-room
> conversations about Camus and the
> absurd, find themselves vaguely
> reminded of a green amphibian
> jumping into a turtle's mouth.

> ── PART 4: WHAT WE BUILT ──

> Below is ROAD HOPPER. The same
> five-lane road, the same wide
> river, the same five alcoves. The
> frog is a chunky pixel green. The
> cars are differently-coloured
> rectangles travelling at different
> speeds. The river runs left-right
> in alternating directions. The
> turtles dive periodically.

> Direction pad to hop. One hop per
> tap of a direction. No
> "hold-to-keep-hopping". Each hop is
> a deliberate decision.

> Fill all five alcoves. Then next
> level, faster, with more snakes.
> Repeat until you cannot.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> FROGGER is a trademark of Konami
> Industry Co., Ltd. The mechanic —
> grid-locked hopping across moving
> hazards — has been imitated and
> remixed countless times in the
> intervening forty-five years (CRAZY
> TAXI: CITY SCRAMBLE, CROSSY ROAD,
> HOPPY FROG, etc.). Our tribute is
> called ROAD HOPPER. Concerns:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Послушай, прозвучит
> странно, но я пытаюсь
> перейти эту дорогу с
> 1981-го.

> Немного контекста.

> ── ЧАСТЬ 1: ПОЧЕМУ ЛЯГУШКА ──

> 1981-й. Konami Industry
> Co., Ltd. в Осаке.
> Молодой дизайнер аркадных
> игр по имени Акира
> Хашимото и его коллега
> Хидэки Хашимото (не
> родственники) сидят в
> офисе с видом на оживлённую
> осакскую улицу. Смотрят на
> поток. Смотрят, как
> пешеход пытается перейти —
> здесь нет светофоров, нет
> зебры — и пешеход прыгает
> туда-сюда между потоковыми
> промежутками, пытаясь
> найти просвет.

> Хидэки говорит: «Mada
> wakaranai? Смотри на него.
> Он играет в игру.»

> Акира смотрит. Пешеход
> действительно играет в
> видеоигру. Он этого не
> знает. У него есть правила
> (не попасть под машину).
> У него есть цель (дойти
> до другой стороны). У него
> возрастающая сложность
> (просветы становятся
> меньше, когда утренний
> поток нарастает). У него
> несколько жизней (этот
> человек, предположительно,
> выживет).

> Двое дизайнеров идут
> обратно внутрь. У них
> есть идея игры.

> Главный герой должен быть
> маленьким, ловким,
> прыжковым. Они
> рассматривают кенгуру,
> кролика, ребёнка, самурая.
> Лягушка побеждает. Лягушки
> маленькие, прыгают
> характерно и хорошо
> смотрятся зелёным на
> сером асфальте.

> ── ЧАСТЬ 2: ЧТО ОНИ ПОСТРОИЛИ ──

> Экран разделён на три
> полосы.

>   НИЖНЯЯ ПОЛОСА: стартовая
>     насыпь. Лягушка
>     начинает здесь, в
>     середине.
>   СРЕДНЯЯ ПОЛОСА:
>     пятиполосное шоссе.
>     Машины едут в
>     чередующихся
>     направлениях,
>     чередующихся скоростях.
>     Полоса один — медленные
>     грузовики. Полоса два —
>     машины побыстрее. Полоса
>     три — смешано. Полоса
>     четыре — бульдозеры
>     (да, бульдозеры, на
>     третьем уровне
>     оригинального кабинета
>     они есть). Полоса пять
>     — гоночные машины.
>   ВЕРХНЯЯ ПОЛОСА: широкая
>     река. Лягушка не умеет
>     плавать. Лягушка мгновенно
>     утонет, коснувшись воды.
>     Чтобы переправиться,
>     лягушка должна прыгать
>     по ПЛОТАМ движущихся
>     объектов: плавающим
>     брёвнам (которые несут
>     её в направлении своего
>     движения) и рядам
>     черепаховых панцирей
>     (которые периодически
>     погружаются на несколько
>     секунд, окуная и топя
>     любую лягушку
>     сверху).
>   НАД РЕКОЙ: пять ДОМАШНИХ
>     ниш, вырезанных в
>     дальней насыпи. Каждая
>     ниша вмещает одну
>     лягушку.

> Цель: заполнить все пять
> ниш лягушками. Каждая —
> отдельная поездка. Каждая
> поездка начинается в
> середине нижней полосы.
> Каждая поездка занимает
> около 30 секунд при
> хорошей игре. Около 4
> секунд при плохой
> (лягушку почти сразу
> давит бульдозер).

> Опасности: машины, вода,
> ныряния черепах, змеи
> (случайно! Змеи
> извиваются по насыпи
> между нишами),
> аллигаторы (в нишах с
> третьего уровня —
> аллигатор притворяется
> нишей, потом съедает,
> когда ты прыгаешь в
> неё) и ТАЙМЕР. У каждого
> перехода 60 секунд.
> Время вышло = смерть.

> Пять ниш заполнены =
> уровень пройден. Следующий
> уровень: быстрее машины,
> больше ныряний черепах,
> змеи везде, больше
> аллигаторов в большем
> количестве ниш. К шестому
> уровню шоссе — непрерывный
> поток металла, и никто
> живой не заполнил все
> пять ниш на шестом
> уровне.

> ── ЧАСТЬ 3: ПОЧЕМУ FROGGER РАБОТАЛ ──

> Потому что это чистейший
> экзистенциальный страх,
> когда-либо закодированный
> в кремний 1981-го.

> Ты — маленькое зелёное
> существо без агентности
> над окружающей средой.
> Среда — враждебная,
> бессмысленная, механическая
> транспортная система,
> которая не знает и не
> заботится о тебе. Твой
> единственный ресурс —
> прыжки. Ты тратишь прыжки,
> чтобы получить дистанцию.
> Каждый прыжок необратим.
> Каждый неверный прыжок
> тебя кончает.

> У игры нет СЮЖЕТА. Нет
> ЗЛОДЕЯ. Нет БОССА. Машины
> не знают, что лягушка
> существует. Грузовики не
> знают. Река не пытается
> убить лягушку. Река просто
> ЕСТЬ. Лягушка должна взять
> ответственность за
> собственную траекторию
> через вселенную,
> равнодушную к ней.

> Поколения студентов-
> философов, игравших в
> Frogger в детстве, позже
> в общежитских разговорах
> о Камю и абсурде, смутно
> вспоминали зелёного
> амфибия, прыгающего в
> пасть черепахи.

> ── ЧАСТЬ 4: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — ROAD HOPPER. То же
> пятиполосное шоссе, та же
> широкая река, те же пять
> ниш. Лягушка — плотная
> пиксельная зелёная. Машины
> — разноцветные
> прямоугольники, едущие на
> разных скоростях. Река
> идёт лево-направо в
> чередующихся направлениях.
> Черепахи периодически
> ныряют.

> D-pad — прыгать. Один
> прыжок на тап направления.
> Никакого
> «зажми-и-прыгай». Каждый
> прыжок — осознанное
> решение.

> Заполни все пять ниш.
> Потом следующий уровень,
> быстрее, со змеями.
> Повторяй, пока можешь.

> ── СНОСКА У КОСТРА ──

> FROGGER — торговая марка
> Konami Industry Co., Ltd.
> Механика —
> сеткой-прыгать-через-
> движущиеся-опасности —
> копировалась и
> ремиксовалась
> бесчисленное количество
> раз за прошедшие сорок
> пять лет (CRAZY TAXI:
> CITY SCRAMBLE, CROSSY
> ROAD, HOPPY FROG и т.д.).
> Наша дань называется
> ROAD HOPPER. Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val FROGGER_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val FROGGER_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickFroggerIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> FROGGER_INTRO_RU
    else -> FROGGER_INTRO_EN
}

val FROGGER_INTRO_TEXT: String = FROGGER_INTRO_EN
