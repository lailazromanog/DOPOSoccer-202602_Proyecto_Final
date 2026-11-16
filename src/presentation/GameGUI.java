package presentation;


import domain.Appereance;
import domain.Ball;
import domain.BlueAura;
import domain.Character;
import domain.Goal;
import domain.Goals;
import domain.Illusion;
import domain.Lightning;
import domain.Match;
import domain.Player;
import domain.Score;
import domain.Time;
import domain.Timer;
import domain.Tornado;
import javax.swing.JFrame;

/**
 * Ventana principal de DOPOSoccer
 * 
 * Ensambla la interfaz, la construccion se separa en: 
 * prepareElements() quien crea los componente 
 * prepareActions() quien conecta los eventos de la venta
 * 
 * 
 */
public class GameGUI extends JFrame {
    //la cancha 
    private ConfigPanel configPanel;
    private int resW = 769;
    private int resH = 450;

    
    public GameGUI(){
        prepareElements();
    }

    /**
     * crea y ubica los componentes de la ventana
     */
    private void prepareElements(){
        setTitle("DOPOSoccer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        
        configPanel = new ConfigPanel(this);
        add(configPanel);

        pack();
        setLocationRelativeTo(null);
    }

    private Match buildMatch(boolean byTime, int value){
        Player p1 = new Player();
        Player p2 = new Player();
        Ball b = new Ball();
        Goal leftGoal  = new Goal(-100, 290, (FieldPanel.LEFT_POST - FieldPanel.BALL_DIAMETER / 2) + 100, 135);
        Goal rightGoal = new Goal(FieldPanel.RIGHT_POST - FieldPanel.BALL_DIAMETER / 2, 290, 200, 135);
        Score score = new Score(p1, p2);

        if(byTime){
            Timer timer = new Timer(value * 60);
            return new Time(p1, p2, b, leftGoal, rightGoal, timer, score);
        }else{
            Timer timer = new Timer(0);
            return new Goals(p1, p2, b, leftGoal, rightGoal, timer, score, value);
        }

    }

    /**
     * Muestra la pantalla de selección de personajes
     */
    public void showCharacterSelect(boolean byTime, int value, int resW, int resH){
        this.resW = resW; 
        this.resH = resH;
        CharacterSelectPanel characterPanel = new CharacterSelectPanel(this, byTime, value);
        setContentPane(characterPanel);
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    public void startGame(boolean byTime, int value, String country1, String country2){
        Match match = buildMatch(byTime, value);
        match.getPlayerOne().setCharacter(createCharacter(country1));
        match.getPlayerTwo().setCharacter(createCharacter(country2));
        FieldPanel fieldPanel = new FieldPanel(match, resW, resH);
        setContentPane(fieldPanel);
        revalidate();
        pack();
        setLocationRelativeTo(null);
        fieldPanel.requestFocusInWindow();
    }

    /**
     * Crea el personaje según el país elegido, con su poder
     */
    private Character createCharacter(String country){
        if(country.equals("Camerun")){
            return new Character("Camerun", "Camerun", new Lightning(), new Appereance());
        }else if(country.equals("Nigeria")){
            return new Character("Nigeria", "Nigeria", new Tornado(), new Appereance());
        }else if(country.equals("Estados Unidos")){
            return new Character("Estados Unidos", "Estados Unidos", new Illusion(), new Appereance());
        }else{
            return new Character("Corea del Sur", "Corea del Sur", new BlueAura(), new Appereance());
        }
    }
    public static void main(String[] args) {
        GameGUI ventana = new GameGUI();
        ventana.setVisible(true);
    }
}
