
package DAO;

import java.sql.*;
import modelo.Pedido;
import conexion.base_de_datos;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ArrayList;

public class PedidoDAO {
    
        private static final String SELECT_BASE =
        "SELECT id_pedido, estado, domicilio, direccion, id_cliente, id_mesa FROM pedido";

    /**
     * Inserta el pedido y sus platillos (tabla pedido_platillo) en UNA transacción:
     * o se guarda todo, o no se guarda nada.
     */
    public boolean insertar(Pedido p) {
        String sqlPedido = "INSERT INTO pedido(estado, domicilio, direccion, id_cliente, id_mesa) "
                          + "VALUES(?,?,?,?,?)";
        String sqlDetalle = "INSERT INTO pedido_platillo(id_pedido, id_platillo) VALUES(?,?)";
        Connection con = null;

        try {
            con = base_de_datos.conectar();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, p.getEstado());
                ps.setBoolean(2, p.getDomicilio());
                ps.setString(3, p.getDireccion());
                setIntONull(ps, 4, p.getId_cliente());
                setIntONull(ps, 5, p.getId_mesa());
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) p.setId(rs.getInt(1));
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlDetalle)) {
                for (modelo.Platillo pl : p.get_platillos_pedidos()) {
                    ps.setInt(1, p.getId());
                    ps.setInt(2, pl.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            con.commit();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            deshacer(con);
            return false;
        } finally {
            cerrar(con);
        }
    }

    public boolean eliminar(int id) {
        Connection con = null;

        try {
            con = base_de_datos.conectar();
            con.setAutoCommit(false);

            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM pedido_platillo WHERE id_pedido = ?")) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }

            boolean borrado;
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM pedido WHERE id_pedido = ?")) {
                ps.setInt(1, id);
                borrado = ps.executeUpdate() > 0;
            }

            con.commit();
            return borrado;

        } catch (SQLException e) {
            e.printStackTrace();
            deshacer(con);
            return false;
        } finally {
            cerrar(con);
        }
    }

    public Pedido buscarUno(int id) {
        Pedido p = null;
        String sql = SELECT_BASE + " WHERE id_pedido = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) p = mapear(rs);
            }
            if (p != null) cargarPlatillos(con, p);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return p;
    }

    public ArrayList<Pedido> buscarTodo() {
        return consultarLista(SELECT_BASE, null);
    }

    public ArrayList<Pedido> buscarPorMesa(int idMesa) {
        return consultarLista(SELECT_BASE + " WHERE id_mesa = ?", idMesa);
    }

    public ArrayList<Pedido> buscarPorCliente(int idCliente) {
        return consultarLista(SELECT_BASE + " WHERE id_cliente = ?", idCliente);
    }

    /** Sirve para la cocina: por ejemplo buscar_por_estado("Pendiente"). */
    public ArrayList<Pedido> buscarPorEstado(String estado) {
        ArrayList<Pedido> lista = new ArrayList<>();
        String sql = SELECT_BASE + " WHERE estado = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            for (Pedido p : lista) cargarPlatillos(con, p);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /** Actualiza los datos del pedido (no toca sus platillos). */
    public boolean actualizar(Pedido p) {
        String sql = "UPDATE pedido SET estado = ?, domicilio = ?, direccion = ?, "
                   + "id_cliente = ?, id_mesa = ? WHERE id_pedido = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, p.getEstado());
            ps.setBoolean(2, p.getDomicilio());
            ps.setString(3, p.getDireccion());
            setIntONull(ps, 4, p.getId_cliente());
            setIntONull(ps, 5, p.getId_mesa());
            ps.setInt(6, p.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
        // ===== PEDIDOS A DOMICILIO (vista de cocina) =====

    // Devuelve null si hubo un error de base de datos
    public ArrayList<Map<String, Object>> listarDomicilios() {
        ArrayList<Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.estado, p.direccion, p.fecha, c.nombre, c.apellido, "
                   + "(SELECT COUNT(*) FROM pedido_platillo pp WHERE pp.id_pedido = p.id_pedido) AS articulos "
                   + "FROM pedido p LEFT JOIN cliente c ON c.id_cliente = p.id_cliente "
                   + "WHERE p.domicilio = TRUE ORDER BY p.fecha DESC, p.id_pedido DESC";

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", rs.getInt("id_pedido"));
                m.put("estado", rs.getString("estado"));
                m.put("direccion", rs.getString("direccion"));
                Timestamp f = rs.getTimestamp("fecha");
                m.put("fecha", f != null ? f.toLocalDateTime().toString() : null);
                m.put("cliente", nombreCliente(rs.getString("nombre"), rs.getString("apellido")));
                m.put("articulos", rs.getInt("articulos"));
                lista.add(m);
            }
            return lista;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Devuelve null si el pedido no existe, no es a domicilio o hubo error
    public Map<String, Object> detalleDomicilio(int id) {
        String sqlPedido = "SELECT p.id_pedido, p.estado, p.direccion, p.fecha, "
                         + "c.nombre, c.apellido, c.telefono "
                         + "FROM pedido p LEFT JOIN cliente c ON c.id_cliente = p.id_cliente "
                         + "WHERE p.id_pedido = ? AND p.domicilio = TRUE";
        String sqlPlatillos = "SELECT pl.nombre, pl.precio, COUNT(*) AS cantidad "
                            + "FROM pedido_platillo pp INNER JOIN platillo pl ON pp.id_platillo = pl.id_platillo "
                            + "WHERE pp.id_pedido = ? GROUP BY pl.id_platillo, pl.nombre, pl.precio "
                            + "ORDER BY pl.nombre";

        try (Connection con = base_de_datos.conectar()) {
            Map<String, Object> m = new LinkedHashMap<>();

            try (PreparedStatement ps = con.prepareStatement(sqlPedido)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return null;
                    m.put("id", rs.getInt("id_pedido"));
                    m.put("estado", rs.getString("estado"));
                    m.put("direccion", rs.getString("direccion"));
                    Timestamp f = rs.getTimestamp("fecha");
                    m.put("fecha", f != null ? f.toLocalDateTime().toString() : null);
                    m.put("cliente", nombreCliente(rs.getString("nombre"), rs.getString("apellido")));
                    m.put("telefono", rs.getString("telefono"));
                }
            }

            ArrayList<Map<String, Object>> platillos = new ArrayList<>();
            double total = 0;
            try (PreparedStatement ps = con.prepareStatement(sqlPlatillos)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> p = new LinkedHashMap<>();
                        int cantidad = rs.getInt("cantidad");
                        double precio = rs.getDouble("precio");
                        p.put("nombre", rs.getString("nombre"));
                        p.put("cantidad", cantidad);
                        p.put("precio", precio);
                        platillos.add(p);
                        total += precio * cantidad;
                    }
                }
            }
            m.put("platillos", platillos);
            m.put("total", Math.round(total * 100) / 100.0);
            return m;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String nombreCliente(String nombre, String apellido) {
        String completo = ((nombre == null ? "" : nombre) + " " + (apellido == null ? "" : apellido)).trim();
        return completo.isEmpty() ? "Cliente" : completo;
    }

    /** Equivale a Cocina.actualizarEstadoPedido(): cambia solo el estado. */
    public boolean actualizarEstado(int idPedido, String estado) {
        String sql = "UPDATE pedido SET estado = ? WHERE id_pedido = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, estado);
            ps.setInt(2, idPedido);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cancelarPedido(int idPedido) {
        return actualizarEstado(idPedido, "Cancelado");
    }

    // ----- Platillos de un pedido (tabla pedido_platillo) -----

    public boolean agregar_platillo(int idPedido, int idPlatillo) {
        String sql = "INSERT INTO pedido_platillo(id_pedido, id_platillo) VALUES(?,?)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ps.setInt(2, idPlatillo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Quita UNA unidad del platillo (si pidieron 2, queda 1). */
    public boolean quitarPlatillo(int idPedido, int idPlatillo) {
        String sql = "DELETE FROM pedido_platillo WHERE id_pedido = ? AND id_platillo = ? LIMIT 1";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            ps.setInt(2, idPlatillo);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ----- Métodos privados de apoyo -----

    private ArrayList<Pedido> consultarLista(String sql, Integer parametro) {
        ArrayList<Pedido> lista = new ArrayList<>();

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (parametro != null) ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            for (Pedido p : lista) cargarPlatillos(con, p);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    private void cargarPlatillos(Connection con, Pedido p) throws SQLException {
        String sql = "SELECT pl.id_platillo, pl.nombre, pl.descripcion, pl.precio "
                   + "FROM pedido_platillo pp INNER JOIN platillo pl ON pp.id_platillo = pl.id_platillo "
                   + "WHERE pp.id_pedido = ?";

        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) p.agregar_platillo(PlatilloDAO.mapear(rs));
            }
        }
    }

    private Pedido mapear(ResultSet rs) throws SQLException {
        Pedido p = new Pedido();
        p.setId(rs.getInt("id_pedido"));
        p.setEstado(rs.getString("estado"));
        p.setDomicilio(rs.getBoolean("domicilio"));
        p.setDireccion(rs.getString("direccion"));
        p.setId_cliente(rs.getInt("id_cliente")); // NULL en la BD se lee como 0
        p.setId(rs.getInt("id_mesa"));
        return p;
    }

    private void setIntONull(PreparedStatement ps, int posicion, int valor) throws SQLException {
        if (valor > 0) {
            ps.setInt(posicion, valor);
        } else {
            ps.setNull(posicion, Types.INTEGER);
        }
    }

    private void deshacer(Connection con) {
        try {
            if (con != null) con.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void cerrar(Connection con) {
        try {
            if (con != null) con.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
}
