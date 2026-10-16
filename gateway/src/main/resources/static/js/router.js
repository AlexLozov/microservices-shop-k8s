import {state, authenticated, admin} from './session.js';
import {row, rows} from './format.js';
import {api} from './api.js';
import {toast} from './toasts.js';
import {shell} from './ui.js';
import {overview} from './views/overview.js';
import {catalog} from './views/catalog.js';
import {orders} from './views/orders.js';
import {payments} from './views/payments.js';
import {notifications} from './views/notifications.js';
import {profile} from './views/profile.js';
import {adminView} from './views/admin.js';
import {authView} from './views/auth.js';

const app = document.querySelector('#app');

export async function load(view) {
  state.loading = true; render();
  try {
    if (['overview','catalog','admin'].includes(view)) state.products = rows(await api('/api/products'));
    if (authenticated() && ['overview','orders'].includes(view)) state.orders = rows(await api('/api/orders'));
    if (authenticated() && ['overview','orders','payments'].includes(view)) state.payments = rows(await api('/api/payments'));
    if (authenticated() && view === 'orders') state.products = rows(await api('/api/products'));
    if (authenticated() && ['overview','notifications'].includes(view)) state.notifications = rows(await api('/api/notifications'));
    if (authenticated() && ['overview','profile'].includes(view) && !admin()) state.profile = row(await api('/api/users/me'));
  } catch (error) { toast(error.message, true); }
  finally { state.loading = false; render(); }
}
export function navigate(view) {
  if (['orders','payments','notifications','profile'].includes(view) && !authenticated()) view = 'login';
  if (view === 'admin' && !admin()) view = 'overview';
  state.view = view; state.search = '';
  location.hash = view;
  load(view);
  window.scrollTo({top:0, behavior:'smooth'});
}

export function render() {
  if (!authenticated() && !['overview','catalog','login','register'].includes(state.view)) state.view = 'login';
  if (state.view === 'admin' && !admin()) state.view = 'overview';
  const body = state.loading ? `<div class="skeleton"></div><div class="section-head"><div class="skeleton" style="width:35%;height:25px"></div></div>` : ({overview,catalog,orders,payments,notifications,profile,admin:adminView,login:authView,register:authView}[state.view] || overview)();
  app.innerHTML = shell(body);
}
