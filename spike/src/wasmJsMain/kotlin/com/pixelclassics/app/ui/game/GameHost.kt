package com.pixelclassics.app.ui.game

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pixelclassics.app.audio.SoundManager
import com.pixelclassics.app.compat.AssetManager
import com.pixelclassics.app.compat.BackDispatcher
import com.pixelclassics.app.compat.BackHandler
import com.pixelclassics.app.data.ScoreStore
import com.pixelclassics.app.data.SettingsStore
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext
import com.pixelclassics.app.engine.computeVirtualTransform
import kotlinx.browser.document
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import org.w3c.dom.events.Event
import org.w3c.dom.events.KeyboardEvent

/**
 * Arcade (wasm) GameHost: frame loop, pointer routing, KEYBOARD routing and
 * back handling. The cabinet chrome lives on the site — the game gets the
 * whole canvas (era NONE). Keys ride document-level listeners: Compose
 * canvas focus inside an iframe is a lottery, the document always hears.
 *
 *   Arrows / WASD  → Game.onDirection (200ms delay-to-repeat, then 100ms)
 *   Space / X / Enter → Game.onFire   (repeat 160ms while held)
 *   Z / Shift      → Game.onSecondary
 *   P              → pause,  Esc → back (intro/pause/exit)
 */
@Composable
fun GameHost(
    game: Game,
    sound: SoundManager,
    scores: ScoreStore,
    settings: SettingsStore,
    onExit: () -> Unit,
) {
    val assets = remember { AssetManager() }
    val ctx = remember(game.id) { GameContext(sound, scores, settings, game.id, assets, null) }

    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var lastFrameNs by remember { mutableLongStateOf(0L) }
    var initialised by remember { mutableStateOf(false) }
    var tick by remember { mutableLongStateOf(0L) }

    DisposableEffect(game.id) {
        game.init(ctx)
        initialised = true
        onDispose {
            game.dispose()
            initialised = false
        }
    }

    LaunchedEffect(game.id) {
        lastFrameNs = 0L
        while (true) {
            withFrameNanos { now ->
                val prev = lastFrameNs
                lastFrameNs = now
                if (prev != 0L && initialised && !ctx.paused) {
                    val dt = ((now - prev) / 1_000_000_000.0).toFloat().coerceAtMost(0.05f)
                    game.update(dt, ctx)
                }
                tick++
            }
            if (ctx.exitRequested) {
                onExit()
                return@LaunchedEffect
            }
        }
    }

    BackHandler(enabled = true) {
        if (!game.onBack(ctx)) onExit()
    }

    var paused by remember(game.id) { mutableStateOf(false) }
    var soundOn by remember { mutableStateOf(settings.soundEnabled) }
    var showIntro by remember(game.id) {
        mutableStateOf(game.introTextFor(settings.lang) != null &&
            !com.pixelclassics.app.compat.ShotMode.enabled)
    }
    ctx.paused = showIntro || paused

    // ── Keyboard state (document-level) ──
    val heldDirs = remember(game.id) { mutableStateOf(setOf<String>()) }
    var firePressed by remember(game.id) { mutableStateOf(false) }
    var introSkipRequest by remember(game.id) { mutableStateOf(0) }

    fun dirOf(keys: Set<String>): Pair<Int, Int> {
        var dx = 0; var dy = 0
        if ("left" in keys) dx -= 1
        if ("right" in keys) dx += 1
        if ("up" in keys) dy -= 1
        if ("down" in keys) dy += 1
        // Cardinal only (matches the phone rocker): horizontal wins ties.
        return if (dx != 0) dx to 0 else 0 to dy
    }

    fun dirKeyOf(k: String): String? = when (k) {
        "ArrowLeft", "a", "A", "ф", "Ф" -> "left"
        "ArrowRight", "d", "D", "в", "В" -> "right"
        "ArrowUp", "w", "W", "ц", "Ц" -> "up"
        "ArrowDown", "s", "S", "ы", "Ы" -> "down"
        else -> null
    }

    fun isFireKey(k: String): Boolean =
        k == " " || k == "x" || k == "X" || k == "ч" || k == "Ч" || k == "Enter"

    DisposableEffect(game.id) {
        val down = keyListener@{ e: Event ->
            val ke = e as KeyboardEvent
            val k = ke.key
            if (showIntro && (isFireKey(k))) { introSkipRequest++; ke.preventDefault(); return@keyListener }
            val dir = dirKeyOf(k)
            when {
                dir != null -> {
                    if (dir !in heldDirs.value) heldDirs.value = heldDirs.value + dir
                    ke.preventDefault()
                }
                isFireKey(k) -> { if (!firePressed) firePressed = true; ke.preventDefault() }
                k == "z" || k == "Z" || k == "я" || k == "Я" || k == "Shift" -> {
                    game.onSecondary(ctx); ke.preventDefault()
                }
                k == "p" || k == "P" || k == "з" || k == "З" -> {
                    if (!showIntro) {
                        if (paused) { paused = false; game.onResume(ctx) }
                        else { paused = true; game.onPause(ctx) }
                    }
                    ke.preventDefault()
                }
                k == "Escape" -> { BackDispatcher.dispatch(); ke.preventDefault() }
            }
        }
        val up = { e: Event ->
            val ke = e as KeyboardEvent
            val dir = dirKeyOf(ke.key)
            when {
                dir != null -> heldDirs.value = heldDirs.value - dir
                isFireKey(ke.key) -> firePressed = false
                else -> {}
            }
        }
        document.addEventListener("keydown", down)
        document.addEventListener("keyup", up)
        onDispose {
            document.removeEventListener("keydown", down)
            document.removeEventListener("keyup", up)
        }
    }

    // Direction auto-repeat — same cadence as the phone D-pad.
    LaunchedEffect(game.id, heldDirs.value) {
        val d = dirOf(heldDirs.value)
        if (d == 0 to 0) {
            game.onDirection(0, 0, ctx)
            return@LaunchedEffect
        }
        game.onDirection(d.first, d.second, ctx)
        // Tile-hoppers (Frogger) take exactly one hop per physical press.
        if (game.discreteDpad) return@LaunchedEffect
        delay(200)
        while (isActive && dirOf(heldDirs.value) == d) {
            game.onDirection(d.first, d.second, ctx)
            delay(100)
        }
    }

    // Fire auto-repeat — same cadence as the on-screen fire button.
    LaunchedEffect(game.id, firePressed) {
        if (!firePressed) return@LaunchedEffect
        game.onFire(ctx)
        delay(200)
        while (isActive && firePressed) {
            game.onFire(ctx)
            delay(160)
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(game.backgroundColor)) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(game.id) {
                    val capturedPointers = mutableSetOf<Long>()
                    awaitEachGesture {
                        while (true) {
                            val event = awaitPointerEvent(PointerEventPass.Main)
                            val transform = computeVirtualTransform(
                                size.toSize(), game.virtualWidth, game.virtualHeight)
                            for (change in event.changes) {
                                val pointerId = change.id.value
                                val inside = transform.contains(change.position)
                                if (change.changedToDown() && inside) capturedPointers += pointerId
                                // Mouse hover for games that ride the bare
                                // pointer (Pong's potentiometer paddle).
                                if (game.hoverPointer && !change.pressed && inside &&
                                    pointerId !in capturedPointers
                                ) {
                                    val virt = transform.toVirtual(change.position)
                                    game.onPointerMove(virt.x, virt.y, pointerId, ctx)
                                }
                                if (pointerId !in capturedPointers) continue
                                val virt = transform.toVirtual(change.position)
                                when {
                                    change.changedToDown() ->
                                        game.onPointerDown(virt.x, virt.y, pointerId, ctx)
                                    change.changedToUp() ->
                                        game.onPointerUp(virt.x, virt.y, pointerId, ctx)
                                    else ->
                                        game.onPointerMove(virt.x, virt.y, pointerId, ctx)
                                }
                                if (change.pressed) change.consume()
                                if (change.changedToUp()) capturedPointers -= pointerId
                            }
                        }
                    }
                },
        ) {
            canvasSize = size
            @Suppress("UNUSED_EXPRESSION") tick
            val t = computeVirtualTransform(size, game.virtualWidth, game.virtualHeight)
            translate(left = t.offsetX, top = t.offsetY) {
                scaleAround(t.scale) {
                    game.draw(this, ctx)
                }
            }
        }

        HostBar(
            soundOn = soundOn,
            hasIntro = game.introTextFor(settings.lang) != null,
            modifier = Modifier.align(Alignment.TopEnd),
            onPause = { if (!paused) { paused = true; game.onPause(ctx) } },
            onMute = { soundOn = !soundOn; settings.soundEnabled = soundOn; sound.enabled = soundOn },
            onHistory = { showIntro = true },
            onExit = { ctx.exitRequested = true },
        )
        if (paused && !showIntro) PauseScreen(
            help = game.helpLinesFor(settings.lang) +
                listOf("", "KEYS: ARROWS/WASD MOVE · SPACE/X FIRE · Z ALT · P PAUSE · ESC BACK"),
            onResume = { paused = false; game.onResume(ctx) },
            onExit = { ctx.exitRequested = true },
        )
        if (showIntro && game.introTextFor(settings.lang) != null) {
            IntroOverlay(
                text = game.introTextFor(settings.lang)!!,
                style = game.introStyle,
                skipSignal = introSkipRequest,
            ) {
                settings.markCampfireSeen(game.id)
                showIntro = false
            }
        }
    }
}

