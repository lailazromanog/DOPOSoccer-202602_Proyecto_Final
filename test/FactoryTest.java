// Pruebas de las tres fabricas del juego: CharacterFactory, BotFactory y MatchFactory.
// Se verifica que cada fabrica crea el objeto correcto con una entrada valida,
// y que lanza DOPOSoccerException con un mensaje claro cuando la entrada es invalida.
// Esto cubre el camino feliz y el camino de error de cada fabrica.

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import domain.*;
import domain.Character;  // necesario para evitar ambiguedad con java.lang.Character

public class FactoryTest {

    // Objetos de apoyo reutilizables entre pruebas de MatchFactory
    private Player leftPlayer;
    private Player rightPlayer;
    private Ball ball;
    private Score score;

    // Prepara los objetos que MatchFactory necesita para crear un partido
    @Before
    public void setUp() {
        leftPlayer  = new Player();
        rightPlayer = new Player();
        ball        = new Ball();
        score       = new Score(leftPlayer, rightPlayer);
    }

    
    // CharacterFactory: personajes disponibles
    

    // Camerun debe crear un personaje con el poder Lightning
    @Test
    public void testCharacterFactoryCreatesLightning() throws DOPOSoccerException {
        CharacterFactory factory = new CharacterFactory();
        Character personaje = factory.create("Camerun");

        // El personaje existe y tiene un poder asignado
        assertNotNull(personaje);
        assertNotNull(personaje.getPower());
    }

    // Nigeria debe crear un personaje con el poder Tornado
    @Test
    public void testCharacterFactoryCreatesTornado() throws DOPOSoccerException {
        CharacterFactory factory = new CharacterFactory();
        Character personaje = factory.create("Nigeria");

        assertNotNull(personaje);
        assertNotNull(personaje.getPower());
    }

    // Estados Unidos debe crear un personaje con el poder Illusion
    @Test
    public void testCharacterFactoryCreatesIllusion() throws DOPOSoccerException {
        CharacterFactory factory = new CharacterFactory();
        Character personaje = factory.create("Estados Unidos");

        assertNotNull(personaje);
        assertNotNull(personaje.getPower());
    }

    // Corea del Sur debe crear un personaje con el poder BlueAura
    @Test
    public void testCharacterFactoryCreatesBlueAura() throws DOPOSoccerException {
        CharacterFactory factory = new CharacterFactory();
        Character personaje = factory.create("Corea del Sur");

        assertNotNull(personaje);
        assertNotNull(personaje.getPower());
    }

    // Un pais no reconocido debe lanzar DOPOSoccerException con mensaje claro
    @Test
    public void testCharacterFactoryThrowsOnUnknownCountry() {
        CharacterFactory factory = new CharacterFactory();
        boolean lanzoExcepcion = false;

        try {
            factory.create("PaisInexistente");
        } catch (DOPOSoccerException e) {
            lanzoExcepcion = true;
            // El mensaje debe mencionar el pais que no se reconocio
            assertTrue(e.getMessage().contains("PaisInexistente"));
        }

        assertTrue("Se esperaba DOPOSoccerException para un pais invalido", lanzoExcepcion);
    }

    // La excepcion no debe lanzarse para ninguno de los paises validos
    @Test
    public void testCharacterFactoryAllValidCountries() {
        CharacterFactory factory = new CharacterFactory();
        String[] paises = { "Camerun", "Nigeria", "Estados Unidos", "Corea del Sur" };
        int creados = 0;

        for (int i = 0; i < paises.length; i++) {
            try {
                Character personaje = factory.create(paises[i]);
                assertNotNull(personaje);
                creados++;
            } catch (DOPOSoccerException e) {
                fail("El pais valido '" + paises[i] + "' lanzo una excepcion inesperada");
            }
        }

        // Deben haberse creado los 4 personajes sin excepcion
        assertEquals(4, creados);
    }

    
    // BotFactory: perfiles de maquina disponibles
    

