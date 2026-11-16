import { ApiError } from '../core/api.js';
import { bindPasswordToggles, passwordError, setBusy, showError } from '../components/auth-form.js';
import { $ } from '../core/dom.js';
import { AuthService } from '../services/auth-service.js';

function validatePasswords() {
    const password = $('cPass').value;
    const error = passwordError(password);
    $('cPass').setCustomValidity(error);
    $('cPassMsg').textContent = password ? error : 'Contraseña requerida.';
    $('cPass2').setCustomValidity($('cPass2').value !== password ? 'no coincide' : '');
}

bindPasswordToggles();
['cPass', 'cPass2'].forEach((id) => $(id).addEventListener('input', validatePasswords));

$('formRecuperar').addEventListener('submit', async (e) => {
    e.preventDefault();
    $('error').hidden = true;
    validatePasswords();
    e.target.classList.add('was-validated');
    if (!e.target.checkValidity()) return;

    setBusy($('btnRecuperar'), true, 'Guardando...');
    try {
        await AuthService.resetPassword($('cEmail').value, $('cPass').value);
        e.target.hidden = true;
        $('exito').hidden = false;
    } catch (error) {
        showError($('error'), error instanceof ApiError ? error.message : 'No se pudo conectar con el servidor.');
    } finally {
        setBusy($('btnRecuperar'), false);
    }
});
