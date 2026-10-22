const form = document.getElementById('form_modificar');
const aviso = document.getElementById('aviso_menu');
const vista = document.getElementById('vista_previa');

const params = new URLSearchParams(location.search);
const idPlatillo = params.get('id');

function mostrarAviso(texto, tipo) {
    aviso.textContent = texto;
    aviso.className = 'aviso_menu ' + tipo;
    aviso.hidden = false;
}

// Si el servlet regresó con error
if (params.has('error')) {
    mostrarAviso('No se pudo guardar. Revisa los datos e intenta de nuevo.', 'error');
}

if (!idPlatillo) {
    mostrarAviso('No se indicó qué platillo modificar.', 'error');
} else {
    fetch('../Menu_Platillos_Servlet')
        .then(r => { if (!r.ok) throw new Error(); return r.json(); })
        .then(datos => {
            const p = datos.find(x => String(x.id) === idPlatillo);
            if (!p) {
                mostrarAviso('El platillo no existe o ya fue eliminado.', 'error');
                return;
            }
            document.getElementById('id_platillo').value = p.id;
            document.getElementById('nombre_platillo').value = p.nombre;
            document.getElementById('desc_platillo').value = p.descripcion;
            document.getElementById('precio_platillo').value = Number(p.precio).toFixed(2);

            vista.src = '../Imagen_Platillo_Servlet?id=' + encodeURIComponent(p.id);
            vista.onerror = () => { vista.hidden = true; };

            form.hidden = false;
        })
        .catch(() => {
            mostrarAviso('No se pudo cargar el platillo. Revisa que el servidor esté encendido.', 'error');
        });
}