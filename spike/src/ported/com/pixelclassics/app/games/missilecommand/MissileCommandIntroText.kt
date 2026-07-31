package com.pixelclassics.app.games.missilecommand

/**
 * Long-form campfire intro for Missile Defense — narrated by Dave Theurer,
 * the designer of the 1980 Atari arcade Missile Command, in the dry, sad
 * voice of a man who later admitted in interviews that designing this
 * game gave him nightmares for over a decade.
 *
 * Bilingual: EN + RU. VI pending.
 */

private val HEADER: String = """
████████████████████████████████████████
█   ATARI · MISSILE COMMAND · 1980     █
█   AUTHOR'S NOTE · DAVE THEURER       █
█   RECORDED ~2010, SUNNYVALE, CA      █
████████████████████████████████████████
""".trimIndent()

private val FOOTER: String = """

> _

[ CLICK TO BEGIN ]
""".trimIndent()

private val BODY_EN: String = """
> I was 27 years old in 1979 when I
> joined Atari's arcade division. They
> needed a new title to compete with
> Cinematronics' Space War cabinet.
> Lyle Rains, my supervisor, handed
> me a newspaper.

> The newspaper had an article about
> the United States' new Ballistic
> Missile Early Warning System — BMEWS.
> Rains pointed at the radar diagram
> in the article — a map of the
> United States with dotted lines
> showing intercontinental ballistic
> missile trajectories arcing over
> the North Pole from the Soviet
> Union — and he said:

>   "Make a game out of THAT."

> I made it. I called it MISSILE
> COMMAND. It is the only game I
> ever made that I would gladly
> un-make.

> ── PART 1: WHAT IT DEPICTED ──

> Six cities. Three anti-missile
> batteries. ICBMs raining down from
> the top of the screen, leaving
> dotted trails as they descended.
> The player moved a crosshair with
> a trackball — a real, weighty,
> heavy-balled trackball that came
> from radar consoles — and fired
> counter-missiles by hitting one of
> three buttons (one per battery).

> Each counter-missile travelled
> upward to the crosshair's last
> position, then detonated in a slowly-
> expanding nuclear explosion. If an
> incoming ICBM happened to pass
> through your explosion's blast
> radius during the seconds it was
> still expanding, the ICBM detonated
> too — and your city was saved for
> that volley.

> If an ICBM reached the ground
> uncountered, the city it landed on
> detonated in a slower, fatter, redder
> explosion. The city disappeared.
> You could not rebuild it.

> The wave ended when either all
> ICBMs were gone OR all six cities
> were gone. If you had cities left,
> you advanced. Each new wave had
> more ICBMs, faster, and after wave
> three they could split mid-flight
> into MIRV warheads — Multiple
> Independent Re-entry Vehicles —
> each of which now needed to be
> countered separately.

> Eventually you lost. Always.

> ── PART 2: THE TWO MOST IMPORTANT
>    PIXELS ──

> When the player lost — when the
> last of their six cities went up
> in red — the screen flashed white.
> Hold for one second. Then, in big
> letters, centred on the screen,
> two words:

>     THE END.

> Not "GAME OVER". Not "INSERT COIN".

>     THE END.

> I added those two words at 3 AM the
> night before we shipped the master
> ROM to the factory. Rains had not
> approved them. Marketing did not
> see them until they were on
> cabinets. They could not unship
> them.

> The reason I added them was that
> "GAME OVER" felt like a lie. The
> game ended because nuclear war
> had ended civilisation. There would
> not be another game. There would
> not be another coin slot to feed.
> There was no civilisation left to
> feed a coin slot.

>     THE END.

> The arcade cabinets shipped in
> July 1980. The Cold War was at its
> peak. The Soviets had invaded
> Afghanistan eight months earlier.
> The President had just announced a
> boycott of the Moscow Olympics.
> Children played MISSILE COMMAND in
> Pizza Hut arcades while their
> parents had genuine, sober
> conversations about whether to
> build a bunker in the basement.

> ── PART 3: THE COST ──

> I had nightmares for ten years.

> The nightmares were always the same:
> I would be standing in my front yard
> in suburban California, and I would
> look up, and I would see the dotted
> arc of an ICBM trail, slow and
> graceful, against a perfectly clear
> blue sky, descending toward my house.
> In the dream I would always
> understand exactly where it was
> going to hit. I would always have
> roughly six seconds of warning. I
> would always wake up at the second
> just before impact.

> The dream stopped in the early
> 1990s, around the time the Soviet
> Union dissolved. I do not know if
> that is a coincidence. I suspect it
> is not.

> I left the game industry in 1984.
> Designed for Symbolics, did some
> early CGI work for film, retired
> in the 2000s. MISSILE COMMAND
> remains the most successful game
> I ever made. It sold approximately
> 19,000 arcade cabinets. Hundreds
> of millions of quarters. Atari's
> single biggest title that year.

> I would still un-make it if I could.

> ── PART 4: WHAT THIS TRIBUTE IS ──

> Below this preamble is our tribute,
> called MISSILE DEFENSE. Three
> batteries. Six cities. ICBMs falling.
> MIRVs from wave three. The same
> blast-radius mechanic — your
> counter-missile detonates where you
> tapped, and the slowly-expanding
> sphere catches whatever incoming
> happens to pass through it during
> its expansion.

> The cities cannot be rebuilt.
>
> If all six are destroyed, the
> screen will flash white. Then it
> will go black. And in big white
> letters, centred:

>     THE END.

> We kept Dave's two words.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> MISSILE COMMAND and related marks
> belong to their respective rights-
> holders. The
> mechanic — point-and-click area-
> denial counter-missile defence — has
> been imitated countless times in
> the intervening 46 years and is
> effectively folk knowledge. Dave
> Theurer was the original designer.
> Our tribute is called MISSILE
> DEFENSE. Concerns: elyssov@gmail.com.
""".trimIndent()

