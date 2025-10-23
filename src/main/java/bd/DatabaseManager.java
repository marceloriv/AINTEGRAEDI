package bd;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseManager {

    private static final Properties config = new Properties();

    // Cargar configuración al iniciar la clase
    static {
        try (InputStream input = DatabaseManager.class.getClassLoader()
                .getResourceAsStream("database.properties")) {
            if (input == null) {
                throw new RuntimeException("No se encontró el archivo database.properties en src/main/resource/");
            }
            config.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error al cargar database.properties: " + e.getMessage(), e);
        }
    }

    public static Connection getConnection() throws SQLException {
        // Leer configuración desde el archivo .properties
        String walletPath = config.getProperty("wallet.path");
        String serviceName = config.getProperty("service.name");
        String dbUser = config.getProperty("db.user");
        String dbPassword = config.getProperty("db.password");

        // Configurar TNS_ADMIN como propiedad del sistema
        System.setProperty("oracle.net.tns_admin", walletPath);

        // URL sin TNS_ADMIN (se usa System.setProperty en su lugar)
        String connectionUrl = "jdbc:oracle:thin:@" + serviceName;

        // Crear propiedades de conexión
        Properties connectionProps = new Properties();
        connectionProps.setProperty("user", dbUser);
        connectionProps.setProperty("password", dbPassword);

        // Forzar UTF-8 en el driver JDBC
        connectionProps.setProperty("oracle.jdbc.defaultNChar", "true");

        return DriverManager.getConnection(connectionUrl, connectionProps);
    }
}
