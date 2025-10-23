package bd;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TestCharset {
    public static void main(String[] args) {
        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement()) {

            // Consultar información del character set de la base de datos
            System.out.println("=== Información de Character Set ===");
            ResultSet rs = stmt.executeQuery(
                    "SELECT * FROM NLS_DATABASE_PARAMETERS WHERE PARAMETER IN ('NLS_CHARACTERSET', 'NLS_NCHAR_CHARACTERSET')");

            while (rs.next()) {
                System.out.println(rs.getString("PARAMETER") + " = " + rs.getString("VALUE"));
            }
            rs.close();

            // Probar lectura de datos
            System.out.println("\n=== Datos de TIPO_PERSONA ===");
            rs = stmt.executeQuery("SELECT * FROM TIPO_PERSONA WHERE ID_TPER = 1");

            if (rs.next()) {
                String desc = rs.getString("DESCRIPCION_TPER");
                System.out.println("Descripción: " + desc);
                System.out.println("Bytes: ");
                for (byte b : desc.getBytes()) {
                    System.out.printf("%02X ", b);
                }
                System.out.println();
            }
            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
