package controlador;

import DAO.VentaDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import socket.Pedidos_Socket;
import util.Json_Util;
import util.Sesion_Util;

/**
 * Ventas (solo ADMIN).
 *   GET                       -> resumen de hoy y los 3 días anteriores
 *   GET  ?por_cobrar=1        -> pedidos que todavía no se cobran
 *   POST id_pedido=12         -> cobra ese pedido (crea la venta)
 */
@WebServlet(name = "Ventas_Servlet", urlPatterns = {"/Ventas_Servlet"})
public class Ventas_Servlet extends HttpServlet {

    private static final int DIAS_RESUMEN = 4;

    private final VentaDAO dao = new VentaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        if (!soloAdmin(request, response)) return;

        Object resultado = request.getParameter("por_cobrar") != null
                ? dao.pedidosPorCobrar()
                : dao.resumenPorDia(DIAS_RESUMEN);

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
        if (!soloAdmin(request, response)) return;

        try {
            int idPedido = Integer.parseInt(request.getParameter("id_pedido"));
            // El empleado que cobra es el que tiene la sesión abierta
            String error = dao.registrarCobro(idPedido, Sesion_Util.idUsuario(request));

            if (error == null) {
                avisarVentaNueva();
            }
            response.getWriter().print(Json_Util.aJson(Json_Util.mapa(
                    "ok", error == null,
                    "mensaje", error == null ? "" : error)));

        } catch (NumberFormatException e) {
            response.getWriter().print("{\"ok\":false,\"mensaje\":\"Elige un pedido\"}");
        }
    }

    /** Lo usan este servlet y Pedidos_Domicilio_Servlet para refrescar ventas_admin en vivo. */
    public static void avisarVentaNueva() {
        Pedidos_Socket.enviarASalas("{\"tipo\":\"venta_nueva\"}", Pedidos_Socket.SALA_ADMIN);
    }

    private boolean soloAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (Sesion_Util.rol(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("{\"ok\":false,\"mensaje\":\"Inicia sesión\"}");
            return false;
        }
        if (!Sesion_Util.tieneRol(request, Sesion_Util.ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().print("{\"ok\":false,\"mensaje\":\"No tienes permiso\"}");
            return false;
        }
        return true;
    }
}
