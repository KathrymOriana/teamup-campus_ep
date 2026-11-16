import { avatarsNode } from '../components/avatar.js';
import { createMembersModal } from '../components/members-modal.js';
import { initShell } from '../components/shell.js';
import { ApiError } from '../core/api.js';
import { $, renderList, setOptions, show } from '../core/dom.js';
import { BADGE_PROYECTO, BARRA, fecha, hoyIso, iconoProyecto } from '../core/format.js';
import { toast, validate } from '../core/toast.js';
import { ProjectService } from '../services/project-service.js';

const ADMIN = 'Administrador';

let projects = [];
let courses = [];
let editingId = null;

const fail = (error) => toast(error instanceof ApiError ? error.message : 'No se pudo completar la acción.', 'danger');

function renderSummary(summary) {
    $('resTotal').textContent = summary.total;
    $('resProgreso').textContent = summary.enProgreso;
    $('resPendientes').textContent = summary.pendientes;
    $('resAtrasados').textContent = summary.atrasados;
}

function filteredProjects() {
    const q = $('fBuscar').value.trim().toLowerCase();
    const course = $('fCurso').value;
    const status = $('fEstado').value;
    const order = $('fOrden').value;
    return projects
        .filter((p) => `${p.nombre} ${p.curso} ${p.descripcion}`.toLowerCase().includes(q))
        .filter((p) => (!course || p.cursoId === Number(course)) && (!status || p.estado === status))
        .sort((a, b) => order === 'entrega' ? a.entrega.localeCompare(b.entrega) : order === 'avance' ? b.avance - a.avance : b.id - a.id);
}

function renderProjects() {
    const list = filteredProjects();
    renderList($('listaProyectos'), 'tpl-project-card', list, (p) => ({
        values: {
            id: p.id, icono: iconoProyecto(p.color), href: `tareas.html?proyecto=${p.id}`, nombre: p.nombre,
            estado: p.estado, estadoClass: BADGE_PROYECTO[p.estado], menuAria: `Opciones de ${p.nombre}`,
            miembrosAccion: p.rol === ADMIN ? 'Administrar integrantes' : 'Ver integrantes',
            curso: p.curso, motivosTexto: p.motivos.join(' · '), descripcion: p.descripcion,
            entrega: fecha(p.entrega), nIntegrantes: p.miembros.length, hechas: p.tareasHechas, total: p.totalTareas,
            ancho: `${p.avance}%`, barra: BARRA[p.color], avancePct: `${p.avance}%`,
        },
        slots: { avatars: avatarsNode(p.miembros, 4) },
    }));
    [...$('listaProyectos').children].forEach((card, i) => {
        card.querySelectorAll('[data-admin-only]').forEach((el) => show(el, list[i].rol === ADMIN));
        show(card.querySelector('[data-attention]'), list[i].necesitaAtencion);
    });
    show($('vacio'), list.length === 0);
    show($('vacioFiltros'), projects.length > 0);
    show($('vacioNuevo'), projects.length === 0);
}

async function load() {
    const [list, summary] = await Promise.all([ProjectService.list(), ProjectService.summary()]);
    projects = list;
    renderSummary(summary);
    renderProjects();
}

/* ----- Crear / editar ----- */
const modal = new bootstrap.Modal($('modalNuevo'));

function openForm(project = null) {
    editingId = project?.id ?? null;
    $('formNuevo').reset();
    $('formNuevo').classList.remove('was-validated');
    $('modalTitulo').textContent = project ? 'Editar proyecto' : 'Nuevo proyecto';
    $('nFecha').min = hoyIso();
    if (project) {
        $('nNombre').value = project.nombre;
        $('nCurso').value = project.cursoId;
        $('nDesc').value = project.descripcion;
        $('nFecha').value = project.entrega;
        $('nEstado').value = project.estado;
    }
    modal.show();
}

async function saveForm(e) {
    e.preventDefault();
    if (!validate(e.target)) return;
    const data = {
        nombre: $('nNombre').value.trim(), cursoId: Number($('nCurso').value), descripcion: $('nDesc').value.trim(),
        entrega: $('nFecha').value, estado: $('nEstado').value,
    };
    try {
        if (editingId) await ProjectService.update(editingId, data); else await ProjectService.create(data);
        modal.hide();
        toast(editingId ? 'Proyecto actualizado' : 'Proyecto creado');
        await load();
    } catch (error) { fail(error); }
}

async function main() {
    await initShell();
    courses = await ProjectService.courses();
    setOptions($('nCurso'), courses.map((c) => ({ value: c.id, label: c.nombre })));
    const filterCourse = $('fCurso');
    filterCourse.append(...courses.map((c) => new Option(c.nombre, c.id)));

    const members = createMembersModal({ onChange: load });
    await load();

    $('btnNuevo').addEventListener('click', () => openForm());
    $('formNuevo').addEventListener('submit', saveForm);
    ['fBuscar', 'fCurso', 'fEstado', 'fOrden'].forEach((id) => $(id).addEventListener('input', renderProjects));
    $('buscador').addEventListener('input', (e) => { $('fBuscar').value = e.target.value; renderProjects(); });

    $('listaProyectos').addEventListener('click', async (e) => {
        const button = e.target.closest('[data-action]');
        if (!button) return;
        const project = projects.find((p) => p.id === Number(button.dataset.id));
        if (button.dataset.action === 'members') await members.open(project).catch(fail);
        if (button.dataset.action === 'edit') openForm(project);
        if (button.dataset.action === 'delete' && confirm('Se eliminará el proyecto con todas sus tareas. ¿Continuar?')) {
            try { await ProjectService.remove(project.id); toast('Proyecto eliminado'); await load(); } catch (error) { fail(error); }
        }
    });

    $('vacio').addEventListener('click', (e) => {
        const action = e.target.closest('[data-action]')?.dataset.action;
        if (action === 'new') openForm();
        if (action === 'clear') { ['fBuscar', 'fCurso', 'fEstado'].forEach((id) => { $(id).value = ''; }); renderProjects(); }
    });
}

main().catch((error) => { if (!(error instanceof ApiError && error.status === 401)) fail(error); });
