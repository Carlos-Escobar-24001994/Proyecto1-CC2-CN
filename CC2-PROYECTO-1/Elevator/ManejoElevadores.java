package Elevator;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ManejoElevadores implements Runnable {

    private final ConcurrentLinkedQueue<SolicitudExterna> solicitudes = new ConcurrentLinkedQueue<>();
    private final Elevator[] elevadores;
    private volatile boolean enEjecucion = true;

    public ManejoElevadores(Elevator[] elevadores) {
        if (elevadores == null || elevadores.length == 0) {
            throw new IllegalArgumentException("Debe haber al menos un elevador");
        }
        this.elevadores = elevadores;
    }

    public synchronized void solicitarElevador(int piso, Direccion direccion) {
        if (piso < 1 || piso > elevadores[0].getCantidadPisos()) {
            throw new IllegalArgumentException("El piso solicitado está fuera de rango");
        }
        solicitudes.offer(new SolicitudExterna(piso, direccion));
    }

    public synchronized void agregarSolicitudInterna(int idElevador, int piso) {
        Elevator elevador = buscarElevadorPorId(idElevador);
        if (elevador == null)
            return;
        elevador.agregarSolicitudInterna(new SolicitudInterna(piso));
    }

    public synchronized void resetSistema() {
        solicitudes.clear();
        for (Elevator elevador : elevadores)
            elevador.resetElevador();
    }

    public void detener() {
        enEjecucion = false;
    }

    @Override
    public String toString() {
        return "Solicitudes en cola: " + solicitudes.size();
    }

    @Override
    public void run() {
        while (enEjecucion) {
            SolicitudExterna solicitud;
            synchronized (this) {
                solicitud = solicitudes.poll();
                if (solicitud != null)
                    asignarSolicitud(solicitud);
            }
            if (solicitud == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public int getCantidadSolicitudesPendientes() {
        return solicitudes.size();
    }

    private void asignarSolicitud(SolicitudExterna solicitud) {
        int mejorIdx = -1;
        long mejorCosto = Long.MAX_VALUE;
        int mejorCarga = Integer.MAX_VALUE;
        int mejorId = Integer.MAX_VALUE;

        for (int i = 0; i < elevadores.length; i++) {
            Elevator elevador = elevadores[i];
            long costo = costoParaElevador(elevador, solicitud);
            int carga = contarParadasPendientes(elevador);
            int id = elevador.getId();

            if (costo < mejorCosto
                    || (costo == mejorCosto && carga < mejorCarga)
                    || (costo == mejorCosto && carga == mejorCarga && id < mejorId)) {
                mejorCosto = costo;
                mejorCarga = carga;
                mejorId = id;
                mejorIdx = i;
            }
        }

        if (mejorIdx >= 0) {
            elevadores[mejorIdx].agregarSolicitudExterna(solicitud);
            System.out.println("Asignado " + solicitud + " al elevador #" + elevadores[mejorIdx].getId() +
                    " (costo=" + mejorCosto + ", carga=" + mejorCarga + ", id=" + mejorId + ")");
        } else {
            solicitudes.offer(solicitud);
        }
    }

    private long costoParaElevador(Elevator elevador, SolicitudExterna solicitud) {
        int pisoElevador = elevador.getPisoActual();
        Direccion dirElevador = elevador.getDireccion();
        int pisoSolicitud = solicitud.getPiso();
        Direccion dirSolicitud = solicitud.getDireccion();

        int distancia = Math.abs(pisoElevador - pisoSolicitud);
        int pendientes = contarParadasPendientes(elevador);
        boolean tieneParadas = pendientes > 0;
        int pisosMax = elevador.getCantidadPisos();

        if (!tieneParadas) {
            long base = distancia;
            long bonoIdle = 1;
            return Math.max(0, base - bonoIdle);
        }

        if (dirElevador == Direccion.UP && dirSolicitud == Direccion.UP && pisoSolicitud >= pisoElevador
                && elevador.tieneParadasEnRuta(Direccion.UP)) {
            return distancia + pendientes;
        }
        if (dirElevador == Direccion.DOWN && dirSolicitud == Direccion.DOWN && pisoSolicitud <= pisoElevador
                && elevador.tieneParadasEnRuta(Direccion.DOWN)) {
            return distancia + pendientes;
        }

        int penalCambio = Math.max(3, pisosMax / 3);
        return distancia + penalCambio + 2L * pendientes;
    }

    private int contarParadasPendientes(Elevator elevador) {
        return elevador.getCantidadParadasPendientes();
    }

    private Elevator buscarElevadorPorId(int id) {
        for (Elevator elevador : elevadores)
            if (elevador.getId() == id)
                return elevador;
        return null;
    }
}
