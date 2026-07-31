package com.pixelclassics.app.games.superbarsik

/**
 * Long-form campfire intro for SUPER БАРСИК BROS — an open homage to the
 * 1985 NES platformer that single-handedly revived the home-console
 * market after the Atari crash of 1983.
 *
 * The narrator is "Антонио" — a fictional Italian-American plumber from
 * Brooklyn, telling his own life story in the voice of a tired
 * grandfather over a pasta plate in Queens. He refuses to say his
 * own name (lawyers from Kyoto, you understand) but every detail of
 * his biography is a wink the player will recognise. The handoff at
 * the end passes the torch to Барсик, our small red cat with big
 * ears, who will live the same arc again — this time in fur.
 *
 * Three full localisations (RU / EN / VI) — [pickSuperBarsikIntro]
 * chooses the right one based on the player's selected language,
 * with the trilingual Pixel Campfire disclaimer appended to every
 * version.
 *
 * Style: PARCHMENT — the tablecloth of a Brooklyn pizzeria over which
 * forty years of nostalgia are recounted.
 */

private val DISCLAIMER_BLOCK: String = """

> ████████████████████████████████████████
> █        THE PIXEL CAMPFIRE             █
> █  не от Антонио — от нас, авторов:        █
> ████████████████████████████████████████

> ── РУССКИЙ ──

> Да, это буквальный оммаж на SUPER
> MARIO BROS. (Nintendo, 1985). Мы это
> не скрываем — мы это празднуем.

> Эта игра имеет полное право
> находиться в нашем сборнике ретро-
> классики. Потому что SMB — это
> великая игра. Игра, которая в
> октябре 1985-го одна вернула
> индустрию домашних приставок к
> жизни после краха 1983-го. Без неё
> не было бы ни нашей коллекции, ни
> вообще того, что мы называем
> «домашняя видеоигра».

> Суть «Пиксельного костра» — не
> копирование классики, а передача
> вайба. Чувства картриджа в
> маленькой ладошке. Чувства
> витрины на углу в октябре
> восемьдесят пятого. Чувства
> одного прыжка, который изменил
> всё.

> Если у Nintendo с нашим знаком
> внимания возникнут вопросы — мы
> немедленно уберём любые
> упоминания. Никаких судов,
> никаких прений. Уберём и
> поклонимся.

> Но честно, амичи: Nintendo от
> нашего знака внимания не
> обеднеет. А наша коллекция без
> этого оммажа — была бы неполной.
> Великая игра заслуживает того,
> чтобы её помнили.

> Спасибо, маэстро Миямото. Из ямы
> в Аламогордо — к котику.

> ── ENGLISH ──

> Yes, this is a literal homage to
> SUPER MARIO BROS. (Nintendo, 1985).
> We do not hide it — we celebrate it.

> This game has every right to sit
> in our retro-classics collection.
> Because SMB is a great game. The
> game that, in October 1985, single-
> handedly brought the home-console
> industry back to life after the
> 1983 crash. Without it there would
> be no collection of ours, nor
> anything we now call a "home video
> game".

> The point of The Pixel Campfire is
> not to copy the classics but to
> pass on the vibe. The feel of a
> cartridge in a small palm. The
> feel of a shop window on the corner
> in October '85. The feel of one
> jump that changed everything.

> If Nintendo takes issue with our
> tip of the hat — we will remove
> any reference at once. No courts,
> no arguments. We will remove and
> bow.

> But honestly, amici: Nintendo
> will not be the poorer for our
> nod. And our collection without
> this homage would be incomplete.
> A great game deserves to be
> remembered.

> Thank you, maestro Miyamoto. From
> the pit at Alamogordo — to the
> kitten.

> ── TIẾNG VIỆT ──

> Vâng, đây là một sự tri ân trực
> tiếp dành cho SUPER MARIO BROS.
> (Nintendo, 1985). Chúng tôi không
> giấu giếm điều đó — chúng tôi ăn
> mừng nó.

> Trò chơi này hoàn toàn xứng đáng
> có mặt trong bộ sưu tập cổ điển
> retro của chúng tôi. Bởi vì SMB
> là một trò chơi vĩ đại. Trò chơi
> mà, vào tháng Mười năm 1985, một
> mình đã hồi sinh ngành công
> nghiệp máy chơi game tại gia sau
> cuộc sụp đổ năm 1983. Không có nó
> thì cũng không có bộ sưu tập của
> chúng tôi, cũng không có thứ
> chúng ta gọi là "trò chơi điện
> tử tại nhà".

> Mục đích của The Pixel Campfire
> không phải sao chép các tác phẩm
> cổ điển, mà là truyền tải tinh
> thần ấy. Cảm giác của một băng
> cartridge nằm gọn trong lòng bàn
> tay nhỏ. Cảm giác của ô cửa kính
> cửa hàng nơi góc phố vào tháng
> Mười năm 1985. Cảm giác của một
> cú nhảy đã thay đổi mọi thứ.

> Nếu Nintendo có ý kiến về dấu
> hiệu tôn kính của chúng tôi —
> chúng tôi sẽ ngay lập tức gỡ bỏ
> mọi nhắc đến. Không kiện tụng,
> không tranh luận. Gỡ bỏ và cúi
> đầu.

> Nhưng nói thật, amici: Nintendo
> sẽ không nghèo đi vì lời tri ân
> nhỏ bé của chúng tôi. Còn bộ sưu
> tập của chúng tôi nếu thiếu lời
> tri ân này — sẽ không trọn vẹn.
> Một trò chơi vĩ đại xứng đáng
> được ghi nhớ.

> Cảm ơn, maestro Miyamoto. Từ cái
> hố ở Alamogordo — đến chú mèo con.

> ──────────────────────────────

> The Pixel Campfire, 2026.
""".trimIndent()

