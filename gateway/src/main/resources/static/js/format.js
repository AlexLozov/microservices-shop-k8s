
export const esc = value => String(value ?? '').replace(/[&<>"']/g, char => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[char]));
export const money = value => new Intl.NumberFormat('ru-RU', {style:'currency', currency:'EUR'}).format(Number(value || 0));
export const shortId = value => String(value || '').slice(0, 8).toUpperCase();
export const date = value => value ? new Intl.DateTimeFormat('ru-RU', {dateStyle:'medium', timeStyle:'short'}).format(new Date(value)) : '—';
export const row = value => Object.fromEntries(Object.entries(value || {}).map(([key, item]) => [key.toLowerCase(), item]));
export const rows = value => (Array.isArray(value) ? value : []).map(row);
export const initials = value => String(value || 'U').trim().split(/\s+/).slice(0,2).map(word => word[0]).join('').toUpperCase();
