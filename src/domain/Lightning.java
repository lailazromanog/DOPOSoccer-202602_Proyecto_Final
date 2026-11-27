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
     * Deja el balón con la trayectoria que ya traía al ser tocado por el
     * jugador con el aura activa. La trayectoria propia de este tiro se
     * define cuando se trabaje este poder.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    @Override
    public void shoot(Match match, Player attacker) {
        // por ahora el balón sigue con la trayectoria que traía
    }

    /**
     * Deja al rival golpeado electrocutado durante la duración propia
     * de este tiro de poder.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que ejecutó el tiro de poder
     * @param opponent jugador rival que fue golpeado por el tiro de poder
     */
    @Override
    protected void applyHitEffect(Match match, Player attacker, Player opponent) {
        opponent.applyEffect("electrocuted", getEffectDuration());
    }
}
