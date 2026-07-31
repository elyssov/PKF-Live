#!/usr/bin/env python3
"""Port the Pixel Campfire Android sources into the PKF-Live arcade (wasm).

Third target of the copy-and-patch pipeline (после desktop). Copies game/UI
sources from the Android repo into spike/src/ported/, applying the same
textual patches the desktop port uses; copies assets/ into
spike/src/wasmJsMain/resources/assets/ (plus the arcade's own mono fonts)
and generates R.kt + StringTable + AssetManifest.

Re-run after any change in the Android repo. FAILS LOUDLY if a patch anchor
is missing, so a drifted upstream never silently half-ports.
"""
import re
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ANDROID = Path(r"C:\projects\pixel-classics\app\src\main")
ARCADE = Path(__file__).resolve().parent.parent / "spike"
SRC_JAVA = ANDROID / "java"
PORTED = ARCADE / "src" / "ported"
RES_OUT = ARCADE / "src" / "wasmJsMain" / "resources" / "assets"
OWN_FONTS = Path(__file__).resolve().parent / "fonts"

# Files replaced by hand-written wasm implementations (src/wasmJsMain/kotlin)
# or dead in the arcade (the hall replaces the NC world entirely).
EXCLUDE_EXACT = {
    "com/pixelclassics/app/MainActivity.kt",
    "com/pixelclassics/app/data/SettingsStore.kt",
    "com/pixelclassics/app/data/ScoreStore.kt",
    "com/pixelclassics/app/audio/SoundManager.kt",
    "com/pixelclassics/app/audio/MelodyPlayer.kt",
    "com/pixelclassics/app/ui/theme/DosFont.kt",
    "com/pixelclassics/app/engine/GameContext.kt",
    "com/pixelclassics/app/ui/game/GameHost.kt",
    # Touch gutters: keyboard+mouse arcade, no on-screen sticks.
    "com/pixelclassics/app/ui/game/ControlsOverlay.kt",
    # Museum overlays live hand-written on the wasm side.
    "com/pixelclassics/app/ui/game/IntroOverlay.kt",
    "com/pixelclassics/app/ui/game/ScreenEra.kt",
    # NC world (menu/launcher/settings/campfire modal) — the hall is the menu.
    "com/pixelclassics/app/ui/game/CampfireModal.kt",
    "com/pixelclassics/app/ui/PixelClassicsApp.kt",
    "com/pixelclassics/app/ui/FreeEditionDialog.kt",
}
EXCLUDE_DIRS = ("com/pixelclassics/app/ui/nc/",)

IMPORT_SWAPS = {
    "import androidx.compose.ui.platform.LocalContext\n": "",
    "import androidx.compose.ui.res.stringResource":
        "import com.pixelclassics.app.compat.stringResource",
    "import androidx.activity.compose.BackHandler":
        "import com.pixelclassics.app.compat.BackHandler",
    "import android.content.res.AssetManager":
        "import com.pixelclassics.app.compat.AssetManager",
    "import android.graphics.BitmapFactory":
        "import com.pixelclassics.app.compat.BitmapFactory",
    "import androidx.compose.ui.graphics.asImageBitmap":
        "import com.pixelclassics.app.compat.asImageBitmap",
}

DELETE_LINE_MARKERS = (
    "performHapticFeedback",
    "LocalHapticFeedback",
    "HapticFeedbackType",
)

