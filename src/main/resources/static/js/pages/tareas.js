import { avatarNode } from '../components/avatar.js';
import { bindProjectSelector } from '../components/project-selector.js';
import { initShell } from '../components/shell.js';
import { ApiError } from '../core/api.js';
import { $, renderList, setOptions, show } from '../core/dom.js';
import { BADGE, fecha } from '../core/format.js';
import { ProjectContext } from '../core/project-context.js';
import { toast, validate } from '../core/toast.js';
import { ProgressService } from '../services/progress-service.js';
import { ProjectService } from '../services/project-service.js';
import { TaskService } from '../services/task-service.js';

const PER_PAGE = 6;
const ADMIN = 'Administrador';
const STATES = ['Completada', 'En progreso', 'Pendiente'];
const PRIORITIES = ['Alta', 'Media', 'Baja'];
const STATE_COLOR = { Completada: '#22b573', 'En progreso': '#3b82f6', Pendiente: '#ef5b6b' };
const PRIORITY_COLOR = { Alta: '#ef5b6b', Media: '#f5a03a', Baja: '#22b573' };
const PRIORITY_WEIGHT = { Alta: 0, Media: 1, Baja: 2 };

let me;
let projects = [];
let tasks = [];
let stats;
let projectId = null;
let page = 1;
let quick = { tipo: 'todas', valor: null };
let editing = null;

const fail = (error) => toast(error instanceof ApiError ? error.message : 'No se pudo completar la acción.', 'danger');
const projectById = (id) => projects.find((p) => p.id === id);
const userById = (id) => projects.flatMap((p) => p.miembros).find((u) => u.id === id) ?? null;

/* ---------- datos ---------- */
async function load() {
    const [list, report] = await Promise.all([TaskService.list(projectId), ProgressService.report(projectId)]);
    tasks = list;
    stats = report.stats;
    render();
}

function filteredTasks() {
    const q = $('fBuscar').value.trim().toLowerCase();
    const responsible = $('fResp').value;
    const state = $('fEstado').value;
    const order = $('fOrden').value;
    const quickFilters = {
        todas: () => true,
        mias: (t) => t.responsableId === me.id,
        proximas: (t) => t.proxima,
        estado: (t) => t.estado === quick.valor,
        vencidas: (t) => t.vencida,
        prioridad: (t) => t.prioridad === quick.valor,
    };
    return tasks
        .filter((t) => `${t.titulo} ${t.descripcion}`.toLowerCase().includes(q))
        .filter((t) => !responsible || (responsible === 'none' ? t.responsableId == null : t.responsableId === Number(responsible)))
        .filter((t) => !state || t.estado === state)
        .filter(quickFilters[quick.tipo])
        .sort((a, b) => order === 'prioridad' ? PRIORITY_WEIGHT[a.prioridad] - PRIORITY_WEIGHT[b.prioridad]
            : order === 'proyecto' ? a.projectId - b.projectId : a.fecha.localeCompare(b.fecha));
}

/* ---------- pintado ---------- */
function renderStats() {
    $('resTotal').textContent = stats.total;
    $('resCompletadas').textContent = stats.completadas;
    $('resCompletadasPct').textContent = `${stats.pctCompletadas}%`;
    $('resProgreso').textContent = stats.enProgreso;
    $('resProgresoPct').textContent = `${stats.pctEnProgreso}%`;
    $('resPendientes').textContent = stats.pendientes;
    $('resPendientesPct').textContent = `${stats.pctPendientes}%`;
}

function paintQuick(containerId, items) {
    renderList($(containerId), 'tpl-quick-item', items, (item) => ({ values: item }));
    [...$(containerId).children].forEach((li, i) => {
        const item = items[i];
        const active = quick.tipo === item.tipo && quick.valor === (item.valor ?? null);
        li.querySelector('button').classList.toggle('on', active);
        show(li.querySelector('[data-dot]'), Boolean(item.color));
        show(li.querySelector('[data-icon]'), Boolean(item.icono));
    });
}

