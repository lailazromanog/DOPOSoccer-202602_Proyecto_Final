package domain;

public class Ball  {
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

    public Ball(){
        this.positionX = 0;
        this.positionY = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.gravity = 0.4; //era 9.8, cuando usamos pixeles usamos algo mas pequeño
    }

    public void move(){
        positionX += velocityX;
        positionY += velocityY;
        applyGravity();
    }
    
    private void applyGravity(){
        velocityY += gravity;
    }

    public void applyForce(double force, double angle){
        velocityX += force * Math.cos(angle);
        velocityY += force * Math.sin(angle);
    }

        /** Avanza un frame: mueve el balon (con su gravedad) y le aplica friccion del aire. */
    public void advance(){
        move();
        velocityX *= AIR_FRICTION;
        velocityY *= AIR_FRICTION;
    }

    /** Rebota el balon contra el piso, el techo y las paredes, y lo frena si va muy lento. */
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

    public void reset(){
        this.positionX = 0;
        this.positionY = 0;
        this.velocityX = 0;
        this.velocityY = 0;
        this.gravity = 0.4;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public double getVelocityX() {
        return velocityX;
    }

    public double getVelocityY() {
        return velocityY;
    }

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
