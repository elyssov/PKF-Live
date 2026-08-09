/**
 * Касса «Пиксельного костра» — Cloudflare Worker + D1.
 *
 * Что здесь происходит:
 *   POST /api/order    — создать заказ на пачку четвертаков
 *   POST /api/capture  — подтвердить оплату НА СЕРВЕРЕ и начислить монеты
 *   GET  /api/wallet   — баланс по токену
 *   POST /api/spend    — монетка в щель: списать один четвертак
 *   GET  /api/packs    — витрина пачек (цены сервера, не клиента)
 *   GET  /api/health   — жив ли, какой провайдер включён
 *
 * Три правила, на которых всё держится:
 *   1. Деньги считает только сервер. Клиент присылает id пачки и id игры.
 *   2. Подтверждение оплаты = ответ провайдера на НАШ capture, не редирект.
 *   3. Токен подписан HMAC и несёт serial; serial из леджера растёт на
 *      каждую операцию, поэтому старая копия токена не воскрешает баланс.
 */

import { bearerFrom, mintToken, newWalletId, parseToken } from './token';
import { PACKS, packById } from './packs';
import { pickProvider } from './providers';

export interface Env {
  DB: D1Database;
  WALLET_SECRET: string;
  ALLOWED_ORIGINS?: string;
  PAYPAL_CLIENT_ID?: string;
  PAYPAL_SECRET?: string;
  PAYPAL_ENV?: string;
}

const DEFAULT_ORIGINS = [
  'https://elyssov.github.io',
  'http://localhost:8137',
  'http://127.0.0.1:8137',
  'http://localhost:8000',
  'http://127.0.0.1:8000',
];

function allowedOrigins(env: Env): string[] {
  const extra = (env.ALLOWED_ORIGINS || '')
    .split(',')
    .map(s => s.trim())
    .filter(Boolean);
  return [...DEFAULT_ORIGINS, ...extra];
}

function corsHeaders(request: Request, env: Env): Record<string, string> {
  const origin = request.headers.get('Origin') || '';
  const ok = allowedOrigins(env).includes(origin);
  return {
    'Access-Control-Allow-Origin': ok ? origin : DEFAULT_ORIGINS[0],
    'Access-Control-Allow-Methods': 'GET,POST,OPTIONS',
    'Access-Control-Allow-Headers': 'Content-Type,Authorization',
    'Access-Control-Max-Age': '86400',
    Vary: 'Origin',
  };
}

function json(body: unknown, init: ResponseInit, request: Request, env: Env): Response {
  return new Response(JSON.stringify(body), {
    ...init,
    headers: {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-store',
      ...corsHeaders(request, env),
      ...(init.headers || {}),
    },
  });
}

const ok = (b: unknown, rq: Request, e: Env) => json(b, { status: 200 }, rq, e);
const bad = (msg: string, rq: Request, e: Env, code = 400) =>
  json({ error: msg }, { status: code }, rq, e);

// ── Кошелёк ───────────────────────────────────────────────────────────

interface WalletRow {
  id: string;
  coins: number;
  serial: number;
}

/**
 * Достаёт кошелёк по Bearer-токену.
 *
 * Возвращает 'stale', когда подпись верна, но serial отстал: это либо
 * реплей старой копии, либо гонка двух вкладок. Ответ на оба случая
 * одинаковый и мягкий — отдать текущий баланс и свежий токен, ничего не
 * начисляя и не списывая.
 */
async function loadWallet(
  env: Env,
  request: Request,
): Promise<{ row: WalletRow; stale: boolean } | null> {
  const parsed = await parseToken(env.WALLET_SECRET, bearerFrom(request));
  if (!parsed) return null;

  const row = await env.DB.prepare('SELECT id, coins, serial FROM wallets WHERE id = ?')
    .bind(parsed.walletId)
    .first<WalletRow>();
  if (!row) return null;

  return { row, stale: row.serial !== parsed.serial };
}

async function walletPayload(env: Env, row: WalletRow) {
  return {
    coins: row.coins,
    token: await mintToken(env.WALLET_SECRET, row.id, row.serial),
  };
}

// ── Обработчики ───────────────────────────────────────────────────────

async function handlePacks(request: Request, env: Env): Promise<Response> {
  return ok({ packs: Object.values(PACKS) }, request, env);
}

