// Pruebas de la vida de los jugadores: vida maxima, daño, aturdimiento y patadas.
// Cubre: tabla de vida maxima de los 10 niveles, la vida nunca baja de cero,
// aturdimiento de 3 segundos con vida que vuelve al maximo, daño ignorado
// mientras el jugador esta aturdido, y las reglas de la patada (daño al rival
// al alcance del pie solo si la patada no toca el balon, y bot aturdido sin patear).

import org.junit.Test;
import static org.junit.Assert.*;
import domain.*;

public class HealthTest {

    // Tiempo de un cuadro de juego, en segundos
    private static final double FRAME = 0.016;

    // Crea un partido por goles entre humanos, listo para usarse en las pruebas
    private Game newHumanGame() throws DOPOSoccerException {
        return new Game("goals", 3, "Camerun", "Nigeria", "human", new Profile(), new Profile());
    }

    // Acerca al jugador derecho al izquierdo hasta que quedan frente a frente, en contacto
    private void bringRightPlayerNextToLeft(Game game) {
        for (int i = 0; i < 600; i++) {
            if (game.getRightPlayerX() - game.getLeftPlayerX() > 60) {
                game.moveRightPlayerLeft();
            }
            game.update(FRAME);
        }
    }

    // Vida maxima

    // La vida maxima de cada nivel de survival coincide con la tabla del enunciado: 100, 110, ... 190
    @Test
    public void testMaxHealthTableForAllLevels() throws Exception {
        Player player = new Player();
        for (int level = 1; level <= 10; level++) {
            player.setStats(1, 1, 1, level);
            double expected = 100 + 10 * (level - 1);
            assertEquals(expected, player.getMaxHealth(), 0.001);
            // al fijar las estadisticas la vida queda en su maximo
            assertEquals(expected, player.getHealth(), 0.001);
        }
    }

    // Daño y aturdimiento

    // Un daño mayor que la vida deja la vida en cero y no en un numero negativo
    @Test
    public void testHealthNeverBelowZero() throws Exception {
        Player player = new Player();
        player.takeDamage(500);
        assertEquals(0.0, player.getHealth(), 0.001);
        assertTrue(player.isStunned());
    }

    // El aturdimiento dura tres segundos: a los 2.9 sigue y a los 3.1 ya termino
    @Test
    public void testStunLastsThreeSeconds() throws Exception {
        Player player = new Player();
        player.takeDamage(100);
        player.updateStun(2.9);
        assertTrue(player.isStunned());
        player.updateStun(0.2);
        assertFalse(player.isStunned());
    }

    // Al terminar el aturdimiento la vida vuelve a la vida maxima del jugador
    @Test
    public void testStunEndRestoresMaxHealth() throws Exception {
        Player player = new Player();
        player.setStats(1, 1, 1, 3);   // vida maxima 120
        player.takeDamage(500);
        player.updateStun(3.1);
        assertEquals(120.0, player.getHealth(), 0.001);
    }

    // Un golpe recibido mientras esta aturdido se ignora y no le alarga el aturdimiento
    @Test
    public void testDamageWhileStunnedIsIgnoredAndDoesNotExtendStun() throws Exception {
        Player player = new Player();
        player.takeDamage(100);          // queda aturdido
        player.updateStun(2.0);          // pasan dos segundos
        player.takeDamage(10);           // golpe durante el aturdimiento
        assertEquals(0.0, player.getHealth(), 0.001);
        player.updateStun(1.1);          // en total pasaron 3.1 segundos
        assertFalse(player.isStunned());
        assertEquals(100.0, player.getHealth(), 0.001);
    }

    // Un daño negativo se rechaza aunque el jugador este aturdido
    @Test
    public void testNegativeDamageWhileStunnedStillThrows() throws Exception {
        Player player = new Player();
        player.takeDamage(100);
        try {
            player.takeDamage(-5);
            fail("Debio lanzar excepcion por el dano negativo");
        } catch (DOPOSoccerException e) {
            assertTrue(e.getMessage().contains("negativo"));
        }
    }

