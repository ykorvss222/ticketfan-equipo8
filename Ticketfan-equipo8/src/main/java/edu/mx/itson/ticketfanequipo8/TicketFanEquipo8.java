package edu.mx.itson.ticketfanequipo8;

import negocio.ticketNegocio.*;
import persistencia.*;
import persistencia.compradao.*;
import persistencia.cuentadao.*;
import persistencia.eventodao.*;
import persistencia.usuariodao.*;

/** Construye DAO, negocio y formulario, como bdaunidad1. */
public class TicketFanEquipo8 {
  public static ITicketNegocio crearNegocio() {
    IConexion conexion = new Conexion();
    return new TicketNegocio(
        new UsuarioDAO(conexion),
        new EventoDAO(conexion),
        new CompraDAO(conexion),
        new CuentaDAO(conexion));
  }

  public static void main(String[] args) {
    javax.swing.SwingUtilities.invokeLater(
        () -> {
          presentacion.Tema.instalarEstilo();
          new presentacion.FrmLogin(crearNegocio()).setVisible(true);
        });
  }
}
