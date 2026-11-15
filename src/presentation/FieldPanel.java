package presentation;
import domain.Ball;
import domain.Player;
import domain.Goal;

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
    private static final int DIAMETRO_JUGADOR = 60;
    private static final int DIAMETRO_BALON = 34; 
    
    //las teclas que presionan los jugadores
    private Set<Integer> teclasPresionadas;

    //suelo de ambos objetos 
    private static final int SUELO_JUGADOR = 348;
    private static final int SUELO_BALON = 374;

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

    //constantes para la fisica del balon
    private static final double FRICCION_AIRE =0.97; //pierde rebote en el aire 
    private static final double REBOTE_PISO = 0.7; //energia que se conserva al rebortar en el piso
    private static final double REBOTE_PARED = 0.8; //'' al rebotar en una pared
    private static final double VELOCIDAD_MINIMA = 0.5; //si el balone sta por debajo, se para
    private static final double FUERZA_PATADA = 12; 
    private static final double ANGULO_PATADA = 0.3; 
    private static final double REBOTE_MINIMO = 2.0; 

    //arco 
    private Goal arcoIzquierdo;
    private Goal arcoDerecho;
    private static final int POSTE_IZQ = 72; 
    private static final int POSTE_DER =  697; 
    private static final int TOPE_ARCO = 295; 
    private static final int BASE_ARCO = 417; 

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
        arcoIzquierdo = new Goal(-100, 290, (POSTE_IZQ - DIAMETRO_BALON / 2) + 100, 135);
        arcoDerecho   = new Goal(POSTE_DER - DIAMETRO_BALON / 2, 290, 200, 135);
        
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
            
            double angulo;
            if(direccion == 1){
                angulo = -ANGULO_PATADA; //patea hacia la derecha
            }else{
                angulo = Math.PI + ANGULO_PATADA; //patea hacia la izq
            }
            balon.applyForce(FUERZA_PATADA, angulo);
        }
    }

    /**
     * Evita que los jugadores se sobrepongan, si chocan de lado se separan
     */
    private void colisionJugadores(){
        double centro1X = jugador1.getPositionX() + DIAMETRO_JUGADOR/ 2.0;
        double centro1Y = jugador1.getPositionY() + DIAMETRO_JUGADOR/ 2.0;
        double centro2X = jugador2.getPositionX() + DIAMETRO_JUGADOR/ 2.0;
        double centro2Y = jugador2.getPositionY() + DIAMETRO_JUGADOR/ 2.0;

        //cuanto se superponen en cada eje, si es positivo entonces lo estan
        double solapeX = DIAMETRO_JUGADOR - Math.abs(centro1X -centro2X);
        double solapeY = DIAMETRO_JUGADOR - Math.abs(centro1Y -centro2Y);

        //chocan solo si se superponen en ambos ejes
        if(solapeX > 0 &&  solapeY >0){
            if(centro1X < centro2X){
                jugador1.setPositionX(jugador1.getPositionX() - solapeX/2);
                jugador2.setPositionX(jugador2.getPositionX() + solapeX/2);
            }else{
                jugador1.setPositionX(jugador1.getPositionX() + solapeX/2);
                jugador2.setPositionX(jugador2.getPositionX() - solapeX/2);
            }
        }//si es el y, uno esta encima del otro, no importa 
    }

    /**
     * Revisa si el balon entro a un arco y suma el gol al jugador que anoto
     */
    private void revisarGol(){
        //gol en el arco izq, gol para el J2
        if(arcoIzquierdo.isGoal(balon)){
            golesJ2++;
            reiniciarPosiciones();
        }else if(arcoDerecho.isGoal(balon)){
            golesJ1++;
            reiniciarPosiciones();
        }
    }

    /**
     * Vuelve el balon al centro y los jugadores a su posicion inicial
     */
    private void reiniciarPosiciones(){
        balon.setPositionX(ANCHO/ 2.0 - DIAMETRO_BALON/2.0);
        balon.setPositionY(SUELO_BALON);
        balon.setVelocityX(0);
        balon.setVelocityY(0);
        jugador1.resetPosition();
        jugador2.resetPosition();
    }

    private void actualizarFisica() {
        // Gravedad de los jugadores (para el salto) 
        jugador1.update(SUELO_JUGADOR);
        jugador2.update(SUELO_JUGADOR);

        //colision entre jugadores, pa que no se atraviesen 
        colisionJugadores();

        // Mover el balon  
        balon.move();
        //friccion del aire
        balon.setVelocityX(balon.getVelocityX() * FRICCION_AIRE);
        balon.setVelocityY(balon.getVelocityY() * FRICCION_AIRE);

        //hay gol?
        revisarGol();

        // Rebote del balon contra el suelo 
        if (balon.getPositionY() > SUELO_BALON) {
            balon.setPositionY(SUELO_BALON);
            balon.bounceY();  
            // amortiguar, el balon pierde energia en cada rebote
            balon.setVelocityY(balon.getVelocityY() * REBOTE_PISO);
            if(Math.abs(balon.getVelocityY()) < REBOTE_MINIMO){ //si el rebote es minimo, se detiene
                balon.setVelocityY(0);
            }
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
            balon.setVelocityX(balon.getVelocityX() * REBOTE_PARED);
        }
        if (balon.getPositionX() > ANCHO - DIAMETRO_BALON){
            balon.setPositionX(ANCHO - DIAMETRO_BALON);
            balon.bounceX();
            balon.setVelocityX(balon.getVelocityX() * REBOTE_PARED);
        }

        //Friccion, el balon se frena poco a poco en X 
        if (Math.abs(balon.getVelocityX()) < VELOCIDAD_MINIMA) {
            balon.setVelocityX(0);
        }

        //Limites de los jugadores, no  se saleb de la cancha
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
        dibujarArcos(g);
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

    /**
     * Dibuja los postes del arco por encima de los jugadores y el balon
     */
    private void dibujarArcos(Graphics g){
        g.setColor(Color.WHITE);
        int grosor = 6;

        //postes delanteros
        g.fillRect(POSTE_IZQ - grosor/2, TOPE_ARCO, grosor, BASE_ARCO - TOPE_ARCO);
        g.fillRect(POSTE_DER -grosor/2, TOPE_ARCO, grosor, BASE_ARCO - TOPE_ARCO);
        //la parte de arriba del poste 
        g.fillRect(0, TOPE_ARCO -grosor/2, POSTE_IZQ, grosor);
        g.fillRect(POSTE_DER, TOPE_ARCO - grosor/2, getWidth()- POSTE_DER, grosor);
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
        ventana.setLocationRelativeTo(null); //fijamos la venta en 769x450
        ventana.setLocationRelativeTo(null); //Centra en la pantalla 
        ventana.setVisible(true);
    }
}
