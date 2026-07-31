package com.pixelclassics.app.games.starwing

/**
 * Long-form campfire intro for STAR SWARM (Galaga). Narrated by a
 * captured pilot whose ship was caught in the tractor-beam capture
 * sequence.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
██████████████████████████████████████████
█  NAMCO · GALAGA · 1981 · STAGE FORMS  █
█  TRANSMISSION RECEIVED · STATIC HEAVY █
█  SOURCE: CAPTURED FIGHTER, HIVE INT.  █
██████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> ░░░ STATIC ░░░

> CAN YOU HEAR ME? I am broadcasting
> from inside the hive. The signal is
> degrading. The mother ship's
> tractor-beam captured my fighter
> three minutes ago. I am alive. My
> ship is intact. Whatever the
> insects are using to absorb our
> tech, it doesn't kill us —
> it stores us.

> If you can hear me — listen
> closely. I have things to tell
> you about how the swarm operates.

> ── PART 1: THE NAMCO STORY ──

> Namco Limited of Tokyo, Japan,
> 1979. The previous year, the
> studio had shipped GALAXIAN —
> their answer to Taito's SPACE
> INVADERS. Galaxian was a hit
> — 25,000 arcade cabinets sold
> — but Namco felt they hadn't
> taken the formation-flight
> mechanic far enough.

> A young engineer named Shigeru
> Yokoyama, working alongside
> art director Yuriko Keino, was
> assigned the project of making
> Galaxian's sequel. They were
> given six months and a single
> design constraint: the new
> game must improve on Galaxian
> in some MEMORABLE way, not
> just be "more Galaxian".

> Yokoyama's pitch was: a CAPTURE
> mechanic. The enemy mother
> ships, instead of always
> killing you on contact, could
> CAPTURE your ship — drag it
> back to the hive — and you
> would have a chance to RESCUE
> it. Rescued ships would JOIN
> your fighter as a DOUBLE-WING
> wingmate, doubling your
> firepower. The risk-vs-reward
> mechanic that capture creates
> is what separated GALAGA from
> every formation-shooter
> before it.

> Yokoyama's prototype shipped in
> arcades in September 1981. The
> name GALAGA — from the
> Japanese onomatopoeia for the
> sound of a swarm of insects
> en masse — was chosen for its
> evocative quality.

> ── PART 2: THE WAVES ──

> The screen is vertical-scroll
> shoot-em-up. You pilot a small
> blue triangular fighter at
> the bottom of the screen.
> Enemies fly in from the top
> in elaborate, choreographed
> swooping patterns. Each wave
> has 8-12 enemies. After
> swooping, the enemies settle
> into a HIVE FORMATION at the
> top of the screen — three or
> four rows of stationary
> targets — and from that
> formation they take turns
> DIVING at you.

> Three enemy types:
>   BEES (small, yellow) — fast
>     and aggressive. Dive in
>     pairs. Low point value
>     (50-150).
>   BUTTERFLIES (red) — slower,
>     bigger, harder to predict.
>     Frequently launch downward
>     spiral patterns. Mid-tier
>     points (80-300).
>   MOTHER SHIPS (green/yellow,
>     larger) — the bosses.
>     They activate a TRACTOR
>     BEAM — a slowly-rotating
>     wave of green light from
>     their underside — and if
>     the beam touches your
>     fighter, you are CAPTURED.

> ── PART 3: THE DOUBLE-WING ──

> The DOUBLE-WING is the
> emotional core of GALAGA.

> When a mother captures your
> fighter, the screen briefly
> displays the captured ship
> being dragged backward by the
> green beam, then absorbed into
> the bottom of the mother ship.
> You die — but you are not
> EATEN. You are STORED. The
> mother carries you back up to
> the formation.

> If, in the next wave or two,
> you can shoot that specific
> mother ship before she dives
> again, she explodes — and
> your captured fighter falls
> back down to YOUR new
> fighter (the next life), and
> the two SNAP TOGETHER into a
> double-wide ship. You now have
> twice the fire rate. You have
> avenged your previous self.

> But the double-wing is also
> twice as wide as a single
> fighter. Twice as easy to
> hit. Worth it? Always, says
> the player who just
> retrieved a wingmate from
> the jaws of the swarm.
> Maybe not, says the player
> who immediately loses the
> double-wing to a stray
> bullet.

> ── PART 4: WHY THIS MATTERED ──

> The capture mechanic is, in
> retrospect, the first
> RISK-VS-REWARD ARC in arcade
> game design. Up to that point,
> arcade games were largely
> SURVIVAL games. Galaga
> introduced the idea that you
> might DELIBERATELY ACCEPT a
> bad outcome (losing a ship to
> capture) in exchange for a
> potential better outcome
> (rescuing the ship for a
> double-wing).

> Players in 1981 figured this
> out within their first few
> games. Skilled players began
> to DELIBERATELY ALLOW their
> ship to be captured on
> certain waves, betting they
> could rescue it in time.
> The whole arcade-game design
> tradition that we now call
> "permadeath with redemption
> mechanics" — Dark Souls, FTL,
> Slay the Spire — traces a
> lineage back, in part, to
> this 1981 capture mechanic.

> ── PART 5: WHAT WE BUILT ──

> Below is STAR SWARM. Our
> tribute. A triangular fighter.
> Wasps and drones in formation.
> Mother ships with tractor
> beams. Capture and rescue.
> Double-wing reward.

> Direction pad to strafe left-
> right. ● FIRE for twin lasers.

> Mother ships glow brighter
> just before they descend for
> a capture attempt — your
> warning to NOT be in the
> centre column when the beam
> activates.

> Kill the mother mid-capture
> to free your fighter and
> earn the double-wing. Eat
> her tractor beam to lose a
> life but possibly recover it
> next wave.

> Welcome to the hive.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> GALAGA is a trademark of
> Namco Limited (now Bandai
> Namco Entertainment). The
> capture mechanic, the
> double-wing reward, and the
> three-tier enemy formation
> are Namco's design. Our
> tribute uses a different art
> style and is called STAR
> SWARM. Concerns:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> ░░░ ПОМЕХА ░░░

> ТЫ МЕНЯ СЛЫШИШЬ? Передаю
> из глубины улья. Сигнал
> ухудшается. Трактор-луч
> материнского корабля
> захватил мой истребитель
> три минуты назад. Я жив.
> Корабль цел. Что бы
> насекомые ни использовали
> для поглощения нашей
> техники — оно нас не
> убивает. Оно нас хранит.

> Если слышишь — слушай
> внимательно. У меня есть
> что сказать о том, как
> работает рой.

> ── ЧАСТЬ 1: ИСТОРИЯ NAMCO ──

> Namco Limited в Токио,
> Япония, 1979-й. В
> предыдущем году студия
> выпустила GALAXIAN — их
> ответ Taito-SPACE
> INVADERS. Galaxian была
> хитом — продано 25 000
> аркадных кабинетов — но
> Namco чувствовали, что не
> довели механику
> формационного полёта до
> конца.

> Молодому инженеру по
> имени Сигеру Ёкояма,
> работающему рядом с
> арт-директором Юрико
> Кейно, поручили проект —
> сиквел Galaxian. Дали
> шесть месяцев и одно
> ограничение дизайна:
> новая игра должна
> улучшить Galaxian каким-то
> ЗАПОМИНАЮЩИМСЯ способом,
> а не быть просто «больше
> Galaxian».

> Питч Ёкоямы — механика
> ЗАХВАТА. Вражеские
> материнские корабли,
> вместо того чтобы всегда
> убивать при контакте,
> могут ЗАХВАТИТЬ твой
> корабль — утащить
> обратно в улей — и у тебя
> есть шанс СПАСТИ его.
> Спасённые корабли
> ПРИСОЕДИНЯЮТСЯ к
> истребителю как
> двухкрылый напарник,
> удваивая огневую мощь.
> Механика
> риск-vs-награда, которую
> создаёт захват, отделяет
> GALAGA от каждого
> формационного шутера до
> неё.

> Прототип Ёкоямы вышел в
> аркады в сентябре
> 1981-го. Название GALAGA
> — от японской ономатопеи
> для звука роя насекомых —
> выбрано за выразительное
> качество.

> ── ЧАСТЬ 2: ВОЛНЫ ──

> Экран — вертикальный
> скролл-шутер. Ты
> пилотируешь маленький
> синий треугольный
> истребитель внизу экрана.
> Враги влетают сверху по
> сложным, поставленным
> траекториям пикирования.
> В каждой волне 8-12
> врагов. После
> пикирования враги
> усаживаются в УЛЕЙНУЮ
> ФОРМАЦИЮ наверху — три
> или четыре ряда
> стационарных целей — и
> из этой формации по
> очереди ПИКИРУЮТ на
> тебя.

> Три типа врагов:
>   ПЧЁЛЫ (маленькие,
>     жёлтые) — быстрые и
>     агрессивные. Пикируют
>     парами. Низкая
>     ценность (50-150).
>   БАБОЧКИ (красные) —
>     медленнее, больше,
>     сложнее предсказать.
>     Часто запускают
>     спиральные узоры
>     вниз. Средняя
>     ценность (80-300).
>   МАТЕРИНСКИЕ КОРАБЛИ
>     (зелёно-жёлтые,
>     большие) — боссы.
>     Активируют ТРАКТОР-ЛУЧ
>     — медленно
>     вращающуюся волну
>     зелёного света снизу
>     — и если луч касается
>     твоего истребителя,
>     ты ЗАХВАЧЕН.

> ── ЧАСТЬ 3: ДВУХКРЫЛЫЙ ──

> ДВУХКРЫЛЫЙ — эмоциональное
> ядро GALAGA.

> Когда мать захватывает
> твой истребитель, экран
> кратко показывает, как
> захваченный корабль
> тянет назад зелёный луч,
> затем поглощается в дно
> материнского корабля. Ты
> умираешь — но тебя не
> СЪЕДАЮТ. Тебя СОХРАНЯЮТ.
> Мать уносит тебя обратно
> к формации.

> Если в следующих волне
> или двух ты сможешь
> сбить именно этот
> материнский корабль до
> того, как он снова
> нырнёт — он взрывается, и
> твой захваченный
> истребитель падает к
> ТВОЕМУ новому истребителю
> (следующая жизнь), и два
> ЩЁЛКАЮТ ВМЕСТЕ в
> двухкрылый корабль.
> Теперь у тебя двойная
> скорость огня. Ты
> отомстил за себя
> прошлого.

> Но двухкрылый и в два
> раза шире одиночного.
> В два раза легче попасть.
> Стоит? Всегда, говорит
> игрок, только что
> вытащивший напарника из
> пасти роя. Может, нет,
> говорит игрок,
> мгновенно потерявший
> двухкрылого от шальной
> пули.

> ── ЧАСТЬ 4: ПОЧЕМУ ЭТО ВАЖНО ──

> Механика захвата — в
> ретроспективе — первая
> ДУГА РИСК-ПРОТИВ-НАГРАДЫ
> в дизайне аркадных игр.
> До этого аркадные игры в
> основном были играми НА
> ВЫЖИВАНИЕ. Galaga ввела
> идею, что можно
> НАМЕРЕННО ПРИНЯТЬ плохой
> исход (потерять корабль в
> захвате) в обмен на
> потенциальный лучший
> исход (спасти корабль для
> двухкрылого).

> Игроки в 1981-м поняли
> это в первые несколько
> игр. Опытные игроки
> начали НАМЕРЕННО
> позволять своему кораблю
> быть захваченным на
> определённых волнах,
> делая ставку, что успеют
> его спасти. Вся
> традиция дизайна
> аркадных игр, которую мы
> сейчас зовём
> «пермасмерть с
> механиками искупления» —
> Dark Souls, FTL, Slay
> the Spire — частично
> тянет линию назад к этой
> механике захвата 1981-го.

> ── ЧАСТЬ 5: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — STAR SWARM. Наша
> дань. Треугольный
> истребитель. Осы и дроны
> в формации. Материнские
> корабли с
> трактор-лучами. Захват и
> спасение. Награда —
> двухкрылый.

> D-pad — стрейф
> влево-вправо. ● FIRE —
> двойные лазеры.

> Материнские корабли
> светятся ярче прямо перед
> тем, как нырнуть для
> попытки захвата — твоё
> предупреждение НЕ
> находиться в центральной
> колонне, когда луч
> активируется.

> Убей мать в середине
> захвата, чтобы освободить
> истребитель и получить
> двухкрылого. Поймай её
> трактор-луч, чтобы
> потерять жизнь, но
> возможно восстановить её
> следующей волной.

> Добро пожаловать в
> улей.

> ── СНОСКА У КОСТРА ──

> GALAGA — торговая марка
> Namco Limited (теперь
> Bandai Namco
> Entertainment). Механика
> захвата, награда
> двухкрылого и
> трёхъярусная вражеская
> формация — дизайн Namco.
> Наша дань использует
> другой арт-стиль и
> называется STAR SWARM.
> Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val STAR_SWARM_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val STAR_SWARM_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickStarWingIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> STAR_SWARM_INTRO_RU
    else -> STAR_SWARM_INTRO_EN
}

val STAR_SWARM_INTRO_TEXT: String = STAR_SWARM_INTRO_EN
