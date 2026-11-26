package controlador;

import DAO.EmpleadoDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Empleado;
import util.Password_Service;

@WebServlet(name = "Modificar_Eliminar_Aniadir_Personal_Servlet", urlPatterns = {"/Modificar_Eliminar_Aniadir_Personal_Servlet"})
public class Modificar_Eliminar_Aniadir_Personal_Servlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Modificar_Eliminar_Aniadir_Personal_Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Modificar_Eliminar_Aniadir_Personal_Servlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EmpleadoDAO edao = new EmpleadoDAO();
        Empleado e = new Empleado();
        
        String modificar = request.getParameter("actualizar");
        String eliminar = request.getParameter("eliminar");
        
        if(modificar != null) {
            int id = Integer.parseInt(request.getParameter("id"));
            String nombre = request.getParameter("nombre");
            String apellido = request.getParameter("apellido");
            String correo = request.getParameter("correo");
            String contraseña = request.getParameter("nueva_password");
            e.setId(id);
            e.setNombre(nombre);
            e.setApellido(apellido);
            e.setCorreo(correo);
            if(contraseña == null && contraseña.trim().isEmpty()) {
                edao.actualizar_sin_contraseña(e);
            } else {
                String contraseña_hash = util.Password_Service.generarHash(contraseña);
                e.setContraseña(contraseña_hash);
                edao.actualizar(e);
            }
            edao.actualizar(e);
        } else if(eliminar != null) {
            int id = Integer.parseInt(request.getParameter("id"));
            edao.eliminar(id);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
