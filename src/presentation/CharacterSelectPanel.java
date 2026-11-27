package presentation;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Pantalla de selección de personajes
 */
public class CharacterSelectPanel extends JPanel{
    private BufferedImage background;
    private JComboBox<String> leftCharacter;
    private JComboBox<String> rightCharacter;
    private RoundedButton playButton;

    
    private String matchType;
    private int value;
    private String opponent;

    /**
     * Crea la pantalla de selección de personajes, con el tipo de
     * partido, el valor del partido y el rival ya elegidos en la
     * pantalla anterior.
     * @param gui ventana principal que recibe la orden de empezar el partido
     * @param matchType tipo de partido elegido, por tiempo o por goles
     * @param value minutos de duración del partido o goles para ganar, según el tipo de partido
     * @param opponent modalidad del jugador 2, humano o perfil de máquina
     */
    public CharacterSelectPanel(GameGUI gui, String matchType, int value, String opponent){
        this.matchType = matchType;
        this.value = value;
        this.opponent = opponent;
        loadBackground();
        prepareElements();
        prepareActions(gui);
    }

    /**
     * Carga la imagen de fondo de la pantalla de selección de
     * personajes desde el archivo de recursos del juego.
     */
    private void loadBackground(){
        try{
            background = ImageIO.read( new File("resources/menu.png"));
        }catch(IOException e){
            System.out.println("No se pudo cargar el fondo: " + e.getMessage());
        }
    }

    /**
     * Dibuja la imagen de fondo de la pantalla, si se pudo cargar.
     * @param g contexto gráfico sobre el que se dibuja el panel
     */
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(background != null){
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    /**
     * Crea y ubica los componentes de la pantalla dentro de la
     * tarjeta central: el combo de personaje de cada jugador y el
     * botón de jugar.
     */
    private void prepareElements(){
        setPreferredSize(new Dimension(769, 450));
        setLayout( new GridBagLayout());

        JPanel card = new JPanel();
        card.setBackground(new Color(0x14212a));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(0xF28C28), 3, true), BorderFactory.createEmptyBorder(26, 48, 26, 48)));

        JLabel title = label("Elige tu personaje", new Font("Arial", Font.BOLD, 26), Color.WHITE);

        String[] paises = {"Corea del Sur", "Camerun", "Nigeria", "Estados Unidos"};
        leftCharacter = new JComboBox<>(paises);
        leftCharacter.setMaximumSize(new Dimension(220, 32));
        leftCharacter.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightCharacter = new JComboBox<>(paises);
        rightCharacter.setSelectedIndex(1); // que empiecen distintos
        rightCharacter.setMaximumSize(new Dimension(220, 32));
        rightCharacter.setAlignmentX(Component.CENTER_ALIGNMENT);

        playButton = new RoundedButton("JUGAR", new Color(0xF28C28));
        playButton.setFont(new Font("Arial", Font.BOLD, 22));
        playButton.setMaximumSize(new Dimension(220, 52));
        playButton.setPreferredSize(new Dimension(220, 52));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(title);
        card.add(Box.createVerticalStrut(26));
        card.add(label("Jugador 1:", new Font("Arial", Font.PLAIN, 16), new Color(0xbcd4df)));
        card.add(Box.createVerticalStrut(6));
        card.add(leftCharacter);
        card.add(Box.createVerticalStrut(18));
        card.add(label("Jugador 2:", new Font("Arial", Font.PLAIN, 16), new Color(0xbcd4df)));
        card.add(Box.createVerticalStrut(6));
        card.add(rightCharacter);
        card.add(Box.createVerticalStrut(28));
        card.add(playButton);

        add(card);
    }

    /**
     * Asigna al botón de jugar la acción de iniciar el partido con los
     * personajes elegidos.
     * @param gui ventana principal que recibe la orden de empezar el partido
     */
    private void prepareActions(final GameGUI gui){
        playButton.addActionListener( new ActionListener(){
            /**
             * Toma el personaje elegido por cada jugador y le pide a
             * la ventana principal que arranque el partido con ellos.
             * @param e evento de clic del botón jugar
             */
            @Override
            public void actionPerformed(ActionEvent e){
                String country1 = (String) leftCharacter.getSelectedItem();
                String country2 = (String) rightCharacter.getSelectedItem();
                gui.startGame(matchType, value, country1, country2, opponent);
            }
        });
    }

    /**
     * Crea una etiqueta de texto con la fuente y el color dados, para
     * no repetir la misma configuración en cada etiqueta de la pantalla.
     * @param text texto de la etiqueta
     * @param font fuente de la etiqueta
     * @param color color del texto de la etiqueta
     * @return la etiqueta creada
     */
    private JLabel label(String text, Font font, Color color){
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

}
