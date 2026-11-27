package domain;
import java.io.Serializable;

/**
 * Representa el tiro de poder de un personaje.
 * Cada subclase concreta define el efecto propio que produce sobre el
 * rival cuando se activa, siguiendo el esquema de personaje y poder
 * descrito en los requisitos del juego.
 */
public abstract class Power implements Serializable{
    private String name;
    private int damageTiro;
    private boolean active;
    private double effectDuration;

    /**
     * Crea un tiro de poder con su nombre, su daño base y la duración
     * del efecto que produce sobre el rival cuando lo golpea.
     * @param name nombre del tiro de poder
     * @param damageTiro daño base del tiro, antes de aplicar el multiplicador de la estadística kick
     * @param effectDuration duración en segundos del efecto que deja en el rival al golpearlo
     */
    public Power(String name, int damageTiro, double effectDuration) {
        this.name = name;
        this.damageTiro = damageTiro;
        this.active = false;
        this.effectDuration = effectDuration;
    }

    /**
     * Entrega el nombre del tiro de poder.
     * @return el nombre del tiro de poder
     */
    public String getName() {
        return name;
    }

    /**
     * Entrega el daño base del tiro de poder, sin aplicar todavía el
     * multiplicador de la estadística kick del jugador que lo ejecuta.
     * @return el daño base del tiro
     */
    public int getDamageTiro() {
        return damageTiro;
    }

    /**
     * Indica si el tiro de poder se encuentra activo en este momento.
     * @return verdadero si el tiro de poder está activo, falso en caso contrario
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Entrega la duración en segundos del efecto que este tiro de poder
     * deja sobre el rival cuando lo golpea.
     * @return la duración del efecto en segundos
     */
    public double getEffectDuration() {
        return effectDuration;
    }

    /**
     * Cambia el estado de actividad del tiro de poder.
     * @param active verdadero para marcar el tiro de poder como activo, falso para marcarlo como inactivo
     */
    public void setActive(boolean active){
        this.active = active;
    }

    /**
     * Ejecuta el efecto propio de este tiro de poder sobre el rival del
     * atacante dentro del partido en curso. Cada subclase concreta define
     * el daño adicional, el empuje o el estado que aplica al oponente.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    public abstract void activate(Match match, Player attacker);
}