package com.pixelclassics.app.engine

import com.pixelclassics.app.audio.SoundManager
import com.pixelclassics.app.compat.AssetManager
import com.pixelclassics.app.data.ScoreStore
import com.pixelclassics.app.data.SettingsStore

/**
 * Per-session game context — browser twin of the Android version.
 * `activity` is always null in the arcade (games null-check it before
 * using platform features, which simply stay off).
 */
class GameContext(
    val sound: SoundManager,
    val scores: ScoreStore,
    val settings: SettingsStore,
    val gameId: String,
    val assets: AssetManager,
    val activity: Any? = null,
) {
    var paused: Boolean = false
    var exitRequested: Boolean = false

    /** The picker language ("en"/"ru"/"vi"), read live — never cache the
     *  result across frames: the picker is the single source of truth. */
    val lang: String get() = settings.lang.ifEmpty { "en" }

    /** Live-localised in-game text. */
    fun tr(en: String, ru: String, vi: String? = null): String = when (lang) {
        "ru" -> ru
        "vi" -> vi ?: en
        else -> en
    }
}
