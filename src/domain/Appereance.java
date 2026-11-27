package domain;
import java.io.Serializable;

/**
 * Representa la apariencia visual de un personaje.
 * Reúne la cabeza y los zapatos del personaje, ambos con el mismo
 * nombre base de imagen, para que la presentación pueda dibujarlo de
 * forma única y fácilmente distinguible de los demás.
 */
public class Appereance implements Serializable{
    private Head head;
    private Shoes shoes;

    /**
     * Crea la apariencia de un personaje a partir del nombre base de su
     * imagen, armando su cabeza y sus zapatos con ese mismo nombre.
     * @param type nombre base de la imagen del personaje
     */
    public Appereance(String type) {
        this.head = new Head(type);
        this.shoes = new Shoes(type);
    }

    /**
     * Entrega la cabeza del personaje.
     * @return la cabeza del personaje
     */
    public Head getHead() {
        return head;
    }

    /**
     * Entrega los zapatos del personaje.
     * @return los zapatos del personaje
     */
    public Shoes getShoes() {
        return shoes;
    }
}