# Same wording swaps as the desktop port: mouse for finger, Space for the
# gutter fire button. Story lore (Saloon tap, phone lore) stays untouched.
TEXT_SWAPS = [
    ("Side joystick rotates the cannon; the red button fires.",
     "\\u25c0 \\u25b6 arrow keys rotate the cannon; SPACE fires."),
    ("Use the side joystick: \\u25c0 \\u25b6 rotate the cannon.",
     "Use the arrow keys: \\u25c0 \\u25b6 rotate the cannon."),
    ("\\u25c0 \\u25b6  ARROWS  +  \\u25cf FIRE", "\\u25c0 \\u25b6  ARROWS  +  SPACE"),
    ("Tap VERB + NOUN chips to issue commands \\u2014 no keyboard needed",
     "Click VERB + NOUN chips to issue commands"),
    ("TAP FIRE TO RESTART", "PRESS SPACE TO RESTART"),
    ("TAP FIRE TO START", "PRESS SPACE TO START"),
    ("TIME OUT \\u00b7 TAP FIRE", "TIME OUT \\u00b7 PRESS SPACE"),
    ("TAP FIRE", "PRESS SPACE"),
    ("\\u25cf FIRE / TAP \\u2014 GO", "SPACE / CLICK \\u2014 GO"),
    ("\\u25cf FIRE \\u2014 pull the tap.", "SPACE \\u2014 pull the tap."),
    ("Tap \\u2014 reveal \\u00b7 long-press \\u2014 flag", "Click \\u2014 reveal \\u00b7 hold \\u2014 flag"),
    ("ЖМИ ОГОНЬ — СТАРТ", "ПРОБЕЛ — СТАРТ"),
    ("ЖМИ ОГОНЬ", "ЖМИ ПРОБЕЛ"),
    ("ТАП — РЕСТАРТ", "КЛИК — РЕСТАРТ"),
    ("ТАП — СТАРТ", "КЛИК — СТАРТ"),
    ("ТАП — ", "КЛИК — "),
    ("ТАП  =  ПРИЦЕЛ", "КЛИК = ПРИЦЕЛ"),
    ("ТАП = ПРИЦЕЛ", "КЛИК = ПРИЦЕЛ"),
    ("Тапни", "Кликни"),
    ("тапни", "кликни"),
    ("Тап — открыть · долгое нажатие — флажок", "Клик — открыть · зажать — флажок"),
    ("Боковой джойстик вращает пушку; красная кнопка стреляет.",
     "Стрелки ◀ ▶ вращают пушку; SPACE стреляет."),
    ("Боковой джойстик: ◀ ▶ вращает пушку.", "Стрелки: ◀ ▶ вращают пушку."),
    ("Красная кнопка ● — выстрел вдоль ствола.", "SPACE — выстрел вдоль ствола."),
    ("ВЕДИ РАКЕТКУ ПАЛЬЦЕМ", "ВЕДИ РАКЕТКУ МЫШЬЮ"),
    ("Веди свою ракетку (слева) вверх и вниз", "Веди свою ракетку (слева) мышью или стрелками"),
    ("Собирай команду из фишек ГЛАГОЛ + СУЩЕСТВИТЕЛЬНОЕ — клавиатура не нужна",
     "Собирай команду из фишек ГЛАГОЛ + СУЩЕСТВИТЕЛЬНОЕ — мышью"),
    ("TAP TO ", "CLICK TO "),
    ("TAP  TO  ", "CLICK  TO  "),
    ("TAP \\u2014 ", "CLICK \\u2014 "),
    ("TAP BELOW", "CLICK BELOW"),
    ("TAP ABOVE", "CLICK ABOVE"),
    ("Tap anywhere", "Click anywhere"),
    ("Tap the sky", "Click the sky"),
    ("Tap a cell", "Click a cell"),
    ("tap an enemy cell", "click an enemy cell"),
    ("Tap a tool, then tap the map", "Click a tool, then click the map"),
    ("then tap RUN", "then click RUN"),
    ("Don't tap a mine", "Don't click a mine"),
    ("Tap \\u2014 descend", "Click \\u2014 descend"),
]


# JVM-isms the wasm stdlib does not have — swapped to compat equivalents.
# Applied after TEXT_SWAPS; imports are injected when a swap actually fired.
WASM_SWAPS = [
    ("Math.random()", "kotlin.random.Random.nextDouble()"),
    ("System.currentTimeMillis()", "nowMillis()"),
    (".format(", ".pxfmt("),
    (".setCharAt(", ".set("),
    ("sb.append(String(map[r]))", "sb.append(map[r].concatToString())"),
]
WASM_IMPORTS = {
    "nowMillis(": "import com.pixelclassics.app.compat.nowMillis",
    ".pxfmt(": "import com.pixelclassics.app.compat.pxfmt",
}


def _unescape(s: str) -> str:
    if "\\u" not in s:
        return s
    return s.encode().decode("unicode_escape")


def must_replace(text: str, old: str, new: str, path: str) -> str:
    if old not in text:
        sys.exit(f"PATCH ANCHOR MISSING in {path}:\n{old}")
    return text.replace(old, new)


def must_splice(text: str, start: str, end: str, replacement: str, path: str) -> str:
    i = text.find(start)
    j = text.find(end)
    if i < 0 or j < 0 or j <= i:
        sys.exit(f"SPLICE ANCHORS MISSING in {path}: {start!r} .. {end!r}")
    return text[:i] + replacement + text[j + len(end):]


def patch_f21(text: str, path: str) -> str:
    for imp in (
        "import android.content.Context\n",
        "import android.hardware.Sensor\n",
        "import android.hardware.SensorEvent\n",
        "import android.hardware.SensorEventListener\n",
        "import android.hardware.SensorManager\n",
    ):
        text = must_replace(text, imp, "", path)
    text = must_replace(
        text,
        "    private var sensorManager: SensorManager? = null\n",
        "",
        path,
    )
    text = must_replace(
        text,
        "    private var gyroListener: SensorEventListener? = null",
        "    private var gyroListener: Any? = null",
        path,
    )
    text = must_splice(
        text,
        "    fun startGyroscope(context: Context) {",
        "        gyroReady = false\n    }",
        "    fun startGyroscope(context: Any) { /* no gyroscope in the arcade; D-pad flies the jet */ }\n\n"
        "    fun stopGyroscope() {\n        gyroListener = null\n        gyroReady = false\n    }",
        path,
    )
    return text


