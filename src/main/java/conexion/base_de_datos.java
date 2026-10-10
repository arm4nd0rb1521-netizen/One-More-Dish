package conexion;

import java.sql.*;

public class base_de_datos {
    private static final String url = "jdbc:mysql://localhost:3306/one_more_dish_db";
    private static final String user = "root";  
    private static final String contraseña = "ces160408";
    
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(url, user, contraseña);
    }
}
