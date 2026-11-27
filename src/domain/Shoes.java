package domain;
import java.io.Serializable;

/**
 * Representa los zapatos del personaje, usados por la presentación
 * para elegir la imagen con la que se dibuja.
 */
public class Shoes implements Serializable{
    private String color;
    private String type;   //mismo nombre base de la imagen que la cabeza

    /**
     * Crea los zapatos de un personaje con su nombre base de imagen y
     * el color por defecto.
     * @param type nombre base de la imagen del personaje, el mismo que el de su cabeza
     */
    public Shoes(String type) {
        this.type = type;
        this.color = "default";
    }

    /**
     * Entrega el color de los zapatos del personaje.
     * @return el color de los zapatos
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