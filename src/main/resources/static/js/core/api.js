/** Cliente HTTP único. Todas las llamadas al servidor pasan por aquí (JSON, cookie de sesión y token CSRF). */
export class ApiError extends Error {
    constructor(status, message, errors = {}) {
        super(message);
        this.status = status;
        this.errors = errors; // errores por campo cuando el servidor responde 422
    }
}

const PUBLIC_PAGES = ['login.html', 'registro.html', 'recuperar-password.html'];
const currentPage = () => location.pathname.split('/').pop() || 'index.html';

function xsrfToken() {
    const cookie = document.cookie.split('; ').find((c) => c.startsWith('XSRF-TOKEN='));
    return cookie ? decodeURIComponent(cookie.split('=')[1]) : null;
}

function goToLogin() {
    const page = currentPage();
    location.replace(`login.html?next=${encodeURIComponent(page)}`);
}

async function request(method, path, body) {
    const headers = { Accept: 'application/json' };
    if (body !== undefined) headers['Content-Type'] = 'application/json';
    if (method !== 'GET') {
        const token = xsrfToken();
        if (token) headers['X-XSRF-TOKEN'] = token;
    }

    const response = await fetch(`/api${path}`, {
        method,
        headers,
        body: body === undefined ? undefined : JSON.stringify(body),
    });

    if (response.status === 401 && !PUBLIC_PAGES.includes(currentPage())) {
        goToLogin();
        throw new ApiError(401, 'Tu sesión expiró. Inicia sesión de nuevo.');
    }

    const data = response.status === 204 ? null : await response.json().catch(() => null);
    if (!response.ok) {
        throw new ApiError(response.status, data?.detail ?? 'Ocurrió un error inesperado. Inténtalo de nuevo.', data?.errors ?? {});
    }
    return data;
}

export const http = {
    get: (path) => request('GET', path),
    post: (path, body = {}) => request('POST', path, body),
    put: (path, body) => request('PUT', path, body),
    patch: (path, body) => request('PATCH', path, body),
    delete: (path) => request('DELETE', path),
};
