package test;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import domain.*;
public class DOPOSoccerTest {
    private Player player;
    private Timer timer;
    private Score score;
    private PowerBar powerBar;
    private Player playerOne;
    private Player playerTwo;

    @Before
    public void setUp() {
        player = new Player();
        timer = new Timer();
        powerBar = new PowerBar();
        playerOne = new Player();
        playerTwo = new Player();
        score = new Score(playerOne, playerTwo);
    }

    // Test Player

    @Test
    public void testPlayerInitialHealth() {
        assertEquals(100, player.getHealth(), 0.01);
    }

    @Test
    public void testPlayerInitialSpeed() {
        assertEquals(5, player.getSpeed());
    }

    @Test
    public void testPlayerInititalSpeed() {
        player.setInitialPosition(0, 0);
        player.moveRight();
        assertEquals(5.0, player.getPositionX(), 0.01);
    }

    @Test
    public void testPlayerNoMoveWhenStunned() {
        player.setInitialPosition(50, 0);
        player.stun(3.0);
        player.moveRight();
        assertEquals(50, player.getPositionX(), 0.01);
    }

    @Test
    public void testPlayerHalfSpeedWhenElectrocuted() {
        player.setInitialPosition(0, 0);
        player.applyEffect("electrocuted", 3.0);
        player.moveRight();
        assertEquals(2.5, player.getPositionX(), 0.01);
    }

    @Test
    public void testPlayerNoMoveWhenUnconscious() {
        player.setInitialPosition(50, 0);
        player.applyEffect("unconscious", 3.0);
        player.moveRight();
        assertEquals(50, player.getPositionX(), 0.01);
    }

    @Test 
    public void testPlayerNoJumpWhenElectrocuted() {
        player.setInitialPosition(0, 300);
        player.applyEffect("electrocuted", 3.0);
        player.jump();
        assertEquals(0.0, player.getVelocity(), 0.01);
    }

    @Test 
    public void testPlayerNoDoubleJump() {
        player.setInitialPosition(0, 300);
        player.jump();
        player.jump();
        assertEquals(-10.0, player.getVelocity(), 0.01);
    }

    @Test 
    public void testPlayerUpdateGravity() {
        player.setInitialPosition(0, 100);
        player.jump();
        player.update(500);
        assertTrue(player.getPositionY() <= 500);
    }

    @Test 
    public void testPlayerTakeDamage() {
        player.takeDamage(30);
        assertEquals(70.0, player.getHealth(), 0.01);
    }

    @Test
    public void testPlayerStunOnZeroHealth() {
        player.takeDamage(150);
        assertEquals(0.0, player.getHealth(), 0.01);
        assertTrue(player.isStunned());
    }

    @Test 
    public void testPlayerUsePowerFullBar() {
        for(int i = 0; i < 10; i++) {
            player.getPowerBar().update(1.0);
        }
        player.usePower();
        assertTrue(player.isAuraActive());
    }

    @Test
    public void testPlayerUsePowerEmptyBar() {
        player.usePower();
        assertFalse(player.isAuraActive());
    }

    @Test 
    public void testPlayerNoUsePowerWhenStunned() {
        for(int i = 0; i < 10; i++) {
            player.getPowerBar().update(1.0);
        }
        player.stun(3.0);
        player.usePower();
        assertFalse(player.isAuraActive());
    }

    @Test
    public void testPlayerUpgradeStat() {
        player.upgradeStat("speed");
        assertEquals(6, player.getSpeed());
    }

    @Test
    public void testPlayerUpgradeStatMax() {
        for(int i = 0; i < 20; i++) {
            player.upgradeStat("kick");
        }
        assertEquals(10, player.getKick());
    }

    // Test Score

    @Test
    public void testScoreAddGoalPlayerOne(){
        score.addGoal(playerOne);
        assertEquals(1, score.getGoalsPlayerOne());
        assertEquals(0, score.getGoalsPlayerTwo());
    }

