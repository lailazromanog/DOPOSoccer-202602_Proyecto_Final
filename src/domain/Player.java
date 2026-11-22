package domain;

public class Player {
    private double positionX;
    private double positionY;
    private double initialX;
    private double initialY;
    private double health;
    private boolean stunned;
    private double stunTimer;
    private boolean auraActive;
    private String currentEffect;
    private double effectTimer;
    private int speed;
    private int jump;
    private int kick;
    private int survival;
    private int points;
    private Character character;
    private PowerBar powerBar;
    private double velocityY; //velocidad vertical, para la fisica del salto del jugador
    private boolean onGround = true; //para que salga solo si esta en el suelo


    public Player() {
        
        this.stunned = false;
        this.stunTimer = 0;
        this.auraActive = false;
        this.speed = 5; //era 1, pero a 1 pxel por frame no vamos notar el cambio de movimmiento
        this.jump = 1;
        this.kick = 1;
        this.survival = 1;
        this.health = getMaxHealth();
        this.points = 0;
        this.currentEffect = null;
        this.effectTimer = 0;
        this.powerBar = new PowerBar();
        this.velocityY = 0; //empiezaa en el suelo, sin velocidad vertical
    }

    public void moveLeft() {
        if(!stunned && !"unconscious".equals(currentEffect)){
            if ("electrocuted".equals(currentEffect)) {
                positionX -= speed * 0.5;
            } else {
                positionX -= speed;
            }
        }
    }

    public void moveRight() {
        if(!stunned && !"unconscious".equals(currentEffect)){
            if ("electrocuted".equals(currentEffect)) {
                positionX += speed * 0.5;
            } else {
                positionX += speed;
            }
        }
    }
    /**
     * El jugador salta solo si esta en el suelo. Pone la velocidad en y
     * en negativo porque en la pantalla y= 0 es arriba, entonces 
     * subir es igual a tener la velocidad en negativo. 
     */
    public void jump() {
        if(!stunned && !"unconscious".equals(currentEffect) && !"electrocuted".equals(currentEffect)){
            if(onGround){ //solo salta si esta en el suelo
                velocityY = -10; // un impulso hacia arriba
                onGround = false; //para que no salga de pantalla
            }
        }
    }

    /**
     * Actualiza la posicion vertical del jugador en cada frame.
     * Aplica gravedad y lo detiene en el suelo.
     * @para, groundY la coordenada Y del suelo, donde se apoyan los jugadores
     */
    public void update(double  groundY){
        //la gravedad aumenta la velocidad hacia abajo en cada frame
        velocityY += 0.5;
        positionY += velocityY; 

        //si llego al suelo o lo paso, se deja al jugador en el suelo 
        if(positionY >= groundY){
            positionY = groundY;
            velocityY = 0; //ya no se mueve verticalmente
            onGround = true; //ya esta en el suelo
        }else{
            onGround = false; //se encuentra en el aire
        }
    }

    /**
     * descuenta el aturdimiento con el tiempo, al llegar a 0 se reinicia la viida
     */
    public void updateStun(double deltaTime){
        if(stunned){
            stunTimer -= deltaTime;
            if(stunTimer <= 0){
                resetHealth(); //la vida vuelve al maximo
            }
        }
    }
    
    /**
     *baja el efecto (electrocutado, insciente, confundido) 
     * y lo quita al terminar
     */
    public void updateEffect(double deltaTime){
        if(currentEffect != null){
            effectTimer -= deltaTime;
            if(effectTimer <= 0){
                clearEffect();
            }
        }
    }

    public void kick(Ball ball) {
        if (!stunned && !"unconscious".equals(currentEffect)) {
            ball.applyForce(kick, 0);
        }
    }

    public void usePower(){
        if(!stunned && powerBar.isFull() && !auraActive){
            auraActive = true;
            powerBar.reset();
        }
    }

    public void stun(double duration) {
        this.stunned = true;
        this.stunTimer = duration;
    }

    public boolean isStunned() {
        return stunned;
    }

    public void takeDamage(double damage){
        health -= damage;
        if (health <= 0) {
            health = 0;
            stun(3.0);
        }
    }

    public void resetHealth() {
        this.health = getMaxHealth();
        this.stunned = false;
        this.stunTimer = 0;
    }

    public void setInitialPosition(double x, double y) {
        this.initialX = x;
        this.initialY = y;
        this.positionX = x;
        this.positionY = y;
    }

    public void resetPosition() {
        this.positionX = initialX;
        this.positionY = initialY;
    }

    public double getMultiplicadorKick() {
        return 1 + 0.10 * (kick - 1);
    }

    public double getMaxHealth() {
        return 100 * (1 + 0.10 * (survival - 1));
    }

    public void applyEffect(String effect, double duration) {
        this.currentEffect = effect;
        this.effectTimer = duration;
    }

    public String getCurrentEffect() {
        return currentEffect;
    }

    public double getEffectTimer() {
        return effectTimer;
    }

    public void clearEffect() {
        this.currentEffect = null;
        this.effectTimer = 0;
    }

    public void upgradeStat(String stat){
        if(stat.equals("speed")){
            if (speed < 10) speed++;
        } else if(stat.equals("jump")){
            if (jump < 10) jump++;
        } else if(stat.equals("kick")){
            if (kick < 10) kick++;
        } else if(stat.equals("survival")){
            if (survival < 10) {
                survival++;
                health = getMaxHealth();
            }
        }
    }

    public void addPoints(int amount){
        this.points += amount;
    }

    public int getPoints(){
        return points;
    }

    public double getPositionX() {
        return positionX;
    }

    public double getPositionY() {
        return positionY;
    }

    public double getHealth() {
        return health;
    }

    public Character getCharacter() {
        return character;
    }

    public void setCharacter(Character character) {
        this.character = character;
    }

    public PowerBar getPowerBar() {
        return powerBar;
    }

    public boolean isAuraActive() {
        return auraActive;
    }

    public void setAuraActive(boolean auraActive){
        this.auraActive = auraActive;
    }

    public int getSpeed() {
        return speed;
    }

    public int getJump() {
        return jump;
    }

    public int getKick() {
        return kick;
    }

    public int getSurvival() {
        return survival;
    }
    //para el salto
    public double getVelocity(){
        return velocityY;
    }
    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }
}
