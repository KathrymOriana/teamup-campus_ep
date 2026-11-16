/**
 * Utilidades de DOM. La estructura HTML vive en los .html (como <template>); el JavaScript solo
 * clona esas plantillas y rellena los datos con textContent (sin innerHTML, así no hay XSS).
 *
 * Convención en las plantillas:
 *   data-text="clave"                    -> textContent
 *   data-attr="atributo:clave; otro:k2"  -> setAttribute (si el valor es nulo, quita el atributo)
 *   data-style="propiedad:clave"         -> style.setProperty
 *   data-class="clave"                   -> añade las clases del valor
 *   data-slot="clave"                    -> recibe nodos hijos (ver parámetro `slots`)
 */
export const $ = (id) => document.getElementById(id);
export const qs = (selector, root = document) => root.querySelector(selector);
export const qsa = (selector, root = document) => [...root.querySelectorAll(selector)];

const within = (root, selector) => [...(root.matches(selector) ? [root] : []), ...root.querySelectorAll(selector)];
const parsePairs = (text) => text.split(';').map((p) => p.split(':').map((s) => s.trim())).filter(([a, k]) => a && k);

/** Clona el primer elemento de un <template>. */
export const cloneTemplate = (id) => $(id).content.firstElementChild.cloneNode(true);

/** Rellena un nodo clonado con `values`; `slots` inserta nodos en los [data-slot]. */
export function fill(root, values = {}, slots = {}) {
    within(root, '[data-text]').forEach((el) => { el.textContent = values[el.dataset.text] ?? ''; });
    within(root, '[data-attr]').forEach((el) => {
        parsePairs(el.dataset.attr).forEach(([attr, key]) => {
            const value = values[key];
            if (value == null || value === false) el.removeAttribute(attr); else el.setAttribute(attr, value === true ? '' : value);
        });
    });
    within(root, '[data-style]').forEach((el) => {
        parsePairs(el.dataset.style).forEach(([prop, key]) => { if (values[key] != null) el.style.setProperty(prop, values[key]); });
    });
    within(root, '[data-class]').forEach((el) => {
        const value = values[el.dataset.class];
        if (value) el.classList.add(...String(value).split(' '));
    });
    Object.entries(slots).forEach(([key, nodes]) => {
        within(root, `[data-slot="${key}"]`).forEach((el) => el.replaceChildren(...[].concat(nodes)));
    });
    return root;
}

/** Vacía `container` y lo llena con una plantilla por elemento. `toValues` devuelve { values, slots }. */
export function renderList(container, templateId, items, toValues) {
    container.replaceChildren(...items.map((item) => {
        const { values, slots } = toValues(item);
        return fill(cloneTemplate(templateId), values, slots);
    }));
}

/** Muestra u oculta un elemento usando el atributo `hidden`. */
export const show = (el, visible) => { el.hidden = !visible; };

/** Llena un <select> con opciones { value, label }. */
export function setOptions(select, options, selected) {
    select.replaceChildren(...options.map(({ value, label }) => new Option(label, value)));
    if (selected !== undefined && selected !== null) select.value = String(selected);
}
