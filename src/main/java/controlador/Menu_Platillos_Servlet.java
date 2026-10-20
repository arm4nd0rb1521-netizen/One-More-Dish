package controlador;

import DAO.PlatilloDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import modelo.Platillo;

@WebServlet(name = "Menu_Platillos_Servlet", urlPatterns = {"/Menu_Platillos_Servlet"})
public class Menu_Platillos_Servlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("application/json;charset=UTF-8");

        ArrayList<Platillo> lista = new PlatilloDAO().buscarTodo();
        StringBuilder json = new StringBuilder("[");

        for (int i = 0; i < lista.size(); i++) {
            Platillo p = lista.get(i);
            if (i > 0) json.append(',');
            json.append("{\"id\":").append(p.getId())
                .append(",\"nombre\":\"").append(esc(p.getNombre()))
                .append("\",\"descripcion\":\"").append(esc(p.getDescripcion()))
                .append("\",\"precio\":").append(p.getPrecio() == null ? 0 : p.getPrecio())
                .append('}');
        }
        json.append(']');

        response.getWriter().print(json);
    }

    // Escapa texto para que comillas o saltos de línea no rompan el JSON
    private String esc(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", " ").replace("\r", " ").replace("\t", " ");
    }
}