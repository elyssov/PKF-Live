/* The Pixel Campfire — Arcade Hall.
   Касса → четвертаки в кошельке (localStorage) → карусель автоматов →
   монетка тащится в щель → звяк → автомат оживает.
   Демо-касса: монеты выдаются без PayPal (санбокс-капча придёт с Go-кассой). */

'use strict';

// ── Каталог автоматов ──────────────────────────────────────────────
// Первый живой экспонат — PONG (wasm). Остальные шильдики ждут порта.
const CABS = [
  {
    id: 'pong', title: 'PONG · 1972', ghost: 'PONG',
    playable: true, src: 'games/pong/index.html',
    plaque: 'Осень 1972-го. Andy Capp’s Tavern, пыльный угол у пивных ' +
      'кранов. Прототип Аллана Алкорна перестал принимать четвертаки через ' +
      'две недели — молочная канистра внутри захлебнулась монетами. ' +
      'Так индустрия узнала, что она индустрия. Веди ракетку мышкой.',
  },
  { id: 'snake', title: 'SNAKE · 1998', ghost: 'SNAKE', playable: false,
    plaque: 'Змейка, которая продала сто миллионов телефонов. Лекция и ' +
      'автомат приедут из музея — порт в пути.' },
  { id: 'brixout', title: 'BRIXOUT · 1986', ghost: 'BRIXOUT', playable: false,
    plaque: 'Кирпичи, ракетка и одна очень упрямая капсула. Порт в пути.' },
  { id: 'jungle', title: 'JUNGLE RUN · 1982', ghost: 'JUNGLE', playable: false,
    plaque: 'Крокодилы, лианы и золото. Порт в пути.' },
  { id: 'dungeon', title: 'DUNGEON · 1980', ghost: 'DUNGEON', playable: false,
    plaque: 'Рогалик с амулетом Йендора на пятом уровне. Порт в пути.' },
  { id: 'rocks', title: 'ROCK STORM · 1979', ghost: 'ROCKS', playable: false,
    plaque: 'Белые векторы на фосфорной трубе. Порт в пути.' },
];

const COINS_PER_DOLLAR = 8;
const LS_KEY = 'pxcf-arcade-wallet';

// ── Кошелёк ────────────────────────────────────────────────────────
let coins = parseInt(localStorage.getItem(LS_KEY) || '0', 10);

function saveWallet() { localStorage.setItem(LS_KEY, String(coins)); }

function renderWallet() {
  const stack = document.getElementById('coinStack');
  stack.innerHTML = '';
  const shown = Math.min(coins, 5);
  for (let i = 0; i < shown; i++) {
    const c = document.createElement('div');
    c.className = 'coin';
    c.textContent = '25¢';
    c.addEventListener('pointerdown', startDrag);
    stack.appendChild(c);
  }
  document.getElementById('coinCount').textContent = coins;
}

// ── Звук: звяк монетки и гудок кассы (WebAudio, без файлов) ───────
function clink() {
  const ctx = new (window.AudioContext || window.webkitAudioContext)();
  const hit = (freq, t0, dur, vol) => {
    const o = ctx.createOscillator();
    const g = ctx.createGain();
    o.type = 'triangle';
    o.frequency.setValueAtTime(freq, ctx.currentTime + t0);
    g.gain.setValueAtTime(vol, ctx.currentTime + t0);
    g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + t0 + dur);
    o.connect(g).connect(ctx.destination);
    o.start(ctx.currentTime + t0);
    o.stop(ctx.currentTime + t0 + dur);
  };
  hit(2450, 0, 0.09, 0.5);     // удар о железо
  hit(3100, 0.05, 0.12, 0.35); // отскок
  hit(1650, 0.13, 0.3, 0.25);  // качение на дно
}

function registerBell() {
  const ctx = new (window.AudioContext || window.webkitAudioContext)();
  [880, 1320].forEach((f, i) => {
    const o = ctx.createOscillator();
    const g = ctx.createGain();
    o.type = 'sine';
    o.frequency.value = f;
    g.gain.setValueAtTime(0.35, ctx.currentTime + i * 0.02);
    g.gain.exponentialRampToValueAtTime(0.001, ctx.currentTime + 0.5);
    o.connect(g).connect(ctx.destination);
    o.start(ctx.currentTime + i * 0.02);
    o.stop(ctx.currentTime + 0.6);
  });
}

