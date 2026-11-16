import { ApiError } from '../core/api.js';
import { bindPasswordToggles, isEmail, nextUrl, passwordError, passwordStrength, setBusy, showError } from '../components/auth-form.js';
import { $ } from '../core/dom.js';
import { AuthService } from '../services/auth-service.js';

const LEVELS = [['', 0, '#e4e8f7'], ['Muy débil', 20, '#ef5b6b'], ['Débil', 40, '#f5a03a'], ['Aceptable', 60, '#e0b020'], ['Fuerte', 80, '#22b573'], ['Muy fuerte', 100, '#22b573']];

function validatePasswords() {
    const password = $('rPass').value;
    const error = passwordError(password);
    const [label, width, color] = LEVELS[password ? Math.max(1, passwordStrength(password)) : 0];
    $('rPass').setCustomValidity(error);
    $('rPassMsg').textContent = password ? error : 'Contraseña requerida.';
    $('rPass2').setCustomValidity($('rPass2').value !== password ? 'no coincide' : '');
    $('barraFuerza').style.width = `${width}%`;
    $('barraFuerza').style.background = color;
    $('txtFuerza').textContent = label ? `Seguridad: ${label}` : '';
}

async function main() {
    if (await AuthService.currentOrNull()) { location.replace(nextUrl()); return; }
    bindPasswordToggles();

    $('rEmail').addEventListener('input', () => $('rEmail').setCustomValidity(!$('rEmail').value || isEmail($('rEmail').value) ? '' : 'inválido'));
    ['rPass', 'rPass2'].forEach((id) => $(id).addEventListener('input', validatePasswords));

    $('formRegistro').addEventListener('submit', async (e) => {
        e.preventDefault();
        $('error').hidden = true;
        validatePasswords();
        e.target.classList.add('was-validated');
        if (!e.target.checkValidity()) return;

        setBusy($('btnRegistro'), true, 'Creando cuenta...');
        try {
            await AuthService.register($('rNombre').value, $('rEmail').value, $('rPass').value);
            location.href = 'index.html';
        } catch (error) {
            showError($('error'), error instanceof ApiError ? error.message : 'No se pudo conectar con el servidor.');
        } finally {
            setBusy($('btnRegistro'), false);
        }
    });
}

main();
