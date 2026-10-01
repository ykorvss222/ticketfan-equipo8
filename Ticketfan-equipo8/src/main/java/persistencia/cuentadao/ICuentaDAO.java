package persistencia.cuentadao;

import entidad.CuentaEntidad;
import java.util.List;
import persistencia.PersistenciaException;

/** Consulta cuentas de un dueño. */
public interface ICuentaDAO {
  List<CuentaEntidad> listar(int idDueno, boolean promotora) throws PersistenciaException;
}
