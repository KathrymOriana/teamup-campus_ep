import { http } from '../core/api.js';

export const AuthService = {
    login: (email, password) => http.post('/auth/login', { email, password }),
    register: (nombre, email, password) => http.post('/auth/register', { nombre, email, password }),
    resetPassword: (email, password) => http.post('/auth/password-reset', { email, password }),
    logout: () => http.post('/auth/logout'),
    me: () => http.get('/auth/me'),

    /** Para páginas públicas: devuelve el usuario o null si no hay sesión. */
    async currentOrNull() {
        const session = await http.get('/auth/session');
        return session.authenticated ? session.user : null;
    },
};
