package Elevator;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Logger;
import Elevator.components.ElevatorLogger;

public class Elevator implements Movimientos, Runnable {
    static int cantidadElevadores = 1;
    protected boolean[] tableroInterno;
    private final Logger mLogger;
    protected int id, pisoActual, pisoDestino;
    protected double tiempoEspera, duracionMovimiento;
    protected boolean detenerElevadores;
    protected ConcurrentLinkedQueue<Integer> cola;
    protected Direccion direccion;

    public Elevator(double tiempoEspera, int cantidadPisos, double duracionMovimiento) {
        this.tiempoEspera = tiempoEspera;
        this.direccion = Direccion.UP;
        this.duracionMovimiento = duracionMovimiento;
        this.pisoActual = 1;
        this.id = cantidadElevadores++;
        this.cola = new ConcurrentLinkedQueue<>();
        this.tableroInterno = new boolean[cantidadPisos];
        for (int i = 0; i < this.tableroInterno.length; i++)
            this.tableroInterno[i] = false;
        this.detenerElevadores = false;
        this.mLogger = ElevatorLogger.cache(this.id);
    }

    public void mover(double duracionMovimiento) {
        for (int i = 0; i < this.tableroInterno.length; i++) {
            if (this.tableroInterno[i]) {
                this.pisoDestino = i + 1;
                if (this.pisoActual < this.pisoDestino) {
                    this.direccion = Direccion.UP;
                    for (int j = this.pisoActual; j < pisoDestino; j++) {
                        try {
                            Thread.sleep((long) (duracionMovimiento * 1000));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        this.pisoActual++;
                    }
                } else if (this.pisoActual > this.pisoDestino) {
                    this.direccion = Direccion.DOWN;
                    for (int j = this.pisoActual; j > pisoDestino; j--) {
                        try {
                            Thread.sleep((long) (duracionMovimiento * 1000));
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        this.pisoActual--;
                    }
                }
            }
        }
    }

    public void parada(double tiempoEspera) {
        try {
            Thread.sleep((long) (tiempoEspera * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (pisoActual >= 1 && pisoActual <= tableroInterno.length)
            this.tableroInterno[pisoActual - 1] = false;
        if (mLogger != null)
            mLogger.info("Elevador #" + this.id + " se detuvo en el piso " + this.pisoActual);
    }

    public int getId() {
        return this.id;
    }

    public int getPisoActual() {
        return this.pisoActual;
    }

    public int getPisoDestino() {
        return this.pisoDestino;
    }

    public Direccion getDireccion() {
        return this.direccion;
    }

    public void resetElevador() {
        this.direccion = Direccion.DOWN;
        this.pisoDestino = 1;
        for (int i = this.pisoActual; i > pisoDestino; i--)
            this.pisoActual--;
        this.pisoActual = 1;
        mLogger.info("Elevador #" + this.id + " fue reseteado al piso 1");
    }

    public void agregarSolicitud(int x) {
        if (x >= 1 && x <= tableroInterno.length) {
            this.tableroInterno[x - 1] = true;
            this.cola.add(x);
        }
    }

    public ConcurrentLinkedQueue<Integer> getsolicitudes() {
        return this.cola;
    }

    public boolean[] getTableroInterno() {
        return this.tableroInterno;
    }

    public void setDetenerElevadores() {
        this.detenerElevadores = true;
    }

    private boolean hayParadasPendientes() {
        for (boolean parada : this.tableroInterno)
            if (parada)
                return true;
        return false;
    }

    private boolean paradasArriba() {
        for (int i = this.pisoActual; i < this.tableroInterno.length; i++)
            if (this.tableroInterno[i])
                return true;
        return false;
    }

    private boolean paradasAbajo() {
        for (int i = pisoActual - 2; i >= 0; i--)
            if (this.tableroInterno[i])
                return true;
        return false;
    }

    private void cambiarDireccion(Direccion nuevaDireccion) {
        if (this.direccion != nuevaDireccion) {
            this.direccion = nuevaDireccion;
            if (mLogger != null)
                mLogger.info("Elevador #" + this.id + " cambió dirección a " + this.direccion);
        }
    }

    private void moverUnPiso(Direccion direccion) {
        int destino = (direccion == Direccion.UP) ? this.pisoActual + 1 : this.pisoActual - 1;
        try {
            Thread.sleep((long) (duracionMovimiento * 1000));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        pisoActual = destino;
        if (mLogger != null)
            mLogger.info("Elevador #" + this.id + " se movió al piso " + this.pisoActual);
    }

    @Override
    public void run() {
        while (!this.detenerElevadores) {
            Integer p;
            while ((p = cola.poll()) != null) {
                if (p >= 1 && p <= tableroInterno.length) {
                    this.tableroInterno[p - 1] = true;
                    if (mLogger != null)
                        mLogger.info("Elevador #" + this.id + " marcó piso " + p + " (Solicitud)");
                }
            }

            if (!hayParadasPendientes()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            if (direccion == Direccion.UP) {
                if (paradasArriba()) {
                    while (paradasArriba()) {
                        moverUnPiso(Direccion.UP);
                        if (tableroInterno[pisoActual - 1])
                            parada(tiempoEspera);
                    }
                    if (paradasAbajo())
                        cambiarDireccion(Direccion.DOWN);
                } else if (paradasAbajo()) {
                    cambiarDireccion(Direccion.DOWN);
                }
            } else {
                if (paradasAbajo()) {
                    while (paradasAbajo()) {
                        moverUnPiso(Direccion.DOWN);
                        if (tableroInterno[pisoActual - 1])
                            parada(tiempoEspera);
                    }
                    if (paradasArriba())
                        cambiarDireccion(Direccion.UP);
                } else if (paradasArriba()) {
                    cambiarDireccion(Direccion.UP);
                }
            }
        }
        if (mLogger != null)
            mLogger.info("Elevador #" + this.id + " ha terminado el ciclo");
    }
}
