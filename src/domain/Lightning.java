package domain;

/**
 * Tiro de poder del personaje de Camerún.
 * El balón sale recto, cubierto de electricidad, apuntado a la boca del
 * arco rival y sin caer por la gravedad ni frenarse por la fricción
 * hasta que rebota contra una pared. Si golpea al rival le aplica el
 * daño del tiro y lo deja electrocutado: no puede saltar y se mueve a
 * la mitad de su velocidad mientras dura el efecto.
 */
public class Lightning extends Power {

    //rapidez con la que sale el balón en el tiro recto hacia el arco rival
    private static final double POWER_SHOT_SPEED = 16;

    //distancia mínima al punto de mira para poder calcular la dirección del tiro
    private static final double MIN_AIM_DISTANCE = 1;

    //nombre del efecto que queda sobre el rival golpeado
    public static final String EFFECT = "electrocuted";

    /**
     * Crea el tiro de poder Rayo con su daño base y la duración del
     * efecto de electrocución que deja sobre el rival.
     */
    public Lightning() {
        super("Lightning", 60, 3.0);
    }

    /**
     * Lanza el balón en línea recta, a alta velocidad, apuntado a la boca
     * del arco contrario al del jugador que ejecuta el tiro, y lo deja en
     * vuelo recto para que no caiga ni se frene mientras viaja. Si el
     * balón ya está sobre el punto de mira, sale recto y horizontal hacia
     * ese mismo arco.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que activa y ejecuta el tiro de poder
     */
    @Override
    public void shoot(Match match, Player attacker) {
        // el jugador izquierdo ataca el arco derecho y el derecho ataca el izquierdo
        Goal target;
        int shotDir;
        if (attacker == match.getLeftPlayer()) {
            target = match.getRightGoal();
            shotDir = 1;
        } else {
            target = match.getLeftGoal();
            shotDir = -1;
        }

        // punto de mira: el borde interior del arco, a media altura
        double aimX;
        if (shotDir == 1) {
            aimX = target.getPositionX();
        } else {
            aimX = target.getPositionX() + target.getWidth();
        }
        double aimY = target.getPositionY() + target.getHeight() / 2.0;

        // distancia del balón al punto de mira
        Ball ball = match.getBall();
        double dx = aimX - ball.getPositionX();
        double dy = aimY - ball.getPositionY();
        double distance = Math.sqrt(dx * dx + dy * dy);

        // si el balón ya está sobre el punto de mira sale horizontal, para no dividir por cero
        if (distance < MIN_AIM_DISTANCE) {
            ball.setVelocityX(shotDir * POWER_SHOT_SPEED);
            ball.setVelocityY(0);
        } else {
            ball.setVelocityX(dx / distance * POWER_SHOT_SPEED);
            ball.setVelocityY(dy / distance * POWER_SHOT_SPEED);
        }

        // mientras viaja no lo afectan la gravedad ni la fricción
        ball.setStraightFlight(true);
    }

    /**
     * Deja al rival golpeado electrocutado durante la duración propia
     * de este tiro de poder. Mientras dura no puede saltar y se mueve a
     * la mitad de su velocidad.
     * @param match partido en el que se ejecuta el tiro de poder
     * @param attacker jugador que ejecutó el tiro de poder
     * @param opponent jugador rival que fue golpeado por el tiro de poder
     */
    @Override
    protected void applyHitEffect(Match match, Player attacker, Player opponent) {
        opponent.applyEffect(EFFECT, getEffectDuration());
    }
}
