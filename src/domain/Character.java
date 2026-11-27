package domain;
import java.io.Serializable;

/**
 * Representa al personaje que controla un jugador.
 * Reúne el país que representa, su tiro de poder propio y su
 * apariencia, y es creado por la fábrica de personajes a partir del
 * país elegido.
 *
 * Punto de extensión: para agregar un personaje nuevo basta con
 * entregarle su propio tiro de poder y su propia apariencia al crearlo,
 * sin necesidad de una subclase ni de cambios en el resto del juego.
 */
public class Character implements Serializable{
    private String name;
    private String country;
    private Power power;
    private Appereance appereance;

    /**
     * Crea un personaje con su nombre, el país que representa, su tiro
     * de poder propio y su apariencia.
     * @param name nombre del personaje
     * @param country país que representa el personaje
     * @param power tiro de poder propio del personaje
     * @param appereance apariencia del personaje, usada por la presentación para dibujarlo
     */
    public Character(String name, String country, Power power, Appereance appereance) {
        this.name = name;
        this.country = country;
        this.power = power;
        this.appereance = appereance;
    }

    /**
     * Entrega el nombre del personaje.
     * @return el nombre del personaje
     */
    public String getName() {
        return name;
    }

    /**
     * Entrega el país que representa el personaje.
     * @return el país del personaje
     */
    public String getCountry() {
        return country;
    }

    /**
     * Entrega el tiro de poder propio del personaje.
     * @return el tiro de poder del personaje
     */
    public Power getPower() {
        return power;
    }

    /**
     * Entrega la apariencia del personaje, usada por la presentación
     * para elegir la imagen con la que se dibuja.
     * @return la apariencia del personaje
     */
    public Appereance getAppereance() {
        return appereance;
    }
}
