import {state, clearSession, setToken} from './session.js';
import {esc, money, shortId, date, row} from './format.js';
import {api} from './api.js';
import {empty, productCard} from './ui.js';
import {load, navigate, render} from './router.js';
import {toast} from './toasts.js';

document.addEventListener('click', async event => {
  const nav = event.target.closest('[data-nav]');
  if (nav) { navigate(nav.dataset.nav); return; }
  const mode = event.target.closest('[data-auth-mode]');
  if (mode) { state.view = mode.dataset.authMode; location.hash = state.view; render(); return; }
  const action = event.target.closest('[data-action]');
  if (!action) return;
  if (action.dataset.action === 'logout') {
    clearSession();
    toast('Вы вышли из аккаунта.'); navigate('overview'); return;
  }
  if (action.dataset.action === 'refresh-notifications') { await load('notifications'); toast('Уведомления обновлены.'); return; }
  if (action.dataset.action === 'receipt') {
    try {
      const receipt = row(await api(`/api/payments/${encodeURIComponent(action.dataset.id)}`));
      const dialog = document.createElement('dialog');
      dialog.className = 'receipt-dialog';
      dialog.innerHTML = `<div class="receipt-head"><span class="notification-icon">✓</span><h2>Квитанция об оплате</h2><p>Демонстрационный платёж завершён</p></div><div class="detail-row"><span>Номер платежа</span><strong>${esc(receipt.id)}</strong></div><div class="detail-row"><span>Заказ</span><strong>#${esc(shortId(receipt.order_id))}</strong></div><div class="detail-row"><span>Дата</span><strong>${esc(date(receipt.created_at))}</strong></div><div class="detail-row"><span>Сумма</span><strong>${money(receipt.amount)}</strong></div><button class="button ghost" data-action="close-dialog">Закрыть</button>`;
      document.body.append(dialog); dialog.showModal();
      dialog.addEventListener('close', () => dialog.remove());
    } catch (error) { toast(error.message, true); }
    return;
  }
  if (action.dataset.action === 'close-dialog') { action.closest('dialog')?.close(); return; }
  if (action.dataset.action === 'order') {
    const quantity = Number(document.querySelector(`[data-quantity="${action.dataset.id}"]`)?.value);
    if (!Number.isInteger(quantity) || quantity < 1) { toast('Укажите корректное количество.', true); return; }
    action.disabled = true;
    try { await api('/api/orders', {method:'POST', body:JSON.stringify({productId:action.dataset.id, quantity})}); toast('Заказ создан. Осталось оплатить.'); navigate('orders'); }
    catch (error) { toast(error.message, true); action.disabled = false; }
  }
  if (action.dataset.action === 'pay') {
    action.disabled = true;
    try {
      await api('/api/payments', {method:'POST', body:JSON.stringify({orderId:action.dataset.id})});
      toast('Оплата завершена. Уведомление скоро появится.'); await load('orders');
    } catch (error) { toast(error.message, true); action.disabled = false; }
  }
});
document.addEventListener('submit', async event => {
  if (event.target.id === 'auth-form') {
    event.preventDefault();
    const form = event.target; const button = form.querySelector('button[type="submit"]'); button.disabled = true;
    const values = Object.fromEntries(new FormData(form));
    try {
      const register = state.view === 'register';
      const result = await api(register ? '/api/auth/register' : '/api/auth/login', {method:'POST', body:JSON.stringify(values)});
      setToken(result.accessToken);
      toast(register ? 'Аккаунт создан. Добро пожаловать!' : 'Вы вошли в аккаунт.');
      navigate('overview');
    } catch (error) { toast(error.message, true); button.disabled = false; }
  }
  if (event.target.id === 'product-form') {
    event.preventDefault(); const form = event.target; const button = form.querySelector('button[type="submit"]'); button.disabled = true;
    const values = Object.fromEntries(new FormData(form));
    try { await api('/api/products', {method:'POST', body:JSON.stringify({name:values.name, price:Number(values.price), stock:Number(values.stock)})}); toast('Товар добавлен в каталог.'); await load('admin'); }
    catch (error) { toast(error.message, true); button.disabled = false; }
  }
});
document.addEventListener('input', event => {
  if (event.target.id !== 'catalog-search') return;
  state.search = event.target.value;
  const filtered = state.products.filter(product => product.name.toLowerCase().includes(state.search.toLowerCase()));
  document.querySelector('#catalog-results').innerHTML = filtered.length ? `<div class="product-grid">${filtered.map(productCard).join('')}</div>` : empty('⌕','Товары не найдены','Попробуйте другой запрос.');
  document.querySelector('.toolbar .muted').textContent = `Найдено: ${filtered.length}`;
});
window.addEventListener('hashchange', () => {
  const view = location.hash.replace('#','') || 'overview';
  if (view !== state.view) navigate(view);
});
