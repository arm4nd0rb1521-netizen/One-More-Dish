package controlador;

import DAO.PedidoDAO;
import DAO.PlatilloDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import modelo.Pedido;
import modelo.Platillo;
import socket.Pedidos_Socket;
import util.Json_Util;
import util.Sesion_Util;

/**
 * Pedidos a domicilio del CLIENTE que inició sesión.
 *   GET                 -> sus pedidos (JSON)
 *   GET  ?id=5          -> detalle de UNO de sus pedidos (si no es suyo: 404)
 *   POST                -> crea un pedido nuevo y avisa por el socket
 *        direccion=Calle 1...
 *        platillo=3&cantidad=2&platillo=7&cantidad=1   (pares en el mismo orden)
 *
 * El id del cliente SIEMPRE sale de la sesión, nunca del formulario:
 * así nadie puede crear o ver pedidos a nombre de otro.
 */
@WebServlet(name = "Pedidos_Cliente_Servlet", urlPatterns = {"/Pedidos_Cliente_Servlet"})
public class Pedidos_Cliente_Servlet extends HttpServlet {

    private static final int MIN_DIRECCION = 10;
    private static final int MAX_DIRECCION = 200;   // igual que la columna pedido.direccion
    private static final int MAX_POR_PLATILLO = 20;
    private static final int MAX_TOTAL_PLATILLOS = 50;

    private final PedidoDAO pedidoDao = new PedidoDAO();
    private final PlatilloDAO platilloDao = new PlatilloDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer idCliente = clienteEnSesion(request, response);
        if (idCliente == null) return;

        String id = request.getParameter("id");
        if (id == null) {
            Object lista = pedidoDao.listarDomiciliosDeCliente(idCliente);
            if (lista == null) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                return;
            }
            response.getWriter().print(Json_Util.aJson(lista));
            return;
        }

        try {
            int idPedido = Integer.parseInt(id);
            // Solo si el pedido es de ESTE cliente
            Map<String, Object> detalle = pedidoDao.idClienteDe(idPedido) == idCliente
                    ? pedidoDao.detalleDomicilio(idPedido) : null;
            if (detalle == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            response.getWriter().print(Json_Util.aJson(detalle));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        Integer idCliente = clienteEnSesion(request, response);
        if (idCliente == null) return;

        // ----- Dirección -----
        String direccion = request.getParameter("direccion");
        direccion = direccion == null ? "" : direccion.trim().replaceAll("\\s+", " ");
        if (direccion.length() < MIN_DIRECCION) {
            responder(response, false, "Escribe tu dirección completa (calle, número y colonia).", 0);
            return;
        }
        if (direccion.length() > MAX_DIRECCION) {
            responder(response, false, "La dirección es muy larga (máximo " + MAX_DIRECCION + " caracteres).", 0);
            return;
        }

        // ----- Platillos -----
        String[] ids = request.getParameterValues("platillo");
        String[] cantidades = request.getParameterValues("cantidad");
        if (ids == null || cantidades == null || ids.length == 0 || ids.length != cantidades.length) {
            responder(response, false, "Tu pedido no tiene platillos.", 0);
            return;
        }

        Pedido pedido = new Pedido();
        pedido.setEstado("Pendiente");
        pedido.setDomicilio(true);
        pedido.setDireccion(direccion);
        pedido.setId_cliente(idCliente);
        pedido.setId_mesa(0);              // 0 -> se guarda NULL (no es pedido de mesa)

        int totalPlatillos = 0;
        try {
            for (int i = 0; i < ids.length; i++) {
                int idPlatillo = Integer.parseInt(ids[i]);
                int cantidad = Integer.parseInt(cantidades[i]);

                if (cantidad < 1 || cantidad > MAX_POR_PLATILLO) {
                    responder(response, false, "Cada platillo puede pedirse de 1 a " + MAX_POR_PLATILLO + " veces.", 0);
                    return;
                }
                totalPlatillos += cantidad;
                if (totalPlatillos > MAX_TOTAL_PLATILLOS) {
                    responder(response, false, "Máximo " + MAX_TOTAL_PLATILLOS + " platillos por pedido.", 0);
                    return;
                }

                // El precio y el nombre salen de la BASE, no del navegador
                Platillo platillo = platilloDao.buscarUno(idPlatillo);
                if (platillo == null) {
                    responder(response, false, "Uno de los platillos ya no está disponible. Recarga el menú.", 0);
                    return;
                }
                // pedido_platillo no tiene columna "cantidad": se guarda una fila por unidad
                for (int c = 0; c < cantidad; c++) {
                    pedido.agregar_platillo(platillo);
                }
            }
        } catch (NumberFormatException e) {
            responder(response, false, "Datos del pedido no válidos.", 0);
            return;
        }

        // ----- Guardar (transacción en PedidoDAO.insertar) -----
        if (!pedidoDao.insertar(pedido)) {
            responder(response, false, "No se pudo guardar tu pedido. Intenta de nuevo.", 0);
            return;
        }

        // ----- Aviso en tiempo real a cocina, admin y al propio cliente -----
        Map<String, Object> resumen = pedidoDao.resumenDomicilio(pedido.getId());
        if (resumen != null) {
            Pedidos_Socket.notificarPedidoNuevo(resumen, idCliente);
        }

        responder(response, true, "", pedido.getId());
    }

    // ----- Ayudas -----

    /** Id del cliente en sesión; si no hay o no es CLIENTE responde 401/403 y regresa null. */
    private Integer clienteEnSesion(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (Sesion_Util.rol(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"ok\":false,\"mensaje\":\"Inicia sesión para hacer tu pedido\"}");
            return null;
        }
        if (!Sesion_Util.tieneRol(request, Sesion_Util.CLIENTE)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"ok\":false,\"mensaje\":\"Solo los clientes pueden hacer pedidos a domicilio\"}");
            return null;
        }
        return Sesion_Util.idUsuario(request);
    }

    private void responder(HttpServletResponse response, boolean ok, String mensaje, int id) throws IOException {
        response.getWriter().print(Json_Util.aJson(Json_Util.mapa(
                "ok", ok,
                "mensaje", mensaje,
                "id", id)));
    }
}
