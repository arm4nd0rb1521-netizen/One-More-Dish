package controlador;

import DAO.InsumoDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import modelo.Insumo;

@WebServlet(name = "Inventario_Servlet", urlPatterns = {"/Inventario_Servlet"})
public class Inventario_Servlet extends HttpServlet {

    private final InsumoDAO dao = new InsumoDAO();

    // Lista el inventario en JSON
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        ArrayList<Insumo> lista = dao.listarInventario();
        if (lista == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        response.setContentType("application/json;charset=UTF-8");
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < lista.size(); i++) {
            Insumo x = lista.get(i);
            if (i > 0) json.append(',');
            json.append("{\"id\":").append(x.getId())
                .append(",\"nombre\":\"").append(esc(x.getNombre()))
                .append("\",\"piezas\":").append(x.getPiezas())
                .append(",\"kilos\":").append(x.getKilos())
                .append(",\"caducidad\":")
                .append(x.getCaducidad() == null ? "null" : "\"" + x.getCaducidad() + "\"")
                .append('}');
        }
        json.append(']');
        response.getWriter().print(json);
    }

    // accion = crear | actualizar | eliminar
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        try {
            String accion = dato(request, "accion");
            boolean ok;

            switch (accion) {
                case "crear":
                    String nombre = dato(request, "nombre");
                    if (nombre.length() > 25) throw new IllegalArgumentException("El nombre admite máximo 25 caracteres");
                    ok = dao.crearInventario(nombre, piezas(request), kilos(request), fecha(request));
                    break;
                case "actualizar":
                    ok = dao.actualizarInventario(Integer.parseInt(dato(request, "id")),
                            piezas(request), kilos(request), fecha(request));
                    break;
                case "eliminar":
                    ok = dao.eliminar(Integer.parseInt(dato(request, "id")));
                    break;
                default:
                    throw new IllegalArgumentException("Acción no válida");
            }

            responder(response, ok, ok ? "" : "No se pudo guardar en la base de datos");

        } catch (Exception e) {
            responder(response, false, e instanceof IllegalArgumentException && !(e instanceof NumberFormatException)
                    ? e.getMessage() : "Datos no válidos");
        }
    }

    // ----- Ayudas -----

    private String dato(HttpServletRequest r, String nombre) {
        String v = r.getParameter(nombre);
        if (v == null || v.trim().isEmpty()) throw new IllegalArgumentException("Falta el dato: " + nombre);
        return v.trim();
    }

    private int piezas(HttpServletRequest r) {
        int v = Integer.parseInt(dato(r, "piezas"));
        if (v < 0 || v > 99999) throw new IllegalArgumentException("Las piezas deben estar entre 0 y 99999");
        return v;
    }

    private double kilos(HttpServletRequest r) {
        double v = Double.parseDouble(dato(r, "kilos").replace(',', '.'));
        if (v < 0 || v > 99999.99) throw new IllegalArgumentException("Los kilos deben estar entre 0 y 99999.99");
        return Math.round(v * 100) / 100.0;
    }

    // La caducidad es opcional (formato yyyy-MM-dd)
    private LocalDate fecha(HttpServletRequest r) {
        String f = r.getParameter("caducidad");
        return (f == null || f.trim().isEmpty()) ? null : LocalDate.parse(f.trim());
    }

    private void responder(HttpServletResponse response, boolean ok, String mensaje) throws IOException {
        response.getWriter().print("{\"ok\":" + ok + ",\"mensaje\":\"" + esc(mensaje) + "\"}");
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ").replace("\t", " ");
    }
}
