package Elevator.components;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;
import Elevator.*;

public class MarcoPantalla extends JFrame {

    private JButton botonIniciar, botonParar, botonResetear, botonAgregarSolicitud;
    private JTextArea areaDeLog;
    private ManejoElevadores manejadorElevadores;
    private ElevatorStandard[] listaDeElevadores;
    private int totalPisos;

    public MarcoPantalla() {
        super("Elevator Manager");
        setLayout(new BorderLayout(10, 10));

        JPanel panelDeControles = new JPanel(new GridLayout(1, 4, 10, 10));
        botonIniciar = new JButton("Iniciar");
        botonParar = new JButton("Parar");
        botonResetear = new JButton("Reset");
        botonAgregarSolicitud = new JButton("Agregar Solicitud");
        panelDeControles.add(botonIniciar);
        panelDeControles.add(botonParar);
        panelDeControles.add(botonResetear);
        panelDeControles.add(botonAgregarSolicitud);

        areaDeLog = new JTextArea();
        areaDeLog.setEditable(false);
        add(panelDeControles, BorderLayout.NORTH);
        add(new JScrollPane(areaDeLog), BorderLayout.CENTER);

        botonIniciar.addActionListener(evento -> {
            if (manejadorElevadores != null) {
                agregarLog("La simulación ya está en ejecución.");
                return;
            }
            new VentanaConfiguracion(this).setVisible(true);
        });
        botonParar.addActionListener(evento -> {
            if (manejadorElevadores == null) {
                agregarLog("No hay simulación activa.");
                return;
            }
            manejadorElevadores.detener();
            for (Elevator ascensor : listaDeElevadores)
                ascensor.setDetenerElevadores();
            agregarLog("Simulación detenida.");
        });
        botonResetear.addActionListener(evento -> {
            if (manejadorElevadores == null) {
                agregarLog("No hay simulación activa.");
                return;
            }
            manejadorElevadores.resetSistema();
            agregarLog("Elevadores reiniciados.");
        });
        botonAgregarSolicitud.addActionListener(evento -> solicitarElevador());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 500);
        setLocationRelativeTo(null);
    }

    private void solicitarElevador() {
        if (manejadorElevadores == null) {
            agregarLog("Debe iniciar la simulación primero.");
            return;
        }
        String pisoTexto = JOptionPane.showInputDialog(this, "Ingrese el piso para llamar elevador:");
        if (pisoTexto == null || pisoTexto.trim().isEmpty())
            return;
        try {
            int piso = Integer.parseInt(pisoTexto.trim());
            if (piso < 1 || piso > totalPisos) {
                JOptionPane.showMessageDialog(this, "Piso fuera de rango (1-" + totalPisos + ").");
                return;
            }
            String[] opciones = { "SUBIR", "BAJAR" };
            String seleccion = (String) JOptionPane.showInputDialog(this, "Seleccione dirección:", "Dirección",
                    JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (seleccion == null)
                return;
            Direccion direccion = seleccion.equals("SUBIR") ? Direccion.UP : Direccion.DOWN;
            manejadorElevadores.solicitarElevador(piso, direccion);
            agregarLog("Solicitud agregada: piso " + piso + " dirección " + direccion);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número de piso válido.");
        }
    }

    public void inicializarElevadores(int cantidadElevadores, double tiempoEspera, int cantidadPisos,
            double tiempoTransporte) {
        totalPisos = cantidadPisos;
        listaDeElevadores = new ElevatorStandard[cantidadElevadores];
        GuiLogHandler manejadorGUI = new GuiLogHandler(areaDeLog);
        for (int i = 0; i < cantidadElevadores; i++) {
            listaDeElevadores[i] = new ElevatorStandard(tiempoEspera, cantidadPisos, tiempoTransporte);
            Logger logger = ElevatorLogger.cache(listaDeElevadores[i].getId());
            logger.addHandler(manejadorGUI);
            new Thread(listaDeElevadores[i], "Elevador-" + listaDeElevadores[i].getId()).start();
            agregarLog("Elevador #" + listaDeElevadores[i].getId() + " iniciado.");
        }
        manejadorElevadores = new ManejoElevadores(listaDeElevadores);
        new Thread(manejadorElevadores, "ManejadorElevadores").start();
        agregarLog("Manejador de elevadores iniciado.");
    }

    public void agregarLog(String mensaje) {
        areaDeLog.append(mensaje + "\n");
    }
}
