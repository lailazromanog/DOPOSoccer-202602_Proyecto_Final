package domain;
import java.io.Serializable;

public class Score implements Serializable{
    private int goalsLeftPlayer;
    private int goalsRightPlayer;
    private Player leftPlayer;
    private Player rightPlayer;

    public Score(Player leftPlayer, Player rightPlayer) {
        this.leftPlayer = leftPlayer;
        this.rightPlayer = rightPlayer;
        this.goalsLeftPlayer = 0;
        this.goalsRightPlayer = 0;
    }

    public void addGoal(Player player) {
        if (player == leftPlayer) {
            goalsLeftPlayer++;
        } else {
            goalsRightPlayer++;
        }
    }

    public Player getWinner() {
        if (goalsLeftPlayer > goalsRightPlayer) {
            return leftPlayer;
        } else if (goalsRightPlayer > goalsLeftPlayer) {
            return rightPlayer;
        }
        return null;
    }

    public int getGoalsLeftPlayer() {
        return goalsLeftPlayer;
    }

    public int getGoalsRightPlayer() {
        return goalsRightPlayer;
    }
}
