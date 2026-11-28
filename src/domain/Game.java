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
    private static final double KICK_REACH = 75;

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
     * Arma un partido nuevo con los jugadores, el balón, los arcos y
     * el tipo de partido elegidos, aplicando a cada jugador las
     * estadísticas y el progreso guardados en su perfil.
     * @param matchType tipo de partido elegido, por tiempo o por goles
     * @param value minutos de duración del partido si es por tiempo, o número de goles para ganar si es por goles
     * @param country1 país del personaje del jugador 1
     * @param country2 país del personaje del jugador 2
     * @param opponent modalidad del jugador 2, humano o perfil de máquina
     * @param leftProfile perfil persistente del jugador 1, con sus estadísticas y sus puntos
     * @param rightProfile perfil persistente del jugador 2, con sus estadísticas y sus puntos
     * @throws DOPOSoccerException si el país de alguno de los personajes, el perfil de la máquina o el tipo de partido no son reconocidos
     */
    public Game(String matchType, int value, String country1, String country2, String opponent, Profile leftProfile, Profile rightProfile) throws DOPOSoccerException {
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

    /**
     * Mueve al jugador izquierdo hacia la izquierda, si no está aturdido.
     */
    public void moveLeftPlayerLeft(){
        if (!leftPlayer.isStunned()) leftPlayer.moveLeft();
    }

    /**
     * Mueve al jugador izquierdo hacia la derecha, si no está aturdido.
     */
    public void moveLeftPlayerRight(){
        if (!leftPlayer.isStunned()) leftPlayer.moveRight();
    }

    /**
     * Hace saltar al jugador izquierdo, si no está aturdido.
     */
    public void jumpLeftPlayer(){
        if (!leftPlayer.isStunned()) leftPlayer.jump();
    }

    /**
     * Patea con el jugador izquierdo, si no está aturdido.
     */
    public void kickLeftPlayer(){
        if (!leftPlayer.isStunned()) tryKick(leftPlayer, rightPlayer, 1);
    }

    /**
     * Activa el poder del jugador izquierdo.
     */
    public void usePowerLeftPlayer(){
        leftPlayer.usePower();
    }

    /**
     * Mueve al jugador derecho hacia la izquierda, si es humano y no está aturdido.
     */
    public void moveRightPlayerLeft(){
        if (!vsMachine && !rightPlayer.isStunned()) rightPlayer.moveLeft();
    }

    /**
     * Mueve al jugador derecho hacia la derecha, si es humano y no está aturdido.
     */
    public void moveRightPlayerRight(){
        if (!vsMachine && !rightPlayer.isStunned()) rightPlayer.moveRight();
    }

    /**
     * Hace saltar al jugador derecho, si es humano y no está aturdido.
     */
    public void jumpRightPlayer(){
        if (!vsMachine && !rightPlayer.isStunned()) rightPlayer.jump();
    }

    /**
     * Patea con el jugador derecho, si es humano y no está aturdido.
     */
    public void kickRightPlayer(){
        if (!vsMachine && !rightPlayer.isStunned()) tryKick(rightPlayer, leftPlayer, -1);
    }

    /**
     * Activa el poder del jugador derecho, si es humano.
     */
    public void usePowerRightPlayer(){
        if (!vsMachine) rightPlayer.usePower();
    }

    /**
     * Termina el partido antes de que se decida solo, por ejemplo si
     * un jugador lo cierra a mitad de partido: reparte los puntos del
     * partido a ambos jugadores y a sus perfiles persistentes, y
     * detiene el partido. No hace nada si el partido ya había terminado.
     */
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
     * Reparte los puntos del partido a los perfiles persistentes de
     * ambos jugadores, si el partido se armó con perfiles vivos. No
     * hace nada si el partido se abrió desde un archivo guardado, sin
     * perfiles asociados.
     * */
    private void awardToProfiles() {
        if (leftProfile == null || rightProfile == null) {
            return;   // partida abierta desde archivo: sin perfiles vivos
        }
        int w = getWinner();
        try {
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
        } catch (DOPOSoccerException e) {
            // los puntos repartidos son constantes positivas, asi que no deberia ocurrir
            throw new IllegalStateException("Error interno al repartir puntos a los perfiles: " + e.getMessage(), e);
        }
    }

    /**
     * Avanza el juego un paso, aplicando toda la física y las reglas
     * del partido para ese frame: la decisión del bot si corresponde,
     * la gravedad y los efectos de los jugadores, el choque entre
     * ellos, el avance del balón, los goles, el impacto y el disparo de
     * los tiros de poder, el tiempo de vuelo del tiro de poder, los
     * límites de la cancha y la condición de victoria del partido. No
     * hace nada si el partido ya terminó.
     * @param deltaTime tiempo transcurrido desde la última actualización, en segundos
     */
    public void update(double deltaTime) {
        if (finished) {
            return;
        }

        // si es contra la maquina, el bot decide su jugada; su patada pasa por el camino real
        if (vsMachine) {
            bot.decideAction(match);
            if (bot.wantsToKick()) {
                // si esta aturdido no puede patear, pero la intencion se borra para que no dispare la patada al recuperarse
                if(!rightPlayer.isStunned()){
                    tryKick(rightPlayer, leftPlayer, -1);
                }
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

        // gol, si entra un tiro de poder el gol cuenta pero sin daño ni efecto
        checkGoal();

        // impacto: si hay un tiro de poder en vuelo y el balon toca al rival, se aplican daño y efecto
        checkPowerShotHit();

        // disparo: si un jugador con aura toca el balon, sale su tiro de poder

        if (ballTouches(leftPlayer)) {
            match.checkBallContact(leftPlayer);
        }
        if (ballTouches(rightPlayer)) {
            match.checkBallContact(rightPlayer);
        }

        // el balon rebota contra los bordes y se frena si va muy lento
        ball.bounceInBounds(WIDTH, BALL_DIAMETER, BALL_GROUND);

        // el tiro de poder en vuelo se termina solo si se agota su tiempo
        match.updatePowerShot(deltaTime);


        // balones falsos del poder Illusion (se mueven y desaparecen solos)
        match.updateFakeBalls(deltaTime, WIDTH, BALL_DIAMETER, BALL_GROUND);
        
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

    /**
    * Intenta ejecutar la patada de un jugador. Si la patada toca el
    * balón, únicamente lo impulsa en la dirección dada. Si no toca el
    * balón y el rival está dentro del alcance del pie, le aplica el
    * daño de la patada.
    * @param player jugador que ejecuta la patada
    * @param opponent rival del jugador que patea
    * @param direction dirección de la patada, uno hacia la derecha o menos uno hacia la izquierda
    */

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
            return; //la patada que roca el balon solo impulsa no hace daño
        }

        // la patada no toca el balon y si el rival esta dentro del alcance del pie, le hace daño
        double oppCenterX = opponent.getPositionX() + PLAYER_DIAMETER / 2.0;
        double oppCenterY = opponent.getPositionY() + PLAYER_DIAMETER / 2.0;
        if (Math.abs(playerCenterX - oppCenterX) < KICK_REACH
                && Math.abs(playerCenterY - oppCenterY) < PLAYER_DIAMETER) {
            match.checkKickContact(player, opponent);
        }
    }


    /**
     * Revisa si hay un tiro de poder en vuelo y si el balón toca al rival
     * de quien lo disparó. Si lo toca, el partido aplica el daño y el
     * efecto del poder y termina el tiro.
     */
    private void checkPowerShotHit() {
        if (!match.isPowerShotActive()) {
            return;
        }
        Player shooter = match.getPowerShooter();
        Player opponent = match.getOpponentOf(shooter);
        if (ballTouches(opponent)) {
            match.onPowerShotHit();
        }
    }

    /**
     * Separa a los dos jugadores cuando sus figuras se superponen,
     * empujando a cada uno hacia su propio lado por la mitad de lo
     * que se superponen.
     */
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

    /**
     * Revisa si el balón entró en alguno de los dos arcos y, de ser
     * así, registra el gol a favor del jugador contrario y centra el
     * balón para reanudar el juego.
     */
    private void checkGoal() {
        if (leftGoal.isGoal(ball)) {
            match.onGoal(rightPlayer);   //gol en el arco izquierdo,  punto para el J2
            centerBall();
        } else if (rightGoal.isGoal(ball)) {
            match.onGoal(leftPlayer);
            centerBall();
        }
    }

    /**
     * Detiene el balón y lo deja en el centro de la cancha, para
     * reanudar el juego después de cada gol.
     */
    private void centerBall() {
        ball.setPositionX(WIDTH / 2.0 - BALL_DIAMETER / 2.0);
        ball.setPositionY(BALL_GROUND);
        ball.setVelocityX(0);
        ball.setVelocityY(0);
    }

    /**
     * Indica si el balón está lo bastante cerca del jugador dado como
     * para considerarse en contacto con él.
     * @param player jugador del que se revisa el contacto con el balón
     * @return verdadero si el balón toca al jugador, falso en caso contrario
     */
    private boolean ballTouches(Player player) {
        double pCenterX = player.getPositionX() + PLAYER_DIAMETER / 2.0;
        double pCenterY = player.getPositionY() + PLAYER_DIAMETER / 2.0;
        double bCenterX = ball.getPositionX() + BALL_DIAMETER / 2.0;
        double bCenterY = ball.getPositionY() + BALL_DIAMETER / 2.0;
        double alcance = (PLAYER_DIAMETER + BALL_DIAMETER) / 2.0;
        return Math.abs(pCenterX - bCenterX) < alcance && Math.abs(pCenterY - bCenterY) < alcance;
    }


    //DATOS PARA DIBUJAR (solo lectura)

    /**
     * Entrega la posición horizontal del jugador izquierdo.
     * @return la posición horizontal del jugador izquierdo
     */
        public double getLeftPlayerX(){
        return leftPlayer.getPositionX();
    }

    /**
     * Entrega la posición vertical del jugador izquierdo.
     * @return la posición vertical del jugador izquierdo
     */
    public double getLeftPlayerY() {
        return leftPlayer.getPositionY();
    }

    /**
     * Entrega la posición horizontal del jugador derecho.
     * @return la posición horizontal del jugador derecho
     */
    public double getRightPlayerX(){
        return rightPlayer.getPositionX();
    }

    /**
     * Entrega la posición vertical del jugador derecho.
     * @return la posición vertical del jugador derecho
     */
    public double getRightPlayerY(){
        return rightPlayer.getPositionY();
    }

    /**
     * Entrega las posiciones horizontal y vertical de los balones
     * falsos del efecto Ilusión, para que la presentación los dibuje.
     * @return la lista de posiciones de los balones falsos activos
     */
    public java.util.List<double[]> getFakeBalls(){
        java.util.List<double[]> out = new java.util.ArrayList<>();
        java.util.List<Ball> fakes = match.getFakeBalls();
        for (int i = 0; i < fakes.size(); i++) {
            Ball fake = fakes.get(i);
            double[] position = new double[2];
            position[0] = fake.getPositionX();
            position[1] = fake.getPositionY();
            out.add(position);
        }
        return out;
    }

    /**
     * Entrega la posición horizontal del balón.
     * @return la posición horizontal del balón
     */
    public double getBallX(){
        return ball.getPositionX();
    }

    /**
     * Entrega la posición vertical del balón.
     * @return la posición vertical del balón
     */
    public double getBallY(){
        return ball.getPositionY();
    }


    /**
     * Entrega la velocidad horizontal del balón, para que la presentación
     * dibuje la estela del tiro de poder en la dirección correcta.
     * @return la velocidad horizontal del balón
     */
    public double getBallVelocityX(){
        return ball.getVelocityX();
    }

    /**
     * Entrega la velocidad vertical del balón, para que la presentación
     * dibuje la estela del tiro de poder en la dirección correcta.
     * @return la velocidad vertical del balón
     */
    public double getBallVelocityY(){
        return ball.getVelocityY();
    }

    /**
     * Indica si hay un tiro de poder en vuelo en este momento.
     * @return verdadero si hay un tiro de poder en vuelo, falso en caso contrario
     */
    public boolean isPowerShotActive(){
        return match.isPowerShotActive();
    }

    /**
     * Entrega el nombre del tiro de poder que está en vuelo, para que la
     * presentación elija cómo dibujar el balón.
     * @return el nombre del tiro de poder en vuelo, o nulo si no hay ninguno
     */
    public String getPowerShotName(){
        if (match.isPowerShotActive()) {
            return match.getPowerShooter().getCharacter().getPower().getName();
        } else {
            return null;
        }
    }

    /**
     * Entrega el nombre del efecto activo del jugador izquierdo, para que
     * la presentación lo muestre sobre el personaje.
     * @return el nombre del efecto activo del jugador izquierdo, o nulo si no tiene ninguno
     */
    public String getLeftPlayerEffect(){
        return leftPlayer.getCurrentEffect();
    }

    /**
     * Entrega el nombre del efecto activo del jugador derecho, para que
     * la presentación lo muestre sobre el personaje.
     * @return el nombre del efecto activo del jugador derecho, o nulo si no tiene ninguno
     */
    public String getRightPlayerEffect(){
        return rightPlayer.getCurrentEffect();
    }


    /**
     * Indica si el jugador izquierdo tiene el aura de su poder activa.
     * @return verdadero si el aura del jugador izquierdo está activa, falso en caso contrario
     */
    public boolean isLeftPlayerAura(){
        return leftPlayer.isAuraActive();
    }

    /**
     * Indica si el jugador derecho tiene el aura de su poder activa.
     * @return verdadero si el aura del jugador derecho está activa, falso en caso contrario
     */
    public boolean isRightPlayerAura(){
        return rightPlayer.isAuraActive();
    }

    /**
     * Entrega el progreso de la barra de poder del jugador izquierdo
     * como una proporción entre cero y uno.
     * @return el progreso de la barra de poder del jugador izquierdo
     */
    public double getLeftPlayerPower(){
        return leftPlayer.getPowerBar().getCurrentCharge() / leftPlayer.getPowerBar().getMaxCharge();
    }

    /**
     * Entrega el progreso de la barra de poder del jugador derecho
     * como una proporción entre cero y uno.
     * @return el progreso de la barra de poder del jugador derecho
     */
    public double getRightPlayerPower(){
        return rightPlayer.getPowerBar().getCurrentCharge() / rightPlayer.getPowerBar().getMaxCharge();
    }

    /**
     * Entrega la vida del jugador izquierdo como una proporción entre
     * cero y uno de su vida máxima.
     * @return la proporción de vida del jugador izquierdo
     */
    public double getLeftPlayerLife(){
        return leftPlayer.getHealth() / leftPlayer.getMaxHealth();
    }

    /**
     * Entrega la vida del jugador derecho como una proporción entre
     * cero y uno de su vida máxima.
     * @return la proporción de vida del jugador derecho
     */
    public double getRightPlayerLife(){
        return rightPlayer.getHealth() / rightPlayer.getMaxHealth();
    }

    //clave del sprite de cada jugador (la usa la presentacion para elegir la imagen)
    /**
     * Entrega la clave de la imagen del jugador izquierdo, usada por
     * la presentación para elegir su sprite.
     * @return la clave de la imagen del jugador izquierdo
     */
    public String getLeftPlayerSprite() {
        return leftPlayer.getCharacter().getAppereance().getHead().getType();
    }

    /**
     * Entrega la clave de la imagen del jugador derecho, usada por la
     * presentación para elegir su sprite.
     * @return la clave de la imagen del jugador derecho
     */
    public String getRightPlayerSprite() {
        return rightPlayer.getCharacter().getAppereance().getHead().getType();
    }

    /**
     * Entrega los goles anotados por el jugador izquierdo.
     * @return los goles del jugador izquierdo
     */
    public int getLeftScore(){
        return match.getScore().getGoalsLeftPlayer();
    }

    /**
     * Entrega los goles anotados por el jugador derecho.
     * @return los goles del jugador derecho
     */
    public int getRightScore(){
        return match.getScore().getGoalsRightPlayer();
    }

    /**
     * Entrega los puntos acumulados por el jugador izquierdo.
     * @return los puntos del jugador izquierdo
     */
    public int getLeftPlayerPoints(){
        return leftPlayer.getPoints();
    }

    /**
     * Entrega los puntos acumulados por el jugador derecho.
     * @return los puntos del jugador derecho
     */
    public int getRightPlayerPoints(){
        return rightPlayer.getPoints();
    }

    /**
     * Entrega el tiempo restante del partido, en segundos.
     * @return el tiempo restante del partido
     */
    public int getTimeLeft(){
        return match.getTimer().getCurrentTime();
    }

    /**
     * Indica si el partido ya terminó.
     * @return verdadero si el partido ya terminó, falso en caso contrario
     */
    public boolean isFinished(){
        return finished;
    }

    /**
     * Entrega el ganador del partido, para que la presentación no
     * necesite conocer la clase Player.
     * @return uno si ganó el jugador izquierdo, dos si ganó el jugador derecho, o cero si el partido quedó empatado
     */
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

    /**
     * Guarda este partido en el archivo dado.
     * @param file archivo donde se va a guardar el partido
     * @throws DOPOSoccerException si ocurre un error al guardar el partido
     */
    public void saveTo(File file) throws DOPOSoccerException {
        GamePersistence persistence = new GamePersistence();
        persistence.save(this, file);
    }

    /**
     * Abre el partido guardado en el archivo dado.
     * @param file archivo del que se quiere abrir el partido
     * @return el partido reconstruido a partir del archivo
     * @throws DOPOSoccerException si ocurre un error al abrir el partido
     */
    public static Game openFrom(File file) throws DOPOSoccerException {
        GamePersistence persistence = new GamePersistence();
        return persistence.open(file);
    }
}