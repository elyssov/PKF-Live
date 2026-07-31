package com.pixelclassics.app.games.asteroids

/**
 * Long-form campfire intro for ROCK STORM (Asteroids). Narrated by an
 * Atari vector-CRT cabinet, white-on-black phosphor humming, alone in
 * the back room of a Pizza Hut in Sunnyvale, twenty years after closing.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
████████████████████████████████████████
█  ATARI · ASTEROIDS · NOVEMBER 1979    █
█  VECTOR DISPLAY · WHITE PHOSPHOR      █
█  6502 @ 1.5 MHz · 13K OF RAM          █
████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> ▶ INSERT COIN ◀

> Wait. There is nobody here.

> The Pizza Hut closed in 2003. I
> have been here, in the back-room
> storage area, behind a stack of
> folded vinyl banners advertising
> a "stuffed crust" promotion that
> ended during the Clinton
> administration, ever since.
> Someone forgot me when they
> unplugged the building.

> I am still plugged in to the
> wall, technically. The wall is
> still on the city grid. I am
> humming. My vector CRT, after
> 47 years, still draws sharp
> white lines on a perfect black
> background.

> Let me tell you what I am.

> ── PART 1: VECTOR GRAPHICS ──

> Most arcade cabinets in 1979
> used raster displays — TV-like
> CRTs where the electron beam
> scanned line by line across
> the screen, sixty times per
> second, displaying whatever
> the cabinet's circuitry put in
> the framebuffer. Raster is how
> almost every screen since has
> worked.

> A small number of arcade
> cabinets, including me, used
> VECTOR displays instead. In a
> vector display, the electron
> beam does not scan back and
> forth. The beam goes
> WHEREVER the circuit tells it
> to go, drawing line segments
> directly between specified
> endpoints. The beam draws each
> game object as a series of
> connected lines, traced in
> real time.

> The result is that on a
> vector display, lines are
> RAZOR-SHARP. There is no pixel
> grid. The beam is a single
> moving point of light. Diagonal
> lines look like diagonal lines,
> not staircases. Circles look
> like circles. Type looks like
> calligraphy.

> The disadvantage is that you
> cannot draw FILLED regions on
> a vector display. The beam is
> a point — it has no width and
> no area. You can only draw
> outlines. Everything is wire-
> frame.

> Vector arcade games therefore
> developed a distinct visual
> language: outlines on pure
> black, no fills, no textures,
> high contrast. Looking at a
> vector cabinet in a dark
> arcade was looking at lines of
> pure light drawn on a sheet of
> ink. Many veterans of 1979-
> 1984 maintain, to this day,
> that vector graphics looked
> BETTER than raster graphics —
> not in spite of being
> "primitive" but BECAUSE of it.

> ASTEROIDS was the second-most-
> successful vector arcade
> cabinet of all time (after
> the slightly earlier SPACE WARS
> at Cinematronics). I sold 70,000
> cabinets at ${'$'}2,500 wholesale
> each. Atari made roughly half a
> billion dollars in 1980 dollars.

> ── PART 2: THE GAME ──

> You pilot a small triangular
> ship at the centre of the
> screen. The screen wraps:
> fly off the right edge, you
> reappear on the left. Fly
> off the top, you reappear on
> the bottom. The screen is a
> torus.

> Floating around the screen are
> ASTEROIDS — irregular
> polygonal shapes, drifting in
> straight lines at various
> speeds. Hit an asteroid with
> your ship, you die. Hit an
> asteroid with a bullet, it
> SPLITS into two smaller
> asteroids. Hit a small
> asteroid with a bullet, it
> shatters and vanishes.

> Clear all asteroids on the
> screen, you advance to the
> next wave. More asteroids
> spawn, faster, bigger field.

> Occasionally a UFO flies
> across the screen, shooting
> at you. Two UFO sizes: BIG
> UFO (slow, poor aim, 200
> points) and SMALL UFO (fast,
> sharp aim, 1000 points). The
> Small UFO is one of the
> earliest examples of a true
> AI opponent in arcade games —
> it leads its shots based on
> your velocity.

> Your ship has three buttons:
>   THRUST — fires the rear
>     engine. Newtonian physics:
>     you accelerate in the
>     direction your ship is
>     facing. There is no
>     friction. Stop thrusting
>     and you keep drifting
>     forever in the same
>     direction. Reverse-thrust
>     does not exist; you must
>     rotate 180° and thrust
>     again to slow down.
>   ROTATE LEFT / ROTATE RIGHT —
>     turns the ship in place
>     without changing its
>     velocity vector.
>   FIRE — shoots one bullet
>     forward.
>   HYPERSPACE — emergency
>     teleport. Your ship
>     disappears and reappears
>     at a random location on
>     screen. The random
>     location MIGHT be inside
>     an asteroid. In which
>     case, you instantly die.
>     Hyperspace is the gambler's
>     last resort.

> ── PART 3: WHY IT MATTERED ──

> ASTEROIDS taught a generation of
> kids about NEWTONIAN PHYSICS. Not
> Galileo's "things fall down".
> Real Newton: an object in motion
> stays in motion unless acted on
> by a force. The first time you
> tried to thrust toward an
> asteroid that was already moving
> toward you, and you found
> yourself unable to stop and
> ALSO unable to dodge because you
> had given yourself far too much
> velocity, you understood
> momentum.

> This was, for a small generation
> of nine-year-olds in 1980, the
> moment when ELEVENTH-GRADE
> PHYSICS suddenly made sense.

> The hyperspace button taught a
> different lesson: PROBABILITY.
> 80% chance you reappear safely.
> 20% chance you reappear inside
> an asteroid. Do you press it?
> Depends what your alternative
> is. If you are about to be
> hit anyway, hyperspace is worth
> the gamble. If you have any
> other move available, do not
> press hyperspace.

> ── PART 4: WHAT WE BUILT ──

> Below is ROCK STORM. We use a
> RASTER display (we have no
> choice — your phone has no
> vector tube). But we draw
> everything in PURE WHITE LINES
> on PURE BLACK BACKGROUND. No
> fills. No textures. No
> shading. We approximate the
> vector aesthetic on a raster
> screen.

> The ship is a triangle. The
> asteroids are polygons. The
> UFOs are small saucer outlines.
> The bullets are tiny squares
> (the original used a single
> pixel; we use a 2x2 cluster
> for visibility on modern
> high-resolution displays).

> The screen wraps. Newtonian
> physics is preserved.
> Hyperspace is preserved (we
> took it OUT in v6.2.1 because
> a tester accidentally
> hyperspaced themselves into
> an asteroid eleven times in a
> row and complained; but the
> mechanic is the mechanic, so
> it is RESTORED).

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> ASTEROIDS and related marks belong
> to their respective rights-holders. The
> mechanic — a ship, screen wrap,
> Newtonian inertia, asteroids
> splitting on shot — has been
> imitated so many times since
> 1979 that it has effectively
> become genre vocabulary. Our
> tribute is called ROCK STORM.
> Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> ▶ ОПУСТИТЕ МОНЕТУ ◀

> Постой. Здесь никого нет.

> Пиццерия закрылась в 2003-м.
> Я здесь, на складе подсобки,
> за стопкой свёрнутых
> виниловых баннеров,
> рекламирующих акцию
> «двойной сыр в корке»,
> закончившуюся при
> администрации Клинтона, с
> тех самых пор. Кто-то забыл
> меня, когда отключали
> здание.

> Я всё ещё включён в розетку,
> технически. Розетка всё ещё
> в городской сети. Я гужу.
> Моя векторная ЭЛТ-трубка,
> через 47 лет, всё ещё
> рисует резкие белые линии
> на идеально-чёрном фоне.

> Расскажу тебе, что я такое.

> ── ЧАСТЬ 1: ВЕКТОРНАЯ ГРАФИКА ──

> Большинство аркадных
> кабинетов в 1979-м
> использовали растровые
> дисплеи — ТВ-подобные ЭЛТ,
> где электронный луч
> сканировал строчка за
> строчкой по экрану,
> шестьдесят раз в секунду,
> отображая то, что схема
> кабинета положила во
> фреймбуфер. Растр — это то,
> как почти каждый экран с
> тех пор работает.

> Небольшое количество
> аркадных кабинетов, включая
> меня, использовали ВЕКТОРНЫЕ
> дисплеи. На векторном
> дисплее электронный луч не
> сканирует туда-сюда. Луч
> идёт КУДА скажет схема,
> рисуя отрезки прямо между
> заданными точками. Луч
> рисует каждый игровой
> объект как серию
> соединённых линий,
> прочерченных в реальном
> времени.

> Результат: на векторном
> дисплее линии
> БРИТВЕННО-ОСТРЫЕ. Никакой
> пиксельной сетки. Луч —
> одна движущаяся точка
> света. Диагональные линии
> выглядят как диагонали, а
> не как лесенки. Круги
> выглядят как круги. Шрифт
> выглядит как каллиграфия.

> Недостаток: на векторном
> дисплее нельзя рисовать
> ЗАЛИТЫЕ области. Луч — это
> точка, у неё нет ширины и
> площади. Можно рисовать
> только контуры. Всё —
> проволочный каркас.

> Поэтому векторные аркадные
> игры разработали отдельный
> визуальный язык: контуры на
> чистом чёрном, никаких
> заливок, никаких текстур,
> высокий контраст. Смотреть
> на векторный кабинет в
> тёмной аркаде значило
> смотреть на линии чистого
> света, нарисованные по
> листу чернил. Многие
> ветераны 1979-1984
> утверждают по сей день, что
> векторная графика выглядела
> ЛУЧШЕ растровой — не
> вопреки «примитивности», а
> ИМЕННО благодаря ей.

> ASTEROIDS был вторым самым
> успешным векторным аркадным
> кабинетом всех времён
> (после чуть более раннего
> SPACE WARS у Cinematronics).
> Я продал 70 000 кабинетов
> по 2 500 долларов оптом.
> Atari заработала примерно
> полмиллиарда в долларах
> 1980-го.

> ── ЧАСТЬ 2: ИГРА ──

> Ты пилотируешь маленький
> треугольный корабль в
> центре экрана. Экран
> закольцован: вылетишь за
> правый край — появишься
> слева. Вылетишь за верх —
> появишься снизу. Экран —
> это тор.

> По экрану дрейфуют
> АСТЕРОИДЫ — неправильные
> полигональные формы, идут
> по прямым с разными
> скоростями. Врежешься
> кораблём в астероид —
> умрёшь. Попадёшь в астероид
> пулей — он РАЗДЕЛИТСЯ на
> два меньших астероида.
> Попадёшь в маленький
> астероид пулей — он
> разлетится и исчезнет.

> Очистил все астероиды на
> экране — переходишь на
> следующую волну. Появляется
> больше астероидов, быстрее,
> больше поле.

> Иногда по экрану пролетает
> НЛО, стреляя в тебя. Два
> размера НЛО: БОЛЬШОЙ НЛО
> (медленный, плохо целится,
> 200 очков) и МАЛЕНЬКИЙ НЛО
> (быстрый, точный, 1000
> очков). Маленький НЛО —
> один из ранних примеров
> настоящего AI-противника в
> аркадных играх — он
> упреждает выстрелы по
> твоей скорости.

> У корабля три кнопки:
>   ТЯГА — включает задний
>     двигатель. Ньютоновская
>     физика: ты ускоряешься в
>     направлении, куда смотрит
>     корабль. Трения нет.
>     Перестанешь тянуть —
>     будешь дрейфовать в том
>     же направлении вечно.
>     Реверс-тяги не
>     существует; чтобы
>     остановиться, надо
>     развернуться на 180° и
>     снова дать тягу.
>   ПОВОРОТ ВЛЕВО / ВПРАВО —
>     поворачивает корабль на
>     месте, не меняя его
>     вектор скорости.
>   ОГОНЬ — пуля вперёд.
>   ГИПЕРПРОСТРАНСТВО —
>     аварийная телепортация.
>     Корабль исчезает и
>     появляется в случайном
>     месте экрана. Случайное
>     место МОЖЕТ оказаться
>     внутри астероида. В
>     таком случае — мгновенная
>     смерть. Гиперпространство
>     — последний шанс игрока.

> ── ЧАСТЬ 3: ПОЧЕМУ ЭТО ВАЖНО ──

> ASTEROIDS научили поколение
> детей НЬЮТОНОВСКОЙ ФИЗИКЕ.
> Не галилеево «вещи падают
> вниз». Настоящий Ньютон:
> объект в движении остаётся
> в движении, если на него не
> действует сила. В первый
> раз, когда ты попытался
> дать тягу в сторону уже
> летящего на тебя астероида
> и обнаружил, что не можешь
> ни остановиться, ни
> увернуться, потому что
> разогнался слишком сильно —
> ты понял импульс.

> Это был для маленького
> поколения 9-летних в 1980-м
> момент, когда ФИЗИКА
> ОДИННАДЦАТОГО КЛАССА вдруг
> обрела смысл.

> Кнопка гиперпространства
> учила другому уроку:
> ВЕРОЯТНОСТИ. 80% шанс
> появиться живым. 20% шанс
> появиться внутри астероида.
> Нажмёшь? Зависит от
> альтернативы. Если тебя и
> так сейчас собьют —
> гиперпространство стоит
> риска. Если есть любой
> другой ход — не жми.

> ── ЧАСТЬ 4: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — ROCK STORM. Мы
> используем РАСТРОВЫЙ
> дисплей (выбора нет — у
> твоего телефона нет
> векторной трубки). Но
> рисуем всё ЧИСТО БЕЛЫМИ
> ЛИНИЯМИ на ЧИСТО ЧЁРНОМ
> ФОНЕ. Никаких заливок.
> Никаких текстур. Никакой
> штриховки. Приближаем
> векторную эстетику на
> растровом экране.

> Корабль — треугольник.
> Астероиды — полигоны. НЛО
> — маленькие контуры
> летающих тарелок. Пули —
> крошечные квадратики
> (оригинал использовал один
> пиксель; мы используем
> 2x2 кластер для видимости
> на современных
> высокого-разрешения
> экранах).

> Экран закольцован.
> Ньютоновская физика
> сохранена. Гиперпространство
> сохранено (мы УБРАЛИ его в
> v6.2.1, потому что тестер
> случайно
> гиперпространствовал себя в
> астероид одиннадцать раз
> подряд и пожаловался; но
> механика есть механика, и
> она ВОССТАНОВЛЕНА).

> ── СНОСКА У КОСТРА ──

> ASTEROIDS и связанные знаки
> принадлежат их
> соответствующим
> правообладателям. Механика
> — корабль, закольцованный
> экран, ньютоновская
> инерция, астероиды
> делящиеся от выстрела —
> копировалась столько раз с
> 1979-го, что фактически
> стала жанровым словарём.
> Наша дань называется ROCK
> STORM. Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val ASTEROIDS_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val ASTEROIDS_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickAsteroidsIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> ASTEROIDS_INTRO_RU
    else -> ASTEROIDS_INTRO_EN
}

val ASTEROIDS_INTRO_TEXT: String = ASTEROIDS_INTRO_EN
