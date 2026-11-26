package domain;

public class Offensive extends Bot {

    public Offensive() {
        super();
    }

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