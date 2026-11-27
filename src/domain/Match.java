package domain;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Representa un partido en curso entre dos jugadores.
 * Reúne a los jugadores, el balón, los dos arcos, el cronómetro y el
 * marcador, y aplica las reglas comunes a todo partido: repartir
 * puntos al terminar, controlar el inicio y la pausa, registrar los
 * goles y ejecutar los tiros de poder cuando el balón toca a un
 * jugador con el aura activa. Cada subclase concreta define su propia
 * condición de victoria según el tipo de partido.
 *
 * Punto de extensión: para agregar un tipo de partido nuevo basta con
 * crear una subclase propia que defina su condición de victoria, y
 * registrarla en la fábrica de partidos. El resto del juego trabaja
 * contra esta clase y no necesita cambios.
 */
public abstract class Match implements Serializable{
    protected Player leftPlayer;
    protected Player rightPlayer;
    protected Ball ball;
    protected Goal leftGoal;
    protected Goal rightGoal;
    protected Timer timer;
    protected Score score;
    protected List<Ball> fakeBalls = new ArrayList<>();   // balones senuelo del poder Illusion
    protected double fakeTimer = 0;                        // cuanto duran los senuelos en pantalla

    /**
     * Crea un partido con los dos jugadores, el balón, los dos arcos,
     * el cronómetro y el marcador que va a coordinar.
     * @param leftPlayer jugador que defiende el lado izquierdo de la cancha
     * @param rightPlayer jugador que defiende el lado derecho de la cancha
     * @param ball balón del partido
     * @param leftGoal arco izquierdo de la cancha
     * @param rightGoal arco derecho de la cancha
     * @param timer cronómetro del partido
     * @param score marcador del partido
     */
    public Match(Player leftPlayer, Player rightPlayer, Ball ball, Goal leftGoal, Goal rightGoal, Timer timer, Score score) {
        this.leftPlayer = leftPlayer;
        this.rightPlayer = rightPlayer;
        this.ball = ball;
        this.leftGoal = leftGoal;
        this.rightGoal = rightGoal;
        this.timer = timer;
        this.score = score;
    }

    //reparte los puntos al terminar: 100 al ganador y 25 al perdedor (regla del juego)
    /**
     * Reparte los puntos del partido al terminar según el marcador
     * final: cien puntos al jugador ganador y veinticinco al jugador
     * perdedor, o veinticinco a cada jugador si el partido quedó empatado.
     */
    public void awardPoints(){
        Player winner = score.getWinner();
        if (winner == leftPlayer) {
            leftPlayer.addPoints(100);
            rightPlayer.addPoints(25);
        }else if (winner == rightPlayer) {
            rightPlayer.addPoints(100);
            leftPlayer.addPoints(25);
        }else {
            //empate 
            leftPlayer.addPoints(25);
            rightPlayer.addPoints(25);
        }
    }


    /**
     * Pone en marcha el partido, arrancando su cronómetro.
     */
    public void start(){
        timer.start();
    }

    /**
     * Pausa momentáneamente el partido, deteniendo su cronómetro sin
     * perder el tiempo restante.
     */
    public void pause(){
        timer.stop();
    }

    /**
     * Reanuda el partido después de una pausa, volviendo a poner en
     * marcha su cronómetro.
     */
    public void resume(){
        timer.start();
    }

    /**
     * Termina el partido, deteniendo definitivamente su cronómetro.
     */
    public void finish(){
        timer.stop();
    }

    /**
     * Registra un gol del jugador dado: lo suma al marcador, carga un
     * cuarto de la barra de poder del rival, devuelve a ambos
     * jugadores a su posición inicial y reinicia el balón al centro de
     * la cancha.
     * @param player jugador al que se le anota el gol
     */
    public void onGoal(Player player){
        score.addGoal(player);

        if(player == leftPlayer){
            rightPlayer.getPowerBar().chargeOnGoal();
        } else {
            leftPlayer.getPowerBar().chargeOnGoal();
        }
        leftPlayer.resetPosition();
        rightPlayer.resetPosition();
        ball.reset();
    }

