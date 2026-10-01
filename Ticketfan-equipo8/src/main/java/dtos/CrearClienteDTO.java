package dtos;

/** Datos capturados para registrar un cliente. */
public class CrearClienteDTO {
  private String nombres, apPaterno, apMaterno, usuario;
  private char[] contrasena;
  private java.time.LocalDate fechaNacimiento;

  public CrearClienteDTO(
      String nombres,
      String apPaterno,
      String apMaterno,
      java.time.LocalDate fechaNacimiento,
      String usuario,
      char[] contrasena) {
    this.nombres = nombres;
    this.apPaterno = apPaterno;
    this.apMaterno = apMaterno;
    this.fechaNacimiento = fechaNacimiento;
    this.usuario = usuario;
    this.contrasena = contrasena;
  }

  public String getNombres() {
    return nombres;
  }

  public String getApPaterno() {
    return apPaterno;
  }

  public String getApMaterno() {
    return apMaterno;
  }

  public String getUsuario() {
    return usuario;
  }

  public char[] getContrasena() {
    return contrasena;
  }

  public java.time.LocalDate getFechaNacimiento() {
    return fechaNacimiento;
  }
}
