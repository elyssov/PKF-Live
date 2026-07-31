package com.pixelclassics.app.games.f21

internal const val F21_INTRO_EN = """
> CLASSIFIED — TOP SECRET
> PROJECT FERRET
> MINISTRY OF DEFENCE — SPECIAL AVIATION BUREAU

Pilot,

You have been selected for a mission of the highest
sensitivity. Your aircraft is the F-21 — a prototype
stealth interceptor invisible to conventional radar.

Your task: penetrate enemy airspace, locate and destroy
strategic targets, and return home undetected.

The F-21's angular fuselage deflects radar waves.
Fly low, fly slow, and the enemy will never know
you were there. But push the throttle too hard or
climb too high — and every SAM battery in the
sector will paint you.

Trust your instruments. Trust your instincts.
And above all — trust the stealth.

Good luck, pilot. You'll need it.

[END TRANSMISSION]
"""

internal const val F21_INTRO_RU = """
> СЕКРЕТНО — ОСОБАЯ ВАЖНОСТЬ
> ПРОЕКТ «ХОРЁК»
> МИНИСТЕРСТВО ОБОРОНЫ — СПЕЦИАЛЬНОЕ АВИАЦИОННОЕ БЮРО

Пилот,

Вы отобраны для задания высшей степени секретности.
Ваш самолёт — Ф-21, прототип стелс-перехватчика,
невидимого для обычных радаров.

Задание: проникнуть в воздушное пространство
противника, обнаружить и уничтожить стратегические
объекты, вернуться незамеченным.

Угловатый фюзеляж Ф-21 отражает радарные волны.
Летите низко, летите медленно — и враг никогда не
узнает, что вы были. Но дайте слишком много газа
или наберите высоту — и каждый ЗРК в секторе
увидит вас.

Доверяйте приборам. Доверяйте инстинктам.
И прежде всего — доверяйте стелсу.

Удачи, пилот. Она вам понадобится.

[КОНЕЦ ПЕРЕДАЧИ]
"""

internal const val F21_INTRO_VI = """
> MẬT — TỐI MẬT
> DỰ ÁN CHỒN
> BỘ QUỐC PHÒNG — CỤC HÀNG KHÔNG ĐẶC BIỆT

Phi công,

Bạn đã được chọn cho nhiệm vụ tối mật. Máy bay
của bạn là F-21 — nguyên mẫu tiêm kích tàng hình,
vô hình trước radar thông thường.

Nhiệm vụ: xâm nhập không phận địch, xác định và
tiêu diệt các mục tiêu chiến lược, trở về mà
không bị phát hiện.

Thân máy bay góc cạnh của F-21 phản xạ sóng radar.
Bay thấp, bay chậm — và kẻ thù sẽ không bao giờ
biết bạn ở đó. Nhưng đẩy ga quá mạnh hoặc leo
quá cao — mọi bệ phóng SAM trong khu vực sẽ
phát hiện bạn.

Tin vào thiết bị. Tin vào bản năng.
Và trên hết — tin vào tàng hình.

Chúc may mắn, phi công. Bạn sẽ cần nó.

[KẾT THÚC TRUYỀN TIN]
"""

internal fun pickF21Intro(lang: String): String = when (lang) {
    "ru" -> F21_INTRO_RU
    "vi" -> F21_INTRO_VI
    else -> F21_INTRO_EN
}
