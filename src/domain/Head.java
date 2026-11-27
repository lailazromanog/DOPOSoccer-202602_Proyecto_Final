package domain;
import java.io.Serializable;

/**
 * Representa la cabeza del personaje, usada por la presentación para
 * elegir la imagen con la que se dibuja.
 */
public class Head implements Serializable{
    private String color;
    private String type; //nombre base de la imagen del personaje

    /**
     * Crea la cabeza de un personaje con su nombre base de imagen y el
     * color por defecto.
     * @param type nombre base de la imagen del personaje
     */
    public Head(String type) {
        this.type = type;
        this.color = "default";
    }

    /**
     * Entrega el color de la cabeza del personaje.
     * @return el color de la cabeza
     */
    public String getColor() {
        return color;
    }

    /**
     * Entrega el nombre base de la imagen del personaje.
     * @return el nombre base de la imagen
     */
    public String getType() {
        return type;
    }
}
