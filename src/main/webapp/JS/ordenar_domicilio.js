/*
 * ordenar_domicilio.js — el cliente arma su pedido y lo envía.
 * El carrito se guarda en sessionStorage para no perderlo si recarga la página.
 */
(function () {
    const API_MENU = '../Menu_Platillos_Servlet';
    const API_PEDIDOS = '../Pedidos_Cliente_Servlet';
    const CLAVE_CARRITO = 'omd_carrito';
    const MAX_POR_PLATILLO = 20;      // mismos límites que Pedidos_Cliente_Servlet
    const MAX_TOTAL = 50;

    const lista = document.getElementById('lista_platillos');
    const contador = document.getElementById('contador');
    const buscar = document.getElementById('buscar');
    const ulCarrito = document.getElementById('carrito_lista');
    const vacio = document.getElementById('carrito_vacio');
    const totalEl = document.getElementById('carrito_total');
    const direccion = document.getElementById('direccion');
    const contadorDir = document.getElementById('contador_dir');
    const btnEnviar = document.getElementById('btn_enviar');
    const toast = document.getElementById('toast');

    let platillos = [];     // menú que viene del servidor
    let carrito = {};       // { idPlatillo: cantidad }

    // ----- Ayudas -----
    function el(tag, clase, texto) {
        const e = document.createElement(tag);
        if (clase) e.className = clase;
        if (texto !== undefined) e.textContent = texto;
        return e;
    }

    function boton(clase, texto, alClic, etiqueta) {
        const b = el('button', clase, texto);
        b.type = 'button';
        if (etiqueta) b.setAttribute('aria-label', etiqueta);
        b.addEventListener('click', alClic);
        return b;
    }

    const dinero = n => '$' + Number(n || 0).toFixed(2);

    function avisar(texto, tipo) {
        toast.textContent = texto;
        toast.className = 'toast ' + (tipo || 'ok') + ' visible';
        clearTimeout(avisar.t);
        avisar.t = setTimeout(() => toast.classList.remove('visible'), 3000);
    }

    function guardarCarrito() {
        try { sessionStorage.setItem(CLAVE_CARRITO, JSON.stringify(carrito)); } catch (e) { /* sin almacenamiento */ }
    }

    function leerCarrito() {
        try { carrito = JSON.parse(sessionStorage.getItem(CLAVE_CARRITO)) || {}; } catch (e) { carrito = {}; }
    }

    const totalPlatillos = () => Object.values(carrito).reduce((a, b) => a + b, 0);
    const buscarPlatillo = id => platillos.find(p => p.id === Number(id));

    // ----- Cambiar cantidades -----
    function cambiar(id, delta) {
        const actual = carrito[id] || 0;
        const nueva = actual + delta;

        if (delta > 0 && totalPlatillos() >= MAX_TOTAL) {
            avisar('Máximo ' + MAX_TOTAL + ' platillos por pedido', 'error');
            return;
        }
        if (nueva > MAX_POR_PLATILLO) {
            avisar('Máximo ' + MAX_POR_PLATILLO + ' de cada platillo', 'error');
            return;
        }

        if (nueva <= 0) delete carrito[id];
        else carrito[id] = nueva;

        guardarCarrito();
        renderMenu();
        renderCarrito();
    }

    // ----- Menú -----
    function controlCantidad(p) {
        const cant = carrito[p.id] || 0;
        if (cant === 0) {
            return boton('btn_principal btn_agregar', 'Agregar', () => cambiar(p.id, 1));
        }
        const caja = el('div', 'cantidad_platillo');
        caja.append(
            boton('', '−', () => cambiar(p.id, -1), 'Quitar uno de ' + p.nombre),
            el('span', '', String(cant)),
            boton('', '+', () => cambiar(p.id, 1), 'Agregar otro ' + p.nombre)
        );
        return caja;
    }

    function tarjeta(p) {
        const art = el('article', 'tarjeta_platillo' + (carrito[p.id] ? ' en_carrito' : ''));

        const foto = el('div', 'foto');
        const img = new Image();
        img.alt = p.nombre;
        img.loading = 'lazy';
        img.src = '../Imagen_Platillo_Servlet?id=' + p.id;
        img.onerror = () => { foto.classList.add('sin_foto'); img.remove(); };
        foto.append(img);

        const info = el('div', 'info_platillo');
        info.append(
            el('h3', '', p.nombre),
            el('p', '', p.descripcion),
            el('span', 'precio_platillo', dinero(p.precio)),
            controlCantidad(p)
        );

        art.append(foto, info);
        return art;
    }

    function renderMenu() {
        const f = buscar.value.trim().toLowerCase();
        const visibles = platillos.filter(p => p.nombre.toLowerCase().includes(f));

        lista.replaceChildren(...visibles.map(tarjeta));
        if (visibles.length === 0) {
            lista.append(el('div', 'vacio_menu', platillos.length === 0
                ? 'Por ahora no hay platillos disponibles.'
                : 'No encontramos platillos con ese nombre.'));
        }
    }

    // ----- Carrito -----
    function renderCarrito() {
        let total = 0;
        const filas = [];

        Object.keys(carrito).forEach(id => {
            const p = buscarPlatillo(id);
            if (!p) return;
            const cant = carrito[id];
            total += p.precio * cant;

            const li = el('li');
            const control = el('div', 'cantidad_platillo chica');
            control.append(
                boton('', '−', () => cambiar(p.id, -1), 'Quitar uno de ' + p.nombre),
                el('span', '', String(cant)),
                boton('', '+', () => cambiar(p.id, 1), 'Agregar otro ' + p.nombre)
            );
            li.append(el('span', 'carrito_nombre', p.nombre), control, el('span', 'carrito_sub', dinero(p.precio * cant)));
            filas.push(li);
        });

        ulCarrito.replaceChildren(...filas);
        vacio.hidden = filas.length > 0;
        totalEl.textContent = dinero(total);

        const n = totalPlatillos();
        contador.textContent = n === 0 ? 'Elige tus platillos'
            : n + (n === 1 ? ' platillo en tu pedido' : ' platillos en tu pedido');

        validar();
    }

    function validar() {
        btnEnviar.disabled = totalPlatillos() === 0 || direccion.value.trim().length < 10;
    }

    // ----- Enviar -----
    async function enviar() {
        btnEnviar.disabled = true;
        btnEnviar.textContent = 'Enviando…';

        // platillo=3&cantidad=2&platillo=7&cantidad=1 ...
        const datos = new URLSearchParams();
        datos.append('direccion', direccion.value.trim());
        Object.keys(carrito).forEach(id => {
            datos.append('platillo', id);
            datos.append('cantidad', carrito[id]);
        });

        try {
            const r = await fetch(API_PEDIDOS, {
                method: 'POST',
                headers: { 'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8' },
                body: datos
            });

            if (r.status === 401) {
                avisar('Inicia sesión para hacer tu pedido', 'error');
                setTimeout(() => { location.href = 'signin.html'; }, 1500);
                return;
            }

            const j = await r.json();
            if (!j.ok) throw new Error(j.mensaje || 'No se pudo enviar el pedido');

            // Listo: vaciamos el carrito y vamos a "Mis pedidos"
            carrito = {};
            guardarCarrito();
            location.href = 'pedidos_cliente.html?creado=' + j.id;

        } catch (e) {
            avisar(e.message, 'error');
            btnEnviar.textContent = 'Enviar pedido';
            validar();
        }
    }

    // ----- Eventos -----
    buscar.addEventListener('input', renderMenu);
    direccion.addEventListener('input', () => {
        contadorDir.textContent = direccion.value.length;
        validar();
    });
    btnEnviar.addEventListener('click', enviar);

    // ----- Inicio -----
    leerCarrito();
    fetch(API_MENU)
        .then(r => { if (!r.ok) throw new Error(); return r.json(); })
        .then(datos => {
            platillos = datos;
            // Si un platillo del carrito guardado ya no existe, se quita
            Object.keys(carrito).forEach(id => { if (!buscarPlatillo(id)) delete carrito[id]; });
            guardarCarrito();
            renderMenu();
            renderCarrito();
        })
        .catch(() => {
            contador.textContent = 'No se pudo cargar el menú';
            lista.append(el('div', 'vacio_menu', 'Revisa que el servidor y la base de datos estén encendidos.'));
        });
})();