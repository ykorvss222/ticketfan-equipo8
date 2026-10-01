package negocio.ticketNegocio;

import dtos.*;
import entidad.*;
import java.math.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.time.*;
import java.util.*;
import negocio.*;
import persistencia.*;
import persistencia.compradao.*;
import persistencia.cuentadao.*;
import persistencia.eventodao.*;
import persistencia.usuariodao.*;

/** Reglas de negocio, validación de DTO y entidades. */
public class TicketNegocio implements ITicketNegocio {
  private final IUsuarioDAO usuarioDAO;
  private final IEventoDAO eventoDAO;
  private final ICompraDAO compraDAO;
  private final ICuentaDAO cuentaDAO;

  public TicketNegocio(
      IUsuarioDAO usuarioDAO, IEventoDAO eventoDAO, ICompraDAO compraDAO, ICuentaDAO cuentaDAO) {
    this.usuarioDAO = usuarioDAO;
    this.eventoDAO = eventoDAO;
    this.compraDAO = compraDAO;
    this.cuentaDAO = cuentaDAO;
  }

  private void texto(String valor, String nombre, int max) throws NegocioException {
    if (valor == null || valor.isBlank() || valor.length() > max)
      throw new NegocioException(nombre + " es obligatorio y admite hasta " + max + " caracteres.");
  }

  private void cliente(UsuarioDTO s) throws NegocioException {
    if (s == null || !"CLIENTE".equals(s.getRol()))
      throw new NegocioException("Esta operación requiere una sesión de cliente.");
  }

  private void admin(UsuarioDTO s) throws NegocioException {
    if (s == null || !"ADMIN".equals(s.getRol()) || s.getIdPromotora() <= 0)
      throw new NegocioException("Esta operación requiere un administrador de promotora.");
  }

