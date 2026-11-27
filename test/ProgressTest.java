// Pruebas de la fachada de progreso de los jugadores (Progress).
// Cubre: puntos iniciales, reparto de puntos por partido, mejoras de
// estadisticas por jugador, valores finales y excepciones.
 
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import domain.*;
 
public class ProgressTest {
 
    private Progress progress;
 
    // Crea un progreso nuevo antes de cada prueba
    @Before
    public void setUp() {
        progress = new Progress();
    }
 
    // Juega partidos que terminan en empate, cada uno da 25 puntos a cada jugador
    private void earnPoints(int draws) throws Exception {
        for (int i = 0; i < draws; i++) {
            Game game = progress.newGame("time", 1, "Camerun", "Nigeria", "human");
            game.finishMatch();
        }
    }
 
       // Estado inicial y puntos
    
    // Los dos jugadores empiezan con cero puntos
    @Test
    public void testInitialPointsAreZero() throws Exception {
        assertEquals(0, progress.getPoints(Progress.LEFT));
        assertEquals(0, progress.getPoints(Progress.RIGHT));
    }
 
    // Todas las estadisticas de los dos jugadores empiezan en nivel 1
    @Test
    public void testInitialLevelsAreOne() throws Exception {
        String[] stats = {"speed", "kick", "jump", "survival"};
        for (int i = 0; i < stats.length; i++) {
            assertEquals(1, progress.getLevel(Progress.LEFT, stats[i]));
            assertEquals(1, progress.getLevel(Progress.RIGHT, stats[i]));
        }
    }
 
    // Un partido empatado da 25 puntos a cada jugador en su progreso
    @Test
    public void testDrawMatchAwardsPointsToProgress() throws Exception {
        earnPoints(1);
        assertEquals(25, progress.getPoints(Progress.LEFT));
        assertEquals(25, progress.getPoints(Progress.RIGHT));
    }
 
    // Los puntos se acumulan entre partidos
    @Test
    public void testPointsAccumulateAcrossMatches() throws Exception {
        earnPoints(3);
        assertEquals(75, progress.getPoints(Progress.LEFT));
        assertEquals(75, progress.getPoints(Progress.RIGHT));
    }
 
       // Mejoras
    
    // Sin puntos no se puede mejorar y la estadistica no esta en su maximo
    @Test
    public void testCannotUpgradeWithoutPoints() throws Exception {
        assertFalse(progress.canUpgrade(Progress.LEFT, "kick"));
        assertFalse(progress.isMax(Progress.LEFT, "kick"));
        assertEquals(500, progress.getUpgradeCost(Progress.LEFT, "kick"));
    }
 
    // Con 500 puntos ya se puede mejorar
    @Test
    public void testCanUpgradeWithEnoughPoints() throws Exception {
        earnPoints(20);
        assertTrue(progress.canUpgrade(Progress.LEFT, "kick"));
    }
 
    // Mejorar gasta solo los puntos del jugador que mejora
    @Test
    public void testUpgradeAffectsOnlyThatPlayer() throws Exception {
        earnPoints(20);
        progress.upgrade(Progress.LEFT, "kick");
        assertEquals(2, progress.getLevel(Progress.LEFT, "kick"));
        assertEquals(0, progress.getPoints(Progress.LEFT));
        assertEquals(1, progress.getLevel(Progress.RIGHT, "kick"));
        assertEquals(500, progress.getPoints(Progress.RIGHT));
    }
 
    // Mejorar sin puntos suficientes lanza excepcion y no cambia nada
    @Test
    public void testUpgradeWithoutPointsThrows() throws Exception {
        try {
            progress.upgrade(Progress.LEFT, "speed");
            fail("Debio lanzar DOPOSoccerException por puntos insuficientes");
        } catch (DOPOSoccerException e) {
            assertNotNull(e.getMessage());
        }
        assertEquals(1, progress.getLevel(Progress.LEFT, "speed"));
    }
 
       // Valores finales
    
    // Los valores base coinciden con las tablas del enunciado
    @Test
    public void testBaseFinalValues() throws Exception {
        assertEquals(100.0, progress.getMaxHealth(Progress.LEFT), 0.01);
        assertEquals(10.0, progress.getKickDamage(Progress.LEFT), 0.01);
        assertEquals(1.0, progress.getKickMultiplier(Progress.LEFT), 0.001);
        assertEquals(0, progress.getSpeedBonusPercent(Progress.LEFT));
        assertEquals(0, progress.getJumpBonusPercent(Progress.LEFT));
    }
 
    // Mejorar survival del jugador izquierdo sube su vida maxima y no la del derecho
    @Test
    public void testFinalValuesFollowUpgrades() throws Exception {
        earnPoints(20);
        progress.upgrade(Progress.LEFT, "survival");
        assertEquals(110.0, progress.getMaxHealth(Progress.LEFT), 0.01);
        assertEquals(100.0, progress.getMaxHealth(Progress.RIGHT), 0.01);
    }
 
       // Excepciones
    
    // Un identificador de jugador invalido lanza excepcion
    @Test
    public void testInvalidPlayerThrows() {
        try {
            progress.getPoints(3);
            fail("Debio lanzar DOPOSoccerException por jugador invalido");
        } catch (DOPOSoccerException e) {
            assertTrue(e.getMessage().contains("3"));
        }
    }
 
    // Una estadistica desconocida lanza excepcion
    @Test
    public void testUnknownStatThrows() {
        try {
            progress.getLevel(Progress.LEFT, "fly");
            fail("Debio lanzar DOPOSoccerException por estadistica desconocida");
        } catch (DOPOSoccerException e) {
            assertTrue(e.getMessage().contains("fly"));
        }
    }
 
    // Crear un partido con un tipo desconocido lanza excepcion
    @Test
    public void testNewGameUnknownTypeThrows() {
        try {
            progress.newGame("penales", 1, "Camerun", "Nigeria", "human");
            fail("Debio lanzar DOPOSoccerException por tipo de partido desconocido");
        } catch (DOPOSoccerException e) {
            assertTrue(e.getMessage().contains("penales"));
        }
    }
}