package controlador;

import conexion.base_de_datos;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.ThreadLocalRandom;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author arm4n
 */
@WebServlet(name = "Signup_Servlet", urlPatterns = {"/Signup_Servlet"})
public class Signup_Servlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Signup_Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Signup_Servlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre = request.getParameter("nombre_registro");
        String apellido = request.getParameter("apellido_registro");
        String contraseña = request.getParameter("contraseña_registro");
        String correo = request.getParameter("correo_registro");
        String confirm_contra = request.getParameter("confirmar_contraseña");
        String telefonoTexto = request.getParameter("telefono_registro");
        String casilla = request.getParameter("terminos_condiciones");

        if (contraseña == null || !contraseña.equals(confirm_contra)) {

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println(
            "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body>" +
            "<script>" +
            "alert('Las contraseñas no coinciden');" +
            "window.history.back();" +
            "</script>" +
            "</body>" +
            "</html>"
            );
            return;
        }
        
        if (casilla == null) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println(
            "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body>" +
            "<script>" +
            "alert('Acepte terminos y condiciones');" +
            "window.history.back();" +
            "</script>" +
            "</body>" +
            "</html>"
            );
            return;
        }

        
        try {
            correo = correo == null ? "" : correo.trim();

            if (correo.isBlank()
                    || !correo.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Ingresa un correo válido."
                );
                return;
            }

            int id = generar_id_unico();
            String contraseñaHash = util.Password_Service.generarHash(contraseña);
            
            String sql ="INSERT INTO cliente" +
                "(id_cliente, nombre, apellido, correo, contraseña," +
                 "telefono, id_rol)" +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection conexion = base_de_datos.conectar()) {

                // Comprobar si el correo ya está registrado
                String consulta = 
                    "SELECT * FROM cliente WHERE correo = ?";

                try (PreparedStatement ps =
                        conexion.prepareStatement(consulta)) {

                    ps.setString(1, correo);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            response.sendError(
                                HttpServletResponse.SC_CONFLICT,
                                "Este correo ya está registrado."
                            );
                            return;
                        }
                    }
                }

                try (PreparedStatement ps =
                        conexion.prepareStatement(sql)) {

                    ps.setInt(1, id);
                    ps.setString(2, nombre);
                    ps.setString(3, apellido);
                    ps.setString(4, correo);
                    ps.setString(5, contraseñaHash);
                    ps.setString(6, telefonoTexto);
                    ps.setInt(7, 4);

                    ps.executeUpdate();
                    
                    response.sendRedirect(request.getContextPath() + "/index.html");
                }
            }

        } catch (java.sql.SQLException e) {
            e.printStackTrace();

            response.sendError(
                HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "No se pudo registrar el cliente."
            );

        } catch (Exception e) {

            // Mostrar el error en la consola de Java
            e.printStackTrace(System.err);

            // Enviar también el error al logger del servidor
            Logger.getLogger(Signup_Servlet.class.getName())
                  .log(Level.SEVERE, "ERROR EN SIGNUP", e);

            if (!response.isCommitted()) {
                response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Error en el registro. Revisa el log de Tomcat."
                );
            }
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    public int generar_id_unico() {
        int id;

        do {
            id = ThreadLocalRandom.current().nextInt(100000, 1000000);
        } while (existe_id(id));
    
        return id;
    }
    
    public boolean existe_id(int id) {
        String sql = "SELECT 1 FROM cliente WHERE id_cliente = ?";

        try (Connection conexion = base_de_datos.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
