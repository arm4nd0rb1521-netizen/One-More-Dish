package controlador;

import DAO.MesaDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import modelo.Mesa;
import socket.Pedidos_Socket;
import util.Json_Util;
import util.Sesion_Util;

/**
 * Mesas del salón (ADMIN y MESERO).
 *   GET                                   -> lista de mesas [{id, estado}]
 *   POST accion=estado&id=3&estado=libre  -> cambia el estado (ADMIN y MESERO)
 *   POST accion=agregar                   -> agrega una mesa Libre (solo ADMIN)
 *   POST accion=quitar                    -> quita la última mesa si está Libre (solo ADMIN)
 *
 * Después de cada cambio avisa por el socket a las salas "admin" y "meseros".
 */
@WebServlet(name = "Mesas_Servlet", urlPatterns = {"/Mesas_Servlet"})
public class Mesas_Servlet extends HttpServlet {

    // clave que manda el JS -> texto que se guarda en la base
    private static final Map<String, String> ESTADOS = new LinkedHashMap<>();
    static {
        ESTADOS.put("libre", "Libre");
        ESTADOS.put("ocupada", "Ocupada");
        ESTADOS.put("sucia", "Sucia");
        ESTADOS.put("reservada", "Reservada");
    }

    private final MesaDAO dao = new MesaDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        if (!permitido(request, response)) return;

        List<Map<String, Object>> lista = new ArrayList<>();
        for (Mesa m : dao.buscarTodo()) {
            lista.add(Json_Util.mapa("id", m.getId(), "estado", m.getEstado()));
        }
        response.getWriter().print(Json_Util.aJson(lista));
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        if (!permitido(request, response)) return;

        String accion = request.getParameter("accion");
        if (accion == null) accion = "";

        switch (accion) {
            case "estado":
                cambiarEstado(request, response);
                break;
            case "agregar":
                if (!soloAdmin(request, response)) return;
                agregar(response);
                break;
            case "quitar":
                if (!soloAdmin(request, response)) return;
                quitar(response);
                break;
            default:
                responder(response, false, "Acción no válida");
        }
    }

    // ----- Acciones -----

    private void cambiarEstado(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String nuevo = ESTADOS.get(request.getParameter("estado"));
        if (nuevo == null) {
            responder(response, false, "Estado no válido");
            return;
        }
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            if (!dao.cambiarEstado(id, nuevo)) {
                responder(response, false, "La mesa no existe");
                return;
            }
            avisar(Json_Util.mapa("tipo", "mesa_estado", "id", id, "estado", nuevo));
            responder(response, true, "");
        } catch (NumberFormatException e) {
            responder(response, false, "Mesa no válida");
        }
    }

    private void agregar(HttpServletResponse response) throws IOException {
        Mesa m = new Mesa();
        m.setEstado("Libre");
        if (!dao.insertar(m)) {
            responder(response, false, "No se pudo agregar la mesa");
            return;
        }
        avisar(Json_Util.mapa("tipo", "mesas_cambiaron"));
        responder(response, true, "");
    }

    private void quitar(HttpServletResponse response) throws IOException {
        List<Mesa> mesas = dao.buscarTodo();
        if (mesas.size() <= 1) {
            responder(response, false, "Debe quedar al menos una mesa");
            return;
        }
        Mesa ultima = mesas.get(mesas.size() - 1);
        if (!"Libre".equalsIgnoreCase(ultima.getEstado())) {
            responder(response, false, "La mesa " + ultima.getId() + " no está libre");
            return;
        }
        // Falla si la mesa tiene pedidos guardados (llave foránea): así no se pierde historial
        if (!dao.eliminar(ultima.getId())) {
            responder(response, false, "La mesa " + ultima.getId() + " tiene pedidos registrados y no se puede quitar");
            return;
        }
        avisar(Json_Util.mapa("tipo", "mesas_cambiaron"));
        responder(response, true, "");
    }

    // ----- Ayudas -----

    private void avisar(Map<String, Object> mensaje) {
        Pedidos_Socket.enviarASalas(Json_Util.aJson(mensaje),
                Pedidos_Socket.SALA_ADMIN, Pedidos_Socket.SALA_MESEROS);
    }

    private boolean permitido(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (Sesion_Util.rol(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            responder(response, false, "Inicia sesión");
            return false;
        }
        if (!Sesion_Util.tieneRol(request, Sesion_Util.ADMIN, Sesion_Util.MESERO)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            responder(response, false, "No tienes permiso");
            return false;
        }
        return true;
    }

    private boolean soloAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!Sesion_Util.tieneRol(request, Sesion_Util.ADMIN)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            responder(response, false, "Solo el administrador puede configurar las mesas");
            return false;
        }
        return true;
    }

    private void responder(HttpServletResponse response, boolean ok, String mensaje) throws IOException {
        response.getWriter().print(Json_Util.aJson(Json_Util.mapa("ok", ok, "mensaje", mensaje)));
    }
}
