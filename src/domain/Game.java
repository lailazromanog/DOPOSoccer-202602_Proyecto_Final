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
    private static final double AIR_FRICTION = 0.97;
    private static final double FLOOR_BOUNCE = 0.7;
    private static final double WALL_BOUNCE = 0.8;
    private static final double MIN_SPEED = 0.5;
    private static final double MIN_BOUNCE = 2.0;
    private static final double KICK_FORCE = 12;
    private static final double KICK_ANGLE = 0.3;

    //  Objetos del dominio que coordina la fachada.
    private Match match;
    private Player playerOne;
    private Player playerTwo;
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
    public Game(boolean byTime, int value, String country1, String country2) {
        playerOne = new Player();
        playerTwo = new Player();
        playerOne.setCharacter(createCharacter(country1));
        playerTwo.setCharacter(createCharacter(country2));

        ball = new Ball();
        leftGoal  = new Goal(-100, 290, (LEFT_POST - BALL_DIAMETER / 2) + 100, 135);
        rightGoal = new Goal(RIGHT_POST - BALL_DIAMETER / 2, 290, 200, 135);
        Score score = new Score(playerOne, playerTwo);

        if (byTime) {
            Timer timer = new Timer(value * 60);                 // value = minutos
            match = new Time(playerOne, playerTwo, ball, leftGoal, rightGoal, timer, score);
        } else {
            Timer timer = new Timer(0);
            match = new Goals(playerOne, playerTwo, ball, leftGoal, rightGoal, timer, score, value); // value = goles
        }

        // posiciones iniciales y balon al centro
        playerOne.setInitialPosition(WIDTH * 0.18, PLAYER_GROUND);
        playerTwo.setInitialPosition(WIDTH * 0.75, PLAYER_GROUND);
        centerBall();

        match.start();
        finished = false;
    }

    /** Crea el personaje segun el pais elegido, con su poder (Anexo 1). */
    private Character createCharacter(String country) {
        if (country.equals("Camerun")) {
            return new Character("Camerun", "Camerun", new Lightning(), new Appereance("camerun"));
        }else if (country.equals("Nigeria")) {
            return new Character("Nigeria", "Nigeria", new Tornado(), new Appereance("nigeria"));
        }else if (country.equals("Estados Unidos")) {
            return new Character("Estados Unidos", "Estados Unidos", new Illusion(), new Appereance("usa"));
        }else {
            return new Character("Corea del Sur", "Corea del Sur", new BlueAura(), new Appereance("corea"));
        }
    }

    //ordenes, lo que la interfaz manda

    public void movePlayerOneLeft()  { if (!playerOne.isStunned()) playerOne.moveLeft();  }
    public void movePlayerOneRight() { if (!playerOne.isStunned()) playerOne.moveRight(); }
    public void jumpPlayerOne()      { if (!playerOne.isStunned()) playerOne.jump();      }
    public void kickPlayerOne()      { if (!playerOne.isStunned()) tryKick(playerOne, playerTwo, 1); }
    public void usePowerPlayerOne()  { playerOne.usePower(); }

    public void movePlayerTwoLeft()  { if (!playerTwo.isStunned()) playerTwo.moveLeft();  }
    public void movePlayerTwoRight() { if (!playerTwo.isStunned()) playerTwo.moveRight(); }
    public void jumpPlayerTwo()      { if (!playerTwo.isStunned()) playerTwo.jump();      }
    public void kickPlayerTwo()      { if (!playerTwo.isStunned()) tryKick(playerTwo, playerOne, -1); }
    public void usePowerPlayerTwo()  { playerTwo.usePower(); }

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
        playerOne.update(PLAYER_GROUND);
        playerTwo.update(PLAYER_GROUND);
        playerOne.updateStun(deltaTime);
        playerTwo.updateStun(deltaTime);
        playerOne.getPowerBar().update(deltaTime);
        playerTwo.getPowerBar().update(deltaTime);
        playerOne.updateEffect(deltaTime);
        playerTwo.updateEffect(deltaTime);

        // choque entre jugadores
        playerCollision();

        // mover el balon + friccion del aire
        ball.move();
        ball.setVelocityX(ball.getVelocityX() * AIR_FRICTION);
        ball.setVelocityY(ball.getVelocityY() * AIR_FRICTION);

        // gol
        checkGoal();

        // si un jugador con aura toca el balon, se ejecuta su poder
        if (ballTouches(playerOne)) {
            match.checkBallContact(playerOne);
        }
        if (ballTouches(playerTwo)) {
            match.checkBallContact(playerTwo);
        }

        // rebote contra el piso
        if (ball.getPositionY() > BALL_GROUND) {
            ball.setPositionY(BALL_GROUND);
            ball.bounceY();
            ball.setVelocityY(ball.getVelocityY() * FLOOR_BOUNCE);
            if (Math.abs(ball.getVelocityY()) < MIN_BOUNCE) {
                ball.setVelocityY(0);
            }
        }
        // rebote contra el techo
        if (ball.getPositionY() < 0) {
            ball.setPositionY(0);
            ball.bounceY();
        }
        // rebote contra las paredes
        if (ball.getPositionX() < 0) {
            ball.setPositionX(0);
            ball.bounceX();
            ball.setVelocityX(ball.getVelocityX() * WALL_BOUNCE);
        }
        if (ball.getPositionX() > WIDTH - BALL_DIAMETER) {
            ball.setPositionX(WIDTH - BALL_DIAMETER);
            ball.bounceX();
            ball.setVelocityX(ball.getVelocityX() * WALL_BOUNCE);
        }
        // friccion horizontal: si va muy lento, se para
        if (Math.abs(ball.getVelocityX()) < MIN_SPEED) {
            ball.setVelocityX(0);
        }

        // limites de los jugadores (que no se salgan de la cancha)
        clampPlayer(playerOne);
        clampPlayer(playerTwo);

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
        double center1X = playerOne.getPositionX() + PLAYER_DIAMETER / 2.0;
        double center1Y = playerOne.getPositionY() + PLAYER_DIAMETER / 2.0;
        double center2X = playerTwo.getPositionX() + PLAYER_DIAMETER / 2.0;
        double center2Y = playerTwo.getPositionY() + PLAYER_DIAMETER / 2.0;

        double overlapX = PLAYER_DIAMETER - Math.abs(center1X - center2X);
        double overlapY = PLAYER_DIAMETER - Math.abs(center1Y - center2Y);

        if (overlapX > 0 && overlapY > 0) {
            if (center1X < center2X) {
                playerOne.setPositionX(playerOne.getPositionX() - overlapX / 2);
                playerTwo.setPositionX(playerTwo.getPositionX() + overlapX / 2);
            } else {
                playerOne.setPositionX(playerOne.getPositionX() + overlapX / 2);
                playerTwo.setPositionX(playerTwo.getPositionX() - overlapX / 2);
            }
        }
    }

    private void checkGoal() {
        if (leftGoal.isGoal(ball)) {
            match.onGoal(playerTwo);   //gol en el arco izquierdo,  punto para el J2
            centerBall();
        } else if (rightGoal.isGoal(ball)) {
            match.onGoal(playerOne);
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

    private void clampPlayer(Player player) {
        if (player.getPositionX() < 0) {
            player.setPositionX(0);
        }
        if (player.getPositionX() > WIDTH - PLAYER_DIAMETER) {
            player.setPositionX(WIDTH - PLAYER_DIAMETER);
        }
    }

    //DATOS PARA DIBUJAR (solo lectura)

    public double getPlayerOneX(){ 
        return playerOne.getPositionX(); 
    }
    public double getPlayerOneY() { 
        return playerOne.getPositionY(); 
    }
    public double getPlayerTwoX(){ 
        return playerTwo.getPositionX(); 
    }
    public double getPlayerTwoY(){ 
        return playerTwo.getPositionY(); 
    }
    public double getBallX(){ 
        return ball.getPositionX(); 
    }
    public double getBallY(){ 
        return ball.getPositionY(); 
    }

    public boolean isPlayerOneAura(){ 
        return playerOne.isAuraActive(); 
    }
    public boolean isPlayerTwoAura(){ 
        return playerTwo.isAuraActive(); 
    }

    public double getPlayerOnePower(){ 
        return playerOne.getPowerBar().getCurrentCharge() / playerOne.getPowerBar().getMaxCharge(); 
    }
    public double getPlayerTwoPower(){ 
        return playerTwo.getPowerBar().getCurrentCharge() / playerTwo.getPowerBar().getMaxCharge(); 
    }
    public double getPlayerOneLife(){ 
        return playerOne.getHealth() / playerOne.getMaxHealth(); 
    }
    public double getPlayerTwoLife(){ 
        return playerTwo.getHealth() / playerTwo.getMaxHealth(); 
    }

    //clave del sprite de cada jugador (la usa la presentacion para elegir la imagen)
    public String getPlayerOneSprite() {
        return playerOne.getCharacter().getAppereance().getHead().getType();
    }

    public String getPlayerTwoSprite() {
        return playerTwo.getCharacter().getAppereance().getHead().getType();
    }

    public int getScoreOne(){ 
        return match.getScore().getGoalsPlayerOne(); 
    }

    public int getScoreTwo(){ 
        return match.getScore().getGoalsPlayerTwo(); 
    }

    public int getPlayerOnePoints(){ 
        return playerOne.getPoints(); 
    }

    public int getPlayerTwoPoints(){ 
        return playerTwo.getPoints(); 
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
        if (w == playerOne) {
            return 1;
        } else if (w == playerTwo) {
            return 2;
        } else {
            return 0;
        }
    }
}