private val BODY_RU: String = """
> Мне было 27 в 1979-м, когда
> я пришёл в аркадное
> подразделение Atari. Им
> нужен был новый тайтл,
> чтобы конкурировать с
> кабинетом Space War от
> Cinematronics. Лайл Рейнс,
> мой начальник, дал мне
> газету.

> В газете была статья про
> новую американскую
> Ballistic Missile Early
> Warning System — BMEWS.
> Рейнс ткнул в радарную
> диаграмму в статье — карту
> США с пунктирными линиями,
> показывающими траектории
> межконтинентальных
> баллистических ракет,
> уходящих дугой через
> Северный полюс от
> Советского Союза — и
> сказал:

>   «Сделай ИЗ ЭТОГО игру.»

> Я сделал. Назвал MISSILE
> COMMAND. Это единственная
> игра, которую я бы с
> радостью «расделал», если
> бы мог.

> ── ЧАСТЬ 1: ЧТО ИГРА ИЗОБРАЖАЛА ──

> Шесть городов. Три
> противоракетные батареи.
> МБР сыпятся сверху экрана,
> оставляя пунктирные следы
> при спуске. Игрок двигал
> прицел трекболом —
> настоящим, тяжёлым,
> тяжелошарным трекболом из
> радарных консолей — и
> запускал контрракеты,
> нажимая одну из трёх кнопок
> (по одной на батарею).

> Каждая контрракета шла
> вверх к последней позиции
> прицела, затем взрывалась
> медленно-расширяющимся
> ядерным взрывом. Если
> входящая МБР проходила
> через радиус взрыва, пока
> он ещё расширялся, МБР
> тоже детонировала — и
> твой город был спасён в
> этом залпе.

> Если МБР достигала земли
> неоспоренной, город, на
> который она упала,
> детонировал в более
> медленном, более жирном,
> более красном взрыве.
> Город исчезал. Перестроить
> нельзя.

> Волна кончалась, когда либо
> все МБР были уничтожены,
> ЛИБО все шесть городов
> погибли. Если города
> оставались — переходишь
> дальше. Каждая новая волна
> — больше МБР, быстрее, а
> после третьей волны они
> могли в полёте разделяться
> на MIRV — боеголовки
> множественного независимого
> входа — каждую из которых
> теперь надо было сбивать
> отдельно.

> В конце концов ты
> проигрывал. Всегда.

> ── ЧАСТЬ 2: ДВА САМЫХ ВАЖНЫХ
>    ПИКСЕЛЯ ──

> Когда игрок проигрывал —
> когда последний из шести
> городов вспыхивал красным
> — экран мигал белым.
> Замирал на секунду. Затем,
> крупными буквами, по
> центру экрана, два слова:

>     THE END.

> Не «GAME OVER». Не
> «INSERT COIN».

>     THE END.

> Я добавил эти два слова в
> 3 часа ночи накануне того
> дня, как мы отправляли
> мастер-ROM на фабрику.
> Рейнс их не утверждал.
> Маркетинг их не видел,
> пока они уже не были на
> кабинетах. Они не могли их
> «отправить обратно».

> Причина, по которой я их
> добавил, — «GAME OVER»
> звучал как ложь. Игра
> заканчивалась потому, что
> ядерная война кончила
> цивилизацию. Не будет
> другой игры. Не будет
> другого монетоприёмника
> для кормления. Не будет
> цивилизации, чтобы кормить
> монетоприёмник.

>     THE END.

> Аркадные кабинеты вышли в
> июле 1980-го. Холодная
> война была на пике.
> Советы вторглись в
> Афганистан восемь месяцев
> назад. Президент только
> что объявил бойкот
> московской Олимпиады. Дети
> играли в MISSILE COMMAND в
> аркадах Pizza Hut, пока их
> родители вели настоящие,
> трезвые разговоры о том,
> стоит ли строить бункер в
> подвале.

> ── ЧАСТЬ 3: ЦЕНА ──

> У меня были кошмары десять
> лет.

> Кошмары всегда одинаковые:
> я стою во дворе своего
> дома в пригороде
> Калифорнии, поднимаю
> глаза и вижу пунктирную
> дугу следа МБР, медленную
> и грациозную, на идеально
> ясном голубом небе,
> спускающуюся к моему
> дому. В сне я всегда
> понимал точно, куда она
> попадёт. У меня всегда
> было примерно шесть секунд
> предупреждения. Я всегда
> просыпался за секунду до
> удара.

> Сон прекратился в начале
> 1990-х, примерно тогда,
> когда распался Советский
> Союз. Не знаю, совпадение
> ли это. Подозреваю, что
> нет.

> Я ушёл из игровой
> индустрии в 1984-м.
> Дизайнил для Symbolics,
> занимался ранней CGI для
> кино, ушёл на пенсию в
> 2000-х. MISSILE COMMAND
> остаётся самой успешной
> игрой, что я когда-либо
> сделал. Продано примерно
> 19 000 аркадных кабинетов.
> Сотни миллионов
> четвертаков. Главный
> тайтл Atari того года.

> Я бы её всё равно
> «расделал», если бы мог.

> ── ЧАСТЬ 4: ЧТО ЗА ЭТА ДАНЬ ──

> Ниже этой преамбулы — наша
> дань, называется MISSILE
> DEFENSE. Три батареи.
> Шесть городов. Падающие
> МБР. MIRV с третьей
> волны. Та же механика
> радиуса взрыва — твоя
> контрракета детонирует там,
> где ты тапнул, и медленно
> расширяющаяся сфера ловит
> всё входящее, что
> проходит через неё во
> время расширения.

> Города не отстраиваются.
>
> Если все шесть
> уничтожены, экран мигнёт
> белым. Затем чёрным. И
> крупными белыми буквами,
> по центру:

>     THE END.

> Мы оставили два слова
> Дэйва.

> ── СНОСКА У КОСТРА ──

> MISSILE COMMAND и связанные
> знаки принадлежат их
> соответствующим
> правообладателям. Механика
> — point-and-click
> area-denial противоракетная
> защита — копировалась
> бесчисленное количество
> раз за прошедшие 46 лет и
> фактически стала народным
> знанием. Дэйв Теурер был
> оригинальным дизайнером.
> Наша дань называется
> MISSILE DEFENSE. Вопросы:
> elyssov@gmail.com.
""".trimIndent()

val MISSILE_INTRO_EN: String = HEADER + "\n" + BODY_EN + "\n" + FOOTER
val MISSILE_INTRO_RU: String = HEADER + "\n" + BODY_RU + "\n" + FOOTER

fun pickMissileIntro(lang: String): String = when (lang.lowercase()) {
    "ru" -> MISSILE_INTRO_RU
    else -> MISSILE_INTRO_EN
}

val MISSILE_INTRO_TEXT: String = MISSILE_INTRO_EN
