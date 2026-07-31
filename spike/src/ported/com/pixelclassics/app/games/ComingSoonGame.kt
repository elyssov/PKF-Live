package com.pixelclassics.app.games

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.pixelclassics.app.engine.Game
import com.pixelclassics.app.engine.GameContext

/**
 * Placeholder shown for games that still need a Compose port.
 * Renders a CRT-style "Coming Soon" panel; tap or back to leave.
 */
class ComingSoonGame(override val titleResId: Int) : Game {
    override val id: String = "coming_soon"
    override val backgroundColor: Color = Color(0xFF000000)

    private var blink = 0f

    override fun init(ctx: GameContext) {}

    override fun update(dt: Float, ctx: GameContext) {
        blink = (blink + dt) % 1.2f
    }

    override fun draw(scope: DrawScope, ctx: GameContext) = with(scope) {
        val cx = 320f; val cy = 240f
        drawRect(Color(0xFF0000AA), topLeft = Offset(80f, 120f), size = androidx.compose.ui.geometry.Size(480f, 240f))
        drawRect(
            Color(0xFF00AAAA),
            topLeft = Offset(80f, 120f),
            size = androidx.compose.ui.geometry.Size(480f, 240f),
            style = Stroke(width = 2f),
        )
        // Two big mock-text bars and one blinking line — drawn as filled rects so we
        // don't need a font baked into the APK for this placeholder.
        drawRect(Color(0xFFFFFF55), topLeft = Offset(cx - 180f, cy - 60f), size = androidx.compose.ui.geometry.Size(360f, 24f))
        drawRect(Color(0xFFAAAAAA), topLeft = Offset(cx - 120f, cy - 10f), size = androidx.compose.ui.geometry.Size(240f, 12f))
        if (blink < 0.6f) {
            drawRect(Color(0xFF55FF55), topLeft = Offset(cx - 80f, cy + 40f), size = androidx.compose.ui.geometry.Size(160f, 12f))
        }
    }

    override fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {
        ctx.exitRequested = true
    }

    override fun onBack(ctx: GameContext): Boolean = false
}
