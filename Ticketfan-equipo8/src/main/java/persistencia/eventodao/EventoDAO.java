package persistencia.eventodao;

import entidad.EventoEntidad;
import java.sql.*;
import java.util.*;
import persistencia.*;

/** Eventos y estadísticas calculadas desde los boletos y compras reales. */
public class EventoDAO implements IEventoDAO {
  private final IConexion conexion;

  public EventoDAO(IConexion conexion) {
    this.conexion = conexion;
  }

  @Override
  public List<EventoEntidad> listar(int promotora) throws PersistenciaException {
    String sql =
        """
        SELECT e.*,p.nombreComercial,CONCAT(cb.banco,' · ',cb.numeroCuenta) cuenta,
        (SELECT COUNT(*) FROM boleto b WHERE b.idEvento=e.idEvento AND b.estatus='vendido') vendidos,
        (SELECT COUNT(*) FROM boleto b WHERE b.idEvento=e.idEvento AND b.estatus='disponible') disponibles,
        (SELECT COUNT(*) FROM compra c JOIN boleto b USING(idBoleto) WHERE b.idEvento=e.idEvento AND c.estatus='cancelado') cancelaciones,
        (SELECT COALESCE(SUM(c.precioFinal),0) FROM compra c JOIN boleto b USING(idBoleto) WHERE b.idEvento=e.idEvento AND c.estatus='comprado') ingreso
        FROM evento e JOIN promotora p USING(idPromotora) JOIN cuenta_bancaria cb USING(idCuenta_Bancaria)
        WHERE (?=0 OR e.idPromotora=?) ORDER BY e.idEvento
        """;
    try (Connection c = conexion.crearConexion();
        PreparedStatement s = c.prepareStatement(sql)) {
      s.setInt(1, promotora);
      s.setInt(2, promotora);
      try (ResultSet r = s.executeQuery()) {
        List<EventoEntidad> eventos = new ArrayList<>();
        while (r.next()) {
          EventoEntidad e = new EventoEntidad();
          e.setIdEvento(r.getInt("idEvento"));
          e.setIdPromotora(r.getInt("idPromotora"));
          e.setIdCuenta_Bancaria(r.getInt("idCuenta_Bancaria"));
          e.setNombreShow(r.getString("nombreShow"));
          e.setTipoEvento(r.getString("tipoEvento"));
          e.setEdadMinima(r.getInt("edadMinima"));
          e.setImagenPromocional(r.getBytes("imagenPromocional"));
          e.setCantidadBoletos(r.getInt("cantidadBoletos"));
          e.setPrecioBoleto(r.getBigDecimal("precioBoleto"));
          e.setPromotora(r.getString("nombreComercial"));
          e.setCuentaReceptora(r.getString("cuenta"));
          e.setVendidos(r.getInt("vendidos"));
          e.setDisponibles(r.getInt("disponibles"));
          e.setCancelaciones(r.getInt("cancelaciones"));
          e.setIngresoNeto(r.getBigDecimal("ingreso"));
          eventos.add(e);
        }
        return eventos;
      }
    } catch (SQLException ex) {
      throw Errores.convertir(ex);
    }
  }

