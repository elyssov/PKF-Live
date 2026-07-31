package com.pixelclassics.app.games.pong

/**
 * Long-form campfire intro for Paddles (Pong). Voice: the original 1972
 * arcade cabinet itself, narrating from a barn behind a museum in 2026,
 * still functional, still occasionally getting played by people who
 * remember when this was the entire industry.
 *
 * Bilingual: EN + RU bodies, picked by the player's selected language.
 * VI translation pending.
 */

private val HEADER: String = """
████████████████████████████████████████
██                                    ██
██      PONG ARCADE — UNIT #00012     ██
██      SUNNYVALE, CA — JUNE 1972     ██
██                                    ██
████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> I am a wooden cabinet with a small
> black-and-white television set bolted
> into me. On my front there is a slot
> for a quarter, a power switch, and two
> small white knobs. On my back, six
> screws hold me together. I am about
> four feet tall. I weigh maybe forty
> kilograms. I am the entire video game
> industry.

> Let me explain.

> ── PART 1: A WAREHOUSE IN CALIFORNIA ──

> 1972. California. Two young engineers,
> Nolan Bushnell and Ted Dabney, have
> founded a company called Syzygy a
> year earlier. They've since renamed
> it Atari. They are basically broke.
> Their first arcade game, COMPUTER
> SPACE, failed at every bar they put
> it in — too complicated for drunk
> people to understand.

> Bushnell hires an engineer named Al
> Alcorn. Alcorn is fresh out of UC
> Berkeley. He has never designed a
> video game in his life. Bushnell does
> not tell him this. Bushnell tells him:
> "Build me a game. A really, really
> simple one. Two paddles. A ball.
> The ball bounces. Use it as your
> warm-up exercise."

> Alcorn thinks he is being trained.
> Bushnell knows he is being given the
> actual product.

> Alcorn builds the prototype in three
> months. It is one TTL board. About
> sixty-six chips wire-wrapped together.
> No microprocessor — microprocessors
> are not really a thing yet for
> consumer products. The whole game is
> hardwired logic. It plays one melody:
> the click of the ball hitting a
> paddle. Click. Click. Click.

> Alcorn adds two refinements without
> being asked:
>   (a) the angle the ball bounces at
>       depends on WHERE on the paddle
>       it hits, so skilled players can
>       direct the ball;
>   (b) when a paddle misses, the ball
>       briefly speeds up before the
>       next serve, to keep the rally
>       from getting boring.

> These are the first emergent strategy
> mechanics in the history of video
> games.

> ── PART 2: A BAR IN SUNNYVALE ──

> Bushnell and Dabney install me inside
> a wooden cabinet, with a slot cut into
> a coffee can taped to the side for
> quarters. They drive me to Andy Capp's
> Tavern in Sunnyvale and set me on the
> floor near the wall.

> No instructions. Bushnell only writes
> one line on a piece of paper taped to
> my screen:

>     AVOID MISSING BALL FOR HIGH SCORE.

> That is the entire user manual. For
> the entire game.

> A few days later, Bill Gattis, the
> tavern's owner, calls Bushnell.
> "Hey, that game machine of yours? It's
> broken." Bushnell drives over,
> expecting a blown fuse or a bad
> capacitor.

> What he finds is that the coffee can
> attached to my side is COMPLETELY
> JAMMED WITH QUARTERS. So full that
> the next coin won't fit. So full that
> when a player puts a coin in, the
> mechanism cannot register the coin
> because there is nowhere for it to
> drop.

> Bushnell empties the can. Plays for a
> while. Goes home thinking.

> The next week, sales of more PONG
> cabinets begin. He doesn't bother to
> license the design from anyone else
> because there is no one to license
> from. He doesn't bother to patent it
> because it's already in production.
> He just builds more of me. Hundreds.
> Thousands. Tens of thousands.

> ── PART 3: THE FIRST INDUSTRY ──

> 1972 ends with 19,000 PONG cabinets in
> bars, bowling alleys, laundromats, and
> pizza parlours across the United
> States. The video game industry has
> just been born. There is one game in
> it. It is me.

> By 1973 clones are everywhere. Magnavox
> sues Atari for patent infringement —
> Ralph Baer at Magnavox had built a
> tennis prototype on the Magnavox
> Odyssey home console back in 1966.
> Bushnell settles for ${'$'}700,000 and
> perpetual rights. (He maintains, to
> the end of his life, that he had
> seen Baer's prototype at a trade show
> and "drawn inspiration", though he
> always insists he didn't directly
> copy it. The court did not find this
> distinction interesting.)

> By 1975, Atari ships PONG as a home
> console — a single-purpose box you
> plug into your TV that ONLY plays
> PONG. Sold through Sears at JCPenney
> prices. Goes berserk for Christmas.

> ── PART 4: WHAT I AM, EXACTLY ──

> I am the simplest possible video game
> that is still a video game.

> Two players each control a vertical
> rectangle. A square ball bounces.
> Hit your rectangle into the ball's
> path — the ball bounces back. Miss
> the ball — the other player scores.
> First to 11 wins. There is no story.
> There is no music. There is no power-
> up. There is no upgrade tree. There
> is no inventory. There is no menu.

> And yet, when I am switched on, you
> cannot stop playing me.

> ── PART 5: WHY ──

> Because it is the truest interactive
> experience humans have ever built. A
> game theorist named Bernie DeKoven
> later wrote that what we built was
> not really a game — it was a
> conversation. Two players, taking
> turns, sending the ball back and
> forth, getting tired together,
> getting good together, getting bored
> together, all without speaking. The
> ball is the topic of conversation.
> Every rally is a sentence. Every
> miss is a punctuation mark.

> I was the first machine in human
> history that two people could share
> a non-verbal conversation through.
> Before me you needed words, or
> gestures, or a shared physical
> activity like rowing a boat. After
> me you could just sit in a bar with
> a stranger and click a knob and
> understand each other.

> ── PART 6: NOW ──

> Here in 2026, the company that owns
> the rights to my name has changed
> hands at least eight times. Most of
> my original cabinets are in landfills.
> A few of us are in museums — the
> Smithsonian has one. The Computer
> History Museum in Mountain View has
> one. The Museum of Modern Art in New
> York lists PONG as part of its
> permanent collection on the grounds
> of being one of the most important
> works of interactive art of the 20th
> century. Which, accidentally, it is.

> Most of the people who played me as
> children are now grandparents.

> What we have made for you below is
> not the original PONG. It is a
> tribute. The paddles are the same.
> The ball is the same. The angles are
> the same. The click sound is the
> same. We have called it PADDLES so
> nobody confuses our love letter
> with the trademark.

> Drag your paddle on the left side of
> the screen, up and down. Your
> opponent is the CPU on the right,
> playing badly on purpose so you can
> learn. First to eleven wins.

> Welcome back. Or, if this is your
> first time — welcome.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> The PONG trademark belongs to whoever
> owns what was once Atari Inc. (a long
> chain of acquisitions). The MECHANIC
> — two paddles, a ball, bouncing — is
> not protectable; courts have ruled
> repeatedly that game mechanics are
> ideas, not protectable expression.
> We call our version PADDLES out of
> politeness. If a rights-holder has
> any concern: elyssov@gmail.com,
> resolved gently and quickly.
""".trimIndent()

