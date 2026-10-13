
package controlador;

import conexion.base_de_datos;
import util.Codigo_Service;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

@WebServlet(name = "Verificar_Correo_Servlet",
            urlPatterns = {"/Verificar_Correo_Servlet"})
public class Verificar_Correo_Servlet extends HttpServlet {

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        HttpSession sesion = request.getSession(false);

        if (sesion == null
                || sesion.getAttribute("correoPendiente") == null) {
            response.sendError(
                HttpServletResponse.SC_UNAUTHORIZED,
                "No hay un registro pendiente de verificación."
            );
            return;
        }

        String correo = (String)
                sesion.getAttribute("correoPendiente");

        String codigo = request.getParameter("codigo");

        if (codigo == null || !codigo.matches("[0-9]{6}")) {
            response.sendError(
                HttpServletResponse.SC_BAD_REQUEST,
                "El código debe tener seis dígitos."
            );
            return;
        }

        try {
            String hash = Codigo_Service.generarHash(codigo);

            String sql = "UPDATE cliente SET "
        + "correo_verificado = TRUE, "
        + "codigo_verificacion_hash = NULL, "
        + "codigo_expira = NULL "
        + "WHERE correo = ?";

            try (Connection conexion =
                    base_de_datos.conectar();
                 PreparedStatement ps =
                    conexion.prepareStatement(sql)) {

                ps.setString(1, correo);
                int filasActualizadas = ps.executeUpdate();

                if (filasActualizadas == 1) {
                    sesion.removeAttribute("correoPendiente");

                    response.sendRedirect(
                        request.getContextPath()
                        + "/index.html?verificacion=exitosa"
                    );
                } else {
                    response.setStatus(
                        HttpServletResponse.SC_BAD_REQUEST
                    );

                    response.getWriter().println(
                        "<h2>Código incorrecto o vencido.</h2>"
                        + "<a href='verificar-correo.jsp'>"
                        + "Intentar nuevamente</a>"
                    );
                }
            }

        } catch (Exception e) {
            throw new ServletException(
                "Error al verificar el correo.", e
            );
        }
    }
}
