package domain;
import java.io.Serializable;

/**
 * Representa el balón del partido.
 * Está afectado por la gravedad y rebota en el suelo, en el techo y
 * en los bordes de la cancha, perdiendo velocidad con cada rebote y
 * con la fricción del aire hasta frenarse del todo.
 */
public class Ball implements Serializable {
    private double positionX;
    private double positionY;
    private double velocityX;
    private double velocityY;
    private double gravity;
    private boolean straightFlight;   // vuelo recto de un tiro de poder, sin gravedad ni friccion

    //constantes de la fisica del balon
    private static final double AIR_FRICTION = 0.97;
    private static final double FLOOR_BOUNCE = 0.7;
    private static final double WALL_BOUNCE = 0.8;
    private static final double MIN_SPEED = 0.5;
    private static final double MIN_BOUNCE = 2.0;

    /**
     * Crea el balón detenido en el origen de la cancha, con su
     * gravedad propia ya fijada.
     */
    public Ball(){
        this.positionX = 0;
        this.positionY = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.gravity = 0.4; //era 9.8, cuando usamos pixeles usamos algo mas pequeño
        this.straightFlight = false;
    }

    /**
     * Desplaza el balón según su velocidad actual y le aplica la gravedad.
     */
    public void move(){
        positionX += velocityX;
        positionY += velocityY;
        applyGravity();
    }

    /**
     * Aumenta la velocidad vertical del balón según su gravedad propia.
     */
    private void applyGravity(){
        velocityY += gravity;
    }

    /**
     * Aplica un impulso al balón en la dirección dada.
     * @param force magnitud del impulso aplicado al balón
     * @param angle ángulo en radianes de la dirección del impulso
     */
    public void applyForce(double force, double angle){
        velocityX += force * Math.cos(angle);
        velocityY += force * Math.sin(angle);
    }

    /**
     * Avanza un frame. En vuelo normal mueve el balón con su gravedad y
     * le aplica la fricción del aire a su velocidad. En vuelo recto de un
     * tiro de poder solo lo desplaza según su velocidad, sin que caiga ni
     * se frene.
     */
    public void advance(){
        if (straightFlight) {
            // tiro de poder recto: conserva su velocidad y su altura
            positionX += velocityX;
            positionY += velocityY;
        } else {
            // vuelo normal: gravedad y friccion del aire
            move();
            velocityX *= AIR_FRICTION;
            velocityY *= AIR_FRICTION;
        }
    }

    /**
     * Rebota el balón contra el piso, el techo y las paredes de la
     * cancha, perdiendo velocidad con cada rebote, y lo frena del todo
     * en horizontal si va demasiado lento.
     * Si el balón venía en vuelo recto, al rebotar contra una pared vuelve 
     * al vuelo normal.
     * @param width ancho de la cancha
     * @param diameter diámetro del balón
     * @param ground posición vertical del suelo de la cancha
     */
    public void bounceInBounds(int width, int diameter, int ground){
        // piso
        if (positionY > ground) {
            positionY = ground;
            bounceY();
            velocityY *= FLOOR_BOUNCE;
            if (Math.abs(velocityY) < MIN_BOUNCE) {
                velocityY = 0;
            }
        }
        // techo
        if (positionY < 0) {
            positionY = 0;
            bounceY();
        }
        // paredes
        if (positionX < 0) {
            positionX = 0;
            bounceX();
            velocityX *= WALL_BOUNCE;
            straightFlight = false;         // paredes:si venia en vuelo recto, al rebotar vuelve al vuelo normal
        }
        if (positionX > width - diameter) {
            positionX = width - diameter;
            bounceX();
            velocityX *= WALL_BOUNCE;
            straightFlight = false;         //paredes: si venia en vuelo recto, al rebotar vuelve al vuelo normal
        }
        // friccion horizontal: si va muy lento, se para
        if (Math.abs(velocityX) < MIN_SPEED) {
            velocityX = 0;
        }
    }
    
    /**
     * Rebote vertical, invierte la velocidad en Y, para el rebote del suelo y el techo
     */
    public void bounceY(){
        velocityY = -this.velocityY;
    }

    /**
     * Rebote horizontal, invierte la velocidad en X, para el rebote en paredes
     */
    public void bounceX(){
        this.velocityX = -this.velocityX;
    }

    /**
     * Detiene el balón y lo regresa al origen de la cancha, con su
     * gravedad propia otra vez en su valor inicial.
     */
    public void reset(){
        this.positionX = 0;
        this.positionY = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.gravity = 0.4;
    }

    /**
     * Cambia el modo de vuelo del balón. En vuelo recto no lo afectan
     * la gravedad ni la fricción del aire, para los tiros de poder que
     * salen en línea recta.
     * @param straightFlight verdadero para el vuelo recto, falso para el vuelo normal
     */
    public void setStraightFlight(boolean straightFlight) {
        this.straightFlight = straightFlight;
    }

    /**
     * Indica si el balón está en vuelo recto de un tiro de poder.
     * @return verdadero si el balón está en vuelo recto, falso en caso contrario
     */
    public boolean isStraightFlight() {
        return straightFlight;
    }


    /**
     * Entrega la posición horizontal del balón.
     * @return la posición horizontal del balón
     */
    public double getPositionX() {
        return positionX;
    }

    /**
     * Entrega la posición vertical del balón.
     * @return la posición vertical del balón
     */
    public double getPositionY() {
        return positionY;
    }

    /**
     * Entrega la velocidad horizontal actual del balón.
     * @return la velocidad horizontal del balón
     */
    public double getVelocityX() {
        return velocityX;
    }

    /**
     * Entrega la velocidad vertical actual del balón.
     * @return la velocidad vertical del balón
     */
    public double getVelocityY() {
        return velocityY;
    }

    /**
     * Entrega la gravedad propia del balón.
     * @return la gravedad del balón
     */
    public double getGravity() {
        return gravity;
    }

    /**
     * Fija la posición horizontal del balón.
     * @param x nueva posición horizontal del balón
     */
    public void setPositionX(double x){
        this.positionX = x;
    }

    /**
     * Fija la posición vertical del balón.
     * @param y nueva posición vertical del balón
     */
    public void setPositionY(double  y){
        this.positionY = y;
    }

    /**
     * Fija la velocidad horizontal del balón.
     * @param vx nueva velocidad horizontal del balón
     */
    public void setVelocityX(double  vx){
        this.velocityX = vx;
    }

    /**
     * Fija la velocidad vertical del balón.
     * @param vy nueva velocidad vertical del balón
     */
    public void setVelocityY(double  vy){
        this.velocityY = vy;
    }

    

    
}
