// Pruebas de guardado y apertura de partidas (GamePersistence).
// Se crea un partido real, se guarda en un archivo temporal, se vuelve a leer
// y se verifica que el estado sea identico al original.
// Al terminar cada prueba se borra el archivo temporal para no dejar basura.

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import domain.*;
import java.io.File;

public class PersistenceTest {

    // Archivo temporal donde se guarda la partida durante cada prueba
    private File archivoTemporal;
    private GamePersistence persistence;

    // Prepara el archivo temporal y el objeto de persistencia antes de cada prueba
    @Before
    public void setUp() {
        // El archivo se crea en la carpeta temporal del sistema operativo
        archivoTemporal = new File(System.getProperty("java.io.tmpdir"), "dopossoccer_test.dat");
        persistence = new GamePersistence();
    }

    // Borra el archivo temporal despues de cada prueba para mantener el sistema limpio
    @After
    public void tearDown() {
        if (archivoTemporal.exists()) {
            archivoTemporal.delete();
        }
    }

    // Crea un partido minimo listo para usarse en las pruebas
    private Game crearPartidoDePrueba() throws DOPOSoccerException {
        // Usamos los parametros mas simples posibles: 1 minuto, paises validos, bot stupid
        return new Game("time", 1, "Camerun", "Nigeria", "stupid", new Profile(), new Profile());
    }

    
    // Guardar partida
    

    // Despues de guardar el archivo debe existir en disco
    @Test
    public void testSaveCreatesFile() throws DOPOSoccerException {
        Game partido = crearPartidoDePrueba();

        persistence.save(partido, archivoTemporal);

        // El archivo debe haberse creado en la ruta indicada
        assertTrue(archivoTemporal.exists());
    }

    // El archivo guardado no debe estar vacio
    @Test
    public void testSaveWritesContent() throws DOPOSoccerException {
        Game partido = crearPartidoDePrueba();

        persistence.save(partido, archivoTemporal);

        // Un archivo con un objeto serializado siempre ocupa mas de cero bytes
        assertTrue(archivoTemporal.length() > 0);
    }

    
    // Abrir partida
    

    // Lo que se guarda debe poder leerse de vuelta sin excepcion
    @Test
    public void testOpenReturnsSavedGame() throws DOPOSoccerException {
        Game partidoOriginal = crearPartidoDePrueba();
        persistence.save(partidoOriginal, archivoTemporal);

        // La lectura no debe lanzar ninguna excepcion
        Game partidoLeido = persistence.open(archivoTemporal);

        assertNotNull(partidoLeido);
    }

    // El partido recuperado debe conservar su marcador original
    @Test
    public void testOpenPreservesScore() throws DOPOSoccerException {
        Game partidoOriginal = crearPartidoDePrueba();
        // Registramos que el marcador existe (comienza en 0-0)
        int golesIzq = partidoOriginal.getLeftScore();
        int golesDer = partidoOriginal.getRightScore();

        persistence.save(partidoOriginal, archivoTemporal);
        Game partidoLeido = persistence.open(archivoTemporal);

        // Los goles del partido leido deben coincidir con los del original
        assertEquals(golesIzq, partidoLeido.getLeftScore());
        assertEquals(golesDer, partidoLeido.getRightScore());
    }

    
    // Manejo de errores
    

    // Intentar abrir un archivo inexistente debe lanzar DOPOSoccerException
    @Test
    public void testOpenNonExistentFileThrows() {
        File archivoInexistente = new File(System.getProperty("java.io.tmpdir"), "no_existe_12345.dat");
        boolean lanzoExcepcion = false;

        try {
            persistence.open(archivoInexistente);
        } catch (DOPOSoccerException e) {
            lanzoExcepcion = true;
            // El mensaje debe explicar el problema de forma clara
            assertNotNull(e.getMessage());
            assertFalse(e.getMessage().isEmpty());
        }

        assertTrue("Se esperaba DOPOSoccerException al abrir un archivo inexistente", lanzoExcepcion);
    }

    // Intentar abrir un archivo que no es una partida valida debe lanzar DOPOSoccerException
    @Test
    public void testOpenInvalidFileThrows() throws Exception {
        // Escribimos contenido invalido en el archivo temporal
        java.io.FileWriter fw = new java.io.FileWriter(archivoTemporal);
        fw.write("esto no es una partida serializada");
        fw.close();

        boolean lanzoExcepcion = false;

        try {
            persistence.open(archivoTemporal);
        } catch (DOPOSoccerException e) {
            lanzoExcepcion = true;
        }

        assertTrue("Se esperaba DOPOSoccerException al abrir un archivo con contenido invalido", lanzoExcepcion);
    }
}
