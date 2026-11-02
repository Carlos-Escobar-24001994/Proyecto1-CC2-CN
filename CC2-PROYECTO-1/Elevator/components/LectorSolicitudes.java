package Elevator.components;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import Elevator.Direccion;
import Elevator.SolicitudElevador;

public final class LectorSolicitudes {

    private LectorSolicitudes() {
    }
    
    public static List<SolicitudElevador> cargar(String ruta) throws IOException {
        List<SolicitudElevador> lista = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(ruta))) {
            String linea;
            int numLinea = 0;
            while ((linea = br.readLine()) != null) {
                numLinea++;
                linea = linea.trim();
                if (linea.isEmpty() || linea.startsWith("#"))
                    continue;

                String[] partes = linea.split(",");
                if (partes.length != 2) {
                    throw new IOException("Línea " + numLinea + " inválida: " + linea);
                }

                int piso;
                try {
                    piso = Integer.parseInt(partes[0].trim());
                } catch (NumberFormatException e) {
                    throw new IOException("Línea " + numLinea + " piso inválido: " + partes[0]);
                }

                String dirTxt = partes[1].trim().toUpperCase();
                Direccion dir;
                if ("UP".equals(dirTxt))
                    dir = Direccion.UP;
                else if ("DOWN".equals(dirTxt))
                    dir = Direccion.DOWN;
                else
                    throw new IOException("Línea " + numLinea + " dirección inválida: " + dirTxt);

                lista.add(new SolicitudElevador(piso, dir));
            }
        }
        return lista;
    }
}
