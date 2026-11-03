package Elevator;

public class SolicitudInterna extends SolicitudElevador {

    public SolicitudInterna(int piso) {
        super(piso);
    }

    @Override
    void registrarEn(Elevator elevador) {
        elevador.registrarDestinoInterno(getPiso());
    }

    @Override
    public String toString() {
        return "Destino interno en piso " + getPiso();
    }
}
