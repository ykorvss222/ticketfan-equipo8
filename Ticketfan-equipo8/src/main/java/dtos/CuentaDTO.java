package dtos;

/** Datos de cuenta en la capa dtos. */
public class CuentaDTO {
  private int idCuenta_Bancaria;
  private String banco;
  private String numeroCuenta;
  private java.math.BigDecimal saldo;

  /** Construye un objeto vacío. */
  public CuentaDTO() {}

  public int getIdCuenta_Bancaria() {
    return idCuenta_Bancaria;
  }

  public void setIdCuenta_Bancaria(int idCuenta_Bancaria) {
    this.idCuenta_Bancaria = idCuenta_Bancaria;
  }

  public String getBanco() {
    return banco;
  }

  public void setBanco(String banco) {
    this.banco = banco;
  }

  public String getNumeroCuenta() {
    return numeroCuenta;
  }

  public void setNumeroCuenta(String numeroCuenta) {
    this.numeroCuenta = numeroCuenta;
  }

  public java.math.BigDecimal getSaldo() {
    return saldo;
  }

  public void setSaldo(java.math.BigDecimal saldo) {
    this.saldo = saldo;
  }

  @Override
  public String toString() {
    return banco + " · " + numeroCuenta + " · $" + saldo + " MXN";
  }
}
