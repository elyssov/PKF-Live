"""Voice the museum lectures LOCALLY with Kokoro-82M (onnx, CPU).

No API, no credits — the docent lives on this laptop.
Usage: python tts_local.py [en] [pong] [voice]
Output: hall/lectures/<game>_<lang>.mp3
"""
import os
import re
import subprocess
import sys

import numpy as np
import soundfile as sf
from kokoro_onnx import Kokoro

HERE = os.path.dirname(os.path.abspath(__file__))
HALL = os.path.join(HERE, "..", "hall", "lectures")
ART = "│┌┐└┘├┤─═║╔╗╚╝█▓▒░╭╮╰╯"


def clean(text: str) -> str:
    out = []
    for line in text.splitlines():
        if any(ch in ART for ch in line):
            continue
        out.append(line.replace(">", " ").replace("*", " ").strip())
    return "\n".join(out)


def chunks(text: str, limit: int = 600):
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


def main() -> None:
    lang = sys.argv[1] if len(sys.argv) > 1 else "en"
    game = sys.argv[2] if len(sys.argv) > 2 else "pong"
    voice = sys.argv[3] if len(sys.argv) > 3 else "am_michael"
    text = clean(open(os.path.join(HALL, f"{game}_{lang}.txt"), encoding="utf-8").read())

    kokoro = Kokoro(os.path.join(HERE, "kokoro-v1.0.onnx"),
                    os.path.join(HERE, "voices-v1.0.bin"))
    pieces = []
    sr = 24000
    for i, chunk in enumerate(chunks(text)):
        print(f"chunk {i}: {len(chunk)} chars")
        samples, sr = kokoro.create(chunk, voice=voice, speed=0.98, lang="en-us")
        pieces.append(samples)
        pieces.append(np.zeros(int(sr * 0.45), dtype=samples.dtype))  # breath

    wav = os.path.join(HALL, f"_{game}_{lang}.wav")
    sf.write(wav, np.concatenate(pieces), sr)
    out = os.path.join(HALL, f"{game}_{lang}.mp3")
    subprocess.run(["ffmpeg", "-y", "-i", wav, "-codec:a", "libmp3lame",
                    "-q:a", "4", out], check=True, capture_output=True)
    os.remove(wav)
    print("WROTE", out, os.path.getsize(out))


if __name__ == "__main__":
    main()
