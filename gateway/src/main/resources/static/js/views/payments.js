import {state} from '../session.js';
import {esc, shortId, date, money} from '../format.js';
import {header, empty, status} from '../ui.js';

export function payments() {
  return `${header('Личный кабинет','Мои платежи','История демонстрационных оплат и детали каждой операции.')}${state.payments.length ? `<div class="card table-wrap"><table class="table"><thead><tr><th>Платёж</th><th>Заказ</th><th>Дата</th><th>Сумма</th><th>Статус</th><th>Детали</th></tr></thead><tbody>${state.payments.map(item => `<tr><td><strong>#${esc(shortId(item.id))}</strong></td><td>#${esc(shortId(item.order_id))}</td><td>${esc(date(item.created_at))}</td><td><strong>${money(item.amount)}</strong></td><td>${status(item.status)}</td><td><button class="button ghost" data-action="receipt" data-id="${esc(item.id)}">Квитанция</button></td></tr>`).join('')}</tbody></table></div>` : empty('€','Платежей пока нет','Создайте заказ и оплатите его — платёж появится здесь.','<button class="button accent" data-nav="orders">Мои заказы</button>')}<div class="callout">Оплата демонстрационная. Реальное списание средств не производится.</div>`;
}