    @Test
    public void testScoreGetWinner() {
        score.addGoal(playerOne);
        score.addGoal(playerOne);
        score.addGoal(playerTwo);
        assertEquals(playerOne, score.getWinner());
    }

    @Test
    public void testScoreGetWinnerTie() {
        score.addGoal(playerOne);
        score.addGoal(playerTwo);
        assertNull(score.getWinner());
    }

    // Test PowerBar

    @Test
    public void testPowerBarUpdate() {
        powerBar.update(1.0);
        assertEquals(10.0, powerBar.getCurrentCharge(), 0.01);
    }

    @Test
    public void testPowerBarChargeOnGoal() {
        powerBar.chargeOnGoal();
        assertEquals(25.0, powerBar.getCurrentCharge(), 0.01);
    }

    @Test
    public void testPowerBarFull() {
        for(int i = 0; i < 10; i++) {
            powerBar.update(1.0);
        }
        assertTrue(powerBar.isFull());
    }

    @Test
    public void testPowerBarNotOvercharge() {
        for(int i = 0; i < 20; i++) {
            powerBar.update(1.0);
        }
        assertEquals(100.0, powerBar.getCurrentCharge(), 0.01);
    }

    // Test Timer

    @Test
    public void testTimerUpdateReducesTime() {
        timer.start();
        timer.update(10.0);
        assertEquals(50, timer.getCurrentTime());
    }

    @Test
    public void testTimerNotNotRunningDontUpdate() {
        timer.update(10.0);
        assertEquals(60, timer.getCurrentTime());
    }

    @Test
    public void testTimerNotNegative() {
        timer.start();
        timer.update(100.0);
        assertEquals(0, timer.getCurrentTime());
    }

    // Test match type Time

    @Test 
    public void testTimeMatchWinWhenTimeUpAndDifferentScore(){
        Time timeMatch = new Time(playerOne, playerTwo, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        score.addGoal(playerOne);
        timer.start();
        timer.update(61.0);
        assertTrue(timeMatch.checkWinCondition());
    }

    @Test
    public void testTimeMatchGoldenGoalOnTie(){
        Time timeMatch = new Time(playerOne, playerTwo, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        timer.start();
        timer.update(61.0);
        timeMatch.checkWinCondition();
        assertTrue(timeMatch.isGoldenGoal());
    }

    @Test
    public void testTimeMatchGoldenGoalWin(){
        Time timeMatch = new Time(playerOne, playerTwo, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        timer.start();
        timer.update(61.0);
        timeMatch.checkWinCondition();
        score.addGoal(playerOne);
        assertTrue(timeMatch.checkWinCondition());
    }

    // Test match type Goals

    @Test
    public void testGoalsMatchWinUnderLimit(){
        Goals goalsMatch = new Goals(playerOne, playerTwo, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score, 3);
        score.addGoal(playerOne);
        score.addGoal(playerOne);
        assertFalse(goalsMatch.checkWinCondition());
    }

    @Test
    public void testGoalsMatchWinAtLimit(){
        Goals goalsMatch = new Goals(playerOne, playerTwo, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score, 3);
        score.addGoal(playerTwo);
        score.addGoal(playerTwo);
        score.addGoal(playerTwo);
        assertTrue(goalsMatch.checkWinCondition());
    }

    // Test Match

    @Test
    public void testMatchInGoalIncrementsScoreAndChargesPowerBar() {
        Time timeMatch = new Time(playerOne, playerTwo, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        timeMatch.onGoal(playerOne);
        assertEquals(1, score.getGoalsPlayerOne());
        assertEquals(25.0, playerTwo.getPowerBar().getCurrentCharge(), 0.01);
    }

    @Test
    public void testMatchCheckKickContactDoesDamage() {
        Time timeMatch = new Time(playerOne, playerTwo, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        timeMatch.checkKickContact(playerOne, playerTwo);
        assertTrue(playerTwo.getHealth() < 100.0);
    }
}
