package persistencia;

import java.sql.*;

/** Clase que ideamos para evitar hacer uso de transaction en mysql. */
public final class Bloqueo implements AutoCloseable {
  private final Connection conexion;

  public Bloqueo(Connection conexion) throws SQLException {
    this.conexion = conexion;
    try (PreparedStatement p =
            conexion.prepareStatement("SELECT GET_LOCK('tuticket_operaciones',10)");
        ResultSet r = p.executeQuery()) {
      if (!r.next() || r.getInt(1) != 1)
        throw new SQLException("El sistema está ocupado. Intenta nuevamente.", "45000");
    }
  }

  @Override
  public void close() throws SQLException {
    try (PreparedStatement p =
        conexion.prepareStatement("SELECT RELEASE_LOCK('tuticket_operaciones')")) {
      p.execute();
    }
  }
}