async function handleHealth(request: Request, env: Env): Promise<Response> {
  const provider = pickProvider(env as unknown as Record<string, unknown>);
  return ok(
    {
      status: 'open',
      provider: provider.name,
      // Честно наружу: в mock-режиме деньги ненастоящие, и зал обязан
      // сказать это игроку словами, а не мелким шрифтом.
      demo: provider.name === 'mock',
    },
    request,
    env,
  );
}

async function handleWallet(request: Request, env: Env): Promise<Response> {
  const w = await loadWallet(env, request);
  if (!w) return bad('no wallet', request, env, 401);
  return ok(await walletPayload(env, w.row), request, env);
}

/**
 * Создать заказ. Кошелёк заводится ЛЕНИВО — ровно здесь, при первой
 * покупке: пустые кошельки не занимают базу и не дают спамить строками.
 */
async function handleOrder(request: Request, env: Env): Promise<Response> {
  const body = (await request.json().catch(() => ({}))) as Record<string, unknown>;
  const pack = packById(body.pack);
  if (!pack) return bad('unknown pack', request, env);

  const now = Date.now();
  let row: WalletRow | null = null;

  const existing = await loadWallet(env, request);
  if (existing) row = existing.row;

  if (!row) {
    const id = newWalletId();
    await env.DB.prepare(
      'INSERT INTO wallets (id, coins, serial, created_at, updated_at) VALUES (?, 0, 0, ?, ?)',
    )
      .bind(id, now, now)
      .run();
    row = { id, coins: 0, serial: 0 };
  }

  const provider = pickProvider(env as unknown as Record<string, unknown>);
  let created;
  try {
    created = await provider.createOrder(pack);
  } catch (e) {
    return bad(`provider error: ${(e as Error).message}`, request, env, 502);
  }

  await env.DB.prepare(
    `INSERT INTO payments
       (order_id, wallet_id, provider, pack_id, coins, amount_cents, currency, status, created_at)
     VALUES (?, ?, ?, ?, ?, ?, ?, 'created', ?)`,
  )
    .bind(
      created.orderId,
      row.id,
      provider.name,
      pack.id,
      pack.coins,
      pack.amountCents,
      pack.currency,
      now,
    )
    .run();

  return ok(
    {
      orderId: created.orderId,
      client: created.clientData,
      ...(await walletPayload(env, row)),
    },
    request,
    env,
  );
}

/**
 * Подтвердить оплату и начислить монеты.
 *
 * Идемпотентность: заказ переводится в 'completed' одним условным
 * UPDATE. Начисляем ТОЛЬКО если этот UPDATE реально изменил строку —
 * значит именно мы выиграли гонку. Повторный вызов увидит 0 изменённых
 * строк и просто вернёт текущий баланс.
 */
async function handleCapture(request: Request, env: Env): Promise<Response> {
  const body = (await request.json().catch(() => ({}))) as Record<string, unknown>;
  const orderId = typeof body.orderId === 'string' ? body.orderId : '';
  if (!orderId) return bad('orderId required', request, env);

  const w = await loadWallet(env, request);
  if (!w) return bad('no wallet', request, env, 401);

  const pay = await env.DB.prepare(
    'SELECT order_id, wallet_id, coins, status FROM payments WHERE order_id = ?',
  )
    .bind(orderId)
    .first<{ order_id: string; wallet_id: string; coins: number; status: string }>();

  if (!pay) return bad('unknown order', request, env, 404);
  if (pay.wallet_id !== w.row.id) return bad('order belongs to another wallet', request, env, 403);

  if (pay.status === 'completed') {
    return ok({ ...(await walletPayload(env, w.row)), credited: 0, already: true }, request, env);
  }

  const provider = pickProvider(env as unknown as Record<string, unknown>);
  let result;
  try {
    result = await provider.captureOrder(orderId);
  } catch (e) {
    return bad(`provider error: ${(e as Error).message}`, request, env, 502);
  }
  if (!result.ok) {
    return json(
      { error: 'payment not completed', status: result.status, detail: result.error },
      { status: 402 },
      request,
      env,
    );
  }

  const now = Date.now();
  const claim = await env.DB.prepare(
    `UPDATE payments SET status = 'completed', capture_id = ?, captured_at = ?
      WHERE order_id = ? AND status = 'created'`,
  )
    .bind(result.captureId || null, now, orderId)
    .run();

  const won = (claim.meta?.changes ?? 0) > 0;
  if (!won) {
    const fresh = await env.DB.prepare('SELECT id, coins, serial FROM wallets WHERE id = ?')
      .bind(w.row.id)
      .first<WalletRow>();
    return ok(
      { ...(await walletPayload(env, fresh || w.row)), credited: 0, already: true },
      request,
      env,
    );
  }

  // Начисление. Порядок «пометить → начислить» выбран сознательно: сбой
  // между шагами оставит игрока без монет по оплаченному заказу (чиним
  // по квитанции руками), тогда как обратный порядок при том же сбое
  // выдал бы монеты дважды за один платёж.
  await env.DB.prepare(
    'UPDATE wallets SET coins = coins + ?, serial = serial + 1, updated_at = ? WHERE id = ?',
  )
    .bind(pay.coins, now, w.row.id)
    .run();

  const fresh = await env.DB.prepare('SELECT id, coins, serial FROM wallets WHERE id = ?')
    .bind(w.row.id)
    .first<WalletRow>();
  if (!fresh) return bad('wallet vanished', request, env, 500);

  return ok({ ...(await walletPayload(env, fresh)), credited: pay.coins }, request, env);
}

