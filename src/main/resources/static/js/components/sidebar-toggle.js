/** Menú hamburguesa: abre y cierra el menú lateral en pantallas pequeñas (≤ 992 px). */
export function initSidebarToggle() {
    const toggle = document.getElementById('menuToggle');
    const close = document.getElementById('sidebarClose');
    const backdrop = document.getElementById('sidebarBackdrop');
    const sidebar = document.getElementById('sidebar');
    const mobile = window.matchMedia('(max-width: 992px)');
    if (!toggle || !sidebar) return;

    const setOpen = (open) => {
        document.body.classList.toggle('menu-open', open);
        toggle.setAttribute('aria-expanded', String(open));
        (open ? close : toggle).focus({ preventScroll: true }); // accesibilidad: mueve el foco
    };

    toggle.addEventListener('click', () => setOpen(!document.body.classList.contains('menu-open')));
    close.addEventListener('click', () => setOpen(false));
    backdrop?.addEventListener('click', () => setOpen(false));
    sidebar.addEventListener('click', (e) => { if (e.target.closest('a[href]:not([aria-disabled])') && mobile.matches) setOpen(false); });
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && document.body.classList.contains('menu-open')) setOpen(false); });
    mobile.addEventListener('change', (e) => {
        if (!e.matches) { document.body.classList.remove('menu-open'); toggle.setAttribute('aria-expanded', 'false'); }
    });
}
