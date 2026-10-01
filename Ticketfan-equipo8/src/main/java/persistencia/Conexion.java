package persistencia;

import java.nio.file.*;
import java.sql.*;
import java.util.Properties;

/** Conexión configurable mediante db.properties, propiedades Java o variables TICKETFAN_DB_*. */
public class Conexion implements IConexion {
  private final Properties propiedades = new Properties();

  public Conexion() {
    Path p = Path.of("db.properties");
    if (Files.exists(p))
      try (var in = Files.newInputStream(p)) {
        propiedades.load(in);
      } catch (java.io.IOException e) {
        throw new IllegalStateException("No se pudo leer db.properties", e);
      }
  }

  private String valor(String nombre, String defecto) {
    String env = System.getenv("TICKETFAN_DB_" + nombre.toUpperCase());
    return System.getProperty(
        "db." + nombre, env != null ? env : propiedades.getProperty("db." + nombre, defecto));
  }

  @Override
  public Connection crearConexion() throws SQLException {
    return DriverManager.getConnection(
        valor("url", "jdbc:mysql://127.0.0.1:3306/tuticket?connectionTimeZone=LOCAL"),
        valor("user", "root"),
        valor("password", "root"));
  }
}
