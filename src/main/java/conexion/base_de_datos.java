package conexion;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class base_de_datos {

    public static Connection conectar() throws SQLException {

        String url = "jdbc:mysql://localhost:3306/one_more_dish_db";
        String usuario = "root";
        String contraseña = "ces160408";

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");

            return DriverManager.getConnection(url, usuario, contraseña);

        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver JDBC de MySQL.", e);
        }
    }
}