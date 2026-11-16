/** Inserta fragmentos HTML compartidos (menú lateral, barra superior, avisos) en los elementos [data-include]. */
export async function loadIncludes(root = document) {
    const hosts = [...root.querySelectorAll('[data-include]')];
    await Promise.all(hosts.map(async (host) => {
        const response = await fetch(host.dataset.include);
        if (!response.ok) throw new Error(`No se pudo cargar ${host.dataset.include}`);
        host.innerHTML = await response.text();
        host.removeAttribute('data-include');
    }));
}