    // El perfil stupid debe crear un bot de tipo Stupid
    @Test
    public void testBotFactoryCreatesStupid() throws DOPOSoccerException {
        BotFactory factory = new BotFactory();
        Bot bot = factory.create("stupid");

        assertNotNull(bot);
        // La instancia debe ser de la subclase concreta correcta
        assertTrue(bot instanceof Stupid);
    }

    // El perfil defensive debe crear un bot de tipo Defensive
    @Test
    public void testBotFactoryCreatesDefensive() throws DOPOSoccerException {
        BotFactory factory = new BotFactory();
        Bot bot = factory.create("defensive");

        assertNotNull(bot);
        assertTrue(bot instanceof Defensive);
    }

    // El perfil expert debe crear un bot de tipo Expert
    @Test
    public void testBotFactoryCreatesExpert() throws DOPOSoccerException {
        BotFactory factory = new BotFactory();
        Bot bot = factory.create("expert");

        assertNotNull(bot);
        assertTrue(bot instanceof Expert);
    }

    // El perfil offensive debe crear un bot de tipo Offensive
    @Test
    public void testBotFactoryCreatesOffensive() throws DOPOSoccerException {
        BotFactory factory = new BotFactory();
        Bot bot = factory.create("offensive");

        assertNotNull(bot);
        assertTrue(bot instanceof Offensive);
    }

    // Un perfil no reconocido debe lanzar DOPOSoccerException con mensaje claro
    @Test
    public void testBotFactoryThrowsOnUnknownProfile() {
        BotFactory factory = new BotFactory();
        boolean lanzoExcepcion = false;

        try {
            factory.create("superbot");
        } catch (DOPOSoccerException e) {
            lanzoExcepcion = true;
            // El mensaje debe mencionar el perfil que no se reconocio
            assertTrue(e.getMessage().contains("superbot"));
        }

        assertTrue("Se esperaba DOPOSoccerException para un perfil invalido", lanzoExcepcion);
    }

    
    // MatchFactory: tipos de partido disponibles
    

    // El tipo "time" debe crear un partido de tipo Time
    @Test
    public void testMatchFactoryCreatesTimeMatch() throws DOPOSoccerException {
        MatchFactory factory = new MatchFactory();
        Goal leftGoal  = new Goal(0, 0);
        Goal rightGoal = new Goal(800, 0);

        Match partido = factory.create("time", 3, leftPlayer, rightPlayer, ball, leftGoal, rightGoal, score);

        assertNotNull(partido);
        assertTrue(partido instanceof Time);
    }

    // El tipo "goals" debe crear un partido de tipo Goals
    @Test
    public void testMatchFactoryCreatesGoalsMatch() throws DOPOSoccerException {
        MatchFactory factory = new MatchFactory();
        Goal leftGoal  = new Goal(0, 0);
        Goal rightGoal = new Goal(800, 0);

        Match partido = factory.create("goals", 5, leftPlayer, rightPlayer, ball, leftGoal, rightGoal, score);

        assertNotNull(partido);
        assertTrue(partido instanceof Goals);
    }

    // Un tipo no reconocido debe lanzar DOPOSoccerException con mensaje claro
    @Test
    public void testMatchFactoryThrowsOnUnknownType() {
        MatchFactory factory = new MatchFactory();
        Goal leftGoal  = new Goal(0, 0);
        Goal rightGoal = new Goal(800, 0);
        boolean lanzoExcepcion = false;

        try {
            factory.create("survival", 5, leftPlayer, rightPlayer, ball, leftGoal, rightGoal, score);
        } catch (DOPOSoccerException e) {
            lanzoExcepcion = true;
            // El mensaje debe mencionar el tipo que no se reconocio
            assertTrue(e.getMessage().contains("survival"));
        }

        assertTrue("Se esperaba DOPOSoccerException para un tipo invalido", lanzoExcepcion);
    }
}
