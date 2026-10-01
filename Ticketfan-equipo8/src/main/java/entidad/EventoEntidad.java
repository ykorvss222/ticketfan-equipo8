package entidad;

/** Datos de evento en la capa entidad. */
public class EventoEntidad {
  private int idEvento;
  private int idPromotora;
  private int idCuenta_Bancaria;
  private String nombreShow;
  private String tipoEvento;
  private int edadMinima;
  private byte[] imagenPromocional;
  private int cantidadBoletos;
  private java.math.BigDecimal precioBoleto;
  private String promotora;
  private String cuentaReceptora;
  private int vendidos;
  private int disponibles;
  private int cancelaciones;
  private java.math.BigDecimal ingresoNeto;

  /** Construye un objeto vacío. */
  public EventoEntidad() {}

  public int getIdEvento() {
    return idEvento;
  }

  public void setIdEvento(int idEvento) {
    this.idEvento = idEvento;
  }

  public int getIdPromotora() {
    return idPromotora;
  }

  public void setIdPromotora(int idPromotora) {
    this.idPromotora = idPromotora;
  }

  public int getIdCuenta_Bancaria() {
    return idCuenta_Bancaria;
  }

  public void setIdCuenta_Bancaria(int idCuenta_Bancaria) {
    this.idCuenta_Bancaria = idCuenta_Bancaria;
  }

  public String getNombreShow() {
    return nombreShow;
  }

  public void setNombreShow(String nombreShow) {
    this.nombreShow = nombreShow;
  }

  public String getTipoEvento() {
    return tipoEvento;
  }

  public void setTipoEvento(String tipoEvento) {
    this.tipoEvento = tipoEvento;
  }

  public int getEdadMinima() {
    return edadMinima;
  }

  public void setEdadMinima(int edadMinima) {
    this.edadMinima = edadMinima;
  }

  public byte[] getImagenPromocional() {
    return imagenPromocional;
  }

  public void setImagenPromocional(byte[] imagenPromocional) {
    this.imagenPromocional = imagenPromocional;
  }

  public int getCantidadBoletos() {
    return cantidadBoletos;
  }

  public void setCantidadBoletos(int cantidadBoletos) {
    this.cantidadBoletos = cantidadBoletos;
  }

  public java.math.BigDecimal getPrecioBoleto() {
    return precioBoleto;
  }

  public void setPrecioBoleto(java.math.BigDecimal precioBoleto) {
    this.precioBoleto = precioBoleto;
  }

  public String getPromotora() {
    return promotora;
  }

  public void setPromotora(String promotora) {
    this.promotora = promotora;
  }

  public String getCuentaReceptora() {
    return cuentaReceptora;
  }

  public void setCuentaReceptora(String cuentaReceptora) {
    this.cuentaReceptora = cuentaReceptora;
  }

  public int getVendidos() {
    return vendidos;
  }

  public void setVendidos(int vendidos) {
    this.vendidos = vendidos;
  }

  public int getDisponibles() {
    return disponibles;
  }

  public void setDisponibles(int disponibles) {
    this.disponibles = disponibles;
  }

  public int getCancelaciones() {
    return cancelaciones;
  }

  public void setCancelaciones(int cancelaciones) {
    this.cancelaciones = cancelaciones;
  }

  public java.math.BigDecimal getIngresoNeto() {
    return ingresoNeto;
  }

  public void setIngresoNeto(java.math.BigDecimal ingresoNeto) {
    this.ingresoNeto = ingresoNeto;
  }
}
