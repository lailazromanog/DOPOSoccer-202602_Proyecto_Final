package domain;

/**
 * Tiro de poder del personaje de Nigeria.
 * El balón sale como un remolino que rebota rápidamente a ras de suelo
 * y, si golpea al rival, lo levanta del suelo y lo deja inconsciente:
 * no puede moverse, saltar ni patear mientras dura el efecto.
 */
public class Tornado extends Power {
    /**
     * Crea el tiro de poder Tornado con su daño base y la duración del
     * efecto de inconsciencia que deja sobre el rival.
     */
    public Tornado() {
        super("Tornado", 40, 3);
    }

    /**
     * Aplica daño al rival del atacante, lo lanza hacia arriba y lo deja
     * inconsciente durante la duración propia de este tiro de poder.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    @Override
    public void activate(Match match, Player attacker) {
        Player opponent;
        if(attacker == match.getLeftPlayer()){
            opponent = match.getRightPlayer();
        } else {
            opponent = match.getLeftPlayer();
        }

        double damage = getDamageTiro() * attacker.getMultiplicadorKick();
        opponent.takeDamage(damage);

        opponent.launchUp(15);   //el tornado lo levanta del piso
        opponent.applyEffect("unconscious", getEffectDuration());
    }
}
