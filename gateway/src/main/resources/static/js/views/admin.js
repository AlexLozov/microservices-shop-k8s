import {state} from '../session.js';
import {esc, shortId, money} from '../format.js';
import {header, empty} from '../ui.js';

export function adminView() {
  return `${header('Панель администратора','Управление товарами','Добавляйте товары в каталог и отслеживайте остатки.')}<div class="split"><div>${state.products.length ? `<div class="card table-wrap"><table class="table"><thead><tr><th>Товар</th><th>ID</th><th>Цена</th><th>Остаток</th></tr></thead><tbody>${state.products.map(item => `<tr><td><strong>${esc(item.name)}</strong></td><td>#${esc(shortId(item.id))}</td><td>${money(item.price)}</td><td>${Number(item.stock)} шт.</td></tr>`).join('')}</tbody></table></div>` : empty('📦','Товаров пока нет','Добавьте первый товар через форму справа.')}</div><div class="card quick-card"><h3>Новый товар</h3><p>Товар сразу появится в публичном каталоге.</p><form id="product-form" class="form"><label>Название<input name="name" required maxlength="150" placeholder="Например, Клавиатура"></label><label>Цена, EUR<input name="price" type="number" step="0.01" min="0.01" required placeholder="49.90"></label><label>Количество<input name="stock" type="number" min="0" step="1" required placeholder="10"></label><button class="button accent" type="submit">+ Добавить товар</button></form></div></div>`;
}
