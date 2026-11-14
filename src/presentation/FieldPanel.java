package presentation;
import domain.Ball;
import domain.Player;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
/**
 * Pantalla de la cancha
 * Carga la imagen del estadio desde la carpeta de recursos y la dibuja como fondo.
 * Primera pieza de la interfaz construida
 * 
 * Responsabilidades: 
 * 1. Dibujar la cancha, jugadores, balon y HUD
 * 2. ejecutar el teclado para los controles de ambos jugadores
 * 3. ejecutar 
 *   
 */
//Nuevo: cargar una imagen con ImageIO y printarla con Graphics.drawImage 
public class FieldPanel extends JPanel {
    
    // la imagen de la cancha ya cargada
    private BufferedImage cancha;
    
    //objetos del dominio 
    private Player jugador1;
    private Player jugador2;
    private Ball balon;

    //
    private static final int ANCHO = 769;
    private static final int ALTO = 450;
    private static final int DIAMETRO_JUGADOR = ALTO/5; //90 px
    private static final int DIAMETRO_BALON = ALTO/9; //50 px
    
    //las teclas que presionan los jugadores
    private Set<Integer> teclasPresionadas;

    //suelo de ambos objetos 
    private static final int SUELO_JUGADOR = (int) (ALTO * 0.70);
    private static final int SUELO_BALON = (int) (ALTO * 0.78);

    //Lee las teclas y llama metodos del dominio
    //actualiza la fisica, tanto la gravdad como los rebotes
    //llama a repaint para dibujar la pantalla 
    private Timer gameLoop;

    //valores del HUB, que son temporales hasta que conecte con score/timer
    private int golesJ1 = 0;
    private int golesJ2 = 0; 
    private int tiempoSegundos = 180; 
    private double poderJ1 = 0.0;
    private double poderJ2 = 0.0;

