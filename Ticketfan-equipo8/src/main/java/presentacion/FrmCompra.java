package presentacion;

import dtos.*;
import negocio.ticketNegocio.ITicketNegocio;

public class FrmCompra extends javax.swing.JFrame {
  private static final java.util.logging.Logger logger =
      java.util.logging.Logger.getLogger(FrmCompra.class.getName());
  private ITicketNegocio negocio;
  private UsuarioDTO sesion;
  private EventoDTO evento;
  private java.util.List<CuentaDTO> cuentas = java.util.List.of();

  public FrmCompra() {
    initComponents();
    Tema.ventana(this, "Vista previa");
  }

  public FrmCompra(ITicketNegocio negocio, UsuarioDTO sesion, EventoDTO evento) {
    initComponents();
    this.negocio = negocio;
    this.sesion = sesion;
    this.evento = evento;
    Tema.ventana(this, "Comprar boletos");
    Tema.tarjeta(jPanel1);
    jLabel2.setText("<html><b style='font-size:18px'>Tu próximo evento</b></html>");
    javax.swing.JTextField[] campos = {
      jTextField1, jTextField2, jTextField3, jTextField4, jTextField5
    };
    String[] titulos = {
      "Evento",
      "Promotora",
      "Tipo / edad mínima",
      "Cantidad · disponibles " + evento.getDisponibles(),
      "Total · " + Tema.dinero(evento.getPrecioBoleto()) + " por boleto"
    };
    String[] valores = {
      evento.getNombreShow(),
      evento.getPromotora(),
      evento.getTipoEvento() + " · " + evento.getEdadMinima() + " años",
      "1",
      Tema.dinero(evento.getPrecioBoleto())
    };
    for (int i = 0; i < campos.length; i++) {
      Tema.campo(campos[i], titulos[i]);
      campos[i].setText(valores[i]);
      campos[i].setEditable(false);
    }
    jTextField4.setEditable(true);
    jTextField4
        .getDocument()
        .addDocumentListener(
            new javax.swing.event.DocumentListener() {
              public void insertUpdate(javax.swing.event.DocumentEvent e) {
                total();
              }

              public void removeUpdate(javax.swing.event.DocumentEvent e) {
                total();
              }

              public void changedUpdate(javax.swing.event.DocumentEvent e) {
                total();
              }
            });
    jComboBox1.setToolTipText("Selecciona tu cuenta de pago");
    jButton1.setText("Confirmar compra");
    jButton2.setText("Volver al catálogo");
    jButton3.setText("Ver mis compras");
    Tema.secundario(jButton2);
    Tema.secundario(jButton3);
    jLabel1.setOpaque(true);
    jLabel1.setBackground(java.awt.Color.WHITE);
    Tema.ejecutar(
        this,
        () -> {
          Tema.imagen(jLabel1, evento.getImagenPromocional(), 340, 380);
          cuentas = negocio.cuentas(sesion);
          jComboBox1.setModel(
              new javax.swing.DefaultComboBoxModel<>(
                  cuentas.stream().map(CuentaDTO::toString).toArray(String[]::new)));
        });
    jButton1.addActionListener(e -> Tema.ejecutar(this, this::comprar));
    jButton2.addActionListener(e -> Navegacion.inicio(this, negocio, sesion));
    jButton3.addActionListener(e -> Tema.abrir(this, new FrmHistorial(negocio, sesion)));
    pack();
    setLocationRelativeTo(null);
  }

  private void total() {
    try {
      int cantidad = Numeros.entero(jTextField4.getText(), "Cantidad de boletos", 1, 100000);
      jTextField5.setText(
          Tema.dinero(evento.getPrecioBoleto().multiply(java.math.BigDecimal.valueOf(cantidad))));
    } catch (negocio.NegocioException e) {
      jTextField5.setText("Cantidad inválida");
    }
  }

  private void comprar() throws Exception {
    int i = jComboBox1.getSelectedIndex();
    if (i < 0) throw new negocio.NegocioException("Selecciona tu cuenta de pago.");
    jButton1.setEnabled(false);
    try {
      int cantidad = Numeros.entero(jTextField4.getText(), "Cantidad de boletos", 1, 100000);
      java.util.List<Integer> compras =
          negocio.comprar(
              sesion, evento.getIdEvento(), cuentas.get(i).getIdCuenta_Bancaria(), cantidad);
      Tema.mensaje(
          this,
          compras.size() + " boletos comprados. Cada entrada tiene su propio PDF en Mis compras.");
      Tema.abrir(this, new FrmHistorial(negocio, sesion));
    } finally {
      jButton1.setEnabled(true);
    }
  }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("jLabel1");

        jPanel1.setLayout(new java.awt.GridLayout(9, 0));

        jLabel2.setText("jLabel2");
        jPanel1.add(jLabel2);

        jTextField1.setText("jTextField1");
        jPanel1.add(jTextField1);

        jTextField2.setText("jTextField2");
        jPanel1.add(jTextField2);

        jTextField3.setText("jTextField3");
        jPanel1.add(jTextField3);

        jTextField4.setText("jTextField4");
        jPanel1.add(jTextField4);

        jTextField5.setText("jTextField5");
        jPanel1.add(jTextField5);

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jPanel1.add(jComboBox1);

        jButton1.setText("jButton1");
        jPanel1.add(jButton1);

        jButton2.setText("jButton2");
        jPanel1.add(jButton2);

        jButton3.setText("jButton3");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(42, 42, 42)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 382, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(99, 99, 99)
                        .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 261, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(64, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(77, 77, 77)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(31, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        Tema.instalarEstilo();

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new FrmCompra().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    // End of variables declaration//GEN-END:variables
}
