package presentation;

import domain.Game;
import javax.swing.JFrame;

/**
 * Ventana principal de DOPOSoccer.
 *
 * Maneja el flujo de pantallas (configuracion -> personajes -> cancha) y la
 * resolucion. Ya NO arma el partido ni crea personajes: eso ahora lo hace la
 * fachada del dominio (Game). GameGUI solo le  pide un Game listo y se lo pasa
 * al FieldPanel.
 */
public class GameGUI extends JFrame {
    private static GameGUI instance;
    private ConfigPanel configPanel;
    private domain.ProfileStore profileStore;
    private int resW = 769;   // resolucion elegida (por defecto mediana)
    private int resH = 450;

    private GameGUI() {
        profileStore = new domain.ProfileStore(); //carga el progreso guardado
        prepareElements();
    }

    /**
     * Punto unico de acceso a la venta
     */
    public static GameGUI getInstance(){
        if(instance == null){
            instance = new GameGUI();
        }
        return instance;
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
        Game game = new Game(matchType, value, country1, country2, opponent, profileStore.getLeft(), profileStore.getRight());
        FieldPanel fieldPanel = new FieldPanel(this, game, resW, resH);
        setContentPane(fieldPanel);
        revalidate();
        pack();
        setLocationRelativeTo(null);
        fieldPanel.requestFocusInWindow();
    }

    /** Entra a la cancha con un partido ya cargado desde archivo. */
    public void openGame(Game game, int resW, int resH) {
        this.resW = resW;
        this.resH = resH;
        FieldPanel fieldPanel = new FieldPanel(this, game, resW, resH);
        setContentPane(fieldPanel);
        revalidate();
        pack();
        setLocationRelativeTo(null);
        fieldPanel.requestFocusInWindow();
    }

    /** 
     * Muestra la pantalla de fin de partido. 
     */
    public void showEndScreen(Game game) {
        setContentPane(new EndPanel(this, game));
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    /** 
     * Muestra la tienda de mejoras. 
     */
    public void showShop() {
        setContentPane(new ShopPanel(this));
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    /** 
     * Vuelve a la pantalla de configuracion. 
     */
    public void showConfig() {
        setContentPane(new ConfigPanel(this));
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }
    

    /** 
     * Acceso al progreso, para la pantalla de mejoras. 
     */
    public domain.ProfileStore getProfileStore() {
        return profileStore;
    }

    public static void main(String[] args) {
        GameGUI ventana = GameGUI.getInstance();
        ventana.setVisible(true);
    }
}