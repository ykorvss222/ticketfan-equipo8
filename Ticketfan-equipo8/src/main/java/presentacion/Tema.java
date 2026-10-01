package presentacion;

import java.awt.*;
import java.text.NumberFormat;
import java.util.Locale;
import javax.swing.*;
import javax.swing.border.*;

public final class Tema {
  public static final Color MORADO = new Color(91, 71, 186);
  public static final Color TEXTO = new Color(36, 41, 69);
  public static final Color FONDO = new Color(244, 245, 252);

  private Tema() {}

  /** Tema FlatLaf a los componentes */
  public static void instalarEstilo() {
    if (UIManager.getLookAndFeel() instanceof com.formdev.flatlaf.FlatLightLaf) return;
    com.formdev.flatlaf.FlatLightLaf.setup();
    UIManager.put("defaultFont", new Font("Segoe UI", Font.PLAIN, 14));
    UIManager.put("Component.arc", 12);
    UIManager.put("Button.arc", 12);
    UIManager.put("TextComponent.arc", 10);
    UIManager.put("Component.focusColor", MORADO);
    UIManager.put("TableHeader.background", new Color(232, 228, 251));
    UIManager.put("TableHeader.foreground", MORADO);
    UIManager.put("TitlePane.background", MORADO);
    UIManager.put("TitlePane.foreground", Color.WHITE);
    UIManager.put("TitlePane.inactiveBackground", new Color(122, 105, 198));
    UIManager.put("TitlePane.inactiveForeground", Color.WHITE);
    UIManager.put("OptionPane.yesButtonText", "Sí");
    UIManager.put("OptionPane.noButtonText", "No");
    UIManager.put("OptionPane.cancelButtonText", "Cancelar");
    UIManager.put("FileChooser.saveButtonText", "Guardar");
    UIManager.put("FileChooser.cancelButtonText", "Cancelar");
  }

  public static String dinero(java.math.BigDecimal valor) {
    return NumberFormat.getCurrencyInstance(Locale.forLanguageTag("es-MX")).format(valor) + " MXN";
  }

  public static String html(String texto) {
    return texto == null
        ? ""
        : texto
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;");
  }

  public static void ventana(JFrame ventana, String titulo) {
    if (!(UIManager.getLookAndFeel() instanceof com.formdev.flatlaf.FlatLightLaf)) {
      instalarEstilo();
      SwingUtilities.updateComponentTreeUI(ventana);
    }
    ventana.setTitle("TicketFan | " + titulo);
    ventana.getRootPane().putClientProperty("JRootPane.titleBarBackground", MORADO);
    ventana.getRootPane().putClientProperty("JRootPane.titleBarForeground", Color.WHITE);
    ventana.getRootPane().putClientProperty("JRootPane.titleBarShowIcon", false);
    ventana.setResizable(false);
    ventana.getContentPane().setBackground(FONDO);
    estilo(ventana.getContentPane());
    ventana.setLocationRelativeTo(null);
  }

  private static void estilo(Container contenedor) {
    for (Component c : contenedor.getComponents()) {
      c.setFont(new Font("Segoe UI", Font.PLAIN, 14));
      c.setForeground(TEXTO);
      if (c instanceof JButton b
          && !(b.getParent() instanceof JComboBox)
          && !(b.getParent() instanceof JScrollBar)) {
        b.setOpaque(false);
        b.setMargin(new Insets(8, 12, 8, 12));
        b.setFont(new Font("Segoe UI", Font.BOLD, 14));
        b.setBackground(MORADO);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      }
      if (c instanceof JTable t) {
        t.setRowHeight(42);
        t.setShowVerticalLines(false);
        t.setGridColor(new Color(233, 235, 247));
        t.setSelectionBackground(new Color(226, 220, 252));
        t.setSelectionForeground(TEXTO);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        t.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        t.getTableHeader().setPreferredSize(new Dimension(0, 38));
        t.setFillsViewportHeight(true);
      }
      if (c instanceof Container hijo) estilo(hijo);
    }
  }

  public static void campo(JTextField campo, String titulo) {
    campo.setText("");
    TitledBorder borde =
        BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(207, 209, 228)), titulo);
    borde.setTitleFont(new Font("Segoe UI", Font.PLAIN, 11));
    borde.setTitleColor(new Color(91, 94, 118));
    campo.setBorder(borde);
    campo.setToolTipText(titulo);
  }

  public static void tarjeta(JPanel panel) {
    panel.setBackground(Color.WHITE);
    panel.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
    if (panel.getLayout() instanceof GridLayout grid) grid.setVgap(4);
  }

  public static void marca(JLabel etiqueta, String mensaje) {
    etiqueta.setOpaque(true);
    etiqueta.setBackground(MORADO);
    etiqueta.setForeground(Color.WHITE);
    etiqueta.setVerticalAlignment(SwingConstants.CENTER);
    etiqueta.setBorder(BorderFactory.createEmptyBorder(28, 28, 28, 28));
    etiqueta.setText(
        "<html><span style='font-size:30px'>TicketFan</span><br><br><span style='font-size:18px'>"
            + mensaje
            + "</span><br><br><span style='font-size:12px'>Tus eventos. Tus boletos.<br>Todo en un"
            + " solo lugar.</span></html>");
  }

  public static void secundario(JButton boton) {
    boton.setBackground(new Color(232, 228, 251));
    boton.setForeground(MORADO);
  }

  public static void imagen(JLabel etiqueta, byte[] datos, int ancho, int alto)
      throws java.io.IOException {
    etiqueta.setText("");
    etiqueta.setIcon(null);
    etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
    if (datos == null) {
      etiqueta.setText("Selecciona una imagen promocional");
      return;
    }
    java.awt.image.BufferedImage imagen =
        javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(datos));
    if (imagen == null) throw new java.io.IOException("La imagen no es válida.");
    double escala =
        Math.min((double) ancho / imagen.getWidth(), (double) alto / imagen.getHeight());
    etiqueta.setIcon(
        new ImageIcon(
            imagen.getScaledInstance(
                Math.max(1, (int) (imagen.getWidth() * escala)),
                Math.max(1, (int) (imagen.getHeight() * escala)),
                Image.SCALE_SMOOTH)));
  }

  public static void abrir(JFrame actual, JFrame siguiente) {
    siguiente.setLocationRelativeTo(actual);
    siguiente.setVisible(true);
    actual.dispose();
  }

  public static void mensaje(Component padre, String texto) {
    JOptionPane.showMessageDialog(padre, texto, "TicketFan", JOptionPane.INFORMATION_MESSAGE);
  }

  @FunctionalInterface
  public interface Accion {
    void ejecutar() throws Exception;
  }

  public static void ejecutar(JFrame ventana, Accion accion) {
    ventana.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
    try {
      accion.ejecutar();
    } catch (negocio.NegocioException e) {
      error(ventana, e.getMessage());
    } catch (java.time.format.DateTimeParseException e) {
      error(ventana, "Escribe una fecha válida con formato dd/MM/aaaa.");
    } catch (NumberFormatException e) {
      error(ventana, "Revisa el valor numérico del campo indicado.");
    } catch (Exception e) {
      java.util.logging.Logger.getLogger("TicketFan")
          .log(java.util.logging.Level.WARNING, "Operación de interfaz", e);
      error(ventana, "No se pudo completar la operación. " + e.getMessage());
    } finally {
      ventana.setCursor(Cursor.getDefaultCursor());
    }
  }

  public static void error(Component padre, String texto) {
    JOptionPane.showMessageDialog(padre, texto, "TicketFan", JOptionPane.ERROR_MESSAGE);
  }
}
