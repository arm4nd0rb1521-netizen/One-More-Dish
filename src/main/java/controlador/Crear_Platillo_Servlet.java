package controlador;

import DAO.PlatilloDAO;
import conexion.base_de_datos;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import modelo.Platillo;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

@WebServlet(name = "Crear_Platillo_Servlet", urlPatterns = {"/Crear_Platillo_Servlet"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024, maxFileSize = 5 * 1024 * 1024, maxRequestSize = 6 * 1024 * 1024)
public class Crear_Platillo_Servlet extends HttpServlet {

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Crear_Platillo_Servlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Crear_Platillo_Servlet at " + request.getContextPath() + "</h1>");
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
        Platillo platillo = new Platillo();
        PlatilloDAO platillo_dao = new PlatilloDAO();

        int id = generar_id_unico();
        String nombre = request.getParameter("nombre_platillo");
        String descripcion = request.getParameter("desc_platillo");
        String precio_texto = request.getParameter("precio_platillo");
        Part parteImagen = request.getPart("imagen");
        Double precio;
        
        byte[] imagen = null;

        try {
            precio = Double.parseDouble(precio_texto);
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath()
                     + "?error=precio");
            return;
        }
        
        if (parteImagen != null && parteImagen.getSize() > 0) {
            String tipo = parteImagen.getContentType();

            if (tipo == null || !tipo.matches("image/(jpeg|png|webp)")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Solo se permiten imágenes JPG, PNG o WEBP.");
                return;
            }

            try (InputStream entrada = parteImagen.getInputStream();
                 ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[8192];
                int bytesLeidos;

                while ((bytesLeidos = entrada.read(buffer)) != -1) {
                    salida.write(buffer, 0, bytesLeidos);
                }

                imagen = salida.toByteArray();
            }
        }
        
        platillo.setId(id);
        platillo.setNombre(nombre);
        platillo.setDescripcion(descripcion);
        platillo.setPrecio(precio);
        platillo.setImagen(imagen);

       
        boolean esAdmin = "admin".equals(request.getParameter("origen"));
        String menu    = esAdmin ? "/HTML/menu_admin.html"             : "/HTML/menu_cocina.html";
        String agregar = esAdmin ? "/HTML/agregar_platillo_admin.html" : "/HTML/agregar_platillo.html";

        boolean ok = platillo_dao.insertar(platillo);
        
        response.sendRedirect(request.getContextPath()
                + (ok ? menu + "?creado=1" : agregar + "?error=1"));
    }  

    @Override
    public String getServletInfo() {
        return "Short description";
    }

    public int generar_id_unico() {
        int id;

        do {
            id = ThreadLocalRandom.current().nextInt(10000, 100000);
        } while (existe_id(id));
    
        return id;
    }
    
    public boolean existe_id(int id) {
        String sql = "SELECT 1 FROM platillo WHERE id_platillo= ?";

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
