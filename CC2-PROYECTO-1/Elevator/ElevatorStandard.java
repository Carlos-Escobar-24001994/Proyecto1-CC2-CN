package Elevator;

import java.util.concurrent.ConcurrentLinkedQueue;

public class ElevatorStandard extends Elevator {

    public ElevatorStandard(double tiempoEspera, int cantidadPisos, double duracionMovimiento) {
        super(tiempoEspera, cantidadPisos, duracionMovimiento);
    }

    public void mover(double duracionMovimiento) {
        super.mover(duracionMovimiento);
    }

    public void parada(double tiempoEspera) {
        super.parada(tiempoEspera);
    }

    public int getId() {
        return super.getId();
    }

    @Override
    public int getPisoActual() {
        return super.getPisoActual();
    }

    @Override
    public int getPisoDestino() {
        return super.getPisoDestino();
    }

    @Override
    public Direccion getDireccion() {
        return super.getDireccion();
    }

    @Override
    public void resetElevador() {
        super.resetElevador();
    }

    @Override
    public void agregarSolicitud(int x) {
        super.agregarSolicitud(x);
    }

    @Override
    public ConcurrentLinkedQueue<Integer> getsolicitudes() {
        return super.getsolicitudes();
    }

    @Override
    public boolean[] getTableroInterno() {
        return super.getTableroInterno();
    }

}
