package domain;
import java.io.File;
import java.io.Serializable;

/**
 * Fachada del dominio (patron Facade).
 *
 * Clase del dominio que la interfaz (presentacion) puede usar.
 * Por dentro coordina a todos los objetos del juego (jugadores, balon, arcos,
 * partido, cronometro, marcador) y las reglas; por fuera ofrece una interfaz
 * sencilla: recibir ordenes (mover, saltar, patear, usar poder), avanzar un
 * frame (update) y entregar datos de solo lectura para dibujar (posiciones,
 * marcador, tiempo, vida, poder, ganador).
 *
 * Asi la presentacion deja de meter mano en Player/Ball/Match/Goal: solo habla
 * con Game.
 *
 * Referencia del patron: Refactoring.Guru - Facade (patrones estructurales).
 */
public class Game implements Serializable{

    // Geometria del mundo, en unidades logicas (la cancha por default 769x450).
    // el mundo del juego siempre vive en estas unidades.
    public static final int WIDTH = 769;
    public static final int HEIGHT = 450;
    public static final int PLAYER_DIAMETER = 60;
    public static final int BALL_DIAMETER = 34;
    public static final int LEFT_POST = 72;
    public static final int RIGHT_POST = 697;
    public static final int GOAL_TOP = 295;
    public static final int GOAL_BOTTOM = 417;

    // Constantes internas (fisica); solo las usa Game.
    private static final int PLAYER_GROUND = 348;
    private static final int BALL_GROUND = 374;
    private static final double KICK_FORCE = 12;
    private static final double KICK_ANGLE = 0.3;

    //  Objetos del dominio que coordina la fachada.
    private transient CharacterFactory characterFactory;
    private transient MatchFactory matchFactory;
    private transient BotFactory botFactory;
    private boolean vsMachine;
    private transient Profile leftProfile;
    private transient Profile rightProfile;
    private Bot bot;
    private Match match;
    private Player leftPlayer;
    private Player rightPlayer;
    private Ball ball;
    private Goal leftGoal;
    private Goal rightGoal;

    private boolean finished;

    /**
     * Arma un partido Jugador vs Jugador.
     * @param byTime   true = por tiempo, false = por goles
     * @param value    minutos (si es por tiempo) o goles para ganar (si es por goles)
     * @param country1 pais/personaje del jugador 1
     * @param country2 pais/personaje del jugador 2
     */
    public Game(String matchType, int value, String country1, String country2, String opponent, Profile leftProfile, Profile rightProfile) {
        characterFactory = new CharacterFactory();
        matchFactory = new MatchFactory();
        botFactory = new BotFactory();

        leftPlayer = new Player();
        leftPlayer.setCharacter(characterFactory.create(country1));

        // el jugador derecho es humano o maquina segun el modo
        if (opponent.equals("human")) {
            vsMachine = false;
            rightPlayer = new Player();
        } else {
            vsMachine = true;
            bot = botFactory.create(opponent);   // opponent = facil/medio/dificil
            rightPlayer = bot;
        }
        rightPlayer.setCharacter(characterFactory.create(country2));

        
        // aplica el progreso: las estadisticas vienen del perfil de cada jugador
        this.leftProfile = leftProfile;
        this.rightProfile = rightProfile;
        leftPlayer.setStats(leftProfile.getSpeed(), leftProfile.getKick(), leftProfile.getJump(), leftProfile.getSurvival());
        rightPlayer.setStats(rightProfile.getSpeed(), rightProfile.getKick(), rightProfile.getJump(), rightProfile.getSurvival());


        ball = new Ball();
        leftGoal  = new Goal(-100, 290, (LEFT_POST - BALL_DIAMETER / 2) + 100, 135);
        rightGoal = new Goal(RIGHT_POST - BALL_DIAMETER / 2, 290, 200, 135);
        Score score = new Score(leftPlayer, rightPlayer);

        match = matchFactory.create(matchType, value, leftPlayer, rightPlayer, ball, leftGoal, rightGoal, score);
        // posiciones iniciales y balon al centro
        leftPlayer.setInitialPosition(WIDTH * 0.18, PLAYER_GROUND);
        rightPlayer.setInitialPosition(WIDTH * 0.75, PLAYER_GROUND);
        centerBall();

        match.start();
        finished = false;
    }

