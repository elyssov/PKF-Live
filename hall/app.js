/* The Pixel Campfire — Arcade Hall v2.
   Касса → четвертаки → зал с НАСТОЯЩИМИ корпусами музея → монетка в щель →
   звяк → ЗУМ в стекло автомата → игра во весь экран. Экскурсовод: полная
   музейная лекция + аудио (speechSynthesis EN/RU; HF-голос придёт поверх). */

'use strict';

// ── Каталог автоматов ──────────────────────────────────────────────
// glass = стекло корпуса в долях фотографии (те же Rect, что в ScreenEra.kt).
const CABS = [
  {
    id: 'pong', title: 'PONG · 1972', ghost: 'PONG',
    playable: true, src: 'games/pong/index.html',
    photo: 'assets/bezels/tavern_tennis.png', ar: '1144 / 1375',
    glass: { l: 21.8, t: 28.2, w: 55.9, h: 39.5 },
    lecture: { en: 'lectures/pong_en.txt', ru: 'lectures/pong_ru.txt' },
  },
  {
    id: 'snake', title: 'SNAKE · 1998', ghost: 'SNAKE', playable: false,
    photo: 'assets/bezels/nokla_phone.png', ar: '729 / 691',
    glass: { l: 14.4, t: 26.5, w: 71.3, h: 54.0 },
  },
  {
    id: 'brixout', title: 'BRIXOUT · 1986', ghost: 'BRIXOUT', playable: false,
    photo: 'assets/bezels/elgee_svga.png', ar: '1364 / 1153',
    glass: { l: 14.4, t: 14.4, w: 71.2, h: 62.5 },
  },
  {
    id: 'jungle', title: 'JUNGLE RUN · 1982', ghost: 'JUNGLE', playable: false,
    photo: 'assets/bezels/vectra77.png', ar: '1536 / 1024',
    glass: { l: 10.9, t: 14.2, w: 63.9, h: 65.6 },
  },
  {
    id: 'dungeon', title: 'DUNGEON · 1980', ghost: 'DUNGEON', playable: false,
    photo: 'assets/bezels/mtec_ega.png', ar: '1402 / 1122',
    glass: { l: 16.8, t: 14.6, w: 66.1, h: 59.2 },
  },
  {
    id: 'muncher', title: 'MUNCHER · 1980', ghost: 'MUNCHER', playable: false,
    photo: 'assets/bezels/gnusmas14.png', ar: '1394 / 1128',
    glass: { l: 8.8, t: 11.8, w: 71.2, h: 65.8 },
  },
];

const COINS_PER_DOLLAR = 8;
const LS_KEY = 'pxcf-arcade-wallet';

// ── Кошелёк ────────────────────────────────────────────────────────
let coins = parseInt(localStorage.getItem(LS_KEY) || '0', 10);
function saveWallet() { localStorage.setItem(LS_KEY, String(coins)); }

function renderWallet() {
  const stack = document.getElementById('coinStack');
  stack.innerHTML = '';
  for (let i = 0; i < Math.min(coins, 5); i++) {
    const c = document.createElement('div');
    c.className = 'coin';
    c.textContent = '25¢';
    c.addEventListener('pointerdown', startDrag);
    stack.appendChild(c);
  }
  document.getElementById('coinCount').textContent = coins;
}

// ── Звуки (WebAudio, без файлов) ───────────────────────────────────
function clink() {
  const ctx = new (window.AudioContext || window.webkitAudioContext)();
  const hit = (freq, t0, dur, vol) => {
    const o = ctx.createOscillator(), g = ctx.createGain();
    o.type = 'triangle';
    o.frequency.setValueAtTime(freq, ctx.currentTime + t0);
    g.gain.setValueAtTime(vol, ctx.currentTime + t0);
    g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + t0 + dur);
    o.connect(g).connect(ctx.destination);
    o.start(ctx.currentTime + t0); o.stop(ctx.currentTime + t0 + dur);
  };
  hit(2450, 0, 0.09, 0.5); hit(3100, 0.05, 0.12, 0.35); hit(1650, 0.13, 0.3, 0.25);
}

function registerBell() {
  const ctx = new (window.AudioContext || window.webkitAudioContext)();
  [880, 1320].forEach((f, i) => {
    const o = ctx.createOscillator(), g = ctx.createGain();
    o.type = 'sine'; o.frequency.value = f;
    g.gain.setValueAtTime(0.35, ctx.currentTime + i * 0.02);
    g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);
    o.connect(g).connect(ctx.destination);
    o.start(ctx.currentTime + i * 0.02); o.stop(ctx.currentTime + 0.6);
  });
}

function toast(msg) {
  const old = document.getElementById('toast');
  if (old) old.remove();
  const t = document.createElement('div');
  t.id = 'toast'; t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 2600);
}

