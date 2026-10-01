package negocio.ticketNegocio;

import dtos.CompraDTO;
import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** Boleto de una compra activa, que hace uso de la API de Apache PDFBox 3. */
public final class BoletoPDF {
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  private BoletoPDF() {}

  /** Genera el PDF: negocio comprueba previamente que el boleto pertenezca al cliente. */
  public static Path generar(CompraDTO compra, Path destino) throws IOException {
    if (compra == null || destino == null)
      throw new IOException("Selecciona un boleto y una ruta para guardar el PDF.");
    
    if (!"comprado".equals(compra.getEstatus()))
      throw new IOException("Solo puedes generar boletos de compras activas.");
    
    if (compra.getHoraCompra() == null || compra.getPrecioFinal() == null)
      throw new IOException("El boleto no tiene los datos de compra completos.");
    
    if (System.getProperty("pdfbox.fontcache") == null)
      System.setProperty("pdfbox.fontcache", System.getProperty("java.io.tmpdir"));
    
    PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    PDFont negrita = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    Path salida = destino.toAbsolutePath();
    
    if (salida.getParent() != null) Files.createDirectories(salida.getParent());
    
    try (PDDocument documento = new PDDocument()) {
      PDPage pagina = new PDPage(PDRectangle.LETTER);
      documento.addPage(pagina);
      try (PDPageContentStream contenido = new PDPageContentStream(documento, pagina)) {
        contenido.setNonStrokingColor(new Color(246, 246, 252));
        contenido.addRect(0, 0, 612, 792);
        contenido.fill();
        contenido.setNonStrokingColor(new Color(91, 71, 186));
        contenido.addRect(32, 624, 548, 136);
        contenido.fill();
        texto(contenido, negrita, 30, 55, 710, Color.WHITE, "TicketFan");
        texto(contenido, normal, 12, 55, 682, Color.WHITE, "TU ACCESO A UNA GRAN EXPERIENCIA");
        texto(
            contenido,
            negrita,
            17,
            55,
            647,
            Color.WHITE,
            "BOLETO #" + compra.getIdBoleto() + "  |  COMPRA #" + compra.getIdCompra());
        contenido.setNonStrokingColor(Color.WHITE);
        contenido.addRect(32, 136, 548, 474);
        contenido.fill();
        float y = 580;
        y = campo(contenido, negrita, normal, y, "EVENTO", compra.getEvento());
        y = campo(contenido, negrita, normal, y, "CLIENTE", compra.getCliente());
        y = campo(contenido, negrita, normal, y, "PROMOTORA", compra.getPromotora());
        y =
            campo(
                contenido,
                negrita,
                normal,
                y,
                "FECHA DE COMPRA",
                compra.getHoraCompra().format(FECHA));
        y = campo(contenido, negrita, normal, y, "CUENTA DE PAGO", compra.getCuenta());
        y =
            campo(
                contenido,
                negrita,
                normal,
                y,
                "PRECIO PAGADO",
                "$" + compra.getPrecioFinal().toPlainString() + " MXN");
        try (java.io.InputStream qr =
            BoletoPDF.class.getResourceAsStream("/imagenes/qr-demo.png")) {
          if (qr == null)
            throw new IOException("No se encontró el QR de demostración incluido en el proyecto.");
          org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject imagen =
              org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject.createFromByteArray(
                  documento, qr.readAllBytes(), "QR demostración");
          contenido.drawImage(imagen, 446, 174, 108, 108);
        }
        texto(contenido, normal, 8, 440, 160, new Color(105, 109, 129), "QR de demostración");
        texto(contenido, negrita, 12, 55, 156, new Color(91, 71, 186), "ESTATUS: COMPRADO");
        contenido.setStrokingColor(new Color(178, 172, 211));
        contenido.setLineDashPattern(new float[] {4, 4}, 0);
        contenido.moveTo(40, 119);
        contenido.lineTo(572, 119);
        contenido.stroke();
        texto(
            contenido,
            normal,
            10,
            55,
            94,
            new Color(71, 76, 100),
            "Cancelación con reembolso íntegro hasta:");
        texto(
            contenido,
            negrita,
            11,
            55,
            77,
            new Color(71, 76, 100),
            compra.getHoraCompra().plusHours(24).format(FECHA));
        texto(
            contenido,
            normal,
            9,
            55,
            53,
            new Color(105, 109, 129),
            "Conserva este boleto. Su vigencia se verifica en TicketFan.");
      }
      documento.save(salida.toFile());
    }
    return salida;
  }

  private static float campo(
      PDPageContentStream c, PDFont negrita, PDFont normal, float y, String etiqueta, String valor)
      throws IOException {
    texto(c, negrita, 9, 55, y, new Color(113, 109, 143), etiqueta);
    y -= 19;
    for (String linea : lineas(normal, 12, seguro(normal, valor), 495)) {
      texto(c, normal, 12, 55, y, new Color(36, 41, 69), linea);
      y -= 16;
    }
    return y - 17;
  }

  private static List<String> lineas(PDFont fuente, float tamano, String texto, float ancho)
      throws IOException {
    List<String> resultado = new ArrayList<>();
    StringBuilder actual = new StringBuilder();
    
    for (int i = 0; i < texto.length(); i++) {
      String siguiente = actual.toString() + texto.charAt(i);
      if (!actual.isEmpty() && fuente.getStringWidth(siguiente) / 1000 * tamano > ancho) {
        int espacio = actual.lastIndexOf(" ");
        if (espacio > 0) {
          resultado.add(actual.substring(0, espacio));
          String resto = actual.substring(espacio + 1);
          actual.setLength(0);
          actual.append(resto);
        } else {
          resultado.add(actual.toString());
          actual.setLength(0);
        }
      }
      actual.append(texto.charAt(i));
    }
    resultado.add(actual.toString());
    
    return resultado;
  }

  private static String seguro(PDFont fuente, String valor) throws IOException {
    if (valor == null) return "Sin dato";
    StringBuilder resultado = new StringBuilder();
    
    for (char ch : valor.replace('\n', ' ').replace('\r', ' ').toCharArray()) {
      try {
        fuente.encode(String.valueOf(ch));
        resultado.append(ch);
      } catch (IllegalArgumentException e) {
        resultado.append('?');
      }
    }
    return resultado.toString();
  }

  private static void texto(
      PDPageContentStream c,
      PDFont fuente,
      float tamano,
      float x,
      float y,
      Color color,
      String valor)
      throws IOException {
    c.beginText();
    c.setFont(fuente, tamano);
    c.setNonStrokingColor(color);
    c.newLineAtOffset(x, y);
    c.showText(seguro(fuente, valor));
    c.endText();
  }
}
