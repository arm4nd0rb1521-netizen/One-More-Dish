package controlador;

import DAO.EmpleadoDAO;
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
import modelo.Empleado;

@WebServlet(name = "Buscar_Personal_Nombre", urlPatterns = {"/Buscar_Personal_Nombre"})
public class Buscar_Personal_Nombre_Servlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Buscar_Personal_Nombre</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Buscar_Personal_Nombre at " + request.getContextPath() + "</h1>");
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String nombre_solicitado = request.getParameter("nombre").toLowerCase();
        EmpleadoDAO empleado_dao = new EmpleadoDAO();
        Empleado e = new Empleado();
        
        try(Connection con = base_de_datos.conectar()) {
            e = empleado_dao.buscar_por_nombre(nombre_solicitado);
            System.out.println("USUARIO CONSULTADO CON EXITO");
            response.setContentType("text/html;charset=UTF-8");
            PrintWriter out = response.getWriter();
            String ruta_base = request.getContextPath();
            String rutaBase = request.getContextPath();
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("    <meta charset=\"UTF-8\">");
            out.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            out.println("    <title>Administrar personal | One More Dish</title>");
            out.println("    <link rel=\"stylesheet\" href=\"" + rutaBase + "/ESTILOS/general.css\"/>");
            out.println("    <link rel=\"stylesheet\" href=\"" + rutaBase + "/ESTILOS/inicio.css\"/>");
            out.println("</head>");
            out.println("<body>");
            out.println("    <h1>Administrar personal</h1>");
            out.println("    <section>");
            out.println("        <div>");
            out.println("            <h2>Datos del personal</h2>");
            out.println("            <hr>");
            out.println("            <form action=\"" + rutaBase + "/Modificar_Eliminar_Aniadir_Personal_Servlet\" method=\"post\">");
            out.println("                <label>ID: </label><br>");
            out.println("                <input type=\"text\" name=\"id\" value= \""+ e.getId()+ "\" readonly=\"\"/><br>");
            out.println("                <label>Nombre: </label><br>");
            out.println("                <input type=\"text\" name=\"nombre\" value= \""+ e.getNombre() + "\" /><br>");
            out.println("                <label>Apellido: </label><br>");
            out.println("                <input type=\"text\" name=\"nombre\" value= \""+ e.getApellido()+ "\"/><br>");
            out.println("                <label>Correo: </label><br>");
            out.println("                <input type=\"email\" name=\"nombre\" value= \""+ e.getCorreo()+ "\"/><br>");
            out.println("                <label>Nueva contraseña (dejar en blanco para mantener la actual):</label>\n<br>" +
                                        "<input type=\"password\" name=\"nueva_password\" placeholder=\"Escribe solo si deseas cambiarla\" /><br>");
            out.println("               <label>Puesto: </label>");
            out.println("               <select name=\"puestos\">");
            out.println("                    <option value=\"mesero\">Mesero</option>");
            out.println("                    <option value=\"cocinero\">Cocinero</option>");
            out.println("                    <option value=\"supervisor\">Supervisor</option>");
            out.println("               </select><br>");
            out.println("           </div>");
            out.println("           <button type=\"submit\" name=\"eliminar\">Eliminar</button>");
            out.println("           <button type=\"submit\" name=\"actualizar\">Actualizar</button>");
            out.println("        </form>");
            out.println("    </section>");
            out.println("    <script src=\"" + rutaBase + "/JS/header_admin.js\"></script>");
            out.println("</body>");
            out.println("</html>");

           
        } catch(Exception exeption_sql) {
            exeption_sql.printStackTrace();
            System.out.println("ERROR EN LA CONSULTA");
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
