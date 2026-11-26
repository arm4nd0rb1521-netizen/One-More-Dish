package controlador;

import DAO.PedidoDAO;
import DAO.VentaDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;
import socket.Pedidos_Socket;
import util.Json_Util;
import util.Sesion_Util;

/**
 * Pedidos a domicilio para COCINA y ADMIN.
 *   GET  (sin parámetros) -> lista en JSON
 *   GET  ?id=5            -> detalle de un pedido en JSON
 *   POST id=5&estado=...  -> cambia el estado y avisa por el socket
 *
 * Estados (clave que manda el JS -> texto que se guarda en la base):
 *   pendiente -> Pendiente
 *   preparacion -> En preparación
 *   listo -> Listo
 *   camino -> En camino
 *   entregado -> Entregado
 *   cancelado -> Cancelado
 *
 * COCINERO solo puede: Pendiente -> En preparación  y  En preparación -> Listo.
 * ADMIN puede poner cualquier estado.
 */
@WebServlet(name = "Pedidos_Domicilio_Servlet", urlPatterns = {"/Pedidos_Domicilio_Servlet"})
public class Pedidos_Domicilio_Servlet extends HttpServlet {

    private static final Map<String, String> ESTADOS = new LinkedHashMap<>();
    static {
        ESTADOS.put("pendiente", "Pendiente");
        ESTADOS.put("preparacion", "En preparación");
        ESTADOS.put("listo", "Listo");
        ESTADOS.put("camino", "En camino");
        ESTADOS.put("entregado", "Entregado");
        ESTADOS.put("cancelado", "Cancelado");
    }

    private final PedidoDAO dao = new PedidoDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        if (!permitido(request, response)) return;

        Object resultado;
        String id = request.getParameter("id");

        try {
            if (id == null) {
                resultado = dao.listarDomicilios();
            } else {
                resultado = dao.detalleDomicilio(Integer.parseInt(id));
                if (resultado == null) {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND);
                    return;
                }
            }
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        if (resultado == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.getWriter().print(Json_Util.aJson(resultado));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        if (!permitido(request, response)) return;

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String clave = request.getParameter("estado");
            String nuevo = clave == null ? null : ESTADOS.get(clave);

            if (nuevo == null) {
                responder(response, false, "Estado no válido", null);
                return;
            }

            Map<String, Object> pedido = dao.detalleDomicilio(id);
            if (pedido == null) {
                responder(response, false, "El pedido no existe o no es a domicilio", null);
                return;
            }

            // La cocina solo avanza un paso; el admin puede todo
            if (Sesion_Util.tieneRol(request, Sesion_Util.COCINERO)) {
                String actual = clave((String) pedido.get("estado"));
                boolean valido = ("pendiente".equals(actual) && "preparacion".equals(clave))
                              || ("preparacion".equals(actual) && "listo".equals(clave));
                if (!valido) {
                    responder(response, false, "La cocina solo puede pasar el pedido a En preparación o Listo", null);
                    return;
                }
            }

            boolean ok = dao.actualizarEstado(id, nuevo);
            if (ok) {
                // Aviso en tiempo real a admin, cocina y al cliente dueño del pedido
                Pedidos_Socket.notificarEstadoPedido(id, nuevo, dao.idClienteDe(id));

                // Entregado = cobrado: se registra la venta (si aún no existe)
                if ("entregado".equals(clave)
                        && new VentaDAO().registrarCobro(id, Sesion_Util.idUsuario(request)) == null) {
                    Ventas_Servlet.avisarVentaNueva();
                }
            }
            responder(response, ok, ok ? "" : "No se pudo actualizar el pedido", ok ? nuevo : null);

        } catch (NumberFormatException e) {
            responder(response, false, "Pedido no válido", null);
        }
    }

    // ----- Ayudas -----

    /** Solo ADMIN y COCINERO. Si no, responde 401 (sin sesión) o 403 (otro rol). */
    private boolean permitido(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (Sesion_Util.rol(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"ok\":false,\"mensaje\":\"Inicia sesión\"}");
            return false;
        }
        if (!Sesion_Util.tieneRol(request, Sesion_Util.ADMIN, Sesion_Util.COCINERO)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"ok\":false,\"mensaje\":\"No tienes permiso\"}");
            return false;
        }
        return true;
    }

    /** "En preparación" -> "preparacion", "Pendiente" -> "pendiente", etc. */
    private String clave(String estado) {
        if (estado == null) return "";
        String s = Normalizer.normalize(estado, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").toLowerCase().trim();
        if (s.startsWith("en ")) s = s.substring(3);
        return s;
    }

    private void responder(HttpServletResponse response, boolean ok, String mensaje, String estado)
            throws IOException {
        response.getWriter().print(Json_Util.aJson(Json_Util.mapa(
                "ok", ok,
                "mensaje", mensaje,
                "estado", estado)));
    }
}