package domain;

public class Time extends Match {
    private boolean goldenGoal;

    public Time(Player leftPlayer, Player rightPlayer, Ball ball, Goal leftGoal, Goal rightGoal, Timer timer, Score score) {
        super(leftPlayer, rightPlayer, ball, leftGoal, rightGoal, timer, score);
        this.goldenGoal = false;
    }

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

    public boolean isGoldenGoal(){
        return goldenGoal;
    }
}
