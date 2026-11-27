package domain;
import java.io.Serializable;

/**
 * Lleva el marcador de un partido entre dos jugadores.
 * Cuenta los goles de cada jugador y determina el ganador según
 * quién tenga más goles anotados en un momento dado.
 */
public class Score implements Serializable{
    private int goalsLeftPlayer;
    private int goalsRightPlayer;
    private Player leftPlayer;
    private Player rightPlayer;

    /**
     * Crea el marcador de un partido entre los dos jugadores dados,
     * con el conteo de goles de ambos en cero.
     * @param leftPlayer jugador que defiende el lado izquierdo de la cancha
     * @param rightPlayer jugador que defiende el lado derecho de la cancha
     */
    public Score(Player leftPlayer, Player rightPlayer) {
        this.leftPlayer = leftPlayer;
        this.rightPlayer = rightPlayer;
        this.goalsLeftPlayer = 0;
        this.goalsRightPlayer = 0;
    }

    /**
     * Suma un gol al jugador dado.
     * @param player jugador al que se le anota el gol
     */
    public void addGoal(Player player) {
        if (player == leftPlayer) {
            goalsLeftPlayer++;
        } else {
            goalsRightPlayer++;
        }
    }

    /**
     * Entrega el jugador que va ganando el partido según el conteo de
     * goles actual.
     * @return el jugador con más goles anotados, o nulo si el marcador está empatado
     */
    public Player getWinner() {
        if (goalsLeftPlayer > goalsRightPlayer) {
            return leftPlayer;
        } else if (goalsRightPlayer > goalsLeftPlayer) {
            return rightPlayer;
        }
        return null;
    }

    /**
     * Entrega los goles anotados por el jugador que defiende el lado izquierdo.
     * @return los goles del jugador izquierdo
     */
    public int getGoalsLeftPlayer() {
        return goalsLeftPlayer;
    }

    /**
     * Entrega los goles anotados por el jugador que defiende el lado derecho.
     * @return los goles del jugador derecho
     */
    public int getGoalsRightPlayer() {
        return goalsRightPlayer;
    }
}
