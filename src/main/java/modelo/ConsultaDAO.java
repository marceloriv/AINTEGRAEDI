package modelo;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import bd.DatabaseManager;

/**
 * DAO (Data Access Object) - El "experto en SQL"
 * Este archivo NO sabe nada de consolas, ventanas o interfaces.
 * Solo sabe ejecutar SQL y devolver datos.
 */
public class ConsultaDAO {

    /**
     * Método de prueba básico - consulta DUAL
     */
    public List<String> getResultadosDual() {
        List<String> resultados = new ArrayList<>();
        String sql = "SELECT 'Conectado exitosamente' AS RESULTADO FROM DUAL";

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Conexión exitosa a Oracle con Wallet.");
            while (rs.next()) {
                resultados.add(rs.getString("RESULTADO"));
            }
        } catch (SQLException e) {
            System.err.println("Error al consultar la base de datos: " + e.getMessage());
        }
        return resultados;
    }

    /**
     * Obtiene la lista de todas las tablas del esquema
     * 
     * @return Lista con nombres de tablas
     */
    public List<String> obtenerNombresDeTablas() {
        List<String> tablas = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            ResultSet rs = metaData.getTables(null, "AINTEGRAED", "%", new String[] { "TABLE" });

            while (rs.next()) {
                tablas.add(rs.getString("TABLE_NAME"));
            }
            rs.close();

        } catch (SQLException e) {
            System.err.println("Error al obtener tablas: " + e.getMessage());
        }

        return tablas;
    }

    /**
     * Obtiene todos los datos de una tabla específica
     * 
     * @param nombreTabla El nombre de la tabla a consultar
     * @return Lista de mapas, donde cada mapa es una fila (columna -> valor)
     */
    public List<Map<String, Object>> obtenerDatosDeTabla(String nombreTabla) {
        List<Map<String, Object>> datos = new ArrayList<>();

        // Validar nombre de tabla (prevenir SQL injection)
        if (!esNombreTablaValido(nombreTabla)) {
            System.err.println("Nombre de tabla inválido: " + nombreTabla);
            return datos;
        }

        String sql = "SELECT * FROM " + nombreTabla;

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> fila = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    Object value = rs.getObject(i);

                    // Corregir datos mal codificados en Oracle
                    if (value instanceof String) {
                        String str = (String) value;
                        // Detectar si está mal codificado (contiene caracteres extraños)
                        if (str.contains("Ã") || str.contains("â€")) {
                            // Recodificar: Los datos están en UTF-8 pero fueron leídos como Windows-1252
                            byte[] wrongBytes = str.getBytes(StandardCharsets.ISO_8859_1);
                            String fixed = new String(wrongBytes, java.nio.charset.StandardCharsets.UTF_8);
                            value = fixed;
                        }
                    }

                    fila.put(columnName, value);
                }
                datos.add(fila);
            }

        } catch (SQLException e) {
            System.err.println("Error al consultar tabla " + nombreTabla + ": " + e.getMessage());
        }

        return datos;
    }

    /**
     * Obtiene los nombres de las columnas de una tabla
     * 
     * @param nombreTabla El nombre de la tabla
     * @return Lista con nombres de columnas
     */
    public List<String> obtenerColumnasDeTabla(String nombreTabla) {
        List<String> columnas = new ArrayList<>();

        if (!esNombreTablaValido(nombreTabla)) {
            return columnas;
        }

        String sql = "SELECT * FROM " + nombreTabla + " WHERE 1=0"; // No trae datos, solo metadata

        try (Connection conn = DatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            for (int i = 1; i <= columnCount; i++) {
                columnas.add(metaData.getColumnName(i));
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener columnas de " + nombreTabla + ": " + e.getMessage());
        }

        return columnas;
    }

    /**
     * Valida que el nombre de tabla sea seguro (prevención SQL injection)
     */
    private boolean esNombreTablaValido(String nombreTabla) {
        return nombreTabla != null && nombreTabla.matches("^[A-Za-z0-9_]+$");
    }

    // ¡Aquí puedes agregar más métodos!
    // public List<Producto> getProductos() { ... }
    // public boolean insertarProducto(Producto p) { ... }
    // public void ejecutarProcedimiento() { ... con CallableStatement }
}
