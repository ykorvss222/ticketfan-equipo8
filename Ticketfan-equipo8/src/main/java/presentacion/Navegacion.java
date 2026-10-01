package presentacion;

import dtos.*;
import javax.swing.*;
import negocio.ticketNegocio.ITicketNegocio;

public final class Navegacion {
  private Navegacion() {}

  public static void inicio(JFrame actual, ITicketNegocio negocio, UsuarioDTO sesion) {
    Tema.abrir(
        actual,
        "ADMIN".equals(sesion.getRol())
            ? new FrmEventosAdmin(negocio, sesion)
            : new FrmCatalogo(negocio, sesion));
  }

  public static void menu(
      JFrame actual,
      JComboBox<String> menu,
      ITicketNegocio negocio,
      UsuarioDTO sesion,
      String seleccion) {
    boolean admin = "ADMIN".equals(sesion.getRol());
    menu.setModel(
        new DefaultComboBoxModel<>(
            admin
                ? new String[] {"Mis eventos", "Dashboard general", "Mis cuentas", "Cerrar sesión"}
                : new String[] {"Catálogo", "Mis compras", "Mis cuentas", "Cerrar sesión"}));
    menu.setSelectedItem(seleccion);
    menu.addActionListener(
        e ->
            Tema.ejecutar(
                actual,
                () -> {
                  String opcion = (String) menu.getSelectedItem();
                  if (opcion.equals(seleccion)) return;
                  switch (opcion) {
                    case "Mis eventos", "Catálogo" -> inicio(actual, negocio, sesion);
                    case "Mis compras" -> Tema.abrir(actual, new FrmHistorial(negocio, sesion));
                    case "Mis cuentas" -> Tema.abrir(actual, new FrmCuentas(negocio, sesion));
                    case "Dashboard general" ->
                        Tema.abrir(actual, new FrmDashboardGeneral(negocio, sesion));
                    case "Cerrar sesión" -> Tema.abrir(actual, new FrmLogin(negocio));
                  }
                }));
  }
}
