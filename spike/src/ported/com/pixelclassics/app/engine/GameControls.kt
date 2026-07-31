package com.pixelclassics.app.engine

/**
 * Touchscreen-control surface that GameHost can overlay above a Game's
 * Canvas. The actual button layout lives in `ui/game/ControlsOverlay`;
 * each game only declares what it needs and implements the handlers
 * on [Game].
 */
enum class GameControls {
    /** No on-screen controls — game handles raw pointer events itself. */
    NONE,

    /** 4-arrow D-pad in the bottom-left corner. */
    DPAD,

    /** D-pad on the left + a fire button on the right. */
    DPAD_AND_FIRE,

    /**
     * Two single-press action buttons, one per gutter (no D-pad). The LEFT
     * gutter button is the primary action ([Game.onFire]), the RIGHT gutter
     * button is the secondary action ([Game.onSecondary]). Labels and colours
     * come from [Game.buttonLabels]. Used by games that move automatically and
     * only need two verbs (e.g. JUMP / SHOOT).
     */
    TWO_BUTTONS,
}
