-- Касса «Пиксельного костра». D1 (SQLite).
-- Три таблицы: кошельки, платежи, потраченные монетки.
--
-- Правило дома: ДЕНЬГИ СЧИТАЕТ ТОЛЬКО СЕРВЕР. Клиент не присылает ни
-- цену, ни количество монет — только id пачки и id игры.

CREATE TABLE IF NOT EXISTS wallets (
  id          TEXT    PRIMARY KEY,        -- случайный 128-битный id
  coins       INTEGER NOT NULL DEFAULT 0,
  -- serial растёт на КАЖДУЮ операцию. Токен со старым serial — это
  -- реплей (кто-то подсунул сохранённую копию с большим балансом).
  serial      INTEGER NOT NULL DEFAULT 0,
  created_at  INTEGER NOT NULL,
  updated_at  INTEGER NOT NULL
);

CREATE TABLE IF NOT EXISTS payments (
  order_id     TEXT    PRIMARY KEY,       -- id заказа у провайдера
  wallet_id    TEXT    NOT NULL,
  provider     TEXT    NOT NULL,          -- 'mock' | 'paypal'
  pack_id      TEXT    NOT NULL,
  coins        INTEGER NOT NULL,          -- сколько начислим при capture
  amount_cents INTEGER NOT NULL,
  currency     TEXT    NOT NULL DEFAULT 'USD',
  -- created -> completed. Начисляем ровно один раз: повторный capture
  -- того же заказа видит 'completed' и молча возвращает тот же баланс.
  status       TEXT    NOT NULL DEFAULT 'created',
  capture_id   TEXT,
  created_at   INTEGER NOT NULL,
  captured_at  INTEGER,
  FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE INDEX IF NOT EXISTS idx_payments_wallet ON payments(wallet_id);

-- Аудит монеток в щели: сколько людей и во что реально играли.
CREATE TABLE IF NOT EXISTS spends (
  id         INTEGER PRIMARY KEY AUTOINCREMENT,
  wallet_id  TEXT    NOT NULL,
  game_id    TEXT    NOT NULL,
  created_at INTEGER NOT NULL,
  FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE INDEX IF NOT EXISTS idx_spends_wallet ON spends(wallet_id);
CREATE INDEX IF NOT EXISTS idx_spends_game   ON spends(game_id);
