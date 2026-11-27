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
     * Avanza un frame: mueve el balón con su gravedad y le aplica
     * fricción del aire a su velocidad.
     */
    public void advance(){
        move();
        velocityX *= AIR_FRICTION;
        velocityY *= AIR_FRICTION;
    }

    /**
     * Rebota el balón contra el piso, el techo y las paredes de la
     * cancha, perdiendo velocidad con cada rebote, y lo frena del todo
     * en horizontal si va demasiado lento.
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
        }
        if (positionX > width - diameter) {
            positionX = width - diameter;
            bounceX();
            velocityX *= WALL_BOUNCE;
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
     * permite colocar la posición X del balón desde la presentación
     */
    public void setPositionX(double x){
        this.positionX = x;
    }

    /**
     * Permite colocar la posición Y del balón desde la presentación
     */
    public void setPositionY(double  y){
        this.positionY = y;
    }

    /**
     * Permite ajustar la velocidad en X, para friccion y rebote en paredes
     */
    public void setVelocityX(double  vx){
        this.velocityX = vx;
    }

    /**
     * Permite ajustar la velocidad en Y, para fricción y rebote en las paredes
     */
    public void setVelocityY(double  vy){
        this.velocityY = vy;
    }

    

    
}