function toast(msg) {
  const old = document.getElementById('toast');
  if (old) old.remove();
  const t = document.createElement('div');
  t.id = 'toast';
  t.textContent = msg;
  document.body.appendChild(t);
  setTimeout(() => t.remove(), 2600);
}

// ── Сцены ──────────────────────────────────────────────────────────
document.getElementById('buyBtn').addEventListener('click', () => {
  coins += COINS_PER_DOLLAR;
  saveWallet();
  renderWallet();
  registerBell();
  toast('+' + COINS_PER_DOLLAR + ' четвертаков. Касса говорит: приятной игры!');
});

document.getElementById('enterHall').addEventListener('click', () => {
  document.getElementById('lobby').classList.add('hidden');
  document.getElementById('hall').classList.remove('hidden');
});
document.getElementById('backLobby').addEventListener('click', () => {
  document.getElementById('hall').classList.add('hidden');
  document.getElementById('lobby').classList.remove('hidden');
});

// ── Карусель ───────────────────────────────────────────────────────
const carousel = document.getElementById('carousel');

function buildCab(cab) {
  const el = document.createElement('div');
  el.className = 'cab' + (cab.playable ? '' : ' soon');
  el.dataset.id = cab.id;
  el.innerHTML =
    '<div class="cab-marquee">' + cab.title + '</div>' +
    '<div class="cab-body">' +
      '<div class="cab-screen">' +
        '<div class="attract">' +
          '<div class="game-ghost">' + cab.ghost + '</div>' +
          '<div class="insert-coin">' +
            (cab.playable ? 'INSERT COIN' : 'SOON') +
          '</div>' +
        '</div>' +
      '</div>' +
      '<div class="coin-door' + (cab.playable ? '' : ' dead') + '">' +
        '<div class="coin-slot"></div>' +
        '<div class="coin-door-label">25¢</div>' +
      '</div>' +
      '<button class="plaque-btn">▸ музейная табличка (бесплатно)</button>' +
      '<div class="plaque"><b>' + cab.title + '</b>' + cab.plaque + '</div>' +
    '</div>';
  el.querySelector('.plaque-btn').addEventListener('click', () => {
    el.querySelector('.plaque').classList.toggle('open');
  });
  return el;
}

CABS.forEach(cab => carousel.appendChild(buildCab(cab)));

document.getElementById('prevCab').addEventListener('click', () =>
  carousel.scrollBy({ left: -380, behavior: 'smooth' }));
document.getElementById('nextCab').addEventListener('click', () =>
  carousel.scrollBy({ left: 380, behavior: 'smooth' }));

// ── Монетка: перетаскивание в щель ─────────────────────────────────
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
  // Монетка принята.
  coins -= 1;
  saveWallet();
  renderWallet();
  clink();
  powerOn(cabEl, cab);
}

function powerOn(cabEl, cab) {
  cabEl.classList.add('active-cab');
  const screen = cabEl.querySelector('.cab-screen');
  const attract = screen.querySelector('.attract');
  attract.style.transition = 'opacity 0.6s';
  attract.style.opacity = '0';
  setTimeout(() => {
    attract.remove();
    const frame = document.createElement('iframe');
    frame.src = cab.src;
    frame.setAttribute('allow', 'autoplay');
    screen.appendChild(frame);
  }, 620);
  document.getElementById('hallHint').textContent =
    'Автомат включён. Ракетка ведётся мышкой прямо на экране автомата.';
}

// ── Старт ──────────────────────────────────────────────────────────
renderWallet();
// Прямой вход в зал по ссылке (и для скриншот-харнесса): index.html#hall
if (location.hash === '#hall') {
  document.getElementById('lobby').classList.add('hidden');
  document.getElementById('hall').classList.remove('hidden');
}
