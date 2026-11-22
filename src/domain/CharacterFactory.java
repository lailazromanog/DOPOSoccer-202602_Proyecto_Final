package domain;

/**
 * Creador de personajes 
 *
 * Es el unico lugar que sabe que personajes existen y con que poder y apariencia.
 * Game no conoce los poderes concretos, le pide un Character a la fabrica.
 *
 * Variaciones protegidas: agregar un personaje nuevo = agregar una rama aqui
 * (y su subclase de Power). El resto del juego no cambia.
 */
public class CharacterFactory {

    /** Crea el personaje segun el pais elegido, con su poder y su sprite. */
    public Character create(String country) {
        if (country.equals("Camerun")) {
            return new Character("Camerun", "Camerun", new Lightning(), new Appereance("camerun"));
        } else if (country.equals("Nigeria")) {
            return new Character("Nigeria", "Nigeria", new Tornado(), new Appereance("nigeria"));
        } else if (country.equals("Estados Unidos")) {
            return new Character("Estados Unidos", "Estados Unidos", new Illusion(), new Appereance("usa"));
        } else {
            return new Character("Corea del Sur", "Corea del Sur", new BlueAura(), new Appereance("corea"));
        }
    }
}