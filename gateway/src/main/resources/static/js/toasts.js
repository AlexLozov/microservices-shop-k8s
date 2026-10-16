const toastRegion = document.querySelector('#toast-region');

export function toast(message, error = false) {
  const element = document.createElement('div');
  element.className = `toast${error ? ' error' : ''}`;
  element.textContent = message;
  toastRegion.append(element);
  setTimeout(() => element.remove(), 4800);
}
