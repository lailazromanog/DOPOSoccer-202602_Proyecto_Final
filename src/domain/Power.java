package domain;
import java.io.Serializable;

/**
 * Representa el tiro de poder de un personaje.
 * Cada subclase concreta define el efecto propio que produce sobre el
 * rival cuando se activa, siguiendo el esquema de personaje y poder
 * descrito en los requisitos del juego.
 * Todo tiro de poder ocurre en dos momentos. Primero el disparo,
 * cuando el jugador con el aura activa toca el balón y el poder define
 * cómo sale el balón. Después el impacto, solo si el balón en vuelo
 * golpea al rival, y ahí se aplican el daño y el efecto. Si el balón
 * entra al arco sin tocar al rival, no hay daño ni efecto.
 *
 * Punto de extensión: para agregar un tiro de poder nuevo basta con
 * crear una subclase propia que defina su daño base, la duración de
 * su efecto, la forma en que sale el balón y el efecto que deja en el
 * rival, y registrarla en la fábrica de personajes. El cálculo del
 * daño ya lo hace esta clase para todos los poderes.
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
    public void setActive(boolean active) {
        this.active = active;
    }
    /**
     * Calcula el daño real que este tiro de poder le hace al rival,
     * multiplicando su daño base por el multiplicador de la estadística
     * kick del jugador que lo ejecuta.
     * @param attacker jugador que ejecutó el tiro de poder
     * @return el daño real que recibe el rival
     */
    public double calculateDamage(Player attacker) {
        double multiplier = attacker.getMultiplicadorKick();
        double damage = damageTiro * multiplier;
        return damage;
    }

    /**
     * Aplica el impacto de este tiro de poder cuando el balón golpea al
     * rival: primero le resta el daño real del tiro y después le deja el
     * efecto propio del poder, que reemplaza a cualquier efecto que el
     * rival tuviera antes.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que ejecutó el tiro de poder
     * @param opponent jugador rival que fue golpeado por el tiro de poder
     */
    public void hit(Match match, Player attacker, Player opponent) {
        // el daño es el mismo calculo para todos los poderes
        double damage = calculateDamage(attacker);
        try {
            opponent.takeDamage(damage);
        } catch (DOPOSoccerException e) {
            // el daño sale del dano base y del multiplicador, ambos positivos, asi que no deberia ocurrir el error
            throw new IllegalStateException("Error interno al aplicar el dano del tiro de poder: " + e.getMessage(), e);
        }

        // el efecto lo define cada poder concreto
        applyHitEffect(match, attacker, opponent);
    }

    /**
     * Define la forma en que sale el balón cuando el jugador con el aura
     * activa lo toca. Cada subclase concreta fija la trayectoria propia
     * de su tiro, sin aplicar todavía ningún daño ni efecto al rival.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    public abstract void shoot(Match match, Player attacker);

    /**
     * Deja sobre el rival el efecto propio de este tiro de poder, una
     * vez que el balón ya lo golpeó y ya recibió el daño. Cada subclase
     * concreta define el estado, el empuje o el lanzamiento que aplica.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que ejecutó el tiro de poder
     * @param opponent jugador rival que fue golpeado por el tiro de poder
     */
    protected abstract void applyHitEffect(Match match, Player attacker, Player opponent);
}