    // La patada

    // Una patada con el rival al alcance del pie y sin balon cerca le quita 10 de vida
    @Test
    public void testKickWithRivalInReachDoesDamage() throws Exception {
        Game game = newHumanGame();
        bringRightPlayerNextToLeft(game);
        game.kickLeftPlayer();
        // vida maxima 100, daño 10 con kick en nivel 1: queda 0.9 de la barra
        assertEquals(0.9, game.getRightPlayerLife(), 0.001);
    }

    // Una patada con el rival apenas fuera del alcance del pie no le hace daño
    @Test
    public void testKickWithRivalJustOutsideReachDoesNoDamage() throws Exception {
        Game game = newHumanGame();
        // el rival se acerca hasta quedar entre 75 y 80 de distancia, fuera del alcance del pie
        for (int i = 0; i < 600; i++) {
            if (game.getRightPlayerX() - game.getLeftPlayerX() > 80) {
                game.moveRightPlayerLeft();
            }
            game.update(FRAME);
        }
        assertTrue(game.getRightPlayerX() - game.getLeftPlayerX() > 75);
        game.kickLeftPlayer();
        assertEquals(1.0, game.getRightPlayerLife(), 0.001);
    }

    // Una patada con el rival lejos no le hace daño
    @Test
    public void testKickWithRivalOutOfReachDoesNoDamage() throws Exception {
        Game game = newHumanGame();
        game.kickLeftPlayer();
        assertEquals(1.0, game.getRightPlayerLife(), 0.001);
    }

    // Una patada que toca el balon solo lo impulsa, aunque el rival tambien este al alcance
    @Test
    public void testKickTouchingBallOnlyImpulsesBall() throws Exception {
        Game game = newHumanGame();
        // el balon cae al centro de la cancha: el jugador izquierdo camina hasta quedar a su alcance
        // y el derecho se le acerca hasta quedar tambien al alcance del pie
        for (int i = 0; i < 600; i++) {
            if (game.getBallX() - game.getLeftPlayerX() > 40) {
                game.moveLeftPlayerRight();
            }
            if (game.getRightPlayerX() - game.getLeftPlayerX() > 60) {
                game.moveRightPlayerLeft();
            }
            game.update(FRAME);
        }
        assertEquals(0.0, game.getBallVelocityX(), 0.001);

        game.kickLeftPlayer();

        // el balon salio impulsado y el rival no perdio vida
        assertTrue(game.getBallVelocityX() > 1.0);
        assertEquals(1.0, game.getRightPlayerLife(), 0.001);
    }

    // Un jugador aturdido no puede patear
    @Test
    public void testStunnedPlayerCannotKick() throws Exception {
        Game game = newHumanGame();
        bringRightPlayerNextToLeft(game);
        // el jugador izquierdo recibe una patada del derecho hasta quedar aturdido
        for (int i = 0; i < 10; i++) {
            game.kickRightPlayer();
        }
        assertEquals(0.0, game.getLeftPlayerLife(), 0.001);
        double rightLife = game.getRightPlayerLife();
        game.kickLeftPlayer();
        assertEquals(rightLife, game.getRightPlayerLife(), 0.001);
    }

    // Un bot aturdido no patea aunque quiera: el humano pegado a el no recibe daño durante el aturdimiento
    @Test
    public void testStunnedBotDoesNotKick() throws Exception {
        Game game = new Game("goals", 3, "Camerun", "Nigeria", "stupid", new Profile(), new Profile());
        // el humano camina hacia el bot y lo patea hasta dejarlo sin vida
        for (int i = 0; i < 1000 && game.getRightPlayerLife() > 0.0; i++) {
            if (game.getRightPlayerX() - game.getLeftPlayerX() > 62) {
                game.moveLeftPlayerRight();
            } else {
                game.kickLeftPlayer();
            }
            game.update(FRAME);
        }
        assertEquals(0.0, game.getRightPlayerLife(), 0.001);   // el bot quedo aturdido

        // durante 2.4 segundos de aturdimiento el bot no puede patear al humano
        double humanLife = game.getLeftPlayerLife();
        for (int i = 0; i < 150; i++) {
            game.update(FRAME);
        }
        assertEquals(humanLife, game.getLeftPlayerLife(), 0.001);
    }
    
