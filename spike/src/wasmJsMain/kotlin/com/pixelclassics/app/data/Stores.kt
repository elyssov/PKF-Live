package com.pixelclassics.app.data

import kotlinx.browser.localStorage

/**
 * Browser persistence: localStorage under pxcf.* keys.
 * Same public API as the Android DataStore-backed stores.
 */
private class KvFile(private val prefix: String) {
    fun get(key: String): String? = localStorage.getItem("$prefix.$key")
    fun set(key: String, value: String) {
        try { localStorage.setItem("$prefix.$key", value) } catch (_: Throwable) {
            // Quota/permission failure is non-fatal — the session keeps its values.
        }
    }
}

class SettingsStore {
    private val f = KvFile("pxcf.settings")

    var lang: String
        get() = f.get("lang") ?: ""
        set(value) { f.set("lang", value) }

    var soundEnabled: Boolean
        get() = f.get("sound")?.toBoolean() ?: true
        set(value) { f.set("sound", value.toString()) }

    var hapticEnabled: Boolean
        get() = f.get("haptic")?.toBoolean() ?: true
        set(value) { f.set("haptic", value.toString()) }

    var musicEnabled: Boolean
        get() = f.get("music")?.toBoolean() ?: true
        set(value) { f.set("music", value.toString()) }

    var etIntroSeen: Boolean
        get() = f.get("et_intro_seen")?.toBoolean() ?: false
        set(value) { f.set("et_intro_seen", value.toString()) }

    /** Free-form serialised game state. Empty string = no save. */
    var rogueSave: String
        get() = f.get("rogue_save") ?: ""
        set(value) { f.set("rogue_save", value) }

    var caveSave: String
        get() = f.get("cave_save") ?: ""
        set(value) { f.set("cave_save", value) }

    private val campfireSeenSet: MutableSet<String> by lazy {
        (f.get("campfire_seen") ?: "").split(',').filter { it.isNotEmpty() }.toMutableSet()
    }
    fun isCampfireSeen(gameKey: String): Boolean = gameKey in campfireSeenSet
    fun markCampfireSeen(gameKey: String) {
        if (campfireSeenSet.add(gameKey)) f.set("campfire_seen", campfireSeenSet.joinToString(","))
    }
}

/** Persistent high-score store, one int per game id. Keeps the maximum. */
class ScoreStore {
    private val f = KvFile("pxcf.scores")

    fun get(gameId: String): Int = f.get("score_$gameId")?.toIntOrNull() ?: 0

    fun set(gameId: String, value: Int) {
        if (value <= 0) return
        if (value > get(gameId)) f.set("score_$gameId", value.toString())
    }
}
