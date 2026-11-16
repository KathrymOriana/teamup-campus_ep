import { avatarNode, avatarsNode } from '../components/avatar.js';
import { bindProjectSelector } from '../components/project-selector.js';
import { initShell } from '../components/shell.js';
import { ApiError } from '../core/api.js';
import { $, renderList, show } from '../core/dom.js';
import { BADGE, BARRA, COLOR, fecha, iconoProyecto } from '../core/format.js';
import { ProjectContext } from '../core/project-context.js';
import { toast } from '../core/toast.js';
import { ProgressService } from '../services/progress-service.js';
import { ProjectService } from '../services/project-service.js';
import { TaskService } from '../services/task-service.js';

const CHART_COLOR = { verde: '#22b573', azul: '#3b46d9', naranja: '#f5a03a' };
const PRIORITY_COLOR = { Alta: '#ef5b6b', Media: '#f5a03a', Baja: '#22b573' };
const RECENT_TASKS = 5;

let projects = [];
let projectId = null;
let tasks = [];
let query = '';
let lineChart = null;
let doughnutChart = null;
const hiddenSlices = new Set();

const projectById = (id) => projects.find((p) => p.id === id);
const userById = (id) => projects.flatMap((p) => p.miembros).find((u) => u.id === id) ?? null;

function renderKpis(s) {
    $('kTotal').textContent = s.total;
    $('kVencidas').textContent = s.vencidas;
    [['kCompletadas', s.completadas, s.pctCompletadas], ['kProgreso', s.enProgreso, s.pctEnProgreso], ['kPendientes', s.pendientes, s.pctPendientes]]
        .forEach(([id, value, pct]) => {
            $(id).textContent = value;
            $(`${id}Pct`).textContent = `${pct}%`;
            $(`${id}Bar`).style.width = `${pct}%`;
        });
}

function renderCharts(s, serie) {
    Chart.defaults.font.family = "'Inter', system-ui, sans-serif";
    Chart.defaults.font.size = 10;
    Chart.defaults.color = '#6b7399';

    lineChart?.destroy();
    lineChart = new Chart($('graficoAvance'), {
        type: 'line',
        data: {
            labels: serie.etiquetas,
            datasets: [{
                data: serie.porcentajes, borderColor: CHART_COLOR.azul, borderWidth: 2, tension: 0.25, fill: true,
                pointBackgroundColor: CHART_COLOR.azul, pointRadius: 3, pointHoverRadius: 5,
                backgroundColor: (c) => {
                    const area = c.chart.chartArea;
                    if (!area) return 'transparent';
                    const g = c.chart.ctx.createLinearGradient(0, area.top, 0, area.bottom);
                    g.addColorStop(0, 'rgba(59,70,217,.22)');
                    g.addColorStop(1, 'rgba(59,70,217,.02)');
                    return g;
                },
            }],
        },
        options: {
            maintainAspectRatio: false,
            plugins: { legend: { display: false }, tooltip: { callbacks: { label: (c) => ` Completado: ${c.parsed.y}%` } } },
            scales: {
                y: { min: 0, max: 100, ticks: { stepSize: 25, callback: (v) => `${v}%` }, grid: { color: '#eef0f8' }, border: { display: false } },
                x: { grid: { display: false }, border: { display: false } },
            },
        },
    });

    const slices = [['Completadas', s.completadas, CHART_COLOR.verde, s.pctCompletadas], ['En progreso', s.enProgreso, CHART_COLOR.azul, s.pctEnProgreso], ['Pendientes', s.pendientes, CHART_COLOR.naranja, s.pctPendientes]];
    doughnutChart?.destroy();
    doughnutChart = new Chart($('graficoDistribucion'), {
        type: 'doughnut',
        data: { labels: slices.map((d) => d[0]), datasets: [{ data: slices.map((d) => d[1]), backgroundColor: slices.map((d) => d[2]), borderWidth: 0 }] },
        options: { cutout: '66%', maintainAspectRatio: false, plugins: { legend: { display: false } } },
    });
    hiddenSlices.forEach((i) => doughnutChart.toggleDataVisibility(i));
    doughnutChart.update();

    renderList($('leyenda'), 'tpl-legend-item', slices, ([nombre, valor, color, pct]) => ({
        values: { i: slices.findIndex((d) => d[0] === nombre), nombre, valor: `${valor} (${pct}%)`, color },
    }));
    [...$('leyenda').children].forEach((li, i) => li.classList.toggle('off', hiddenSlices.has(i)));
    $('distTotal').textContent = s.total;
}

