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

    public Profile() {
        this.points = 0;
        this.speed = 1;
        this.kick = 1;
        this.jump = 1;
        this.survival = 1;
    }

    public int getPoints(){ 
        return points; 
    }

    public int getSpeed(){ 
        return speed; 
    }

    public int getKick(){ 
        return kick; 
    }

    public int getJump(){ 
        return jump; 
    }

    public int getSurvival(){ 
        return survival; 
    }

    public void addPoints(int amount) {
        points += amount;
    }

    /** Nivel actual de la estadistica pedida. */
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

    /** Precio del proximo nivel de la estadistica: 500, 1000, 2000... (se duplica). */
    public int upgradeCost(String stat) {
        return 500 * (int) Math.pow(2, levelOf(stat) - 1);
    }

    /** True si la estadistica no esta al maximo (10) y alcanzan los puntos. */
    public boolean canUpgrade(String stat) {
        return levelOf(stat) < 10 && points >= upgradeCost(stat);
    }

    /** True si la estadistica ya esta en el nivel maximo. */
    public boolean isMax(String stat) {
        return levelOf(stat) >= 10;
    }

    /** Sube la estadistica un nivel y descuenta los puntos (si se puede). */
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