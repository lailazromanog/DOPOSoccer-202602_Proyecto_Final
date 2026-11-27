package domain;

import java.io.Serializable;

/**
 * Progreso persistente de un jugador (J1 o J2): puntos acumulados y niveles de
 * estadisticas. Se conserva entre partidos; las estadisticas aplican a cualquier
 * personaje que el jugador elija.
 *
 * Reglas:
 *  - cada estadistica va de 1 a 10.
 *  - el precio del proximo nivel arranca en 500 y se duplica: 500, 1000, 2000
 *  - se ganan 100 puntos por victoria y 25 por derrota.
 */
public class Profile implements Serializable {

    // Nivel minimo que puede tener una estadistica
    public static final int MIN_LEVEL = 1;
    // Nivel maximo que puede tener una estadistica
    public static final int MAX_LEVEL = 10;
    // Precio en puntos del primer nivel de mejora
    public static final int BASE_COST = 500;
    // Vida base de un jugador con la estadistica survival en su nivel minimo
    public static final int BASE_HEALTH = 100;
    // Dano base de una patada normal con la estadistica kick en su nivel minimo
    public static final int BASE_KICK_DAMAGE = 10;
    // Fraccion que suma cada nivel de una estadistica por encima del primero
    public static final double BONUS_PER_LEVEL = 0.10;

    private int points;
    private int speed;
    private int kick;
    private int jump;
    private int survival;

    /**
     * Crea un perfil nuevo con sus puntos en cero y todas sus
     * estadísticas en el nivel mínimo.
     */
    public Profile() {
        this.points = 0;
        this.speed = MIN_LEVEL;
        this.kick = MIN_LEVEL;
        this.jump = MIN_LEVEL;
        this.survival = MIN_LEVEL;
    }

    /**
     * Entrega los puntos acumulados del perfil.
     * @return los puntos acumulados del perfil
     */
    public int getPoints(){
        return points;
    }

    /**
     * Entrega el nivel de la estadística de velocidad del perfil.
     * @return el nivel de la estadística speed
     */
    public int getSpeed(){
        return speed;
    }

    /**
     * Entrega el nivel de la estadística de daño de patada del perfil.
     * @return el nivel de la estadística kick
     */
    public int getKick(){
        return kick;
    }

    /**
     * Entrega el nivel de la estadística de altura de salto del perfil.
     * @return el nivel de la estadística jump
     */
    public int getJump(){
        return jump;
    }

    /**
     * Entrega el nivel de la estadística de vida máxima del perfil.
     * @return el nivel de la estadística survival
     */
    public int getSurvival(){
        return survival;
    }

    /**
     * Calcula la vida máxima que da el nivel actual de la estadística
     * survival, partiendo de la vida base y sumando un diez por ciento
     * por cada nivel por encima del primero.
     * @return la vida máxima del jugador con su nivel actual de survival
     */
    public double getMaxHealth(){
        return BASE_HEALTH * (1 + BONUS_PER_LEVEL *(survival - MIN_LEVEL));
    }

    /**
     * Calcula el multiplicador de daño que da el nivel actual de la
     * estadística kick, que suma un diez por ciento por cada nivel por
     * encima del primero.
     * @return el multiplicador de daño según el nivel actual de kick
     */
    public double getKickMultiplier(){
        return 1 + BONUS_PER_LEVEL * (kick - MIN_LEVEL);
    }

    /**
     * Calcula el daño de una patada normal con el nivel actual de la
     * estadística kick, multiplicando el daño base de la patada por el
     * multiplicador de daño.
     * @return el daño de una patada normal
     */
    public double getKickDamage(){
        return BASE_KICK_DAMAGE * getKickMultiplier();
    }

    /**
     * Calcula el porcentaje de mejora que da el nivel actual de la
     * estadística speed sobre la velocidad base.
     * @return el porcentaje de mejora de la velocidad, cero en el nivel mínimo
     */
    public int getSpeedBonusPercent(){
        return (int) Math.round(100 * BONUS_PER_LEVEL * (speed - MIN_LEVEL));
    }

    /**
     * Calcula el porcentaje de mejora que da el nivel actual de la
     * estadística jump sobre la altura de salto base.
     * @return el porcentaje de mejora del salto, cero en el nivel mínimo
     */
    public int getJumpBonusPercent(){
        return (int) Math.round(100 * BONUS_PER_LEVEL * (jump - MIN_LEVEL));
    }

