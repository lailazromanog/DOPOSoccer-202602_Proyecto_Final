package presentation;

/**
 * Catalogo de resoluciones de la ventana.
 *
 * El mundo del juego siempre es 769x450 ; la resolucion solo cambia el
 * tamaño de la ventana. Agregar una resolucion nueva = agregar una constante
 * aqui; ConfigPanel y el resto NO cambian.
 */
public enum Resolution {
    SMALL("Pequena (640x375)", 640, 375),
    MEDIUM("Mediana (769x450)", 769, 450),
    LARGE("Grande (1024x600)", 1024, 600);

    private final String label;
    private final int width;
    private final int height;

    /**
     * Crea una resolución con su etiqueta para mostrar y el ancho y
     * el alto de ventana que le corresponden.
     * @param label etiqueta de la resolución, para mostrarla en el combo de selección
     * @param width ancho de la ventana para esta resolución
     * @param height alto de la ventana para esta resolución
     */
    Resolution(String label, int width, int height) {
        this.label = label;
        this.width = width;
        this.height = height;
    }

    /**
     * Entrega el ancho de ventana de esta resolución.
     * @return el ancho de ventana de esta resolución
     */
    public int getWidth() {
        return width;
    }

    /**
     * Entrega el alto de ventana de esta resolución.
     * @return el alto de ventana de esta resolución
     */
    public int getHeight() {
        return height;
    }

    /**
     * Entrega la etiqueta de esta resolución, para mostrarla en el
     * combo de selección.
     * @return la etiqueta de esta resolución
     */
    @Override
    public String toString() {
        return label;   // lo que se muestra en el combo
    }
}