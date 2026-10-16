import {state, admin} from '../session.js';
import {esc} from '../format.js';
import {header, productCard, empty} from '../ui.js';

export function catalog() {
  const filtered = state.products.filter(product => product.name.toLowerCase().includes(state.search.toLowerCase()));
  return `${header('Магазин','Каталог товаров','Выберите товар и количество. Заказ появится в вашем кабинете.',admin() ? '<button class="button accent" data-nav="admin">+ Добавить товар</button>' : '')}<div class="toolbar"><input class="search" type="search" placeholder="Поиск по названию..." aria-label="Поиск товаров" id="catalog-search" value="${esc(state.search)}"><span class="muted">Найдено: ${filtered.length}</span></div><div id="catalog-results">${filtered.length ? `<div class="product-grid">${filtered.map(productCard).join('')}</div>` : empty('⌕','Товары не найдены','Попробуйте другой запрос.')}</div>`;
}
