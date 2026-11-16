/** Formato de fechas y tablas de apariencia (clases CSS y colores por valor). */
export const MES = ['ene', 'feb', 'mar', 'abr', 'may', 'jun', 'jul', 'ago', 'sep', 'oct', 'nov', 'dic'];

export const fecha = (iso) => {
    if (!iso) return '—';
    const [y, m, d] = iso.slice(0, 10).split('-');
    return `${Number(d)} ${MES[Number(m) - 1]} ${y}`;
};

export const BADGE = {
    Alta: 'badge-alta', Media: 'badge-media', Baja: 'badge-baja',
    'En progreso': 'badge-progreso', Pendiente: 'badge-pendiente', Completada: 'badge-baja',
};

export const BADGE_PROYECTO = { 'En progreso': 'badge-progreso', Pendiente: 'badge-naranja', 'En planificación': 'badge-planificacion' };

export const COLOR = { purple: '#6a4de0', green: '#22b573', blue: '#3b46d9', orange: '#f5a03a' };

export const BARRA = {
    purple: 'linear-gradient(90deg,#3b46d9,#7b8cf5)',
    green: 'linear-gradient(90deg,#22b573,#5fd3a2)',
    blue: 'linear-gradient(90deg,#5b72f0,#a9b6fb)',
    orange: '#a9b6fb',
};

const ICONO = { purple: 1, green: 2, blue: 3, orange: 4 };
export const iconoProyecto = (color) => `assets/img/icono-proyecto-${ICONO[color] ?? 4}.png`;

export const iniciales = (nombre) => nombre.split(' ').map((p) => p[0]).slice(0, 2).join('').toUpperCase();

export const hoyIso = () => new Date().toLocaleDateString('sv-SE'); // AAAA-MM-DD en hora local
