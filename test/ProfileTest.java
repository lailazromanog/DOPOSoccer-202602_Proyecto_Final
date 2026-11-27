// Pruebas del perfil persistente del jugador (Profile).
// Cubre: estado inicial, costo de mejora, disponibilidad de mejora,
// nivel maximo, y la operacion de mejora sobre cada estadistica.

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import domain.*;

public class ProfileTest {

    private Profile profile;

    // Crea un perfil nuevo antes de cada prueba
    @Before
    public void setUp() {
        profile = new Profile();
    }

    // ----------------------------------------------------------------
    // Estado inicial
    // ----------------------------------------------------------------

    // Un perfil nuevo empieza con cero puntos
    @Test
    public void testProfileInitialPoints() {
        assertEquals(0, profile.getPoints());
    }

    // Todas las estadisticas arrancan en nivel 1
    @Test
    public void testProfileInitialStats() {
        assertEquals(1, profile.getSpeed());
        assertEquals(1, profile.getKick());
        assertEquals(1, profile.getJump());
        assertEquals(1, profile.getSurvival());
    }

    // ----------------------------------------------------------------
    // Puntos
    // ----------------------------------------------------------------

    // addPoints suma correctamente la cantidad dada
    @Test
    public void testProfileAddPoints() {
        profile.addPoints(100);
        assertEquals(100, profile.getPoints());
    }

    // Se pueden acumular puntos en multiples llamadas
    @Test
    public void testProfileAddPointsAccumulates() {
        profile.addPoints(100);
        profile.addPoints(25);
        assertEquals(125, profile.getPoints());
    }

    // ----------------------------------------------------------------
    // Costo de mejora: upgradeCost
    // ----------------------------------------------------------------

    // En nivel 1 el costo del proximo nivel es 500
    @Test
    public void testUpgradeCostLevel1() throws Exception {
        // Formula: 500 * 2^(nivel-1) = 500 * 2^0 = 500
        assertEquals(500, profile.upgradeCost("speed"));
    }

    // En nivel 2 el costo se duplica a 1000
    @Test
    public void testUpgradeCostLevel2() throws Exception {
        // Subimos la estadistica a nivel 2 pagando su costo
        profile.addPoints(500);
        profile.upgrade("speed");

        // Ahora en nivel 2: 500 * 2^1 = 1000
        assertEquals(1000, profile.upgradeCost("speed"));
    }

    // El costo funciona igual para todas las estadisticas
    @Test
    public void testUpgradeCostSameForAllStats() throws Exception {
        // Todas en nivel 1: mismo costo base
        assertEquals(profile.upgradeCost("speed"),    profile.upgradeCost("kick"));
        assertEquals(profile.upgradeCost("kick"),     profile.upgradeCost("jump"));
        assertEquals(profile.upgradeCost("jump"),     profile.upgradeCost("survival"));
    }

    // ----------------------------------------------------------------
    // Disponibilidad de mejora: canUpgrade
    // ----------------------------------------------------------------

    // Sin puntos suficientes no se puede mejorar
    @Test
    public void testCanUpgradeWithoutPoints() throws Exception {
        // El perfil tiene 0 puntos y el costo es 500
        assertFalse(profile.canUpgrade("speed"));
    }

    // Con puntos exactos se puede mejorar
    @Test
    public void testCanUpgradeWithExactPoints() throws Exception {
        profile.addPoints(500);
        assertTrue(profile.canUpgrade("speed"));
    }

    // Con mas puntos que el costo tambien se puede mejorar
    @Test
    public void testCanUpgradeWithMorePoints() throws Exception {
        profile.addPoints(1000);
        assertTrue(profile.canUpgrade("speed"));
    }

    // Una estadistica en nivel maximo no se puede mejorar aunque haya puntos
    @Test
    public void testCanUpgradeWhenAtMax() throws Exception {
        // Subimos speed hasta el maximo pagando cada nivel
        int nivel = 1;
        while (nivel < 10) {
            int costo = 500 * (int) Math.pow(2, nivel - 1);
            profile.addPoints(costo);
            profile.upgrade("speed");
            nivel++;
        }
        // Con muchos puntos extra tampoco debe dejar mejorar
        profile.addPoints(100000);
        assertFalse(profile.canUpgrade("speed"));
    }

    // ----------------------------------------------------------------
    // Nivel maximo: isMax
    // ----------------------------------------------------------------

    // Una estadistica nueva no esta en su maximo
    @Test
    public void testIsMaxFalseAtStart() throws Exception {
        assertFalse(profile.isMax("speed"));
    }

    // Despues de subir hasta nivel 10 la estadistica esta en su maximo
    @Test
    public void testIsMaxTrueAtLevel10() throws Exception {
        // Pagamos cada nivel para llegar al maximo
        int nivel = 1;
        while (nivel < 10) {
            int costo = 500 * (int) Math.pow(2, nivel - 1);
            profile.addPoints(costo);
            profile.upgrade("speed");
            nivel++;
        }
        assertTrue(profile.isMax("speed"));
    }

    // ----------------------------------------------------------------
    // Operacion de mejora: upgrade
    // ----------------------------------------------------------------

    // Mejorar speed sube su nivel y descuenta los puntos
    @Test
    public void testUpgradeSpeedIncreasesLevel() throws Exception {
        profile.addPoints(500);
        profile.upgrade("speed");

        assertEquals(2, profile.getSpeed());
        // Los puntos deben haber bajado de 500 a 0
        assertEquals(0, profile.getPoints());
    }

    // Mejorar kick sube su nivel correctamente
    @Test
    public void testUpgradeKickIncreasesLevel() throws Exception {
        profile.addPoints(500);
        profile.upgrade("kick");
        assertEquals(2, profile.getKick());
    }

    // Mejorar jump sube su nivel correctamente
    @Test
    public void testUpgradeJumpIncreasesLevel() throws Exception {
        profile.addPoints(500);
        profile.upgrade("jump");
        assertEquals(2, profile.getJump());
    }

    // Mejorar survival sube su nivel correctamente
    @Test
    public void testUpgradeSurvivalIncreasesLevel() throws Exception {
        profile.addPoints(500);
        profile.upgrade("survival");
        assertEquals(2, profile.getSurvival());
    }

    // Si no hay puntos suficientes upgrade no hace nada
    @Test
    public void testUpgradeDoesNothingWithoutPoints() throws Exception {
        profile.upgrade("speed");  // sin puntos, no debe cambiar nada
        assertEquals(1, profile.getSpeed());
        assertEquals(0, profile.getPoints());
    }

    // Mejorar multiples veces acumula los niveles correctamente
    @Test
    public void testUpgradeMultipleTimes() throws Exception {
        // Nivel 1 -> 2: 500 pts
        profile.addPoints(500);
        profile.upgrade("kick");

        // Nivel 2 -> 3: 1000 pts
        profile.addPoints(1000);
        profile.upgrade("kick");

        assertEquals(3, profile.getKick());
    }
}
