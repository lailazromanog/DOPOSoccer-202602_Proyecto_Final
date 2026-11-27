package domain;

/**
 * Representa a un jugador controlado por la máquina.
 * Cada subclase concreta define el perfil de juego propio, es decir
 * la manera en que decide moverse, saltar, patear y usar su poder en
 * cada instante del partido.
 *
 * Punto de extensión: para agregar un perfil de máquina nuevo basta
 * con crear una subclase propia que decida la acción según sus
 * propias reglas, y registrarla en la fábrica de bots. El resto del
 * juego trabaja contra esta clase y no necesita cambios.
 */
public abstract class Bot extends Player {

    protected boolean wantsToKick;   // el bot pide patear; Game ejecuta la patada real

    /**
     * Crea un bot con la vida, las estadísticas y la barra de poder
     * iniciales propias de todo jugador.
     */
    public Bot() {
        super();
        wantsToKick = false;
    }

    /**
     * Decide y ejecuta la acción del bot para el instante actual del
     * partido, según el estado del balón, del marcador o de la cancha
     * que necesite su propio perfil de juego.
     * @param match partido en curso sobre el que el bot toma su decisión
     */
    public abstract void decideAction(Match match);

    /**
     * Indica si el bot pidió patear en la decisión más reciente.
     * @return verdadero si el bot quiere patear, falso en caso contrario
     */
    public boolean wantsToKick() {
        return wantsToKick;
    }

    /**
     * Limpia la intención de patear del bot, una vez que ya se ejecutó
     * la patada correspondiente.
     */
    public void clearKickIntent() {
        wantsToKick = false;
    }
}