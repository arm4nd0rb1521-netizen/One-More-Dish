package controlador;

import DAO.PlatilloDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "Eliminar_Platillo_Servlet", urlPatterns = {"/Eliminar_Platillo_Servlet"})
public class Eliminar_Platillo_Servlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String destino = "/HTML/menu_cocina.html?error=1";
        try {
            int id = Integer.parseInt(request.getParameter("id_platillo"));
            if (new PlatilloDAO().eliminar(id)) destino = "/HTML/menu_cocina.html?eliminado=1";
        } catch (NumberFormatException e) {
            // se queda con el destino de error
        }
        response.sendRedirect(request.getContextPath() + destino);
    }
}