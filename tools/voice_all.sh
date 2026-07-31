#!/bin/bash
# Voice every EN lecture with the Lord Compton voice (am_michael, Kokoro).
cd /c/projects/pixel-arcade/tools
for f in ../hall/lectures/*_en.txt; do
  g=$(basename "$f" _en.txt)
  if [ -f "../hall/lectures/${g}_en.mp3" ]; then echo "$g skip (exists)"; continue; fi
  echo "=== $g ==="
  python tts_local.py en "$g" am_michael || echo "$g FAILED"
done
echo VOICE-ALL-DONE
