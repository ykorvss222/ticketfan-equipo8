package presentacion;

import dtos.*;
import negocio.ticketNegocio.ITicketNegocio;

public class FrmEventoEditor extends javax.swing.JFrame {
  private static final java.util.logging.Logger logger =
      java.util.logging.Logger.getLogger(FrmEventoEditor.class.getName());
  private ITicketNegocio negocio;
  private UsuarioDTO sesion;
  private EventoDTO edicion;
  private byte[] imagen;
  private java.util.List<CuentaDTO> cuentas = java.util.List.of();

  public FrmEventoEditor() {
    initComponents();
    Tema.ventana(this, "Vista previa");
  }

  public FrmEventoEditor(ITicketNegocio negocio, UsuarioDTO sesion, EventoDTO edicion) {
    initComponents();
    this.negocio = negocio;
    this.sesion = sesion;
    this.edicion = edicion;
    Tema.ventana(this, edicion == null ? "Nuevo evento" : "Editar evento");
    Tema.tarjeta(jPanel1);
    jLabel2.setText(
        "<html><b style='font-size:18px'>"
            + (edicion == null ? "Crea un nuevo evento" : "Modifica tu evento")
            + "</b></html>");
    Tema.campo(jTextField1, "Nombre del show");
    Tema.campo(jTextField2, "Tipo de evento");
    Tema.campo(jTextField3, "Edad mínima");
    Tema.campo(jTextField4, "Cantidad de boletos");
    Tema.campo(jTextField5, "Precio por boleto · MXN");
    jTextField3.setText("0");
    jTextField4.setEditable(true);
    jComboBox1.setToolTipText("Cuenta receptora de tu promotora");
    jLabel1.setOpaque(true);
    jLabel1.setBackground(java.awt.Color.WHITE);
    jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
    jButton1.setText("Guardar evento");
    jButton2.setText("Volver a mis eventos");
    jButton3.setText("Seleccionar imagen");
    Tema.secundario(jButton2);
    Tema.secundario(jButton3);
    jButton1.addActionListener(e -> Tema.ejecutar(this, this::guardar));
    jButton2.addActionListener(e -> Navegacion.inicio(this, negocio, sesion));
    jButton3.addActionListener(e -> Tema.ejecutar(this, this::imagen));
    Tema.ejecutar(
        this,
        () -> {
          cuentas = negocio.cuentas(sesion);
          jComboBox1.setModel(
              new javax.swing.DefaultComboBoxModel<>(
                  cuentas.stream().map(CuentaDTO::toString).toArray(String[]::new)));
          if (edicion != null) {
            jTextField1.setText(edicion.getNombreShow());
            jTextField2.setText(edicion.getTipoEvento());
            jTextField3.setText("" + edicion.getEdadMinima());
            jTextField4.setText("" + edicion.getCantidadBoletos());
            jTextField5.setText(edicion.getPrecioBoleto().toPlainString());
            imagen = edicion.getImagenPromocional();
            for (int i = 0; i < cuentas.size(); i++)
              if (cuentas.get(i).getIdCuenta_Bancaria() == edicion.getIdCuenta_Bancaria())
                jComboBox1.setSelectedIndex(i);
          }
          Tema.imagen(jLabel1, imagen, 340, 380);
        });
    pack();
    setLocationRelativeTo(null);
  }

  private void guardar() throws Exception {
    int indice = jComboBox1.getSelectedIndex();
    if (indice < 0) throw new negocio.NegocioException("Selecciona una cuenta receptora.");
    EventoDTO evento = new EventoDTO();
    evento.setIdEvento(edicion == null ? 0 : edicion.getIdEvento());
    evento.setNombreShow(jTextField1.getText());
    evento.setTipoEvento(jTextField2.getText());
    evento.setEdadMinima(Numeros.entero(jTextField3.getText(), "Edad mínima", 0, 120));
    evento.setCantidadBoletos(
        Numeros.entero(jTextField4.getText(), "Cantidad de boletos", 1, 100000));
    evento.setPrecioBoleto(Numeros.precio(jTextField5.getText()));
    evento.setImagenPromocional(imagen);
    evento.setIdCuenta_Bancaria(cuentas.get(indice).getIdCuenta_Bancaria());
    negocio.guardar(sesion, evento);
    Tema.mensaje(
        this,
        edicion == null ? "Evento creado. Sus boletos están disponibles." : "Evento actualizado.");
    Navegacion.inicio(this, negocio, sesion);
  }

  private void imagen() throws Exception {
    javax.swing.JFileChooser selector = new javax.swing.JFileChooser();
    selector.setFileFilter(
        new javax.swing.filechooser.FileNameExtensionFilter(
            "Imagen PNG o JPEG", "png", "jpg", "jpeg"));
    if (selector.showOpenDialog(this) != javax.swing.JFileChooser.APPROVE_OPTION) return;
    java.nio.file.Path ruta = selector.getSelectedFile().toPath();
    if (java.nio.file.Files.size(ruta) > 16777215)
      throw new negocio.NegocioException("La imagen debe ocupar menos de 16 MB.");
    byte[] nueva = java.nio.file.Files.readAllBytes(ruta);
    Tema.imagen(jLabel1, nueva, 340, 380);
    imagen = nueva;
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
        java.awt.EventQueue.invokeLater(() -> new FrmEventoEditor().setVisible(true));
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
