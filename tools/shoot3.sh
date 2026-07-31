#!/bin/bash
EDGE="/c/Program Files (x86)/Microsoft/Edge/Application/msedge.exe"
OUT=/c/projects/pixel-arcade/hall/assets/shots
for g in "$@"; do
  [ -s "$OUT/$g.jpg" ] && { echo "$g skip"; continue; }
  P="$LOCALAPPDATA/Temp/claude/edge-g2-$g"
  "$EDGE" --headless=new --disable-gpu --user-data-dir="$(cygpath -w "$P")" \
    --window-size=1280,960 --virtual-time-budget=22000 \
    --screenshot="$(cygpath -w "$OUT/_$g.png")" \
    "http://localhost:8137/games/arcade/index.html?game=$g&shot=1" 2>/dev/null &
  for i in $(seq 1 45); do [ -s "$OUT/_$g.png" ] && break; sleep 1; done
  sleep 1
  if [ -s "$OUT/_$g.png" ]; then
    magick "$OUT/_$g.png" -resize 640x "$OUT/$g.jpg" && rm -f "$OUT/_$g.png" && echo "$g OK"
  else
    echo "$g MISSING"
  fi
done
echo CHUNK-DONE
