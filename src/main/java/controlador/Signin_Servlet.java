package controlador;

import conexion.base_de_datos;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.Password_Service;
import util.Sesion_Util;

@WebServlet(urlPatterns = {"/servlet_signin"})
public class Signin_Servlet extends HttpServlet {

    // Si alguien abre la URL del servlet directo en el navegador, lo mandamos al formulario
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.sendRedirect(request.getContextPath() + "/HTML/signin.html");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String correo = request.getParameter("correo_login");
        String contra = request.getParameter("contrasena_login");

        // Primero se revisa null y DESPUÉS se usa toLowerCase (antes daba NullPointerException)
        if (correo == null || contra == null || correo.trim().isEmpty() || contra.isEmpty()) {
            alerta(response, "Rellene todos los campos");
            return;
        }
        correo = correo.trim().toLowerCase();

        try (Connection conexion = base_de_datos.conectar()) {

            // ----- 1) ¿Es cliente? -----
            String sqlCliente = "SELECT id_cliente, nombre, contraseña FROM cliente WHERE correo = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlCliente)) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        if (Password_Service.verificar(contra, rs.getString("contraseña"))) {
                            Sesion_Util.iniciar(request, rs.getInt("id_cliente"),
                                    Sesion_Util.CLIENTE, rs.getString("nombre"));
                            response.sendRedirect(request.getContextPath()
                                    + "/HTML/inicio_cliente.html?registro=exitoso");
                        } else {
                            alerta(response, "Correo y/o contraseña incorrectos");
                        }
                        return;
                    }
                }
            }

            // ----- 2) ¿Es empleado? (el nombre del rol sale de la tabla rol) -----
            String sqlEmpleado = "SELECT e.id_empleado, e.nombre, e.contraseña, r.nombre AS rol "
                               + "FROM empleado e INNER JOIN rol r ON r.id_rol = e.id_rol "
                               + "WHERE e.correo = ?";
            try (PreparedStatement ps = conexion.prepareStatement(sqlEmpleado)) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && Password_Service.verificar(contra, rs.getString("contraseña"))) {
                        String rol = rs.getString("rol") == null ? "" : rs.getString("rol").toUpperCase();
                        String pagina = paginaInicio(rol);

                        if (pagina == null) {
                            alerta(response, "Tu usuario no tiene un rol válido. Contacta al administrador.");
                            return;
                        }

                        Sesion_Util.iniciar(request, rs.getInt("id_empleado"), rol, rs.getString("nombre"));
                        response.sendRedirect(request.getContextPath() + "/HTML/" + pagina + "?registro=exitoso");
                        return;
                    }
                }
            }

            alerta(response, "Correo y/o contraseña incorrectos");

        } catch (SQLException e) {
            e.printStackTrace();
            alerta(response, "No se pudo conectar con la base de datos. Intenta más tarde.");
        }
    }

    /** Página de inicio de cada rol de empleado (null si el rol no existe). */
    private String paginaInicio(String rol) {
        switch (rol) {
            case Sesion_Util.ADMIN:     return "inicio_admin.html";
            case Sesion_Util.MESERO:    return "inicio_mesero.html";
            case Sesion_Util.COCINERO:  return "inicio_cocina.html";
            case Sesion_Util.PROVEEDOR: return "inicio_proveedor.html";
            default:                    return null;
        }
    }

    /** Muestra un alert y regresa al formulario (igual que antes). */
    private void alerta(HttpServletResponse response, String mensaje) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().println(
            "<!DOCTYPE html>"
            + "<html lang='es'>"
            + "<head><meta charset='UTF-8'></head>"
            + "<body>"
            + "<script>"
            + "alert('" + mensaje.replace("\\", "\\\\").replace("'", "\\'") + "');"
            + "window.history.back();"
            + "</script>"
            + "</body>"
            + "</html>"
        );
    }

    @Override
    public String getServletInfo() {
        return "Inicio de sesión de clientes y empleados";
    }
}
