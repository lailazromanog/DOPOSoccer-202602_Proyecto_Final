package domain;

/**
 * Tiro de poder del personaje de Corea del Sur.
 * El balón sale recto y horizontal a alta velocidad, y si golpea al
 * rival lo lanza hacia su propio arco mediante un empuje horizontal.
 */
public class BlueAura extends Power {

    private static final double POWER_SHOT_SPEED = 20; // tiro recto y horizontal, a alta velocidad
    private static final double KNOCKBACK = 20;        // empuje inicial del rival (se desliza y frena)

    /**
     * Crea el tiro de poder Aura Azul con su daño base y la duración
     * del efecto de empuje que deja sobre el rival.
     */
    public BlueAura() {
        super("Blue Aura", 40, 1.5);
    }

    /**
     * Lanza el balón en línea recta a alta velocidad hacia el arco
     * contrario y, si golpea al rival, le aplica daño y lo empuja hacia
     * su propio arco mediante un empuje horizontal que se frena con el tiempo.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    @Override
    public void activate(Match match, Player attacker) {
        Player opponent;
        int shotDir;   // +1 dispara a la derecha, -1 a la izquierda
        if(attacker == match.getLeftPlayer()){
            opponent = match.getRightPlayer();
            shotDir = 1;   // J1 dispara hacia el arco derecho
        } else {
            opponent = match.getLeftPlayer();
            shotDir = -1;  // J2 dispara hacia el arco izquierdo
        }

        // tiro de poder: el balon sale recto y horizontal a alta velocidad
        Ball ball = match.getBall();
        ball.setVelocityX(shotDir * POWER_SHOT_SPEED);
        ball.setVelocityY(0);

        // daño al rival
        double damage = getDamageTiro() * attacker.getMultiplicadorKick();
        opponent.takeDamage(damage);

        // empuje fuerte: el rival es lanzado hacia su propio arco
        opponent.applyKnockback(shotDir * KNOCKBACK);
    }
}