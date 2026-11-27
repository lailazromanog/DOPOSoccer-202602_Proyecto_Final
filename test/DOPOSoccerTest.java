// Pruebas principales del juego DOPOSoccer.
// Cubre: Player, Score, PowerBar, Timer, Time y Goals.
// Cada metodo de prueba verifica una sola responsabilidad.

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import domain.*;

public class DOPOSoccerTest {

    private Player player;
    private Timer timer;
    private Score score;
    private PowerBar powerBar;
    private Player leftPlayer;
    private Player rightPlayer;

    // Se ejecuta antes de cada prueba para dejar objetos limpios
    @Before
    public void setUp() {
        player      = new Player();
        timer       = new Timer();
        powerBar    = new PowerBar();
        leftPlayer  = new Player();
        rightPlayer = new Player();
        score       = new Score(leftPlayer, rightPlayer);
    }

    
    // Player: estado inicial
    

    // La vida inicial debe ser 100
    @Test
    public void testPlayerInitialHealth() {
        assertEquals(100, player.getHealth(), 0.01);
    }

    // La estadistica de velocidad arranca en nivel 1 (escala 1-10)
    @Test
    public void testPlayerInitialSpeed() {
        assertEquals(1, player.getSpeed());
    }

    // Con velocidad nivel 1 el jugador se desplaza 5 px por tick (getMoveSpeed devuelve 5)
    @Test
    public void testPlayerInititalSpeed() {
        player.setInitialPosition(0, 0);
        player.moveRight();
        assertEquals(5.0, player.getPositionX(), 0.01);
    }

    
    // Player: efectos y estados especiales
    

    // Un jugador aturdido no puede moverse
    @Test
    public void testPlayerNoMoveWhenStunned() {
        player.setInitialPosition(50, 0);
        player.stun(3.0);
        player.moveRight();
        assertEquals(50, player.getPositionX(), 0.01);
    }

    // Con el efecto electrocutado el jugador se mueve a la mitad de velocidad
    @Test
    public void testPlayerHalfSpeedWhenElectrocuted() {
        player.setInitialPosition(0, 0);
        player.applyEffect("electrocuted", 3.0);
        player.moveRight();
        assertEquals(2.5, player.getPositionX(), 0.01);
    }

    // Con el efecto inconsciente el jugador no puede moverse
    @Test
    public void testPlayerNoMoveWhenUnconscious() {
        player.setInitialPosition(50, 0);
        player.applyEffect("unconscious", 3.0);
        player.moveRight();
        assertEquals(50, player.getPositionX(), 0.01);
    }

    // Con el efecto electrocutado el jugador no puede saltar
    @Test
    public void testPlayerNoJumpWhenElectrocuted() {
        player.setInitialPosition(0, 300);
        player.applyEffect("electrocuted", 3.0);
        player.jump();
        assertEquals(0.0, player.getVelocity(), 0.01);
    }

    // El jugador no puede saltar dos veces seguidas (sin tocar el suelo)
    @Test
    public void testPlayerNoDoubleJump() {
        player.setInitialPosition(0, 300);
        player.jump();
        player.jump();
        // la velocidad queda en -10 del primer salto; el segundo no suma
        assertEquals(-10.0, player.getVelocity(), 0.01);
    }

    // La gravedad hace que la posicion Y aumente con el tiempo
    @Test
    public void testPlayerUpdateGravity() {
        player.setInitialPosition(0, 100);
        player.jump();
        player.update(500);
        assertTrue(player.getPositionY() <= 500);
    }

    
    // Player: vida y daño
    

    // Recibir 30 de daño reduce la vida a 70
    @Test
    public void testPlayerTakeDamage() {
        player.takeDamage(30);
        assertEquals(70.0, player.getHealth(), 0.01);
    }

    // Cuando la vida llega a 0 el jugador queda aturdido automaticamente
    @Test
    public void testPlayerStunOnZeroHealth() {
        player.takeDamage(150);
        assertEquals(0.0, player.getHealth(), 0.01);
        assertTrue(player.isStunned());
    }

    
    // Player: uso de poder
    

    // Con la barra llena el jugador puede activar su aura
    @Test
    public void testPlayerUsePowerFullBar() {
        for (int i = 0; i < 10; i++) {
            player.getPowerBar().update(1.0);
        }
        player.usePower();
        assertTrue(player.isAuraActive());
    }

    // Con la barra vacia el jugador no puede activar su aura
    @Test
    public void testPlayerUsePowerEmptyBar() {
        player.usePower();
        assertFalse(player.isAuraActive());
    }

    // Aunque la barra este llena, un jugador aturdido no puede usar su poder
    @Test
    public void testPlayerNoUsePowerWhenStunned() {
        for (int i = 0; i < 10; i++) {
            player.getPowerBar().update(1.0);
        }
        player.stun(3.0);
        player.usePower();
        assertFalse(player.isAuraActive());
    }

    
    // Player: estadisticas y mejoras
    

    // Mejorar speed una vez la sube de 1 a 2
    @Test
    public void testPlayerUpgradeStat() {
        player.upgradeStat("speed");
        assertEquals(2, player.getSpeed());
    }

    // La estadistica no puede superar el maximo de nivel 10
    @Test
    public void testPlayerUpgradeStatMax() {
        for (int i = 0; i < 20; i++) {
            player.upgradeStat("kick");
        }
        assertEquals(10, player.getKick());
    }

    
    // Score
    

