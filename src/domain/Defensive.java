package domain;

/**
 * Perfil de máquina Defensiva.
 * Su prioridad es defender su arco: solo sale a despejar el balón
 * cuando lo tiene cerca, y el resto del tiempo vuelve a su puesto de
 * guardia frente al arco propio.
 */
public class Defensive extends Bot {

    /**
     * Crea un bot con el perfil Defensiva.
     */
    public Defensive() {
        super();
    }

    /**
     * Sale a despejar el balón cuando lo tiene cerca y, si está lejos,
     * vuelve a su puesto de guardia frente al arco propio. Salta
     * cuando el balón está cerca y por encima suyo, y al tenerlo al
     * alcance del pie activa su poder si está disponible y pide patear.
     * @param match partido en curso sobre el que el bot toma su decisión
     */
    @Override
    public void decideAction(Match match) {
        Ball ball = match.getBall();
        Goal myGoal = match.getRightGoal();
        double dx = ball.getPositionX() - getPositionX();
        double dist = Math.abs(dx);
        double guardX = myGoal.getPositionX() - 90;   // puesto de guardia frente al arco

        if (dist < 130) {
            // el balon esta cerca: sale a despejarlo
            if (dx > 10) {
                moveRight();
            } else if (dx < -10) {
                moveLeft();
            }
        } else {
            // el balon esta lejos: vuelve a cuidar el arco
            if (getPositionX() < guardX - 10) {
                moveRight();
            } else if (getPositionX() > guardX + 10) {
                moveLeft();
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