function renderQuick() {
    const count = (predicate) => tasks.filter(predicate).length;
    paintQuick('qRapidos', [
        { tipo: 'todas', icono: 'bi-journal-text', texto: 'Todas las tareas', n: tasks.length },
        { tipo: 'mias', icono: 'bi-person-fill', texto: 'Mis tareas', n: count((t) => t.responsableId === me.id) },
        { tipo: 'proximas', icono: 'bi-card-checklist', texto: 'Próximas (7 días)', n: count((t) => t.proxima) },
    ]);
    paintQuick('qEstado', [
        ...STATES.map((s) => ({ tipo: 'estado', valor: s, color: STATE_COLOR[s], texto: s === 'Completada' ? 'Completadas' : s === 'Pendiente' ? 'Pendientes' : s, n: count((t) => t.estado === s) })),
        { tipo: 'vencidas', color: STATE_COLOR.Pendiente, texto: 'Vencidas', n: count((t) => t.vencida) },
    ]);
    paintQuick('qPrioridad', PRIORITIES.map((p) => ({ tipo: 'prioridad', valor: p, color: PRIORITY_COLOR[p], texto: p, n: count((t) => t.prioridad === p) })));
}

function renderResponsibleFilter() {
    const select = $('fResp');
    const previous = select.value;
    const scope = projectId ? [projectById(projectId)] : projects;
    const users = [...new Map(scope.flatMap((p) => p?.miembros ?? []).map((u) => [u.id, u])).values()];
    setOptions(select, [{ value: '', label: 'Todos los responsables' }, { value: 'none', label: 'Sin responsable' },
        ...users.map((u) => ({ value: u.id, label: u.nombre }))]);
    select.value = [...select.options].some((o) => o.value === previous) ? previous : '';
}

function renderTable() {
    const list = filteredTasks();
    const pages = Math.max(1, Math.ceil(list.length / PER_PAGE));
    page = Math.min(page, pages);
    const start = (page - 1) * PER_PAGE;
    const visible = list.slice(start, start + PER_PAGE);

    renderList($('filas'), 'tpl-task-row', visible, (t) => {
        const project = projectById(t.projectId);
        const user = userById(t.responsableId);
        return {
            values: {
                id: t.id, filaClass: t.estado === 'Completada' ? 't-done' : '', chkAria: `Completar ${t.titulo}`,
                completada: t.estado === 'Completada', bloqueada: !t.editable,
                ayuda: t.editable ? null : 'Solo el responsable o el administrador pueden cambiarla',
                titulo: t.titulo, descripcion: t.descripcion, proyecto: project?.nombre ?? '', curso: project?.curso ?? '',
                responsable: user?.nombre ?? 'Sin asignar', prioridad: t.prioridad, prioClass: BADGE[t.prioridad],
                fecha: fecha(t.fecha), fechaClass: t.vencida ? 'text-danger' : '', estado: t.estado, estadoClass: BADGE[t.estado],
            },
            slots: { resp: avatarNode(user) },
        };
    });
    [...$('filas').children].forEach((row, i) => {
        show(row.querySelector('[data-late]'), visible[i].vencida);
        show(row.querySelector('[data-editable]'), visible[i].editable);
        show(row.querySelector('[data-readonly]'), !visible[i].editable);
    });

    show($('vacio'), list.length === 0);
    show($('vacioFiltros'), tasks.length > 0);
    show($('vacioNueva'), tasks.length === 0);
    $('contador').textContent = list.length ? `Mostrando ${start + 1} - ${start + visible.length} de ${list.length} tareas` : 'Mostrando 0 tareas';

    const items = [{ label: '‹', aria: 'Anterior', page: page - 1, disabled: page === 1, itemClass: page === 1 ? 'disabled' : '' },
        ...Array.from({ length: pages }, (_, i) => ({ label: String(i + 1), aria: `Página ${i + 1}`, page: i + 1, disabled: false, itemClass: i + 1 === page ? 'active' : '' })),
        { label: '›', aria: 'Siguiente', page: page + 1, disabled: page === pages, itemClass: page === pages ? 'disabled' : '' }];
    renderList($('paginacion'), 'tpl-page-item', items, (item) => ({ values: item }));
    $('chkTodas').checked = false;
}

function render() {
    renderStats();
    renderQuick();
    renderResponsibleFilter();
    renderTable();
}

/* ---------- formulario ---------- */
const modal = new bootstrap.Modal($('modalTarea'));

/** El administrador asigna a cualquiera; un integrante solo puede asignarse a sí mismo. */
function fillResponsible(selectedProjectId, selected) {
    const project = projectById(Number(selectedProjectId));
    const admin = project?.rol === ADMIN;
    const options = admin
        ? [{ value: '', label: 'Sin asignar' }, ...project.miembros.map((u) => ({ value: u.id, label: u.nombre }))]
        : [{ value: me.id, label: me.nombre }];
    setOptions($('mResp'), options, selected ?? '');
    if (!admin) $('mResp').selectedIndex = 0;
}

