package Elevator;

public class SolicitudElevador {
    private final int piso;
    private final Direccion direccion;
    
    public SolicitudElevador(int piso, Direccion direccion) {
        if (piso < 1) {
            throw new IllegalArgumentException("El piso debe ser mayor o igual a 1");
        }
        if (direccion == null) {
            throw new IllegalArgumentException("La dirección no puede ser nula");
        }
        this.piso = piso;
        this.direccion = direccion;
    }
    public int getPiso() {
        return this.piso;
    }
    
    public Direccion getDireccion() {
        return this.direccion;
    }
    @Override
    public String toString() {
        return "Solicitud de Elevador en el [piso= " + piso + " y direccion= " + direccion + "]";
    }
}

