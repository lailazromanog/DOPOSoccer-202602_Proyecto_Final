package presentation;

/**
 * Catalogo de resoluciones de la ventana.
 *
 * El mundo del juego siempre es 769x450 ; la resolucion solo cambia el
 * tamaño de la ventana. Agregar una resolucion nueva = agregar una constante
 * aqui; ConfigPanel y el resto NO cambian.
 */
public enum Resolution {
    PEQUENA("Pequena (640x375)", 640, 375),
    MEDIANA("Mediana (769x450)", 769, 450),
    GRANDE("Grande (1024x600)", 1024, 600);

    private final String label;
    private final int width;
    private final int height;

    Resolution(String label, int width, int height) {
        this.label = label;
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public String toString() {
        return label;   // lo que se muestra en el combo
    }
}