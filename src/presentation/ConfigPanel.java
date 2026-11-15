package presentation;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class ConfigPanel extends JPanel {
    private JRadioButton byTimeOption;
    private JRadioButton byGoalsOption;
    private JLabel valueLabel;
    private JTextField valueField;
    private JButton playButton;


    public ConfigPanel(GameGUI gui){
        prepareElements();
        prepareActions(gui);
    }

    /**
     * Crea y ubica los componentes de la pantalla
     */
    private void prepareElements(){
        setPreferredSize(new Dimension(769, 450));
        setBackground(new Color(0x1b2b34));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40));

        JLabel title = label("DOPOSoccer", new Font("Arial", Font.BOLD, 42), Color.WHITE);
        JLabel subtitle = label("Configuracion del partido", new Font("Arial", Font.PLAIN, 18), new Color(0xbcd4df));

        //tipo de partido
        byTimeOption = radio("Por tiempo", true);
        byGoalsOption = radio("Por goles", false);
        ButtonGroup group = new ButtonGroup();
        group.add(byTimeOption);
        group.add(byGoalsOption);

        //tiempo de la partida o numero de goles que elija el usuario
        valueLabel = label("Minutos: ", new Font("Arial", Font.PLAIN, 16), Color.WHITE);
        valueField = new JTextField("3", 5);
        valueField.setMaximumSize(new Dimension(90, 32));
        valueField.setAlignmentX(Component.CENTER_ALIGNMENT);

        playButton = new JButton("JUGAR");
        playButton.setFont(new Font("Arial", Font.BOLD, 20));
        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        add(title);
        add(Box.createVerticalStrut(6));
        add(subtitle);
        add(Box.createVerticalStrut(34));
        add(byTimeOption);
        add(Box.createVerticalStrut(4));
        add(byGoalsOption);
        add(Box.createVerticalStrut(24));
        add(valueLabel);
        add(Box.createVerticalStrut(6));
        add(valueField);
        add(Box.createVerticalStrut(34));
        add(playButton);
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
                boolean byTime = byTimeOption.isSelected();
                int value = readValue();
                gui.startGame(byTime, value);
            }
        });
    }

    /**
     * lee el numero escrito, si no es valido usa un valor por defecto 
     */
    private int readValue(){
        try{
            int v = Integer.parseInt(valueField.getText().trim());
            if(v >0){
                return v;
            }
        }catch(NumberFormatException ex){
            //se escribe el valor por defecto, ya que lo que escribio el usuario no es un numero
        }
        if(byTimeOption.isSelected()){
            return 2; // min por defecto 
        }else{
            return 4; //goles por defecto
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
