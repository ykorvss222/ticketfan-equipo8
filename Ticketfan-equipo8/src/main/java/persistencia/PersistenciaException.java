package persistencia;

/** Error comunicado con un mensaje. */
public class PersistenciaException extends Exception {
  public PersistenciaException(String mensaje) {
    super(mensaje);
  }

  public PersistenciaException(String mensaje, Throwable causa) {
    super(mensaje, causa);
  }
}