private val INTRO_HEADER: String = """
████████████████████████████████████████
█  SUPER БАРСИК BROS.                  █
█  pixel-classics homage edition       █
█  recounted by ANTONIO, plumber       █
████████████████████████████████████████
""".trimIndent()

private val INTRO_RU_BODY: String = """
> Я тебе расскажу одну историю,
> амико. Мою. Лично. С самого
> начала и до сегодняшнего дня.
> Но!

> Имя моё ты услышать не должен.
> Ни от меня. Ни от моего брата.
> Ни от моей принцессы. Ни даже
> от Дженнаро, который меня знал
> ещё в коротких штанишках. Потому
> что если ты услышишь моё имя —
> приедет Большая Эн на автобусе с
> юристами из Киото, и эти юристы
> скажут «синьор, мы вас
> закрываем», и я не смогу даже
> доесть мою пасту. А я её недоел,
> ма́мма мия. Половина тарелки
> осталась. С тефтелями.

> Так что зови меня — Антонио.
> Это не моё имя. Но звучит
> правильно.

> ── ЧАСТЬ 1: БРУКЛИНСКАЯ ДИНАСТИЯ ──

> Я родился в Бруклине, амико. В
> семидесятых. Семья из Тосканы —
> мама, папа, бабушка Роза, и мой
> младший брат.

> Брат у меня — он повыше меня.
> Худенький такой. Любит зелёное
> всё. Зелёная рубашка, зелёная
> кепка, даже носки зелёные. Я
> ношу красное, он зелёное. Я с
> детства толстый, он с детства
> худой. Я с детства громкий, он
> с детства застенчивый. Но
> прыгает выше, мерзавец. Это
> нечестно, но я смирился.

> Мы оба работали с самого
> детства — водопроводчиками.
> Си, амико! Это семейная
> профессия. Дед мой чинил трубы
> в Сиене. Папа чинил трубы в
> Бруклине. Я чиню трубы. Брат
> мой чинит трубы. Шесть
> поколений мокрых рук и
> ругательств на полудюжине
> диалектов.

> ── ЧАСТЬ 2: ЧЕРЕПАХА В ПОДВАЛЕ ──

> В восемьдесят первом мне было
> лет двадцать с чем-то. Молодой,
> тощий, с усами как у папы.
> Чинил унитазы в Куинсе.
> Однажды — клянусь Девой Марией
> Куинсской — я полез в подвал
> чинить лопнувшую трубу и нашёл
> там черепаху. Большую, с
> панцирем как корыто. И эта
> черепаха меня укусила.

> И с того дня я могу прыгать на
> черепах. Что-то итало-
> черепашье. Не объясняется. Но
> работает.

> ── ЧАСТЬ 3: КОНЕЦ ЭПОХИ ──

> В том же восемьдесят первом —
> пока я учился прыгать на
> черепах — в Калифорнии умирала
> Atari. Чёрная коробка, у
> каждого второго дома была.
> Игра про инопланетянина вышла
> такая плохая, что компания
> похоронила двенадцать
> грузовиков картриджей в
> пустыне Аламогордо. Под
> бетоном. Чтобы никто не нашёл.

> И вся индустрия упала, амико.
> Магазины убрали полки. Дети
> забыли. Все взрослые сказали:
> «домашние приставки — это была
> ошибка. Закопано. Конец.» И
> два года была тишина.

> ── ЧАСТЬ 4: МОЯ ПРИНЦЕССА ──

> А у меня в это время была
> невеста, белиссимо. Невеста
> была — принцесса. В розовом
> платье. Розовое было её цветом.
> Розовая корона, розовые
> перчатки, розовые туфельки.
> Когда я встретил эту принцессу
> в розовом платье в восемьдесят
> первом году в булочной на
> Бруклин-авеню, я понял: я буду
> чинить трубы вечно, лишь бы её
> розовое платье было каждый
> вечер на нашей кухне.

> ── ЧАСТЬ 5: СЕРАЯ КОРОБКА ──

> И вот, восемьдесят четвёртый.
> Японцы — благослови их Дева
> Мария Киотская — пришли ко мне.
> Прямо в Куинс. К пиццерии
> Дженнаро. С предложением.

> «Антонио (они не сказали
> Антонио, но условно), мы делаем
> серую коробку. В коробке будет
> картридж. На картридже — ты.
> Ты будешь бегать. Прыгать.
> Толкать кирпичики. Есть грибы
> — после грибов станешь вдвое
> больше.»

> Я сказал: «У меня и так пышные
> формы.» Они: «Ещё пышнее.»

> «Есть цветочки — после цветочка
> будешь плеваться огнём.»

> Я: «У меня от острого изжога.»
> Они: «Будет смотреться красиво.»

> «Прыгать на маленьких
> коричневых существ.»

> Я: «Это негуманно.» Они: «Это
> весело.»

> Я согласился, амико. Потому
> что у меня была принцесса.

> ── ЧАСТЬ 6: ОКТЯБРЬ 1985 ──

> Манхэттен. Универмаг на углу.
> Витрина светится. В витрине —
> я. На картридже. В синем
> комбинезончике, в красной
> кепочке. Я бегу. Я прыгаю. Я
> ем грибы и становлюсь большой.
> Прыгаю в зелёную трубу (моя
> профессия!) и оказываюсь под
> землёй. Собираю золотые
> кругляшки. Добираюсь до флага и
> поднимаю его. Захожу в замок.
> В замке — огромный шипастый
> дракон. Я его побеждаю. И вижу
> — записку.

> «Не та крепость, синьор. Иди
> дальше.»

> Восемь миров. Восемь замков. И
> только в восьмом — она. В
> розовом платье. На троне. И мы
> воссоединяемся.

> ── ЧАСТЬ 7: ВОЗРОЖДЕНИЕ ──

> К Рождеству восемьдесят пятого
> серую коробку купили все.
> Каждый папа. Каждая мама.
> Каждая бабушка. Я был лицом
> возрождения индустрии. Один
> прыжок маленького толстого
> итальянского водопроводчика
> через одну черепаху — и сорок
> лет потом каждая Sega, каждая
> PlayStation, каждый Xbox,
> каждый Switch, каждый Steam
> Deck стоят на моих красных
> подошвах.

> Сорок лет, амико. Сорок!

> ── ЧАСТЬ 8: ПЕРЕДАЧА ЭСТАФЕТЫ ──

> Я не могу позволить тебе
> поиграть в меня. Лично. Большая
> Эн не позволит.

> Но я хочу, чтобы ты понял то
> чувство. Прыжок. Трубу. Грибы.
> Кирпичики. Замок. Принцессу.
> Возрождение.

> Поэтому я познакомлю тебя с
> моим другом. Не итальянец, нет
> — русский. Маленький такой.
> Рыжий. Уши большие. Кот.
> Зовут — Барсик. Сын дворового
> кота с Брайтон-Бич.

> У него тоже есть любимая —
> кошка Муська, сидит на балконе
> напротив. Только она не в
> розовом платье — она в
> собственной шёрстке. И к ней
> нельзя со словами — к ней
> можно только с Вискасом. Целую
> тележку.

> Он будет прыгать, как я. Через
> тапки вместо моих коричневых
> существ. Через крыс в
> кастрюлях вместо моих
> черепашек. От ворон будет
> уворачиваться. Будет пить
> сливочное молоко и становиться
> большой. Будет ловить мышку и
> стрелять тапками. Из труб
> будут вылазить рукавички для
> вычесывания — он будет их
> прыгать на голову, как я
> прыгал на черепах.

> Он будет добираться до сардины
> на шесте в конце каждого
> уровня. Будет открывать
> холодильник в конце мира. И в
> каждом холодильнике — записка:
> «твой Вискас в другом
> холодильнике, Барсик.» И
> только в финале, после Кухни,
> Двора, Крыши, Подвала и
> Чердака — настоящий Вискас. И
> Муська. И финальный кадр с
> тележкой между двумя котами
> на крыше под луной.

> ── ЧАСТЬ 9: ПРЫГАЙ ──

> Из ямы в Аламогордо. Через
> мою серую коробку. К сардине
> на шесте. К Вискасу. К Муське
> на крыше.

> Возрождение, амико.
> Возрождение дважды. Один раз
> я возродил индустрию. Второй
> раз индустрию поприветствует —
> котик.

> Поехали, бамбино. Я обещал
> не называть имени. Но я
> здесь. Я тебя благословляю.
> Прыгай.

> Ма́мма мия. Один прыжок.

> На этот раз — с усами и в
> шёрстке.
""".trimIndent()

