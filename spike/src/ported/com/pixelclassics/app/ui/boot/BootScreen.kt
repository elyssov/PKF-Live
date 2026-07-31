package com.pixelclassics.app.ui.boot

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.pixelclassics.app.audio.SoundManager
import com.pixelclassics.app.ui.theme.DosPalette
import kotlinx.coroutines.delay

/**
 * AMIBIOS POST + DOS boot sequence. Plays the classic POST beep midway,
 * types lines with the original delays, then hands off to [onComplete].
 * Tapping anywhere fast-forwards past the rest of the script.
 */
@Composable
fun BootScreen(
    sound: SoundManager,
    onComplete: () -> Unit,
) {
    val lines = remember { mutableListOf<BootLine>().toMutableStateList() }
    var skip by remember { mutableIntStateOf(0) }

    LaunchedEffect(skip) {
        lines.clear()
        for (line in BOOT_SCRIPT) {
            if (skip > 0) { lines.add(line); continue }
            line.action?.invoke(sound)
            lines.add(line)
            delay(line.delayMs.toLong())
        }
        delay(if (skip > 0) 0L else 350L)
        sound.pcBoot()
        delay(450L)
        onComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DosPalette.Black)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        awaitPointerEvent()
                        skip = 1
                    }
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 16.dp, top = 12.dp, end = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            for (line in lines) {
                BootLineRow(line)
            }
            BlinkingCursor()
        }
    }
}

@Composable
private fun BootLineRow(line: BootLine) {
    val color = when (line.style) {
        BootStyle.WHITE -> DosPalette.White
        BootStyle.GRAY -> DosPalette.Gray
    }
    Text(
        text = line.text,
        color = color,
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        fontWeight = if (line.style == BootStyle.WHITE) FontWeight.SemiBold else FontWeight.Normal,
    )
}

@Composable
private fun BlinkingCursor() {
    val t = rememberInfiniteTransition(label = "cursor")
    val alpha by t.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            tween(500, easing = LinearEasing),
            RepeatMode.Reverse,
        ),
        label = "cursorAlpha",
    )
    Text(
        text = "_",
        color = DosPalette.Gray.copy(alpha = alpha),
        fontFamily = FontFamily.Monospace,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
    )
}

private enum class BootStyle { WHITE, GRAY }

private data class BootLine(
    val text: String,
    val style: BootStyle = BootStyle.GRAY,
    val delayMs: Int = 50,
    val action: ((SoundManager) -> Unit)? = null,
)

private val BOOT_SCRIPT: List<BootLine> = listOf(
    BootLine("", delayMs = 100),
    BootLine("╔══════════════════════════════════════════╗", BootStyle.WHITE, 20),
    BootLine("║  American Megatrends Inc.                ║", BootStyle.WHITE, 20),
    BootLine("║  A M I B I O S  (C) 1993                 ║", BootStyle.WHITE, 20),
    BootLine("╚══════════════════════════════════════════╝", BootStyle.WHITE, 20),
    BootLine("", delayMs = 150),
    BootLine("BIOS Date: 09/30/93  Ver: 08.00.15", BootStyle.GRAY, 60),
    BootLine("", delayMs = 100),
    BootLine("Main Processor : Intel 80486DX2-66 MHz", BootStyle.WHITE, 60),
    BootLine("Numeric Processor : Present", BootStyle.GRAY, 40),
    BootLine("Floppy Drive A : 1.44M, 3.5 in.", BootStyle.GRAY, 40),
    BootLine("Hard Disk C : Type 47 — 504 MB", BootStyle.GRAY, 40),
    BootLine("Base Memory : 640K", BootStyle.GRAY, 40),
    BootLine("Extended Memory : 15360K", BootStyle.GRAY, 40),
    BootLine("", delayMs = 220, action = { it.pcBeep() }),
    BootLine("Starting MS-DOS...", BootStyle.WHITE, 400),
    BootLine("", delayMs = 150),
    BootLine("HIMEM.SYS is testing extended memory...done.", BootStyle.GRAY, 200),
    BootLine("DEVICE=C:\\DOS\\HIMEM.SYS", BootStyle.GRAY, 100),
    BootLine("DEVICE=C:\\DOS\\EMM386.EXE NOEMS", BootStyle.GRAY, 100),
    BootLine("DEVICE=C:\\DOS\\SETVER.EXE", BootStyle.GRAY, 80),
    BootLine("", delayMs = 100),
    BootLine("C:\\DOS\\SMARTDRV.EXE /X", BootStyle.GRAY, 80),
    BootLine("Microsoft SMARTDrive Disk Cache version 5.02", BootStyle.GRAY, 60),
    BootLine("  Cache size: 2,097,152 bytes", BootStyle.GRAY, 50),
    BootLine("", delayMs = 100),
    BootLine("C:\\DOS\\MSCDEX.EXE /D:MSCD001 /L:D", BootStyle.GRAY, 80),
    BootLine("MSCDEX Version 2.23  Drive D: = Driver MSCD001", BootStyle.GRAY, 60),
    BootLine("", delayMs = 100),
    BootLine("C:\\MOUSE\\MOUSE.COM", BootStyle.GRAY, 80),
    BootLine("Mouse driver installed.", BootStyle.GRAY, 60),
    BootLine("", delayMs = 100),
    BootLine("C:\\>", BootStyle.GRAY, 200),
    BootLine("C:\\>CD GAMES", BootStyle.WHITE, 300),
    BootLine("", delayMs = 100),
    BootLine("C:\\GAMES>NC.EXE", BootStyle.WHITE, 400),
    BootLine("", delayMs = 200),
)