    //ordenes, lo que la interfaz manda

    public void moveLeftPlayerLeft(){ 
        if (!leftPlayer.isStunned()) leftPlayer.moveLeft();  
    }

    public void moveLeftPlayerRight(){ 
        if (!leftPlayer.isStunned()) leftPlayer.moveRight(); 
    }

    public void jumpLeftPlayer(){ 
        if (!leftPlayer.isStunned()) leftPlayer.jump();      
    }

    public void kickLeftPlayer(){ 
        if (!leftPlayer.isStunned()) tryKick(leftPlayer, rightPlayer, 1); 
    }

    public void usePowerLeftPlayer(){ 
        leftPlayer.usePower(); 
    }

    public void moveRightPlayerLeft(){ 
        if (!vsMachine && !rightPlayer.isStunned()) rightPlayer.moveLeft();  
    }

    public void moveRightPlayerRight(){ 
        if (!vsMachine && !rightPlayer.isStunned()) rightPlayer.moveRight(); 
    }

    public void jumpRightPlayer(){ 
        if (!vsMachine && !rightPlayer.isStunned()) rightPlayer.jump();      
    }

    public void kickRightPlayer(){ 
        if (!vsMachine && !rightPlayer.isStunned()) tryKick(rightPlayer, leftPlayer, -1); 
    }

    public void usePowerRightPlayer(){ 
        if (!vsMachine) rightPlayer.usePower(); 
    }

    /** Termina el partido ya (por ejemplo con ESC): reparte puntos y detiene. */
    public void finishMatch() {
        if (finished) {
            return;
        }
        finished = true; 
        match.awardPoints();
        awardToProfiles();
        match.finish();
    }

    /** 
     * Reparte los puntos del partido a los perfiles persistentes (si los hay). 
     * */
    private void awardToProfiles() {
        if (leftProfile == null || rightProfile == null) {
            return;   // partida abierta desde archivo: sin perfiles vivos
        }
        int w = getWinner();
        if (w == 1) {
            leftProfile.addPoints(100);
            rightProfile.addPoints(25);
        } else if (w == 2) {
            rightProfile.addPoints(100);
            leftProfile.addPoints(25);
        } else {
            leftProfile.addPoints(25);
            rightProfile.addPoints(25);
        }
    }

    /**
     * Avanza el juego un paso (lo llama el reloj de la interfaz cada 16 ms).
     * Aqui vive toda la fisica y las reglas por frame.
     */
    public void update(double deltaTime) {
        if (finished) {
            return;
        }

        // si es contra la maquina, el bot decide su jugada; su patada pasa por el camino real
        if (vsMachine) {
            bot.decideAction(match);
            if (bot.wantsToKick()) {
                tryKick(rightPlayer, leftPlayer, -1);
                bot.clearKickIntent();
            }
        }

        // gravedad de los jugadores, aturdimiento, carga de poder y efectos
        leftPlayer.update(PLAYER_GROUND);
        rightPlayer.update(PLAYER_GROUND);
        leftPlayer.updateStun(deltaTime);
        rightPlayer.updateStun(deltaTime);
        leftPlayer.getPowerBar().update(deltaTime);
        rightPlayer.getPowerBar().update(deltaTime);
        leftPlayer.updateEffect(deltaTime);
        rightPlayer.updateEffect(deltaTime);

        // choque entre jugadores
        playerCollision();

        //el balon avanza (se mueve con su gravedad y su friccion)
        ball.advance();

        // gol
        checkGoal();

        // si un jugador con aura toca el balon, se ejecuta su poder
        if (ballTouches(leftPlayer)) {
            match.checkBallContact(leftPlayer);
        }
        if (ballTouches(rightPlayer)) {
            match.checkBallContact(rightPlayer);
        }

        // el balon rebota contra los bordes y se frena si va muy lento
        ball.bounceInBounds(WIDTH, BALL_DIAMETER, BALL_GROUND);

        // limites de los jugadores (que no se salgan de la cancha)
        leftPlayer.clampToField(WIDTH, PLAYER_DIAMETER);
        rightPlayer.clampToField(WIDTH, PLAYER_DIAMETER);

        // cronometro + condicion de victoria (la decide el dominio)
        match.getTimer().update(deltaTime);
        if (match.checkWinCondition()) {
            finishMatch();
        }
    }

