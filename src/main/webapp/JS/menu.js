const lista = document.getElementById('lista_platillos');
const contador = document.getElementById('contador');
const buscar = document.getElementById('buscar');
let platillos = [];

function cerrarMenus(excepto) {
    document.querySelectorAll('.menu_flotante').forEach(m => {
        if (m !== excepto) m.hidden = true;
    });
}
document.addEventListener('click', () => cerrarMenus(null));

function tarjeta(p) {
    const art = document.createElement('article');
    art.className = 'tarjeta_platillo';

    // Foto (si no hay, queda el fondo con emoji)
    const foto = document.createElement('div');
    foto.className = 'foto';
    const img = new Image();
    img.alt = p.nombre;
    img.loading = 'lazy';
    img.src = '../Imagen_Platillo_Servlet?id=' + p.id;
    img.onerror = () => { foto.classList.add('sin_foto'); img.remove(); };
    foto.append(img);

    // Botón ⋯ con opción Eliminar
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'btn_opciones';
    btn.textContent = '⋯';
    btn.setAttribute('aria-label', 'Opciones de ' + p.nombre);

    const menu = document.createElement('div');
    menu.className = 'menu_flotante';
    menu.hidden = true;
    menu.addEventListener('click', e => e.stopPropagation());

    const form = document.createElement('form');
    form.method = 'post';
    form.action = '../Eliminar_Platillo_Servlet';
    form.onsubmit = () => confirm('¿Eliminar "' + p.nombre + '" del menú?');

    const id = document.createElement('input');
    id.type = 'hidden';
    id.name = 'id_platillo';
    id.value = p.id;

    // NUEVO: le dice al servlet desde dónde se hizo la acción
    const origen = document.createElement('input');
    origen.type = 'hidden';
    origen.name = 'origen';
    origen.value = 'admin';

    const del = document.createElement('button');
    del.type = 'submit';
    del.textContent = '🗑 Eliminar platillo';

    form.append(id, origen, del);
    menu.append(form);
    btn.onclick = e => {
        e.stopPropagation();
        cerrarMenus(menu);
        menu.hidden = !menu.hidden;
    };

    // Texto (textContent evita inyectar HTML desde la base de datos)
    const info = document.createElement('div');
    info.className = 'info_platillo';
    const h3 = document.createElement('h3');
    h3.textContent = p.nombre;
    const desc = document.createElement('p');
    desc.textContent = p.descripcion;
    const precio = document.createElement('span');
    precio.className = 'precio_platillo';
    precio.textContent = '$' + Number(p.precio).toFixed(2);
    info.append(h3, desc, precio);

    art.append(foto, btn, menu, info);
    return art;
}

function render() {
    const f = buscar.value.trim().toLowerCase();
    const visibles = platillos.filter(p => p.nombre.toLowerCase().includes(f));

    lista.replaceChildren(...visibles.map(tarjeta));

    contador.textContent = visibles.length === 1
        ? '1 platillo' : visibles.length + ' platillos';

    if (visibles.length === 0) {
        const vacio = document.createElement('div');
        vacio.className = 'vacio_menu';
        vacio.textContent = platillos.length === 0
            ? 'Aún no hay platillos. ¡Agrega el primero!'
            : 'No encontramos platillos con ese nombre.';
        lista.append(vacio);
    }
}

buscar.addEventListener('input', render);

fetch('../Menu_Platillos_Servlet')
    .then(r => { if (!r.ok) throw new Error(); return r.json(); })
    .then(datos => { platillos = datos; render(); })
    .catch(() => {
        contador.textContent = 'No se pudo cargar el menú';
        const err = document.createElement('div');
        err.className = 'vacio_menu';
        err.textContent = 'Revisa que el servidor y la base de datos estén encendidos.';
        lista.append(err);
    });

// Avisos al volver de crear o eliminar
const q = new URLSearchParams(location.search);
const aviso = document.getElementById('aviso_menu');
const msgs = {
    creado: ['Platillo creado correctamente.', 'ok'],
    eliminado: ['Platillo eliminado.', 'ok'],
    error: ['No se pudo completar la acción. Intenta de nuevo.', 'error']
};
for (const k in msgs) {
    if (q.has(k)) {
        aviso.textContent = msgs[k][0];
        aviso.className = 'aviso_menu ' + msgs[k][1];
        aviso.hidden = false;
    }
}

