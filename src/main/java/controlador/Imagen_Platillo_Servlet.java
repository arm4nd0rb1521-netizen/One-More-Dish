package controlador;

import DAO.PlatilloDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Imagen_Platillo_Servlet", urlPatterns = {"/Imagen_Platillo_Servlet"})
public class Imagen_Platillo_Servlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        byte[] imagen = null;
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            imagen = new PlatilloDAO().obtenerImagen(id);
        } catch (NumberFormatException e) {
            // id inválido: se responde 404 abajo
        }

        if (imagen == null || imagen.length == 0) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(tipo(imagen));
        response.setContentLength(imagen.length);
        response.setHeader("Cache-Control", "public, max-age=3600");
        response.getOutputStream().write(imagen);
    }

    // Detecta el formato por los primeros bytes
    private String tipo(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0x89 && b[1] == 'P') return "image/png";
        if (b.length > 11 && b[0] == 'R' && b[1] == 'I' && b[8] == 'W') return "image/webp";
        return "image/jpeg";
    }
}