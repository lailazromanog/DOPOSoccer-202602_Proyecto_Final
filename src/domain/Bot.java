package domain;

/**
 * Creador de bots (patron Creador / Fabricacion Pura).
 *
 * Unico lugar que sabe que perfiles de maquina existen y que bot usa cada uno.
 * Game le pide un bot segun el perfil y no conoce las subclases concretas.
 *
 */
public class BotFactory {

    /**
     * Crea el bot correspondiente al perfil de máquina elegido.
     * @param profile perfil de máquina del bot a crear
     * @return el bot creado con ese perfil
     */
    public Bot create(String profile) {
        if (profile.equals("stupid")) {
            return new Stupid();
        } else if (profile.equals("defensive")) {
            return new Defensive();
        } else if (profile.equals("expert")) {
            return new Expert();
        } else {
            return new Offensive();   // offensive
        }
    }
}