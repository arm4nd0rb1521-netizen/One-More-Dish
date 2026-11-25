/*
 * pedidos_cliente.js — "Mis pedidos" del cliente, con seguimiento en vivo por el socket.
 */
(function () {
    const API = '../Pedidos_Cliente_Servlet';

    // Pasos que ve el cliente (Cancelado se muestra aparte)
    const PASOS = [
        ['pendiente', 'Recibido'],
        ['en preparacion', 'En preparación'],
        ['listo', 'Listo'],
        ['en camino', 'En camino'],
        ['entregado', 'Entregado']
    ];

    const lista = document.getElementById('lista_mis_pedidos');
    const aviso = document.getElementById('aviso');
    const indicador = document.getElementById('indicador_vivo');
    const toast = document.getElementById('toast');

    let pedidos = [];
    let resaltar = null;           // id del pedido recién creado

    // ----- Ayudas -----
    function el(tag, clase, texto) {
        const e = document.createElement(tag);
        if (clase) e.className = clase;
        if (texto !== undefined) e.textContent = texto;
        return e;
    }

    const norm = s => (s || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '').trim();
    const dinero = n => '$' + Number(n || 0).toFixed(2);

    function fecha(iso) {
        if (!iso) return '';
        return new Date(iso).toLocaleString('es-MX',
            { day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit' });
    }

    function avisar(texto, tipo) {
        toast.textContent = texto;
        toast.className = 'toast ' + (tipo || 'ok') + ' visible';
        clearTimeout(avisar.t);
        avisar.t = setTimeout(() => toast.classList.remove('visible'), 3500);
    }

    function irALogin() {
        location.href = 'signin.html';
    }

    // ----- Dibujar -----
    function barraPasos(estado) {
        const actual = PASOS.findIndex(p => p[0] === norm(estado));
        const ol = el('ol', 'pasos_pedido');
        PASOS.forEach((paso, i) => {
            const clase = i < actual ? 'hecho' : i === actual ? 'actual' : '';
            ol.append(el('li', clase, paso[1]));
        });
        return ol;
    }

    function tarjeta(p) {
        const cancelado = norm(p.estado) === 'cancelado';
        const art = el('article', 'pedido_cliente' + (p.id === resaltar ? ' recien_creado' : ''));
        art.dataset.id = p.id;

        const cab = el('div', 'pedido_cliente_cab');
        const titulo = el('div');
        titulo.append(
            el('h3', '', 'Pedido #' + p.id),
            el('p', 'pedido_cliente_meta',
                fecha(p.fecha) + ' · ' + p.articulos + (p.articulos === 1 ? ' platillo' : ' platillos'))
        );
        cab.append(titulo, el('strong', 'pedido_cliente_total', dinero(p.total)));

        art.append(cab);
        art.append(cancelado ? el('p', 'pedido_cancelado', 'Este pedido fue cancelado.') : barraPasos(p.estado));
        art.append(el('p', 'pedido_dir', p.direccion || ''));
        return art;
    }

    function render() {
        if (pedidos.length === 0) {
            const v = el('div', 'vacio_menu');
            v.append(document.createTextNode('Todavía no tienes pedidos. '));
            const a = el('a', '', '¡Haz el primero!');
            a.href = 'ordenar_domicilio.html';
            v.append(a);
            lista.replaceChildren(v);
            return;
        }
        lista.replaceChildren(...pedidos.map(tarjeta));
    }

    function cargar() {
        return fetch(API)
            .then(r => {
                if (r.status === 401 || r.status === 403) { irALogin(); throw new Error(); }
                if (!r.ok) throw new Error();
                return r.json();
            })
            .then(datos => { pedidos = datos; render(); })
            .catch(() => {
                lista.replaceChildren(el('div', 'vacio_menu',
                    'No se pudieron cargar tus pedidos. Intenta de nuevo en un momento.'));
            });
    }

    // ----- Tiempo real -----
    function alMensaje(msg) {
        if (msg.tipo === 'pedido_nuevo' && msg.pedido) {
            if (!pedidos.some(p => p.id === msg.pedido.id)) {
                pedidos.unshift(msg.pedido);
                render();
            }
        }

        if (msg.tipo === 'pedido_estado') {
            const p = pedidos.find(x => x.id === msg.id);
            if (!p) { cargar(); return; }
            p.estado = msg.estado;
            render();
            avisar('Tu pedido #' + msg.id + ' ahora está: ' + msg.estado);
        }
    }

    function pintarConexion(conectado) {
        indicador.textContent = conectado ? 'En vivo' : 'Reconectando…';
        indicador.classList.toggle('conectado', conectado);
    }

    // ----- Inicio -----
    const q = new URLSearchParams(location.search);
    if (q.has('creado')) {
        resaltar = Number(q.get('creado'));
        aviso.textContent = '¡Recibimos tu pedido #' + resaltar + '! Aquí verás cómo avanza.';
        aviso.hidden = false;
        // Quita ?creado=… de la barra para que no se repita al recargar
        history.replaceState(null, '', location.pathname);
    }

    cargar();

    Socket_Pedidos.conectar({
        alMensaje: alMensaje,
        alCambiarConexion: pintarConexion,
        respaldo: cargar,
        alSinSesion: irALogin
    });
})();