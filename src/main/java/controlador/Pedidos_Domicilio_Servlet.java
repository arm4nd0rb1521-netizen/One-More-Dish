package controlador;

import DAO.PedidoDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;    
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "Pedidos_Domicilio_Servlet", urlPatterns = {"/Pedidos_Domicilio_Servlet"})
public class Pedidos_Domicilio_Servlet extends HttpServlet {

    private final PedidoDAO dao = new PedidoDAO();

    // Sin parámetros: lista. Con ?id=: detalle de un pedido
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
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

        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(json(resultado));
    }

    // Cambia el estado: estado = preparacion | listo
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String clave = request.getParameter("estado");

            String estado = "preparacion".equals(clave) ? "En preparación"
                          : "listo".equals(clave) ? "Listo" : null;

            if (estado == null) {
                responder(response, false, "Estado no válido");
                return;
            }
            if (dao.detalleDomicilio(id) == null) {
                responder(response, false, "El pedido no existe o no es a domicilio");
                return;
            }

            boolean ok = dao.actualizarEstado(id, estado);
            responder(response, ok, ok ? "" : "No se pudo actualizar el pedido");

        } catch (NumberFormatException e) {
            responder(response, false, "Pedido no válido");
        }
    }

    // ----- Ayudas -----

    private void responder(HttpServletResponse response, boolean ok, String mensaje) throws IOException {
        response.getWriter().print("{\"ok\":" + ok + ",\"mensaje\":\"" + esc(mensaje) + "\"}");
    }

    // Convierte Map / List / números / texto a JSON
    private String json(Object o) {
        if (o == null) return "null";
        if (o instanceof Number || o instanceof Boolean) return o.toString();

        if (o instanceof Map) {
            StringBuilder sb = new StringBuilder("{");
            boolean primero = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) o).entrySet()) {
                if (!primero) sb.append(',');
                primero = false;
                sb.append('"').append(esc(String.valueOf(e.getKey()))).append("\":").append(json(e.getValue()));
            }
            return sb.append('}').toString();
        }

        if (o instanceof List) {
            StringBuilder sb = new StringBuilder("[");
            boolean primero = true;
            for (Object x : (List<?>) o) {
                if (!primero) sb.append(',');
                primero = false;
                sb.append(json(x));
            }
            return sb.append(']').toString();
        }

        return "\"" + esc(o.toString()) + "\"";
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ").replace("\t", " ");
    }
}
