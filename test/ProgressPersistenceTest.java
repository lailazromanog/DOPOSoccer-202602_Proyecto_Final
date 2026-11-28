// Pruebas del guardado en disco del progreso de los jugadores (Progress.save).
// Cubre: progreso nuevo sin archivo, guardar y volver a abrir puntos y
// niveles, independencia de los dos jugadores, cambios sin guardar y
// archivos danados o invalidos.
// El progreso real del jugador (progreso.dat) se aparta antes de las
// pruebas y se devuelve a su lugar al terminar.

import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import domain.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

public class ProgressPersistenceTest {

    private static final File FILE = new File("progreso.dat");
    private static final File BACKUP = new File("progreso.dat.respaldo-pruebas");

    // Aparta el progreso real del jugador, si existe
    @BeforeClass
    public static void backupRealProgress() {
        BACKUP.delete();
        if (FILE.exists()) {
            FILE.renameTo(BACKUP);
        }
    }

    // Devuelve el progreso real del jugador a su lugar
    @AfterClass
    public static void restoreRealProgress() {
        FILE.delete();
        if (BACKUP.exists()) {
            BACKUP.renameTo(FILE);
        }
    }

    // Cada prueba empieza sin progreso guardado
    @Before
    public void setUp() {
        FILE.delete();
    }

    // Juega partidos que terminan en empate, cada uno da 25 puntos a cada jugador
    private void earnPoints(Progress progress, int draws) throws Exception {
        for (int i = 0; i < draws; i++) {
            Game game = progress.newGame("time", 1, "Camerun", "Nigeria", "human");
            game.finishMatch();
        }
    }

    // Escribe en el archivo de progreso el contenido dado
    private void writeFile(byte[] content) throws Exception {
        FileOutputStream out = new FileOutputStream(FILE);
        out.write(content);
        out.close();
    }

    // Sin archivo guardado, el progreso empieza en cero y no crea el archivo
    @Test
    public void testMissingFileStartsFresh() throws Exception {
        Progress progress = new Progress();
        assertEquals(0, progress.getPoints(Progress.LEFT));
        assertEquals(0, progress.getPoints(Progress.RIGHT));
        assertEquals(1, progress.getLevel(Progress.LEFT, "speed"));
        assertFalse(FILE.exists());
    }

    // Guardar crea el archivo
    @Test
    public void testSaveCreatesFile() throws Exception {
        new Progress().save();
        assertTrue(FILE.exists());
    }

    // Los puntos ganados se conservan al volver a abrir el juego
    @Test
    public void testPointsSurviveRestart() throws Exception {
        Progress progress = new Progress();
        earnPoints(progress, 3);
        progress.save();

        Progress reopened = new Progress();
        assertEquals(75, reopened.getPoints(Progress.LEFT));
        assertEquals(75, reopened.getPoints(Progress.RIGHT));
    }

    // Las mejoras compradas y los puntos restantes se conservan, cada jugador por separado
    @Test
    public void testUpgradesSurviveRestart() throws Exception {
        Progress progress = new Progress();
        earnPoints(progress, 24);          // 600 puntos cada uno
        progress.upgrade(Progress.LEFT, "kick");
        progress.upgrade(Progress.RIGHT, "survival");
        progress.save();

        Progress reopened = new Progress();
        assertEquals(2, reopened.getLevel(Progress.LEFT, "kick"));
        assertEquals(1, reopened.getLevel(Progress.LEFT, "survival"));
        assertEquals(2, reopened.getLevel(Progress.RIGHT, "survival"));
        assertEquals(1, reopened.getLevel(Progress.RIGHT, "kick"));
        assertEquals(100, reopened.getPoints(Progress.LEFT));
        assertEquals(100, reopened.getPoints(Progress.RIGHT));
        assertEquals(1000, reopened.getUpgradeCost(Progress.LEFT, "kick"));
        assertEquals(110.0, reopened.getMaxHealth(Progress.RIGHT), 0.001);
    }

    // El progreso abierto sigue creciendo con partidos nuevos
    @Test
    public void testReopenedProgressKeepsGrowing() throws Exception {
        Progress progress = new Progress();
        earnPoints(progress, 2);
        progress.save();

        Progress reopened = new Progress();
        earnPoints(reopened, 1);
        reopened.save();

        assertEquals(75, new Progress().getPoints(Progress.LEFT));
    }

    // Un partido nuevo usa las estadisticas abiertas del disco
    @Test
    public void testNewGameUsesSavedStats() throws Exception {
        Progress progress = new Progress();
        earnPoints(progress, 20);          // 500 puntos cada uno
        progress.upgrade(Progress.LEFT, "kick");
        progress.save();

        Progress reopened = new Progress();
        Game game = reopened.newGame("time", 1, "Camerun", "Nigeria", "human");
        assertNotNull(game);
        assertEquals(11.0, reopened.getKickDamage(Progress.LEFT), 0.001);
    }

    // Lo que no se guardo no aparece al volver a abrir
    @Test
    public void testUnsavedChangesAreLost() throws Exception {
        Progress progress = new Progress();
        earnPoints(progress, 1);
        progress.save();
        earnPoints(progress, 1);           // no se guarda

        assertEquals(25, new Progress().getPoints(Progress.LEFT));
    }

    // Un archivo danado no rompe el juego: se empieza con progreso nuevo
    @Test
    public void testCorruptFileStartsFresh() throws Exception {
        writeFile("esto no es un progreso".getBytes());
        Progress progress = new Progress();
        assertEquals(0, progress.getPoints(Progress.LEFT));
        assertEquals(1, progress.getLevel(Progress.RIGHT, "jump"));
    }

    // Un archivo vacio no rompe el juego
    @Test
    public void testEmptyFileStartsFresh() throws Exception {
        writeFile(new byte[0]);
        assertEquals(0, new Progress().getPoints(Progress.RIGHT));
    }

    // Un archivo con otro objeto serializado no es un progreso valido
    @Test
    public void testFileWithOtherObjectStartsFresh() throws Exception {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE));
        oos.writeObject("hola");
        oos.close();
        assertEquals(0, new Progress().getPoints(Progress.LEFT));
    }

    // Tras un archivo danado, guardar lo reemplaza por un progreso valido
    @Test
    public void testSaveOverwritesCorruptFile() throws Exception {
        writeFile("basura".getBytes());
        Progress progress = new Progress();
        earnPoints(progress, 1);
        progress.save();
        assertEquals(25, new Progress().getPoints(Progress.LEFT));
    }

    // Si no se puede escribir el archivo, save lanza una excepcion del dominio
    @Test
    public void testSaveFailureThrows() throws Exception {
        Progress progress = new Progress();
        FILE.mkdir();                      // una carpeta en lugar del archivo
        try {
            progress.save();
            fail("Se esperaba DOPOSoccerException");
        } catch (DOPOSoccerException e) {
            assertTrue(e.getMessage().contains("No se pudo guardar el progreso"));
        } finally {
            FILE.delete();
        }
    }
}