    public FieldPanel() {
        // cargamos la imagen; puede fallar si no existe, por eso el try/catch
        try {
            File f = new File("resources/cancha.png");
            //System.out.println("Buscando imagen en: " + f.getAbsolutePath() + " existe? " + f.exists());
            cancha = ImageIO.read(f);
        } catch (IOException e) {
            System.out.println("No se pudo cargar la cancha: " + e.getMessage());
        }
        // el tamaño preferido del panel = tamaño real de la imagen
        setPreferredSize(new Dimension(ANCHO, ALTO));

        //creamos los objetos del dominio 
        jugador1 = new Player();
        jugador2 = new Player();
        balon = new Ball();

        //posiciones iniciales
        jugador1.setInitialPosition(ANCHO * 0.18, SUELO_JUGADOR);
        jugador2.setInitialPosition(ANCHO * 0.75, SUELO_JUGADOR);
        balon.setPositionX(ANCHO/2.0 - DIAMETRO_BALON/2.0);
        balon.setPositionY(SUELO_BALON);

        //teclas 
        teclasPresionadas = new HashSet<>();

        //kelistener
        addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                teclasPresionadas.add(e.getKeyCode());
            }
            @Override 
            public void keyReleased(KeyEvent e){
                teclasPresionadas.remove(e.getKeyCode());
            }
        });
        setFocusable(true);

        //iniciar el gameloop
        gameLoop = new Timer(16, new java.awt.event.ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e){
                procesarTeclas();
                actualizarFisica();
                repaint();
            }
        });
        gameLoop.start();
    }

    private void procesarTeclas(){
        // Jugador 1: A W S D Q 
        if (teclasPresionadas.contains(KeyEvent.VK_A)) {
            jugador1.moveLeft();
        }
        if (teclasPresionadas.contains(KeyEvent.VK_D)) {
            jugador1.moveRight();
        }
        if (teclasPresionadas.contains(KeyEvent.VK_W)) {
            jugador1.jump();  // el dominio ya controla que solo salte si esta en el suelo
        }
        // patear es one-shot: se quita del Set para que no patee 60 veces por segundo
        if (teclasPresionadas.contains(KeyEvent.VK_S)) {
            intentarPatear(jugador1, 1);  // 1 = direccion derecha
            teclasPresionadas.remove(KeyEvent.VK_S);
        }

        // Jugador 2: flechas + Shift 
        if (teclasPresionadas.contains(KeyEvent.VK_LEFT)) {
            jugador2.moveLeft();
        }
        if (teclasPresionadas.contains(KeyEvent.VK_RIGHT)) {
            jugador2.moveRight();
        }
        if (teclasPresionadas.contains(KeyEvent.VK_UP)) {
            jugador2.jump();
        }
        if (teclasPresionadas.contains(KeyEvent.VK_DOWN)) {
            intentarPatear(jugador2, -1);  // -1 = direccion izquierda
            teclasPresionadas.remove(KeyEvent.VK_DOWN);
        }
    }

    private void intentarPatear(Player jugador, int direccion){
        // calcular el centro de cada uno
        double centroJugadorX = jugador.getPositionX() + DIAMETRO_JUGADOR / 2.0;
        double centroJugadorY = jugador.getPositionY() + DIAMETRO_JUGADOR / 2.0;
        double centroBalonX   = balon.getPositionX() + DIAMETRO_BALON / 2.0;
        double centroBalonY   = balon.getPositionY() + DIAMETRO_BALON / 2.0;

        // distancia entre centros
        double distX = Math.abs(centroJugadorX - centroBalonX);
        double distY = Math.abs(centroJugadorY - centroBalonY);

        // si esta lo suficientemente cerca, patear
        if (distX < DIAMETRO_JUGADOR && distY < DIAMETRO_JUGADOR) {
            
            double angulo = (direccion == 1) ? -0.3 : Math.PI + 0.3;
            balon.applyForce(12, angulo);
        }
    }

    private void actualizarFisica() {
        // Gravedad de los jugadores (para el salto) 
        jugador1.update(SUELO_JUGADOR);
        jugador2.update(SUELO_JUGADOR);

        // Mover el balon  
        balon.move();

        // Rebote del balon contra el suelo 
        if (balon.getPositionY() > SUELO_BALON) {
            balon.setPositionY(SUELO_BALON);
            balon.bounceY();  // invierte velocityY
            // amortiguar: el balon pierde energia en cada rebote
            balon.setVelocityY(balon.getVelocityY() * 0.7);
        }

        // Rebote contra el techo 
        if (balon.getPositionY() < 0) {
            balon.setPositionY(0);
            balon.bounceY();
        }

        // Rebote contra las paredes laterales 
        if (balon.getPositionX() < 0) {
            balon.setPositionX(0);
            balon.bounceX();  // invierte velocityX
        }
        if (balon.getPositionX() > ANCHO - DIAMETRO_BALON) {
            balon.setPositionX(ANCHO - DIAMETRO_BALON);
            balon.bounceX();
        }

        // Friccion: el balon se frena poco a poco en X 
        if (Math.abs(balon.getVelocityX()) > 0.1) {
            balon.setVelocityX(balon.getVelocityX() * 0.99);
        } else {
            balon.setVelocityX(0);  // si es muy lento, pararlo
        }

        // Limites de los jugadores (no salirse de la cancha) 
        if (jugador1.getPositionX() < 0) jugador1.setPositionX(0);
        if (jugador1.getPositionX() > ANCHO - DIAMETRO_JUGADOR) jugador1.setPositionX(ANCHO - DIAMETRO_JUGADOR);
        if (jugador2.getPositionX() < 0) jugador2.setPositionX(0);
        if (jugador2.getPositionX() > ANCHO - DIAMETRO_JUGADOR) jugador2.setPositionX(ANCHO - DIAMETRO_JUGADOR);
    }

    /**
     * Swing llama para dibujar el panel, aqui pintamos la imagen
     */
    @Override
    protected  void paintComponent(Graphics g){
        super.paintComponent(g); //limpiamos antes de dibujar el fondo 
        dibujarCancha(g);
        dibujarJugadores(g);
        dibujarBalon(g);
        dibujarHUD(g);
    }
    private void dibujarCancha(Graphics g) {
        if (cancha != null) {
            g.drawImage(cancha, 0, 0, getWidth(), getHeight(), this);
        }
    }

     private void dibujarJugadores(Graphics g) {
        int diametro = DIAMETRO_JUGADOR;

        // J1: posicion viene del dominio
        int x1 = (int) jugador1.getPositionX();
        int y1 = (int) jugador1.getPositionY();
        g.setColor(new Color(0xE8A033));
        g.fillOval(x1, y1, diametro, diametro);
        g.setColor(Color.BLACK);
        g.drawOval(x1, y1, diametro, diametro);

        // J2: posicion viene del dominio
        int x2 = (int) jugador2.getPositionX();
        int y2 = (int) jugador2.getPositionY();
        g.setColor(new Color(0xDD1144));
        g.fillOval(x2, y2, diametro, diametro);
        g.setColor(Color.BLACK);
        g.drawOval(x2, y2, diametro, diametro);
    }

    /** Dibuja el balon leyendo su posicion del dominio */
    private void dibujarBalon(Graphics g) {
        int diametro = DIAMETRO_BALON;
        int x = (int) balon.getPositionX();
        int y = (int) balon.getPositionY();
        g.setColor(Color.WHITE);
        g.fillOval(x, y, diametro, diametro);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, diametro, diametro);
    }

    /** Dibuja el HUD: marcador, cronometro y barras de poder */
    private void dibujarHUD(Graphics g) {
        // fondo semitransparente
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), 48);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();

        // marcador centrado
        String marcador = golesJ1 + "-" + golesJ2;
        int anchoMarcador = fm.stringWidth(marcador);
        g.drawString(marcador, (getWidth() - anchoMarcador) / 2, 28);

        // etiquetas J1 y J2
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics fmSmall = g.getFontMetrics();
        g.drawString("J1", getWidth()/2 - anchoMarcador/2 - fmSmall.stringWidth("J1") - 8, 28);
        g.drawString("J2", getWidth()/2 + anchoMarcador/2 + 8, 28);

        // cronometro
        int minutos = tiempoSegundos / 60;
        int segundos = tiempoSegundos % 60;
        String tiempo = String.format("%d:%02d", minutos, segundos);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fmTime = g.getFontMetrics();
        g.drawString(tiempo, getWidth() - fmTime.stringWidth(tiempo) - 15, 28);

        // barras de poder
        int barW = 100, barH = 10, barY = 34;
        // J1
        int barX1 = 15;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX1, barY, barW, barH);
        g.setColor(new Color(0xE8A033));
        g.fillRect(barX1, barY, (int)(barW * poderJ1), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX1, barY, barW, barH);
        // J2
        int barX2 = getWidth() - barW - 15;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX2, barY, barW, barH);
        g.setColor(new Color(0xE8A033));
        g.fillRect(barX2, barY, (int)(barW * poderJ2), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX2, barY, barW, barH);
    }

    /**
     * Main para probar solo la cancha
     */
    public static void main(String[] args) {
        JFrame ventana = new JFrame("Cancha DOPOSoccer");
        ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.add(new FieldPanel());
        ventana.pack(); //ajusta la venta al tamaño preferido del panel 
        ventana.setLocationRelativeTo(null); //Centra en la pantalla 
        ventana.setVisible(true);
    }
}
