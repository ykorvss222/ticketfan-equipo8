package persistencia.cuentadao;

import entidad.CuentaEntidad;
import java.sql.*;
import java.util.*;
import persistencia.*;

/** Consulta bancaria limitada al cliente o promotora. */
public class CuentaDAO implements ICuentaDAO {
  private final IConexion conexion;

  public CuentaDAO(IConexion conexion) {
    this.conexion = conexion;
  }

  @Override
  public List<CuentaEntidad> listar(int idDueno, boolean promotora) throws PersistenciaException {
    String sql =
        promotora
            ? "SELECT b.* FROM cuenta_bancaria b JOIN cuenta_promotora p"
                + " USING(idCuenta_Bancaria) WHERE p.idPromotora=?"
            : "SELECT b.* FROM cuenta_bancaria b JOIN cuenta_cliente c"
                + " USING(idCuenta_Bancaria) WHERE c.idCliente=?";
    try (Connection c = conexion.crearConexion();
        PreparedStatement s = c.prepareStatement(sql)) {
      s.setInt(1, idDueno);
      try (ResultSet r = s.executeQuery()) {
        List<CuentaEntidad> cuentas = new ArrayList<>();
        while (r.next()) {
          CuentaEntidad x = new CuentaEntidad();
          x.setIdCuenta_Bancaria(r.getInt("idCuenta_Bancaria"));
          x.setBanco(r.getString("banco"));
          x.setNumeroCuenta(r.getString("numeroCuenta"));
          x.setSaldo(r.getBigDecimal("saldo"));
          cuentas.add(x);
        }
        return cuentas;
      }
    } catch (SQLException e) {
      throw Errores.convertir(e);
    }
  }
}
