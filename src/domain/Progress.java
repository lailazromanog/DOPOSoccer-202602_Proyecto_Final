package domain;
import java.io.File;
/**
 * Fachada del progreso de los jugadores entre partidos.
 * Es la única puerta que usa la presentación para consultar y mejorar
 * los puntos y las estadísticas de cada jugador, sin conocer los
 * perfiles ni su almacén. También crea los partidos nuevos, de modo
 * que cada partido use el progreso vigente de ambos jugadores.
 * El progreso vive mientras la aplicación está abierta.
 *
 * Los jugadores se identifican con las constantes de esta clase, 1
 * para el jugador izquierdo y 2 para el derecho.
 */

public class Progress {
    
    //Identificador del jugador del lado izquierdo 
    public static final int LEFT = 1; //--> extensibilidad

    //Identificador del jugador del lado derecho
    public static final int RIGHT = 2; //--> extensibilidad

    private ProfileStore store;

    /**
     * Crea el progreso con un perfil nuevo para cada jugador, con sus
     * puntos en cero y sus estadísticas en el nivel mínimo.
     */
    public Progress(){
        store = new ProfileStore();
    }

    /**
     * Entrega el perfil del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @return el perfil de ese jugador
     * @throws DOPOSoccerException si el identificador no corresponde a ningún jugador
     */
    private Profile profileOf(int player) throws DOPOSoccerException {
        if (player == LEFT) {
            return store.getLeft();
        } else if (player == RIGHT) {
            return store.getRight();
        } else {
            throw new DOPOSoccerException("Numero de jugador invalido: " + player + ". Los validos son: " + LEFT + " para el juador izquierdo y " + RIGHT + " para el derecho.");
        }
    }

    /**
     * Crea un partido nuevo con el progreso vigente de ambos jugadores.
     * @param matchType tipo de partido elegido, por tiempo o por goles
     * @param value minutos de duración del partido si es por tiempo, o número de goles para ganar si es por goles
     * @param country1 país del personaje del jugador izquierdo
     * @param country2 país del personaje del jugador derecho
     * @param opponent modalidad del jugador derecho, humano o perfil de máquina
     * @return el partido listo para jugarse
     * @throws DOPOSoccerException si el tipo de partido, algún país o el perfil de la máquina no son reconocidos
     */
    public Game newGame(String matchType, int value, String country1, String country2, String opponent) throws DOPOSoccerException{
        return new Game(matchType, value, country1, country2, opponent, store.getLeft(), store.getRight());
    }

    /**
     * Abre el partido guardado en el archivo dado y lo asocia al
     * progreso vigente de ambos jugadores, de modo que al terminar
     * sus puntos se sumen a ese progreso.
     * @param file archivo del que se quiere abrir el partido
     * @return el partido reconstruido, listo para continuar
     * @throws DOPOSoccerException si el archivo no contiene un partido válido o no se puede leer
     */
    public Game openGame(File file) throws DOPOSoccerException{
        Game game = Game.openFrom(file);
        game.attachProfiles(store.getLeft(), store.getRight());
        return game;
    }

    /**
     * Entrega los puntos acumulados del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @return los puntos acumulados de ese jugador
     * @throws DOPOSoccerException si el identificador no corresponde a ningún jugador
     */
    public int getPoints(int player) throws DOPOSoccerException{
        return profileOf(player).getPoints();
    }

    /**
     * Entrega el nivel actual de una estadística del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return el nivel actual de esa estadística
     * @throws DOPOSoccerException si el identificador o el nombre de la estadística no son válidos
     */
    public int getLevel(int player, String stat) throws DOPOSoccerException{
        return profileOf(player).getLevel(stat);
    }

     /**
     * Entrega el precio en puntos del próximo nivel de una estadística del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return el precio del próximo nivel
     * @throws DOPOSoccerException si el identificador o el nombre de la estadística no son válidos
     */
    public int getUpgradeCost(int player, String stat) throws DOPOSoccerException{
        return profileOf(player).upgradeCost(stat);
    }

    /**
     * Indica si una estadística del jugador dado se puede mejorar, es
     * decir si no está en su nivel máximo y le alcanzan los puntos.
     * @param player identificador del jugador, izquierdo o derecho
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return verdadero si la estadística se puede mejorar, falso en caso contrario
     * @throws DOPOSoccerException si el identificador o el nombre de la estadística no son válidos
     */
    public boolean canUpgrade(int player, String stat) throws DOPOSoccerException{
        return profileOf(player).canUpgrade(stat);
    }

    /**
     * Indica si una estadística del jugador dado ya está en su nivel máximo.
     * @param player identificador del jugador, izquierdo o derecho
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return verdadero si la estadística está en su nivel máximo, falso en caso contrario
     * @throws DOPOSoccerException si el identificador o el nombre de la estadística no son válidos
     */
    public boolean isMax(int player, String stat) throws DOPOSoccerException{
        return profileOf(player).isMax(stat);
    }

    /**
     * Sube un nivel una estadística del jugador dado, descontando de
     * sus puntos el precio de ese nivel.
     * @param player identificador del jugador, izquierdo o derecho
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @throws DOPOSoccerException si el identificador o el nombre no son válidos, si la estadística ya está en su nivel máximo o si los puntos no alcanzan
     */
    public void upgrade(int player, String stat) throws DOPOSoccerException{
        profileOf(player).upgrade(stat);
    }

    /**
     * Entrega la vida máxima que da el nivel actual de survival del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @return la vida máxima del jugador
     * @throws DOPOSoccerException si el identificador no corresponde a ningún jugador
     */
    public double getMaxHealth(int player) throws DOPOSoccerException{
        return profileOf(player).getMaxHealth();
    }

    /**
     * Entrega el daño de una patada normal con el nivel actual de kick del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @return el daño de una patada normal
     * @throws DOPOSoccerException si el identificador no corresponde a ningún jugador
     */
    public double getKickDamage(int player) throws DOPOSoccerException{
        return profileOf(player).getKickDamage();
    }

    /**
     * Entrega el multiplicador de daño que da el nivel actual de kick del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @return el multiplicador de daño
     * @throws DOPOSoccerException si el identificador no corresponde a ningún jugador
     */
    public double getKickMultiplier(int player) throws DOPOSoccerException{
        return profileOf(player).getKickMultiplier();
    }

    /**
     * Entrega el porcentaje de mejora de velocidad que da el nivel actual de speed del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @return el porcentaje de mejora de la velocidad
     * @throws DOPOSoccerException si el identificador no corresponde a ningún jugador
     */
    public int getSpeedBonusPercent(int player) throws DOPOSoccerException{
        return profileOf(player).getSpeedBonusPercent();
    }

    /**
     * Entrega el porcentaje de mejora de salto que da el nivel actual de jump del jugador dado.
     * @param player identificador del jugador, izquierdo o derecho
     * @return el porcentaje de mejora del salto
     * @throws DOPOSoccerException si el identificador no corresponde a ningún jugador
     */
    public int getJumpBonusPercent(int player) throws DOPOSoccerException{
        return profileOf(player).getJumpBonusPercent();
    }
}