function renderSide(report) {
    const { stats: s, proyectos, equipo, salud } = report;
    $('badgeAvance').textContent = `${s.pctCompletadas}% completado`;

    renderList($('porProyecto'), 'tpl-progress-project', proyectos, (p) => ({
        values: { icono: iconoProyecto(p.color), href: `progreso.html?proyecto=${p.id}`, nombre: p.nombre, ancho: `${p.avance}%`, barra: BARRA[p.color], avancePct: `${p.avance}%` },
    }));
    renderList($('porPrioridad'), 'tpl-priority-item', Object.entries(s.prioridad), ([nombre, valor]) => ({
        values: { nombre, valor, color: PRIORITY_COLOR[nombre] },
    }));

    $('equipoAvatares').replaceChildren(avatarsNode(equipo.miembros, 6));
    $('equipoActivo').textContent = `${equipo.activos} de ${equipo.miembros.length} integrantes activos`;

    $('kudosTitulo').textContent = salud.atencion ? 'Necesita atención' : s.total ? '¡Buen trabajo!' : 'Empieza a planificar';
    show($('motivos'), salud.atencion);
    show($('mensajeAnimo'), !salud.atencion);
    $('mensajeAnimo').textContent = s.total === 0 ? 'Aún no hay tareas: empiecen creando la primera.'
        : s.pctCompletadas >= 70 ? 'Excelente avance, están muy cerca de terminar.' : 'El equipo mantiene un buen ritmo en el proyecto.';
    renderList($('motivos'), 'tpl-motivo', salud.motivos, (texto) => ({ values: { texto } }));
}

function renderRecentTasks() {
    const list = tasks
        .filter((t) => t.titulo.toLowerCase().includes(query))
        .sort((a, b) => (a.estado === 'Completada') - (b.estado === 'Completada') || a.fecha.localeCompare(b.fecha))
        .slice(0, RECENT_TASKS);
    show($('tablaVacia'), list.length === 0);
    renderList($('tablaTareas'), 'tpl-recent-task', list, (t) => {
        const project = projectById(t.projectId);
        const user = userById(t.responsableId);
        return {
            values: {
                titulo: t.titulo, color: COLOR[project?.color], proyecto: project?.nombre ?? '', responsable: user?.nombre ?? 'Sin asignar',
                prioridad: t.prioridad, prioClass: BADGE[t.prioridad], fecha: fecha(t.fecha), estado: t.estado, estadoClass: BADGE[t.estado],
            },
            slots: { resp: avatarNode(user) },
        };
    });
}

async function load() {
    const [report, list] = await Promise.all([ProgressService.report(projectId), TaskService.list(projectId)]);
    tasks = list;
    $('titulo').textContent = projectId ? projectById(projectId).nombre : 'Todos los proyectos';
    renderKpis(report.stats);
    renderCharts(report.stats, report.serie);
    renderSide(report);
    renderRecentTasks();
}

async function main() {
    await initShell();
    ProjectContext.applyQueryParam();
    projects = await ProjectService.list();
    projectId = bindProjectSelector($('selProyecto'), projects, {
        allowAll: true,
        onChange: (id) => { projectId = id; load().catch(fail); },
    });

    $('leyenda').addEventListener('click', (e) => {
        const li = e.target.closest('li[data-i]');
        if (!li) return;
        const i = Number(li.dataset.i);
        if (hiddenSlices.has(i)) hiddenSlices.delete(i); else hiddenSlices.add(i);
        doughnutChart.toggleDataVisibility(i);
        doughnutChart.update();
        li.classList.toggle('off');
    });
    $('buscador').addEventListener('input', (e) => { query = e.target.value.trim().toLowerCase(); renderRecentTasks(); });
    await load();
}

const fail = (error) => { if (!(error instanceof ApiError && error.status === 401)) toast('No se pudo cargar la información.', 'danger'); };
main().catch(fail);
