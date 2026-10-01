package persistencia.usuariodao;

import entidad.UsuarioEntidad;
import java.sql.*;
import persistencia.*;

/** Implementación JDBC de usuarios. */
public class UsuarioDAO implements IUsuarioDAO {
  private final IConexion conexion;

  public UsuarioDAO(IConexion conexion) {
    this.conexion = conexion;
  }

  @Override
  public UsuarioEntidad autenticar(String usuario, String hash) throws PersistenciaException {
    String sql =
        "SELECT u.*, a.idPromotora,p.nombreComercial,c.fechaNacimiento FROM usuario u LEFT"
            + " JOIN admin_promotora a ON a.idUsuario=u.idUsuario LEFT JOIN promotora p ON"
            + " p.idPromotora=a.idPromotora LEFT JOIN cliente c ON c.idUsuario=u.idUsuario"
            + " WHERE u.usuario=? AND u.contrasena=?";
    try (Connection c = conexion.crearConexion();
        PreparedStatement s = c.prepareStatement(sql)) {
      s.setString(1, usuario);
      s.setString(2, hash);
      try (ResultSet r = s.executeQuery()) {
        if (!r.next()) return null;
        UsuarioEntidad u = new UsuarioEntidad();
        u.setIdUsuario(r.getInt("idUsuario"));
        u.setNombres(r.getString("nombres"));
        u.setApPaterno(r.getString("apPaterno"));
        u.setApMaterno(r.getString("apMaterno"));
        u.setUsuario(r.getString("usuario"));
        u.setIdPromotora(r.getInt("idPromotora"));
        u.setPromotora(r.getString("nombreComercial"));
        u.setRol(u.getIdPromotora() > 0 ? "ADMIN" : "CLIENTE");
        Date d = r.getDate("fechaNacimiento");
        if (d != null) u.setFechaNacimiento(d.toLocalDate());
        return u;
      }
    } catch (SQLException e) {
      throw Errores.convertir(e);
    }
  }

  @Override
  public UsuarioEntidad crear(UsuarioEntidad u) throws PersistenciaException {
    try (Connection c = conexion.crearConexion();
        Bloqueo bloqueo = new Bloqueo(c)) {
      try (PreparedStatement s = c.prepareStatement("SELECT 1 FROM usuario WHERE usuario=?")) {
        s.setString(1, u.getUsuario());
        try (ResultSet r = s.executeQuery()) {
          if (r.next()) throw new PersistenciaException("El usuario ya está registrado.");
        }
      }
      try (PreparedStatement s =
          c.prepareStatement(
              "INSERT INTO usuario(nombres,apPaterno,apMaterno,usuario,contrasena)"
                  + " VALUES(?,?,?,?,?)",
              Statement.RETURN_GENERATED_KEYS)) {
        s.setString(1, u.getNombres());
        s.setString(2, u.getApPaterno());
        s.setString(3, u.getApMaterno());
        s.setString(4, u.getUsuario());
        s.setString(5, u.getContrasena());
        s.executeUpdate();
        try (ResultSet r = s.getGeneratedKeys()) {
          r.next();
          u.setIdUsuario(r.getInt(1));
        }
      }
      try (PreparedStatement s =
          c.prepareStatement("INSERT INTO cliente(idUsuario,fechaNacimiento) VALUES(?,?)")) {
        s.setInt(1, u.getIdUsuario());
        s.setDate(2, Date.valueOf(u.getFechaNacimiento()));
        s.executeUpdate();
      }
      String[] bancos = {"BBVA", "Santander", "Banorte"};
      for (int i = 0; i < 3; i++) {
        // Dieciséis dígitos únicos
        String numero = String.format("9%014d%d", u.getIdUsuario(), i + 1);
        int idCuenta;
        try (PreparedStatement s =
            c.prepareStatement(
                "INSERT INTO cuenta_bancaria(banco,numeroCuenta,saldo)" + " VALUES(?,?,1000.00)",
                Statement.RETURN_GENERATED_KEYS)) {
          s.setString(1, bancos[i]);
          s.setString(2, numero);
          s.executeUpdate();
          try (ResultSet r = s.getGeneratedKeys()) {
            r.next();
            idCuenta = r.getInt(1);
          }
        }
        try (PreparedStatement s =
            c.prepareStatement(
                "INSERT INTO cuenta_cliente(idCuenta_Bancaria,idCliente)" + " VALUES(?,?)")) {
          s.setInt(1, idCuenta);
          s.setInt(2, u.getIdUsuario());
          s.executeUpdate();
        }
      }
      u.setContrasena(null);
      u.setRol("CLIENTE");
      return u;
    } catch (SQLException e) {
      throw Errores.convertir(e);
    }
  }
}
