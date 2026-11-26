package domain;

public class Expert extends Bot {

    public Expert() {
        super();
    }

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