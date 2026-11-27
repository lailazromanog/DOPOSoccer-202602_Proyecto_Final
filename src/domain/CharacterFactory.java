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

    /**
     * Crea el personaje correspondiente al país elegido, con su tiro
     * de poder propio y su apariencia.
     * @param country país del personaje a crear
     * @return el personaje creado para ese país
     * @throws DOPOSoccerException si el país dado no corresponde a ningún personaje disponible
     */
    public Character create(String country) throws DOPOSoccerException {
        if (country.equals("Camerun")) {
            return new Character("Camerun", "Camerun", new Lightning(), new Appereance("camerun"));
        } else if (country.equals("Nigeria")) {
            return new Character("Nigeria", "Nigeria", new Tornado(), new Appereance("nigeria"));
        } else if (country.equals("Estados Unidos")) {
            return new Character("Estados Unidos", "Estados Unidos", new Illusion(), new Appereance("usa"));
        } else if (country.equals("Corea del Sur")) {
            return new Character("Corea del Sur", "Corea del Sur", new BlueAura(), new Appereance("corea"));
        } else {
            throw new DOPOSoccerException("Pais no reconocido: " + country);
        }
    }
}