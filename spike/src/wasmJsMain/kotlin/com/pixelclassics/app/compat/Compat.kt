package com.pixelclassics.app.compat

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import com.pixelclassics.app.StringTable
import org.jetbrains.skia.Image

/**
 * Browser (wasm) stand-ins for the handful of Android APIs the shared
 * sources use — sibling of the desktop compat layer. The port script
 * rewrites the corresponding imports to point here, so the game/UI
 * sources stay byte-identical to the Android repo everywhere else.
 */

/** Every asset the games may touch, fetched up-front by Main before the
 *  first frame — wasm has no synchronous IO, so we preload into memory. */
object AssetStore {
    val files = HashMap<String, ByteArray>()
}

/** Minimal java.io.InputStream stand-in over preloaded bytes. Supports the
 *  three call shapes the shared sources actually use:
 *  `.use { BitmapFactory.decodeStream(it) }`, `.readBytes()`,
 *  `.bufferedReader().use { it.readText() }`. */
class InputStream(private val bytes: ByteArray) : AutoCloseable {
    fun readBytes(): ByteArray = bytes
    fun bufferedReader(): Reader = Reader(bytes)
    override fun close() {}
}

class Reader(private val bytes: ByteArray) : AutoCloseable {
    fun readText(): String = bytes.decodeToString()
    override fun close() {}
}

/** Android AssetManager → the preloaded [AssetStore]. */
class AssetManager {
    fun open(path: String): InputStream =
        InputStream(AssetStore.files[path] ?: error("asset not preloaded: $path"))
}

/** android.graphics.BitmapFactory — decode is deferred to [asImageBitmap]. */
object BitmapFactory {
    fun decodeStream(input: InputStream): ByteArray = input.readBytes()
}

/** androidx.compose.ui.graphics.asImageBitmap for the bytes above (Skia). */
fun ByteArray.asImageBitmap(): ImageBitmap =
    Image.makeFromEncoded(this).toComposeImageBitmap()

/** Current UI language ("en"/"ru"/"vi"). Mutable state → recomposes on change. */
object AppLang {
    var lang: String by mutableStateOf("en")
}

/** Screenshot harness (?shot=1): skip intros so the showcase camera sees
 *  the playfield, not the campfire plaque. Never set in normal play. */
object ShotMode {
    var enabled: Boolean = false
}

private fun dateNow(): Double = js("Date.now()")

/** System.currentTimeMillis stand-in (wasm has no java.lang.System). */
fun nowMillis(): Long = dateNow().toLong()

/** String.format stand-in: %s %d %0Nd %.Nf — all the shared sources use. */
fun String.pxfmt(vararg args: Any?): String {
    val out = StringBuilder()
    var argIdx = 0
    var i = 0
    while (i < length) {
        val ch = this[i]
        if (ch == '%' && i + 1 < length) {
            var j = i + 1
            var precision = -1
            if (this[j] == '.') {
                var k = j + 1
                while (k < length && this[k].isDigit()) k++
                precision = substring(j + 1, k).toIntOrNull() ?: -1
                j = k
            } else {
                while (j < length && this[j].isDigit()) j++
            }
            val spec = getOrNull(j)
            if (spec == 's' || spec == 'd' || spec == 'f') {
                val arg = args.getOrNull(argIdx++)
                var s = when (spec) {
                    'f' -> {
                        val v = (arg as? Number)?.toDouble() ?: 0.0
                        val p = if (precision >= 0) precision else 6
                        var scale = 1.0
                        repeat(p) { scale *= 10.0 }
                        val r = kotlin.math.round(v * scale) / scale
                        val txt = r.toString()
                        val dot = txt.indexOf('.')
                        when {
                            p == 0 -> txt.substringBefore('.')
                            dot < 0 -> txt + "." + "0".repeat(p)
                            else -> {
                                val frac = txt.length - dot - 1
                                if (frac >= p) txt.substring(0, dot + 1 + p)
                                else txt + "0".repeat(p - frac)
                            }
                        }
                    }
                    else -> arg?.toString() ?: ""
                }
                val padSrc = substring(i + 1, j).takeWhile { it.isDigit() }
                val width = padSrc.removePrefix("0").toIntOrNull() ?: padSrc.toIntOrNull() ?: 0
                val zero = padSrc.startsWith("0")
                while (s.length < width) s = (if (zero) "0" else " ") + s
                out.append(s)
                i = j + 1
                continue
            }
        }
        out.append(ch)
        i++
    }
    return out.toString()
}

/** Tiny printf: sequential %s/%d/%0Nd — all the shared sources ever use.
 *  (wasm has no java.util.Formatter.) */
fun miniFormat(template: String, vararg args: Any): String {
    var out = StringBuilder()
    var argIdx = 0
    var i = 0
    while (i < template.length) {
        val ch = template[i]
        if (ch == '%' && i + 1 < template.length) {
            var j = i + 1
            while (j < template.length && template[j].isDigit()) j++
            if (j < template.length && (template[j] == 's' || template[j] == 'd')) {
                val pad = template.substring(i + 1, j).removePrefix("0")
                var s = args.getOrNull(argIdx++)?.toString() ?: ""
                val width = pad.toIntOrNull() ?: 0
                val zero = template.getOrNull(i + 1) == '0'
                while (s.length < width) s = (if (zero) "0" else " ") + s
                out.append(s)
                i = j + 1
                continue
            }
        }
        out.append(ch)
        i++
    }
    return out.toString()
}

/** androidx.compose.ui.res.stringResource(Int) over the generated table. */
@Composable
fun stringResource(id: Int): String = StringTable.lookup(AppLang.lang, id)

@Composable
fun stringResource(id: Int, vararg args: Any): String =
    miniFormat(StringTable.lookup(AppLang.lang, id), *args)

/**
 * androidx.activity.compose.BackHandler → a plain handler stack. The host
 * routes Esc to [BackDispatcher.dispatch], which invokes the most recently
 * registered enabled handler — same LIFO semantics as Android.
 */
object BackDispatcher {
    private val handlers = ArrayDeque<Pair<Any, () -> Unit>>()
    private val enabled = HashMap<Any, Boolean>()

    fun register(key: Any, isEnabled: Boolean, action: () -> Unit) {
        handlers.removeAll { it.first === key }
        handlers.addLast(key to action)
        enabled[key] = isEnabled
    }

    fun unregister(key: Any) {
        handlers.removeAll { it.first === key }
        enabled.remove(key)
    }

    /** Returns true if a handler consumed the back press. */
    fun dispatch(): Boolean {
        val h = handlers.lastOrNull { enabled[it.first] == true } ?: return false
        h.second()
        return true
    }
}

@Composable
fun BackHandler(enabled: Boolean = true, onBack: () -> Unit) {
    val key = remember { Any() }
    DisposableEffect(key, enabled, onBack) {
        BackDispatcher.register(key, enabled, onBack)
        onDispose { BackDispatcher.unregister(key) }
    }
}
