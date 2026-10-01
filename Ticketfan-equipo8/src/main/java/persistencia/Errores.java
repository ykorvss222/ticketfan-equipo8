package persistencia;

import java.sql.SQLException;

/** Traduce errores de JDBC sin exponer SQL al usuario. */
public final class Errores {
  private Errores() {}

  public static PersistenciaException convertir(SQLException e) {
    String mensaje;
    if ("45000".equals(e.getSQLState())) mensaje = e.getMessage();
    else if (e.getErrorCode() == 1062) mensaje = "Ya existe ese usuario, cuenta o compra activa.";
    else if (e.getSQLState() != null && e.getSQLState().startsWith("08"))
      mensaje = "No se pudo conectar a MySQL. Revisa db.properties y el servidor.";
    else
      mensaje =
          "No se pudo completar la operación. Consulta el registro técnico y revisa los"
              + " datos.";
    java.util.logging.Logger.getLogger("TicketFan")
        .log(java.util.logging.Level.WARNING, "Error de persistencia", e);
    return new PersistenciaException(mensaje, e);
  }
}
