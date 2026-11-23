package domain;

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
public class Game {

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
    private CharacterFactory characterFactory;
    private MatchFactory matchFactory;
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
    public Game(String matchType, int value, String country1, String country2) {
        characterFactory = new CharacterFactory();
        matchFactory = new MatchFactory();
        leftPlayer = new Player();
        rightPlayer = new Player();
        leftPlayer.setCharacter(characterFactory.create(country1));
        rightPlayer.setCharacter(characterFactory.create(country2));

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

    public void moveLeftPlayerLeft()  { if (!leftPlayer.isStunned()) leftPlayer.moveLeft();  }
    public void moveLeftPlayerRight() { if (!leftPlayer.isStunned()) leftPlayer.moveRight(); }
    public void jumpLeftPlayer()      { if (!leftPlayer.isStunned()) leftPlayer.jump();      }
    public void kickLeftPlayer()      { if (!leftPlayer.isStunned()) tryKick(leftPlayer, rightPlayer, 1); }
    public void usePowerLeftPlayer()  { leftPlayer.usePower(); }

    public void moveRightPlayerLeft()  { if (!rightPlayer.isStunned()) rightPlayer.moveLeft();  }
    public void moveRightPlayerRight() { if (!rightPlayer.isStunned()) rightPlayer.moveRight(); }
    public void jumpRightPlayer()      { if (!rightPlayer.isStunned()) rightPlayer.jump();      }
    public void kickRightPlayer()      { if (!rightPlayer.isStunned()) tryKick(rightPlayer, leftPlayer, -1); }
    public void usePowerRightPlayer()  { rightPlayer.usePower(); }

    /** Termina el partido ya (por ejemplo con ESC): reparte puntos y detiene. */
    public void finishMatch() {
        if (finished) {
            return;
        }
        finished = true; 
        match.awardPoints();
        match.finish();
    }

    //UN FRAME DEL JUEGO 

    /**
     * Avanza el juego un paso (lo llama el reloj de la interfaz cada 16 ms).
     * Aqui vive toda la fisica y las reglas por frame.
     */
    public void update(double deltaTime) {
        if (finished) {
            return;
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
        return leftPlayer.getPowerBar().getCurrentCharge() / leftPlayer.getPowerBar().getMaxCharge(); 
    }
    public double getRightPlayerPower(){ 
        return rightPlayer.getPowerBar().getCurrentCharge() / rightPlayer.getPowerBar().getMaxCharge(); 
    }
    public double getLeftPlayerLife(){ 
        return leftPlayer.getHealth() / leftPlayer.getMaxHealth(); 
    }
    public double getRightPlayerLife(){ 
        return rightPlayer.getHealth() / rightPlayer.getMaxHealth(); 
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
}