    /**
     * Recibe los balones señuelo ya creados por el tiro de poder
     * Ilusión y los mantiene en pantalla durante la duración dada.
     * @param balls balones señuelo creados por el tiro de poder
     * @param duration duración en segundos durante la que permanecen los balones señuelo
     */
    public void addFakeBalls(List<Ball> balls, double duration) {
        this.fakeBalls = balls;
        this.fakeTimer = duration;
    }

    /**
     * Avanza los balones señuelo del efecto Ilusión un paso dentro de
     * la cancha y los elimina cuando se acaba la duración del efecto.
     * @param deltaTime tiempo transcurrido desde la última actualización, en segundos
     * @param width ancho de la cancha
     * @param ballDiameter diámetro de los balones
     * @param ground posición vertical del suelo de la cancha
     */
    public void updateFakeBalls(double deltaTime, int width, int ballDiameter, int ground) {
        if (fakeTimer <= 0) {
            return;
        }
        fakeTimer -= deltaTime;
        if (fakeTimer <= 0) {
            fakeBalls.clear();
            return;
        }
        for (int i = 0; i < fakeBalls.size(); i++) {
            Ball fake = fakeBalls.get(i);
            fake.advance();
            fake.bounceInBounds(width, ballDiameter, ground);
        }
    }

    /**
     * Entrega los balones señuelo activos del efecto Ilusión.
     * @return la lista de balones señuelo activos
     */
    public List<Ball> getFakeBalls() {
        return fakeBalls;
    }


    /**
     * Indica si el partido ya se decidió, según la condición de
     * victoria propia del tipo de partido.
     * @return verdadero si el partido ya tiene un resultado definitivo, falso en caso contrario
     */
    public abstract boolean checkWinCondition();

    /**
     * Entrega el jugador que defiende el lado izquierdo de la cancha.
     * @return el jugador izquierdo
     */
    public Player getLeftPlayer() {
        return leftPlayer;
    }

    /**
     * Entrega el jugador que defiende el lado derecho de la cancha.
     * @return el jugador derecho
     */
    public Player getRightPlayer() {
        return rightPlayer;
    }

    /**
     * Entrega el balón del partido.
     * @return el balón del partido
     */
    public Ball getBall() {
        return ball;
    }

    /**
     * Entrega el marcador del partido.
     * @return el marcador del partido
     */
    public Score getScore() {
        return score;
    }

    /**
     * Entrega el cronómetro del partido.
     * @return el cronómetro del partido
     */
    public Timer getTimer() {
        return timer;
    }

    /**
     * Ejecuta el tiro de poder del jugador dado cuando el balón lo
     * toca mientras tiene el aura activa, y apaga el aura una vez ejecutado.
     * @param player jugador que tocó el balón
     */
    public void checkBallContact(Player player) {
        if (player.isAuraActive()) {
            player.getCharacter().getPower().activate(this, player);
            player.setAuraActive(false);
        }
    }

    /**
     * Aplica el daño de una patada normal del atacante sobre el rival,
     * según el multiplicador de la estadística kick del atacante.
     * @param attacker jugador que ejecuta la patada
     * @param opponent jugador que recibe el daño de la patada
     */
    public void checkKickContact(Player attacker, Player opponent){
        double damage = 10 * attacker.getMultiplicadorKick();
        opponent.takeDamage(damage);
    }

    /**
     * Entrega el arco que defiende el jugador izquierdo.
     * @return el arco izquierdo
     */
    public Goal getLeftGoal() {
        return leftGoal;
    }

    /**
     * Entrega el arco que defiende el jugador derecho.
     * @return el arco derecho
     */
    public Goal getRightGoal() {
        return rightGoal;
    }
}
