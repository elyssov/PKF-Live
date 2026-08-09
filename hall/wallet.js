/**
 * Кошелёк зала.
 *
 * Два режима, и зал обязан честно говорить, в каком он сейчас:
 *   server — касса отвечает, монеты живут в леджере, баланс подписан;
 *   local  — кассы нет (не задеплоена/недоступна), монеты живут в
 *            браузере. Это ВИТРИНА, не деньги, и так и подписано.
 *
 * Токен держим в localStorage, а не в куке: зал на github.io, касса на
 * workers.dev — кука была бы третьесторонней, а такие браузеры режут.
 * Защита от этого не страдает: подпись и серийник проверяет сервер.
 */
(function (global) {
  'use strict';

  var TOKEN_KEY = 'pxcf-arcade-token';
  var LOCAL_COINS_KEY = 'pxcf-arcade-wallet';

  var state = {
    mode: 'local',
    demo: true,
    provider: 'none',
    coins: 0,
    ready: false,
  };

  var listeners = [];
  var base = (global.PKF_CASHIER_URL || '').replace(/\/+$/, '');

  function emit() {
    for (var i = 0; i < listeners.length; i++) {
      try { listeners[i](state); } catch (e) { /* слушатель не должен ронять кошелёк */ }
    }
  }

  function token() { return localStorage.getItem(TOKEN_KEY) || ''; }
  function setToken(t) { if (t) localStorage.setItem(TOKEN_KEY, t); }

  function localCoins() { return parseInt(localStorage.getItem(LOCAL_COINS_KEY) || '0', 10) || 0; }
  function setLocalCoins(n) {
    state.coins = Math.max(0, n);
    localStorage.setItem(LOCAL_COINS_KEY, String(state.coins));
    emit();
  }

  async function api(path, opts) {
    opts = opts || {};
    var headers = { 'Content-Type': 'application/json' };
    var t = token();
    if (t) headers.Authorization = 'Bearer ' + t;

    var r = await fetch(base + path, {
      method: opts.method || 'GET',
      headers: headers,
      body: opts.body ? JSON.stringify(opts.body) : undefined,
    });

    var data = null;
    try { data = await r.json(); } catch (e) { data = null; }

    // Сервер отдаёт свежий токен почти в каждом ответе, включая отказы
    // (протухший токен, проигранная гонка) — забираем всегда.
    if (data && data.token) setToken(data.token);
    if (data && typeof data.coins === 'number') {
      state.coins = data.coins;
      emit();
    }
    return { ok: r.ok, status: r.status, data: data };
  }

  /** Поднять кошелёк: жива ли касса, каков баланс. */
  async function init() {
    if (!base) {
      state.mode = 'local';
      state.demo = true;
      state.provider = 'none';
      state.coins = localCoins();
      state.ready = true;
      emit();
      return state;
    }

    try {
      var h = await api('/api/health');
      if (!h.ok || !h.data) throw new Error('cashier unreachable');
      state.mode = 'server';
      state.provider = h.data.provider || 'unknown';
      // demo=true — касса работает на мок-провайдере: контур настоящий,
      // деньги нет. Зал показывает это игроку прямым текстом.
      state.demo = !!h.data.demo;

      if (token()) {
        var w = await api('/api/wallet');
        // 401 — токен от другой кассы или кошелёк стёрт: начинаем с нуля,
        // новый заведётся при первой покупке.
        if (!w.ok) { localStorage.removeItem(TOKEN_KEY); state.coins = 0; }
      } else {
        state.coins = 0;
      }
    } catch (e) {
      state.mode = 'local';
      state.demo = true;
      state.provider = 'none';
      state.coins = localCoins();
    }

    state.ready = true;
    emit();
    return state;
  }

  /**
   * Купить пачку. Возвращает {ok, credited, error}.
   * Цену и количество монет назначает сервер — сюда идёт только id пачки.
   */
  async function buy(packId) {
    if (state.mode !== 'server') {
      // Локальная витрина: та же щедрость, что была, без всякой оплаты.
      setLocalCoins(state.coins + 4);
      return { ok: true, credited: 4, demo: true };
    }

    var order = await api('/api/order', { method: 'POST', body: { pack: packId } });
    if (!order.ok || !order.data || !order.data.orderId) {
      return { ok: false, error: (order.data && order.data.error) || 'касса не приняла заказ' };
    }

    var client = order.data.client || {};

    // Настоящий PayPal откроет своё окно и вызовет capture в onApprove.
    // Пока провайдер мок — подтверждаем сразу, и зал об этом честно пишет.
    if (!client.demo && client.provider === 'paypal') {
      return {
        ok: false,
        needsCheckout: true,
        orderId: order.data.orderId,
        client: client,
        error: 'окно PayPal ещё не подключено к залу',
      };
    }

    var cap = await api('/api/capture', { method: 'POST', body: { orderId: order.data.orderId } });
    if (!cap.ok || !cap.data) {
      return { ok: false, error: (cap.data && cap.data.error) || 'оплата не подтверждена' };
    }
    return { ok: true, credited: cap.data.credited || 0, demo: !!client.demo };
  }

  /**
   * Монетка в щель. Возвращает true, только если монета РЕАЛЬНО списана —
   * зал обязан не пускать в игру, если касса отказала.
   */
  async function spend(gameId) {
    if (state.mode !== 'server') {
      if (state.coins <= 0) return { ok: false, error: 'нет монет' };
      setLocalCoins(state.coins - 1);
      return { ok: true };
    }

    var r = await api('/api/spend', { method: 'POST', body: { game: gameId } });
    if (r.ok) return { ok: true };

    if (r.status === 402) return { ok: false, error: 'нет монет' };
    if (r.status === 409) return { ok: false, error: 'монета уже потрачена в другой вкладке' };
    if (r.status === 401) return { ok: false, error: 'кошелёк не найден — купи монеты' };
    return { ok: false, error: (r.data && r.data.error) || 'касса не отвечает' };
  }

  global.Wallet = {
    init: init,
    buy: buy,
    spend: spend,
    onChange: function (cb) { listeners.push(cb); if (state.ready) cb(state); },
    get coins() { return state.coins; },
    get mode() { return state.mode; },
    get demo() { return state.demo; },
    get provider() { return state.provider; },
  };
})(window);
