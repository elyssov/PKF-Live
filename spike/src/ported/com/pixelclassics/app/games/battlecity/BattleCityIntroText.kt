package com.pixelclassics.app.games.battlecity

/**
 * STEEL FRONTIER — long-form campfire intro for our original top-down
 * tank-action homage. Narrator is "Mr Saito" — a fictional retired
 * Akihabara arcade-cabinet repairman who watched the rise and fall and
 * rise of tank-game cabinets through the 1980s, and is now telling a
 * young player about the era over a cup of green tea.
 *
 * Three full localisations (RU / EN / VI) — [pickBattleCityIntro]
 * chooses the right one for the player's selected language.
 *
 * Style: PARCHMENT — warm tea-stained paper, the back room of an
 * arcade in 1985.
 */

private val HEADER: String = """
========================================
 STEEL FRONTIER // COMMAND LINK ONLINE
 ORIGINAL ARCADE CAMPAIGN // 35 SECTORS
 narrated by MR SAITO, Akihabara
========================================
""".trimIndent()

private val FOOTER: String = """

> This is an original game inspired by
> the readable, immediate design
> language of early top-down tank
> arcades. Its title (Steel Frontier),
> vehicles, world, sprites and maps
> are original to The Pixel Campfire.

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> Sit down, young one. The tea is
> still hot. I will tell you about
> the cabinets.

> ── PART 1: THE ARCADE FLOOR ──

> My name is Saito. I was a
> mechanic. Thirty-eight years
> repairing arcade machines in
> Akihabara, second floor, above
> the noodle stand on Chuo
> Street. From 1979 to 2017. The
> oil under my fingernails never
> left me. My wife says it is in
> the cooking now.

> When I started, the arcade
> floor was a sea of vector
> spaceships. Asteroid hunters,
> shoot-the-formation games,
> simple paddles. Then came the
> tank cabinets.

> ── PART 2: THE TANK CABINETS ──

> The tanks were different. They
> moved in four directions,
> grid by grid, slowly enough
> that a child could plan a
> shot. The screens were small.
> The colours were cheap — mostly
> red brick, green forest, blue
> river, grey wall. The sound
> was a single thudding
> "tup-tup-tup" from a piezo
> speaker.

> The cabinets were heavy. We
> wheeled them in on dollies.
> Two boys could not lift one.
> The tubes burned out every
> six months. I changed
> hundreds of them.

> But the children loved them.
> Why? Because every level was
> a small puzzle: where is the
> base, where are the walls,
> where do the enemies come
> from. You learned the map.
> You felt clever.

> ── PART 3: 1985 — THE GREY BOX ──

> In October 1985, the Famicom
> reached America as a grey box
> with a different name. The
> arcade industry held its
> breath. We thought: the
> children will go home, they
> will play on their televisions,
> the cabinets will die.

> Some did. Some did not.

> The tank cabinets, I think,
> died slower than most. Because
> the tank-game feeling — the
> small map, the careful shot,
> the precious base in the
> middle — that feeling
> translated to the cartridge
> very well. Children played at
> home. Children came to the
> arcade to play the four-player
> cabinets that the home
> machines could not match.
> Different rooms. Same love.

> ── PART 4: THE GOLDEN YEARS ──

> Between 1985 and 1992 my shop
> ran twelve cabinets at once.
> I had a queue every Saturday.
> I changed coin-mechanisms
> every Monday morning. The
> shouts of "GET THE BASE" and
> "FRIENDLY FIRE!" still ring
> in my ears.

> The tank-action genre, you
> understand, was the perfect
> arcade format. Quick rounds.
> Readable screens. Clear
> stakes. A child could watch
> for ten seconds and
> understand. A grandmother
> could watch for thirty
> seconds and understand. That
> is rare in a video game.

> ── PART 5: STEEL FRONTIER ──

> The young people at Pixel
> Classics, they came to me
> last year. They said: Saito-
> san, we want to make a tank
> game that feels like the
> cabinets, but on a phone.
> Original. New maps. New
> story. New rules. But the
> feeling — the same.

> I said: good. The feeling
> deserves to live another
> forty years.

> They made Steel Frontier.
> Listen.

> ── PART 6: WHAT YOU WILL FIND ──

> You command the last mobile
> defender. You break masonry.
> You cross drainage lanes.
> You hide under canopy. You
> hold the command core at the
> bottom centre of every map.

> Titanium stops ordinary
> shells. Water stops tanks.
> Ice carries momentum. Marked
> enemies release field
> upgrades — helmet for ten
> seconds of shield, clock to
> freeze the enemy column,
> shovel to reinforce the
> base in steel, star to
> upgrade your gun, bomb to
> clear the field, tank for
> an extra life.

> Thirty-five sectors. Five
> named, thirty procedurally
> seeded. The level editor
> lets you draw your own
> frontier and launch it at
> once.

> ── PART 7: ONE LAST THING ──

> When you play, listen for
> the "tup-tup-tup". The
> grandchildren of that piezo
> sound are still in here.
> Hear them, and you will
> know the cabinets are not
> forgotten.

> Pour yourself the rest of
> the tea. The cabinets are
> waiting.

> — Saito, Akihabara, 2026.
""".trimIndent()