function openForm(task = null) {
    editing = task;
    $('formTarea').reset();
    $('formTarea').classList.remove('was-validated');
    $('modalTitulo').textContent = task ? 'Editar tarea' : 'Nueva tarea';
    setOptions($('mProyecto'), projects.map((p) => ({ value: p.id, label: p.nombre })), task?.projectId ?? projectId ?? projects[0].id);
    $('mProyecto').disabled = Boolean(task); // una tarea no cambia de proyecto
    fillResponsible($('mProyecto').value, task ? task.responsableId : me.id);
    if (task) {
        $('mTitulo').value = task.titulo;
        $('mDesc').value = task.descripcion;
        $('mPrioridad').value = task.prioridad;
        $('mFecha').value = task.fecha;
        $('mEstado').value = task.estado;
    }
    modal.show();
}

async function saveForm(e) {
    e.preventDefault();
    if (!validate(e.target)) return;
    const data = {
        projectId: Number($('mProyecto').value), titulo: $('mTitulo').value.trim(), descripcion: $('mDesc').value.trim(),
        responsableId: $('mResp').value ? Number($('mResp').value) : null, prioridad: $('mPrioridad').value,
        fecha: $('mFecha').value, estado: $('mEstado').value,
    };
    try {
        if (editing) await TaskService.update(editing.id, data); else await TaskService.create(data);
        modal.hide();
        toast(editing ? 'Tarea actualizada' : 'Tarea creada');
        await load();
    } catch (error) { fail(error); }
}

/* ---------- eventos ---------- */
async function changeState(id, estado) {
    try { await TaskService.setState(id, estado); } catch (error) { fail(error); }
}

function bindEvents() {
    ['fBuscar', 'fResp', 'fEstado', 'fOrden'].forEach((id) => $(id).addEventListener('input', () => { page = 1; renderTable(); }));
    $('buscador').addEventListener('input', (e) => { $('fBuscar').value = e.target.value; page = 1; renderTable(); });

    $('filtrosRapidos').addEventListener('click', (e) => {
        const button = e.target.closest('button[data-tipo]');
        if (!button) return;
        quick = { tipo: button.dataset.tipo, valor: button.dataset.valor ?? null };
        page = 1;
        renderQuick();
        renderTable();
    });

    $('paginacion').addEventListener('click', (e) => {
        const button = e.target.closest('[data-pag]');
        if (button) { page = Number(button.dataset.pag); renderTable(); }
    });

    $('filas').addEventListener('click', async (e) => {
        const button = e.target.closest('[data-action]');
        if (!button) return;
        const id = Number(button.dataset.id);
        if (button.dataset.action === 'toggle') { await changeState(id, button.checked ? 'Completada' : 'Pendiente'); await load(); }
        if (button.dataset.action === 'edit') openForm(tasks.find((t) => t.id === id));
        if (button.dataset.action === 'delete' && confirm('¿Eliminar esta tarea?')) {
            try { await TaskService.remove(id); toast('Tarea eliminada'); await load(); } catch (error) { fail(error); }
        }
    });

    $('chkTodas').addEventListener('change', async (e) => {
        const boxes = [...document.querySelectorAll('.chk:not(:disabled)')];
        await Promise.allSettled(boxes.map((b) => TaskService.setState(Number(b.dataset.id), e.target.checked ? 'Completada' : 'Pendiente')));
        await load();
    });

    $('btnNueva').addEventListener('click', () => {
        if (!projects.length) { toast('Primero crea un proyecto en Mis proyectos.', 'danger'); return; }
        openForm();
    });
    $('mProyecto').addEventListener('change', (e) => fillResponsible(e.target.value, me.id));
    $('formTarea').addEventListener('submit', saveForm);

    $('vacio').addEventListener('click', (e) => {
        const action = e.target.closest('[data-action]')?.dataset.action;
        if (action === 'new') $('btnNueva').click();
        if (action === 'clear') {
            ['fBuscar', 'fResp', 'fEstado'].forEach((id) => { $(id).value = ''; });
            quick = { tipo: 'todas', valor: null };
            render();
        }
    });
}

async function main() {
    me = await initShell();
    ProjectContext.applyQueryParam();
    projects = await ProjectService.list();
    projectId = bindProjectSelector($('fProyecto'), projects, {
        allowAll: true,
        onChange: (id) => { projectId = id; page = 1; load().catch(fail); },
    });
    bindEvents();
    await load();
}

main().catch((error) => { if (!(error instanceof ApiError && error.status === 401)) fail(error); });
