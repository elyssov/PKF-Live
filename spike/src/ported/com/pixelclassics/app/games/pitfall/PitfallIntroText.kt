package com.pixelclassics.app.games.pitfall

/**
 * Long-form campfire intro for Jungle Run — narrated by Pitfall Harry's
 * cousin's nephew, a perpetually-running adventurer who never quite made
 * it out of the Atari-2600-era jungle and now exists eternally between
 * vine-swings.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
████████████████████████████████████████
█       J U N G L E   R U N            █
█  ATARI-EPOCH SUMMARY · BAND 03 · MD  █
████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> The vines are still here.
> I have been swinging across them for
> 44 years. I think I may have made
> some progress. Last week I caught a
> glimpse of what might be the edge of
> the jungle. It might also have been
> another vine.

> I want to tell you a story. Not
> about me — about the era I was
> born into. The 2600 epoch. The
> ten years where the entire video
> game industry tried, with 128 bytes
> of RAM and a 6502 processor running
> at 1.19 MHz, to give children
> jungles, dragons, spaceships, and
> wars.

> Pour out a tin can of imaginary
> Kool-Aid. Settle in.

> ── PART 1: THE 2600 ──

> The Atari 2600 (originally the
> Atari VCS, "Video Computer System")
> launched in October 1977 at ${'$'}199 USD,
> which was real money. It had a CPU
> that could run about 1.19 million
> instructions per second. It had 128
> bytes of RAM (not kilobytes; bytes —
> roughly the length of this sentence
> you are reading). It had no
> framebuffer. The CPU had to draw each
> horizontal line of the TV image
> directly, in real time, while the
> electron beam was already moving.

> If you, the game programmer, did not
> have the next scanline's instructions
> ready in time, the TV would show
> garbage and the game would crash.
> So the programmer wrote the entire
> game as a carefully-timed real-time
> dance with the cathode ray. Every
> frame, every line. By hand.

> This is what the games of the 2600
> era are: hand-choreographed dances
> between a programmer and an electron
> beam. The dances had to fit on a
> 4-kilobyte cartridge. Sometimes
> 2-kilobyte. Occasionally 8-kilobyte
> if you were a luxury studio.

> ── PART 2: THE JUNGLE GAMES ──

> Multiple studios tried to give the
> player a jungle in those constraints.
> Some highlights of what they
> achieved:

>   PITFALL! (Activision, David Crane,
>   1982) — Harry has 20 minutes to
>   find 32 treasures across 256
>   screens. The game contains 256
>   distinct screens encoded in
>   1024 bytes of cartridge. Crane
>   used a single byte of polynomial
>   randomness as a "screen seed",
>   so every screen could be
>   regenerated deterministically
>   on demand. The result feels like
>   a vast jungle. It is actually 256
>   bytes of LCG output.

>   JUNGLE HUNT (Atari, 1982) — Tarzan-
>   adjacent, four screens: vines,
>   river with crocodiles, falling
>   rocks, cannibal-encampment-with-
>   royalty-to-rescue (1980s
>   sensibilities). Light story, light
>   logic. The vine-swinging is what
>   sells it.

>   PITFALL II: LOST CAVERNS (Activision,
>   David Crane, 1984) — sequel with
>   underwater caves, condors,
>   electric eels, restoration of
>   continuous music (genuine
>   technical breakthrough — Crane
>   built a custom audio chip into
>   the cartridge itself, the
>   "DPC", because the 2600's
>   native audio could not loop
>   melodies). 320 chambers of
>   jungle and cavern. Continuous
>   music. From a 1984 cartridge.

>   H.E.R.O. (Activision, John van
>   Ryzin, 1984) — different vibe —
>   you fly a backpack-propeller
>   into vertical caves to rescue
>   miners. Microhelicopter. Bombs.
>   Lasers. Lava. One of the
>   tightest action games ever
>   built for the 2600.

>   MONTEZUMA'S REVENGE (Parker
>   Brothers, 1984, originally Robert
>   Jaeger Atari-800) — Mayan
>   pyramid with 100 rooms, skulls,
>   lava, ropes, locked doors,
>   keys, treasure. Brutally hard.
>   Beloved.

> All of those came out in the same
> three-year window — 1982 to 1984 —
> on a ${'$'}199 black box with 128 bytes
> of RAM. By 1985 the industry would
> crash and these would all be sitting
> in ${'$'}5 bargain bins at Toys'R'Us.

> ── PART 3: WHAT WE TOOK ──

> Our JUNGLE RUN is not a port of any
> single one of these games. It is a
> tribute to all five at once. The
> vine-swinging from Jungle Hunt. The
> pits and crocodiles from Pitfall!.
> The procedurally-generated terrain
> from Pitfall! II. The desperate
> jumping rhythm of H.E.R.O. The
> sense, throughout, that there is
> something LARGER going on but you
> can never quite see the whole map.

> Our hero runs forever to the right.
> The jungle scrolls past at the speed
> of memory. Pits open in front of
> them. Logs roll. Crocodiles open
> and close their mouths. Vines hang
> from a canopy you can never quite
> see. Snakes coil and rear. Gold
> bars glint. Scorpions patrol.

> Press GREEN to jump (over a pit, a
> snake, a log, a croc).
> Press RED to fire your revolver.
> You have six shots before reloading
> at an ammo barrel. The revolver was
> never standard for the 2600-era —
> Pitfall! Harry never carried one —
> but we added it because firing a
> revolver in a jungle feels
> tactically pleasing, and the
> mechanic is older than the 2600
> (it goes back to Air-Sea Battle,
> 1977).

> ── PART 4: WHY THIS MATTERS ──

> You may think a 2600-era jungle is
> simple. It is not. It is squeezed.
> Every pixel is hand-placed. Every
> behaviour is hand-coded against the
> electron beam. The total RAM the
> game uses to track YOUR POSITION,
> the obstacles in the current
> screen, the world's pseudo-random
> seed, the loot you've collected,
> your remaining time, your score,
> and your health — combined — is
> less RAM than the registers in
> your phone's BLUETOOTH chip.

> The era was constrained. The
> constraints forced creativity.
> The creativity gave us a vocabulary
> for "jungle game" that has, somehow,
> never been improved on. Modern
> graphics-juggernaut platformers
> still echo the rhythm of pit-jump-
> log-jump-vine-grab-snake-shoot from
> 1982.

> ── PART 5: THE RUN ──

> Below is the run. The d-pad will
> not appear because this is a
> TWO-BUTTON game — green and red,
> as the title screen shows. The hero
> runs automatically. Your only
> decisions are: jump when, shoot
> what. The longer you survive, the
> faster everything happens.

> There is no end. There is only your
> distance. Better than last time?
> That's the whole loop. Welcome to
> the run.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> PITFALL is a trademark of Activision.
> JUNGLE HUNT, PITFALL II and H.E.R.O.
> trademarks belong to their respective
> rights-holders. MONTEZUMA'S REVENGE
> is Parker Brothers. We claim NO
> affiliation. Our tribute is called
> JUNGLE RUN to keep the love letter
> distinct from any of the originals.
> Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Лианы всё ещё здесь.
> Я качаюсь по ним уже 44 года.
> Думаю, я даже немного продвинулся.
> На прошлой неделе мне показалось,
> что я увидел край джунглей. Или
> это была ещё одна лиана.

> Я расскажу тебе историю. Не
> про себя — про эпоху, в
> которую я родился. Эпоху 2600.
> Десять лет, когда вся
> видеоигровая индустрия
> пыталась, имея 128 байт RAM и
> процессор 6502 на 1.19 МГц,
> подарить детям джунгли,
> драконов, космолёты и войны.

> Налей себе воображаемый
> Kool-Aid из жестяной банки.
> Устраивайся.

> ── ЧАСТЬ 1: 2600 ──

> Atari 2600 (изначально Atari
> VCS, «Video Computer System»)
> вышла в октябре 1977-го по
> цене 199 долларов — настоящие
> деньги. У неё был процессор,
> исполнявший около 1.19
> миллиона инструкций в
> секунду. У неё было 128 байт
> RAM (не килобайт; байт —
> примерно длина этого
> предложения, которое ты
> читаешь). Не было
> фреймбуфера. Процессор
> должен был рисовать каждую
> горизонтальную линию ТВ-кадра
> напрямую, в реальном времени,
> пока электронный луч уже
> двигался.

> Если ты, программист игры, не
> успевал подготовить
> инструкции для следующей
> строки — телевизор показывал
> мусор, а игра падала. Так
> что программист писал всю
> игру как тщательно
> рассчитанный по времени
> реальный танец с
> катодно-лучевой трубкой.
> Каждый кадр, каждая линия.
> Руками.

> Вот что такое игры эпохи
> 2600: ручная хореография
> танцев программиста с
> электронным лучом. Танцы
> должны были помещаться на
> 4-килобайтный картридж.
> Иногда 2-килобайтный. Изредка
> 8-килобайтный, если ты был
> люксовой студией.

> ── ЧАСТЬ 2: ИГРЫ-ДЖУНГЛИ ──

> Несколько студий пытались
> дать игроку джунгли в этих
> ограничениях. Несколько
> хайлайтов того, что они
> добились:

>   PITFALL! (Activision, Дэвид
>   Крейн, 1982) — у Гарри 20
>   минут найти 32 сокровища на
>   256 экранах. Игра содержит
>   256 различных экранов,
>   закодированных в 1024
>   байтах картриджа. Крейн
>   использовал один байт
>   полиномиальной случайности
>   как «зерно экрана», так что
>   каждый экран мог быть
>   детерминированно
>   восстановлен по
>   требованию. На ощущение —
>   огромные джунгли. По
>   факту — 256 байт вывода
>   LCG.

>   JUNGLE HUNT (Atari, 1982) —
>   Тарзан-подобное, четыре
>   экрана: лианы, река с
>   крокодилами, падающие
>   камни, лагерь каннибалов
>   с-королевской-особой-для-
>   спасения (восприятие
>   1980-х). Лёгкий сюжет,
>   лёгкая логика. Раскачка на
>   лианах — вот что продаёт.

>   PITFALL II: LOST CAVERNS
>   (Activision, Дэвид Крейн,
>   1984) — сиквел с подводными
>   пещерами, кондорами,
>   электрическими угрями,
>   восстановление непрерывной
>   музыки (настоящий
>   технический прорыв — Крейн
>   встроил кастомный
>   аудиочип в сам картридж,
>   «DPC», потому что родной
>   звук 2600 не мог зацикливать
>   мелодии). 320 комнат
>   джунглей и пещер.
>   Непрерывная музыка. С
>   картриджа 1984-го года.

>   H.E.R.O. (Activision, Джон
>   ван Райзин, 1984) — другой
>   вайб — летаешь с
>   рюкзаком-пропеллером в
>   вертикальных пещерах,
>   спасая шахтёров.
>   Микровертолёт. Бомбы.
>   Лазеры. Лава. Одна из самых
>   плотных action-игр,
>   когда-либо построенных
>   для 2600.

>   MONTEZUMA'S REVENGE (Parker
>   Brothers, 1984, изначально
>   Роберт Йегер Atari-800) —
>   пирамида майя со 100
>   комнатами, черепами, лавой,
>   верёвками, запертыми
>   дверьми, ключами,
>   сокровищем. Зверски сложно.
>   Любимое.

> Всё это вышло в одно и то же
> трёхлетнее окно — 1982-1984 —
> на 199-долларовой чёрной
> коробке со 128 байтами RAM.
> К 1985-му индустрия рухнет,
> и всё это будет лежать в
> 5-долларовых корзинках
> уценки у Toys'R'Us.

> ── ЧАСТЬ 3: ЧТО МЫ ВЗЯЛИ ──

> Наш JUNGLE RUN — не порт
> ни одной из этих игр. Это
> дань всем пяти сразу.
> Раскачка на лианах — из
> Jungle Hunt. Ямы и крокодилы
> — из Pitfall!. Процедурно
> сгенерированный рельеф — из
> Pitfall! II. Отчаянный
> прыгательный ритм — из
> H.E.R.O. Ощущение, что
> происходит что-то БОЛЬШЕЕ,
> но всю карту ты так и не
> увидишь.

> Наш герой бежит вечно
> вправо. Джунгли прокручиваются
> со скоростью памяти. Перед
> ним открываются ямы. Катятся
> брёвна. Крокодилы открывают
> и закрывают пасти. С полога,
> которого ты так и не увидишь,
> свисают лианы. Змеи свиваются
> и поднимаются. Золотые
> слитки блестят. Скорпионы
> патрулируют.

> Жми ЗЕЛЁНЫЙ, чтобы прыгнуть
> (через яму, змею, бревно,
> крокодила).
> Жми КРАСНЫЙ, чтобы выстрелить
> из револьвера. У тебя шесть
> выстрелов до перезарядки у
> бочки с патронами. Револьвер
> не был стандартом для эпохи
> 2600 — Гарри из Pitfall! его
> никогда не носил — но мы
> добавили, потому что
> стрельба из револьвера в
> джунглях ощущается тактически
> приятно, а механика старше
> 2600 (восходит к Air-Sea
> Battle, 1977).

> ── ЧАСТЬ 4: ПОЧЕМУ ЭТО ВАЖНО ──

> Ты можешь подумать, что
> джунгли эпохи 2600 — это
> просто. Нет. Это сжато.
> Каждый пиксель размещён
> руками. Каждое поведение
> закодировано руками против
> электронного луча. Вся RAM,
> которой игра отслеживает
> ТВОЮ ПОЗИЦИЮ, препятствия на
> текущем экране, псевдослучайное
> зерно мира, лут, который ты
> собрал, оставшееся время,
> очки и здоровье — всё вместе
> — меньше регистров в
> Bluetooth-чипе твоего
> телефона.

> Эпоха была ограниченной.
> Ограничения вынуждали
> творчество. Творчество
> подарило нам словарь
> «джунгельной игры», который,
> почему-то, никогда не был
> улучшен. Современные
> графические-джаггернаут
> платформеры всё ещё
> отзываются ритмом
> прыжок-через-яму-прыжок-
> через-бревно-схвати-лиану-
> стрельни-в-змею из 1982-го.

> ── ЧАСТЬ 5: ЗАБЕГ ──

> Ниже — забег. D-pad'а не
> будет, потому что это
> ДВУХКНОПОЧНАЯ игра — зелёная
> и красная, как показывает
> заставка. Герой бежит
> автоматически. Твои
> единственные решения:
> когда прыгнуть, во что
> выстрелить. Чем дольше ты
> выживаешь, тем быстрее всё
> происходит.

> Конца нет. Есть только твоя
> дистанция. Лучше, чем в
> прошлый раз? Это весь цикл.
> Добро пожаловать в забег.

> ── СНОСКА У КОСТРА ──

> PITFALL — торговая марка
> Activision. JUNGLE HUNT,
> PITFALL II и H.E.R.O. —
> торговые марки их
> соответствующих
> правообладателей.
> MONTEZUMA'S REVENGE — Parker
> Brothers. Мы НЕ заявляем
> никакой связи. Наша дань
> называется JUNGLE RUN, чтобы
> наше любовное письмо
> отличалось от любого из
> оригиналов. Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val PITFALL_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val PITFALL_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickPitfallIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> PITFALL_INTRO_RU
    else -> PITFALL_INTRO_EN
}

val PITFALL_INTRO_TEXT: String = PITFALL_INTRO_EN
