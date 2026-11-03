package Elevator;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import Elevator.componentes.ElevatorLogger;

public class Elevator implements Movimientos, Runnable {
    private static int cantidadElevadores = 1;
    private final boolean[] destinosInternos;
    private final boolean[] llamadasSubir;
    private final boolean[] llamadasBajar;
    private final ConcurrentLinkedQueue<SolicitudElevador> cola;
    private final Logger mLogger;
    private final int id;
    private volatile int pisoActual;
    private volatile int pisoDestino;
    private volatile Direccion direccion;
    private volatile boolean detenerElevadores;
    private final double tiempoEspera;
    private final double duracionMovimiento;
    private long versionEstado;

    public Elevator(double tiempoEspera, int cantidadPisos, double duracionMovimiento) {
        if (cantidadPisos < 1)
            throw new IllegalArgumentException("Debe haber al menos un piso");
        this.tiempoEspera = tiempoEspera;
        this.direccion = Direccion.UP;
        this.duracionMovimiento = duracionMovimiento;
        this.pisoActual = 1;
        synchronized (Elevator.class) {
            this.id = cantidadElevadores++;
        }
        this.cola = new ConcurrentLinkedQueue<>();
        this.destinosInternos = new boolean[cantidadPisos];
        this.llamadasSubir = new boolean[cantidadPisos];
        this.llamadasBajar = new boolean[cantidadPisos];
        this.detenerElevadores = false;
        this.mLogger = ElevatorLogger.cache(this.id);
    }

    public void mover(double duracionMovimiento) {
        boolean[] pisosPendientes = getTableroInterno();
        long versionInicial = getVersionEstado();
        for (int i = 0; i < pisosPendientes.length && !detenerElevadores
                && versionInicial == getVersionEstado(); i++) {
            if (pisosPendientes[i]) {
                pisoDestino = i + 1;
                while (pisoActual < pisoDestino && !detenerElevadores
                        && versionInicial == getVersionEstado()) {
                    direccion = Direccion.UP;
                    moverUnPiso(Direccion.UP, duracionMovimiento);
                }
                while (pisoActual > pisoDestino && !detenerElevadores
                        && versionInicial == getVersionEstado()) {
                    direccion = Direccion.DOWN;
                    moverUnPiso(Direccion.DOWN, duracionMovimiento);
                }
                if (!detenerElevadores && versionInicial == getVersionEstado())
                    parada(tiempoEspera);
            }
        }
    }

    public void parada(double tiempoEspera) {
        parada(tiempoEspera, direccion);
    }

    public int getId() {
        return id;
    }

    public int getPisoActual() {
        return pisoActual;
    }

