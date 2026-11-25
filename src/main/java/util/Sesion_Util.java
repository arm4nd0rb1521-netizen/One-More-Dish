package util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * Todo lo relacionado con la sesión del usuario en un solo lugar.
 * Lo usan Signin_Servlet, los servlets que validan rol y el socket.
 */
public final class Sesion_Util {

    // Nombres de los atributos guardados en la HttpSession
    public static final String ATR_ID = "id_usuario";
    public static final String ATR_ROL = "rol";
    public static final String ATR_NOMBRE = "nombre";

    // Roles (iguales a la tabla rol)
    public static final String ADMIN = "ADMIN";
    public static final String MESERO = "MESERO";
    public static final String COCINERO = "COCINERO";
    public static final String CLIENTE = "CLIENTE";
    public static final String PROVEEDOR = "PROVEEDOR";

    private Sesion_Util() {
    }

    /** Crea una sesión NUEVA (descarta la anterior para evitar robo de sesión). */
    public static void iniciar(HttpServletRequest request, int id, String rol, String nombre) {
        HttpSession anterior = request.getSession(false);
        if (anterior != null) {
            anterior.invalidate();
        }
        HttpSession sesion = request.getSession(true);
        sesion.setAttribute(ATR_ID, id);
        sesion.setAttribute(ATR_ROL, rol);
        sesion.setAttribute(ATR_NOMBRE, nombre);
    }

    /** Rol del usuario o null si no ha iniciado sesión. */
    public static String rol(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) return null;
        Object rol = sesion.getAttribute(ATR_ROL);
        return rol instanceof String ? (String) rol : null;
    }

    /** Id del usuario o null si no ha iniciado sesión (ojo: el admin tiene id 0). */
    public static Integer idUsuario(HttpServletRequest request) {
        HttpSession sesion = request.getSession(false);
        if (sesion == null) return null;
        Object id = sesion.getAttribute(ATR_ID);
        return id instanceof Integer ? (Integer) id : null;
    }

    /** true si el usuario tiene alguno de los roles indicados. */
    public static boolean tieneRol(HttpServletRequest request, String... roles) {
        String actual = rol(request);
        if (actual == null) return false;
        for (String r : roles) {
            if (r.equals(actual)) return true;
        }
        return false;
    }
}
