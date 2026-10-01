package negocio.ticketNegocio;

import dtos.*;
import java.util.List;
import negocio.NegocioException;

/** Operaciones públicas de TicketFan. */
public interface ITicketNegocio {
  /** Autentica el usuario mediante hash SHA-256 e identifica el tipo de usuario. */
  UsuarioDTO iniciarSesion(String usuario, char[] contrasena) throws NegocioException;

  /** Registra un cliente y sus tres cuentas de prueba con saldo inicial. */
  UsuarioDTO registrar(CrearClienteDTO cliente) throws NegocioException;

  /** Consulta inventario y ventas, restringe al administrador a su promotora. */
  List<EventoDTO> eventos(UsuarioDTO sesion) throws NegocioException;

  /** Consulta solo a las cuentas asociadas a la sesión. */
  List<CuentaDTO> cuentas(UsuarioDTO sesion) throws NegocioException;

  /** Valida y crea o modifica un evento ajustando únicamente boletos disponibles. */
  EventoDTO guardar(UsuarioDTO sesion, EventoDTO evento) throws NegocioException;

  /** Compra un boleto disponible con una cuenta propia y devuelve el ID de compra. */
  int comprar(UsuarioDTO sesion, int evento, int cuenta) throws NegocioException;

  /** Compra la cantidad solicitada y devuelve un ID de compra por entrada. */
  List<Integer> comprar(UsuarioDTO sesion, int evento, int cuenta, int cantidad)
      throws NegocioException;

  /** Consulta las compras activas y canceladas del cliente. */
  List<CompraDTO> historial(UsuarioDTO sesion) throws NegocioException;

  /** Solicita a MySQL la cancelación y reembolso. */
  void cancelar(UsuarioDTO sesion, int compra) throws NegocioException;

  /** Obtiene una compra propia activa para generar su boleto PDF. */
  CompraDTO boleto(UsuarioDTO sesion, int compra) throws NegocioException;
}
