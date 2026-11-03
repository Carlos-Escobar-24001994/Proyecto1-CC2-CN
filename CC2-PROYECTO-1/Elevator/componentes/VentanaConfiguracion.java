package Elevator.componentes;

import javax.swing.*;
import java.awt.*;
import Elevator.*;

public class VentanaConfiguracion extends JDialog {

    private final JTextField txtElevadores = new JTextField(10);
    private final JTextField txtPisos = new JTextField(10);
    private final JTextField txtEspera = new JTextField(10);
    private final JTextField txtTransporte = new JTextField(10);
    private final JButton btnAceptar = new JButton("Iniciar simulación");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final MarcoPantalla marco;

    public VentanaConfiguracion(MarcoPantalla marco) {
        super(marco, "Configuración de simulación", true);
        this.marco = marco;
        setLayout(new BorderLayout(10, 12));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel descripcion = new JLabel("Indique los datos del edificio y los tiempos de operación.");
        descripcion.setFont(new Font("SansSerif", Font.PLAIN, 13));
        add(descripcion, BorderLayout.NORTH);

        JPanel formulario = new JPanel(new GridLayout(4, 2, 10, 10));
        formulario.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        formulario.add(new JLabel("Número de elevadores:"));
        formulario.add(txtElevadores);
        formulario.add(new JLabel("Cantidad de pisos:"));
        formulario.add(txtPisos);
        formulario.add(new JLabel("Espera en cada parada (ms):"));
        formulario.add(txtEspera);
        formulario.add(new JLabel("Movimiento entre pisos (ms):"));
        formulario.add(txtTransporte);
        txtElevadores.setToolTipText("Ingrese un número entero mayor que cero.");
        txtPisos.setToolTipText("Ingrese un número entero mayor que cero.");
        txtEspera.setToolTipText("Tiempo en milisegundos; puede ser cero.");
        txtTransporte.setToolTipText("Tiempo en milisegundos; puede ser cero.");
        add(formulario, BorderLayout.CENTER);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        botones.add(btnAceptar);
        botones.add(btnCancelar);
        add(botones, BorderLayout.SOUTH);

        btnAceptar.addActionListener(evento -> onAceptar());
        btnCancelar.addActionListener(evento -> dispose());
        getRootPane().setDefaultButton(btnAceptar);
        pack();
        setResizable(false);
        setLocationRelativeTo(marco);
        txtElevadores.requestFocusInWindow();
    }

    private void onAceptar() {
        try {
            int cantidadElevadores = Integer.parseInt(txtElevadores.getText().trim());
            int cantidadPisos = Integer.parseInt(txtPisos.getText().trim());
            double tiempoEspera = Double.parseDouble(txtEspera.getText().trim());
            double tiempoTransporte = Double.parseDouble(txtTransporte.getText().trim());
            if (cantidadElevadores < 1 || cantidadPisos < 1 || tiempoEspera < 0 || tiempoTransporte < 0
                    || Double.isNaN(tiempoEspera) || Double.isInfinite(tiempoEspera)
                    || Double.isNaN(tiempoTransporte) || Double.isInfinite(tiempoTransporte)) {
                JOptionPane.showMessageDialog(this,
                        "La cantidad de elevadores y pisos debe ser positiva. Los tiempos no pueden ser negativos.",
                        "Datos fuera de rango", JOptionPane.WARNING_MESSAGE);
                return;
            }
            marco.inicializarElevadores(cantidadElevadores, tiempoEspera, cantidadPisos, tiempoTransporte);
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Revise los campos: cantidades enteras y tiempos numéricos en ms.",
                    "Formato inválido", JOptionPane.WARNING_MESSAGE);
        }
    }
}
