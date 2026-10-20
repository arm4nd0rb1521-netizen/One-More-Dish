(function () {
    // Ruta base: sale de donde se cargó este script (no depende del nombre del proyecto)
    const base = document.currentScript.src.replace(/JS\/header_cocina\.js.*$/, '');

    const header = document.createElement('header');

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
            <a href="${base}HTML/inicio_cocina.html">Home</a>
            <a href="${base}HTML/menu_cocina.html">Menú</a>
            <a href="${base}HTML/inventario.html">Inventario</a>
            <a href="${base}HTML/pedidos_domicio.html">Pedidos a domicilio</a>
        </div>
    `;

    document.body.prepend(header);
})();