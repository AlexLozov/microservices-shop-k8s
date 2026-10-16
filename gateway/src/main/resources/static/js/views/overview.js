import {state, authenticated, admin} from '../session.js';
import {money} from '../format.js';
import {productCard, empty} from '../ui.js';
import {orderTable} from './orders.js';

export function overview() {
  const welcome = authenticated() ? (admin() ? 'Управляйте магазином в одном месте' : 'Добро пожаловать обратно') : 'Покупки начинаются здесь';
  const description = authenticated() ? 'Каталог, заказы, оплата и уведомления связаны в одном интерфейсе.' : 'Просматривайте товары, создайте аккаунт и оформите свой первый заказ.';
  const action = authenticated() ? (admin() ? 'admin' : 'catalog') : 'register';
  const actionText = authenticated() ? (admin() ? 'Добавить товар' : 'Перейти в каталог') : 'Создать аккаунт';
  const paid = state.orders.filter(order => order.status === 'PAID').length;
  const total = state.orders.filter(order => order.status === 'PAID').reduce((sum, order) => sum + Number(order.total), 0);
  const stats = admin() ? [
    ['Товаров в каталоге', state.products.length, 'Всего позиций', '▦'],
    ['Единиц на складе', state.products.reduce((sum, product) => sum + Number(product.stock), 0), 'Доступны покупателям', '◫'],
    ['Нет в наличии', state.products.filter(product => Number(product.stock) === 0).length, 'Нужно пополнить', '!'],
    ['Сервисы', '6', 'Бизнес-сервисы платформы', '⚙']
  ] : [
    ['Товаров в каталоге',state.products.length,'Доступны для заказа','▦'],
    ['Мои заказы',authenticated() ? state.orders.length : '—','Созданные заказы','▤'],
    ['Оплачено',authenticated() ? paid : '—','Завершённые покупки','✓'],
    ['Сумма покупок',authenticated() ? money(total) : '—','По оплаченным заказам','€']
  ];
  return `<div class="hero"><div class="hero-content"><p class="eyebrow">Ваш цифровой магазин</p><h2>${welcome}</h2><p>${description}</p><button class="button" data-nav="${action}">${actionText} →</button></div><div class="hero-art"><span class="bag" aria-hidden="true">🛍️</span></div></div><div class="stat-grid">${stats.map(([label,value,detail,icon]) => `<div class="card stat"><div class="stat-top"><span>${label}</span><span class="stat-icon">${icon}</span></div><div class="stat-value">${value}</div><div class="stat-detail">${detail}</div></div>`).join('')}</div><div class="section-head"><h2>Популярные товары</h2><button class="text-link" data-nav="catalog">Весь каталог →</button></div>${state.products.length ? `<div class="product-grid">${state.products.slice(0,3).map(productCard).join('')}</div>` : empty('📦','Каталог пока пуст','Администратор ещё не добавил товары.')}${authenticated() && !admin() ? `<div class="section-head"><h2>Последние заказы</h2><button class="text-link" data-nav="orders">Все заказы →</button></div>${state.orders.length ? orderTable(state.orders.slice(0,4)) : empty('▤','Заказов пока нет','Выберите товар в каталоге, чтобы оформить первый заказ.','<button class="button accent" data-nav="catalog">Открыть каталог</button>')}` : ''}`;
}