    /**
     * Entrega el nivel actual de la estadistica dada
     * @param stat nombre de la estadistica, speed, kick, jump o survival
     * @throws DOPOSoccerException si el nombre es nulo o no corresponde a ninguna estadistica
     */
    public int getLevel(String stat) throws DOPOSoccerException{
        return levelOf(stat);
    }

    /**
     * Suma la cantidad dada a los puntos acumulados del perfil.
     * @param amount cantidad de puntos a sumar
     * @throws DOPOSoccerException si la cantidad es negativa
     */
    public void addPoints(int amount) throws DOPOSoccerException{
        if (amount < 0){
            throw new DOPOSoccerException("No se pueden sumar puntos negativos al perfil. Cantidad recibida: " + amount + ".");
        }
        points += amount;
    }

    /**
     * Entrega el nivel actual de la estadística dada.
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return el nivel actual de esa estadística
     * @throws DOPOSoccerException si el nombre es nulo o no corresponde a ninguna estadistica
     */
    private int levelOf(String stat) throws DOPOSoccerException {
        if (stat == null){
            throw new DOPOSoccerException("El nombre de la estadistica no puede ser nulo o no existe. Las estadisticas existentes son: speed, jump, kick o survival");
        }
        if (stat.equals("speed")) {
            return speed;
        } else if (stat.equals("kick")) {
            return kick;
        } else if (stat.equals("jump")) {
            return jump;
        } else if (stat.equals("survival")) {
            return survival;
        } else {
            throw new DOPOSoccerException("Estadistica desconocida: '" + stat + "'. Las validas son speed, kick, jump y survival.");
        }
    }

    /**
     * Calcula el precio en puntos del próximo nivel de la estadística
     * dada, que arranca en quinientos puntos y se duplica con cada nivel.
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return el precio del próximo nivel de esa estadística
     * @throws DOPOSoccerException si el nombre no corresponde a ninguna estadística
     */
    public int upgradeCost(String stat) throws DOPOSoccerException {
        int level = levelOf(stat);
        int cost = BASE_COST;
        // se duplica una vez por cada nivel ya alcanzado por encima del primero
        for (int i = MIN_LEVEL; i < level; i++){
            cost = cost * 2;
        }
        return cost;
    }

    /**
     * Indica si la estadística dada se puede mejorar: si no está en su
     * nivel máximo y el perfil tiene puntos suficientes para pagar el
     * próximo nivel.
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return verdadero si la estadística se puede mejorar, falso en caso contrario
     * @throws DOPOSoccerException si el nombre no corresponde a ninguna estadística
     */
    public boolean canUpgrade(String stat) throws DOPOSoccerException {
        return levelOf(stat) < MAX_LEVEL && points >= upgradeCost(stat);
    }

    /**
     * Indica si la estadística dada ya llegó a su nivel máximo.
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return verdadero si la estadística está en su nivel máximo, falso en caso contrario
     * @throws DOPOSoccerException si el nombre no corresponde a ninguna estadística
     */
    public boolean isMax(String stat) throws DOPOSoccerException {
        return levelOf(stat) >= MAX_LEVEL;
    }

    /**
     * Sube un nivel la estadística dada y descuenta del perfil el
     * precio de ese nivel, siempre que la estadística se pueda mejorar.
     * @param stat nombre de la estadística a subir, speed, kick, jump o survival
     * @throws DOPOSoccerException si el nombre no corresponde a ninguna estadística, si la estadística ya está en su nivel máximo o si los puntos no son suficientes
     */
    public void upgrade(String stat) throws DOPOSoccerException {
        if (isMax(stat)){
              throw DOPOSoccerException.maxLevelReached(stat, MAX_LEVEL);
        }
        int cost = upgradeCost(stat);
        if (points < cost){
            throw new DOPOSoccerException("Puntos insuficientes para mejorar '" + stat + "'. Cuesta " + cost + " puntos y tienes " + points + ". Faltan "+ (cost-points)+ ".");
        }
        points -= cost;
        if (stat.equals("speed")) {
            speed++;
        } else if (stat.equals("kick")) {
            kick++;
        } else if (stat.equals("jump")) {
            jump++;
        } else if (stat.equals("survival")) {
            survival++;
        } else {
            throw new DOPOSoccerException("Estadistica desconocida: '" + stat + "'. Las validas son speed, kick, jump y survival.");
        }
    }
}