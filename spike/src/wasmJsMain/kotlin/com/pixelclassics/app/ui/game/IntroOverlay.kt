package com.pixelclassics.app.ui.game

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex

/**
 * Campfire intro (arcade/wasm twin) — the museum plaque of every game (обязательная музейная
 * лекция, Юджин 30.07: «обложка + артефакты изображения у КАЖДОЙ»).
 * Screen-era styles (terminals, CGA, amber consoles) render inside a 4:3
 * CRT bezel with glass vignette and scanlines; handheld LCD styles get a
 * plastic device shell; paper styles stay frameless but read as a centered
 * column. Typewriter mechanic + REAL clickable button at the bottom (Юджин
 * 03.06: «тап то бегин не работает тупо нигде → чини кнопкой»).
 * Back-ported from the desktop museum reformat (88d4447); desktop file is
 * the art-direction source of truth, keep them in step.
 */

private enum class IntroFrame { CRT, PLASTIC, NONE }

/**
 * The stories are hard-wrapped at one historical phone width, which fights
 * every other frame width (inside the CRT bezel the column is narrower).
 * Rejoin consecutive prose lines into one paragraph per line and let Text
 * soft-wrap to the frame width instead. Headers (──/█/═) and blank lines
 * survive untouched.
 */
internal fun reflowIntro(text: String): String {
    // Pseudo-graphics: plaque borders, dividers, ASCII art — never rejoin those.
    val artChars = "│┌┐└┘├┤─═║╔╗╚╝█▓▒░╭╮╰╯"
    fun isJoinable(line: String): Boolean {
        val body = line.trimStart().removePrefix(">").trimStart()
        return body.isNotEmpty() && body.first() !in artChars &&
            body.first() !in "-•*" &&
            // Runs of interior spaces mean column alignment (tables) — keep rows.
            !body.contains("  ")
    }
    val out = StringBuilder()
    val run = mutableListOf<String>()
    fun flush() {
        if (run.isEmpty()) return
        val hadQuote = run.first().trimStart().startsWith(">")
        val joined = buildString {
            for (piece in run.map { it.trimStart().removePrefix(">").trim() }) {
                when {
                    isEmpty() -> append(piece)
                    // A hard wrap split a hyphenated word — rejoin without a space.
                    last() == '-' -> append(piece)
                    else -> append(' ').append(piece)
                }
            }
        }
        // A table row hard-wrapped mid-cell leaves its tail ("4 cells)") as the
        // next line — if the previous emitted line dangles on a comma, reattach.
        if (out.length >= 2 && out[out.length - 1] == '\n' && out[out.length - 2] == ',') {
            out.setLength(out.length - 1)
            out.append(' ').append(joined).append('\n')
        } else {
            out.append(if (hadQuote) "> $joined" else joined).append('\n')
        }
        run.clear()
    }
    for (line in text.lines()) {
        if (isJoinable(line)) run.add(line) else { flush(); out.append(line).append('\n') }
    }
    flush()
    return out.toString().trimEnd('\n')
}

private fun frameFor(style: IntroStyle): IntroFrame = when (style) {
    IntroStyle.TERMINAL_GREEN_CRT,
    IntroStyle.VECTOR_WHITE_ON_BLACK,
    IntroStyle.ASCII_DOS,
    IntroStyle.CGA_CYAN,
    IntroStyle.COLD_WAR_AMBER,
    IntroStyle.WIN31_GREY -> IntroFrame.CRT
    IntroStyle.NOKIA_LCD,
    IntroStyle.BRICK_GAME -> IntroFrame.PLASTIC
    else -> IntroFrame.NONE
}

