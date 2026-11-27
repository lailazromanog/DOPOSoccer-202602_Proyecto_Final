package domain;

/**
 * 
 * Tiro de poder del personaje de Corea del Sur.
 * El balón sale recto y horizontal a alta velocidad, sin caer por la
 * gravedad ni frenarse por la fricción, hasta que rebota contra una
 * pared. Si golpea al rival le aplica el daño del tiro y lo deja
 * empujado hacia su propio arco durante la duración del efecto.
 */

public class BlueAura extends Power {

    //velocidad horizontal con la que sale el balón en el tiro recto
    private static final double POWER_SHOT_SPEED = 20;

    //velocidad inicial del empuje que recibe el rival, se va frenando sola
    private static final double KNOCKBACK = 12;

    //nombre del efecto que queda sobre el rival golpeado
    public static final String EFFECT = "pushed";

    /**
     * Crea el tiro de poder Aura Azul con su daño base y la duración
     * del efecto de empuje que deja sobre el rival.
     */
    public BlueAura() {
        super("Blue Aura", 40, 1.5);
    }

        /**
     * Lanza el balón en línea recta y horizontal, a alta velocidad,
     * hacia el arco contrario al del jugador que ejecuta el tiro, y lo
     * deja en vuelo recto para que no caiga ni se frene mientras viaja.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    @Override
    public void shoot(Match match, Player attacker) {
        // el jugador izquierdo dispara hacia la derecha y el derecho hacia la izquierda
        int shotDir;
        if (attacker == match.getLeftPlayer()) {
            shotDir = 1;
        } else {
            shotDir = -1;
        }

        // el balón sale recto y horizontal, sin velocidad vertical
        Ball ball = match.getBall();
        ball.setVelocityX(shotDir * POWER_SHOT_SPEED);
        ball.setVelocityY(0);

        // mientras viaja no lo afectan la gravedad ni la fricción
        ball.setStraightFlight(true);
    }

    /**
     * Deja al rival golpeado con el efecto de empuje durante la duración
     * propia de este tiro, y lo lanza deslizándose hacia su propio arco.
     * El rival puede seguir moviéndose mientras dura el empuje.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que ejecutó el tiro de poder
     * @param opponent jugador rival que fue golpeado por el tiro de poder
     */
    @Override
    protected void applyHitEffect(Match match, Player attacker, Player opponent) {
        // el rival es empujado hacia su propio arco, que queda del lado contrario al atacante
        int pushDir;
        if (opponent == match.getRightPlayer()) {
            pushDir = 1;
        } else {
            pushDir = -1;
        }

        // primero el efecto, porque un efecto nuevo reemplaza al anterior y borra cualquier empuje viejo
        opponent.applyEffect(EFFECT, getEffectDuration());
        opponent.applyKnockback(pushDir * KNOCKBACK);
    }
}