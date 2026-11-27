package presentation;

import domain.DOPOSoccerException;
import domain.Game;
import domain.Progress;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Pantalla de fin de partido: resultado + navegacion a la tienda o a configuracion. */
public class EndPanel extends JPanel {

    /**
     * Crea la pantalla de fin de partido con el resultado del partido
     * dado, los puntos acumulados de ambos jugadores y los botones
     * para ir a la tienda de mejoras o volver al menú.
     * @param gui ventana principal que recibe la orden de cambiar de pantalla
     * @param game partido que acaba de terminar
     */
    public EndPanel(final GameGUI gui, Game game) {
        setBackground(new Color(0x0e1a24));
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(769, 450));

        JPanel card = new JPanel();
        card.setBackground(new Color(0x14212a));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xF28C28), 3, true),
                BorderFactory.createEmptyBorder(26, 48, 26, 48)));

        int w = game.getWinner();
        String result;
        if (w == 1) {
            result = "Gana el Jugador 1";
        } else if (w == 2) {
            result = "Gana el Jugador 2";
        } else {
            result = "Empate";
        }

        card.add(label(result, new Font("Arial", Font.BOLD, 30), Color.WHITE));
        card.add(Box.createVerticalStrut(10));
        card.add(label(game.getLeftScore() + " - " + game.getRightScore(),
                new Font("Arial", Font.BOLD, 24), new Color(0xF28C28)));
        card.add(Box.createVerticalStrut(8));
        String pointsText;
        try {
            pointsText = "Puntos  J1: " + gui.getProgress().getPoints(Progress.LEFT)
                    + "     J2: " + gui.getProgress().getPoints(Progress.RIGHT);
        } catch (DOPOSoccerException ex) {
            // los identificadores son constantes validas, asi que esto no deberia ocurrir
            GameLogger.error("No se pudieron leer los puntos de los jugadores", ex);
            pointsText = "Puntos no disponibles";
        }
        card.add(label(pointsText, new Font("Arial", Font.PLAIN, 16), new Color(0xbcd4df)));
        card.add(Box.createVerticalStrut(24));

        RoundedButton shop = new RoundedButton("Mejorar estadisticas", new Color(0xF28C28));
        shop.setFont(new Font("Arial", Font.BOLD, 18));
        shop.setMaximumSize(new Dimension(240, 48));
        shop.setPreferredSize(new Dimension(240, 48));
        shop.setAlignmentX(Component.CENTER_ALIGNMENT);
        shop.addActionListener(new ActionListener(){
            /**
             * Muestra la tienda de mejoras de estadísticas.
             * @param e evento de clic del botón mejorar estadísticas
             */
            @Override public void actionPerformed(ActionEvent e){ gui.showShop(); }
        });
        card.add(shop);
        card.add(Box.createVerticalStrut(10));

        RoundedButton menu = new RoundedButton("Menu", new Color(0x3a4a57));
        menu.setFont(new Font("Arial", Font.BOLD, 16));
        menu.setMaximumSize(new Dimension(240, 42));
        menu.setPreferredSize(new Dimension(240, 42));
        menu.setAlignmentX(Component.CENTER_ALIGNMENT);
        menu.addActionListener(new ActionListener(){
            /**
             * Vuelve a la pantalla de configuración del partido.
             * @param e evento de clic del botón menú
             */
            @Override public void actionPerformed(ActionEvent e){ gui.showConfig(); }
        });
        card.add(menu);

        add(card);
    }

    /**
     * Crea una etiqueta de texto con la fuente y el color dados, para
     * no repetir la misma configuración en cada etiqueta de la pantalla.
     * @param text texto de la etiqueta
     * @param font fuente de la etiqueta
     * @param color color del texto de la etiqueta
     * @return la etiqueta creada
     */
    private JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }
}