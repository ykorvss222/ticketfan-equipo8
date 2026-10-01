package dtos;

/** Datos de compra en la capa dtos. */
public class CompraDTO {
  private int idCompra;
  private int idCliente;
  private int idBoleto;
  private int idCuenta_Bancaria;
  private java.math.BigDecimal precioFinal;
  private java.time.LocalDateTime horaCompra;
  private java.time.LocalDateTime horaCancelacion;
  private String estatus;
  private String evento;
  private String cliente;
  private String promotora;
  private String cuenta;

  /** Construye un objeto vacío. */
  public CompraDTO() {}

  public int getIdCompra() {
    return idCompra;
  }

  public void setIdCompra(int idCompra) {
    this.idCompra = idCompra;
  }

  public int getIdCliente() {
    return idCliente;
  }

  public void setIdCliente(int idCliente) {
    this.idCliente = idCliente;
  }

  public int getIdBoleto() {
    return idBoleto;
  }

  public void setIdBoleto(int idBoleto) {
    this.idBoleto = idBoleto;
  }

  public int getIdCuenta_Bancaria() {
    return idCuenta_Bancaria;
  }

  public void setIdCuenta_Bancaria(int idCuenta_Bancaria) {
    this.idCuenta_Bancaria = idCuenta_Bancaria;
  }

  public java.math.BigDecimal getPrecioFinal() {
    return precioFinal;
  }

  public void setPrecioFinal(java.math.BigDecimal precioFinal) {
    this.precioFinal = precioFinal;
  }

  public java.time.LocalDateTime getHoraCompra() {
    return horaCompra;
  }

  public void setHoraCompra(java.time.LocalDateTime horaCompra) {
    this.horaCompra = horaCompra;
  }

  public java.time.LocalDateTime getHoraCancelacion() {
    return horaCancelacion;
  }

  public void setHoraCancelacion(java.time.LocalDateTime horaCancelacion) {
    this.horaCancelacion = horaCancelacion;
  }

  public String getEstatus() {
    return estatus;
  }

  public void setEstatus(String estatus) {
    this.estatus = estatus;
  }

  public String getEvento() {
    return evento;
  }

  public void setEvento(String evento) {
    this.evento = evento;
  }

  public String getCliente() {
    return cliente;
  }

  public void setCliente(String cliente) {
    this.cliente = cliente;
  }

  public String getPromotora() {
    return promotora;
  }

  public void setPromotora(String promotora) {
    this.promotora = promotora;
  }

  public String getCuenta() {
    return cuenta;
  }

  public void setCuenta(String cuenta) {
    this.cuenta = cuenta;
  }
}
