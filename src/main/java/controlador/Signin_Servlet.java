package controlador;

import conexion.base_de_datos;
import java.io.IOException;
import java.io.PrintWriter;
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

@WebServlet(urlPatterns = {"/servlet_signin"})
public class Signin_Servlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet servlet_signin</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet servlet_signin at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

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
        String correo = request.getParameter("correo_login");
        String contra = request.getParameter("contrasena_login");
        String casilla = request.getParameter("recordarme");
        
        if (correo == null || contra == null) {
            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println(
            "<!DOCTYPE html>" +
            "<html lang='es'>" +
            "<head><meta charset='UTF-8'></head>" +
            "<body>" +
            "<script>" +
            "alert('Rellene todos los campos');" +
            "window.history.back();" +
            "</script>" +
            "</body>" +
            "</html>"
            );
            return;
        }
        
        try {
            String sql_cliente = "SELECT * FROM cliente WHERE correo = ?";
            
            try(Connection conexion = base_de_datos.conectar()) {
                PreparedStatement ps = conexion.prepareStatement(sql_cliente);
                ResultSet rs;
                
                ps.setString(1, correo);
                
                rs = ps.executeQuery();
                
                if (rs.next()) {
                    String contraseña_real = rs.getString("contraseña");
                   
                    if (Password_Service.verificar(contra, contraseña_real)) {
                        response.sendRedirect(
                            request.getContextPath() + "/HTML/inicio_cliente.html?registro=exitoso"
                        );
                        return;
                    }
                } else {
                    String sql_empleado = "SELECT * FROM empleado WHERE correo = ?";
                    PreparedStatement ps_empleado = conexion.prepareStatement(sql_empleado);
                    
                    ps_empleado.setString(1, correo);
                    
                    rs = ps_empleado.executeQuery();
                    
                    if(rs.next()) {
                        String contraseña_real = rs.getString("contraseña");
                        int rol_empleado = rs.getInt("id_rol");
                        
                        switch (rol_empleado) {
                            case 1:
                                if (Password_Service.verificar(contra, contraseña_real)) {
                                    response.sendRedirect(request.getContextPath() + "/HTML/inicio_admin.html?registro=exitoso");
                                    break;
                                }
                                
                            case 2:
                                if (Password_Service.verificar(contra, contraseña_real)) {
                                    response.sendRedirect(request.getContextPath() + "/HTML/inicio_mesero.html?registro=exitoso");
                                    break;
                                }
                                
                            case 3:
                                if (Password_Service.verificar(contra, contraseña_real)) {
                                    response.sendRedirect(request.getContextPath() + "/HTML/inicio_cocina.html?registro=exitoso");
                                    break;
                                }
                                
                            case 5:
                                if (Password_Service.verificar(contra, contraseña_real)) {
                                    response.sendRedirect(request.getContextPath() + "/HTML/inicio_proveedor.html?registro=exitoso");
                                    break;
                                }
                                
                            default:
                                throw new AssertionError();
                        }
                    }
                }
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().println(
                "<!DOCTYPE html>" +
                "<html lang='es'>" +
                "<head><meta charset='UTF-8'></head>" +
                "<body>" +
                "<script>" +
                "alert('Correo y/o contraseña incorrectos');" +
                "window.history.back();" +
                "</script>" +
                "</body>" +
                "</html>"
                );
                
            } catch(SQLException e) {
                e.printStackTrace();
            }
            
        } catch(Exception e) {
            e.printStackTrace();
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

}
