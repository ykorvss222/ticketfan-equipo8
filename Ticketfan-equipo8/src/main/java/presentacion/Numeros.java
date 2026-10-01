package presentacion;

import java.math.BigDecimal;
import java.math.BigInteger;
import negocio.NegocioException;

public final class Numeros {
  private Numeros() {}

  public static int entero(String texto, String campo, int minimo, int maximo)
      throws NegocioException {
    String valor = texto == null ? "" : texto.trim();
    if (!valor.matches("[0-9]+"))
      throw new NegocioException(
          campo
              + ": escribe un número entero entre "
              + minimo
              + " y "
              + String.format(java.util.Locale.US, "%,d", maximo)
              + ", sin comas ni decimales.");
    BigInteger numero = new BigInteger(valor);
    if (numero.compareTo(BigInteger.valueOf(minimo)) < 0
        || numero.compareTo(BigInteger.valueOf(maximo)) > 0)
      throw new NegocioException(
          campo
              + " debe estar entre "
              + minimo
              + " y "
              + String.format(java.util.Locale.US, "%,d", maximo)
              + ".");
    return numero.intValue();
  }

  public static BigDecimal precio(String texto) throws NegocioException {
    try {
      if (texto == null || !texto.trim().matches("[0-9]+(?:\\.[0-9]{1,2})?"))
        throw new NumberFormatException();
      return new BigDecimal(texto.trim());
    } catch (NumberFormatException e) {
      throw new NegocioException(
          "Precio por boleto: escribe un importe positivo con hasta dos decimales; usa punto"
              + " decimal.");
    }
  }
}
