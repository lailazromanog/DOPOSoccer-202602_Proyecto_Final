package presentation;

import domain.DOPOSoccerException;
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
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

/**
 * Pantalla de configuracion del partido
 * Deja elegir el tipo de partido, por tiempo o por goles y el valor.
 */

public class ConfigPanel extends JPanel {

    private BufferedImage background;
    private JRadioButton byTimeOption;
    private JRadioButton byGoalsOption;
    private JLabel valueLabel;
    private JTextField valueField;
    private RoundedButton playButton;
    private JComboBox<Resolution> resolutionBox;
    private JComboBox<String> opponentBox;


    public ConfigPanel(GameGUI gui){
        loadBackground();
        prepareElements();
        prepareActions(gui);
    }

    /**
     * Carga la imagen de fondo de la configuracion
     */
    private void loadBackground(){
        try{
            background = ImageIO.read(new File("resources/menu.png"));
        }catch(IOException e){
            System.out.println("No se pudo cargar el fondo: " + e.getMessage());
        }
    }
    /**
     * Dibuja la foto de fondo 
     */
    @Override
    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if(background != null){
            g.drawImage(background, 0, 0, getWidth(), getHeight(), this);
        }
    }

    /**
     * Crea y ubica los componentes de la pantalla
     */
    private void prepareElements(){
        setPreferredSize(new Dimension(769, 450));
        setLayout(new GridBagLayout()); // centra la tarjeta en la pantalla

        // tarjeta oscura para que todo se lea sobre la foto
        JPanel card = new JPanel();
        card.setBackground(new Color(0x14212a));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xF28C28), 3, true),
                BorderFactory.createEmptyBorder(26, 48, 26, 48)));

        JLabel subtitle = label("Configuracion del partido", new Font("Arial", Font.BOLD, 26), Color.WHITE);

        //tipo de partido
        byTimeOption = radio("Por tiempo", true);
        byGoalsOption = radio("Por goles", false);
        ButtonGroup group = new ButtonGroup();
        group.add(byTimeOption);
        group.add(byGoalsOption);

        //tiempo de la partida o numero de goles que elija el usuario
        valueLabel = label("Minutos: ", new Font("Arial", Font.PLAIN, 16), new Color(0xbcd4df));
        valueField = new JTextField("3", 5);
        valueField.setMaximumSize(new Dimension(90, 34));
        valueField.setFont(new Font("Arial", Font.BOLD, 16));
        valueField.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        resolutionBox = new JComboBox<>(Resolution.values());
        resolutionBox.setSelectedItem(Resolution.MEDIANA); // mediana por defecto
        resolutionBox.setMaximumSize(new Dimension(220, 32));
        resolutionBox.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String[] rivales = {"Humano", "Maquina (facil)", "Maquina (media)", "Maquina (dificil)"};
        opponentBox = new JComboBox<>(rivales);
        opponentBox.setSelectedIndex(0); // humano por defecto
        opponentBox.setMaximumSize(new Dimension(220, 32));
        opponentBox.setAlignmentX(Component.CENTER_ALIGNMENT);

        playButton = new RoundedButton("JUGAR", new Color(0xF28C28));
        playButton.setFont(new Font("Arial", Font.BOLD, 22));
        playButton.setMaximumSize(new Dimension(220, 52));
        playButton.setPreferredSize(new Dimension(220, 52));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        // todo va en la card
        card.add(subtitle);
        card.add(Box.createVerticalStrut(22));
        card.add(byTimeOption);
        card.add(Box.createVerticalStrut(4));
        card.add(byGoalsOption);
        card.add(Box.createVerticalStrut(20));
        card.add(valueLabel);
        card.add(Box.createVerticalStrut(6));
        card.add(valueField);
        card.add(Box.createVerticalStrut(16));
        card.add(label("Resolucion:", new Font("Arial", Font.PLAIN, 14), new Color(0xbcd4df)));
        card.add(resolutionBox);
        card.add(Box.createVerticalStrut(12));
        card.add(label("Rival:", new Font("Arial", Font.PLAIN, 14), new Color(0xbcd4df)));
        card.add(opponentBox);
        card.add(Box.createVerticalStrut(22));
        card.add(playButton);

        add(card);
    }

    /**
     * crea cada boton en la pantalla
     */
    private void prepareActions(final GameGUI gui){
        byTimeOption.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                valueLabel.setText("Minutos: ");
            }
        });

        byGoalsOption.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                valueLabel.setText("Goles para ganar: ");
            }
        });

        playButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try {
                    String matchType;
                    if (byTimeOption.isSelected()) {
                        matchType = "time";
                    } else {
                        matchType = "goals";
                    }
                    int value = readValue();          // puede lanzar DOPOSoccerException
                    Resolution res = (Resolution) resolutionBox.getSelectedItem();
                    String opponent;
                    int oi = opponentBox.getSelectedIndex();
                    if (oi == 0) {
                        opponent = "human";
                    } else if (oi == 1) {
                        opponent = "facil";
                    } else if (oi == 2) {
                        opponent = "medio";
                    } else {
                        opponent = "dificil";
                    }
                    gui.showCharacterSelect(matchType, value, opponent, res.getWidth(), res.getHeight());
                }catch (DOPOSoccerException ex) {
                    GameLogger.error("Configuracion invalida del partido", ex);  // para los programadores
                    JOptionPane.showMessageDialog(ConfigPanel.this,ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // para el usuario
                }
            }
        });
    }

    /**
     * lee el numero escrito, si no es valido usa un valor por defecto 
     */
    private int readValue() throws DOPOSoccerException{
        String text = valueField.getText().trim();
        try{
            int v = Integer.parseInt(text);
            if(v <= 0){
                throw new DOPOSoccerException("El valor debe ser un numero mayor que 0. Escribiste: ");
            }
            return v;
        }catch(NumberFormatException ex){
            throw new DOPOSoccerException("El valor debe ser un numero entero valido.");
        }
    }

    //pa no repetir codigo 
    private JLabel label(String text, Font font, Color color){
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JRadioButton radio(String text, boolean selected){
        JRadioButton r = new JRadioButton(text, selected);
        r.setFont(new Font("Arial", Font.PLAIN, 16));
        r.setForeground(Color.WHITE);
        r.setOpaque(false);
        r.setAlignmentX(Component.CENTER_ALIGNMENT);
        return r; 
    }
}
