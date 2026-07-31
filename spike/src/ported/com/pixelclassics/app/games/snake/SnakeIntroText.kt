package com.pixelclassics.app.games.snake

/**
 * Long-form campfire intro for the Snake game, told in the voice of a
 * very tired Nokia 3310 dictating its own memoir on a 84×48 pixel screen.
 * Style: telegraph-short lines (the monochrome LCD doesn't fit much per
 * line), with the dry self-aware humour of a phone that genuinely thinks
 * it deserves a museum wing.
 *
 * Bilingual: EN + RU bodies, picked by the player's selected language.
 * VI translation pending — for now Vietnamese falls back to EN.
 */

private val HEADER: String = """
┌────────────────────────────────────┐
│  3310.BIOS  v05.10  —  Nokia AS    │
│  Battery: ████████░░  72% (week 4) │
└────────────────────────────────────┘
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> SNAKE.MEM /TAIL

> Hi. I am a Nokia 3310. There are 126
> million of me out there in the world.
> Even now, decades later, some of me
> still work. Charge me once, leave me
> in a drawer, come back in a month — I
> will still find a tower. Try that
> with your smartphone.

> Let me tell you about Snake.

> ── PART 1: BEFORE THE PHONE ──

> Snake is older than mobile phones.
> Older than mobile phones, that is, not
> older than ME — I was born in September
> 2000, which is admittedly recent for
> an electronic patriarch. Snake is from
> 1976. An arcade cabinet called BLOCKADE,
> from Gremlin Industries in San Diego.
> Two players, two control sticks, two
> growing tails on a black screen.
> Crash into the wall, into the other
> tail, into your own tail — you lose.

> Nobody played BLOCKADE in 1976 because
> nobody knew what a video game was yet.
> Pong had only been out for four years.
> The world was still figuring out the
> idea of "a game on a screen".

> The arcade Snake died quietly.

> ── PART 2: ENTER NOKIA ──

> Twenty-one years later, in 1997, an
> engineer at Nokia named Taneli Armanto
> needs a small game to test the screen
> drivers of the new 6110 handset. He
> remembers BLOCKADE. He simplifies it
> for one player, gives it a square apple
> to eat, makes the snake grow longer
> with each apple, and writes it in
> 6 kilobytes of assembly.

> He calls it Snake. He puts it on the
> menu. The phone ships in March 1998.

> ── PART 3: SNAKE ON A PHONE ──

> Then the strangest thing in the history
> of casual gaming happens.

> Nokia's marketing department had
> assumed customers would buy the 6110
> for, you know, MAKING CALLS. Calls were
> the headline feature. Calls were what
> you bought a mobile phone for.

> The thing customers actually started
> doing with the 6110 was: hiding it
> under their desk and playing Snake all
> day. Office workers. Students. Bus
> drivers between stops. Truck drivers
> at red lights. Bored doctors. Bored
> politicians. Bored teenagers. Bored
> everyone.

> One game. 84 by 48 pixels. Black and
> green. No save. No high-score upload.
> No multiplayer (well, there was a
> Bluetooth two-player added later,
> but most of the world never tried it).
> Just: dot, dot, dot, dot, oh god the
> dot is gone, restart.

> By 2003 it was estimated that Snake
> was the most-played video game in the
> world, by player count. Not the most
> SOLD, not the most REVENUE — but the
> most actively played. Because every
> Nokia phone shipped with it. And
> everyone had a Nokia phone.

> ── PART 4: ENTER ME, THE 3310 ──

> Nokia 3310. April 2000.

> I was built like a brick. Cast plastic
> shell, polycarbonate. Drop me onto
> concrete — concrete chips. Drop me into
> a washing machine — washing machine
> stops, I'm fine. Freeze me in a block
> of ice and then thaw me out — I make
> the phone call. People will, two
> decades later, post videos of dropping
> me from helicopters, running me over
> with tanks, throwing me into volcanoes.
> Spoiler: I work afterwards.

> But the magic, the REAL magic, was
> the battery. Standby: up to one month.
> Talk time: 4.5 hours. You charged me
> on Saturday. By Tuesday next week the
> indicator still had three bars. By
> Friday it might dip to two. By the
> following Wednesday, sure, OK, time
> to plug me in.

> The reason was simple: monochrome
> screen, no apps, no internet, no GPS,
> no Bluetooth (yet), no camera, no
> background processes. A radio, a
> keypad, a tiny screen, and Snake.
> What did I need power for?

> ── PART 5: SNAKE II ON THE 3310 ──

> The 3310 shipped with SNAKE II. Same
> game, slightly nicer. The snake could
> wrap around the edges instead of
> crashing. There were obstacles. There
> were "mazes" (4 of them). The apple
> was now an actual little symbol, not
> just a square.

> 126 million of us shipped. Conservative
> estimate of total hours of Snake II
> played on 3310 hardware: a billion.
> One billion hours. Of one game. On one
> phone model.

> ── PART 6: MEMES FROM THE FUTURE ──

> Years later, when smartphones started
> their long slow march to dominance,
> the world began to remember the
> indestructible little brick that had
> taught it how to procrastinate. The
> jokes wrote themselves.

> "Why is the Earth tilted at 23 degrees?
> Because someone dropped a Nokia 3310."

> "On the floor of the Marianas Trench,
> there are three whales and one 3310."

> "Thor used a Nokia 3310 as his hammer.
> Then he lost the hammer. The Nokia is
> still down there, working."

> "Archaeologists in 50,000 years will
> uncover a Nokia 3310, charge it on
> any handy lightning strike, and play
> two hours of Snake II before deciding
> they understand 21st century humans."

> "Quantum entanglement is just two
> Nokias agreeing to share a Snake save
> file."

> All slightly absurd, all slightly true.
> I am still in your grandmother's
> kitchen drawer. I will outlive most
> of you.

> ── PART 7: WHAT WE'RE GIVING YOU ──

> Below is Snake. Our Snake. The Nokia
> Snake II in spirit, in pixel-count, in
> the slightly-too-fast escalation as
> you eat your fifth apple, in the
> CLACK-CLACK-CLACK of the snake's body
> turning corners on the LCD, in the
> tiny gasping FUUUU sound a 24-year-old
> Soviet kid in 2001 made when his tail
> bit itself at score 247.

> Steer with the D-pad. Don't bite
> yourself. Don't hit the wall. Eat
> every apple you see. The game ends
> when you make one mistake. You will
> make many. The game will reset
> instantly because resetting takes
> zero milliseconds. Try again.

> Eventually, like a billion humans
> before you, you will get fluent. You
> will look up from your phone in a
> cafeteria queue and discover you have
> scored 312 without remembering doing
> any of it. Your snake is now spanning
> the entire screen. The apple is in
> the only cell your body doesn't fill.
> You reach for it. You bite yourself.
> You laugh, because there is nothing
> else to do.

> Welcome to history.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> Snake the gameplay mechanic — eat a
> dot, get longer, don't crash — is
> public domain. Nobody owns "a snake
> on a grid".

> Nokia is a Finnish company, a real
> registered trademark, and we mention
> them here only with love and reverence.
> If somebody at Nokia ever sees this
> game and feels we're stepping on
> something — write to elyssov@gmail.com
> and we'll fix it kindly.
""".trimIndent()

