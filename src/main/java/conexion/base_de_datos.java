package conexion;

import java.sql.*;

public class base_de_datos {
    private static final String url = "jdbc:mysql://localhost:3306/one_more_dish_db";
    private static final String user = "root";  
    private static final String contraseña = "CUB200915GDS";
    
    public static Connection conectar() throws SQLException {
        try {
        Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de MySQL", e);
        }
        return DriverManager.getConnection(url, user, contraseña);
    }
}
