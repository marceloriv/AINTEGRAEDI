package modelo;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.CallableStatement;
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
import java.util.stream.Collectors;

import bd.DatabaseManager;

/**
 * DAO (Data Access Object) para consultas SQL y PL/SQL
 */
public class ConsultaDAO {

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

    /**
     * Ejecuta un bloque PL/SQL y devuelve los mensajes de salida
     *
     * @param nombreArchivo Nombre del archivo SQL (ej: "izrael.sql")
     * @return Lista de mensajes de salida de DBMS_OUTPUT
     */
    public List<String> ejecutarBloquePLSQLConSalida(String nombreArchivo) {
        List<String> mensajes = new ArrayList<>();

        try {
            String sqlContent = leerArchivoSQL(nombreArchivo);

            if (sqlContent == null || sqlContent.isEmpty()) {
                mensajes.add("ERROR: El archivo " + nombreArchivo + " está vacío o no existe");
                return mensajes;
            }

            return ejecutarBloqueConSalida(sqlContent);

        } catch (Exception e) {
            mensajes.add("ERROR: " + e.getMessage());
            return mensajes;
        }
    }

    /**
     * Lee un archivo SQL desde src/main/resource/sql/
     *
     * @param nombreArchivo Nombre del archivo (ej: "izrael.sql")
     * @return Contenido del archivo como String
     */
    private String leerArchivoSQL(String nombreArchivo) {
        try {
            InputStream inputStream = getClass().getClassLoader()
                    .getResourceAsStream("sql/" + nombreArchivo);

            if (inputStream == null) {
                System.err.println("No se encontró el archivo: sql/" + nombreArchivo);
                return null;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }

        } catch (Exception e) {
            System.err.println("Error al leer archivo SQL: " + e.getMessage());
            return null;
        }
    }

    /**
     * Ejecuta un bloque PL/SQL y devuelve los mensajes de salida
     *
     * @param bloquePLSQL Contenido del bloque PL/SQL
     * @return Lista de mensajes de DBMS_OUTPUT
     */
    private List<String> ejecutarBloqueConSalida(String bloquePLSQL) {
        List<String> mensajes = new ArrayList<>();

        try (Connection conn = DatabaseManager.getConnection()) {

            // Desactivar auto-commit
            conn.setAutoCommit(false);

            // Habilitar SERVEROUTPUT para ver los DBMS_OUTPUT
            try (CallableStatement enableOutput = conn.prepareCall(
                    "BEGIN DBMS_OUTPUT.ENABLE(1000000); END;")) {
                enableOutput.execute();
            }

            // Separar comandos SQL que no pueden ir dentro del bloque PL/SQL
            String[] lineas = bloquePLSQL.split("\n");
            StringBuilder bloqueLimpio = new StringBuilder();

            for (String linea : lineas) {
                String lineaTrim = linea.trim().toUpperCase();

                // Ejecutar TRUNCATE por separado
                if (lineaTrim.startsWith("TRUNCATE ") && lineaTrim.endsWith(";")) {
                    String truncateCmd = linea.trim();
                    truncateCmd = truncateCmd.substring(0, truncateCmd.length() - 1);

                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute(truncateCmd);
                        mensajes.add("✓ Ejecutado: " + truncateCmd);
                    } catch (SQLException e) {
                        mensajes.add("⚠ Advertencia: " + e.getMessage());
                    }
                    continue;
                }

                // Ignorar líneas de SET SERVEROUTPUT
                if (lineaTrim.startsWith("SET ")) {
                    continue;
                }

                bloqueLimpio.append(linea).append("\n");
            }

            // Ejecutar el bloque PL/SQL limpio
            String sqlLimpio = bloqueLimpio.toString().trim();

            // Remover el / final si existe
            if (sqlLimpio.endsWith("/")) {
                sqlLimpio = sqlLimpio.substring(0, sqlLimpio.length() - 1).trim();
            }

            if (!sqlLimpio.isEmpty()) {
                try (CallableStatement stmt = conn.prepareCall(sqlLimpio)) {
                    stmt.execute();
                    mensajes.add("✓ Bloque PL/SQL ejecutado correctamente");
                }
            }

            // Capturar la salida de DBMS_OUTPUT
            List<String> outputMensajes = capturarDBMSOutputLista(conn);
            mensajes.addAll(outputMensajes);

            // Hacer commit manual
            conn.commit();

        } catch (SQLException e) {
            mensajes.add("ERROR SQL: " + e.getMessage());
        }

        return mensajes;
    }

    /**
     * Captura la salida de DBMS_OUTPUT.PUT_LINE y la devuelve como lista
     *
     * @param conn Conexión activa
     * @return Lista de mensajes de salida
     */
    private List<String> capturarDBMSOutputLista(Connection conn) throws SQLException {
        List<String> mensajes = new ArrayList<>();

        try (CallableStatement stmt = conn.prepareCall(
                "DECLARE "
                        + "  l_line VARCHAR2(32767); "
                        + "  l_status INTEGER; "
                        + "BEGIN "
                        + "  LOOP "
                        + "    DBMS_OUTPUT.GET_LINE(l_line, l_status); "
                        + "    EXIT WHEN l_status = 1; "
                        + "    ? := l_line; "
                        + "  END LOOP; "
                        + "END;")) {

            stmt.registerOutParameter(1, java.sql.Types.VARCHAR);

            while (true) {
                stmt.execute();
                String line = stmt.getString(1);
                if (line == null) {
                    break;
                }
                mensajes.add(line);
            }
        }

        return mensajes;
    }

    /**
     * Ejecuta todos los scripts SQL en orden
     */
    public void ejecutarTodosLosScripts() {
        System.out.println("=== EJECUTANDO SCRIPTS PL/SQL ===\n");

        String[] scripts = {
                "izrael.sql",
                "consulta.sql",
                "Ingrid_Nunez.sql"
        };

        for (String script : scripts) {
            System.out.println("\n📋 Ejecutando: " + script);
            System.out.println("─".repeat(50));

            List<String> mensajes = ejecutarBloquePLSQLConSalida(script);

            if (mensajes.isEmpty() || mensajes.get(0).startsWith("ERROR")) {
                System.err.println("✗ Error en " + script);
                for (String mensaje : mensajes) {
                    System.err.println(mensaje);
                }
            } else {
                System.out.println("✓ " + script + " completado");
            }
        }

        System.out.println("\n=== EJECUCIÓN FINALIZADA ===");
    }
}
