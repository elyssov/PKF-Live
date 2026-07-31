import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.CanvasBasedWindow
import kotlin.math.abs

/**
 * Arcade-hall exhibit #1: the same shape our 27 games have — update(dt)
 * + DrawScope draw + input — self-contained, no engine deps.
 * Controls: ArrowUp/Down or W/S (primary), mouse as the 1972 potentiometer.
 */
private class MiniPong {
    val w = 800f; val h = 600f
    var ballX = 400f; var ballY = 300f
    var vx = 260f; var vy = 180f
    var paddleY = 260f          // left paddle — the player
    var cpuY = 260f
    var scoreL = 0; var scoreR = 0
    val ph = 90f; val pw = 12f
    var keyDir = 0f             // -1 up, +1 down while a key is held

    fun update(dt: Float) {
        paddleY = (paddleY + keyDir * 420f * dt).coerceIn(0f, h - ph)
        ballX += vx * dt; ballY += vy * dt
        if (ballY < 8f && vy < 0) vy = -vy
        if (ballY > h - 8f && vy > 0) vy = -vy
        // CPU chases lazily.
        cpuY += ((ballY - ph / 2f) - cpuY).coerceIn(-200f * dt, 200f * dt)
        // Left paddle.
        if (vx < 0 && ballX < 30f + pw && ballY in paddleY..(paddleY + ph)) vx = abs(vx) * 1.03f
        // Right paddle.
        if (vx > 0 && ballX > w - 30f - pw && ballY in cpuY..(cpuY + ph)) vx = -abs(vx) * 1.03f
        if (ballX < -20f) { scoreR++; reset() }
        if (ballX > w + 20f) { scoreL++; reset() }
    }

    private fun reset() { ballX = w / 2f; ballY = h / 2f; vx = if (vx > 0) -260f else 260f; vy = 180f }
}

// 3×5 pixel digits — the only score display Pong ever needed.
private val DIGITS = arrayOf(
    "111101101101111", "010110010010111", "111001111100111", "111001111001111",
    "101101111001001", "111100111001111", "111100111101111", "111001010010010",
    "111101111101111", "111101111001111",
)

private fun DrawScope.drawScoreDigit(n: Int, x: Float, y: Float, cell: Float) {
    val bits = DIGITS[n.coerceIn(0, 9)]
    for (r in 0 until 5) for (c in 0 until 3) {
        if (bits[r * 3 + c] == '1') {
            drawRect(Color.White, Offset(x + c * cell, y + r * cell), Size(cell - 1f, cell - 1f))
        }
    }
}

private fun DrawScope.drawScore(value: Int, cx: Float, y: Float, cell: Float) {
    val s = value.coerceAtMost(99).toString()
    val digitW = 3 * cell + cell
    var x = cx - s.length * digitW / 2f
    for (ch in s) { drawScoreDigit(ch - '0', x, y, cell); x += digitW }
}

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow(canvasElementId = "arcade") {
        val game = remember { MiniPong() }
        var tick by remember { mutableLongStateOf(0L) }
        var canvasSize by remember { mutableStateOf(Size(1f, 1f)) }
        val focus = remember { FocusRequester() }

        LaunchedEffect(Unit) {
            focus.requestFocus()
            var last = 0L
            while (true) {
                withFrameNanos { now ->
                    if (last != 0L) game.update(((now - last) / 1e9f).coerceAtMost(0.05f))
                    last = now
                    tick++
                }
            }
        }

        Canvas(
            Modifier.fillMaxSize().background(Color.Black)
                .focusRequester(focus).focusable()
                .onPreviewKeyEvent { e ->
                    val down = e.type == KeyEventType.KeyDown
                    when (e.key) {
                        Key.DirectionUp, Key.W -> { game.keyDir = if (down) -1f else 0f; true }
                        Key.DirectionDown, Key.S -> { game.keyDir = if (down) 1f else 0f; true }
                        else -> false
                    }
                }
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val e = awaitPointerEvent()
                            val p = e.changes.firstOrNull() ?: continue
                            val scale = canvasSize.height / game.h
                            game.paddleY = (p.position.y / scale - game.ph / 2f)
                                .coerceIn(0f, game.h - game.ph)
                        }
                    }
                },
        ) {
            @Suppress("UNUSED_EXPRESSION") tick
            canvasSize = size
            val s = size.height / game.h
            val ox = (size.width - game.w * s) / 2f
            // Court.
            var y = 0f
            while (y < game.h) {
                drawRect(Color(0xFF2E2E2E), Offset(ox + game.w / 2f * s - 2f, y * s), Size(4f, 18f * s))
                y += 36f
            }
            // Scoreboard — chunky 1972 pixels, either side of the net.
            drawScore(game.scoreL, ox + game.w * 0.25f * s, 30f * s, 14f * s)
            drawScore(game.scoreR, ox + game.w * 0.75f * s, 30f * s, 14f * s)
            // Paddles + ball.
            drawRect(Color.White, Offset(ox + 30f * s - game.pw * s, game.paddleY * s), Size(game.pw * s, game.ph * s))
            drawRect(Color.White, Offset(ox + (game.w - 30f) * s, game.cpuY * s), Size(game.pw * s, game.ph * s))
            drawRect(Color.White, Offset(ox + (game.ballX - 8f) * s, (game.ballY - 8f) * s), Size(16f * s, 16f * s))
        }
    }
}
