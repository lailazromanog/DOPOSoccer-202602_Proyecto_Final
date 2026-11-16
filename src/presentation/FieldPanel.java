package presentation;
import domain.Ball;
import domain.Goal;
import domain.Match;
import domain.Player;
import domain.Score;
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
    public static final int BALL_DIAMETER = 34; 
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
    public static final int LEFT_POST = 72; 
    public static final int RIGHT_POST =  697; 
    private static final int GOAL_TOP = 295; 
    private static final int GOAL_BOTTON = 417; 

    private boolean paused = false;

    public FieldPanel(Match match) {
        // cargamos la imagen; puede fallar si no existe, por eso el try/catch
        try {
            fieldImage = ImageIO.read(new File("resources/cancha.png"));
        } catch (IOException e) {
            System.out.println("No se pudo cargar la cancha: " + e.getMessage());
        }
        // el tamaño preferido del panel = tamaño real de la imagen
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        this.match = match;

        leftPlayer = match.getPlayerOne();
        rightPlayer = match.getPlayerTwo();
        ball = match.getBall();
        leftGoal = match.getLeftGoal();
        rightGoal = match.getRightGoal();
        match.start();

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
                int code = e.getKeyCode();
                if(code == KeyEvent.VK_P){
                    paused = !paused; //P:pausar o reanudar
                }else if(code == KeyEvent.VK_ESCAPE){
                    finishMatch(); //ESC:terminar el partido
                }else if(code == KeyEvent.VK_Q){
                    leftPlayer.usePower(); //q activa el power
                }else if(code == KeyEvent.VK_M){
                    rightPlayer.usePower(); //activa el pawer
                }else{
                    pressedKeys.add(code);
                }
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
                if(!paused){
                     processKeys();
                    updatePhysics();
                    updateTime();
                }
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
        if(matchFinished){
            return; //no termina y no reparte puntos,
        }

        matchFinished = true;
        match.awardPoints();
        match.finish();
        gameLoop.stop();
        repaint();
    }

    private void processKeys(){
        // Jugador 1 A/W/S/D si esta aturdido no responde
    if (!leftPlayer.isStunned()) {
        if (pressedKeys.contains(KeyEvent.VK_A)) {
            leftPlayer.moveLeft();
        }
        if (pressedKeys.contains(KeyEvent.VK_D)) {
            leftPlayer.moveRight();
        }
        if (pressedKeys.contains(KeyEvent.VK_W)) {
            leftPlayer.jump();
        }
        if (pressedKeys.contains(KeyEvent.VK_S)) {
            tryKick(leftPlayer, rightPlayer, 1);  // patea a la derecha; rightPlayer es el rival
            pressedKeys.remove(KeyEvent.VK_S);
        }
    }

    // Jugador 2 flechas si esta aturdido no responde
    if (!rightPlayer.isStunned()) {
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
            tryKick(rightPlayer, leftPlayer, -1); // patea a la izquierda; leftPlayer es el rival
            pressedKeys.remove(KeyEvent.VK_DOWN);
        }
    }
    }
    
    private void tryKick(Player player, Player opponent, int direccion){
        double playerCenterX = player.getPositionX() + PLAYER_DIAMETER / 2.0;
        double playerCenterY = player.getPositionY() + PLAYER_DIAMETER / 2.0;

        //si el balon esta cerca, lo impulsa
        double ballCenterX = ball.getPositionX() + BALL_DIAMETER / 2.0;
        double ballCenterY = ball.getPositionY() + BALL_DIAMETER / 2.0;
        if (Math.abs(playerCenterX - ballCenterX) < PLAYER_DIAMETER && Math.abs(playerCenterY - ballCenterY) < PLAYER_DIAMETER) {
            double angle;
            if(direccion == 1){
                angle = -KICK_ANGLE;
            }else{
                angle = Math.PI + KICK_ANGLE;
            }
            ball.applyForce(KICK_FORCE, angle);
        }

        //si el rival esta dentro del alcance del pie, le hace daño
        double oppCenterX = opponent.getPositionX() + PLAYER_DIAMETER / 2.0;
        double oppCenterY = opponent.getPositionY() + PLAYER_DIAMETER / 2.0;
        if (Math.abs(playerCenterX - oppCenterX) < PLAYER_DIAMETER && Math.abs(playerCenterY - oppCenterY) < PLAYER_DIAMETER) {
            match.checkKickContact(player, opponent); 
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

        //bajar el aturdimiento y reiniciar vida cuando termine
        leftPlayer.updateStun(16/1000.0);
        rightPlayer.updateStun(16/1000.0);

        //carga la barra de poder
        leftPlayer.getPowerBar().update(16/1000.0);
        rightPlayer.getPowerBar().update(16/1000.0);
        leftPlayer.updateEffect(16/1000.0);
        rightPlayer.updateEffect(16/1000.0);

        //colision entre jugadores, pa que no se atraviesen 
        playerCollision();

        // Mover el ball  
        ball.move();
        //friccion del aire
        ball.setVelocityX(ball.getVelocityX() * AIR_FRICTION);
        ball.setVelocityY(ball.getVelocityY() * AIR_FRICTION);

        //hay gol?
        checkGoal();

        //si un jugador con aura toca el arco, se ejecuta su poder
        if(ballTouches(leftPlayer)){
            match.checkBallContact(leftPlayer);
        }
        if(ballTouches(rightPlayer)){
            match.checkBallContact(rightPlayer);
        }
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
     * cuando el jugador este tocando el balon, se dispara el poder
     */
    private boolean ballTouches(Player player){
        double pCenterX = player.getPositionX() + PLAYER_DIAMETER / 2.0;
        double pCenterY = player.getPositionY() + PLAYER_DIAMETER / 2.0;
        double bCenterX = ball.getPositionX() + BALL_DIAMETER / 2.0;
        double bCenterY = ball.getPositionY() + BALL_DIAMETER / 2.0;
        double alcance = (PLAYER_DIAMETER + BALL_DIAMETER) / 2.0;
        return Math.abs(pCenterX - bCenterX) < alcance && Math.abs(pCenterY - bCenterY) < alcance;
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
        g.drawString(scoreText, (getWidth() - fm2.stringWidth(scoreText))/2, getHeight()/2 + 30);

        g.setFont(new Font("Arial", Font.PLAIN, 20));
        String puntos = "Puntos   J1: " + leftPlayer.getPoints() + "    J2: " + rightPlayer.getPoints();
        FontMetrics fm3 = g.getFontMetrics();
        g.drawString(puntos, (getWidth() - fm3.stringWidth(puntos))/2, getHeight()/2 + 72);
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
        }else if(paused){
            drawPaused(g);
        }
    }

    /**
     * dibuja cuando se pausa el juego
     */
    private void drawPaused(Graphics g){
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 44));
        FontMetrics fm = g.getFontMetrics();
        String text = "PAUSA";
        g.drawString(text, (getWidth() - fm.stringWidth(text))/2, getHeight()/2 - 10);

        g.setFont(new Font("Arial", Font.PLAIN, 18));
        FontMetrics fm2 = g.getFontMetrics();
        String ayuda = "P: reanudar    ESC: terminar";
        g.drawString(ayuda, (getWidth() - fm2.stringWidth(ayuda))/2, getHeight()/2 + 30);
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
        if(leftPlayer.isAuraActive()){
            g.setColor(new Color(80, 180, 255, 150)); // aura
            g.fillOval(x1 - 8, y1 - 8, diametro + 16, diametro + 16);
        }
        g.setColor(new Color(0xE8A033));
        g.fillOval(x1, y1, diametro, diametro);
        g.setColor(Color.BLACK);
        g.drawOval(x1, y1, diametro, diametro);

        // J2: posicion viene del dominio
        int x2 = (int) rightPlayer.getPositionX();
        int y2 = (int) rightPlayer.getPositionY();
        if(rightPlayer.isAuraActive()){
            g.setColor(new Color(80, 180, 255, 150)); 
            g.fillOval(x1 - 8, y1 - 8, diametro + 16, diametro + 16);
        }
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
        g.fillRect(0, 0, getWidth(), 58);

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

        // barras de vida (verde) debajo de las de poder
        double leftLife  = leftPlayer.getHealth()  / leftPlayer.getMaxHealth();
        double rightLife = rightPlayer.getHealth() / rightPlayer.getMaxHealth();
        int lifeY = 47, lifeH = 6;
        // J1
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX1, lifeY, barW, lifeH);
        g.setColor(new Color(0x3ddc84));
        g.fillRect(barX1, lifeY, (int)(barW * leftLife), lifeH);
        // J2
        g.setColor(Color.DARK_GRAY);
        g.fillRect(barX2, lifeY, barW, lifeH);
        g.setColor(new Color(0x3ddc84));
        g.fillRect(barX2, lifeY, (int)(barW * rightLife), lifeH);
    }
}
