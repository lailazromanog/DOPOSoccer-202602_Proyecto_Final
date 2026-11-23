package presentation;

import domain.Profile;
import domain.ProfileStore;

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
 * Pantalla "Mejorar estadisticas" (la tienda de puntos).
 * Lee y modifica el progreso (ProfileStore) que mantiene GameGUI.
 */
public class ShopPanel extends JPanel {
    private GameGUI gui;
    private ProfileStore store;

    public ShopPanel(GameGUI gui) {
        this.gui = gui;
        this.store = gui.getProfileStore();
        setBackground(new Color(0x0e1a24));
        setLayout(new GridBagLayout());
        setPreferredSize(new Dimension(960, 560));
        buildUI();
    }

    /** (Re)arma la pantalla; se llama al inicio y despues de cada mejora. */
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
        players.add(playerColumn("JUGADOR 1", store.getLeft(), new Color(0x213a5c)));
        players.add(playerColumn("JUGADOR 2", store.getRight(), new Color(0x5c2130)));
        card.add(players);

        card.add(Box.createVerticalStrut(16));
        RoundedButton back = new RoundedButton("Listo", new Color(0xF28C28));
        back.setFont(new Font("Arial", Font.BOLD, 18));
        back.setMaximumSize(new Dimension(200, 44));
        back.setPreferredSize(new Dimension(200, 44));
        back.setAlignmentX(Component.CENTER_ALIGNMENT);
        back.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent e){ gui.showConfig(); }
        });
        card.add(back);

        add(card);
        revalidate();
        repaint();
    }

    private JPanel playerColumn(String name, Profile profile, Color color) {
        JPanel col = new JPanel();
        col.setBackground(color);
        col.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        col.setLayout(new BoxLayout(col, BoxLayout.Y_AXIS));

        col.add(label(name + "     " + profile.getPoints() + " pts", new Font("Arial", Font.BOLD, 18), Color.WHITE));
        col.add(Box.createVerticalStrut(12));
        col.add(statRow(profile, "speed", "SPEED", "Velocidad +" + (10 * (profile.getSpeed() - 1)) + "%"));
        col.add(Box.createVerticalStrut(8));
        col.add(statRow(profile, "kick", "KICK", "Dano x" + mult(profile.getKick())));
        col.add(Box.createVerticalStrut(8));
        col.add(statRow(profile, "jump", "JUMP", "Salto +" + (10 * (profile.getJump() - 1)) + "%"));
        col.add(Box.createVerticalStrut(8));
        col.add(statRow(profile, "survival", "SURVIVAL", "Vida max " + (100 + 10 * (profile.getSurvival() - 1))));
        return col;
    }

    private JPanel statRow(final Profile profile, final String stat, String name, String detail) {
        JPanel row = new JPanel();
        row.setOpaque(false);
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        info.add(left(label(name + "  (nivel " + levelOf(profile, stat) + ")", new Font("Arial", Font.BOLD, 14), Color.WHITE)));
        info.add(left(label(detail, new Font("Arial", Font.PLAIN, 11), new Color(0xbcd4df))));

        JButton btn = new JButton();
        btn.setFont(new Font("Arial", Font.BOLD, 12));
        if (profile.isMax(stat)) {
            btn.setText("MAXIMO");
            btn.setEnabled(false);
        } else if (profile.canUpgrade(stat)) {
            btn.setText("MEJORAR - " + profile.upgradeCost(stat));
        } else {
            btn.setText("FALTAN " + (profile.upgradeCost(stat) - profile.getPoints()) + " pts");
            btn.setEnabled(false);
        }

        final String statName = name;
        btn.addActionListener(new ActionListener(){
            @Override public void actionPerformed(ActionEvent e){
                int cost = profile.upgradeCost(stat);
                int option = JOptionPane.showConfirmDialog(ShopPanel.this,
                        "Gastar " + cost + " pts para subir " + statName + " al nivel " + (levelOf(profile, stat) + 1) + "?",
                        "Mejorar estadistica", JOptionPane.YES_NO_OPTION);
                if (option == JOptionPane.YES_OPTION) {
                    profile.upgrade(stat);
                    buildUI();        // refresca la pantalla
                }
            }
        });

        row.add(info);
        row.add(Box.createHorizontalGlue());
        row.add(btn);
        return row;
    }

    private int levelOf(Profile p, String stat) {
        if (stat.equals("speed")) {
            return p.getSpeed();
        } else if (stat.equals("kick")) {
            return p.getKick();
        } else if (stat.equals("jump")) {
            return p.getJump();
        } else {
            return p.getSurvival();
        }
    }

    private String mult(int kick) {
        return String.format("%.1f", 1 + 0.10 * (kick - 1));
    }

    private JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JLabel left(JLabel l) {
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }
}