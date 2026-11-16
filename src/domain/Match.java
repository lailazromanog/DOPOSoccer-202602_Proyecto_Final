package domain;

public abstract class Match {
    protected Player playerOne;
    protected Player playerTwo;
    protected Ball ball;
    protected Goal leftGoal;
    protected Goal rightGoal;
    protected Timer timer;
    protected Score score;

    public Match(Player playerOne, Player playerTwo, Ball ball, Goal leftGoal, Goal rightGoal, Timer timer, Score score) {
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.ball = ball;
        this.leftGoal = leftGoal;
        this.rightGoal = rightGoal;
        this.timer = timer;
        this.score = score;
    }

    //reparte los puntos al terminar: 100 al ganador y 25 al perdedor (regla del juego)
    public void awardPoints(){
        Player winner = score.getWinner();
        if (winner == playerOne) {
            playerOne.addPoints(100);
            playerTwo.addPoints(25);
        }else if (winner == playerTwo) {
            playerTwo.addPoints(100);
            playerOne.addPoints(25);
        }else {
            //empate 
            playerOne.addPoints(25);
            playerTwo.addPoints(25);
        }
    }

    
    public void start(){
        timer.start();
    }

    public void pause(){
        timer.stop();
    }

    public void resume(){
        timer.start();
    }

    public void finish(){
        timer.stop();
    }

    public void onGoal(Player player){
        score.addGoal(player);

        if(player == playerOne){
            playerTwo.getPowerBar().chargeOnGoal();
        } else {
            playerOne.getPowerBar().chargeOnGoal();
        }
        playerOne.resetPosition();
        playerTwo.resetPosition();
        ball.reset();
    }

    public abstract boolean checkWinCondition();

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public Ball getBall() {
        return ball;
    }

    public Score getScore() {
        return score;
    }

    public Timer getTimer() {
        return timer;
    }

    public void checkBallContact(Player player) {
        if (player.isAuraActive()) {
            player.getCharacter().getPower().activate(this, player);
            player.setAuraActive(false);
        }
    }

    public void checkKickContact(Player attacker, Player opponent){
        double damage = 10 * attacker.getMultiplicadorKick();
        opponent.takeDamage(damage);
    }

    public Goal getLeftGoal() {
        return leftGoal;
    }

    public Goal getRightGoal() {
        return rightGoal;
    }
}
