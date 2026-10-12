
package DAO;
import java.sql.*;
import modelo.Mesa;
import modelo.Pedido;
import conexion.base_de_datos;
import java.util.ArrayList;
public class MesaDAO {
        public boolean insertar(Mesa m) {
        String sql = "INSERT INTO mesa(estado) VALUES(?)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, m.getEstado());

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) m.setId(rs.getInt(1));
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
        String sql = "DELETE FROM mesa WHERE id_mesa = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Mesa buscarUno(int id) {
        Mesa m = null;
        String sql = "SELECT id_mesa, estado FROM mesa WHERE id_mesa = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) m = mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return m;
    }

    public ArrayList<Mesa> buscarTodo() {
        ArrayList<Mesa> lista = new ArrayList<>();

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery("SELECT id_mesa, estado FROM mesa")) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizar(Mesa m) {
        return cambiarEstado(m.getId(), m.getEstado());
    }

    /** Atajo para cambiar solo el estado (por ejemplo "Libre" / "Ocupada"). */
    public boolean cambiarEstado(int id, String estado) {
        String sql = "UPDATE mesa SET estado = ? WHERE id_mesa = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Carga una mesa junto con sus pedidos (usa PedidoDAO). */
    public Mesa buscarUnoConPedidos(int id) {
        Mesa m = buscarUno(id);
        if (m != null) {
            for (Pedido p : new PedidoDAO().buscarPorMesa(id)) {
                m.agregar_pedido(p);
            }
        }
        return m;
    }

    private Mesa mapear(ResultSet rs) throws SQLException {
        Mesa m = new Mesa();
        m.setId(rs.getInt("id_mesa"));
        m.setEstado(rs.getString("estado"));
        return m;
    }
}
