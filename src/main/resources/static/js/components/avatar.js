import { iniciales } from '../core/format.js';

/** Avatar de un usuario { id, nombre, foto }: foto si existe, iniciales de color o icono si no hay responsable. */
export function avatarNode(user, { size } = {}) {
    let node;
    if (!user) {
        node = document.createElement('span');
        node.className = 'avatar avatar--ini avatar--none';
        node.title = 'Sin asignar';
        const icon = document.createElement('i');
        icon.className = 'bi bi-person-dash';
        node.append(icon);
    } else if (user.foto) {
        node = new Image();
        node.className = 'avatar';
        node.src = `assets/img/resp-${user.id}.png`;
        node.alt = user.nombre;
        node.title = user.nombre;
    } else {
        node = document.createElement('span');
        node.className = 'avatar avatar--ini';
        node.title = user.nombre;
        node.textContent = iniciales(user.nombre);
        node.style.background = `hsl(${(user.id * 67) % 360} 55% 45%)`;
    }
    if (size) {
        node.style.width = `${size}px`;
        node.style.height = `${size}px`;
        node.style.margin = '0';
    }
    return node;
}

/** Fila de avatares superpuestos con el contador "+N". */
export function avatarsNode(users, max = 3) {
    const wrap = document.createElement('span');
    wrap.className = 'avatars';
    wrap.append(...users.slice(0, max).map((u) => avatarNode(u)));
    if (users.length > max) {
        const more = document.createElement('span');
        more.className = 'avatars__more';
        more.textContent = `+${users.length - max}`;
        wrap.append(more);
    }
    return wrap;
}
