
package DAO;

import java.sql.*;
import modelo.Insumo;
import conexion.base_de_datos;
import java.util.ArrayList;
import java.time.LocalDate;

public class InsumoDAO {
     private static final String SELECT_BASE =
        "SELECT id_insumo, nombre, stock, caducidad, id_proveedor FROM insumo";

    public boolean insertar(Insumo i) {
        String sql = "INSERT INTO insumo(nombre, stock, caducidad, id_proveedor) VALUES(?,?,?,?)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, i.getNombre());
            ps.setDouble(2, i.getStock() != null ? i.getStock() : 0);
            ps.setDate(3, i.getCaducidad() != null ? Date.valueOf(i.getCaducidad()) : null);
            if (i.getId_proveedor() > 0) {
                ps.setInt(4, i.getId_proveedor());
            } else {
                ps.setNull(4, Types.INTEGER);
            }

            if (ps.executeUpdate() > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) i.setId(rs.getInt(1));
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
        // platillo_insumo tiene FK hacia insumo: primero se borran esas filas
        String sqlRelacion = "DELETE FROM platillo_insumo WHERE id_insumo = ?";
        String sqlInsumo = "DELETE FROM insumo WHERE id_insumo = ?";

        try (Connection con = base_de_datos.conectar()) {

            try (PreparedStatement ps = con.prepareStatement(sqlRelacion)) {
                ps.setInt(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlInsumo)) {
                ps.setInt(1, id);
                return ps.executeUpdate() > 0;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Insumo buscarUno(int id) {
        Insumo i = null;
        String sql = SELECT_BASE + " WHERE id_insumo = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) i = mapear(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return i;
    }

    public ArrayList<Insumo> buscarTodo() {
        ArrayList<Insumo> lista = new ArrayList<>();

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(SELECT_BASE)) {

            while (rs.next()) lista.add(mapear(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean actualizar(Insumo i) {
        String sql = "UPDATE insumo SET nombre = ?, stock = ?, caducidad = ?, id_proveedor = ? "
                   + "WHERE id_insumo = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, i.getNombre());
            ps.setDouble(2, i.getStock() != null ? i.getStock() : 0);
            ps.setDate(3, i.getCaducidad() != null ? Date.valueOf(i.getCaducidad()) : null);
            if (i.getId_proveedor() > 0) {
                ps.setInt(4, i.getId_proveedor());
            } else {
                ps.setNull(4, Types.INTEGER);
            }
            ps.setInt(5, i.getId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
        // ===== INVENTARIO (piezas y kilos) =====

    // Devuelve null si hubo un error de base de datos
    public ArrayList<Insumo> listarInventario() {
        ArrayList<Insumo> lista = new ArrayList<>();
        String sql = "SELECT id_insumo, nombre, piezas, kilos, caducidad FROM insumo ORDER BY nombre";

        try (Connection con = base_de_datos.conectar();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Insumo i = new Insumo();
                i.setId(rs.getInt("id_insumo"));
                i.setNombre(rs.getString("nombre"));
                i.setPiezas(rs.getInt("piezas"));
                i.setKilos(rs.getDouble("kilos"));
                Date f = rs.getDate("caducidad");
                i.setCaducidad(f != null ? f.toLocalDate() : null);
                lista.add(i);
            }
            return lista;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean crearInventario(String nombre, int piezas, double kilos, LocalDate caducidad) {
        String sql = "INSERT INTO insumo(nombre, piezas, kilos, caducidad) VALUES(?,?,?,?)";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.setInt(2, piezas);
            ps.setDouble(3, kilos);
            ps.setDate(4, caducidad != null ? Date.valueOf(caducidad) : null);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizarInventario(int id, int piezas, double kilos, LocalDate caducidad) {
        String sql = "UPDATE insumo SET piezas = ?, kilos = ?, caducidad = ? WHERE id_insumo = ?";

        try (Connection con = base_de_datos.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, piezas);
            ps.setDouble(2, kilos);
            ps.setDate(3, caducidad != null ? Date.valueOf(caducidad) : null);
            ps.setInt(4, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Package-private y static: PlatilloDAO también lo usa
    static Insumo mapear(ResultSet rs) throws SQLException {
        Insumo i = new Insumo();
        i.setId(rs.getInt("id_insumo"));
        i.setNombre(rs.getString("nombre"));
        i.setStock(rs.getDouble("stock"));
        Date fecha = rs.getDate("caducidad");
        i.setCaducidad(fecha != null ? fecha.toLocalDate() : null);
        i.setId_proveedor(rs.getInt("id_proveedor"));
        return i;
    }
}