    // reglas internas de la fachada

    private void tryKick(Player player, Player opponent, int direction) {
        double playerCenterX = player.getPositionX() + PLAYER_DIAMETER / 2.0;
        double playerCenterY = player.getPositionY() + PLAYER_DIAMETER / 2.0;

        // si el balon esta cerca, lo impulsa
        double ballCenterX = ball.getPositionX() + BALL_DIAMETER / 2.0;
        double ballCenterY = ball.getPositionY() + BALL_DIAMETER / 2.0;
        if (Math.abs(playerCenterX - ballCenterX) < PLAYER_DIAMETER
                && Math.abs(playerCenterY - ballCenterY) < PLAYER_DIAMETER) {
            double angle;
            if (direction == 1) {
                angle = -KICK_ANGLE;
            } else {
                angle = Math.PI + KICK_ANGLE;
            }
            ball.applyForce(KICK_FORCE, angle);
        }

        // si el rival esta dentro del alcance del pie, le hace daño
        double oppCenterX = opponent.getPositionX() + PLAYER_DIAMETER / 2.0;
        double oppCenterY = opponent.getPositionY() + PLAYER_DIAMETER / 2.0;
        if (Math.abs(playerCenterX - oppCenterX) < PLAYER_DIAMETER
                && Math.abs(playerCenterY - oppCenterY) < PLAYER_DIAMETER) {
            match.checkKickContact(player, opponent);
        }
    }

    private void playerCollision() {
        double center1X = leftPlayer.getPositionX() + PLAYER_DIAMETER / 2.0;
        double center1Y = leftPlayer.getPositionY() + PLAYER_DIAMETER / 2.0;
        double center2X = rightPlayer.getPositionX() + PLAYER_DIAMETER / 2.0;
        double center2Y = rightPlayer.getPositionY() + PLAYER_DIAMETER / 2.0;

        double overlapX = PLAYER_DIAMETER - Math.abs(center1X - center2X);
        double overlapY = PLAYER_DIAMETER - Math.abs(center1Y - center2Y);

        if (overlapX > 0 && overlapY > 0) {
            if (center1X < center2X) {
                leftPlayer.setPositionX(leftPlayer.getPositionX() - overlapX / 2);
                rightPlayer.setPositionX(rightPlayer.getPositionX() + overlapX / 2);
            } else {
                leftPlayer.setPositionX(leftPlayer.getPositionX() + overlapX / 2);
                rightPlayer.setPositionX(rightPlayer.getPositionX() - overlapX / 2);
            }
        }
    }

    private void checkGoal() {
        if (leftGoal.isGoal(ball)) {
            match.onGoal(rightPlayer);   //gol en el arco izquierdo,  punto para el J2
            centerBall();
        } else if (rightGoal.isGoal(ball)) {
            match.onGoal(leftPlayer);
            centerBall();
        }
    }

    private void centerBall() {
        ball.setPositionX(WIDTH / 2.0 - BALL_DIAMETER / 2.0);
        ball.setPositionY(BALL_GROUND);
        ball.setVelocityX(0);
        ball.setVelocityY(0);
    }

