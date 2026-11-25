/*
 * socket_pedidos.js — cliente WebSocket de One More Dish.
 *
 * Es el "Solicitante" de tu ejemplo de clase, pero en el navegador:
 *   new WebSocket(url)  ≈  new Socket(host, puerto)
 *
 * Uso en cualquier página:
 *   <script src="../JS/socket_pedidos.js"></script>
 *   Socket_Pedidos.conectar({
 *       alMensaje: msg => { ... },            // llega un aviso del servidor (JSON ya convertido)
 *       alCambiarConexion: conectado => {},   // true / false para mostrar "En vivo"
 *       respaldo: () => cargar(true),         // recarga normal por fetch (plan B)
 *       alSinSesion: () => {}                 // el servidor rechazó: no hay login
 *   });
 *
 * Qué hace solo:
 *   - Calcula la URL correcta en local (/one_more_dish/) y en Azure (raíz), ws:// o wss://.
 *   - Manda un "ping" cada 25 s para que Azure no corte la conexión por inactividad.
 *   - Si se cae, reintenta: 1 s, 2 s, 4 s, 8 s... hasta 30 s entre intentos.
 *   - Mientras está caído, ejecuta "respaldo" cada 30 s (como el setInterval de antes).
 *   - Al reconectar, ejecuta "respaldo" una vez para recuperar lo que pasó mientras tanto.
 */
(function () {
    // Ruta base: sale de donde se cargó este script (igual que header_admin.js)
    const base = document.currentScript.src.replace(/JS\/socket_pedidos\.js.*$/, '');
    // http -> ws  y  https -> wss
    const URL_WS = base.replace(/^http/, 'ws') + 'ws/pedidos';

    const CODIGO_SIN_SESION = 1008;   // lo manda Pedidos_Socket cuando no hay login
    const CADA_PING = 25000;
    const CADA_RESPALDO = 30000;
    const ESPERA_MAXIMA = 30000;

    function conectar(op) {
        op = op || {};

        let ws = null;
        let intentos = 0;
        let yaConectoAntes = false;
        let sinSesion = false;
        let cerradoAMano = false;
        let timerReconexion = null;
        let timerPing = null;
        let timerRespaldo = null;
        let conectado = false;

        function avisarConexion(valor) {
            if (conectado === valor) return;
            conectado = valor;
            if (op.alCambiarConexion) op.alCambiarConexion(valor);
        }

        // ----- Plan B: recargar por fetch mientras no hay socket -----
        function iniciarRespaldo() {
            if (timerRespaldo || !op.respaldo) return;
            timerRespaldo = setInterval(() => {
                if (!document.hidden) op.respaldo();
            }, op.cadaRespaldo || CADA_RESPALDO);
        }

        function detenerRespaldo() {
            clearInterval(timerRespaldo);
            timerRespaldo = null;
        }

        // ----- Conexión -----
        function abrir() {
            clearTimeout(timerReconexion);
            if (sinSesion || cerradoAMano) return;
            if (ws && (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING)) return;

            try {
                ws = new WebSocket(URL_WS);
            } catch (e) {
                programarReintento();
                return;
            }

            ws.onopen = () => {
                intentos = 0;
                avisarConexion(true);
                detenerRespaldo();
                // Si es una RE-conexión, pedimos la lista completa por si nos perdimos algo
                if (yaConectoAntes && op.respaldo) op.respaldo();
                yaConectoAntes = true;

                clearInterval(timerPing);
                timerPing = setInterval(() => {
                    if (ws.readyState === WebSocket.OPEN) ws.send('{"tipo":"ping"}');
                }, CADA_PING);
            };

            ws.onmessage = (evento) => {
                let msg;
                try { msg = JSON.parse(evento.data); } catch (e) { return; }
                if (msg.tipo === 'pong') return;
                if (op.alMensaje) op.alMensaje(msg);
            };

            ws.onclose = (evento) => {
                clearInterval(timerPing);
                avisarConexion(false);

                if (evento.code === CODIGO_SIN_SESION) {
                    sinSesion = true;
                    if (op.alSinSesion) op.alSinSesion(evento.reason);
                    return;
                }
                if (cerradoAMano) return;

                iniciarRespaldo();
                programarReintento();
            };

            // Si hay error, el navegador llama después a onclose; ahí se reintenta
            ws.onerror = () => {};
        }

        function programarReintento() {
            clearTimeout(timerReconexion);
            const espera = Math.min(ESPERA_MAXIMA, 1000 * Math.pow(2, intentos));
            intentos++;
            timerReconexion = setTimeout(abrir, espera);
        }

        // Al volver a la pestaña o recuperar internet, reintenta de inmediato
        function reintentarYa() {
            if (document.hidden || sinSesion || cerradoAMano) return;
            intentos = 0;
            abrir();
        }
        document.addEventListener('visibilitychange', reintentarYa);
        window.addEventListener('online', reintentarYa);

        abrir();

        return {
            estaConectado: () => conectado,
            cerrar: () => {
                cerradoAMano = true;
                clearTimeout(timerReconexion);
                clearInterval(timerPing);
                detenerRespaldo();
                if (ws) ws.close(1000, 'Adios');
            }
        };
    }

    window.Socket_Pedidos = { conectar: conectar, URL: URL_WS };
})();