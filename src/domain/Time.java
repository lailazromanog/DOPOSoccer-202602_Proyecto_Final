package domain;

/**
 * Partido jugado por tiempo.
 * Dura un número de minutos predefinido y gana el jugador que tenga
 * más goles al finalizar el tiempo reglamentario. En caso de empate,
 * el partido entra en muerte súbita y gana el primer jugador que
 * marque un gol.
 */
public class Time extends Match {
    private boolean goldenGoal;

    /**
     * Crea un partido por tiempo, con el cronómetro que define los
     * minutos de duración del partido.
     * @param leftPlayer jugador que defiende el lado izquierdo de la cancha
     * @param rightPlayer jugador que defiende el lado derecho de la cancha
     * @param ball balón del partido
     * @param leftGoal arco izquierdo de la cancha
     * @param rightGoal arco derecho de la cancha
     * @param timer cronómetro con la duración del partido
     * @param score marcador del partido
     */
    public Time(Player leftPlayer, Player rightPlayer, Ball ball, Goal leftGoal, Goal rightGoal, Timer timer, Score score) {
        super(leftPlayer, rightPlayer, ball, leftGoal, rightGoal, timer, score);
        this.goldenGoal = false;
    }

    /**
     * Indica si el partido ya se decidió: al agotarse el tiempo
     * reglamentario con un marcador distinto, o durante la muerte
     * súbita apenas un jugador se pone por encima del otro en goles.
     * Si el tiempo se agota con el marcador empatado, el partido entra
     * en muerte súbita en lugar de terminar.
     * @return verdadero si el partido ya tiene un resultado definitivo, falso en caso contrario
     */
    @Override
    public boolean checkWinCondition(){
        if (timer.isTimeUp()){
            int goalsOne = score.getGoalsLeftPlayer();
            int goalsTwo = score.getGoalsRightPlayer();

            if (goalsOne != goalsTwo){
                return true;
            } else {
                goldenGoal = true;
                return false;
            }
        }

        if(goldenGoal){
            int goalsOne = score.getGoalsLeftPlayer();
            int goalsTwo = score.getGoalsRightPlayer();

            return goalsOne != goalsTwo;
            
        }

        return false;
    }

    /**
     * Indica si el partido se encuentra en muerte súbita por haber
     * terminado el tiempo reglamentario con el marcador empatado.
     * @return verdadero si el partido está en muerte súbita, falso en caso contrario
     */
    public boolean isGoldenGoal(){
        return goldenGoal;
    }
}
