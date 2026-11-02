package Elevator;

import Elevator.components.MarcoPantalla;
import javax.swing.SwingUtilities;

public class ElevatorManager {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MarcoPantalla ventanaPrincipal = new MarcoPantalla();
            ventanaPrincipal.setVisible(true);
        });
    }
}
