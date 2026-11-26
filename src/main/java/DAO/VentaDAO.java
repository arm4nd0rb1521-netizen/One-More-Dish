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

    // ======================= Bloque 4: ventas reales =======================

    /** true si ese pedido ya tiene una venta (ya se cobró). */
    public boolean existePorPedido(int idPedido) {
        String sql = "SELECT 1 FROM venta WHERE id_pedido = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idPedido);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return true; // ante la duda, no cobramos dos veces
        }
    }

    /**
     * Cobra un pedido: crea la venta con el total calculado desde pedido_platillo.
     * Regresa null si todo salió bien, o el mensaje de error para mostrar.
     */
    public String registrarCobro(int idPedido, int idEmpleado) {
        if (existePorPedido(idPedido)) return "Ese pedido ya estaba cobrado.";

        double total = calcularTotalPedido(idPedido);
        if (total <= 0) return "El pedido no tiene platillos.";

        Venta v = new Venta();
        v.setTotal(Math.round(total * 100) / 100.0);
        v.setIdPedido(idPedido);
        v.setIdMesero(idEmpleado);

        // Si dos personas cobran al mismo tiempo, la llave UNIQUE(id_pedido) evita el duplicado
        return insertar(v) ? null : "No se pudo registrar la venta.";
    }

    /**
     * Resumen de los últimos "dias" días (hoy incluido), del más reciente al más viejo.
     * Los días sin ventas salen en 0.
     * Cada elemento: {fecha:"2026-11-25", ventas: 12, monto: 1530.5}
     */
    public ArrayList<java.util.Map<String, Object>> resumenPorDia(int dias) {
        java.util.Map<String, double[]> porDia = new java.util.HashMap<>();
        String sql = "SELECT DATE(fecha) AS dia, COUNT(*) AS ventas, COALESCE(SUM(total), 0) AS monto "
                   + "FROM venta "
                   + "WHERE fecha >= CURDATE() - INTERVAL ? DAY "
                   + "GROUP BY DATE(fecha)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, dias - 1);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    porDia.put(rs.getDate("dia").toString(),
                               new double[]{rs.getInt("ventas"), rs.getDouble("monto")});
                }
            }

            // Para que "hoy" sea el mismo día que usa MySQL
            java.time.LocalDate hoy;
            try (Statement st = con.createStatement();
                 ResultSet rs = st.executeQuery("SELECT CURDATE() AS hoy")) {
                rs.next();
                hoy = rs.getDate("hoy").toLocalDate();
            }

            ArrayList<java.util.Map<String, Object>> lista = new ArrayList<>();
            for (int i = 0; i < dias; i++) {
                String fecha = hoy.minusDays(i).toString();
                double[] d = porDia.getOrDefault(fecha, new double[]{0, 0});
                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("fecha", fecha);
                m.put("ventas", (int) d[0]);
                m.put("monto", Math.round(d[1] * 100) / 100.0);
                lista.add(m);
            }
            return lista;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /** Pedidos que todavía no se cobran (sin venta y no cancelados), con su total. */
    public ArrayList<java.util.Map<String, Object>> pedidosPorCobrar() {
        ArrayList<java.util.Map<String, Object>> lista = new ArrayList<>();
        String sql = "SELECT p.id_pedido, p.domicilio, p.id_mesa, p.estado, c.nombre, c.apellido, "
                   + "(SELECT COALESCE(SUM(pl.precio), 0) FROM pedido_platillo pp "
                   + "   INNER JOIN platillo pl ON pl.id_platillo = pp.id_platillo "
                   + "   WHERE pp.id_pedido = p.id_pedido) AS total "
                   + "FROM pedido p "
                   + "LEFT JOIN venta v ON v.id_pedido = p.id_pedido "
                   + "LEFT JOIN cliente c ON c.id_cliente = p.id_cliente "
                   + "WHERE v.id_venta IS NULL AND (p.estado IS NULL OR p.estado <> 'Cancelado') "
                   + "ORDER BY p.fecha DESC, p.id_pedido DESC "
                   + "LIMIT 50";

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                double total = rs.getDouble("total");
                if (total <= 0) continue; // pedidos vacíos no se pueden cobrar

                String quien;
                if (rs.getBoolean("domicilio")) {
                    String n = rs.getString("nombre");
                    String a = rs.getString("apellido");
                    quien = "Domicilio · " + ((n == null ? "" : n) + " " + (a == null ? "" : a)).trim();
                } else {
                    int mesa = rs.getInt("id_mesa");
                    quien = rs.wasNull() ? "Sin mesa" : "Mesa " + mesa;
                }

                java.util.Map<String, Object> m = new java.util.LinkedHashMap<>();
                m.put("id", rs.getInt("id_pedido"));
                m.put("descripcion", quien);
                m.put("estado", rs.getString("estado"));
                m.put("total", Math.round(total * 100) / 100.0);
                lista.add(m);
            }
            return lista;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
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