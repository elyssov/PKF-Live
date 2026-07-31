package com.pixelclassics.app.games.exodus

/**
 * Long-form campfire intro for EXODUS — told as a refugee's letter on a
 * piece of folded notebook paper, mailed (when post still worked) to
 * a friend in another country, dated 2025.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
╭────────────────────────────────────╮
│   ОТПРАВЛЕНО · APRIL 2025          │
│   FROM: REDACTED, MOSCOW           │
│   TO:   REDACTED, LISBON           │
╰────────────────────────────────────╯
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> Привет.

> Take this letter as you find it.
> Translation is rough; the meaning
> survives.

> ── PART 1: LEMMINGS ──

> In the spring of 1991 — I was
> fifteen — a Scottish company called
> DMA Design (based in Dundee, a town
> nobody outside of Scotland has ever
> heard of) shipped a game called
> LEMMINGS on the Amiga.

> The premise was a small parable.
> A line of small green-haired
> humanoid creatures (the "lemmings")
> would emerge from a trapdoor at the
> top of the level and walk
> mechanically, single-file, toward
> the bottom of the screen. They had
> no will of their own. They walked
> off cliffs without hesitation. They
> walked into fires. They walked into
> spike pits. They walked into the
> teeth of crushers, gears, lava,
> blades — anything that lay in their
> path.

> The player's job was to assign
> ROLES to individual lemmings: a
> BUILDER would lay down a small
> staircase; a BASHER would tunnel
> through walls; a BLOCKER would
> stand still and turn the column
> around; a CLIMBER would climb
> vertical walls; a FLOATER would
> open an umbrella and fall slowly.
> Each role consumed one "credit"
> from a small inventory at the
> bottom of the screen.

> The win condition: get a certain
> percentage of the lemming column
> safely to the exit before the
> timer ran out. The loss condition:
> too many lemmings drowned, burned,
> or got crushed.

> It was a heartbreaking little
> game. The lemmings walked
> mindlessly. The player was the
> only mind in the world. The
> player was responsible for
> everyone.

> ── PART 2: THE OBVIOUS METAPHOR ──

> Then, somewhere around 2014,
> something happened in my country.

> Websites started getting blocked.
> First — extremist material, on
> reasonable grounds. Then —
> opposition newspapers, on shakier
> grounds. Then — VPN services, on
> the grounds that VPN services
> bypass the previous blocks. Then —
> some social networks, on the
> grounds that they did not provide
> data to authorities. Then —
> educational platforms, supposedly
> by accident. Then — Wikipedia
> articles, individually, for hours
> at a time.

> By 2017, the agency responsible
> for blocking websites had a name
> that everyone knew but few said
> aloud — the acronym for
> "Federal Service for Supervision
> of Communications, Information
> Technology and Mass Communications".
> In Russian it shortens to four
> letters, three syllables: RKN.
> Say it out loud — ROS-KOM-NADZOR
> clipped down to "ErKaEn" — and
> you hear the same bouncy rhythm
> as CHEBURASHKA, the sweet little
> brown creature with oversized
> ears from the 1969 Soviet
> stop-motion film. A creature so
> naive it doesn't even know what
> species it is.

> Internet users started to draw
> the agency as the cartoon
> character, advancing across maps
> of the Russian internet, leaving
> burned websites behind. The
> drawings spread on Telegram.
> Newspapers picked them up.
> Eventually the cartoon character
> became, in the public imagination,
> the personified spirit of online
> censorship.

> The end state has a folk name
> too. The dream of a fully
> isolated, sovereign, fenced-off
> Russian internet — no Google, no
> Wikipedia, no world — is called,
> by the people who will have to
> live in it, the CHEBURNET. The
> word carries a precise flavour
> that translates roughly as:
> "our own, homespun — and dumb."
> Nobody who uses the word wants
> to live there. That is the whole
> point of the word.

> ── PART 3: WHY EXODUS ──

> A LEMMINGS-shaped game was
> obviously waiting to be made.

> A column of pixel-people emerges
> from a HOUSE on the left of the
> screen, tagged with the white-blue-
> red of the Russian flag. The
> people walk RIGHT, mechanically,
> single-file, toward the right
> edge of the screen.

> The right edge of the screen is
> tagged VPN — a small green door
> behind which lies, in our metaphor,
> the wider Internet. The world.
> Freedom of inquiry.

> Between HOME and the VPN exit are
> obstacles: pits of fire, walls of
> bureaucracy, rivers of data, cliffs
> where the column will fall to
> nothing. The player places BRIDGES
> across pits, LADDERS over walls,
> SIGNS to turn the column,
> UMBRELLAS to soften falls, and
> can SACRIFICE a single lemming-
> person as a STOP-BLOCKER to give
> others a chance.

> Coming from the left, advancing
> slowly across the level, is a
> giant figure with very large round
> ears, branded across its chest
> with the four-letter acronym. The
> figure drags a trail of fire
> behind it. Whatever the figure
> catches up to, burns.

> Save thirty percent of the column.
> Beat six levels. End-of-game
> screen reads:

>     "RKN weeps."

> ── PART 4: THE TONE ──

> This is satire. It is not a
> manifesto. It is not a call to
> action. The game does not name
> any individual person. It does not
> claim that any particular country
> is uniquely evil; censorship of
> the internet is a global trend,
> and EVERY large country has its
> own version of the same struggle.
> We chose to make our parable in
> the visual language of the country
> we know best, because satire is
> sharper when it speaks the dialect
> of where it was born.

> The freedom to make this little
> satirical level is itself the
> point of the game. If it ever
> becomes impossible to publish a
> game like this, the game has
> already lost its main message.

> Right now, in 2026, you are
> reading this on a screen and
> about to play a small Lemmings-
> alike that you bought from the
> Google Play Store. That fact
> alone — that you can — is the
> happy ending we hope for.

> ── PART 5: HOW IT PLAYS ──

> Tap a TOOL at the bottom of the
> screen to select it. Tap the
> MAP to place it. The crowd walks
> automatically. The censor crawls
> forward inexorably. You have a
> limited number of each tool per
> level. Plan accordingly.

> The censor's speed scales by
> level. So does the difficulty of
> the terrain. Level six is the
> finale and requires every tool
> you have.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> LEMMINGS is a trademark of DMA
> Design (now part of Sony, via the
> Psygnosis acquisition). The
> mechanic — a self-walking column
> assigned roles by the player to
> guide it through hazards — is no
> longer protectable, having been
> implemented hundreds of times in
> the intervening 35 years. The
> cartoon character we draw as our
> antagonist is property of his
> creators (the author of the
> original 1966 children's book and
> Soyuzmultfilm's 1969 animation
> studios); we draw our caricature
> only as satirical allegory. The
> agency is a real government body
> with a real acronym; satire of
> government activities is legally
> protected expression in most
> jurisdictions where this game is
> sold. Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Привет.

> Возьми это письмо как есть.
> Перевод грубый; смысл
> выживает.

> ── ЧАСТЬ 1: LEMMINGS ──

> Весной 1991-го — мне было
> пятнадцать — шотландская
> компания DMA Design (из
> Данди, города, о котором никто
> за пределами Шотландии не
> слышал) выпустила игру
> LEMMINGS на Amiga.

> Концепция была маленькой
> притчей. Вереница маленьких
> зеленоволосых гуманоидных
> существ («лемминги») выходила
> из люка наверху уровня и
> шла механически, гуськом, к
> низу экрана. Они не имели
> собственной воли. Они без
> колебаний шагали с обрывов.
> Они шли в огонь. Они шли в
> ямы с шипами. Они шли в зубы
> дробилок, шестерён, лавы,
> лезвий — во всё, что лежало
> на их пути.

> Задача игрока — назначать
> РОЛИ отдельным леммингам:
> СТРОИТЕЛЬ кладёт маленькую
> лестницу; ПРОБИВАЛА туннелит
> сквозь стены; БЛОКИРОВЩИК
> стоит на месте и
> разворачивает колонну;
> АЛЬПИНИСТ лезет по
> вертикальным стенам;
> ПАРАШЮТИСТ раскрывает зонт и
> медленно падает. Каждая роль
> тратит один «кредит» из
> маленького инвентаря внизу
> экрана.

> Условие победы: довести
> определённый процент
> колонны лемммингов до
> выхода до того, как
> кончится таймер. Условие
> поражения: слишком много
> леммингов утонули, сгорели
> или были раздавлены.

> Это была маленькая
> душераздирающая игра.
> Лемминги шли бездумно.
> Игрок был единственным
> разумом в мире. Игрок
> отвечал за всех.

> ── ЧАСТЬ 2: ОЧЕВИДНАЯ МЕТАФОРА ──

> Потом, где-то в 2014-м,
> что-то случилось в моей
> стране.

> Сайты начали блокировать.
> Сначала — экстремистский
> материал, на разумных
> основаниях. Потом —
> оппозиционные газеты, на
> более шатких. Потом — VPN-
> сервисы, на основании того,
> что VPN-сервисы обходят
> предыдущие блоки. Потом —
> некоторые соцсети, на
> основании того, что они не
> предоставляли данные властям.
> Потом — образовательные
> платформы, якобы по
> случайности. Потом — статьи
> Википедии, поштучно, на
> часы за раз.

> К 2017-му у агентства,
> ответственного за блокировку
> сайтов, было имя, которое
> знали все, но мало кто
> говорил вслух — аббревиатура
> от «Федеральная служба по
> надзору в сфере связи,
> информационных технологий и
> массовых коммуникаций».
> Четыре буквы, три слога:
> РКН. Произнеси вслух —
> «Эр-Ка-эН» — и услышишь тот
> же прыгучий ритм, что и в
> имени ЧЕБУРАШКА: милого
> маленького коричневого
> существа с огромными ушами
> из советского кукольного
> фильма 1969-го. Существа
> настолько наивного, что оно
> даже не знает, какого оно
> вида.

> Интернет-пользователи начали
> рисовать агентство как этот
> мультяшный персонаж,
> наступающий через карты
> русского интернета, оставляя
> позади выжженные сайты.
> Рисунки распространились в
> Telegram. Газеты подхватили.
> В конце концов мультяшный
> персонаж стал, в
> общественном воображении,
> олицетворённым духом онлайн-
> цензуры.

> У конечной точки тоже есть
> народное имя. Мечта о
> полностью изолированном,
> суверенном, отгороженном
> русском интернете — без
> Google, без Википедии, без
> мира — зовётся теми, кому в
> нём жить, ЧЕБУРНЕТОМ. Слово
> несёт точный вкус: «своё,
> посконное — и тупое». Никто
> из тех, кто это слово
> употребляет, жить там не
> хочет. В этом и весь смысл
> слова.

> ── ЧАСТЬ 3: ПОЧЕМУ EXODUS ──

> LEMMINGS-образная игра
> очевидно ждала, чтобы её
> сделали.

> Колонна пиксель-человечков
> выходит из ДОМА слева на
> экране, помеченного бело-сине-
> красным русского флага. Люди
> идут ВПРАВО, механически,
> гуськом, к правому краю
> экрана.

> Правый край экрана помечен
> VPN — маленькая зелёная
> дверь, за которой лежит, в
> нашей метафоре, более
> широкий Интернет. Мир.
> Свобода познания.

> Между ДОМОМ и выходом VPN —
> препятствия: ямы огня, стены
> бюрократии, реки данных,
> обрывы, на которых колонна
> упадёт в никуда. Игрок ставит
> МОСТЫ через ямы, ЛЕСТНИЦЫ
> через стены, ЗНАКИ, чтобы
> развернуть колонну, ЗОНТЫ
> для смягчения падений, и
> может ПОЖЕРТВОВАТЬ одного
> человечка-лемминга как
> СТОП-БЛОКИРОВЩИКА, чтобы
> дать шанс остальным.

> Слева, медленно продвигаясь
> через уровень, идёт огромная
> фигура с очень большими
> круглыми ушами, на груди —
> та четырёхбуквенная
> аббревиатура. Фигура
> тянет за собой огненный
> след. Что бы фигура ни
> догнала — горит.

> Спаси тридцать процентов
> колонны. Пройди шесть
> уровней. Экран конца игры
> читает:

>     «РКН плачет.»

> ── ЧАСТЬ 4: ТОН ──

> Это сатира. Это не
> манифест. Это не призыв к
> действию. Игра не называет
> ни одного конкретного
> человека. Не утверждает,
> что какая-то страна
> уникально зла; цензура
> интернета — глобальный
> тренд, и У КАЖДОЙ большой
> страны есть своя версия
> той же борьбы. Мы решили
> сделать нашу притчу на
> визуальном языке страны,
> которую знаем лучше всего,
> потому что сатира острее,
> когда говорит на диалекте
> того места, где родилась.

> Свобода сделать этот
> маленький сатирический
> уровень — сама по себе суть
> игры. Если когда-нибудь
> станет невозможно
> опубликовать такую игру,
> игра уже потеряла главное
> сообщение.

> Прямо сейчас, в 2026-м, ты
> читаешь это на экране и
> вот-вот сыграешь в
> маленькую Lemmings-подобную,
> которую купил в Google Play
> Store. Один этот факт — что
> ты можешь — счастливый
> конец, на который мы
> надеемся.

> ── ЧАСТЬ 5: КАК ИГРАТЬ ──

> Кликни ИНСТРУМЕНТ внизу
> экрана, чтобы выбрать.
> Кликни КАРТУ, чтобы
> поставить. Толпа идёт
> автоматически. Цензор
> ползёт вперёд неумолимо. У
> тебя ограниченное количество
> каждого инструмента на
> уровень. Планируй
> соответственно.

> Скорость цензора растёт по
> уровням. Сложность местности
> тоже. Шестой уровень —
> финал, требует каждого
> инструмента, что у тебя
> есть.

> ── СНОСКА У КОСТРА ──

> LEMMINGS — торговая марка
> DMA Design (теперь часть
> Sony через приобретение
> Psygnosis). Механика —
> сама-идущая колонна,
> которой игрок назначает
> роли, чтобы провести её
> сквозь опасности — больше
> не охраняется, будучи
> реализованной сотни раз за
> прошедшие 35 лет.
> Мультяшный персонаж,
> которого мы рисуем как
> антагониста — собственность
> его создателей (автор
> оригинальной детской книги
> 1966-го и анимационной
> студии «Союзмультфильм»
> 1969-го); мы рисуем
> карикатуру только как
> сатирическую аллегорию.
> Агентство — реальный
> госорган с реальной
> аббревиатурой; сатира
> деятельности государства —
> юридически охраняемое
> выражение в большинстве
> юрисдикций, где эта игра
> продаётся. Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val EXODUS_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val EXODUS_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickExodusIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> EXODUS_INTRO_RU
    else -> EXODUS_INTRO_EN
}

val EXODUS_INTRO_TEXT: String = EXODUS_INTRO_EN
