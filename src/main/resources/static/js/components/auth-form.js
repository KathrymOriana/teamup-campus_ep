/** Ayudas compartidas por login, registro y recuperación (las reglas definitivas las valida el servidor). */
const PAGES = /^(index|proyectos|tareas|progreso)\.html$/;

export const isEmail = (value) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(String(value).trim());

export const passwordError = (password) => {
    if (password.length < 8) return 'Usa al menos 8 caracteres.';
    if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) return 'Incluye letras y números.';
    return '';
};

/** Nivel de seguridad de 0 a 5. */
export const passwordStrength = (p) => [p.length >= 8, /[a-z]/.test(p) && /[A-Z]/.test(p), /\d/.test(p), /[^A-Za-z0-9]/.test(p), p.length >= 12].filter(Boolean).length;

/** Página a la que volver después de iniciar sesión (solo páginas propias de la aplicación). */
export function nextUrl() {
    const next = new URLSearchParams(location.search).get('next');
    return PAGES.test(next ?? '') ? next : 'index.html';
}

/** Botón "ojo" para mostrar u ocultar la contraseña. */
export function bindPasswordToggles() {
    document.querySelectorAll('[data-toggle-pass]').forEach((button) => button.addEventListener('click', () => {
        const input = button.parentElement.querySelector('input');
        const reveal = input.type === 'password';
        input.type = reveal ? 'text' : 'password';
        button.firstElementChild.className = `bi bi-eye${reveal ? '-slash' : ''}`;
        button.setAttribute('aria-label', reveal ? 'Ocultar contraseña' : 'Mostrar contraseña');
    }));
}

export function showError(box, message) {
    box.textContent = message;
    box.hidden = false;
}

export function setBusy(button, busy, text) {
    button.disabled = busy;
    if (busy) { button.dataset.label = button.textContent; button.textContent = text; } else { button.textContent = button.dataset.label ?? button.textContent; }
}
