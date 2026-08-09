/**
 * Токен кошелька.
 *
 * Юджин решил: «токены, без выпендрежа». Он говорил про куку — но зал
 * живёт на github.io, а касса на workers.dev, то есть кука была бы
 * третьесторонней, а такие Chrome и Safari режут. Поэтому конверт —
 * localStorage + заголовок Authorization, а защита ровно та, что была
 * задумана: HMAC-подпись сервера + серийник в леджере против реплея.
 *
 * Формат: <walletId>.<serial>.<base64url(HMAC-SHA256)>
 * Подписывается строка "<walletId>.<serial>" секретом из env.
 */

const enc = new TextEncoder();

function b64urlEncode(bytes: ArrayBuffer): string {
  const b = new Uint8Array(bytes);
  let s = '';
  for (let i = 0; i < b.length; i++) s += String.fromCharCode(b[i]);
  return btoa(s).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '');
}

async function hmacKey(secret: string): Promise<CryptoKey> {
  return crypto.subtle.importKey(
    'raw',
    enc.encode(secret),
    { name: 'HMAC', hash: 'SHA-256' },
    false,
    ['sign'],
  );
}

async function sign(secret: string, payload: string): Promise<string> {
  const key = await hmacKey(secret);
  const sig = await crypto.subtle.sign('HMAC', key, enc.encode(payload));
  return b64urlEncode(sig);
}

/** Сравнение за постоянное время — чтобы подпись нельзя было подобрать по таймингу. */
function timingSafeEqual(a: string, b: string): boolean {
  if (a.length !== b.length) return false;
  let diff = 0;
  for (let i = 0; i < a.length; i++) diff |= a.charCodeAt(i) ^ b.charCodeAt(i);
  return diff === 0;
}

export async function mintToken(secret: string, walletId: string, serial: number): Promise<string> {
  const payload = `${walletId}.${serial}`;
  return `${payload}.${await sign(secret, payload)}`;
}

export interface ParsedToken {
  walletId: string;
  serial: number;
}

/**
 * Разбирает и ПРОВЕРЯЕТ подпись. Возвращает null на любой брак —
 * вызывающий не должен различать «кривой формат» и «плохая подпись».
 */
export async function parseToken(secret: string, token: string | null): Promise<ParsedToken | null> {
  if (!token) return null;
  const parts = token.split('.');
  if (parts.length !== 3) return null;

  const [walletId, serialRaw, sig] = parts;
  if (!/^[a-f0-9]{32}$/.test(walletId)) return null;
  if (!/^\d{1,15}$/.test(serialRaw)) return null;

  const expected = await sign(secret, `${walletId}.${serialRaw}`);
  if (!timingSafeEqual(sig, expected)) return null;

  return { walletId, serial: parseInt(serialRaw, 10) };
}

/** Достаёт Bearer-токен из заголовка Authorization. */
export function bearerFrom(request: Request): string | null {
  const h = request.headers.get('Authorization') || '';
  const m = /^Bearer\s+(.+)$/i.exec(h.trim());
  return m ? m[1] : null;
}

export function newWalletId(): string {
  const b = new Uint8Array(16);
  crypto.getRandomValues(b);
  return Array.from(b, x => x.toString(16).padStart(2, '0')).join('');
}