    private boolean ballTouches(Player player) {
        double pCenterX = player.getPositionX() + PLAYER_DIAMETER / 2.0;
        double pCenterY = player.getPositionY() + PLAYER_DIAMETER / 2.0;
        double bCenterX = ball.getPositionX() + BALL_DIAMETER / 2.0;
        double bCenterY = ball.getPositionY() + BALL_DIAMETER / 2.0;
        double alcance = (PLAYER_DIAMETER + BALL_DIAMETER) / 2.0;
        return Math.abs(pCenterX - bCenterX) < alcance && Math.abs(pCenterY - bCenterY) < alcance;
    }

    // Metodo para calcular las estadisticas actuales

    private double calculations(double actual, double max){
        return actual - max;
    }

    //DATOS PARA DIBUJAR (solo lectura)

    public double getLeftPlayerX(){ 
        return leftPlayer.getPositionX(); 
    }
    public double getLeftPlayerY() { 
        return leftPlayer.getPositionY(); 
    }
    public double getRightPlayerX(){ 
        return rightPlayer.getPositionX(); 
    }
    public double getRightPlayerY(){ 
        return rightPlayer.getPositionY(); 
    }
    public double getBallX(){ 
        return ball.getPositionX(); 
    }
    public double getBallY(){ 
        return ball.getPositionY(); 
    }

    public boolean isLeftPlayerAura(){ 
        return leftPlayer.isAuraActive(); 
    }
    public boolean isRightPlayerAura(){ 
        return rightPlayer.isAuraActive(); 
    }

    public double getLeftPlayerPower(){ 
        double actual = leftPlayer.getPowerBar().getCurrentCharge();
        double max = leftPlayer.getPowerBar().getMaxCharge();
        return  calculations(actual, max); 
    }
    public double getRightPlayerPower(){ 
        double actual = rightPlayer.getPowerBar().getCurrentCharge();
        double max = rightPlayer.getPowerBar().getMaxCharge();
        return  calculations(actual, max); 
    }
    public double getLeftPlayerLife(){ 
        double actual = leftPlayer.getHealth();
        double max = leftPlayer.getMaxHealth();
        return  calculations(actual, max); 
    }
    public double getRightPlayerLife(){ 
        double actual = rightPlayer.getHealth();
        double max = rightPlayer.getMaxHealth();
        return  calculations(actual, max); 
    }

    //clave del sprite de cada jugador (la usa la presentacion para elegir la imagen)
    public String getLeftPlayerSprite() {
        return leftPlayer.getCharacter().getAppereance().getHead().getType();
    }

    public String getRightPlayerSprite() {
        return rightPlayer.getCharacter().getAppereance().getHead().getType();
    }

    public int getLeftScore(){ 
        return match.getScore().getGoalsLeftPlayer(); 
    }

    public int getRightScore(){ 
        return match.getScore().getGoalsRightPlayer(); 
    }

    public int getLeftPlayerPoints(){ 
        return leftPlayer.getPoints(); 
    }

    public int getRightPlayerPoints(){ 
        return rightPlayer.getPoints(); 
    }

    public int getTimeLeft(){ 
        return match.getTimer().getCurrentTime(); 
    }

    public boolean isFinished(){ 
        return finished; 
    }

    /** Ganador: 1 (J1), 2 (J2) o 0 (empate). Asi la interfaz no necesita Player. */
    public int getWinner() {
        Player w = match.getScore().getWinner();
        if (w == leftPlayer) {
            return 1;
        } else if (w == rightPlayer) {
            return 2;
        } else {
            return 0;
        }
    }

    // persistencia 

    /** Guarda este partido en un archivo. */
    public void saveTo(File file) throws DOPOSoccerException {
        GamePersistence persistence = new GamePersistence();
        persistence.save(this, file);
    }

    /** Abre un partido guardado en un archivo. */
    public static Game openFrom(File file) throws DOPOSoccerException {
        GamePersistence persistence = new GamePersistence();
        return persistence.open(file);
    }
}