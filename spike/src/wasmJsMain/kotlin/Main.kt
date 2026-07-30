import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.window.CanvasBasedWindow
import kotlin.math.abs

/**
 * Arcade-hall gate spike: the same shape our 27 games have — update(dt)
 * + DrawScope draw + pointer input — but self-contained, no engine deps.
 * Proves Kotlin/Wasm + Compose canvas holds a 60fps game in the browser.
 */
private class MiniPong {
    val w = 800f; val h = 600f
    var ballX = 400f; var ballY = 300f
    var vx = 260f; var vy = 180f
    var paddleY = 260f          // left paddle, mouse-driven
    var cpuY = 260f
    var scoreL = 0; var scoreR = 0
    val ph = 90f; val pw = 12f

    fun update(dt: Float) {
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

fun main() {
    CanvasBasedWindow(canvasElementId = "arcade") {
        val game = remember { MiniPong() }
        var tick by remember { mutableLongStateOf(0L) }
        var canvasSize by remember { mutableStateOf(Size(1f, 1f)) }

        LaunchedEffect(Unit) {
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
            // Paddles + ball, chunky 1972 pixels.
            drawRect(Color.White, Offset(ox + 30f * s - game.pw * s, game.paddleY * s), Size(game.pw * s, game.ph * s))
            drawRect(Color.White, Offset(ox + (game.w - 30f) * s, game.cpuY * s), Size(game.pw * s, game.ph * s))
            drawRect(Color.White, Offset(ox + (game.ballX - 8f) * s, (game.ballY - 8f) * s), Size(16f * s, 16f * s))
        }
    }
}
