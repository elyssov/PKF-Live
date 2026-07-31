package com.pixelclassics.app.games.et

/**
 * Long-form campfire intro for the ExTer game. Юджин: «не экономь буквы,
 * кайф в байке, почти новелла. Начать стоит с того откуда появилась Atari,
 * и обязательно про подвиг Nintendo. Кайф от этой "игры" в том, что ты в
 * неё не играешь, а слушаешь охуенную и длинную байку.»
 *
 * Style: campfire storytelling + mid-80s teletext on a green CRT.
 * Prose, no harsh language, with humour.
 *
 * Bilingual: EN + RU bodies, picked by the player's selected language.
 * Ray's instruction block at the end (Operating Instructions) is
 * carried into both languages.
 */

private val HEADER: String = """
╔════════════════════════════════════════════════╗
║  WARSHAW.SYS v1.0 — TERMINAL OUT — 1983.09.??  ║
╚════════════════════════════════════════════════╝
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> ATARI.LOG /TAIL

> Sit closer to the fire. Throw on another log.
> I'm going to tell you how an empire fell apart
> in one year. Because of one game. Because of
> THIS one. The one you're about to play in
> exactly two minutes. And you have to hear the
> whole story first — otherwise you won't
> understand WHY it is the way it is.
> And it is, oh, it is.

> Let's start from the beginning.

> ── PART 1: CALIFORNIA AND CANNABIS ──

> The seventies. California. A young bearded
> engineer named Nolan Bushnell is playing
> Spacewar! on a university DEC PDP-1. A game
> for physicists. Free. Incredible. Bushnell
> thinks: hold on, you could charge coins for
> this.

> 1971. Nolan and a buddy named Ted Dabney
> stuff Spacewar! into a coin-op cabinet and
> call it COMPUTER SPACE. They put it in a bar.
> Nobody plays. Complicated instructions, no one
> gets what to do. A flop.

> Bushnell doesn't give up. He gets drunk with
> an engineer named Al Alcorn, they smoke weed
> on the roof of a Californian warehouse. Alcorn
> says: listen, let's build a game a girl with
> a beer in a bar will understand without any
> instructions. Bushnell: let's do it.

> A month later they assemble a wooden box with
> a black-and-white TV. On screen: two white
> rectangles and a little square between them.
> They cut a slot in a coffee can for quarters.
> They drag it to Andy Capp's Tavern in
> Sunnyvale.

> Three days in, the bar owner calls them.
> "Hey guys, come over, your machine is broken."
> Bushnell drives over to fix it. He opens it
> up — and the coffee can is OVERFLOWING with
> quarters. Players have stuffed so many in
> that the coin slot is jammed solid.

> That's how PONG was born. That's how ATARI
> was born.

> ── PART 2: THE EMPIRE ──

> By 1977 Atari ships the home console VCS —
> later renamed the Atari 2600. Cartridges. A
> woodsman burns through the cabin on petrol,
> everything's on fire. Kids all over America
> are obsessed with Space Invaders, Pac-Man,
> Asteroids, Pitfall, Adventure. By 1980 half
> the planet's gaming industry sits in Atari's
> pocket.

> By then Bushnell has already sold the company
> to Warner Communications for 28 million.
> He left to open pizzerias with animatronic
> robots (seriously, look up Chuck E. Cheese).
> And Warner installed a guy named Ray Kassar
> on Atari's throne — a textiles executive.
> Underline that: TEXTILES. He understood
> nothing about games and treated his
> programmers like seamstresses.

> Atari's programmers rebelled. The four best
> walked out and founded Activision. The first
> company in history to make games WITHOUT
> selling hardware. Small. Nimble. Their
> Pitfall! sold 4 million copies. Activision
> became the second-most-profitable gaming
> company in the US within a year.

> Atari panics. Pac-Man is rush-ported to the
> 2600 — the result is terrible, 12 million
> cartridges manufactured, 7 sold, the rest
> returned. The first warning bell.

> ── PART 3: SUMMER 1982 AND SPIELBERG ──

> Summer 1982. Steven Spielberg releases a
> film about a little green man stranded on
> Earth. THE EXTRA-TERRESTRIAL. Box office
> blows the doors off. By year-end it's the
> highest-grossing film in history.

> Ray Kassar looks at the posters, runs the
> profit math, and thinks: GOLD MINE. We'll
> buy the licence, make a cartridge for
> Christmas, scoop up money with a shovel.

> He calls Spielberg. Steve answers: 21 million
> dollars. Kassar blinks, coughs — and pays.
> Because Christmas is coming. Because
> confidence. Because the alien on the box =
> automatic gold rush.

> Next step: find a developer. They pick a
> 25-year-old kid named Howard Scott Warshaw.
> He's already shipped Yars' Revenge and
> Raiders of the Lost Ark — both hits.
> Spielberg personally asks for him. Warshaw
> shows up at Kassar's office:

> "OK. How long do I have?"
> "Five weeks and three days."
> "...What?"
> "Spielberg needs the cartridge by September 1
> to make Christmas. Today is July 28. Do the
> math."

> Warshaw goes to his computer and cries. Then
> works 20-hour days. No weekends. No vacation.
> No wife. No sleep. Five weeks later he ships
> the game.

> Atari pressed 5 million cartridges in a
> single run. Each one had the little green
> face on the cover. Stores were packed.
> Christmas displays glowed.

> ── PART 4: JANUARY 1983 ──

> The kids who got E.T. for the holidays cried.

> The little green guy keeps falling into pits
> he can't climb out of. The game's goal is
> unclear — collect three phone pieces and
> call home? Where are they? What does "landing
> zone" mean? Why is this guy in black chasing
> me and stealing my parts? WHY?

> The graphics are ugly even by 1982 standards.
> The sounds are unpleasant squeaks. One level.
> The same map over and over. No bosses. No
> resolution except "the ship leaves".

> By February stores are buried in returns.
> Millions unsold. Millions returned. Something
> has gone very wrong.

> ── PART 5: THE GREAT CRASH ──

> 1983. The Great Video Game Crash. Not only
> Atari — the ENTIRE North American market
> implodes. Stores stop accepting new games
> AT ALL. Time magazine writes: "Video games
> were a fad and the fad is over." Toys'R'Us
> rolls Atari cartridges into the discount
> aisle at five bucks apiece. Kids switch back
> to notebooks and bicycles.

> Atari loses 536 million dollars in one year.
> In 1983 dollars. Adjusted for today, that's
> about 1.8 billion. In ONE year.

> Warner Communications sells what's left of
> Atari to a guy named Jack Tramiel in 1984.
> The programmers have scattered. Kassar is
> fired. Warshaw leaves the industry and
> becomes a licensed psychotherapist. No, really.

> ── PART 6: SEPTEMBER 1983, THE DESERT ──

> Trucks roll out of an Atari warehouse in
> El Paso. They drive east. Into the New Mexico
> desert. To a small town called Alamogordo.
> Out by the municipal landfill, a pit is dug.
> A big one.

> Into it go 14 truckloads of unsold cartridges.
> Most of them — E.T. Some — failed Pac-Man
> ports. Some — other dead inventory. A
> bulldozer flattens them. Concrete is poured
> on top, so souvenir hunters can't dig them
> back up. Sand is packed over the concrete.
> And that's that — shhh — forget it, pretend
> none of this happened.

> Atari officially denied the burial for thirty
> years. The New York Times ran the story in
> September 1983 — Atari called it a rumour.
> Warehouse workers gave interviews anonymously.
> It became an urban legend. "My friend's uncle
> saw it." "I know the guy who loaded the
> trucks." A folk tale.

> ── PART 7: NINTENDO'S MIRACLE ──

> 1985. The industry is dead. Journalists
> write obituaries. Magazine racks carry only
> personal-computer titles now.

> Then a strange little grey box arrives from
> Japan called the NINTENDO ENTERTAINMENT
> SYSTEM. They explicitly do NOT call it a
> "game console" (because the word "game" is
> a dirty word now), but an "entertainment
> system". They bundle a robot called R.O.B. —
> because robots are technology, technology is
> serious, serious is what parents will buy.

> Retailers refuse to stock it. "Video games
> are dead, you can't shift these." Nintendo
> plays its masterstroke: CONSIGNMENT. As in,
> free. "Take it, put it on the shelf. Doesn't
> sell — return it for full credit. Sells —
> we split the take. Bonus: the retailer that
> moves the most units wins a brand new TV.
> Free. Just for selling."

> Retailers reluctantly agree. Why not.

> And it works. Kids see Mario on the shelf.
> Mario jumps on goombas. Mario does not fall
> into unclimbable pits. Mario's goal is
> obvious without instructions: GO-RIGHT-RESCUE-
> PRINCESS. Kids drag their parents in. Parents
> buy. Retailers reorder.

> By 1987 Nintendo sells 7 million NES units
> in the US. By 1990 — 30 million. The industry
> isn't just restored — it's ten times bigger
> than it ever was. Then will come the SNES,
> then the N64, then you will be born, then in
> childhood you will sink hours into Super
> Mario Bros 3 and Zelda without ever knowing
> all of this happened because ONE small
> Japanese company decided: we are going to
> raise the dead.

> ── PART 8: 2014, THE DIG ──

> Thirty years later — April 2014. Microsoft
> shoots a documentary for the Xbox One launch.
> They hire archaeologists (actual archaeologists,
> with shovels). They drive to Alamogordo.
> They core. They dig.

> One metre down — trash bags. Two metres —
> old newspapers. Three metres — cartridges.
> Thousands of cartridges. Most of them E.T.
> The rumour turned out to be true. The urban
> legend was the urban history.

> They recover 1,300 units. Some go to museums
> (the Smithsonian, MoMA New York, the Strong
> Museum of Play). Some sell on eBay for fifty
> dollars apiece. Some go to Atari devotees as
> relics. The remaining millions stay in the
> ground — re-buried, re-concreted.

> ── EPILOGUE ──

> And now, dear player, you are about to sit
> down and play that very same E.T. — not a
> remake. Not a port-with-improvements. A
> reconstruction. With the same un-climbable
> pits. With the same FBI agent stealing your
> phone pieces. With the same pointless running
> between zones. With the same feeling of
> "WHY AM I DOING THIS".

> You're holding a piece of history. The
> exact piece that was actually buried in
> concrete. The piece that almost killed the
> industry. The piece Warshaw wrote in five
> weeks and three days, weeping into a
> Californian terminal in the dark.

> A "touch-the-legend" amusement-park ride.
> That is the whole point. The game is
> terrible — that is its greatest virtue.

> Good luck, ExTer. Phone home.

> ── OPERATING INSTRUCTIONS, UNFORTUNATELY ──

> Ah. Yes. Children. One more thing.

> The original nightmare had another elegant
> historical problem: when you picked up the
> cartridge, you did not merely wonder whether
> the game was good. You wondered what the game
> WAS. What is a pit? Why am I in a pit? Why is
> a government man stealing my stuff? Why does
> the map loop like a cursed hallway? Why am I
> doing any of this?

> I will not be that cruel. Here are the rules.

> You control a small stranded alien. Use the
> D-pad to walk between six looping zones. Three
> phone pieces are hidden in the world. Find all
> three. If you touch one, you collect it.

> Some zones contain pits. They are dark holes
> in the ground because subtlety was apparently
> expensive in 1982. If you fall in, hold UP or
> press FIRE to stretch your neck and levitate
> out. Yes, really. That is how we live now.

> The blue agent wants your phone pieces. If he
> catches you, he may take one and throw it back
> into the world. The pale scientist wastes your
> time. Avoid both unless you are collecting
> authentic suffering.

> Candy restores energy. Energy and time both
> run down. When you have all three phone parts,
> go to the LANDING zone. Press FIRE there to
> place the call. The ship comes down. You win.

> To summarize, because mercy exists:
> 1. collect 3 phone parts;
> 2. avoid pits, agents and scientists;
> 3. go to LANDING;
> 4. press FIRE;
> 5. go home and never trust movie licences
>    again.

> ── LEGAL NOTE AT THE CAMPFIRE ──

> A small but honest footnote — can't be
> avoided. This is not the original 1982 game
> and not its port. It's our reconstruction,
> from memory and folklore — a tribute to the
> most famous flop in video-game history.

> We claim NO connection with the well-known
> film about an alien stranded on Earth. The
> film is mentioned here only as one artefact
> in the legend, by historical reference, as
> part of the campfire story.

> All rights in the original names, films and
> characters belong to their respective owners.
> If a rights-holder has any concern, we will
> resolve it kindly and promptly. Write:
> elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> ATARI.LOG /TAIL

> Сядь поближе к огню. Подкинь
> ещё полено. Я расскажу тебе, как
> империя развалилась за один
> год. Из-за одной игры. Из-за
> ВОТ этой. Той самой, в которую
> ты сядешь играть ровно через
> две минуты. И сначала ты должен
> услышать всю историю —
> иначе не поймёшь, ПОЧЕМУ она
> такая, какая есть. А она —
> о, какая она есть.

> Начнём с начала.

> ── ЧАСТЬ 1: КАЛИФОРНИЯ И КАННАБИС ──

> Семидесятые. Калифорния.
> Молодой бородатый инженер по
> имени Нолан Бушнелл играет в
> Spacewar! на университетской
> DEC PDP-1. Игра для физиков.
> Бесплатная. Невероятная.
> Бушнелл думает: погодите-ка, за
> это можно брать монетки.

> 1971-й. Нолан и его дружок Тед
> Дабни запихивают Spacewar! в
> аркадный кабинет и называют
> COMPUTER SPACE. Ставят в бар.
> Никто не играет. Сложные
> инструкции, никто не понимает,
> что делать. Провал.

> Бушнелл не сдаётся. Он
> напивается с инженером по
> имени Эл Алкорн, они курят
> траву на крыше калифорнийского
> склада. Алкорн говорит: слушай,
> давай сделаем игру, которую
> поймёт девчонка с пивом в баре,
> без всяких инструкций. Бушнелл:
> поехали.

> Через месяц они собирают
> деревянный ящик с чёрно-белым
> телевизором. На экране — два
> белых прямоугольника и
> квадратик между ними.
> Прорезают щель в банке из-под
> кофе для четвертаков. Тащат
> его в «Таверну Энди Кэппа» в
> Саннивейл.

> На третий день владелец бара
> им звонит. «Ребят, подъезжайте,
> ваша машина сломалась.»
> Бушнелл едет чинить. Открывает
> — а банка ПЕРЕПОЛНЕНА
> четвертаками. Игроки набили
> столько, что монетоприёмник
> заклинило намертво.

> Так родился PONG. Так родилась
> ATARI.

> ── ЧАСТЬ 2: ИМПЕРИЯ ──

> К 1977-му Atari выпускает
> домашнюю консоль VCS — позже
> переименуют в Atari 2600.
> Картриджи. Лесник полил
> избушку бензином, всё горит.
> Дети по всей Америке
> одержимы Space Invaders,
> Pac-Man, Asteroids, Pitfall,
> Adventure. К 1980-му полпланеты
> игровой индустрии сидит у
> Atari в кармане.

> К этому времени Бушнелл уже
> продал компанию Warner
> Communications за 28 миллионов.
> Ушёл открывать пиццерии с
> аниматронными роботами
> (серьёзно, погугли Chuck E.
> Cheese). А Warner посадил на
> трон Atari парня по имени
> Рэй Кассар — текстильного
> топ-менеджера. Подчеркну:
> ТЕКСТИЛЬНОГО. Он ничего не
> понимал в играх и обращался
> со своими программистами как
> со швеями.

> Программисты Atari взбунтовались.
> Четверо лучших ушли и основали
> Activision. Первая в истории
> компания, делающая игры БЕЗ
> продажи железа. Маленькая.
> Ловкая. Их Pitfall! продался
> 4 миллионами копий. Activision
> стала второй по доходам
> игровой компанией США за год.

> Atari в панике. Pac-Man срочно
> портируют на 2600 — результат
> ужасен, выпустили 12 миллионов
> картриджей, продали 7, остальные
> вернули. Первый тревожный
> звонок.

> ── ЧАСТЬ 3: ЛЕТО 1982 И СПИЛБЕРГ ──

> Лето 1982-го. Стивен Спилберг
> выпускает фильм про маленького
> зелёного человечка, застрявшего
> на Земле. THE EXTRA-TERRESTRIAL.
> Бокс-офис сносит крышу. К концу
> года это самый кассовый фильм
> в истории.

> Рэй Кассар смотрит на постеры,
> прикидывает математику прибыли
> и думает: ЗОЛОТАЯ ЖИЛА. Купим
> лицензию, сделаем картридж к
> Рождеству, будем грести деньги
> лопатой.

> Звонит Спилбергу. Стив отвечает:
> 21 миллион долларов. Кассар
> моргает, кашляет — и платит.
> Потому что Рождество близко.
> Потому что уверенность. Потому
> что инопланетянин на коробке =
> автоматическая золотая
> лихорадка.

> Следующий шаг: найти
> разработчика. Выбирают
> 25-летнего парня по имени
> Говард Скотт Уоршо. Он уже
> отгрузил Yars' Revenge и
> Raiders of the Lost Ark — оба
> хита. Спилберг лично просит
> его. Уоршо появляется в офисе
> Кассара:

> «Окей. Сколько у меня времени?»
> «Пять недель и три дня.»
> «...Что?»
> «Спилбергу нужен картридж к
> 1 сентября, чтобы успеть к
> Рождеству. Сегодня 28 июля.
> Посчитай.»

> Уоршо идёт к компьютеру и
> плачет. Потом работает по 20
> часов в день. Без выходных. Без
> отпуска. Без жены. Без сна.
> Через пять недель отгружает
> игру.

> Atari прессует 5 миллионов
> картриджей одним тиражом. На
> каждом — маленькое зелёное
> личико на обложке. Магазины
> ломятся. Рождественские
> витрины светятся.

> ── ЧАСТЬ 4: ЯНВАРЬ 1983 ──

> Дети, которым E.T. подарили на
> праздники, плакали.

> Маленький зелёный парень
> постоянно падает в ямы, из
> которых не может выбраться.
> Цель игры непонятна — собрать
> три части телефона и позвонить
> домой? Где они? Что такое
> «зона посадки»? Почему этот
> чувак в чёрном за мной гоняется
> и крадёт мои детали? ПОЧЕМУ?

> Графика уродская даже по
> меркам 1982-го. Звуки —
> неприятные писки. Один
> уровень. Одна и та же карта
> по кругу. Боссов нет. Развязки
> нет, кроме «корабль улетает».

> К февралю магазины завалены
> возвратами. Миллионы
> непроданных. Миллионы
> возвращённых. Что-то пошло
> очень не так.

> ── ЧАСТЬ 5: ВЕЛИКИЙ КРАХ ──

> 1983-й. Великий крах
> видеоигр. Не только Atari —
> весь североамериканский
> рынок имплозирует. Магазины
> ВООБЩЕ перестают принимать
> новые игры. Журнал Time
> пишет: «Видеоигры были
> модой, и мода закончилась.»
> Toys'R'Us выкатывает
> картриджи Atari в скидочный
> ряд по пять баксов. Дети
> переключаются обратно на
> блокноты и велосипеды.

> Atari теряет 536 миллионов
> долларов за один год. В
> долларах 1983-го. С поправкой
> на сегодня — это около 1.8
> миллиарда. За ОДИН год.

> Warner Communications
> продаёт остатки Atari парню
> по имени Джек Трамиел в
> 1984-м. Программисты
> разбежались. Кассара уволили.
> Уоршо уходит из индустрии и
> становится лицензированным
> психотерапевтом. Нет, правда.

> ── ЧАСТЬ 6: СЕНТЯБРЬ 1983, ПУСТЫНЯ ──

> Грузовики выкатываются со
> склада Atari в Эль-Пасо. Едут
> на восток. В пустыню
> Нью-Мексико. В маленький
> городок Аламогордо. У местной
> муниципальной свалки роют
> яму. Большую.

> В неё ссыпают 14 грузовиков
> непроданных картриджей.
> Большинство — E.T. Часть —
> провальные порты Pac-Man.
> Часть — другие мёртвые
> запасы. Бульдозер их
> утрамбовывает. Сверху льют
> бетон, чтобы охотники за
> сувенирами не выкопали.
> Песок утаптывают сверху на
> бетон. И всё — тссс — забудьте,
> сделайте вид, что ничего не
> было.

> Atari официально отрицала
> захоронение тридцать лет.
> New York Times опубликовала
> историю в сентябре 1983-го —
> Atari назвала это слухами.
> Складские рабочие давали
> интервью анонимно. Это стало
> городской легендой. «Мой друг
> знал дядьку, который видел.»
> «Я знаю парня, который
> грузил.» Народная байка.

> ── ЧАСТЬ 7: ЧУДО NINTENDO ──

> 1985-й. Индустрия мертва.
> Журналисты пишут некрологи.
> На полках журналов теперь
> только заголовки про
> персональные компьютеры.

> Тут из Японии приезжает
> странная серая коробочка под
> названием NINTENDO
> ENTERTAINMENT SYSTEM. Они
> специально НЕ называют её
> «игровая консоль» (потому что
> слово «игра» теперь грязное),
> а «развлекательная система».
> К ней в комплекте робот по
> имени R.O.B. — потому что
> роботы — это технология,
> технология — это серьёзно, а
> серьёзное родители покупают.

> Ритейлеры отказываются
> ставить на полку. «Видеоигры
> мертвы, эти не разойдутся.»
> Nintendo делает свой
> мастер-ход: КОНСИГНАЦИЯ. То
> есть бесплатно. «Берите,
> ставьте на полку. Не продаётся
> — возвращайте за полную
> стоимость. Продаётся — делим
> выручку. Бонус: ритейлер,
> который продаст больше всех,
> получает новый телевизор.
> Бесплатно. Просто за продажу.»

> Ритейлеры неохотно
> соглашаются. Почему нет.

> И это срабатывает. Дети видят
> Марио на полке. Марио прыгает
> на гумб. Марио не падает в
> ямы, из которых не выбраться.
> Цель Марио очевидна без
> инструкций: ИДИ-НАПРАВО-СПАСИ-
> ПРИНЦЕССУ. Дети тянут
> родителей. Родители покупают.
> Ритейлеры дозаказывают.

> К 1987-му Nintendo продаёт
> 7 миллионов NES в США. К
> 1990-му — 30 миллионов.
> Индустрия не просто
> восстановлена — она в десять
> раз больше, чем когда-либо
> была. Потом будут SNES,
> потом N64, потом ты родишься,
> потом в детстве ты утопишь
> часы в Super Mario Bros 3 и
> Zelda, ни разу не зная, что
> всё это случилось потому, что
> ОДНА маленькая японская
> компания решила: мы будем
> поднимать мёртвых.

> ── ЧАСТЬ 8: 2014, РАСКОПКИ ──

> Тридцать лет спустя — апрель
> 2014-го. Microsoft снимает
> документальный фильм к
> запуску Xbox One. Они нанимают
> археологов (настоящих
> археологов, с лопатами). Едут
> в Аламогордо. Берут пробы.
> Копают.

> Метр вглубь — мусорные
> мешки. Два метра — старые
> газеты. Три метра —
> картриджи. Тысячи картриджей.
> Большинство — E.T. Слухи
> оказались правдой. Городская
> легенда была городской
> историей.

> Достают 1300 экземпляров.
> Часть идёт в музеи
> (Смитсоновский, MoMA в
> Нью-Йорке, Strong Museum of
> Play). Часть продаётся на
> eBay по пятьдесят долларов
> за штуку. Часть достаётся
> верующим Atari как реликвии.
> Оставшиеся миллионы остаются
> в земле — снова закопаны,
> снова забетонированы.

> ── ЭПИЛОГ ──

> А теперь, дорогой игрок, ты
> сядешь играть в тот самый
> E.T. — не ремейк. Не
> порт-с-улучшениями.
> Реконструкция. С теми же
> неприступными ямами. С тем же
> агентом ФБР, ворующим твои
> части телефона. С той же
> бессмысленной беготнёй между
> зонами. С тем же чувством
> «ПОЧЕМУ Я ЭТО ДЕЛАЮ».

> Ты держишь кусочек истории.
> Тот самый кусочек, который и
> правда был залит бетоном.
> Кусочек, который чуть не
> убил индустрию. Кусочек,
> который Уоршо написал за
> пять недель и три дня, плача
> в темноте над калифорнийским
> терминалом.

> «Прикоснись-к-легенде»,
> аттракцион в парке. Это и
> есть смысл. Игра ужасна — в
> этом её главное достоинство.

> Удачи, ExTer. Звони домой.

> ── ИНСТРУКЦИЯ К ПРИМЕНЕНИЮ, УВЫ ──

> А. Да. Дети. Ещё одна вещь.

> У оригинального кошмара была
> ещё одна изящная историческая
> проблема: подбирая картридж,
> ты не просто гадал, хорошая
> ли это игра. Ты гадал, ЧТО
> вообще это за игра. Что такое
> яма? Почему я в яме? Почему
> правительственный мужик
> крадёт мои вещи? Почему карта
> ходит по кругу как проклятый
> коридор? Зачем я всё это
> делаю?

> Я не буду столь жесток. Вот
> правила.

> Ты управляешь маленьким
> застрявшим инопланетянином.
> Используй D-pad, чтобы ходить
> между шестью зацикленными
> зонами. Три части телефона
> спрятаны в мире. Найди все
> три. Коснёшься — подберёшь.

> В некоторых зонах есть ямы.
> Это тёмные дыры в земле,
> потому что в 1982-м тонкости,
> по-видимому, дорого стоили.
> Если упал — держи ВВЕРХ или
> жми FIRE, чтобы вытянуть шею
> и левитировать наверх. Да,
> правда. Так мы теперь живём.

> Синий агент хочет твои части
> телефона. Если поймает —
> может забрать одну и
> зашвырнуть обратно в мир.
> Бледный учёный тратит твоё
> время. Избегай обоих, если
> только не коллекционируешь
> аутентичные страдания.

> Конфеты восстанавливают
> энергию. Энергия и время оба
> тикают вниз. Когда у тебя
> все три части телефона —
> иди в зону LANDING. Жми там
> FIRE, чтобы сделать звонок.
> Корабль спускается. Ты
> выиграл.

> Короче, потому что милосердие
> существует:
> 1. собери 3 части телефона;
> 2. избегай ям, агентов и
>    учёных;
> 3. иди в LANDING;
> 4. жми FIRE;
> 5. иди домой и больше никогда
>    не доверяй кинолицензиям.

> ── ЮРИДИЧЕСКАЯ СНОСКА У КОСТРА ──

> Маленькая, но честная сноска —
> никуда не деться. Это не
> оригинальная игра 1982-го и не
> её порт. Это наша
> реконструкция по памяти и
> фольклору — дань самому
> знаменитому провалу в истории
> видеоигр.

> Мы НЕ заявляем никакой связи
> с известным фильмом про
> инопланетянина, застрявшего
> на Земле. Фильм упомянут
> здесь только как один из
> артефактов легенды, по
> историческому референсу, как
> часть рассказа у костра.

> Все права на оригинальные
> названия, фильмы и
> персонажей принадлежат их
> соответствующим владельцам.
> Если у правообладателя есть
> вопросы — мы их уладим
> по-доброму и быстро. Пишите:
> elyssov@gmail.com.
""".trimIndent()

val ET_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val ET_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickETIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> ET_INTRO_RU
    else -> ET_INTRO_EN
}

/** Back-compat alias. */
