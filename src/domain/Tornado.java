package domain;

/**
 * Tiro de poder del personaje de Nigeria.
 * El balón sale como un remolino que rebota rápidamente a ras de suelo
 * y, si golpea al rival, lo levanta del suelo y lo deja inconsciente:
 * no puede moverse, saltar ni patear mientras dura el efecto.
 */
public class Tornado extends Power {

    // velocidad horizontal con la que sale el balon convertido en remolino
    private static final double TORNADO_SPEED = 16;

    
    /**
     * Crea el tiro de poder Tornado con su daño base y la duración del
     * efecto de inconsciencia que deja sobre el rival.
     */
    public Tornado() {
        super("Tornado", 40, 3);
    }


    /**
     * Lanza el balón a ras de suelo, a alta velocidad horizontal y sin
     * impulso vertical, para que la gravedad y el rebote normales del
     * balón lo hagan rebotar rápidamente contra el piso mientras avanza,
     * como un remolino.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    @Override
    public void shoot(Match match, Player attacker) {
        int shotDir;
        if (attacker == match.getLeftPlayer()) {
            shotDir = 1;
        } else {
            shotDir = -1;
        }

        Ball ball = match.getBall();
        ball.setVelocityX(shotDir * TORNADO_SPEED);
        ball.setVelocityY(0);
    }

    /**
     * Lanza hacia arriba al rival golpeado y lo deja inconsciente
     * durante la duración propia de este tiro de poder.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que ejecutó el tiro de poder
     * @param opponent jugador rival que fue golpeado por el tiro de poder
     */
    @Override
    protected void applyHitEffect(Match match, Player attacker, Player opponent) {
        opponent.launchUp(15);   // el tornado lo levanta del piso
        opponent.applyEffect("unconscious", getEffectDuration());
    }
}
