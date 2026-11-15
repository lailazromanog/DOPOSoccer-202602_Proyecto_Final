package presentation;


import javax.swing.JFrame;
import domain.Ball;
import domain.Goal;
import domain.Goals;
import domain.Match;
import domain.Player;
import domain.Score;
import domain.Time;
import domain.Timer;

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
    public void startGame(boolean byTime, int value){
        Match match = buildMatch(byTime, value);
        FieldPanel fieldPanel = new FieldPanel(match);
        setContentPane(fieldPanel);
        revalidate();
        pack();
        setLocationRelativeTo(null);
        fieldPanel.requestFocusInWindow();
    }

    public static void main(String[] args) {
        GameGUI ventana = new GameGUI();
        ventana.setVisible(true);
    }
}
