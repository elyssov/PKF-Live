package com.pixelclassics.app.games.tetris

/**
 * Long-form campfire intro for BRIX — the Chinese Brick Game handheld,
 * narrated by an anonymous Shenzhen-factory PCB describing its forty-
 * year tour of the Eastern half of the planet's childhoods.
 */
val TETRIS_INTRO_TEXT: String = """
██████████████████████████████████████████
█  S U P E R   B R I X   H A N D H E L D █
█  ░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░░  █
█  MADE IN SHENZHEN — 9999 IN 1 — 4xAA   █
██████████████████████████████████████████

> Reset. Beep. Eight quarter-tone notes.
> Green LCD wakes up. Welcome back. I am
> a Brick Game.

> You probably had one of me. Or your
> mum did. Or your aunt's neighbour's
> kid. Maybe still has me, in a kitchen
> drawer, somewhere between dead AA
> batteries and rolls of Scotch tape.

> ── PART 1: I AM NOT TETRIS ──

> First — important distinction. I am
> not TETRIS. The Tetris Company,
> Limited, owns TETRIS. I have a
> tetris-shaped puzzle MODE inside me,
> labelled discreetly "01 of 9999",
> but I am a different animal.

> I am the Chinese Brick Game. Born in
> Shenzhen factories sometime in the
> late 1980s. Sold for one US dollar
> retail. Bigger than your hand. Made
> from injection-molded plastic in
> beige, grey, red, blue, occasionally
> transparent. Runs on two AA batteries
> for weeks. Single dot-matrix LCD,
> 10 columns wide by 20 rows tall.
> Green-on-green.

> Underneath my pixel grid: a Holtek
> microcontroller, hot off the
> assembly line, with all my games
> hardwired into a 4-kilobyte mask
> ROM. The chip costs eighty cents in
> bulk. The whole device, assembled,
> retails for one US dollar in the
> Yiwu wholesale market.

> Marketing labels on my front:
>   "9999 IN 1"
>   "BRICK GAME"
>   "SUPER HANDHELD"
>   "POWERED BY YOUR DREAMS"

> The 9999 is a lie. I have between
> four and twenty distinct games. The
> rest is the same game with different
> starting speeds and obstacle patterns
> labelled with sequential numbers.
> The lie is not malicious. It is the
> way packaging works in 1995 in
> Yiwu.

> ── PART 2: WHERE I WENT ──

> While the West was buying Nintendo
> Game Boys and Sega Game Gears at
> US$80 a unit, I was selling at the
> Almaty bazaar for two thousand
> tenge (~$5). At the Saigon Saturday
> market for 50,000 dong. At the
> Belgrade bus station kiosk for 200
> dinar. At the Cairo electronics
> souk for 30 pounds. At the bazar
> in Tashkent for the price of a
> kilo of sugar.

> If you grew up between Vladivostok
> and Lisbon and your family did not
> live in a country with a Nintendo
> distributor — and that is most of
> the world's population — your first
> portable gaming experience was me.
> Not the Game Boy. Not Sega Game
> Gear. Not Atari Lynx. Me.

> A whole generation of Eastern
> European, Central Asian, Vietnamese,
> Egyptian and Latin American kids
> learned to play games on me, in
> green monochrome, with screen ghosts,
> on AA batteries that lasted longer
> than the parents' marriages.

> ── PART 3: WHAT'S INSIDE ──

> The four real games every Brick
> Game ships with:

>   TETRIS — the puzzle, the puzzle
>     that needs no introduction. Seven
>     piece shapes fall, rotate, lock.
>     Clear a row, it disappears. Clear
>     four rows at once and the device
>     plays a Holtek-chip rendition of
>     KOROBEINIKI through a piezo
>     speaker the size of a thumbnail.

>   RACE — top-down dodging. You drive
>     a tiny F1-shaped silhouette. Other
>     vehicles come down the road at
>     you. Don't crash. Speed up over
>     time. Get hit. Restart.

>   SNAKE — eat a square, grow a square
>     longer, don't bite yourself. The
>     same Snake you saw on every Nokia
>     phone, two years before Nokia
>     thought of it.

>   SHOOTER — a tiny ship at the bottom
>     of the well. Aliens in formation
>     at the top. Tap the big button to
>     fire a single bullet upward.
>     Aliens descend one row when they
>     reach the edge. You probably will
>     not see the boss because the boss
>     is in mode "047", and you played
>     mode "001".

> All of these in the same 10x20 grid.
> All of these in the same green-on-
> green palette. All of these scored
> in three-digit numbers. None of
> these can be saved. Battery dies —
> game forgotten.

> ── PART 4: THE CASE I LIVE IN ──

> The plastic shell I sit inside is
> usually beige or red or black. On
> the front you will find:

>   - The LCD screen, with a faint
>     "ghost grid" visible even when
>     the device is off — every cell
>     position permanently dark from
>     decades of partial-on rendering.
>   - A vertically-mounted speaker
>     grill.
>   - A four-arrow direction cluster.
>   - A big circular "ACTION" button
>     (rotates pieces in TETRIS, fires
>     in SHOOTER, ignored elsewhere).
>   - A small "START / PAUSE" button.
>   - A "SOUND ON/OFF" button which
>     does what it says.
>   - A "LIGHT" button which on the
>     real device toggled a tiny
>     orange LED backlight (so you
>     could play under bedsheets at
>     night) — and which, in our
>     emulation, toggles between a
>     monochrome LCD palette and a
>     full-colour 8-bit palette,
>     because the real device's
>     backlight is unrecreatable in
>     software.

> ── PART 5: WHY YOU LOVE ME ──

> Because I was cheap, durable, and
> available before there was anything
> else. Because two AA batteries lasted
> three months and you could read a
> faded TETRIS score in dim sunlight
> on a bus going from your village
> to the city to visit your grandma.
> Because none of my buttons had any
> documentation but you figured out
> all of them within five seconds.
> Because you have never in your life
> heard a sound as joyful and as awful
> as my piezo-speaker rendition of
> KOROBEINIKI from a four-bit
> resonator running on chip noise.

> ── PART 6: WHAT WE HAVE BUILT ──

> Below this preamble is our tribute.
> A Brick Game-shaped on-screen
> handheld. You can hold it
> portrait. The LCD is in the centre.
> The buttons are on the chassis below
> the screen — direction pad, action,
> START / PAUSE, SOUND, LIGHT
> (colour↔monochrome). It plays four
> games: BRIX (our take on the
> puzzle), RACE, SNAKE and SHOOTER.

> START selects the active mini-
> game. ACTION rotates / fires.
> Direction pad steers / drops.
> LIGHT toggles palette. SOUND
> toggles audio.

> When paused, LIGHT also doubles as
> "back to game-select" — so you can
> exit a mini-game without quitting
> Brix entirely.

> Play me. Charge me. Charge me one
> more time. I will still work in
> twenty years.

> ── A SMALL FOOTNOTE AT THE CAMPFIRE ──

> TETRIS is a trademark of The Tetris
> Company. We call our handheld BRIX
> and the included puzzle mode "TETRIS"
> only in the way a museum labels a
> Mona Lisa replica "Mona Lisa" — as
> a description of what is depicted,
> not a claim of being it. If The
> Tetris Company has any concern:
> elyssov@gmail.com, resolved
> quickly. The Brick Game form factor
> itself was never trademarked by any
> single party — it is the collective
> design language of two decades of
> Shenzhen handheld manufacture.

> _

[ CLICK TO BEGIN ]
""".trimIndent()
