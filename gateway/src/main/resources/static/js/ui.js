import {state, authenticated, admin} from './session.js';
import {esc, money, initials} from './format.js';

export function navButton(view, icon, title) {
  return `<button type="button" class="nav-button ${state.view === view ? 'active' : ''}" data-nav="${view}"><span class="nav-icon">${icon}</span><span>${title}</span></button>`;
}
export function shell(content) {
  const links = [navButton('overview','⌂','Обзор'), navButton('catalog','▦','Каталог'), navButton('orders','▤','Мои заказы'), navButton('payments','€','Платежи'), navButton('notifications','♧','Уведомления'), navButton('profile','◉','Профиль')];
  if (admin()) links.push(navButton('admin','⚙','Управление'));
  const side = `<aside class="sidebar"><div class="brand"><span class="brand-mark">M</span><span>маркет<small>microservice store</small></span></div><div class="nav-label">Магазин</div><nav>${links.join('')}</nav><div class="sidebar-bottom"><div class="small">Лабораторная работа №2</div><strong>Spring Boot + RabbitMQ</strong><div class="small">6 независимых сервисов</div></div></aside>`;
  const activeTitle = {overview:'Обзор',catalog:'Каталог',orders:'Мои заказы',payments:'Платежи',notifications:'Уведомления',profile:'Профиль',admin:'Управление',login:'Вход',register:'Регистрация'}[state.view] || 'Обзор';
  const userButton = authenticated() ? `<span class="avatar" title="${admin() ? 'Администратор' : 'Мой профиль'}">${admin() ? 'AD' : initials(state.profile?.name || 'U')}</span><button class="button ghost" data-action="logout">Выйти</button>` : `<button class="button accent" data-nav="login">Войти</button>`;
  const top = `<header class="topbar"><div class="breadcrumb">Маркет / <strong>${activeTitle}</strong></div><div class="top-actions"><span class="pill"><i></i> Локальный магазин</span>${userButton}</div></header>`;
  const mobile = `<nav class="mobile-nav">${[navButton('overview','⌂','Обзор'),navButton('catalog','▦','Товары'),navButton('orders','▤','Заказы'),navButton('payments','€','Оплаты'),navButton('notifications','♧','События'),navButton(admin() ? 'admin' : 'profile','◉',admin() ? 'Админ' : 'Профиль')].join('')}</nav>`;
  return `<div class="app-shell">${side}<div class="workspace">${top}<main class="content">${content}</main></div>${mobile}</div>`;
}
export function header(eyebrow, title, description, action = '') {
  return `<div class="page-header"><div><p class="eyebrow">${eyebrow}</p><h1 class="page-title">${title}</h1><p class="page-desc">${description}</p></div>${action}</div>`;
}
export function empty(icon, title, description, action = '') {
  return `<div class="card empty"><div class="empty-icon">${icon}</div><h3>${title}</h3><p>${description}</p>${action}</div>`;
}
export function status(value) {
  const paid = ['PAID','COMPLETED'].includes(value);
  return `<span class="status ${paid ? 'paid' : ''}">${paid ? (value === 'PAID' ? 'Оплачен' : 'Завершён') : 'Ожидает оплаты'}</span>`;
}
export function productArt(product) {
  const icons = ['⌨️','🎧','📱','🖥️','⌚','📦'];
  const id = String(product.id || product.name || 'x');
  const index = [...id].reduce((sum, char) => sum + char.charCodeAt(0), 0) % icons.length;
  return `<div class="product-art tone-${index % 3}"><b>В НАЛИЧИИ</b><span aria-hidden="true">${icons[index]}</span></div>`;
}
export function productCard(product) {
  const available = Number(product.stock) > 0;
  const controls = authenticated() && !admin() ? `<div class="buy-controls"><input class="quantity" type="number" min="1" max="${Number(product.stock)}" value="1" aria-label="Количество для ${esc(product.name)}" data-quantity="${esc(product.id)}"><button class="button accent" data-action="order" data-id="${esc(product.id)}" ${available ? '' : 'disabled'}>Заказать</button></div>` : `<button class="button ghost" data-nav="${authenticated() ? 'admin' : 'login'}">${authenticated() ? 'Управлять' : 'Войти'}</button>`;
  return `<article class="card product-card">${productArt(product)}<div class="product-info"><h3 class="product-name">${esc(product.name)}</h3><span class="muted">${available ? `Осталось: ${Number(product.stock)} шт.` : 'Нет в наличии'}</span><div class="product-footer"><strong class="price">${money(product.price)}</strong>${controls}</div></div></article>`;
}
