import { ApiError } from '../core/api.js';
import { $, renderList, show } from '../core/dom.js';
import { toast, validate } from '../core/toast.js';
import { ProjectService } from '../services/project-service.js';
import { avatarNode } from './avatar.js';

const ADMIN = 'Administrador';

/** Ventana "Administrar integrantes". La estructura está en proyectos.html; aquí solo se enlazan datos y eventos. */
export function createMembersModal({ onChange }) {
    const modal = new bootstrap.Modal($('modalMiembros'));
    let project = null;
    let members = [];

    const isAdmin = () => project?.rol === ADMIN;
    const fail = (error) => toast(error instanceof ApiError ? error.message : 'No se pudo completar la acción.', 'danger');

    function render() {
        $('mmTitulo').textContent = `Integrantes · ${project.nombre}`;
        $('mmInfo').textContent = isAdmin()
            ? 'Como administrador puedes agregar integrantes, cambiar roles y quitar personas.'
            : 'Solo el administrador del proyecto puede modificar los integrantes.';
        show($('mmForm'), isAdmin());

        renderList($('mmLista'), 'tpl-member-row', members, (m) => ({
            values: {
                nombre: m.nombre, email: m.email, rol: m.rol, userId: m.userId,
                rolAria: `Rol de ${m.nombre}`, quitarAria: `Quitar a ${m.nombre}`,
                rolClass: m.rol === ADMIN ? 'badge-progreso' : 'badge-pendiente',
            },
            slots: { avatar: avatarNode({ id: m.userId, nombre: m.nombre, foto: m.foto }, { size: 38 }) },
        }));
        [...$('mmLista').children].forEach((row, i) => {
            row.querySelector('select').value = members[i].rol;
            row.querySelectorAll('[data-admin-only]').forEach((el) => show(el, isAdmin()));
            row.querySelectorAll('[data-read-only]').forEach((el) => show(el, !isAdmin()));
        });
    }

    async function refresh() {
        members = await ProjectService.members(project.id);
        render();
        onChange();
    }

    $('mmLista').addEventListener('change', async (e) => {
        const select = e.target.closest('select[data-user]');
        if (!select) return;
        try { await ProjectService.setRole(project.id, Number(select.dataset.user), select.value); await refresh(); } catch (error) { fail(error); await refresh(); }
    });

    $('mmLista').addEventListener('click', async (e) => {
        const button = e.target.closest('[data-action="remove"]');
        if (!button || !confirm('¿Quitar a esta persona del proyecto? Sus tareas quedarán sin responsable.')) return;
        try { await ProjectService.removeMember(project.id, Number(button.dataset.user)); toast('Integrante eliminado del proyecto'); await refresh(); } catch (error) { fail(error); }
    });

    $('mmForm').addEventListener('submit', async (e) => {
        e.preventDefault();
        if (!validate(e.target)) return;
        try {
            const added = await ProjectService.addMember(project.id, {
                nombre: $('mmNombre').value, email: $('mmEmail').value, rol: $('mmRol').value,
            });
            e.target.reset();
            e.target.classList.remove('was-validated');
            toast(`${added.nombre} se agregó al proyecto`);
            await refresh();
        } catch (error) { fail(error); }
    });

    return {
        async open(selectedProject) {
            project = selectedProject;
            await refresh();
            modal.show();
        },
    };
}
