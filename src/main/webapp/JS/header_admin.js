const header = document.createElement('header');

// nombre para css
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
        <a href="${base}HTML/inicio_admin.html">Home</a>
        <a href="${base}HTML/menu_admin.html">Menú</a>
        <a href="${base}HTML/mesas_admin.html">Mesas</a>
        <a href="${base}HTML/domicilio_admin.html">P. domicilio</a>
        <a href="${base}HTML/personal_admin.html">Personal</a>
        <a href="${base}HTML/ventas_admin.html">Ventas</a>
        <a href="${base}HTML/cocina_admin.html">Cocina</a>
        <a href="${base}HTML/proveedores_admin.html">Proveedores</a>
    </div>
`;

document.body.prepend(header);