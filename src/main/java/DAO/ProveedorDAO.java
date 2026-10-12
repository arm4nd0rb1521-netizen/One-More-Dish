
package DAO;

import java.sql.*;
import modelo.Proveedor;
import conexion.base_de_datos;
import java.util.ArrayList;

public class ProveedorDAO {
        private static final String SELECT_BASE =
        "SELECT id_proveedor, nombre, apellido, rol, correo, contraseña FROM proveedor";

    public boolean insertar(Proveedor p) {
        String sql = "INSERT INTO proveedor(nombre, apellido, rol, correo, contraseña) "
                   + "VALUES(?,?,?,?,?)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getServicio());
            ps.setString(4, p.getCorreo());
            ps.setString(5, p.getContraseña());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setId(rs.getInt(1));
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int id) {
        String sql = "DELETE FROM proveedor WHERE id_proveedor = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Proveedor buscarUno(int id) {
        Proveedor p = null;
        String sql = SELECT_BASE + " WHERE id_proveedor = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p = mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    public ArrayList<Proveedor> buscarTodo() {
        ArrayList<Proveedor> lista = new ArrayList<>();

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizar(Proveedor p) {
        String sql = "UPDATE proveedor SET nombre = ?, apellido = ?, rol = ?, "
                   + "correo = ?, contraseña = ? WHERE id_proveedor = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getApellido());
            ps.setString(3, p.getServicio());
            ps.setString(4, p.getCorreo());
            ps.setString(5, p.getContraseña());
            ps.setInt(6, p.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Proveedor mapear(ResultSet rs) throws SQLException {
        Proveedor p = new Proveedor();
        p.setId(rs.getInt("id_proveedor"));
        p.setNombre(rs.getString("nombre"));
        p.setApellido(rs.getString("apellido"));
        p.setServicio(rs.getString("rol"));
        p.setCorreo(rs.getString("correo"));
        p.setContraseña(rs.getString("contraseña"));
        return p;
    }
}