/** drawScope.scale(scale, pivot=Offset.Zero) without losing the receiver. */
private inline fun androidx.compose.ui.graphics.drawscope.DrawScope.scaleAround(
    factor: Float,
    block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit,
) {
    if (factor == 1f) { block(); return }
    scale(factor, factor, pivot = Offset.Zero, block = block)
}

private fun androidx.compose.ui.input.pointer.PointerInputChange.changedToDown(): Boolean =
    pressed && !previousPressed

private fun androidx.compose.ui.input.pointer.PointerInputChange.changedToUp(): Boolean =
    !pressed && previousPressed

private fun androidx.compose.ui.unit.IntSize.toSize(): Size =
    Size(width.toFloat(), height.toFloat())

// ── Universal host overlay: pause / mute / ? / exit ────────────────────
@Composable
private fun HostBar(soundOn: Boolean, hasIntro: Boolean, modifier: Modifier, onPause: () -> Unit, onMute: () -> Unit, onHistory: () -> Unit, onExit: () -> Unit) {
    Row(modifier.padding(6.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        HostBtn("II", onPause)
        HostBtn(if (soundOn) "VOL" else "OFF", onMute)
        if (hasIntro) HostBtn("?", onHistory)
        HostBtn("✕", onExit)
    }
}

@Composable
private fun HostBtn(label: String, onClick: () -> Unit) {
    Box(
        Modifier.height(30.dp).clip(RoundedCornerShape(6.dp))
            .background(Color.Black.copy(alpha = 0.5f)).clickable(onClick = onClick)
            .padding(horizontal = 9.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun PauseScreen(help: List<String>, onResume: () -> Unit, onExit: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text("PAUSED", color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
            if (help.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                help.forEach { Text(it, color = Color(0xFFCFCFCF), fontSize = 14.sp, modifier = Modifier.padding(vertical = 2.dp)) }
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                PauseBtn("RESUME", Color(0xFF2E7D32), onResume)
                PauseBtn("MENU", Color(0xFF8B2500), onExit)
            }
        }
    }
}

@Composable
private fun PauseBtn(label: String, color: Color, onClick: () -> Unit) {
    Box(
        Modifier.height(44.dp).clip(RoundedCornerShape(8.dp)).background(color)
            .clickable(onClick = onClick).padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center,
    ) { Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold) }
}
