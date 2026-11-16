/** Proyecto seleccionado, compartido entre páginas (preferencia de interfaz, se guarda en sessionStorage). */
const KEY = 'teamup.projectId';

export const ProjectContext = {
    /** Devuelve el id del proyecto actual o null si se ven todos. */
    get() {
        try {
            const value = sessionStorage.getItem(KEY);
            return value && value !== 'all' ? Number(value) : null;
        } catch { return null; }
    },
    set(id) {
        try { sessionStorage.setItem(KEY, id == null || id === 'all' ? 'all' : String(id)); } catch { /* sin almacenamiento */ }
    },
    /** Los enlaces `?proyecto=ID` abren la página con ese proyecto seleccionado. */
    applyQueryParam() {
        const param = new URLSearchParams(location.search).get('proyecto');
        if (param && /^\d+$/.test(param)) {
            ProjectContext.set(param);
            history.replaceState(null, '', location.pathname);
        }
    },
};
