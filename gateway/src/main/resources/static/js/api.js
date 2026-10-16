import {state, clearSession} from './session.js';

export async function api(path, options = {}) {
  const headers = {'Accept':'application/json', ...(options.body ? {'Content-Type':'application/json'} : {}), ...options.headers};
  if (state.token) headers.Authorization = `Bearer ${state.token}`;
  let response;
  try { response = await fetch(path, {...options, headers}); }
  catch { throw new Error('Нет связи с сервером. Проверьте, запущены ли сервисы.'); }
  const content = response.headers.get('content-type') || '';
  const payload = content.includes('json') ? await response.json() : null;
  if (!response.ok) {
    if (response.status === 401 && state.token) {
      clearSession();
      window.dispatchEvent(new Event('session-expired'));
    }
    const raw = payload?.detail || payload?.error || payload?.title;
    const translation = {
      'Invalid credentials':'Неверная почта или пароль.',
      'Admin role required':'Для этого действия нужны права администратора.',
      'Order belongs to another user':'Этот заказ принадлежит другому пользователю.',
      'Insufficient stock or product not found':'Недостаточно товара на складе.',
      'Order is not pending':'Этот заказ уже оплачен.'
    };
    throw new Error(translation[raw] || raw || `Ошибка сервера (${response.status}).`);
  }
  return payload;
}
