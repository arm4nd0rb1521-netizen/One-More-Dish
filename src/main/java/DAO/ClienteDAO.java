
package DAO;
import java.sql.*;
import modelo.Cliente;
import conexion.base_de_datos;
import java.util.ArrayList;
public class ClienteDAO {
        private static final String SELECT_BASE =
        "SELECT id_cliente, nombre, apellido, correo, contraseña, telefono FROM cliente";

    public boolean insertar(Cliente c) {
        String sql = "INSERT INTO cliente(nombre, apellido, correo, contraseña, telefono, id_rol) "
                   + "VALUES(?,?,?,?,?,(SELECT id_rol FROM rol WHERE nombre = 'CLIENTE'))";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getCorreo());
            ps.setString(4, c.getContraseña());
            ps.setString(5, c.getTelefono());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) c.setId(rs.getInt(1)); // id que generó MySQL
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
        String sql = "DELETE FROM cliente WHERE id_cliente = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Cliente buscarUno(int id) {
        Cliente c = null;
        String sql = SELECT_BASE + " WHERE id_cliente = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) c = mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    public ArrayList<Cliente> buscarTodo() {
        ArrayList<Cliente> lista = new ArrayList<>();

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizar(Cliente c) {
        String sql = "UPDATE cliente SET nombre = ?, apellido = ?, correo = ?, "
                   + "contraseña = ?, telefono = ? WHERE id_cliente = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, c.getNombre());
            ps.setString(2, c.getApellido());
            ps.setString(3, c.getCorreo());
            ps.setString(4, c.getContraseña());
            ps.setString(5, c.getTelefono());
            ps.setInt(6, c.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Devuelve el cliente si correo y contraseña coinciden; si no, null. */
    public Cliente login(String correo, String contraseña) {
        Cliente c = null;
        String sql = SELECT_BASE + " WHERE correo = ? AND contraseña = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, correo);
            ps.setString(2, contraseña);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) c = mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return c;
    }

    // Convierte la fila actual del ResultSet en un objeto Cliente
    private Cliente mapear(ResultSet rs) throws SQLException {
        Cliente c = new Cliente();
        c.setId(rs.getInt("id_cliente"));
        c.setNombre(rs.getString("nombre"));
        c.setApellido(rs.getString("apellido"));
        c.setCorreo(rs.getString("correo"));
        c.setContraseña(rs.getString("contraseña"));
        c.setTelefono(rs.getString("telefono"));
        return c;
    }
}
