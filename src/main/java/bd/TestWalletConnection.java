/*
 * Clase de prueba para verificar la conexión a Oracle usando WalletConnection
 */
package bd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author Marcelo-HP
 */
public class TestWalletConnection {

    public static void main(String[] args) throws SQLException {
        System.out.println("Intentando conectar a Oracle con Wallet...");

        Connection conexion = DatabaseManager.getConnection();

        if (conexion != null) {
            System.out.println("¡Conexión exitosa a Oracle con Wallet!");

            try (Statement statement = conexion.createStatement()) {
                ResultSet resultSet = statement.executeQuery("SELECT 'Conectado' AS RESULTADO FROM DUAL");
                if (resultSet.next()) {
                    System.out.println("Resultado de la consulta: " + resultSet.getString("RESULTADO"));
                }
                conexion.close();
                System.out.println("Conexión cerrada correctamente.");
            } catch (SQLException e) {
                System.err.println("Error al ejecutar la consulta: " + e.getMessage());
            }
        } else {
            System.err.println("No se pudo establecer la conexión. Revisa el mensaje de error en el diálogo.");
        }
    }
}
