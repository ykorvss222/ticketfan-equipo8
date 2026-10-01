package persistencia.compradao;

import entidad.CompraEntidad;
import java.util.List;
import persistencia.PersistenciaException;

/** Compra, historial y cancelación mediante procedimiento. */
public interface ICompraDAO {
  int comprar(int cliente, int evento, int cuenta) throws PersistenciaException;

  List<Integer> comprar(int cliente, int evento, int cuenta, int cantidad)
      throws PersistenciaException;

  void cancelar(int compra, int cliente) throws PersistenciaException;

  List<CompraEntidad> listar(int cliente) throws PersistenciaException;
}
