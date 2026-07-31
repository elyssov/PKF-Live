package com.pixelclassics.app.games.muncher

/**
 * Long-form campfire intro for MUNCHER (Pac-Man homage). Narrated as a
 * historical-museum piece about the 1980 Namco arcade — Iwatani's
 * design process, sister's request for a non-violent game, the pizza
 * silhouette anecdote, the four ghosts' AI personalities. All
 * documentary commentary on a famous public history. Our tribute uses
 * different cast, art and maze.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
████████████████████████████████████████
█  NAMCO LIMITED · PAC-MAN · 1980      █
█  TORU IWATANI · ASSISTANT DESIGNER   █
█  ARCADE CABINET · 8-WAY JOYSTICK     █
████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> Hello. My name is Toru Iwatani.
> I was born in 1955 in Tokyo. I
> joined Namco Limited in 1977 as
> a young engineer, mostly making
> pinball machines.

> I want to tell you a story about
> a pizza, an angry man, and a
> family decision to make video
> games friendlier.

> ── PART 1: THE PROBLEM IN 1979 ──

> Arcades in Japan in 1979 were
> hostile environments for half the
> population. The cabinets that
> drove the most coins were
> SPACE INVADERS (1978, Taito),
> ASTEROIDS (1979, Atari), and
> their many clones. All shooters.
> All combat. The arcade was, in
> 1979, a place where you
> defeated enemies. The
> aesthetic was tanks, missiles,
> aliens.

> Women did not, generally,
> visit arcades. The percentage of
> female players in any given
> Japanese arcade in 1979 was
> estimated at less than 5%. The
> environment was loud, smoky,
> male-dominated, full of older
> teenagers showing off scores on
> shooting games.

> My sister, who was 14 at the
> time, asked me once: "Aniki,
> can you make a game where you
> don't have to KILL anything?
> I don't enjoy shooting." I
> said I would think about it.

> ── PART 2: THE PIZZA ──

> I was eating dinner with a few
> Namco colleagues at a Tokyo
> izakaya. We had ordered a
> pizza. I took the first slice.
> Looking down at the pizza pan,
> with one slice missing, I saw
> a shape — a circle with a
> wedge cut out of it. The
> wedge was the open mouth.
> The circle was the head.

> I sketched the shape on a
> napkin. I added two eyes. I
> added small leg-like
> appendages at the bottom.
> Within twenty minutes I had
> the silhouette of what would
> become PAC-MAN.

> The next morning I started
> sketching the game design at
> the Namco office. I had three
> goals from my sister's
> request:
>   1. The player should EAT, not
>      shoot.
>   2. The setting should be a
>      PLAYFUL maze, not a
>      battlefield.
>   3. The enemies should be
>      CUTE, not scary. Defeating
>      them should feel like
>      catching them, not
>      destroying them.

> The maze was straightforward.
> The eating was straightforward
> (you walk over a pellet, you
> eat it). The cute enemies took
> longer. I designed four
> GHOSTS — round, sheet-like,
> wide-eyed. Each one a different
> bright colour. Each one with
> a slightly different personality.

> ── PART 3: THE GHOSTS' AI ──

> This is the part I am most
> proud of, looking back.

> The four ghosts have DISTINCT
> AI personalities, hardcoded.
> One is direct — he targets
> your current position. One
> is an ambusher — he aims
> ahead of you, where you are
> going, not where you are.
> One is unpredictable — his
> target is computed from two
> reference points and ends up
> drifting in odd patterns.
> One is shy — he advances
> until he gets close, then
> retreats. He never quite
> catches you, but he harasses
> you the whole time.

> The four personalities
> together create a sense that
> the ghosts are LIVING
> CREATURES with intentions —
> not random AIs. Players
> spent hours studying their
> habits. By the mid-1980s,
> serious players had named
> the precise patterns of
> ghost behaviour and could
> describe them as if they
> were characters in a novel.

> Players also discovered that
> if the AI logic ran
> deterministically — and it
> did — then PERFECT PATTERNS
> existed. A precise sequence
> of joystick movements that
> would clear every pellet on
> a stage without ever being
> caught. Walkthrough booklets
> distributed these patterns
> throughout the early 1980s.

> ── PART 4: THE NAME CHANGE ──

> The original Japanese title was
> a transliteration of the
> Japanese onomatopoeia for
> "chomp-chomp". Latinised for
> English-speaking markets with
> a P. The American distributor
> immediately objected: the P
> on the cabinet might be
> easily defaced with a marker
> stroke to become an F, and
> they did not want their
> 1980 family-arcade title to
> be vandalised into a sexually-
> suggestive Beatles reference.
> The name was changed for the
> US market — to what you know
> today.

> ── PART 5: WHAT WE BUILT ──

> Below is MUNCHER. The hero is
> a SQUARE chomper-bot, not a
> yellow circle. Our maze layout
> is our own — not the
> original's. The "spooks" are
> our own design, not the four
> ghosts. The power pellets and
> tunnel-wrap mechanic are
> preserved because those are
> public-domain ideas.

> The maze-chase mechanic itself
> — walk-eat-pellets-get-chased-
> by-enemies-power-pellet-eat-
> enemies-back-to-being-chased
> — is the central genre I
> invented in 1979, looking down
> at a slice of pizza.

> Direction pad to walk. Eat
> dots. Eat power pellets when
> the spooks are dangerous —
> they turn frightened for seven
> seconds, during which you can
> eat them too. Clear every
> dot to advance the level.

> My sister, Kyoko, is now in
> her sixties. She finally
> visited an arcade in 1980,
> after I had finished my
> game. She played for hours.
> She was very good at it. She
> is still my best playtester.

> Kyoko, if you read this —
> arigato. The whole genre is
> yours.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> The 1980 arcade game and its
> ghost personalities belong to
> their respective rights-
> holders (Namco, now Bandai
> Namco Entertainment). Our
> tribute is called MUNCHER and
> uses an entirely different
> cast, art, and maze design.
> The general mechanic —
> maze-chase with dots, power
> pellets, and eatable enemies
> during vulnerability — has
> been imitated countless times
> and is no longer protectable
> under any modern game-design
> system. Iwatani-sensei is
> still alive, still teaching;
> if you are reading this,
> sensei — hat off, again.
> Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Здравствуй. Меня зовут Тору
> Иватани. Я родился в 1955-м
> в Токио. В 1977-м пришёл в
> Namco Limited как молодой
> инженер, в основном делал
> пинбол-машины.

> Я расскажу тебе историю про
> пиццу, сердитого мужчину и
> семейное решение сделать
> видеоигры дружелюбнее.

> ── ЧАСТЬ 1: ПРОБЛЕМА В 1979 ──

> Аркады в Японии в 1979-м
> были враждебной средой для
> половины населения. Кабинеты,
> приносящие больше всего
> монет, — SPACE INVADERS
> (1978, Taito), ASTEROIDS
> (1979, Atari) и их многие
> клоны. Все шутеры. Всё —
> бой. Аркада в 1979-м была
> местом, где ты побеждаешь
> врагов. Эстетика — танки,
> ракеты, инопланетяне.

> Женщины, в общем, аркады не
> посещали. Процент игроков-
> женщин в любой японской
> аркаде в 1979-м оценивался
> меньше 5%. Среда — громкая,
> прокуренная, мужская, полная
> подростков постарше,
> хвастающихся очками в
> стрелялках.

> Моя сестра, которой тогда
> было 14, однажды спросила:
> «Аники, можешь сделать игру,
> где не надо никого УБИВАТЬ?
> Мне не нравится стрелять.»
> Я сказал, что подумаю.

> ── ЧАСТЬ 2: ПИЦЦА ──

> Я ужинал с парой коллег из
> Namco в токийском идзакая.
> Мы заказали пиццу. Я взял
> первый кусок. Глядя сверху
> на сковородку с одним
> недостающим куском, я
> увидел форму — круг с
> вырезанным клином. Клин —
> открытый рот. Круг —
> голова.

> Я зарисовал силуэт на
> салфетке. Добавил два глаза.
> Добавил маленькие ножки
> снизу. За двадцать минут у
> меня был силуэт того, что
> станет PAC-MAN.

> На следующее утро я начал
> наброски дизайна в офисе
> Namco. У меня было три цели
> из просьбы сестры:
>   1. Игрок должен ЕСТЬ, а
>      не стрелять.
>   2. Сеттинг должен быть
>      ИГРИВЫМ лабиринтом, а
>      не полем боя.
>   3. Враги должны быть
>      МИЛЫМИ, а не страшными.
>      Побеждать их должно
>      ощущаться как ловить,
>      а не уничтожать.

> Лабиринт — просто. Еда —
> просто (наступил на точку —
> съел её). Милые враги
> заняли больше времени. Я
> спроектировал четырёх
> ПРИЗРАКОВ — круглых, в
> простыньку, большеглазых.
> Каждый — другого яркого
> цвета. Каждый — с слегка
> другим характером.

> ── ЧАСТЬ 3: ИИ ПРИЗРАКОВ ──

> Это часть, которой я больше
> всего горжусь, оглядываясь.

> У четырёх призраков
> ОТЛИЧНЫЕ ИИ-характеры,
> прошитые в коде. Один —
> прямой — он целится в твою
> текущую позицию. Один —
> засадник — он целится
> вперёд тебя, туда, куда ты
> идёшь, не туда, где ты.
> Один — непредсказуемый —
> его цель вычисляется по
> двум опорным точкам и в
> итоге дрейфует странными
> узорами. Один — стеснительный
> — он наступает, пока не
> приблизится, потом
> отступает. Никогда тебя
> толком не ловит, но
> донимает всё время.

> Четыре характера вместе
> создают ощущение, что
> призраки — ЖИВЫЕ СУЩЕСТВА
> с намерениями, а не
> случайные ИИ. Игроки
> проводили часы, изучая их
> привычки. К середине 1980-х
> серьёзные игроки назвали
> точные узоры поведения
> призраков и могли описывать
> их, как персонажей в
> романе.

> Игроки также обнаружили,
> что если ИИ-логика работает
> детерминированно — а так и
> было — то существуют
> ИДЕАЛЬНЫЕ УЗОРЫ. Точная
> последовательность движений
> джойстика, которая очищает
> каждую точку на этапе, не
> попадая в ловушку.
> Брошюры-прохождения
> распространяли эти узоры
> в начале 1980-х.

> ── ЧАСТЬ 4: СМЕНА ИМЕНИ ──

> Оригинальное японское
> название — транслитерация
> японской ономатопеи для
> «чавк-чавк». Латинизировано
> для англоязычных рынков с
> буквой П. Американский
> дистрибьютор немедленно
> возразил: П на кабинете
> можно было легко
> испортить маркерным
> штрихом, превратив в Ф, и
> они не хотели, чтобы их
> семейный аркадный тайтл
> 1980-го вандализировался в
> сексуально-намекающую
> отсылку к Beatles. Имя
> сменили для рынка США — на
> то, которое ты знаешь
> сегодня.

> ── ЧАСТЬ 5: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — MUNCHER. Герой —
> КВАДРАТНЫЙ
> чавкалка-бот, не жёлтый
> кружок. Наш лабиринт-
> макет наш собственный, не
> оригинальный. «Жути» — наш
> собственный дизайн, не
> четыре призрака. Энергетические
> точки и механика
> туннеля-обёртки сохранены,
> потому что это
> public-domain идеи.

> Сама механика
> лабиринт-погони —
> идёшь-ешь-точки-тебя-гонят-
> враги-power-точка-ешь-врагов-
> снова-тебя-гонят — это
> центральный жанр, который я
> изобрёл в 1979-м, глядя
> вниз на кусок пиццы.

> D-pad — идти. Ешь точки.
> Ешь энергетические точки,
> когда жути опасны — они
> становятся напуганными на
> семь секунд, во время
> которых ты можешь их тоже
> съесть. Очисти каждую
> точку, чтобы перейти на
> следующий уровень.

> Моей сестре Кёко сейчас
> за шестьдесят. Она наконец
> зашла в аркаду в 1980-м,
> после того как я закончил
> мою игру. Играла часами.
> Очень хорошо. Она до сих
> пор мой лучший
> playtester.

> Кёко, если это читаешь —
> аригато. Весь жанр — твой.

> ── СНОСКА У КОСТРА ──

> Аркадная игра 1980-го и её
> характеры призраков
> принадлежат их
> соответствующим
> правообладателям (Namco,
> теперь Bandai Namco
> Entertainment). Наша дань
> называется MUNCHER и
> использует совершенно
> другой состав, арт и
> макет лабиринта. Общая
> механика — лабиринт-погоня
> с точками,
> power-точками и
> съедобными врагами во
> время уязвимости — была
> копирована бесчисленное
> количество раз и больше
> не охраняется ни одной
> современной системой
> game-design. Иватани-сенсей
> жив, до сих пор
> преподаёт; если ты это
> читаешь, сенсей — шляпа
> долой, ещё раз. Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val MUNCHER_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val MUNCHER_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickMuncherIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> MUNCHER_INTRO_RU
    else -> MUNCHER_INTRO_EN
}

val MUNCHER_INTRO_TEXT: String = MUNCHER_INTRO_EN
