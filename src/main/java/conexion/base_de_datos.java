package conexion;

import java.sql.*;

public class base_de_datos {
    
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