@Composable
fun IntroOverlay(
    text: String,
    style: IntroStyle,
    skipSignal: Int = 0,
    onDismiss: () -> Unit,
) {
    @Suppress("NAME_SHADOWING") val text = remember(text) { reflowIntro(text) }
    var elapsed by remember { mutableFloatStateOf(0f) }
    var skipToEnd by remember { mutableStateOf(false) }

    // Drive the typewriter clock from frame nanos so it pauses with the activity.
    LaunchedEffect(text) {
        var last = 0L
        while (true) {
            withFrameNanos { now ->
                if (last != 0L) elapsed += ((now - last) / 1_000_000_000.0).toFloat()
                last = now
            }
        }
    }

    val printed by remember(text) {
        derivedStateOf {
            if (skipToEnd) text.length else (elapsed * style.charsPerSec).toInt().coerceAtMost(text.length)
        }
    }
    val shown = remember(printed) { text.take(printed) }
    val done = printed >= text.length

    // Keep the freshly typed line in view. ScrollState.scrollTo only moves the
    // offset — it never forces a remeasure mid-frame (the LazyColumn +
    // scrollToItem pair crashed headless renders on desktop; same medicine).
    val scrollState = rememberScrollState()
    LaunchedEffect(text) {
        snapshotFlow { printed }.collect {
            if (!done) scrollState.scrollTo(scrollState.maxValue)
        }
    }

    // Pulse for the action button — slow breathing while typing, faster heartbeat when ready.
    val infTrans = rememberInfiniteTransition(label = "intro-pulse")
    val pulse by infTrans.animateFloat(
        initialValue = 0.55f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(if (done) 700 else 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse-alpha",
    )

    var lastSkip by remember(text) { mutableStateOf(skipSignal) }
    LaunchedEffect(skipSignal) {
        if (skipSignal != lastSkip) {
            lastSkip = skipSignal
            if (!done) skipToEnd = true else onDismiss()
        }
    }

    val frame = frameFor(style)
    val roomColor = when (frame) {
        IntroFrame.CRT -> Color(0xFF121214)      // dim museum hall around the tube
        IntroFrame.PLASTIC -> Color(0xFF1A1D24)
        IntroFrame.NONE -> style.bg
    }

    // zIndex(10f) guarantees the overlay sits above whatever GameHost drew below;
    // the action button itself is what receives taps — no detectTapGestures here.
    Box(
        Modifier.fillMaxSize().background(roomColor).zIndex(10f),
        contentAlignment = Alignment.Center,
    ) {
        when (frame) {
            IntroFrame.CRT -> CrtBezel {
                IntroBody(style, shown, done, pulse, scrollState,
                    onAction = { if (!done) skipToEnd = true else onDismiss() })
            }
            IntroFrame.PLASTIC -> PlasticShell(style) {
                IntroBody(style, shown, done, pulse, scrollState,
                    onAction = { if (!done) skipToEnd = true else onDismiss() })
            }
            IntroFrame.NONE -> Box(
                Modifier.fillMaxSize().background(style.bg),
                contentAlignment = Alignment.Center,
            ) {
                Box(Modifier.widthIn(max = 640.dp).fillMaxHeight()) {
                    IntroBody(style, shown, done, pulse, scrollState,
                        onAction = { if (!done) skipToEnd = true else onDismiss() })
                }
            }
        }
    }
}

/** A 4:3 cathode-ray tube in a dark chassis, glass vignette + scanlines. */
@Composable
private fun CrtBezel(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxHeight(0.96f)
            .aspectRatio(4f / 3f)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF2B2B2E))                      // chassis plastic
            .border(3.dp, Color(0xFF0B0B0C), RoundedCornerShape(22.dp))
            .padding(16.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black)
                .border(2.dp, Color(0xFF1B1B1D), RoundedCornerShape(14.dp))
                .drawWithContent {
                    drawContent()
                    // Scanlines — the tube refreshes in front of your eyes.
                    var y = 0f
                    while (y < size.height) {
                        drawRect(Color.Black.copy(alpha = 0.16f), Offset(0f, y), Size(size.width, 1f))
                        y += 3f
                    }
                    // Glass vignette + a faint diagonal glare.
                    drawRect(
                        Brush.radialGradient(
                            0.62f to Color.Transparent,
                            1f to Color.Black.copy(alpha = 0.42f),
                            center = Offset(size.width / 2f, size.height / 2f),
                            radius = size.width * 0.72f,
                        ),
                    )
                    drawRect(
                        Brush.linearGradient(
                            0f to Color.White.copy(alpha = 0.05f),
                            0.25f to Color.Transparent,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, size.height),
                        ),
                    )
                },
        ) { content() }
        // Power LED on the chassis, bottom-right.
        Box(
            Modifier.align(Alignment.BottomEnd).padding(end = 2.dp)
                .size(6.dp).clip(CircleShape).background(Color(0xFF57E389)),
        )
    }
}

