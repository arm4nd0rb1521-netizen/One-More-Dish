(function () {
    const base = document.currentScript.src.replace(/JS\/footer_admin\.js.*$/, '');

    const footer = document.createElement('footer');

    // misma clase que el footer de inicio para reutilizar el css
    footer.className = 'main-footer-inicio';

    footer.innerHTML = `
        <div class="footer_contenido">
            <div class="footer_marca">
                <div class="footer_logo">
                    <img src="${base}IMAGENES/logo.jpeg" alt="one_more_dish"/>
                    <h4>One More Dish</h4>
                </div>
                <p>Modo administración. Controla el menú, las mesas, los pedidos y todo tu equipo desde un solo lugar.</p>
            </div>

            <div class="footer_columna">
                <h5>Navegación</h5>
                <a href="${base}HTML/inicio_admin.html">Home</a>
                <a href="${base}HTML/menu_admin.html">Menú</a>
                <a href="${base}HTML/mesas_admin.html">Mesas</a>
                <a href="${base}HTML/domicilio_admin.html">P. domicilio</a>
                <a href="${base}HTML/personal_admin.html">Personal</a>
                <a href="${base}HTML/ventas_admin.html">Ventas</a>
                <a href="${base}HTML/cocina_admin.html">Cocina</a>
                <a href="${base}HTML/proveedores_admin.html">Proveedores</a>
            </div>

            <div class="footer_columna">
                <h5>En administración</h5>
                <span>Platillos</span>
                <span>Equipo</span>
                <span>Finanzas</span>
            </div>

            <div class="footer_columna">
                <h5>Contacto</h5>
                <span>hola@onemoredish.com</span>
                <span>+52 55 0000 0000</span>
                <span>Ciudad de México</span>
            </div>
        </div>

        <div class="footer_inferior">
            <p>© 2026 One More Dish. Todos los derechos reservados.</p>
            <div class="footer_legal">
                <a href="#">Privacidad</a>
                <a href="#">Términos</a>
            </div>
        </div>
    `;

    document.body.append(footer);
})();

