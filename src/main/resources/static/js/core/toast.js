import { $, cloneTemplate, fill } from './dom.js';

/** Aviso temporal (toast de Bootstrap). La estructura está en partials/toast.html. */
export function toast(message, type = 'success') {
    const element = fill(cloneTemplate('tpl-toast'), { mensaje: message });
    element.classList.add(`text-bg-${type}`);
    $('toastBox').append(element);
    element.addEventListener('hidden.bs.toast', () => element.remove());
    new bootstrap.Toast(element, { delay: 3000 }).show();
}

/** Valida un formulario con las reglas HTML5 y marca los campos faltantes. */
export function validate(form) {
    form.classList.add('was-validated');
    if (form.checkValidity()) return true;
    toast('Completa los campos obligatorios marcados en rojo.', 'danger');
    return false;
}