private val BODY_RU: String = """
> Садись, молодой. Чай ещё
> горячий. Я расскажу тебе про
> аркадные шкафы.

> ── ЧАСТЬ 1: ПОЛ АРКАДЫ ──

> Меня зовут Сайто. Я был
> механиком. Тридцать восемь
> лет чинил аркадные автоматы в
> Акихабаре, второй этаж над
> лапшичной на Чуо-стрит. С
> 1979 по 2017. Масло под
> ногтями так и не отмылось.
> Жена говорит, оно теперь в
> еде.

> Когда я начинал, пол аркады
> был морем векторных
> космолётов. Охотники на
> астероиды, расстрел
> формаций, простые ракетки.
> Потом пришли танковые
> кабинеты.

> ── ЧАСТЬ 2: ТАНКОВЫЕ КАБИНЕТЫ ──

> Танки были другие. Двигались
> в четырёх направлениях, по
> клеточкам, медленно — ребёнок
> успевал прицелиться. Экраны
> маленькие. Цвета дешёвые: в
> основном красный кирпич,
> зелёный лес, синяя река,
> серая стена. Звук — одно
> тупое «туп-туп-туп» из
> пьезоизлучателя.

> Кабинеты тяжёлые. Завозили на
> тележках. Два мальчика не
> подымут один. Лампы
> перегорали раз в полгода. Я
> сменил их сотни.

> Но дети их любили. Почему?
> Потому что каждый уровень —
> маленькая головоломка: где
> база, где стены, откуда идут
> враги. Ты заучивал карту.
> Чувствовал себя умным.

> ── ЧАСТЬ 3: 1985 — СЕРАЯ КОРОБКА ──

> В октябре восемьдесят пятого
> Famicom добралась до Америки
> в виде серой коробки с
> другим именем. Аркадная
> индустрия затаила дыхание.
> Мы думали: дети уйдут домой,
> будут играть на телевизорах,
> кабинеты умрут.

> Часть умерла. Часть нет.

> Танковые кабинеты, я думаю,
> умирали медленнее. Потому что
> ощущение танковой игры —
> маленькая карта, точный
> выстрел, дорогая база
> посередине — это ощущение
> очень хорошо легло на
> картридж. Дети играли дома.
> Дети приходили в аркаду
> играть в четырёхместные
> кабинеты, которые домашние
> машины не тянули. Разные
> залы. Одна любовь.

> ── ЧАСТЬ 4: ЗОЛОТЫЕ ГОДЫ ──

> С восемьдесят пятого по
> девяносто второй у меня в
> мастерской работало
> двенадцать кабинетов
> одновременно. Очередь по
> субботам. Монетоприёмники
> менял каждый понедельник
> утром. Крики «ВЗЯТЬ БАЗУ!» и
> «СВОЙ ПО СВОИМ!» до сих пор
> звенят в ушах.

> Жанр танкового экшна,
> понимаешь, был идеальным
> аркадным форматом. Короткие
> раунды. Читаемые экраны.
> Ясные ставки. Ребёнок мог
> смотреть десять секунд и
> понять. Бабушка — тридцать
> секунд и понять. Это редко в
> видеоигре.

> ── ЧАСТЬ 5: STEEL FRONTIER ──

> Молодые ребята из «Пиксельного
> костра» пришли ко мне в
> прошлом году. Сказали:
> Сайто-сан, мы хотим сделать
> танковую игру с ощущением
> кабинетов, но на телефоне.
> Оригинал. Новые карты. Новый
> сюжет. Новые правила. Но
> ощущение — то же.

> Я сказал: хорошо. Это
> ощущение заслужило прожить
> ещё сорок лет.

> Они сделали Steel Frontier.
> Слушай.

> ── ЧАСТЬ 6: ЧТО ТЫ НАЙДЁШЬ ──

> Ты командуешь последним
> мобильным защитником. Ломаешь
> кирпич. Пересекаешь
> водоотводы. Прячешься под
> зелёным навесом. Держишь
> командное ядро внизу в центре
> каждой карты.

> Титан останавливает обычные
> снаряды. Вода — танки. Лёд
> несёт инерцию. Помеченные
> враги выпускают улучшения:
> шлем — десять секунд щита,
> часы — заморозить колонну,
> лопата — обернуть базу в
> сталь, звезда — улучшить
> пушку, бомба — очистить
> поле, танк — одна жизнь.

> Тридцать пять секторов.
> Пять именованных, тридцать
> процедурно засеянных.
> Редактор уровней позволяет
> нарисовать свой фронтир и
> запустить его сразу.

> ── ЧАСТЬ 7: ПОСЛЕДНЕЕ ──

> Когда играешь — слушай
> «туп-туп-туп». Внуки того
> пьезосигнала всё ещё здесь.
> Услышишь их — и поймёшь,
> что кабинеты не забыты.

> Налей себе остаток чая.
> Кабинеты ждут.

> — Сайто, Акихабара, 2026.
""".trimIndent()

