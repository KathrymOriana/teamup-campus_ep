import { $, cloneTemplate, fill, qsa, show } from '../core/dom.js';
import { loadIncludes } from '../core/include.js';
import { AuthService } from '../services/auth-service.js';
import { ProgressService } from '../services/progress-service.js';
import { avatarNode } from './avatar.js';
import { initSidebarToggle } from './sidebar-toggle.js';

const currentPage = () => location.pathname.split('/').pop() || 'index.html';

function markActiveLink() {
    qsa('.nav-list a[data-page]').forEach((link) => {
        const active = link.dataset.page === currentPage();
        link.classList.toggle('active', active);
        if (active) link.setAttribute('aria-current', 'page'); else link.removeAttribute('aria-current');
    });
}

function renderUser(user) {
    $('userAvatar').replaceChildren(avatarNode(user, { size: 36 }));
    $('userName').textContent = user.nombre;
    $('userEmail').textContent = user.email;
}

async function renderNotifications() {
    const items = await ProgressService.notifications();
    $('bellCount').textContent = items.length;
    show($('bellDot'), items.length > 0);
    show($('bellEmpty'), items.length === 0);
    $('bellItems').replaceChildren(...items.slice(0, 8).map((n) => fill(cloneTemplate('tpl-notification'), {
        texto: n.texto,
        href: `tareas.html?proyecto=${n.projectId}`,
        nivelClass: `bell-item--${n.nivel}`,
        iconoClass: `bi-${n.icono}`,
    })));
}

/** Arma la estructura común de las páginas privadas y devuelve el usuario con sesión. */
export async function initShell() {
    await loadIncludes();
    markActiveLink();
    initSidebarToggle();
    document.addEventListener('hide.bs.modal', () => document.activeElement?.blur());
    const user = await AuthService.me(); // sin sesión, el cliente HTTP redirige a login.html
    renderUser(user);
    document.addEventListener('click', async (e) => {
        if (!e.target.closest('[data-action="logout"]')) return;
        try { await AuthService.logout(); } finally { location.href = 'login.html'; }
    });
    renderNotifications().catch(() => { /* los avisos no son críticos */ });
    return user;
}
