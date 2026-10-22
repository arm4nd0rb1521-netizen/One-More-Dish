package conexion;

import java.sql.*;

public class base_de_datos {
    private static final String url = "jdbc:mysql://localhost:3306/one_more_dish_db";
    private static final String user = "root";  
    private static final String contraseña = "ces160408";
    
    public static Connection conectar() throws SQLException {
        try {
        String url = System.getenv("MYSQLCONNSTR_base_mysql");
        
        if(url == null) {
            throw new SQLException("Error: La variable de entorno MYSQLCONNSTR_base_mysql no fue encontrada en Azure.");
        }
        
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de MySQL", e);
        }
    }
}
