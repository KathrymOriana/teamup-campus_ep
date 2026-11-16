import { avatarsNode } from '../components/avatar.js';
import { initShell } from '../components/shell.js';
import { ApiError } from '../core/api.js';
import { $, renderList, show } from '../core/dom.js';
import { BADGE, BARRA, COLOR, fecha, iconoProyecto } from '../core/format.js';
import { toast } from '../core/toast.js';
import { ProgressService } from '../services/progress-service.js';
import { ProjectService } from '../services/project-service.js';
import { TaskService } from '../services/task-service.js';

const CYCLE = ['Pendiente', 'En progreso', 'Completada'];
const FEATURED_PROJECTS = 3;
const RECENT_TASKS = 4;

let projects = [];
let tasks = [];
let query = '';

const matches = (...texts) => texts.join(' ').toLowerCase().includes(query);

function renderStats(stats, summary) {
    $('statProyectos').textContent = summary.activos;
    $('statProyectosSub').textContent = `de ${summary.total} proyectos en total`;
    $('statCompletadas').textContent = stats.completadas;
    $('statCompletadasDe').textContent = `de ${stats.total} tareas`;
    $('statBarra').style.width = `${stats.pctCompletadas}%`;
    $('statPct').textContent = `${stats.pctCompletadas}%`;
    $('statProgreso').textContent = stats.enProgreso;
    $('statPendientes').textContent = stats.pendientes;
    $('statVencidas').textContent = stats.vencidas;
}

function renderProjects() {
    const list = projects.filter((p) => matches(p.nombre, p.curso, p.descripcion)).slice(0, FEATURED_PROJECTS);
    show($('proyectosVacio'), projects.length === 0);
    renderList($('proyectos'), 'tpl-project-mini', list, (p) => ({
        values: {
            icono: iconoProyecto(p.color), curso: p.curso, href: `tareas.html?proyecto=${p.id}`, nombre: p.nombre,
            descripcion: p.descripcion, ancho: `${p.avance}%`, barra: BARRA[p.color], avancePct: `${p.avance}%`,
            nIntegrantes: p.miembros.length,
        },
        slots: { avatars: avatarsNode(p.miembros) },
    }));
}

function renderTasks() {
    const byId = new Map(projects.map((p) => [p.id, p]));
    const list = tasks
        .filter((t) => matches(t.titulo, byId.get(t.projectId)?.nombre))
        .sort((a, b) => (a.estado === 'Completada') - (b.estado === 'Completada') || a.fecha.localeCompare(b.fecha))
        .slice(0, RECENT_TASKS);
    show($('tareasVacio'), list.length === 0);
    renderList($('tareas'), 'tpl-task-mini', list, (t) => {
        const project = byId.get(t.projectId);
        return {
            values: {
                id: t.id, titulo: t.titulo, href: `tareas.html?proyecto=${t.projectId}`, proyecto: project?.nombre ?? '',
                color: COLOR[project?.color], prioridad: t.prioridad, prioClass: BADGE[t.prioridad], fecha: fecha(t.fecha),
                estado: t.estado, estadoClass: BADGE[t.estado],
                bloqueada: !t.editable, ayuda: t.editable ? 'Clic para cambiar el estado' : 'Solo el responsable o el administrador pueden cambiarla',
            },
        };
    });
}

async function load() {
    const [list, taskList, summary, report] = await Promise.all([ProjectService.list(), TaskService.list(null), ProjectService.summary(), ProgressService.report(null)]);
    projects = list;
    tasks = taskList;
    renderStats(report.stats, summary);
    renderProjects();
    renderTasks();
}

async function main() {
    const user = await initShell();
    $('heroNombre').textContent = user.nombre.split(' ')[0];
    await load();

    $('tareas').addEventListener('click', async (e) => {
        const button = e.target.closest('[data-action="cycle"]');
        if (!button) return;
        const task = tasks.find((t) => t.id === Number(button.dataset.id));
        try {
            await TaskService.setState(task.id, CYCLE[(CYCLE.indexOf(task.estado) + 1) % CYCLE.length]);
        } catch (error) {
            toast(error instanceof ApiError ? error.message : 'No se pudo cambiar el estado.', 'danger');
        }
        await load();
    });

    $('buscador').addEventListener('input', (e) => { query = e.target.value.trim().toLowerCase(); renderProjects(); renderTasks(); });
}

main().catch((error) => { if (!(error instanceof ApiError && error.status === 401)) toast('No se pudo cargar la información.', 'danger'); });