private val BODY_VI: String = """
> Ngồi xuống, bạn trẻ. Trà còn
> nóng. Tôi sẽ kể cho bạn về
> những thùng máy arcade.

> ── PHẦN 1: SÀN ARCADE ──

> Tên tôi là Saito. Tôi từng là
> thợ máy. Ba mươi tám năm sửa
> máy arcade ở Akihabara, tầng
> hai trên quán mì trên đường
> Chuo. Từ 1979 đến 2017. Dầu
> dưới móng tay không bao giờ
> sạch. Vợ tôi nói nó đã ngấm
> vào thức ăn.

> Khi tôi mới bắt đầu, sàn
> arcade là biển vector phi
> thuyền. Săn thiên thạch, bắn
> đội hình, vợt đơn giản. Rồi
> đến những thùng máy tăng.

> ── PHẦN 2: NHỮNG THÙNG MÁY TĂNG ──

> Tăng thì khác. Chúng di chuyển
> bốn hướng, từng ô một, chậm
> đến mức một đứa trẻ có thể
> ngắm bắn. Màn hình nhỏ. Màu
> rẻ tiền: chủ yếu là gạch đỏ,
> rừng xanh, sông xanh dương,
> tường xám. Âm thanh là một
> tiếng "tup-tup-tup" đơn điệu
> từ loa piezo.

> Thùng máy nặng. Phải đẩy bằng
> xe lăn. Hai cậu bé không
> nhấc nổi một cái. Bóng đèn
> cháy nửa năm một lần. Tôi đã
> thay hàng trăm cái.

> Nhưng trẻ con yêu chúng. Vì
> sao? Vì mỗi màn là một câu đố
> nhỏ: căn cứ ở đâu, tường ở
> đâu, kẻ thù từ đâu đến. Bạn
> học thuộc bản đồ. Bạn cảm
> thấy mình thông minh.

> ── PHẦN 3: 1985 — HỘP XÁM ──

> Tháng Mười năm 1985, Famicom
> đến Mỹ dưới dạng một hộp xám
> với tên khác. Ngành arcade
> nín thở. Chúng tôi nghĩ: trẻ
> con sẽ về nhà, chúng sẽ chơi
> trên tivi, thùng máy sẽ chết.

> Một số đã chết. Một số không.

> Thùng máy tăng, tôi nghĩ,
> chết chậm hơn. Vì cảm giác
> của trò chơi tăng — bản đồ
> nhỏ, cú bắn cẩn thận, căn cứ
> quý giá ở giữa — cảm giác đó
> chuyển sang băng cartridge
> rất tốt. Trẻ con chơi ở nhà.
> Trẻ con đến arcade chơi
> những thùng bốn người mà máy
> ở nhà không kham nổi. Phòng
> khác nhau. Cùng một tình yêu.

> ── PHẦN 4: NHỮNG NĂM VÀNG ──

> Từ tám mươi lăm đến chín mươi
> hai, xưởng của tôi chạy mười
> hai thùng máy cùng lúc. Xếp
> hàng mỗi thứ Bảy. Tôi thay bộ
> nhận xu mỗi sáng thứ Hai.
> Tiếng hét "LẤY CĂN CỨ!" và
> "BẮN ĐỒNG ĐỘI!" vẫn vang
> trong tai tôi.

> Thể loại tăng-action, bạn
> hiểu không, là định dạng
> arcade hoàn hảo. Vòng ngắn.
> Màn hình dễ đọc. Kết cục rõ
> ràng. Một đứa trẻ xem mười
> giây là hiểu. Một bà cụ xem
> ba mươi giây là hiểu. Điều
> đó hiếm trong một trò chơi
> điện tử.

> ── PHẦN 5: STEEL FRONTIER ──

> Các bạn trẻ ở The Pixel Campfire
> đến tôi năm ngoái. Họ nói:
> Saito-san, chúng tôi muốn
> làm một trò tăng có cảm giác
> như thùng máy, nhưng trên
> điện thoại. Nguyên bản. Bản
> đồ mới. Câu chuyện mới. Luật
> mới. Nhưng cảm giác — vẫn
> vậy.

> Tôi nói: tốt. Cảm giác đó
> xứng đáng sống thêm bốn mươi
> năm nữa.

> Họ làm Steel Frontier. Nghe
> đây.

> ── PHẦN 6: BẠN SẼ TÌM THẤY ──

> Bạn chỉ huy người bảo vệ di
> động cuối cùng. Phá tường
> gạch. Vượt rãnh thoát nước.
> Núp dưới tán cây. Giữ lõi
> chỉ huy ở giữa dưới mỗi bản
> đồ.

> Titan chặn đạn thường. Nước
> chặn tăng. Băng giữ đà. Kẻ
> thù được đánh dấu thả nâng
> cấp chiến trường: mũ bảo
> hiểm — mười giây khiên, đồng
> hồ — đóng băng cột địch,
> xẻng — bọc căn cứ bằng thép,
> sao — nâng cấp pháo, bom —
> dọn sạch trận địa, tăng —
> một mạng.

> Ba mươi lăm khu vực. Năm có
> tên, ba mươi tạo ngẫu nhiên.
> Trình chỉnh sửa cấp độ cho
> phép bạn vẽ chiến tuyến của
> mình và bắt đầu ngay.

> ── PHẦN 7: ĐIỀU CUỐI ──

> Khi bạn chơi — nghe tiếng
> "tup-tup-tup". Cháu chắt của
> tiếng piezo đó vẫn còn đây.
> Nghe được — bạn sẽ biết
> những thùng máy chưa bị lãng
> quên.

> Tự rót cho mình chỗ trà còn
> lại. Thùng máy đang chờ.

> — Saito, Akihabara, 2026.
""".trimIndent()

val BATTLE_CITY_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER
val BATTLE_CITY_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val BATTLE_CITY_INTRO_VI: String = HEADER + "\n" + BODY_VI + "\n" + FOOTER

fun pickBattleCityIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> BATTLE_CITY_INTRO_RU
    "vi" -> BATTLE_CITY_INTRO_VI
    "en" -> BATTLE_CITY_INTRO_EN
    else -> BATTLE_CITY_INTRO_EN
}

/** Back-compat alias for any code still referring to the old single text. */
val BATTLE_CITY_INTRO_TEXT: String = BATTLE_CITY_INTRO_EN
