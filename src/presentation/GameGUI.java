package presentation;

import domain.Game;
import javax.swing.JFrame;

/**
 * Ventana principal de DOPOSoccer.
 *
 * Maneja el flujo de pantallas (configuracion -> personajes -> cancha) y la
 * resolucion. Ya NO arma el partido ni crea personajes: eso ahora lo hace la
 * fachada del dominio (Game). GameGUI solo le pide un Game listo y se lo pasa
 * al FieldPanel.
 */
public class GameGUI extends JFrame {

    private ConfigPanel configPanel;
    private int resW = 769;   // resolucion elegida (por defecto mediana)
    private int resH = 450;

    public GameGUI() {
        prepareElements();
    }

    private void prepareElements() {
        setTitle("DOPOSoccer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        configPanel = new ConfigPanel(this);
        add(configPanel);

        pack();
        setLocationRelativeTo(null);
    }

    public void showCharacterSelect(String matchType, int value,String opponent, int resW, int resH) {
        this.resW = resW;
        this.resH = resH;
        CharacterSelectPanel characterPanel = new CharacterSelectPanel(this, matchType, value, opponent);
        setContentPane(characterPanel);
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    /** Lo llama CharacterSelectPanel al dar "JUGAR": crea la fachada y entra a la cancha. */
    public void startGame(String matchType, int value, String country1, String country2, String opponent) {
        Game game = new Game(matchType, value, country1, country2, opponent);
        FieldPanel fieldPanel = new FieldPanel(game, resW, resH);
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