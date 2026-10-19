const header = document.createElement('header');

header.className = 'main-header-inicio';

const base = window.location.origin + '/one_more_dish/';

header.innerHTML = `
    <div class="principal_inicio">
        <div class="nombre_y_logo">
            <img src="${base}IMAGENES/logo.jpeg" alt="one_more_dish"/>
            <h4>One More Dish</h4>
        </div>
    </div>
    <div class="navegacion">
        <a href="${base}HTML/inicio_cliente.html">Home</a>
        <a href="${base}HTML/FALTA EL MENU">Menú</a>
        <a href="${base}HTML/pedidos_domicio.html">Pedidos a domicilio</a>
    </div>
`;

header.querySelectorAll('.navegacion a').forEach(enlace => {
    if (window.location.pathname.endsWith(enlace.getAttribute('href').split('/').pop())) {
        enlace.classList.add('activo');
    }
});

document.body.prepend(header);