package com.pixelclassics.app.engine

import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope

/**
 * Common interface every game implements.
 *
 * Coordinate system is "virtual": the game draws into a logical rectangle
 * [0, virtualWidth] x [0, virtualHeight]. GameHost scales-to-fit and routes
 * pointer events back into the same virtual space, so games never deal with
 * physical pixels or DPI directly.
 */
interface Game {
    val id: String
    val titleResId: Int
    val virtualWidth: Float get() = 640f
    val virtualHeight: Float get() = 480f
    val backgroundColor: Color get() = Color.Black

    /** Lines shown on the universal PAUSE screen as in-game help/legend
     *  (controls, goal, what to pick up). Empty = no help shown. */
    val helpLines: List<String> get() = emptyList()

    /** Localised pause help: same contract as [introTextFor] — GameHost
     *  passes the launcher-picker language, default ignores it. */
    fun helpLinesFor(lang: String): List<String> = helpLines

    /** Long-form "campfire" introduction text shown the first time the
     *  player launches this game. The visual treatment is determined by
     *  [introStyle]. null = no intro (game starts directly).
     *  Single-language games override this; multilingual games override
     *  [introTextFor] instead. */
    val introText: String? get() = null

    /** Localised intro: GameHost passes the player's selected language
     *  ("ru", "en", "vi", or "" for system default) and the game returns
     *  the right version. Default: ignore the lang and return [introText].
     *  Multilingual games override this and pick the matching block. */
    fun introTextFor(lang: String): String? = introText

    /** Visual theme of the intro overlay (terminal-green, Nokia-LCD,
     *  blueprint, etc.). Defaults to parchment. */
    val introStyle: com.pixelclassics.app.ui.game.IntroStyle
        get() = com.pixelclassics.app.ui.game.IntroStyle.PARCHMENT

    /** Called once when the game is launched. */
    fun init(ctx: GameContext)

    /** Per-frame logic update. dt is in seconds. */
    fun update(dt: Float, ctx: GameContext)

    /** Per-frame render in virtual coords. */
    fun draw(scope: DrawScope, ctx: GameContext)

    /** Pointer events in virtual coords. id distinguishes multi-touch. */
    fun onPointerDown(x: Float, y: Float, id: Long, ctx: GameContext) {}
    fun onPointerMove(x: Float, y: Float, id: Long, ctx: GameContext) {}
    fun onPointerUp(x: Float, y: Float, id: Long, ctx: GameContext) {}

    /**
     * If non-NONE, GameHost overlays a touchscreen control surface
     * (D-pad and/or fire button) and routes presses back via the
     * callbacks below — saving every game from having to reinvent
     * one-finger virtual joysticks.
     */
    val controls: GameControls get() = GameControls.NONE

    /**
     * When true, GameHost lets touches START anywhere on the canvas (virtual
     * coords clamped into the screen) instead of only inside the museum
     * glass. Default: every raw-touch game (no gutter controls) — the era
     * chassis is a picture frame, not a touch fence (Юджин, 31.07: Pong
     * became unplayable when input shrank to the photographed tube).
     */
    val wholeCanvasTouch: Boolean get() = controls == GameControls.NONE

    /**
     * If a game switches its control surface at runtime (e.g. a control-style
     * picker that turns the side gutters on or off), expose the live value here
     * so GameHost re-lays-out the gutters reactively. Null = use the static
     * [controls] value. Opt-in; costs nothing for games that don't override it.
     */
    val controlsState: State<GameControls>? get() = null

    /**
     * If true, GameHost flips the Activity to PORTRAIT while this game is open
     * and restores landscape on exit. For handheld-style portrait games (the
     * Brick-Game Tetris). The manifest declares configChanges for orientation,
     * so the switch does not recreate the Activity or lose game state.
     */
    val portrait: Boolean get() = false

    /** Direction-pad press in cardinal coords (dx and dy each ∈ {-1, 0, 1}). */
    fun onDirection(dx: Int, dy: Int, ctx: GameContext) {}

    /** True = tile-hopper: one onDirection per physical press, hosts must
     *  NOT auto-repeat while the control is held (Frogger-style movement). */
    val discreteDpad: Boolean get() = false

    /** True = the game wants onPointerMove even with no button held (mouse
     *  hover). Desktop-only behaviour: Pong's paddle rides the mouse like the
     *  1972 potentiometer knob. Touch platforms have no hover — no-op there. */
    val hoverPointer: Boolean get() = false

    /** Fire button — also the LEFT (primary) button in TWO_BUTTONS mode. */
    fun onFire(ctx: GameContext) {}

    /** Secondary action — the RIGHT button in TWO_BUTTONS mode. */
    fun onSecondary(ctx: GameContext) {}

    /** TWO_BUTTONS only: (left, right) button labels shown on the gutters. */
    val buttonLabels: Pair<String, String> get() = "A" to "B"

    /** Return true if the game consumed the system back press. */
    fun onBack(ctx: GameContext): Boolean = false

    /** Called when pausing/resuming (lifecycle, foreground/background). */
    fun onPause(ctx: GameContext) {}
    fun onResume(ctx: GameContext) {}

    /** Called once when the game is exited. Release resources here. */
    fun dispose() {}
}
