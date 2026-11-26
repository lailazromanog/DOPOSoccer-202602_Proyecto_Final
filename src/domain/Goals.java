package domain;

public class Goals extends Match {
    private int goalLimit;

    public Goals(Player leftPlayer, Player rightPlayer, Ball ball, Goal leftGoal, Goal rightGoal, Timer timer, Score score, int goalLimit) {
        super(leftPlayer, rightPlayer, ball, leftGoal, rightGoal, timer, score);
        this.goalLimit = goalLimit;
    }

    @Override 
    public boolean checkWinCondition(){
        return score.getGoalsLeftPlayer() >= goalLimit || score.getGoalsRightPlayer() >= goalLimit;
    }

    public int getGoalLimit(){
        return goalLimit;
    }
}
