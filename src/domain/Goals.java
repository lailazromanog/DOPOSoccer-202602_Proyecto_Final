package domain;

/**
 * Partido jugado por goles.
 * Gana el primer jugador que llegue al número de goles predefinido,
 * sin límite de tiempo.
 */
public class Goals extends Match {
    private int goalLimit;

    /**
     * Crea un partido por goles con el número de goles para ganar dado.
     * @param leftPlayer jugador que defiende el lado izquierdo de la cancha
     * @param rightPlayer jugador que defiende el lado derecho de la cancha
     * @param ball balón del partido
     * @param leftGoal arco izquierdo de la cancha
     * @param rightGoal arco derecho de la cancha
     * @param timer cronómetro del partido
     * @param score marcador del partido
     * @param goalLimit número de goles que se necesita anotar para ganar el partido
     */
    public Goals(Player leftPlayer, Player rightPlayer, Ball ball, Goal leftGoal, Goal rightGoal, Timer timer, Score score, int goalLimit) {
        super(leftPlayer, rightPlayer, ball, leftGoal, rightGoal, timer, score);
        this.goalLimit = goalLimit;
    }

    /**
     * Indica si alguno de los dos jugadores ya llegó al número de
     * goles predefinido para ganar el partido.
     * @return verdadero si algún jugador alcanzó el número de goles para ganar, falso en caso contrario
     */
    @Override
    public boolean checkWinCondition(){
        return score.getGoalsLeftPlayer() >= goalLimit || score.getGoalsRightPlayer() >= goalLimit;
    }

    /**
     * Entrega el número de goles que se necesita anotar para ganar el partido.
     * @return el número de goles para ganar
     */
    public int getGoalLimit(){
        return goalLimit;
    }
}
