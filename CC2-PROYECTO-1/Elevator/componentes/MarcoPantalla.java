package Elevator.componentes;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import Elevator.*;

public class MarcoPantalla extends JFrame {

    private JButton botonIniciar, botonParar, botonResetear, botonAgregarSolicitud, botonCargarSolicitudes;
    private JTextArea areaDeLog;
    private JLabel etiquetaEstado, etiquetaSolicitudes;
    private JTable tablaElevadores;
    private DefaultTableModel modeloElevadores;
    private ManejoElevadores manejadorElevadores;
    private Elevator[] listaDeElevadores;
    private Thread[] hilosElevadores;
    private Thread hiloManejador;
    private final List<SolicitudExterna> solicitudesPendientes = new ArrayList<>();
    private int totalPisos;
    private boolean simulacionActiva;
    private boolean deteniendo;
    private boolean sistemaEnReset = true;

    public MarcoPantalla() {
        super("Elevator Manager");
        setLayout(new BorderLayout(12, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        getContentPane().setBackground(new Color(242, 245, 249));

        JPanel panelDeControles = new JPanel(new GridLayout(1, 5, 8, 8));
        panelDeControles.setBackground(new Color(242, 245, 249));
        botonIniciar = crearBoton("Iniciar", new Color(39, 109, 180));
        botonParar = crearBoton("Detener", new Color(178, 65, 65));
        botonResetear = crearBoton("Reset", new Color(92, 105, 120));
        botonAgregarSolicitud = crearBoton("Nueva solicitud", new Color(39, 109, 180));
        botonCargarSolicitudes = crearBoton("Cargar archivo", new Color(39, 109, 180));
        panelDeControles.add(botonIniciar);
        panelDeControles.add(botonParar);
        panelDeControles.add(botonResetear);
        panelDeControles.add(botonAgregarSolicitud);
        panelDeControles.add(botonCargarSolicitudes);

        etiquetaEstado = new JLabel("Estado: sin iniciar");
        etiquetaEstado.setFont(new Font("SansSerif", Font.BOLD, 13));
        etiquetaEstado.setForeground(new Color(75, 87, 101));
        etiquetaEstado.setBorder(BorderFactory.createEmptyBorder(4, 2, 4, 2));
        etiquetaSolicitudes = new JLabel("Solicitudes sin asignar: 0");
        etiquetaSolicitudes.setFont(new Font("SansSerif", Font.PLAIN, 12));
        etiquetaSolicitudes.setForeground(new Color(75, 87, 101));
        etiquetaSolicitudes.setHorizontalAlignment(SwingConstants.RIGHT);

        areaDeLog = new JTextArea();
        areaDeLog.setEditable(false);
        areaDeLog.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        areaDeLog.setLineWrap(true);
        areaDeLog.setWrapStyleWord(true);
        JScrollPane panelRegistros = new JScrollPane(areaDeLog);
        panelRegistros.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 228)), "Registros de la simulación"));

        String[] columnas = { "Elevador", "Piso actual", "Dirección", "Piso destino", "Llamadas externas" };
        modeloElevadores = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int fila, int columna) {
                return false;
            }
        };
        tablaElevadores = new JTable(modeloElevadores);
        tablaElevadores.setRowHeight(25);
        tablaElevadores.setFillsViewportHeight(true);
        tablaElevadores.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tablaElevadores.getTableHeader().setReorderingAllowed(false);
        tablaElevadores.setFont(new Font("SansSerif", Font.PLAIN, 12));
        modeloElevadores.addRow(new Object[] { "—", "—", "—", "Sin elevadores configurados", "—" });
        JScrollPane panelTabla = new JScrollPane(tablaElevadores);
        panelTabla.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 228)), "Estado de los elevadores"));

        JPanel panelInformacion = new JPanel(new GridLayout(1, 2, 8, 4));
        panelInformacion.setBackground(new Color(242, 245, 249));
        panelInformacion.add(etiquetaEstado);
        panelInformacion.add(etiquetaSolicitudes);

        JPanel panelInferior = new JPanel(new BorderLayout(4, 4));
        panelInferior.setBackground(new Color(242, 245, 249));
        panelInferior.add(panelInformacion, BorderLayout.NORTH);
        panelInferior.add(panelTabla, BorderLayout.CENTER);

        JSplitPane panelCentral = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panelRegistros, panelInferior);
        panelCentral.setResizeWeight(0.62);
        panelCentral.setBorder(null);
        add(panelDeControles, BorderLayout.NORTH);
        add(panelCentral, BorderLayout.CENTER);

        botonIniciar.addActionListener(evento -> {
            if (manejadorElevadores != null) {
                agregarLog("La simulación ya fue iniciada.");
                return;
            }
            new VentanaConfiguracion(this).setVisible(true);
        });
        botonParar.addActionListener(evento -> detenerSimulacion());
        botonResetear.addActionListener(evento -> {
            if (!simulacionActiva)
                return;
            manejadorElevadores.resetSistema();
            sistemaEnReset = true;
            etiquetaEstado.setText("Estado: reiniciado; puede cargar solicitudes");
            agregarLog("Elevadores reiniciados.");
            actualizarEstadoElevadores();
            actualizarBotones();
        });
        botonAgregarSolicitud.addActionListener(evento -> solicitarElevador());
        botonCargarSolicitudes.addActionListener(evento -> cargarSolicitudes());

        actualizarBotones();
        new Timer(250, evento -> actualizarEstadoElevadores()).start();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 620);
        setMinimumSize(new Dimension(760, 500));
        setLocationRelativeTo(null);
    }

    private JButton crearBoton(String texto, Color color) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("SansSerif", Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(color);
        boton.setFocusPainted(false);
        boton.setBorder(BorderFactory.createEmptyBorder(9, 8, 9, 8));
        boton.setToolTipText(texto);
        return boton;
    }

    private void detenerSimulacion() {
        if (!simulacionActiva)
            return;
        simulacionActiva = false;
        deteniendo = true;
        etiquetaEstado.setText("Estado: deteniendo...");
        actualizarBotones();
        agregarLog("Deteniendo los hilos de la simulación...");

        ManejoElevadores manejadorDetenido = manejadorElevadores;
        Thread[] hilosDetenidos = hilosElevadores;
        Thread hiloManejadorDetenido = hiloManejador;
        manejadorDetenido.detener();
        for (Elevator ascensor : listaDeElevadores) {
            ascensor.setDetenerElevadores();
        }
        for (Thread hilo : hilosDetenidos)
            hilo.interrupt();
        hiloManejadorDetenido.interrupt();

        Thread hiloFinalizador = new Thread(() -> {
            esperarHilos(hilosDetenidos);
            esperarHilo(hiloManejadorDetenido);
            SwingUtilities.invokeLater(() -> {
                if (manejadorElevadores == manejadorDetenido) {
                    manejadorElevadores = null;
                    hilosElevadores = null;
                    hiloManejador = null;
                    deteniendo = false;
                    sistemaEnReset = true;
                    etiquetaEstado.setText("Estado: detenida; puede iniciar otra simulación");
                    agregarLog("Simulación detenida. Puede iniciar una nueva.");
                    actualizarBotones();
                }
            });
        }, "FinalizarSimulacion");
        hiloFinalizador.start();
    }

    private void esperarHilos(Thread[] hilos) {
        for (Thread hilo : hilos)
            esperarHilo(hilo);
    }

    private void esperarHilo(Thread hilo) {
        boolean interrumpido = false;
        while (hilo.isAlive()) {
            try {
                hilo.join();
            } catch (InterruptedException ex) {
                interrumpido = true;
            }
        }
        if (interrumpido)
            Thread.currentThread().interrupt();
    }

    private void actualizarBotones() {
        botonIniciar.setEnabled(manejadorElevadores == null && !deteniendo);
        botonParar.setEnabled(simulacionActiva);
        botonResetear.setEnabled(simulacionActiva);
        botonAgregarSolicitud.setEnabled(simulacionActiva);
        botonCargarSolicitudes.setEnabled(!deteniendo
                && (manejadorElevadores == null || (simulacionActiva && sistemaEnReset)));
    }

    private void solicitarElevador() {
        if (!simulacionActiva)
            return;

        String[] tiposSolicitud = { "Llamar elevador desde un piso", "Elegir destino dentro de un elevador" };
        JComboBox<String> tipoSolicitud = new JComboBox<>(tiposSolicitud);
        JTextField campoPiso = new JTextField(8);
        JComboBox<String> direccion = new JComboBox<>(new String[] { "Subir", "Bajar" });
        JComboBox<String> elevadorSeleccionado = new JComboBox<>();
        for (Elevator elevador : listaDeElevadores)
            elevadorSeleccionado.addItem("Elevador #" + elevador.getId());

        JLabel etiquetaDireccion = new JLabel("Dirección:");
        JLabel etiquetaElevador = new JLabel("Elevador:");
        JPanel formulario = new JPanel(new GridLayout(4, 2, 8, 8));
        formulario.add(new JLabel("Tipo de solicitud:"));
        formulario.add(tipoSolicitud);
        formulario.add(new JLabel("Piso:"));
        formulario.add(campoPiso);
        formulario.add(etiquetaDireccion);
        formulario.add(direccion);
        formulario.add(etiquetaElevador);
        formulario.add(elevadorSeleccionado);

        tipoSolicitud.addItemListener(evento -> {
            boolean llamadaExterna = evento.getStateChange() == ItemEvent.SELECTED
                    && tipoSolicitud.getSelectedIndex() == 0;
            etiquetaDireccion.setVisible(llamadaExterna);
            direccion.setVisible(llamadaExterna);
            etiquetaElevador.setVisible(!llamadaExterna);
            elevadorSeleccionado.setVisible(!llamadaExterna);
        });
        etiquetaElevador.setVisible(false);
        elevadorSeleccionado.setVisible(false);

        int resultado = JOptionPane.showConfirmDialog(this, formulario, "Nueva solicitud",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (resultado != JOptionPane.OK_OPTION)
            return;
        try {
            int piso = Integer.parseInt(campoPiso.getText().trim());
            if (piso < 1 || piso > totalPisos) {
                JOptionPane.showMessageDialog(this, "Ingrese un piso entre 1 y " + totalPisos + ".",
                        "Piso fuera de rango", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (tipoSolicitud.getSelectedIndex() == 0) {
                Direccion direccionSolicitud = direccion.getSelectedIndex() == 0 ? Direccion.UP : Direccion.DOWN;
                manejadorElevadores.solicitarElevador(piso, direccionSolicitud);
                sistemaEnReset = false;
                agregarLog("Llamada agregada: piso " + piso + ", dirección " + direccion.getSelectedItem() + ".");
            } else {
                String textoElevador = (String) elevadorSeleccionado.getSelectedItem();
                int idElevador = Integer.parseInt(textoElevador.substring(textoElevador.indexOf('#') + 1));
                manejadorElevadores.agregarSolicitudInterna(idElevador, piso);
                sistemaEnReset = false;
                agregarLog("Destino agregado: elevador #" + idElevador + ", piso " + piso + ".");
            }
            actualizarBotones();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Ingrese un número de piso válido.",
                    "Dato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void cargarSolicitudes() {
        if (deteniendo || (manejadorElevadores != null && (!simulacionActiva || !sistemaEnReset)))
            return;
        JFileChooser selectorArchivo = new JFileChooser();
        selectorArchivo.setDialogTitle("Seleccionar archivo de solicitudes");
        selectorArchivo.setFileFilter(new FileNameExtensionFilter("Archivos de texto (*.txt)", "txt"));
        int resultado = selectorArchivo.showOpenDialog(this);
        if (resultado != JFileChooser.APPROVE_OPTION)
            return;
        File archivo = selectorArchivo.getSelectedFile();
        try {
            List<SolicitudExterna> solicitudes = LectorSolicitudes.cargar(archivo.getAbsolutePath());
            int cargadas = 0;
            int ignoradas = 0;
            for (SolicitudExterna solicitud : solicitudes) {
                if (manejadorElevadores != null && solicitud.getPiso() > totalPisos) {
                    ignoradas++;
                } else {
                    if (manejadorElevadores == null)
                        solicitudesPendientes.add(solicitud);
                    else
                        manejadorElevadores.solicitarElevador(solicitud.getPiso(), solicitud.getDireccion());
                    cargadas++;
                }
            }
            agregarLog("Archivo " + archivo.getName() + ": " + cargadas + " solicitudes cargadas, "
                    + ignoradas + " ignoradas por piso fuera de rango.");
            if (manejadorElevadores != null && cargadas > 0)
                sistemaEnReset = false;
            actualizarBotones();
            actualizarEstadoElevadores();
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar el archivo:\n" + ex.getMessage(),
                    "Error al cargar", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void inicializarElevadores(int cantidadElevadores, double tiempoEspera, int cantidadPisos,
            double tiempoTransporte) {
        totalPisos = cantidadPisos;
        listaDeElevadores = new Elevator[cantidadElevadores];
        hilosElevadores = new Thread[cantidadElevadores];
        GuiLogHandler manejadorGUI = new GuiLogHandler(areaDeLog);
        for (int i = 0; i < cantidadElevadores; i++) {
            listaDeElevadores[i] = new Elevator(tiempoEspera, cantidadPisos, tiempoTransporte);
            Logger logger = ElevatorLogger.cache(listaDeElevadores[i].getId());
            logger.addHandler(manejadorGUI);
            hilosElevadores[i] = new Thread(listaDeElevadores[i], "Elevador-" + listaDeElevadores[i].getId());
            hilosElevadores[i].start();
            agregarLog("Elevador #" + listaDeElevadores[i].getId() + " iniciado.");
        }
        manejadorElevadores = new ManejoElevadores(listaDeElevadores);
        hiloManejador = new Thread(manejadorElevadores, "ManejadorElevadores");
        hiloManejador.start();
        int solicitudesAgregadas = 0;
        int solicitudesIgnoradas = 0;
        for (SolicitudExterna solicitud : solicitudesPendientes) {
            if (solicitud.getPiso() <= totalPisos) {
                manejadorElevadores.solicitarElevador(solicitud.getPiso(), solicitud.getDireccion());
                solicitudesAgregadas++;
            } else {
                solicitudesIgnoradas++;
            }
        }
        if (solicitudesAgregadas > 0)
            agregarLog(solicitudesAgregadas + " solicitudes pendientes agregadas a la simulación.");
        if (solicitudesIgnoradas > 0)
            agregarLog(solicitudesIgnoradas + " solicitudes omitidas por piso fuera de rango.");
        solicitudesPendientes.clear();
        simulacionActiva = true;
        sistemaEnReset = false;
        etiquetaEstado.setText("Estado: en ejecución");
        agregarLog("Manejador de elevadores iniciado.");
        actualizarEstadoElevadores();
        actualizarBotones();
    }

    private void actualizarEstadoElevadores() {
        if (listaDeElevadores == null) {
            etiquetaSolicitudes.setText("Solicitudes cargadas para iniciar: " + solicitudesPendientes.size());
            return;
        }
        modeloElevadores.setRowCount(0);
        for (Elevator elevador : listaDeElevadores) {
            modeloElevadores.addRow(new Object[] {
                    "#" + elevador.getId(),
                    elevador.getPisoActual(),
                    elevador.getDireccion(),
                    elevador.getPisosDestinoTexto(),
                    elevador.getLlamadasExternasTexto()
            });
        }
        if (manejadorElevadores != null) {
            etiquetaSolicitudes.setText("Solicitudes sin asignar: "
                    + manejadorElevadores.getCantidadSolicitudesPendientes());
        } else {
            etiquetaSolicitudes.setText("Solicitudes cargadas para iniciar: " + solicitudesPendientes.size());
        }
    }

    public void agregarLog(String mensaje) {
        areaDeLog.append(mensaje + "\n");
        areaDeLog.setCaretPosition(areaDeLog.getDocument().getLength());
    }
}
