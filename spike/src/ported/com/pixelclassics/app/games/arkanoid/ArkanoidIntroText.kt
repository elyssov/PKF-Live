package com.pixelclassics.app.games.arkanoid

/**
 * Long-form campfire intro for Brick Buster (Arkanoid). Voice: Taito's
 * narrative-flavoured 1986 spaceship VAUS, escaped from a destroyed
 * mothership, drifting through energy fields, still chasing the next
 * brick. The lore Taito put on the cabinet was florid sci-fi; we lean
 * into it.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
═══════════════════════════════════════
   VAUS-7  ::  SHIP'S LOG  ::  Y4 D118
   SECTOR THETA  ::  TAITO PROTOCOL
═══════════════════════════════════════
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> The mothership ARKANOID is gone.

> A dimensional warp engulfed her in
> Sector Theta and she did not come
> out. I was launched seconds before
> she vanished, on Captain Doh's
> standing order: "If anything ever
> happens, scout. Find a way through.
> Come back if you can."

> I am VAUS-7. A small craft. Two
> short engine nacelles, one wide
> deflector plate, no offensive
> armament except whatever I can
> bounce.

> I have been drifting for three years.

> ── PART 1: WHY I'M HERE ──

> The energy fields in this sector
> form themselves into rectangular
> lattices. Walls of bricks, suspended
> in vacuum, each one humming with a
> different harmonic resonance. They
> block all conventional propulsion.
> You cannot push through them. You
> cannot fly around them — they
> extend indefinitely in every
> direction. You can only break them.

> And the only way to break them, the
> Captain figured out before we lost
> her, is with a small mass moving at
> high velocity. A ball of compressed
> plasma, bounced off your own
> deflector plate, into the brick.
> Each brick shatters when struck.
> Each shattered brick releases its
> resonance and weakens the lattice
> around it.

> Clear the lattice. Advance to the
> next field. Eventually — reach the
> centre of the dimensional warp.
> Find what swallowed the mothership.
> Bring it home, or die trying.

> ── PART 2: WHAT TAITO MADE OF US ──

> The history below is technically not
> mine. It is what was written about
> me by the cartographers at TAITO,
> a small electronics company in
> Tokyo, in the year 1986 by their
> calendar. They built me as a coin-
> operated cabinet for arcades. I
> understand this is normal in your
> dimension.

> TAITO had been making arcade games
> since 1973 and had become Japan's
> third or fourth biggest arcade
> manufacturer. By 1986 the company
> was looking for a hit. The genre
> they chose was "block breaker",
> pioneered by Atari's BREAKOUT in
> 1976. BREAKOUT was monochrome,
> silent, joyless. TAITO wanted more.

> They hired a designer named Akira
> Fujita and an artist named Hiroshi
> Tsujino. Fujita designed me — a
> spacecraft, not just a paddle. A
> ship with a story. Tsujino drew
> the bricks as glowing energy
> blocks, the background as deep-
> space starfield, the power-ups as
> Greek letters falling slowly down
> the playfield like prophecies.

> The Greek letters mean things.

> ── PART 3: THE POWER-UPS ──

> When you shatter a SILVER brick — a
> brick that takes two hits, glowing
> with extra hardness — sometimes the
> brick releases a capsule. The
> capsule drifts down. Catch it on
> your deflector. The capsule's
> letter tells you what you have:

>   L  (LASER)   — twin guns mount
>      on my deflector, I can shoot
>      bricks directly.
>   E  (ENLARGE) — my deflector
>      doubles in width.
>   C  (CATCH)   — the next time the
>      ball strikes me, it adheres.
>      Hold the ball. Fire when
>      ready. Repeat.
>   S  (SLOW)    — the ball decelerates.
>      For human reaction times.
>   B  (BREAK)   — exit portal opens
>      to the side, escape this
>      lattice immediately, skip the
>      remaining bricks.
>   D  (DISRUPT) — the ball splits.
>      Three balls in play at once.
>      Triple destruction, triple
>      chaos.
>   P  (PLAYER)  — bonus life.

> Some of the capsules look identical
> until you catch them. Roll the dice.

> ── PART 4: WHAT WAITS AT THE CENTRE ──

> 33 lattice fields stand between me
> and the dimensional warp. Each one
> harder than the last. Indestructible
> GOLD bricks appear from field 7
> onward — bricks that cannot be
> destroyed, only deflected past.
> By field 12 the bricks start
> moving. By field 20, alien creatures
> emerge from rifts in the lattice
> and try to deflect my plasma ball
> off course; they cannot kill me,
> but they will disrupt my aim.

> At field 33 there is no lattice.
> There is only DOH.

> DOH is — was — going to be — an
> alien intelligence, a giant stone
> head with three eyes and one mouth,
> floating in vacuum. DOH absorbs my
> plasma ball into himself. DOH shoots
> energy beams at me. DOH speaks
> Japanese with a heavy artificial
> reverb that translates poorly. The
> name DOH is, in some interpretations,
> short for "Dominator of Hour" or
> "Deflector Of Humans" — Taito never
> definitively said. In other
> interpretations, the name is just
> the Japanese transliteration of
> "DOH" (どぅ) and means nothing in
> particular. The mystery is the
> point.

> If I defeat DOH, I see what the
> warp swallowed. If I lose to DOH,
> the cabinet plays a sting and asks
> for another quarter.

> ── PART 5: WHAT THE PLAYER DOES ──

> The player — that is you, reading
> this in the year 2026, on a flat
> piece of glass that fits in your
> pocket — does not see any of the
> above narrative when they play me.
> They see bricks. They see a paddle.
> They see a ball. They see capsules
> falling.

> But somewhere in their hands, in
> the rhythm of pinning the ball
> against the top of the screen and
> watching three different colours
> of bricks chain-shatter, a memory
> is being assembled. A memory of
> being TAITO's pilot. A memory of
> a Captain you never met saying:
> "Come back if you can."

> You did not need to read this
> file to play me. But now that you
> have, the bricks will look slightly
> heavier when they fall. Each
> chain-clear will feel like a small
> victory in a long campaign. That
> is what TAITO drew for me, and
> what I am, in 2026, still trying
> to deliver.

> Bring back the mothership.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> ARKANOID is a trademark of TAITO
> Corporation. We call our tribute
> BRICK BUSTER. The mechanic of
> breaking bricks with a paddle-
> bounced ball was invented by Atari
> for BREAKOUT in 1976 and is part
> of the public folklore of game
> design. The lore above — Captain
> Doh, dimensional warp, the Greek-
> letter capsules — is faithful to
> the 1986 Taito narrative, told
> with affection. If a rights-holder
> has concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Материнский корабль ARKANOID
> утрачен.

> Мерное искажение поглотило его
> в секторе Тета, и он не вышел.
> Меня выпустили за секунды до
> того, как он исчез, по
> постоянному приказу капитана
> Доу: «Если что-нибудь случится
> — иди в разведку. Найди проход.
> Возвращайся, если сможешь.»

> Я — VAUS-7. Малый аппарат. Две
> короткие моторные гондолы, один
> широкий отражательный щит,
> никакого наступательного
> вооружения, кроме того, что я
> могу отскочить.

> Я дрейфую уже три года.

> ── ЧАСТЬ 1: ЗАЧЕМ Я ЗДЕСЬ ──

> Энергетические поля в этом
> секторе складываются в
> прямоугольные решётки. Стены
> из кирпичей, висящие в
> вакууме, каждая гудит на
> своей гармонической частоте.
> Они блокируют любую обычную
> тягу. Сквозь них нельзя
> протолкнуться. Их нельзя
> облететь — они тянутся
> бесконечно во всех
> направлениях. Их можно только
> ломать.

> А единственный способ их
> сломать, как капитан выяснил
> до того, как мы её потеряли —
> это малая масса, движущаяся
> на высокой скорости. Шар
> сжатой плазмы, отскочивший от
> твоего собственного
> отражателя, в кирпич. Каждый
> кирпич разваливается при
> ударе. Каждый разваленный
> кирпич отпускает свой
> резонанс и ослабляет решётку
> вокруг.

> Чисти решётку. Двигайся к
> следующему полю. В итоге —
> добраться до центра
> мирного искажения. Найти то,
> что проглотило материнский
> корабль. Привести его домой
> или погибнуть пытаясь.

> ── ЧАСТЬ 2: КЕМ TAITO НАС СДЕЛАЛИ ──

> История ниже — технически не
> моя. Это то, что обо мне
> написали картографы из TAITO,
> маленькой электронной
> компании в Токио, в год
> 1986-й по их календарю. Они
> построили меня как монетный
> аркадный кабинет. Я понимаю —
> в вашей мерности это
> нормально.

> TAITO делали аркадные игры с
> 1973-го и стали третьим или
> четвёртым крупнейшим аркадным
> производителем Японии. К
> 1986-му компания искала хит.
> Жанр, который они выбрали —
> «block breaker», пионером
> которого был BREAKOUT от
> Atari в 1976-м. BREAKOUT был
> монохромным, тихим,
> безрадостным. TAITO хотели
> больше.

> Они наняли дизайнера по имени
> Акира Фудзита и художника по
> имени Хироси Цудзино. Фудзита
> спроектировал меня —
> космический корабль, а не
> просто ракетку. Корабль с
> историей. Цудзино нарисовал
> кирпичи как светящиеся
> энергетические блоки, фон —
> как звёздное поле дальнего
> космоса, бонусы — как
> греческие буквы, медленно
> падающие по полю, как
> пророчества.

> Греческие буквы что-то
> значат.

> ── ЧАСТЬ 3: БОНУСЫ ──

> Когда ты разбиваешь
> СЕРЕБРЯНЫЙ кирпич — тот,
> который выдерживает два удара
> и светится повышенной
> твёрдостью — иногда кирпич
> выпускает капсулу. Капсула
> дрейфует вниз. Поймай её на
> отражатель. Буква капсулы
> говорит, что у тебя:

>   L  (LASER)   — двойные пушки
>      встают на моём отражателе,
>      я могу стрелять по
>      кирпичам напрямую.
>   E  (ENLARGE) — мой отражатель
>      удваивается в ширине.
>   C  (CATCH)   — следующий раз,
>      когда мяч в меня попадает,
>      он прилипает. Держи мяч.
>      Стреляй когда готов.
>      Повторить.
>   S  (SLOW)    — мяч замедляется.
>      Для человеческого
>      времени реакции.
>   B  (BREAK)   — портал-выход
>      открывается сбоку,
>      выпрыгни из решётки
>      немедленно, пропусти
>      оставшиеся кирпичи.
>   D  (DISRUPT) — мяч
>      раздваивается. Три мяча в
>      игре одновременно. Тройное
>      разрушение, тройной хаос.
>   P  (PLAYER)  — бонусная
>      жизнь.

> Некоторые капсулы выглядят
> одинаково, пока не поймаешь.
> Бросай кости.

> ── ЧАСТЬ 4: ЧТО ЖДЁТ В ЦЕНТРЕ ──

> 33 решётчатых поля стоят
> между мной и измерительным
> искажением. Каждое сложнее
> предыдущего. Нерушимые
> ЗОЛОТЫЕ кирпичи появляются с
> поля 7 — кирпичи, которые
> нельзя уничтожить, только
> обойти отскоком. К полю 12
> кирпичи начинают двигаться.
> К полю 20 пришельцы появляются
> из разломов в решётке и
> пытаются сбить мой плазменный
> мяч с курса; убить меня они не
> могут, но прицел собьют.

> На поле 33 нет решётки. Есть
> только DOH.

> DOH — был — собирался быть —
> чужой интеллект, гигантская
> каменная голова с тремя
> глазами и одним ртом,
> плавающая в вакууме. DOH
> поглощает мой плазменный мяч
> в себя. DOH стреляет в меня
> энергетическими лучами. DOH
> говорит по-японски с тяжёлой
> искусственной реверберацией,
> плохо переводимой. Имя DOH в
> некоторых интерпретациях —
> сокращение от «Dominator of
> Hour» или «Deflector Of
> Humans» — Taito никогда
> определённо не сказали. В
> других интерпретациях имя —
> просто японская
> транслитерация «DOH» (どぅ),
> ничего конкретного не
> значащая. Загадка — в этом
> смысл.

> Если я побеждаю DOH, я вижу,
> что проглотило искажение.
> Если я проигрываю DOH,
> кабинет проигрывает удар и
> просит ещё четвертак.

> ── ЧАСТЬ 5: ЧТО ДЕЛАЕТ ИГРОК ──

> Игрок — это ты, читающий это
> в 2026-м, на плоском кусочке
> стекла, помещающемся в
> карман — ничего из этого
> рассказа не видит, когда
> играет. Он видит кирпичи. Он
> видит ракетку. Он видит мяч.
> Он видит падающие капсулы.

> Но где-то в его руках, в
> ритме пришпиливания мяча к
> верху экрана и наблюдения,
> как три разных цвета кирпичей
> цепочкой разваливаются,
> собирается воспоминание.
> Воспоминание о том, чтобы быть
> пилотом TAITO. Воспоминание о
> капитане, которого ты никогда
> не встречал, говорящем:
> «Возвращайся, если сможешь.»

> Тебе не нужно было читать этот
> файл, чтобы играть в меня. Но
> теперь, когда ты прочитал,
> кирпичи покажутся чуть тяжелее
> при падении. Каждая цепочка-
> очистка ощутится как маленькая
> победа в длинной кампании.
> Это то, что TAITO нарисовали
> для меня, и то, что я в
> 2026-м всё ещё пытаюсь
> доставить.

> Верни материнский корабль.

> ── СНОСКА У КОСТРА ──

> ARKANOID — торговая марка
> TAITO Corporation. Мы зовём
> нашу дань BRICK BUSTER.
> Механика разбивания кирпичей
> ракеткой-отражённым мячом
> была изобретена Atari для
> BREAKOUT в 1976-м и стала
> частью общего фольклора
> геймдизайна. Лор выше —
> капитан Доу, измерительное
> искажение, капсулы с
> греческими буквами — верен
> нарративу Taito 1986-го,
> рассказан с любовью. Если у
> правообладателя есть
> вопросы: elyssov@gmail.com.
""".trimIndent()

val ARKANOID_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val ARKANOID_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickArkanoidIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> ARKANOID_INTRO_RU
    else -> ARKANOID_INTRO_EN
}

/** Back-compat alias. */
val ARKANOID_INTRO_TEXT: String = ARKANOID_INTRO_EN