  @Override
  public EventoEntidad guardar(EventoEntidad e) throws PersistenciaException {
    try (Connection c = conexion.crearConexion();
        Bloqueo bloqueo = new Bloqueo(c)) {
      try (PreparedStatement s =
          c.prepareStatement(
              "SELECT 1 FROM cuenta_promotora WHERE idCuenta_Bancaria=? AND" + " idPromotora=?")) {
        s.setInt(1, e.getIdCuenta_Bancaria());
        s.setInt(2, e.getIdPromotora());
        try (ResultSet r = s.executeQuery()) {
          if (!r.next()) throw new PersistenciaException("Selecciona una cuenta de tu promotora.");
        }
      }
      if (e.getCantidadBoletos() < 1 || e.getCantidadBoletos() > 100000)
        throw new PersistenciaException("La cantidad de boletos debe estar entre 1 y 100,000.");
      boolean crear = e.getIdEvento() == 0;
      int anterior = 0;
      List<Integer> ajustar = new ArrayList<>();
      if (!crear) {
        try (PreparedStatement s =
            c.prepareStatement(
                "SELECT cantidadBoletos,idCuenta_Bancaria FROM evento WHERE"
                    + " idEvento=? AND idPromotora=?")) {
          s.setInt(1, e.getIdEvento());
          s.setInt(2, e.getIdPromotora());
          try (ResultSet r = s.executeQuery()) {
            if (!r.next())
              throw new PersistenciaException("El evento no pertenece a tu promotora.");
            anterior = r.getInt(1);
            if (r.getInt(2) != e.getIdCuenta_Bancaria())
              try (PreparedStatement p =
                  c.prepareStatement(
                      "SELECT 1 FROM compra c JOIN boleto b USING(idBoleto)"
                          + " WHERE b.idEvento=? AND c.estatus='comprado'")) {
                p.setInt(1, e.getIdEvento());
                try (ResultSet activos = p.executeQuery()) {
                  if (activos.next())
                    throw new PersistenciaException(
                        "No se puede cambiar la cuenta receptora mientras"
                            + " existan compras activas.");
                }
              }
          }
        }
      }
      int diferencia = e.getCantidadBoletos() - anterior;
      if (!crear && diferencia != 0) {
        String estado = diferencia < 0 ? "disponible" : "retirado";
        try (PreparedStatement s =
            c.prepareStatement(
                "SELECT b.idBoleto FROM boleto b WHERE b.idEvento=? AND b.estatus=? AND NOT"
                    + " EXISTS(SELECT 1 FROM compra co WHERE co.idBoleto=b.idBoleto AND"
                    + " co.estatus='comprado') ORDER BY b.idBoleto DESC LIMIT ?")) {
          s.setInt(1, e.getIdEvento());
          s.setString(2, estado);
          s.setInt(3, Math.abs(diferencia));
          try (ResultSet r = s.executeQuery()) {
            while (r.next()) ajustar.add(r.getInt(1));
          }
        }
        if (diferencia < 0 && ajustar.size() != -diferencia)
          throw new PersistenciaException(
              "No puedes reducir el inventario por debajo de los boletos vendidos. Solo se pueden"
                  + " retirar boletos disponibles.");
      }
      String sql =
          crear
              ? "INSERT INTO"
                    + " evento(nombreShow,tipoEvento,edadMinima,imagenPromocional,cantidadBoletos,precioBoleto,idCuenta_Bancaria,idPromotora)"
                    + " VALUES(?,?,?,?,?,?,?,?)"
              : "UPDATE evento SET"
                    + " nombreShow=?,tipoEvento=?,edadMinima=?,imagenPromocional=?,cantidadBoletos=?,precioBoleto=?,idCuenta_Bancaria=?"
                    + " WHERE idPromotora=? AND idEvento=?";
      try (PreparedStatement s = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        s.setString(1, e.getNombreShow());
        s.setString(2, e.getTipoEvento());
        s.setInt(3, e.getEdadMinima());
        s.setBytes(4, e.getImagenPromocional());
        s.setInt(5, e.getCantidadBoletos());
        s.setBigDecimal(6, e.getPrecioBoleto());
        s.setInt(7, e.getIdCuenta_Bancaria());
        s.setInt(8, e.getIdPromotora());
        if (!crear) s.setInt(9, e.getIdEvento());
        s.executeUpdate();
        if (crear)
          try (ResultSet r = s.getGeneratedKeys()) {
            r.next();
            e.setIdEvento(r.getInt(1));
          }
      }
      if (!ajustar.isEmpty()) {
        try (PreparedStatement s =
            c.prepareStatement("UPDATE boleto SET estatus=? WHERE idBoleto=?")) {
          for (int id : ajustar) {
            s.setString(1, diferencia < 0 ? "retirado" : "disponible");
            s.setInt(2, id);
            s.addBatch();
          }
          s.executeBatch();
        }
      }
      int nuevos = diferencia > 0 ? diferencia - ajustar.size() : 0;
      if (nuevos > 0)
        try (PreparedStatement s =
            c.prepareStatement("INSERT INTO boleto(idEvento,estatus) VALUES(?,'disponible')")) {
          s.setInt(1, e.getIdEvento());
          for (int i = 0; i < nuevos; i++) s.addBatch();
          s.executeBatch();
        }
      return e;
    } catch (SQLException ex) {
      throw Errores.convertir(ex);
    }
  }
}