def excluded(rel: str) -> bool:
    return rel in EXCLUDE_EXACT or any(rel.startswith(d) for d in EXCLUDE_DIRS)


def copy_sources() -> int:
    shutil.rmtree(PORTED, ignore_errors=True)
    PORTED.mkdir(parents=True, exist_ok=True)
    count = 0
    for src in SRC_JAVA.rglob("*.kt"):
        rel = src.relative_to(SRC_JAVA).as_posix()
        if excluded(rel):
            continue
        text = src.read_text(encoding="utf-8")
        for old, new in IMPORT_SWAPS.items():
            text = text.replace(old, new)
        for old, new in TEXT_SWAPS:
            text = text.replace(_unescape(old), _unescape(new))
        if rel.endswith("games/f21/F21Game.kt"):
            text = patch_f21(text, rel)
        for old, new in WASM_SWAPS:
            text = text.replace(old, new)
        for marker, imp in WASM_IMPORTS.items():
            if marker in text and imp not in text:
                text = re.sub(r"(?m)^(package .+)$", r"\1\n\n" + imp, text, count=1)
        if "import android." in text:
            sys.exit(f"UNPATCHED android import remains in {rel}")
        dst = PORTED / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        with dst.open("w", encoding="utf-8", newline="\n") as fh:
            fh.write(text)
        count += 1
    return count


def copy_assets() -> list:
    shutil.rmtree(RES_OUT, ignore_errors=True)
    files = []
    for sub in ("content", "games", "sprites", "fonts"):
        src = ANDROID / "assets" / sub
        if src.exists():
            shutil.copytree(src, RES_OUT / sub)
    # The arcade's own faces (browser has no system fonts).
    (RES_OUT / "fonts").mkdir(parents=True, exist_ok=True)
    for ttf in OWN_FONTS.glob("*.ttf"):
        shutil.copy2(ttf, RES_OUT / "fonts" / ttf.name)
    for f in sorted(RES_OUT.rglob("*")):
        if f.is_file():
            files.append(f.relative_to(RES_OUT).as_posix())
    return files


def parse_strings(path: Path) -> dict:
    root = ET.parse(path).getroot()
    out = {}
    for el in root.findall("string"):
        raw = "".join(el.itertext())
        out[el.attrib["name"]] = (
            raw.replace("\\'", "'").replace("\\\"", '"').replace("\\n", "\n")
        )
    return out


def klit(s: str) -> str:
    return '"' + s.replace("\\", "\\\\").replace('"', '\\"').replace("\n", "\\n").replace("$", "\\$") + '"'


def gen_r() -> int:
    en = parse_strings(ANDROID / "res" / "values" / "strings.xml")
    ru = parse_strings(ANDROID / "res" / "values-ru" / "strings.xml")
    vi = parse_strings(ANDROID / "res" / "values-vi" / "strings.xml")
    names = list(en.keys())

    lines = [
        "// GENERATED by tools/port_to_wasm.py — do not edit.",
        "package com.pixelclassics.app",
        "",
        "object R {",
        "    object string {",
    ]
    for i, n in enumerate(names):
        lines.append(f"        const val {n}: Int = {i}")
    lines += ["    }", "}", "", "object StringTable {"]
    for lang, table in (("en", en), ("ru", ru), ("vi", vi)):
        lines.append(f"    val {lang}: Array<String> = arrayOf(")
        for n in names:
            lines.append(f"        {klit(table.get(n, en[n]))},")
        lines.append("    )")
    lines += [
        "",
        "    fun lookup(lang: String, id: Int): String = when (lang) {",
        '        "ru" -> ru[id]',
        '        "vi" -> vi[id]',
        "        else -> en[id]",
        "    }",
        "}",
        "",
    ]
    out = PORTED / "com/pixelclassics/app/R.kt"
    out.parent.mkdir(parents=True, exist_ok=True)
    with out.open("w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(lines))
    return len(names)


def gen_manifest(files: list) -> None:
    lines = [
        "// GENERATED by tools/port_to_wasm.py — do not edit.",
        "package com.pixelclassics.app",
        "",
        "/** Every asset Main preloads before the first frame (no sync IO in wasm). */",
        "object AssetManifest {",
        "    val files: List<String> = listOf(",
    ]
    for f in files:
        lines.append(f"        {klit(f)},")
    lines += ["    )", "}", ""]
    out = PORTED / "com/pixelclassics/app/AssetManifest.kt"
    with out.open("w", encoding="utf-8", newline="\n") as fh:
        fh.write("\n".join(lines))


def main() -> None:
    n = copy_sources()
    files = copy_assets()
    s = gen_r()
    gen_manifest(files)
    print(f"ported {n} kt files, {s} strings, {len(files)} assets -> {RES_OUT}")


if __name__ == "__main__":
    main()