// ── Сцены ──────────────────────────────────────────────────────────
document.getElementById('buyBtn').addEventListener('click', () => {
  coins += COINS_PER_DOLLAR;
  saveWallet(); renderWallet(); registerBell();
  toast('+' + COINS_PER_DOLLAR + ' четвертаков. Приятной игры!');
});
document.getElementById('enterHall').addEventListener('click', () => {
  document.getElementById('lobby').classList.add('hidden');
  document.getElementById('hall').classList.remove('hidden');
});
document.getElementById('backLobby').addEventListener('click', () => {
  document.getElementById('hall').classList.add('hidden');
  document.getElementById('lobby').classList.remove('hidden');
});

// ── Кабинеты ───────────────────────────────────────────────────────
const carousel = document.getElementById('carousel');

function buildCab(cab) {
  const g = cab.glass;
  const el = document.createElement('div');
  el.className = 'cab' + (cab.playable ? '' : ' soon');
  el.dataset.id = cab.id;
  el.innerHTML =
    '<div class="cab-marquee">' + cab.title + '</div>' +
    '<div class="cab-photo" style="background-image:url(' + cab.photo + ');aspect-ratio:' + cab.ar + '">' +
      '<div class="cab-screen" style="left:' + g.l + '%;top:' + g.t + '%;width:' + g.w + '%;height:' + g.h + '%">' +
        '<div class="attract">' +
          '<div class="game-ghost">' + cab.ghost + '</div>' +
          '<div class="insert-coin">' + (cab.playable ? 'INSERT COIN' : 'SOON') + '</div>' +
        '</div>' +
      '</div>' +
    '</div>' +
    '<div class="cab-base">' +
      '<div class="coin-door' + (cab.playable ? '' : ' dead') + '">' +
        '<div class="coin-slot"></div><div class="coin-door-label">25¢</div>' +
      '</div>' +
      '<button class="guide-btn">🔔 ПОЗВАТЬ ЭКСКУРСОВОДА</button>' +
    '</div>';
  el.querySelector('.guide-btn').addEventListener('click', () => openDocent(cab));
  return el;
}

CABS.forEach(cab => carousel.appendChild(buildCab(cab)));
document.getElementById('prevCab').addEventListener('click', () =>
  carousel.scrollBy({ left: -440, behavior: 'smooth' }));
document.getElementById('nextCab').addEventListener('click', () =>
  carousel.scrollBy({ left: 440, behavior: 'smooth' }));

// ── Экскурсовод: лекция + озвучка ──────────────────────────────────
const docent = document.getElementById('docent');
let speechOn = false;

async function loadLecture(cab, lang) {
  if (!cab.lecture || !cab.lecture[lang]) return null;
  try {
    const r = await fetch(cab.lecture[lang]);
    return r.ok ? await r.text() : null;
  } catch (e) { return null; }
}

async function openDocent(cab) {
  docent.classList.remove('hidden');
  document.getElementById('docentTitle').textContent = cab.title;
  const body = document.getElementById('docentBody');
  const lang = document.getElementById('langRu').checked ? 'ru' : 'en';
  const text = await loadLecture(cab, lang);
  body.textContent = text ||
    'Экскурсовод разводит руками: материалы по этому экспонату ещё в пути.\n' +
    'Лекция приедет вместе с самим автоматом.';
  body.scrollTop = 0;
  docent.dataset.cabId = cab.id;
}

document.getElementById('docentClose').addEventListener('click', () => {
  docent.classList.add('hidden');
  stopSpeech();
});
document.querySelectorAll('input[name="lang"]').forEach(r =>
  r.addEventListener('change', () => {
    const cab = CABS.find(c => c.id === docent.dataset.cabId);
    if (cab && !docent.classList.contains('hidden')) openDocent(cab);
  }));

// Аудиолекция. Приоритет — студийные mp3 (Kokoro-82M, генерятся локально
// в tools/tts_local.py); если mp3 для экспоната ещё нет — браузерный
// speechSynthesis (EN/RU из коробки). Играет фоном во время игры.
let audioEl = null;

async function mp3For(cabId, lang) {
  const url = 'lectures/' + cabId + '_' + lang + '.mp3';
  try {
    const r = await fetch(url, { method: 'HEAD' });
    return r.ok ? url : null;
  } catch (e) { return null; }
}
function pickVoice(lang) {
  const pref = lang === 'ru' ? 'ru' : 'en';
  const voices = speechSynthesis.getVoices();
  return voices.find(v => v.lang.toLowerCase().startsWith(pref) && v.localService) ||
         voices.find(v => v.lang.toLowerCase().startsWith(pref)) || null;
}

