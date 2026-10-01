package presentacion;

import dtos.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;
import negocio.NegocioException;
import negocio.ticketNegocio.*;

final class ListaControlador {
  enum Vista {
    ADMIN,
    CATALOGO,
    HISTORIAL,
    CUENTAS,
    GENERAL,
    DETALLE
  }

  private final JFrame ventana;
  private final ITicketNegocio negocio;
  private final UsuarioDTO sesion;
  private final Vista vista;
  private final JTable tabla;
  private JTable tablaCuentas;
  private JTabbedPane pestanas;
  private final JLabel estado, tarjeta;
  private final JTextField filtro;
  private final JButton principal, secundario;
  private List<EventoDTO> eventos = List.of();
  private List<CompraDTO> compras = List.of();
  private final int idEvento;
  private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");

  ListaControlador(
      JFrame ventana,
      ITicketNegocio negocio,
      UsuarioDTO sesion,
      Vista vista,
      int idEvento,
      JTable tabla,
      JLabel titulo,
      JLabel estado,
      JLabel tarjeta,
      JTextField filtro,
      JComboBox<String> menu,
      JButton principal,
      JButton secundario,
      JButton actualizar) {
    this.ventana = ventana;
    this.negocio = negocio;
    this.sesion = sesion;
    this.vista = vista;
    this.idEvento = idEvento;
    this.tabla = tabla;
    this.estado = estado;
    this.tarjeta = tarjeta;
    this.filtro = filtro;
    this.principal = principal;
    this.secundario = secundario;
    String nombre =
        switch (vista) {
          case ADMIN -> "Mis eventos";
          case CATALOGO -> "Catálogo";
          case HISTORIAL -> "Mis compras";
          case CUENTAS -> "Mis cuentas";
          case GENERAL -> "Dashboard general";
          case DETALLE -> "Dashboard del evento";
        };
    Tema.ventana(ventana, nombre);
    titulo.setText("TicketFan  /  " + nombre);
    titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
    titulo.setForeground(Tema.MORADO);
    filtro.setText("");
    filtro.setToolTipText("Buscar por nombre o tipo de evento");
    filtro.setUI(
        new javax.swing.plaf.basic.BasicTextFieldUI() {
          @Override
          protected void paintSafely(java.awt.Graphics g) {
            super.paintSafely(g);
            if (filtro.getText().isEmpty() && !filtro.hasFocus()) {
              g.setColor(new Color(125, 128, 149));
              g.drawString(
                  "Buscar evento · Enter",
                  6,
                  (filtro.getHeight()
                          + g.getFontMetrics().getAscent()
                          - g.getFontMetrics().getDescent())
                      / 2);
            }
          }
        });
    filtro.setVisible(vista == Vista.ADMIN || vista == Vista.CATALOGO);
    filtro.addActionListener(e -> Tema.ejecutar(ventana, this::cargar));
    tarjeta.setOpaque(true);
    tarjeta.setBackground(Color.WHITE);
    tarjeta.setBorder(BorderFactory.createEmptyBorder(16, 14, 16, 14));
    tarjeta.setVerticalAlignment(SwingConstants.CENTER);
    tarjeta.setHorizontalAlignment(SwingConstants.CENTER);
    estado.setFont(new Font("Segoe UI", Font.PLAIN, 12));
    Navegacion.menu(
        ventana, menu, negocio, sesion, vista == Vista.DETALLE ? "Mis eventos" : nombre);
    principal.setText(
        switch (vista) {
          case ADMIN -> "Nuevo evento";
          case CATALOGO -> "Comprar boleto";
          case HISTORIAL -> "Descargar PDF";
          case DETALLE -> "Editar evento";
          case GENERAL -> "Ver cuentas";
          case CUENTAS -> "ADMIN".equals(sesion.getRol()) ? "Mis eventos" : "Catálogo";
          default -> "Actualizar";
        });
    secundario.setText(
        switch (vista) {
          case ADMIN -> "Editar evento";
          case CATALOGO -> "Ver detalles";
          case HISTORIAL -> "Cancelar compra";
          case GENERAL -> "Ver dashboard";
          case CUENTAS -> "Cerrar sesión";
          default -> "Volver";
        });
    Tema.secundario(secundario);
    Tema.secundario(actualizar);
    actualizar.setText("Actualizar");
    principal.addActionListener(e -> Tema.ejecutar(ventana, this::accion));
    secundario.addActionListener(e -> Tema.ejecutar(ventana, this::segunda));
    actualizar.addActionListener(e -> Tema.ejecutar(ventana, this::cargar));
    tabla
        .getSelectionModel()
        .addListSelectionListener(
            e -> {
              if (!e.getValueIsAdjusting()) seleccion();
            });
    tabla.addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2)
              Tema.ejecutar(
                  ventana,
                  () -> {
                    if (vista == Vista.ADMIN || vista == Vista.GENERAL) detalle();
                    else if (vista == Vista.CATALOGO) accion();
                  });
          }
        });
    if (sesion != null) Tema.ejecutar(ventana, this::cargar);
    else {
      principal.setEnabled(false);
      secundario.setEnabled(false);
      actualizar.setEnabled(false);
    }
    ventana.pack();
    ventana.setLocationRelativeTo(null);
  }

  void pestanas(JTabbedPane panel, JTable cuentas) {
    this.pestanas = panel;
    this.tablaCuentas = cuentas;
    panel.setTitleAt(0, "Eventos");
    panel.setTitleAt(1, "Cuentas bancarias");
    panel.setSelectedIndex(0);
    cuentas.setRowHeight(34);
    cuentas.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    cuentas.getTableHeader().setReorderingAllowed(false);
    panel.addChangeListener(
        e -> {
          boolean eventos = panel.getSelectedIndex() == 0;
          principal.setText(eventos ? "Ver cuentas" : "Ver eventos");
          secundario.setEnabled(eventos && tabla.getRowCount() > 0);
        });
    Tema.ejecutar(ventana, this::cargar);
  }

  private void modelo(List<Object[]> filas, String... columnas) {
    tabla.setModel(
        new DefaultTableModel(filas.toArray(Object[][]::new), columnas) {
          @Override
          public boolean isCellEditable(int r, int c) {
            return false;
          }
        });
    tabla.setAutoCreateRowSorter(vista != Vista.DETALLE);
    if (!filas.isEmpty()) tabla.setRowSelectionInterval(0, 0);
    if (columnas.length >= 6) {
      tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
      int[] widths =
          vista == Vista.HISTORIAL
              ? new int[] {55, 55, 190, 130, 120, 95, 130}
              : vista == Vista.GENERAL
                  ? new int[] {50, 210, 60, 75, 95, 130, 155}
                  : new int[] {45, 235, 110, 70, 110, 95, 190};
      for (int i = 0; i < columnas.length; i++)
        tabla.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
    } else tabla.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
  }

  private int id() throws NegocioException {
    int fila = tabla.getSelectedRow();
    if (fila < 0) throw new NegocioException("Selecciona una fila para continuar.");
    return Integer.parseInt(
        tabla.getModel().getValueAt(tabla.convertRowIndexToModel(fila), 0).toString());
  }

  private EventoDTO evento() throws NegocioException {
    int id = vista == Vista.DETALLE ? idEvento : id();
    return eventos.stream()
        .filter(e -> e.getIdEvento() == id)
        .findFirst()
        .orElseThrow(() -> new NegocioException("Selecciona un evento disponible."));
  }

  private void cargar() throws Exception {
    tarjeta.setIcon(null);
    switch (vista) {
      case ADMIN, CATALOGO -> {
        eventos = negocio.eventos(sesion);
        List<Object[]> filas = new ArrayList<>();
        String texto = filtro.getText().trim().toLowerCase(java.util.Locale.ROOT);
        for (EventoDTO e : eventos)
          if ((vista == Vista.ADMIN || e.getDisponibles() > 0)
              && (e.getNombreShow() + " " + e.getTipoEvento())
                  .toLowerCase(java.util.Locale.ROOT)
                  .contains(texto))
            filas.add(
                new Object[] {
                  e.getIdEvento(),
                  e.getNombreShow(),
                  e.getTipoEvento(),
                  e.getEdadMinima() + "+",
                  Tema.dinero(e.getPrecioBoleto()),
                  e.getDisponibles(),
                  e.getPromotora()
                });
        modelo(filas, "ID", "Evento", "Tipo", "Edad", "Precio", "Disponibles", "Promotora");
        estado.setText(
            filas.size()
                + " eventos · Buscar: escribe arriba y presiona Enter · "
                + (vista == Vista.ADMIN ? sesion.getPromotora() : "Compra con una de tus cuentas"));
        if (filas.isEmpty()) tarjeta("Sin resultados", "Prueba otro nombre o registra un evento.");
      }
      case HISTORIAL -> {
        compras = negocio.historial(sesion);
        List<Object[]> filas = new ArrayList<>();
        for (CompraDTO c : compras)
          filas.add(
              new Object[] {
                c.getIdCompra(),
                c.getIdBoleto(),
                c.getEvento(),
                c.getHoraCompra().format(FECHA),
                Tema.dinero(c.getPrecioFinal()),
                c.getEstatus(),
                c.getHoraCompra().plusHours(24).format(FECHA)
              });
        modelo(
            filas,
            "Compra",
            "Boleto",
            "Evento",
            "Compra realizada",
            "Precio final",
            "Estado",
            "Cancelar hasta");
        estado.setText(
            compras.size()
                + " compras · Cancelaciones dentro de 24 horas · PDF de boletos activos");
        if (filas.isEmpty()) tarjeta("Tus boletos", "Compra tu primer boleto desde el catálogo.");
      }
      case CUENTAS -> {
        List<Object[]> filas = new ArrayList<>();
        BigDecimal saldo = BigDecimal.ZERO;
        for (CuentaDTO c : negocio.cuentas(sesion)) {
          filas.add(
              new Object[] {
                c.getIdCuenta_Bancaria(),
                c.getBanco(),
                c.getNumeroCuenta(),
                Tema.dinero(c.getSaldo())
              });
          saldo = saldo.add(c.getSaldo());
        }
        modelo(filas, "ID", "Banco", "Número de cuenta", "Saldo disponible");
        estado.setText(
            "Las cuentas pertenecen a "
                + ("ADMIN".equals(sesion.getRol()) ? sesion.getPromotora() : sesion.getNombres()));
        tarjeta(
            "Saldo total", Tema.dinero(saldo) + "<br><br>" + filas.size() + " cuentas bancarias");
      }
      case GENERAL -> {
        eventos = negocio.eventos(sesion);
        List<Object[]> filas = new ArrayList<>();
        BigDecimal ingresos = BigDecimal.ZERO, saldos = BigDecimal.ZERO;
        int vendidos = 0, disponibles = 0;
        for (EventoDTO e : eventos) {
          filas.add(
              new Object[] {
                e.getIdEvento(),
                e.getNombreShow(),
                e.getCantidadBoletos(),
                e.getVendidos(),
                e.getDisponibles(),
                Tema.dinero(e.getIngresoNeto()),
                Tema.dinero(e.getPrecioBoleto().multiply(BigDecimal.valueOf(e.getDisponibles())))
              });
          ingresos = ingresos.add(e.getIngresoNeto());
          vendidos += e.getVendidos();
          disponibles += e.getDisponibles();
        }
        List<Object[]> cuentas = new ArrayList<>();
        for (CuentaDTO c : negocio.cuentas(sesion)) {
          saldos = saldos.add(c.getSaldo());
          cuentas.add(
              new Object[] {
                c.getIdCuenta_Bancaria(),
                c.getBanco(),
                c.getNumeroCuenta(),
                Tema.dinero(c.getSaldo())
              });
        }
        modelo(
            filas,
            "ID",
            "Evento",
            "Total",
            "Vendidos",
            "Disponibles",
            "Ventas netas",
            "Por vender");
        if (tablaCuentas != null) {
          tablaCuentas.setModel(
              new DefaultTableModel(
                  cuentas.toArray(Object[][]::new),
                  new String[] {"ID", "Banco", "Número de cuenta", "Saldo disponible"}) {
                @Override
                public boolean isCellEditable(int r, int c) {
                  return false;
                }
              });
          tablaCuentas.setAutoCreateRowSorter(true);
        }
        secundario.setEnabled(
            (pestanas == null || pestanas.getSelectedIndex() == 0) && !filas.isEmpty());
        tarjeta(
            "Resumen",
            eventos.size()
                + " eventos<br>"
                + vendidos
                + " vendidos<br>"
                + disponibles
                + " disponibles<br><br>Ventas netas<br><b>"
                + Tema.dinero(ingresos)
                + "</b><br><br>Saldo bancario<br><b>"
                + Tema.dinero(saldos)
                + "</b>");
        estado.setText(
            "Promotora: "
                + sesion.getPromotora()
                + " · Eventos y cuentas de esta promotora · Doble clic para ver detalles");
      }
      case DETALLE -> {
        eventos = negocio.eventos(sesion);
        EventoDTO e = evento();
        List<Object[]> filas = new ArrayList<>();
        filas.add(new Object[] {"Evento", e.getNombreShow()});
        filas.add(new Object[] {"Promotora", e.getPromotora()});
        filas.add(
            new Object[] {
              "Tipo / edad mínima", e.getTipoEvento() + " / " + e.getEdadMinima() + " años"
            });
        filas.add(new Object[] {"Inventario total", e.getCantidadBoletos()});
        filas.add(new Object[] {"Boletos vendidos", e.getVendidos()});
        filas.add(new Object[] {"Boletos disponibles", e.getDisponibles()});
        filas.add(new Object[] {"Cancelaciones históricas", e.getCancelaciones()});
        filas.add(new Object[] {"Ingreso neto real", Tema.dinero(e.getIngresoNeto())});
        filas.add(new Object[] {"Precio vigente", Tema.dinero(e.getPrecioBoleto())});
        filas.add(
            new Object[] {
              "Valor potencial total",
              Tema.dinero(e.getPrecioBoleto().multiply(BigDecimal.valueOf(e.getCantidadBoletos())))
            });
        filas.add(
            new Object[] {
              "Dinero por vender",
              Tema.dinero(e.getPrecioBoleto().multiply(BigDecimal.valueOf(e.getDisponibles())))
            });
        filas.add(new Object[] {"Cuenta receptora", e.getCuentaReceptora()});
        modelo(filas, "Indicador", "Valor");
        Tema.imagen(tarjeta, e.getImagenPromocional(), 180, 235);
        estado.setText("Ingreso neto calculado con los precios finales de compras activas.");
      }
    }
  }

  private void tarjeta(String titulo, String texto) {
    tarjeta.setIcon(null);
    tarjeta.setText(
        "<html><div style='width:170px;text-align:center'><span"
            + " style='font-size:18px;color:#5b47ba'>"
            + titulo
            + "</span><br><br>"
            + texto
            + "</div></html>");
  }

  private void seleccion() {
    try {
      if (vista == Vista.ADMIN || vista == Vista.CATALOGO) {
        EventoDTO e = evento();
        Tema.imagen(tarjeta, e.getImagenPromocional(), 180, 235);
        tarjeta.setToolTipText(e.getNombreShow());
      }
      if (vista == Vista.HISTORIAL) {
        int id = id();
        CompraDTO c = compras.stream().filter(v -> v.getIdCompra() == id).findFirst().orElseThrow();
        boolean activo = "comprado".equals(c.getEstatus());
        principal.setEnabled(activo);
        secundario.setEnabled(activo);
        tarjeta(
            "Boleto #" + c.getIdBoleto(),
            Tema.html(c.getEvento())
                + "<br><br>"
                + Tema.dinero(c.getPrecioFinal())
                + "<br>"
                + c.getEstatus()
                + "<br><br>"
                + (c.getHoraCancelacion() == null
                    ? "Cuenta original<br>" + Tema.html(c.getCuenta()).replace(" · ", "<br>")
                    : "Cancelado<br>" + c.getHoraCancelacion().format(FECHA)));
      }
    } catch (Exception ignored) {
    }
  }

  private void accion() throws Exception {
    switch (vista) {
      case ADMIN -> Tema.abrir(ventana, new FrmEventoEditor(negocio, sesion, null));
      case CATALOGO -> Tema.abrir(ventana, new FrmCompra(negocio, sesion, evento()));
      case HISTORIAL -> pdf();
      case DETALLE -> Tema.abrir(ventana, new FrmEventoEditor(negocio, sesion, evento()));
      case GENERAL -> {
        if (pestanas != null) pestanas.setSelectedIndex(pestanas.getSelectedIndex() == 0 ? 1 : 0);
      }
      case CUENTAS -> Navegacion.inicio(ventana, negocio, sesion);
      default -> cargar();
    }
  }

  private void segunda() throws Exception {
    switch (vista) {
      case ADMIN -> Tema.abrir(ventana, new FrmEventoEditor(negocio, sesion, evento()));
      case CATALOGO -> Tema.abrir(ventana, new FrmCompra(negocio, sesion, evento()));
      case HISTORIAL -> {
        int id = id();
        if (JOptionPane.showConfirmDialog(
                ventana,
                "¿Cancelar la compra #" + id + " y reembolsar a tu cuenta original?",
                "Cancelar compra",
                JOptionPane.YES_NO_OPTION)
            == JOptionPane.YES_OPTION) {
          negocio.cancelar(sesion, id);
          Tema.mensaje(ventana, "Compra cancelada. Reembolso completo a la cuenta original.");
          cargar();
        }
      }
      case GENERAL -> detalle();
      case CUENTAS -> Tema.abrir(ventana, new FrmLogin(negocio));
      default -> Navegacion.inicio(ventana, negocio, sesion);
    }
  }

  private void detalle() throws Exception {
    if (vista == Vista.GENERAL && pestanas != null && pestanas.getSelectedIndex() != 0) return;
    Tema.abrir(ventana, new FrmDashboardEvento(negocio, sesion, evento().getIdEvento()));
  }

  private void pdf() throws Exception {
    CompraDTO c = negocio.boleto(sesion, id());
    Path destino =
        Path.of(
            System.getProperty("user.home"),
            "Downloads",
            "TicketFan",
            "boletos",
            "boleto-"
                + c.getIdBoleto()
                + "-compra-"
                + c.getIdCompra()
                + "-"
                + java.util.UUID.randomUUID()
                + ".pdf");
    BoletoPDF.generar(c, destino);
    Tema.mensaje(ventana, "Boleto guardado en " + destino.toAbsolutePath());
  }
}
