"""Voice the museum lectures via HuggingFace Inference API (MMS-TTS).

EN first (facebook/mms-tts-eng), RU next (facebook/mms-tts-rus).
Long lectures are chunked by paragraph, synthesized per chunk and
concatenated with ffmpeg into hall/lectures/<game>_<lang>.mp3.
Token: ~/.cache/huggingface/token (never committed).
"""
import json
import os
import re
import sys
import time
import subprocess
import urllib.request

HALL = os.path.join(os.path.dirname(__file__), "..", "hall", "lectures")
MODELS = {"en": "facebook/mms-tts-eng", "ru": "facebook/mms-tts-rus"}
ART = "│┌┐└┘├┤─═║╔╗╚╝█▓▒░╭╮╰╯"


def token() -> str:
    path = os.path.expanduser("~/.cache/huggingface/token")
    with open(path, encoding="ascii") as fh:
        return fh.read().strip()


def clean(text: str) -> str:
    out = []
    for line in text.splitlines():
        if any(ch in ART for ch in line):
            continue
        line = line.replace(">", " ").replace("*", " ").strip()
        out.append(line)
    return "\n".join(out)


def chunks(text: str, limit: int = 500):
    buf = ""
    for para in re.split(r"\n\s*\n", text):
        para = " ".join(para.split())
        if not para:
            continue
        if len(buf) + len(para) < limit:
            buf = (buf + " " + para).strip()
        else:
            if buf:
                yield buf
            buf = para
    if buf:
        yield buf


def synth(model: str, text: str, tok: str, dest: str, tries: int = 5) -> None:
    req = urllib.request.Request(
        "https://router.huggingface.co/hf-inference/models/" + model,
        data=json.dumps({"inputs": text}).encode("utf-8"),
        headers={"Authorization": "Bearer " + tok,
                 "Content-Type": "application/json"},
    )
    for attempt in range(tries):
        try:
            with urllib.request.urlopen(req, timeout=120) as resp:
                audio = resp.read()
            with open(dest, "wb") as fh:
                fh.write(audio)
            return
        except urllib.error.HTTPError as err:
            # 503 = model loading; wait and retry.
            if err.code == 503:
                time.sleep(15)
                continue
            raise
    raise RuntimeError("gave up on " + dest)


def main() -> None:
    lang = sys.argv[1] if len(sys.argv) > 1 else "en"
    game = sys.argv[2] if len(sys.argv) > 2 else "pong"
    src = os.path.join(HALL, f"{game}_{lang}.txt")
    text = clean(open(src, encoding="utf-8").read())
    tok = token()
    parts = []
    for i, chunk in enumerate(chunks(text)):
        dest = os.path.join(HALL, f"_{game}_{lang}_{i:02d}.flac")
        print(f"chunk {i}: {len(chunk)} chars -> {os.path.basename(dest)}")
        synth(MODELS[lang], chunk, tok, dest)
        parts.append(dest)
    listing = os.path.join(HALL, f"_{game}_{lang}.txt")
    with open(listing, "w", encoding="ascii") as fh:
        for p in parts:
            fh.write("file '" + p.replace("\\", "/") + "'\n")
    out = os.path.join(HALL, f"{game}_{lang}.mp3")
    subprocess.run(
        ["ffmpeg", "-y", "-f", "concat", "-safe", "0", "-i", listing,
         "-codec:a", "libmp3lame", "-q:a", "4", out],
        check=True, capture_output=True,
    )
    for p in parts:
        os.remove(p)
    os.remove(listing)
    print("WROTE", out, os.path.getsize(out))


if __name__ == "__main__":
    main()
