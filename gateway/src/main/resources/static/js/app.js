import {state, authenticated, clearSession} from './session.js';
import {load, navigate} from './router.js';
import './actions.js';

if (!authenticated() && state.token) clearSession();
window.addEventListener('session-expired', () => navigate('login'));
load(state.view);