  /**Hash de los administradores de prueba */
  public static String hash(char[] clave) {
    try {
      return HexFormat.of()
          .formatHex(
              MessageDigest.getInstance("SHA-256")
                  .digest(new String(clave).getBytes(StandardCharsets.UTF_8)));
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  public UsuarioDTO iniciarSesion(String usuario, char[] clave) throws NegocioException {
    texto(usuario, "Usuario", 30);
    if (clave == null || clave.length == 0) throw new NegocioException("Captura tu contraseña.");
    try {
      UsuarioEntidad u = usuarioDAO.autenticar(usuario.trim(), hash(clave));
      if (u == null) throw new NegocioException("Usuario o contraseña incorrectos.");
      return convertir(u);
    } catch (PersistenciaException e) {
      throw new NegocioException(e.getMessage(), e);
    } finally {
      Arrays.fill(clave, '\0');
    }
  }

  @Override
  public UsuarioDTO registrar(CrearClienteDTO d) throws NegocioException {
    texto(d.getNombres(), "Nombres", 50);
    texto(d.getApPaterno(), "Apellido paterno", 30);
    texto(d.getApMaterno(), "Apellido materno", 30);
    texto(d.getUsuario(), "Usuario", 30);
    if (!d.getUsuario().matches("[A-Za-z0-9_.-]{3,30}"))
      throw new NegocioException(
          "El usuario debe tener entre 3 y 30 letras, números, puntos, guiones o guiones"
              + " bajos.");
    if (d.getFechaNacimiento() == null
        || d.getFechaNacimiento().isAfter(LocalDate.now())
        || d.getFechaNacimiento().isBefore(LocalDate.now().minusYears(120)))
      throw new NegocioException("Revisa la fecha de nacimiento.");
    if (d.getContrasena() == null || d.getContrasena().length < 6 || d.getContrasena().length > 128)
      throw new NegocioException("La contraseña debe tener entre 6 y 128 caracteres.");
    
    UsuarioEntidad u = new UsuarioEntidad();
    u.setNombres(d.getNombres().trim());
    u.setApPaterno(d.getApPaterno().trim());
    u.setApMaterno(d.getApMaterno().trim());
    u.setUsuario(d.getUsuario().trim());
    u.setFechaNacimiento(d.getFechaNacimiento());
    u.setContrasena(hash(d.getContrasena()));
    Arrays.fill(d.getContrasena(), '\0');
    
    try {
      return convertir(usuarioDAO.crear(u));
    } catch (PersistenciaException e) {
      throw new NegocioException(e.getMessage(), e);
    }
  }

  @Override
  public List<EventoDTO> eventos(UsuarioDTO s) throws NegocioException {
    if (s == null) throw new NegocioException("Inicia sesión.");
    try {
      List<EventoDTO> lista = new ArrayList<>();
      for (EventoEntidad e : eventoDAO.listar("ADMIN".equals(s.getRol()) ? s.getIdPromotora() : 0))
        lista.add(convertir(e));
      return lista;
    } catch (PersistenciaException e) {
      throw new NegocioException(e.getMessage(), e);
    }
  }

  @Override
  public List<CuentaDTO> cuentas(UsuarioDTO s) throws NegocioException {
    if (s == null) throw new NegocioException("Inicia sesión.");
    boolean a = "ADMIN".equals(s.getRol());
    try {
      List<CuentaDTO> lista = new ArrayList<>();
      for (CuentaEntidad c : cuentaDAO.listar(a ? s.getIdPromotora() : s.getIdUsuario(), a))
        lista.add(convertir(c));
      return lista;
    } catch (PersistenciaException e) {
      throw new NegocioException(e.getMessage(), e);
    }
  }

  @Override
  public EventoDTO guardar(UsuarioDTO s, EventoDTO d) throws NegocioException {
    admin(s);
    texto(d.getNombreShow(), "Nombre del show", 100);
    texto(d.getTipoEvento(), "Tipo de evento", 40);
    if (d.getEdadMinima() < 0 || d.getEdadMinima() > 120)
      throw new NegocioException("La edad mínima debe estar entre 0 y 120 años.");
    if (d.getCantidadBoletos() <= 0 || d.getCantidadBoletos() > 100000)
      throw new NegocioException("La cantidad de boletos debe estar entre 1 y 100,000.");
    if (d.getPrecioBoleto() == null
        || d.getPrecioBoleto().signum() <= 0
        || d.getPrecioBoleto().compareTo(new BigDecimal("99999999.99")) > 0
        || d.getPrecioBoleto().stripTrailingZeros().scale() > 2)
      throw new NegocioException("El precio debe ser positivo, con hasta dos decimales.");
    if (d.getImagenPromocional() == null
        || d.getImagenPromocional().length == 0
        || d.getImagenPromocional().length > 16777215)
      throw new NegocioException("Selecciona una imagen de promoción menor a 16 MB.");
    try {
      if (javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(d.getImagenPromocional()))
          == null) throw new NegocioException("Selecciona una imagen PNG o JPEG válida.");
    } catch (java.io.IOException e) {
      throw new NegocioException("No se pudo leer la imagen.");
    }
    
    EventoEntidad e = new EventoEntidad();
    e.setIdEvento(d.getIdEvento());
    e.setIdPromotora(s.getIdPromotora());
    e.setIdCuenta_Bancaria(d.getIdCuenta_Bancaria());
    e.setNombreShow(d.getNombreShow().trim());
    e.setTipoEvento(d.getTipoEvento().trim());
    e.setEdadMinima(d.getEdadMinima());
    e.setCantidadBoletos(d.getCantidadBoletos());
    e.setPrecioBoleto(d.getPrecioBoleto());
    e.setImagenPromocional(d.getImagenPromocional());
    
    try {
      return convertir(eventoDAO.guardar(e));
    } catch (PersistenciaException ex) {
      throw new NegocioException(ex.getMessage(), ex);
    }
  }

  @Override
  public int comprar(UsuarioDTO s, int evento, int cuenta) throws NegocioException {
    return comprar(s, evento, cuenta, 1).getFirst();
  }

  @Override
  public List<Integer> comprar(UsuarioDTO s, int evento, int cuenta, int cantidad)
      throws NegocioException {
    cliente(s);
    if (cantidad < 1 || cantidad > 100000)
      throw new NegocioException("La cantidad de boletos debe estar entre 1 y 100,000.");
    try {
      return compraDAO.comprar(s.getIdUsuario(), evento, cuenta, cantidad);
    } catch (PersistenciaException e) {
      throw new NegocioException(e.getMessage(), e);
    }
  }

  @Override
  public List<CompraDTO> historial(UsuarioDTO s) throws NegocioException {
    cliente(s);
    try {
      List<CompraDTO> lista = new ArrayList<>();
      for (CompraEntidad c : compraDAO.listar(s.getIdUsuario())) lista.add(convertir(c));
      return lista;
    } catch (PersistenciaException e) {
      throw new NegocioException(e.getMessage(), e);
    }
  }

  @Override
  public void cancelar(UsuarioDTO s, int compra) throws NegocioException {
    cliente(s);
    try {
      compraDAO.cancelar(compra, s.getIdUsuario());
    } catch (PersistenciaException e) {
      throw new NegocioException(e.getMessage(), e);
    }
  }

  @Override
  public CompraDTO boleto(UsuarioDTO s, int compra) throws NegocioException {
    for (CompraDTO c : historial(s))
      if (c.getIdCompra() == compra) {
        if (!"comprado".equals(c.getEstatus()))
          throw new NegocioException("Solo las compras activas tienen un boleto válido.");
        return c;
      }
    throw new NegocioException("La compra no pertenece a tu perfil.");
  }

  private UsuarioDTO convertir(UsuarioEntidad origen) {
    UsuarioDTO destino = new UsuarioDTO();
    
    destino.setIdUsuario(origen.getIdUsuario());
    destino.setNombres(origen.getNombres());
    destino.setApPaterno(origen.getApPaterno());
    destino.setApMaterno(origen.getApMaterno());
    destino.setUsuario(origen.getUsuario());
    destino.setRol(origen.getRol());
    destino.setIdPromotora(origen.getIdPromotora());
    destino.setPromotora(origen.getPromotora());
    destino.setFechaNacimiento(origen.getFechaNacimiento());
    
    return destino;
  }

  private CuentaDTO convertir(CuentaEntidad origen) {
    CuentaDTO destino = new CuentaDTO();
    
    destino.setIdCuenta_Bancaria(origen.getIdCuenta_Bancaria());
    destino.setBanco(origen.getBanco());
    destino.setNumeroCuenta(origen.getNumeroCuenta());
    destino.setSaldo(origen.getSaldo());
    
    return destino;
  }

  private EventoDTO convertir(EventoEntidad origen) {
    EventoDTO destino = new EventoDTO();
    
    destino.setIdEvento(origen.getIdEvento());
    destino.setIdPromotora(origen.getIdPromotora());
    destino.setIdCuenta_Bancaria(origen.getIdCuenta_Bancaria());
    destino.setNombreShow(origen.getNombreShow());
    destino.setTipoEvento(origen.getTipoEvento());
    destino.setEdadMinima(origen.getEdadMinima());
    destino.setImagenPromocional(origen.getImagenPromocional());
    destino.setCantidadBoletos(origen.getCantidadBoletos());
    destino.setPrecioBoleto(origen.getPrecioBoleto());
    destino.setPromotora(origen.getPromotora());
    destino.setCuentaReceptora(origen.getCuentaReceptora());
    destino.setVendidos(origen.getVendidos());
    destino.setDisponibles(origen.getDisponibles());
    destino.setCancelaciones(origen.getCancelaciones());
    destino.setIngresoNeto(origen.getIngresoNeto());
    
    return destino;
  }

  private CompraDTO convertir(CompraEntidad origen) {
    CompraDTO destino = new CompraDTO();
    
    destino.setIdCompra(origen.getIdCompra());
    destino.setIdCliente(origen.getIdCliente());
    destino.setIdBoleto(origen.getIdBoleto());
    destino.setIdCuenta_Bancaria(origen.getIdCuenta_Bancaria());
    destino.setPrecioFinal(origen.getPrecioFinal());
    destino.setHoraCompra(origen.getHoraCompra());
    destino.setHoraCancelacion(origen.getHoraCancelacion());
    destino.setEstatus(origen.getEstatus());
    destino.setEvento(origen.getEvento());
    destino.setCliente(origen.getCliente());
    destino.setPromotora(origen.getPromotora());
    destino.setCuenta(origen.getCuenta());
    
    return destino;
  }
}
