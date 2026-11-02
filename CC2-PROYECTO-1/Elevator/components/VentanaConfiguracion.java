package Elevator.components;

import javax.swing.*;
import java.awt.*;
import Elevator.*;

public class VentanaConfiguracion extends JDialog {

    private final JTextField txtElevadores = new JTextField(6);
    private final JTextField txtPisos = new JTextField(6);
    private final JTextField txtEspera = new JTextField(6);
    private final JTextField txtTransporte = new JTextField(6);
    private final JButton btnAceptar = new JButton("Aceptar");
    private final JButton btnCancelar = new JButton("Cancelar");
    private final MarcoPantalla marco;

    public VentanaConfiguracion(MarcoPantalla marco) {
        super(marco, "Configuración de simulación", true);
        this.marco = marco;

        setLayout(new GridLayout(5, 2, 10, 10));
        add(new JLabel("Número de elevadores:"));
        add(txtElevadores);
        add(new JLabel("Cantidad de pisos:"));
        add(txtPisos);
        add(new JLabel("Tiempo de espera (s):"));
        add(txtEspera);
        add(new JLabel("Tiempo por piso (s):"));
        add(txtTransporte);
        add(btnAceptar);
        add(btnCancelar);

        btnAceptar.addActionListener(e -> onAceptar());
        btnCancelar.addActionListener(e -> dispose());

        pack();
        setLocationRelativeTo(marco);
    }

    private void onAceptar() {
        try {
            int nElevadores = Integer.parseInt(txtElevadores.getText().trim());
            int cantidadPisos = Integer.parseInt(txtPisos.getText().trim());
            double tiempoEspera = Double.parseDouble(txtEspera.getText().trim());
            double tiempoTransporte = Double.parseDouble(txtTransporte.getText().trim());
            marco.inicializarElevadores(nElevadores, tiempoEspera, cantidadPisos, tiempoTransporte);
            dispose();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Datos inválidos, ingrese números enteros o decimales.");
        }
    }
}
