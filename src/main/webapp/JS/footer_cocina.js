// Ruta base: sale de donde se cargó este script
const baseFooterCocina = document.currentScript.src.replace(/JS\/footer_cocina\.js.*$/, '');

const footerCocina = document.createElement('footer');

// misma clase que el footer de inicio para reutilizar su css
footerCocina.className = 'main-footer-inicio';

footerCocina.innerHTML = `
    <div class="footer_contenido">
        <div class="footer_marca">
            <div class="footer_logo">
                <img src="${baseFooterCocina}IMAGENES/logo.jpeg" alt="one_more_dish"/>
                <h4>One More Dish</h4>
            </div>
            <p>Modo cocina. Prepara cada platillo a tiempo y ten tus insumos siempre bajo control.</p>
        </div>

        <div class="footer_columna">
            <h5>Navegación</h5>
            <a href="${baseFooterCocina}HTML/inicio_cocina.html">Home</a>
            <a href="${baseFooterCocina}HTML/menu_cocina.html">Menú</a>
            <a href="${baseFooterCocina}HTML/inventario.html">Inventario</a>
            <a href="${baseFooterCocina}HTML/pedidos_domicio.html">Pedidos a domicilio</a>
        </div>

        <div class="footer_columna">
            <h5>En cocina</h5>
            <span>Platillos</span>
            <span>Insumos</span>
            <span>Entregas</span>
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

document.body.append(footerCocina);