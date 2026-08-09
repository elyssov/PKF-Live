/**
 * Пачки четвертаков. ЦЕНЫ ЖИВУТ ТОЛЬКО ЗДЕСЬ — клиент присылает id
 * пачки, никогда не сумму и не количество монет.
 *
 * Номинал: доллар = ЧЕТЫРЕ четвертака (слово Юджина, «честные 4»).
 * Пачки от $1: PayPal берёт фиксированную комиссию с транзакции, и
 * поштучная продажа монет ушла бы ей целиком.
 */

export interface Pack {
  id: string;
  coins: number;
  amountCents: number;
  currency: string;
  title: string;
}

export const PACKS: Record<string, Pack> = {
  quarter_4: {
    id: 'quarter_4',
    coins: 4,
    amountCents: 100,
    currency: 'USD',
    title: '4 четвертака — $1',
  },
  quarter_12: {
    id: 'quarter_12',
    coins: 12,
    amountCents: 250,
    currency: 'USD',
    // 12 за $2.50: одна монета в подарок, чтобы пачка была не «×2.5», а щедрее
    title: '12 четвертаков — $2.50',
  },
  quarter_30: {
    id: 'quarter_30',
    coins: 30,
    amountCents: 500,
    currency: 'USD',
    title: '30 четвертаков — $5',
  },
};

export function packById(id: unknown): Pack | null {
  if (typeof id !== 'string') return null;
  return Object.prototype.hasOwnProperty.call(PACKS, id) ? PACKS[id] : null;
}
