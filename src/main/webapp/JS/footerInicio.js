// Ruta base: sale de donde se cargó este script (sirve en index y en HTML/)
const baseFooter = document.currentScript.src.replace(/JS\/footerInicio\.js.*$/, '');

const footerInicio = document.createElement('footer');

// nombre para css
footerInicio.className = 'main-footer-inicio';

footerInicio.innerHTML = `
    <div class="footer_contenido">
        <div class="footer_marca">
            <div class="footer_logo">
                <img src="${baseFooter}IMAGENES/logo.jpeg" alt="one_more_dish"/>
                <h4>One More Dish</h4>
            </div>
            <p>Tu restaurante, en un solo lugar. Pide, reserva y gestiona todo con una sola cuenta.</p>
        </div>

        <div class="footer_columna">
            <h5>Navegación</h5>
            <a href="${baseFooter}index.html">Inicio</a>
            <a href="${baseFooter}HTML/que_ofrecemos.html">Qué ofrecemos</a>
            <a href="${baseFooter}HTML/para_cada_persona.html">Para cada persona</a>
            <a href="${baseFooter}HTML/contacto.html">Contacto</a>
        </div>

        <div class="footer_columna">
            <h5>Para ti</h5>
            <span>Cliente</span>
            <span>Administrador</span>
            <span>Cocina</span>
            <span>Personal de sala</span>
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

document.body.append(footerInicio);