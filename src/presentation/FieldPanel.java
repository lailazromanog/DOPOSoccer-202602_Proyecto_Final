package presentation;
import domain.Ball;
import domain.Goal;
import domain.Match;
import domain.Player;
import domain.Score;
import domain.Time;
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
import javax.swing.JPanel;
import javax.swing.Timer;


/**
 * Pantalla de la cancha
 * Carga la imagen del estadio desde la carpeta de recursos y la dibuja como fondo.
 * Primera pieza de la interfaz construida
 * 
 * Responsabilidades: 
 * 1. Dibujar la cancha, jugadores, ball y HUD
 * 2. ejecutar el teclado para los controles de ambos jugadores
 * 3. ejecutar 
 *   
 */
//Nuevo: cargar una imagen con ImageIO y printarla con Graphics.drawImage 
public class FieldPanel extends JPanel {
    
    // la imagen de la cancha ya cargada
    private BufferedImage fieldImage;
    
    //objetos del dominio 
    private Player leftPlayer;
    private Player rightPlayer;
    private Ball ball;
    private Goal leftGoal;
    private Goal rightGoal;
    private Match match;
 

    private static final int WIDTH = 769;
    private static final int HEIGHT = 450;
    private static final int PLAYER_DIAMETER = 60;
    private static final int BALL_DIAMETER = 34; 
    //suelo de ambos objetos 
    private static final int PLAYER_GROUND = 348;
    private static final int BALL_GROUND = 374;
    
    //las teclas que presionan los jugadores
    private Set<Integer> pressedKeys;
    private Timer gameLoop;
    private boolean matchFinished = false;

    //constantes para la fisica del ball
    private static final double AIR_FRICTION =0.97; //pierde rebote en el aire 
    private static final double FLOOR_BOUNCE = 0.7; //energia que se conserva al rebortar en el piso
    private static final double WALL_BOUNCE = 0.8; //'' al rebotar en una pared
    private static final double MIN_SPEED = 0.5; //si el balle sta por debajo, se para
    private static final double KICK_FORCE = 12; 
    private static final double KICK_ANGLE = 0.3; 
    private static final double MIN_BOUNCE = 2.0; 

    //arco 
    private static final int LEFT_POST = 72; 
    private static final int RIGHT_POST =  697; 
    private static final int GOAL_TOP = 295; 
    private static final int GOAL_BOTTON = 417; 