private val BODY_RU: String = """
> SNAKE.MEM /TAIL

> Привет. Я — Nokia 3310. Нас
> сто двадцать шесть миллионов по
> всему миру. И сейчас, спустя
> десятилетия, часть из меня всё
> ещё работает. Заряди один раз,
> положи в ящик стола, вернись
> через месяц — я найду вышку. С
> вашим смартфоном такое не
> прокатит.

> Я расскажу тебе про Змейку.

> ── ЧАСТЬ 1: ДО ТЕЛЕФОНА ──

> Змейка старше мобильных
> телефонов. Старше телефонов —
> не меня — я родилась в сентябре
> 2000-го, что, признаю, недавно
> для электронного патриарха.
> Змейка из 1976-го. Аркадный
> кабинет BLOCKADE, от Gremlin
> Industries в Сан-Диего. Два
> игрока, две палки-джойстика,
> два растущих хвоста на чёрном
> экране. Врежешься в стену, в
> чужой хвост, в свой хвост —
> проиграл.

> В 1976-м в BLOCKADE никто не
> играл, потому что никто ещё не
> знал, что такое видеоигра. Pong
> вышел всего четыре года назад.
> Мир ещё разбирался с идеей
> «игра на экране».

> Аркадная Змейка тихо умерла.

> ── ЧАСТЬ 2: ВХОДИТ NOKIA ──

> Через двадцать один год, в
> 1997-м, инженеру Nokia по имени
> Танели Арманто понадобилась
> маленькая игра — проверять
> экранные драйверы нового
> телефона 6110. Он вспомнил
> BLOCKADE. Упростил до одного
> игрока, дал квадратное яблоко,
> чтобы съесть, сделал змейку
> длиннее с каждым яблоком, и
> написал всё это в шесть
> килобайтах ассемблера.

> Назвал — Snake. Положил в меню.
> Телефон вышел в марте 1998-го.

> ── ЧАСТЬ 3: ЗМЕЙКА В ТЕЛЕФОНЕ ──

> И тут случается самая странная
> вещь в истории casual-гейминга.

> Маркетологи Nokia предполагали,
> что люди покупают 6110, ну,
> ЧТОБЫ ЗВОНИТЬ. Звонки — это
> заголовочная фича. Звонки —
> это то, ради чего покупают
> мобильник.

> На деле же с 6110 люди стали
> заниматься следующим: прятать
> его под стол и играть в
> Змейку весь день. Офисные
> работники. Студенты. Водители
> автобусов между остановками.
> Дальнобойщики на красном
> светофоре. Скучающие врачи.
> Скучающие политики. Скучающие
> подростки. Скучающие все.

> Одна игра. 84 на 48 пикселей.
> Чёрно-зелёная. Без сейва. Без
> загрузки рекордов. Без
> мультиплеера (ну, потом
> появился двухигрок по Bluetooth,
> но большая часть мира его так
> и не попробовала). Просто:
> точка, точка, точка, точка, о
> боже точка пропала, рестарт.

> К 2003-му году по подсчётам
> Змейка стала самой играемой
> видеоигрой в мире — по
> количеству игроков. Не самой
> ПРОДАВАЕМОЙ, не самой
> ДОХОДНОЙ — а самой активно
> играемой. Потому что в каждом
> телефоне Nokia она была. А
> Nokia была у всех.

> ── ЧАСТЬ 4: ВХОЖУ Я, 3310 ──

> Nokia 3310. Апрель 2000-го.

> Меня построили как кирпич.
> Литой пластиковый корпус,
> поликарбонат. Урони меня на
> бетон — у бетона будет скол.
> Урони меня в стиральную
> машинку — машинка остановится,
> а я в порядке. Заморозь меня в
> кубе льда и потом разморозь —
> я возьму трубку. Спустя пару
> десятилетий люди будут
> выкладывать видео, как меня
> бросают с вертолётов,
> переезжают танками, кидают в
> вулкан. Спойлер: после всего —
> я работаю.

> Но магия, НАСТОЯЩАЯ магия —
> это батарея. В режиме ожидания:
> до месяца. Время разговора:
> 4.5 часа. Заряжали меня в
> субботу. К следующему вторнику
> индикатор показывал ещё три
> деления. К пятнице мог
> опуститься до двух. А в среду
> через неделю — ну ладно, пора
> в розетку.

> Причина была простая:
> монохромный экран, никаких
> приложений, никакого
> интернета, никакого GPS,
> никакого Bluetooth (пока),
> никакой камеры, никаких
> фоновых процессов. Радио,
> клавиатура, маленький экран и
> Змейка. На что мне расходовать
> заряд?

> ── ЧАСТЬ 5: SNAKE II НА 3310 ──

> 3310 поставлялась со SNAKE II.
> Та же игра, чуть приятнее.
> Змея могла проходить через
> край экрана, а не врезаться.
> Появились препятствия.
> Появились «лабиринты» (четыре
> штуки). Яблоко стало
> настоящим маленьким символом,
> а не просто квадратиком.

> Нас выпустили 126 миллионов.
> Консервативная оценка общего
> времени, наигранного в SNAKE
> II на железе 3310: миллиард.
> Один миллиард часов. На одну
> игру. На одну модель телефона.

> ── ЧАСТЬ 6: МЕМЫ ИЗ БУДУЩЕГО ──

> Годы спустя, когда смартфоны
> начали свой долгий медленный
> марш к доминированию, мир
> начал вспоминать
> неубиваемый кирпичик, научивший
> его прокрастинации. Шутки
> писались сами.

> «Почему Земля наклонена на 23
> градуса? Потому что кто-то
> уронил Nokia 3310.»

> «На дне Марианской впадины
> лежат три кита и одна 3310.»

> «Тор использовал Nokia 3310 в
> качестве молота. Потом потерял
> молот. Nokia до сих пор там,
> работает.»

> «Через 50 000 лет археологи
> откопают Nokia 3310, зарядят её
> от случайной молнии и
> поиграют два часа в SNAKE II,
> прежде чем решить, что они
> поняли людей 21-го века.»

> «Квантовая запутанность — это
> просто две Nokia, согласные
> делиться одним сейв-файлом
> Змейки.»

> Всё слегка абсурдно, всё
> слегка правда. Я всё ещё в
> ящике твоей бабушки на кухне.
> Я переживу большинство из
> вас.

> ── ЧАСТЬ 7: ЧТО МЫ ТЕБЕ ДАЁМ ──

> Ниже — Змейка. Наша Змейка.
> По духу — Nokia Snake II, по
> пиксельной плотности, по тому
> чуть-слишком-быстрому
> ускорению, когда ты съел пятое
> яблоко, по KLAK-KLAK-KLAK,
> когда тело змеи поворачивает в
> угол на LCD, по крошечному
> придушенному ФУУУ, которое
> издал 24-летний советский
> парень в 2001-м, когда его
> хвост укусил сам себя на
> счёте 247.

> Руль — D-pad. Не кусай себя.
> Не врежься в стену. Ешь
> каждое яблоко. Игра кончается
> при первой ошибке. Ошибок
> будет много. Игра сбросится
> мгновенно, потому что сброс
> занимает ноль миллисекунд.
> Попробуй ещё раз.

> В конце концов, как миллиард
> людей до тебя, ты освоишься.
> Поднимешь глаза от телефона в
> очереди в столовой и
> обнаружишь, что набрал 312, не
> помня как. Твоя змея теперь
> занимает весь экран. Яблоко в
> единственной клетке, где
> твоего тела нет. Ты тянешься
> за ним. Кусаешь себя. Смеёшься,
> потому что больше ничего не
> остаётся.

> Добро пожаловать в историю.

> ── СНОСКА У КОСТРА ──

> Игровая механика Змейки —
> съешь точку, удлинись, не
> разбейся — public domain.
> Никто не владеет «змейкой на
> сетке».

> Nokia — финская компания,
> настоящий зарегистрированный
> бренд, и мы упоминаем её
> здесь только с любовью и
> почтением. Если кто-то в
> Nokia увидит эту игру и
> почувствует, что мы наступаем
> на что-то — напишите на
> elyssov@gmail.com, и мы
> поправим по-доброму.
""".trimIndent()

val SNAKE_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val SNAKE_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickSnakeIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> SNAKE_INTRO_RU
    else -> SNAKE_INTRO_EN
}

/** Back-compat alias. */
val SNAKE_INTRO_TEXT: String = SNAKE_INTRO_EN