private val INTRO_EN_BODY: String = """
> Pull up a chair, amico. Put down
> your plate. I'll tell you a story.
> Mine. Personal. From the very
> beginning to today. But!

> My name — you must not hear it.
> Not from me. Not from my brother.
> Not from my princess. Not even
> from Gennaro, who knew me in
> short pants. Because if you hear
> my name — Big N comes on a bus
> with lawyers from Kyoto, and the
> lawyers will say "signore, we are
> closing you down", and I will
> not even finish my pasta. And I
> have not finished, mamma mia.
> Half a plate left. With meatballs.

> So call me — Antonio. It is not
> my name. But it sounds right.

> ── PART 1: BROOKLYN DYNASTY ──

> I was born in Brooklyn, amico. In
> the seventies. Family from Tuscany
> — mamma, papà, nonna Rosa, and
> my little brother.

> My brother — he is taller than me.
> Skinny like a stick. Loves green,
> everything green. Green shirt,
> green cap, even green socks. I
> wear red, he wears green. I have
> been chubby from childhood, he
> has been thin. I have been loud,
> he has been shy. But he jumps
> higher, the brat. It is not fair,
> but I have made my peace.

> We both worked from childhood as
> plumbers. Si, amico! It is a
> family profession. My grandfather
> fixed pipes in Siena. My father
> fixed pipes in Brooklyn. I fix
> pipes. My brother fixes pipes.
> Six generations of wet hands and
> swearing in half a dozen dialects.

> ── PART 2: THE TURTLE IN THE BASEMENT ──

> In nineteen eighty-one I was
> twenty-something. Young, skinny,
> with a moustache like papà. I
> fixed toilets in Queens. One day
> — I swear by the Madonna of
> Queens — I crawled into a
> basement to fix a burst pipe and
> I found a turtle there. Large, a
> shell like a wash-tub. And that
> turtle bit me.

> And from that day I can jump on
> turtles. Something Italo-turtle.
> It does not explain. But it works.

> ── PART 3: THE END OF AN ERA ──

> In that same eighty-one — while I
> was learning to jump on turtles —
> in California, Atari was dying.
> The black box, every other house
> had one. The game about an
> extraterrestrial came out so
> badly that the company buried
> twelve truckloads of cartridges
> in the New Mexico desert at
> Alamogordo. Under concrete. So
> nobody would find them.

> And the whole industry fell down,
> amico. The toy stores took down
> the shelves. The children forgot.
> The grown-ups said: "home consoles
> were a mistake. Buried. The end."
> And there were two years of
> silence.

> ── PART 4: MY PRINCESS ──

> And in that time I had a fiancée,
> bellissimo. The fiancée was — a
> princess. In a pink dress. Pink
> was her colour. Pink crown, pink
> gloves, pink slippers. When I
> met that princess in the pink
> dress in eighty-one in a bakery
> on Brooklyn Avenue, I understood:
> I will fix pipes forever, as
> long as her pink dress is on our
> kitchen every evening.

> ── PART 5: THE GREY BOX ──

> Then, eighty-four. The Japanese —
> bless them, Madonna of Kyoto —
> came to me. Right to Queens. To
> Gennaro's pizzeria. With an offer.

> "Antonio (they did not call me
> Antonio, but let's say) — we
> are making a grey box. In the box
> there will be a cartridge. On
> the cartridge — you. You will
> run. Jump. Knock the bricks. Eat
> mushrooms — after mushrooms you
> become twice as big."

> I said: "I am already amply
> proportioned." They: "More
> amply."

> "Eat a flower — after a flower
> you spit fire."

> Me: "Spicy gives me heartburn."
> Them: "It will look beautiful."

> "Jump on small brown creatures."

> Me: "That is inhumane." Them:
> "It is fun."

> I agreed, amico. Because I had
> the princess.

> ── PART 6: OCTOBER 1985 ──

> Manhattan. The department store
> on the corner. The window glows.
> In the window — me. On a
> cartridge. In a blue overall, in
> a red little cap. I run. I jump.
> I eat mushrooms and grow big. I
> jump into a green pipe (my
> profession!) and come out
> underground. I collect golden
> coins. I reach the flag and
> raise it. I enter the castle. In
> the castle — a huge spiked
> dragon. I defeat him. And I see
> — a note.

> "Wrong fortress, signore. Go on."

> Eight worlds. Eight castles. And
> only in the eighth — her. In the
> pink dress. On the throne. And
> we are reunited.

> ── PART 7: REBIRTH ──

> By Christmas eighty-five
> everyone bought the grey box.
> Every papa. Every mamma. Every
> nonna. I watched the sales
> charts and I cried. I was the
> face of an industry's rebirth.
> One jump of a chubby little
> Italian plumber over one turtle
> — and forty years later every
> Sega, every PlayStation, every
> Xbox, every Switch, every Steam
> Deck stands on my red soles.

> Forty years, amico. Forty!

> ── PART 8: PASSING THE TORCH ──

> I cannot let you play as me.
> Personally. Big N will not allow
> it.

> But I want you to understand the
> feeling. The jump. The pipe. The
> mushrooms. The bricks. The
> castle. The princess. The
> rebirth.

> So I will introduce you to my
> friend. Not Italian, no —
> Russian. A small one. Ginger.
> Big ears. A cat. His name is
> Барсик (Barsik). Son of a yard
> cat from Brighton Beach.

> He has a loved one too — a she-
> cat called Muska, who sits on
> the balcony across the way. Only
> she is not in a pink dress —
> she is in her own fur. And you
> cannot reach her with words —
> you can only reach her with
> Whiskas. A whole cart of it.

> He will jump, like I jumped.
> Over slippers instead of my
> brown creatures. Over rats in
> saucepans instead of my turtles
> in shells. He will dodge crows
> instead of my eagles. He will
> drink cream-milk and grow big.
> He will catch a mouse and shoot
> slippers. From the pipes will
> climb out grooming-gloves with
> stiff bristles — he will jump
> on their heads, like I jumped
> on turtles.

> He will reach the sardine on
> the pole at the end of every
> stage. He will open the
> refrigerator at the end of every
> world. And in every refrigerator
> — a note: "your Whiskas is in
> another refrigerator, Barsik."
> And only in the finale, after
> the Kitchen, the Yard, the Roof,
> the Cellar and the Attic — the
> real Whiskas. And Muska. And
> the final shot with a cart
> between two cats on a rooftop
> under the moon.

> ── PART 9: JUMP ──

> From the pit at Alamogordo.
> Through my grey box. To the
> sardine on the pole. To the
> Whiskas. To Muska on the roof.

> A rebirth, amico. A rebirth
> twice. Once I revived the
> industry. The second time the
> industry will be greeted by — a
> kitten.

> Andiamo, bambino. I promised
> not to say my name. But I am
> here. I bless you. Jump.

> Mamma mia. One jump.

> This time — with whiskers and
> in fur.
""".trimIndent()

