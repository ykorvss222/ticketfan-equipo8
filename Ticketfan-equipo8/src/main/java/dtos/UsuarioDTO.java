package dtos;

/** Datos de usuario en la capa dtos. */
public class UsuarioDTO {
  private int idUsuario;
  private String nombres;
  private String apPaterno;
  private String apMaterno;
  private String usuario;
  private String contrasena;
  private String rol;
  private int idPromotora;
  private String promotora;
  private java.time.LocalDate fechaNacimiento;

  /** Construye un objeto vacío. */
  public UsuarioDTO() {}

  public int getIdUsuario() {
    return idUsuario;
  }

  public void setIdUsuario(int idUsuario) {
    this.idUsuario = idUsuario;
  }

  public String getNombres() {
    return nombres;
  }

  public void setNombres(String nombres) {
    this.nombres = nombres;
  }

  public String getApPaterno() {
    return apPaterno;
  }

  public void setApPaterno(String apPaterno) {
    this.apPaterno = apPaterno;
  }

  public String getApMaterno() {
    return apMaterno;
  }

  public void setApMaterno(String apMaterno) {
    this.apMaterno = apMaterno;
  }

  public String getUsuario() {
    return usuario;
  }

  public void setUsuario(String usuario) {
    this.usuario = usuario;
  }

  public String getContrasena() {
    return contrasena;
  }

  public void setContrasena(String contrasena) {
    this.contrasena = contrasena;
  }

  public String getRol() {
    return rol;
  }

  public void setRol(String rol) {
    this.rol = rol;
  }

  public int getIdPromotora() {
    return idPromotora;
  }

  public void setIdPromotora(int idPromotora) {
    this.idPromotora = idPromotora;
  }

  public String getPromotora() {
    return promotora;
  }

  public void setPromotora(String promotora) {
    this.promotora = promotora;
  }

  public java.time.LocalDate getFechaNacimiento() {
    return fechaNacimiento;
  }

  public void setFechaNacimiento(java.time.LocalDate fechaNacimiento) {
    this.fechaNacimiento = fechaNacimiento;
  }
}
