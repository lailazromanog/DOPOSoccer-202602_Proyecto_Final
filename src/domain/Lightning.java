package domain;

/**
 * Tiro de poder del personaje de Camerún.
 * El balón sale cubierto de electricidad y, si golpea al rival, lo deja
 * electrocutado: no puede saltar y se mueve a la mitad de su velocidad
 * mientras dura el efecto.
 */
public class Lightning extends Power {

    /**
     * Crea el tiro de poder Rayo con su daño base y la duración del
     * efecto de electrocución que deja sobre el rival.
     */
    public Lightning() {
        super("Lightning", 60, 3.0);
    }

    /**
     * Aplica daño al rival del atacante y lo deja electrocutado durante
     * la duración propia de este tiro de poder.
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

        opponent.applyEffect("electrocuted", getEffectDuration());
    }
}