private val INTRO_VI_BODY: String = """
> Kéo ghế lại, amico. Đặt đĩa
> xuống. Tôi kể cho bạn một câu
> chuyện. Của tôi. Riêng tư. Từ
> đầu đến hôm nay. Nhưng!

> Tên tôi — bạn không được nghe.
> Không phải từ tôi. Không phải
> từ em trai tôi. Không phải từ
> công chúa của tôi. Cũng không
> phải từ Gennaro, người biết tôi
> từ hồi quần ngắn. Bởi vì nếu
> bạn nghe tên tôi — Big N sẽ đến
> trên xe buýt với luật sư từ
> Kyoto, và các luật sư sẽ nói
> "signore, chúng tôi đóng cửa
> ông", và tôi sẽ không kịp ăn
> nốt đĩa mì. Mà tôi chưa ăn hết,
> mamma mia. Còn nửa đĩa. Có thịt
> viên.

> Vậy cứ gọi tôi là — Antonio.
> Đó không phải tên tôi. Nhưng
> nghe hợp.

> ── PHẦN 1: GIA TỘC BROOKLYN ──

> Tôi sinh ra ở Brooklyn, amico.
> Vào những năm bảy mươi. Gia
> đình gốc Tuscany — mẹ, bố, bà
> Rosa, và em trai tôi.

> Em trai tôi — cao hơn tôi. Gầy
> như que. Thích màu xanh, mọi
> thứ xanh. Áo xanh, mũ xanh,
> tất cũng xanh. Tôi mặc đỏ, nó
> mặc xanh. Tôi mập từ nhỏ, nó
> gầy từ nhỏ. Tôi ồn ào, nó rụt
> rè. Nhưng nó nhảy cao hơn tôi,
> đồ ranh con. Không công bằng,
> nhưng tôi chấp nhận.

> Cả hai chúng tôi làm thợ ống
> nước từ nhỏ. Si, amico! Đây là
> nghề gia truyền. Ông tôi sửa
> ống ở Siena. Bố tôi sửa ống ở
> Brooklyn. Tôi sửa ống. Em tôi
> sửa ống. Sáu thế hệ tay ướt và
> chửi thề bằng nửa tá phương
> ngữ.

> ── PHẦN 2: CON RÙA TRONG HẦM ──

> Năm tám mươi mốt tôi khoảng
> hai mươi. Trẻ, gầy, ria mép
> như bố. Sửa bồn cầu ở Queens.
> Một hôm — thề với Đức Mẹ
> Queens — tôi chui vào hầm sửa
> ống vỡ và tìm thấy ở đó một
> con rùa. To, mai như cái thùng.
> Và con rùa đó cắn tôi.

> Và từ ngày đó tôi có thể nhảy
> lên rùa. Cái gì đó kiểu Ý-rùa.
> Không giải thích được. Nhưng
> nó hoạt động.

> ── PHẦN 3: KẾT THÚC MỘT THỜI ĐẠI ──

> Cũng trong năm tám mươi mốt
> đó — trong khi tôi học nhảy
> lên rùa — ở California, Atari
> đang chết. Hộp đen, nhà nào
> cũng có. Trò chơi về người
> ngoài hành tinh ra dở đến nỗi
> công ty chôn mười hai xe tải
> băng cartridge ở sa mạc New
> Mexico tại Alamogordo. Dưới bê
> tông. Để không ai tìm thấy.

> Và cả ngành công nghiệp sụp đổ,
> amico. Cửa hàng đồ chơi dỡ kệ.
> Trẻ con quên. Người lớn nói:
> "máy chơi game tại gia là sai
> lầm. Chôn rồi. Hết." Và hai
> năm im lặng.

> ── PHẦN 4: CÔNG CHÚA CỦA TÔI ──

> Thời gian đó tôi có một vị hôn
> thê, bellissimo. Vị hôn thê là
> — một công chúa. Mặc váy hồng.
> Màu hồng là màu của cô. Vương
> miện hồng, găng tay hồng, giày
> hồng. Khi tôi gặp công chúa
> mặc váy hồng đó tại tiệm bánh
> trên Đại lộ Brooklyn năm tám
> mươi mốt, tôi hiểu: tôi sẽ sửa
> ống mãi mãi, miễn là váy hồng
> của cô ở trên bàn ăn của chúng
> tôi mỗi tối.

> ── PHẦN 5: HỘP MÀU XÁM ──

> Năm tám mươi tư. Người Nhật —
> chúc lành bởi Đức Mẹ Kyoto —
> đến tôi. Ngay tại Queens. Tại
> tiệm pizza của Gennaro. Với
> một đề nghị.

> "Antonio (họ không gọi là
> Antonio, nhưng tạm thế) — chúng
> tôi làm hộp màu xám. Trong hộp
> sẽ có băng cartridge. Trên
> cartridge — là anh. Anh sẽ
> chạy. Nhảy. Đập gạch. Ăn nấm —
> sau nấm anh sẽ to gấp đôi."

> Tôi nói: "Tôi đã rộng rãi sẵn
> rồi." Họ: "Rộng hơn nữa."

> "Ăn bông hoa — sau hoa anh sẽ
> phun lửa."

> Tôi: "Cay làm tôi ợ chua." Họ:
> "Sẽ trông đẹp."

> "Nhảy lên các sinh vật nhỏ
> màu nâu."

> Tôi: "Tàn nhẫn." Họ: "Vui mà."

> Tôi đồng ý, amico. Vì tôi có
> công chúa.

> ── PHẦN 6: THÁNG MƯỜI 1985 ──

> Manhattan. Cửa hàng bách hóa ở
> góc phố. Ô cửa kính sáng lên.
> Trong cửa kính — là tôi. Trên
> cartridge. Trong bộ quần áo
> xanh, mũ đỏ nhỏ. Tôi chạy. Tôi
> nhảy. Tôi ăn nấm và to lên.
> Tôi nhảy vào ống xanh (nghề
> của tôi!) và lọt ra dưới đất.
> Tôi nhặt đồng vàng. Tôi đến lá
> cờ và kéo lên. Tôi vào lâu
> đài. Trong lâu đài — một con
> rồng có gai khổng lồ. Tôi đánh
> bại nó. Và tôi thấy — một mẩu
> giấy.

> "Sai pháo đài, signore. Đi
> tiếp."

> Tám thế giới. Tám lâu đài. Và
> chỉ ở cái thứ tám — là cô. Mặc
> váy hồng. Trên ngai. Và chúng
> tôi đoàn tụ.

> ── PHẦN 7: HỒI SINH ──

> Đến Giáng sinh tám mươi lăm
> mọi người mua hộp màu xám. Mỗi
> papa. Mỗi mamma. Mỗi nonna.
> Tôi xem bảng xếp hạng doanh số
> và khóc. Tôi là khuôn mặt của
> sự hồi sinh ngành công nghiệp.
> Một cú nhảy của một thợ ống
> nước Ý nhỏ và mập qua một con
> rùa — và bốn mươi năm sau mỗi
> Sega, mỗi PlayStation, mỗi
> Xbox, mỗi Switch, mỗi Steam
> Deck đứng trên đế đỏ của tôi.

> Bốn mươi năm, amico. Bốn mươi!

> ── PHẦN 8: TRAO ĐUỐC ──

> Tôi không thể cho phép bạn
> chơi với chính tôi. Big N sẽ
> không cho.

> Nhưng tôi muốn bạn hiểu cảm
> giác đó. Cú nhảy. Cái ống.
> Nấm. Gạch. Lâu đài. Công chúa.
> Sự hồi sinh.

> Vì vậy tôi sẽ giới thiệu bạn
> với bạn của tôi. Không phải Ý,
> không — Nga. Nhỏ con. Lông
> hung. Tai to. Một chú mèo. Tên
> là Барсик (Barsik). Con của
> một chú mèo sân ở Brighton
> Beach.

> Cậu ấy cũng có người yêu — một
> cô mèo tên Muska, ngồi trên
> ban công đối diện. Chỉ là cô
> không mặc váy hồng — cô mặc bộ
> lông của mình. Và không thể đến
> với cô bằng lời — chỉ có thể
> đến với cô bằng Whiskas. Cả
> một xe đẩy.

> Cậu ấy sẽ nhảy, như tôi đã
> nhảy. Qua những đôi dép thay
> vì các sinh vật nâu của tôi.
> Qua chuột trong nồi thay vì
> rùa trong mai. Tránh quạ thay
> vì đại bàng. Uống kem sữa và
> to lên. Bắt chuột và bắn dép.
> Từ ống sẽ chui ra găng chải
> lông cứng — cậu sẽ nhảy lên
> đầu chúng, như tôi nhảy lên
> rùa.

> Cậu ấy sẽ đến cá mòi trên cột
> ở cuối mỗi màn. Sẽ mở tủ lạnh
> ở cuối mỗi thế giới. Và trong
> mỗi tủ lạnh — một mẩu giấy:
> "Whiskas của em ở tủ lạnh
> khác, Barsik." Và chỉ ở trận
> cuối, sau Bếp, Sân, Mái nhà,
> Hầm và Gác mái — Whiskas thật.
> Và Muska. Và khung cảnh cuối
> với chiếc xe đẩy giữa hai con
> mèo trên mái nhà dưới ánh
> trăng.

> ── PHẦN 9: NHẢY ĐI ──

> Từ cái hố ở Alamogordo. Qua
> hộp xám của tôi. Đến cá mòi
> trên cột. Đến Whiskas. Đến
> Muska trên mái.

> Hồi sinh, amico. Hồi sinh hai
> lần. Một lần tôi hồi sinh
> ngành công nghiệp. Lần thứ hai
> ngành sẽ được chào đón bởi —
> một con mèo.

> Andiamo, bambino. Tôi đã hứa
> không nói tên tôi. Nhưng tôi
> ở đây. Tôi chúc phúc cho bạn.
> Nhảy đi.

> Mamma mia. Một cú nhảy.

> Lần này — với ria mép và bộ
> lông.
""".trimIndent()

val SUPER_BARSIK_INTRO_RU: String = INTRO_HEADER + "\n" + INTRO_RU_BODY + "\n" + DISCLAIMER_BLOCK
val SUPER_BARSIK_INTRO_EN: String = INTRO_HEADER + "\n" + INTRO_EN_BODY + "\n" + DISCLAIMER_BLOCK
val SUPER_BARSIK_INTRO_VI: String = INTRO_HEADER + "\n" + INTRO_VI_BODY + "\n" + DISCLAIMER_BLOCK

/** Pick the right narrator block for the player's selected language. */
fun pickSuperBarsikIntro(lang: String): String = when (lang.lowercase()) {
    "en" -> SUPER_BARSIK_INTRO_EN
    "vi" -> SUPER_BARSIK_INTRO_VI
    "ru" -> SUPER_BARSIK_INTRO_RU
    else -> SUPER_BARSIK_INTRO_RU
}

/** Back-compat alias for any code still referring to the old single text. */
val SUPER_BARSIK_INTRO_TEXT: String = SUPER_BARSIK_INTRO_RU
