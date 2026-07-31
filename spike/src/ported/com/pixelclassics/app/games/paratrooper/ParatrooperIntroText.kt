package com.pixelclassics.app.games.paratrooper

/**
 * Long-form campfire intro for PARATROOPER. Voice: an anti-aircraft
 * gunner narrating his fifth tour of duty defending a static base on a
 * CGA-blue sky background.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
═══════════════════════════════════════════
   ORION SOFTWARE · PARATROOPER · 1982
   IBM PC · CGA · 320×200 · 4 COLOURS
═══════════════════════════════════════════
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> The sky is cyan. Always cyan. Cyan
> all the way to the horizon, where it
> meets the white sand at the bottom.
> No clouds. Cyan does not weather.
> Just cyan, and me, and the gun.

> ── PART 1: WHO MADE ME ──

> The year is 1982. The IBM PC has
> been on the market for fifteen
> months. It costs ${'$'}1,565 for the
> base configuration — green text on
> black, no graphics, no floppy drive.
> Add the CGA card for graphics:
> +${'$'}300. Add a floppy drive: +${'$'}400.
> Add a parallel printer port: +${'$'}100.
> The total package costs roughly
> ${'$'}2,500 in 1982 dollars, which is
> roughly ${'$'}7,000 in 2026 dollars,
> which means the entire personal-
> computer market is, in 1982,
> firmly upper-middle-class.

> A small two-man studio called
> Orion Software in Maryland decides
> to make a game for this audience.
> Their argument is: the IBM PC has
> a 4-color graphics mode (CGA),
> which is exactly the same number
> of colours as the Atari 2600 had,
> and the Atari 2600's library is
> selling millions. Therefore: bring
> Atari-style arcade games to the
> CGA-equipped IBM PC.

> The studio writes a game called
> PARATROOPER. The premise is: you
> are a stationary anti-aircraft
> gunner. Helicopters fly across the
> sky from both directions. They
> drop paratroopers. If too many
> paratroopers land within striking
> distance of your gun position,
> they swarm you and you lose. So:
> shoot the paratroopers in mid-
> air. Or, better, shoot the
> helicopters before they can drop
> anyone. Or, best, shoot the
> occasional jet fighter that
> screams across the screen too
> fast to react properly. If you
> miss the jet, the jet drops a
> bomb, and the bomb destroys your
> gun, and you lose.

> Ammunition is finite. You start
> with 500 rounds. You can earn
> more rounds by precise shooting
> (each clean kill of a paratrooper
> awards +5 rounds). A sloppy
> player runs out of bullets and
> dies. A precise player can stretch
> infinite.

> ── PART 2: HOW IT FEELS ──

> The screen is split between cyan
> sky (top three-quarters) and white
> sand (bottom quarter). Your gun
> is a small red anti-aircraft
> battery in the centre of the
> sand, with a rotating barrel.

> Use the LEFT and RIGHT arrows
> (or, on our tribute, the d-pad)
> to swing the barrel. Press
> SPACE (or FIRE) to fire one round.
> The round travels upward at the
> angle your barrel was pointing.
> Hits a helicopter? Helicopter
> explodes; +25 points; any
> paratrooper currently dropping
> from that helicopter is now in
> free fall.

> Hits a paratrooper mid-fall?
> Paratrooper dies; +10 points.

> Hits a paratrooper's parachute
> instead of the man? The chute
> rips; the man falls faster; +5
> points (you get scored for the
> chute but the man may still
> survive his now-faster landing).

> Paratrooper lands safely?
> Paratrooper walks toward your
> gun. Four paratroopers reach
> the gun position = game over.

> Jet flies across? You have
> about two seconds to land the
> shot. Miss it? Bomb. Your
> base is gone.

> ── PART 3: THE CGA THING ──

> The CGA palette is famously
> awful. Mode 1 (the one
> Paratrooper uses) offers four
> colours: black, cyan, magenta,
> and white. These four are not
> attractive colours together.
> They are the colours of a
> medical waiting room in 1979.

> But Orion did the only thing
> you could do with the CGA
> palette: lean into it. The
> sky is unapologetically cyan.
> The sand is white. The
> helicopters are magenta. The
> paratroopers are also magenta
> (with white parachutes). The
> gun is also magenta. The
> bullets are white.

> This palette is, paradoxically,
> very legible. You always know
> what is a threat (magenta) and
> what is a bullet (white) and
> what is sand (white) and what
> is sky (cyan). The mind quickly
> stops noticing the ugly colour
> choices and starts seeing the
> game.

> Decades later, when 32-bit
> colour was standard and CGA
> was a punchline, people who had
> grown up on the CGA palette
> would feel a small nostalgia
> for cyan-and-magenta. It is
> not pretty, but it is THE
> WAY THINGS LOOKED when you
> were young.

> ── PART 4: WHAT WE BUILT ──

> Below is PARATROOPER. Pixel-
> faithful to the CGA original.
> Cyan sky, white sand, magenta
> threats.

> Choose your control style on
> the title screen: TAP (point
> and shoot — modern), or
> ARROWS (rotate the barrel
> with the d-pad, fire with the
> red button — classic). The
> tap style is faster on a
> touchscreen. The arrow style
> is harder, more authentic,
> and the only one that
> recreates the precise
> Frustration-Of-1982 we are
> aiming for.

> Defend the base. Don't run
> out of ammo. Watch for the
> jet.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> PARATROOPER is the 1982 game
> by Orion Software (specifically
> Greg Kuperberg, occasionally
> credited as the lead programmer).
> Orion Software dissolved in the
> mid-1980s. The original game
> remains the property of its
> rights-holders. We do not
> reuse any of their code or art;
> our tribute is a complete
> re-implementation. Concerns:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Небо — циан. Всегда циан.
> Циан до самого горизонта,
> где он встречает белый
> песок внизу. Облаков нет.
> Циан не меняется от погоды.
> Только циан, я и пушка.

> ── ЧАСТЬ 1: КТО МЕНЯ СДЕЛАЛ ──

> Год 1982-й. IBM PC на
> рынке пятнадцать месяцев.
> Базовая конфигурация —
> 1565 долларов: зелёный
> текст на чёрном, никакой
> графики, никакого
> дисковода. Добавь CGA-карту
> для графики: +300.
> Добавь дисковод: +400.
> Добавь параллельный порт
> для принтера: +100.
> Итого пакет стоит около
> 2500 долларов 1982-го, что
> примерно 7000 в долларах
> 2026-го, и это значит,
> что весь рынок персональных
> компьютеров в 1982-м —
> крепко выше среднего
> класса.

> Маленькая двухчеловечная
> студия под названием
> Orion Software в Мэриленде
> решает сделать игру для
> этой аудитории. Их
> аргумент: у IBM PC есть
> 4-цветный графический
> режим (CGA), точно столько
> же цветов, сколько было у
> Atari 2600, а библиотека
> Atari 2600 продаётся
> миллионами. Следовательно:
> принести аркадные игры
> в стиле Atari на
> CGA-оснащённый IBM PC.

> Студия пишет игру
> PARATROOPER. Замысел: ты —
> стационарный зенитный
> наводчик. Вертолёты летят
> через небо с двух сторон.
> Они сбрасывают
> парашютистов. Если слишком
> много парашютистов
> приземляется в зоне удара
> вокруг твоей пушки — они
> тебя окружают и ты
> проиграл. Значит: стреляй
> по парашютистам в воздухе.
> Или, ещё лучше, стреляй
> по вертолётам, пока они
> никого не сбросили. Или,
> совсем лучше, стреляй по
> редкому реактивному
> истребителю, что
> проносится по экрану
> слишком быстро, чтобы
> успеть среагировать. Если
> ты не попал по истребителю
> — он сбрасывает бомбу, и
> бомба уничтожает твою
> пушку, и ты проиграл.

> Боезапас конечен.
> Начинаешь с 500
> снарядов. Можешь заработать
> ещё точной стрельбой
> (каждое чистое убийство
> парашютиста даёт +5
> снарядов). Неаккуратный
> игрок остаётся без
> патронов и умирает. Точный
> игрок может тянуть
> бесконечно.

> ── ЧАСТЬ 2: КАК ЭТО ОЩУЩАЕТСЯ ──

> Экран разделён между
> цианным небом (верхние
> три четверти) и белым
> песком (нижняя четверть).
> Твоя пушка — маленькая
> красная зенитная батарея
> в центре песка, с
> поворотным стволом.

> Используй ВЛЕВО и ВПРАВО
> стрелки (или, в нашей
> дани, d-pad), чтобы
> качать ствол. Жми
> ПРОБЕЛ (или FIRE), чтобы
> выпустить один снаряд.
> Снаряд летит вверх под
> углом, на который смотрел
> ствол. Попал по
> вертолёту? Вертолёт
> взорвался; +25 очков;
> любой парашютист, в
> данный момент падающий с
> этого вертолёта, теперь в
> свободном падении.

> Попал в парашютиста в
> полёте? Парашютист умер;
> +10 очков.

> Попал в парашют вместо
> человека? Купол порвался;
> человек падает быстрее;
> +5 очков (тебя засчитали
> за парашют, но человек
> ещё может выжить при
> теперь-более-быстрой
> посадке).

> Парашютист сел нормально?
> Парашютист идёт к твоей
> пушке. Четыре парашютиста
> добрались до пушки =
> конец игры.

> Истребитель промчался?
> У тебя около двух секунд
> на выстрел. Промазал?
> Бомба. Твоей базы нет.

> ── ЧАСТЬ 3: ПРО CGA ──

> CGA-палитра знаменито
> уродлива. Режим 1
> (которым пользуется
> Paratrooper) даёт четыре
> цвета: чёрный, циан,
> маджента, белый. Эти
> четыре — не привлекательные
> цвета вместе. Это цвета
> медицинской приёмной
> 1979-го.

> Но Orion сделали
> единственное, что можно
> было сделать с CGA-палитрой:
> опереться на неё.
> Небо беспардонно цианное.
> Песок белый. Вертолёты —
> маджента. Парашютисты —
> тоже маджента (с белыми
> парашютами). Пушка — тоже
> маджента. Пули — белые.

> Эта палитра, парадоксально,
> очень читаема. Ты всегда
> знаешь, где угроза
> (маджента), где пуля
> (белая), где песок
> (белый) и где небо
> (циан). Мозг быстро
> перестаёт замечать
> уродливый цветовой выбор
> и начинает видеть игру.

> Десятилетия спустя, когда
> 32-битный цвет стал
> стандартом, а CGA — шуткой,
> люди, выросшие на
> CGA-палитре, ощущали
> маленькую ностальгию по
> циану-и-мадженте. Не
> красиво, но это ТО, КАК
> ВЕЩИ ВЫГЛЯДЕЛИ, когда ты
> был молод.

> ── ЧАСТЬ 4: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — PARATROOPER.
> Пиксельно-верный CGA-
> оригиналу. Цианное небо,
> белый песок, маджентовые
> угрозы.

> Выбери стиль управления
> на title-экране: TAP
> (кликни-и-стреляй —
> современный) или ARROWS
> (вращай ствол d-pad'ом,
> стреляй красной кнопкой —
> классический). Тап-стиль
> быстрее на сенсорном
> экране. Стиль со стрелками
> сложнее, аутентичнее, и
> единственный, который
> воссоздаёт точное
> Разочарование-1982,
> которое мы ловим.

> Защищай базу. Не остаться
> без патронов. Следи за
> истребителем.

> ── СНОСКА У КОСТРА ──

> PARATROOPER — игра 1982-го
> от Orion Software (а
> конкретно Грег Куперберг,
> иногда указанный как
> ведущий программист).
> Orion Software распалась
> к середине 1980-х.
> Оригинальная игра остаётся
> собственностью её
> правообладателей. Мы не
> переиспользуем их код или
> арт; наша дань —
> полная реимплементация.
> Вопросы: elyssov@gmail.com.
""".trimIndent()

val PARATROOPER_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val PARATROOPER_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickParatrooperIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> PARATROOPER_INTRO_RU
    else -> PARATROOPER_INTRO_EN
}

val PARATROOPER_INTRO_TEXT: String = PARATROOPER_INTRO_EN
