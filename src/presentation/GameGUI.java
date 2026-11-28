package presentation;

import domain.DOPOSoccerException;
import domain.Game;
import domain.Progress;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

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
    private Progress progress;
    private int resW = 769;   // resolucion elegida (por defecto mediana)
    private int resH = 450;

    /**
     * Crea la ventana principal con el progreso guardado de ambos
     * jugadores (o uno nuevo si no hay nada guardado) y prepara sus
     * elementos iniciales.
     */
    private GameGUI() {
        progress = new Progress(); //abre el progreso guardado en disco
        prepareElements();
    }

    /**
     * Guarda en disco el progreso de ambos jugadores. Si no se puede
     * guardar, lo registra en el log sin interrumpir el juego.
     */
    private void saveProgress() {
        try {
            progress.save();
        } catch (DOPOSoccerException ex) {
            GameLogger.error("No se pudo guardar el progreso", ex);
        }
    }

    /**
     * Entrega la única instancia de la ventana principal, creándola la
     * primera vez que se pide.
     * @return la instancia única de la ventana principal
     */
    public static GameGUI getInstance(){
        if(instance == null){
            instance = new GameGUI();
        }
        return instance;
    }

    /**
     * Prepara el título, el tamaño y el panel inicial de la ventana,
     * mostrando primero la pantalla de configuración.
     */
    private void prepareElements() {
        setTitle("DOPOSoccer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            /**
             * Guarda el progreso de los jugadores antes de cerrar la ventana.
             * @param e evento de cierre de la ventana
             */
            @Override public void windowClosing(WindowEvent e) {
                saveProgress();
            }
        });
        setResizable(false);

        configPanel = new ConfigPanel(this);
        add(configPanel);

        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Guarda la resolución elegida y muestra la pantalla de selección
     * de personajes con el tipo de partido y el rival ya definidos.
     * @param matchType tipo de partido elegido, por tiempo o por goles
     * @param value minutos de duración del partido o goles para ganar, según el tipo de partido
     * @param opponent modalidad del jugador 2, humano o perfil de máquina
     * @param resW ancho de la ventana según la resolución elegida
     * @param resH alto de la ventana según la resolución elegida
     */
    public void showCharacterSelect(String matchType, int value,String opponent, int resW, int resH) {
        this.resW = resW;
        this.resH = resH;
        CharacterSelectPanel characterPanel = new CharacterSelectPanel(this, matchType, value, opponent);
        setContentPane(characterPanel);
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Lo llama CharacterSelectPanel al dar "JUGAR": crea la fachada y
     * entra a la cancha. Si alguno de los datos del partido no es
     * reconocido por el dominio, muestra el error al usuario, lo
     * registra en el log y se queda en la pantalla actual.
     * @param matchType tipo de partido elegido, por tiempo o por goles
     * @param value minutos de duración del partido o goles para ganar, según el tipo de partido
     * @param country1 país del personaje del jugador 1
     * @param country2 país del personaje del jugador 2
     * @param opponent modalidad del jugador 2, humano o perfil de máquina
     */
    public void startGame(String matchType, int value, String country1, String country2, String opponent) {
        try {
            Game game = progress.newGame(matchType, value, country1, country2, opponent);
            FieldPanel fieldPanel = new FieldPanel(this, game, resW, resH);
            setContentPane(fieldPanel);
            revalidate();
            pack();
            setLocationRelativeTo(null);
            fieldPanel.requestFocusInWindow();
        } catch (DOPOSoccerException ex) {
            GameLogger.error("No se pudo crear el partido", ex);
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Entra a la cancha con un partido ya cargado desde archivo, con
     * la resolución dada.
     * @param game partido ya cargado desde archivo
     * @param resW ancho de la ventana según la resolución elegida
     * @param resH alto de la ventana según la resolución elegida
     */
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
     * Muestra la pantalla de fin de partido con el resultado del
     * partido dado.
     * @param game partido que acaba de terminar
     */
    public void showEndScreen(Game game) {
        saveProgress();   // el partido ya repartio sus puntos a los perfiles
        setContentPane(new EndPanel(this, game));
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Muestra la tienda de mejoras de estadísticas.
     */
    public void showShop() {
        setContentPane(new ShopPanel(this));
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Vuelve a la pantalla de configuración del partido.
     */
    public void showConfig() {
        setContentPane(new ConfigPanel(this));
        revalidate();
        pack();
        setLocationRelativeTo(null);
    }


    /**
     * Entrega el progreso de los dos jugadores, para que las pantallas
     * de la aplicación puedan consultarlo y mejorarlo.
     * @return el progreso de los dos jugadores
     */
    public Progress getProgress() {
        return progress;
    }

    /**
     * Punto de entrada de la aplicación: crea la ventana principal y la muestra.
     * @param args argumentos de línea de comandos, sin uso en este programa
     */
    public static void main(String[] args) {
        GameGUI ventana = GameGUI.getInstance();
        ventana.setVisible(true);
    }
}