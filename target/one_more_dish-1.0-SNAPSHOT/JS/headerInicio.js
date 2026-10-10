const headerInicio = document.createElement('header');

// nombre para css
headerInicio.className = 'main-header-inicio';

headerInicio.innerHTML = `
    <div class="principal_inicio">
        <div class="nombre_y_logo">
            <img src="IMAGENES/prueba.png" alt="one_more_dish"/>
            <h4>One More Dish</h4>
        </div>
        <div class="incio_registro">
            <a href="HTML/signin.html">
                <button value="iniciar_sesion" name="signin">Iniciar Sesion</button>
            </a>
            <a href="HTML/signup.html">
                <button value="registrarse" name="signup">Registrarse</button>
            </a>
        </div>
    </div>
    <div class="navegacion">
        <a href="index.html">Inicio</a>
        <a href="HTML/que_ofrecemos.html">Que ofrecemos</a>
        <a href="HTML/para_cada_persona.html">Para cada persona</a>
        <a href="HTML/contacto.html">Contacto</a>
    </div>
`;

document.body.prepend(headerInicio);

