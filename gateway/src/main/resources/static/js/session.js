
export const state = {
  token: sessionStorage.getItem('storeToken') || '',
  view: location.hash.replace('#', '') || 'overview',
  search: '',
  products: [], orders: [], payments: [], notifications: [], profile: null,
  loading: false
};

export function claims() {
  try {
    const part = state.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(atob(part));
  } catch { return null; }
}
export function authenticated() { return Boolean(state.token && claims() && claims().exp * 1000 > Date.now()); }
export function admin() { return authenticated() && claims().role === 'ADMIN'; }

export function setToken(token) {
  state.token = token;
  sessionStorage.setItem('storeToken', token);
}

export function clearSession() {
  sessionStorage.removeItem('storeToken');
  state.token = '';
  state.profile = null;
  state.orders = [];
  state.payments = [];
  state.notifications = [];
}
