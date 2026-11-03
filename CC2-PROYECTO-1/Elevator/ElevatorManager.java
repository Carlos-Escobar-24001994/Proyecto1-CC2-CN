package Elevator;

import Elevator.componentes.MarcoPantalla;
import javax.swing.SwingUtilities;

public class ElevatorManager {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MarcoPantalla ventanaPrincipal = new MarcoPantalla();
            ventanaPrincipal.setVisible(true);
        });
    }
}
