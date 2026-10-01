package persistencia.compradao;

import entidad.CompraEntidad;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import persistencia.*;

/** Valida antes de efectuar cambios. */
public class CompraDAO implements ICompraDAO {
  private final IConexion conexion;

  public CompraDAO(IConexion conexion) {
    this.conexion = conexion;
  }

  @Override
  public int comprar(int cliente, int evento, int cuenta) throws PersistenciaException {
    return comprar(cliente, evento, cuenta, 1).getFirst();
  }

  @Override
  public List<Integer> comprar(int cliente, int evento, int cuenta, int cantidad)
      throws PersistenciaException {
    if (cantidad < 1 || cantidad > 100000)
      throw new PersistenciaException("La cantidad de boletos debe estar entre 1 y 100,000.");
    
    try (Connection c = conexion.crearConexion();
        Bloqueo bloqueo = new Bloqueo(c)) {
      BigDecimal precio, saldo, receptorSaldo;
      int receptor, edad, edadMinima;
      List<Integer> boletos = new ArrayList<>(), compras = new ArrayList<>();
      
      try (PreparedStatement s =
          c.prepareStatement(
              "SELECT"
                  + " e.precioBoleto,e.idCuenta_Bancaria,e.edadMinima,TIMESTAMPDIFF(YEAR,cl.fechaNacimiento,CURDATE())"
                  + " edad FROM evento e JOIN cliente cl ON cl.idUsuario=? WHERE e.idEvento=?")) {
        s.setInt(1, cliente);
        s.setInt(2, evento);
        
        try (ResultSet r = s.executeQuery()) {
          if (!r.next()) throw new PersistenciaException("No se encontró el cliente o evento.");
          precio = r.getBigDecimal(1);
          receptor = r.getInt(2);
          edadMinima = r.getInt(3);
          edad = r.getInt(4);
        }
      }
      if (edad < edadMinima)
        throw new PersistenciaException("No cumples la edad mínima del evento.");
      
      try (PreparedStatement s =
          c.prepareStatement(
              "SELECT cb.saldo FROM cuenta_bancaria cb JOIN cuenta_cliente cc"
                  + " USING(idCuenta_Bancaria) WHERE cc.idCliente=? AND"
                  + " cb.idCuenta_Bancaria=?")) {
        s.setInt(1, cliente);
        s.setInt(2, cuenta);
        try (ResultSet r = s.executeQuery()) {
          if (!r.next()) throw new PersistenciaException("La cuenta no pertenece al cliente.");
          saldo = r.getBigDecimal(1);
        }
      }
      BigDecimal total = precio.multiply(BigDecimal.valueOf(cantidad));
      if (saldo.compareTo(total) < 0)
        throw new PersistenciaException(
            "Saldo insuficiente para comprar la cantidad de boletos solicitada.");
      
      try (PreparedStatement s =
          c.prepareStatement(
              "SELECT cb.saldo FROM cuenta_bancaria cb JOIN cuenta_promotora cp"
                  + " USING(idCuenta_Bancaria) JOIN evento e ON"
                  + " e.idPromotora=cp.idPromotora AND"
                  + " e.idCuenta_Bancaria=cp.idCuenta_Bancaria WHERE e.idEvento=?")) {
        s.setInt(1, evento);
        try (ResultSet r = s.executeQuery()) {
          if (!r.next())
            throw new PersistenciaException("La cuenta receptora del evento no es válida.");
          receptorSaldo = r.getBigDecimal(1);
        }
      }
      if (receptorSaldo.add(total).compareTo(new BigDecimal("9999999999.99")) > 0)
        throw new PersistenciaException("La cuenta receptora alcanzó su límite.");
      
      try (PreparedStatement s =
          c.prepareStatement(
              "SELECT b.idBoleto FROM boleto b WHERE b.idEvento=? AND"
                  + " b.estatus='disponible' AND NOT EXISTS(SELECT 1 FROM compra co"
                  + " WHERE co.idBoleto=b.idBoleto AND co.estatus='comprado') ORDER"
                  + " BY b.idBoleto LIMIT ?")) {
        s.setInt(1, evento);
        s.setInt(2, cantidad);
        try (ResultSet r = s.executeQuery()) {
          while (r.next()) boletos.add(r.getInt(1));
        }
        if (boletos.size() != cantidad)
          throw new PersistenciaException(
              "La cantidad solicitada supera los boletos disponibles: " + boletos.size() + ".");
      }
      
      try (PreparedStatement insertar =
              c.prepareStatement(
                  "INSERT INTO compra(idCliente,idBoleto,idCuenta_Bancaria,precioFinal,estatus)"
                      + " VALUES(?,?,?,?,'comprado')",
                  Statement.RETURN_GENERATED_KEYS);
          PreparedStatement vender =
              c.prepareStatement("UPDATE boleto SET estatus='vendido' WHERE idBoleto=?")) {
        for (int boleto : boletos) {
          insertar.setInt(1, cliente);
          insertar.setInt(2, boleto);
          insertar.setInt(3, cuenta);
          insertar.setBigDecimal(4, precio);
          insertar.executeUpdate();
          try (ResultSet r = insertar.getGeneratedKeys()) {
            if (!r.next()) throw new SQLException("No se obtuvo el ID de compra.");
            compras.add(r.getInt(1));
          }
          vender.setInt(1, boleto);
          vender.executeUpdate();
        }
      }
      
      try (PreparedStatement s =
          c.prepareStatement(
              "UPDATE cuenta_bancaria SET saldo=saldo-? WHERE idCuenta_Bancaria=?")) {
        s.setBigDecimal(1, total);
        s.setInt(2, cuenta);
        s.executeUpdate();
      }
      try (PreparedStatement s =
          c.prepareStatement(
              "UPDATE cuenta_bancaria SET saldo=saldo+? WHERE idCuenta_Bancaria=?")) {
        s.setBigDecimal(1, total);
        s.setInt(2, receptor);
        s.executeUpdate();
      }
      return List.copyOf(compras);
    } catch (SQLException e) {
      throw Errores.convertir(e);
    }
  }

