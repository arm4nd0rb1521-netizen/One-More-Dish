(function () {
    // Ruta base: sale de donde se cargó este script (funciona en local y en Azure)
    const base = document.currentScript.src.replace(/JS\/header_cliente\.js.*$/, '');

    const header = document.createElement('header');
    
    // Oculta la página hasta confirmar que hay sesión
    document.documentElement.style.visibility = 'hidden';

    function verificarSesion() {
        fetch(base + 'Sesion_Servlet', { cache: 'no-store', credentials: 'same-origin' })
            .then(function (r) {
                if (r.ok) {
                    document.documentElement.style.visibility = 'visible';
                } else {
                    window.location.replace(base);
                }
            })
            .catch(function () {
                window.location.replace(base);
            });
    }

    verificarSesion();

    // Al volver con la flecha, la página puede restaurarse desde la caché: se verifica de nuevo
    window.addEventListener('pageshow', function (e) {
        if (e.persisted) {
            verificarSesion();
        }
    });

    // nombre para css
    header.className = 'main-header-inicio';

    header.innerHTML = `
        <div class="principal_inicio">
            <div class="nombre_y_logo">
                <img src="${base}IMAGENES/logo.jpeg" alt="one_more_dish"/>
                <h4>One More Dish</h4>
            </div>
        </div>
        <div class="navegacion">
            <a href="${base}HTML/inicio_cliente.html">Home</a>
            <a href="${base}HTML/menu_cliente.html">Menú</a>
            <a href="${base}HTML/ordenar_domicilio.html">Pedir a domicilio</a>
            <a href="${base}HTML/pedidos_cliente.html">Mis pedidos</a>
            <a href="${base}Logout_Servlet" class="btn-cerrar-sesion">Cerrar sesión</a>
        </div>
    `;

    // Marca la página actual
    header.querySelectorAll('.navegacion a').forEach(enlace => {
        if (window.location.pathname.endsWith(enlace.getAttribute('href').split('/').pop())) {
            enlace.classList.add('activo');
        }
    });

    document.body.prepend(header);
})();