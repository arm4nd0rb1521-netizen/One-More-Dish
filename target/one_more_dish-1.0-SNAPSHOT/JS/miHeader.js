const header = document.createElement('header');

// nombre para css
header.className = 'main-header';

header.innerHTML = `
    <div class="principal">
        <img src="../IMAGENES/prueba.png" alt="one_more_dish"
        <h4>One More Dish</h4>
    </div>
    <div class="navegacion">
        <a href="../HTML/prueba1.html">Ir a otra pagina</a>
    </div>
`;

document.body.prepend(header);