    public int getPisoDestino() {
        return pisoDestino;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    public synchronized int getCantidadPisos() {
        return destinosInternos.length;
    }

    public synchronized void resetElevador() {
        cola.clear();
        limpiarSolicitudes();
        versionEstado++;
        direccion = Direccion.UP;
        pisoDestino = 1;
        pisoActual = 1;
        mLogger.info("Elevador #" + id + " fue reseteado al piso 1");
    }

    public synchronized void agregarSolicitudExterna(SolicitudExterna solicitud) {
        validarPiso(solicitud.getPiso());
        cola.offer(solicitud);
    }

    public synchronized void agregarSolicitudInterna(SolicitudInterna solicitud) {
        validarPiso(solicitud.getPiso());
        cola.offer(solicitud);
    }

    public synchronized boolean[] getTableroInterno() {
        boolean[] tablero = new boolean[destinosInternos.length];
        for (int i = 0; i < tablero.length; i++)
            tablero[i] = destinosInternos[i] || llamadasSubir[i] || llamadasBajar[i];
        for (SolicitudElevador solicitud : cola)
            tablero[solicitud.getPiso() - 1] = true;
        return tablero;
    }

    public synchronized int getCantidadParadasPendientes() {
        boolean[] tablero = getTableroInterno();
        int cantidad = 0;
        for (boolean pendiente : tablero)
            if (pendiente)
                cantidad++;
        return cantidad;
    }

    public synchronized boolean tieneParadasEnRuta(Direccion direccionRuta) {
        int inicio = direccionRuta == Direccion.UP ? pisoActual - 1 : 0;
        int fin = direccionRuta == Direccion.UP ? destinosInternos.length : pisoActual;
        for (int i = inicio; i < fin; i++) {
            if (destinosInternos[i]
                    || (direccionRuta == Direccion.UP && llamadasSubir[i])
                    || (direccionRuta == Direccion.DOWN && llamadasBajar[i]))
                return true;
        }
        for (SolicitudElevador solicitud : cola) {
            boolean enRuta = direccionRuta == Direccion.UP
                    ? solicitud.getPiso() >= pisoActual
                    : solicitud.getPiso() <= pisoActual;
            if (enRuta && solicitud instanceof SolicitudInterna)
                return true;
            if (enRuta && solicitud instanceof SolicitudExterna) {
                SolicitudExterna externa = (SolicitudExterna) solicitud;
                if (externa.getDireccion() == direccionRuta)
                    return true;
            }
        }
        return false;
    }

    public synchronized String getPisosDestinoTexto() {
        StringBuilder pisos = new StringBuilder();
        for (int i = 0; i < destinosInternos.length; i++) {
            boolean pendiente = destinosInternos[i];
            for (SolicitudElevador solicitud : cola) {
                if (solicitud instanceof SolicitudInterna && solicitud.getPiso() == i + 1)
                    pendiente = true;
            }
            if (pendiente) {
                if (pisos.length() > 0)
                    pisos.append(", ");
                pisos.append(i + 1);
            }
        }
        return pisos.length() == 0 ? "Ninguno" : pisos.toString();
    }

    public synchronized String getLlamadasExternasTexto() {
        StringBuilder llamadas = new StringBuilder();
        for (int i = 0; i < llamadasSubir.length; i++) {
            boolean subir = llamadasSubir[i];
            boolean bajar = llamadasBajar[i];
            for (SolicitudElevador solicitud : cola) {
                if (solicitud instanceof SolicitudExterna && solicitud.getPiso() == i + 1) {
                    SolicitudExterna externa = (SolicitudExterna) solicitud;
                    if (externa.getDireccion() == Direccion.UP)
                        subir = true;
                    else
                        bajar = true;
                }
            }
            if (subir) {
                if (llamadas.length() > 0)
                    llamadas.append(", ");
                llamadas.append(i + 1).append(" ↑");
            }
            if (bajar) {
                if (llamadas.length() > 0)
                    llamadas.append(", ");
                llamadas.append(i + 1).append(" ↓");
            }
        }
        return llamadas.length() == 0 ? "Ninguna" : llamadas.toString();
    }

    public void setDetenerElevadores() {
        detenerElevadores = true;
    }

    private synchronized void validarPiso(int piso) {
        if (piso < 1 || piso > destinosInternos.length)
            throw new IllegalArgumentException("El piso solicitado está fuera de rango");
    }

    synchronized void registrarLlamadaExterna(int piso, Direccion direccionLlamada) {
        if (direccionLlamada == Direccion.UP)
            llamadasSubir[piso - 1] = true;
        else
            llamadasBajar[piso - 1] = true;
    }

    synchronized void registrarDestinoInterno(int piso) {
        destinosInternos[piso - 1] = true;
    }

    private synchronized void procesarSolicitudes() {
        SolicitudElevador solicitud;
        while ((solicitud = cola.poll()) != null) {
            solicitud.registrarEn(this);
            mLogger.info("Elevador #" + id + " registró " + solicitud);
        }
    }

    private synchronized void limpiarSolicitudes() {
        for (int i = 0; i < destinosInternos.length; i++) {
            destinosInternos[i] = false;
            llamadasSubir[i] = false;
            llamadasBajar[i] = false;
        }
    }

    private synchronized long getVersionEstado() {
        return versionEstado;
    }

    private synchronized boolean hayParadasPendientes() {
        return getCantidadParadasPendientes() > 0;
    }

    private synchronized boolean paradasArriba() {
        for (int i = pisoActual; i < destinosInternos.length; i++)
            if (destinosInternos[i] || llamadasSubir[i] || llamadasBajar[i])
                return true;
        for (SolicitudElevador solicitud : cola)
            if (solicitud.getPiso() > pisoActual)
                return true;
        return false;
    }

    private synchronized boolean paradasAbajo() {
        for (int i = pisoActual - 2; i >= 0; i--)
            if (destinosInternos[i] || llamadasSubir[i] || llamadasBajar[i])
                return true;
        for (SolicitudElevador solicitud : cola)
            if (solicitud.getPiso() < pisoActual)
                return true;
        return false;
    }

    private synchronized boolean paradaEnPisoActual(Direccion direccionViaje) {
        int indice = pisoActual - 1;
        return destinosInternos[indice]
                || (direccionViaje == Direccion.UP && llamadasSubir[indice])
                || (direccionViaje == Direccion.DOWN && llamadasBajar[indice]);
    }

    private synchronized boolean llamadaEnPisoActual(Direccion direccionLlamada) {
        int indice = pisoActual - 1;
        return direccionLlamada == Direccion.UP ? llamadasSubir[indice] : llamadasBajar[indice];
    }

    private void cambiarDireccion(Direccion nuevaDireccion) {
        if (direccion != nuevaDireccion) {
            direccion = nuevaDireccion;
            mLogger.info("Elevador #" + id + " cambió dirección a " + direccion);
        }
    }

    private void moverUnPiso(Direccion direccionMovimiento, double duracion) {
        long versionAntesDeMoverse;
        int destino;
        synchronized (this) {
            versionAntesDeMoverse = versionEstado;
            destino = direccionMovimiento == Direccion.UP ? pisoActual + 1 : pisoActual - 1;
            pisoDestino = destino;
        }
        mLogger.info("Elevador #" + id + " va "
                + (direccionMovimiento == Direccion.UP ? "subiendo" : "bajando") + " al piso " + destino);
        if (!esperar((long) duracion))
            return;
        synchronized (this) {
            if (versionAntesDeMoverse != versionEstado || detenerElevadores)
                return;
            pisoActual = destino;
        }
        mLogger.info("Elevador #" + id + " se movió al piso " + destino);
    }

    private void parada(double tiempo, Direccion direccionAtendida) {
        long versionAntesDeParar;
        int piso;
        synchronized (this) {
            versionAntesDeParar = versionEstado;
            piso = pisoActual;
        }
        if (!esperar((long) tiempo))
            return;
        synchronized (this) {
            if (versionAntesDeParar != versionEstado || detenerElevadores)
                return;
            int indice = piso - 1;
            destinosInternos[indice] = false;
            if (direccionAtendida == Direccion.UP)
                llamadasSubir[indice] = false;
            else
                llamadasBajar[indice] = false;
        }
        mLogger.info("Elevador #" + id + " se detuvo en el piso " + piso);
    }

    private boolean esperar(long milisegundos) {
        try {
            Thread.sleep(milisegundos);
        } catch (InterruptedException e) {
            if (!detenerElevadores)
                Thread.currentThread().interrupt();
            return false;
        }
        return !detenerElevadores;
    }

    @Override
    public void run() {
        while (!detenerElevadores) {
            procesarSolicitudes();

            if (!hayParadasPendientes()) {
                esperar(100);
                continue;
            }

            if (paradaEnPisoActual(direccion))
                parada(tiempoEspera, direccion);

            if (direccion == Direccion.UP) {
                if (paradasArriba()) {
                    while (!detenerElevadores) {
                        procesarSolicitudes();
                        if (!paradasArriba())
                            break;
                        moverUnPiso(Direccion.UP, duracionMovimiento);
                        procesarSolicitudes();
                        if (!detenerElevadores && paradaEnPisoActual(Direccion.UP))
                            parada(tiempoEspera, Direccion.UP);
                    }
                    if (paradasAbajo() || llamadaEnPisoActual(Direccion.DOWN))
                        cambiarDireccion(Direccion.DOWN);
                } else if (paradasAbajo() || llamadaEnPisoActual(Direccion.DOWN)) {
                    cambiarDireccion(Direccion.DOWN);
                }
            } else {
                if (paradasAbajo()) {
                    while (!detenerElevadores) {
                        procesarSolicitudes();
                        if (!paradasAbajo())
                            break;
                        moverUnPiso(Direccion.DOWN, duracionMovimiento);
                        procesarSolicitudes();
                        if (!detenerElevadores && paradaEnPisoActual(Direccion.DOWN))
                            parada(tiempoEspera, Direccion.DOWN);
                    }
                    if (paradasArriba() || llamadaEnPisoActual(Direccion.UP))
                        cambiarDireccion(Direccion.UP);
                } else if (paradasArriba() || llamadaEnPisoActual(Direccion.UP)) {
                    cambiarDireccion(Direccion.UP);
                }
            }
        }
        mLogger.info("Elevador #" + id + " ha terminado el ciclo");
    }
}
