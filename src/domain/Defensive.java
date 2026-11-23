package domain;

public class Defensive extends Bot {

    public Defensive() {
        super();
    }

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