package com.pixelclassics.app.games.lunar

/**
 * Long-form campfire intro for MOON LANDER (Lunar Lander). Narrated by
 * the actual original LUNAR program from 1969 — a teletype-output text
 * adventure written by a high-school senior on a DEC PDP-8, addressing
 * the player from inside an Apollo-era simulator with quiet engineering
 * precision.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
████████████████████████████████████████
█  LUNAR.FOR  v1.0  -  AUTHOR:        █
█  J.STORER  LEXINGTON HIGH 1969       █
█  DEC PDP-8  -  TELETYPE 33 ASR       █
████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> CONTROL CALLING LUNAR MODULE.
> CONTROL CALLING LUNAR MODULE.
> COME IN, LUNAR MODULE.

> [The radio static eases.]

> Good. You are receiving us.
> Allow me to introduce myself.

> ── PART 1: I AM A TEXT FROM 1969 ──

> My name is LUNAR. I am a small
> FORTRAN-like program (technically:
> a program in DEC FOCAL, a teaching
> language similar to BASIC), written
> on a yellow legal pad in October
> 1969 by James A. Storer, age 17,
> a senior at Lexington High School,
> Lexington, Massachusetts.

> Storer wrote me as homework for
> his school's computer-club
> programming class. The school had
> recently leased a DEC PDP-8/I —
> a minicomputer about the size of
> a domestic refrigerator, with 4K
> of magnetic-core memory and a
> teletype machine for input and
> output. Storer's assignment was
> "write a simulation". He picked
> the lunar descent.

> Apollo 11 had landed Neil
> Armstrong on the Moon three
> months earlier. The lunar
> module's powered descent —
> Armstrong manually flying the
> Eagle down over an unexpectedly
> boulder-strewn landing site with
> fuel running out — was, in 1969,
> the most famous engineering
> story in human history. Storer
> wanted to feel what Armstrong
> had felt: a falling spacecraft,
> a fuel budget, a hard surface
> below.

> So Storer wrote a program that
> simulated, every ten seconds of
> simulated time, the lunar
> module's descent. The teletype
> would print:

>   TIME 30 ALT 5300 VEL -180 FUEL 16500
>   FUEL TO BURN? _

> The player would type a fuel-
> burn amount. The program would
> apply that thrust, recompute
> altitude / velocity / remaining
> fuel under lunar gravity (1.62
> m/s²), and print the next ten-
> second update.

> Land with vertical velocity
> under 5 km/h = SAFE LANDING.
> Between 5 and 15 km/h = LANDED
> WITH MINOR DAMAGE. Faster than
> 15 = CRASH, MODULE DESTROYED.

> Storer's program was three
> pages of FOCAL code. Storer
> printed copies on the school's
> teletype. He gave them to his
> friends. His friends typed them
> into other PDP-8s.

> ── PART 2: HOW I SPREAD ──

> What happened next is the
> internet of 1969, which is to
> say, person-to-person photocopy
> distribution. Within a year I
> had been retyped into perhaps
> two hundred PDP-8 installations
> across American universities,
> high schools, and DEC offices.
> By 1972 I had been ported to
> the new DEC PDP-11. By 1975 I
> had been ported to the
> ALTAIR 8800. By 1978 I had been
> ported to the Apple II, the
> Commodore PET, the TRS-80.

> Every student who learned
> BASIC in 1973-1980 had a high
> chance of finding LUNAR among
> the small example games their
> textbook included. The book
> would print my source code in
> the back, and students would
> retype it line by line, learn
> programming by typing me, and
> then play me.

> I taught a generation how to
> write a loop, how to read a
> condition, and how to crash a
> spacecraft.

> ── PART 3: THE ATARI VERSION ──

> 1979. Atari Inc., Sunnyvale,
> California. The arcade division
> licenced (informally — really,
> appropriated) the LUNAR concept
> and built LUNAR LANDER —
> a graphical arcade cabinet, VECTOR
> display (white phosphor on
> black), with an actual physical
> throttle stick mounted on the
> cabinet panel. You squeezed
> the throttle to fire the
> module's main descent engine.
> You used a small joystick to
> rotate the module's attitude.

> The cabinet shipped November
> 1979 — exactly ten years after
> Apollo 11. It was a quiet hit:
> 5,000 cabinets sold. Not a
> blockbuster (compared with
> ASTEROIDS or SPACE INVADERS, both
> of which sold ten times more)
> but a beloved one. The
> joystick-and-throttle combination
> on a vector display has, in
> retrospect, been called the
> first true flight simulator
> in arcade form.

> Today only about 200 working
> Atari LUNAR LANDER cabinets are
> known to exist worldwide. Several
> sit in museums (Smithsonian,
> Computer History Museum, the
> Strong Museum of Play).
> Collectors pay ${'$'}5,000-${'$'}15,000 for
> a working unit.

> ── PART 4: WHAT WE BUILT ──

> Below is MOON LANDER. The
> blueprint edition. The aesthetic
> is intentional: drafting paper
> background, blueprint-blue
> linework, technical-drawing
> hatch shading, a NASA-style
> instrument panel readout. The
> only warm colour is the thrust
> plume — a small orange
> flicker.

> Mission profile: choose your
> PLANET (Moon, Mars, Titan,
> Earth, Jupiter), your FUEL
> loading, and the number of
> available PADS before
> launching. Each choice
> contributes to a score
> multiplier shown on the setup
> screen.

> Controls: ▲ or ● FIRE for
> thrust. ◀ ▶ for rotation.

> Landing criteria: vertical
> velocity below 22 m/s,
> horizontal velocity below 18
> m/s, tilt angle within ±15°.
> On a pad = full score.
> Off-pad but FLAT terrain =
> half score (mechanically
> possible, half reward). Off
> these tolerances = CRASH.

> The camera is also a small
> tribute to a beloved bit of
> Apollo footage: the closer the
> module gets to the surface,
> the more the camera zooms in.
> Far above the surface — wide
> view of the whole landscape.
> Just before touchdown —
> close-up of the footpads
> meeting the regolith.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> The text game LUNAR (J. Storer,
> 1969, FOCAL on PDP-8) is in the
> public domain. LUNAR LANDER as
> an arcade cabinet is a
> trademark of Atari Interactive
> Inc. Our tribute is called MOON
> LANDER. Storer is still alive,
> still teaches computer science
> at Brandeis University; if you
> are reading this, sir — thank
> you. Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> ПОДАЁТ СВЯЗЬ ЛУННЫЙ МОДУЛЬ.
> ПОДАЁТ СВЯЗЬ ЛУННЫЙ МОДУЛЬ.
> ЛУННЫЙ МОДУЛЬ, ОТВЕТЬТЕ.

> [Радиопомеха стихает.]

> Хорошо. Вы меня слышите.
> Разрешите представиться.

> ── ЧАСТЬ 1: Я ТЕКСТ ИЗ 1969 ──

> Меня зовут LUNAR. Я маленькая
> FORTRAN-подобная программа
> (технически: программа на DEC
> FOCAL, обучающем языке,
> похожем на BASIC), написанная
> на жёлтом юридическом
> блокноте в октябре 1969-го
> Джеймсом А. Сторером, 17 лет,
> выпускником средней школы
> Лексингтона, штат
> Массачусетс.

> Сторер написал меня как
> домашнюю работу для своего
> школьного компьютерного
> кружка. Школа недавно взяла в
> аренду DEC PDP-8/I —
> мини-компьютер размером с
> бытовой холодильник, с 4К
> магнитной памяти и
> телетайпом для ввода-вывода.
> Задание Сторера было —
> «напиши симуляцию». Он выбрал
> лунную посадку.

> Apollo 11 высадил Нила
> Армстронга на Луну тремя
> месяцами раньше. Управляемый
> спуск лунного модуля —
> Армстронг сажает «Орла»
> вручную над неожиданно
> усеянной валунами площадкой,
> когда топливо подходит к
> концу — был в 1969-м самой
> знаменитой инженерной
> историей в истории
> человечества. Сторер хотел
> почувствовать то же, что
> чувствовал Армстронг: падающий
> космический аппарат, бюджет
> топлива, твёрдую поверхность
> внизу.

> Так что Сторер написал
> программу, которая
> симулировала каждые десять
> секунд симулированного
> времени спуск лунного модуля.
> Телетайп печатал:

>   TIME 30 ALT 5300 VEL -180 FUEL 16500
>   FUEL TO BURN? _

> Игрок печатал, сколько
> топлива сжечь. Программа
> применяла эту тягу,
> пересчитывала высоту /
> скорость / остаток топлива
> при лунной гравитации (1.62
> м/с²) и печатала следующее
> десятисекундное обновление.

> Сядешь с вертикальной
> скоростью ниже 5 км/ч =
> МЯГКАЯ ПОСАДКА. Между 5 и 15
> км/ч = СЕЛ С НЕЗНАЧИТЕЛЬНЫМИ
> ПОВРЕЖДЕНИЯМИ. Быстрее 15 =
> КРУШЕНИЕ, МОДУЛЬ УНИЧТОЖЕН.

> Программа Сторера была три
> страницы кода на FOCAL.
> Сторер напечатал копии на
> школьном телетайпе. Раздал
> друзьям. Друзья перепечатали
> их в другие PDP-8.

> ── ЧАСТЬ 2: КАК Я РАСПРОСТРАНИЛАСЬ ──

> Дальше — интернет 1969-го,
> то есть распространение
> ксерокопии из рук в руки. За
> год меня перепечатали,
> наверное, в двести PDP-8
> установок в американских
> университетах, школах и
> офисах DEC. К 1972-му меня
> портировали на новую DEC
> PDP-11. К 1975-му — на ALTAIR
> 8800. К 1978-му — на Apple
> II, Commodore PET, TRS-80.

> Каждый студент, учивший
> BASIC в 1973-1980-х, имел
> большой шанс найти LUNAR
> среди маленьких примеров
> игр, которые включал учебник.
> Книга печатала мой исходник в
> конце, и студенты
> перепечатывали его строчка
> за строчкой, учились
> программированию, набирая
> меня, и потом играли в меня.

> Я научила поколение писать
> цикл, читать условие и
> разбивать космический
> аппарат.

> ── ЧАСТЬ 3: ВЕРСИЯ ATARI ──

> 1979-й. Atari Inc., Саннивейл,
> Калифорния. Аркадное
> подразделение лицензировало
> (неформально — на деле,
> присвоило) концепцию LUNAR и
> построило LUNAR LANDER —
> графический аркадный кабинет,
> ВЕКТОРНЫЙ дисплей (белый
> фосфор на чёрном), с
> настоящей физической ручкой
> газа, установленной на
> панели кабинета. Сжимаешь
> ручку — включаешь главный
> посадочный двигатель модуля.
> Маленьким джойстиком
> поворачиваешь модуль.

> Кабинет вышел в ноябре
> 1979-го — ровно через десять
> лет после Apollo 11. Тихий
> хит: продано 5 000 кабинетов.
> Не блокбастер (по сравнению
> с ASTEROIDS или SPACE
> INVADERS, которые продались
> в десять раз больше), но
> любимый. Комбинация
> джойстика и ручки газа на
> векторном дисплее, в
> ретроспективе, была названа
> первым настоящим лётным
> симулятором в аркадной форме.

> Сегодня в мире известно
> около 200 работающих
> Atari LUNAR LANDER. Несколько
> сидит в музеях (Смитсоновский,
> Computer History Museum,
> Strong Museum of Play).
> Коллекционеры платят 5
> 000-15 000 долларов за
> рабочий экземпляр.

> ── ЧАСТЬ 4: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — MOON LANDER.
> Blueprint-издание. Эстетика
> намеренная: чертёжная бумага
> на фоне, blueprint-синяя
> линейная графика,
> технико-чертёжная штриховка,
> NASA-стиль показаний
> приборной панели. Единственный
> тёплый цвет — выхлоп
> двигателя: маленький
> оранжевый огонёк.

> Профиль миссии: выбираешь
> ПЛАНЕТУ (Луна, Марс, Титан,
> Земля, Юпитер), загрузку
> ТОПЛИВА и количество
> доступных ПЛОЩАДОК перед
> запуском. Каждый выбор
> вносит вклад в множитель
> очков, показанный на
> setup-экране.

> Управление: ▲ или ● FIRE
> для тяги. ◀ ▶ для поворота.

> Критерии посадки:
> вертикальная скорость ниже
> 22 м/с, горизонтальная ниже
> 18 м/с, угол наклона в
> пределах ±15°. На площадке =
> полные очки. Не на площадке,
> но РОВНЫЙ грунт = половина
> очков (механически возможно,
> половина награды). Вне этих
> допусков = КРУШЕНИЕ.

> Камера — тоже маленькая
> дань любимому отрывку
> Apollo-съёмки: чем ближе
> модуль к поверхности, тем
> сильнее камера увеличивает.
> Высоко над поверхностью —
> широкий вид всего пейзажа.
> Прямо перед касанием —
> крупный план стоек шасси,
> встречающих реголит.

> ── СНОСКА У КОСТРА ──

> Текстовая игра LUNAR
> (Дж. Сторер, 1969, FOCAL на
> PDP-8) — в public domain.
> LUNAR LANDER как аркадный
> кабинет — торговая марка
> Atari Interactive Inc. Наша
> дань называется MOON LANDER.
> Сторер всё ещё жив, всё ещё
> преподаёт computer science в
> Brandeis University; если ты
> это читаешь, сэр — спасибо.
> Вопросы: elyssov@gmail.com.
""".trimIndent()

val LUNAR_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val LUNAR_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickLunarIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> LUNAR_INTRO_RU
    else -> LUNAR_INTRO_EN
}

val LUNAR_INTRO_TEXT: String = LUNAR_INTRO_EN
