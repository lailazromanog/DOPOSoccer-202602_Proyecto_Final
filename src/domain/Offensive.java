package domain;

/**
 * Perfil de máquina Ofensiva.
 * Su prioridad es anotar: persigue el balón en todo momento, salta
 * para cabecearlo cuando lo tiene cerca y por encima, y patea y usa
 * su poder apenas el balón queda al alcance de su pie.
 */
public class Offensive extends Bot {

    /**
     * Crea un bot con el perfil Ofensiva.
     */
    public Offensive() {
        super();
    }

    /**
     * Persigue el balón en horizontal, salta cuando el balón está
     * cerca y por encima suyo, y al tenerlo al alcance del pie activa
     * su poder si está disponible y pide patear.
     * @param match partido en curso sobre el que el bot toma su decisión
     */
    @Override
    public void decideAction(Match match) {
        Ball ball = match.getBall();
        double dx = ball.getPositionX() - getPositionX();
        double dist = Math.abs(dx);

        // persigue el balon en horizontal (con zona muerta para no vibrar en el sitio)
        if (dx > 10) {
            moveRight();
        } else if (dx < -10) {
            moveLeft();
        }

        // salta SOLO si el balon esta por encima y cerca (para cabecear)
        if (ball.getPositionY() < getPositionY() - 20 && dist < 60) {
            jump();
        }

        // patea cuando el balon esta al alcance del pie
        if (dist < 55) {
            if (getPowerBar().isFull() && !isAuraActive()) {
                usePower();
            }
            wantsToKick = true;
        }
    }
}