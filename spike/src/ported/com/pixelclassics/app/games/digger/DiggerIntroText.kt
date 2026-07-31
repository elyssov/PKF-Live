package com.pixelclassics.app.games.digger

/**
 * Long-form campfire intro for Tunneler (Digger). Voice: the PC speaker
 * of an IBM PCjr in 1983, narrating the rise and fall of Windmill
 * Software through its tinny one-bit voice.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
█▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒█
█  IBM PCjr · PC SPEAKER LOG · 1983 ░░░  █
█  CGA · 4 COLOURS · ONE-BIT TONE  ░░░░  █
█▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒█
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> *BEEP*
> Hello. I am a PC speaker. I have one
> voice. I cannot produce chords, I
> cannot produce volume, and I cannot
> produce silence (I am either ON or
> OFF; in between, I click at you).

> Despite all this, in late 1983,
> someone made me sing.

> ── PART 1: WINDMILL SOFTWARE ──

> Ottawa, Canada. 1981. A small
> software company called Windmill
> Software opens shop in a residential
> townhouse. Two founders, one
> programmer, a kitchen table. Their
> first product is a clone of Lunar
> Lander for the IBM PC. They sell
> maybe 200 copies through a Toronto
> hobbyist newsletter.

> In 1983 — IBM has just announced
> the PCjr, the consumer version of
> the PC, intended to break into the
> Apple II's grip on the home market
> — Windmill releases a game called
> DIGGER. CGA graphics: black, cyan,
> magenta, white. Four colours. That's
> the entire palette. PC speaker
> audio. One voice, one note at a
> time, no envelope, no decay.

> Digger is a clone of an arcade
> game called MR. DO! (Universal,
> 1982), which itself riffs on Atari's
> DIG DUG (1982). Underground maze.
> You tunnel through dirt. Push
> boulders to crush monsters. Collect
> emeralds. Avoid being touched by
> red enemies.

> Three studios circling the same
> mechanic in the same year is what
> historians call "the arcade
> convergence". Pac-Man had proved
> that maze-chases sold. Dig-Dug
> proved that destructible terrain
> made them better. Mr. Do! proved
> that boulders made them brilliant.
> Digger added: a PC. A real personal
> computer, not an arcade cabinet,
> running in your living room, in
> CGA, with PC speaker audio. The
> first time the genre crossed over.

> ── PART 2: WHY DIGGER MATTERED ──

> Personal computers in 1983 were
> mostly utility devices. Word
> processing. Spreadsheets. Basic
> programming. The argument from
> parents to children was "the
> computer is for school, not for
> play". Digger was one of a small
> handful of games that quietly
> destroyed that argument.

> A kid with five minutes between
> school and dinner could fire up
> DIGGER from a 5.25" floppy disk,
> dig through three levels, push a
> boulder onto a Nobbin, collect
> sixteen emeralds, get crushed by
> a Hobbin, restart. The whole loop
> in five minutes. Repeat infinitely.

> Windmill sold maybe 100,000 copies
> of Digger across the PCjr's brief
> commercial life (the PCjr was a
> disaster — IBM's chiclet keyboard
> was the wrong call — and the
> machine was withdrawn after 18
> months). They moved on to a few
> more titles — STYX, FLOPPY FRENZY,
> ROLLO AND THE BRUSH BROTHERS — but
> Digger was the hit.

> ── PART 3: 1998, THE GIFT ──

> Windmill Software dissolved by the
> mid-1980s. The founders moved on
> to other projects. The source code
> for Digger sat on a 5.25" floppy
> disk in a box in someone's
> basement.

> In 1998, fifteen years after
> Digger's original release, the
> rights-holder — Andrew Mr. Jaffer,
> one of the original creators —
> formally released the Digger
> source code into the public
> domain. No restrictions. Freely
> distributable. Modifiable. Use it
> for anything.

> This was, in 1998, an unusually
> generous act. Most companies that
> went out of business in the 80s
> simply abandoned their software
> behind copyright fences nobody
> could climb. Jaffer made an active
> decision: give Digger to the
> world.

> Hobbyists immediately built a
> dozen ports. Digger Remastered.
> Digger on Linux. Digger on Macintosh.
> Digger on Windows. Digger in a
> browser. The game is, in 2026,
> probably more widely available
> than it ever was at its
> commercial peak.

> ── PART 4: WHAT WE BUILT ──

> Our TUNNELER is a chunky pixel
> tribute to Digger. The grid is
> larger (20 columns by 14 rows
> instead of the original 15x10).
> The dirt is brown. Boulders are
> grey. Emeralds are green. Red
> monsters chase you through your
> own tunnels.

> Direction pad to tunnel and
> dodge. Fire to spit a fireball at
> a monster (limited ammo).

> Push a boulder onto an enemy to
> crush them. The boulder falls
> when its supporting dirt is
> dug out from underneath. The
> physics is two-cell-deep — the
> moment the boulder has empty
> space below it, gravity wins.
> Be careful: gravity does not care
> whether YOU are below it.

> ── PART 5: WHY THE TIN VOICE ──

> Our audio is intentionally
> retained as a square-wave-ish
> chiptune approximation. The
> original DIGGER could only play
> one note at a time, on a speaker
> the size of a coin, in a PCjr
> that cost ${'$'}1300 in 1983 dollars.
> Modern phones can synthesise
> entire orchestras. We could have
> given Tunneler a full orchestral
> score.

> We didn't. The point of the
> tribute is to remember what it
> felt like. What it felt like
> was: BEEP-beep-BEEP-beep-CRASH-
> beep, while the disk drive
> clattered, in a dim corner of
> the living room, while you were
> supposed to be doing homework.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> DIGGER by Windmill Software (1983)
> is in the public domain as of
> 1998, by formal release. The
> direct gameplay ancestors — MR.
> DO! (Universal Games) and DIG-DUG
> (Atari/Namco) — remain under
> their respective trademarks. We
> call our tribute TUNNELER to keep
> the lineage clear. Hat off to
> Andrew Jaffer for the public-
> domain release in 1998. Concerns:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> *БИП*
> Привет. Я — PC-спикер. У
> меня один голос. Я не умею
> аккорды, не умею громкость,
> не умею тишину (я либо
> ВКЛ, либо ВЫКЛ; между ними
> — щёлкаю).

> Несмотря на всё это, в
> конце 1983-го кто-то
> заставил меня петь.

> ── ЧАСТЬ 1: WINDMILL SOFTWARE ──

> Оттава, Канада. 1981-й.
> Маленькая софтверная
> компания Windmill Software
> открывает лавку в жилом
> таунхаусе. Два основателя,
> один программист, кухонный
> стол. Их первый продукт —
> клон Lunar Lander для IBM
> PC. Продают штук 200 через
> торонтский хоббистский
> newsletter.

> В 1983-м — IBM только что
> объявила PCjr,
> потребительскую версию PC,
> рассчитанную пробить хват
> Apple II на домашнем рынке
> — Windmill выпускает игру
> DIGGER. CGA-графика:
> чёрный, циан, маджента,
> белый. Четыре цвета. Это
> вся палитра. PC-спикер.
> Один голос, одна нота за
> раз, без огибающей, без
> затухания.

> Digger — клон аркадной
> игры MR. DO! (Universal,
> 1982), которая сама рифмует
> на DIG DUG от Atari
> (1982). Подземный лабиринт.
> Туннелишь через грунт.
> Толкаешь валуны, чтобы
> раздавить монстров.
> Собираешь изумруды.
> Избегаешь касания красных
> врагов.

> Три студии, нарезающие
> одну механику в один год
> — это то, что историки
> зовут «аркадной
> конвергенцией». Pac-Man
> доказал, что лабиринт-
> погони продаются. Dig-Dug
> доказал, что разрушаемая
> местность делает их лучше.
> Mr. Do! доказал, что
> валуны делают их
> гениальными. Digger
> добавил: PC. Реальный
> персональный компьютер, а
> не аркадный кабинет,
> работающий у тебя в
> гостиной, в CGA, с
> PC-спикерным звуком.
> Первый раз жанр перешёл
> границу.

> ── ЧАСТЬ 2: ПОЧЕМУ DIGGER ВАЖЕН ──

> Персональные компьютеры в
> 1983-м были в основном
> утилитарными устройствами.
> Текстовый процессор.
> Таблицы. Программирование
> на BASIC. Аргумент
> родителей детям — «компьютер
> для школы, не для игр».
> Digger был одной из
> небольшой горстки игр,
> тихо разрушивших этот
> аргумент.

> Ребёнок с пятью минутами
> между школой и ужином мог
> запустить DIGGER с
> 5.25" дискеты, прокопать
> три уровня, толкнуть
> валун на Nobbin'а, собрать
> шестнадцать изумрудов,
> быть раздавленным Hobbin'ом,
> перезапустить. Весь цикл
> за пять минут. Повторять
> бесконечно.

> Windmill продали штук
> 100 000 копий Digger за
> краткую коммерческую жизнь
> PCjr (PCjr был катастрофой
> — клавиатура-«чиклет» от
> IBM была неправильным
> решением — машина была
> снята через 18 месяцев).
> Они выпустили ещё несколько
> тайтлов — STYX, FLOPPY
> FRENZY, ROLLO AND THE
> BRUSH BROTHERS — но
> Digger был хитом.

> ── ЧАСТЬ 3: 1998, ПОДАРОК ──

> Windmill Software
> распалась к середине
> 1980-х. Основатели ушли в
> другие проекты. Исходник
> Digger лежал на 5.25"
> дискете в коробке в
> чьём-то подвале.

> В 1998-м, через 15 лет
> после оригинального
> выпуска Digger,
> правообладатель — Эндрю
> Джаффер, один из
> создателей — формально
> выпустил исходный код
> Digger в public domain.
> Без ограничений.
> Свободно
> распространяемый.
> Модифицируемый. Используй
> для чего угодно.

> Это был, в 1998-м,
> необычно щедрый акт.
> Большинство компаний,
> закрывшихся в 80-х,
> просто бросали свой софт
> за копирайтным забором,
> через который никто не
> мог перелезть. Джаффер
> принял активное решение:
> отдать Digger миру.

> Хоббисты тут же
> построили десяток портов.
> Digger Remastered.
> Digger на Linux. Digger
> на Macintosh. Digger на
> Windows. Digger в
> браузере. Игра в 2026-м,
> вероятно, доступнее, чем
> когда-либо была в
> коммерческий пик.

> ── ЧАСТЬ 4: ЧТО МЫ ПОСТРОИЛИ ──

> Наш TUNNELER — плотная
> пиксельная дань Digger'у.
> Сетка больше (20 столбцов
> на 14 строк вместо
> оригинальной 15×10).
> Грунт коричневый. Валуны
> серые. Изумруды зелёные.
> Красные монстры гонятся
> за тобой по твоим же
> туннелям.

> D-pad — туннелить и
> уворачиваться. Fire —
> плюнуть огненным шаром в
> монстра (ограниченный
> боезапас).

> Толкни валун на врага,
> чтобы раздавить. Валун
> падает, когда
> поддерживающий его грунт
> выкопан снизу. Физика
> двухклеточно-глубокая —
> в момент, когда под
> валуном пустое
> пространство, гравитация
> побеждает. Осторожно:
> гравитации всё равно,
> ТЫ ли под ним.

> ── ЧАСТЬ 5: ПОЧЕМУ ЖЕСТЯНОЙ ГОЛОС ──

> Наш звук намеренно оставлен
> в виде square-wave-ish
> chiptune-приближения.
> Оригинальный DIGGER мог
> играть только одну ноту за
> раз, на спикере размером с
> монету, в PCjr, который
> стоил 1300 долларов в
> ценах 1983-го. Современные
> телефоны могут
> синтезировать целые
> оркестры. Мы могли дать
> Tunneler полный оркестровый
> саундтрек.

> Не дали. Смысл дани —
> вспомнить, как это
> ощущалось. Ощущалось это
> так: БИП-бип-БИП-бип-БУМ-
> бип, пока дисковод стучал,
> в тусклом углу гостиной,
> пока ты должен был делать
> уроки.

> ── СНОСКА У КОСТРА ──

> DIGGER от Windmill
> Software (1983) — в
> public domain с 1998-го,
> по формальному выпуску.
> Прямые геймплейные
> предки — MR. DO!
> (Universal Games) и
> DIG-DUG (Atari/Namco) —
> остаются под своими
> торговыми марками. Мы
> зовём нашу дань TUNNELER,
> чтобы линия была ясна.
> Шляпа долой Эндрю
> Джафферу за public-
> domain выпуск в 1998-м.
> Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val DIGGER_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val DIGGER_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickDiggerIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> DIGGER_INTRO_RU
    else -> DIGGER_INTRO_EN
}

val DIGGER_INTRO_TEXT: String = DIGGER_INTRO_EN
