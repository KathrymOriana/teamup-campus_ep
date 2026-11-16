import { ApiError } from '../core/api.js';
import { bindPasswordToggles, nextUrl, setBusy, showError } from '../components/auth-form.js';
import { $ } from '../core/dom.js';
import { AuthService } from '../services/auth-service.js';

const DEMO_PASSWORD = 'teamup123';

async function main() {
    if (await AuthService.currentOrNull()) { location.replace(nextUrl()); return; }
    bindPasswordToggles();

    $('demoCuentas').addEventListener('click', (e) => {
        const button = e.target.closest('[data-email]');
        if (!button) return;
        $('lEmail').value = button.dataset.email;
        $('lPass').value = DEMO_PASSWORD;
        $('error').hidden = true;
        $('lPass').focus();
    });

    $('formLogin').addEventListener('submit', async (e) => {
        e.preventDefault();
        $('error').hidden = true;
        e.target.classList.add('was-validated');
        if (!e.target.checkValidity()) return;

        setBusy($('btnLogin'), true, 'Ingresando...');
        try {
            await AuthService.login($('lEmail').value, $('lPass').value);
            location.href = nextUrl();
        } catch (error) {
            showError($('error'), error instanceof ApiError ? error.message : 'No se pudo conectar con el servidor.');
            $('lPass').select();
        } finally {
            setBusy($('btnLogin'), false);
        }
    });
}

main();
