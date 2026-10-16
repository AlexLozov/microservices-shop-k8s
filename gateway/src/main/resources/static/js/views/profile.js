import {state, admin, claims} from '../session.js';
import {esc, initials, date} from '../format.js';
import {header} from '../ui.js';

export function profile() {
  const person = state.profile;
  const name = admin() ? 'Администратор' : person?.name || 'Пользователь';
  const id = person?.id || claims()?.sub || '—';
  return `${header('Учётная запись','Мой профиль','Личные данные и информация об учётной записи.')}<div class="profile-grid"><div class="card profile-head"><div class="profile-avatar">${esc(initials(name))}</div><h2>${esc(name)}</h2><span class="muted">${admin() ? 'Администратор магазина' : 'Покупатель'}</span></div><div class="card detail-list"><div class="detail-row"><span>Имя</span><strong>${esc(name)}</strong></div><div class="detail-row"><span>Email</span><strong>${esc(person?.email || '—')}</strong></div><div class="detail-row"><span>ID пользователя</span><strong>${esc(id)}</strong></div><div class="detail-row"><span>Роль</span><strong>${admin() ? 'ADMIN' : 'USER'}</strong></div><div class="detail-row"><span>Сессия</span><strong>Токен действует до ${esc(date(new Date(claims().exp * 1000).toISOString()))}</strong></div></div></div>`;
}
