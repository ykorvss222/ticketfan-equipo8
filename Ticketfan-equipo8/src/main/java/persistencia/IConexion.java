package persistencia;

/** Conexión JDBC. */
public interface IConexion {
  java.sql.Connection crearConexion() throws java.sql.SQLException;
}
