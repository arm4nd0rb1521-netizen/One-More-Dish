
package DAO;
import java.sql.*;
import modelo.Empleado;
import conexion.base_de_datos;
import java.util.ArrayList;
public class EmpleadoDAO {
    
     private static final String SELECT_BASE =
        "SELECT e.id_empleado, e.nombre, e.apellido, e.correo, e.contraseña, r.nombre AS rol "
      + "FROM empleado e LEFT JOIN rol r ON e.id_rol = r.id_rol";

    public boolean insertar(Empleado e) {
        String sql = "INSERT INTO empleado(nombre, apellido, correo, contraseña, id_rol) "
                   + "VALUES(?,?,?,?,(SELECT id_rol FROM rol WHERE nombre = ?))";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getCorreo());
            ps.setString(4, e.getContraseña());
            ps.setString(5, e.getRol());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) e.setId(rs.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM empleado WHERE id_empleado = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Empleado buscarUno(int id) {
        Empleado e = null;
        String sql = SELECT_BASE + " WHERE e.id_empleado = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) e = mapear(rs);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return e;
    }

    public ArrayList<Empleado> buscarTodo() {
        ArrayList<Empleado> lista = new ArrayList<>();

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return lista;
    }

    public boolean actualizar(Empleado e) {
        String sql = "UPDATE empleado SET nombre = ?, apellido = ?, correo = ?, "
                   + "contraseña = ?, id_rol = (SELECT id_rol FROM rol WHERE nombre = ?) WHERE id_empleado = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, e.getNombre());
            ps.setString(2, e.getApellido());
            ps.setString(3, e.getCorreo());
            ps.setString(4, e.getContraseña());
            ps.setString(5, e.getRol());
            ps.setInt(6, e.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    /** Devuelve el empleado si correo y contraseña coinciden; si no, null. */
    public Empleado login(String correo, String contraseña) {
        Empleado e = null;
        String sql = SELECT_BASE + " WHERE e.correo = ? AND e.contraseña = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            ps.setString(2, contraseña);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) e = mapear(rs);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return e;
    }

    private Empleado mapear(ResultSet rs) throws SQLException {
        Empleado e = new Empleado();
        e.setId(rs.getInt("id_empleado"));
        e.setNombre(rs.getString("nombre"));
        e.setApellido(rs.getString("apellido"));
        e.setCorreo(rs.getString("correo"));
        e.setContraseña(rs.getString("contraseña"));
        e.setRol(rs.getString("rol"));
        return e;
    }
}