private val BODY_RU: String = """
> Я — деревянный шкаф с маленьким
> чёрно-белым телевизором,
> вкрученным в меня. Спереди —
> прорезь для четвертака,
> выключатель и две белые
> ручки-крутилки. Сзади шесть
> винтов держат меня вместе.
> Высотой я метра полтора. Вешу
> килограммов сорок. Я — вся
> видеоигровая индустрия,
> целиком.

> Объясню.

> ── ЧАСТЬ 1: СКЛАД В КАЛИФОРНИИ ──

> 1972-й. Калифорния. Два
> молодых инженера, Нолан
> Бушнелл и Тед Дабни, основали
> год назад компанию Syzygy. С
> тех пор переименовали в Atari.
> У них в кармане пусто. Их
> первая аркадная игра, COMPUTER
> SPACE, провалилась в каждом
> баре, куда её ставили — слишком
> сложна для пьяных людей.

> Бушнелл нанимает инженера по
> имени Эл Алкорн. Алкорн только
> что выпустился из UC Berkeley.
> Он ни разу в жизни не
> разрабатывал видеоигру.
> Бушнелл ему этого не говорит.
> Бушнелл говорит: «Сделай мне
> игру. Очень-очень простую. Две
> ракетки. Мяч. Мяч отскакивает.
> Будет тебе разминкой.»

> Алкорн думает, что его
> тренируют. Бушнелл знает: ему
> только что отдали настоящий
> продукт.

> Алкорн делает прототип за три
> месяца. Это одна TTL-плата.
> Около шестидесяти шести
> микросхем, соединённых
> накруткой. Без процессора —
> процессоры для бытовой
> электроники ещё толком не
> существуют. Вся игра — это
> зашитая в железо логика. Она
> играет одну мелодию: щелчок
> мяча, попадающего в ракетку.
> Щёлк. Щёлк. Щёлк.

> Алкорн добавляет два уточнения,
> не спрашивая:
>   (а) угол отскока зависит от
>       того, КУДА на ракетке
>       попал мяч, чтобы умелые
>       игроки могли направлять
>       мяч;
>   (б) когда ракетка
>       промахивается, мяч
>       ненадолго ускоряется
>       перед следующей подачей,
>       чтобы розыгрыш не
>       наскучивал.

> Это первые ёмердж-стратегичные
> механики в истории видеоигр.

> ── ЧАСТЬ 2: БАР В САННИВЕЙЛ ──

> Бушнелл и Дабни вставляют меня
> в деревянный корпус, с
> прорезью в банке из-под кофе,
> примотанной скотчем сбоку, для
> четвертаков. Везут меня в
> «Таверну Энди Кэппа» в
> Саннивейл и ставят на пол у
> стены.

> Без инструкций. Бушнелл только
> пишет одну строчку на бумажке,
> прилепленной к моему экрану:

>     НЕ ПРОПУСКАЙ МЯЧ — ВЫИГРАЕШЬ.

> Это весь пользовательский
> мануал. На всю игру.

> Через несколько дней Билл
> Гэттис, владелец таверны,
> звонит Бушнеллу. «Слушай, твоя
> игровая машина — сломалась.»
> Бушнелл едет туда, ждёт
> сгоревший предохранитель или
> плохой конденсатор.

> А находит он то, что банка
> из-под кофе у меня сбоку —
> ПОЛНОСТЬЮ ЗАБИТА
> ЧЕТВЕРТАКАМИ. Так забита, что
> следующая монета уже не лезет.
> Так забита, что когда игрок
> опускает четвертак, механизм
> не может его зарегистрировать
> — некуда падать.

> Бушнелл вытряхивает банку.
> Играет немного. Уезжает домой
> думать.

> На следующей неделе начинают
> продавать ещё кабинеты PONG.
> Он не заморачивается лицензией
> у кого-то ещё, потому что не у
> кого. Не заморачивается
> патентом, потому что игра уже
> в производстве. Он просто
> строит ещё. Сотни. Тысячи.
> Десятки тысяч.

> ── ЧАСТЬ 3: ПЕРВАЯ ИНДУСТРИЯ ──

> 1972-й заканчивается с 19 000
> кабинетов PONG в барах,
> боулингах, прачечных и
> пиццериях по всей Америке.
> Видеоигровая индустрия только
> что родилась. В ней одна игра.
> Это я.

> К 1973-му клоны повсюду.
> Magnavox судит Atari за
> нарушение патента — Ральф
> Баер в Magnavox построил
> теннисный прототип на домашней
> консоли Magnavox Odyssey ещё
> в 1966-м. Бушнелл откупается
> за 700 000 долларов и
> бессрочные права. (Он до конца
> жизни утверждал, что видел
> прототип Баера на выставке и
> «вдохновился», хотя всегда
> настаивал, что не копировал
> напрямую. Суду это различие
> неинтересно.)

> К 1975-му Atari выпускает
> PONG как домашнюю консоль —
> одноцелевую коробку, которая
> втыкается в телевизор и играет
> ТОЛЬКО в PONG. Продаётся в
> Sears по ценам JCPenney. К
> Рождеству — берсерк.

> ── ЧАСТЬ 4: ЧТО Я, ТОЧНЕЕ ──

> Я — простейшая возможная
> видеоигра, которая ещё
> является видеоигрой.

> Два игрока, каждый управляет
> вертикальным прямоугольником.
> Квадратный мяч отскакивает.
> Сунь свой прямоугольник на
> путь мяча — мяч отскочит
> обратно. Промахнёшься — у
> другого очко. Первый до 11
> выигрывает. Сюжета нет.
> Музыки нет. Усилителей нет.
> Дерева прокачки нет.
> Инвентаря нет. Меню нет.

> И всё же, когда меня
> включают — оторваться
> невозможно.

> ── ЧАСТЬ 5: ПОЧЕМУ ──

> Потому что это самый
> подлинный интерактивный опыт,
> когда-либо построенный
> людьми. Теоретик игр по имени
> Берни ДеКовен позже написал,
> что то, что мы построили —
> это не совсем игра. Это был
> разговор. Два игрока, по
> очереди отправляющие мяч
> туда-сюда, устающие вместе,
> становящиеся хорошими вместе,
> скучающие вместе, всё это без
> слов. Мяч — тема разговора.
> Каждый розыгрыш — фраза.
> Каждый промах — знак
> препинания.

> Я был первой машиной в
> истории человечества, через
> которую двое могли вести
> невербальный разговор. До
> меня нужны были слова, или
> жесты, или общая физическая
> активность — типа грести в
> лодке. После меня можно было
> сидеть в баре с незнакомцем,
> щёлкать ручкой и понимать
> друг друга.

> ── ЧАСТЬ 6: СЕЙЧАС ──

> Сейчас, в 2026-м, компания,
> владеющая правами на моё имя,
> сменила хозяев не меньше
> восьми раз. Большинство моих
> оригинальных кабинетов на
> свалке. Несколько из нас в
> музеях — у Смитсоновского
> один. У Computer History
> Museum в Маунтин-Вью один.
> Музей современного искусства
> в Нью-Йорке держит PONG в
> постоянной коллекции на
> основании того, что это одна
> из важнейших работ
> интерактивного искусства
> 20-го века. Что, между
> прочим, так и есть.

> Большинство людей, игравших в
> меня детьми, сейчас —
> бабушки и дедушки.

> То, что мы сделали для тебя
> ниже — не оригинальный PONG.
> Это дань. Ракетки те же.
> Мяч тот же. Углы те же.
> Звук-щелчок тот же. Мы
> назвали это PADDLES, чтобы
> никто не путал наше любовное
> письмо с торговой маркой.

> Тяни свою ракетку на левой
> стороне экрана, вверх-вниз.
> Твой противник — CPU справа,
> играющий нарочно плохо,
> чтобы ты мог научиться.
> Первый до одиннадцати
> выигрывает.

> С возвращением. А если это
> первый раз — добро
> пожаловать.

> ── СНОСКА У КОСТРА ──

> Бренд PONG принадлежит тому,
> кто сейчас владеет тем, что
> когда-то было Atari Inc.
> (длинная цепочка
> приобретений). МЕХАНИКА —
> две ракетки, мяч, отскоки —
> не охраняется; суды
> неоднократно подтверждали,
> что игровые механики — идеи,
> а не охраняемое выражение.
> Мы зовём свою версию
> PADDLES из вежливости. Если
> у правообладателя есть
> вопросы: elyssov@gmail.com,
> уладим тихо и быстро.
""".trimIndent()

val PONG_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val PONG_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickPongIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> PONG_INTRO_RU
    else -> PONG_INTRO_EN
}

/** Back-compat alias. */
val PONG_INTRO_TEXT: String = PONG_INTRO_EN