  @Override
  public void cancelar(int compra, int cliente) throws PersistenciaException {
    try (Connection c = conexion.crearConexion();
        CallableStatement s = c.prepareCall("{CALL sp_cancelar_compra(?,?)}")) {
      s.setInt(1, compra);
      s.setInt(2, cliente);
      s.execute();
    } catch (SQLException e) {
      throw Errores.convertir(e);
    }
  }

  @Override
  public List<CompraEntidad> listar(int cliente) throws PersistenciaException {
    String sql =
        """
        SELECT co.*, e.nombreShow,p.nombreComercial,CONCAT(u.nombres,' ',u.apPaterno,' ',u.apMaterno) nombreCliente,
        CONCAT(cb.banco,' · ',cb.numeroCuenta) cuenta
        FROM compra co JOIN boleto b USING(idBoleto) JOIN evento e USING(idEvento)
        JOIN promotora p USING(idPromotora) JOIN usuario u ON u.idUsuario=co.idCliente
        JOIN cuenta_bancaria cb ON cb.idCuenta_Bancaria=co.idCuenta_Bancaria
        WHERE co.idCliente=? ORDER BY co.idCompra DESC
        """;
    try (Connection c = conexion.crearConexion();
        PreparedStatement s = c.prepareStatement(sql)) {
      s.setInt(1, cliente);
      try (ResultSet r = s.executeQuery()) {
        List<CompraEntidad> compras = new ArrayList<>();
        while (r.next()) {
          CompraEntidad x = new CompraEntidad();
          x.setIdCompra(r.getInt("idCompra"));
          x.setIdCliente(r.getInt("idCliente"));
          x.setIdBoleto(r.getInt("idBoleto"));
          x.setIdCuenta_Bancaria(r.getInt("idCuenta_Bancaria"));
          x.setPrecioFinal(r.getBigDecimal("precioFinal"));
          x.setHoraCompra(r.getTimestamp("horaCompra").toLocalDateTime());
          Timestamp t = r.getTimestamp("horaCancelacion");
          if (t != null) x.setHoraCancelacion(t.toLocalDateTime());
          x.setEstatus(r.getString("estatus"));
          x.setEvento(r.getString("nombreShow"));
          x.setCliente(r.getString("nombreCliente"));
          x.setPromotora(r.getString("nombreComercial"));
          x.setCuenta(r.getString("cuenta"));
          compras.add(x);
        }
        return compras;
      }
    } catch (SQLException e) {
      throw Errores.convertir(e);
    }
  }
}