/** Handheld LCD device shell (Nokia-blue / brick-game beige plastic). */
@Composable
private fun PlasticShell(style: IntroStyle, content: @Composable () -> Unit) {
    val shell = if (style == IntroStyle.NOKIA_LCD) Color(0xFF27324E) else Color(0xFF8F8468)
    Box(
        Modifier
            .fillMaxHeight(0.96f)
            .aspectRatio(3.4f / 3f)
            .clip(RoundedCornerShape(28.dp))
            .background(shell)
            .border(3.dp, Color.Black.copy(alpha = 0.55f), RoundedCornerShape(28.dp))
            .padding(18.dp),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .background(style.bg)
                .border(2.dp, Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                .drawWithContent {
                    drawContent()
                    // Faint LCD pixel grid.
                    var y = 0f
                    while (y < size.height) {
                        drawRect(Color.Black.copy(alpha = 0.045f), Offset(0f, y), Size(size.width, 1f))
                        y += 4f
                    }
                },
        ) { content() }
    }
}

@Composable
private fun IntroBody(
    style: IntroStyle,
    shown: String,
    done: Boolean,
    pulse: Float,
    scrollState: ScrollState,
    onAction: () -> Unit,
) {
    val framed = frameFor(style) != IntroFrame.NONE
    Column(
        Modifier.fillMaxSize()
            .then(if (framed) Modifier.background(style.bg) else Modifier)
            .padding(horizontal = if (framed) 16.dp else 18.dp, vertical = 14.dp),
    ) {
        Box(Modifier.fillMaxWidth().height(2.dp).background(style.frameMark))
        Spacer(Modifier.height(8.dp))

        val font = if (style.monospace) FontFamily.Monospace else FontFamily.Serif
        val lines = remember(shown) { shown.split('\n') }
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(scrollState),
        ) {
            for (line in lines) {
                val isHeader = line.trimStart().startsWith("──") ||
                    line.trimStart().startsWith("█") ||
                    line.trimStart().startsWith("═")
                val isQuote = line.trimStart().startsWith(">")
                Text(
                    text = line,
                    color = when {
                        isHeader -> style.accent
                        isQuote -> style.ink
                        else -> style.inkDim
                    },
                    fontFamily = font,
                    fontWeight = if (isHeader) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                )
            }
            if (!done) {
                Text(
                    text = "_",
                    color = style.ink,
                    fontFamily = font,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Box(Modifier.fillMaxWidth().height(2.dp).background(style.frameMark))
        Spacer(Modifier.height(8.dp))

        // ───── ACTION BUTTON ─────────────────────────────────────────────
        // Big, visible, tappable. Foundation Modifier.clickable — no
        // pointerInput arbitration. Two states: SKIP while typing, BEGIN
        // once the whole story has been printed.
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp))
                    .background(style.accent.copy(alpha = pulse * 0.22f))
                    .border(2.dp, style.accent.copy(alpha = pulse), RoundedCornerShape(6.dp))
                    .clickable(onClick = onAction)
                    .padding(vertical = 12.dp, horizontal = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (!done) "▓▓▓  SKIP TYPING  ▓▓▓" else "▶▶▶  CLICK TO BEGIN  ◀◀◀",
                    color = style.accent,
                    fontFamily = if (style.monospace) FontFamily.Monospace else FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 2.sp,
                )
            }
        }
    }
}
