package presentation;


import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import javax.swing.JButton;

/**
 * Botón con las esquinas redondeadas y su propio color de relleno,
 * usado en toda la interfaz en lugar del botón estándar de Swing.
 */
public class RoundedButton extends JButton {
    private Color fillColor;

    /**
     * Crea un botón redondeado con su texto y su color de relleno.
     * @param text texto que muestra el botón
     * @param fillColor color de relleno del botón
     */
    public RoundedButton(String text, Color fillColor){
        super(text);
        this.fillColor = fillColor;
        setForeground(new Color(0x14212a));
        setFocusPainted(false);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
    }

    /**
     * Dibuja el fondo redondeado del botón con su color de relleno, y
     * luego el texto encima.
     * @param g contexto gráfico sobre el que se dibuja el botón
     */
    @Override
    protected void paintComponent(Graphics g){
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fillColor);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
        g2.dispose();
        super.paintComponent(g); //dibuja el texto encima
    }
}
