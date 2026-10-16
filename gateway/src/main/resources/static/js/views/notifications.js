import {state} from '../session.js';
import {esc, shortId, date} from '../format.js';
import {header, empty} from '../ui.js';

export function notifications() {
  return `${header('Активность','Уведомления','После оплаты событие проходит через RabbitMQ и сохраняется здесь.','<button class="button ghost" data-action="refresh-notifications">↻ Обновить</button>')}${state.notifications.length ? `<div class="notification-list">${state.notifications.map(item => `<article class="card notification"><span class="notification-icon">✓</span><div><h3>Оплата получена</h3><p>Заказ #${esc(shortId(item.order_id))} успешно оплачен.</p><p class="muted">${esc(date(item.created_at))}</p></div></article>`).join('')}</div>` : empty('♧','Уведомлений пока нет','После первой оплаты здесь появится сообщение от сервиса уведомлений.')}`;
}
