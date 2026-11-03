package Elevator;

public abstract class SolicitudElevador {
    private final int piso;

    public SolicitudElevador(int piso) {
        if (piso < 1)
            throw new IllegalArgumentException("El piso debe ser mayor o igual a 1");
        this.piso = piso;
    }

    public int getPiso() {
        return piso;
    }

    abstract void registrarEn(Elevator elevador);
}
