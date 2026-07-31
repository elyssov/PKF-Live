import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.CanvasBasedWindow
import com.pixelclassics.app.AssetManifest
import com.pixelclassics.app.audio.SoundManager
import com.pixelclassics.app.compat.AppLang
import com.pixelclassics.app.compat.AssetStore
import com.pixelclassics.app.data.ScoreStore
import com.pixelclassics.app.data.SettingsStore
import com.pixelclassics.app.ui.game.GameHost
import com.pixelclassics.app.ui.game.GameRegistry
import kotlinx.browser.window
import kotlinx.coroutines.await
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get

/**
 * The arcade cabinet's power switch: reads ?game=<id>&lang=<en|ru|vi> from
 * the URL, preloads every asset the games may touch (wasm has no sync IO),
 * then boots that game inside GameHost. Exit posts pxcf-exit to the parent
 * hall, which closes the stage.
 */
@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val params = window.location.search
    val gameId = param(params, "game") ?: "pong"
    val lang = param(params, "lang") ?: "en"
    if (param(params, "shot") == "1") com.pixelclassics.app.compat.ShotMode.enabled = true

    CanvasBasedWindow(canvasElementId = "arcade") {
        var ready by remember { mutableStateOf(false) }
        var error by remember { mutableStateOf<String?>(null) }

        LaunchedEffect(Unit) {
            try {
                for (path in AssetManifest.files) {
                    val resp = jsFetch("assets/$path").await<FetchResponse>()
                    if (!resp.ok) continue
                    val buf = resp.arrayBuffer().await<ArrayBuffer>()
                    AssetStore.files[path] = toByteArray(buf)
                }
                ready = true
            } catch (t: Throwable) {
                error = t.message ?: "asset preload failed"
            }
        }

        if (!ready) {
            Box(Modifier.fillMaxSize().background(Color.Black), contentAlignment = Alignment.Center) {
                Text(
                    if (error == null) "LOADING…" else "LOAD ERROR: $error",
                    color = if (error == null) Color(0xFF57E389) else Color(0xFFD9302E),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 18.sp,
                )
            }
            return@CanvasBasedWindow
        }

        val sound = remember { SoundManager() }
        val scores = remember { ScoreStore() }
        val settings = remember { SettingsStore().apply { this.lang = lang } }
        remember { AppLang.lang = lang }

        val entry = remember { GameRegistry.byId(gameId) ?: GameRegistry.games.first() }
        val game = remember { entry.factory() }

        GameHost(
            game = game,
            sound = sound,
            scores = scores,
            settings = settings,
            onExit = {
                // Inside the hall the parent removes the stage; standalone
                // tabs just show the dead tube.
                window.parent.postMessage("pxcf-exit".toJsString(), "*")
            },
        )
    }
}

external interface FetchResponse : JsAny {
    val ok: Boolean
    fun arrayBuffer(): kotlin.js.Promise<ArrayBuffer>
}

private fun jsFetch(url: String): kotlin.js.Promise<FetchResponse> = js("fetch(url)")

private fun param(search: String, name: String): String? {
    val q = search.removePrefix("?")
    for (pair in q.split('&')) {
        val idx = pair.indexOf('=')
        if (idx > 0 && pair.substring(0, idx) == name) {
            return pair.substring(idx + 1).takeIf { it.isNotEmpty() }
        }
    }
    return null
}

private fun toByteArray(buf: ArrayBuffer): ByteArray {
    val view = Int8Array(buf)
    val out = ByteArray(view.length)
    for (i in 0 until view.length) out[i] = view[i]
    return out
}
