package com.pixelclassics.app.games.cave

/**
 * Long-form campfire intro for CAVE QUEST (Colossal Cave Adventure).
 * Narrated by Will Crowther himself, who wrote the original in 1976 on
 * a PDP-10 between caving expeditions in Mammoth Cave, Kentucky.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
██████████████████████████████████████████
█  COLOSSAL CAVE ADVENTURE · 1976       █
█  WILL CROWTHER · BOLT BERANEK NEWMAN  █
█  PDP-10 · FORTRAN IV · BBN INTERNAL   █
██████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> You are standing at the end of a
> road before a small brick
> building. Around you is a forest.
> A small stream flows out of the
> building and down a gully.

> ─────────────────────────────────

> Hi. I am Will Crowther. I wrote
> that line in the spring of 1976.

> The line is, technically, the
> opening of the first text
> adventure ever written. Every
> piece of interactive fiction
> since — Zork, Hitchhiker's
> Guide to the Galaxy, every
> Infocom game, the entire
> Choose-Your-Own-Adventure
> tradition, the entire 80s text
> RPG industry, modern narrative
> games like Disco Elysium and
> 80 Days — all of them are, in
> a real sense, children of that
> opening line.

> But the line was not written
> for an audience. The line was
> written for my daughters.

> ── PART 1: WHO I WAS ──

> 1976. I was a 39-year-old
> programmer at Bolt Beranek and
> Newman in Cambridge,
> Massachusetts — the company
> that had built ARPANET, the
> ancestor of the Internet, for
> the US Department of Defence.
> I had been at BBN for ten
> years. Solid job. Good
> colleagues. My wife Pat and
> I had two daughters, Laura
> and Sandra, ages five and
> seven.

> I was also a caver. Caving
> was my obsession. Every
> weekend I disappeared into
> the limestone caves of West
> Virginia, Kentucky, and
> Tennessee with the Cave
> Research Foundation, mapping
> tunnels by hand on graph
> paper, by carbide lamp, with
> a clinometer and a length of
> string.

> Mammoth Cave in Kentucky is
> the longest cave system on
> Earth — over 400 miles of
> mapped passage. I had been
> mapping Mammoth Cave's
> Bedquilt section for two
> years. I knew its rooms,
> chambers, vertical drops,
> water flows, formations.

> In late 1975, my marriage
> ended. Pat and I divorced.
> I had visitation with the
> girls every other weekend.

> The girls wanted to know
> what I did on weekends when
> I went caving. I tried to
> describe it to them. They
> were five and seven. The
> descriptions were not
> landing.

> I sat down at the BBN PDP-10
> over the Christmas holiday
> of 1975 and started writing
> them a program that would
> let them EXPLORE the cave
> themselves, virtually.

> ── PART 2: WHAT I WROTE ──

> I wrote it in FORTRAN IV. The
> program was about 700 lines
> of code. It loaded a database
> of cave locations,
> descriptions, and connections.
> The player typed simple
> commands ("GO NORTH", "TAKE
> LAMP", "LIGHT LAMP", "READ
> SIGN") and the program
> responded with new
> descriptions and updated game
> state.

> I based the geometry on
> Mammoth Cave's actual
> Bedquilt section. The room
> names I used in the program
> — "Hall of the Mountain
> King", "Twopit Room", "East
> Pit", "Bedquilt", "Cave with
> Plant" — came directly from
> my real cave survey notes.

> I added some fantasy elements
> that did NOT exist in
> Bedquilt: dwarves who threw
> axes at the player, a giant
> snake blocking a key
> passage, magic words, hidden
> rooms revealed only by
> incantations, a treasure
> hunt across the cave system.
> The dwarves were partly a
> joke for my daughters
> (they were very into "Lord
> of the Rings" at school).
> The fantasy elements made
> the game more compelling
> than a pure mapping
> exercise.

> The girls played it through
> the dial-up terminal we
> had at home. They mapped
> the cave on graph paper as
> they explored, same as I
> did at work. They solved
> the dwarves problem within
> two weekends.

> ── PART 3: HOW IT ESCAPED ──

> The original program was on
> a BBN PDP-10 with internal
> ARPANET access. BBN's PDP-10
> was on ARPANET. Anyone with
> ARPANET access could
> theoretically log in and
> run my program — and many
> did. Word spread through
> the BBN community first,
> then to other ARPANET
> sites, then to MIT, Stanford,
> CMU.

> By 1977 the program had
> escaped BBN's network and
> was running on twenty
> mainframes across the
> United States.

> ── PART 4: THE WOODS EXPANSION ──

> Spring 1977. Don Woods, a
> graduate student at the
> Stanford Artificial
> Intelligence Lab, found my
> program on Stanford's PDP-10.
> Woods loved it. But he felt
> the fantasy elements were
> too sparse — the dwarves
> were good, but the cave
> needed more puzzles, more
> magic, more dragons, more
> treasures.

> Woods emailed me (the term
> "email" had been coined only
> the previous year) and asked
> for the source code. I sent
> it to him. He expanded the
> game enormously — added
> over a dozen new locations,
> dozens of new objects, a
> dragon (which can only be
> killed by punching it with
> bare hands, an inside joke
> from a Stanford AI Lab
> discussion of bizarre
> command parsers), a
> volcano, treasure-vault
> mechanics, scoring rules.

> The expanded version,
> ADVENTURE (or sometimes
> COLOSSAL CAVE), was the
> version that exploded across
> the academic computing
> world. By 1978 it had been
> ported to dozens of systems.
> By 1980 it was part of the
> standard UNIX games
> distribution. The first
> commercial text adventures
> from Infocom (ZORK,
> released 1980) were
> deliberate evolutions of
> ADVENTURE — Marc Blank and
> Dave Lebling, Infocom's
> founders, had played
> ADVENTURE obsessively at
> MIT in 1977-78.

> ── PART 5: WHAT WE BUILT ──

> Below is CAVE QUEST. A
> chunky, compact homage to
> the 1976-77 original.
> Twenty locations
> (representing roughly the
> first quarter of the
> Crowther-Woods cave). A
> command parser that
> understands a small
> vocabulary of VERBS and
> NOUNS.

> Mobile-friendly: instead of
> typing on a keyboard,
> assemble commands by
> tapping VERB chips at the
> bottom of the screen plus
> NOUN chips. "GO" + "NORTH",
> "TAKE" + "LAMP", "LIGHT" +
> "LAMP", "READ" + "SIGN".
> Press RUN to execute.

> Find four treasures (a
> Persian rug, a brass lantern,
> a gold nugget, a diamond)
> and return them to the
> brick building. Then you
> win.

> A small puzzle: the cave is
> DARK. You need the LAMP. The
> LAMP needs BATTERIES. The
> LAMP runs out of batteries
> if used too long. There are
> spare batteries somewhere.
> Find them. Time your lamp
> use.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> COLOSSAL CAVE ADVENTURE is in
> the public domain. Crowther
> released it for free in 1976.
> Woods's 1977 expansion has
> always been freely
> redistributable. The genre
> they invented — interactive
> fiction, text adventure,
> early-RPG narrative — is one
> of the most generously open
> traditions in software.

> Crowther retired from BBN in
> the 1980s, spent his later
> years still caving in the
> Catskill Mountains, and
> passed away in 2025 at age
> 88. Woods is still alive,
> recently retired from a long
> career at Sun Microsystems
> and later Google. Our
> tribute is called CAVE QUEST.
> Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Ты стоишь у конца дороги
> перед маленьким кирпичным
> зданием. Вокруг тебя — лес.
> Маленький ручей вытекает из
> здания и спускается по
> оврагу.

> ─────────────────────────────────

> Привет. Я — Уилл Кроутер.
> Я написал эту строку весной
> 1976-го.

> Эта строка — технически —
> начало первого
> текстового приключения,
> когда-либо написанного.
> Каждое interactive fiction
> с тех пор — Zork,
> «Автостопом по Галактике»,
> каждая игра Infocom, вся
> традиция «Выбери своё
> приключение», вся
> индустрия текстовых RPG
> 80-х, современные
> нарративные игры вроде
> Disco Elysium и 80 Days —
> все они, в реальном
> смысле, дети той строки.

> Но строка написана не для
> аудитории. Строка написана
> для моих дочерей.

> ── ЧАСТЬ 1: КЕМ Я БЫЛ ──

> 1976-й. Мне 39, программист
> в Bolt Beranek and Newman в
> Кембридже, штат Массачусетс
> — компания, построившая
> ARPANET, предка Интернета,
> для Минобороны США. Я был
> в BBN десять лет. Надёжная
> работа. Хорошие коллеги.
> Жена Пэт и я — две дочери,
> Лаура и Сандра, пять и
> семь лет.

> Я был ещё и спелеологом.
> Спелеология была моим
> наваждением. Каждые выходные
> я исчезал в известняковых
> пещерах Западной Вирджинии,
> Кентукки и Теннесси с Cave
> Research Foundation,
> картируя тоннели от руки на
> клетчатой бумаге, при
> карбидовой лампе, с
> уклономером и обрывком
> верёвки.

> Мамонтова пещера в Кентукки
> — самая длинная пещерная
> система на Земле — больше
> 400 миль картированных
> ходов. Я картировал секцию
> Bedquilt Мамонтовой пещеры
> два года. Знал её комнаты,
> залы, вертикальные сбросы,
> водотоки, формации.

> В конце 1975-го мой брак
> кончился. Мы с Пэт
> развелись. Я виделся с
> девочками через выходные.

> Девочки хотели знать, что я
> делаю на выходных, когда
> ухожу в пещеры. Я пробовал
> им описать. Им было пять и
> семь. Описания не
> приземлялись.

> Я сел за BBN PDP-10 на
> рождественских каникулах
> 1975-го и начал писать им
> программу, которая позволила
> бы ИССЛЕДОВАТЬ пещеру
> самим, виртуально.

> ── ЧАСТЬ 2: ЧТО Я НАПИСАЛ ──

> Я писал на FORTRAN IV.
> Программа была около 700
> строк кода. Загружала базу
> локаций, описаний и
> соединений. Игрок печатал
> простые команды («GO
> NORTH», «TAKE LAMP», «LIGHT
> LAMP», «READ SIGN»), и
> программа отвечала новыми
> описаниями и обновлённым
> состоянием.

> Геометрию я основал на
> реальной секции Bedquilt
> Мамонтовой пещеры. Имена
> комнат в программе — «Hall
> of the Mountain King»,
> «Twopit Room», «East Pit»,
> «Bedquilt», «Cave with
> Plant» — пришли напрямую
> из моих реальных полевых
> заметок.

> Я добавил фэнтези-элементы,
> которых в Bedquilt НЕ было:
> дварфов, кидающих топоры в
> игрока, гигантскую змею,
> блокирующую ключевой
> проход, магические слова,
> скрытые комнаты,
> открывающиеся только
> заклинаниями, охоту за
> сокровищами по системе
> пещер. Дварфы — отчасти
> шутка для моих дочерей
> (они увлекались «Властелином
> колец» в школе).
> Фэнтези-элементы делали
> игру увлекательнее, чем
> чистое картирование.

> Девочки играли через
> модемный терминал, что был
> у нас дома. Они
> картировали пещеру на
> клетчатой бумаге, пока
> исследовали — так же, как
> я на работе. Они решили
> задачу с дварфами за
> два выходных.

> ── ЧАСТЬ 3: КАК ОНА УБЕЖАЛА ──

> Оригинал был на BBN PDP-10
> с внутренним доступом к
> ARPANET. BBN'овский PDP-10
> был в ARPANET. Любой с
> доступом к ARPANET
> теоретически мог
> залогиниться и запустить
> мою программу — многие так
> и сделали. Молва прошла
> сначала по BBN, потом по
> другим сайтам ARPANET,
> потом в MIT, Стэнфорд, CMU.

> К 1977-му программа сбежала
> из сети BBN и работала на
> двадцати мейнфреймах по
> США.

> ── ЧАСТЬ 4: РАСШИРЕНИЕ ВУДСА ──

> Весна 1977-го. Дон Вудс,
> аспирант Stanford AI Lab,
> нашёл мою программу на
> стэнфордском PDP-10. Вудсу
> она понравилась. Но он
> чувствовал, что
> фэнтези-элементов мало —
> дварфы хороши, но пещере
> нужно больше загадок,
> больше магии, больше
> драконов, больше сокровищ.

> Вудс послал мне email
> (термин «email» появился
> только годом раньше) и
> попросил исходник. Я
> отправил. Он расширил игру
> огромно — добавил больше
> десятка новых локаций,
> десятки новых объектов,
> дракона (которого можно
> убить только ударом
> голыми руками — внутренняя
> шутка из обсуждения
> странных command parser'ов
> в Stanford AI Lab), вулкан,
> механику сокровищницы,
> правила счёта.

> Расширенная версия,
> ADVENTURE (или иногда
> COLOSSAL CAVE), стала
> версией, взорвавшейся по
> академическому
> компьютерному миру. К
> 1978-му её портировали на
> десятки систем. К 1980-му
> она была частью стандартной
> UNIX games-поставки. Первые
> коммерческие текстовые
> приключения от Infocom
> (ZORK, выпущен в 1980-м)
> были осознанной эволюцией
> ADVENTURE — Марк Бланк и
> Дэйв Леблинг, основатели
> Infocom, играли в
> ADVENTURE одержимо в MIT
> в 1977-78.

> ── ЧАСТЬ 5: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — CAVE QUEST. Плотная,
> компактная дань оригиналу
> 1976-77. Двадцать локаций
> (представляющих примерно
> первую четверть пещеры
> Кроутер-Вудс).
> Command parser, понимающий
> небольшой словарь ГЛАГОЛОВ
> и СУЩЕСТВИТЕЛЬНЫХ.

> Mobile-friendly: вместо
> печати на клавиатуре
> собирай команды, тапая по
> чипам-ГЛАГОЛАМ внизу
> экрана + чипам-
> СУЩЕСТВИТЕЛЬНЫМ. «GO» +
> «NORTH», «TAKE» + «LAMP»,
> «LIGHT» + «LAMP», «READ»
> + «SIGN». Жми RUN, чтобы
> выполнить.

> Найди четыре сокровища
> (персидский ковёр, медную
> лампу, золотой самородок,
> алмаз) и верни в кирпичное
> здание. Тогда ты
> выиграешь.

> Маленькая загадка: в
> пещере ТЕМНО. Нужна ЛАМПА.
> ЛАМПЕ нужны БАТАРЕЙКИ.
> ЛАМПА разряжается, если её
> использовать слишком
> долго. Запасные батарейки
> где-то есть. Найди их.
> Распределяй использование
> лампы.

> ── СНОСКА У КОСТРА ──

> COLOSSAL CAVE ADVENTURE —
> в public domain. Кроутер
> выпустил её бесплатно в
> 1976-м. Расширение Вудса
> 1977-го всегда было
> свободно распространяемым.
> Жанр, который они изобрели
> — interactive fiction,
> текстовое приключение,
> ранний нарратив RPG —
> одна из самых щедро
> открытых традиций в
> софтверной индустрии.

> Кроутер ушёл на пенсию из
> BBN в 1980-х, провёл
> поздние годы, всё ещё лазая
> по пещерам в Катскильских
> горах, и скончался в
> 2025-м в 88 лет. Вудс
> жив, недавно ушёл на
> пенсию после долгой карьеры
> в Sun Microsystems и потом
> Google. Наша дань
> называется CAVE QUEST.
> Вопросы: elyssov@gmail.com.
""".trimIndent()

val CAVE_QUEST_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val CAVE_QUEST_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickCaveIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> CAVE_QUEST_INTRO_RU
    else -> CAVE_QUEST_INTRO_EN
}

val CAVE_QUEST_INTRO_TEXT: String = CAVE_QUEST_INTRO_EN
