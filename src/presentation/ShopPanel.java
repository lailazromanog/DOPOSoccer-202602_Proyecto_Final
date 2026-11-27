package presentation;

import domain.DOPOSoccerException;
import domain.Progress;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


/**
 * Pantalla de mejora de estadísticas, la tienda de puntos.
 * Consulta y modifica el progreso de los jugadores a través de la
 * fachada de progreso del dominio, sin conocer los perfiles ni su almacén.
 */
public class ShopPanel extends JPanel {
    private GameGUI gui;
    private Progress progress;

    /**
     * Crea la pantalla de mejoras de estadísticas, tomando el progreso
     * de ambos jugadores desde la ventana principal.
     * @param gui ventana principal, de donde se toma el progreso de los jugadores
     */
    public ShopPanel(GameGUI gui) {
        this.gui = gui;
        this.progress = gui.getProgress();
        setBackground(new Color(0x0e1a24));
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(960, 560));
        buildUI();
    }

    /**
     * Arma de nuevo toda la pantalla a partir del progreso actual de
     * los dos jugadores. Se llama al crear la pantalla y de nuevo
     * después de cada mejora, para que los precios y los niveles
     * mostrados queden al día.
     */
    private void buildUI() {
        removeAll();
        JPanel card = new JPanel();
        card.setBackground(new Color(0x14212a));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xF28C28), 3, true),
                BorderFactory.createEmptyBorder(20, 32, 20, 32)));

        card.add(label("Mejorar estadisticas", new Font("Arial", Font.BOLD, 26), Color.WHITE));
        card.add(Box.createVerticalStrut(4));
        card.add(label("Cada nivel suma 10% sobre la base. Precio: 500, 1000, 2000...  Ganas 100 pts por victoria y 25 por derrota.",
                new Font("Arial", Font.PLAIN, 12), new Color(0xbcd4df)));
        card.add(Box.createVerticalStrut(16));

        JPanel players = new JPanel(new GridLayout(1, 2, 20, 0));
        players.setOpaque(false);
        players.add(playerColumn("JUGADOR 1", Progress.LEFT, new Color(0x213a5c)));
        players.add(playerColumn("JUGADOR 2", Progress.RIGHT, new Color(0x5c2130)));
        card.add(players);

        card.add(Box.createVerticalStrut(16));
        RoundedButton back = new RoundedButton("Listo", new Color(0xF28C28));
        back.setFont(new Font("Arial", Font.BOLD, 18));
        back.setMaximumSize(new Dimension(200, 44));
        back.setPreferredSize(new Dimension(200, 44));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(new ActionListener(){
            /**
             * Vuelve a la pantalla de configuración del partido.
             * @param e evento de clic del botón listo
             */
            @Override public void actionPerformed(ActionEvent e){ gui.showConfig(); }
        });
        card.add(back);

        add(card);
        revalidate();
        repaint();
    }

    /**
     * Arma la columna de un jugador con su nombre, sus puntos
     * acumulados y una fila por cada estadística que puede mejorar,
     * mostrando los valores finales que da cada nivel. Si el progreso
     * del jugador no se puede leer, deja un aviso en lugar de las filas
     * y registra el error para los programadores.
     * @param name nombre del jugador a mostrar
     * @param player identificador del jugador dentro del progreso
     * @param color color de fondo de la columna del jugador
     * @return el panel con la columna armada
     */
    private JPanel playerColumn(String name, int player, Color color) {
        JPanel col = new JPanel();
        col.setBackground(color);
        col.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        try {
            col.add(label(name + "     " + progress.getPoints(player) + " pts", new Font("Arial", Font.BOLD, 18), Color.WHITE));
            col.add(Box.createVerticalStrut(12));
            col.add(statRow(player, "speed", "SPEED", "Velocidad +" + progress.getSpeedBonusPercent(player) + "%"));
            col.add(Box.createVerticalStrut(8));
            col.add(statRow(player, "kick", "KICK", "Dano de patada: " + Math.round(progress.getKickDamage(player))
                    + "  (x" + String.format("%.1f", progress.getKickMultiplier(player)) + ")"));
            col.add(Box.createVerticalStrut(8));
            col.add(statRow(player, "jump", "JUMP", "Salto +" + progress.getJumpBonusPercent(player) + "%"));
            col.add(Box.createVerticalStrut(8));
            col.add(statRow(player, "survival", "SURVIVAL", "Vida maxima: " + Math.round(progress.getMaxHealth(player))));
        } catch (DOPOSoccerException ex) {
            // el jugador y las estadisticas son constantes validas, asi que esto no deberia ocurrir
            GameLogger.error("No se pudo armar la columna de la tienda de " + name, ex);
            col.add(label("No se pudo cargar el progreso", new Font("Arial", Font.BOLD, 14), Color.WHITE));
        }
        return col;
    }

    /**
     * Arma la fila de una estadística del jugador: su nombre, su nivel
     * actual, su detalle, y el botón para mejorarla, mostrando el
     * precio del próximo nivel o la razón por la que no se puede
     * mejorar todavía.
     * @param player identificador del jugador dueño de la estadística
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @param name nombre de la estadística a mostrar
     * @param detail detalle del efecto actual de la estadística, para mostrar
     * @return el panel con la fila armada
     * @throws DOPOSoccerException si el identificador del jugador o el nombre de la estadística no son válidos
     */
    private JPanel statRow(final int player, final String stat, String name, String detail) throws DOPOSoccerException {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(left(label(name + "  (nivel " + progress.getLevel(player, stat) + ")", new Font("Arial", Font.BOLD, 14), Color.WHITE)));
        info.add(left(label(detail, new Font("Arial", Font.PLAIN, 11), new Color(0xbcd4df))));

        JButton btn = new JButton();
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        // los stats que llegan aqui son siempre validos (speed, kick, jump, survival)
        // pero el progreso declara checked exception, asi que hay que capturarla
        try {
            if (progress.isMax(player, stat)) {
                btn.setText("MAXIMO");
                btn.setEnabled(false);
            } else if (progress.canUpgrade(player, stat)) {
                btn.setText("MEJORAR - " + progress.getUpgradeCost(player, stat));
            } else {
                btn.setText("FALTAN " + (progress.getUpgradeCost(player, stat) - progress.getPoints(player)) + " pts");
                btn.setEnabled(false);
            }
        } catch (DOPOSoccerException ex) {
            // no deberia ocurrir con stats hardcodeados validos
            GameLogger.error("Stat invalido al armar fila de tienda: " + stat, ex);
            btn.setText("ERROR");
            btn.setEnabled(false);
        }

        final String statName = name;
        btn.addActionListener(new ActionListener(){
            /**
             * Pregunta al jugador si confirma gastar los puntos para
             * mejorar la estadística y, si acepta, la sube de nivel y
             * vuelve a armar la pantalla con el progreso al día.
             * @param e evento de clic del botón mejorar
             */
            @Override public void actionPerformed(ActionEvent e){
                try {
                    int cost = progress.getUpgradeCost(player, stat);
                    int option = JOptionPane.showConfirmDialog(ShopPanel.this,
                            "Gastar " + cost + " pts para subir " + statName + " al nivel " + (progress.getLevel(player, stat) + 1) + "?",
                            "Mejorar estadistica", JOptionPane.YES_NO_OPTION);
                    if (option == JOptionPane.YES_OPTION) {
                        progress.upgrade(player, stat);
                        buildUI();        // refresca la pantalla
                    }
                } catch (DOPOSoccerException ex) {
                    GameLogger.error("Error al mejorar estadistica " + stat, ex);
                    JOptionPane.showMessageDialog(ShopPanel.this,
                            ex.getMessage(), "Error en la tienda", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        row.add(info);
        row.add(Box.createHorizontalGlue());
        row.add(btn);
        return row;
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

    /**
     * Alinea la etiqueta dada hacia la izquierda, para las filas de
     * información que no deben quedar centradas.
     * @param l etiqueta a alinear
     * @return la misma etiqueta, ya alineada a la izquierda
     */
    private JLabel left(JLabel l) {
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}