    // Datos de vida y aturdimiento para la barra de vida

    // Al empezar los dos jugadores tienen la vida llena, no estan aturdidos y no tienen tiempo de aturdimiento
    @Test
    public void testHealthGettersAtStart() throws Exception {
        Game game = newHumanGame();
        assertEquals(100.0, game.getLeftPlayerHealth(), 0.001);
        assertEquals(100.0, game.getRightPlayerHealth(), 0.001);
        assertEquals(100.0, game.getLeftPlayerMaxHealth(), 0.001);
        assertEquals(100.0, game.getRightPlayerMaxHealth(), 0.001);
        assertFalse(game.isLeftPlayerStunned());
        assertFalse(game.isRightPlayerStunned());
        assertEquals(0.0, game.getLeftPlayerStunTime(), 0.001);
        assertEquals(0.0, game.getRightPlayerStunTime(), 0.001);
    }

    // Despues de una patada la vida actual baja 10 y la vida maxima no cambia
    @Test
    public void testHealthGettersAfterKick() throws Exception {
        Game game = newHumanGame();
        bringRightPlayerNextToLeft(game);
        game.kickLeftPlayer();
        assertEquals(90.0, game.getRightPlayerHealth(), 0.001);
        assertEquals(100.0, game.getRightPlayerMaxHealth(), 0.001);
        assertEquals(100.0, game.getLeftPlayerHealth(), 0.001);
    }

    // Un jugador sin vida queda aturdido con tres segundos, y el tiempo baja mientras pasa el juego
    @Test
    public void testStunGettersWhileStunned() throws Exception {
        Game game = newHumanGame();
        bringRightPlayerNextToLeft(game);
        for (int i = 0; i < 10; i++) {
            game.kickRightPlayer();
        }
        assertTrue(game.isLeftPlayerStunned());
        assertFalse(game.isRightPlayerStunned());
        assertEquals(0.0, game.getLeftPlayerHealth(), 0.001);
        assertEquals(3.0, game.getLeftPlayerStunTime(), 0.001);
        // pasa un segundo de juego: al aturdido le quedan dos segundos
        for (int i = 0; i < 62; i++) {
            game.update(FRAME);
        }
        assertTrue(game.isLeftPlayerStunned());
        assertEquals(2.0, game.getLeftPlayerStunTime(), 0.05);
    }

    // Al terminar el aturdimiento el jugador ya no esta aturdido, no tiene tiempo y su vida vuelve a la maxima
    @Test
    public void testStunGettersAfterRecovery() throws Exception {
        Game game = newHumanGame();
        bringRightPlayerNextToLeft(game);
        for (int i = 0; i < 10; i++) {
            game.kickRightPlayer();
        }
        for (int i = 0; i < 200; i++) {
            game.update(FRAME);
        }
        assertFalse(game.isLeftPlayerStunned());
        assertEquals(0.0, game.getLeftPlayerStunTime(), 0.001);
        assertEquals(game.getLeftPlayerMaxHealth(), game.getLeftPlayerHealth(), 0.001);
    }

    // El tiempo restante de aturdimiento del jugador es cero sin aturdir, tres al aturdirse y baja con el tiempo
    @Test
    public void testPlayerStunTimeLeft() throws Exception {
        Player player = new Player();
        assertEquals(0.0, player.getStunTimeLeft(), 0.001);
        player.takeDamage(100);
        assertEquals(3.0, player.getStunTimeLeft(), 0.001);
        player.updateStun(1.0);
        assertEquals(2.0, player.getStunTimeLeft(), 0.001);
        player.updateStun(2.5);
        assertEquals(0.0, player.getStunTimeLeft(), 0.001);
    }
}