async function startSpeech() {
  const body = document.getElementById('docentBody').textContent;
  const lang = document.getElementById('langRu').checked ? 'ru' : 'en';
  // Студийная дорожка, если записана.
  const mp3 = await mp3For(docent.dataset.cabId, lang);
  if (mp3) {
    audioEl = new Audio(mp3);
    audioEl.onended = () => { speechOn = false; renderSpeechBtn(); };
    audioEl.play();
    speechOn = true; renderSpeechBtn();
    return;
  }
  // Чистим псевдографику — она не для ушей.
  const clean = body.replace(/[│┌┐└┘├┤─═║╔╗╚╝█▓▒░╭╮╰╯>*]/g, ' ')
                    .replace(/\s+/g, ' ').trim();
  if (!clean) return;
  const u = new SpeechSynthesisUtterance(clean);
  u.lang = lang === 'ru' ? 'ru-RU' : 'en-US';
  const v = pickVoice(lang);
  if (v) u.voice = v;
  u.rate = 0.95;
  u.onend = () => { speechOn = false; renderSpeechBtn(); };
  speechSynthesis.cancel();
  speechSynthesis.speak(u);
  speechOn = true; renderSpeechBtn();
}

function stopSpeech() {
  speechSynthesis.cancel();
  if (audioEl) { audioEl.pause(); audioEl = null; }
  speechOn = false; renderSpeechBtn();
}

function renderSpeechBtn() {
  document.getElementById('speakBtn').textContent =
    speechOn ? '■ ОСТАНОВИТЬ ЛЕКЦИЮ' : '▶ АУДИОЛЕКЦИЯ';
}

document.getElementById('speakBtn').addEventListener('click', () =>
  speechOn ? stopSpeech() : startSpeech());

// ── Монетка → щель → ЗУМ в стекло → игра ───────────────────────────
const dragCoin = document.getElementById('dragCoin');
let dragging = false;

function startDrag(e) {
  if (coins <= 0) return;
  dragging = true;
  dragCoin.classList.remove('hidden');
  moveDrag(e);
  window.addEventListener('pointermove', moveDrag);
  window.addEventListener('pointerup', endDrag, { once: true });
}
function moveDrag(e) {
  dragCoin.style.left = e.clientX + 'px';
  dragCoin.style.top = e.clientY + 'px';
  const door = doorUnder(e.clientX, e.clientY);
  document.querySelectorAll('.coin-door').forEach(d =>
    d.classList.toggle('hot', d === door));
}
function doorUnder(x, y) {
  const el = document.elementFromPoint(x, y);
  const door = el && el.closest('.coin-door');
  return (door && !door.classList.contains('dead')) ? door : null;
}
function endDrag(e) {
  window.removeEventListener('pointermove', moveDrag);
  dragCoin.classList.add('hidden');
  document.querySelectorAll('.coin-door').forEach(d => d.classList.remove('hot'));
  if (!dragging) return;
  dragging = false;
  const door = doorUnder(e.clientX, e.clientY);
  if (!door) return;
  const cabEl = door.closest('.cab');
  const cab = CABS.find(c => c.id === cabEl.dataset.id);
  if (!cab || !cab.playable) return;
  coins -= 1; saveWallet(); renderWallet();
  clink();
  zoomIntoGame(cabEl, cab);
}

/* Зум: чёрная «трубка» стартует ровно из стекла автомата и раздувается
   на весь экран; внутри включается игра. Выход — кнопка на рамке. */
function zoomIntoGame(cabEl, cab) {
  const glass = cabEl.querySelector('.cab-screen').getBoundingClientRect();
  const stage = document.createElement('div');
  stage.id = 'gameStage';
  stage.style.left = glass.left + 'px';
  stage.style.top = glass.top + 'px';
  stage.style.width = glass.width + 'px';
  stage.style.height = glass.height + 'px';
  stage.innerHTML = '<div class="stage-glow"></div>';
  document.body.appendChild(stage);
  // Форсируем layout, затем анимируем к фуллскрину.
  stage.getBoundingClientRect();
  stage.classList.add('grow');
  stage.style.left = '0px'; stage.style.top = '0px';
  stage.style.width = '100vw'; stage.style.height = '100vh';
  setTimeout(() => {
    const frame = document.createElement('iframe');
    frame.src = cab.src;
    frame.setAttribute('allow', 'autoplay');
    stage.appendChild(frame);
    const exit = document.createElement('button');
    exit.id = 'stageExit';
    exit.textContent = '◂ ВЫЙТИ ИЗ АВТОМАТА';
    exit.addEventListener('click', () => { stage.remove(); });
    stage.appendChild(exit);
    frame.addEventListener('load', () => frame.focus());
    document.getElementById('hallHint').textContent =
      'Стрелки/W-S — ракетка (мышь тоже работает). Экскурсовод может рассказывать фоном.';
  }, 950);
}

// ── Старт ──────────────────────────────────────────────────────────
renderWallet();
if (location.hash === '#hall') {
  document.getElementById('lobby').classList.add('hidden');
  document.getElementById('hall').classList.remove('hidden');
}
// Голоса подгружаются асинхронно — прогреваем список.
if ('speechSynthesis' in window) speechSynthesis.getVoices();
