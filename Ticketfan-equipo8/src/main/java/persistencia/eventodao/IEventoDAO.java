package persistencia.eventodao;

import entidad.EventoEntidad;
import java.util.List;
import persistencia.PersistenciaException;

/** Persistencia del evento e inventario. */
public interface IEventoDAO {
  List<EventoEntidad> listar(int idPromotora) throws PersistenciaException;

  EventoEntidad guardar(EventoEntidad evento) throws PersistenciaException;
}
