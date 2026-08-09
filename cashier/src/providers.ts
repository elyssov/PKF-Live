/**
 * Провайдеры оплаты.
 *
 * Контракт один на всех: создать заказ → (человек платит) → capture на
 * НАШЕМ сервере. Подтверждение оплаты — это ответ провайдера на наш
 * серверный capture, а НЕ редирект браузера (адресную строку подделает
 * кто угодно). Это записано в docs/PAYMENTS.md и здесь исполняется.
 *
 * Сейчас включён mock: весь контур — леджер, подпись, монетки, ритуал —
 * гоняется и отлаживается без единого ключа. Когда появятся sandbox-ключи
 * PayPal, меняется РОВНО реализация ниже, остальной код не трогаем.
 */

import type { Pack } from './packs';

export interface CreatedOrder {
  orderId: string;
  /** Что отдать фронту, чтобы он открыл окно оплаты. */
  clientData: Record<string, unknown>;
}

export interface CaptureResult {
  ok: boolean;
  captureId?: string;
  status: string;
  error?: string;
}

export interface PaymentProvider {
  readonly name: string;
  createOrder(pack: Pack): Promise<CreatedOrder>;
  captureOrder(orderId: string): Promise<CaptureResult>;
}

// ── Mock ──────────────────────────────────────────────────────────────
// Изображает честный двухшаговый поток: заказ создаётся, capture
// подтверждает. Ничего не списывает и никуда не ходит.

export class MockProvider implements PaymentProvider {
  readonly name = 'mock';

  async createOrder(pack: Pack): Promise<CreatedOrder> {
    const b = new Uint8Array(12);
    crypto.getRandomValues(b);
    const orderId = 'MOCK-' + Array.from(b, x => x.toString(16).padStart(2, '0')).join('');
    return {
      orderId,
      clientData: {
        provider: 'mock',
        // Фронт по этому флагу рисует «демо-оплату» вместо окна PayPal —
        // чтобы на экране НИКОГДА не выглядело, будто деньги настоящие.
        demo: true,
        pack: pack.id,
        amountCents: pack.amountCents,
        currency: pack.currency,
      },
    };
  }

  async captureOrder(orderId: string): Promise<CaptureResult> {
    return { ok: true, captureId: 'MOCKCAP-' + orderId.slice(5, 17), status: 'COMPLETED' };
  }
}

// ── PayPal Orders v2 ──────────────────────────────────────────────────

interface PayPalEnv {
  clientId: string;
  secret: string;
  /** true → api-m.sandbox.paypal.com, false → боевой */
  sandbox: boolean;
}

export class PayPalProvider implements PaymentProvider {
  readonly name = 'paypal';
  private env: PayPalEnv;
  private token: { value: string; expiresAt: number } | null = null;

  constructor(env: PayPalEnv) {
    this.env = env;
  }

  private get base(): string {
    return this.env.sandbox ? 'https://api-m.sandbox.paypal.com' : 'https://api-m.paypal.com';
  }

  /** OAuth2 client_credentials; держим до истечения минус минута запаса. */
  private async accessToken(): Promise<string> {
    const now = Date.now();
    if (this.token && this.token.expiresAt > now) return this.token.value;

    const basic = btoa(`${this.env.clientId}:${this.env.secret}`);
    const r = await fetch(`${this.base}/v1/oauth2/token`, {
      method: 'POST',
      headers: {
        Authorization: `Basic ${basic}`,
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      body: 'grant_type=client_credentials',
    });
    if (!r.ok) throw new Error(`paypal auth failed: ${r.status}`);
    const j = (await r.json()) as { access_token: string; expires_in: number };
    this.token = { value: j.access_token, expiresAt: now + (j.expires_in - 60) * 1000 };
    return this.token.value;
  }

  async createOrder(pack: Pack): Promise<CreatedOrder> {
    const at = await this.accessToken();
    const value = (pack.amountCents / 100).toFixed(2);
    const r = await fetch(`${this.base}/v2/checkout/orders`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${at}`, 'Content-Type': 'application/json' },
      body: JSON.stringify({
        intent: 'CAPTURE',
        purchase_units: [
          {
            amount: { currency_code: pack.currency, value },
            description: pack.title,
            custom_id: pack.id,
          },
        ],
      }),
    });
    if (!r.ok) throw new Error(`paypal create order failed: ${r.status}`);
    const j = (await r.json()) as { id: string };
    return {
      orderId: j.id,
      clientData: {
        provider: 'paypal',
        demo: false,
        orderId: j.id,
        clientId: this.env.clientId,
        sandbox: this.env.sandbox,
      },
    };
  }

  async captureOrder(orderId: string): Promise<CaptureResult> {
    const at = await this.accessToken();
    const r = await fetch(`${this.base}/v2/checkout/orders/${encodeURIComponent(orderId)}/capture`, {
      method: 'POST',
      headers: { Authorization: `Bearer ${at}`, 'Content-Type': 'application/json' },
    });
    const j = (await r.json().catch(() => ({}))) as any;

    // 422 ORDER_ALREADY_CAPTURED — не ошибка, а гонка (человек нажал
    // дважды). Заказ оплачен; идемпотентность добьёт наш леджер.
    if (!r.ok) {
      const issue = j?.details?.[0]?.issue;
      if (issue === 'ORDER_ALREADY_CAPTURED') {
        return { ok: true, status: 'COMPLETED', captureId: undefined };
      }
      return { ok: false, status: `HTTP_${r.status}`, error: issue || 'capture failed' };
    }

    const status = j?.status || 'UNKNOWN';
    const captureId = j?.purchase_units?.[0]?.payments?.captures?.[0]?.id;
    // ВОТ ЕДИНСТВЕННОЕ настоящее подтверждение оплаты.
    return { ok: status === 'COMPLETED', status, captureId };
  }
}

export function pickProvider(env: Record<string, unknown>): PaymentProvider {
  const id = String(env.PAYPAL_CLIENT_ID || '');
  const secret = String(env.PAYPAL_SECRET || '');
  if (id && secret) {
    return new PayPalProvider({
      clientId: id,
      secret,
      sandbox: String(env.PAYPAL_ENV || 'sandbox') !== 'live',
    });
  }
  return new MockProvider();
}
