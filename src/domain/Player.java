package domain;
import java.io.Serializable;

/**
 * Representa a un jugador dentro de un partido.
 * Mantiene su posición en la cancha, su vida, su barra de poder, su
 * personaje, sus estadísticas, sus puntos acumulados y el efecto que
 * tenga en un momento dado, y ofrece las acciones que puede ejecutar:
 * moverse, saltar, patear y usar su poder.
 */
public class Player implements Serializable{
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
    private double knockbackVX = 0; //empuje horizontal (lo usa el poder Aura Azul)
    
    // cuanto conserva el empuje en cada frame, se va frenando poco a poco
    private static final double KNOCKBACK_FRICTION = 0.96;

    /**
     * Crea un jugador con sus estadísticas en el valor mínimo, su vida
     * en el máximo correspondiente, sus puntos en cero, sin efecto
     * activo y con su barra de poder vacía.
     */
    public Player() {
        
        this.stunned = false;
        this.stunTimer = 0;
        this.auraActive = false;
        this.speed = 1; 
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

    /**
     * Mueve al jugador hacia la izquierda, según las reglas de
     * movimiento propias de su estado actual.
     */
    public void moveLeft() {
        move(-1);
    }

    /**
     * Mueve al jugador hacia la derecha, según las reglas de
     * movimiento propias de su estado actual.
     */
    public void moveRight() {
        move(1);
    }

    /**
     * Desplaza al jugador en horizontal hacia el lado indicado. Si está
     * aturdido o inconsciente no se mueve, si está electrocutado avanza
     * a la mitad de su velocidad y si está confundido avanza hacia el
     * lado contrario al pedido.
     * @param dir sentido del movimiento, menos uno para la izquierda y uno para la derecha
     */
    private void move(int dir) {
        if (stunned || "unconscious".equals(currentEffect)) {
            return;
        }
        double step = getMoveSpeed();
        if ("electrocuted".equals(currentEffect)) {
            step = getMoveSpeed() * 0.5;
        }
        if ("confused".equals(currentEffect)) {
            dir = -dir;   //los controles quedan al reves
        }
        positionX += dir * step;
    }

    /**
     * El jugador salta solo si esta en el suelo. Pone la velocidad en y
     * en negativo porque en la pantalla y= 0 es arriba, entonces 
     * subir es igual a tener la velocidad en negativo. 
     */
    public void jump() {
        if(!stunned && !"unconscious".equals(currentEffect) && !"electrocuted".equals(currentEffect)){
            if(onGround){ //solo salta si esta en el suelo
                velocityY = -10 * (1+ 0.10 *(jump -1)); //mas nivel de salto
                onGround = false; //para que no salga de pantalla
            }
        }
    }


        /**
     * Lanza al jugador hacia arriba, despegándolo del suelo, para el
     * efecto del tiro de poder Tornado. La gravedad que se aplica en
     * la actualización de cada frame se encarga de bajarlo después.
     * @param strength fuerza con la que se lanza al jugador hacia arriba
     */
    public void launchUp(double strength) {
        this.velocityY = -strength;
        this.onGround = false;
    }


    /**
     * Aplica un empuje horizontal al jugador, para el efecto del tiro
     * de poder Aura Azul. El empuje se desliza y se frena solo con el
     * paso de los frames, y se suma al movimiento propio del jugador,
     * que puede seguir moviéndose mientras lo empujan.
     * @param vx velocidad horizontal inicial del empuje aplicado al jugador
     */
    public void applyKnockback(double vx) {
        this.knockbackVX = vx;
    }

    /**
     * Actualiza la posición del jugador en cada frame: desliza y frena
     * el empuje horizontal que tenga, aplica la gravedad a su
     * velocidad vertical, y lo detiene al llegar al suelo de la cancha.
     * @param groundY posición vertical del suelo, donde se apoyan los jugadores
     */
    public void update(double  groundY){

        //empuje horizontal (knockback): se desliza y se va frenando
        if(knockbackVX != 0){
            positionX += knockbackVX;
            knockbackVX *= KNOCKBACK_FRICTION;
            if(Math.abs(knockbackVX) < 0.5){
                knockbackVX = 0;
            }
        }
        
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
     * Descuenta el tiempo de aturdimiento transcurrido y, al agotarse,
     * reinicia la vida del jugador a su máximo y lo deja listo para
     * volver a jugar normalmente.
     * @param deltaTime tiempo transcurrido desde la última actualización, en segundos
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
     * Descuenta el tiempo del efecto activo del jugador, ya sea
     * electrocutado, inconsciente o confundido, y lo retira al
     * agotarse su duración.
     * @param deltaTime tiempo transcurrido desde la última actualización, en segundos
     */
    public void updateEffect(double deltaTime){
        if(currentEffect != null){
            effectTimer -= deltaTime;
            if(effectTimer <= 0){
                clearEffect();
            }
        }
    }

    /**
     * Patea el balón dado, impulsándolo según la estadística kick del
     * jugador, siempre que el jugador no esté aturdido ni inconsciente.
     * @param ball balón que el jugador patea
     */
    public void kick(Ball ball) {
        if (!stunned && !"unconscious".equals(currentEffect)) {
            ball.applyForce(kick, 0);
        }
    }

    /**
     * Activa el aura del poder del jugador y vacía su barra de poder,
     * siempre que la barra esté llena, el jugador no esté aturdido y
     * no tenga ya el aura activa.
     */
    public void usePower(){
        if(!stunned && powerBar.isFull() && !auraActive){
            auraActive = true;
            powerBar.reset();
        }
    }

    /**
     * Deja al jugador aturdido durante la duración dada, sin poder
     * moverse, saltar ni patear mientras dure el aturdimiento.
     * @param duration duración del aturdimiento en segundos
     */
    public void stun(double duration) {
        this.stunned = true;
        this.stunTimer = duration;
    }

    /**
     * Indica si el jugador se encuentra aturdido en este momento.
     * @return verdadero si el jugador está aturdido, falso en caso contrario
     */
    public boolean isStunned() {
        return stunned;
    }

    /**
     * Resta el daño dado a la vida del jugador y, si su vida llega a
     * cero, lo deja aturdido durante tres segundos.
     * @param damage cantidad de daño que recibe el jugador
     * @throws DOPOSoccerException si el daño es negativo
     */
    public void takeDamage(double damage) throws DOPOSoccerException{
        if (damage < 0){
            throw new DOPOSoccerException("El dano recibido no puede ser negativo. Valor recibido: " + damage + ".");
        }
        health -= damage;
        if (health <= 0) {
            health = 0;
            stun(3.0);
        }
    }

    /**
     * Reinicia la vida del jugador a su máximo y le quita el
     * aturdimiento, dejándolo listo para volver a jugar normalmente.
     */
    public void resetHealth() {
        this.health = getMaxHealth();
        this.stunned = false;
        this.stunTimer = 0;
    }

    /**
     * Fija la posición inicial del jugador dentro de la cancha, a la
     * que vuelve después de cada gol.
     * @param x posición horizontal inicial del jugador
     * @param y posición vertical inicial del jugador
     */
    public void setInitialPosition(double x, double y) {
        this.initialX = x;
        this.initialY = y;
        this.positionX = x;
        this.positionY = y;
    }

    /**
     * Devuelve al jugador a su posición inicial dentro de la cancha,
     * según las reglas del juego después de cada gol.
     */
    public void resetPosition() {
        this.positionX = initialX;
        this.positionY = initialY;
    }

    /**
     * Mantiene al jugador dentro de los bordes horizontales de la cancha.
     * @param width ancho de la cancha
     * @param diameter diámetro del jugador
     */
    public void clampToField(int width, int diameter) {
        if (positionX < 0) {
            positionX = 0;
        }
        if (positionX > width - diameter) {
            positionX = width - diameter;
        }
    }

    /**
     * Calcula el multiplicador de daño que corresponde a la
     * estadística kick actual del jugador.
     * @return el multiplicador de daño según la estadística kick
     */
    public double getMultiplicadorKick() {
        return 1 + 0.10 * (kick - 1);
    }

    /**
     * Calcula la velocidad real de desplazamiento del jugador en
     * píxeles por frame, según su estadística speed actual.
     * @return la velocidad de desplazamiento del jugador
     */
    public double getMoveSpeed() {
        return 5 * (1 + 0.10 * (speed - 1));
    }

    /**
     * Calcula la vida máxima del jugador según su estadística survival actual.
     * @return la vida máxima del jugador
     */
    public double getMaxHealth() {
        return 100 * (1 + 0.10 * (survival - 1));
    }

    /*
    * Aplica al jugador el efecto dado durante la duración dada,
     * reemplazando cualquier efecto que tuviera antes junto con el
     * empuje que ese efecto le estuviera dejando.
     * @param effect nombre del efecto a aplicar, electrocutado, inconsciente, confundido o empujado
     * @param duration duración en segundos del efecto
     */
    public void applyEffect(String effect, double duration) {
        // los efectos no se acumulan: el nuevo reemplaza al anterior y quita cualquier empuje viejo
        this.knockbackVX = 0;
        this.currentEffect = effect;
        this.effectTimer = duration;
    }

    /**
     * Entrega el nombre del efecto que tiene activo el jugador en este momento.
     * @return el nombre del efecto activo, o nulo si el jugador no tiene ningún efecto
     */
    public String getCurrentEffect() {
        return currentEffect;
    }

    /**
     * Entrega el tiempo restante del efecto activo del jugador, en segundos.
     * @return el tiempo restante del efecto activo
     */
    public double getEffectTimer() {
        return effectTimer;
    }

    /**
     * Quita cualquier efecto activo del jugador, junto con el empuje que
     * ese efecto le estuviera dejando.
     */
    public void clearEffect() {
        this.currentEffect = null;
        this.effectTimer = 0;
        this.knockbackVX = 0;
    }


    /**
     * Verifica que el nivel dado de una estadistica este dentro del rango permitido para las estadisticas del jugador.
     * @param stat nombre de la estadistica que se verifica, para armar el mensaje de error
     * @param level nivel que se quiere verificar
     * @throws DOPOSoccerException si el nivel es menor que el mínimo o mayor que el máximo permitidos
     */
    private void checkLevel(String stat, int level) throws DOPOSoccerException{
        if (level < Profile.MIN_LEVEL || level > Profile.MAX_LEVEL) {
            throw new DOPOSoccerException("Nivel invalido para la estadistica '" + stat + "': " + level + ". Debe estar entre " + Profile.MIN_LEVEL + " y " + Profile.MAX_LEVEL + ".");
        }
    }


    /**
     * Fija las estadísticas del jugador desde su perfil, al empezar el
     * partido, y ajusta su vida al máximo que les corresponde.
     * @param speed estadística de velocidad de desplazamiento del jugador
     * @param kick estadística de daño de patada del jugador
     * @param jump estadística de altura de salto del jugador
     * @param survival estadística de vida máxima del jugador
     * @throws DOPOSoccerException si alguna estadística está fuera del rango permitido
     */
    public void setStats(int speed, int kick, int jump, int survival) throws DOPOSoccerException{
        //primero se validan las 4 estadisticas
        checkLevel("speed", speed);
        checkLevel("kick", kick);
        checkLevel("jump", jump);
        checkLevel("survival", survival);

        this.speed = speed;
        this.kick = kick;
        this.jump = jump;
        this.survival = survival;
        this.health = getMaxHealth();   //la vida maxima depende de survival
    }

    /**
     * Sube en un punto la estadística dada del jugador, hasta un
     * máximo de diez puntos, y si la estadística subida es survival
     * también ajusta la vida al nuevo máximo.
     * @param stat nombre de la estadística a subir, speed, jump, kick o survival
     */
    public void upgradeStat(String stat) throws DOPOSoccerException {
        if(stat.equals("speed")){
            if (speed >= Profile.MAX_LEVEL){
                throw DOPOSoccerException.maxLevelReached(stat, Profile.MAX_LEVEL);
            }
            speed++;
        } else if(stat.equals("jump")){
            if (jump >= Profile.MAX_LEVEL){
                throw DOPOSoccerException.maxLevelReached(stat, Profile.MAX_LEVEL);
            }
            jump++;
        } else if(stat.equals("kick")){
            if (kick >= Profile.MAX_LEVEL){
                throw DOPOSoccerException.maxLevelReached(stat, Profile.MAX_LEVEL);
            }
            kick++;
        } else if(stat.equals("survival")){
            if (survival >= Profile.MAX_LEVEL){
                throw DOPOSoccerException.maxLevelReached(stat, Profile.MAX_LEVEL);
            }
            survival++;
            health = getMaxHealth();
        } else {
            throw new DOPOSoccerException("Estadistica desconocida para el jugador: '" + stat + "'. Las validas son speed, kick, jump y survival.");
        }
    }

    /**
     * Suma la cantidad dada a los puntos acumulados del jugador.
     * @param amount cantidad de puntos a sumar
     * @throws DOPOSoccerException si la cantidad es negativa
     */
    public void addPoints(int amount) throws DOPOSoccerException{
        if (amount < 0) {
            throw new DOPOSoccerException("No se pueden sumar puntos negativos al jugador. Cantidad recibida: " + amount + ".");
        }
        this.points += amount;
    }

    /**
     * Entrega los puntos acumulados por el jugador.
     * @return los puntos acumulados del jugador
     */
    public int getPoints(){
        return points;
    }

    /**
     * Entrega la posición horizontal del jugador.
     * @return la posición horizontal del jugador
     */
    public double getPositionX() {
        return positionX;
    }

    /**
     * Entrega la posición vertical del jugador.
     * @return la posición vertical del jugador
     */
    public double getPositionY() {
        return positionY;
    }

    /**
     * Entrega la vida actual del jugador.
     * @return la vida actual del jugador
     */
    public double getHealth() {
        return health;
    }

    /**
     * Entrega el personaje del jugador.
     * @return el personaje del jugador
     */
    public Character getCharacter() {
        return character;
    }

    /**
     * Fija el personaje del jugador.
     * @param character personaje a asignar al jugador
     */
    public void setCharacter(Character character) {
        this.character = character;
    }

    /**
     * Entrega la barra de poder del jugador.
     * @return la barra de poder del jugador
     */
    public PowerBar getPowerBar() {
        return powerBar;
    }

    /**
     * Indica si el jugador tiene el aura de su poder activa en este momento.
     * @return verdadero si el aura del poder está activa, falso en caso contrario
     */
    public boolean isAuraActive() {
        return auraActive;
    }

    /**
     * Cambia el estado del aura del poder del jugador.
     * @param auraActive verdadero para activar el aura del poder, falso para apagarla
     */
    public void setAuraActive(boolean auraActive){
        this.auraActive = auraActive;
    }

    /**
     * Entrega la estadística de velocidad de desplazamiento del jugador.
     * @return la estadística speed del jugador
     */
    public int getSpeed() {
        return speed;
    }

    /**
     * Entrega la estadística de altura de salto del jugador.
     * @return la estadística jump del jugador
     */
    public int getJump() {
        return jump;
    }

    /**
     * Entrega la estadística de daño de patada del jugador.
     * @return la estadística kick del jugador
     */
    public int getKick() {
        return kick;
    }

    /**
     * Entrega la estadística de vida máxima del jugador.
     * @return la estadística survival del jugador
     */
    public int getSurvival() {
        return survival;
    }
    //para el salto
    /**
     * Entrega la velocidad vertical actual del jugador, usada por la
     * física de su salto.
     * @return la velocidad vertical del jugador
     */
    public double getVelocity(){
        return velocityY;
    }

    /**
     * Fija la posición horizontal del jugador.
     * @param positionX nueva posición horizontal del jugador
     */
    public void setPositionX(double positionX) {
        this.positionX = positionX;
    }

    /**
     * Fija la posición vertical del jugador.
     * @param positionY nueva posición vertical del jugador
     */
    public void setPositionY(double positionY) {
        this.positionY = positionY;
    }
}
