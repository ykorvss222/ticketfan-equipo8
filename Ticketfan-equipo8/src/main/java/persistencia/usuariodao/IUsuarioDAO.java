package persistencia.usuariodao;

import entidad.UsuarioEntidad;
import persistencia.PersistenciaException;

/** Acceso a usuarios y registro del cliente. */
public interface IUsuarioDAO {
  UsuarioEntidad autenticar(String usuario, String hash) throws PersistenciaException;

  UsuarioEntidad crear(UsuarioEntidad nuevo) throws PersistenciaException;
}
