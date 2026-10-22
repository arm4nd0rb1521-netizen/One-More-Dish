package controlador;

import DAO.PlatilloDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import modelo.Platillo;

@WebServlet(name = "Modificar_Platillo_Servlet", urlPatterns = {"/Modificar_Platillo_Servlet"})
public class Modificar_Platillo_Servlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        request.setCharacterEncoding("UTF-8"); // para acentos y ñ

        // Solo valores permitidos: nunca se redirige a una URL escrita por el usuario
        boolean esAdmin = "admin".equals(request.getParameter("origen"));
        String menu = esAdmin ? "/HTML/menu_admin.html" : "/HTML/menu_cocina.html";
        String modificar = esAdmin ? "/HTML/modificar_platillo_admin.html"
                                   : "/HTML/modificar_platillo_cocina.html";

        String idTexto = request.getParameter("id_platillo");
        String destino = modificar + "?id=" + (idTexto == null ? "" : idTexto.replaceAll("\\D", "")) + "&error=1";

        try {
            int id = Integer.parseInt(idTexto);
            String nombre = request.getParameter("nombre_platillo");
            String descripcion = request.getParameter("desc_platillo");
            double precio = Double.parseDouble(request.getParameter("precio_platillo"));

            boolean datosValidos = nombre != null && !nombre.trim().isEmpty() && nombre.length() <= 25
                    && descripcion != null && !descripcion.trim().isEmpty() && descripcion.length() <= 100
                    && precio >= 0 && precio <= 999.99;

            if (datosValidos) {
                Platillo p = new Platillo();
                p.setId(id);
                p.setNombre(nombre.trim());
                p.setDescripcion(descripcion.trim());
                p.setPrecio(precio);

                if (new PlatilloDAO().actualizar(p)) {
                    destino = menu + "?modificado=1";
                }
            }
        } catch (NumberFormatException | NullPointerException e) {
            // se queda con el destino de error
        }

        response.sendRedirect(request.getContextPath() + destino);
    }
}