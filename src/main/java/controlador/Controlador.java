package controlador;

import java.util.List;
import java.util.Map;
import modelo.ConsultaDAO;

public class Controlador {
    private final ConsultaDAO dao;

    public Controlador(ConsultaDAO dao) {
        this.dao = dao;
    }

    public List<String> obtenerTablas() {
        return dao.obtenerNombresDeTablas();
    }

    public List<String> obtenerColumnas(String nombreTabla) {
        return dao.obtenerColumnasDeTabla(nombreTabla);
    }

    public List<Map<String, Object>> obtenerDatos(String nombreTabla) {
        return dao.obtenerDatosDeTabla(nombreTabla);
    }

    public List<String> probarConexion() {
        return dao.getResultadosDual();
    }
}
