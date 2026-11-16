import { setOptions } from '../core/dom.js';
import { ProjectContext } from '../core/project-context.js';

/** Proyecto efectivo: el guardado si todavía existe; si no, "todos" (o el primero cuando no se permite "todos"). */
export function resolveProjectId(projects, { allowAll }) {
    const saved = ProjectContext.get();
    if (saved != null && projects.some((p) => p.id === saved)) return saved;
    return allowAll ? null : (projects[0]?.id ?? null);
}

/** Llena un <select> con los proyectos y notifica cuando la persona cambia de proyecto. */
export function bindProjectSelector(select, projects, { allowAll, onChange }) {
    const options = projects.map((p) => ({ value: p.id, label: p.nombre }));
    if (allowAll) options.unshift({ value: 'all', label: 'Todos los proyectos' });
    const current = resolveProjectId(projects, { allowAll });
    setOptions(select, options, current ?? 'all');
    select.addEventListener('change', () => {
        ProjectContext.set(select.value);
        onChange(select.value === 'all' ? null : Number(select.value));
    });
    return current;
}