    // Registrar un gol del jugador izquierdo incrementa solo su contador
    @Test
    public void testScoreAddGoalLeftPlayer() {
        score.addGoal(leftPlayer);
        assertEquals(1, score.getGoalsLeftPlayer());
        assertEquals(0, score.getGoalsRightPlayer());
    }

    // El ganador es el jugador con mas goles
    @Test
    public void testScoreGetWinner() {
        score.addGoal(leftPlayer);
        score.addGoal(leftPlayer);
        score.addGoal(rightPlayer);
        assertEquals(leftPlayer, score.getWinner());
    }

    // Si hay empate no hay ganador (devuelve null)
    @Test
    public void testScoreGetWinnerTie() {
        score.addGoal(leftPlayer);
        score.addGoal(rightPlayer);
        assertNull(score.getWinner());
    }

    
    // PowerBar
    

    // Un tick de actualizacion carga 10 puntos la barra
    @Test
    public void testPowerBarUpdate() {
        powerBar.update(1.0);
        assertEquals(10.0, powerBar.getCurrentCharge(), 0.01);
    }

    // Al anotar un gol la barra del rival sube 25 puntos
    @Test
    public void testPowerBarChargeOnGoal() {
        powerBar.chargeOnGoal();
        assertEquals(25.0, powerBar.getCurrentCharge(), 0.01);
    }

    // Despues de 10 ticks la barra esta llena
    @Test
    public void testPowerBarFull() {
        for (int i = 0; i < 10; i++) {
            powerBar.update(1.0);
        }
        assertTrue(powerBar.isFull());
    }

    // La barra no puede superar el 100%
    @Test
    public void testPowerBarNotOvercharge() {
        for (int i = 0; i < 20; i++) {
            powerBar.update(1.0);
        }
        assertEquals(100.0, powerBar.getCurrentCharge(), 0.01);
    }

    
    // Timer
    

    // Al avanzar 10 segundos el tiempo baja de 60 a 50
    @Test
    public void testTimerUpdateReducesTime() {
        timer.start();
        timer.update(10.0);
        assertEquals(50, timer.getCurrentTime());
    }

    // Si el cronometro no esta corriendo no descuenta tiempo
    @Test
    public void testTimerNotNotRunningDontUpdate() {
        timer.update(10.0);
        assertEquals(60, timer.getCurrentTime());
    }

    // El tiempo no baja de cero
    @Test
    public void testTimerNotNegative() {
        timer.start();
        timer.update(100.0);
        assertEquals(0, timer.getCurrentTime());
    }

    
    // Modo de partido: Time
    

    // Cuando el tiempo se agota y hay un ganador la condicion de victoria es true
    @Test
    public void testTimeMatchWinWhenTimeUpAndDifferentScore() {
        Time timeMatch = new Time(leftPlayer, rightPlayer, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        score.addGoal(leftPlayer);
        timer.start();
        timer.update(61.0);
        assertTrue(timeMatch.checkWinCondition());
    }

    // Cuando el tiempo se agota en empate se activa el golden goal
    @Test
    public void testTimeMatchGoldenGoalOnTie() {
        Time timeMatch = new Time(leftPlayer, rightPlayer, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        timer.start();
        timer.update(61.0);
        timeMatch.checkWinCondition();
        assertTrue(timeMatch.isGoldenGoal());
    }

    // En golden goal el primer gol termina el partido
    @Test
    public void testTimeMatchGoldenGoalWin() {
        Time timeMatch = new Time(leftPlayer, rightPlayer, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        timer.start();
        timer.update(61.0);
        timeMatch.checkWinCondition();
        score.addGoal(leftPlayer);
        assertTrue(timeMatch.checkWinCondition());
    }

    
    // Modo de partido: Goals
    

    // Con 2 de 3 goles el partido no termina
    @Test
    public void testGoalsMatchWinUnderLimit() {
        Goals goalsMatch = new Goals(leftPlayer, rightPlayer, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score, 3);
        score.addGoal(leftPlayer);
        score.addGoal(leftPlayer);
        assertFalse(goalsMatch.checkWinCondition());
    }

    // Al llegar al limite de goles el partido termina
    @Test
    public void testGoalsMatchWinAtLimit() {
        Goals goalsMatch = new Goals(leftPlayer, rightPlayer, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score, 3);
        score.addGoal(rightPlayer);
        score.addGoal(rightPlayer);
        score.addGoal(rightPlayer);
        assertTrue(goalsMatch.checkWinCondition());
    }

    
    // Match: eventos de juego
    

    // Al anotar un gol el marcador sube y la barra del rival se carga
    @Test
    public void testMatchInGoalIncrementsScoreAndChargesPowerBar() {
        Time timeMatch = new Time(leftPlayer, rightPlayer, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        timeMatch.onGoal(leftPlayer);
        assertEquals(1, score.getGoalsLeftPlayer());
        assertEquals(25.0, rightPlayer.getPowerBar().getCurrentCharge(), 0.01);
    }

    // Un contacto de patada reduce la vida del rival
    @Test
    public void testMatchCheckKickContactDoesDamage() {
        Time timeMatch = new Time(leftPlayer, rightPlayer, new Ball(), new Goal(0, 0), new Goal(100, 0), timer, score);
        timeMatch.checkKickContact(leftPlayer, rightPlayer);
        assertTrue(rightPlayer.getHealth() < 100.0);
    }
}
