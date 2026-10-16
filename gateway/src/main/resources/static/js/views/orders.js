import {state} from '../session.js';
import {esc, shortId, date, money} from '../format.js';
import {header, empty, status} from '../ui.js';

export function orderTable(items) {
  return `<div class="card table-wrap"><table class="table"><thead><tr><th>Заказ</th><th>Товар</th><th>Дата</th><th>Сумма</th><th>Статус</th><th>Действие</th></tr></thead><tbody>${items.map(order => `<tr><td><strong>#${esc(shortId(order.id))}</strong></td><td>${esc(state.products.find(product => product.id === order.product_id)?.name || shortId(order.product_id))}<span class="muted"> · ${Number(order.quantity)} шт.</span></td><td>${esc(date(order.created_at))}</td><td><strong>${money(order.total)}</strong></td><td>${status(order.status)}</td><td>${order.status === 'PENDING' ? `<button class="button accent" data-action="pay" data-id="${esc(order.id)}">Оплатить</button>` : '<span class="muted">Готово</span>'}</td></tr>`).join('')}</tbody></table></div>`;
}
export function orders() {
  return `${header('Личный кабинет','Мои заказы','Статусы заказов и оплата. Для каждого заказа резервируется товар.','<button class="button ghost" data-nav="catalog">+ Новый заказ</button>')}${state.orders.length ? orderTable(state.orders) : empty('▤','Заказов пока нет','Перейдите в каталог и выберите первый товар.','<button class="button accent" data-nav="catalog">Открыть каталог</button>')}<div class="callout">Оплата в этой лабораторной имитируется. При нажатии «Оплатить» деньги не списываются.</div>`;
}
