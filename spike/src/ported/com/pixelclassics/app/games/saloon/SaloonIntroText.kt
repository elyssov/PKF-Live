package com.pixelclassics.app.games.saloon

/**
 * Long-form campfire intro for SALOON (Tapper). Voice: the bartender of
 * the original Bally Midway 1983 cabinet.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
████████████████████████████████████████
█  BALLY MIDWAY · TAPPER · 1983         █
█  ARCADE CABINET · BEER-TAP HANDLE     █
█  REAL DRAFT LEVER · TENSION SENSOR    █
████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> Howdy partner.

> Pull up a stool. Have a drink. The
> draft beer is real. The tap handle
> in your hand — yes, that one, the
> shiny mahogany lever sticking up
> out of the cabinet panel — is a
> real beer-tap handle, mounted on a
> tension sensor so the arcade
> machine can tell how hard you're
> pulling. The cabinet is built like
> a small bar. There are five of you
> standing around it, because that's
> how the cabinet was designed to
> accommodate spectators.

> This is TAPPER. Bally Midway, 1983.
> I'm the bartender.

> ── PART 1: HOW THIS HAPPENED ──

> Late 1981. Bally Midway, then a
> major US arcade-game manufacturer,
> approached Marvin Glass &
> Associates — a Chicago design
> consultancy famous for inventing
> Hasbro's "Operation" board game,
> "Mr. Mouth", and "Ants in the
> Pants" — and asked them: design
> us a NOVEL arcade game. Not
> another shoot-em-up. Not another
> maze chase. Something the arcades
> haven't seen.

> Marvin Glass had a designer named
> Steve Meyer working on industrial-
> design concepts. Meyer came back
> with the idea of a TIME-MANAGEMENT
> arcade game.

> Time-management games had not
> existed in arcades before 1982.
> Every existing arcade game was
> "kill the enemies" or "navigate
> the maze". Meyer's pitch was
> radically different: you are
> a BARTENDER. You serve drinks.
> You catch the mugs that come
> back. The faster you go, the
> harder it gets. The challenge
> is THROUGHPUT, not aim.

> Bally Midway loved it. They
> green-lit the cabinet design —
> a stand-up cabinet with a real
> wooden bar-top, a real beer-tap
> handle as the only controller,
> and a tension sensor under the
> handle to measure how hard the
> player was pulling.

> ── PART 2: THE SPONSORSHIP DEAL ──

> Bally Midway also signed a
> sponsorship deal with a major
> American brewery. The deal: the
> brewery's branding would appear
> prominently in the game.
> Customers would order the
> brewery's drink. The mugs
> sliding down the bar would
> carry the brewery's logo.

> This was, in 1983, one of the
> FIRST major product-placement
> deals in arcade game history.

> The game shipped in early 1983
> as the branded version and was
> a hit. Tens of thousands of
> cabinets sold. Players spent
> hours mastering the art of the
> four-bar bartender.

> Then the moral panic arrived.

> ── PART 3: THE MORAL PANIC ──

> Late 1983. American educators,
> child-welfare organisations,
> and various Parent-Teacher
> Associations began organising
> against the "alcoholisation of
> children". They pointed at the
> branded cabinet — sitting in
> arcades beside Pac-Man and
> Galaga, surrounded by
> 11-year-olds with quarters,
> teaching them to be FASTER
> BARTENDERS — and they were
> not amused.

> Bally Midway responded by
> producing a kid-friendly
> re-release: ROOT BEER TAPPER.
> The game was mechanically
> identical. The cabinet art
> was now root-beer-themed.
> The mugs were root-beer mugs.
> The setting was a kid-
> friendly drugstore soda
> fountain.

> 80% of subsequent cabinet
> sales were ROOT BEER TAPPER.

> The whole episode is a small
> piece of arcade history that
> shows: in 1983, the arcade
> industry was negotiating its
> relationship with American
> family values in real time,
> one cabinet at a time.

> ── PART 4: HOW IT PLAYS ──

> Four parallel bars across
> the screen. At the LEFT END
> of each bar, the bartender
> (you) stands at the tap.
> At the RIGHT END of each
> bar, the entry door — where
> customers walk in. Customers
> walk steadily LEFT, advancing
> toward the bartender. You
> must serve them a drink
> BEFORE they reach the
> bartender's end — otherwise
> they grab you (in the
> original cabinet they
> literally pulled you over
> the bar) and you lose a
> life.

> To serve: STAND on the
> appropriate bar (use ▲/▼
> to step between bars), then
> PULL THE TAP (the FIRE
> button) to slide a full mug
> down the bar toward the
> customers. The customer
> nearest the tap end will
> catch the mug, drink it,
> slide the EMPTY MUG back
> toward the tap. You must
> CATCH the empty mug by
> being at the correct bar
> when it arrives.

> If a mug slides off the
> END of the bar uncaught,
> it CRASHES on the floor.
> You lose a life.

> Difficulty escalates: more
> customers, faster, four
> bars worth of simultaneous
> mug-tracking. By level 4
> you are running a four-
> armed bartender ballet.

> ── PART 5: WHAT WE BUILT ──

> Below is SALOON. Our
> tribute. Four bars. A
> western-styled saloon
> (mahogany counters, brass
> handles, dim yellow light)
> rather than the chrome-and-
> red Bally Midway original.
> Patrons are cowboys.
> Bartender is a moustachioed
> figure in a white shirt and
> embroidered waistcoat.

> ▲▼ between bars. ● FIRE to
> pull the tap. Catch empties
> by being at the correct bar.

> No alcohol-warnings here.
> No moral panic. Just the
> ballet of bartending,
> preserved in pixel form.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> TAPPER is a trademark of
> Bally Midway (now Sega). All
> beverage brands referenced in
> the historical narrative are
> trademarks of their respective
> rights-holders. We claim NO
> affiliation. Our tribute is
> called SALOON and depicts a
> generic Western saloon. We do
> not depict alcohol consumption
> by minors, do not endorse
> alcohol, and do not name any
> specific alcoholic brand. The
> mechanic — slide-a-drink-down-
> a-bar, catch-the-empty — is a
> 1983 design by Steve Meyer at
> Marvin Glass & Associates and
> is sufficiently distinct in
> our implementation to be a
> tribute, not a clone.
> Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Здорово, дружище.

> Подтащи табурет. Выпей.
> Разливное — настоящее.
> Рукоятка крана в твоей
> руке — да, эта, блестящий
> рычаг из красного дерева,
> торчащий из панели
> кабинета — настоящая
> рукоятка пивного крана,
> поставленная на датчик
> натяжения, чтобы аркадная
> машина знала, насколько
> сильно ты тянешь. Кабинет
> построен как маленький
> бар. Вокруг тебя пятеро,
> потому что кабинет
> рассчитан на зрителей.

> Это TAPPER. Bally Midway,
> 1983-й. Я — бармен.

> ── ЧАСТЬ 1: КАК ЭТО ПРОИЗОШЛО ──

> Конец 1981-го. Bally
> Midway, тогда крупный
> американский производитель
> аркадных игр, обратился к
> Marvin Glass & Associates
> — чикагскому дизайн-
> бюро, знаменитому
> изобретением настолки
> «Operation» от Hasbro,
> «Mr. Mouth» и «Ants in
> the Pants» — и попросил:
> придумайте нам НОВУЮ
> аркадную игру. Не очередной
> шутер. Не очередную
> погоню по лабиринту.
> Что-то, чего аркады не
> видели.

> У Marvin Glass был
> дизайнер Стив Майер,
> работавший над концептами
> промышленного дизайна.
> Майер вернулся с идеей
> аркадной игры на
> ТАЙМ-МЕНЕДЖМЕНТ.

> Тайм-менеджмент-игр в
> аркадах до 1982-го не
> существовало. Каждая
> аркадная игра была «убей
> врагов» или «пройди
> лабиринт». Питч Майера
> был радикально другим:
> ты — БАРМЕН. Ты подаёшь
> напитки. Ты ловишь
> возвращающиеся кружки.
> Чем быстрее идёшь, тем
> сложнее. Вызов —
> ПРОПУСКНАЯ СПОСОБНОСТЬ, а
> не прицел.

> Bally Midway полюбили.
> Утвердили дизайн кабинета
> — стоячий кабинет с
> настоящей деревянной
> столешницей бара,
> настоящей рукояткой
> пивного крана в качестве
> единственного контроллера
> и датчиком натяжения под
> рукояткой, измеряющим,
> насколько сильно тянет
> игрок.

> ── ЧАСТЬ 2: СПОНСОРСКАЯ СДЕЛКА ──

> Bally Midway также
> подписали спонсорский
> контракт с крупной
> американской пивоварней.
> По договору: брендинг
> пивоварни появится в
> игре заметно. Клиенты
> заказывают напиток
> пивоварни. Кружки,
> скользящие по стойке,
> несут логотип
> пивоварни.

> Это была, в 1983-м,
> одна из ПЕРВЫХ крупных
> сделок product-placement в
> истории аркадных игр.

> Игра вышла в начале
> 1983-го как брендированная
> версия и стала хитом.
> Десятки тысяч кабинетов
> проданы. Игроки часами
> осваивали искусство
> четырёхбарного бармена.

> Потом пришла моральная
> паника.

> ── ЧАСТЬ 3: МОРАЛЬНАЯ ПАНИКА ──

> Конец 1983-го.
> Американские педагоги,
> детские организации и
> разные Parent-Teacher
> Associations начали
> организовываться против
> «алкоголизации детей».
> Они показывали на
> брендированный кабинет —
> стоящий в аркадах рядом с
> Pac-Man и Galaga,
> окружённый 11-летними с
> четвертаками, учащий их
> быть БЫСТРЕЕ БАРМЕНОМ —
> и им это не нравилось.

> Bally Midway ответили,
> выпустив детский ре-релиз:
> ROOT BEER TAPPER. Игра
> механически идентична.
> Кабинетный арт теперь —
> рут-биер. Кружки —
> рут-биер кружки. Сеттинг
> — детский содовый
> фонтан-аптека.

> 80% последующих продаж
> кабинетов были ROOT BEER
> TAPPER.

> Весь эпизод — маленький
> кусочек аркадной истории,
> показывающий: в 1983-м
> аркадная индустрия
> вырабатывала свои
> отношения с американскими
> семейными ценностями в
> реальном времени, по
> одному кабинету за раз.

> ── ЧАСТЬ 4: КАК ИГРАТЬ ──

> Четыре параллельные
> стойки через экран. На
> ЛЕВОМ конце каждой стойки
> стоит бармен (ты) у крана.
> На ПРАВОМ конце каждой —
> входная дверь, откуда
> заходят клиенты. Клиенты
> размеренно идут ВЛЕВО,
> приближаясь к бармену.
> Ты должен подать им
> напиток ДО ТОГО, как они
> дойдут до конца — иначе
> они тебя хватают (в
> оригинальном кабинете они
> буквально перетягивают
> тебя через стойку), и ты
> теряешь жизнь.

> Чтобы подать: СТАНЬ на
> нужную стойку (▲/▼ —
> переход между стойками),
> потом ПОТЯНИ КРАН (FIRE),
> чтобы скользнуть полную
> кружку по стойке к
> клиентам. Ближайший к
> крану клиент поймает
> кружку, выпьет, скользнёт
> ПУСТУЮ КРУЖКУ обратно.
> Ты должен ПОЙМАТЬ пустую
> кружку, стоя на правильной
> стойке, когда она
> прибудет.

> Если кружка скатится с
> конца стойки непойманной
> — она РАЗОБЬЁТСЯ. Жизнь
> минус.

> Сложность растёт: больше
> клиентов, быстрее, четыре
> стойки одновременной
> кружко-слежки. К уровню
> 4 ты управляешь
> четырёхруким балетом
> бармена.

> ── ЧАСТЬ 5: ЧТО МЫ ПОСТРОИЛИ ──

> Ниже — SALOON. Наша
> дань. Четыре стойки.
> Вестерн-стилизованный
> салун (стойки из красного
> дерева, медные ручки,
> тусклый жёлтый свет), а
> не хром-красный
> Bally-Midway оригинал.
> Посетители — ковбои.
> Бармен — усатая фигура в
> белой рубашке и
> вышитой жилетке.

> ▲▼ между стойками. ●
> FIRE — потянуть кран.
> Лови пустые, стоя на
> правильной стойке.

> Никаких
> алкогольных-предупреждений
> здесь. Никакой моральной
> паники. Только балет
> бармена, сохранённый в
> пиксельной форме.

> ── СНОСКА У КОСТРА ──

> TAPPER — торговая марка
> Bally Midway (теперь
> Sega). Все бренды
> напитков, упомянутые в
> историческом нарративе,
> — торговые марки их
> соответствующих
> правообладателей. Мы НЕ
> заявляем никакой связи.
> Наша дань называется
> SALOON и изображает
> обобщённый
> вестерн-салун. Мы не
> изображаем потребление
> алкоголя
> несовершеннолетними, не
> рекламируем алкоголь и
> не называем конкретного
> алкогольного бренда.
> Механика —
> кружка-по-стойке,
> поймай-пустую — дизайн
> 1983-го от Стива Майера
> в Marvin Glass &
> Associates и достаточно
> отличается в нашей
> реализации, чтобы быть
> данью, а не клоном.
> Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val SALOON_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val SALOON_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickSaloonIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> SALOON_INTRO_RU
    else -> SALOON_INTRO_EN
}

val SALOON_INTRO_TEXT: String = SALOON_INTRO_EN
