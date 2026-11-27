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
        this.speed = 1;
        this.kick = 1;
        this.jump = 1;
        this.survival = 1;
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
     * Suma la cantidad dada a los puntos acumulados del perfil.
     * @param amount cantidad de puntos a sumar
     */
    public void addPoints(int amount) {
        points += amount;
    }

    /**
     * Entrega el nivel actual de la estadística dada.
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return el nivel actual de esa estadística
     */
    private int levelOf(String stat) {
        if (stat.equals("speed")) {
            return speed;
        } else if (stat.equals("kick")) {
            return kick;
        } else if (stat.equals("jump")) {
            return jump;
        } else {
            return survival;
        }
    }

    /**
     * Calcula el precio en puntos del próximo nivel de la estadística
     * dada, que arranca en quinientos puntos y se duplica con cada nivel.
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return el precio del próximo nivel de esa estadística
     */
    public int upgradeCost(String stat) {
        return 500 * (int) Math.pow(2, levelOf(stat) - 1);
    }

    /**
     * Indica si la estadística dada se puede mejorar: si no está en su
     * nivel máximo y el perfil tiene puntos suficientes para pagar el
     * próximo nivel.
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return verdadero si la estadística se puede mejorar, falso en caso contrario
     */
    public boolean canUpgrade(String stat) {
        return levelOf(stat) < 10 && points >= upgradeCost(stat);
    }

    /**
     * Indica si la estadística dada ya llegó a su nivel máximo.
     * @param stat nombre de la estadística, speed, kick, jump o survival
     * @return verdadero si la estadística está en su nivel máximo, falso en caso contrario
     */
    public boolean isMax(String stat) {
        return levelOf(stat) >= 10;
    }

    /**
     * Sube un nivel la estadística dada y descuenta del perfil el
     * precio de ese nivel, siempre que la estadística se pueda mejorar.
     * @param stat nombre de la estadística a subir, speed, kick, jump o survival
     */
    public void upgrade(String stat) {
        if (!canUpgrade(stat)) {
            return;
        }
        points -= upgradeCost(stat);
        if (stat.equals("speed")) {
            speed++;
        } else if (stat.equals("kick")) {
            kick++;
        } else if (stat.equals("jump")) {
            jump++;
        } else {
            survival++;
        }
    }
}