
package DAO;

import java.sql.*;
import modelo.Venta;
import conexion.base_de_datos;
import java.util.ArrayList;
public class VentaDAO {
        private static final String SELECT_BASE =
        "SELECT id_venta, total, id_pedido, id_empleado FROM venta";

    public boolean insertar(Venta v) {
        String sql = "INSERT INTO venta(total, id_pedido, id_empleado) VALUES(?,?,?)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setDouble(1, v.getTotal());
            ps.setInt(2, v.getIdPedido());
            ps.setInt(3, v.getIdMesero());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) v.setIdVenta(rs.getInt(1));
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
        String sql = "DELETE FROM venta WHERE id_venta = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Venta buscarUno(int id) {
        Venta v = null;
        String sql = SELECT_BASE + " WHERE id_venta = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) v = mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return v;
    }

    public ArrayList<Venta> buscarTodo() {
        ArrayList<Venta> lista = new ArrayList<>();

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizar(Venta v) {
        String sql = "UPDATE venta SET total = ?, id_pedido = ?, id_empleado = ? WHERE id_venta = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setDouble(1, v.getTotal());
            ps.setInt(2, v.getIdPedido());
            ps.setInt(3, v.getIdMesero());
            ps.setInt(4, v.getIdVenta());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Suma el precio de todos los platillos de un pedido */
    public double calcularTotalPedido(int idPedido) {
        String sql = "SELECT COALESCE(SUM(pl.precio), 0) AS total "
                   + "FROM pedido_platillo pp INNER JOIN platillo pl ON pp.id_platillo = pl.id_platillo "
                   + "WHERE pp.id_pedido = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("total");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private Venta mapear(ResultSet rs) throws SQLException {
        Venta v = new Venta();
        v.setIdVenta(rs.getInt("id_venta"));
        v.setTotal(rs.getDouble("total"));
        v.setIdPedido(rs.getInt("id_pedido"));
        v.setIdMesero(rs.getInt("id_empleado"));
        return v;
    }
    
}
