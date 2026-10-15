
package DAO;
import java.sql.*;
import modelo.Platillo;
import modelo.Insumo;
import conexion.base_de_datos;
import java.util.ArrayList;

public class PlatilloDAO {
    
    
    private static final String SELECT_BASE =
        "SELECT id_platillo, nombre, descripcion, precio FROM platillo";

    public boolean insertar(Platillo p) {
        String sql = "INSERT INTO platillo(nombre, descripcion, precio, imagen) VALUES(?,?,?,?)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setBytes(4, p.getImagen());

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
        // Primero las tablas que dependen de platillo (FK), luego el platillo
        String[] sqlHijas = {
            "DELETE FROM platillo_insumo WHERE id_platillo = ?",
            "DELETE FROM pedido_platillo WHERE id_platillo = ?"
        };
        String sqlPlatillo = "DELETE FROM platillo WHERE id_platillo = ?";

        try (Connection con = base_de_datos.conectar()) {

            for (String sql : sqlHijas) {
                try (PreparedStatement ps = con.prepareStatement(sql)) {
                    ps.setInt(1, id);
                    ps.executeUpdate();
                }
            }
            try (PreparedStatement ps = con.prepareStatement(sqlPlatillo)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Platillo buscarUno(int id) {
        Platillo p = null;
        String sql = SELECT_BASE + " WHERE id_platillo = ?";

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

    public ArrayList<Platillo> buscarTodo() {
        ArrayList<Platillo> lista = new ArrayList<>();

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizar(Platillo p) {
        String sql = "UPDATE platillo SET nombre = ?, descripcion = ?, precio = ? "
                   + "WHERE id_platillo = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setString(2, p.getDescripcion());
            ps.setDouble(3, p.getPrecio());
            ps.setInt(4, p.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ----- Relación platillo <-> insumo (tabla platillo_insumo) -----

    public boolean agregarInsumo(int idPlatillo, int idInsumo) {
        String sql = "INSERT INTO platillo_insumo(id_platillo, id_insumo) VALUES(?,?)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPlatillo);
            ps.setInt(2, idInsumo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean quitarInsumo(int idPlatillo, int idInsumo) {
        String sql = "DELETE FROM platillo_insumo WHERE id_platillo = ? AND id_insumo = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPlatillo);
            ps.setInt(2, idInsumo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Insumos que lleva un platillo. */
    public ArrayList<Insumo> buscarInsumos(int idPlatillo) {
        ArrayList<Insumo> lista = new ArrayList<>();
        String sql = "SELECT i.id_insumo, i.nombre, i.stock, i.caducidad, i.id_proveedor "
                   + "FROM platillo_insumo pi INNER JOIN insumo i ON pi.id_insumo = i.id_insumo "
                   + "WHERE pi.id_platillo = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPlatillo);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(InsumoDAO.mapear(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // Package-private y static: PedidoDAO también lo usa
    static Platillo mapear(ResultSet rs) throws SQLException {
        Platillo p = new Platillo();
        p.setId(rs.getInt("id_platillo"));
        p.setNombre(rs.getString("nombre"));
        p.setDescripcion(rs.getString("descripcion"));
        p.setPrecio(rs.getDouble("precio"));
        return p;
    }
    
}
