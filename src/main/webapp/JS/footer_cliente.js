const baseFooterCliente = document.currentScript.src.replace(/JS\/footer_cliente\.js.*$/, '');

const footerCliente = document.createElement('footer');

footerCliente.className = 'main-footer-inicio';

footerCliente.innerHTML = `
    <div class="footer_contenido">
        <div class="footer_marca">
            <div class="footer_logo">
                <img src="${baseFooterCliente}IMAGENES/logo.jpeg" alt="one_more_dish"/>
                <h4>One More Dish</h4>
            </div>
            <p>Platillos hechos al momento, directo a tu mesa o a tu puerta.</p>
        </div>

        <div class="footer_columna">
            <h5>Navegación</h5>
            <a href="${baseFooterCliente}HTML/inicio_cliente.html">Home</a>
            <a href="${baseFooterCliente}HTML/platillos_cliente.html">Platillos</a>
            <a href="${baseFooterCliente}HTML/pedidos_cliente.html">Pedidos a domicilio</a>
        </div>

        <div class="footer_columna">
            <h5>Para ti</h5>
            <span>Menú</span>
            <span>Entregas</span>
            <span>Seguimiento</span>
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

document.body.append(footerCliente);