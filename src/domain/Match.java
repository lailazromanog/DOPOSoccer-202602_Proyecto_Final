package domain;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public abstract class Match implements Serializable{
    protected Player leftPlayer;
    protected Player rightPlayer;
    protected Ball ball;
    protected Goal leftGoal;
    protected Goal rightGoal;
    protected Timer timer;
    protected Score score;
    protected List<Ball> fakeBalls = new ArrayList<>();   // balones senuelo del poder Illusion
    protected double fakeTimer = 0;                        // cuanto duran los senuelos en pantalla

    public Match(Player leftPlayer, Player rightPlayer, Ball ball, Goal leftGoal, Goal rightGoal, Timer timer, Score score) {
        this.leftPlayer = leftPlayer;
        this.rightPlayer = rightPlayer;
        this.ball = ball;
        this.leftGoal = leftGoal;
        this.rightGoal = rightGoal;
        this.timer = timer;
        this.score = score;
    }

    //reparte los puntos al terminar: 100 al ganador y 25 al perdedor (regla del juego)
    public void awardPoints(){
        Player winner = score.getWinner();
        if (winner == leftPlayer) {
            leftPlayer.addPoints(100);
            rightPlayer.addPoints(25);
        }else if (winner == rightPlayer) {
            rightPlayer.addPoints(100);
            leftPlayer.addPoints(25);
        }else {
            //empate 
            leftPlayer.addPoints(25);
            rightPlayer.addPoints(25);
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

        if(player == leftPlayer){
            rightPlayer.getPowerBar().chargeOnGoal();
        } else {
            leftPlayer.getPowerBar().chargeOnGoal();
        }
        leftPlayer.resetPosition();
        rightPlayer.resetPosition();
        ball.reset();
    }

    /** Recibe los balones senuelo ya creados (los crea el poder) y los mantiene por un tiempo. */
    public void addFakeBalls(List<Ball> balls, double duration) {
        this.fakeBalls = balls;
        this.fakeTimer = duration;
    }

        /** Mueve los senuelos cada frame y los elimina cuando se acaba el efecto. */
    public void updateFakeBalls(double deltaTime, int width, int ballDiameter, int ground) {
        if (fakeTimer <= 0) {
            return;
        }
        fakeTimer -= deltaTime;
        if (fakeTimer <= 0) {
            fakeBalls.clear();
            return;
        }
        for (int i = 0; i < fakeBalls.size(); i++) {
            Ball fake = fakeBalls.get(i);
            fake.advance();
            fake.bounceInBounds(width, ballDiameter, ground);
        }
    }

    public List<Ball> getFakeBalls() {
        return fakeBalls;
    }

    
    public abstract boolean checkWinCondition();

    public Player getLeftPlayer() {
        return leftPlayer;
    }

    public Player getRightPlayer() {
        return rightPlayer;
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