    public FieldPanel() {
        // cargamos la imagen; puede fallar si no existe, por eso el try/catch
        try {
            fieldImage = ImageIO.read(new File("resources/cancha.png"));
        } catch (IOException e) {
            System.out.println("No se pudo cargar la cancha: " + e.getMessage());
        }
        // el tamaño preferido del panel = tamaño real de la imagen
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        //creamos los objetos del dominio 
        Player p1 = new Player();
        Player p2 = new Player();
        Ball b = new Ball();
        leftGoal = new Goal(-100, 290, (LEFT_POST - BALL_DIAMETER / 2) + 100, 135);
        rightGoal = new Goal(RIGHT_POST - BALL_DIAMETER / 2, 290, 200, 135);
        domain.Timer timer = new domain.Timer(180);
        Score score = new Score(p1, p2);

        //armamos el match 
        match = new Time(p1, p2, b, leftGoal, rightGoal, timer, score);
        match.start();

        leftPlayer = match.getPlayerOne();
        rightPlayer = match.getPlayerTwo();
        ball = match.getBall();

        //posiciones iniciales
        leftPlayer.setInitialPosition(WIDTH * 0.18, PLAYER_GROUND);
        rightPlayer.setInitialPosition(WIDTH * 0.75, PLAYER_GROUND);
        centerBall();

        //teclas 
        pressedKeys = new HashSet<>();

        //kelistener
        addKeyListener(new KeyAdapter(){
            @Override
            public void keyPressed(KeyEvent e){
                pressedKeys.add(e.getKeyCode());
            }
            @Override 
            public void keyReleased(KeyEvent e){
                pressedKeys.remove(e.getKeyCode());
            }
        });
        setFocusable(true);

        //iniciar el gameloop
        gameLoop = new Timer(16, new java.awt.event.ActionListener(){
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e){
                processKeys();
                updatePhysics();
                updateTime();
                repaint();
            }
        });
        gameLoop.start();
    }
    /**
     * Descuenta el tiempo
     */
    private void updateTime(){
        match.getTimer().update(16/1000.0);
        if(match.checkWinCondition()){
            finishMatch();
        }
    }

    private void finishMatch(){
        matchFinished = true;
        match.finish();
        gameLoop.stop();
    }
    private void processKeys(){
        // Jugador 1: A W S D Q 
        if (pressedKeys.contains(KeyEvent.VK_A)) {
            leftPlayer.moveLeft();
        }
        if (pressedKeys.contains(KeyEvent.VK_D)) {
            leftPlayer.moveRight();
        }
        if (pressedKeys.contains(KeyEvent.VK_W)) {
            leftPlayer.jump();  // el dominio ya controla que solo salte si esta en el suelo
        }
        // patear es one-shot: se quita del Set para que no patee 60 veces por segundo
        if (pressedKeys.contains(KeyEvent.VK_S)) {
            tryKick(leftPlayer, 1);  // 1 = direccion derecha
            pressedKeys.remove(KeyEvent.VK_S);
        }

        // Jugador 2: flechas + Shift 
        if (pressedKeys.contains(KeyEvent.VK_LEFT)) {
            rightPlayer.moveLeft();
        }
        if (pressedKeys.contains(KeyEvent.VK_RIGHT)) {
            rightPlayer.moveRight();
        }
        if (pressedKeys.contains(KeyEvent.VK_UP)) {
            rightPlayer.jump();
        }
        if (pressedKeys.contains(KeyEvent.VK_DOWN)) {
            tryKick(rightPlayer, -1);  // -1 = direccion izquierda
            pressedKeys.remove(KeyEvent.VK_DOWN);
        }
    }
    
    private void tryKick(Player player, int direccion){
        // calcular el centro de cada uno
        double playerCenterX = player.getPositionX() + PLAYER_DIAMETER / 2.0;
        double playerCenterY = player.getPositionY() + PLAYER_DIAMETER / 2.0;
        double ballCenterX   = ball.getPositionX() + BALL_DIAMETER / 2.0;
        double ballCenterY   = ball.getPositionY() + BALL_DIAMETER / 2.0;

        // distancia entre centros
        double distX = Math.abs(playerCenterX - ballCenterX);
        double distY = Math.abs(playerCenterY - ballCenterY);

        // si esta lo suficientemente cerca, patear
        if (distX < PLAYER_DIAMETER && distY < PLAYER_DIAMETER) {
            
            double angle;
            if(direccion == 1){
                angle = -KICK_ANGLE; //patea hacia la derecha
            }else{
                angle = Math.PI + KICK_ANGLE; //patea hacia la izq
            }
            ball.applyForce(KICK_FORCE, angle);
        }
    }

    /**
     * Evita que los jugadores se sobrepongan, si chocan de lado se separan
     */
    private void playerCollision(){
        double center1X = leftPlayer.getPositionX() + PLAYER_DIAMETER/ 2.0;
        double center1Y = leftPlayer.getPositionY() + PLAYER_DIAMETER/ 2.0;
        double center2X = rightPlayer.getPositionX() + PLAYER_DIAMETER/ 2.0;
        double center2Y = rightPlayer.getPositionY() + PLAYER_DIAMETER/ 2.0;

        //cuanto se superponen en cada eje, si es positivo entonces lo estan
        double overlapX = PLAYER_DIAMETER - Math.abs(center1X -center2X);
        double overlapY = PLAYER_DIAMETER - Math.abs(center1Y -center2Y);

        //chocan solo si se superponen en ambos ejes
        if(overlapX > 0 &&  overlapY >0){
            if(center1X < center2X){
                leftPlayer.setPositionX(leftPlayer.getPositionX() - overlapX/2);
                rightPlayer.setPositionX(rightPlayer.getPositionX() + overlapX/2);
            }else{
                leftPlayer.setPositionX(leftPlayer.getPositionX() + overlapX/2);
                rightPlayer.setPositionX(rightPlayer.getPositionX() - overlapX/2);
            }
        }//si es el y, uno esta encima del otro, no importa 
    }

    /**
     * Revisa si el ball entro a un arco y suma el gol al jugador que anoto
     */
    private void checkGoal(){
        //gol en el arco izq, gol para el J2
        if(leftGoal.isGoal(ball)){
            match.onGoal(rightPlayer);
            centerBall();
        }else if(rightGoal.isGoal(ball)){
            match.onGoal(leftPlayer);
            centerBall();
        }
    }

    /**
     * Vuelve el ball al centro y los jugadores a su posicion inicial
     */
    private void centerBall(){
        ball.setPositionX(WIDTH/ 2.0 - BALL_DIAMETER/2.0);
        ball.setPositionY(BALL_GROUND);
        ball.setVelocityX(0);
        ball.setVelocityY(0);
    }

    private void updatePhysics() {
        // Gravedad de los jugadores (para el sHEIGHT) 
        leftPlayer.update(PLAYER_GROUND);
        rightPlayer.update(PLAYER_GROUND);

        //colision entre jugadores, pa que no se atraviesen 
        playerCollision();

        // Mover el ball  
        ball.move();
        //friccion del aire
        ball.setVelocityX(ball.getVelocityX() * AIR_FRICTION);
        ball.setVelocityY(ball.getVelocityY() * AIR_FRICTION);

        //hay gol?
        checkGoal();

        // Rebote del ball contra el suelo 
        if (ball.getPositionY() > BALL_GROUND) {
            ball.setPositionY(BALL_GROUND);
            ball.bounceY();  
            // amortiguar, el ball pierde energia en cada rebote
            ball.setVelocityY(ball.getVelocityY() * FLOOR_BOUNCE);
            if(Math.abs(ball.getVelocityY()) < MIN_BOUNCE){ //si el rebote es minimo, se detiene
                ball.setVelocityY(0);
            }
        }

        // Rebote contra el techo 
        if (ball.getPositionY() < 0) {
            ball.setPositionY(0);
            ball.bounceY();
        }

        // Rebote contra las paredes laterales 
        if (ball.getPositionX() < 0) {
            ball.setPositionX(0);
            ball.bounceX();  // invierte velocityX
            ball.setVelocityX(ball.getVelocityX() * WALL_BOUNCE);
        }
        if (ball.getPositionX() > WIDTH - BALL_DIAMETER){
            ball.setPositionX(WIDTH - BALL_DIAMETER);
            ball.bounceX();
            ball.setVelocityX(ball.getVelocityX() * WALL_BOUNCE);
        }

        //Friccion, el ball se frena poco a poco en X 
        if (Math.abs(ball.getVelocityX()) < MIN_SPEED) {
            ball.setVelocityX(0);
        }

        //Limites de los jugadores, no  se saleb de la cancha
        if (leftPlayer.getPositionX() < 0) leftPlayer.setPositionX(0);
        if (leftPlayer.getPositionX() > WIDTH - PLAYER_DIAMETER) leftPlayer.setPositionX(WIDTH - PLAYER_DIAMETER);
        if (rightPlayer.getPositionX() < 0) rightPlayer.setPositionX(0);
        if (rightPlayer.getPositionX() > WIDTH - PLAYER_DIAMETER) rightPlayer.setPositionX(WIDTH - PLAYER_DIAMETER);
    }

    /**
     * dibuja como finaliza el match en diferentes casos
     */
    private void drawMatchEnd(Graphics g){
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, getWidth(), getHeight());

        Player winner = match.getScore().getWinner();
        String result; 
        if(winner == leftPlayer){
            result = "Gana Jugador 1";
        }else if( winner == rightPlayer){
            result = "Gana Jugador 2";
        }else{
            result = "EMPATE";
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 40));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(result, (getWidth() - fm.stringWidth(result))/2, getHeight()/2 - 20);

        g.setFont(new Font("Arial", Font.BOLD, 28));
        String scoreText = match.getScore().getGoalsPlayerOne() + " - " + match.getScore().getGoalsPlayerTwo();
        FontMetrics fm2 = g.getFontMetrics();
        g.drawString(scoreText, (getWidth() - fm2.stringWidth(m))/2, getHeight()/2 + 30);

    }
    /**
     * Swing llama para dibujar el panel, aqui pintamos la imagen
     */
    @Override
    protected  void paintComponent(Graphics g){
        super.paintComponent(g); //limpiamos antes de dibujar el fondo 
        drawField(g);
        drawPlayers(g);
        drawBall(g);
        drawGoals(g);
        drawHUD(g);

        if(matchFinished){
            drawMatchEnd(g);
        }
    }
    private void drawField(Graphics g) {
        if (fieldImage != null) {
            g.drawImage(fieldImage, 0, 0, getWidth(), getHeight(), this);
        }
    }

     private void drawPlayers(Graphics g) {
        int diametro = PLAYER_DIAMETER;

        // J1: posicion viene del dominio
        int x1 = (int) leftPlayer.getPositionX();
        int y1 = (int) leftPlayer.getPositionY();
        g.setColor(new Color(0xE8A033));
        g.fillOval(x1, y1, diametro, diametro);
        g.setColor(Color.BLACK);
        g.drawOval(x1, y1, diametro, diametro);

        // J2: posicion viene del dominio
        int x2 = (int) rightPlayer.getPositionX();
        int y2 = (int) rightPlayer.getPositionY();
        g.setColor(new Color(0xDD1144));
        g.fillOval(x2, y2, diametro, diametro);
        g.setColor(Color.BLACK);
        g.drawOval(x2, y2, diametro, diametro);
    }

    /** Dibuja el ball leyendo su posicion del dominio */
    private void drawBall(Graphics g) {
        int diametro = BALL_DIAMETER;
        int x = (int) ball.getPositionX();
        int y = (int) ball.getPositionY();
        g.setColor(Color.WHITE);
        g.fillOval(x, y, diametro, diametro);
        g.setColor(Color.BLACK);
        g.drawOval(x, y, diametro, diametro);
    }

    /**
     * Dibuja los postes del arco por encima de los jugadores y el ball
     */
    private void drawGoals(Graphics g){
        g.setColor(Color.WHITE);
        int grosor = 6;

        //postes delanteros
        g.fillRect(LEFT_POST - grosor/2, GOAL_TOP, grosor, GOAL_BOTTON - GOAL_TOP);
        g.fillRect(RIGHT_POST -grosor/2, GOAL_TOP, grosor, GOAL_BOTTON - GOAL_TOP);
        //la parte de arriba del poste 
        g.fillRect(0, GOAL_TOP -grosor/2, LEFT_POST, grosor);
        g.fillRect(RIGHT_POST, GOAL_TOP - grosor/2, getWidth()- RIGHT_POST, grosor);
    }

    /** Dibuja el HUD: score, timer y barras de poder */
    private void drawHUD(Graphics g) {
        Score score = match.getScore();

        // fondo semitransparente
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), 48);

        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.setColor(Color.WHITE);
        FontMetrics fm = g.getFontMetrics();

        // score centrado
        String scoreTexto = score.getGoalsPlayerOne() + " - " + score.getGoalsPlayerTwo();
        int WIDTHscore = fm.stringWidth(scoreTexto);
        g.drawString(scoreTexto, (getWidth() - WIDTHscore) / 2, 28);

        // etiquetas J1 y J2
        g.setFont(new Font("Arial", Font.PLAIN, 14));
        FontMetrics fmSmall = g.getFontMetrics();
        g.drawString("J1", getWidth()/2 - WIDTHscore/2 - fmSmall.stringWidth("J1") - 8, 28);
        g.drawString("J2", getWidth()/2 + WIDTHscore/2 + 8, 28);

        // timer
        int t = match.getTimer().getCurrentTime();
        int minutos = t/60;
        int segundos = t % 60;
        String timeText = String.format("%d:%02d", minutos, segundos);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fmTime = g.getFontMetrics();
        g.drawString(timeText, getWidth() - fmTime.stringWidth(timeText) - 15, 28);

        double leftPower = leftPlayer.getPowerBar().getCurrentCharge() / leftPlayer.getPowerBar().getMaxCharge();
        double rightPower = rightPlayer.getPowerBar().getCurrentCharge() / rightPlayer.getPowerBar().getMaxCharge();

        // barras de poder
        int barW = 100, barH = 10, barY = 34;
        // J1
        int barX1 = 15;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX1, barY, barW, barH);
        g.setColor(new Color(0xE8A033));
        g.fillRect(barX1, barY, (int)(barW * leftPower), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX1, barY, barW, barH);
        // J2
        int barX2 = getWidth() - barW - 15;
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX2, barY, barW, barH);
        g.setColor(new Color(0xE8A033));
        g.fillRect(barX2, barY, (int)(barW * rightPower), barH);
        g.setColor(Color.WHITE);
        g.drawRect(barX2, barY, barW, barH);
    }
}
