package domain;

/**
 * Perfil de máquina Experta.
 * Decide su acción según el estado del partido: si va perdiendo o
 * empatando ataca y persigue el balón, y si va ganando defiende su
 * arco, saliendo solo cuando el balón queda cerca.
 */
public class Expert extends Bot {

    /**
     * Crea un bot con el perfil Experta.
     */
    public Expert() {
        super();
    }

    /**
     * Compara el marcador propio contra el del rival para elegir entre
     * atacar persiguiendo el balón o defender su arco, salta cuando el
     * balón está cerca y por encima suyo, y al tenerlo al alcance del
     * pie activa su poder si está disponible y pide patear.
     * @param match partido en curso sobre el que el bot toma su decisión
     */
    @Override
    public void decideAction(Match match) {
        Ball ball = match.getBall();
        Score score = match.getScore();
        int myGoals = score.getGoalsRightPlayer();
        int opponentGoals = score.getGoalsLeftPlayer();
        double dx = ball.getPositionX() - getPositionX();
        double dist = Math.abs(dx);

        if (myGoals <= opponentGoals) {
            // va perdiendo o empatando: ataca, persigue el balon
            if (dx > 10) {
                moveRight();
            } else if (dx < -10) {
                moveLeft();
            }
        } else {
            // va ganando: defiende su arco, solo sale si el balon esta cerca
            Goal myGoal = match.getRightGoal();
            double guardX = myGoal.getPositionX() - 90;
            if (dist < 130) {
                if (dx > 10) {
                    moveRight();
                } else if (dx < -10) {
                    moveLeft();
                }
            } else {
                if (getPositionX() < guardX - 10) {
                    moveRight();
                } else if (getPositionX() > guardX + 10) {
                    moveLeft();
                }
            }
        }

        if (ball.getPositionY() < getPositionY() - 20 && dist < 60) {
            jump();
        }

        if (dist < 55) {
            if (getPowerBar().isFull() && !isAuraActive()) {
                usePower();
            }
            wantsToKick = true;
        }
    }
}