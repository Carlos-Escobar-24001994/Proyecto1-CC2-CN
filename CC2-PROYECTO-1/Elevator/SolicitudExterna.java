package Elevator;

public class SolicitudExterna extends SolicitudElevador {
    private final Direccion direccion;

    public SolicitudExterna(int piso, Direccion direccion) {
        super(piso);
        if (direccion == null)
            throw new IllegalArgumentException("La dirección no puede ser nula");
        this.direccion = direccion;
    }

    public Direccion getDireccion() {
        return direccion;
    }

    @Override
    void registrarEn(Elevator elevador) {
        elevador.registrarLlamadaExterna(getPiso(), direccion);
    }

    @Override
    public String toString() {
        return "Llamada externa en piso " + getPiso() + " dirección " + direccion;
    }
}
