package presentation;

import javax.swing.JFrame;

/**
 * Ventana principal de DOPOSoccer
 * 
 * Ensambla la interfaz, la construccion se separa en: 
 * prepareElements() quien crea los componente 
 * prepareActions() quien conecta los eventos de la venta
 * 
 * 
 */
public class GameGUI extends JFrame {
    //la cancha 
    private FieldPanel canchaPanel;

    public GameGUI(){
        prepareElements();
        prepareActions();
    }

    /**
     * crea y ubica los componentes de la ventana
     */
    private void prepareElements(){
        setTitle("DOPOSoccer");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);

        canchaPanel = new FieldPanel();
        add(canchaPanel);

        pack();
        setLocationRelativeTo(null);
    }

    private void prepareActions(){

    }
    
    public static void main(String[] args) {
        GameGUI ventana = new GameGUI();
        ventana.setVisible(true);
    }
}