/**
 * Монетка в щель. Списание — одним условным UPDATE, поэтому две вкладки
 * не потратят одну монету дважды: вторая увидит несовпавший serial.
 */
async function handleSpend(request: Request, env: Env): Promise<Response> {
  const body = (await request.json().catch(() => ({}))) as Record<string, unknown>;
  const gameId = typeof body.game === 'string' ? body.game.slice(0, 64) : '';
  if (!gameId) return bad('game required', request, env);

  const w = await loadWallet(env, request);
  if (!w) return bad('no wallet', request, env, 401);

  if (w.stale) {
    return json(
      { error: 'stale token', ...(await walletPayload(env, w.row)) },
      { status: 409 },
      request,
      env,
    );
  }
  if (w.row.coins <= 0) {
    return json(
      { error: 'no coins', ...(await walletPayload(env, w.row)) },
      { status: 402 },
      request,
      env,
    );
  }

  const now = Date.now();
  const res = await env.DB.prepare(
    `UPDATE wallets SET coins = coins - 1, serial = serial + 1, updated_at = ?
      WHERE id = ? AND serial = ? AND coins > 0`,
  )
    .bind(now, w.row.id, w.row.serial)
    .run();

  if ((res.meta?.changes ?? 0) === 0) {
    const fresh = await env.DB.prepare('SELECT id, coins, serial FROM wallets WHERE id = ?')
      .bind(w.row.id)
      .first<WalletRow>();
    return json(
      { error: 'race lost', ...(await walletPayload(env, fresh || w.row)) },
      { status: 409 },
      request,
      env,
    );
  }

  await env.DB.prepare('INSERT INTO spends (wallet_id, game_id, created_at) VALUES (?, ?, ?)')
    .bind(w.row.id, gameId, now)
    .run();

  const fresh = await env.DB.prepare('SELECT id, coins, serial FROM wallets WHERE id = ?')
    .bind(w.row.id)
    .first<WalletRow>();
  if (!fresh) return bad('wallet vanished', request, env, 500);

  return ok({ ...(await walletPayload(env, fresh)), game: gameId }, request, env);
}

// ── Роутер ────────────────────────────────────────────────────────────

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    if (request.method === 'OPTIONS') {
      return new Response(null, { status: 204, headers: corsHeaders(request, env) });
    }

    if (!env.WALLET_SECRET) {
      return bad('WALLET_SECRET is not configured', request, env, 500);
    }

    const url = new URL(request.url);
    const path = url.pathname.replace(/\/+$/, '') || '/';
    const method = request.method.toUpperCase();

    try {
      if (method === 'GET' && path === '/api/health') return await handleHealth(request, env);
      if (method === 'GET' && path === '/api/packs') return await handlePacks(request, env);
      if (method === 'GET' && path === '/api/wallet') return await handleWallet(request, env);
      if (method === 'POST' && path === '/api/order') return await handleOrder(request, env);
      if (method === 'POST' && path === '/api/capture') return await handleCapture(request, env);
      if (method === 'POST' && path === '/api/spend') return await handleSpend(request, env);
      return bad('not found', request, env, 404);
    } catch (e) {
      return bad(`cashier error: ${(e as Error).message}`, request, env, 500);
    }
  },
};
